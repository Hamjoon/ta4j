### Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator}.  The tests use very small
 * hand‑crafted implementations of {@link Num} and {@link BarSeries}
 * (named {@code SimpleNum} and {@code SimpleBarSeries}) so that the
 * library can be exercised without any external dependencies.
 *
 * The test suite is divided into three parts:
 *
 * 1. **Public method listing** – just for documentation.
 * 2. **Basic functionality** – verifies that each fluent method returns a
 *    non‑null indicator/rule and that simple arithmetic works as expected.
 * 3. **Edge‑case / exception tests** – checks division by zero,
 *    square‑root of a negative number, equality handling for {@code min}/{@code max},
 *    and rule satisfaction for crossing and comparison rules.
 *
 * All tests are written for JUnit 4.
 */
public class NumericIndicatorTest {

    /* --------------------------------------------------------------------
     *  Helper classes: SimpleNum and SimpleBarSeries
     * -------------------------------------------------------------------- */

    /** Minimal {@link Num} implementation based on a double value. */
    private static class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* ----- arithmetic ----- */
        @Override public Num plus(Num augend)   { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend){ return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand){ return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor){
            double d = ((SimpleNum) divisor).value;
            if (d == 0.0) {
                throw new ArithmeticException("division by zero");
            }
            return new SimpleNum(this.value / d);
        }
        @Override public Num remainder(Num divisor){ return new SimpleNum(this.value % ((SimpleNum) divisor).value); }
        @Override public Num floor(){ return new SimpleNum(Math.floor(this.value)); }
        @Override public Num ceil(){ return new SimpleNum(Math.ceil(this.value)); }
        @Override public Num pow(int n){ return new SimpleNum(Math.pow(this.value, n)); }
        @Override public Num pow(Num n){ return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value)); }
        @Override public Num log(){ return new SimpleNum(Math.log(this.value)); }
        @Override public Num sqrt(){ return new SimpleNum(Math.sqrt(this.value)); }
        @Override public Num sqrt(int precision){ return sqrt(); }
        @Override public Num abs(){ return new SimpleNum(Math.abs(this.value)); }
        @Override public Num negate(){ return new SimpleNum(-this.value); }

        /* ----- comparisons ----- */
        @Override public boolean isZero(){ return this.value == 0.0; }
        @Override public boolean isPositive(){ return this.value > 0.0; }
        @Override public boolean isPositiveOrZero(){ return this.value >= 0.0; }
        @Override public boolean isNegative(){ return this.value < 0.0; }
        @Override public boolean isNegativeOrZero(){ return this.value <= 0.0; }
        @Override public boolean isEqual(Num other){ return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other){ return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other){ return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other){ return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other){ return this.value <= ((SimpleNum) other).value; }
        @Override public Num min(Num other){ return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other){ return this.isGreaterThan(other) ? this : other; }

        /* ----- misc ----- */
        @Override public Number getDelegate(){ return this.value; }
        @Override public String getName(){ return Double.toString(this.value); }
        @Override public Function<Number, Num> function(){ return SimpleNum::new; }
        @Override public Num numOf(Number number){ return new SimpleNum(number.doubleValue()); }
        @Override public Num numOf(String value, int precision){ return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN(){ return Double.isNaN(this.value); }
        @Override public double doubleValue(){ return this.value; }
        @Override public int intValue(){ return (int) this.value; }
        @Override public long longValue(){ return (long) this.value; }
        @Override public float floatValue(){ return (float) this.value; }
        @Override public int hashCode(){ return Double.hashCode(this.value); }
        @Override public String toString(){ return Double.toString(this.value); }
        @Override public boolean equals(Object obj){
            return (obj instanceof SimpleNum) && Double.compare(this.value, ((SimpleNum) obj).value) == 0;
        }
    }

    /** Minimal {@link BarSeries} implementation – only the methods required
     *  by the tests are implemented; everything else throws {@link UnsupportedOperationException}. */
    private static class SimpleBarSeries implements BarSeries {

        private final SimpleNum zero = new SimpleNum(0);
        private final SimpleNum one  = new SimpleNum(1);
        private final SimpleNum hundred = new SimpleNum(100);

        @Override public String getName(){ return "SimpleSeries"; }
        @Override public Num num(){ return zero; }
        @Override public Function<Number, Num> function(){ return SimpleNum::new; }
        @Override public Num zero(){ return zero; }
        @Override public Num one(){ return one; }
        @Override public Num hundred(){ return hundred; }
        @Override public Num numOf(Number number){ return new SimpleNum(number.doubleValue()); }
        @Override public Bar getBar(int i){ throw new UnsupportedOperationException(); }
        @Override public Bar getFirstBar(){ throw new UnsupportedOperationException(); }
        @Override public Bar getLastBar(){ throw new UnsupportedOperationException(); }
        @Override public int getBarCount(){ return 0; }
        @Override public boolean isEmpty(){ return true; }
        @Override public List<Bar> getBarData(){ return Collections.emptyList(); }
        @Override public int getBeginIndex(){ return 0; }
        @Override public int getEndIndex(){ return -1; }
        @Override public String getSeriesPeriodDescription(){ return ""; }
        @Override public int getMaximumBarCount(){ return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount){ }
        @Override public int getRemovedBarsCount(){ return 0; }
        @Override public void addBar(Bar bar){ }
        @Override public void addBar(Bar bar, boolean replace){ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime){ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice){ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume){ }
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount){ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume){ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount){ }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice){ }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume){ }
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume, String amount){ }
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume){ }
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount){ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume){ }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount){ }
        @Override public void addTrade(Number tradeVolume, Number tradePrice){ }
        @Override public void addTrade(String tradeVolume, String tradePrice){ }
        @Override public void addTrade(Num tradeVolume, Num tradePrice){ }
        @Override public void addPrice(Num price){ }
        @Override public void addPrice(String price){ }
        @Override public void addPrice(Number price){ }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex){ throw new UnsupportedOperationException(); }
    }

    /** Helper method to create a {@link NumericIndicator} that always returns the same value. */
    private NumericIndicator constantIndicator(double value) {
        SimpleBarSeries series = new SimpleBarSeries();
        SimpleNum num = new SimpleNum(value);
        ConstantIndicator<Num> constInd = new ConstantIndicator<>(series, num);
        return NumericIndicator.of(constInd);
    }

    /* --------------------------------------------------------------------
     *  1. List of public methods (for documentation only)
     * -------------------------------------------------------------------- */
    // The public API of NumericIndicator (extracted from the source):
    //   static NumericIndicator of(Indicator<Num> delegate)
    //   static NumericIndicator closePrice(BarSeries bs)
    //   static NumericIndicator volume(BarSeries bs)
    //   Indicator<Num> delegate()
    //   NumericIndicator plus(Indicator<Num> other)
    //   NumericIndicator plus(Number n)
    //   NumericIndicator minus(Indicator<Num> other)
    //   NumericIndicator minus(Number n)
    //   NumericIndicator multipliedBy(Indicator<Num> other)
    //   NumericIndicator multipliedBy(Number n)
    //   NumericIndicator dividedBy(Indicator<Num> other)
    //   NumericIndicator dividedBy(Number n)
    //   NumericIndicator min(Indicator<Num> other)
    //   NumericIndicator min(Number n)
    //   NumericIndicator max(Indicator<Num> other)
    //   NumericIndicator max(Number n)
    //   NumericIndicator abs()
    //   NumericIndicator sqrt()
    //   NumericIndicator squared()
    //   NumericIndicator sma(int barCount)
    //   NumericIndicator ema(int barCount)
    //   NumericIndicator stddev(int barCount)
    //   NumericIndicator highest(int barCount)
    //   NumericIndicator lowest(int barCount)
    //   NumericIndicator previous(int barCount)
    //   Indicator<Num> previous()
    //   Rule crossedOver(Indicator<Num> other)
    //   Rule crossedOver(Number n)
    //   Rule crossedUnder(Indicator<Num> other)
    //   Rule crossedUnder(Number n)
    //   Rule isGreaterThan(Indicator<Num> other)
    //   Rule isGreaterThan(Number n)
    //   Rule isLessThan(Indicator<Num> other)
    //   Rule isLessThan(Number n)
    //   private Indicator<Num> createConstant(Number n)
    //   Num getValue(int index)
    //   int getUnstableBars()
    //   BarSeries getBarSeries()
    //   String toString()

    /* --------------------------------------------------------------------
     *  2. Basic functionality tests
     * -------------------------------------------------------------------- */

    @Test
    public void testOfCreatesNonNullIndicator() {
        NumericIndicator ind = NumericIndicator.of(constantIndicator(5).delegate());
        assertNotNull(ind);
        assertEquals(5.0, ind.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testPlusWithIndicator() {
        NumericIndicator a = constantIndicator(3);
        NumericIndicator b = constantIndicator(4);
        NumericIndicator sum = a.plus(b);
        assertEquals(7.0, sum.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator a = constantIndicator(10);
        NumericIndicator sum = a.plus(5);
        assertEquals(15.0, sum.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator a = constantIndicator(8);
        NumericIndicator b = constantIndicator(3);
        NumericIndicator diff = a.minus(b);
        assertEquals(5.0, diff.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator a = constantIndicator(10);
        NumericIndicator diff = a.minus(4);
        assertEquals(6.0, diff.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMultipliedByWithIndicator() {
        NumericIndicator a = constantIndicator(6);
        NumericIndicator b = constantIndicator(7);
        NumericIndicator prod = a.multipliedBy(b);
        assertEquals(42.0, prod.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMultipliedByWithNumber() {
        NumericIndicator a = constantIndicator(5);
        NumericIndicator prod = a.multipliedBy(3);
        assertEquals(15.0, prod.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testDividedByWithIndicator() {
        NumericIndicator a = constantIndicator(20);
        NumericIndicator b = constantIndicator(4);
        NumericIndicator quot = a.dividedBy(b);
        assertEquals(5.0, quot.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testDividedByWithNumber() {
        NumericIndicator a = constantIndicator(9);
        NumericIndicator quot = a.dividedBy(3);
        assertEquals(3.0, quot.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMinWithIndicator() {
        NumericIndicator a = constantIndicator(2);
        NumericIndicator b = constantIndicator(5);
        NumericIndicator min = a.min(b);
        assertEquals(2.0, min.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMinWithNumber() {
        NumericIndicator a = constantIndicator(7);
        NumericIndicator min = a.min(10);
        assertEquals(7.0, min.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator a = constantIndicator(12);
        NumericIndicator b = constantIndicator(9);
        NumericIndicator max = a.max(b);
        assertEquals(12.0, max.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testMaxWithNumber() {
        NumericIndicator a = constantIndicator(4);
        NumericIndicator max = a.max(1);
        assertEquals(4.0, max.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testAbs() {
        NumericIndicator a = constantIndicator(-3.5);
        NumericIndicator abs = a.abs();
        assertEquals(3.5, abs.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testSqrt() {
        NumericIndicator a = constantIndicator(16);
        NumericIndicator sqrt = a.sqrt();
        assertEquals(4.0, sqrt.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testSquared() {
        NumericIndicator a = constantIndicator(5);
        NumericIndicator sq = a.squared();
        assertEquals(25.0, sq.getValue(0).doubleValue(), 0.00001);
    }

    @Test
    public void testSmaCreatesIndicator() {
        NumericIndicator a = constantIndicator(1);
        NumericIndicator sma = a.sma(3);
        assertNotNull(sma);
        // SMA of constant series = constant
        assertEquals(1.0, sma.getValue(0).doubleValue(), 0.00001);
        assertTrue(sma.delegate() instanceof SMAIndicator);
    }

    @Test
    public void testEmaCreatesIndicator() {
        NumericIndicator a = constantIndicator(2);
        NumericIndicator ema = a.ema(3);
        assertNotNull(ema);
        assertEquals(2.0, ema.getValue(0).doubleValue(), 0.00001);
        assertTrue(ema.delegate() instanceof EMAIndicator);
    }

    @Test
    public void testStddevCreatesIndicator() {
        NumericIndicator a = constantIndicator(3);
        NumericIndicator stddev = a.stddev(2);
        assertNotNull(stddev);
        assertEquals(0.0, stddev.getValue(0).doubleValue(), 0.00001); // zero variance
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
    }

    @Test
    public void testHighestCreatesIndicator() {
        NumericIndicator a = constantIndicator(4);
        NumericIndicator highest = a.highest(2);
        assertNotNull(highest);
        assertEquals(4.0, highest.getValue(0).doubleValue(), 0.00001);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);
    }

    @Test
    public void testLowestCreatesIndicator() {
        NumericIndicator a = constantIndicator(4);
        NumericIndicator lowest = a.lowest(2);
        assertNotNull(lowest);
        assertEquals(4.0, lowest.getValue(0).doubleValue(), 0.00001);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
    }

    @Test
    public void testPreviousCreatesIndicator() {
        NumericIndicator a = constantIndicator(9);
        Indicator<Num> prev = a.previous();
        assertNotNull(prev);
        assertTrue(prev instanceof PreviousValueIndicator);
    }

    @Test
    public void testCrossedOverWithIndicator() {
        // a goes from 1 to 2, b stays at 1.5 -> crossing up at index 1
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(new SimpleBarSeries(), new SimpleNum(1)));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(new SimpleBarSeries(), new SimpleNum(1.5)));
        Rule rule = a.crossedOver(b);
        // Since both are constant, there is never a crossing – rule must be false
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderWithNumber() {
        NumericIndicator a = constantIndicator(5);
        Rule rule = a.crossedUnder(10); // 5 never crosses under 10 (it stays below)
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testIsGreaterThan() {
        NumericIndicator a = constantIndicator(8);
        Rule rule = a.isGreaterThan(5);
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThan() {
        NumericIndicator a = constantIndicator(3);
        Rule rule = a.isLessThan(7);
        assertTrue(rule.isSatisfied(0));
    }

    /* --------------------------------------------------------------------
     *  3. Edge‑case / exception tests
     * -------------------------------------------------------------------- */

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZeroThrowsException() {
        NumericIndicator a = constantIndicator(10);
        // This should trigger division by zero inside SimpleNum.dividedBy
        a.dividedBy(0).getValue(0);
    }

    @Test
    public void testSqrtOfNegativeNumberReturnsNaN() {
        NumericIndicator a = constantIndicator(-4);
        NumericIndicator sqrt = a.sqrt();
        assertTrue(sqrt.getValue(0).isNaN());
    }

    @Test
    public void testMinWhenValuesAreEqualReturnsFirstIndicator() {
        NumericIndicator a = constantIndicator(5);
        NumericIndicator b = constantIndicator(5);
        NumericIndicator min = a.min(b);
        // The result should be the same instance as 'a' (by contract)
        assertSame(a.delegate(), min.delegate());
    }

    @Test
    public void testMaxWhenValuesAreEqualReturnsFirstIndicator() {
        NumericIndicator a = constantIndicator(7);
        NumericIndicator b = constantIndicator(7);
        NumericIndicator max = a.max(b);
        assertSame(a.delegate(), max.delegate());
    }

    @Test
    public void testCrossedOverRuleTrueWhenCrossingUpwards() {
        // Create two indicators that cross: a = [1, 3], b = [2, 2]
        SimpleBarSeries series = new SimpleBarSeries();
        ConstantIndicator<Num> a0 = new ConstantIndicator<>(series, new SimpleNum(1));
        ConstantIndicator<Num> a1 = new ConstantIndicator<>(series, new SimpleNum(3));
        ConstantIndicator<Num> b = new ConstantIndicator<>(series, new SimpleNum(2));

        // Helper to switch values by index
        Indicator<Num> a = index -> (index == 0 ? a0.getValue(0) : a1.getValue(0));

        NumericIndicator na = NumericIndicator.of(a);
        Rule rule = na.crossedOver(b);
        // At index 1 the value goes from below 2 to above 2 → rule true
        assertTrue(rule.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderRuleTrueWhenCrossingDownwards() {
        // a = [4, 1], b = constant 2 → crossing down at index 1
        SimpleBarSeries series = new SimpleBarSeries();
        ConstantIndicator<Num> a0 = new ConstantIndicator<>(series, new SimpleNum(4));
        ConstantIndicator<Num> a1 = new ConstantIndicator<>(series, new SimpleNum(1));
        ConstantIndicator<Num> b = new ConstantIndicator<>(series, new SimpleNum(2));

        Indicator<Num> a = index -> (index == 0 ? a0.getValue(0) : a1.getValue(0));

        NumericIndicator na = NumericIndicator.of(a);
        Rule rule = na.crossedUnder(b);
        assertTrue(rule.isSatisfied(1));
    }

    @Test
    public void testIsGreaterThanRuleFalseWhenEqual() {
        NumericIndicator a = constantIndicator(5);
        Rule rule = a.isGreaterThan(5);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanRuleFalseWhenEqual() {
        NumericIndicator a = constantIndicator(5);
        Rule rule = a.isLessThan(5);
        assertFalse(rule.isSatisfied(0));
    }
}
```
### Test END##