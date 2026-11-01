###Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.Rule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * The tests are divided into three logical parts:
 *
 * 1. **Public method extraction** – a comment block that lists every public
 *    method (including static factory methods) together with its signature.
 *
 * 2. **Basic functionality tests** – one test per public method that checks
 *    the “happy‑path” behaviour.
 *
 * 3. **Edge‑case / exception tests** – tests that target boundary conditions,
 *    illegal arguments and special numeric situations (division by zero,
 *    square‑root of a negative number, insufficient data for moving‑average
 *    calculations, etc.).
 *
 * The test class uses only the public constructors / factory methods that are
 * present in the supplied source code or in the minimal TA‑4J API fragment
 * given in the problem statement.  No mocking frameworks are used; a real
 * {@link BarSeries} implementation (``BaseBarSeries``) is instantiated via its
 * builder.
 */
public class NumericIndicatorTest {

    /* ----------------------------------------------------------------------
     * 1. PUBLIC METHOD LIST (extracted from NumericIndicator)
     * ----------------------------------------------------------------------
     *
     * static NumericIndicator of(Indicator<Num> delegate)
     * static NumericIndicator closePrice(BarSeries bs)
     * static NumericIndicator volume(BarSeries bs)
     *
     * Indicator<Num> delegate()
     *
     * NumericIndicator plus(Indicator<Num> other)
     * NumericIndicator plus(Number n)
     *
     * NumericIndicator minus(Indicator<Num> other)
     * NumericIndicator minus(Number n)
     *
     * NumericIndicator multipliedBy(Indicator<Num> other)
     * NumericIndicator multipliedBy(Number n)
     *
     * NumericIndicator dividedBy(Indicator<Num> other)
     * NumericIndicator dividedBy(Number n)
     *
     * NumericIndicator min(Indicator<Num> other)
     * NumericIndicator min(Number n)
     *
     * NumericIndicator max(Indicator<Num> other)
     * NumericIndicator max(Number n)
     *
     * NumericIndicator abs()
     * NumericIndicator sqrt()
     * NumericIndicator squared()
     *
     * NumericIndicator sma(int barCount)
     * NumericIndicator ema(int barCount)
     * NumericIndicator stddev(int barCount)
     * NumericIndicator highest(int barCount)
     * NumericIndicator lowest(int barCount)
     *
     * NumericIndicator previous(int barCount)
     * Indicator<Num>     previous()
     *
     * Rule crossedOver(Indicator<Num> other)
     * Rule crossedOver(Number n)
     *
     * Rule crossedUnder(Indicator<Num> other)
     * Rule crossedUnder(Number n)
     *
     * Rule isGreaterThan(Indicator<Num> other)
     * Rule isGreaterThan(Number n)
     *
     * Rule isLessThan(Indicator<Num> other)
     * Rule isLessThan(Number n)
     *
     * Num getValue(int index)
     * int getUnstableBars()
     * BarSeries getBarSeries()
     * String toString()
     *
     * ---------------------------------------------------------------------- */

    private BarSeries series;
    private Num n0, n1, n2, n3, n4, n5;

    @Before
    public void setUp() {
        // Build a simple series with six bars (close price = 1,2,3,4,5,6)
        series = new BaseBarSeriesBuilder().withName("test-series").build();

        // Helper to create Num values
        n0 = series.numFactory().numOf(0);
        n1 = series.numFactory().numOf(1);
        n2 = series.numFactory().numOf(2);
        n3 = series.numFactory().numOf(3);
        n4 = series.numFactory().numOf(4);
        n5 = series.numFactory().numOf(5);

        // Populate the series – only close price is needed for most tests
        series.addBar(series.numFactory().numOf(1), // open
                      n1, // high
                      n1, // low
                      n1, // close
                      n1); // volume
        series.addBar(series.numFactory().numOf(2), n2, n2, n2, n2);
        series.addBar(series.numFactory().numOf(3), n3, n3, n3, n3);
        series.addBar(series.numFactory().numOf(4), n4, n4, n4, n4);
        series.addBar(series.numFactory().numOf(5), n5, n5, n5, n5);
        // add a sixth bar for extra safety
        series.addBar(series.numFactory().numOf(6), series.numFactory().numOf(6),
                      series.numFactory().numOf(6), series.numFactory().numOf(6),
                      series.numFactory().numOf(6));
    }

    /* ----------------------------------------------------------------------
     * 2. BASIC FUNCTIONALITY TESTS
     * ---------------------------------------------------------------------- */

    @Test
    public void testStaticFactoryOf() {
        ConstantIndicator<Num> constInd = new ConstantIndicator<>(series, n2);
        NumericIndicator ni = NumericIndicator.of(constInd);
        assertNotNull(ni);
        assertSame(constInd, ni.delegate());
    }

    @Test
    public void testStaticFactoryClosePrice() {
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertNotNull(ni);
        assertTrue(ni.delegate() instanceof ClosePriceIndicator);
    }

    @Test
    public void testStaticFactoryVolume() {
        NumericIndicator ni = NumericIndicator.volume(series);
        assertNotNull(ni);
        assertTrue(ni.delegate() instanceof VolumeIndicator);
    }

    @Test
    public void testPlusIndicator() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n1));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, n2));
        NumericIndicator sum = a.plus(b);
        assertEquals(n3, sum.getValue(0));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n3));
        NumericIndicator sum = a.plus(4); // 3 + 4 = 7
        assertEquals(series.numFactory().numOf(7), sum.getValue(0));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n5));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, n2));
        NumericIndicator diff = a.minus(b);
        assertEquals(n3, diff.getValue(0));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n5));
        NumericIndicator diff = a.minus(2);
        assertEquals(n3, diff.getValue(0));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n2));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, n3));
        NumericIndicator prod = a.multipliedBy(b);
        assertEquals(series.numFactory().numOf(6), prod.getValue(0));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n4));
        NumericIndicator prod = a.multipliedBy(3);
        assertEquals(series.numFactory().numOf(12), prod.getValue(0));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n6()));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, n2));
        NumericIndicator quot = a.dividedBy(b);
        assertEquals(series.numFactory().numOf(3), quot.getValue(0));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n6()));
        NumericIndicator quot = a.dividedBy(2);
        assertEquals(series.numFactory().numOf(3), quot.getValue(0));
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n4));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, n2));
        NumericIndicator min = a.min(b);
        assertEquals(n2, min.getValue(0));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n5));
        NumericIndicator min = a.min(3);
        assertEquals(series.numFactory().numOf(3), min.getValue(0));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n1));
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, n5));
        NumericIndicator max = a.max(b);
        assertEquals(n5, max.getValue(0));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n2));
        NumericIndicator max = a.max(7);
        assertEquals(series.numFactory().numOf(7), max.getValue(0));
    }

    @Test
    public void testAbs() {
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(-10)));
        NumericIndicator abs = neg.abs();
        assertEquals(series.numFactory().numOf(10), abs.getValue(0));
    }

    @Test
    public void testSqrt() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(9)));
        NumericIndicator sqrt = a.sqrt();
        assertEquals(series.numFactory().numOf(3), sqrt.getValue(0));
    }

    @Test
    public void testSquared() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n3));
        NumericIndicator sq = a.squared();
        assertEquals(series.numFactory().numOf(9), sq.getValue(0));
    }

    @Test
    public void testSma() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator sma = close.sma(3);
        // SMA of first three close prices (1,2,3) = 2
        assertEquals(series.numFactory().numOf(2), sma.getValue(2));
    }

    @Test
    public void testEma() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator ema = close.ema(3);
        // EMA is a bit more involved – we only verify that it returns a value
        // and that the value is a Num (non‑null)
        assertNotNull(ema.getValue(2));
    }

    @Test
    public void testStddev() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator std = close.stddev(3);
        assertNotNull(std.getValue(2));
    }

    @Test
    public void testHighest() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator high = close.highest(4);
        // Highest of [1,2,3,4] = 4
        assertEquals(series.numFactory().numOf(4), high.getValue(3));
    }

    @Test
    public void testLowest() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator low = close.lowest(4);
        // Lowest of [1,2,3,4] = 1
        assertEquals(series.numFactory().numOf(1), low.getValue(3));
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator prev = close.previous(2);
        // value at index 3 should equal close price at index 1 (value = 2)
        assertEquals(series.numFactory().numOf(2), prev.getValue(3));
    }

    @Test
    public void testPreviousDefault() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        Indicator<Num> prev1 = close.previous();
        Indicator<Num> prev2 = close.previous(1);
        // Both objects should produce the same values
        for (int i = 1; i < series.getBarCount(); i++) {
            assertEquals(prev1.getValue(i), prev2.getValue(i));
        }
    }

    @Test
    public void testCrossedOverRule() {
        // Indicator A: 1,2,3,4,5
        // Indicator B: 5,4,3,2,1  – they cross at index 2 (3 == 3) and after that A > B
        NumericIndicator a = NumericIndicator.closePrice(series);
        NumericIndicator b = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(5)))
                .minus(NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(1)))
                        .multipliedBy(NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(1)))));
        // Simpler: create a decreasing indicator manually
        // We'll just reuse the ClosePriceIndicator but with a reversed series
        BarSeries revSeries = new BaseBarSeriesBuilder().withName("rev").build();
        revSeries.addBar(series.numFactory().numOf(5), series.numFactory().numOf(5),
                series.numFactory().numOf(5), series.numFactory().numOf(5), series.numFactory().numOf(5));
        revSeries.addBar(series.numFactory().numOf(4), series.numFactory().numOf(4),
                series.numFactory().numOf(4), series.numFactory().numOf(4), series.numFactory().numOf(4));
        revSeries.addBar(series.numFactory().numOf(3), series.numFactory().numOf(3),
                series.numFactory().numOf(3), series.numFactory().numOf(3), series.numFactory().numOf(3));
        revSeries.addBar(series.numFactory().numOf(2), series.numFactory().numOf(2),
                series.numFactory().numOf(2), series.numFactory().numOf(2), series.numFactory().numOf(2));
        revSeries.addBar(series.numFactory().numOf(1), series.numFactory().numOf(1),
                series.numFactory().numOf(1), series.numFactory().numOf(1), series.numFactory().numOf(1));

        NumericIndicator decreasing = NumericIndicator.closePrice(revSeries);
        Rule crossedOver = a.crossedOver(decreasing);
        // The cross occurs at index 2 (value 3 == 3) – rule should be satisfied at index 3
        assertTrue(crossedOver.isSatisfied(3));
        // Not satisfied before the crossing
        assertFalse(crossedOver.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderRule() {
        NumericIndicator a = NumericIndicator.closePrice(series);
        NumericIndicator constFive = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(5)));
        Rule crossedUnder = a.crossedUnder(constFive);
        // a (1,2,3,4,5) is under 5 until index 4, where it equals 5 → crossing under at index 4
        assertTrue(crossedUnder.isSatisfied(4));
        assertFalse(crossedUnder.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanRule() {
        NumericIndicator a = NumericIndicator.closePrice(series);
        Rule gt = a.isGreaterThan(3);
        // Values 4 and 5 are > 3
        assertTrue(gt.isSatisfied(3));
        assertFalse(gt.isSatisfied(2));
    }

    @Test
    public void testIsLessThanRule() {
        NumericIndicator a = NumericIndicator.closePrice(series);
        Rule lt = a.isLessThan(4);
        // Values 1,2,3 are < 4
        assertTrue(lt.isSatisfied(0));
        assertTrue(lt.isSatisfied(2));
        assertFalse(lt.isSatisfied(4));
    }

    /* ----------------------------------------------------------------------
     * 3. EDGE CASE / EXCEPTION TESTS
     * ---------------------------------------------------------------------- */

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZeroNumber() {
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n5));
        // This should trigger a division‑by‑zero inside the Num implementation
        a.dividedBy(0).getValue(0);
    }

    @Test
    public void testSqrtOfNegativeNumber() {
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-4)));
        NumericIndicator sqrt = neg.sqrt();
        // According to Num contract, sqrt of a negative returns NaN
        assertTrue(sqrt.getValue(0).isNaN());
    }

    @Test
    public void testSmaBarCountLargerThanSeries() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        // series has 6 bars; ask for SMA over 10 bars – should not throw
        NumericIndicator sma = close.sma(10);
        // The first few values are undefined (NaN) – we just assert they are NaN
        assertTrue(sma.getValue(0).isNaN());
        assertTrue(sma.getValue(5).isNaN());
    }

    @Test
    public void testPreviousBeyondSeriesStart() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator prev = close.previous(10); // look back 10 bars on a 6‑bar series
        // Index 0 should return NaN because there is no previous value
        assertTrue(prev.getValue(0).isNaN());
    }

    @Test
    public void testCreateConstantWithNullNumber() {
        // The private createConstant method cannot be accessed directly,
        // but we can indirectly test its behaviour via plus(Number) with null.
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series, n1));
        try {
            a.plus((Number) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected – NumFactory.numOf(null) throws NPE
        }
    }

    /* ----------------------------------------------------------------------
     * Helper method to avoid repetition for the value 6 (which is not part of
     * the original 5‑element series).
     * ---------------------------------------------------------------------- */
    private Num n6() {
        return series.numFactory().numOf(6);
    }
}
```
###Test END##