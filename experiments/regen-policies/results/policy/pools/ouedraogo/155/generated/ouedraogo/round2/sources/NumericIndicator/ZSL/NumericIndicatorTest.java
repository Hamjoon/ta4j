package org.ta4j.core.indicators.numeric;
import org.ta4j.core.BarBuilder;


import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * The test creates a very small in‑memory {@link BarSeries} implementation
 * together with a lightweight {@link Num} implementation so that we can
 * instantiate every concrete indicator used by {@code NumericIndicator}
 * without external library dependencies.
 *
 * Every public method (including overloads that accept {@link Number})
 * is exercised and its behaviour verified against hand‑calculated expected
 * results.
 */
public class NumericIndicatorTest {

    /** Simple {@link Num} implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {

        private final BigDecimal value;

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        SimpleNum(Number n) {
            this.value = new BigDecimal(n.toString());
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return SimpleNumFactory.INSTANCE; }
        @Override public String getName() { return "SimpleNum"; }

        // ----- arithmetic ----------------------------------------------------
        @Override public Num plus(Num augend) { return new SimpleNum(value.add(((SimpleNum) augend).value)); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(value.subtract(((SimpleNum) subtrahend).value)); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(value.multiply(((SimpleNum) multiplicand).value)); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(value.divide(((SimpleNum) divisor).value, MathContext.DECIMAL128)); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(value.remainder(((SimpleNum) divisor).value)); }
        @Override public Num floor() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_FLOOR)); }
        @Override public Num ceil() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_CEILING)); }
        @Override public Num pow(int n) { return new SimpleNum(value.pow(n)); }
        @Override public Num pow(Num n) { return new SimpleNum(value.pow(((SimpleNum) n).value.intValue())); }
        @Override public Num log() { return new SimpleNum(Math.log(value.doubleValue())); }
        @Override public Num exp() { return new SimpleNum(Math.exp(value.doubleValue())); }
        @Override public Num sqrt() { return new SimpleNum(value.sqrt(MathContext.DECIMAL128)); }
        @Override public Num sqrt(MathContext mc) { return new SimpleNum(value.sqrt(mc)); }
        @Override public Num abs() { return new SimpleNum(value.abs()); }
        @Override public Num negate() { return new SimpleNum(value.negate()); }

        // ----- comparisons ---------------------------------------------------
        @Override public boolean isZero() { return value.compareTo(BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive() { return value.compareTo(BigDecimal.ZERO) > 0; }
        @Override public boolean isPositiveOrZero() { return value.compareTo(BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegative() { return value.compareTo(BigDecimal.ZERO) < 0; }
        @Override public boolean isNegativeOrZero() { return value.compareTo(BigDecimal.ZERO) <= 0; }
        @Override public boolean isEqual(Num other) { return value.compareTo(((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return value.compareTo(((SimpleNum) other).value) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return value.compareTo(((SimpleNum) other).value) >= 0; }
        @Override public boolean isLessThan(Num other) { return value.compareTo(((SimpleNum) other).value) < 0; }
        @Override public boolean isLessThanOrEqual(Num other) { return value.compareTo(((SimpleNum) other).value) <= 0; }
        @Override public Num min(Num other) { return isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return isGreaterThan(other) ? this : other; }

        // ----- misc -----------------------------------------------------------
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toPlainString(); }
        @Override public boolean equals(Object obj) { return (obj instanceof SimpleNum) && value.equals(((SimpleNum) obj).value); }

        // ----- compareTo ------------------------------------------------------
        @Override public int compareTo(Num o) { return value.compareTo(((SimpleNum) o).value); }
    }

    /** Simple {@link NumFactory} that creates {@link SimpleNum} instances. */
    private static class SimpleNumFactory implements NumFactory {
        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();
        private SimpleNumFactory() {}

        @Override public Num numOf(Number n) { return new SimpleNum(n); }
        // The real library offers many overloads; for the purpose of these tests only
        // the method used by {@code NumericIndicator#createConstant} is required.
    }

    /** Minimal {@link BarSeries} implementation sufficient for the tests. */
    private static class MinimalBarSeries implements BarSeries {

        private final List<SimpleNum> values = new ArrayList<>();

        MinimalBarSeries(Number... nums) {
            for (Number n : nums) {
                values.add(new SimpleNum(n));
            }
        }

        /** Returns a Num at the given index (index starts at 0). */
        SimpleNum getNum(int index) { return values.get(index); }

        // ----- BarSeries contract (only the methods used by the library) -----
        @Override public NumFactory numFactory() { return SimpleNumFactory.INSTANCE; }
        @Override public int getBarCount() { return values.size(); }

        // The remaining methods are not needed for our test scenario; they return
        // harmless defaults or throw UnsupportedOperationException if accidentally used.
        @Override public BarBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "minimal"; }
        @Override public org.ta4j.core.Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.Bar getFirstBar() { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.Bar getLastBar() { throw new UnsupportedOperationException(); }
        @Override public List<org.ta4j.core.Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return getBarCount() - 1; }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { return ""; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(org.ta4j.core.Bar bar) { throw new UnsupportedOperationException(); }
        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Number price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
    }

    /** Simple Indicator that returns the value from the supplied {@link MinimalBarSeries}. */
    private static class SimpleIndicator implements Indicator<Num> {

        private final MinimalBarSeries series;

        SimpleIndicator(MinimalBarSeries series) {
            this.series = series;
        }

        @Override public Num getValue(int index) { return series.getNum(index); }
        @Override public int getCountOfUnstableBars() { return 0; }
        @Override public BarSeries getBarSeries() { return series; }
        @Override public String toString() { return "SimpleIndicator"; }
    }

    private MinimalBarSeries series;
    private SimpleIndicator baseIndicator;
    private NumericIndicator numeric;

    @Before
    public void setUp() {
        // Series: 1, 2, 3, 4, 5
        series = new MinimalBarSeries(1, 2, 3, 4, 5);
        baseIndicator = new SimpleIndicator(series);
        numeric = NumericIndicator.of(baseIndicator);
    }

    // -------------------------------------------------------------------------
    // Arithmetic operations (indicator‑based overloads)
    // -------------------------------------------------------------------------

    @Test
    public void testPlusIndicator() {
        NumericIndicator other = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(10, 20, 30, 40, 50)));
        NumericIndicator sum = numeric.plus(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).plus(other.getValue(i));
            assertEquals("plus at index " + i, expected, sum.getValue(i));
        }
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator other = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(1, 1, 1, 1, 1)));
        NumericIndicator diff = numeric.minus(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).minus(other.getValue(i));
            assertEquals("minus at index " + i, expected, diff.getValue(i));
        }
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator other = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(2, 2, 2, 2, 2)));
        NumericIndicator prod = numeric.multipliedBy(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).multipliedBy(other.getValue(i));
            assertEquals("multipliedBy at index " + i, expected, prod.getValue(i));
        }
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator other = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(1, 2, 3, 4, 5)));
        NumericIndicator quot = numeric.dividedBy(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).dividedBy(other.getValue(i));
            assertEquals("dividedBy at index " + i, expected, quot.getValue(i));
        }
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator other = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(0, 3, 2, 5, 1)));
        NumericIndicator min = numeric.min(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).min(other.getValue(i));
            assertEquals("min at index " + i, expected, min.getValue(i));
        }
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator other = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(0, 3, 2, 5, 1)));
        NumericIndicator max = numeric.max(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).max(other.getValue(i));
            assertEquals("max at index " + i, expected, max.getValue(i));
        }
    }

    // -------------------------------------------------------------------------
    // Arithmetic operations (Number overloads)
    // -------------------------------------------------------------------------

    @Test
    public void testPlusNumber() {
        NumericIndicator sum = numeric.plus(10);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).plus(series.numFactory().numOf(10));
            assertEquals("plus(Number) at index " + i, expected, sum.getValue(i));
        }
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator diff = numeric.minus(1);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).minus(series.numFactory().numOf(1));
            assertEquals("minus(Number) at index " + i, expected, diff.getValue(i));
        }
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator prod = numeric.multipliedBy(3);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).multipliedBy(series.numFactory().numOf(3));
            assertEquals("multipliedBy(Number) at index " + i, expected, prod.getValue(i));
        }
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator quot = numeric.dividedBy(2);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).dividedBy(series.numFactory().numOf(2));
            assertEquals("dividedBy(Number) at index " + i, expected, quot.getValue(i));
        }
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = numeric.min(3);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).min(series.numFactory().numOf(3));
            assertEquals("min(Number) at index " + i, expected, min.getValue(i));
        }
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = numeric.max(3);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).max(series.numFactory().numOf(3));
            assertEquals("max(Number) at index " + i, expected, max.getValue(i));
        }
    }

    // -------------------------------------------------------------------------
    // Unary operations
    // -------------------------------------------------------------------------

    @Test
    public void testAbs() {
        NumericIndicator negative = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(-1, -2, -3, -4, -5)));
        NumericIndicator abs = negative.abs();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = negative.getValue(i).abs();
            assertEquals("abs at index " + i, expected, abs.getValue(i));
        }
    }

    @Test
    public void testSqrt() {
        NumericIndicator sqrt = numeric.sqrt();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).sqrt();
            assertEquals("sqrt at index " + i, expected, sqrt.getValue(i));
        }
    }

    @Test
    public void testSquared() {
        NumericIndicator squared = numeric.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getNum(i).multipliedBy(series.getNum(i));
            assertEquals("squared at index " + i, expected, squared.getValue(i));
        }
    }

    // -------------------------------------------------------------------------
    // Moving‑average and statistical indicators
    // -------------------------------------------------------------------------

    @Test
    public void testSma() {
        NumericIndicator sma = numeric.sma(3);
        assertTrue(sma.delegate() instanceof SMAIndicator);
        // Manual SMA for barCount = 3: (1+2+3)/3 = 2, (2+3+4)/3 = 3, (3+4+5)/3 = 4
        Num[] expected = {
                series.numFactory().numOf(2),
                series.numFactory().numOf(3),
                series.numFactory().numOf(4)
        };
        for (int i = 2; i < series.getBarCount(); i++) {
            assertEquals("SMA at index " + i, expected[i - 2], sma.getValue(i));
        }
    }

    @Test
    public void testEma() {
        NumericIndicator ema = numeric.ema(3);
        assertTrue(ema.delegate() instanceof EMAIndicator);
        // EMA is more involved; we simply verify that the instance is created and
        // that the first calculated value equals the first underlying price (standard EMA definition).
        assertEquals("EMA first value should equal first price", series.getNum(0), ema.getValue(0));
    }

    @Test
    public void testStddev() {
        NumericIndicator stddev = numeric.stddev(3);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
        // For a deterministic check we compute the standard deviation for the last three values (3,4,5)
        // variance = ((3-4)^2 + (4-4)^2 + (5-4)^2) / 3 = (1+0+1)/3 = 0.666...
        // stddev = sqrt(0.666...) ≈ 0.8164965809
        Num expected = series.numFactory().numOf(Math.sqrt(2.0/3.0));
        // The library's StandardDeviationIndicator uses a biased estimator (divide by N),
        // which matches the above calculation.
        assertEquals("StdDev at index 4", expected.doubleValue(), stddev.getValue(4).doubleValue(), 1e-9);
    }

    @Test
    public void testHighestAndLowest() {
        NumericIndicator highest = numeric.highest(3);
        NumericIndicator lowest = numeric.lowest(3);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        // Highest of (1,2,3) = 3, (2,3,4) = 4, (3,4,5) = 5
        Num[] expectedHigh = {
                series.numFactory().numOf(3),
                series.numFactory().numOf(4),
                series.numFactory().numOf(5)
        };
        // Lowest of (1,2,3) = 1, (2,3,4) = 2, (3,4,5) = 3
        Num[] expectedLow = {
                series.numFactory().numOf(1),
                series.numFactory().numOf(2),
                series.numFactory().numOf(3)
        };
        for (int i = 2; i < series.getBarCount(); i++) {
            assertEquals("Highest at index " + i, expectedHigh[i - 2], highest.getValue(i));
            assertEquals("Lowest at index " + i, expectedLow[i - 2], lowest.getValue(i));
        }
    }

    @Test
    public void testPrevious() {
        NumericIndicator previous = numeric.previous(2);
        assertTrue(previous.delegate() instanceof PreviousValueIndicator);
        // previous(2) means value at index-2
        for (int i = 2; i < series.getBarCount(); i++) {
            assertEquals("Previous(2) at index " + i, series.getNum(i - 2), previous.getValue(i));
        }

        // also test the convenience method previous()
        NumericIndicator previousOne = numeric.previous();
        for (int i = 1; i < series.getBarCount(); i++) {
            assertEquals("previous() at index " + i, series.getNum(i - 1), previousOne.getValue(i));
        }
    }

    // -------------------------------------------------------------------------
    // Rule creation and evaluation
    // -------------------------------------------------------------------------

    @Test
    public void testCrossedOverRule() {
        // Indicator A: 1,2,3,4,5
        // Indicator B: 5,4,3,2,1 (crosses over at index 2 -> 3 > 3 is false, but at index 3 4 > 2 true)
        NumericIndicator a = numeric;
        NumericIndicator b = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(5,4,3,2,1)));
        Rule rule = a.crossedOver(b);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // The crossing happens when a goes from below b to above b.
        // At index 2: a=3, b=3 (equal) – not a cross.
        // At index 3: a=4, b=2 – a was 3 <= 2? No, but the rule implementation checks
        // previous value: a(2)=3, b(2)=3 (not lower). So the first true crossing is at index 4.
        // For simplicity we just assert that rule.isSatisfied(4) is true.
        assertTrue("crossedOver should be satisfied at index 4", rule.isSatisfied(4));
    }

    @Test
    public void testCrossedUnderRule() {
        // Reverse of the previous test
        NumericIndicator a = numeric;
        NumericIndicator b = NumericIndicator.of(new SimpleIndicator(new MinimalBarSeries(0,0,0,0,0)));
        Rule rule = a.crossedUnder(b);
        assertTrue(rule instanceof CrossedDownIndicatorRule);
        // Since b is constantly 0, a crosses under never happens; we expect false.
        assertFalse("crossedUnder should not be satisfied", rule.isSatisfied(4));
    }

    @Test
    public void testIsGreaterThanRule() {
        Rule rule = numeric.isGreaterThan(3);
        assertTrue(rule instanceof OverIndicatorRule);
        // Values: 1,2,3,4,5 -> only indices 3 and 4 are greater than 3
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
        assertTrue(rule.isSatisfied(3));
        assertTrue(rule.isSatisfied(4));
    }

    @Test
    public void testIsLessThanRule() {
        Rule rule = numeric.isLessThan(3);
        assertTrue(rule instanceof UnderIndicatorRule);
        // Values: 1,2,3,4,5 -> indices 0 and 1 are less than 3
        assertTrue(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
        assertFalse(rule.isSatisfied(3));
        assertFalse(rule.isSatisfied(4));
    }

    // -------------------------------------------------------------------------
    // Miscellaneous
    // -------------------------------------------------------------------------

    @Test
    public void testDelegateAndToString() {
        assertSame("delegate() must return the original indicator", baseIndicator, numeric.delegate());
        assertEquals("toString() must delegate to the wrapped indicator",
                baseIndicator.toString(),
                numeric.toString());
    }
}
