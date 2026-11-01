/**
 * JUnit4 test suite for {@link org.ta4j.core.backtest.BarSeriesManager}.
 *
 * The tests use only the public API that is listed in the prompt and avoid any
 * external mocking frameworks.  Simple concrete implementations of the required
 * interfaces are provided inside the test file.
 */
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Test class for {@link BarSeriesManager}.
 */
public class BarSeriesManagerTest {

    /** Simple {@link Num} implementation based on {@link BigDecimal}. */
    private static final class SimpleNum implements Num {

        private final BigDecimal value;

        SimpleNum(Number n) {
            this.value = new BigDecimal(n.toString());
        }

        SimpleNum(BigDecimal v) {
            this.value = v;
        }

        /* --------------------- factory helpers --------------------- */
        static SimpleNum of(Number n) {
            return new SimpleNum(n);
        }

        static SimpleNum zero() {
            return new SimpleNum(BigDecimal.ZERO);
        }

        static SimpleNum one() {
            return new SimpleNum(BigDecimal.ONE);
        }

        static SimpleNum hundred() {
            return new SimpleNum(BigDecimal.valueOf(100));
        }

        /* --------------------- Num interface --------------------- */
        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        @Override
        public Num plus(Num augend) {
            return new SimpleNum(this.value.add(((SimpleNum) augend).value));
        }

        @Override
        public Num minus(Num subtrahend) {
            return new SimpleNum(this.value.subtract(((SimpleNum) subtrahend).value));
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return new SimpleNum(this.value.multiply(((SimpleNum) multiplicand).value));
        }

        @Override
        public Num dividedBy(Num divisor) {
            return new SimpleNum(this.value.divide(((SimpleNum) divisor).value, BigDecimal.ROUND_HALF_EVEN));
        }

        @Override
        public Num remainder(Num divisor) {
            return new SimpleNum(this.value.remainder(((SimpleNum) divisor).value));
        }

        @Override
        public Num floor() {
            return new SimpleNum(this.value.setScale(0, BigDecimal.ROUND_FLOOR));
        }

        @Override
        public Num ceil() {
            return new SimpleNum(this.value.setScale(0, BigDecimal.ROUND_CEILING));
        }

        @Override
        public Num pow(int n) {
            return new SimpleNum(this.value.pow(n));
        }

        @Override
        public Num pow(Num n) {
            return pow(n.intValue());
        }

        @Override
        public Num log() {
            double d = Math.log(this.value.doubleValue());
            return new SimpleNum(d);
        }

        @Override
        public Num sqrt() {
            double d = Math.sqrt(this.value.doubleValue());
            return new SimpleNum(d);
        }

        @Override
        public Num sqrt(int precision) {
            return sqrt();
        }

        @Override
        public Num abs() {
            return new SimpleNum(this.value.abs());
        }

        @Override
        public Num negate() {
            return new SimpleNum(this.value.negate());
        }

        @Override
        public boolean isZero() {
            return value.compareTo(BigDecimal.ZERO) == 0;
        }

        @Override
        public boolean isPositive() {
            return value.compareTo(BigDecimal.ZERO) > 0;
        }

        @Override
        public boolean isPositiveOrZero() {
            return value.compareTo(BigDecimal.ZERO) >= 0;
        }

        @Override
        public boolean isNegative() {
            return value.compareTo(BigDecimal.ZERO) < 0;
        }

        @Override
        public boolean isNegativeOrZero() {
            return value.compareTo(BigDecimal.ZERO) <= 0;
        }

        @Override
        public boolean isEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return value.compareTo(((SimpleNum) other).value) > 0;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) >= 0;
        }

        @Override
        public boolean isLessThan(Num other) {
            return value.compareTo(((SimpleNum) other).value) < 0;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) <= 0;
        }

        @Override
        public Num min(Num other) {
            return value.compareTo(((SimpleNum) other).value) <= 0 ? this : (SimpleNum) other;
        }

        @Override
        public Num max(Num other) {
            return value.compareTo(((SimpleNum) other).value) >= 0 ? this : (SimpleNum) other;
        }

        @Override
        public Function<Number, Num> function() {
            return n -> SimpleNum.of(n);
        }

        @Override
        public Num numOf(Number value) {
            return SimpleNum.of(value);
        }

        @Override
        public Num numOf(String value, int precision) {
            return SimpleNum.of(new BigDecimal(value));
        }

        @Override
        public boolean isNaN() {
            return false;
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

        @Override
        public int compareTo(Num o) {
            return value.compareTo(((SimpleNum) o).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value.toPlainString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleNum)) return false;
            return value.compareTo(((SimpleNum) obj).value) == 0;
        }
    }

    /** Minimal {@link Bar} implementation – content is irrelevant for the tests. */
    private static final class DummyBar implements Bar {
        // All methods of Bar are irrelevant for the manager tests; we keep it empty.
    }

    /** Very small in‑memory {@link BarSeries} implementation. */
    private static final class DummyBarSeries implements BarSeries {

        private final String name;
        private final List<Bar> bars = new ArrayList<>();

        DummyBarSeries(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Num num() {
            // Returns a factory that creates SimpleNum instances.
            return new SimpleNum(0) {
                @Override
                public Num zero() {
                    return SimpleNum.zero();
                }

                @Override
                public Num one() {
                    return SimpleNum.one();
                }

                @Override
                public Num hundred() {
                    return SimpleNum.hundred();
                }

                @Override
                public Num numOf(Number number) {
                    return SimpleNum.of(number);
                }
            };
        }

        @Override
        public Bar getBar(int i) {
            return bars.get(i);
        }

        @Override
        public int getBarCount() {
            return bars.size();
        }

        @Override
        public List<Bar> getBarData() {
            return Collections.unmodifiableList(bars);
        }

        @Override
        public int getBeginIndex() {
            return bars.isEmpty() ? 0 : 0;
        }

        @Override
        public int getEndIndex() {
            return bars.isEmpty() ? -1 : bars.size() - 1;
        }

        @Override
        public void addBar(Bar bar) {
            bars.add(bar);
        }

        @Override
        public void addBar(Bar bar, boolean replace) {
            if (replace && !bars.isEmpty()) {
                bars.set(bars.size() - 1, bar);
            } else {
                addBar(bar);
            }
        }

        @Override
        public void addBar(Duration timePeriod, ZonedDateTime endTime) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
                Number closePrice) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
                Number closePrice, Number volume) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
                Number closePrice, Number volume, Number amount) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice,
                Number lowPrice, Number closePrice, Number volume) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice,
                Number lowPrice, Number closePrice, Number volume, Number amount) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice,
                String closePrice) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice,
                String closePrice, String volume) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice,
                String closePrice, String volume, String amount) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                Num closePrice, Num volume) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                Num closePrice, Num volume, Num amount) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice,
                Num lowPrice, Num closePrice, Num volume) {
            addBar(new DummyBar());
        }

        @Override
        public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice,
                Num lowPrice, Num closePrice, Num volume, Num amount) {
            addBar(new DummyBar());
        }

        @Override
        public void addTrade(Number tradeVolume, Number tradePrice) {
        }

        @Override
        public void addTrade(String tradeVolume, String tradePrice) {
        }

        @Override
        public void addTrade(Num tradeVolume, Num tradePrice) {
        }

        @Override
        public void addPrice(Num price) {
        }

        @Override
        public void addPrice(String price) {
        }

        @Override
        public void addPrice(Number price) {
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            DummyBarSeries sub = new DummyBarSeries(name + "-sub");
            for (int i = startIndex; i <= endIndex && i < bars.size(); i++) {
                sub.addBar(bars.get(i));
            }
            return sub;
        }

        // The remaining default methods from the interface are not needed for the tests.
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
    }

    /** A {@link Strategy} that triggers operation on a predefined set of indexes. */
    private static final class DummyStrategy implements Strategy {

        private final Set<Integer> triggerIndexes;
        private final String name;

        DummyStrategy(String name, Integer... indexes) {
            this.name = name;
            this.triggerIndexes = new HashSet<>(Arrays.asList(indexes));
        }

        @Override
        public String getName() {
            return name;
        }

        // The default implementation of shouldOperate checks entry/exit rules.
        // We override it to simply use the trigger set.
        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return triggerIndexes.contains(index);
        }

        // Unused methods – provide minimal stubs.
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
        @Override public boolean shouldEnter(int index) { return false; }
        @Override public boolean shouldEnter(int index, TradingRecord tradingRecord) { return false; }
        @Override public boolean shouldExit(int index) { return false; }
        @Override public boolean shouldExit(int index, TradingRecord tradingRecord) { return false; }
    }

    /** Trade execution model that records every execution and performs a simple operate. */
    private static final class RecordingTradeExecutionModel implements TradeExecutionModel {

        private final List<Integer> executedIndexes = new ArrayList<>();

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            executedIndexes.add(index);
            // Use a dummy price (the amount itself) – the precise value is irrelevant.
            tradingRecord.operate(index, amount, amount);
        }

        List<Integer> getExecutedIndexes() {
            return executedIndexes;
        }
    }

    private DummyBarSeries series;
    private SimpleNum amount;

    @Before
    public void setUp() {
        series = new DummyBarSeries("testSeries");
        // add three dummy bars (indexes 0,1,2)
        series.addBar(new DummyBar());
        series.addBar(new DummyBar());
        series.addBar(new DummyBar());

        amount = SimpleNum.one();
    }

    @Test
    public void testConstructorAndGetters() {
        CostModel transactionCost = new ZeroCostModel();
        CostModel holdingCost = new ZeroCostModel();
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, transactionCost, holdingCost, execModel);

        assertSame("BarSeries getter must return the same instance passed to the constructor",
                series, manager.getBarSeries());
        assertSame("Transaction cost model getter must return the same instance",
                transactionCost, manager.getTransactionCostModel());
        assertSame("Holding cost model getter must return the same instance",
                holdingCost, manager.getHoldingCostModel());
    }

    @Test
    public void testRunDefaultBuyClosesPosition() {
        // Strategy: enter at index 0, exit at index 2
        DummyStrategy strategy = new DummyStrategy("enterExit", 0, 2);
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount);

        // Verify execution order
        List<Integer> execIndexes = execModel.getExecutedIndexes();
        assertEquals("Two executions expected (entry and exit)", Arrays.asList(0, 2), execIndexes);

        // Verify that a single position was created and closed
        assertEquals("Exactly one position should be recorded", 1, record.getPositions().size());
        assertTrue("The position must be closed", record.isClosed());

        // Verify that the starting trade type matches the requested BUY
        assertEquals("Starting trade type must be BUY", TradeType.BUY, record.getStartingType());
    }

    @Test
    public void testRunWithCustomTradeTypeSell() {
        // Strategy: entry at index 1, exit at index 2 (sell‑type start)
        DummyStrategy strategy = new DummyStrategy("sellStrategy", 1, 2);
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, execModel);
        TradingRecord record = manager.run(strategy, TradeType.SELL, amount);

        // Execution should happen at 1 and 2
        assertEquals(Arrays.asList(1, 2), execModel.getExecutedIndexes());

        // Position should be closed and start with SELL
        assertTrue(record.isClosed());
        assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRunWithStartAndFinishIndices() {
        // Strategy triggers on every index, but we limit the run to 1..2
        DummyStrategy strategy = new DummyStrategy("rangeStrategy", 0, 1, 2);
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, execModel);
        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 1, 2);

        // Only indexes 1 and 2 must be processed
        assertEquals(Arrays.asList(1, 2), execModel.getExecutedIndexes());

        // Expect a position opened at 1 and closed at 2
        assertEquals(1, record.getPositions().size());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunClosesOpenPositionAfterSeriesEnd() {
        // Strategy enters at the last real index (2) and signals again at index 3 (outside series)
        DummyStrategy strategy = new DummyStrategy("postClose", 2, 3);
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, execModel);
        TradingRecord record = manager.run(strategy, TradeType.BUY, amount);

        // Expected execution: first at 2 (entry), then at 3 (closing after series end)
        assertEquals(Arrays.asList(2, 3), execModel.getExecutedIndexes());

        // Position must be closed despite the exit being after the series end.
        assertTrue("The position should be closed after the extra loop", record.isClosed());
        assertEquals(1, record.getPositions().size());
    }

    @Test
    public void testRunWithExplicitStartFinishAndUnclosedPosition() {
        // Series has 3 bars (0..2). We run from 0 to 1, entering at 0.
        // The strategy also signals at index 3 (outside the run) to close.
        DummyStrategy strategy = new DummyStrategy("partialRun", 0, 3);
        RecordingTradeExecutionModel execModel = new RecordingTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, execModel);
        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 0, 1);

        // Executed inside the run: index 0; after‑run extra loop should hit index 3.
        assertEquals(Arrays.asList(0, 3), execModel.getExecutedIndexes());

        // Position must be closed.
        assertTrue(record.isClosed());
        assertEquals(1, record.getPositions().size());
    }
}
