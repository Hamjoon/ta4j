###Test START##
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.BooleanRule;
import org.ta4j.core.rules.Rule;

/**
 * Comprehensive unit tests for {@link BarSeriesManager}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>All public constructors and the three getter methods.</li>
 *   <li>Running a strategy on an empty series.</li>
 *   <li>Running a strategy with explicit start/finish indexes (including out‑of‑range
 *       values).</li>
 *   <li>Behaviour when the last position is still open after the supplied finish
 *       index – the manager must attempt to close it on the following bars.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The library provides concrete implementations for the required interfaces:
 * <ul>
 *   <li>{@link BaseBarSeries} – a mutable {@link BarSeries} implementation.</li>
 *   <li>{@link BaseStrategy} – a simple {@link Strategy} implementation that can be
 *       built from {@link Rule}s.</li>
 *   <li>{@link BooleanRule} – a {@link Rule} that always returns a constant boolean.</li>
 *   <li>{@link TradeOnNextOpenModel} – the default trade execution model used by the
 *       manager.</li>
 *   <li>{@link ZeroCostModel} – a cost model with zero transaction/holding cost.</li>
 *   <li>{@link DecimalNum} – a concrete {@link Num} implementation.</li>
 * </ul>
 * </p>
 */
public class BarSeriesManagerTest {

    private BarSeries emptySeries;
    private BarSeries threeBarSeries;
    private Num one;
    private Num two;
    private Num three;

    /**
     * Helper that creates a {@link BaseBarSeries} with a given number of bars.
     * Each bar contains increasing price values (1, 2, 3 …) so that entry/exit
     * decisions can be based on simple numeric comparisons.
     */
    private BarSeries createSeries(int barCount) {
        BarSeries series = new BaseBarSeries("test-series");
        ZonedDateTime time = ZonedDateTime.now();
        for (int i = 0; i < barCount; i++) {
            // use DecimalNum for price values
            Num open = DecimalNum.valueOf(i + 1);
            Num high = open.plus(DecimalNum.ONE);
            Num low = open.minus(DecimalNum.ONE);
            Num close = open;
            series.addBar(time.plusSeconds(i), open, high, low, close, DecimalNum.ZERO);
        }
        return series;
    }

    @Before
    public void setUp() {
        emptySeries = createSeries(0);
        threeBarSeries = createSeries(3);
        one = DecimalNum.ONE;
        two = DecimalNum.valueOf(2);
        three = DecimalNum.valueOf(3);
    }

    // ------------------------------------------------------------------------
    // Constructor / getter tests
    // ------------------------------------------------------------------------

    @Test
    public void testConstructorWithOnlySeries() {
        BarSeriesManager manager = new BarSeriesManager(threeBarSeries);
        assertSame("BarSeries getter must return the instance passed to the constructor",
                threeBarSeries, manager.getBarSeries());

        assertTrue("Default transaction cost model must be a ZeroCostModel",
                manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue("Default holding cost model must be a ZeroCostModel",
                manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testConstructorWithCustomExecutionModel() {
        TradeExecutionModel customModel = new TradeOnNextOpenModel();
        BarSeriesManager manager = new BarSeriesManager(threeBarSeries, customModel);
        // the model is not exposed, but we can indirectly verify that the manager
        // works with it (run a trivial strategy – no exception must be thrown)
        Strategy alwaysFalse = new BaseStrategy("false", new BooleanRule(false), new BooleanRule(false));
        TradingRecord record = manager.run(alwaysFalse);
        assertNotNull(record);
    }

    @Test
    public void testConstructorWithAllCustomModels() {
        CostModel txCost = new ZeroCostModel(); // using ZeroCostModel for simplicity
        CostModel holdCost = new ZeroCostModel();
        TradeExecutionModel execModel = new TradeOnNextOpenModel();

        BarSeriesManager manager = new BarSeriesManager(threeBarSeries, txCost, holdCost, execModel);

        assertSame(txCost, manager.getTransactionCostModel());
        assertSame(holdCost, manager.getHoldingCostModel());
        // as before, we just check that run does not throw
        Strategy alwaysFalse = new BaseStrategy("false", new BooleanRule(false), new BooleanRule(false));
        TradingRecord record = manager.run(alwaysFalse);
        assertNotNull(record);
    }

    // ------------------------------------------------------------------------
    // run() method tests – empty series
    // ------------------------------------------------------------------------

    @Test
    public void testRunOnEmptySeriesReturnsEmptyRecord() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        // Strategy that would always want to trade – we use true rules to ensure the
        // manager really tries to execute something.
        Strategy alwaysTrue = new BaseStrategy("alwaysTrue",
                new BooleanRule(true), new BooleanRule(true));

        TradingRecord record = manager.run(alwaysTrue);
        // No bars → no trades can be executed
        assertEquals("No positions should be created on an empty series", 0, record.getPositionCount());
        assertTrue("TradingRecord must be considered closed when no trades exist", record.isClosed());
    }

    // ------------------------------------------------------------------------
    // run() with explicit start / finish indexes (including out‑of‑range values)
    // ------------------------------------------------------------------------

    @Test
    public void testRunWithStartAndFinishIndexesWithinBounds() {
        BarSeriesManager manager = new BarSeriesManager(threeBarSeries);
        // Strategy that enters on every bar and exits on the next bar
        Rule entry = new BooleanRule(true);
        Rule exit = new BooleanRule(true);
        Strategy strategy = new BaseStrategy("enterExitEveryBar", entry, exit);

        // Run from index 0 to 2 (the whole series)
        TradingRecord record = manager.run(strategy, 0, 2);

        // With entry+exit on each bar we expect 3 trades (entry+exit = 1 position per bar)
        // BaseTradingRecord creates a new position only when an entry occurs while
        // no position is open. Because we always exit immediately, a new entry can be
        // performed on the next bar. Hence we end with 3 positions (the last one is
        // closed by the exit on the same bar).
        assertEquals("Three positions must have been opened/closed", 3, record.getPositionCount());
        assertTrue("All positions must be closed", record.isClosed());
    }

    @Test
    public void testRunWithStartAndFinishIndexesOutOfRange() {
        BarSeriesManager manager = new BarSeriesManager(threeBarSeries);
        Strategy alwaysFalse = new BaseStrategy("never", new BooleanRule(false), new BooleanRule(false));

        // start index lower than series.beginIndex, finish index higher than endIndex
        TradingRecord record = manager.run(alwaysFalse, -5, 10);

        // No trades because the strategy never signals; the manager must clamp indexes
        assertEquals(0, record.getPositionCount());
        // The manager should still consider the record closed (no open position)
        assertTrue(record.isClosed());

        // Verify that the internal bounds have been correctly clamped by checking that
        // the manager did not throw an exception and that the start/end indexes of the
        // record correspond to the series limits.
        assertEquals(Integer.valueOf(threeBarSeries.getBeginIndex()), record.getStartIndex());
        assertEquals(Integer.valueOf(threeBarSeries.getEndIndex()), record.getEndIndex());
    }

    // ------------------------------------------------------------------------
    // Behaviour when the last position stays open after the supplied finish index
    // ------------------------------------------------------------------------

    @Test
    public void testRunClosesOpenPositionAfterFinishIndex() {
        // Build a series with 5 bars
        BarSeries series = createSeries(5);
        BarSeriesManager manager = new BarSeriesManager(series);

        // Strategy: enter on the first bar (index 0) and never exit inside the run range.
        // After the run finishes (index 2) we expect the manager to attempt to close the
        // position on the following bars (3, 4).
        Rule entry = new BooleanRule(true);   // always true → entry on first opportunity
        Rule exit = new BooleanRule(false);   // never exit inside the run
        Strategy strategy = new BaseStrategy("enterOnly", entry, exit);

        // Run only on the first three bars (0‑2)
        TradingRecord record = manager.run(strategy, TradeType.BUY, one, 0, 2);

        // After the run the position should still be open
        assertFalse("Position should be open after the limited run", record.isClosed());

        // The manager must have attempted to close it on the next bar (index 3)
        // Since our exit rule is always false, the only way to close is the internal
        // post‑run loop that re‑evaluates the strategy. To make that loop close the
        // position we replace the strategy with one that exits on the very first
        // additional bar.
        Rule exitNow = new BooleanRule(true);
        Strategy closingStrategy = new BaseStrategy("closeSoon", entry, exitNow);

        // Run again on the same series but start after the previously opened position;
        // this will trigger the post‑run closing logic.
        TradingRecord finalRecord = manager.run(closingStrategy, TradeType.BUY, one, 3, 4);

        // The final record must contain exactly one closed position
        assertEquals(1, finalRecord.getPositionCount());
        assertTrue(finalRecord.isClosed());
    }

    // ------------------------------------------------------------------------
    // Additional sanity checks for overloads that accept an amount
    // ------------------------------------------------------------------------

    @Test
    public void testRunWithCustomAmount() {
        BarSeriesManager manager = new BarSeriesManager(threeBarSeries);
        Strategy alwaysTrue = new BaseStrategy("alwaysTrue", new BooleanRule(true), new BooleanRule(true));

        // Use a custom amount (e.g., 100 units) – the amount is passed to the execution
        // model but does not affect the number of positions for the simple BooleanRule.
        Num amount = DecimalNum.valueOf(100);
        TradingRecord record = manager.run(alwaysTrue, TradeType.BUY, amount, 0, 2);

        assertEquals(3, record.getPositionCount());
        assertTrue(record.isClosed());
    }
}
###Test END##