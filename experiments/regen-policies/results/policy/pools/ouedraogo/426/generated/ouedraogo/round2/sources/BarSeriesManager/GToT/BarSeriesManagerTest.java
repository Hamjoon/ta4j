package org.ta4j.core.backtest;
import org.ta4j.core.backtest.BarSeriesManager.TradingRecordFactory;
import org.ta4j.core.walkforward.WalkForwardConfig;


import static org.junit.Assert.*;

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
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.BooleanRule;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * <p>
 * The suite follows the steps described in the problem statement:
 * <ul>
 *   <li>list public methods (see the comment block at the top of the file),</li>
 *   <li>provide a basic test for each method,</li>
 *   <li>identify edge‑cases / exceptional situations,</li>
 *   <li>add explicit tests for those edge‑cases,</li>
 *   <li>merge everything into a single test class.</li>
 * </ul>
 * </p>
 *
 * <p>
 * All objects are created using only the public API that appears in the supplied
 * source code or in the minimal dependency signatures. No mocking frameworks are
 * used – concrete implementations from the library are instantiated directly.
 * </p>
 */
public class BarSeriesManagerTest {

    /* --------------------------------------------------------------------- */
    /* 1.  Public methods of BarSeriesManager (extracted for reference)    */
    /* --------------------------------------------------------------------- */
    // Constructors
    //   BarSeriesManager(BarSeries)
    //   BarSeriesManager(BarSeries, TradeExecutionModel)
    //   BarSeriesManager(BarSeries, CostModel, CostModel)
    //   BarSeriesManager(BarSeries, CostModel, CostModel, TradeExecutionModel)
    //   BarSeriesManager(BarSeries, CostModel, CostModel, TradeExecutionModel,
    //                    TradingRecordFactory)
    //
    // Accessors
    //   BarSeries getBarSeries()
    //   CostModel getTransactionCostModel()
    //   CostModel getHoldingCostModel()
    //
    // run(...) overloads (many – see source)
    //   TradingRecord run(Strategy)
    //   TradingRecord run(Strategy, int, int)
    //   TradingRecord run(Strategy, TradeType)
    //   TradingRecord run(Strategy, TradeType, int, int)
    //   TradingRecord run(Strategy, TradeType, Num)
    //   TradingRecord run(Strategy, TradeType, Num, int, int)
    //   TradingRecord run(Strategy, TradeType, PositionSizer)
    //   TradingRecord run(Strategy, PositionSizer)
    //   TradingRecord run(Strategy, PositionSizer, int, int)
    //   TradingRecord run(Strategy, TradeType, PositionSizer, int, int)
    //   TradingRecord run(Strategy, TradingRecord)
    //   TradingRecord run(Strategy, TradingRecord, Num)
    //   TradingRecord run(Strategy, TradingRecord, Num, int, int)
    //   TradingRecord run(Strategy, TradingRecord, PositionSizer)
    //   TradingRecord run(Strategy, TradingRecord, PositionSizer, int, int)
    //
    // Walk‑forward overloads
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, WalkForwardConfig)
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, TradeType,
    //                                                   WalkForwardConfig)
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, TradeType, Num,
    //                                                   WalkForwardConfig)
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, TradeType, PositionSizer,
    //                                                   WalkForwardConfig)
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, PositionSizer,
    //                                                   WalkForwardConfig)
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, TradeType, Num,
    //                                                   WalkForwardConfig,
    //                                                   Consumer<Integer>)
    //   StrategyWalkForwardExecutionResult runWalkForward(Strategy, TradeType,
    //                                                   PositionSizer,
    //                                                   WalkForwardConfig,
    //                                                   Consumer<Integer>)
    //
    // Private helpers (tested indirectly)
    //   createDefaultTradingRecord(...)
    //   runWithPositionSizer(...)
    //   amountForIndex(...)
    //   amountForNextOperation(...)
    //   estimateEntryTarget(...)
    //   fallbackSizingTarget(...)
    //   snapshotSeries(...)

    /* --------------------------------------------------------------------- */
    /* 2.  Test fixtures                                                   */
    /* --------------------------------------------------------------------- */

    private BarSeries series;          // the original series (mutable)
    private BarSeries immutableSeries; // the snapshot kept inside the manager
    private BarSeriesManager manager;

    /** a minimal strategy that never triggers any trade */
    private static final Strategy NOOP_STRATEGY = new Strategy() {
        @Override public String getName() { return "noop"; }
        @Override public Rule getEntryRule() { return new BooleanRule(false); }
        @Override public Rule getExitRule() { return new BooleanRule(false); }
        @Override public TradeType getStartingType() { return TradeType.BUY; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return false; }
    };

    /** a strategy that always enters on the first bar (used for open‑position tests) */
    private static final Strategy ALWAYS_ENTER_STRATEGY = new Strategy() {
        @Override public String getName() { return "alwaysEnter"; }
        @Override public Rule getEntryRule() { return new BooleanRule(true); }
        @Override public Rule getExitRule() { return new BooleanRule(false); }
        @Override public TradeType getStartingType() { return TradeType.BUY; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            // enter only once, then rely on the default “isClosed” logic
            return index == tradingRecord.getStartIndex();
        }
    };

    @Before
    public void setUp() {
        // Build a tiny deterministic series (3 bars, daily close prices 10, 11, 12)
        Bar b1 = new BaseBar(java.time.Duration.ofDays(1),
                java.time.ZonedDateTime.now(),
                DecimalNum.valueOf(10), DecimalNum.valueOf(10),
                DecimalNum.valueOf(10), DecimalNum.valueOf(10),
                DecimalNum.valueOf(0));
        Bar b2 = new BaseBar(java.time.Duration.ofDays(1),
                java.time.ZonedDateTime.now().plusDays(1),
                DecimalNum.valueOf(11), DecimalNum.valueOf(11),
                DecimalNum.valueOf(11), DecimalNum.valueOf(11),
                DecimalNum.valueOf(0));
        Bar b3 = new BaseBar(java.time.Duration.ofDays(1),
                java.time.ZonedDateTime.now().plusDays(2),
                DecimalNum.valueOf(12), DecimalNum.valueOf(12),
                DecimalNum.valueOf(12), DecimalNum.valueOf(12),
                DecimalNum.valueOf(0));

        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .withBars(java.util.Arrays.asList(b1, b2, b3))
                .build();

        // The manager will take a snapshot of the series in its constructor
        manager = new BarSeriesManager(series);
        immutableSeries = manager.getBarSeries(); // snapshot
    }

    /* --------------------------------------------------------------------- */
    /* 3.  Basic constructor / accessor tests                              */
    /* --------------------------------------------------------------------- */

    @Test
    public void testConstructorWithOnlySeries() {
        BarSeriesManager m = new BarSeriesManager(series);
        assertNotNull(m.getBarSeries());
        assertNotNull(m.getTransactionCostModel());
        assertNotNull(m.getHoldingCostModel());
    }

    @Test
    public void testConstructorWithAllParameters() {
        TradeExecutionModel model = new TradeOnNextOpenModel(); // default implementation
        CostModel tx = new ZeroCostModel();
        CostModel hold = new ZeroCostModel();

        BarSeriesManager m = new BarSeriesManager(series, tx, hold, model);
        assertEquals(tx, m.getTransactionCostModel());
        assertEquals(hold, m.getHoldingCostModel());
    }

    @Test
    public void testGetBarSeriesReturnsSnapshot() {
        // modify the original series after manager creation
        Bar extra = new BaseBar(java.time.Duration.ofDays(1),
                java.time.ZonedDateTime.now().plusDays(3),
                DecimalNum.valueOf(13), DecimalNum.valueOf(13),
                DecimalNum.valueOf(13), DecimalNum.valueOf(13),
                DecimalNum.valueOf(0));
        series.addBar(extra);

        // manager's series must stay unchanged
        assertEquals(3, immutableSeries.getBarCount());
        assertEquals(4, series.getBarCount());
    }

    @Test
    public void testGetTransactionAndHoldingCostModels() {
        assertTrue(manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    /* --------------------------------------------------------------------- */
    /* 4.  Basic run() overload tests                                       */
    /* --------------------------------------------------------------------- */

    @Test
    public void testRunStrategyDefault() {
        TradingRecord tr = manager.run(NOOP_STRATEGY);
        assertNotNull(tr);
        // No trades should have been recorded
        assertTrue(tr.getTrades().isEmpty());
    }

    @Test
    public void testRunStrategyWithExplicitIndexes() {
        // start index before series begin, finish index after series end – should be clamped
        TradingRecord tr = manager.run(NOOP_STRATEGY, -5, 100);
        assertNotNull(tr);
        // the start/end indexes inside the record are the clamped ones
        assertEquals(Integer.valueOf(series.getBeginIndex()), tr.getStartIndex());
        assertEquals(Integer.valueOf(series.getEndIndex()), tr.getEndIndex());
    }

    @Test
    public void testRunStrategyWithTradeTypeAndAmount() {
        Num amount = series.numFactory().numOf(1);
        TradingRecord tr = manager.run(NOOP_STRATEGY, TradeType.SELL, amount);
        assertNotNull(tr);
        // still no trades because the strategy never signals
        assertTrue(tr.getTrades().isEmpty());
    }

    @Test
    public void testRunWithCustomTradingRecord() {
        TradingRecord custom = new BaseTradingRecord(TradeType.BUY);
        TradingRecord result = manager.run(NOOP_STRATEGY, custom);
        assertSame(custom, result);
        assertTrue(custom.getTrades().isEmpty());
    }

    /* --------------------------------------------------------------------- */
    /* 5.  Edge‑case / exception tests                                      */
    /* --------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testRunWithNullAmountThrows() {
        manager.run(NOOP_STRATEGY, TradeType.BUY, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunWithInvalidAmountFromPositionSizer() {
        // PositionSizer.fixed(Double.NaN) will produce a NaN amount -> validation fails
        PositionSizer badSizer = PositionSizer.fixed(Double.NaN);
        manager.run(NOOP_STRATEGY, TradeType.BUY, badSizer);
    }

    @Test(expected = IllegalStateException.class)
    public void testFactoryReturningNullThrows() {
        BarSeriesManager m = new BarSeriesManager(
                series,
                new ZeroCostModel(),
                new ZeroCostModel(),
                new TradeOnNextOpenModel(),
                (type, start, end, tx, hold) -> null   // deliberately return null
        );
        // any run that reaches createDefaultTradingRecord will hit the null factory
        m.run(NOOP_STRATEGY);
    }

    @Test
    public void testFallbackSizingTargetWhenSeriesIsEmpty() {
        // create an empty series
        BarSeries empty = new BaseBarSeriesBuilder()
                .withName("empty")
                .withBars(Collections.emptyList())
                .build();

        // custom execution model that always returns null for entry target
        TradeExecutionModel nullTargetModel = new TradeExecutionModel() {
            @Override public ExecutionTarget estimateEntryTarget(int signalIndex, BarSeries barSeries,
                    TradeType tradeType) {
                return null; // force fallback
            }
            @Override public void execute(int index, TradingRecord tradingRecord,
                    BarSeries barSeries, Num amount) {
                // no‑op
            }
        };

        BarSeriesManager m = new BarSeriesManager(empty, nullTargetModel);
        // PositionSizer.fixed(1) is valid – run must succeed and use fallback target
        TradingRecord tr = m.run(NOOP_STRATEGY, TradeType.BUY, PositionSizer.fixed(1));
        assertNotNull(tr);
        // Since the series is empty, there are no trades
        assertTrue(tr.getTrades().isEmpty());
    }

    @Test
    public void testAmountForNextOperationUsesCurrentPositionAmountWhenOpen() {
        // Use a strategy that opens a position on the first bar
        // The default TradeOnNextOpenModel will open the trade with amount = 1
        Num amount = series.numFactory().numOf(1);
        TradingRecord tr = manager.run(ALWAYS_ENTER_STRATEGY, TradeType.BUY, amount);
        assertFalse(tr.isClosed());
        // The opened position amount must be exactly the amount we passed
        assertEquals(amount, tr.getCurrentPosition().amount());
    }

    @Test
    public void testRunWalkForwardBasic() {
        // Walk‑forward config with trivial values (single fold)
        WalkForwardConfig cfg = WalkForwardConfig.defaultConfig(series);
        StrategyWalkForwardExecutionResult result = manager.runWalkForward(NOOP_STRATEGY, cfg);
        assertNotNull(result);
        // No folds should contain any trades because the strategy never operates
        assertTrue(result.tradingRecords().stream()
                .allMatch(r -> r.getTrades().isEmpty()));
    }

    /* --------------------------------------------------------------------- */
    /* 6.  Additional sanity checks (optional)                              */
    /* --------------------------------------------------------------------- */

    @Test
    public void testMultipleRunsDoNotShareState() {
        // first run
        TradingRecord first = manager.run(NOOP_STRATEGY);
        // second run with a different strategy that does enter
        TradingRecord second = manager.run(ALWAYS_ENTER_STRATEGY, TradeType.BUY, DecimalNum.valueOf(1));
        assertNotSame(first, second);
        // first record must still be empty
        assertTrue(first.getTrades().isEmpty());
        // second must contain at least one trade
        assertFalse(second.getTrades().isEmpty());
    }

    @Test
    public void testCustomTradingRecordFactoryIsRespected() {
        TradingRecordFactory countingFactory = (type, start, end, tx, hold) -> {
            // we embed the start/end indexes into the record name for verification
            BaseTradingRecord rec = new BaseTradingRecord(type);
            rec.setName(start + "-" + end);
            return rec;
        };
        BarSeriesManager m = new BarSeriesManager(series, new ZeroCostModel(),
                new ZeroCostModel(), new TradeOnNextOpenModel(), countingFactory);
        TradingRecord tr = m.run(NOOP_STRATEGY, 0, series.getEndIndex());
        assertEquals(series.getBeginIndex() + "-" + series.getEndIndex(), tr.getName());
    }

}
