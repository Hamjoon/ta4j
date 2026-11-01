###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.backtest.BarSeriesManager}.
 *
 * The tests use very small, self‑contained stub implementations of the
 * library interfaces (BarSeries, Strategy, TradeExecutionModel, Num, …) to
 * avoid any external dependencies while still exercising the real logic of
 * {@code BarSeriesManager}.
 *
 * The tests cover:
 * <ul>
 *   <li>All constructors and the corresponding getter methods.</li>
 *   <li>All overloads of {@code run(...)} – default BUY, custom TradeType,
 *       custom amount, custom start/finish indexes.</li>
 *   <li>The “close‑after‑run‑end” behaviour when the supplied finish index
 *       is before the end of the series.</li>
 * </ul>
 *
 * The stubs implement only the methods required by {@code BarSeriesManager}
 * and are deliberately simple – they do **not** perform any real price
 * calculations.
 */
package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Minimal {@link Num} implementation used only for the tests.
 */
class SimpleNum implements Num {

    private final double value;

    SimpleNum(double value) {
        this.value = value;
    }

    @Override public Num zero() { return new SimpleNum(0); }
    @Override public Num one() { return new SimpleNum(1); }
    @Override public Num hundred() { return new SimpleNum(100); }

    @Override public Number getDelegate() { return value; }
    @Override public String getName() { return Double.toString(value); }

    @Override public Num plus(Num augend) { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
    @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
    @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
    @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value / ((SimpleNum) divisor).value); }
    @Override public Num remainder(Num divisor) { return new SimpleNum(this.value % ((SimpleNum) divisor).value); }
    @Override public Num floor() { return new SimpleNum(Math.floor(value)); }
    @Override public Num ceil() { return new SimpleNum(Math.ceil(value)); }
    @Override public Num pow(int n) { return new SimpleNum(Math.pow(value, n)); }
    @Override public Num pow(Num n) { return new SimpleNum(Math.pow(value, ((SimpleNum) n).value)); }
    @Override public Num log() { return new SimpleNum(Math.log(value)); }
    @Override public Num sqrt() { return new SimpleNum(Math.sqrt(value)); }
    @Override public Num sqrt(int precision) { return sqrt(); }
    @Override public Num abs() { return new SimpleNum(Math.abs(value)); }
    @Override public Num negate() { return new SimpleNum(-value); }

    @Override public boolean isZero() { return value == 0; }
    @Override public boolean isPositive() { return value > 0; }
    @Override public boolean isPositiveOrZero() { return value >= 0; }
    @Override public boolean isNegative() { return value < 0; }
    @Override public boolean isNegativeOrZero() { return value <= 0; }
    @Override public boolean isEqual(Num other) { return Double.compare(value, ((SimpleNum) other).value) == 0; }
    @Override public boolean isGreaterThan(Num other) { return value > ((SimpleNum) other).value; }
    @Override public boolean isGreaterThanOrEqual(Num other) { return value >= ((SimpleNum) other).value; }
    @Override public boolean isLessThan(Num other) { return value < ((SimpleNum) other).value; }
    @Override public boolean isLessThanOrEqual(Num other) { return value <= ((SimpleNum) other).value; }
    @Override public Num min(Num other) { return value <= ((SimpleNum) other).value ? this : other; }
    @Override public Num max(Num other) { return value >= ((SimpleNum) other).value ? this : other; }

    @Override public Function<Number, Num> function() {
        return n -> new SimpleNum(n.doubleValue());
    }

    @Override public Num numOf(Number number) { return new SimpleNum(number.doubleValue()); }
    @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
    @Override public boolean isNaN() { return Double.isNaN(value); }
    @Override public double doubleValue() { return value; }
    @Override public int intValue() { return (int) value; }
    @Override public long longValue() { return (long) value; }
    @Override public float floatValue() { return (float) value; }

    @Override public int compareTo(Num o) {
        return Double.compare(this.value, ((SimpleNum) o).value);
    }

    @Override public String toString() { return Double.toString(value); }
    @Override public boolean equals(Object obj) {
        return obj instanceof SimpleNum && Double.compare(value, ((SimpleNum) obj).value) == 0;
    }
    @Override public int hashCode() { return Objects.hash(value); }
}

/**
 * Very small {@link Bar} placeholder – the manager never accesses any
 * information from a {@code Bar}, it only needs the series indices.
 */
class SimpleBar implements org.ta4j.core.Bar {
    // all default methods are fine – the interface has many default methods.
}

/**
 * Minimal {@link BarSeries} implementation required for the tests.
 */
class SimpleBarSeries implements BarSeries {

    private final List<Bar> bars = new ArrayList<>();

    SimpleBarSeries(int size) {
        for (int i = 0; i < size; i++) {
            bars.add(new SimpleBar());
        }
    }

    @Override public String getName() { return "simple-series"; }
    @Override public Num num() { return new SimpleNum(0); }
    @Override public Bar getBar(int i) { return bars.get(i); }
    @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }
    @Override public int getBeginIndex() { return 0; }
    @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }
    @Override public Num one() { return new SimpleNum(1); }

    // The remaining methods are not used by BarSeriesManager – we provide
    // simple default implementations where necessary.
    @Override public int getBarCount() { return bars.size(); }
    @Override public boolean isEmpty() { return bars.isEmpty(); }
    @Override public void addBar(Bar bar) { bars.add(bar); }
    @Override public void addBar(Bar bar, boolean replace) { bars.add(bar); }
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) {}
    @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                 Number lowPrice, Number closePrice) {}
    @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                 Number lowPrice, Number closePrice, Number volume) {}
    @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                 Number lowPrice, Number closePrice, Number volume, Number amount) {}
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                 Number openPrice, Number highPrice, Number lowPrice,
                                 Number closePrice, Number volume) {}
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                 Number openPrice, Number highPrice, Number lowPrice,
                                 Number closePrice, Number volume, Number amount) {}
    @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                 String lowPrice, String closePrice) {}
    @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                 String lowPrice, String closePrice, String volume) {}
    @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                 String lowPrice, String closePrice, String volume, String amount) {}
    @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                 Num lowPrice, Num closePrice, Num volume) {}
    @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                 Num lowPrice, Num closePrice, Num volume, Num amount) {}
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                 Num openPrice, Num highPrice, Num lowPrice,
                                 Num closePrice, Num volume) {}
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                 Num openPrice, Num highPrice, Num lowPrice,
                                 Num closePrice, Num volume, Num amount) {}
    @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
    @Override public void addTrade(String tradeVolume, String tradePrice) {}
    @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
    @Override public void addPrice(Num price) {}
    @Override public void addPrice(String price) {}
    @Override public void addPrice(Number price) {}
    @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return this; }
    @Override public String getSeriesPeriodDescription() { return ""; }
    @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
    @Override public void setMaximumBarCount(int maximumBarCount) {}
    @Override public int getRemovedBarsCount() { return 0; }
}

/**
 * Simple {@link Strategy} that delegates the decision to a supplied
 * {@link java.util.function.BiPredicate}.
 */
class SimpleStrategy implements Strategy {

    private final java.util.function.BiPredicate<Integer, TradingRecord> predicate;

    SimpleStrategy(java.util.function.BiPredicate<Integer, TradingRecord> predicate) {
        this.predicate = predicate;
    }

    @Override public String getName() { return "simple-strategy"; }
    @Override public org.ta4j.core.Rule getEntryRule() { return null; }
    @Override public org.ta4j.core.Rule getExitRule() { return null; }
    @Override public Strategy and(Strategy strategy) { return this; }
    @Override public Strategy or(Strategy strategy) { return this; }
    @Override public Strategy and(String name, Strategy strategy, int unstableBars) { return this; }
    @Override public Strategy or(String name, Strategy strategy, int unstableBars) { return this; }
    @Override public Strategy opposite() { return this; }
    @Override public void setUnstableBars(int unstableBars) {}
    @Override public int getUnstableBars() { return 0; }
    @Override public boolean isUnstableAt(int index) { return false; }

    // The only method we really need for the manager.
    @Override
    public boolean shouldOperate(int index, TradingRecord tradingRecord) {
        return predicate.test(index, tradingRecord);
    }
}

/**
 * Execution model used in the tests. It simply opens a position when none is
 * open and closes it otherwise, using the supplied {@code amount} for both
 * price and amount.
 */
class SimpleExecutionModel implements TradeExecutionModel {

    private final List<Integer> executedIndexes = new ArrayList<>();

    List<Integer> getExecutedIndexes() {
        return executedIndexes;
    }

    @Override
    public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
        executedIndexes.add(index);
        // If there is no open position, we open one; otherwise we close it.
        if (tradingRecord.getCurrentPosition() == null) {
            tradingRecord.enter(index, amount, amount);
        } else {
            tradingRecord.exit(index, amount, amount);
        }
    }
}

/**
 * Test class for {@link BarSeriesManager}.
 */
public class BarSeriesManagerTest {

    private SimpleBarSeries series5;   // 5 bars: indices 0‑4
    private SimpleBarSeries series3;   // 3 bars: indices 0‑2

    @Before
    public void setUp() {
        series5 = new SimpleBarSeries(5);
        series3 = new SimpleBarSeries(3);
    }

    /* -------------------------------------------------------------
     *  Constructor / getter tests
     * ------------------------------------------------------------- */
    @Test
    public void testDefaultConstructorAndGetters() {
        BarSeriesManager mgr = new BarSeriesManager(series5);
        assertSame(series5, mgr.getBarSeries());
        assertTrue(mgr.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(mgr.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testFullConstructorAndGetters() {
        CostModel tx = new ZeroCostModel();
        CostModel hold = new ZeroCostModel();
        TradeExecutionModel exec = new SimpleExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series5, tx, hold, exec);
        assertSame(series5, mgr.getBarSeries());
        assertSame(tx, mgr.getTransactionCostModel());
        assertSame(hold, mgr.getHoldingCostModel());
    }

    /* -------------------------------------------------------------
     *  run(...) overloads – default BUY, full series
     * ------------------------------------------------------------- */
    @Test
    public void testRunDefaultBuyFullSeries() {
        SimpleExecutionModel exec = new SimpleExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series5, exec);
        // strategy that always wants to operate
        Strategy always = new SimpleStrategy((i, r) -> true);
        TradingRecord record = mgr.run(always);

        // Expected: open at 0, close at 1, open at 2, close at 3, open at 4 (still open)
        assertEquals(5, exec.getExecutedIndexes().size());
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), exec.getExecutedIndexes());

        // Two closed positions and one open
        assertFalse(record.isClosed());   // last position still open
        assertEquals(3, record.getPositions().size());

        // Verify start/end indexes stored in the BaseTradingRecord
        assertEquals(Integer.valueOf(0), record.getStartIndex());
        assertEquals(Integer.valueOf(4), record.getEndIndex());
    }

    /* -------------------------------------------------------------
     *  run(strategy, TradeType, start, finish) – custom start/finish
     * ------------------------------------------------------------- */
    @Test
    public void testRunWithCustomStartFinishAndSell() {
        SimpleExecutionModel exec = new SimpleExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series5, exec);
        Strategy always = new SimpleStrategy((i, r) -> true);

        // run from index 1 to 3, starting with a SELL trade
        TradingRecord record = mgr.run(always, TradeType.SELL, 1, 3);

        // Expected indices: 1 (open), 2 (close), 3 (open)
        assertEquals(Arrays.asList(1, 2, 3), exec.getExecutedIndexes());

        // One closed position and one open (total 2 positions)
        assertEquals(2, record.getPositions().size());
        assertFalse(record.isClosed());

        // The manager should have stored the supplied start/end indexes
        assertEquals(Integer.valueOf(1), record.getStartIndex());
        assertEquals(Integer.valueOf(3), record.getEndIndex());
    }

    /* -------------------------------------------------------------
     *  run with explicit amount and a finish index before series end
     *  – ensures the “extra loop” tries to close the remaining position.
     * ------------------------------------------------------------- */
    @Test
    public void testRunClosesAfterFinishIndex() {
        SimpleExecutionModel exec = new SimpleExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series5, exec);
        // strategy that always wants to operate
        Strategy always = new SimpleStrategy((i, r) -> true);
        Num amount = new SimpleNum(10);

        // run only over indices 0‑2 (finishIndex < series end)
        TradingRecord record = mgr.run(always, TradeType.BUY, amount, 0, 2);

        // Execution indexes:
        // 0 (open), 1 (close), 2 (open) – then extra loop should close at 3
        assertEquals(Arrays.asList(0, 1, 2, 3), exec.getExecutedIndexes());

        // After the extra loop the position must be closed
        assertTrue(record.isClosed());
        assertEquals(2, record.getPositions().size()); // two closed positions
        // The start/end indexes stored in the record are those passed to run()
        assertEquals(Integer.valueOf(0), record.getStartIndex());
        assertEquals(Integer.valueOf(2), record.getEndIndex());
    }

    /* -------------------------------------------------------------
     *  run(strategy, amount) overload – uses series full range
     * ------------------------------------------------------------- */
    @Test
    public void testRunWithExplicitAmountFullSeries() {
        SimpleExecutionModel exec = new SimpleExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series3, exec);
        Strategy always = new SimpleStrategy((i, r) -> true);
        Num amount = new SimpleNum(5);

        TradingRecord record = mgr.run(always, TradeType.BUY, amount);

        // series3 has indices 0‑2: open 0, close 1, open 2 (open at end)
        assertEquals(Arrays.asList(0, 1, 2), exec.getExecutedIndexes());

        assertFalse(record.isClosed());
        assertEquals(2, record.getPositions().size()); // one closed, one open
        assertEquals(Integer.valueOf(0), record.getStartIndex());
        assertEquals(Integer.valueOf(2), record.getEndIndex());
    }
}
```
###Test END##