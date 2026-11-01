package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * The suite uses tiny in‑memory implementations of the TA‑4J interfaces
 * (BarSeries, Bar, Strategy, TradeExecutionModel, Num) so that no external
 * library code is required beyond the public APIs listed in the task.
 */
public class BarSeriesManagerTest {

    /** Simple {@link Num} implementation based on double values. */
    private static class SimpleNum implements Num {
        private final double value;

        SimpleNum(double value) { this.value = value; }

        // ----- factory helpers -------------------------------------------------
        static SimpleNum zero() { return new SimpleNum(0d); }
        static SimpleNum one()  { return new SimpleNum(1d); }

        // ----- required Num operations (only a subset is needed) -------------
        @Override public Num plus(Num augend)               { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend)           { return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand){ return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor)          { return new SimpleNum(this.value / ((SimpleNum) divisor).value); }
        @Override public Num remainder(Num divisor)         { return new SimpleNum(this.value % ((SimpleNum) divisor).value); }
        @Override public Num floor()                         { return new SimpleNum(Math.floor(this.value)); }
        @Override public Num ceil()                          { return new SimpleNum(Math.ceil(this.value)); }
        @Override public Num pow(int n)                      { return new SimpleNum(Math.pow(this.value, n)); }
        @Override public Num pow(Num n)                      { return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value)); }
        @Override public Num log()                           { return new SimpleNum(Math.log(this.value)); }
        @Override public Num sqrt()                          { return new SimpleNum(Math.sqrt(this.value)); }
        @Override public Num sqrt(int precision)             { return sqrt(); }
        @Override public Num abs()                           { return new SimpleNum(Math.abs(this.value)); }
        @Override public Num negate()                        { return new SimpleNum(-this.value); }

        // ----- predicates ------------------------------------------------------
        @Override public boolean isZero()                    { return this.value == 0d; }
        @Override public boolean isPositive()                { return this.value > 0d; }
        @Override public boolean isPositiveOrZero()          { return this.value >= 0d; }
        @Override public boolean isNegative()                { return this.value < 0d; }
        @Override public boolean isNegativeOrZero()          { return this.value <= 0d; }
        @Override public boolean isEqual(Num other)          { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other)    { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other){ return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other)       { return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other){ return this.value <= ((SimpleNum) other).value; }

        // ----- misc ------------------------------------------------------------
        @Override public Number getDelegate()               { return value; }
        @Override public String getName()                   { return Double.toString(value); }
        @Override public Function<Number, Num> function()   { return n -> new SimpleNum(n.doubleValue()); }
        @Override public Num numOf(Number number)           { return new SimpleNum(number.doubleValue()); }
        @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN()                    { return Double.isNaN(value); }
        @Override public double doubleValue()               { return value; }
        @Override public int intValue()                     { return (int) value; }
        @Override public long longValue()                   { return (long) value; }
        @Override public float floatValue()                 { return (float) value; }
        @Override public int hashCode()                     { return Double.hashCode(value); }
        @Override public String toString()                  { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && isEqual((SimpleNum) obj);
        }
        @Override public int compareTo(Num o)               { return Double.compare(this.value, ((SimpleNum) o).value); }

        // ----- default methods from the interface (no need to override) -----
        @Override public Num zero()                         { return zero(); }
        @Override public Num one()                          { return one(); }
        @Override public Num hundred()                      { return new SimpleNum(100d); }
        @Override public Num min(Num other)                 { return isLessThan(other) ? this : other; }
        @Override public Num max(Num other)                 { return isGreaterThan(other) ? this : other; }
    }

    /** Minimal {@link Bar} implementation – only the close price is required. */
    private static class SimpleBar implements Bar {
        private final SimpleNum closePrice;

        SimpleBar(double close) { this.closePrice = new SimpleNum(close); }

        @Override public Num getClosePrice() { return closePrice; }

        // All other Bar methods are irrelevant for the manager – they throw
        // UnsupportedOperationException to make any accidental use obvious.
        @Override public ZonedDateTime getEndTime()               { throw new UnsupportedOperationException(); }
        @Override public Num getOpenPrice()                      { throw new UnsupportedOperationException(); }
        @Override public Num getHighPrice()                      { throw new UnsupportedOperationException(); }
        @Override public Num getLowPrice()                       { throw new UnsupportedOperationException(); }
        @Override public Num getVolume()                         { throw new UnsupportedOperationException(); }
        @Override public Num getAmount()                         { throw new UnsupportedOperationException(); }
        @Override public Duration getTimePeriod()                { throw new UnsupportedOperationException(); }
        @Override public String getName()                        { throw new UnsupportedOperationException(); }
    }

    /** Very small in‑memory {@link BarSeries}. */
    private static class SimpleBarSeries implements BarSeries {
        private final List<Bar> bars = new ArrayList<>();
        private final SimpleNum ONE = SimpleNum.one();

        SimpleBarSeries addBar(double close) {
            bars.add(new SimpleBar(close));
            return this;
        }

        @Override public String getName()                                 { return "simple-series"; }
        @Override public Num num()                                        { return ONE; }
        @Override public Bar getBar(int i)                                 { return bars.get(i); }
        @Override public List<Bar> getBarData()                            { return Collections.unmodifiableList(bars); }
        @Override public int getBeginIndex()                               { return bars.isEmpty() ? 0 : 0; }
        @Override public int getEndIndex()                                 { return bars.isEmpty() ? -1 : bars.size() - 1; }
        @Override public int getBarCount()                                 { return bars.size(); }
        @Override public boolean isEmpty()                                 { return bars.isEmpty(); }
        @Override public int getMaximumBarCount()                          { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount)      { /* no‑op */ }
        @Override public int getRemovedBarsCount()                         { return 0; }
        @Override public void addBar(Bar bar)                              { bars.add(bar); }
        @Override public void addBar(Bar bar, boolean replace)            { if (replace && !bars.isEmpty()) bars.set(bars.size() - 1, bar); else bars.add(bar); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                      Number lowPrice, Number closePrice) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                      Number lowPrice, Number closePrice, Number volume) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                      Number lowPrice, Number closePrice, Number volume, Number amount) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Number openPrice, Number highPrice, Number lowPrice,
                                      Number closePrice, Number volume) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Number openPrice, Number highPrice, Number lowPrice,
                                      Number closePrice, Number volume, Number amount) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                      String lowPrice, String closePrice) { addBar(Double.parseDouble(closePrice)); }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                      String lowPrice, String closePrice, String volume) { addBar(Double.parseDouble(closePrice)); }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                      String lowPrice, String closePrice, String volume, String amount) { addBar(Double.parseDouble(closePrice)); }
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                      Num lowPrice, Num closePrice, Num volume) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                      Num lowPrice, Num closePrice, Num volume, Num amount) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Num openPrice, Num highPrice, Num lowPrice,
                                      Num closePrice, Num volume) { addBar(closePrice.doubleValue()); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Num openPrice, Num highPrice, Num lowPrice,
                                      Num closePrice, Num volume, Num amount) { addBar(closePrice.doubleValue()); }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { /* no‑op */ }
        @Override public void addTrade(String tradeVolume, String tradePrice) { /* no‑op */ }
        @Override public void addTrade(Num tradeVolume, Num tradePrice)       { /* no‑op */ }
        @Override public void addPrice(Num price)                             { /* no‑op */ }
        @Override public void addPrice(String price)                          { /* no‑op */ }
        @Override public void addPrice(Number price)                          { /* no‑op */ }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { 
            SimpleBarSeries sub = new SimpleBarSeries();
            for (int i = startIndex; i <= endIndex && i < bars.size(); i++) {
                sub.bars.add(bars.get(i));
            }
            return sub;
        }
        @Override public String getSeriesPeriodDescription() { return "simple"; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
    }

    /** Strategy that always returns `true` for `shouldOperate`. */
    private static class AlwaysOperateStrategy implements Strategy {
        @Override public String getName() { return "always-true"; }
        @Override public Rule getEntryRule() { throw new UnsupportedOperationException(); }
        @Override public Rule getExitRule()  { throw new UnsupportedOperationException(); }
        @Override public Strategy and(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy strategy)  { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars)  { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) { }
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) { return true; }
        @Override public boolean shouldEnter(int index) { return true; }
        @Override public boolean shouldEnter(int index, TradingRecord tradingRecord) { return true; }
        @Override public boolean shouldExit(int index) { return true; }
        @Override public boolean shouldExit(int index, TradingRecord tradingRecord) { return true; }
    }

    /** Simple execution model – it opens a trade on the first call and closes it on the second. */
    private static class SimpleTradeExecutionModel implements TradeExecutionModel {
        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // If there is no open position, enter; otherwise exit.
            if (tradingRecord.getCurrentPosition() == null) {
                // use close price as dummy price
                Num price = barSeries.getBar(index).getClosePrice();
                tradingRecord.enter(index, price, amount);
            } else {
                Num price = barSeries.getBar(index).getClosePrice();
                tradingRecord.exit(index, price, amount);
            }
        }
    }

    private SimpleBarSeries series;
    private BarSeriesManager manager;
    private final CostModel zeroCost = new ZeroCostModel();

    @Before
    public void setUp() {
        // a series with three bars (close prices 10, 20, 30)
        series = new SimpleBarSeries()
                .addBar(10.0)
                .addBar(20.0)
                .addBar(30.0);
    }

    /* --------------------------------------------------------------------- */
    /* 1‑3 – getters                                                          */
    /* --------------------------------------------------------------------- */
    @Test
    public void testGettersFromFullConstructor() {
        TradeExecutionModel execModel = new SimpleTradeExecutionModel();
        manager = new BarSeriesManager(series, zeroCost, zeroCost, execModel);

        assertSame(series, manager.getBarSeries());
        assertSame(zeroCost, manager.getTransactionCostModel());
        assertSame(zeroCost, manager.getHoldingCostModel());
    }

    @Test
    public void testDefaultConstructorInitialisesDefaultModels() {
        manager = new BarSeriesManager(series);
        assertNotNull(manager.getTransactionCostModel());
        assertNotNull(manager.getHoldingCostModel());
        // default execution model is TradeOnNextOpenModel – we only check that it is not null
        assertNotNull(manager.getBarSeries());
    }

    /* --------------------------------------------------------------------- */
    /* 4‑8 – basic run() variations                                            */
    /* --------------------------------------------------------------------- */
    @Test
    public void testRunDefaultStrategyWholeSeries() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();

        TradingRecord record = manager.run(strategy);
        // The simple execution model opens on first bar, closes on second, ignores third
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithCustomTradeTypeAndAmount() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();
        Num amount = SimpleNum.one(); // amount = 1

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount);
        // Same behaviour as above – a position is opened and then closed
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithSubRangeIndexes() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();

        // Sub‑range 1‑2 (bars with close 20 and 30)
        TradingRecord record = manager.run(strategy, 1, 2);
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithSubRangeAndTradeType() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();

        TradingRecord record = manager.run(strategy, TradeType.SELL, 0, 2);
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    /* --------------------------------------------------------------------- */
    /* 9 – edge‑case handling (index trimming, empty series, etc.)            */
    /* --------------------------------------------------------------------- */
    @Test
    public void testRunWithStartIndexBeyondEnd() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();

        // start > finish – loop never executes; we still get a non‑null record
        TradingRecord record = manager.run(strategy, TradeType.BUY, 5, 2);
        assertNotNull(record);
        assertEquals(0, record.getPositionCount());
    }

    @Test
    public void testRunClampsNegativeStartAndTooLargeFinish() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();

        // start = -5 (clamped to 0), finish = 100 (clamped to series.endIndex = 2)
        TradingRecord record = manager.run(strategy, TradeType.BUY, SimpleNum.one(), -5, 100);
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunOnEmptySeries() {
        SimpleBarSeries empty = new SimpleBarSeries(); // no bars
        manager = new BarSeriesManager(empty, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();

        TradingRecord record = manager.run(strategy);
        assertNotNull(record);
        assertEquals(0, record.getPositionCount());
        assertTrue(record.isClosed()); // no open position -> considered closed
    }

    /* --------------------------------------------------------------------- */
    /* Exception scenarios                                                    */
    /* --------------------------------------------------------------------- */
    @Test(expected = NullPointerException.class)
    public void testRunThrowsWhenStrategyIsNull() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        manager.run(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRunThrowsWhenAmountIsNull() {
        manager = new BarSeriesManager(series, new SimpleTradeExecutionModel());
        Strategy strategy = new AlwaysOperateStrategy();
        manager.run(strategy, TradeType.BUY, null);
    }

    @Test
    public void testRunPropagatesExceptionFromExecutionModel() {
        // Execution model that deliberately throws
        TradeExecutionModel exploding = (idx, rec, ser, amt) -> { throw new IllegalStateException("boom"); };
        manager = new BarSeriesManager(series, exploding);
        Strategy strategy = new AlwaysOperateStrategy();

        try {
            manager.run(strategy);
            fail("Expected IllegalStateException to be propagated");
        } catch (IllegalStateException e) {
            assertEquals("boom", e.getMessage());
        }
    }

    @Test
    public void testRunWithExecutionModelThatDoesNothing() {
        TradeExecutionModel noop = (idx, rec, ser, amt) -> { /* nothing */ };
        manager = new BarSeriesManager(series, noop);
        Strategy strategy = new AlwaysOperateStrategy();

        TradingRecord record = manager.run(strategy);
        // No trades executed → no positions
        assertEquals(0, record.getPositionCount());
        assertTrue(record.isClosed());
    }
}
