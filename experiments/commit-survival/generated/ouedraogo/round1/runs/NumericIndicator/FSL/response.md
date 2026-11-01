###Test START##
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;

/**
 * Unit tests for {@link NumericIndicator}. The tests use a very small in‑memory
 * implementation of {@link BarSeries} and a tiny {@link Num} implementation
 * ( {@code SimpleNum}) that operates on {@code double} values.  This keeps the
 * tests independent from the rest of the ta4j library while still exercising
 * all public methods of {@code NumericIndicator}.
 */
public class NumericIndicatorTest {

    /** Very small {@link Num} implementation based on {@code double}. */
    private static final class SimpleNum implements org.ta4j.core.num.Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        // -----------------------------------------------------------------
        // Basic arithmetic – only the operations used by NumericIndicator are
        // implemented.  All other methods delegate to the same simple logic.
        // -----------------------------------------------------------------
        @Override
        public org.ta4j.core.num.Num plus(org.ta4j.core.num.Num augend) {
            return new SimpleNum(this.value + ((SimpleNum) augend).value);
        }

        @Override
        public org.ta4j.core.num.Num minus(org.ta4j.core.num.Num subtrahend) {
            return new SimpleNum(this.value - ((SimpleNum) subtrahend).value);
        }

        @Override
        public org.ta4j.core.num.Num multipliedBy(org.ta4j.core.num.Num multiplicand) {
            return new SimpleNum(this.value * ((SimpleNum) multiplicand).value);
        }

        @Override
        public org.ta4j.core.num.Num dividedBy(org.ta4j.core.num.Num divisor) {
            return new SimpleNum(this.value / ((SimpleNum) divisor).value);
        }

        @Override
        public org.ta4j.core.num.Num sqrt() {
            return new SimpleNum(Math.sqrt(this.value));
        }

        @Override
        public org.ta4j.core.num.Num abs() {
            return new SimpleNum(Math.abs(this.value));
        }

        // -----------------------------------------------------------------
        // Comparison helpers used by the rule tests.
        // -----------------------------------------------------------------
        @Override
        public boolean isGreaterThan(org.ta4j.core.num.Num other) {
            return this.value > ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThan(org.ta4j.core.num.Num other) {
            return this.value < ((SimpleNum) other).value;
        }

        @Override
        public boolean isEqual(org.ta4j.core.num.Num other) {
            return Double.compare(this.value, ((SimpleNum) other).value) == 0;
        }

        // -----------------------------------------------------------------
        // Boiler‑plate – most methods are either trivial or not needed for the
        // tests, so they are implemented in the simplest possible way.
        // -----------------------------------------------------------------
        @Override public Number getDelegate() { return value; }
        @Override public String getName() { return Double.toString(value); }
        @Override public org.ta4j.core.num.Num remainder(org.ta4j.core.num.Num divisor) { return new SimpleNum(this.value % ((SimpleNum) divisor).value); }
        @Override public org.ta4j.core.num.Num floor() { return new SimpleNum(Math.floor(this.value)); }
        @Override public org.ta4j.core.num.Num ceil() { return new SimpleNum(Math.ceil(this.value)); }
        @Override public org.ta4j.core.num.Num pow(int n) { return new SimpleNum(Math.pow(this.value, n)); }
        @Override public org.ta4j.core.num.Num pow(org.ta4j.core.num.Num n) { return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value)); }
        @Override public org.ta4j.core.num.Num log() { return new SimpleNum(Math.log(this.value)); }
        @Override public org.ta4j.core.num.Num sqrt(int precision) { return sqrt(); }
        @Override public org.ta4j.core.num.Num negate() { return new SimpleNum(-this.value); }
        @Override public boolean isZero() { return this.value == 0.0; }
        @Override public boolean isPositive() { return this.value > 0.0; }
        @Override public boolean isPositiveOrZero() { return this.value >= 0.0; }
        @Override public boolean isNegative() { return this.value < 0.0; }
        @Override public boolean isNegativeOrZero() { return this.value <= 0.0; }
        @Override public boolean isGreaterThanOrEqual(org.ta4j.core.num.Num other) { return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(org.ta4j.core.num.Num other) { return this.value <= ((SimpleNum) other).value; }
        @Override public org.ta4j.core.num.Num min(org.ta4j.core.num.Num other) { return this.isLessThan(other) ? this : other; }
        @Override public org.ta4j.core.num.Num max(org.ta4j.core.num.Num other) { return this.isGreaterThan(other) ? this : other; }
        @Override public java.util.function.Function<Number, org.ta4j.core.num.Num> function() { return SimpleNum::new; }
        @Override public org.ta4j.core.num.Num numOf(Number value) { return new SimpleNum(value.doubleValue()); }
        @Override public org.ta4j.core.num.Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN() { return Double.isNaN(this.value); }
        @Override public double doubleValue() { return this.value; }
        @Override public int intValue() { return (int) this.value; }
        @Override public long longValue() { return (long) this.value; }
        @Override public float floatValue() { return (float) this.value; }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(this.value, ((SimpleNum) obj).value) == 0;
        }
        @Override public int compareTo(org.ta4j.core.num.Num o) {
            return Double.compare(this.value, ((SimpleNum) o).value);
        }
    }

    /** Minimal {@link BarSeries} implementation – only the methods required by the tests are provided. */
    private static final class SimpleBarSeries implements BarSeries {

        private final SimpleNum zero = new SimpleNum(0.0);

        @Override public String getName() { return "simple-series"; }

        @Override public org.ta4j.core.num.Num num() { return zero; }

        // The default methods in the interface already implement
        // numOf, zero, one, hundred, etc.  They rely on {@code num()}.
        // No additional behaviour is needed for the tests.

        @Override public org.ta4j.core.Bar getBar(int i) { throw new UnsupportedOperationException(); }

        @Override public int getBarCount() { return 0; }

        @Override public List<org.ta4j.core.Bar> getBarData() { return Collections.emptyList(); }

        @Override public int getBeginIndex() { return 0; }

        @Override public int getEndIndex() { return -1; }

        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }

        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }

        @Override public int getRemovedBarsCount() { return 0; }

        @Override public void addBar(org.ta4j.core.Bar bar) { throw new UnsupportedOperationException(); }

        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) { throw new UnsupportedOperationException(); }

        @Override public void addBar(java.time.Duration timePeriod, java.time.ZonedDateTime endTime) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) { throw new UnsupportedOperationException(); }

        @Override public void addBar(java.time.Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume) { throw new UnsupportedOperationException(); }

        @Override public void addBar(java.time.Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume, String amount) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, org.ta4j.core.num.Num openPrice, org.ta4j.core.num.Num highPrice, org.ta4j.core.num.Num lowPrice, org.ta4j.core.num.Num closePrice, org.ta4j.core.num.Num volume) { throw new UnsupportedOperationException(); }

        @Override public void addBar(ZonedDateTime endTime, org.ta4j.core.num.Num openPrice, org.ta4j.core.num.Num highPrice, org.ta4j.core.num.Num lowPrice, org.ta4j.core.num.Num closePrice, org.ta4j.core.num.Num volume, org.ta4j.core.num.Num amount) { throw new UnsupportedOperationException(); }

        @Override public void addBar(java.time.Duration timePeriod, ZonedDateTime endTime, org.ta4j.core.num.Num openPrice, org.ta4j.core.num.Num highPrice, org.ta4j.core.num.Num lowPrice, org.ta4j.core.num.Num closePrice, org.ta4j.core.num.Num volume) { throw new UnsupportedOperationException(); }

        @Override public void addBar(java.time.Duration timePeriod, ZonedDateTime endTime, org.ta4j.core.num.Num openPrice, org.ta4j.core.num.Num highPrice, org.ta4j.core.num.Num lowPrice, org.ta4j.core.num.Num closePrice, org.ta4j.core.num.Num volume, org.ta4j.core.num.Num amount) { throw new UnsupportedOperationException(); }

        @Override public void addTrade(Number tradeVolume, Number tradePrice) { throw new UnsupportedOperationException(); }

        @Override public void addTrade(String tradeVolume, String tradePrice) { throw new UnsupportedOperationException(); }

        @Override public void addTrade(org.ta4j.core.num.Num tradeVolume, org.ta4j.core.num.Num tradePrice) { throw new UnsupportedOperationException(); }

        @Override public void addPrice(org.ta4j.core.num.Num price) { throw new UnsupportedOperationException(); }

        @Override public void addPrice(String price) { throw new UnsupportedOperationException(); }

        @Override public void addPrice(Number price) { throw new UnsupportedOperationException(); }

        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
    }

    /** Helper to create a constant indicator with the given double value. */
    private static Indicator<org.ta4j.core.num.Num> constant(double value) {
        SimpleBarSeries series = new SimpleBarSeries();
        SimpleNum num = new SimpleNum(value);
        return new ConstantIndicator<>(series, num);
    }

    @Test
    public void testArithmeticOperationsWithIndicator() {
        Indicator<org.ta4j.core.num.Num> a = constant(5);
        Indicator<org.ta4j.core.num.Num> b = constant(3);

        NumericIndicator na = NumericIndicator.of(a);

        // plus
        NumericIndicator plus = na.plus(b);
        assertEquals(8.0, plus.getValue(0).doubleValue(), 1e-9);

        // minus
        NumericIndicator minus = na.minus(b);
        assertEquals(2.0, minus.getValue(0).doubleValue(), 1e-9);

        // multipliedBy
        NumericIndicator mul = na.multipliedBy(b);
        assertEquals(15.0, mul.getValue(0).doubleValue(), 1e-9);

        // dividedBy
        NumericIndicator div = na.dividedBy(b);
        assertEquals(5.0 / 3.0, div.getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testArithmeticOperationsWithNumber() {
        Indicator<org.ta4j.core.num.Num> a = constant(7);
        NumericIndicator na = NumericIndicator.of(a);

        // plus(Number)
        NumericIndicator plus = na.plus(2);
        assertEquals(9.0, plus.getValue(0).doubleValue(), 1e-9);

        // minus(Number)
        NumericIndicator minus = na.minus(4);
        assertEquals(3.0, minus.getValue(0).doubleValue(), 1e-9);

        // multipliedBy(Number)
        NumericIndicator mul = na.multipliedBy(3);
        assertEquals(21.0, mul.getValue(0).doubleValue(), 1e-9);

        // dividedBy(Number)
        NumericIndicator div = na.dividedBy(2);
        assertEquals(3.5, div.getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testMinAndMax() {
        Indicator<org.ta4j.core.num.Num> low = constant(2);
        Indicator<org.ta4j.core.num.Num> high = constant(9);
        NumericIndicator ni = NumericIndicator.of(high);

        // min with indicator
        NumericIndicator minInd = ni.min(low);
        assertEquals(2.0, minInd.getValue(0).doubleValue(), 1e-9);

        // max with indicator
        NumericIndicator maxInd = ni.max(low);
        assertEquals(9.0, maxInd.getValue(0).doubleValue(), 1e-9);

        // min with number
        assertEquals(5.0, ni.min(5).getValue(0).doubleValue(), 1e-9);

        // max with number
        assertEquals(9.0, ni.max(5).getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testUnaryOperations() {
        NumericIndicator negative = NumericIndicator.of(constant(-4));

        // abs
        NumericIndicator abs = negative.abs();
        assertEquals(4.0, abs.getValue(0).doubleValue(), 1e-9);

        // sqrt
        NumericIndicator sqrt = NumericIndicator.of(constant(9)).sqrt();
        assertEquals(3.0, sqrt.getValue(0).doubleValue(), 1e-9);

        // squared (delegates to multipliedBy)
        NumericIndicator squared = NumericIndicator.of(constant(3)).squared();
        assertEquals(9.0, squared.getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testStatisticalIndicators() {
        // Use a constant indicator; SMA, EMA, STDDEV, Highest, Lowest should all
        // return the same constant value.
        Indicator<org.ta4j.core.num.Num> constant = constant(5);
        NumericIndicator ni = NumericIndicator.of(constant);

        NumericIndicator sma = ni.sma(3);
        assertTrue(sma.delegate() instanceof SMAIndicator);
        assertEquals(5.0, sma.getValue(0).doubleValue(), 1e-9);

        NumericIndicator ema = ni.ema(3);
        assertTrue(ema.delegate() instanceof EMAIndicator);
        assertEquals(5.0, ema.getValue(0).doubleValue(), 1e-9);

        NumericIndicator stddev = ni.stddev(3);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
        // Standard deviation of a constant series is zero
        assertEquals(0.0, stddev.getValue(0).doubleValue(), 1e-9);

        NumericIndicator highest = ni.highest(3);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);
        assertEquals(5.0, highest.getValue(0).doubleValue(), 1e-9);

        NumericIndicator lowest = ni.lowest(3);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        assertEquals(5.0, lowest.getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testPreviousIndicators() {
        Indicator<org.ta4j.core.num.Num> series = constant(10);
        NumericIndicator ni = NumericIndicator.of(series);

        // previous(2)
        NumericIndicator prev2 = ni.previous(2);
        assertTrue(prev2.delegate() instanceof PreviousValueIndicator);
        // With a constant series, the previous value equals the constant
        assertEquals(10.0, prev2.getValue(0).doubleValue(), 1e-9);

        // previous() == previous(1)
        Indicator<org.ta4j.core.num.Num> prev1 = ni.previous();
        assertTrue(prev1 instanceof PreviousValueIndicator);
        assertEquals(10.0, prev1.getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // Indicator a: 1, 2, 3
        // Indicator b: 3, 2, 1
        SimpleBarSeries series = new SimpleBarSeries();

        Indicator<org.ta4j.core.num.Num> a = new ConstantIndicator<>(series, new SimpleNum(1));
        Indicator<org.ta4j.core.num.Num> b = new ConstantIndicator<>(series, new SimpleNum(3));

        NumericIndicator na = NumericIndicator.of(a);
        NumericIndicator nb = NumericIndicator.of(b);

        // crossedOver should be false because a never goes above b
        Rule crossedOverRule = na.crossedOver(nb);
        assertFalse(crossedOverRule.isSatisfied(0));

        // crossedUnder should be true because a is below b
        Rule crossedUnderRule = na.crossedUnder(nb);
        assertTrue(crossedUnderRule.isSatisfied(0));

        // crossedOver with a number (threshold 2) – a (1) is not > 2
        Rule crossedOverNum = na.crossedOver(2);
        assertFalse(crossedOverNum.isSatisfied(0));
    }

    @Test
    public void testComparisonRules() {
        Indicator<org.ta4j.core.num.Num> higher = constant(8);
        Indicator<org.ta4j.core.num.Num> lower = constant(3);
        NumericIndicator nh = NumericIndicator.of(higher);
        NumericIndicator nl = NumericIndicator.of(lower);

        // isGreaterThan
        Rule gtRule = nh.isGreaterThan(nl);
        assertTrue(gtRule.isSatisfied(0));

        // isLessThan
        Rule ltRule = nl.isLessThan(nh);
        assertTrue(ltRule.isSatisfied(0));

        // Number variants
        Rule gtNum = nh.isGreaterThan(5);
        assertTrue(gtNum.isSatisfied(0));

        Rule ltNum = nl.isLessThan(5);
        assertTrue(ltNum.isSatisfied(0));
    }
}
###Test END##