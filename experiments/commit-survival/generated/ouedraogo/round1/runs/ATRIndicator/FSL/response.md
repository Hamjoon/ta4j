###Test START##
package org.ta4j.core.indicators;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;
import java.time.*;
import java.time.ZonedDateTime;

/**
 * Minimal {@link Num} implementation based on {@code double}.
 */
class SimpleNum implements Num {
    private final double value;

    SimpleNum(double value) {
        this.value = value;
    }

    // ----- factory helpers -----
    @Override
    public Num zero() {
        return new SimpleNum(0.0);
    }

    @Override
    public Num one() {
        return new SimpleNum(1.0);
    }

    @Override
    public Num hundred() {
        return new SimpleNum(100.0);
    }

    // ----- arithmetic -----
    @Override
    public Num plus(Num augend) {
        return new SimpleNum(this.value + ((SimpleNum) augend).value);
    }

    @Override
    public Num minus(Num subtrahend) {
        return new SimpleNum(this.value - ((SimpleNum) subtrahend).value);
    }

    @Override
    public Num multipliedBy(Num multiplicand) {
        return new SimpleNum(this.value * ((SimpleNum) multiplicand).value);
    }

    @Override
    public Num dividedBy(Num divisor) {
        return new SimpleNum(this.value / ((SimpleNum) divisor).value);
    }

    @Override
    public Num remainder(Num divisor) {
        return new SimpleNum(this.value % ((SimpleNum) divisor).value);
    }

    // ----- rounding / power -----
    @Override
    public Num floor() {
        return new SimpleNum(Math.floor(this.value));
    }

    @Override
    public Num ceil() {
        return new SimpleNum(Math.ceil(this.value));
    }

    @Override
    public Num pow(int n) {
        return new SimpleNum(Math.pow(this.value, n));
    }

    @Override
    public Num pow(Num n) {
        return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value));
    }

    @Override
    public Num log() {
        return new SimpleNum(Math.log(this.value));
    }

    @Override
    public Num sqrt() {
        return new SimpleNum(Math.sqrt(this.value));
    }

    @Override
    public Num sqrt(int precision) {
        // precision is ignored for the simple implementation
        return sqrt();
    }

    // ----- misc -----
    @Override
    public Num abs() {
        return new SimpleNum(Math.abs(this.value));
    }

    @Override
    public Num negate() {
        return new SimpleNum(-this.value);
    }

    @Override
    public boolean isZero() {
        return this.value == 0.0;
    }

    @Override
    public boolean isPositive() {
        return this.value > 0.0;
    }

    @Override
    public boolean isPositiveOrZero() {
        return this.value >= 0.0;
    }

    @Override
    public boolean isNegative() {
        return this.value < 0.0;
    }

    @Override
    public boolean isNegativeOrZero() {
        return this.value <= 0.0;
    }

    @Override
    public boolean isEqual(Num other) {
        return Double.compare(this.value, ((SimpleNum) other).value) == 0;
    }

    @Override
    public boolean isGreaterThan(Num other) {
        return this.value > ((SimpleNum) other).value;
    }

    @Override
    public boolean isGreaterThanOrEqual(Num other) {
        return this.value >= ((SimpleNum) other).value;
    }

    @Override
    public boolean isLessThan(Num other) {
        return this.value < ((SimpleNum) other).value;
    }

    @Override
    public boolean isLessThanOrEqual(Num other) {
        return this.value <= ((SimpleNum) other).value;
    }

    @Override
    public Num min(Num other) {
        return this.isLessThan(other) ? this : other;
    }

    @Override
    public Num max(Num other) {
        return this.isGreaterThan(other) ? this : other;
    }

    @Override
    public java.util.function.Function<Number, Num> function() {
        return n -> new SimpleNum(n.doubleValue());
    }

    @Override
    public Num numOf(Number value) {
        return new SimpleNum(value.doubleValue());
    }

    @Override
    public Num numOf(String value, int precision) {
        return new SimpleNum(Double.parseDouble(value));
    }

    @Override
    public boolean isNaN() {
        return Double.isNaN(this.value);
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return (int) this.value;
    }

    @Override
    public long longValue() {
        return (long) this.value;
    }

    @Override
    public float floatValue() {
        return (float) this.value;
    }

    @Override
    public Number getDelegate() {
        return this.value;
    }

    @Override
    public String getName() {
        return "SimpleNum";
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SimpleNum)) return false;
        SimpleNum other = (SimpleNum) obj;
        return Double.compare(this.value, other.value) == 0;
    }

    @Override
    public int compareTo(Num o) {
        return Double.compare(this.value, ((SimpleNum) o).value);
    }
}

/**
 * Minimal {@link Bar} implementation required by {@link TRIndicator}.
 */
class SimpleBar implements org.ta4j.core.Bar {
    private final ZonedDateTime endTime;
    private final SimpleNum open;
    private final SimpleNum high;
    private final SimpleNum low;
    private final SimpleNum close;
    private final SimpleNum volume;

    SimpleBar(ZonedDateTime endTime, double open, double high, double low, double close, double volume) {
        this.endTime = endTime;
        this.open = new SimpleNum(open);
        this.high = new SimpleNum(high);
        this.low = new SimpleNum(low);
        this.close = new SimpleNum(close);
        this.volume = new SimpleNum(volume);
    }

    @Override public ZonedDateTime getEndTime() { return endTime; }
    @Override public SimpleNum getOpenPrice() { return open; }
    @Override public SimpleNum getClosePrice() { return close; }
    @Override public SimpleNum getLowPrice() { return low; }
    @Override public SimpleNum getHighPrice() { return high; }
    @Override public SimpleNum getVolume() { return volume; }
    @Override public SimpleNum getAmount() { return null; }
    @Override public SimpleNum getTrades() { return null; }
    @Override public SimpleNum getBidVolume() { return null; }
    @Override public SimpleNum getAskVolume() { return null; }
    @Override public SimpleNum getBidPrice() { return null; }
    @Override public SimpleNum getAskPrice() { return null; }
}

/**
 * Minimal {@link BarSeries} implementation required by the indicators.
 */
class SimpleBarSeries implements BarSeries {
    private final List<Bar> bars = new ArrayList<>();

    @Override public String getName() { return "test-series"; }

    @Override public Num num() { return new SimpleNum(0.0); }

    @Override public Bar getBar(int i) { return bars.get(i); }

    @Override public int getBarCount() { return bars.size(); }

    @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }

    @Override public int getBeginIndex() { return 0; }

    @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }

    @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }

    @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }

    @Override public int getRemovedBarsCount() { return 0; }

    @Override public void addBar(Bar bar) { bars.add(bar); }

    @Override public void addBar(Bar bar, boolean replace) { bars.add(bar); }

    // The remaining default methods are not required for the tests.
    @Override public Bar getFirstBar() { return bars.isEmpty() ? null : bars.get(0); }
    @Override public Bar getLastBar() { return bars.isEmpty() ? null : bars.get(bars.size() - 1); }
    @Override public boolean isEmpty() { return bars.isEmpty(); }
    @Override public String getSeriesPeriodDescription() { return ""; }
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) { }
    @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice) { }
    @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume) { }
    @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) { }
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume) { }
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) { }
    @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice) { }
    @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume) { }
    @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume, String amount) { }
    @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) { }
    @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) { }
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) { }
    @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) { }
    @Override public void addTrade(Number tradeVolume, Number tradePrice) { }
    @Override public void addTrade(String tradeVolume, String tradePrice) { }
    @Override public void addTrade(Num tradeVolume, Num tradePrice) { }
    @Override public void addPrice(Num price) { }
    @Override public void addPrice(String price) { }
    @Override public void addPrice(Number price) { }
    @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return null; }
}

/**
 * JUnit‑4 test suite for {@link ATRIndicator}.
 */
public class ATRIndicatorTest {

    private SimpleBarSeries series;
    private static final int BAR_COUNT = 3;

    @Before
    public void setUp() {
        series = new SimpleBarSeries();

        // Create 5 bars with deterministic prices
        series.addBar(new SimpleBar(ZonedDateTime.now().minusDays(5), 10, 12, 9, 11, 1000));
        series.addBar(new SimpleBar(ZonedDateTime.now().minusDays(4), 11, 13, 10, 12, 1100));
        series.addBar(new SimpleBar(ZonedDateTime.now().minusDays(3), 12, 14, 11, 13, 1200));
        series.addBar(new SimpleBar(ZonedDateTime.now().minusDays(2), 13, 15, 12, 14, 1300));
        series.addBar(new SimpleBar(ZonedDateTime.now().minusDays(1), 14, 16, 13, 15, 1400));
    }

    @Test
    public void testConstructorAndBasicGetters() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);

        // TRIndicator must be non‑null and refer to the same BarSeries
        assertNotNull(atr.getTRIndicator());
        assertSame(series, atr.getTRIndicator().getBarSeries());

        // barCount getter must return the value supplied to the constructor
        assertEquals(BAR_COUNT, atr.getBarCount());

        // unstable bars must be equal to the bar count
        assertEquals(BAR_COUNT, atr.getUnstableBars());

        // toString must contain class name and bar count
        String s = atr.toString();
        assertTrue(s.contains("ATRIndicator"));
        assertTrue(s.contains(String.valueOf(BAR_COUNT)));
    }

    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        TRIndicator tr = new TRIndicator(series);
        MMAIndicator mma = new MMAIndicator(tr, BAR_COUNT);

        // compare the first 5 values (the series has 5 bars)
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = mma.getValue(i);
            Num actual = atr.getValue(i);
            assertEquals("Value mismatch at index " + i, expected, actual);
        }
    }

    @Test
    public void testGetValueConsistencyWithDirectTRIndicator() {
        // Verify that the first computed ATR value equals the first TR value
        // because MMA with barCount = 1 returns the raw value.
        ATRIndicator atrBarCount1 = new ATRIndicator(series, 1);
        TRIndicator tr = new TRIndicator(series);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrVal = atrBarCount1.getValue(i);
            Num trVal = tr.getValue(i);
            assertEquals("ATR(1) should equal TR at index " + i, trVal, atrVal);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfBounds() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // series has 5 bars; index 10 is out of range and should throw
        atr.getValue(10);
    }
}
###Test END##