package org.ta4j.core.backtest;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.walkforward.WalkForwardConfig;


import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.TradeExecutionModel.ExecutionTarget;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Constructor argument validation.</li>
 *   <li>Getter methods – ensuring the returned objects are snapshots / immutable.</li>
 *   <li>All public {@code run(...)} overloads (including amount handling and
 *       position‑sizer overloads).</li>
 *   <li>Internal helper methods such as {@code createDefaultTradingRecord},
 *       {@code fallbackSizingTarget} and amount validation.</li>
 *   <li>Edge‑cases: empty series, out‑of‑range indexes, negative/NaN amounts.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Real objects from the library are used – no mocking frameworks are employed.
 * Simple in‑line implementations of {@link Strategy}, {@link TradeExecutionModel}
 * and {@link PositionSizer} are provided where the library does not expose a
 * concrete class suitable for the test scenario.
 * </p>
 */
public class BarSeriesManagerTest {

    /** Small series (3 bars) used for most tests. */
    private BarSeries series;

    /** Zero‑cost models – they are cheap and deterministic. */
    private final CostModel zeroTransactionCost = new ZeroCostModel();
    private final CostModel zeroHoldingCost = new ZeroCostModel();

    /** Simple execution model that always executes trades at the close price. */
    private static class SimpleExecutionModel implements TradeExecutionModel {

        @Override
        public void onBar(int index, TradingRecord tradingRecord, BarSeries barSeries) {
            // No‑op for this simple model.
        }

        @Override
        public ExecutionTarget estimateEntryTarget(int signalIndex, BarSeries barSeries, Trade.TradeType tradeType) {
            // Use the close price of the bar at signalIndex as the target.
            Bar bar = barSeries.getBar(signalIndex);
            return new ExecutionTarget(signalIndex, bar.getClosePrice());
        }

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // Alternate between entry and exit based on the current position state.
            Bar bar = barSeries.getBar(index);
            Num price = bar.getClosePrice();

            if (tradingRecord.isClosed() || tradingRecord.getCurrentPosition() == null) {
                // entry
                tradingRecord.enter(index, price, amount);
            } else {
                // exit
                tradingRecord.exit(index, price, amount);
            }
        }
    }

    /** Minimal strategy that always signals an operation. */
    private static class AlwaysOperateStrategy implements Strategy {

        private final Trade.TradeType startingType;

        AlwaysOperateStrategy(Trade.TradeType startingType) {
            this.startingType = startingType;
        }

        @Override
        public String getName() {
            return "AlwaysOperate";
        }

        @Override
        public org.ta4j.core.Rule getEntryRule() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.ta4j.core.Rule getExitRule() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Trade.TradeType getStartingType() {
            return startingType;
        }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            // Always signal a trade.
            return true;
        }

        // The remaining default methods are left untouched.
    }

    /** Position sizer that returns a fixed, configurable amount. */
    private static class FixedAmountSizer implements PositionSizer {

        private final Num amount;

        FixedAmountSizer(Num amount) {
            this.amount = amount;
        }

        @Override
        public Num amount(Context context) {
            return amount;
        }
    }

    /** Helper to create a three‑bar series with deterministic numbers. */
    private static BarSeries createThreeBarSeries() {
        NumFactory nf = DecimalNum::valueOf;
        Bar b1 = new BaseBar(ZonedDateTime.now().minusDays(3),
                nf.valueOf(10), nf.valueOf(12), nf.valueOf(9), nf.valueOf(11), nf.valueOf(100));
        Bar b2 = new BaseBar(ZonedDateTime.now().minusDays(2),
                nf.valueOf(11), nf.valueOf(13), nf.valueOf(10), nf.valueOf(12), nf.valueOf(150));
        Bar b3 = new BaseBar(ZonedDateTime.now().minusDays(1),
                nf.valueOf(12), nf.valueOf(14), nf.valueOf(11), nf.valueOf(13), nf.valueOf(200));

        return new BaseBarSeriesBuilder()
                .withName("test-series")
                .withNumFactory(nf)
                .withBars(List.of(b1, b2, b3))
                .build();
    }

    @Before
    public void setUp() {
        series = createThreeBarSeries();
    }

    /* --------------------------------------------------------------
     *  Constructor argument validation
     * -------------------------------------------------------------- */
    @Test(expected = NullPointerException.class)
    public void ctor_nullBarSeries_throwsNPE() {
        new BarSeriesManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void ctor_nullTransactionCostModel_throwsNPE() {
        new BarSeriesManager(series, null, zeroHoldingCost, new SimpleExecutionModel());
    }

    @Test(expected = NullPointerException.class)
    public void ctor_nullHoldingCostModel_throwsNPE() {
        new BarSeriesManager(series, zeroTransactionCost, null, new SimpleExecutionModel());
    }

    @Test(expected = NullPointerException.class)
    public void ctor_nullExecutionModel_throwsNPE() {
        new BarSeriesManager(series, zeroTransactionCost, zeroHoldingCost, null);
    }

    @Test(expected = NullPointerException.class)
    public void ctor_nullTradingRecordFactory_throwsNPE() {
        new BarSeriesManager(series, zeroTransactionCost, zeroHoldingCost,
                new SimpleExecutionModel(), null);
    }

    /* --------------------------------------------------------------
     *  Getter methods – snapshot behavior
     * -------------------------------------------------------------- */
    @Test
    public void getBarSeries_returnsSnapshot_notSameInstance() {
        BarSeriesManager manager = new BarSeriesManager(series);
        BarSeries snapshot = manager.getBarSeries();

        assertNotSame("Snapshot must be a different instance", series, snapshot);
        assertEquals("Snapshot must contain the same number of bars", series.getBarCount(),
                snapshot.getBarCount());
    }

    @Test
    public void getTransactionCostModel_returnsConfiguredInstance() {
        BarSeriesManager manager = new BarSeriesManager(series, zeroTransactionCost,
                zeroHoldingCost, new SimpleExecutionModel());
        assertSame(zeroTransactionCost, manager.getTransactionCostModel());
    }

    @Test
    public void getHoldingCostModel_returnsConfiguredInstance() {
        BarSeriesManager manager = new BarSeriesManager(series, zeroTransactionCost,
                zeroHoldingCost, new SimpleExecutionModel());
        assertSame(zeroHoldingCost, manager.getHoldingCostModel());
    }

    /* --------------------------------------------------------------
     *  createDefaultTradingRecord – clamping & factory validation
     * -------------------------------------------------------------- */
    @Test
    public void createDefaultTradingRecord_clampsIndexes() {
        // custom factory that records received indexes
        final int[] received = new int[2];
        BarSeriesManager.TradingRecordFactory factory = (type, start, end, tx, hold) -> {
            received[0] = start;
            received[1] = end;
            return new BaseTradingRecord(type);
        };

        BarSeriesManager manager = new BarSeriesManager(series, zeroTransactionCost,
                zeroHoldingCost, new SimpleExecutionModel(), factory);

        // request a range that exceeds the series bounds
        manager.run(new AlwaysOperateStrategy(Trade.TradeType.BUY), Trade.TradeType.BUY,
                series.numFactory().one(), -5, 100);

        // start should be clamped to series.getBeginIndex()
        // end   should be clamped to series.getEndIndex()
        assertEquals(series.getBeginIndex(), received[0]);
        assertEquals(series.getEndIndex(), received[1]);
    }

    @Test(expected = IllegalStateException.class)
    public void createDefaultTradingRecord_factoryReturnsNull_throws() {
        BarSeriesManager.TradingRecordFactory nullFactory = (t, s, e, tx, hold) -> null;
        BarSeriesManager manager = new BarSeriesManager(series, zeroTransactionCost,
                zeroHoldingCost, new SimpleExecutionModel(), nullFactory);
        // invoke any run overload that triggers factory usage
        manager.run(new AlwaysOperateStrategy(Trade.TradeType.BUY));
    }

    /* --------------------------------------------------------------
     *  run(...) overloads – basic successful execution
     * -------------------------------------------------------------- */
    @Test
    public void run_withStrategyOnly_usesStartingTypeAndDefaultAmount() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.SELL);
        TradingRecord record = manager.run(strategy);

        // The strategy always operates; with 3 bars we expect 3 trades (entry‑exit‑entry‑…
        // The simple execution model alternates entry/exit, ending with an entry
        // because the number of bars is odd.
        assertEquals(3, record.getTrades().size());
        // First trade must be a SELL (starting type)
        assertEquals(Trade.TradeType.SELL, record.getTrades().get(0).getTradeType());
    }

    @Test
    public void run_withExplicitAmount_usesProvidedAmount() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        Num amount = series.numFactory().valueOf(5);
        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        TradingRecord record = manager.run(strategy, Trade.TradeType.BUY, amount);

        // Verify that each recorded trade contains the exact amount we passed.
        for (Trade trade : record.getTrades()) {
            assertEquals("Each trade must use the supplied amount", amount, trade.getAmount());
        }
    }

    @Test
    public void run_withPositionSizer_usesSizerAmount() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        Num fixedAmount = series.numFactory().valueOf(7);
        PositionSizer sizer = new FixedAmountSizer(fixedAmount);
        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        TradingRecord record = manager.run(strategy, Trade.TradeType.BUY, sizer);

        // All trades must have the amount supplied by the sizer.
        for (Trade trade : record.getTrades()) {
            assertEquals(fixedAmount, trade.getAmount());
        }
    }

    @Test
    public void run_withCustomTradingRecord_mutatesSuppliedRecord() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        BaseTradingRecord customRecord = new BaseTradingRecord(Trade.TradeType.BUY);
        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        TradingRecord result = manager.run(strategy, customRecord, series.numFactory().one());

        // The returned instance must be the same as the supplied one.
        assertSame(customRecord, result);
        // Ensure at least one trade was recorded.
        assertFalse(result.getTrades().isEmpty());
    }

    /* --------------------------------------------------------------
     *  Amount validation – negative / zero / NaN
     * -------------------------------------------------------------- */
    @Test(expected = IllegalArgumentException.class)
    public void amountValidation_negativeAmount_throws() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());
        PositionSizer badSizer = new FixedAmountSizer(series.numFactory().valueOf(-1));
        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        manager.run(strategy, Trade.TradeType.BUY, badSizer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void amountValidation_zeroAmount_throws() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());
        PositionSizer badSizer = new FixedAmountSizer(series.numFactory().zero());
        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        manager.run(strategy, Trade.TradeType.BUY, badSizer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void amountValidation_NaNAmount_throws() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());
        PositionSizer badSizer = new FixedAmountSizer(DecimalNum.NaN);
        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        manager.run(strategy, Trade.TradeType.BUY, badSizer);
    }

    /* --------------------------------------------------------------
     *  fallbackSizingTarget – empty series case
     * -------------------------------------------------------------- */
    @Test
    public void fallbackSizingTarget_onEmptySeries_returnsOne() {
        // Build an empty series (no bars)
        BarSeries empty = new BaseBarSeriesBuilder()
                .withName("empty")
                .withNumFactory(DecimalNum::valueOf)
                .build();

        BarSeriesManager manager = new BarSeriesManager(empty, new SimpleExecutionModel());

        // Directly invoke the private method via reflection
        try {
            java.lang.reflect.Method m = BarSeriesManager.class
                    .getDeclaredMethod("fallbackSizingTarget", int.class);
            m.setAccessible(true);
            ExecutionTarget target = (ExecutionTarget) m.invoke(manager, 5);
            // Index is clamped to begin (which is 0 for an empty series)
            assertEquals(0, target.index());
            // Price must be the default one‑unit value
            assertEquals(empty.numFactory().one(), target.price());
        } catch (Exception e) {
            fail("Reflection failed: " + e);
        }
    }

    /* --------------------------------------------------------------
     *  run(...) with range that ends before series end – ensures
     *  post‑run onRunEnd is called.
     * -------------------------------------------------------------- */
    @Test
    public void run_withLimitedRange_executesOnRunEnd() {
        // Spy‑like execution model that records the lastProcessedIndex passed to onRunEnd
        class RecordingExecutionModel extends SimpleExecutionModel {
            int lastProcessed = -1;

            @Override
            public void onRunEnd(int lastProcessedIndex, TradingRecord tradingRecord) {
                this.lastProcessed = lastProcessedIndex;
            }
        }

        RecordingExecutionModel exec = new RecordingExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, exec);

        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        // Run only on the first two bars (indexes 0 and 1)
        manager.run(strategy, Trade.TradeType.BUY, series.numFactory().one(), 0, 1);

        // The last processed index must be 1 (the finish index)
        assertEquals(1, exec.lastProcessed);
    }

    /* --------------------------------------------------------------
     *  run with startIndex > endIndex – should clamp and still run
     * -------------------------------------------------------------- */
    @Test
    public void run_startGreaterThanEnd_clampsAndExecutes() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        // Intentionally reversed indexes
        TradingRecord record = manager.run(strategy, Trade.TradeType.BUY,
                series.numFactory().one(), 5, 2);

        // After clamping start becomes beginIndex (0) and end becomes endIndex (2)
        // Hence we expect three trades (one per bar)
        assertEquals(3, record.getTrades().size());
    }

    /* --------------------------------------------------------------
     *  runWalkForward – basic non‑null result (uses default config)
     * -------------------------------------------------------------- */
    @Test
    public void runWalkForward_returnsResult() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        Strategy strategy = new AlwaysOperateStrategy(Trade.TradeType.BUY);
        WalkForwardConfig cfg = WalkForwardConfig.defaultConfig(series);

        StrategyWalkForwardExecutionResult result = manager.runWalkForward(strategy, cfg);
        assertNotNull("runWalkForward must return a non‑null result", result);
        // The result should contain the original series and strategy.
        assertSame(series, result.barSeries());
        assertSame(strategy, result.strategy());
    }

}
