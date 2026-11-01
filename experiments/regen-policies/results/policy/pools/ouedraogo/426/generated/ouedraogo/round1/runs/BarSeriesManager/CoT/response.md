###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.function.IntFunction;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.Num;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.backtest.TradeExecutionModel.ExecutionTarget;
import org.ta4j.core.backtest.PositionSizer;
import org.ta4j.core.walkforward.WalkForwardConfig;
import org.ta4j.core.backtest.StrategyWalkForwardExecutionResult;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests cover:
 * <ul>
 *   <li>Public API signatures</li>
 *   <li>Basic functional behaviour</li>
 *   <li>Edge‑case handling (null arguments, illegal factory return, etc.)</li>
 * </ul>
 *
 * All tests are written with JUnit 4.
 */
public class BarSeriesManagerTest {

    private BarSeries emptySeries;

    @Before
    public void setUp() {
        // Build an empty series – it is sufficient for the tests that do not
        // depend on actual price data.
        emptySeries = new BaseBarSeriesBuilder()
                .withName("empty")
                .build();
    }

    /**
     * Helper: a very small {@link Strategy} implementation that never triggers a
     * trade.  All Rule‑related methods return {@code null} because they are never
     * consulted – the {@code shouldOperate} method is overridden to always return
     * {@code false}.
     */
    private static Strategy dummyStrategy() {
        return new Strategy() {
            @Override
            public String getName() {
                return "dummy";
            }

            @Override
            public org.ta4j.core.Rule getEntryRule() {
                return null; // not used
            }

            @Override
            public org.ta4j.core.Rule getExitRule() {
                return null; // not used
            }

            @Override
            public TradeType getStartingType() {
                return TradeType.BUY;
            }

            @Override
            public boolean shouldOperate(int index, org.ta4j.core.TradingRecord tradingRecord) {
                // Prevent any trade execution – we only want to test the manager’s
                // plumbing.
                return false;
            }
        };
    }

    /**
     * 1. Verify that the public constructors reject a {@code null} {@link BarSeries}.
     */
    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullBarSeries() {
        new BarSeriesManager(null);
    }

    /**
     * 2. The manager must return a *snapshot* of the original series – i.e. a
     * different instance that contains the same data.
     */
    @Test
    public void getBarSeriesReturnsSnapshot() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        BarSeries snapshot = manager.getBarSeries();

        assertNotNull(snapshot);
        // The snapshot must not be the same reference as the original series.
        assertNotSame(emptySeries, snapshot);
        // The name (and thus the identity) must be equal.
        assertEquals(emptySeries.getName(), snapshot.getName());
    }

    /**
     * 3. All {@code run(...)} overloads require a non‑null {@link Strategy}.
     * Passing {@code null} must raise {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void runWithNullStrategyThrows() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        manager.run((Strategy) null);
    }

    /**
     * 4. {@code runWalkForward(...)} also validates the supplied strategy.
     */
    @Test(expected = NullPointerException.class)
    public void runWalkForwardWithNullStrategyThrows() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        WalkForwardConfig cfg = WalkForwardConfig.defaultConfig();
        manager.runWalkForward(null, cfg);
    }

    /**
     * 5. If a custom {@link TradingRecordFactory} returns {@code null},
     * {@code createDefaultTradingRecord} must throw {@link IllegalStateException}.
     */
    @Test(expected = IllegalStateException.class)
    public void factoryReturningNullCausesIllegalStateException() {
        // Custom factory that deliberately returns null.
        BarSeriesManager.TradingRecordFactory nullFactory = (type, start, end, txCost, holdCost) -> null;

        BarSeriesManager manager = new BarSeriesManager(
                emptySeries,
                new ZeroCostModel(),
                new ZeroCostModel(),
                new TradeOnNextOpenModel(),
                nullFactory);

        // The dummy strategy never triggers a trade, but the manager still needs a
        // TradingRecord instance – the factory will return null and the manager must
        // react with IllegalStateException.
        manager.run(dummyStrategy(), TradeType.BUY, emptySeries.numFactory().one(), 0, 0);
    }

    /**
     * 6. Verify that the manager correctly forwards the {@code getTransactionCostModel}
     * and {@code getHoldingCostModel} getters.
     */
    @Test
    public void costModelGettersReturnSuppliedInstances() {
        ZeroCostModel txModel = new ZeroCostModel();
        ZeroCostModel holdModel = new ZeroCostModel();

        BarSeriesManager manager = new BarSeriesManager(
                emptySeries,
                txModel,
                holdModel,
                new TradeOnNextOpenModel());

        assertSame(txModel, manager.getTransactionCostModel());
        assertSame(holdModel, manager.getHoldingCostModel());
    }

    /**
     * 7. Basic execution path: run a strategy over an empty series.
     * The returned {@link TradingRecord} must be non‑null and in a closed state
     * (no open position because the series contains no bars).
     */
    @Test
    public void runOnEmptySeriesReturnsClosedTradingRecord() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        TradingRecord record = manager.run(dummyStrategy());

        assertNotNull(record);
        // With an empty series the manager never opens a position, therefore the
        // record is considered closed.
        assertTrue(record.isClosed());
    }

    /**
     * 8. Verify that the walk‑forward executor is created and invoked.
     * The test only checks that the method returns a non‑null result; the heavy
     * walk‑forward logic is exercised by the library itself.
     */
    @Test
    public void runWalkForwardReturnsResult() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);
        WalkForwardConfig cfg = WalkForwardConfig.defaultConfig();

        StrategyWalkForwardExecutionResult result = manager.runWalkForward(dummyStrategy(), cfg);
        assertNotNull(result);
        // The result must reference the series and the strategy we supplied.
        assertSame(emptySeries, result.barSeries());
        assertSame(dummyStrategy(), result.strategy());
    }

    /**
     * 9. Verify that the internal method {@code fallbackSizingTarget} works
     * correctly when the series is empty.  The method is private, therefore we
     * invoke it indirectly via the public {@code run(...)} overload that uses a
     * {@link PositionSizer}.  The sizer returns a valid positive amount, so the
     * run completes without throwing.  If the fallback target were wrong, an
     * exception would be raised during amount calculation.
     */
    @Test
    public void runWithPositionSizerOnEmptySeriesDoesNotThrow() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);

        // PositionSizer that always returns a constant amount of 1.
        PositionSizer fixedSizer = PositionSizer.fixed(manager.getBarSeries().numFactory().one());

        // The dummy strategy never triggers a trade, but the manager still asks
        // the sizer for an amount when the record is closed.
        TradingRecord record = manager.run(dummyStrategy(), TradeType.BUY, fixedSizer);

        assertNotNull(record);
        // No trade should have been opened.
        assertTrue(record.isClosed());
    }

    /**
     * 10. Verify that {@code amountForIndex} validates the amount correctly.
     * The sizer returns an invalid (negative) amount, which must cause an
     * {@link IllegalArgumentException}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void runWithInvalidAmountThrows() {
        BarSeriesManager manager = new BarSeriesManager(emptySeries);

        // PositionSizer that returns a negative amount.
        PositionSizer badSizer = new PositionSizer() {
            @Override
            public Num amount(Context context) {
                // Return -1 (negative) which should be rejected.
                return context.numFactory().numOf(-1);
            }
        };

        // The dummy strategy forces the manager to query the sizer.
        Strategy alwaysEnter = new Strategy() {
            @Override
            public String getName() { return "alwaysEnter"; }
            @Override
            public org.ta4j.core.Rule getEntryRule() { return null; }
            @Override
            public org.ta4j.core.Rule getExitRule() { return null; }
            @Override
            public TradeType getStartingType() { return TradeType.BUY; }
            @Override
            public boolean shouldOperate(int index, org.ta4j.core.TradingRecord tradingRecord) {
                // Force the manager to try to open a position.
                return true;
            }
        };

        // This call must throw because the amount is invalid.
        manager.run(alwaysEnter, TradeType.BUY, badSizer, 0, 0);
    }
}
```
###Test END##