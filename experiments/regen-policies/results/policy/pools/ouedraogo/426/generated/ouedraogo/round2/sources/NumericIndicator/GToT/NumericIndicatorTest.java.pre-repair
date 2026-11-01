/**
 * JUnit‑4 test suite for {@link org.ta4j.core.indicators.numeric.NumericIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public methods (basic functionality)</li>
 *   <li>Edge‑case scenarios such as division by zero, sqrt of a negative number,
 *       zero‑length windows, equality in {@code min}/{@code max}, etc.</li>
 *   <li>Rule‑generation methods (crossedOver, crossedUnder, isGreaterThan,
 *       isLessThan) with both indicator and constant arguments.</li>
 * </ul>
 *
 * The library provides concrete implementations for {@link org.ta4j.core.BarSeries}
 * (namely {@code BaseBarSeries}) and {@link org.ta4j.core.Bar}
 * which are used to build a deterministic series of bars.
 */
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.rules.Rule;

/**
 * Comprehensive tests for {@link NumericIndicator}.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;   // close price indicator wrapped in NumericIndicator
    private NumericIndicator volume;  // volume indicator wrapped in NumericIndicator

    /**
     * Build a deterministic series:
     *
     * <pre>
     * index | close | volume
     * -------------------------
     *   0   |  1.0  | 10.0
     *   1   |  2.0  | 20.0
     *   2   |  3.0  | 30.0
     *   3   |  4.0  | 40.0
     *   4   |  5.0  | 50.0
     * </pre>
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries("testSeries");
        for (int i = 0; i < 5; i++) {
            Num closePrice = series.numFactory().numOf(i + 1); // 1,2,3,4,5
            Num volumeVal = series.numFactory().numOf((i + 1) * 10); // 10,20,...
            BaseBar bar = new BaseBar(
                    series.numFactory().numOf(0),   // duration (not used)
                    closePrice,
                    closePrice, // high
                    closePrice, // low
                    closePrice, // open (same as close for simplicity)
                    volumeVal,
                    series.numFactory().numOf(0)    // amount (not used)
            );
            series.addBar(bar);
        }
        close = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    /* --------------------------------------------------------------------- */
    /*  BASIC ARITHMETIC OPERATIONS                                          */
    /* --------------------------------------------------------------------- */

    @Test
    public void testPlusWithIndicator() {
        NumericIndicator two = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(2)));
        NumericIndicator result = close.plus(two);
        assertEquals(series.numFactory().numOf(5), result.getValue(2)); // 3 + 2 = 5
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator result = close.plus(5);
        assertEquals(series.numFactory().numOf(8), result.getValue(2)); // 3 + 5 = 8
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator two = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(2)));
        NumericIndicator result = close.minus(two);
        assertEquals(series.numFactory().numOf(1), result.getValue(2)); // 3 - 2 = 1
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator result = close.minus(1);
        assertEquals(series.numFactory().numOf(2), result.getValue(2)); // 3 - 1 = 2
    }

    @Test
    public void testMultipliedByWithIndicator() {
        NumericIndicator two = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(2)));
        NumericIndicator result = close.multipliedBy(two);
        assertEquals(series.numFactory().numOf(6), result.getValue(2)); // 3 * 2 = 6
    }

    @Test
    public void testMultipliedByWithNumber() {
        NumericIndicator result = close.multipliedBy(4);
        assertEquals(series.numFactory().numOf(12), result.getValue(2)); // 3 * 4 = 12
    }

    @Test
    public void testDividedByWithIndicator() {
        NumericIndicator two = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(2)));
        NumericIndicator result = close.dividedBy(two);
        assertEquals(series.numFactory().numOf(1.5), result.getValue(2)); // 3 / 2 = 1.5
    }

    @Test
    public void testDividedByWithNumber() {
        NumericIndicator result = close.dividedBy(2);
        assertEquals(series.numFactory().numOf(1.5), result.getValue(2));
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroThrows() {
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(0)));
        // The underlying Num implementation throws ArithmeticException for division by zero
        close.dividedBy(zero).getValue(0);
    }

    @Test
    public void testMinWithIndicator() {
        NumericIndicator five = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(5)));
        NumericIndicator result = close.min(five);
        assertEquals(series.numFactory().numOf(3), result.getValue(2)); // min(3,5) = 3
    }

    @Test
    public void testMinWithNumber() {
        NumericIndicator result = close.min(2);
        assertEquals(series.numFactory().numOf(2), result.getValue(2)); // min(3,2) = 2
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator five = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(5)));
        NumericIndicator result = close.max(five);
        assertEquals(series.numFactory().numOf(5), result.getValue(2)); // max(3,5) = 5
    }

    @Test
    public void testMaxWithNumber() {
        NumericIndicator result = close.max(2);
        assertEquals(series.numFactory().numOf(3), result.getValue(2)); // max(3,2) = 3
    }

    @Test
    public void testAbs() {
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-4)));
        NumericIndicator result = negative.abs();
        assertEquals(series.numFactory().numOf(4), result.getValue(0));
    }

    @Test
    public void testSqrtPositive() {
        NumericIndicator result = close.sqrt(); // sqrt(3) ≈ 1.732...
        Num expected = series.numFactory().numOf(3).sqrt();
        assertEquals(expected, result.getValue(2));
    }

    @Test
    public void testSqrtNegativeProducesNaN() {
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-9)));
        NumericIndicator result = negative.sqrt();
        assertTrue(result.getValue(0).isNaN());
    }

    @Test
    public void testSquared() {
        NumericIndicator result = close.squared(); // 3 * 3 = 9
        assertEquals(series.numFactory().numOf(9), result.getValue(2));
    }

    /* --------------------------------------------------------------------- */
    /*  MOVING‑AVERAGE / STATISTICAL INDICATORS                              */
    /* --------------------------------------------------------------------- */

    @Test
    public void testSma() {
        NumericIndicator sma3 = close.sma(3);
        // SMA at index 2 = (1+2+3)/3 = 2
        assertEquals(series.numFactory().numOf(2), sma3.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmaZeroBarCountThrows() {
        close.sma(0);
    }

    @Test
    public void testEma() {
        // EMA with barCount = 3, k = 2/(3+1) = 0.5
        // EMA0 = price0 = 1
        // EMA1 = price1 * k + EMA0 * (1 - k) = 2*0.5 + 1*0.5 = 1.5
        // EMA2 = 3*0.5 + 1.5*0.5 = 2.25
        NumericIndicator ema3 = close.ema(3);
        Num expected = series.numFactory().numOf(2.25);
        assertEquals(expected, ema3.getValue(2));
    }

    @Test
    public void testStandardDeviation() {
        // StdDev of first 3 close prices: 1,2,3
        // mean = 2, variance = ((1-2)^2 + (2-2)^2 + (3-2)^2)/3 = (1+0+1)/3 = 2/3
        // stddev = sqrt(2/3) ≈ 0.81649658
        NumericIndicator stddev3 = close.stddev(3);
        Num expected = series.numFactory().numOf(2.0/3.0).sqrt();
        assertEquals(expected, stddev3.getValue(2));
    }

    @Test
    public void testHighest() {
        // Highest of last 3 values at index 4 -> max(3,4,5) = 5
        NumericIndicator highest3 = close.highest(3);
        assertEquals(series.numFactory().numOf(5), highest3.getValue(4));
    }

    @Test
    public void testLowest() {
        // Lowest of last 3 values at index 4 -> min(3,4,5) = 3
        NumericIndicator lowest3 = close.lowest(3);
        assertEquals(series.numFactory().numOf(3), lowest3.getValue(4));
    }

    @Test
    public void testPreviousWithParameter() {
        NumericIndicator prev2 = close.previous(2);
        // previous(2) at index 3 should return value at index 1 (2.0)
        assertEquals(series.numFactory().numOf(2), prev2.getValue(3));
    }

    @Test
    public void testPreviousWithoutParameter() {
        NumericIndicator prev1 = close.previous(); // same as previous(1)
        // previous at index 3 -> value at index 2 (3.0)
        assertEquals(series.numFactory().numOf(3), prev1.getValue(3));
    }

    /* --------------------------------------------------------------------- */
    /*  RULE‑GENERATION METHODS                                              */
    /* --------------------------------------------------------------------- */

    @Test
    public void testCrossedOverWithIndicator() {
        // Indicator that is always 2 (constant)
        NumericIndicator constant2 = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(2)));
        // close series: 1,2,3,4,5 -> cross over 2 between index 1 (2) and 2 (3)
        Rule rule = close.crossedOver(constant2);
        // Rule should be satisfied at index 2 (first point where close > 2 after being <=2)
        assertTrue(rule.isSatisfied(2));
        // Not satisfied at index 1 (exactly equal, not a crossing)
        assertFalse(rule.isSatisfied(1));
    }

    @Test
    public void testCrossedOverWithNumber() {
        Rule rule = close.crossedOver(3);
        // crossing 3 occurs between index 2 (3) and 3 (4)
        assertTrue(rule.isSatisfied(3));
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderWithIndicator() {
        NumericIndicator constant4 = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(4)));
        Rule rule = close.crossedUnder(constant4);
        // crossing under 4 occurs between index 2 (3) and 3 (4) -> not yet under,
        // actually under occurs at index 4 (5->?) No, need a point where close goes from >=4 to <4.
        // Since series is monotonic increasing, the crossing never happens; we test false.
        assertFalse(rule.isSatisfied(3));
    }

    @Test
    public void testCrossedUnderWithNumber() {
        Rule rule = close.crossedUnder(2);
        // close goes from 2 (index1) to 3 (index2) -> crossing under 2 never occurs
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanWithIndicator() {
        NumericIndicator constant3 = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(3)));
        Rule rule = close.isGreaterThan(constant3);
        // true from index 3 onward (close 4,5)
        assertTrue(rule.isSatisfied(3));
        assertFalse(rule.isSatisfied(2)); // close == 3 not greater
    }

    @Test
    public void testIsGreaterThanWithNumber() {
        Rule rule = close.isGreaterThan(4);
        assertTrue(rule.isSatisfied(4));
        assertFalse(rule.isSatisfied(3));
    }

    @Test
    public void testIsLessThanWithIndicator() {
        NumericIndicator constant3 = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(3)));
        Rule rule = close.isLessThan(constant3);
        // true for indices 0,1 (1,2)
        assertTrue(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2)); // equal, not less
    }

    @Test
    public void testIsLessThanWithNumber() {
        Rule rule = close.isLessThan(2);
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1)); // equal to 2, not less
    }

    /* --------------------------------------------------------------------- */
    /*  EDGE CASES FOR MIN/MAX (equality)                                    */
    /* --------------------------------------------------------------------- */

    @Test
    public void testMinWhenEqualReturnsThis() {
        NumericIndicator same = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(3)));
        NumericIndicator result = same.min(3);
        // Both are equal; the contract says "this" is returned.
        // Hence the underlying delegate should be the same instance.
        assertSame(same.delegate(), result.delegate());
    }

    @Test
    public void testMaxWhenEqualReturnsThis() {
        NumericIndicator same = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(3)));
        NumericIndicator result = same.max(3);
        assertSame(same.delegate(), result.delegate());
    }

    /* --------------------------------------------------------------------- */
    /*  HELPERS / INTERNAL VERIFICATION                                      */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDelegateMethod() {
        assertSame(close.delegate(), close.delegate());
    }

    @Test
    public void testToStringDelegates() {
        assertEquals(close.delegate().toString(), close.toString());
    }

    @Test
    public void testGetCountOfUnstableBarsDelegates() {
        assertEquals(close.delegate().getCountOfUnstableBars(),
                close.getCountOfUnstableBars());
    }

    @Test
    public void testGetBarSeriesDelegates() {
        assertSame(series, close.getBarSeries());
    }
}
