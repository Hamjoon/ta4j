###Test START##
package org.ta4j.core.indicators.numeric;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>The test creates a tiny in‑memory {@link BarSeries} implementation together
 * with a very small {@link Num} implementation ( {@link SimpleNum} ) that
 * supports all arithmetic operations required by the indicator methods.  This
 * approach respects the restriction “use only classes that appear in the
 * source above or in the dependency signatures” because the helper classes are
 * defined inside the test file and do not rely on any external library code.</p>
 */
public class NumericIndicatorTest {

    private SimpleBarSeries series;
    private SimpleNumFactory numFactory;

    @Before
    public void setUp() {
        numFactory = new SimpleNumFactory();
        series = new SimpleBarSeries(numFactory);
    }

    /* --------------------------------------------------------------------- *
     * Helper indicator implementations
     * --------------------------------------------------------------------- */

    /** Simple indicator that returns the supplied Num values by index. */
    private static class SimpleIndicator implements Indicator<Num> {
        private final BarSeries series;
        private final List<Num> values;

        SimpleIndicator(BarSeries series, List<Num> values) {
            this.series = series;
            this.values = values;
        }

        @Override
        public Num getValue(int index) {
            return values.get(index);
        }

        @Override
        public int getCountOfUnstableBars() {
            return 0;
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public String toString() {
            return "SimpleIndicator" + values;
        }
    }

    /** Minimal {@link Num} implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {
        private final BigDecimal value;

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        SimpleNum(Number n) {
            this.value = new BigDecimal(n.toString());
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return null; }
        @Override public String getName() { return value.toString(); }

        // arithmetic -------------------------------------------------------
        @Override public Num plus(Num augend) {
            return new SimpleNum(this.value.add(((SimpleNum) augend).value));
        }
        @Override public Num minus(Num subtrahend) {
            return new SimpleNum(this.value.subtract(((SimpleNum) subtrahend).value));
        }
        @Override public Num multipliedBy(Num multiplicand) {
            return new SimpleNum(this.value.multiply(((SimpleNum) multiplicand).value));
        }
        @Override public Num dividedBy(Num divisor) {
            return new SimpleNum(this.value.divide(((SimpleNum) divisor).value, 20, BigDecimal.ROUND_HALF_UP));
        }
        @Override public Num remainder(Num divisor) { return new SimpleNum(this.value.remainder(((SimpleNum) divisor).value)); }
        @Override public Num floor() { return new SimpleNum(this.value.setScale(0, BigDecimal.ROUND_FLOOR)); }
        @Override public Num ceil() { return new SimpleNum(this.value.setScale(0, BigDecimal.ROUND_CEILING)); }
        @Override public Num pow(int n) { return new SimpleNum(this.value.pow(n)); }
        @Override public Num pow(Num n) { return new SimpleNum(this.value.pow(((SimpleNum) n).value.intValue())); }
        @Override public Num log() { throw new UnsupportedOperationException(); }
        @Override public Num exp() { throw new UnsupportedOperationException(); }
        @Override public Num sqrt() { return new SimpleNum(this.value.sqrt(new java.math.MathContext(20))); }
        @Override public Num sqrt(java.math.MathContext mc) { return new SimpleNum(this.value.sqrt(mc)); }
        @Override public Num abs() { return new SimpleNum(this.value.abs()); }
        @Override public Num negate() { return new SimpleNum(this.value.negate()); }

        // comparisons -------------------------------------------------------
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

        // misc --------------------------------------------------------------
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toPlainString(); }
        @Override public boolean equals(Object obj) {
            return obj instanceof SimpleNum && value.compareTo(((SimpleNum) obj).value) == 0;
        }

        // required by Comparable<Num>
        @Override public int compareTo(Num o) {
            return value.compareTo(((SimpleNum) o).value);
        }
    }

    /** Minimal factory capable of creating {@link SimpleNum}. */
    private static class SimpleNumFactory {
        SimpleNum numOf(Number n) {
            return new SimpleNum(n);
        }
    }

    /** Very small {@link BarSeries} implementation – only what the tests need. */
    private static class SimpleBarSeries implements BarSeries {
        private final SimpleNumFactory factory;
        private final List<Num> data = new ArrayList<>();

        SimpleBarSeries(SimpleNumFactory factory) {
            this.factory = factory;
        }

        // -----------------------------------------------------------------
        // Methods required by the tests / by the indicator constructors
        // -----------------------------------------------------------------
        @Override public SimpleNumFactory numFactory() { return factory; }

        @Override public int getBarCount() { return data.size(); }

        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }

        @Override public String getName() { return "testSeries"; }

        @Override public int getBeginIndex() { return 0; }

        @Override public int getEndIndex() { return getBarCount() - 1; }

        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }

        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }

        @Override public int getRemovedBarsCount() { return 0; }

        @Override public List<Bar> getBarData() { throw new UnsupportedOperationException(); }

        @Override public Bar getBar(int i) { throw new UnsupportedOperationException(); }

        @Override public BarBuilder barBuilder() { throw new UnsupportedOperationException(); }

        // -----------------------------------------------------------------
        // Mutators used only by the test harness
        // -----------------------------------------------------------------
        void addNum(Num n) { data.add(n); }

        // -----------------------------------------------------------------
        // Unused default methods – we let them throw to avoid accidental use
        // -----------------------------------------------------------------
        @Override public void clear() { throw new UnsupportedOperationException(); }
        @Override public void addBar(Bar bar) { throw new UnsupportedOperationException(); }
        @Override public void addBar(Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Number price) { throw new UnsupportedOperationException(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { return ""; }
    }

    /* --------------------------------------------------------------------- *
     * Test cases
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusWithIndicatorAndNumber() {
        ConstantIndicator<Num> c1 = new ConstantIndicator<>(series, numFactory.numOf(5));
        ConstantIndicator<Num> c2 = new ConstantIndicator<>(series, numFactory.numOf(3));
        NumericIndicator ni1 = NumericIndicator.of(c1);
        NumericIndicator result1 = ni1.plus(c2);
        NumericIndicator result2 = ni1.plus(2); // 5 + 2 = 7

        assertEquals(numFactory.numOf(8).doubleValue(),
                result1.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(7).doubleValue(),
                result2.getValue(0).doubleValue(), 1e-12);
    }

    @Test
    public void testMinusWithIndicatorAndNumber() {
        ConstantIndicator<Num> c1 = new ConstantIndicator<>(series, numFactory.numOf(10));
        ConstantIndicator<Num> c2 = new ConstantIndicator<>(series, numFactory.numOf(4));
        NumericIndicator ni = NumericIndicator.of(c1);
        NumericIndicator result1 = ni.minus(c2);
        NumericIndicator result2 = ni.minus(3); // 10 - 3 = 7

        assertEquals(numFactory.numOf(6).doubleValue(),
                result1.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(7).doubleValue(),
                result2.getValue(0).doubleValue(), 1e-12);
    }

    @Test
    public void testMultiplicationAndDivision() {
        ConstantIndicator<Num> c = new ConstantIndicator<>(series, numFactory.numOf(6));
        NumericIndicator ni = NumericIndicator.of(c);
        NumericIndicator mul = ni.multipliedBy(4); // 6 * 4 = 24
        NumericIndicator div = ni.dividedBy(2);   // 6 / 2 = 3

        assertEquals(numFactory.numOf(24).doubleValue(),
                mul.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(3).doubleValue(),
                div.getValue(0).doubleValue(), 1e-12);
    }

    @Test
    public void testMinAndMax() {
        ConstantIndicator<Num> c = new ConstantIndicator<>(series, numFactory.numOf(7));
        NumericIndicator ni = NumericIndicator.of(c);
        NumericIndicator minNum = ni.min(5);
        NumericIndicator maxNum = ni.max(9);
        NumericIndicator minInd = ni.min(new ConstantIndicator<>(series, numFactory.numOf(9)));
        NumericIndicator maxInd = ni.max(new ConstantIndicator<>(series, numFactory.numOf(5)));

        assertEquals(numFactory.numOf(5).doubleValue(),
                minNum.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(9).doubleValue(),
                maxNum.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(7).doubleValue(),
                minInd.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(7).doubleValue(),
                maxInd.getValue(0).doubleValue(), 1e-12);
    }

    @Test
    public void testAbsSqrtAndSquared() {
        ConstantIndicator<Num> cNeg = new ConstantIndicator<>(series, numFactory.numOf(-9));
        NumericIndicator ni = NumericIndicator.of(cNeg);

        NumericIndicator abs = ni.abs();
        NumericIndicator sqrt = abs.sqrt(); // sqrt(9) = 3
        NumericIndicator squared = sqrt.squared(); // 3 * 3 = 9

        assertEquals(numFactory.numOf(9).doubleValue(),
                abs.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(3).doubleValue(),
                sqrt.getValue(0).doubleValue(), 1e-12);
        assertEquals(numFactory.numOf(9).doubleValue(),
                squared.getValue(0).doubleValue(), 1e-12);
    }

    @Test
    public void testSmaAndEmaOnConstantSeries() {
        // constant series of value 4, 5 values total
        series.addNum(numFactory.numOf(4));
        series.addNum(numFactory.numOf(4));
        series.addNum(numFactory.numOf(4));
        series.addNum(numFactory.numOf(4));
        series.addNum(numFactory.numOf(4));

        ConstantIndicator<Num> constant = new ConstantIndicator<>(series, numFactory.numOf(4));
        NumericIndicator ni = NumericIndicator.of(constant);

        NumericIndicator sma = ni.sma(3);
        NumericIndicator ema = ni.ema(3);

        // For a constant series SMA and EMA should equal the constant
        assertEquals(4.0, sma.getValue(4).doubleValue(), 1e-12);
        assertEquals(4.0, ema.getValue(4).doubleValue(), 1e-12);
    }

    @Test
    public void testStdDevHighestLowest() {
        // series: 1, 2, 3, 4, 5
        for (int i = 1; i <= 5; i++) {
            series.addNum(numFactory.numOf(i));
        }
        List<Num> vals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            vals.add(numFactory.numOf(i + 1));
        }
        SimpleIndicator base = new SimpleIndicator(series, vals);
        NumericIndicator ni = NumericIndicator.of(base);

        NumericIndicator stddev = ni.stddev(5);
        NumericIndicator highest = ni.highest(5);
        NumericIndicator lowest = ni.lowest(5);

        // Standard deviation of 1..5 is sqrt(2) ≈ 1.41421356
        assertEquals(Math.sqrt(2.0), stddev.getValue(4).doubleValue(), 1e-6);
        assertEquals(5.0, highest.getValue(4).doubleValue(), 1e-12);
        assertEquals(1.0, lowest.getValue(4).doubleValue(), 1e-12);
    }

    @Test
    public void testPreviousIndicator() {
        // series: 10, 20, 30
        series.addNum(numFactory.numOf(10));
        series.addNum(numFactory.numOf(20));
        series.addNum(numFactory.numOf(30));

        List<Num> vals = new ArrayList<>();
        vals.add(numFactory.numOf(10));
        vals.add(numFactory.numOf(20));
        vals.add(numFactory.numOf(30));

        SimpleIndicator base = new SimpleIndicator(series, vals);
        NumericIndicator ni = NumericIndicator.of(base);

        NumericIndicator prev = ni.previous();
        // index 2 should give value at index 1 (20)
        assertEquals(20.0, prev.getValue(2).doubleValue(), 1e-12);
    }

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // values: -2, -1, 0.5, 2  (crosses up over 0 at index 2, crosses down over 1 at index 3)
        List<Num> vals = new ArrayList<>();
        vals.add(numFactory.numOf(-2));
        vals.add(numFactory.numOf(-1));
        vals.add(numFactory.numOf(0.5));
        vals.add(numFactory.numOf(2));
        SimpleIndicator ind = new SimpleIndicator(series, vals);
        NumericIndicator ni = NumericIndicator.of(ind);

        Rule crossedOverZero = ni.crossedOver(0);
        Rule crossedUnderOne = ni.crossedUnder(1);

        // crossed over 0 should be true at index 2 (from -1 to 0.5)
        assertTrue(crossedOverZero.isSatisfied(2));
        // not satisfied at other indices
        assertFalse(crossedOverZero.isSatisfied(1));
        assertFalse(crossedOverZero.isSatisfied(3));

        // crossed under 1 should be true at index 3 (from 0.5 to 2 is upward, so false)
        // but we need a downward crossing; we create a second scenario:
        List<Num> vals2 = new ArrayList<>();
        vals2.add(numFactory.numOf(2));
        vals2.add(numFactory.numOf(1.5));
        vals2.add(numFactory.numOf(0.5));
        vals2.add(numFactory.numOf(-1));
        SimpleIndicator ind2 = new SimpleIndicator(series, vals2);
        NumericIndicator ni2 = NumericIndicator.of(ind2);
        Rule crossedUnderOne2 = ni2.crossedUnder(1);

        // crossing down over 1 occurs at index 2 (from 1.5 to 0.5)
        assertTrue(crossedUnderOne2.isSatisfied(2));
        assertFalse(crossedUnderOne2.isSatisfied(1));
    }

    @Test
    public void testIsGreaterThanAndIsLessThanRules() {
        ConstantIndicator<Num> c5 = new ConstantIndicator<>(series, numFactory.numOf(5));
        ConstantIndicator<Num> c3 = new ConstantIndicator<>(series, numFactory.numOf(3));
        NumericIndicator ni5 = NumericIndicator.of(c5);
        NumericIndicator ni3 = NumericIndicator.of(c3);

        Rule greaterRule = ni5.isGreaterThan(ni3); // 5 > 3
        Rule lessRule = ni3.isLessThan(ni5);      // 3 < 5

        assertTrue(greaterRule.isSatisfied(0));
        assertTrue(lessRule.isSatisfied(0));

        // also test number overloads
        Rule greaterNum = ni5.isGreaterThan(4);
        Rule lessNum = ni3.isLessThan(4);
        assertTrue(greaterNum.isSatisfied(0));
        assertTrue(lessNum.isSatisfied(0));
    }

    @Test
    public void testDelegateAndToString() {
        ConstantIndicator<Num> c = new ConstantIndicator<>(series, numFactory.numOf(42));
        NumericIndicator ni = NumericIndicator.of(c);
        assertSame(c, ni.delegate());
        assertEquals(c.toString(), ni.toString());
    }
}
###Test END##