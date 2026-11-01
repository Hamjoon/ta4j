###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.BooleanRule;
import org.ta4j.core.Strategy;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * <p>The test suite covers:
 *
 * <ul>
 *   <li>All public constructors and accessor methods.</li>
 *   <li>All overloads of the {@code run(...)} method with typical usage.</li>
 *   <li>Edge‑case scenarios such as empty series, out‑of‑range indexes,
 *       zero amount, and different {@link TradeType}s.</li>
 *   <li>Exception‑handling (e.g. {@code null} strategy).</li>
 * </ul>
 *
 * <p>Only classes that are part of the public API of TA‑4J (or appear in the
 * provided signatures) are instantiated – no mocking frameworks are used.
 */
public class BarSeriesManagerTest {

    /** A simple 5‑bar series used by most tests. */
    private BarSeries series;

    /** A dummy strategy that always returns {@code true} for {@code shouldOperate}. */
    private Strategy alwaysTrueStrategy;

    /** A {@link TradeExecutionModel} that simply delegates to {@link TradingRecord#operate(int)}. */
    private TradeExecutionModel simpleExecutionModel;

    @Before
    public void setUp() {
        // Build a tiny series (price values are irrelevant for the manager logic)
        series = new BaseBarSeries("test-series");
        NumFactory nf = series.numFactory();

        // add 5 bars with increasing close price
        for (int i = 0; i < 5; i++) {
            // Using DecimalNum for simplicity (the concrete implementation is part of TA‑4J)
            Bar bar = new org.ta4j.core.BaseBar(
                    nf.numOf(i),          // open
                    nf.numOf(i + 1),      // high
                    nf.numOf(i - 1),      // low
                    nf.numOf(i + 0.5),    // close
                    nf.numOf(1)           // volume
            );
            series.addBar(bar);
        }

        // Strategy that always wants to operate (enter/exit) on every tick
        alwaysTrueStrategy = new Strategy() {
            @Override
            public String getName() {
                return "always-true";
            }

            @Override
            public org.ta4j.core.Rule getEntryRule() {
                return BooleanRule.TRUE;
            }

            @Override
            public org.ta4j.core.Rule getExitRule() {
                return BooleanRule.TRUE;
            }

            @Override
            public Strategy and(Strategy strategy) {
                return this;
            }

            @Override
            public Strategy or(Strategy strategy) {
                return this;
            }

            @Override
            public Strategy and(String name, Strategy strategy, int unstableBars) {
                return this;
            }

            @Override
            public Strategy or(String name, Strategy strategy, int unstableBars) {
                return this;
            }

            @Override
            public Strategy opposite() {
                return this;
            }

            @Override
            public void setUnstableBars(int unstableBars) {
                // no‑op
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public boolean isUnstableAt(int index) {
                return false;
            }

            @Override
            public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                // Force an operation on every index
                return true;
            }
        };

        // Simple execution model: just call TradingRecord.operate(index)
        simpleExecutionModel = new TradeExecutionModel() {
            @Override
            public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
                // BaseTradingRecord will decide whether to enter or exit based on its
                // internal state (entry/exit rules are not needed because we force
                // execution via the manager)
                tradingRecord.operate(index);
            }
        };
    }

    /* ----------------------------------------------------------------------
     *  Constructor and accessor tests
     * ---------------------------------------------------------------------- */

    @Test
    public void testConstructorWithOnlySeries() {
        BarSeriesManager manager = new BarSeriesManager(series);
        assertSame(series, manager.getBarSeries());
        assertTrue(manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testConstructorWithSeriesAndExecutionModel() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        assertSame(series, manager.getBarSeries());
        // Execution model is not exposed, but we can ensure manager still works
        TradingRecord record = manager.run(alwaysTrueStrategy);
        assertNotNull(record);
    }

    @Test
    public void testConstructorWithCustomCostModels() {
        CostModel txCost = new ZeroCostModel();
        CostModel holdCost = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost);
        assertSame(txCost, manager.getTransactionCostModel());
        assertSame(holdCost, manager.getHoldingCostModel());
    }

    @Test
    public void testFullConstructor() {
        CostModel txCost = new ZeroCostModel();
        CostModel holdCost = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost, simpleExecutionModel);
        assertSame(series, manager.getBarSeries());
        assertSame(txCost, manager.getTransactionCostModel());
        assertSame(holdCost, manager.getHoldingCostModel());
    }

    /* ----------------------------------------------------------------------
     *  Basic run(...) overloads – nominal behaviour
     * ---------------------------------------------------------------------- */

    @Test
    public void testRunDefaultBuy() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        TradingRecord record = manager.run(alwaysTrueStrategy);
        // With always‑true strategy, we expect an entry on the first bar and an exit
        // on the second bar (BaseTradingRecord toggles entry/exit on each operate)
        assertEquals(2, record.getPositionCount()); // one closed position
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithCustomTradeTypeSell() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        TradingRecord record = manager.run(alwaysTrueStrategy, TradeType.SELL);
        assertEquals(TradeType.SELL, record.getStartingType());
        assertEquals(2, record.getPositionCount());
    }

    @Test
    public void testRunWithStartAndFinishIndexes() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        // Run only on bars 2‑3 (inclusive)
        TradingRecord record = manager.run(alwaysTrueStrategy, 2, 3);
        // Two bars → one entry and one exit → one closed position
        assertEquals(1, record.getPositionCount());
        assertEquals(Integer.valueOf(2), record.getStartIndex());
        assertEquals(Integer.valueOf(3), record.getEndIndex());
    }

    @Test
    public void testRunWithAmount() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        Num amount = series.numFactory().numOf(10);
        TradingRecord record = manager.run(alwaysTrueStrategy, TradeType.BUY, amount);
        // Amount does not affect position count in this simple execution model
        assertEquals(2, record.getPositionCount());
    }

    @Test
    public void testRunWithAllParameters() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        Num amount = series.numFactory().numOf(5);
        TradingRecord record = manager.run(alwaysTrueStrategy, TradeType.SELL, amount, 1, 4);
        // Operates on indexes 1‑4 → 4 calls → 2 closed positions
        assertEquals(2, record.getPositionCount());
        assertEquals(Integer.valueOf(1), record.getStartIndex());
        assertEquals(Integer.valueOf(4), record.getEndIndex());
    }

    /* ----------------------------------------------------------------------
     *  Edge‑case and exception tests
     * ---------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testRunWithNullStrategyThrows() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        manager.run(null);
    }

    @Test
    public void testRunOnEmptySeries() {
        BarSeries empty = new BaseBarSeries("empty");
        BarSeriesManager manager = new BarSeriesManager(empty, simpleExecutionModel);
        TradingRecord record = manager.run(alwaysTrueStrategy);
        // No bars → no trades
        assertEquals(0, record.getPositionCount());
        assertTrue(record.isClosed()); // trivially closed because nothing opened
    }

    @Test
    public void testRunWithStartIndexGreaterThanEndIndex() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        // startIndex > endIndex → loop should not execute any operation
        TradingRecord record = manager.run(alwaysTrueStrategy, 4, 2);
        assertEquals(0, record.getPositionCount());
    }

    @Test
    public void testRunWithIndexesOutsideSeriesBounds() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        // Provide indexes that exceed the series; manager should clamp them
        TradingRecord record = manager.run(alwaysTrueStrategy, -10, 100);
        // Series has 5 bars → 5 operations → 2 closed positions + 1 open (since odd number)
        assertEquals(2, record.getPositionCount()); // closed positions
        assertFalse(record.isClosed()); // last position remains open
    }

    @Test
    public void testRunWithZeroAmount() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExecutionModel);
        Num zero = series.numFactory().zero();
        TradingRecord record = manager.run(alwaysTrueStrategy, TradeType.BUY, zero);
        // Zero amount does not prevent trades; BaseTradingRecord still records positions
        assertEquals(2, record.getPositionCount());
    }

    @Test
    public void testRunWithCustomCostModelsAffectingResult() {
        // Use a non‑zero cost model to ensure manager forwards the model correctly.
        CostModel txCost = new org.ta4j.core.analysis.cost.FixedTransactionCostModel(series.numFactory().numOf(1));
        CostModel holdCost = new org.ta4j.core.analysis.cost.FixedHoldingCostModel(series.numFactory().numOf(0.5));
        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost, simpleExecutionModel);
        assertSame(txCost, manager.getTransactionCostModel());
        assertSame(holdCost, manager.getHoldingCostModel());

        TradingRecord record = manager.run(alwaysTrueStrategy);
        // Positions are still created; costs are internal to the record and do not affect count
        assertEquals(2, record.getPositionCount());
    }

    /* ----------------------------------------------------------------------
     *  Helper class – FixedTransactionCostModel (simple implementation)
     * ---------------------------------------------------------------------- */
    // The library provides FixedTransactionCostModel; we use it directly in the test above.
    // No additional helper code is required.

}
```
###Test END##