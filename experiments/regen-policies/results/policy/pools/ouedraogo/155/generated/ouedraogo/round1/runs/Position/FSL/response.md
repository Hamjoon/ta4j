###Test START##
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public constructors (including illegal‑argument cases)</li>
 *   <li>State queries {@code isNew()}, {@code isOpened()}, {@code isClosed()}</li>
 *   <li>Operation flow via {@code operate(...)} (creation of entry/exit trades)</li>
 *   <li>Profit / loss detection and gross return calculations for BUY and SELL positions</li>
 *   <li>Equality, hashCode and {@code toString()}</li>
 *   <li>Cost‑model related methods (using the provided {@link ZeroCostModel})</li>
 * </ul>
 *
 * The library supplies {@link DecimalNum} as a concrete {@link Num}
 * implementation – it is used to build realistic price/amount values.
 */
public class PositionTest {

    // -------------------------------------------------------------------------
    // Helper factory methods
    // -------------------------------------------------------------------------

    private static Num n(double value) {
        return DecimalNum.valueOf(value);
    }

    private static Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, n(price), n(amount));
    }

    private static Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, n(price), n(amount));
    }

    // -------------------------------------------------------------------------
    // Constructor tests
    // -------------------------------------------------------------------------

    @Test
    public void testDefaultConstructorSetsBuyStartingType() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithSellStartingType() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingTypeThrows() {
        new Position(null);
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit = sell(5, 12.0, 1.0);
        Position p = new Position(entry, exit);
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryExitSameTypeThrows() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit = buy(5, 12.0, 1.0); // also BUY → illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModelThrows() {
        // Use a non‑zero cost model for the entry trade only
        CostModel customCost = new ZeroCostModel(); // still zero but distinct instance
        Trade entry = Trade.buyAt(0, n(10.0), n(1.0), customCost);
        Trade exit = Trade.sellAt(5, n(12.0), n(1.0)); // default ZeroCostModel
        new Position(entry, exit, customCost, new ZeroCostModel());
    }

    // -------------------------------------------------------------------------
    // State‑transition tests (operate)
    // -------------------------------------------------------------------------

    @Test
    public void testOperateCreatesEntryWhenPositionIsNew() {
        Position p = new Position();
        Trade t = p.operate(3, n(10.0), n(2.0));
        assertNotNull(t);
        assertEquals(TradeType.BUY, t.getType());
        assertEquals(t, p.getEntry());
        assertNull(p.getExit());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testOperateCreatesExitWhenPositionIsOpened() {
        Position p = new Position();
        p.operate(1, n(10.0), n(1.0)); // entry
        Trade exit = p.operate(4, n(12.0), n(1.0)); // exit
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
        assertFalse(p.isOpened());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateIndexBeforeEntryThrows() {
        Position p = new Position();
        p.operate(5, n(10.0), n(1.0)); // entry at index 5
        p.operate(3, n(12.0), n(1.0)); // attempt exit before entry → exception
    }

    @Test
    public void testIsNewOpenedClosedTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0, n(10.0), n(1.0)); // entry
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(2, n(12.0), n(1.0)); // exit
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    // -------------------------------------------------------------------------
    // Profit / loss and return calculations
    // -------------------------------------------------------------------------

    @Test
    public void testGetProfitClosedBuyPositionPositive() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0)); // BUY entry at 10
        p.operate(2, n(13.0), n(1.0)); // SELL exit at 13
        Num profit = p.getProfit();
        assertTrue(profit.isPositive());
        assertEquals(n(3.0), profit); // 13 - 10 = 3 (zero transaction cost)
    }

    @Test
    public void testHasProfitAndHasLossForBuy() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0));
        p.operate(2, n(8.0), n(1.0)); // loss
        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());
    }

    @Test
    public void testGetProfitClosedSellPositionPositive() {
        // SELL at 12, BUY back at 9 → profit = 3
        Position p = new Position(TradeType.SELL);
        p.operate(0, n(12.0), n(1.0)); // SELL entry
        p.operate(3, n(9.0), n(1.0));  // BUY exit
        Num profit = p.getProfit();
        assertTrue(profit.isPositive());
        assertEquals(n(3.0), profit);
    }

    @Test
    public void testHasProfitAndHasLossForSell() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, n(12.0), n(1.0));
        p.operate(2, n(15.0), n(1.0)); // price moved up → loss
        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());
    }

    @Test
    public void testGetProfitOnOpenedPositionReturnsZero() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0)); // only entry
        Num profit = p.getProfit();
        assertTrue(profit.isZero());
    }

    @Test
    public void testGetGrossReturnClosedBuy() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0));
        p.operate(5, n(15.0), n(1.0));
        Num grossReturn = p.getGrossReturn();
        assertEquals(n(1.5), grossReturn); // 15 / 10
    }

    @Test
    public void testGetGrossReturnClosedSell() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, n(20.0), n(1.0)); // sell at 20
        p.operate(4, n(14.0), n(1.0)); // buy back at 14
        Num grossReturn = p.getGrossReturn();
        // For sell: ((14/20 - 1) * -1) + 1 = (0.7 -1)*-1 +1 = 0.3 +1 = 1.3
        assertEquals(n(1.3), grossReturn);
    }

    @Test
    public void testGetGrossReturnOnOpenedPositionIsZero() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0));
        assertTrue(p.getGrossReturn().isZero());
    }

    // -------------------------------------------------------------------------
    // Cost‑model related methods (ZeroCostModel yields zero)
    // -------------------------------------------------------------------------

    @Test
    public void testGetPositionCostClosedReturnsZero() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0));
        p.operate(2, n(12.0), n(1.0));
        Num cost = p.getPositionCost();
        assertTrue(cost.isZero());
    }

    @Test
    public void testGetPositionCostOpenReturnsZero() {
        Position p = new Position();
        p.operate(0, n(10.0), n(1.0));
        Num cost = p.getPositionCost(5);
        assertTrue(cost.isZero());
    }

    @Test
    public void testHoldingCostIsZeroWithZeroCostModel() {
        Position p = new Position();
        assertTrue(p.getHoldingCost().isZero());
        assertTrue(p.getHoldingCost(10).isZero());
    }

    // -------------------------------------------------------------------------
    // Equality, hashCode and toString
    // -------------------------------------------------------------------------

    @Test
    public void testEqualsAndHashCodeWithSameTrades() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit = sell(3, 12.0, 1.0);
        Position p1 = new Position(entry, exit);
        Position p2 = new Position(entry, exit);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsDifferentEntryOrExit() {
        Trade entry1 = buy(0, 10.0, 1.0);
        Trade exit1 = sell(3, 12.0, 1.0);
        Trade entry2 = buy(0, 11.0, 1.0); // different price
        Position p1 = new Position(entry1, exit1);
        Position p2 = new Position(entry2, exit1);
        assertNotEquals(p1, p2);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit = sell(2, 12.0, 1.0);
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(exit.toString()));
    }

    // -------------------------------------------------------------------------
    // Additional edge cases
    // -------------------------------------------------------------------------

    @Test
    public void testOperateWithNaNPriceAndAmountDoesNotBreakState() {
        Position p = new Position();
        Trade t1 = p.operate(0, DecimalNum.NaN, DecimalNum.NaN);
        assertNotNull(t1);
        assertTrue(p.isOpened());

        Trade t2 = p.operate(1, DecimalNum.NaN, DecimalNum.NaN);
        assertNotNull(t2);
        assertTrue(p.isClosed());
    }
}
###Test END##