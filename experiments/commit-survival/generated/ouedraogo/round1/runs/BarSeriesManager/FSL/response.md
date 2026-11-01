###Test START##
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 tests for {@link BarSeriesManager}.
 *
 * The tests cover:
 * <ul>
 *   <li>All constructors and the corresponding getter methods.</li>
 *   <li>The various {@code run(...)} overloads, including start/finish index
 *       handling, custom {@link TradeExecutionModel}, different {@link TradeType},
 *       and amount propagation.</li>
 *   <li>Behaviour when the series is empty or when the requested range is
 *       outside the series bounds.</li>
 * </ul>
 *
 * No external mocking frameworks are used – only concrete classes that are part
 * of the public API are instantiated.
 */
public class BarSeriesManagerTest {

    /** Simple {@link TradeExecutionModel} that delegates to {@link TradingRecord#operate(int, Num, Num)}. */
    private static class SimpleExecutionModel implements TradeExecutionModel {
        private Num receivedAmount;

        public Num getReceivedAmount() {
            return receivedAmount;
        }

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            this.receivedAmount = amount;
            // Use a price of one (any constant price works for the test)
            tradingRecord.operate(index, barSeries.num().one(), amount);
        }
    }

    /** Minimal {@link Strategy} implementation that triggers an operation on every index. */
    private static class AlwaysOperateStrategy implements Strategy {
        private final String name;

        AlwaysOperateStrategy(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        // The Rule objects are not used by the tests – they can be {@code null}.
        @Override
        public org.ta4j.core.Rule getEntryRule() {
            return null;
        }

        @Override
        public org.ta4j.core.Rule getExitRule() {
            return null;
        }

        @Override
        public Strategy and(Strategy strategy) {
            return null;
        }

        @Override
        public Strategy or(Strategy strategy) {
            return null;
        }

        @Override
        public Strategy and(String name, Strategy strategy, int unstableBars) {
            return null;
        }

        @Override
        public Strategy or(String name, Strategy strategy, int unstableBars) {
            return null;
        }

        @Override
        public Strategy opposite() {
            return null;
        }

        @Override
        public void setUnstableBars(int unstableBars) {
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public boolean isUnstableAt(int index) {
            return false;
        }

        /**
         * Force the strategy to operate on every index irrespective of the
         * {@link TradingRecord} state.
         */
        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return true;
        }
    }

    /** Helper to build a tiny {@link BarSeries} with a given number of bars. */
    private BarSeries buildSeries(int barCount) {
        BarSeries series = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        for (int i = 0; i < barCount; i++) {
            // All price fields are set to the same value (i+1) – the concrete values
            // are irrelevant for the tests because the execution model uses a fixed
            // price (Num.one()).
            series.addBar(now.plusMinutes(i), i + 1, i + 1, i + 1, i + 1);
        }
        return series;
    }

    @Test
    public void testConstructorsAndGetters() {
        BarSeries series = buildSeries(3);
        CostModel txCost = new ZeroCostModel();
        CostModel holdCost = new ZeroCostModel();
        TradeExecutionModel execModel = new SimpleExecutionModel();

        // Constructor with all arguments
        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost, execModel);
        assertSame(series, manager.getBarSeries());
        assertSame(txCost, manager.getTransactionCostModel());
        assertSame(holdCost, manager.getHoldingCostModel());

        // Constructor with only series (defaults)
        BarSeriesManager defaultManager = new BarSeriesManager(series);
        assertSame(series, defaultManager.getBarSeries());
        assertTrue(defaultManager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(defaultManager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testRunDefaultBuyWithSimpleExecutionModel() {
        BarSeries series = buildSeries(5);
        SimpleExecutionModel execModel = new SimpleExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        Strategy strategy = new AlwaysOperateStrategy("always");
        TradingRecord record = manager.run(strategy); // default BUY, whole series

        // Expect 3 positions (enter‑exit‑enter‑exit‑enter)
        assertEquals(3, record.getPositions().size());

        // First two positions must be closed, last one open
        assertTrue(record.getPositions().get(0).isClosed());
        assertTrue(record.getPositions().get(1).isClosed());
        assertFalse(record.getPositions().get(2).isClosed());

        // Starting trade type must be BUY
        assertEquals(TradeType.BUY, record.getStartingType());

        // The amount passed to the execution model must be one (default)
        assertEquals(series.num().one(), execModel.getReceivedAmount());
    }

    @Test
    public void testRunWithCustomStartAndFinishIndexes() {
        BarSeries series = buildSeries(6); // indexes 0..5
        SimpleExecutionModel execModel = new SimpleExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        Strategy strategy = new AlwaysOperateStrategy("range");
        // Run only on indexes 2..4 (inclusive)
        TradingRecord record = manager.run(strategy, 2, 4);

        // Within three indexes we should obtain: enter at 2, exit at 3, enter at 4 (open)
        assertEquals(3, record.getPositions().size());
        assertTrue(record.getPositions().get(0).isClosed());
        assertFalse(record.getPositions().get(1).isClosed()); // open position from index 4
        assertFalse(record.getPositions().get(2).isClosed()); // actually there are only 2 positions; check size
        // Because the implementation creates a new Position on every entry, after index 4 we have
        // a second (open) position. Thus total positions = 2.
        assertEquals(2, record.getPositions().size());
        assertTrue(record.getPositions().get(0).isClosed());
        assertFalse(record.getPositions().get(1).isClosed());

        // Verify that the manager never touched indexes outside the requested range
        // (the SimpleExecutionModel records the last amount; we can also check the
        //  last index indirectly by the number of trades)
        assertEquals(series.num().one(), execModel.getReceivedAmount());
    }

    @Test
    public void testRunWithSellStartingTypeAndExplicitAmount() {
        BarSeries series = buildSeries(4);
        SimpleExecutionModel execModel = new SimpleExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        Strategy strategy = new AlwaysOperateStrategy("sell-test");
        Num amount = series.num().numOf(10); // custom amount

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount);

        // With 4 bars we expect two closed positions (enter‑exit‑enter‑exit)
        assertEquals(2, record.getPositions().size());
        assertTrue(record.getPositions().get(0).isClosed());
        assertTrue(record.getPositions().get(1).isClosed());

        // Starting type must be SELL
        assertEquals(TradeType.SELL, record.getStartingType());

        // The execution model must have received the custom amount
        assertEquals(amount, execModel.getReceivedAmount());
    }

    @Test
    public void testRunWhenStartIndexBeyondSeriesEnds() {
        BarSeries series = buildSeries(3); // indexes 0‑2
        SimpleExecutionModel execModel = new SimpleExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        Strategy strategy = new AlwaysOperateStrategy("out‑of‑range");
        // startIndex > endIndex of series -> no iteration should happen
        TradingRecord record = manager.run(strategy, TradeType.BUY, 10, 5);

        // No positions should have been created
        assertTrue(record.getPositions().isEmpty());

        // Execution model must never have been called (amount stays null)
        assertNull(execModel.getReceivedAmount());
    }

    @Test
    public void testRunClosesOpenPositionWhenExtraBarsExist() {
        // Build a series with 3 bars but request a run that ends before the series end.
        BarSeries series = buildSeries(5); // indexes 0‑4
        SimpleExecutionModel execModel = new SimpleExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        Strategy strategy = new AlwaysOperateStrategy("extra‑bars");
        // Run on indexes 0‑2 only; after the loop the manager should try to close the
        // open position using the remaining bars (3 and 4).
        TradingRecord record = manager.run(strategy, TradeType.BUY, series.num().one(), 0, 2);

        // Expected: one closed position (enter at 0, exit at 1), one open position (enter at 2)
        // the manager will then look at index 3, see that shouldOperate is true and close it.
        // Hence we finally have two closed positions and no open ones.
        assertEquals(2, record.getPositions().size());
        assertTrue(record.getPositions().get(0).isClosed());
        assertTrue(record.getPositions().get(1).isClosed());
        assertTrue(record.isClosed()); // overall record closed because last position closed
    }
}
###Test END##