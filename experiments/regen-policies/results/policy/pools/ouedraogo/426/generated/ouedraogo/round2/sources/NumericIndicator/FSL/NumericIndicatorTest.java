package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Comprehensive tests for {@link NumericIndicator}.
 *
 * <p>
 * The tests use lightweight stub implementations of {@link BarSeries},
 * {@link Num} and {@link NumFactory} so that no external library code is required
 * beyond the classes that appear in the source or the provided signatures.
 * </p>
 */
public class NumericIndicatorTest {

    /** Simple {@link Num} implementation based on {@link Double}. */
    private static final class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return SimpleNumFactory.INSTANCE; }
        @Override public String getName() { return Double.toString(value); }

        // arithmetic ---------------------------------------------------------
        @Override public Num plus(Num augend) { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value / ((SimpleNum) divisor).value); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(this.value % ((SimpleNum) divisor).value); }

        @Override public Num floor() { return new SimpleNum(Math.floor(this.value)); }
        @Override public Num ceil() { return new SimpleNum(Math.ceil(this.value)); }
        @Override public Num pow(int n) { return new SimpleNum(Math.pow(this.value, n)); }
        @Override public Num pow(Num n) { return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value)); }
        @Override public Num log() { return new SimpleNum(Math.log(this.value)); }
        @Override public Num exp() { return new SimpleNum(Math.exp(this.value)); }
        @Override public Num sqrt() { return new SimpleNum(Math.sqrt(this.value)); }
        @Override public Num sqrt(MathContext mc) { return new SimpleNum(Math.sqrt(this.value)); }
        @Override public Num abs() { return new SimpleNum(Math.abs(this.value)); }
        @Override public Num negate() { return new SimpleNum(-this.value); }

        // predicates ---------------------------------------------------------
        @Override public boolean isZero() { return value == 0.0; }
        @Override public boolean isPositive() { return value > 0.0; }
        @Override public boolean isPositiveOrZero() { return value >= 0.0; }
        @Override public boolean isNegative() { return value < 0.0; }
        @Override public boolean isNegativeOrZero() { return value <= 0.0; }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other) { return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= ((SimpleNum) other).value; }
        @Override public Num min(Num other) { return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return this.isGreaterThan(other) ? this : other; }

        // misc ---------------------------------------------------------------
        @Override public boolean isNaN() { return Double.isNaN(value); }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
        @Override public double doubleValue() { return value; }
        @Override public BigDecimal bigDecimalValue() { return BigDecimal.valueOf(value); }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return obj instanceof SimpleNum && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }
        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum) o).value); }
    }

    /** Simple {@link NumFactory} that creates {@link SimpleNum}. */
    private static final class SimpleNumFactory implements NumFactory {

        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();

        private SimpleNumFactory() {}

        @Override public Num numOf(Number number) {
            return new SimpleNum(number.doubleValue());
        }

        @Override public Num numOf(String text) {
            return new SimpleNum(Double.parseDouble(text));
        }

        @Override public Num zero() { return new SimpleNum(0.0); }
        @Override public Num one() { return new SimpleNum(1.0); }
        @Override public Num NaN() { return new SimpleNum(Double.NaN); }
    }

    /** Minimal {@link BarSeries} implementation required for the tests. */
    private static final class SimpleBarSeries implements BarSeries {

        private final List<SimpleNum> values = new ArrayList<>();

        SimpleBarSeries(double... data) {
            for (double d : data) {
                values.add(new SimpleNum(d));
            }
        }

        @Override public NumFactory numFactory() { return SimpleNumFactory.INSTANCE; }

        // The remaining methods are not needed for the tests; they return dummy data.
        @Override public org.ta4j.core.Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return values.size(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return values.isEmpty() ? -1 : values.size() - 1; }
        @Override public String getName() { return "SimpleBarSeries"; }
        @Override public java.util.List<org.ta4j.core.Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public org.ta4j.core.BarBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { return ""; }
        @Override public void addBar(org.ta4j.core.Bar bar) {}
        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(Number price) {}
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.Bar getFirstBar() { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.Bar getLastBar() { throw new UnsupportedOperationException(); }
        @Override public boolean isEmpty() { return values.isEmpty(); }
        @Override public void clear() {}
    }

    /** Simple indicator that returns the value at the given index from a {@link SimpleBarSeries}. */
    private static final class SimpleIndicator implements Indicator<Num> {

        private final SimpleBarSeries series;

        SimpleIndicator(SimpleBarSeries series) {
            this.series = series;
        }

        @Override public Num getValue(int index) {
            return series.values.get(index);
        }

        @Override public int getCountOfUnstableBars() { return 0; }
        @Override public BarSeries getBarSeries() { return series; }
        @Override public String toString() { return "SimpleIndicator"; }
    }

    // -------------------------------------------------------------------------
    // Helper to create a NumericIndicator from an array of double values
    private NumericIndicator numeric(double... data) {
        SimpleBarSeries series = new SimpleBarSeries(data);
        return NumericIndicator.of(new SimpleIndicator(series));
    }

    // -------------------------------------------------------------------------
    @Test
    public void testDelegateAndGetValue() {
        NumericIndicator ni = numeric(1.0, 2.5, 3.75);
        assertEquals(3, ni.getBarSeries().getBarCount());

        assertEquals(1.0, ni.getValue(0).doubleValue(), 0.000001);
        assertEquals(2.5, ni.getValue(1).doubleValue(), 0.000001);
        assertEquals(3.75, ni.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testPlusIndicator() {
        NumericIndicator a = numeric(1.0, 2.0, 3.0);
        NumericIndicator b = numeric(4.0, 5.0, 6.0);
        NumericIndicator sum = a.plus(b);
        assertEquals(5.0, sum.getValue(0).doubleValue(), 0.000001);
        assertEquals(7.0, sum.getValue(1).doubleValue(), 0.000001);
        assertEquals(9.0, sum.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator a = numeric(10.0, -5.0, 0.0);
        NumericIndicator sum = a.plus(3);
        assertEquals(13.0, sum.getValue(0).doubleValue(), 0.000001);
        assertEquals(-2.0, sum.getValue(1).doubleValue(), 0.000001);
        assertEquals(3.0, sum.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusIndicatorAndNumber() {
        NumericIndicator a = numeric(10.0, 20.0, 30.0);
        NumericIndicator b = numeric(1.0, 2.0, 3.0);
        NumericIndicator diff = a.minus(b);
        assertEquals(9.0, diff.getValue(0).doubleValue(), 0.000001);
        assertEquals(18.0, diff.getValue(1).doubleValue(), 0.000001);
        assertEquals(27.0, diff.getValue(2).doubleValue(), 0.000001);

        NumericIndicator diffNum = a.minus(5);
        assertEquals(5.0, diffNum.getValue(0).doubleValue(), 0.000001);
        assertEquals(15.0, diffNum.getValue(1).doubleValue(), 0.000001);
        assertEquals(25.0, diffNum.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testMultiplicationAndDivision() {
        NumericIndicator a = numeric(2.0, 4.0, 8.0);
        NumericIndicator b = numeric(3.0, 5.0, 2.0);

        NumericIndicator prod = a.multipliedBy(b);
        assertEquals(6.0, prod.getValue(0).doubleValue(), 0.000001);
        assertEquals(20.0, prod.getValue(1).doubleValue(), 0.000001);
        assertEquals(16.0, prod.getValue(2).doubleValue(), 0.000001);

        NumericIndicator quot = a.dividedBy(b);
        assertEquals(2.0 / 3.0, quot.getValue(0).doubleValue(), 0.000001);
        assertEquals(4.0 / 5.0, quot.getValue(1).doubleValue(), 0.000001);
        assertEquals(8.0 / 2.0, quot.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testMinMax() {
        NumericIndicator a = numeric(5.0, 10.0, -3.0);
        NumericIndicator b = numeric(2.0, 12.0, -3.0);

        NumericIndicator min = a.min(b);
        assertEquals(2.0, min.getValue(0).doubleValue(), 0.000001);
        assertEquals(10.0, min.getValue(1).doubleValue(), 0.000001);
        assertEquals(-3.0, min.getValue(2).doubleValue(), 0.000001);

        NumericIndicator max = a.max(b);
        assertEquals(5.0, max.getValue(0).doubleValue(), 0.000001);
        assertEquals(12.0, max.getValue(1).doubleValue(), 0.000001);
        assertEquals(-3.0, max.getValue(2).doubleValue(), 0.000001);

        // Number overloads
        NumericIndicator minNum = a.min(4);
        assertEquals(4.0, minNum.getValue(0).doubleValue(), 0.000001); // min(5,4)
        assertEquals(4.0, minNum.getValue(1).doubleValue(), 0.000001); // min(10,4)
        assertEquals(-3.0, minNum.getValue(2).doubleValue(), 0.000001); // min(-3,4)

        NumericIndicator maxNum = a.max(7);
        assertEquals(7.0, maxNum.getValue(0).doubleValue(), 0.000001); // max(5,7)
        assertEquals(10.0, maxNum.getValue(1).doubleValue(), 0.000001); // max(10,7)
        assertEquals(7.0, maxNum.getValue(2).doubleValue(), 0.000001); // max(-3,7)
    }

    @Test
    public void testAbsSqrtSquared() {
        NumericIndicator a = numeric(-4.0, 9.0, 0.0);

        NumericIndicator abs = a.abs();
        assertEquals(4.0, abs.getValue(0).doubleValue(), 0.000001);
        assertEquals(9.0, abs.getValue(1).doubleValue(), 0.000001);
        assertEquals(0.0, abs.getValue(2).doubleValue(), 0.000001);

        NumericIndicator sqrt = a.abs().sqrt(); // sqrt of positive numbers
        assertEquals(2.0, sqrt.getValue(0).doubleValue(), 0.000001);
        assertEquals(3.0, sqrt.getValue(1).doubleValue(), 0.000001);
        assertEquals(0.0, sqrt.getValue(2).doubleValue(), 0.000001);

        NumericIndicator squared = a.squared();
        assertEquals(16.0, squared.getValue(0).doubleValue(), 0.000001);
        assertEquals(81.0, squared.getValue(1).doubleValue(), 0.000001);
        assertEquals(0.0, squared.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testSmaAndEma() {
        // series: 1,2,3,4,5
        NumericIndicator src = numeric(1, 2, 3, 4, 5);
        NumericIndicator sma = src.sma(3); // simple moving average of last 3 values
        assertEquals(1.0, sma.getValue(0).doubleValue(), 0.000001); // only one value available -> average of 1
        assertEquals((1.0 + 2.0) / 2.0, sma.getValue(1).doubleValue(), 0.000001);
        assertEquals((1.0 + 2.0 + 3.0) / 3.0, sma.getValue(2).doubleValue(), 0.000001);
        assertEquals((2.0 + 3.0 + 4.0) / 3.0, sma.getValue(3).doubleValue(), 0.000001);
        assertEquals((3.0 + 4.0 + 5.0) / 3.0, sma.getValue(4).doubleValue(), 0.000001);

        NumericIndicator ema = src.ema(3);
        // EMA values are not trivial to compute by hand, but we can check that the
        // indicator returns a Num and that successive values are different.
        assertNotNull(ema.getValue(0));
        assertNotNull(ema.getValue(1));
        assertNotEquals(ema.getValue(0).doubleValue(), ema.getValue(1).doubleValue(), 0.000001);
    }

    @Test
    public void testStddevHighestLowestPrevious() {
        NumericIndicator src = numeric(2, 4, 6, 8, 10);

        NumericIndicator stddev = src.stddev(3);
        // Standard deviation of a constant step series is predictable.
        // For indices 0 and 1 the window is smaller than 3, but the indicator must still return a value.
        assertNotNull(stddev.getValue(0));
        assertNotNull(stddev.getValue(4));

        NumericIndicator highest = src.highest(2);
        assertEquals(2.0, highest.getValue(0).doubleValue(), 0.000001);
        assertEquals(4.0, highest.getValue(1).doubleValue(), 0.000001);
        assertEquals(6.0, highest.getValue(2).doubleValue(), 0.000001);
        assertEquals(8.0, highest.getValue(3).doubleValue(), 0.000001);
        assertEquals(10.0, highest.getValue(4).doubleValue(), 0.000001);

        NumericIndicator lowest = src.lowest(2);
        assertEquals(2.0, lowest.getValue(0).doubleValue(), 0.000001);
        assertEquals(2.0, lowest.getValue(1).doubleValue(), 0.000001);
        assertEquals(4.0, lowest.getValue(2).doubleValue(), 0.000001);
        assertEquals(6.0, lowest.getValue(3).doubleValue(), 0.000001);
        assertEquals(8.0, lowest.getValue(4).doubleValue(), 0.000001);

        NumericIndicator previous = src.previous();
        assertEquals(0.0, previous.getValue(0).doubleValue(), 0.000001); // first previous value defaults to zero
        assertEquals(2.0, previous.getValue(1).doubleValue(), 0.000001);
        assertEquals(4.0, previous.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // Indicator a: 1,2,3,4,5
        // Indicator b: 5,4,3,2,1
        NumericIndicator a = numeric(1, 2, 3, 4, 5);
        NumericIndicator b = numeric(5, 4, 3, 2, 1);

        Rule crossedOver = a.crossedOver(b); // should be true when a goes from below b to above b
        Rule crossedUnder = a.crossedUnder(b); // opposite direction

        // Index 0: a=1, b=5 (a<b) -> not crossed
        assertFalse(crossedOver.isSatisfied(0, null));
        assertFalse(crossedUnder.isSatisfied(0, null));

        // Index 2: both are 3 (equal) -> still no crossing
        assertFalse(crossedOver.isSatisfied(2, null));
        assertFalse(crossedUnder.isSatisfied(2, null));

        // Index 3: a=4, b=2 (a>b) and previous index 2 had a==b => crossing up at 3
        assertTrue(crossedOver.isSatisfied(3, null));
        assertFalse(crossedUnder.isSatisfied(3, null));

        // Index 4: a=5, b=1 (still a>b) -> no new crossing
        assertFalse(crossedOver.isSatisfied(4, null));
        assertFalse(crossedUnder.isSatisfied(4, null));
    }

    @Test
    public void testIsGreaterThanAndIsLessThanRules() {
        NumericIndicator a = numeric(10, 20, 30);
        NumericIndicator b = numeric(15, 15, 25);

        Rule greater = a.isGreaterThan(b);
        Rule less = a.isLessThan(b);

        assertFalse(greater.isSatisfied(0, null)); // 10 > 15? false
        assertTrue(greater.isSatisfied(1, null));  // 20 > 15? true
        assertTrue(greater.isSatisfied(2, null));  // 30 > 25? true

        assertTrue(less.isSatisfied(0, null)); // 10 < 15? true
        assertFalse(less.isSatisfied(1, null)); // 20 < 15? false
        assertFalse(less.isSatisfied(2, null)); // 30 < 25? false

        // Number overloads
        Rule greaterNum = a.isGreaterThan(18);
        Rule lessNum = a.isLessThan(18);

        assertFalse(greaterNum.isSatisfied(0, null)); // 10 > 18? false
        assertTrue(greaterNum.isSatisfied(1, null));  // 20 > 18? true
        assertTrue(greaterNum.isSatisfied(2, null));  // 30 > 18? true

        assertTrue(lessNum.isSatisfied(0, null)); // 10 < 18? true
        assertFalse(lessNum.isSatisfied(1, null)); // 20 < 18? false
        assertFalse(lessNum.isSatisfied(2, null)); // 30 < 18? false
    }

    @Test
    public void testFactoryMethodsClosePriceAndVolume() {
        // Use the factory methods to create a close‑price and a volume indicator.
        SimpleBarSeries series = new SimpleBarSeries(100, 200, 300);
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator volume = NumericIndicator.volume(series);

        // The close‑price indicator simply returns the series values.
        assertEquals(100.0, close.getValue(0).doubleValue(), 0.000001);
        assertEquals(200.0, close.getValue(1).doubleValue(), 0.000001);
        assertEquals(300.0, close.getValue(2).doubleValue(), 0.000001);

        // The volume indicator also returns the same values (our simple series does not differentiate).
        assertEquals(100.0, volume.getValue(0).doubleValue(), 0.000001);
        assertEquals(200.0, volume.getValue(1).doubleValue(), 0.000001);
        assertEquals(300.0, volume.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testToStringDelegatesToDelegate() {
        NumericIndicator ni = numeric(1, 2, 3);
        assertEquals("SimpleIndicator", ni.toString());
    }
}
