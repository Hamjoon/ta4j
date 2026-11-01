package org.ta4j.core.backtest;
import org.ta4j.core.rules.AbstractRule;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.trading.rules.AbstractRule;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests use the real concrete classes that are part of the TA‑4J library
 * (e.g. {@link org.ta4j.core.BaseBarSeries}, {@link DecimalNum},
 * {@link BaseTradingRecord}) together with tiny custom implementations of
 * {@link Strategy} and {@link TradeExecutionModel} that are sufficient for the
 * behaviour exercised by {@link BarSeriesManager#run(Strategy, TradeType, Num, int, int)}.
 *
 * All public methods of {@code BarSeriesManager} are covered:
 * <ul>
 *   <li>constructors</li>
 *   <li>getter methods</li>
 *   <li>the various {@code run(...)} overloads</li>
 *   <li>the “close‑last‑position‑after‑end‑index” logic</li>
 * </ul>
 */
public class BarSeriesManagerTest {

    private BarSeries series;
    private CostModel zeroCost;
    private TradeExecutionModel noopExecutionModel;
    private TradeExecutionModel recordingExecutionModel;

    @Before
    public void setUp() {
        // Build a tiny bar series (3 bars) – the concrete implementation
        // {@link org.ta4j.core.BaseBarSeries} is part of the library and therefore
        // allowed.  Each bar only needs a close price; other fields are filled with
        // dummy values.
        series = new org.ta4j.core.BaseBarSeries("test-series");
        series.addBar(new org.ta4j.core.BaseBar(
                DecimalNum.valueOf(1),   // time (ignored)
                DecimalNum.valueOf(10),  // open price
                DecimalNum.valueOf(12),  // max price
                DecimalNum.valueOf(9),   // min price
                DecimalNum.valueOf(11),  // close price
                DecimalNum.valueOf(100), // volume
                DecimalNum.valueOf(0)    // amount (not used)
        ));
        series.addBar(new org.ta4j.core.BaseBar(
                DecimalNum.valueOf(2),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(110),
                DecimalNum.valueOf(0)
        ));
        series.addBar(new org.ta4j.core.BaseBar(
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(120),
                DecimalNum.valueOf(0)
        ));

        zeroCost = new ZeroCostModel();

        // A TradeExecutionModel that does nothing – useful for tests where we only
        // want to verify that the manager iterates over the expected indexes.
        noopExecutionModel = (index, tradingRecord, barSeries, amount) -> {
            // intentionally empty
        };

        // A TradeExecutionModel that records every index it receives.
        recordingExecutionModel = new TradeExecutionModel() {
            final List<Integer> executedIndexes = new ArrayList<>();

            @Override
            public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
                executedIndexes.add(index);
                // For the purpose of the test we simply open a position on the
                // first call and close it on the second call.
                if (tradingRecord.getCurrentPosition() == null) {
                    tradingRecord.enter(index, barSeries.getBar(index).getClosePrice(), amount);
                } else if (!tradingRecord.isClosed()) {
                    tradingRecord.exit(index, barSeries.getBar(index).getClosePrice(), amount);
                }
            }

            List<Integer> getExecutedIndexes() {
                return Collections.unmodifiableList(executedIndexes);
            }
        };
    }

    /* -----------------------------------------------------------------------
     * Constructor / getter tests
     * --------------------------------------------------------------------- */

    @Test
    public void testConstructorWithDefaults() {
        BarSeriesManager manager = new BarSeriesManager(series);
        assertSame(series, manager.getBarSeries());
        assertTrue(manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testConstructorWithCustomExecutionModel() {
        BarSeriesManager manager = new BarSeriesManager(series, noopExecutionModel);
        assertSame(noopExecutionModel, manager.tradeExecutionModel);
    }

    @Test
    public void testConstructorWithCustomCostModels() {
        CostModel customTx = new ZeroCostModel();
        CostModel customHold = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(series, customTx, customHold);
        assertSame(customTx, manager.getTransactionCostModel());
        assertSame(customHold, manager.getHoldingCostModel());
    }

    @Test
    public void testFullConstructor() {
        CostModel tx = new ZeroCostModel();
        CostModel hold = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(series, tx, hold, noopExecutionModel);
        assertSame(series, manager.getBarSeries());
        assertSame(tx, manager.getTransactionCostModel());
        assertSame(hold, manager.getHoldingCostModel());
        assertSame(noopExecutionModel, manager.tradeExecutionModel);
    }

    /* -----------------------------------------------------------------------
     * run(...) overloads – simple “do‑nothing” strategy
     * --------------------------------------------------------------------- */

    private static class AlwaysFalseStrategy implements Strategy {
        @Override public String getName() { return "always-false"; }
        @Override public org.ta4j.core.Rule getEntryRule() { return new AbstractRule() { @Override public boolean isSatisfied(int index, TradingRecord record) { return false; } }; }
        @Override public org.ta4j.core.Rule getExitRule() { return new AbstractRule() { @Override public boolean isSatisfied(int index, TradingRecord record) { return false; } }; }
        @Override public Strategy and(Strategy s) { return this; }
        @Override public Strategy or(Strategy s) { return this; }
        @Override public Strategy and(String name, Strategy s, int unstableBars) { return this; }
        @Override public Strategy or(String name, Strategy s, int unstableBars) { return this; }
        @Override public Strategy opposite() { return this; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
        // Force shouldOperate to always return false → no trade execution.
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return false; }
    }

    @Test
    public void testRunWithNeverOperatingStrategy() {
        BarSeriesManager manager = new BarSeriesManager(series, noopExecutionModel);
        Strategy never = new AlwaysFalseStrategy();

        TradingRecord record = manager.run(never);
        // No entry/exit should have happened.
        assertTrue(record.getPositions().isEmpty());
        assertFalse(record.isClosed());
    }

    @Test
    public void testRunWithIndexesBoundaries() {
        BarSeriesManager manager = new BarSeriesManager(series, noopExecutionModel);
        Strategy never = new AlwaysFalseStrategy();

        // startIndex lower than series begin and finishIndex higher than series end
        TradingRecord record = manager.run(never, TradeType.BUY,
                DecimalNum.valueOf(1), -10, 100);
        // The manager must clamp the indexes to the series bounds.
        assertEquals(series.getBeginIndex(), record.getStartIndex());
        assertEquals(series.getEndIndex(), record.getEndIndex());
    }

    /* -----------------------------------------------------------------------
     * run(...) – testing the “close last position after end index” logic
     * --------------------------------------------------------------------- */

    /**
     * A strategy that always wants to operate.  By overriding {@code shouldOperate}
     * we bypass the rule system completely.
     */
    private static class AlwaysTrueStrategy implements Strategy {
        @Override public String getName() { return "always-true"; }
        @Override public org.ta4j.core.Rule getEntryRule() { return null; }
        @Override public org.ta4j.core.Rule getExitRule() { return null; }
        @Override public Strategy and(Strategy s) { return this; }
        @Override public Strategy or(Strategy s) { return this; }
        @Override public Strategy and(String name, Strategy s, int unstableBars) { return this; }
        @Override public Strategy or(String name, Strategy s, int unstableBars) { return this; }
        @Override public Strategy opposite() { return this; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return true; }
    }

    @Test
    public void testRunClosesOpenPositionAfterEndIndex() {
        // Use the recording execution model so we can inspect which indexes were hit.
        BarSeriesManager manager = new BarSeriesManager(series, recordingExecutionModel);
        Strategy always = new AlwaysTrueStrategy();

        // Run only on the first bar (index 0).  The execution model will open a
        // position on index 0 and leave it open because the loop finishes at index 0.
        TradingRecord record = manager.run(always, TradeType.BUY,
                DecimalNum.valueOf(1), 0, 0);

        // At this point the position is still open.
        assertNotNull(record.getCurrentPosition());
        assertFalse(record.isClosed());

        // Because the series has more bars after index 0, the manager must try to
        // close the position by invoking the execution model on the next index
        // (index 1).  Our custom execution model closes the position on its second
        // call.
        List<Integer> executed = ((BarSeriesManagerTest.recordingExecutionModel) recordingExecutionModel)
                .getExecutedIndexes();

        // Expected sequence: first call at index 0 (open), second call at index 1 (close)
        assertEquals(2, executed.size());
        assertEquals(Integer.valueOf(0), executed.get(0));
        assertEquals(Integer.valueOf(1), executed.get(1));

        // After the extra iteration the position must be closed.
        assertTrue(record.isClosed());
        assertEquals(1, record.getPositionCount());
    }

    @Test
    public void testRunDoesNotAttemptExtraCloseWhenSeriesEnds() {
        // Run over the whole series (indexes 0‑2).  The execution model will
        // open on 0, close on 1, and then leave the record closed.  The extra
        // loop must not be entered because the position is already closed.
        BarSeriesManager manager = new BarSeriesManager(series, recordingExecutionModel);
        Strategy always = new AlwaysTrueStrategy();

        TradingRecord record = manager.run(always, TradeType.BUY,
                DecimalNum.valueOf(1), 0, series.getEndIndex());

        List<Integer> executed = ((BarSeriesManagerTest.recordingExecutionModel) recordingExecutionModel)
                .getExecutedIndexes();

        // Expected: open at 0, close at 1, then a third call at 2 (no position to close)
        // The extra loop is still entered because runEndIndex == series.getEndIndex()
        // but the position is already closed, so no further execute is performed.
        assertEquals(3, executed.size());
        assertEquals(Integer.valueOf(0), executed.get(0));
        assertEquals(Integer.valueOf(1), executed.get(1));
        assertEquals(Integer.valueOf(2), executed.get(2));

        assertTrue(record.isClosed());
        assertEquals(1, record.getPositionCount());
    }

    /* -----------------------------------------------------------------------
     * run(...) overloads delegating to the main method
     * --------------------------------------------------------------------- */

    @Test
    public void testRunOverloadsDelegateCorrectly() {
        BarSeriesManager manager = new BarSeriesManager(series, noopExecutionModel);
        Strategy never = new AlwaysFalseStrategy();

        // run(Strategy)
        TradingRecord r1 = manager.run(never);
        // run(Strategy, TradeType)
        TradingRecord r2 = manager.run(never, TradeType.SELL);
        // run(Strategy, int, int)
        TradingRecord r3 = manager.run(never, 0, series.getEndIndex());
        // run(Strategy, TradeType, int, int)
        TradingRecord r4 = manager.run(never, TradeType.SELL, 0, series.getEndIndex());

        // All records must be empty because the strategy never operates.
        assertTrue(r1.getPositions().isEmpty());
        assertTrue(r2.getPositions().isEmpty());
        assertTrue(r3.getPositions().isEmpty());
        assertTrue(r4.getPositions().isEmpty());

        // The start/end indexes stored inside the BaseTradingRecord must be
        // correctly set by each overload.
        assertEquals(Integer.valueOf(series.getBeginIndex()), r1.getStartIndex());
        assertEquals(Integer.valueOf(series.getEndIndex()), r1.getEndIndex());

        assertEquals(Integer.valueOf(series.getBeginIndex()), r2.getStartIndex());
        assertEquals(Integer.valueOf(series.getEndIndex()), r2.getEndIndex());

        assertEquals(Integer.valueOf(0), r3.getStartIndex());
        assertEquals(Integer.valueOf(series.getEndIndex()), r3.getEndIndex());

        assertEquals(Integer.valueOf(0), r4.getStartIndex());
        assertEquals(Integer.valueOf(series.getEndIndex()), r4.getEndIndex());
    }
}
