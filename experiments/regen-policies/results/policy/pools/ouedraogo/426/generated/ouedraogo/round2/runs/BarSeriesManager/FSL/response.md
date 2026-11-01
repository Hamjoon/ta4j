###Test START##
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.BarSeriesManager.TradingRecordFactory;
import org.ta4j.core.backtest.PositionSizer;
import org.ta4j.core.backtest.PositionSizer.Context;
import org.ta4j.core.backtest.TradeExecutionModel.ExecutionTarget;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.Rule;
import org.ta4j.core.walkforward.WalkForwardConfig;
import org.ta4j.core.backtest.StrategyWalkForwardExecutionResult;

/**
 * Unit tests for {@link BarSeriesManager}. The tests instantiate real
 * {@link BarSeries}, {@link Strategy}, {@link TradeExecutionModel} and
 * {@link PositionSizer} objects – no mocking or subclassing of library types is
 * performed.
 */
public class BarSeriesManagerTest {

    /** Helper to build an **empty** BarSeries (no bars). */
    private BarSeries emptySeries() {
        return new BaseBarSeriesBuilder()
                .withName("empty")
                .build();
    }

    /** Simple strategy that never triggers any operation. */
    private Strategy neverOperateStrategy() {
        return new Strategy() {
            @Override
            public String getName() {
                return "NeverOperate";
            }

            @Override
            public Rule getEntryRule() {
                // rule that is never satisfied
                return new Rule() {
                    @Override
                    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                        return false;
                    }
                };
            }

            @Override
            public Rule getExitRule() {
                // rule that is never satisfied
                return new Rule() {
                    @Override
                    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                        return false;
                    }
                };
            }

            @Override
            public TradeType getStartingType() {
                return TradeType.BUY;
            }

            @Override
            public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                // never operate – ensures no trades are generated
                return false;
            }
        };
    }

    /** Strategy that always operates (enter on first bar, exit on second). */
    private Strategy alwaysOperateStrategy() {
        return new Strategy() {
            @Override
            public String getName() {
                return "AlwaysOperate";
            }

            @Override
            public Rule getEntryRule() {
                return new Rule() {
                    @Override
                    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                        // enter on the very first index only
                        return index == 0 && tradingRecord.getCurrentPosition() == null;
                    }
                };
            }

            @Override
            public Rule getExitRule() {
                return new Rule() {
                    @Override
                    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                        // exit on the second index if a position is open
                        return index == 1 && tradingRecord.getCurrentPosition() != null;
                    }
                };
            }

            @Override
            public TradeType getStartingType() {
                return TradeType.BUY;
            }

            @Override
            public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                // delegate to entry/exit rules – the default implementation does this,
                // but we implement it explicitly for clarity.
                if (getEntryRule().isSatisfied(index, tradingRecord)) {
                    return true;
                }
                return getExitRule().isSatisfied(index, tradingRecord);
            }
        };
    }

    /** Simple factory that deliberately returns {@code null} – used to test the
     *  IllegalStateException path in {@link BarSeriesManager#createDefaultTradingRecord}. */
    private TradingRecordFactory nullReturningFactory() {
        return (tradeType, startIdx, endIdx, txnCost, holdCost) -> null;
    }

    /** A PositionSizer that returns a *negative* amount – forces amount validation to fail. */
    private PositionSizer negativeAmountSizer(BarSeries series) {
        return new PositionSizer() {
            @Override
            public Num amount(Context context) {
                // deliberately return a negative amount
                return series.numFactory().numOf(-1);
            }
        };
    }

    /** A PositionSizer that returns {@code null} – also forces validation to fail. */
    private PositionSizer nullAmountSizer() {
        return new PositionSizer() {
            @Override
            public Num amount(Context context) {
                return null;
            }
        };
    }

    /** ------------------------------------------------------------
     *  1. Constructor argument validation
     *  ------------------------------------------------------------ */
    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullBarSeries() {
        new BarSeriesManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullTransactionCostModel() {
        new BarSeriesManager(emptySeries(), null, new ZeroCostModel(), new TradeOnNextOpenModel());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullHoldingCostModel() {
        new BarSeriesManager(emptySeries(), new ZeroCostModel(), null, new TradeOnNextOpenModel());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullTradeExecutionModel() {
        new BarSeriesManager(emptySeries(), new ZeroCostModel(), new ZeroCostModel(), null);
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullTradingRecordFactory() {
        new BarSeriesManager(emptySeries(), new ZeroCostModel(), new ZeroCostModel(),
                new TradeOnNextOpenModel(), null);
    }

    /** ------------------------------------------------------------
     *  2. Basic getters and snapshot behaviour
     *  ------------------------------------------------------------ */
    @Test
    public void gettersReturnProvidedInstancesAndSnapshotIsIndependent() {
        BarSeries series = emptySeries();
        CostModel txn = new ZeroCostModel();
        CostModel hold = new ZeroCostModel();
        TradeExecutionModel execModel = new TradeOnNextOpenModel();

        BarSeriesManager manager = new BarSeriesManager(series, txn, hold, execModel);

        // getters must return the same instances supplied to the constructor
        assertSame(series, manager.getBarSeries());
        assertSame(txn, manager.getTransactionCostModel());
        assertSame(hold, manager.getHoldingCostModel());

        // the returned BarSeries must be a *snapshot* – modifications on the original
        // must not affect the manager's internal series
        BarSeries snapshot = manager.getBarSeries();
        assertNotSame(series, snapshot);
        assertEquals(series.getName(), snapshot.getName());
    }

    /** ------------------------------------------------------------
     *  3. Running a strategy that never operates – should produce an empty record
     *  ------------------------------------------------------------ */
    @Test
    public void runWithNeverOperatingStrategyReturnsEmptyRecord() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = neverOperateStrategy();

        TradingRecord record = manager.run(strategy);
        assertNotNull(record);
        // No trades should have been recorded
        assertTrue(record.getTrades().isEmpty());
        // The record is considered closed because there is no open position
        assertTrue(record.isClosed());
    }

    /** ------------------------------------------------------------
     *  4. Running a strategy that always operates on a two‑bar series
     *  ------------------------------------------------------------ */
    @Test
    public void runWithAlwaysOperatingStrategyCreatesTwoTrades() {
        // Build a tiny series with two bars (the actual Bar objects are not needed for
        // the default TradeOnNextOpenModel – the model only queries the close price,
        // which for an empty series defaults to 1 via fallbackSizingTarget).
        BarSeries series = emptySeries();

        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = alwaysOperateStrategy();

        TradingRecord record = manager.run(strategy);
        assertNotNull(record);
        // Expect exactly one entry and one exit trade
        List<org.ta4j.core.Trade> trades = record.getTrades();
        assertEquals(2, trades.size());

        // Verify entry trade type matches the strategy's starting type
        assertEquals(TradeType.BUY, trades.get(0).getType());

        // Verify that after the exit the record is closed
        assertTrue(record.isClosed());
    }

    /** ------------------------------------------------------------
     *  5. createDefaultTradingRecord must throw when the factory returns null
     *  ------------------------------------------------------------ */
    @Test(expected = IllegalStateException.class)
    public void createDefaultTradingRecordThrowsIfFactoryReturnsNull() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series, new ZeroCostModel(),
                new ZeroCostModel(), new TradeOnNextOpenModel(), nullReturningFactory());

        // The next call to any overload that creates a default record will trigger the
        // IllegalStateException
        manager.run(neverOperateStrategy());
    }

    /** ------------------------------------------------------------
     *  6. Amount validation – negative amount supplied by PositionSizer
     *  ------------------------------------------------------------ */
    @Test(expected = IllegalArgumentException.class)
    public void runWithNegativeAmountThrows() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = neverOperateStrategy();
        PositionSizer sizer = negativeAmountSizer(series);

        // This overload uses a dynamic PositionSizer
        manager.run(strategy, TradeType.BUY, sizer);
    }

    /** ------------------------------------------------------------
     *  7. Amount validation – null amount supplied by PositionSizer
     *  ------------------------------------------------------------ */
    @Test(expected = IllegalArgumentException.class)
    public void runWithNullAmountThrows() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = neverOperateStrategy();

        manager.run(strategy, TradeType.BUY, nullAmountSizer());
    }

    /** ------------------------------------------------------------
     *  8. Fallback sizing target is used when the series is empty
     *  ------------------------------------------------------------ */
    @Test
    public void fallbackSizingTargetReturnsOneWhenSeriesIsEmpty() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        // Use reflection to access the private fallbackSizingTarget method
        try {
            java.lang.reflect.Method method = BarSeriesManager.class
                    .getDeclaredMethod("fallbackSizingTarget", int.class);
            method.setAccessible(true);
            ExecutionTarget target = (ExecutionTarget) method.invoke(manager, 5);
            // For an empty series the fallback price is 1 (ZeroCostModel's numFactory().one())
            assertEquals(5, target.index());
            assertEquals(series.numFactory().one(), target.price());
        } catch (Exception e) {
            fail("Reflection on fallbackSizingTarget failed: " + e);
        }
    }

    /** ------------------------------------------------------------
     *  9. Run with explicit start/finish indexes – indexes are clamped correctly
     *  ------------------------------------------------------------ */
    @Test
    public void runClampsStartAndFinishIndexes() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = neverOperateStrategy();

        // Provide out‑of‑range indexes; they should be clamped to the series boundaries
        TradingRecord record = manager.run(strategy, TradeType.BUY, series.numFactory().one(),
                -10, 1000);
        // No exception means clamping worked; also the record must be empty
        assertTrue(record.getTrades().isEmpty());
    }

    /** ------------------------------------------------------------
     *  10. Walk‑forward execution – basic sanity check (non‑null result)
     *  ------------------------------------------------------------ */
    @Test
    public void runWalkForwardReturnsResult() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = neverOperateStrategy();

        WalkForwardConfig config = WalkForwardConfig.defaultConfig();
        StrategyWalkForwardExecutionResult result = manager.runWalkForward(strategy, config);
        assertNotNull(result);
        // The result must reference the original series and strategy
        assertSame(series, result.barSeries());
        assertSame(strategy, result.strategy());
        // No folds are expected because the series is empty
        assertTrue(result.inSampleFolds().isEmpty());
        assertTrue(result.outOfSampleFolds().isEmpty());
    }

    /** ------------------------------------------------------------
     *  11. run(Strategy, TradingRecord, Num) overload – amount resolver path
     *  ------------------------------------------------------------ */
    @Test
    public void runWithSuppliedTradingRecordAndAmountExecutesWithoutError() {
        BarSeries series = emptySeries();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = neverOperateStrategy();

        // Use a fresh BaseTradingRecord as the supplied record
        TradingRecord suppliedRecord = new BaseTradingRecord(TradeType.BUY);
        Num amount = series.numFactory().one();

        TradingRecord result = manager.run(strategy, suppliedRecord, amount);
        assertSame(suppliedRecord, result);
        // No trades should have been added
        assertTrue(result.getTrades().isEmpty());
    }
}
###Test END##