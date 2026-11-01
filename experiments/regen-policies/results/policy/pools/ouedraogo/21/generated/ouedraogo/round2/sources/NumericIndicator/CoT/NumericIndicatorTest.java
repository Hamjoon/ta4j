package org.ta4j.core.indicators.numeric;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;


import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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

/**
 * JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * The tests deliberately use only the classes, constructors and methods that are
 * listed in the source code of {@code NumericIndicator} and in the dependency
 * signatures provided in the exercise.
 *
 * A very small in‑memory implementation of {@link BarSeries},
 * {@link NumFactory} and {@link Num} is provided inside the test class to avoid
 * external library dependencies while still exercising the real TA‑4J objects.
 */
public class NumericIndicatorTest {

    /* --------------------------------------------------------------------- *
     *  Minimal helper implementations (Num, NumFactory, BarSeries)
     * --------------------------------------------------------------------- */

    /** Simple immutable Num implementation based on {@code double}. */
    private static final class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        @Override public Number getDelegate()                     { return value; }
        @Override public NumFactory getNumFactory()               { return SimpleNumFactory.INSTANCE; }
        @Override public String getName()                         { return Double.toString(value); }
        @Override public Num plus(Num augend)                     { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend)                 { return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand)       { return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor) {
            double d = ((SimpleNum) divisor).value;
            if (d == 0.0) {
                throw new ArithmeticException("division by zero");
            }
            return new SimpleNum(this.value / d);
        }
        @Override public Num remainder(Num divisor)               { return new SimpleNum(this.value % ((SimpleNum) divisor).value); }
        @Override public Num floor()                              { return new SimpleNum(Math.floor(this.value)); }
        @Override public Num ceil()                               { return new SimpleNum(Math.ceil(this.value)); }
        @Override public Num pow(int n)                           { return new SimpleNum(Math.pow(this.value, n)); }
        @Override public Num pow(Num n)                           { return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value)); }
        @Override public Num log()                                { return new SimpleNum(Math.log(this.value)); }
        @Override public Num sqrt() {
            if (this.value < 0) {
                throw new IllegalArgumentException("sqrt of negative number");
            }
            return new SimpleNum(Math.sqrt(this.value));
        }
        @Override public Num sqrt(int precision)                 { return sqrt(); }
        @Override public Num abs()                                { return new SimpleNum(Math.abs(this.value)); }
        @Override public Num negate()                             { return new SimpleNum(-this.value); }
        @Override public boolean isZero()                         { return this.value == 0.0; }
        @Override public boolean isPositive()                     { return this.value > 0.0; }
        @Override public boolean isPositiveOrZero()               { return this.value >= 0.0; }
        @Override public boolean isNegative()                     { return this.value < 0.0; }
        @Override public boolean isNegativeOrZero()               { return this.value <= 0.0; }
        @Override public boolean isEqual(Num other)               { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other)         { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other)  { return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other)            { return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other)     { return this.value <= ((SimpleNum) other).value; }
        @Override public Num min(Num other)                       { return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other)                       { return this.isGreaterThan(other) ? this : other; }
        @Override public boolean isNaN()                         { return Double.isNaN(this.value); }
        @Override public int intValue()                           { return (int) this.value; }
        @Override public long longValue()                         { return (long) this.value; }
        @Override public float floatValue()                       { return (float) this.value; }
        @Override public double doubleValue()                     { return this.value; }
        @Override public BigDecimal bigDecimalValue()             { return BigDecimal.valueOf(this.value); }
        @Override public int hashCode()                           { return Double.hashCode(value); }
        @Override public String toString()                        { return Double.toString(value); }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleNum)) return false;
            return Double.compare(value, ((SimpleNum) o).value) == 0;
        }
    }

    /** Simple factory that creates {@link SimpleNum} instances. */
    private static final class SimpleNumFactory implements org.ta4j.core.num.NumFactory {
        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();

        private SimpleNumFactory() {}

        @Override
        public Num numOf(Number n) {
            return new SimpleNum(n.doubleValue());
        }
    }

    /** Minimal in‑memory {@link BarSeries}. Only the methods required by the
     *  tests are implemented; everything else returns a harmless default. */
    private static final class SimpleBarSeries implements BarSeries {

        private final SimpleNumFactory factory = SimpleNumFactory.INSTANCE;

        @Override public org.ta4j.core.num.NumFactory numFactory() { return factory; }

        /* The remaining methods are not used by the tests; they return dummy data. */
        @Override public org.ta4j.core.BaseBarConvertibleBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "SimpleSeries"; }
        @Override public org.ta4j.core.Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return 0; }
        @Override public List<org.ta4j.core.Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return -1; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return this; }
        @Override public void addBar(org.ta4j.core.Bar bar) {}
        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(Number price) {}
        @Override public String getSeriesPeriodDescription() { return ""; }
    }

    /** Simple Indicator that returns a predefined array of values. */
    private static final class ArrayIndicator implements Indicator<Num> {
        private final Num[] values;
        private final BarSeries series;

        ArrayIndicator(BarSeries series, double... vals) {
            this.series = series;
            this.values = new Num[vals.length];
            for (int i = 0; i < vals.length; i++) {
                this.values[i] = new SimpleNum(vals[i]);
            }
        }

        @Override public Num getValue(int index) {
            if (index < 0 || index >= values.length) {
                throw new IndexOutOfBoundsException();
            }
            return values[index];
        }

        @Override public int getUnstableBars() { return 0; }

        @Override public BarSeries getBarSeries() { return series; }
    }

    private BarSeries series;
    private NumericIndicator constFive;
    private NumericIndicator constMinusThree;

    @Before
    public void setUp() {
        series = new SimpleBarSeries();
        constFive = NumericIndicator.of(new ConstantIndicator<>(series, new SimpleNum(5.0)));
        constMinusThree = NumericIndicator.of(new ConstantIndicator<>(series, new SimpleNum(-3.0)));
    }

    /* --------------------------------------------------------------------- *
     *  1. List of public methods (signature only)
     * --------------------------------------------------------------------- */

    // The list is provided as comments for reference; the tests below cover them.
    /*
     * public static NumericIndicator of(Indicator<Num> delegate)
     * public static NumericIndicator closePrice(BarSeries bs)
     * public static NumericIndicator volume(BarSeries bs)
     * public Indicator<Num> delegate()
     * public NumericIndicator plus(Indicator<Num> other)
     * public NumericIndicator plus(Number n)
     * public NumericIndicator minus(Indicator<Num> other)
     * public NumericIndicator minus(Number n)
     * public NumericIndicator multipliedBy(Indicator<Num> other)
     * public NumericIndicator multipliedBy(Number n)
     * public NumericIndicator dividedBy(Indicator<Num> other)
     * public NumericIndicator dividedBy(Number n)
     * public NumericIndicator min(Indicator<Num> other)
     * public NumericIndicator min(Number n)
     * public NumericIndicator max(Indicator<Num> other)
     * public NumericIndicator max(Number n)
     * public NumericIndicator abs()
     * public NumericIndicator sqrt()
     * public NumericIndicator squared()
     * public NumericIndicator sma(int barCount)
     * public NumericIndicator ema(int barCount)
     * public NumericIndicator stddev(int barCount)
     * public NumericIndicator highest(int barCount)
     * public NumericIndicator lowest(int barCount)
     * public NumericIndicator previous(int barCount)
     * public Indicator<Num> previous()
     * public Rule crossedOver(Indicator<Num> other)
     * public Rule crossedOver(Number n)
     * public Rule crossedUnder(Indicator<Num> other)
     * public Rule crossedUnder(Number n)
     * public Rule isGreaterThan(Indicator<Num> other)
     * public Rule isGreaterThan(Number n)
     * public Rule isLessThan(Indicator<Num> other)
     * public Rule isLessThan(Number n)
     * private Indicator<Num> createConstant(Number n)
     * public Num getValue(int index)
     * public int getUnstableBars()
     * public BarSeries getBarSeries()
     * public String toString()
     */

    /* --------------------------------------------------------------------- *
     *  2. Basic functionality tests
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusWithIndicator() {
        NumericIndicator result = constFive.plus(constMinusThree);
        // 5 + (-3) = 2
        assertEquals(2.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator result = constFive.plus(10);
        // 5 + 10 = 15
        assertEquals(15.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator result = constFive.minus(constMinusThree);
        // 5 - (-3) = 8
        assertEquals(8.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator result = constFive.minus(2);
        // 5 - 2 = 3
        assertEquals(3.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedByWithIndicator() {
        NumericIndicator result = constFive.multipliedBy(constMinusThree);
        // 5 * (-3) = -15
        assertEquals(-15.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedByWithNumber() {
        NumericIndicator result = constFive.multipliedBy(4);
        // 5 * 4 = 20
        assertEquals(20.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedByWithIndicator() {
        NumericIndicator result = constFive.dividedBy(constMinusThree);
        // 5 / (-3) = -1.666...
        assertEquals(-1.6666667, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedByWithNumber() {
        NumericIndicator result = constFive.dividedBy(2);
        // 5 / 2 = 2.5
        assertEquals(2.5, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinWithIndicator() {
        NumericIndicator result = constFive.min(constMinusThree);
        // min(5, -3) = -3
        assertEquals(-3.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinWithNumber() {
        NumericIndicator result = constFive.min(-10);
        // min(5, -10) = -10
        assertEquals(-10.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator result = constFive.max(constMinusThree);
        // max(5, -3) = 5
        assertEquals(5.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMaxWithNumber() {
        NumericIndicator result = constMinusThree.max(7);
        // max(-3, 7) = 7
        assertEquals(7.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testAbs() {
        NumericIndicator result = constMinusThree.abs();
        // abs(-3) = 3
        assertEquals(3.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSqrt() {
        NumericIndicator value = constFive; // sqrt(5) ≈ 2.2360679
        NumericIndicator result = value.sqrt();
        assertEquals(Math.sqrt(5.0), result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSquared() {
        NumericIndicator result = constFive.squared(); // 5 * 5 = 25
        assertEquals(25.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPreviousDefault() {
        // previous() should delegate to previous(1)
        Indicator<Num> previous = constFive.previous();
        assertTrue(previous instanceof PreviousValueIndicator);
        assertEquals(5.0, previous.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSmaIndicatorCreation() {
        NumericIndicator sma = constFive.sma(3);
        assertTrue(sma.delegate() instanceof SMAIndicator);
    }

    @Test
    public void testEmaIndicatorCreation() {
        NumericIndicator ema = constFive.ema(5);
        assertTrue(ema.delegate() instanceof EMAIndicator);
    }

    @Test
    public void testStdDevIndicatorCreation() {
        NumericIndicator stddev = constFive.stddev(4);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
    }

    @Test
    public void testHighestIndicatorCreation() {
        NumericIndicator highest = constFive.highest(2);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);
    }

    @Test
    public void testLowestIndicatorCreation() {
        NumericIndicator lowest = constFive.lowest(2);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev = constFive.previous(2);
        assertTrue(prev.delegate() instanceof PreviousValueIndicator);
    }

    @Test
    public void testCrossedOverRule() {
        Indicator<Num> up = new ArrayIndicator(series, 1, 2, 3);
        Indicator<Num> down = new ArrayIndicator(series, 3, 2, 1);
        Rule rule = NumericIndicator.of(down).crossedOver(up);
        // crossing happens at index 1 (2 goes from below 2 to equal then above)
        assertTrue(rule.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderRule() {
        Indicator<Num> up = new ArrayIndicator(series, 1, 2, 3);
        Indicator<Num> down = new ArrayIndicator(series, 3, 2, 1);
        Rule rule = NumericIndicator.of(up).crossedUnder(down);
        // crossing under occurs at index 1
        assertTrue(rule.isSatisfied(1));
    }

    @Test
    public void testIsGreaterThanRule() {
        Rule rule = constFive.isGreaterThan(3);
        assertTrue(rule.isSatisfied(0));
        Rule falseRule = constFive.isGreaterThan(6);
        assertFalse(falseRule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanRule() {
        Rule rule = constMinusThree.isLessThan(0);
        assertTrue(rule.isSatisfied(0));
        Rule falseRule = constMinusThree.isLessThan(-5);
        assertFalse(falseRule.isSatisfied(0));
    }

    /* --------------------------------------------------------------------- *
     *  3. Edge‑case and exception handling tests
     * --------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testPlusWithNullIndicator() {
        constFive.plus((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testMinusWithNullIndicator() {
        constFive.minus((Indicator<Num>) null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZeroNumber() {
        constFive.dividedBy(0);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZeroIndicator() {
        Indicator<Num> zero = NumericIndicator.of(new ConstantIndicator<>(series, new SimpleNum(0.0)));
        constFive.dividedBy(zero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSqrtOfNegativeNumber() {
        constMinusThree.sqrt();
    }

    @Test
    public void testMinMaxWhenEqual() {
        NumericIndicator a = constFive;
        NumericIndicator b = constFive;
        // When equal, the method should return the left operand (a)
        NumericIndicator min = a.min(b);
        assertSame(a, min);
        NumericIndicator max = a.max(b);
        assertSame(a, max);
    }

    @Test
    public void testCrossedOverWithConstantNumber() {
        // Indicator that is constantly 2
        NumericIndicator constantTwo = NumericIndicator.of(new ConstantIndicator<>(series, new SimpleNum(2.0)));
        // Cross over 1 should be true from the first index onward because 2 > 1
        Rule rule = constantTwo.crossedOver(1);
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderWithConstantNumber() {
        // Constant 1 crossing under constant 2 -> never true because it never goes below
        NumericIndicator constantOne = NumericIndicator.of(new ConstantIndicator<>(series, new SimpleNum(1.0)));
        Rule rule = constantOne.crossedUnder(2);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testDelegateMethod() {
        Indicator<Num> delegate = constFive.delegate();
        assertTrue(delegate instanceof ConstantIndicator);
        assertEquals(5.0, delegate.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testToStringDelegates() {
        String s = constFive.toString();
        // ConstantIndicator's toString contains the constant value
        assertTrue(s.contains("5.0"));
    }

    @Test
    public void testGetUnstableBarsIsZero() {
        assertEquals(0, constFive.getUnstableBars());
    }

    @Test
    public void testGetBarSeriesReturnsOriginalSeries() {
        assertSame(series, constFive.getBarSeries());
    }
}
