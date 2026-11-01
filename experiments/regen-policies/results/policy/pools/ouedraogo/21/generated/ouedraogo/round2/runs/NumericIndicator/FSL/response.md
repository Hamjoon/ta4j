###Test START##
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Tests for {@link NumericIndicator}. The tests use simple in‑memory
 * implementations of {@link BarSeries}, {@link NumFactory} and {@link Num}
 * to avoid any external dependencies.
 */
public class NumericIndicatorTest {

    /** Simple {@link Num} implementation backed by a {@code double}. */
    private static final class SimpleNum implements Num {

        private final double value;
        private final SimpleNumFactory factory;

        SimpleNum(SimpleNumFactory factory, double value) {
            this.factory = factory;
            this.value = value;
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return factory; }
        @Override public String getName() { return Double.toString(value); }

        private SimpleNum newNum(double v) { return new SimpleNum(factory, v); }

        @Override public Num plus(Num augend) { return newNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend) { return newNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand) { return newNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor) { return newNum(this.value / ((SimpleNum) divisor).value); }

        @Override public Num remainder(Num divisor) { throw new UnsupportedOperationException(); }
        @Override public Num floor() { return newNum(Math.floor(value)); }
        @Override public Num ceil() { return newNum(Math.ceil(value)); }
        @Override public Num pow(int n) { return newNum(Math.pow(value, n)); }
        @Override public Num pow(Num n) { return newNum(Math.pow(value, ((SimpleNum) n).value)); }
        @Override public Num log() { return newNum(Math.log(value)); }
        @Override public Num sqrt() { return newNum(Math.sqrt(value)); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs() { return newNum(Math.abs(value)); }
        @Override public Num negate() { return newNum(-value); }

        @Override public boolean isZero() { return value == 0.0; }
        @Override public boolean isPositive() { return value > 0.0; }
        @Override public boolean isPositiveOrZero() { return value >= 0.0; }
        @Override public boolean isNegative() { return value < 0.0; }
        @Override public boolean isNegativeOrZero() { return value <= 0.0; }
        @Override public boolean isEqual(Num other) { return Double.compare(value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other) { return value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return value <= ((SimpleNum) other).value; }
        @Override public Num min(Num other) { return isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return isGreaterThan(other) ? this : other; }

        @Override public boolean isNaN() { return Double.isNaN(value); }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
        @Override public double doubleValue() { return value; }
        @Override public java.math.BigDecimal bigDecimalValue() { return java.math.BigDecimal.valueOf(value); }

        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum) o).value); }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }
    }

    /** Simple {@link NumFactory} that creates {@link SimpleNum}s. */
    private static final class SimpleNumFactory implements NumFactory {
        @Override public Num numOf(Number n) {
            return new SimpleNum(this, n.doubleValue());
        }
    }

    /** Minimal {@link BarSeries} implementation sufficient for the tests. */
    private static final class SimpleBarSeries implements BarSeries {

        private final SimpleNumFactory factory = new SimpleNumFactory();
        private final List<org.ta4j.core.Bar> bars = new ArrayList<>();

        @Override public NumFactory numFactory() { return factory; }
        @Override public org.ta4j.core.BaseBarConvertibleBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "SimpleSeries"; }
        @Override public org.ta4j.core.Bar getBar(int i) { return bars.get(i); }
        @Override public int getBarCount() { return bars.size(); }
        @Override public java.util.List<org.ta4j.core.Bar> getBarData() { return bars; }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(org.ta4j.core.Bar bar) { bars.add(bar); }
        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Number price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public boolean isEmpty() { return bars.isEmpty(); }
        // default methods from the interface are inherited automatically
    }

    /** Helper to create a constant indicator with a given double value. */
    private static NumericIndicator constant(double v) {
        SimpleBarSeries series = new SimpleBarSeries();
        ConstantIndicator<Num> ci = new ConstantIndicator<>(series, series.numFactory().numOf(v));
        return NumericIndicator.of(ci);
    }

    /** Helper to create a variable indicator based on an array of double values. */
    private static NumericIndicator variable(double[] values) {
        SimpleBarSeries series = new SimpleBarSeries();
        Indicator<Num> ind = new Indicator<Num>() {
            @Override public Num getValue(int index) {
                return series.numFactory().numOf(values[index]);
            }
            @Override public int getUnstableBars() { return 0; }
            @Override public BarSeries getBarSeries() { return series; }
            @Override public String toString() { return "ArrayIndicator"; }
        };
        return NumericIndicator.of(ind);
    }

    // -------------------------------------------------------------------------
    // Arithmetic operations
    // -------------------------------------------------------------------------

    @Test
    public void testPlusWithIndicator() {
        NumericIndicator a = constant(5);
        NumericIndicator b = constant(3);
        NumericIndicator result = a.plus(b);
        assertEquals(8.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator a = constant(7);
        NumericIndicator result = a.plus(4);
        assertEquals(11.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator a = constant(10);
        NumericIndicator b = constant(4);
        NumericIndicator result = a.minus(b);
        assertEquals(6.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator a = constant(9);
        NumericIndicator result = a.minus(2);
        assertEquals(7.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedByWithIndicator() {
        NumericIndicator a = constant(6);
        NumericIndicator b = constant(7);
        NumericIndicator result = a.multipliedBy(b);
        assertEquals(42.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedByWithNumber() {
        NumericIndicator a = constant(3);
        NumericIndicator result = a.multipliedBy(5);
        assertEquals(15.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedByWithIndicator() {
        NumericIndicator a = constant(20);
        NumericIndicator b = constant(4);
        NumericIndicator result = a.dividedBy(b);
        assertEquals(5.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedByWithNumber() {
        NumericIndicator a = constant(15);
        NumericIndicator result = a.dividedBy(3);
        assertEquals(5.0, result.getValue(0).doubleValue(), 0.000001);
    }

    // -------------------------------------------------------------------------
    // Min / Max
    // -------------------------------------------------------------------------

    @Test
    public void testMinWithIndicator() {
        NumericIndicator a = constant(8);
        NumericIndicator b = constant(3);
        NumericIndicator result = a.min(b);
        assertEquals(3.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinWithNumber() {
        NumericIndicator a = constant(5);
        NumericIndicator result = a.min(9);
        assertEquals(5.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator a = constant(2);
        NumericIndicator b = constant(7);
        NumericIndicator result = a.max(b);
        assertEquals(7.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMaxWithNumber() {
        NumericIndicator a = constant(4);
        NumericIndicator result = a.max(1);
        assertEquals(4.0, result.getValue(0).doubleValue(), 0.000001);
    }

    // -------------------------------------------------------------------------
    // Unary operations
    // -------------------------------------------------------------------------

    @Test
    public void testAbs() {
        NumericIndicator a = constant(-12);
        NumericIndicator result = a.abs();
        assertEquals(12.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSqrt() {
        NumericIndicator a = constant(16);
        NumericIndicator result = a.sqrt();
        assertEquals(4.0, result.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSquared() {
        NumericIndicator a = constant(5);
        NumericIndicator result = a.squared();
        assertEquals(25.0, result.getValue(0).doubleValue(), 0.000001);
    }

    // -------------------------------------------------------------------------
    // Cached indicators (SMA, EMA, StdDev, Highest, Lowest, Previous)
    // -------------------------------------------------------------------------

    @Test
    public void testSma() {
        double[] values = {2, 4, 6};
        NumericIndicator ind = variable(values);
        NumericIndicator sma = ind.sma(3);
        // SMA of [2,4,6] = (2+4+6)/3 = 4
        assertEquals(4.0, sma.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testEma() {
        double[] values = {10, 20, 30, 40};
        NumericIndicator ind = variable(values);
        NumericIndicator ema = ind.ema(2);
        // EMA calculation is more involved; we just verify that the object is created
        // and that the first value equals the first input (standard EMA behaviour)
        assertEquals(10.0, ema.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testStdDev() {
        double[] values = {1, 1, 1};
        NumericIndicator ind = variable(values);
        NumericIndicator std = ind.stddev(3);
        // Standard deviation of identical values is 0
        assertEquals(0.0, std.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testHighest() {
        double[] values = {3, 7, 5};
        NumericIndicator ind = variable(values);
        NumericIndicator highest = ind.highest(2);
        // Highest of last two values (7,5) is 7
        assertEquals(7.0, highest.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testLowest() {
        double[] values = {8, 2, 6};
        NumericIndicator ind = variable(values);
        NumericIndicator lowest = ind.lowest(2);
        // Lowest of last two values (2,6) is 2
        assertEquals(2.0, lowest.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testPrevious() {
        double[] values = {10, 20, 30};
        NumericIndicator ind = variable(values);
        Indicator<Num> prev = ind.previous();
        // previous(1) at index 2 should give the value at index 1 (20)
        assertEquals(20.0, prev.getValue(2).doubleValue(), 0.000001);
    }

    // -------------------------------------------------------------------------
    // Rules
    // -------------------------------------------------------------------------

    @Test
    public void testCrossedOverRule() {
        // first: 1, 3, 2   second: 2, 2, 2
        double[] firstVals = {1, 3, 2};
        double[] secondVals = {2, 2, 2};

        NumericIndicator first = variable(firstVals);
        NumericIndicator second = variable(secondVals);

        Rule rule = first.crossedOver(second);
        // Crossing up occurs at index 1 (1 < 2 -> 3 > 2)
        assertTrue(rule.isSatisfied(1));
        // No crossing at index 2 (3 > 2 -> 2 < 2 is false)
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderRule() {
        // first: 5, 1, 4   second: 2, 2, 2
        double[] firstVals = {5, 1, 4};
        double[] secondVals = {2, 2, 2};

        NumericIndicator first = variable(firstVals);
        NumericIndicator second = variable(secondVals);

        Rule rule = first.crossedUnder(second);
        // Crossing down occurs at index 1 (5 > 2 -> 1 < 2)
        assertTrue(rule.isSatisfied(1));
        // No crossing at index 2 (1 < 2 -> 4 > 2)
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanNumberRule() {
        NumericIndicator ind = constant(9);
        Rule rule = ind.isGreaterThan(5);
        assertTrue(rule.isSatisfied(0));
        Rule ruleFalse = ind.isGreaterThan(10);
        assertFalse(ruleFalse.isSatisfied(0));
    }

    @Test
    public void testIsLessThanNumberRule() {
        NumericIndicator ind = constant(3);
        Rule rule = ind.isLessThan(5);
        assertTrue(rule.isSatisfied(0));
        Rule ruleFalse = ind.isLessThan(2);
        assertFalse(ruleFalse.isSatisfied(0));
    }

    // -------------------------------------------------------------------------
    // Delegate accessor
    // -------------------------------------------------------------------------

    @Test
    public void testDelegateAccessor() {
        NumericIndicator ind = constant(42);
        assertNotNull(ind.delegate());
        assertEquals(42.0, ind.delegate().getValue(0).doubleValue(), 0.000001);
    }
}
###Test END##