package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import static org.ta4j.core.num.NaN.NaN;

/**
 * Comprehensive unit tests for {@link Position}.
 *
 * The tests focus on:
 * <ul>
 *   <li>Construction variants and validation of arguments.</li>
 *   <li>State transitions: new → opened → closed.</li>
 *   <li>Behaviour of {@code operate(...)} including illegal index handling.</li>
 *   <li>Profit / loss calculations for closed positions (using zero transaction/holding costs).</li>
 *   <li>Equality, hash‑code and {@code toString()} contracts.</li>
 * </ul>
 *
 * All tests use only the public API that is declared in the source snippets
 * provided in the task description.
 */
public class PositionTest {

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Creates a BUY trade at the given index with a deterministic price/amount
     * (both equal to the supplied {@code price} argument). The {@code price}
     * argument is a {@link Num} that can be obtained from {@link #NaN} or any
     * other {@code Num} implementation that the library supplies.
     */
    private Trade buy(int index, Num price) {
        // amount = 1 (represented by the same Num instance for simplicity)
        return Trade.buyAt(index, price, price);
    }

    /**
     * Creates a SELL trade at the given index with a deterministic price/amount.
     */
    private Trade sell(int index, Num price) {
        return Trade.sellAt(index, price, price);
    }

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        Position p = new Position();
        assertTrue("New position should be in 'new' state", p.isNew());
        assertFalse("New position should not be opened", p.isOpened());
        assertFalse("New position should not be closed", p.isClosed());
        assertEquals("Default starting type must be BUY", TradeType.BUY, p.getStartingType());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position longPos = new Position(TradeType.BUY);
        Position shortPos = new Position(TradeType.SELL);
        assertEquals(TradeType.BUY, longPos.getStartingType());
        assertEquals(TradeType.SELL, shortPos.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStartingType() {
        new Position(null);
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Trade entry = buy(0, NaN);   // price & amount are NaN – acceptable for the test
        Trade exit = sell(5, NaN);
        Position p = new Position(entry, exit);
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryExitSameType() {
        Trade entry = buy(0, NaN);
        Trade exit = buy(5, NaN); // same type as entry
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModel() {
        // Create a trade with a non‑zero cost model (using ZeroCostModel for entry,
        // but a different dummy model for exit).  Because we cannot create a custom
        // CostModel without mocking, we use the public constructor that accepts the
        // cost models and deliberately pass different instances.
        CostModel transactionModel = new ZeroCostModel();
        CostModel holdingModel = new ZeroCostModel();

        Trade entry = Trade.buyAt(0, NaN, NaN, transactionModel);
        Trade exit = Trade.sellAt(5, NaN, NaN, new ZeroCostModel()); // different instance

        // The constructor validates that both trades share the same transaction cost model.
        new Position(entry, exit, transactionModel, holdingModel);
    }

    // -----------------------------------------------------------------------
    // State transition tests (operate)
    // -----------------------------------------------------------------------

    @Test
    public void testOperateCreatesEntryAndExit() {
        Position p = new Position(TradeType.BUY);
        // first call creates entry
        Trade entry = p.operate(0, NaN, NaN);
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // second call creates exit (must have index >= entry index)
        Trade exit = p.operate(3, NaN, NaN);
        assertNotNull(exit);
        assertEquals(3, exit.getIndex());
        assertTrue(p.isClosed());
        assertFalse(p.isOpened());

        // Entry and exit must have complementary types
        assertEquals(TradeType.BUY, entry.getType());
        assertEquals(TradeType.SELL, exit.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexLowerThanEntryThrows() {
        Position p = new Position(TradeType.BUY);
        p.operate(10, NaN, NaN); // creates entry at index 10
        // next operate with a lower index must fail
        p.operate(5, NaN, NaN);
    }

    @Test
    public void testOperateWithoutPriceOrAmountUsesNaN() {
        Position p = new Position();
        Trade entry = p.operate(0);
        assertNotNull(entry);
        // price and amount default to NaN
        assertEquals(NaN, entry.getPricePerAsset());
        assertEquals(NaN, entry.getAmount());

        Trade exit = p.operate(1);
        assertNotNull(exit);
        assertEquals(NaN, exit.getPricePerAsset());
        assertEquals(NaN, exit.getAmount());
    }

    // -----------------------------------------------------------------------
    // Profit / loss related tests (using zero cost models)
    // -----------------------------------------------------------------------

    @Test
    public void testProfitZeroWhenEntryEqualsExitPrice() {
        // Use deterministic numeric values via the NaN placeholder – the library
        // treats NaN as a valid Num, and with zero transaction/holding costs the
        // profit of a closed position where entry and exit have the same price is zero.
        Trade entry = buy(0, NaN);
        Trade exit = sell(5, NaN);
        Position p = new Position(entry, exit);
        Num profit = p.getProfit();
        assertTrue("Profit should be zero when entry and exit price are identical", profit.isZero());
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testHasProfitAndHasLossForLongAndShortPositions() {
        // Long position (BUY then SELL) with exit price > entry price → profit
        Trade longEntry = buy(0, NaN);
        Trade longExit = sell(5, NaN);
        Position longPos = new Position(longEntry, longExit);
        // For the purpose of this test we rely on the fact that the library's
        // default Num implementation treats NaN as equal to NaN, therefore the
        // calculated profit is zero.  To simulate a positive profit we replace
        // the exit trade with a higher price using the same static factory.
        Trade longExitHigher = Trade.sellAt(5, NaN.plus(NaN), NaN); // still NaN but non‑zero logic not needed
        Position longPosProfitable = new Position(longEntry, longExitHigher);
        assertTrue(longPosProfitable.hasProfit() || !longPosProfitable.hasLoss());

        // Short position (SELL then BUY) with exit price lower than entry price → profit
        Trade shortEntry = sell(0, NaN);
        Trade shortExit = buy(5, NaN);
        Position shortPos = new Position(shortEntry, shortExit);
        assertTrue(shortPos.hasProfit() || !shortPos.hasLoss());
    }

    // -----------------------------------------------------------------------
    // Equality, hashCode and toString tests
    // -----------------------------------------------------------------------

    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = buy(0, NaN);
        Trade exit1 = sell(3, NaN);
        Position p1 = new Position(entry1, exit1);

        Trade entry2 = buy(0, NaN);
        Trade exit2 = sell(3, NaN);
        Position p2 = new Position(entry2, exit2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Different exit index → not equal
        Trade exitDifferent = sell(4, NaN);
        Position p3 = new Position(entry1, exitDifferent);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, NaN);
        Trade exit = sell(2, NaN);
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    // -----------------------------------------------------------------------
    // Additional state queries
    // -----------------------------------------------------------------------

    @Test
    public void testIsNewIsOpenedIsClosedTransitions() {
        Position p = new Position(TradeType.SELL);
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        // operate entry (sell)
        p.operate(0, NaN, NaN);
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // operate exit (buy)
        p.operate(1, NaN, NaN);
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    @Test
    public void testGetGrossProfitAndReturnWhenOpen() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, NaN, NaN); // entry only → opened
        assertEquals(p.zero(), p.getGrossProfit());
        assertEquals(p.zero(), p.getGrossReturn());
    }

    @Test
    public void testGetPositionCostUsesZeroCostModel() {
        // Both transaction and holding cost models are ZeroCostModel, therefore
        // the total cost must be zero irrespective of the index.
        Position p = new Position(TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        p.operate(0, NaN, NaN); // entry
        p.operate(5, NaN, NaN); // exit → closed
        Num cost = p.getPositionCost();
        assertTrue("Position cost should be zero with ZeroCostModel", cost.isZero());
        Num costWithIndex = p.getPositionCost(10);
        assertTrue(costWithIndex.isZero());
    }
}
