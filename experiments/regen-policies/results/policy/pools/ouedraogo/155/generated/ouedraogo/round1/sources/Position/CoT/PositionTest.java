package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;


import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * JUnit 4 test cases for {@link Position}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public methods (basic functionality)</li>
 *   <li>Edge cases and exception scenarios</li>
 *   <li>Equality, hashCode and toString contracts</li>
 * </ul>
 */
public class PositionTest {

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                       */
    /* ---------------------------------------------------------------------- */

    private static Num n(double v) {
        return DecimalNum.valueOf(v);
    }

    private static Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, n(price), n(amount));
    }

    private static Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, n(price), n(amount));
    }

    /* ---------------------------------------------------------------------- */
    /* 1. Constructors                                                       */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructor() {
        Position p = new Position(); // startingType = BUY
        assertTrue(p.isNew());
        assertEquals(TradeType.BUY, p.getStartingType());
        assertNull(p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(TradeType.SELL);
        assertTrue(p.isNew());
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStartingType() {
        new Position(null);
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Trade entry = buy(0, 10.0, 2.0);
        Trade exit = sell(5, 12.0, 2.0);
        Position p = new Position(entry, exit);
        assertFalse(p.isNew());
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithSameTradeTypesThrows() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit = buy(5, 12.0, 1.0); // both BUY -> illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithMismatchingCostModelThrows() {
        // create a custom cost model (different from entry's default)
        CostModel custom = new ZeroCostModel(); // ZeroCostModel is also the default,
                                                // but we will pass a different instance
        Trade entry = Trade.buyAt(0, n(10), n(1), custom);
        Trade exit = Trade.sellAt(5, n(12), n(1));
        // entry cost model != transactionCostModel argument (null -> default)
        new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    }

    /* ---------------------------------------------------------------------- */
    /* 2. operate() – creating entry and exit trades                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryWhenNew() {
        Position p = new Position();
        Trade t = p.operate(0, n(10), n(5));
        assertNotNull(t);
        assertEquals(TradeType.BUY, t.getType());
        assertEquals(t, p.getEntry());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testOperateCreatesExitWhenOpened() {
        Position p = new Position();
        // first call creates entry
        p.operate(0, n(10), n(5));
        // second call creates exit
        Trade exit = p.operate(3, n(12), n(5));
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexBeforeEntryThrows() {
        Position p = new Position();
        p.operate(5, n(10), n(1)); // entry at index 5
        // attempting to close before entry index
        p.operate(3, n(12), n(1));
    }

    @Test
    public void testOperateWithoutPriceAmountUsesNaN() {
        Position p = new Position();
        Trade t = p.operate(0); // uses NaN for price and amount
        assertNotNull(t);
        // price and amount are NaN – we only assert that a trade was created
        assertEquals(TradeType.BUY, t.getType());
    }

    /* ---------------------------------------------------------------------- */
    /* 3. State query methods (isNew, isOpened, isClosed)                    */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testStateTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0, n(10), n(1));
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(2, n(12), n(1));
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    /* ---------------------------------------------------------------------- */
    /* 4. Profit / loss calculations                                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testProfitForClosedBuyPosition() {
        Trade entry = buy(0, 10, 2);   // cost = 20
        Trade exit  = sell(5, 15, 2);  // revenue = 30
        Position p = new Position(entry, exit);
        // gross profit = 30 - 20 = 10, no transaction cost (ZeroCostModel)
        assertEquals(n(10), p.getGrossProfit());
        assertEquals(n(10), p.getProfit());
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitForClosedSellPosition() {
        Trade entry = sell(0, 15, 3);  // receive 45 (short)
        Trade exit  = buy(4, 10, 3);   // pay 30 to cover
        Position p = new Position(entry, exit);
        // For short, gross profit = entry value - exit value = 45 - 30 = 15
        // internal logic negates when entry.isSell()
        assertEquals(n(15), p.getGrossProfit());
        assertEquals(n(15), p.getProfit());
        assertTrue(p.hasProfit());
    }

    @Test
    public void testLossScenario() {
        Trade entry = buy(0, 20, 1);
        Trade exit  = sell(3, 15, 1);
        Position p = new Position(entry, exit);
        assertEquals(n(-5), p.getGrossProfit()); // loss
        assertEquals(n(-5), p.getProfit());
        assertTrue(p.hasLoss());
        assertFalse(p.hasProfit());
    }

    @Test
    public void testProfitWhenOpenPositionIsZero() {
        Position p = new Position();
        p.operate(0, n(10), n(1)); // only entry, still opened
        assertEquals(n(0), p.getProfit());
        assertEquals(n(0), p.getGrossProfit());
    }

    @Test
    public void testProfitWithFinalPriceForOpenPosition() {
        Position p = new Position();
        p.operate(0, n(10), n(2)); // entry BUY 10*2 = 20
        // final price 12, index 5
        Num profit = p.getProfit(5, n(12));
        // gross = (12*2) - 20 = 4, transaction cost = 0, holding cost = 0
        assertEquals(n(4), profit);
    }

    /* ---------------------------------------------------------------------- */
    /* 5. Gross return calculations                                           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGrossReturnBuyPosition() {
        Trade entry = buy(0, 10, 1);
        Trade exit  = sell(4, 12, 1);
        Position p = new Position(entry, exit);
        // (12 / 10) = 1.2
        assertEquals(n(1.2), p.getGrossReturn());
    }

    @Test
    public void testGrossReturnSellPosition() {
        Trade entry = sell(0, 12, 1);
        Trade exit  = buy(4, 10, 1);
        Position p = new Position(entry, exit);
        // for sell: ((exit/entry - 1) * -1) + 1 = ((10/12 -1)*-1)+1 = ( -0.1666 * -1)+1 = 1.1666...
        Num expected = n(10).dividedBy(n(12)).minus(n(1)).negate().plus(n(1));
        assertEquals(expected, p.getGrossReturn());
    }

    @Test
    public void testGrossReturnWhenOpenIsZero() {
        Position p = new Position();
        p.operate(0, n(10), n(1));
        assertEquals(n(0), p.getGrossReturn());
    }

    /* ---------------------------------------------------------------------- */
    /* 6. Equality, hashCode and toString                                    */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Trade e1 = buy(0, 10, 1);
        Trade x1 = sell(5, 12, 1);
        Position p1 = new Position(e1, x1);

        Trade e2 = buy(0, 10, 1);
        Trade x2 = sell(5, 12, 1);
        Position p2 = new Position(e2, x2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // different exit trade
        Trade x3 = sell(5, 13, 1);
        Position p3 = new Position(e1, x3);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, 10, 1);
        Trade exit = sell(2, 12, 1);
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    /* ---------------------------------------------------------------------- */
    /* 7. Position cost calculations (ZeroCostModel => zero)                 */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPositionCostZeroWhenClosed() {
        Trade entry = buy(0, 10, 1);
        Trade exit = sell(3, 12, 1);
        Position p = new Position(entry, exit);
        assertEquals(n(0), p.getPositionCost());
    }

    @Test
    public void testPositionCostZeroWhenOpen() {
        Position p = new Position();
        p.operate(0, n(10), n(1));
        assertEquals(n(0), p.getPositionCost(5));
    }

}
