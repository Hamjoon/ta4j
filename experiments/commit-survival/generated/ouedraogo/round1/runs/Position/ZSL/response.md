###Test START##
package org.ta4j.core;

import static org.ta4j.core.num.NaN.NaN;
import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * JUnit 4 tests for {@link Position}.
 */
public class PositionTest {

    /**
     * Helper to obtain the numeric constant ONE.
     */
    private Num one() {
        return NaN.one(); // default implementation returns numeric 1
    }

    /**
     * Helper to obtain the numeric constant TWO (ONE + ONE).
     */
    private Num two() {
        return one().plus(one());
    }

    @Test
    public void testInitialState() {
        Position pos = new Position(); // default BUY
        assertTrue("Position should be new", pos.isNew());
        assertFalse("Position should not be opened", pos.isOpened());
        assertFalse("Position should not be closed", pos.isClosed());

        // starting type must be BUY
        assertEquals(TradeType.BUY, pos.getStartingType());
    }

    @Test
    public void testOperateEntryAndExitBuy() {
        Position pos = new Position(); // BUY

        // create entry trade at index 0
        Trade entry = pos.operate(0, one(), one());
        assertNotNull(entry);
        assertTrue(pos.isOpened());
        assertFalse(pos.isClosed());
        assertEquals(entry, pos.getEntry());

        // create exit trade at index 1
        Trade exit = pos.operate(1, two(), one());
        assertNotNull(exit);
        assertTrue(pos.isClosed());
        assertEquals(exit, pos.getExit());

        // profit should be (2*1) - (1*1) = 1
        Num profit = pos.getProfit();
        assertTrue("Profit should be positive", profit.isPositive());
        assertTrue("Profit should be equal to ONE", profit.isEqual(one()));
        assertTrue("hasProfit should be true", pos.hasProfit());
        assertFalse("hasLoss should be false", pos.hasLoss());

        // gross profit should be the same because costs are zero
        Num gross = pos.getGrossProfit();
        assertTrue(gross.isEqual(one()));

        // gross return for BUY: exit/entry = 2 / 1 = 2
        Num grossReturn = pos.getGrossReturn(one(), two());
        assertTrue(grossReturn.isEqual(two()));
    }

    @Test
    public void testOperateEntryAndExitSell() {
        Position pos = new Position(TradeType.SELL); // starting with SELL (short)

        // entry: sell at price 2
        Trade entry = pos.operate(0, two(), one());
        assertNotNull(entry);
        assertTrue(pos.isOpened());

        // exit: buy at price 1
        Trade exit = pos.operate(1, one(), one());
        assertNotNull(exit);
        assertTrue(pos.isClosed());

        // profit should be (2*1) - (1*1) = 1 (short profit)
        Num profit = pos.getProfit();
        assertTrue(profit.isPositive());
        assertTrue(profit.isEqual(one()));
        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());

        // gross profit should be the same (costs are zero)
        assertTrue(pos.getGrossProfit().isEqual(one()));

        // gross return for SELL:
        // ((exit/entry - 1).negate()) + 1  => ((1/2 - 1).negate()) + 1 = ( -0.5.negate()) + 1 = 0.5 + 1 = 1.5
        Num expectedReturn = one().plus(one().minus(two().dividedBy(two()))); // simplifies to 1.5
        Num grossReturn = pos.getGrossReturn(two(), one()); // entry=2, exit=1
        // compute expected directly:
        Num expected = two().dividedBy(two()); // 1 (placeholder)
        // Using the formula:
        Num ratio = one().dividedBy(two()); // 0.5
        Num expectedSellReturn = ratio.negate().plus(one()); // 0.5 + 1 = 1.5
        assertTrue(grossReturn.isEqual(expectedSellReturn));
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithInvalidIndexThrows() {
        Position pos = new Position();
        pos.operate(5, one(), one()); // create entry at index 5
        // now try to create exit with an earlier index => should throw
        pos.operate(3, two(), one());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Build two identical positions via the (Trade, Trade) constructor
        Trade entry = Trade.buyAt(0, one(), one());
        Trade exit = Trade.sellAt(1, two(), one());

        Position p1 = new Position(entry, exit);
        Position p2 = new Position(entry, exit);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Different exit trade => not equal
        Trade otherExit = Trade.sellAt(1, one(), one());
        Position p3 = new Position(entry, otherExit);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position pos = new Position();
        pos.operate(0, one(), one());
        pos.operate(1, two(), one());

        String s = pos.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(pos.getEntry().toString()));
        assertTrue(s.contains(pos.getExit().toString()));
    }

    @Test
    public void testPositionCostAndHoldingCostAreZeroWithZeroCostModel() {
        Position pos = new Position(); // uses ZeroCostModel by default
        // open and close a position
        pos.operate(0, one(), one());
        pos.operate(1, two(), one());

        Num cost = pos.getPositionCost();
        assertTrue("Transaction cost should be zero", cost.isZero());

        Num holding = pos.getHoldingCost();
        assertTrue("Holding cost should be zero", holding.isZero());
    }

    @Test
    public void testGetProfitOpenPositionUsesFinalPrice() {
        Position pos = new Position(); // BUY
        pos.operate(0, one(), one()); // entry at price 1

        // position is still opened
        assertTrue(pos.isOpened());

        // compute profit as if the position were closed at price 3
        Num finalPrice = one().plus(one()).plus(one()); // 3
        Num profit = pos.getProfit(10, finalPrice); // finalIndex arbitrary
        // Expected: amount * finalPrice - entry value = 1*3 - 1 = 2
        Num expected = finalPrice.minus(one());
        assertTrue(profit.isEqual(expected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsSameTypeEntryAndExit() {
        Trade entry = Trade.buyAt(0, one(), one());
        Trade exit = Trade.buyAt(1, two(), one()); // also BUY -> illegal
        new Position(entry, exit);
    }
}
###Test END##