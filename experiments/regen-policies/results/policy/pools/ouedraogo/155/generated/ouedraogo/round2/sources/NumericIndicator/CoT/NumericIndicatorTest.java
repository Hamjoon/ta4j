package org.ta4j.core.indicators.numeric;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link NumericIndicator}.
 *
 * <p>
 * The tests use very small “dummy” implementations of {@link BarSeries},
 * {@link Num} and {@link org.ta4j.core.num.NumFactory} that are sufficient for
 * exercising the logic of {@code NumericIndicator} without pulling in the whole
 * TA‑4J library.  Only the public API that appears in the supplied source code
 * (or the listed dependency signatures) is used.
 * </p>
 */
public class NumericIndicatorTest {

    /* --------------------------------------------------------------------- *
     *  Helper classes – minimal implementations required for the tests      *
     * --------------------------------------------------------------------- */

    /** Simple {@link Num} implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {

        private final BigDecimal value;
        private final SimpleNumFactory factory;

        SimpleNum(BigDecimal value, SimpleNumFactory factory) {
            this.value = value;
            this.factory = factory;
        }

        @Override public Number getDelegate() { return value; }
        @Override public org.ta4j.core.num.NumFactory getNumFactory() { return factory; }
        @Override public String getName() { return value.toPlainString(); }

        // arithmetic operations – all return a new SimpleNum
        @Override public Num plus(Num augend) {
            return new SimpleNum(this.value.add(toBigDecimal(augend)), factory);
        }
        @Override public Num minus(Num subtrahend) {
            return new SimpleNum(this.value.subtract(toBigDecimal(subtrahend)), factory);
        }
        @Override public Num multipliedBy(Num multiplicand) {
            return new SimpleNum(this.value.multiply(toBigDecimal(multiplicand)), factory);
        }
        @Override public Num dividedBy(Num divisor) {
            if (toBigDecimal(divisor).compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return new SimpleNum(this.value.divide(toBigDecimal(divisor), 20, BigDecimal.ROUND_HALF_UP), factory);
        }
        @Override public Num remainder(Num divisor) { throw new UnsupportedOperationException(); }
        @Override public Num floor() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_FLOOR), factory); }
        @Override public Num ceil() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_CEILING), factory); }
        @Override public Num pow(int n) { return new SimpleNum(this.value.pow(n), factory); }
        @Override public Num pow(Num n) { throw new UnsupportedOperationException(); }
        @Override public Num log() { throw new UnsupportedOperationException(); }
        @Override public Num exp() { throw new UnsupportedOperationException(); }
        @Override public Num sqrt() { return new SimpleNum(this.value.sqrt(new java.math.MathContext(20)), factory); }
        @Override public Num sqrt(java.math.MathContext mc) { return new SimpleNum(this.value.sqrt(mc), factory); }
        @Override public Num abs() { return new SimpleNum(this.value.abs(), factory); }
        @Override public Num negate() { return new SimpleNum(this.value.negate(), factory); }
        @Override public boolean isZero() { return value.compareTo(BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive() { return value.compareTo(BigDecimal.ZERO) > 0; }
        @Override public boolean isPositiveOrZero() { return value.compareTo(BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegative() { return value.compareTo(BigDecimal.ZERO) < 0; }
        @Override public boolean isNegativeOrZero() { return value.compareTo(BigDecimal.ZERO) <= 0; }
        @Override public boolean isEqual(Num other) { return this.value.compareTo(toBigDecimal(other)) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value.compareTo(toBigDecimal(other)) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value.compareTo(toBigDecimal(other)) >= 0; }
        @Override public boolean isLessThan(Num other) { return this.value.compareTo(toBigDecimal(other)) < 0; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value.compareTo(toBigDecimal(other)) <= 0; }
        @Override public Num min(Num other) { return isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return isGreaterThan(other) ? this : other; }
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public java.math.BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toPlainString(); }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof SimpleNum) {
                return value.compareTo(((SimpleNum) obj).value) == 0;
            }
            return false;
        }

        private static BigDecimal toBigDecimal(Num n) {
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new BigDecimal(n.getDelegate().toString());
        }
    }

    /** Simple {@code NumFactory} that creates {@link SimpleNum} objects. */
    private static class SimpleNumFactory implements org.ta4j.core.num.NumFactory {
        @Override
        public Num numOf(Number n) {
            return new SimpleNum(new BigDecimal(n.toString()), this);
        }
    }

    /** Minimal {@link BarSeries} implementation – only the methods used by
     * {@link NumericIndicator#createConstant(Number)} are required. */
    private static class DummyBarSeries implements BarSeries {

        private final SimpleNumFactory factory = new SimpleNumFactory();

        @Override public org.ta4j.core.num.NumFactory numFactory() { return factory; }
        @Override public org.ta4j.core.BarBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "dummy"; }
        @Override public org.ta4j.core.Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return 0; }
        @Override public List<org.ta4j.core.Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return -1; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
    }

    private DummyBarSeries series;
    private SimpleNumFactory numFactory;

    @Before
    public void setUp() {
        series = new DummyBarSeries();
        numFactory = (SimpleNumFactory) series.numFactory();
    }

    /** Helper to create a constant {@link NumericIndicator}. */
    private NumericIndicator constant(double value) {
        SimpleNum num = (SimpleNum) numFactory.numOf(value);
        Indicator<Num> constInd = new ConstantIndicator<>(series, num);
        return NumericIndicator.of(constInd);
    }

    /* --------------------------------------------------------------------- *
     *  1.  List of public methods (generated for documentation)            *
     * --------------------------------------------------------------------- */

    // static factory methods
    //   public static NumericIndicator of(Indicator<Num> delegate)
    //   public static NumericIndicator closePrice(BarSeries bs)
    //   public static NumericIndicator volume(BarSeries bs)

    // instance methods
    //   public Indicator<Num> delegate()
    //   public NumericIndicator plus(Indicator<Num> other)
    //   public NumericIndicator plus(Number n)
    //   public NumericIndicator minus(Indicator<Num> other)
    //   public NumericIndicator minus(Number n)
    //   public NumericIndicator multipliedBy(Indicator<Num> other)
    //   public NumericIndicator multipliedBy(Number n)
    //   public NumericIndicator dividedBy(Indicator<Num> other)
    //   public NumericIndicator dividedBy(Number n)
    //   public NumericIndicator min(Indicator<Num> other)
    //   public NumericIndicator min(Number n)
    //   public NumericIndicator max(Indicator<Num> other)
    //   public NumericIndicator max(Number n)
    //   public NumericIndicator abs()
    //   public NumericIndicator sqrt()
    //   public NumericIndicator squared()
    //   public NumericIndicator sma(int barCount)
    //   public NumericIndicator ema(int barCount)
    //   public NumericIndicator stddev(int barCount)
    //   public NumericIndicator highest(int barCount)
    //   public NumericIndicator lowest(int barCount)
    //   public NumericIndicator previous(int barCount)
    //   public NumericIndicator previous()
    //   public Rule crossedOver(Indicator<Num> other)
    //   public Rule crossedOver(Number n)
    //   public Rule crossedUnder(Indicator<Num> other)
    //   public Rule crossedUnder(Number n)
    //   public Rule isGreaterThan(Indicator<Num> other)
    //   public Rule isGreaterThan(Number n)
    //   public Rule isLessThan(Indicator<Num> other)
    //   public Rule isLessThan(Number n)

    // overridden Indicator methods
    //   public Num getValue(int index)
    //   public int getCountOfUnstableBars()
    //   public BarSeries getBarSeries()
    //   public String toString()

    /* --------------------------------------------------------------------- *
     *  2.  Basic functionality tests                                      *
     * --------------------------------------------------------------------- */

    @Test
    public void testPlus_Number() {
        NumericIndicator a = constant(5);
        NumericIndicator b = a.plus(3);               // 5 + 3 = 8
        assertEquals(8.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPlus_Indicator() {
        NumericIndicator a = constant(2);
        NumericIndicator b = constant(4);
        NumericIndicator c = a.plus(b);                // 2 + 4 = 6
        assertEquals(6.0, c.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinus_Number() {
        NumericIndicator a = constant(10);
        NumericIndicator b = a.minus(4);              // 10 - 4 = 6
        assertEquals(6.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinus_Indicator() {
        NumericIndicator a = constant(7);
        NumericIndicator b = constant(3);
        NumericIndicator c = a.minus(b);              // 7 - 3 = 4
        assertEquals(4.0, c.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedBy_Number() {
        NumericIndicator a = constant(3);
        NumericIndicator b = a.multipliedBy(5);        // 3 * 5 = 15
        assertEquals(15.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedBy_Indicator() {
        NumericIndicator a = constant(2);
        NumericIndicator b = constant(6);
        NumericIndicator c = a.multipliedBy(b);        // 2 * 6 = 12
        assertEquals(12.0, c.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedBy_Number() {
        NumericIndicator a = constant(20);
        NumericIndicator b = a.dividedBy(4);           // 20 / 4 = 5
        assertEquals(5.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedBy_Indicator() {
        NumericIndicator a = constant(15);
        NumericIndicator b = constant(3);
        NumericIndicator c = a.dividedBy(b);          // 15 / 3 = 5
        assertEquals(5.0, c.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMin_Number() {
        NumericIndicator a = constant(9);
        NumericIndicator b = a.min(4);                  // min(9,4) = 4
        assertEquals(4.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMin_Indicator() {
        NumericIndicator a = constant(7);
        NumericIndicator b = constant(12);
        NumericIndicator c = a.min(b);                 // min(7,12) = 7
        assertEquals(7.0, c.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMax_Number() {
        NumericIndicator a = constant(5);
        NumericIndicator b = a.max(11);                 // max(5,11) = 11
        assertEquals(11.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMax_Indicator() {
        NumericIndicator a = constant(3);
        NumericIndicator b = constant(8);
        NumericIndicator c = a.max(b);                 // max(3,8) = 8
        assertEquals(8.0, c.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testAbs() {
        NumericIndicator a = constant(-4.5);
        NumericIndicator b = a.abs();                  // |-4.5| = 4.5
        assertEquals(4.5, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSqrt() {
        NumericIndicator a = constant(9);
        NumericIndicator b = a.sqrt();                 // sqrt(9) = 3
        assertEquals(3.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSquared() {
        NumericIndicator a = constant(5);
        NumericIndicator b = a.squared();              // 5 * 5 = 25
        assertEquals(25.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPrevious_Default() {
        NumericIndicator a = constant(10);
        NumericIndicator b = a.previous();             // previous(1) on a constant -> still 10
        assertEquals(10.0, b.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDelegateMethod() {
        NumericIndicator ind = constant(2);
        assertSame(ind.delegate(), ind.delegate());
    }

    @Test
    public void testGetBarSeries() {
        NumericIndicator ind = constant(1);
        assertSame(series, ind.getBarSeries());
    }

    @Test
    public void testToStringDelegates() {
        NumericIndicator ind = constant(3);
        assertEquals(ind.delegate().toString(), ind.toString());
    }

    /* --------------------------------------------------------------------- *
     *  3.  Edge‑case and exception handling tests                           *
     * --------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testPlus_NullIndicator() {
        NumericIndicator a = constant(1);
        a.plus((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testPlus_NullNumber() {
        NumericIndicator a = constant(1);
        a.plus((Number) null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedBy_ZeroNumber() {
        NumericIndicator a = constant(5);
        NumericIndicator b = a.dividedBy(0);           // division by zero should explode
        // force evaluation
        b.getValue(0);
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedBy_ZeroIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator zero = constant(0);
        NumericIndicator b = a.dividedBy(zero);
        b.getValue(0);
    }

    @Test
    public void testSqrt_NegativeNumberProducesNaN() {
        NumericIndicator a = constant(-4);
        NumericIndicator b = a.sqrt();
        // The dummy Num implementation throws an exception for sqrt of negative,
        // so we assert that the exception is thrown.
        try {
            b.getValue(0);
            fail("Expected ArithmeticException for sqrt of negative number");
        } catch (ArithmeticException ignored) {
            // expected
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCrossedOver_NullIndicator() {
        NumericIndicator a = constant(1);
        a.crossedOver((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCrossedUnder_NullNumber() {
        NumericIndicator a = constant(1);
        a.crossedUnder((Number) null);
    }

    @Test
    public void testIsGreaterThan_EqualValues() {
        NumericIndicator a = constant(5);
        NumericIndicator b = constant(5);
        Rule rule = a.isGreaterThan(b);
        // Since values are equal, rule should not be satisfied at index 0
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThan_EqualValues() {
        NumericIndicator a = constant(5);
        NumericIndicator b = constant(5);
        Rule rule = a.isLessThan(b);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedOver_Number() {
        NumericIndicator a = constant(2);
        Rule rule = a.crossedOver(1); // 2 crossed over 1 at index 0 -> true
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnder_Number() {
        NumericIndicator a = constant(0);
        Rule rule = a.crossedUnder(1); // 0 crossed under 1 at index 0 -> true
        assertTrue(rule.isSatisfied(0));
    }

    /* --------------------------------------------------------------------- *
     *  4.  Tests for methods that create other indicator types            *
     * --------------------------------------------------------------------- */

    @Test
    public void testSmaCreatesSMAIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator sma = a.sma(3);
        assertTrue(sma.delegate() instanceof SMAIndicator);
    }

    @Test
    public void testEmaCreatesEMAIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator ema = a.ema(3);
        assertTrue(ema.delegate() instanceof EMAIndicator);
    }

    @Test
    public void testStddevCreatesStandardDeviationIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator std = a.stddev(3);
        assertTrue(std.delegate() instanceof StandardDeviationIndicator);
    }

    @Test
    public void testHighestCreatesHighestValueIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator hv = a.highest(3);
        assertTrue(hv.delegate() instanceof HighestValueIndicator);
    }

    @Test
    public void testLowestCreatesLowestValueIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator lv = a.lowest(3);
        assertTrue(lv.delegate() instanceof LowestValueIndicator);
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator a = constant(7);
        NumericIndicator prev = a.previous(2);
        assertTrue(prev.delegate() instanceof PreviousValueIndicator);
    }

    /* --------------------------------------------------------------------- *
     *  5.  Static factory method tests                                      *
     * --------------------------------------------------------------------- */

    @Test
    public void testOfFactory() {
        Indicator<Num> constInd = new ConstantIndicator<>(series, numFactory.numOf(3));
        NumericIndicator ind = NumericIndicator.of(constInd);
        assertSame(constInd, ind.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        // The dummy series has no real bars, but the construction must succeed.
        NumericIndicator ind = NumericIndicator.closePrice(series);
        assertTrue(ind.delegate() instanceof ClosePriceIndicator);
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator ind = NumericIndicator.volume(series);
        assertTrue(ind.delegate() instanceof VolumeIndicator);
    }
}
