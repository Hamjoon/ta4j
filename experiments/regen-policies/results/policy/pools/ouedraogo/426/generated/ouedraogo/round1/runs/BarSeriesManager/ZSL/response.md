###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.Rule;

/**
 * Comprehensive unit tests for {@link BarSeriesManager}.
 *
 * <p>
 * The tests use only the public API that appears in the supplied source code
 * and the minimal interfaces listed in the prompt. No mocking frameworks are
 * used – real objects are instantiated wherever possible.
 * </p>
 */
public class BarSeriesManagerTest {

    private BarSeries series;
    private NumFactory numFactory;

    /** Helper to create a very small series (5 bars) with constant price 1.0. */
    @Before
    public void setUp() {
        // Build a simple series with 5 bars – each bar has the same close price.
        List<Bar> bars = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            bars.add(new SimpleBar(i));
        }
        series = new BaseBarSeriesBuilder()
                .withName("testSeries")
                .withNumFactory(SimpleNumFactory.INSTANCE)
                .withBars(bars)
                .build();
        numFactory = series.numFactory();
    }

    /** Simple immutable {@link Bar} implementation used only for tests. */
    private static class SimpleBar implements Bar {
        private final int index;

        SimpleBar(int index) {
            this.index = index;
        }

        @Override
        public Num getClosePrice() {
            return SimpleNumFactory.INSTANCE.numOf(1);
        }

        // All other methods are irrelevant for the tests – we return dummy values.
        @Override public Num getOpenPrice() { return getClosePrice(); }
        @Override public Num getMaxPrice() { return getClosePrice(); }
        @Override public Num getMinPrice() { return getClosePrice(); }
        @Override public Num getVolume() { return SimpleNumFactory.INSTANCE.numOf(1); }
        @Override public int getBeginIndex() { return index; }
        @Override public int getEndIndex() { return index; }
        @Override public BarSeries getSeries() { return null; }
    }

    /** Simple {@link Num} implementation based on {@link java.math.BigDecimal}. */
    private static class SimpleNum implements Num {
        private final java.math.BigDecimal value;

        SimpleNum(java.math.BigDecimal v) {
            this.value = v;
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return SimpleNumFactory.INSTANCE; }
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num plus(Num augend) { return new SimpleNum(value.add(((SimpleNum) augend).value)); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(value.subtract(((SimpleNum) subtrahend).value)); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(value.multiply(((SimpleNum) multiplicand).value)); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(value.divide(((SimpleNum) divisor).value, java.math.MathContext.DECIMAL128)); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(value.remainder(((SimpleNum) divisor).value)); }
        @Override public Num floor() { return new SimpleNum(value.setScale(0, java.math.RoundingMode.FLOOR)); }
        @Override public Num ceil() { return new SimpleNum(value.setScale(0, java.math.RoundingMode.CEILING)); }
        @Override public Num pow(int n) { return new SimpleNum(value.pow(n)); }
        @Override public Num pow(Num n) { return pow(n.intValue()); }
        @Override public Num log() { return new SimpleNum(java.math.BigDecimal.valueOf(Math.log(value.doubleValue()))); }
        @Override public Num exp() { return new SimpleNum(java.math.BigDecimal.valueOf(Math.exp(value.doubleValue()))); }
        @Override public Num sqrt() { return new SimpleNum(java.math.BigDecimal.valueOf(Math.sqrt(value.doubleValue()))); }
        @Override public Num sqrt(java.math.MathContext mc) { return new SimpleNum(java.math.BigDecimal.valueOf(Math.sqrt(value.doubleValue()), mc)); }
        @Override public Num abs() { return new SimpleNum(value.abs()); }
        @Override public Num negate() { return new SimpleNum(value.negate()); }
        @Override public boolean isZero() { return value.compareTo(java.math.BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive() { return value.compareTo(java.math.BigDecimal.ZERO) > 0; }
        @Override public boolean isPositiveOrZero() { return value.compareTo(java.math.BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegative() { return value.compareTo(java.math.BigDecimal.ZERO) < 0; }
        @Override public boolean isNegativeOrZero() { return value.compareTo(java.math.BigDecimal.ZERO) <= 0; }
        @Override public boolean isEqual(Num other) { return compareTo(other) == 0; }
        @Override public boolean isGreaterThan(Num other) { return compareTo(other) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return compareTo(other) >= 0; }
        @Override public boolean isLessThan(Num other) { return compareTo(other) < 0; }
        @Override public boolean isLessThanOrEqual(Num other) { return compareTo(other) <= 0; }
        @Override public Num min(Num other) { return compareTo(other) <= 0 ? this : other; }
        @Override public Num max(Num other) { return compareTo(other) >= 0 ? this : other; }
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public java.math.BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toString(); }
        @Override public boolean equals(Object obj) { return (obj instanceof SimpleNum) && value.equals(((SimpleNum) obj).value); }
        @Override public int compareTo(Num o) { return value.compareTo(((SimpleNum) o).value); }
    }

    /** Simple factory producing {@link SimpleNum} instances. */
    private enum SimpleNumFactory implements NumFactory {
        INSTANCE;

        @Override public Num numOf(Number number) {
            return new SimpleNum(new java.math.BigDecimal(number.toString()));
        }

        @Override public Num zero() { return numOf(0); }
        @Override public Num one() { return numOf(1); }
        @Override public Num two() { return numOf(2); }
        @Override public Num three() { return numOf(3); }
        @Override public Num four() { return numOf(4); }
        @Override public Num five() { return numOf(5); }
        @Override public Num six() { return numOf(6); }
        @Override public Num seven() { return numOf(7); }
        @Override public Num eight() { return numOf(8); }
        @Override public Num nine() { return numOf(9); }
        @Override public Num ten() { return numOf(10); }
    }

    /** Minimal {@link Rule} that always returns {@code true}. */
    private static class AlwaysTrueRule implements Rule {
        @Override public boolean isSatisfied(int index, BarSeries series) { return true; }
        @Override public Rule and(Rule rule) { return this; }
        @Override public Rule or(Rule rule) { return this; }
        @Override public Rule not() { return this; }
        @Override public Rule xor(Rule rule) { return this; }
        @Override public String getLabel() { return "alwaysTrue"; }
        @Override public Rule withLabel(String label) { return this; }
        @Override public Rule reverse() { return this; }
        @Override public int getUnstableBars() { return 0; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public boolean isUnstableAt(int index) { return false; }
        @Override public String toString() { return "AlwaysTrueRule"; }
    }

    /** Simple {@link Strategy} implementation used in many tests. */
    private static class SimpleStrategy implements Strategy {
        private final Trade.TradeType startingType;
        private final Rule entryRule;
        private final Rule exitRule;
        private final String name;

        SimpleStrategy(Trade.TradeType startingType, Rule entryRule, Rule exitRule, String name) {
            this.startingType = startingType;
            this.entryRule = entryRule;
            this.exitRule = exitRule;
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public Rule getEntryRule() { return entryRule; }
        @Override public Rule getExitRule() { return exitRule; }
        @Override public Trade.TradeType getStartingType() { return startingType; }

        // The default implementation of shouldOperate uses entry/exit rules.
        // For the tests we want deterministic behaviour, so we override it.
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            // Enter on the first bar, exit on the last bar.
            if (!tradingRecord.isClosed()) {
                // If we have an open position, we want to exit on the last index.
                return index == tradingRecord.getCurrentPosition().getEntry().getIndex() + 1;
            }
            // No open position – enter on the very first index only.
            return index == 0;
        }
    }

    /** Custom {@link TradeExecutionModel} that always returns {@code null}
     * for {@link #estimateEntryTarget(int, BarSeries, Trade.TradeType)} and
     * records execution calls. */
    private static class NullTargetExecutionModel implements TradeExecutionModel {
        int executeCalls = 0;
        final List<Integer> executedIndexes = new ArrayList<>();

        @Override public ExecutionTarget estimateEntryTarget(int signalIndex, BarSeries barSeries,
                Trade.TradeType tradeType) {
            return null; // force fallback sizing logic
        }

        @Override public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            executeCalls++;
            executedIndexes.add(index);
            // Perform a simple entry/exit using the supplied amount.
            // For simplicity we just call enter/exit directly.
            if (tradingRecord.isClosed()) {
                tradingRecord.enter(index, barSeries.getBar(index).getClosePrice(), amount);
            } else {
                tradingRecord.exit(index, barSeries.getBar(index).getClosePrice(), amount);
            }
        }
    }

    /** Custom {@link TradingRecordFactory} that records the provided arguments. */
    private static class RecordingTradingRecordFactory implements BarSeriesManager.TradingRecordFactory {
        Trade.TradeType receivedTradeType;
        int receivedStart;
        int receivedEnd;
        CostModel receivedTransactionModel;
        CostModel receivedHoldingModel;

        @Override
        public TradingRecord create(Trade.TradeType tradeType, int startIndex, int endIndex,
                CostModel transactionCostModel, CostModel holdingCostModel) {
            receivedTradeType = tradeType;
            receivedStart = startIndex;
            receivedEnd = endIndex;
            receivedTransactionModel = transactionCostModel;
            receivedHoldingModel = holdingCostModel;
            return new BaseTradingRecord(tradeType);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullBarSeries() {
        new BarSeriesManager(null);
    }

    @Test
    public void testGetBarSeriesReturnsSnapshot() {
        BarSeriesManager manager = new BarSeriesManager(series);
        BarSeries snapshot = manager.getBarSeries();
        assertNotSame("Snapshot must be a different instance", series, snapshot);
        // Modifying the snapshot must not affect the manager's internal series.
        snapshot.addBar(new SimpleBar(99));
        assertEquals("Original series size must stay unchanged", 5, series.getBarCount());
        assertEquals("Snapshot size should be increased", 6, snapshot.getBarCount());
    }

    @Test
    public void testRunWithStrategyThatNeverOperatesProducesEmptyRecord() {
        // Strategy that never signals an operation
        Strategy neverStrategy = new SimpleStrategy(Trade.TradeType.BUY,
                new AlwaysTrueRule(), new AlwaysTrueRule(), "never");
        // Override shouldOperate to always return false
        Strategy neverOperate = new Strategy() {
            @Override public String getName() { return "neverOperate"; }
            @Override public Rule getEntryRule() { return new AlwaysTrueRule(); }
            @Override public Rule getExitRule() { return new AlwaysTrueRule(); }
            @Override public Trade.TradeType getStartingType() { return Trade.TradeType.BUY; }
            @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return false; }
        };
        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(neverOperate);
        assertTrue("No trades should be recorded", record.getTrades().isEmpty());
        assertTrue("Record must be closed (no open position)", record.isClosed());
    }

    @Test
    public void testRunWithCustomExecutionModelUsesFallbackSizingTarget() {
        // Use a custom execution model that returns null for entry target.
        NullTargetExecutionModel execModel = new NullTargetExecutionModel();
        // Simple strategy that always wants to operate.
        Strategy alwaysStrategy = new SimpleStrategy(Trade.TradeType.BUY,
                new AlwaysTrueRule(), new AlwaysTrueRule(), "always");
        BarSeriesManager manager = new BarSeriesManager(series, execModel);
        // Use a fixed PositionSizer of amount = 1
        PositionSizer fixedSizer = PositionSizer.fixed(numFactory.one());

        TradingRecord record = manager.run(alwaysStrategy, Trade.TradeType.BUY, fixedSizer);
        // The execution model should have been called for each bar until a position is closed.
        // With 5 bars we expect at most 5 execute calls.
        assertTrue("Execute should have been called at least once", execModel.executeCalls > 0);
        // Verify that the fallback target returned an index within series bounds.
        for (int idx : execModel.executedIndexes) {
            assertTrue("Fallback target index must be >= beginIndex", idx >= series.getBeginIndex());
            assertTrue("Fallback target index must be <= endIndex", idx <= series.getEndIndex());
        }
        // The record must contain at most one completed trade (enter+exit) because the
        // simple strategy opens then immediately closes on the next bar.
        assertTrue("Record should contain at most one position", record.getPositions().size() <= 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunWithInvalidAmountFromPositionSizerThrowsException() {
        // PositionSizer that returns a negative amount.
        PositionSizer invalidSizer = ctx -> ctx.numFactory().numOf(-1);
        Strategy alwaysStrategy = new SimpleStrategy(Trade.TradeType.BUY,
                new AlwaysTrueRule(), new AlwaysTrueRule(), "always");
        BarSeriesManager manager = new BarSeriesManager(series);
        // This should trigger validateAmount -> IllegalArgumentException
        manager.run(alwaysStrategy, Trade.TradeType.BUY, invalidSizer);
    }

    @Test
    public void testCustomTradingRecordFactoryClampsIndices() {
        RecordingTradingRecordFactory factory = new RecordingTradingRecordFactory();
        // Construct manager with custom factory
        BarSeriesManager manager = new BarSeriesManager(series,
                new ZeroCostModel(), new ZeroCostModel(),
                new TradeOnNextOpenModel(), factory);
        // Provide out‑of‑range indexes
        int start = -10;
        int end = 100;
        Strategy dummy = new SimpleStrategy(Trade.TradeType.BUY,
                new AlwaysTrueRule(), new AlwaysTrueRule(), "dummy");
        manager.run(dummy, start, end);
        // The factory must receive clamped values
        assertEquals("Start index must be clamped to series begin", series.getBeginIndex(), factory.receivedStart);
        assertEquals("End index must be clamped to series end", series.getEndIndex(), factory.receivedEnd);
        assertEquals("Trade type must be forwarded correctly", Trade.TradeType.BUY, factory.receivedTradeType);
        assertSame("Transaction cost model must be the one supplied to manager",
                manager.getTransactionCostModel(), factory.receivedTransactionModel);
        assertSame("Holding cost model must be the one supplied to manager",
                manager.getHoldingCostModel(), factory.receivedHoldingModel);
    }

    @Test
    public void testRunWalkForwardDelegatesToExecutor() {
        // Walk‑forward execution is delegated to StrategyWalkForwardExecutor.
        // We cannot instantiate the executor directly (it is not part of the public API
        // in the prompt), but we can verify that calling the method does not throw and
        // returns a non‑null result.
        Strategy dummy = new SimpleStrategy(Trade.TradeType.BUY,
                new AlwaysTrueRule(), new AlwaysTrueRule(), "dummy");
        WalkForwardConfig config = WalkForwardConfig.defaultConfig();
        BarSeriesManager manager = new BarSeriesManager(series);
        StrategyWalkForwardExecutionResult result = manager.runWalkForward(dummy, config);
        assertNotNull("Walk‑forward result must not be null", result);
        // The result should contain the original series.
        assertSame("Result series must be the same as manager's series", series, result.barSeries());
    }
}
```
###Test END##