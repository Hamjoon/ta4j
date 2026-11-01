**Public methods of `NumericIndicator`**

| # | Signature | Description |
|---|-----------|-------------|
| 1 | `public static NumericIndicator of(Indicator<Num> delegate)` | Wrap a regular `Indicator<Num>` into a fluent `NumericIndicator`. |
| 2 | `public static NumericIndicator closePrice(BarSeries bs)` | Shortcut to create a `NumericIndicator` around a `ClosePriceIndicator`. |
| 3 | `public static NumericIndicator volume(BarSeries bs)` | Shortcut to create a `NumericIndicator` around a `VolumeIndicator`. |
| 4 | `public Indicator<Num> delegate()` | Returns the underlying delegate indicator. |
| 5 | `public NumericIndicator plus(Indicator<Num> other)` | Returns a new `NumericIndicator` representing the sum of this and *other*. |
| 6 | `public NumericIndicator plus(Number n)` | Returns a new `NumericIndicator` representing the sum of this and *n*. |
| 7 | `public NumericIndicator minus(Indicator<Num> other)` | Returns a new `NumericIndicator` representing the difference of this and *other*. |
| 8 | `public NumericIndicator minus(Number n)` | Returns a new `NumericIndicator` representing the difference of this and *n*. |
| 9 | `public NumericIndicator multipliedBy(Indicator<Num> other)` | Returns a new `NumericIndicator` representing the product of this and *other*. |
|10| `public NumericIndicator multipliedBy(Number n)` | Returns a new `NumericIndicator` representing the product of this and *n*. |
|11| `public NumericIndicator dividedBy(Indicator<Num> other)` | Returns a new `NumericIndicator` representing the quotient of this and *other*. |
|12| `public NumericIndicator dividedBy(Number n)` | Returns a new `NumericIndicator` representing the quotient of this and *n*. |
|13| `public NumericIndicator min(Indicator<Num> other)` | Returns a new `NumericIndicator` containing the minimum of this and *other*. |
|14| `public NumericIndicator min(Number n)` | Returns a new `NumericIndicator` containing the minimum of this and *n*. |
|15| `public NumericIndicator max(Indicator<Num> other)` | Returns a new `NumericIndicator` containing the maximum of this and *other*. |
|16| `public NumericIndicator max(Number n)` | Returns a new `NumericIndicator` containing the maximum of this and *n*. |
|17| `public NumericIndicator abs()` | Returns an indicator with absolute values of this. |
|18| `public NumericIndicator sqrt()` | Returns an indicator with square‑root of this. |
|19| `public NumericIndicator squared()` | Returns an indicator with this multiplied by itself. |
|20| `public NumericIndicator sma(int barCount)` | Simple Moving Average of this indicator. |
|21| `public NumericIndicator ema(int barCount)` | Exponential Moving Average of this indicator. |
|22| `public NumericIndicator stddev(int barCount)` | Standard deviation of this indicator. |
|23| `public NumericIndicator highest(int barCount)` | Highest value of this indicator over the period. |
|24| `public NumericIndicator lowest(int barCount)` | Lowest value of this indicator over the period. |
|25| `public NumericIndicator previous(int barCount)` | Value *barCount* periods ago. |
|26| `public NumericIndicator previous()` | Value 1 period ago (shortcut). |
|27| `public Rule crossedOver(Indicator<Num> other)` | Rule that is satisfied when this crosses **up** over *other*. |
|28| `public Rule crossedOver(Number n)` | Rule that is satisfied when this crosses **up** over a constant *n*. |
|29| `public Rule crossedUnder(Indicator<Num> other)` | Rule that is satisfied when this crosses **down** under *other*. |
|30| `public Rule crossedUnder(Number n)` | Rule that is satisfied when this crosses **down** under a constant *n*. |
|31| `public Rule isGreaterThan(Indicator<Num> other)` | Rule that is satisfied when this is greater than *other*. |
|32| `public Rule isGreaterThan(Number n)` | Rule that is satisfied when this is greater than a constant *n*. |
|33| `public Rule isLessThan(Indicator<Num> other)` | Rule that is satisfied when this is less than *other*. |
|34| `public Rule isLessThan(Number n)` | Rule that is satisfied when this is less than a constant *n*. |
|35| `public Num getValue(int index)` | Delegates to the wrapped indicator. |
|36| `public int getCountOfUnstableBars()` | Returns `0` (no unstable bars). |
|37| `public BarSeries getBarSeries()` | Delegates to the wrapped indicator. |
|38| `public String toString()` | Delegates to the wrapped indicator. |

---

## Identified edge‑case / exception scenarios  

| Method | Edge case / Exception | Reason |
|--------|----------------------|--------|
| `plus(Number)`, `minus(Number)`, `multipliedBy(Number)`, `dividedBy(Number)` | `null` argument | The helper `createConstant` would receive a `null` → `NullPointerException`. |
| `dividedBy(Number)` | Division by **zero** | `Num` division by zero throws `ArithmeticException`. |
| `sqrt()` | Underlying value **negative** | `Num.sqrt()` returns **NaN** for negative numbers (checked with `isNaN()`). |
| `sma(int)`, `ema(int)`, `stddev(int)`, `highest(int)`, `lowest(int)`, `previous(int)` | `barCount ≤ 0` | Underlying indicator constructors reject non‑positive periods → `IllegalArgumentException`. |
| `previous(int)` | `barCount` larger than current index | `PreviousValueIndicator` returns the earliest available value (index 0). |
| `crossedOver(Number)`, `crossedUnder(Number)`, `isGreaterThan(Number)`, `isLessThan(Number)` | `null` argument | Same `NullPointerException` via `createConstant`. |
| `crossedOver(Indicator)`, `crossedUnder(Indicator)`, `isGreaterThan(Indicator)`, `isLessThan(Indicator)` | `null` argument | Direct NPE when the library tries to use the null indicator. |
| `of(Indicator)` | `null` argument | Should throw `NullPointerException`. |

---

## Complete JUnit 4 test file  

```java
###Test START##
package org.ta4j.core.indicators.numeric;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.rules.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;   // close‑price based NumericIndicator
    private NumericIndicator constantFive; // constant 5 indicator

    /** Helper to create a simple series with deterministic close prices 1..5 */
    private BarSeries buildSeries() {
        BarSeries s = new BaseBarSeriesBuilder().withName("test-series").build();
        for (int i = 1; i <= 5; i++) {
            Num price = s.numFactory().numOf(i);
            // open, high, low, close, volume are all the same for simplicity
            s.addBar(new BaseBar(
                    Duration.ofDays(1),
                    ZonedDateTime.now().plusDays(i),
                    price, price, price, price,
                    price));
        }
        return s;
    }

    @Before
    public void setUp() {
        series = buildSeries();
        close = NumericIndicator.closePrice(series);
        constantFive = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(5)));
    }

    /* ---------------------------------------------------------------------- */
    /* Basic functionality tests                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOfCreatesNonNullInstance() {
        Indicator<Num> ind = new ClosePriceIndicator(series);
        NumericIndicator ni = NumericIndicator.of(ind);
        assertNotNull(ni);
        assertSame(ind, ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator ind = NumericIndicator.closePrice(series);
        assertNotNull(ind);
        assertEquals(close.getValue(0), ind.getValue(0));
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertNotNull(vol);
        // first bar volume = 1 (as we used price for volume as well)
        assertEquals(series.numFactory().numOf(1), vol.getValue(0));
    }

    @Test
    public void testPlusWithIndicator() {
        NumericIndicator result = close.plus(constantFive);
        Num expected = series.numFactory().numOf(1 + 5); // first close = 1
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator result = close.plus(5);
        Num expected = series.numFactory().numOf(1 + 5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator result = close.minus(constantFive);
        Num expected = series.numFactory().numOf(1 - 5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator result = close.minus(5);
        Num expected = series.numFactory().numOf(1 - 5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator result = close.multipliedBy(constantFive);
        Num expected = series.numFactory().numOf(1 * 5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator result = close.multipliedBy(5);
        Num expected = series.numFactory().numOf(1 * 5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator divisor = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(2)));
        NumericIndicator result = close.dividedBy(divisor);
        Num expected = series.numFactory().numOf(1.0 / 2.0);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator result = close.dividedBy(2);
        Num expected = series.numFactory().numOf(1.0 / 2.0);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMinWithIndicator() {
        NumericIndicator result = close.min(constantFive);
        Num expected = series.numFactory().numOf(1); // 1 < 5
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMinWithNumber() {
        NumericIndicator result = close.min(5);
        Num expected = series.numFactory().numOf(1);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator result = close.max(constantFive);
        Num expected = series.numFactory().numOf(5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testMaxWithNumber() {
        NumericIndicator result = close.max(5);
        Num expected = series.numFactory().numOf(5);
        assertEquals(expected, result.getValue(0));
    }

    @Test
    public void testAbs() {
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-3)));
        NumericIndicator result = negative.abs();
        assertEquals(series.numFactory().numOf(3), result.getValue(0));
    }

    @Test
    public void testSqrtPositive() {
        NumericIndicator result = close.sqrt(); // sqrt(1) = 1
        assertEquals(series.numFactory().numOf(1), result.getValue(0));
    }

    @Test
    public void testSqrtNegativeProducesNaN() {
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-4)));
        NumericIndicator result = negative.sqrt();
        assertTrue(result.getValue(0).isNaN());
    }

    @Test
    public void testSquared() {
        NumericIndicator result = close.squared(); // 1 * 1 = 1
        assertEquals(series.numFactory().numOf(1), result.getValue(0));
    }

    @Test
    public void testSma() {
        NumericIndicator sma = close.sma(3);
        // SMA of first 3 closes: (1+2+3)/3 = 2
        assertEquals(series.numFactory().numOf(2), sma.getValue(2));
    }

    @Test
    public void testEma() {
        NumericIndicator ema = close.ema(3);
        // EMA is not trivial to compute manually, just verify non‑null and same series
        assertNotNull(ema);
        assertEquals(series, ema.getBarSeries());
    }

    @Test
    public void testStdDev() {
        NumericIndicator std = close.stddev(3);
        assertNotNull(std);
        // standard deviation of (1,2,3) is sqrt(2/3) ≈ 0.81649658
        Num expected = series.numFactory().numOf(Math.sqrt(2.0 / 3.0));
        assertEquals(expected.doubleValue(), std.getValue(2).doubleValue(), 1e-8);
    }

    @Test
    public void testHighest() {
        NumericIndicator highest = close.highest(4);
        // highest of first 4 values (1..4) = 4
        assertEquals(series.numFactory().numOf(4), highest.getValue(3));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest = close.lowest(4);
        // lowest of first 4 values (1..4) = 1
        assertEquals(series.numFactory().numOf(1), lowest.getValue(3));
    }

    @Test
    public void testPrevious() {
        NumericIndicator prev = close.previous();
        // previous of index 2 (value 3) = value at index 1 (2)
        assertEquals(series.numFactory().numOf(2), prev.getValue(2));
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev3 = close.previous(3);
        // index 4 value = 5, 3 periods back = index 1 value = 2
        assertEquals(series.numFactory().numOf(2), prev3.getValue(4));
    }

    @Test
    public void testCrossedOverNumber() {
        // close series: 1,2,3,4,5 – crossing over 3 happens at index 3 (value 4)
        Rule rule = close.crossedOver(3);
        assertTrue(rule.isSatisfied(3));
        // before crossing (index 2) the rule must be false
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderNumber() {
        // crossing under 4 happens at index 3 (value 4 -> 3)
        Rule rule = close.crossedUnder(4);
        assertTrue(rule.isSatisfied(3));
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(3);
        assertTrue(rule.isSatisfied(3)); // value 4 > 3
        assertFalse(rule.isSatisfied(2)); // value 3 not > 3
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(3);
        assertTrue(rule.isSatisfied(1)); // value 2 < 3
        assertFalse(rule.isSatisfied(2)); // value 3 not < 3
    }

    /* ---------------------------------------------------------------------- */
    /* Edge‑case / exception tests                                           */
    /* ---------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testOfNullDelegate() {
        NumericIndicator.of(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPlusNullIndicator() {
        close.plus((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testPlusNullNumber() {
        close.plus((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void testMinusNullNumber() {
        close.minus((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void testMultipliedByNullIndicator() {
        close.multipliedBy((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testDividedByNullNumber() {
        close.dividedBy((Number) null);
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroNumber() {
        close.dividedBy(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmaWithNonPositiveBarCount() {
        close.sma(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmaWithNonPositiveBarCount() {
        close.ema(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStddevWithNonPositiveBarCount() {
        close.stddev(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHighestWithNonPositiveBarCount() {
        close.highest(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowestWithNonPositiveBarCount() {
        close.lowest(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreviousWithNonPositiveBarCount() {
        close.previous(0);
    }

    @Test
    public void testPreviousBeyondSeriesStartReturnsFirstValue() {
        // request a previous value that is older than index 0
        NumericIndicator prev5 = close.previous(5);
        // for every index we should get the first close price (=1)
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(series.numFactory().numOf(1), prev5.getValue(i));
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCrossedOverNullIndicator() {
        close.crossedOver((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCrossedOverNullNumber() {
        close.crossedOver((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCrossedUnderNullNumber() {
        close.crossedUnder((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsGreaterThanNullIndicator() {
        close.isGreaterThan((Indicator<Num>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsLessThanNullNumber() {
        close.isLessThan((Number) null);
    }

    /* ---------------------------------------------------------------------- */
    /* Miscellaneous getters                                                  */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetValueDelegatesCorrectly() {
        assertEquals(series.numFactory().numOf(3), close.getValue(2));
    }

    @Test
    public void testGetCountOfUnstableBarsIsZero() {
        assertEquals(0, close.getCountOfUnstableBars());
    }

    @Test
    public void testGetBarSeriesDelegates() {
        assertSame(series, close.getBarSeries());
    }

    @Test
    public void testToStringDelegates() {
        String expected = new ClosePriceIndicator(series).toString();
        assertEquals(expected, close.toString());
    }
}
###Test END##
```