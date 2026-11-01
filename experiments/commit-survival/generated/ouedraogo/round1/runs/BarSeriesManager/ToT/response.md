###Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * <p>
 * The tests use only the public API listed in the prompt.  All auxiliary
 * objects (BarSeries, Bar, Strategy, TradeExecutionModel, Num, etc.) are
 * lightweight concrete implementations created inside the test class – no
 * mocking frameworks or external library classes are used.
 * </p>
 */
public class BarSeriesManagerTest {

    /* --------------------------------------------------------------------- *
     *  Simple Num implementation (only the operations required by the   *
     *  tests are provided).                                                *
     * --------------------------------------------------------------------- */
    private static class SimpleNum implements Num {
        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        @Override public Num zero() { return new SimpleNum(0d); }
        @Override public Num one() { return new SimpleNum(1d); }
        @Override public Num hundred() { return new SimpleNum(100d); }

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

        @Override public boolean isZero() { return value == 0d; }
        @Override public boolean isPositive() { return value > 0d; }
        @Override public boolean isPositiveOrZero() { return value >= 0d; }
        @Override public boolean isNegative() { return value < 0d; }
        @Override public boolean isNegativeOrZero() { return value <= 0d; }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other) { return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= ((SimpleNum) other).value; }
        @Override public Num min(Num other) { return this.value <= ((SimpleNum) other).value ? this : other; }
        @Override public Num max(Num other) { return this.value >= ((SimpleNum) other).value ? this : other; }
        @Override public java.util.function.Function<Number, Num> function() { return SimpleNum::new; }

        @Override public Num numOf(Number value) { return new SimpleNum(value.doubleValue()); }
        @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN() { return Double.isNaN(value); }
        @Override public double doubleValue() { return value; }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }

        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum) o).value); }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return obj instanceof SimpleNum && Double.compare(this.value, ((SimpleNum) obj).value) == 0;
        }
    }

    /* --------------------------------------------------------------------- *
     *  Minimal Bar implementation – the manager never inspects the bar   *
     *  content, therefore an empty placeholder is sufficient.            *
     * --------------------------------------------------------------------- */
    private static class SimpleBar implements Bar {
        // All methods are default / not used in the tests
    }

    /* --------------------------------------------------------------------- *
     *  Simple BarSeries implementation – only the methods required by      *
     *  BarSeriesManager are implemented.                                   *
     * --------------------------------------------------------------------- */
    private static class SimpleBarSeries implements BarSeries {
        private final List<Bar> bars = new ArrayList<>();
        private final SimpleNum ONE = new SimpleNum(1d);
        private final String name = "test-series";

        SimpleBarSeries(int size) {
            for (int i = 0; i < size; i++) {
                bars.add(new SimpleBar());
            }
        }

        @Override public String getName() { return name; }
        @Override public Num num() { return ONE; }
        @Override public Num one() { return ONE; }
        @Override public Bar getBar(int i) { return bars.get(i); }
        @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }

        // The rest of the interface methods are either defaults or irrelevant for the tests
        @Override public int getBarCount() { return bars.size(); }
        @Override public boolean isEmpty() { return bars.isEmpty(); }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return null; }
        @Override public void addBar(Bar bar) {}
        @Override public void addBar(Bar bar, boolean replace) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) {}
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice) {}
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume) {}
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume, String amount) {}
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) {}
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(String tradeVolume, String tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(String price) {}
        @Override public void addPrice(Number price) {}
        @Override public String getSeriesPeriodDescription() { return ""; }
    }

    /* --------------------------------------------------------------------- *
     *  Dummy Strategy – returns true for the indices supplied at          *
     *  construction time via {@code entryIndices}.  The default implementation*
     *  of {@code shouldOperate} (provided by the interface) forwards to   *
     *  {@code shouldEnter}/{@code shouldExit}.  To keep things simple we   *
     *  override {@code shouldOperate} directly.                             *
     * --------------------------------------------------------------------- */
    private static class DummyStrategy implements Strategy {
        private final Set<Integer> operativeIndices;

        DummyStrategy(Integer... indices) {
            this.operativeIndices = new HashSet<>(Arrays.asList(indices));
        }

        @Override public String getName() { return "dummy"; }
        @Override public Rule getEntryRule() { return null; }
        @Override public Rule getExitRule() { return null; }
        @Override public Strategy and(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operativeIndices.contains(index);
        }

        // The remaining default methods are not needed for the tests
    }

    /* --------------------------------------------------------------------- *
     *  Very small TradeExecutionModel – it simply opens a position if      *
     *  none exists, otherwise it closes the current one.                  *
     * --------------------------------------------------------------------- */
    private static class SimpleTradeExecutionModel implements TradeExecutionModel {
        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // If there is no open position (or all positions are closed) -> enter
            if (tradingRecord.isClosed() || tradingRecord.getCurrentPosition() == null) {
                tradingRecord.enter(index, amount, amount);
            } else {
                tradingRecord.exit(index, amount, amount);
            }
        }
    }

    private SimpleBarSeries series;
    private SimpleNum amount;

    @Before
    public void setUp() {
        // a series with 5 bars (indices 0 .. 4)
        series = new SimpleBarSeries(5);
        amount = new SimpleNum(1d);
    }

    /* -------------------------- Constructor tests ----------------------- */

    @Test
    public void testDefaultConstructorUsesZeroCostModels() {
        BarSeriesManager manager = new BarSeriesManager(series);
        assertTrue(manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testFullConstructorKeepsProvidedModels() {
        CostModel txModel = new ZeroCostModel();
        CostModel holdModel = new ZeroCostModel();
        TradeExecutionModel execModel = new SimpleTradeExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, txModel, holdModel, execModel);
        assertSame(txModel, manager.getTransactionCostModel());
        assertSame(holdModel, manager.getHoldingCostModel());
    }

    /* --------------------------- Getter tests --------------------------- */

    @Test
    public void testGetBarSeriesReturnsSameInstance() {
        BarSeriesManager manager = new BarSeriesManager(series);
        assertSame(series, manager.getBarSeries());
    }

    /* --------------------------- run() overloads ----------------------- */

    @Test
    public void testRunDefaultBuyOpensAndClosesPosition() {
        // Strategy will operate at index 0 (enter) and 1 (exit)
        DummyStrategy strategy = new DummyStrategy(0, 1);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        TradingRecord record = manager.run(strategy);
        // Exactly one closed position
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
        // Starting type must be BUY (default)
        assertEquals(TradeType.BUY, record.getStartingType());
    }

    @Test
    public void testRunWithExplicitTradeTypeSell() {
        DummyStrategy strategy = new DummyStrategy(0, 1);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        TradingRecord record = manager.run(strategy, TradeType.SELL);
        assertEquals(TradeType.SELL, record.getStartingType());
        assertEquals(1, record.getPositionCount());
    }

    @Test
    public void testRunWithStartAndFinishIndices() {
        // Operate only on indices 2 and 3
        DummyStrategy strategy = new DummyStrategy(2, 3);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        TradingRecord record = manager.run(strategy, 2, 3);
        assertEquals(1, record.getPositionCount());
        // The recorded start/end indexes must be inside the supplied window
        assertEquals(Integer.valueOf(2), record.getStartIndex());
        assertEquals(Integer.valueOf(3), record.getEndIndex());
    }

    @Test
    public void testRunWithCustomAmount() {
        DummyStrategy strategy = new DummyStrategy(0, 1);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        Num customAmount = new SimpleNum(5d);
        TradingRecord record = manager.run(strategy, TradeType.BUY, customAmount);
        // The amount does not affect the number of positions – just ensure the call succeeds
        assertEquals(1, record.getPositionCount());
    }

    @Test
    public void testRunClampsIndicesBeyondSeriesBounds() {
        // Supply a start index before the series and a finish index after it
        DummyStrategy strategy = new DummyStrategy(0, 1, 2, 3, 4);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        // start = -10, finish = 100  -> should be clamped to 0 .. 4
        TradingRecord record = manager.run(strategy, TradeType.BUY, -10, 100);
        // All operative indices are inside the clamped range, we expect a single closed position
        assertEquals(1, record.getPositionCount());
        assertEquals(Integer.valueOf(0), record.getStartIndex());
        assertEquals(Integer.valueOf(4), record.getEndIndex());
    }

    @Test
    public void testRunWhenStrategyNeverOperatesReturnsEmptyRecord() {
        DummyStrategy neverOperates = new DummyStrategy(); // empty set
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        TradingRecord record = manager.run(neverOperates);
        assertEquals(0, record.getPositionCount());
        assertTrue(record.isClosed()); // no open position at the end
    }

    @Test(expected = NullPointerException.class)
    public void testRunWithNullStrategyThrowsNPE() {
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        manager.run(null);
    }

    /* -------------------- Closing‑position‑after‑run logic -------------------- */

    @Test
    public void testRunAttemptsToCloseOpenPositionAfterEndIndex() {
        // Strategy will enter at the last index (4) and would need a later bar to exit.
        DummyStrategy strategy = new DummyStrategy(4);
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());

        TradingRecord record = manager.run(strategy, 4, 4);
        // The manager should try to close the position on the (non‑existent) next bar.
        // Since there is no next bar, the position stays open.
        assertFalse(record.isClosed());
        assertEquals(1, record.getPositionCount());
    }

    @Test
    public void testRunClosesOpenPositionUsingAdditionalBarsBeyondSeries() {
        // Extend the series with two extra dummy bars (size = 7)
        SimpleBarSeries extended = new SimpleBarSeries(7);
        DummyStrategy strategy = new DummyStrategy(5); // operate on index 5 (beyond original end)
        BarSeriesManager manager = new BarSeriesManager(extended, new SimpleTradeExecutionModel());

        // Run only over the original 0..4 range; after the loop the manager will
        // iterate over indices 5 and 6 trying to close the open position.
        TradingRecord record = manager.run(strategy, 0, 4);
        // The extra operative index (5) should close the position.
        assertTrue(record.isClosed());
        assertEquals(1, record.getPositionCount());
    }
}
```
###Test END##