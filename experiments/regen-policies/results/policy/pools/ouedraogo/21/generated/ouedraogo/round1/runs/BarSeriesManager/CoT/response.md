###Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Assert;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests use simple, self‑contained stub implementations of the TA‑4J
 * interfaces (BarSeries, Strategy, TradeExecutionModel, Num, NumFactory) so
 * that no external library code is required.
 */
public class BarSeriesManagerTest {

    /** -------------------   Stub implementations   ------------------- */

    /** Very small {@link Num} implementation sufficient for the tests. */
    private static class SimpleNum implements Num {

        static final SimpleNum ONE = new SimpleNum(BigDecimal.ONE);
        private final BigDecimal value;

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num plus(Num augend) { return new SimpleNum(value.add(toBigDecimal(augend))); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(value.subtract(toBigDecimal(subtrahend))); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(value.multiply(toBigDecimal(multiplicand))); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(value.divide(toBigDecimal(divisor), BigDecimal.ROUND_HALF_UP)); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(value.remainder(toBigDecimal(divisor))); }
        @Override public Num floor() { return new SimpleNum(new BigDecimal(value.toBigInteger())); }
        @Override public Num ceil() { return floor(); }
        @Override public Num pow(int n) { return new SimpleNum(value.pow(n)); }
        @Override public Num pow(Num n) { return new SimpleNum(value.pow(toBigDecimal(n).intValue())); }
        @Override public Num log() { return new SimpleNum(BigDecimal.valueOf(Math.log(value.doubleValue()))); }
        @Override public Num sqrt() { return new SimpleNum(BigDecimal.valueOf(Math.sqrt(value.doubleValue()))); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs() { return new SimpleNum(value.abs()); }
        @Override public Num negate() { return new SimpleNum(value.negate()); }
        @Override public boolean isZero() { return value.compareTo(BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive() { return value.compareTo(BigDecimal.ZERO) > 0; }
        @Override public boolean isPositiveOrZero() { return value.compareTo(BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegative() { return value.compareTo(BigDecimal.ZERO) < 0; }
        @Override public boolean isNegativeOrZero() { return value.compareTo(BigDecimal.ZERO) <= 0; }
        @Override public boolean isEqual(Num other) { return value.compareTo(toBigDecimal(other)) == 0; }
        @Override public boolean isGreaterThan(Num other) { return value.compareTo(toBigDecimal(other)) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return value.compareTo(toBigDecimal(other)) >= 0; }
        @Override public boolean isLessThan(Num other) { return value.compareTo(toBigDecimal(other)) < 0; }
        @Override public boolean isLessThanOrEqual(Num other) { return value.compareTo(toBigDecimal(other)) <= 0; }
        @Override public Num min(Num other) { return isLessThan(other) ? this : new SimpleNum(toBigDecimal(other)); }
        @Override public Num max(Num other) { return isGreaterThan(other) ? this : new SimpleNum(toBigDecimal(other)); }
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toPlainString(); }
        @Override public boolean equals(Object obj) { return (obj instanceof SimpleNum) && value.equals(((SimpleNum) obj).value); }
        @Override public int compareTo(Num o) { return value.compareTo(toBigDecimal(o)); }

        private static BigDecimal toBigDecimal(Num n) {
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new BigDecimal(n.doubleValue());
        }
    }

    /** Minimal {@link NumFactory} that only provides {@code one()}. */
    private static class SimpleNumFactory implements NumFactory {
        @Override public Num one() { return SimpleNum.ONE; }
        // All other methods are not required for the tests and therefore omitted.
    }

    /** Very small {@link Bar} placeholder – the manager never inspects its content. */
    private static class SimpleBar { /* empty */ }

    /** Stub {@link BarSeries} implementation used only for index handling. */
    private static class SimpleBarSeries implements BarSeries {

        private final List<SimpleBar> bars;
        private final int beginIndex;
        private final int endIndex;
        private final SimpleNumFactory numFactory = new SimpleNumFactory();

        SimpleBarSeries(int size, int beginIndex, int endIndex) {
            this.bars = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                bars.add(new SimpleBar());
            }
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }

        @Override public NumFactory numFactory() { return numFactory; }
        @Override public Bar getBar(int i) { return bars.get(i); }
        @Override public String getName() { return "SimpleSeries"; }
        @Override public int getBarCount() { return bars.size(); }
        @Override public List getBarData() { return bars; }
        @Override public int getBeginIndex() { return beginIndex; }
        @Override public int getEndIndex() { return endIndex; }
        @Override public void addBar(Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        // The remaining default methods are not needed for the unit tests.
    }

    /** Simple {@link Strategy} that triggers operation on a predefined set of indexes. */
    private static class SimpleStrategy implements Strategy {

        private final Set<Integer> operateIndexes;

        SimpleStrategy(Set<Integer> operateIndexes) {
            this.operateIndexes = new HashSet<>(operateIndexes);
        }

        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operateIndexes.contains(index);
        }

        // All other methods are either default or irrelevant for the tests.
        @Override public String getName() { return "SimpleStrategy"; }
        @Override public org.ta4j.core.Rule getEntryRule() { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.Rule getExitRule() { throw new UnsupportedOperationException(); }
        @Override public Strategy and(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public int getUnstableBars() { throw new UnsupportedOperationException(); }
        @Override public boolean isUnstableAt(int index) { throw new UnsupportedOperationException(); }
    }

    /**
     * Very small {@link TradeExecutionModel} that alternates between an entry and
     * an exit operation. It uses the supplied {@code amount} as both price and
     * amount for the {@link TradingRecord} calls.
     */
    private static class SimpleTradeExecutionModel implements TradeExecutionModel {

        private boolean nextIsEnter = true;

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            if (nextIsEnter) {
                tradingRecord.enter(index, amount, amount);
            } else {
                tradingRecord.exit(index, amount, amount);
            }
            nextIsEnter = !nextIsEnter;
        }
    }

    /** -------------------   Public method signatures   ------------------- */

    // The manager exposes the following public methods (extracted from the source):
    // 1. BarSeries getBarSeries()
    // 2. CostModel getTransactionCostModel()
    // 3. CostModel getHoldingCostModel()
    // 4. TradingRecord run(Strategy strategy)
    // 5. TradingRecord run(Strategy strategy, int startIndex, int finishIndex)
    // 6. TradingRecord run(Strategy strategy, TradeType tradeType)
    // 7. TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex)
    // 8. TradingRecord run(Strategy strategy, TradeType tradeType, Num amount)
    // 9. TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex)

    /** -------------------   Basic functionality tests   ------------------- */

    @Test
    public void testGettersReturnProvidedInstances() {
        SimpleBarSeries series = new SimpleBarSeries(5, 0, 4);
        CostModel transaction = new ZeroCostModel();
        CostModel holding = new ZeroCostModel();
        TradeExecutionModel execution = new SimpleTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, transaction, holding, execution);

        Assert.assertSame(series, manager.getBarSeries());
        Assert.assertSame(transaction, manager.getTransactionCostModel());
        Assert.assertSame(holding, manager.getHoldingCostModel());
    }

    @Test
    public void testRunWithStrategyThatNeverOperates() {
        SimpleBarSeries series = new SimpleBarSeries(3, 0, 2);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Strategy that never returns true for shouldOperate
        Strategy neverStrategy = new SimpleStrategy(Collections.emptySet());

        TradingRecord record = manager.run(neverStrategy);
        Assert.assertTrue("No trades should have been created", record.getPositions().isEmpty());
        Assert.assertTrue("Record should be considered closed", record.isClosed());
    }

    @Test
    public void testRunEnterAndExitUsingDefaultParameters() {
        SimpleBarSeries series = new SimpleBarSeries(4, 0, 3);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Operate on index 0 (enter) and index 1 (exit)
        Strategy simple = new SimpleStrategy(new HashSet<>(Arrays.asList(0, 1)));

        TradingRecord record = manager.run(simple);
        Assert.assertEquals("Exactly one position should have been created", 1, record.getPositions().size());
        Assert.assertTrue("The position must be closed after exit", record.isClosed());
    }

    /** -------------------   Edge‑case & exception tests   ------------------- */

    @Test
    public void testRunWithStartIndexBeyondSeriesEnd() {
        SimpleBarSeries series = new SimpleBarSeries(5, 0, 4);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Strategy that would normally operate, but indices are out of range
        Strategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(10, 11)));

        // startIndex = 10, finishIndex = 12 (both beyond series end)
        TradingRecord record = manager.run(strategy, TradeType.BUY, 10, 12);

        // No operation should have been performed
        Assert.assertTrue("No positions should exist when startIndex is out of range", record.getPositions().isEmpty());
        Assert.assertTrue("Record must be closed when nothing was executed", record.isClosed());
    }

    @Test
    public void testRunClampsIndicesAndExecutesWithinRange() {
        SimpleBarSeries series = new SimpleBarSeries(5, 0, 4);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Operate on indexes 1 and 2 (both inside the series)
        Strategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(1, 2)));

        // Provide startIndex lower than begin and finishIndex higher than end to test clamping
        TradingRecord record = manager.run(strategy, TradeType.BUY,  SimpleNum.ONE, -5, 10);

        // Expected: one position (enter at 1, exit at 2)
        Assert.assertEquals(1, record.getPositions().size());
        Assert.assertTrue(record.isClosed());
    }

    @Test
    public void testRunAttemptsToCloseOpenPositionUsingExtraBarsBeyondEndIndex() {
        // Build a series where the logical end index is 1 but there are 3 bars in the data list.
        SimpleBarSeries series = new SimpleBarSeries(3, 0, 1);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Strategy will enter at index 0 and attempt to exit at index 2 (which is beyond endIndex)
        Strategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(0, 2)));

        // Run only up to the official endIndex (1). The extra bar (index 2) should be inspected
        // by the post‑run loop and close the open position.
        TradingRecord record = manager.run(strategy, TradeType.BUY, SimpleNum.ONE, 0, 1);

        Assert.assertEquals("One position should have been created", 1, record.getPositions().size());
        Assert.assertTrue("The position must be closed after the extra‑bar handling", record.isClosed());
    }

    /** -------------------   Additional overload tests   ------------------- */

    @Test
    public void testRunWithExplicitTradeTypeAndAmount() {
        SimpleBarSeries series = new SimpleBarSeries(4, 0, 3);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Operate on 0 (enter) and 1 (exit)
        Strategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(0, 1)));

        TradingRecord record = manager.run(strategy, TradeType.SELL, SimpleNum.ONE);
        Assert.assertEquals(1, record.getPositions().size());
        Assert.assertTrue(record.isClosed());
        Assert.assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRunWithAllParametersProvided() {
        SimpleBarSeries series = new SimpleBarSeries(5, 0, 4);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // Operate on 2 (enter) and 3 (exit)
        Strategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(2, 3)));

        TradingRecord record = manager.run(strategy, TradeType.BUY, SimpleNum.ONE, 2, 3);
        Assert.assertEquals(1, record.getPositions().size());
        Assert.assertTrue(record.isClosed());
    }
}
```
###Test END##