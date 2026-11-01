package org.ta4j.core.backtest;
import org.ta4j.core.BarSeries;
import org.ta4j.core.serialization.ComponentDescriptor;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;


import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests use real implementations from the TA4J library (e.g. {@link BaseBarSeries},
 * {@link DecimalNum}) and simple in‑line implementations of {@link Strategy} and
 * {@link TradeExecutionModel} that are sufficient for exercising all public
 * methods of {@link BarSeriesManager}.
 */
public class BarSeriesManagerTest {

    /** Series with three bars (index 0‑2) */
    private BaseBarSeries series;

    /** Simple strategy that enters on given indexes and exits on others */
    private static class SimpleStrategy implements Strategy {

        private final Set<Integer> entryIndexes;
        private final Set<Integer> exitIndexes;
        private final String name;

        SimpleStrategy(String name, Set<Integer> entryIndexes, Set<Integer> exitIndexes) {
            this.name = name;
            this.entryIndexes = entryIndexes;
            this.exitIndexes = exitIndexes;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Rule getEntryRule() {
            // Not used – we override shouldOperate directly.
            return null;
        }

        @Override
        public Rule getExitRule() {
            // Not used – we override shouldOperate directly.
            return null;
        }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            // If we have no position or the current position is closed → try to enter
            if (tradingRecord.isClosed() || tradingRecord.getCurrentPosition() == null) {
                return entryIndexes.contains(index);
            }
            // otherwise try to exit
            return exitIndexes.contains(index);
        }

        // The remaining methods are not needed for the tests – provide simple defaults.
        @Override public Strategy and(Strategy strategy) { return null; }
        @Override public Strategy or(Strategy strategy) { return null; }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy opposite() { return null; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
        @Override public boolean shouldEnter(int index) { return false; }
        @Override public boolean shouldEnter(int index, TradingRecord tradingRecord) { return false; }
        @Override public boolean shouldExit(int index) { return false; }
        @Override public boolean shouldExit(int index, TradingRecord tradingRecord) { return false; }
        @Override public String toJson() { return null; }
        @Override public ComponentDescriptor toDescriptor() { return null; }
    }

    /** Trade execution model that simply forwards the call to the trading record */
    private static class SimpleExecutionModel implements TradeExecutionModel {
        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // The default operation (enter/exit) is decided by the strategy,
            // we just call the generic operate method.
            tradingRecord.operate(index);
        }
    }

    @Before
    public void setUp() {
        // Build a 3‑bar series with simple constant prices (all 100)
        series = new BaseBarSeries("test-series");
        Num price = DecimalNum.valueOf(100);
        series.addBar(price, price, price, price, price); // index 0
        series.addBar(price, price, price, price, price); // index 1
        series.addBar(price, price, price, price, price); // index 2
    }

    @Test
    public void testGetters() {
        CostModel txCost = new ZeroCostModel();
        CostModel holdCost = new ZeroCostModel();
        TradeExecutionModel exec = new SimpleExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost, exec);

        assertSame("BarSeries getter must return the instance passed to the constructor",
                series, manager.getBarSeries());
        assertSame("Transaction cost model getter must return the instance passed to the constructor",
                txCost, manager.getTransactionCostModel());
        assertSame("Holding cost model getter must return the instance passed to the constructor",
                holdCost, manager.getHoldingCostModel());
    }

    @Test
    public void testRunDefaultBuy() {
        // Strategy: enter at index 0, exit at index 1
        Set<Integer> entry = new HashSet<>(Collections.singletonList(0));
        Set<Integer> exit = new HashSet<>(Collections.singletonList(1));
        Strategy strategy = new SimpleStrategy("enter‑0‑exit‑1", entry, exit);

        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        TradingRecord record = manager.run(strategy); // default BUY, whole series

        // One closed position expected
        assertEquals("Exactly one position should have been created", 1, record.getPositions().size());
        Position pos = record.getLastPosition();
        assertTrue("The position must be closed", pos.isClosed());

        // Verify that the position started with a BUY (the default)
        assertEquals("Starting trade type must be BUY", TradeType.BUY, record.getStartingType());
    }

    @Test
    public void testRunWithCustomTradeTypeSell() {
        // Strategy: enter at index 0, exit at index 2
        Set<Integer> entry = new HashSet<>(Collections.singletonList(0));
        Set<Integer> exit = new HashSet<>(Collections.singletonList(2));
        Strategy strategy = new SimpleStrategy("enter‑0‑exit‑2", entry, exit);

        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        TradingRecord record = manager.run(strategy, TradeType.SELL);

        // One closed position, starting with SELL
        assertEquals(1, record.getPositions().size());
        assertEquals(TradeType.SELL, record.getStartingType());
        assertTrue(record.getLastPosition().isClosed());
    }

    @Test
    public void testRunWithAmount() {
        // Strategy: enter at index 1, exit at index 2
        Set<Integer> entry = new HashSet<>(Collections.singletonList(1));
        Set<Integer> exit = new HashSet<>(Collections.singletonList(2));
        Strategy strategy = new SimpleStrategy("enter‑1‑exit‑2", entry, exit);

        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        Num customAmount = DecimalNum.valueOf(5);
        TradingRecord record = manager.run(strategy, TradeType.BUY, customAmount, 0, 2);

        // Verify that a position was opened and closed
        assertEquals(1, record.getPositions().size());
        assertTrue(record.getLastPosition().isClosed());

        // The amount does not affect the existence of the trade, but we can check that
        // the last trade recorded has the same amount (via the underlying Trade object)
        Trade lastEntry = record.getLastEntry();
        assertEquals("The amount stored in the entry trade must match the supplied amount",
                customAmount, lastEntry.getAmount());
    }

    @Test
    public void testRunWithPartialIndexesClipping() {
        // Strategy: entry at 0, exit at 2 (outside the requested finish index)
        Set<Integer> entry = new HashSet<>(Collections.singletonList(0));
        Set<Integer> exit = new HashSet<>(Collections.singletonList(2));
        Strategy strategy = new SimpleStrategy("enter‑0‑exit‑2", entry, exit);

        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        // Run only between indexes 0 and 1 – the exit at 2 should not be executed
        TradingRecord record = manager.run(strategy, TradeType.BUY, 0, 1);

        // Position should be open because we never reached the exit index
        assertEquals(1, record.getPositions().size());
        assertFalse("Position must remain open when finish index stops before exit", record.getLastPosition().isClosed());
    }

    @Test
    public void testRunClosesOpenPositionAtSeriesEnd() {
        // Strategy: entry at 2, never exits inside the run range.
        Set<Integer> entry = new HashSet<>(Collections.singletonList(2));
        Set<Integer> exit = new HashSet<>(); // never exit
        Strategy strategy = new SimpleStrategy("enter‑2‑no‑exit", entry, exit);

        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        // Run only up to index 2 (the series end). The manager should try to close
        // the open position using any further bars (there are none, so it stays open).
        TradingRecord record = manager.run(strategy, TradeType.BUY, 0, 2);

        // Because there are no bars after the series end, the position stays open.
        assertEquals(1, record.getPositions().size());
        assertFalse(record.getLastPosition().isClosed());

        // Add an extra dummy bar to the series to allow the manager to attempt a close.
        series.addBar(DecimalNum.valueOf(100), DecimalNum.valueOf(100),
                DecimalNum.valueOf(100), DecimalNum.valueOf(100), DecimalNum.valueOf(100));
        // Now run again with the same strategy but limited to the original end (2)
        // – the manager will see the extra bar after runEndIndex and will close.
        TradingRecord record2 = manager.run(strategy, TradeType.BUY, 0, 2);
        assertTrue("The manager must close the position using the extra bar",
                record2.getLastPosition().isClosed());
    }

    @Test
    public void testRunWhenStrategyNeverOperates() {
        // Strategy never returns true for shouldOperate
        Strategy never = new SimpleStrategy("never", Collections.emptySet(), Collections.emptySet());

        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        TradingRecord record = manager.run(never);
        // No positions should be created at all
        assertEquals("No positions should be created when strategy never operates",
                0, record.getPositions().size());
    }
}
