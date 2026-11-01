package org.ta4j.core;
import org.ta4j.core.num.Num;


import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * JUnit‑4 test suite for {@link Position}.
 *
 * <p>The tests are written from the point of view of three independent
 * “experts”.  Each expert contributed one test case for every public method of
 * {@code Position}.  The suite therefore covers:
 *
 * <ul>
 *   <li>construction (including error scenarios)</li>
 *   <li>state queries (isNew, isOpened, isClosed, hasProfit, hasLoss)</li>
 *   <li>state transitions via {@code operate}</li>
 *   <li>profit / gross‑profit / gross‑return calculations (zero‑cost, NaN
 *       inputs)</li>
 *   <li>equality, hash‑code and {@code toString}</li>
 * </ul>
 *
 * <p>Only the APIs that appear in the supplied source code or the listed
 * dependency signatures are used – no external helpers, no mocking and no
 * subclassing of library classes.
 */
public class PositionTest {

    /** Trades used by several tests – created with {@code NaN} values so that
     *  the library’s internal {@code NumFactory} can still produce a zero
     *  instance when required. */
    private Trade buyTrade;
    private Trade sellTrade;
    private CostModel zeroCostModel;

    @Before
    public void setUp() {
        // Both trades share the same (zero) transaction cost model.
        zeroCostModel = new ZeroCostModel();

        // create a BUY trade at index 0 and a SELL trade at index 1.
        // price and amount are {@code NaN} – acceptable for the operations
        // we test (they never depend on the actual numeric value).
        buyTrade = Trade.buyAt(0, NaN, NaN, zeroCostModel);
        sellTrade = Trade.sellAt(1, NaN, NaN, zeroCostModel);
    }

    /* --------------------------------------------------------------------- *
     *  Construction / error handling
     * --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorHasBuyStartingType() {
        Position p = new Position();                // uses default BUY
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingTypeThrows() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedTradeTypesThrows() {
        // both trades are BUY – illegal
        Trade anotherBuy = Trade.buyAt(2, NaN, NaN, zeroCostModel);
        new Position(buyTrade, anotherBuy, zeroCostModel, zeroCostModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTradeAndCostModelMismatchThrows() {
        // Use a different cost model for the position than the one used by the trades
        CostModel otherCost = new ZeroCostModel(); // different instance
        new Position(buyTrade, sellTrade, otherCost, zeroCostModel);
    }

    /* --------------------------------------------------------------------- *
     *  State query methods
     * --------------------------------------------------------------------- */

    @Test
    public void testIsNewInitially() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testIsOpenedAfterEntryCreated() {
        Position p = new Position();
        p.operate(5); // creates entry trade
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testIsClosedAfterEntryAndExitCreated() {
        Position p = new Position();
        p.operate(3);               // entry
        p.operate(7, NaN, NaN);     // exit
        assertTrue(p.isClosed());
        assertFalse(p.isOpened());
    }

    @Test
    public void testHasProfitAndHasLossOnOpenedPosition() {
        Position p = new Position();
        p.operate(0);               // entry
        // still opened – profit is defined as zero, therefore neither profit nor loss
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    /* --------------------------------------------------------------------- *
     *  operate(..) – state transitions and error handling
     * --------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryAndExitAndPreservesTypes() {
        Position p = new Position(TradeType.BUY);
        Trade entry = p.operate(1, NaN, NaN);
        assertNotNull(entry);
        assertEquals(TradeType.BUY, entry.getType());
        assertEquals(entry, p.getEntry());

        Trade exit = p.operate(4, NaN, NaN);
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType()); // complement of BUY
        assertEquals(exit, p.getExit());

        // final state must be closed
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexLessThanEntryThrows() {
        Position p = new Position(TradeType.SELL);
        p.operate(10, NaN, NaN); // creates entry at index 10
        // next operate must have index >= 10; using 5 triggers exception
        p.operate(5, NaN, NaN);
    }

    /* --------------------------------------------------------------------- *
     *  Equality, hashCode and toString
     * --------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCodeWithSameTrades() {
        Position p1 = new Position(buyTrade, sellTrade, zeroCostModel, zeroCostModel);
        Position p2 = new Position(buyTrade, sellTrade, zeroCostModel, zeroCostModel);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsWithNullTrades() {
        Position p1 = new Position(); // both entry and exit are null
        Position p2 = new Position(); // same state
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position(buyTrade, sellTrade, zeroCostModel, zeroCostModel);
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(buyTrade.toString()));
        assertTrue(s.contains(sellTrade.toString()));
    }

    /* --------------------------------------------------------------------- *
     *  Profit / gross‑profit / gross‑return (zero‑cost, NaN inputs)
     * --------------------------------------------------------------------- */

    @Test
    public void testGetProfitReturnsZeroForOpenedPosition() {
        Position p = new Position();
        p.operate(2, NaN, NaN);               // entry
        // still opened – profit must be zero (the internal zero() uses the NumFactory)
        assertEquals(p.zero(), p.getProfit());
    }

    @Test
    public void testGetGrossProfitReturnsZeroForOpenedPosition() {
        Position p = new Position();
        p.operate(0, NaN, NaN);               // entry
        assertEquals(p.zero(), p.getGrossProfit());
    }

    @Test
    public void testGetGrossReturnReturnsZeroForOpenedPosition() {
        Position p = new Position();
        p.operate(0, NaN, NaN);               // entry
        assertEquals(p.zero(), p.getGrossReturn());
    }

    @Test
    public void testGetProfitForClosedPositionIsZeroWhenAllValuesAreNaN() {
        // Build a closed position using NaN values for price/amount.
        Position p = new Position(buyTrade, sellTrade, zeroCostModel, zeroCostModel);
        // Because all numeric values are NaN, the gross profit is NaN and the
        // zero‑cost model returns zero, therefore the final profit is zero.
        Num profit = p.getProfit();
        assertEquals(p.zero(), profit);
    }

    @Test
    public void testHasProfitAndHasLossReturnFalseForClosedNaNPosition() {
        Position p = new Position(buyTrade, sellTrade, zeroCostModel, zeroCostModel);
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }
}
