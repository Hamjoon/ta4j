###Test START##
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * The tests use small concrete implementations of the library interfaces
 * (BarSeries, Strategy, TradeExecutionModel) so that no mocking framework is
 * required and the behaviour of {@code BarSeriesManager#run(...)} can be
 * verified end‑to‑end.
 */
public class BarSeriesManagerTest {

    /** Simple immutable Bar implementation – only needed to satisfy the BarSeries contract. */
    private static class SimpleBar implements Bar {
        private final Num open, high, low, close;

        SimpleBar(Num open, Num high, Num low, Num close) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        @Override public Num getOpenPrice() { return open; }
        @Override public Num getHighPrice() { return high; }
        @Override public Num getLowPrice() { return low; }
        @Override public Num getClosePrice() { return close; }
        @Override public Num getVolume() { return DecimalNum.ZERO; }
        @Override public Num getAmount() { return DecimalNum.ZERO; }
        @Override public int getIndex() { return 0; }
        @Override public BarSeries getSeries() { return null; }
        @Override public ZonedDateTime getEndTime() { return null; }
        @Override public ZonedDateTime getBeginTime() { return null; }
        @Override public Num getNumClosePrice() { return close; }
        @Override public Num getNumOpenPrice() { return open; }
        @Override public Num getNumHighPrice() { return high; }
        @Override public Num getNumLowPrice() { return low; }
        @Override public Num getNumVolume() { return DecimalNum.ZERO; }
        @Override public Num getNumAmount() { return DecimalNum.ZERO; }
    }

    /** Minimal BarSeries implementation used only by the manager. */
    private static class SimpleBarSeries implements BarSeries {

        private final List<Bar> bars = new ArrayList<>();
        private final NumFactory numFactory = new DecimalNumFactory();

        SimpleBarSeries(int size) {
            // create a series of “size” bars with deterministic prices
            for (int i = 0; i < size; i++) {
                Num price = DecimalNum.valueOf(i + 1);
                bars.add(new SimpleBar(price, price, price, price));
            }
        }

        @Override public NumFactory numFactory() { return numFactory; }
        @Override public BaseBarConvertibleBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "SimpleSeries"; }
        @Override public Bar getBar(int i) { return bars.get(i); }
        @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return bars.size() - 1; }

        // the remaining methods are not needed for the tests – they throw
        // UnsupportedOperationException to highlight unexpected usage
        @Override public void addBar(Bar bar) { throw new UnsupportedOperationException(); }
        @Override public void addBar(Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Number price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return bars.size(); }
        @Override public boolean isEmpty() { return bars.isEmpty(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { }
        @Override public int getRemovedBarsCount() { return 0; }
    }

    /** Strategy that triggers operation on a predefined set of indexes. */
    private static class IndexStrategy implements Strategy {

        private final Set<Integer> operateIndexes;
        private final String name;

        IndexStrategy(String name, int... indexes) {
            this.name = name;
            this.operateIndexes = new HashSet<>();
            for (int i : indexes) {
                operateIndexes.add(i);
            }
        }

        @Override public String getName() { return name; }
        @Override public Rule getEntryRule() { throw new UnsupportedOperationException(); }
        @Override public Rule getExitRule() { throw new UnsupportedOperationException(); }
        @Override public Strategy and(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) { }
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operateIndexes.contains(index);
        }

        // default implementations of the other “shouldX” methods delegate to
        // shouldOperate – they are not used directly in the manager.
    }

    /** TradeExecutionModel that simply opens a position on the first call and
     *  closes it on the next call. */
    private static class SimpleExecutionModel implements TradeExecutionModel {

        private final List<Integer> executedIndexes = new ArrayList<>();

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            executedIndexes.add(index);
            if (tradingRecord.isClosed()) {
                // open a new position
                tradingRecord.enter(index, barSeries.getBar(index).getClosePrice(), amount);
            } else {
                // close the current position
                tradingRecord.exit(index, barSeries.getBar(index).getClosePrice(), amount);
            }
        }

        public List<Integer> getExecutedIndexes() {
            return executedIndexes;
        }
    }

    /** Helper to build a manager with a series of the requested length. */
    private BarSeriesManager createManager(int seriesSize) {
        BarSeries series = new SimpleBarSeries(seriesSize);
        return new BarSeriesManager(series);
    }

    @Test
    public void testConstructorsAndGetters() {
        BarSeries series = new SimpleBarSeries(3);
        CostModel txnModel = new ZeroCostModel();
        CostModel holdModel = new ZeroCostModel();
        TradeExecutionModel execModel = new SimpleExecutionModel();

        BarSeriesManager mgr = new BarSeriesManager(series, txnModel, holdModel, execModel);

        assertSame(series, mgr.getBarSeries());
        assertSame(txnModel, mgr.getTransactionCostModel());
        assertSame(holdModel, mgr.getHoldingCostModel());
    }

    @Test
    public void testRunWithDefaultAmountAndFullSeries() {
        BarSeries series = new SimpleBarSeries(5);
        SimpleExecutionModel execModel = new SimpleExecutionModel();

        // Strategy will fire on indexes 0, 1, 2, 3, 4
        Strategy strategy = new IndexStrategy("fullRun", 0, 1, 2, 3, 4);

        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        TradingRecord record = mgr.run(strategy); // uses default BUY and default amount

        // The execution model opens on 0, closes on 1, opens on 2, closes on 3,
        // leaves an open position at 4 which is then automatically closed by the
        // “post‑run” loop (because 4 is the series end index).
        assertEquals(4, record.getPositionCount()); // 2 complete + 2 incomplete that get closed
        assertTrue(record.isClosed());

        // Verify that the execution model was called for each index where the
        // strategy signalled operation (including the extra post‑run index).
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4, 5); // 5 is the extra attempt after end
        assertEquals(expected, execModel.getExecutedIndexes());
    }

    @Test
    public void testRunWithExplicitAmountAndCustomTradeType() {
        BarSeries series = new SimpleBarSeries(3);
        SimpleExecutionModel execModel = new SimpleExecutionModel();

        // Strategy fires only on index 1 (to open) and index 2 (to close)
        Strategy strategy = new IndexStrategy("customAmount", 1, 2);

        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        Num customAmount = DecimalNum.valueOf(10);
        TradingRecord record = mgr.run(strategy, TradeType.SELL, customAmount, 0, 2);

        // One position should be created (SELL opening then BUY closing)
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());

        // The execution model must have been invoked exactly twice.
        assertEquals(Arrays.asList(1, 2), execModel.getExecutedIndexes());

        // Verify that the starting trade type of the record matches the one we passed.
        assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRunWithStartAndFinishIndexesLimits() {
        BarSeries series = new SimpleBarSeries(6);
        SimpleExecutionModel execModel = new SimpleExecutionModel();

        // Strategy fires on every index, but we will limit the run to 2..4
        Strategy strategy = new IndexStrategy("rangeRun", 0, 1, 2, 3, 4, 5);

        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        TradingRecord record = mgr.run(strategy, TradeType.BUY, 2, 4);

        // Expected execution indexes: 2 (open), 3 (close), 4 (open) -> then the
        // post‑run loop should close it at index 5 (first index after finish that
        // the strategy also signals).
        List<Integer> expected = Arrays.asList(2, 3, 4, 5);
        assertEquals(expected, execModel.getExecutedIndexes());

        // Two complete positions: (2‑3) and (4‑5)
        assertEquals(2, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWhenStrategyNeverOperates() {
        BarSeries series = new SimpleBarSeries(4);
        SimpleExecutionModel execModel = new SimpleExecutionModel();

        // Strategy that never returns true
        Strategy neverStrategy = new IndexStrategy("never", new int[0]);

        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        TradingRecord record = mgr.run(neverStrategy);

        // No execution, no positions
        assertEquals(0, execModel.getExecutedIndexes().size());
        assertEquals(0, record.getPositionCount());
        assertTrue(record.isClosed()); // a TradingRecord with no trades is considered closed
    }

    @Test
    public void testRunClosesOpenPositionAtSeriesEnd() {
        BarSeries series = new SimpleBarSeries(3);
        SimpleExecutionModel execModel = new SimpleExecutionModel();

        // Strategy opens at index 0 and never signals a close – the manager must
        // attempt to close after the series end.
        Strategy openOnly = new IndexStrategy("openOnly", 0);

        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        TradingRecord record = mgr.run(openOnly);

        // Execution should happen at 0 (open) and then at 1 (first bar after end) to try closing.
        List<Integer> expected = Arrays.asList(0, 1);
        assertEquals(expected, execModel.getExecutedIndexes());

        // After the extra attempt the position must be closed.
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }
}
###Test END##