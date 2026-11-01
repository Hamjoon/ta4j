###Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>The tests cover:
 *
 * <ul>
 *   <li>Arithmetic operations (plus, minus, multipliedBy, dividedBy)</li>
 *   <li>Min / Max, absolute and square‑root helpers</li>
 *   <li>Derived indicators (sma, ema, stddev, highest, lowest, previous)</li>
 *   <li>Rule creation (crossedOver / crossedUnder, isGreaterThan / isLessThan)</li>
 *   <li>Delegate forwarding and toString()</li>
 *   <li>Edge cases such as division by zero and equal‑value min/max</li>
 * </ul>
 *
 * <p>All objects are created using the public constructors / factory methods that
 * belong to the ta4j library – no mocking or custom subclassing is performed.
 */
public class NumericIndicatorTest {

    /** Series used for all tests – contains 4 bars with deterministic prices & volume. */
    private BarSeries series;

    /** Helper to create a {@link Num} from a {@link Number} using the series’ Num factory. */
    private Num n(Number value) {
        return series.numOf(value);
    }

    /** Creates a simple {@link ConstantIndicator} that always returns {@code value}. */
    private ConstantIndicator<Num> constant(Number value) {
        return new ConstantIndicator<>(series, n(value));
    }

    @Before
    public void setUp() {
        // Build a series with known close prices and volumes
        series = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now();

        // (open, high, low, close, volume)
        List<Bar> bars = Arrays.asList(
                new BaseBar(Duration.ofDays(1), now.minusDays(4), n(10), n(12), n(9), n(11), n(1000)),
                new BaseBar(Duration.ofDays(1), now.minusDays(3), n(11), n(13), n(10), n(12), n(1100)),
                new BaseBar(Duration.ofDays(1), now.minusDays(2), n(12), n(14), n(11), n(13), n(1200)),
                new BaseBar(Duration.ofDays(1), now.minusDays(1), n(13), n(15), n(12), n(14), n(1300))
        );

        for (Bar b : bars) {
            series.addBar(b);
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                     Arithmetic operation test cases                     */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPlusWithIndicator() {
        NumericIndicator a = NumericIndicator.of(constant(5));
        NumericIndicator b = NumericIndicator.of(constant(3));
        NumericIndicator sum = a.plus(b);
        assertEquals(8.0, sum.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator a = NumericIndicator.of(constant(7));
        NumericIndicator sum = a.plus(2);
        assertEquals(9.0, sum.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator a = NumericIndicator.of(constant(10));
        NumericIndicator b = NumericIndicator.of(constant(4));
        NumericIndicator diff = a.minus(b);
        assertEquals(6.0, diff.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator a = NumericIndicator.of(constant(15));
        NumericIndicator diff = a.minus(5);
        assertEquals(10.0, diff.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testMultipliedByWithIndicator() {
        NumericIndicator a = NumericIndicator.of(constant(6));
        NumericIndicator b = NumericIndicator.of(constant(7));
        NumericIndicator prod = a.multipliedBy(b);
        assertEquals(42.0, prod.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testMultipliedByWithNumber() {
        NumericIndicator a = NumericIndicator.of(constant(8));
        NumericIndicator prod = a.multipliedBy(3);
        assertEquals(24.0, prod.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testDividedByWithIndicator() {
        NumericIndicator a = NumericIndicator.of(constant(20));
        NumericIndicator b = NumericIndicator.of(constant(4));
        NumericIndicator quot = a.dividedBy(b);
        assertEquals(5.0, quot.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testDividedByWithNumber() {
        NumericIndicator a = NumericIndicator.of(constant(18));
        NumericIndicator quot = a.dividedBy(2);
        assertEquals(9.0, quot.getValue(0).doubleValue(), 1e-10);
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroThrows() {
        NumericIndicator a = NumericIndicator.of(constant(5));
        // Division by zero should raise an exception from the underlying Num implementation
        a.dividedBy(0).getValue(0);
    }

    /* ---------------------------------------------------------------------- */
    /*                     Min / Max, Abs, Sqrt, Squared                     */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testMinWithIndicator() {
        NumericIndicator a = NumericIndicator.of(constant(9));
        NumericIndicator b = NumericIndicator.of(constant(4));
        NumericIndicator min = a.min(b);
        assertEquals(4.0, min.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testMinWithNumberEqualValuesReturnsThis() {
        NumericIndicator a = NumericIndicator.of(constant(5));
        NumericIndicator min = a.min(5);
        // When equal, the original instance should be returned (identity check)
        assertSame(a, min);
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator a = NumericIndicator.of(constant(2));
        NumericIndicator b = NumericIndicator.of(constant(7));
        NumericIndicator max = a.max(b);
        assertEquals(7.0, max.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testMaxWithNumberEqualValuesReturnsThis() {
        NumericIndicator a = NumericIndicator.of(constant(8));
        NumericIndicator max = a.max(8);
        assertSame(a, max);
    }

    @Test
    public void testAbs() {
        NumericIndicator a = NumericIndicator.of(constant(-9));
        NumericIndicator abs = a.abs();
        assertEquals(9.0, abs.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testSqrt() {
        NumericIndicator a = NumericIndicator.of(constant(16));
        NumericIndicator sqrt = a.sqrt();
        assertEquals(4.0, sqrt.getValue(0).doubleValue(), 1e-10);
    }

    @Test
    public void testSquared() {
        NumericIndicator a = NumericIndicator.of(constant(5));
        NumericIndicator sq = a.squared();
        assertEquals(25.0, sq.getValue(0).doubleValue(), 1e-10);
    }

    /* ---------------------------------------------------------------------- */
    /*                     Derived indicator test cases                       */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testSma() {
        // Close prices: 11,12,13,14 (from setUp)
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator sma2 = close.sma(2);
        // Expected SMA values (index 0 = first price, then rolling average)
        double[] expected = {11.0, (11 + 12) / 2.0, (12 + 13) / 2.0, (13 + 14) / 2.0};
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(expected[i], sma2.getValue(i).doubleValue(), 1e-10);
        }
    }

    @Test
    public void testEma() {
        // EMA with period 2 on the same close price series
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator ema2 = close.ema(2);
        // EMA calculation (α = 2/(period+1) = 2/3)
        double alpha = 2.0 / 3.0;
        double[] prices = {11, 12, 13, 14};
        double[] expected = new double[prices.length];
        expected[0] = prices[0]; // first EMA = first price
        for (int i = 1; i < prices.length; i++) {
            expected[i] = prices[i] * alpha + expected[i - 1] * (1 - alpha);
        }
        for (int i = 0; i < prices.length; i++) {
            assertEquals(expected[i], ema2.getValue(i).doubleValue(), 1e-10);
        }
    }

    @Test
    public void testStdDev() {
        // Use close price indicator with barCount = 2
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator std = close.stddev(2);
        // Manual standard deviation for each window of size 2
        double[] prices = {11, 12, 13, 14};
        for (int i = 0; i < prices.length; i++) {
            if (i == 0) {
                // With a single element, stddev = 0 (ta4j implementation)
                assertEquals(0.0, std.getValue(i).doubleValue(), 1e-10);
            } else {
                double mean = (prices[i - 1] + prices[i]) / 2.0;
                double variance = ((prices[i - 1] - mean) * (prices[i - 1] - mean)
                                 + (prices[i] - mean) * (prices[i] - mean)) / 2.0;
                double expected = Math.sqrt(variance);
                assertEquals(expected, std.getValue(i).doubleValue(), 1e-10);
            }
        }
    }

    @Test
    public void testHighestAndLowest() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator highest3 = close.highest(3);
        NumericIndicator lowest3 = close.lowest(3);
        double[] prices = {11, 12, 13, 14};

        for (int i = 0; i < prices.length; i++) {
            int start = Math.max(0, i - 2);
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for (int j = start; j <= i; j++) {
                max = Math.max(max, prices[j]);
                min = Math.min(min, prices[j]);
            }
            assertEquals(max, highest3.getValue(i).doubleValue(), 1e-10);
            assertEquals(min, lowest3.getValue(i).doubleValue(), 1e-10);
        }
    }

    @Test
    public void testPrevious() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator prev = close.previous();
        // previous(1) should give the value of the preceding bar, first bar => NaN or 0?
        // In ta4j, out‑of‑range indexes return NaN; we test the second bar.
        assertEquals(close.getValue(0).doubleValue(), prev.getValue(1).doubleValue(), 1e-10);
    }

    /* ---------------------------------------------------------------------- */
    /*                     Rule creation test cases                           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCrossedOverWithIndicator() {
        // Build two indicators that cross at index 2
        NumericIndicator low = NumericIndicator.of(constant(5));
        NumericIndicator high = NumericIndicator.of(constant(10));

        // Create a rule that triggers when low crosses up over high (won’t happen)
        Rule rule = low.crossedOver(high);
        assertFalse(rule.isSatisfied(1));
        // Reverse order to get a crossing (high crossing over low)
        Rule opposite = high.crossedOver(low);
        assertTrue(opposite.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderWithNumber() {
        NumericIndicator indicator = NumericIndicator.of(constant(7));
        Rule rule = indicator.crossedUnder(10); // 7 crosses under 10 at first bar
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsGreaterThanAndIsLessThan() {
        NumericIndicator a = NumericIndicator.of(constant(8));
        NumericIndicator b = NumericIndicator.of(constant(3));

        Rule greater = a.isGreaterThan(b);
        Rule less = b.isLessThan(a);

        assertTrue(greater.isSatisfied(0));
        assertTrue(less.isSatisfied(0));

        // Using numbers
        Rule greaterNum = a.isGreaterThan(5);
        Rule lessNum = b.isLessThan(5);

        assertTrue(greaterNum.isSatisfied(0));
        assertTrue(lessNum.isSatisfied(0));
    }

    /* ---------------------------------------------------------------------- */
    /*                     Delegation and toString tests                       */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDelegateAndGetBarSeries() {
        ConstantIndicator<Num> constInd = constant(42);
        NumericIndicator ni = NumericIndicator.of(constInd);
        assertSame(constInd, ni.delegate());
        assertSame(series, ni.getBarSeries());
    }

    @Test
    public void testToStringDelegates() {
        ConstantIndicator<Num> constInd = constant(99);
        NumericIndicator ni = NumericIndicator.of(constInd);
        assertEquals(constInd.toString(), ni.toString());
    }
}
```
###Test END##