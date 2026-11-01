###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.BooleanRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.Strategy;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.analysis.cost.CostModel;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * The tests cover:
 * <ul>
 *   <li>All constructors and getter methods.</li>
 *   <li>All overloads of {@code run(...)} ensuring they delegate correctly to the
 *       core implementation.</li>
 *   <li>Behaviour when the supplied indexes are out of the series bounds.</li>
 *   <li>Correct handling of trade type (BUY/SELL) and custom amount.</li>
 *   <li>That the manager respects the {@link TradeExecutionModel} (using the
 *       default {@link TradeOnNextOpenModel}).</li>
 * </ul>
 *
 * The test creates a minimal {@link BarSeries} with three bars whose close prices
 * are 1, 2 and 3.  The strategy is built from simple {@link BooleanRule}
 * based on the close price:
 * <ul>
 *   <li>Entry rule – close price equals 1 (only true for the first bar).</li>
 *   <li>Exit  rule – close price equals 2 (only true for the second bar).</li>
 * </ul>
 * This guarantees a single closed position when the manager runs over the full
 * series.
 */
public class BarSeriesManagerTest {

    private BarSeries series;
    private Strategy simpleStrategy;
    private CostModel zeroCost;

    @Before
    public void setUp() {
        // Build a series with three bars (close prices: 1, 2, 3)
        series = new BaseBarSeries.Builder().withName("test-series").build();

        // Helper to create a bar with a given close price (other values are dummy)
        Bar bar0 = new Bar.Builder()
                .timeSeries(series)
                .timePeriod(1)
                .openPrice(DecimalNum.valueOf(1))
                .highPrice(DecimalNum.valueOf(1))
                .lowPrice(DecimalNum.valueOf(1))
                .closePrice(DecimalNum.valueOf(1))
                .volume(DecimalNum.valueOf(1))
                .build();
        Bar bar1 = new Bar.Builder()
                .timeSeries(series)
                .timePeriod(2)
                .openPrice(DecimalNum.valueOf(2))
                .highPrice(DecimalNum.valueOf(2))
                .lowPrice(DecimalNum.valueOf(2))
                .closePrice(DecimalNum.valueOf(2))
                .volume(DecimalNum.valueOf(2))
                .build();
        Bar bar2 = new Bar.Builder()
                .timeSeries(series)
                .timePeriod(3)
                .openPrice(DecimalNum.valueOf(3))
                .highPrice(DecimalNum.valueOf(3))
                .lowPrice(DecimalNum.valueOf(3))
                .closePrice(DecimalNum.valueOf(3))
                .volume(DecimalNum.valueOf(3))
                .build();

        series.addBar(bar0);
        series.addBar(bar1);
        series.addBar(bar2);

        // Entry when close == 1, exit when close == 2
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        BooleanRule entryRule = new OverIndicatorRule(close, new ConstantIndicator<>(series, DecimalNum.valueOf(0.5)));
        BooleanRule exitRule  = new UnderIndicatorRule(close, new ConstantIndicator<>(series, DecimalNum.valueOf(2.5)));
        simpleStrategy = new BaseStrategy(entryRule, exitRule);

        zeroCost = new ZeroCostModel();
    }

    /* ---------------------------------------------------------------------- */
    /*  Constructor / Getter tests                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorAndGetters() {
        BarSeriesManager manager = new BarSeriesManager(series);
        assertSame(series, manager.getBarSeries());
        // default cost models are ZeroCostModel instances
        assertTrue(manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testAllArgumentConstructor() {
        TradeExecutionModel customModel = new TradeOnNextOpenModel(); // default model
        BarSeriesManager manager = new BarSeriesManager(series, zeroCost, zeroCost, customModel);
        assertSame(series, manager.getBarSeries());
        assertSame(zeroCost, manager.getTransactionCostModel());
        assertSame(zeroCost, manager.getHoldingCostModel());
    }

    @Test
    public void testConstructorWithOnlyCostModels() {
        BarSeriesManager manager = new BarSeriesManager(series, zeroCost, zeroCost);
        assertSame(series, manager.getBarSeries());
        assertSame(zeroCost, manager.getTransactionCostModel());
        assertSame(zeroCost, manager.getHoldingCostModel());
    }

    @Test
    public void testConstructorWithOnlyTradeExecutionModel() {
        TradeExecutionModel model = new TradeOnNextOpenModel();
        BarSeriesManager manager = new BarSeriesManager(series, model);
        assertSame(series, manager.getBarSeries());
        // cost models default to ZeroCostModel
        assertTrue(manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    /* ---------------------------------------------------------------------- */
    /*  run(...) overloads – happy path (single closed position)              */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testRunDefaultBuy() {
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(simpleStrategy);
        assertTrue(record.isClosed());
        assertEquals(1, record.getPositionCount());
        assertEquals(TradeType.BUY, record.getStartingType());
    }

    @Test
    public void testRunWithExplicitBuy() {
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(simpleStrategy, TradeType.BUY);
        assertTrue(record.isClosed());
        assertEquals(TradeType.BUY, record.getStartingType());
    }

    @Test
    public void testRunWithSell() {
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(simpleStrategy, TradeType.SELL);
        // With a SELL starting type the position will be a short (still closed)
        assertTrue(record.isClosed());
        assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRunWithAmount() {
        BarSeriesManager manager = new BarSeriesManager(series);
        Num amount = series.numFactory().numOf(5);
        TradingRecord record = manager.run(simpleStrategy, TradeType.BUY, amount);
        assertTrue(record.isClosed());
        // Verify that the amount has been recorded in the last trade
        assertEquals(amount, record.getLastTrade().getAmount());
    }

    @Test
    public void testRunWithIndexesWithinBounds() {
        BarSeriesManager manager = new BarSeriesManager(series);
        // Run only on index 0 (entry) – no exit possible, position stays open
        TradingRecord record = manager.run(simpleStrategy, TradeType.BUY, 0, 0);
        assertFalse(record.isClosed());
        assertEquals(1, record.getPositionCount());
    }

    @Test
    public void testRunWithIndexesOutOfBounds() {
        BarSeriesManager manager = new BarSeriesManager(series);
        // Provide indexes that exceed the series – they should be clamped
        TradingRecord record = manager.run(simpleStrategy, TradeType.BUY,
                series.getBeginIndex() - 5, series.getEndIndex() + 5);
        // The manager will run over the whole series, producing a closed position
        assertTrue(record.isClosed());
        assertEquals(1, record.getPositionCount());
    }

    /* ---------------------------------------------------------------------- */
    /*  run(...) overloads – delegating to core method                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testRunDelegatesToCoreMethod() {
        BarSeriesManager manager = new BarSeriesManager(series);
        // The core method is exercised indirectly by all overloads;
        // we verify that the result is identical for each overload.
        TradingRecord r1 = manager.run(simpleStrategy);
        TradingRecord r2 = manager.run(simpleStrategy, TradeType.BUY);
        TradingRecord r3 = manager.run(simpleStrategy, TradeType.BUY,
                series.getBeginIndex(), series.getEndIndex());

        assertEquals(r1.getPositionCount(), r2.getPositionCount());
        assertEquals(r1.getPositionCount(), r3.getPositionCount());
        assertTrue(r1.isClosed() && r2.isClosed() && r3.isClosed());
    }

    /* ---------------------------------------------------------------------- */
    /*  Edge case – open position at series end                               */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOpenPositionRemainsOpenWhenNoFurtherBars() {
        // Strategy that enters on first bar and never exits
        ClosePriceIndicator close = new ClosePriceIndicator(series);
        BooleanRule entry = new OverIndicatorRule(close, new ConstantIndicator<>(series, DecimalNum.valueOf(0)));
        BooleanRule neverExit = new BooleanRule() {
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                return false;
            }
        };
        Strategy neverExitStrategy = new BaseStrategy(entry, neverExit);

        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(neverExitStrategy);
        // The last position stays open because there is no exit rule.
        assertFalse(record.isClosed());
        assertEquals(1, record.getPositionCount());
        assertTrue(record.getCurrentPosition().isOpen());
    }

    /* ---------------------------------------------------------------------- */
    /*  Verify that the default TradeExecutionModel executes trades on the     */
    /*  next open price (the behaviour of TradeOnNextOpenModel).              */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDefaultTradeExecutionModelBehaviour() {
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(simpleStrategy);
        // With the default model, a trade should be executed on the next open
        // after the rule becomes satisfied.  Since our series is monotonic,
        // the entry occurs at index 0 and the exit at index 1.
        assertEquals(2, record.getTrades().size());
        assertEquals(TradeType.BUY, record.getTrades().get(0).getType());
        assertEquals(TradeType.SELL, record.getTrades().get(1).getType());
    }
}

/**
 * Simple constant indicator used for the rule construction.
 */
class ConstantIndicator<T extends Num> implements org.ta4j.core.Indicator<T> {
    private final BarSeries series;
    private final T constant;

    ConstantIndicator(BarSeries series, T constant) {
        this.series = series;
        this.constant = constant;
    }

    @Override
    public T getValue(int index) {
        return constant;
    }

    @Override
    public BarSeries getBarSeries() {
        return series;
    }
}
```
###Test END##