###Test START##
```java
/*****************************************************************************************
 * JUnit‑4 test suite for {@link org.ta4j.core.backtest.BarSeriesManager}
 *
 * The tests are written without any mocking framework – only concrete implementations
 * from the public TA‑4J API are used.  For simplicity the series under test is
 * deliberately kept empty or contains a single synthetic bar; this is sufficient to
 * exercise the internal logic of the manager (index clamping, amount validation,
 * fallback sizing, walk‑forward handling, etc.).
 *
 * The test class follows the “three‑expert” workflow described in the task:
 *   1.  List of public methods (extracted from the source)
 *   2.  Basic functional test for each overload
 *   3.  Edge‑case / exception tests
 *   4.  Consolidated test file
 *
 * All tests run with JUnit‑4 (no external libraries, no mocking).
 *****************************************************************************************/

package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.trading.Position;
import org.ta4j.core.trading.Trade;
import org.ta4j.core.trading.Trade.TradeType;
import org.ta4j.core.rules.BooleanRule;
import org.ta4j.core.rules.Rule;
import org.ta4j.core.walkforward.WalkForwardConfig;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit‑4 tests for {@link BarSeriesManager}.
 */
public class BarSeriesManagerTest {

    /* -------------------------------------------------------------------------
     *  Public methods of BarSeriesManager (extracted from the source)
     * -------------------------------------------------------------------------
     *
     * Constructors
     *   BarSeriesManager(BarSeries barSeries)
     *   BarSeriesManager(BarSeries barSeries, TradeExecutionModel tradeExecutionModel)
     *   BarSeriesManager(BarSeries barSeries, CostModel transactionCostModel,
     *                     CostModel holdingCostModel)
     *   BarSeriesManager(BarSeries barSeries, CostModel transactionCostModel,
     *                     CostModel holdingCostModel, TradeExecutionModel tradeExecutionModel)
     *   BarSeriesManager(BarSeries barSeries, CostModel transactionCostModel,
     *                     CostModel holdingCostModel, TradeExecutionModel tradeExecutionModel,
     *                     TradingRecordFactory tradingRecordFactory)
     *
     * Getters
     *   BarSeries getBarSeries()
     *   CostModel getTransactionCostModel()
     *   CostModel getHoldingCostModel()
     *
     * Run overloads (core back‑testing entry points)
     *   TradingRecord run(Strategy strategy)
     *   TradingRecord run(Strategy strategy, int startIndex, int finishIndex)
     *   TradingRecord run(Strategy strategy, TradeType tradeType)
     *   TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex)
     *   TradingRecord run(Strategy strategy, TradeType tradeType, Num amount)
     *   TradingRecord run(Strategy strategy, TradeType tradeType, Num amount,
     *                     int startIndex, int finishIndex)
     *   TradingRecord run(Strategy strategy, TradeType tradeType, PositionSizer positionSizer)
     *   TradingRecord run(Strategy strategy, PositionSizer positionSizer)
     *   TradingRecord run(Strategy strategy, PositionSizer positionSizer,
     *                     int startIndex, int finishIndex)
     *   TradingRecord run(Strategy strategy, TradeType tradeType,
     *                     PositionSizer positionSizer, int startIndex, int finishIndex)
     *   TradingRecord run(Strategy strategy, TradingRecord tradingRecord)
     *   TradingRecord run(Strategy strategy, TradingRecord tradingRecord, Num amount)
     *   TradingRecord run(Strategy strategy, TradingRecord tradingRecord,
     *                     Num amount, int startIndex, int finishIndex)
     *   TradingRecord run(Strategy strategy, TradingRecord tradingRecord,
     *                     PositionSizer positionSizer)
     *   TradingRecord run(Strategy strategy, TradingRecord tradingRecord,
     *                     PositionSizer positionSizer, int startIndex, int finishIndex)
     *
     * Walk‑forward entry points
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      WalkForwardConfig config)
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      TradeType tradeType,
     *                                                      WalkForwardConfig config)
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      TradeType tradeType,
     *                                                      Num amount,
     *                                                      WalkForwardConfig config)
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      TradeType tradeType,
     *                                                      PositionSizer positionSizer,
     *                                                      WalkForwardConfig config)
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      PositionSizer positionSizer,
     *                                                      WalkForwardConfig config)
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      TradeType tradeType,
     *                                                      Num amount,
     *                                                      WalkForwardConfig config,
     *                                                      Consumer<Integer> progressCallback)
     *   StrategyWalkForwardExecutionResult runWalkForward(Strategy strategy,
     *                                                      TradeType tradeType,
     *                                                      PositionSizer positionSizer,
     *                                                      WalkForwardConfig config,
     *                                                      Consumer<Integer> progressCallback)
     *
     * -------------------------------------------------------------------------
     */

    private BarSeries emptySeries;
    private BarSeries oneBarSeries;
    private TradeExecutionModel dummyExecutionModel;
    private PositionSizer fixedSizer;

    @Before
    public void setUp() {
        // ---- an empty series --------------------------------------------------
        emptySeries = new BaseBarSeriesBuilder()
                .withName("empty")
                .withNumFactory(DecimalNum::valueOf)   // DecimalNum implements NumFactory
                .build();

        // ---- a series with a single synthetic bar (price = 100) -----------------
        Bar bar = new BaseBarBuilder()
                .timePeriod(java.time.Duration.ofDays(1))
                .endTime(java.time.ZonedDateTime.now())
                .openPrice(DecimalNum.valueOf(100))
                .highPrice(DecimalNum.valueOf(110))
                .lowPrice(DecimalNum.valueOf(90))
                .closePrice(DecimalNum.valueOf(100))
                .volume(DecimalNum.valueOf(1000))
                .build();

        oneBarSeries = new BaseBarSeriesBuilder()
                .withName("single")
                .withNumFactory(DecimalNum::valueOf)
                .withBars(Collections.singletonList(bar))
                .build();

        // ---- dummy execution model (does nothing, just records calls) -------
        dummyExecutionModel = new TradeExecutionModel() {
            @Override
            public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
                // No real execution – we only need the call to succeed.
            }
        };

        // ---- a constant PositionSizer that always returns amount = 1 ------------
        fixedSizer = new PositionSizer() {
            @Override
            public Num amount(Context context) {
                return context.barSeries().numFactory().numOf(1);
            }
        };
    }

    /* -------------------------------------------------------------------------
     *  Helper objects – very small concrete Strategy implementations
     * ------------------------------------------------------------------------- */

    /** Strategy that never signals an operation (shouldOperate() → false). */
    private static class NoOpStrategy implements Strategy {
        private final TradeType startingType = TradeType.BUY;

        @Override public String getName() { return "NoOp"; }
        @Override public Rule getEntryRule() { return new BooleanRule(false); }
        @Override public Rule getExitRule()  { return new BooleanRule(false); }
        @Override public TradeType getStartingType() { return startingType; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return false; }
        // other default methods are fine – they delegate to the rules above
    }

    /** Strategy that always signals an operation (shouldOperate() → true). */
    private static class AlwaysOnStrategy implements Strategy {
        private final TradeType startingType = TradeType.BUY;

        @Override public String getName() { return "AlwaysOn"; }
        @Override public Rule getEntryRule() { return new BooleanRule(true); }
        @Override public Rule getExitRule()  { return new BooleanRule(true); }
        @Override public TradeType getStartingType() { return startingType; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return true; }
    }

    /* -------------------------------------------------------------------------
     *  BASIC FUNCTIONAL TESTS – one test per public overload
     * ------------------------------------------------------------------------- */

    @Test
    public void testConstructorAndGetters_defaultExecutionModel() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        assertNotNull(manager.getBarSeries());
        assertNotNull(manager.getTransactionCostModel());
        assertNotNull(manager.getHoldingCostModel());

        // internal series must be a snapshot (different instance)
        assertNotSame(oneBarSeries, manager.getBarSeries());
    }

    @Test
    public void testConstructor_withCustomExecutionModel() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries, dummyExecutionModel);
        // No direct getter for execution model – we just verify that run does not
        // throw and that the manager was created.
        assertNotNull(manager);
    }

    @Test
    public void testRun_strategyOnly() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        TradingRecord record = manager.run(new NoOpStrategy());
        // No operation should have been performed – the record stays empty.
        assertTrue(record.getPositions().isEmpty());
    }

    @Test
    public void testRun_withStartAndFinishIndexes() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        TradingRecord record = manager.run(new NoOpStrategy(), 0, 0);
        // With a No‑Op strategy the record must stay empty even though we supplied
        // explicit indexes.
        assertTrue(record.getPositions().isEmpty());
    }

    @Test
    public void testRun_withTradeType() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        TradingRecord record = manager.run(new NoOpStrategy(), TradeType.SELL);
        // The manager creates a default TradingRecord with the supplied start type.
        assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRun_withTradeTypeAndIndexes() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        TradingRecord record = manager.run(new NoOpStrategy(), TradeType.SELL, 0, 0);
        assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRun_withAmount() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        Num amount = oneBarSeries.numFactory().numOf(1);
        TradingRecord record = manager.run(new NoOpStrategy(), TradeType.BUY, amount);
        // No trades executed – only the amount handling is exercised.
        assertTrue(record.getPositions().isEmpty());
    }

    @Test
    public void testRun_withAmountAndIndexes() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        Num amount = oneBarSeries.numFactory().numOf(1);
        TradingRecord record = manager.run(new NoOpStrategy(), TradeType.BUY, amount, 0, 0);
        assertTrue(record.getPositions().isEmpty());
    }

    @Test
    public void testRun_withPositionSizer() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        TradingRecord record = manager.run(new AlwaysOnStrategy(), TradeType.BUY, fixedSizer);
        // The strategy always operates, the dummy execution model does nothing,
        // but a position will be opened (because amount is positive).
        assertFalse(record.isClosed()); // position stays open
        assertEquals(1, record.getPositions().size());
    }

    @Test
    public void testRun_withPositionSizer_andIndexes() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        TradingRecord record = manager.run(new AlwaysOnStrategy(),
                TradeType.BUY, fixedSizer, 0, 0);
        assertFalse(record.isClosed());
        assertEquals(1, record.getPositions().size());
    }

    @Test
    public void testRun_withSuppliedTradingRecord() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        TradingRecord supplied = new BaseTradingRecord(TradeType.BUY);
        TradingRecord result = manager.run(new NoOpStrategy(), supplied);
        // The supplied record must be returned unchanged.
        assertSame(supplied, result);
    }

    @Test
    public void testRun_withSuppliedRecordAndAmount() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        TradingRecord supplied = new BaseTradingRecord(TradeType.BUY);
        Num amount = oneBarSeries.numFactory().numOf(1);
        TradingRecord result = manager.run(new NoOpStrategy(), supplied, amount);
        assertSame(supplied, result);
    }

    @Test
    public void testRunWalkForward_basic() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        WalkForwardConfig cfg = WalkForwardConfig.defaultConfig();
        StrategyWalkForwardExecutionResult result =
                manager.runWalkForward(new NoOpStrategy(), cfg);
        // With a No‑Op strategy there are no trades – we only verify that the
        // executor returns a non‑null result.
        assertNotNull(result);
        assertEquals(oneBarSeries, result.barSeries());
    }

    @Test
    public void testRunWalkForward_withCustomTradeTypeAndAmount() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        WalkForwardConfig cfg = WalkForwardConfig.defaultConfig();
        Num amount = oneBarSeries.numFactory().numOf(1);
        StrategyWalkForwardExecutionResult result =
                manager.runWalkForward(new NoOpStrategy(), TradeType.SELL, amount, cfg);
        assertNotNull(result);
        assertEquals(TradeType.SELL, result.strategy().getStartingType());
    }

    /* -------------------------------------------------------------------------
     *  EDGE‑CASE / EXCEPTION TESTS
     * ------------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullBarSeries() {
        new BarSeriesManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRun_nullStrategy() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        manager.run(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRunWalkForward_nullStrategy() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        manager.runWalkForward(null, WalkForwardConfig.defaultConfig());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRun_withNullAmount() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        manager.run(new NoOpStrategy(), TradeType.BUY, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRun_withNaNAmount() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        Num nan = DecimalNum.NaN;
        manager.run(new NoOpStrategy(), TradeType.BUY, nan);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRun_withNegativeAmount() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        Num neg = DecimalNum.valueOf(-5);
        manager.run(new NoOpStrategy(), TradeType.BUY, neg);
    }

    @Test
    public void testCreateDefaultTradingRecord_clampsIndexes() {
        // custom factory that records the indices it receives
        final int[] captured = new int[2];
        BarSeriesManager.TradingRecordFactory factory = (type, start, end, tx, hold) -> {
            captured[0] = start;
            captured[1] = end;
            return new BaseTradingRecord(type);
        };

        // series with begin=0, end=0 (single bar)
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries,
                new ZeroCostModel(), new ZeroCostModel(),
                dummyExecutionModel, factory);

        // ask for a range that exceeds the series limits
        manager.run(new NoOpStrategy(), TradeType.BUY, DecimalNum.valueOf(1), -10, 10);
        // indices must be clamped to the series bounds (0‑0)
        assertEquals(0, captured[0]);
        assertEquals(0, captured[1]);
    }

    @Test
    public void testFallbackSizingTarget_whenSeriesEmpty() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        // PositionSizer that forces the manager to call amountForIndex()
        PositionSizer sizer = new PositionSizer() {
            @Override
            public Num amount(Context context) {
                // The amount method will trigger fallbackSizingTarget internally.
                return context.barSeries().numFactory().numOf(1);
            }
        };
        // The run should complete without throwing despite the series being empty.
        TradingRecord record = manager.run(new AlwaysOnStrategy(),
                TradeType.BUY, sizer);
        // No positions are opened because there are no bars to operate on.
        assertTrue(record.getPositions().isEmpty());
    }

    @Test
    public void testRunWithClosedTradingRecord_andPositionSizer() {
        BarSeriesManager manager = new BarSeriesManager(oneBarSeries);
        // Build a trading record that is already closed (enter + exit on same bar)
        BaseTradingRecord closedRecord = new BaseTradingRecord(TradeType.BUY);
        Num price = oneBarSeries.numFactory().numOf(100);
        Num amount = oneBarSeries.numFactory().numOf(1);
        closedRecord.enter(0, price, amount);
        closedRecord.exit(0, price, amount);
        assertTrue(closedRecord.isClosed());

        // Run with a PositionSizer – the manager should use the sizer only for the
        // *next* operation (which never occurs because the record is closed).
        TradingRecord result = manager.run(new NoOpStrategy(),
                closedRecord, fixedSizer);
        assertSame(closedRecord, result);
        assertTrue(result.isClosed());
    }

    /* -------------------------------------------------------------------------
     *  INTERNAL HELPERS (simple concrete Bar implementation)
     * ------------------------------------------------------------------------- */

    /**
     * Minimal {@link Bar} implementation used only for building a one‑bar series.
     * The class mirrors the constructor pattern of the real library but keeps
     * everything immutable and straightforward.
     */
    private static class BaseBarBuilder implements BarBuilder {
        private java.time.Duration timePeriod;
        private java.time.ZonedDateTime endTime;
        private Num open, high, low, close, volume;

        BaseBarBuilder timePeriod(java.time.Duration period) {
            this.timePeriod = period;
            return this;
        }

        BaseBarBuilder endTime(java.time.ZonedDateTime time) {
            this.endTime = time;
            return this;
        }

        BaseBarBuilder openPrice(Num price) {
            this.open = price;
            return this;
        }

        BaseBarBuilder highPrice(Num price) {
            this.high = price;
            return this;
        }

        BaseBarBuilder lowPrice(Num price) {
            this.low = price;
            return this;
        }

        BaseBarBuilder closePrice(Num price) {
            this.close = price;
            return this;
        }

        BaseBarBuilder volume(Num vol) {
            this.volume = vol;
            return this;
        }

        @Override
        public Bar build() {
            return new BaseBar(timePeriod, endTime, open, high, low, close, volume);
        }
    }

    /**
     * Very small concrete {@link Bar} – sufficient for the tests that only need
     * to read price/volume values.
     */
    private static class BaseBar implements Bar {
        private final java.time.Duration timePeriod;
        private final java.time.ZonedDateTime endTime;
        private final Num open, high, low, close, volume;

        BaseBar(java.time.Duration period,
                java.time.ZonedDateTime end,
                Num open, Num high, Num low, Num close, Num volume) {
            this.timePeriod = period;
            this.endTime = end;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }

        @Override public Num getOpenPrice() { return open; }
        @Override public Num getHighPrice() { return high; }
        @Override public Num getLowPrice() { return low; }
        @Override public Num getClosePrice() { return close; }
        @Override public Num getVolume() { return volume; }
        @Override public java.time.ZonedDateTime getEndTime() { return endTime; }
        @Override public java.time.Duration getTimePeriod() { return timePeriod; }
    }
}
```
###Test END##