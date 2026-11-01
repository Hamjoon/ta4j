###Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Strategy;
import org.ta4j.core.Rule;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests use simple in‑memory implementations of {@link BarSeries},
 * {@link Strategy} and {@link TradeExecutionModel} that are sufficient for the
 * behaviour exercised by {@link BarSeriesManager}. No external library classes
 * (other than those listed in the provided signatures) are used.
 */
public class BarSeriesManagerTest {

    /* --------------------------------------------------------------------- */
    /*  Helper implementations (only use classes/interfaces from the list)   */
    /* --------------------------------------------------------------------- */

    /** Minimal {@link Num} implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {

        private static final long serialVersionUID = 1L;
        private final BigDecimal value;

        static final SimpleNum ONE = new SimpleNum(BigDecimal.ONE);
        static final SimpleNum ZERO = new SimpleNum(BigDecimal.ZERO);

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        static SimpleNum valueOf(Number n) {
            return new SimpleNum(new BigDecimal(n.toString()));
        }

        /* ---------- only the methods that are actually used in the tests --------- */

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return value.toString(); }
        @Override public Num plus(Num augend) { return new SimpleNum(value.add(toBigDecimal(augend))); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(value.subtract(toBigDecimal(subtrahend))); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(value.multiply(toBigDecimal(multiplicand))); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(value.divide(toBigDecimal(divisor), BigDecimal.ROUND_HALF_UP)); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(value.remainder(toBigDecimal(divisor))); }
        @Override public Num floor() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_FLOOR)); }
        @Override public Num ceil() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_CEILING)); }
        @Override public Num pow(int n) { return new SimpleNum(value.pow(n)); }
        @Override public Num pow(Num n) { return new SimpleNum(value.pow(toBigDecimal(n).intValue())); }
        @Override public Num log() { throw new UnsupportedOperationException(); }
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
        @Override public Num min(Num other) { return (value.compareTo(toBigDecimal(other)) <= 0) ? this : new SimpleNum(toBigDecimal(other)); }
        @Override public Num max(Num other) { return (value.compareTo(toBigDecimal(other)) >= 0) ? this : new SimpleNum(toBigDecimal(other)); }
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toString(); }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleNum)) return false;
            return value.equals(((SimpleNum) obj).value);
        }
        @Override public int compareTo(Num o) { return value.compareTo(toBigDecimal(o)); }

        private static BigDecimal toBigDecimal(Num n) {
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new BigDecimal(n.getDelegate().toString());
        }
    }

    /** Minimal {@link NumFactory} – only the {@code one()} method is required. */
    private static class SimpleNumFactory {
        Num one() { return SimpleNum.ONE; }
    }

    /** Very small {@link BarSeries} implementation – only the index‑related methods are needed. */
    private static class SimpleBarSeries implements BarSeries {

        private final List<Object> bars = new ArrayList<>(); // content is irrelevant for the manager
        private final SimpleNumFactory numFactory = new SimpleNumFactory();

        SimpleBarSeries(int size) {
            for (int i = 0; i < size; i++) {
                bars.add(new Object());
            }
        }

        @Override public NumFactory numFactory() { return numFactory; }
        @Override public BaseBarConvertibleBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "simple-series"; }
        @Override public Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return bars.size(); }
        @Override public List<Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getBeginIndex() { return bars.isEmpty() ? 0 : 0; }
        @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }
        @Override public int getMaximumBarCount() { return bars.size(); }
        @Override public void setMaximumBarCount(int maximumBarCount) { throw new UnsupportedOperationException(); }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }

        // default methods that are not used directly are left as default implementations
    }

    /** Strategy that returns {@code true} for a predefined set of indexes. */
    private static class IndexBasedStrategy implements Strategy {

        private final Set<Integer> operativeIndexes;
        private final String name;

        IndexBasedStrategy(String name, Collection<Integer> indexes) {
            this.name = name;
            this.operativeIndexes = new HashSet<>(indexes);
        }

        @Override public String getName() { return name; }
        @Override public Rule getEntryRule() { return null; }
        @Override public Rule getExitRule() { return null; }
        @Override public Strategy and(Strategy strategy) { return null; }
        @Override public Strategy or(Strategy strategy) { return null; }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy opposite() { return null; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }

        /** The only method we really need – decide whether to operate on the given index. */
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operativeIndexes.contains(index);
        }
    }

    /** TradeExecutionModel that simply forwards the call to {@link TradingRecord#operate(int)} and records the amount passed. */
    private static class RecordingTradeExecutionModel implements TradeExecutionModel {

        private final List<Num> recordedAmounts = new ArrayList<>();

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            recordedAmounts.add(amount);
            // Use the simple operate(int) default method – it will delegate to the underlying
            // BaseTradingRecord implementation.
            tradingRecord.operate(index);
        }

        List<Num> getRecordedAmounts() {
            return recordedAmounts;
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Test fixtures                                                       */
    /* --------------------------------------------------------------------- */

    private SimpleBarSeries series3;   // 3 bars -> indexes 0,1,2
    private SimpleBarSeries series5;   // 5 bars -> indexes 0..4

    @Before
    public void setUp() {
        series3 = new SimpleBarSeries(3);
        series5 = new SimpleBarSeries(5);
    }

    /* --------------------------------------------------------------------- */
    /*  Tests                                                               */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorSetsZeroCostModels() {
        BarSeriesManager manager = new BarSeriesManager(series3);
        assertSame(series3, manager.getBarSeries());

        CostModel tx = manager.getTransactionCostModel();
        CostModel hold = manager.getHoldingCostModel();

        assertTrue(tx instanceof ZeroCostModel);
        assertTrue(hold instanceof ZeroCostModel);
    }

    @Test
    public void testCustomCostModelsAreReturned() {
        CostModel tx = new ZeroCostModel();   // using the same class for simplicity
        CostModel hold = new ZeroCostModel();

        BarSeriesManager manager = new BarSeriesManager(series3, tx, hold);
        assertSame(tx, manager.getTransactionCostModel());
        assertSame(hold, manager.getHoldingCostModel());
    }

    @Test
    public void testRunWithBuyDefaultAndAllIndexesOperated() {
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series3,
                new ZeroCostModel(),
                new ZeroCostModel(),
                execModel);

        // Operate on every index 0,1,2
        Strategy strategy = new IndexBasedStrategy("all", Arrays.asList(0, 1, 2));

        TradingRecord record = manager.run(strategy, TradeType.BUY);

        // ---- Assertions on the TradingRecord ----
        // BaseTradingRecord stores a Trade for each operate call.
        // With 3 calls we expect 3 trades (entry, exit, entry)
        assertEquals(3, record.getTrades().size());

        // Position count should be 2 (first closed, second still open)
        assertEquals(2, record.getPositions().size());

        // The last position must be open (not closed)
        assertFalse(record.isClosed());

        // ---- Assertions on the Execution Model ----
        // The amount used in each execution is the series' "one()" value
        List<Num> amounts = execModel.getRecordedAmounts();
        assertEquals(3, amounts.size());
        for (Num n : amounts) {
            assertTrue(n.isEqual(SimpleNum.ONE));
        }
    }

    @Test
    public void testRunWithCustomAmountIsPassedToExecutionModel() {
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series5,
                new ZeroCostModel(),
                new ZeroCostModel(),
                execModel);

        // Operate only on indexes 1 and 3
        Strategy strategy = new IndexBasedStrategy("odd", Arrays.asList(1, 3));

        Num customAmount = SimpleNum.valueOf(5); // amount = 5

        TradingRecord record = manager.run(strategy, TradeType.SELL, customAmount, 0, 4);

        // Expect two operate calls => two recorded amounts
        List<Num> amounts = execModel.getRecordedAmounts();
        assertEquals(2, amounts.size());
        for (Num n : amounts) {
            assertTrue(n.isEqual(customAmount));
        }

        // Verify that the TradingRecord has exactly two trades (entry + exit)
        assertEquals(2, record.getTrades().size());
        // Since we start with SELL, the first call should be a SELL entry,
        // the second call should be a BUY exit – the record should be closed.
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithStartAndFinishIndexesLimitsExecution() {
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series5,
                new ZeroCostModel(),
                new ZeroCostModel(),
                execModel);

        // Operate on indexes 0..4, but we limit the run to 2..3
        Strategy strategy = new IndexBasedStrategy("range", Arrays.asList(0, 1, 2, 3, 4));

        TradingRecord record = manager.run(strategy, TradeType.BUY, 2, 3);

        // Only indexes 2 and 3 should have been processed
        List<Num> amounts = execModel.getRecordedAmounts();
        assertEquals(2, amounts.size());

        // Two trades -> entry at 2, exit at 3, record must be closed
        assertEquals(2, record.getTrades().size());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithStartBeyondSeriesBeginAndFinishBeyondSeriesEndAreClamped() {
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series3,
                new ZeroCostModel(),
                new ZeroCostModel(),
                execModel);

        // Use start = -5 (before begin) and finish = 100 (after end)
        Strategy strategy = new IndexBasedStrategy("clamp", Arrays.asList(0, 1, 2));

        TradingRecord record = manager.run(strategy, TradeType.BUY, -5, 100);

        // All three indexes should have been processed (clamped)
        assertEquals(3, execModel.getRecordedAmounts().size());

        // Verify the trading record reflects three operations (entry, exit, entry)
        assertEquals(3, record.getTrades().size());
        assertFalse(record.isClosed()); // last position still open
    }
}
```
###Test END##