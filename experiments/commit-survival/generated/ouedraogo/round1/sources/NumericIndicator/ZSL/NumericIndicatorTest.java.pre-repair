package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>
 * The library does not expose a concrete {@link BarSeries} or {@link Num}
 * implementation in the public API that can be instantiated directly.  For the
 * purpose of these tests we provide minimal in‑package implementations that are
 * sufficient for the behaviour exercised by {@code NumericIndicator}.  They
 * deliberately implement only the methods required by the indicators used in
 * the test suite.
 * </p>
 */
public class NumericIndicatorTest {

    /** Simple {@link Num} implementation backed by a {@code double}. */
    private static final class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* ---------- factory helpers ---------- */
        @Override
        public Num numOf(Number number) {
            return new SimpleNum(number.doubleValue());
        }

        @Override
        public Num numOf(String value, int precision) {
            return new SimpleNum(Double.parseDouble(value));
        }

        /* ---------- arithmetic ---------- */
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
        public Num sqrt() {
            return new SimpleNum(Math.sqrt(this.value));
        }

        @Override
        public Num abs() {
            return new SimpleNum(Math.abs(this.value));
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
        public Num remainder(Num divisor) {
            return new SimpleNum(this.value % ((SimpleNum) divisor).value);
        }

        @Override
        public Num floor() {
            return new SimpleNum(Math.floor(this.value));
        }

        @Override
        public Num ceil() {
            return new SimpleNum(Math.ceil(this.value));
        }

        @Override
        public Num log() {
            return new SimpleNum(Math.log(this.value));
        }

        @Override
        public Num sqrt(int precision) {
            // precision is ignored for this simple implementation
            return sqrt();
        }

        @Override
        public Num negate() {
            return new SimpleNum(-this.value);
        }

        /* ---------- comparisons ---------- */
        @Override
        public boolean isZero() {
            return Double.compare(value, 0.0) == 0;
        }

        @Override
        public boolean isPositive() {
            return value > 0;
        }

        @Override
        public boolean isPositiveOrZero() {
            return value >= 0;
        }

        @Override
        public boolean isNegative() {
            return value < 0;
        }

        @Override
        public boolean isNegativeOrZero() {
            return value <= 0;
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
            return new SimpleNum(Math.min(this.value, ((SimpleNum) other).value));
        }

        @Override
        public Num max(Num other) {
            return new SimpleNum(Math.max(this.value, ((SimpleNum) other).value));
        }

        /* ---------- misc ---------- */
        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public String getName() {
            return Double.toString(value);
        }

        @Override
        public boolean isNaN() {
            return Double.isNaN(value);
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public int intValue() {
            return (int) value;
        }

        @Override
        public long longValue() {
            return (long) value;
        }

        @Override
        public float floatValue() {
            return (float) value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && isEqual((SimpleNum) obj);
        }

        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }

        @Override
        public Num zero() {
            return new SimpleNum(0);
        }

        @Override
        public Num one() {
            return new SimpleNum(1);
        }

        @Override
        public Num hundred() {
            return new SimpleNum(100);
        }

        @Override
        public java.util.function.Function<Number, Num> function() {
            return n -> new SimpleNum(n.doubleValue());
        }
    }

    /** Minimal {@link Bar} implementation – only the fields required by the
     *  indicators used in the test suite are stored. */
    private static final class SimpleBar implements Bar {

        private final Num open;
        private final Num high;
        private final Num low;
        private final Num close;
        private final Num volume;
        private final ZonedDateTime endTime;

        SimpleBar(BarSeries series, ZonedDateTime endTime,
                  Number open, Number high, Number low,
                  Number close, Number volume) {
            this.endTime = endTime;
            this.open = series.num().numOf(open);
            this.high = series.num().numOf(high);
            this.low = series.num().numOf(low);
            this.close = series.num().numOf(close);
            this.volume = series.num().numOf(volume);
        }

        @Override public Num getOpenPrice() { return open; }
        @Override public Num getHighPrice() { return high; }
        @Override public Num getLowPrice() { return low; }
        @Override public Num getClosePrice() { return close; }
        @Override public Num getVolume() { return volume; }
        @Override public ZonedDateTime getEndTime() { return endTime; }

        // The remaining methods of the Bar interface are not needed for the
        // tests and therefore return dummy values.
        @Override public Num getAmount() { return open; }
        @Override public Num getTypicalPrice() { return open; }
        @Override public Num getWeightedAveragePrice() { return open; }
        @Override public Num getTrades() { return open; }
        @Override public Num getBidVolume() { return open; }
        @Override public Num getAskVolume() { return open; }
        @Override public Num getBidOpenPrice() { return open; }
        @Override public Num getBidHighPrice() { return high; }
        @Override public Num getBidLowPrice() { return low; }
        @Override public Num getBidClosePrice() { return close; }
        @Override public Num getAskOpenPrice() { return open; }
        @Override public Num getAskHighPrice() { return high; }
        @Override public Num getAskLowPrice() { return low; }
        @Override public Num getAskClosePrice() { return close; }
        @Override public Duration getTimePeriod() { return Duration.ZERO; }
    }

    /** Very small concrete {@link BarSeries} that stores a mutable list of bars
     *  and provides a {@link Num} factory based on {@link SimpleNum}. */
    private static final class SimpleBarSeries implements BarSeries {

        private final String name;
        private final List<Bar> bars = new ArrayList<>();

        SimpleBarSeries(String name) {
            this.name = name;
        }

        void addBar(Bar bar) {
            bars.add(bar);
        }

        @Override public String getName() { return name; }

        @Override public Num num() {
            // The series’ num() method returns a prototype; we simply return 0.
            return new SimpleNum(0);
        }

        @Override public Bar getBar(int i) { return bars.get(i); }

        @Override public int getBarCount() { return bars.size(); }

        @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }

        @Override public int getBeginIndex() { return 0; }

        @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }

        // The rest of the methods are not required for the test logic.
        @Override public Bar getFirstBar() { return bars.isEmpty() ? null : bars.get(0); }
        @Override public Bar getLastBar() { return bars.isEmpty() ? null : bars.get(bars.size() - 1); }
        @Override public boolean isEmpty() { return bars.isEmpty(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(Bar bar) { addBar(bar); }
        @Override public void addBar(Bar bar, boolean replace) { addBar(bar); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                     Number lowPrice, Number closePrice) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                     Number lowPrice, Number closePrice, Number volume) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                     Number lowPrice, Number closePrice, Number volume, Number amount) { /* no‑op */ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                     Number openPrice, Number highPrice, Number lowPrice,
                                     Number closePrice, Number volume) { /* no‑op */ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                     Number openPrice, Number highPrice, Number lowPrice,
                                     Number closePrice, Number volume, Number amount) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                     String lowPrice, String closePrice) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                     String lowPrice, String closePrice, String volume) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                     String lowPrice, String closePrice, String volume,
                                     String amount) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                     Num lowPrice, Num closePrice, Num volume) { /* no‑op */ }
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                     Num lowPrice, Num closePrice, Num volume, Num amount) { /* no‑op */ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                     Num openPrice, Num highPrice, Num lowPrice,
                                     Num closePrice, Num volume) { /* no‑op */ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                     Num openPrice, Num highPrice, Num lowPrice,
                                     Num closePrice, Num volume, Num amount) { /* no‑op */ }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { /* no‑op */ }
        @Override public void addTrade(String tradeVolume, String tradePrice) { /* no‑op */ }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { /* no‑op */ }
        @Override public void addPrice(Num price) { /* no‑op */ }
        @Override public void addPrice(String price) { /* no‑op */ }
        @Override public void addPrice(Number price) { /* no‑op */ }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return this; }
        @Override public Num zero() { return num().zero(); }
        @Override public Num one() { return num().one(); }
        @Override public Num hundred() { return num().hundred(); }
        @Override public Num numOf(Number number) { return new SimpleNum(number.doubleValue()); }
        @Override public java.util.function.Function<Number, Num> function() { return n -> new SimpleNum(n.doubleValue()); }
    }

    /** Indicator that returns an increasing numeric value equal to the index. */
    private static final class IncreasingIndicator implements Indicator<Num> {
        private final BarSeries series;

        IncreasingIndicator(BarSeries series) {
            this.series = series;
        }

        @Override public Num getValue(int index) {
            return series.num().numOf(index);
        }

        @Override public int getUnstableBars() { return 0; }

        @Override public BarSeries getBarSeries() { return series; }
    }

    private SimpleBarSeries series;
    private NumericIndicator base;

    @Before
    public void setUp() {
        series = new SimpleBarSeries("test-series");
        // add a few bars with deterministic values
        series.addBar(new SimpleBar(series, ZonedDateTime.now(),
                10, 12, 9, 11, 1000));
        series.addBar(new SimpleBar(series, ZonedDateTime.now().plusMinutes(1),
                11, 13, 10, 12, 1500));
        series.addBar(new SimpleBar(series, ZonedDateTime.now().plusMinutes(2),
                12, 14, 11, 13, 2000));

        // base indicator = close price
        base = NumericIndicator.closePrice(series);
    }

    /* --------------------------------------------------------------
     *  Factory methods
     * -------------------------------------------------------------- */
    @Test
    public void testOfFactory() {
        ConstantIndicator<Num> constInd = new ConstantIndicator<>(series,
                series.num().numOf(42));
        NumericIndicator ni = NumericIndicator.of(constInd);
        assertSame(constInd, ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertTrue(cp.delegate() instanceof ClosePriceIndicator);
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertTrue(vol.delegate() instanceof VolumeIndicator);
    }

    /* --------------------------------------------------------------
     *  Arithmetic operations
     * -------------------------------------------------------------- */
    @Test
    public void testPlusWithIndicatorAndNumber() {
        ConstantIndicator<Num> five = new ConstantIndicator<>(series,
                series.num().numOf(5));
        NumericIndicator plusFive = base.plus(five);          // base + 5
        NumericIndicator plusFiveAgain = plusFive.plus(3);    // (base + 5) + 3

        // index 0 close price = 11
        Num expected = series.num().numOf(11 + 5 + 3);
        assertEquals(expected, plusFiveAgain.getValue(0));
    }

    @Test
    public void testMinusWithIndicatorAndNumber() {
        ConstantIndicator<Num> three = new ConstantIndicator<>(series,
                series.num().numOf(3));
        NumericIndicator result = base.minus(three); // close - 3

        Num expected = series.num().numOf(11 - 3);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMultipliedByWithIndicatorAndNumber() {
        ConstantIndicator<Num> two = new ConstantIndicator<>(series,
                series.num().numOf(2));
        NumericIndicator result = base.multipliedBy(two).multipliedBy(4); // *2 then *4

        Num expected = series.num().numOf(11 * 2 * 4);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testDividedByWithIndicatorAndNumber() {
        ConstantIndicator<Num> two = new ConstantIndicator<>(series,
                series.num().numOf(2));
        NumericIndicator result = base.dividedBy(two).dividedBy(5); // /2 then /5

        Num expected = series.num().numOf(11.0 / 2 / 5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMinAndMax() {
        ConstantIndicator<Num> seven = new ConstantIndicator<>(series,
                series.num().numOf(7));
        ConstantIndicator<Num> twelve = new ConstantIndicator<>(series,
                series.num().numOf(12));

        NumericIndicator min = seven.min(twelve);
        NumericIndicator max = seven.max(twelve);

        assertEquals(series.num().numOf(7), min.getValue(0));
        assertEquals(series.num().numOf(12), max.getValue(0));
    }

    @Test
    public void testAbsAndSqrtAndSquared() {
        // create a negative constant indicator
        ConstantIndicator<Num> minusNine = new ConstantIndicator<>(series,
                series.num().numOf(-9));

        NumericIndicator abs = minusNine.abs();
        NumericIndicator sqrt = abs.sqrt();
        NumericIndicator squared = sqrt.squared(); // (sqrt(|-9|))^2 => 9

        assertEquals(series.num().numOf(9), abs.getValue(0));
        assertEquals(series.num().numOf(3), sqrt.getValue(0));
        assertEquals(series.num().numOf(9), squared.getValue(0));
    }

    /* --------------------------------------------------------------
     *  Helper indicator factories
     * -------------------------------------------------------------- */
    @Test
    public void testSmaEmaStddev() {
        NumericIndicator sma = base.sma(2);
        NumericIndicator ema = base.ema(2);
        NumericIndicator stddev = base.stddev(2);

        assertTrue(sma.delegate() instanceof SMAIndicator);
        assertTrue(ema.delegate() instanceof EMAIndicator);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
    }

    @Test
    public void testHighestLowestPrevious() {
        NumericIndicator highest = base.highest(2);
        NumericIndicator lowest = base.lowest(2);
        NumericIndicator prev = base.previous(2);
        Indicator<Num> prevDefault = base.previous(); // barCount = 1

        assertTrue(highest.delegate() instanceof HighestValueIndicator);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        assertTrue(prev.delegate() instanceof PreviousValueIndicator);
        assertTrue(prevDefault instanceof PreviousValueIndicator);
    }

    /* --------------------------------------------------------------
     *  Rule creation
     * -------------------------------------------------------------- */
    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        ConstantIndicator<Num> low = new ConstantIndicator<>(series,
                series.num().numOf(5));
        ConstantIndicator<Num> high = new ConstantIndicator<>(series,
                series.num().numOf(10));

        Rule upRule = base.crossedOver(high);
        Rule downRule = base.crossedUnder(low);
        Rule upNumberRule = base.crossedOver(7);
        Rule downNumberRule = base.crossedUnder(3);

        assertTrue(upRule instanceof CrossedUpIndicatorRule);
        assertTrue(downRule instanceof CrossedDownIndicatorRule);
        assertTrue(upNumberRule instanceof CrossedUpIndicatorRule);
        assertTrue(downNumberRule instanceof CrossedDownIndicatorRule);
    }

    @Test
    public void testComparisonRules() {
        ConstantIndicator<Num> threshold = new ConstantIndicator<>(series,
                series.num().numOf(10));

        Rule greaterRule = base.isGreaterThan(threshold);
        Rule lessRule = base.isLessThan(15);
        Rule greaterNumberRule = base.isGreaterThan(8);
        Rule lessNumberRule = base.isLessThan(threshold);

        assertTrue(greaterRule instanceof OverIndicatorRule);
        assertTrue(lessRule instanceof UnderIndicatorRule);
        assertTrue(greaterNumberRule instanceof OverIndicatorRule);
        assertTrue(lessNumberRule instanceof UnderIndicatorRule);
    }

    /* --------------------------------------------------------------
     *  Delegated methods
     * -------------------------------------------------------------- */
    @Test
    public void testDelegateMethods() {
        // getValue should simply forward to the underlying ClosePriceIndicator
        Num closeAt0 = series.getBar(0).getClosePrice();
        assertEquals(closeAt0, base.getValue(0));

        // getBarSeries should be the original series
        assertSame(series, base.getBarSeries());

        // toString should delegate
        assertEquals(base.delegate().toString(), base.toString());
    }

    /* --------------------------------------------------------------
     *  Additional sanity check – arithmetic chain with increasing indicator
     * -------------------------------------------------------------- */
    @Test
    public void testChainWithIncreasingIndicator() {
        Indicator<Num> inc = new IncreasingIndicator(series);
        NumericIndicator ni = NumericIndicator.of(inc)
                .plus(2)
                .minus(1)
                .multipliedBy(3)
                .dividedBy(2); // ((i + 2 - 1) * 3) / 2

        // For index 4: ((4 + 2 - 1) * 3) / 2 = (5 * 3) / 2 = 7.5
        Num expected = series.num().numOf(7.5);
        assertEquals(expected, ni.getValue(4));
    }
}
