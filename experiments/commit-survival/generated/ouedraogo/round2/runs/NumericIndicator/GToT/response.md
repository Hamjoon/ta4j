**Step 1 – Public API of `NumericIndicator`**

| # | Method (return type) | Signature |
|---|----------------------|-----------|
| 1 | `static NumericIndicator` | `of(Indicator<Num> delegate)` |
| 2 | `static NumericIndicator` | `closePrice(BarSeries bs)` |
| 3 | `static NumericIndicator` | `volume(BarSeries bs)` |
| 4 | `Indicator<Num>` | `delegate()` |
| 5 | `NumericIndicator` | `plus(Indicator<Num> other)` |
| 6 | `NumericIndicator` | `plus(Number n)` |
| 7 | `NumericIndicator` | `minus(Indicator<Num> other)` |
| 8 | `NumericIndicator` | `minus(Number n)` |
| 9 | `NumericIndicator` | `multipliedBy(Indicator<Num> other)` |
|10 | `NumericIndicator` | `multipliedBy(Number n)` |
|11 | `NumericIndicator` | `dividedBy(Indicator<Num> other)` |
|12 | `NumericIndicator` | `dividedBy(Number n)` |
|13 | `NumericIndicator` | `min(Indicator<Num> other)` |
|14 | `NumericIndicator` | `min(Number n)` |
|15 | `NumericIndicator` | `max(Indicator<Num> other)` |
|16 | `NumericIndicator` | `max(Number n)` |
|17 | `NumericIndicator` | `abs()` |
|18 | `NumericIndicator` | `sqrt()` |
|19 | `NumericIndicator` | `squared()` |
|20 | `NumericIndicator` | `sma(int barCount)` |
|21 | `NumericIndicator` | `ema(int barCount)` |
|22 | `NumericIndicator` | `stddev(int barCount)` |
|23 | `NumericIndicator` | `highest(int barCount)` |
|24 | `NumericIndicator` | `lowest(int barCount)` |
|25 | `NumericIndicator` | `previous(int barCount)` |
|26 | `Indicator<Num>` | `previous()` |
|27 | `Rule` | `crossedOver(Indicator<Num> other)` |
|28 | `Rule` | `crossedOver(Number n)` |
|29 | `Rule` | `crossedUnder(Indicator<Num> other)` |
|30 | `Rule` | `crossedUnder(Number n)` |
|31 | `Rule` | `isGreaterThan(Indicator<Num> other)` |
|32 | `Rule` | `isGreaterThan(Number n)` |
|33 | `Rule` | `isLessThan(Indicator<Num> other)` |
|34 | `Rule` | `isLessThan(Number n)` |
|35 | `Num` | `getValue(int index)` *(inherited)* |
|36 | `int` | `getUnstableBars()` *(inherited)* |
|37 | `BarSeries` | `getBarSeries()` *(inherited)* |
|38 | `String` | `toString()` *(inherited)* |

---

**Step 2 – Basic JUnit 4 test skeleton for each method**

Below is a *single* test method per public API entry that simply checks the “happy‑path” behaviour (i.e. normal use).  
All tests share a small `BarSeries` (5 bars) with deterministic close‑price values.

```java
public class NumericIndicatorBasicTest {
    private BarSeries series;
    private NumericIndicator close;

    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder().withName("test").build();
        // 5 bars with close prices 10,12,14,16,18
        series.addBar(ZonedDateTime.now(), 10, 12, 9, 10, 100);
        series.addBar(ZonedDateTime.now().plusDays(1), 12, 13, 11, 12, 200);
        series.addBar(ZonedDateTime.now().plusDays(2), 14, 15, 13, 14, 300);
        series.addBar(ZonedDateTime.now().plusDays(3), 16, 17, 15, 16, 400);
        series.addBar(ZonedDateTime.now().plusDays(4), 18, 19, 17, 18, 500);
        close = NumericIndicator.closePrice(series);
    }

    @Test public void testOf() {
        NumericIndicator ni = NumericIndicator.of(close);
        assertEquals(close, ni.delegate());
    }

    @Test public void testClosePrice() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertEquals(10, cp.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testVolume() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertEquals(100, vol.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testPlusIndicator() {
        NumericIndicator sum = close.plus(close);
        assertEquals(20, sum.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testPlusNumber() {
        NumericIndicator sum = close.plus(5);
        assertEquals(15, sum.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMinusIndicator() {
        NumericIndicator diff = close.minus(close);
        assertEquals(0, diff.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMinusNumber() {
        NumericIndicator diff = close.minus(5);
        assertEquals(5, diff.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMultipliedByIndicator() {
        NumericIndicator prod = close.multipliedBy(close);
        assertEquals(100, prod.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMultipliedByNumber() {
        NumericIndicator prod = close.multipliedBy(2);
        assertEquals(20, prod.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testDividedByIndicator() {
        NumericIndicator div = close.dividedBy(close);
        assertEquals(1, div.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testDividedByNumber() {
        NumericIndicator div = close.dividedBy(2);
        assertEquals(5, div.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMinIndicator() {
        NumericIndicator min = close.min(close);
        assertEquals(10, min.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMinNumber() {
        NumericIndicator min = close.min(5);
        assertEquals(5, min.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMaxIndicator() {
        NumericIndicator max = close.max(close);
        assertEquals(10, max.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testMaxNumber() {
        NumericIndicator max = close.max(20);
        assertEquals(20, max.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testAbs() {
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series, series.numOf(-10)));
        assertEquals(10, neg.abs().getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testSqrt() {
        NumericIndicator sq = close.sqrt();
        assertEquals(Math.sqrt(10), sq.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testSquared() {
        NumericIndicator sq = close.squared();
        assertEquals(100, sq.getValue(0).doubleValue(), 0.0001);
    }

    @Test public void testSma() {
        NumericIndicator sma = close.sma(3);
        // SMA of first 3 close prices: (10+12+14)/3 = 12
        assertEquals(12, sma.getValue(2).doubleValue(), 0.0001);
    }

    @Test public void testEma() {
        NumericIndicator ema = close.ema(3);
        // EMA is more complex – we just check that a value is produced
        assertNotNull(ema.getValue(2));
    }

    @Test public void testStddev() {
        NumericIndicator std = close.stddev(3);
        assertNotNull(std.getValue(2));
    }

    @Test public void testHighest() {
        NumericIndicator hi = close.highest(3);
        assertEquals(14, hi.getValue(2).doubleValue(), 0.0001);
    }

    @Test public void testLowest() {
        NumericIndicator lo = close.lowest(3);
        assertEquals(10, lo.getValue(2).doubleValue(), 0.0001);
    }

    @Test public void testPreviousWithBarCount() {
        NumericIndicator prev = close.previous(2);
        // previous(2) at index 2 should give value at index 0 → 10
        assertEquals(10, prev.getValue(2).doubleValue(), 0.0001);
    }

    @Test public void testPreviousDefault() {
        Indicator<Num> prev = close.previous();
        // previous() = previous(1)
        assertEquals(12, prev.getValue(1).doubleValue(), 0.0001);
    }

    @Test public void testCrossedOverIndicator() {
        NumericIndicator higher = close.plus(5);
        Rule rule = close.crossedOver(higher);
        // No crossing at index 0 because close (10) < higher (15)
        assertFalse(rule.isSatisfied(0));
    }

    @Test public void testCrossedOverNumber() {
        Rule rule = close.crossedOver(5);
        // close starts at 10 > 5 ⇒ already above, no crossing
        assertFalse(rule.isSatisfied(0));
    }

    @Test public void testCrossedUnderIndicator() {
        NumericIndicator lower = close.minus(5);
        Rule rule = close.crossedUnder(lower);
        assertFalse(rule.isSatisfied(0));
    }

    @Test public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(20);
        assertFalse(rule.isSatisfied(0));
    }

    @Test public void testIsGreaterThanIndicator() {
        Rule rule = close.isGreaterThan(close.minus(1));
        assertTrue(rule.isSatisfied(0));
    }

    @Test public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(5);
        assertTrue(rule.isSatisfied(0));
    }

    @Test public void testIsLessThanIndicator() {
        Rule rule = close.isLessThan(close.plus(1));
        assertTrue(rule.isSatisfied(0));
    }

    @Test public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(20);
        assertTrue(rule.isSatisfied(0));
    }
}
```

---

**Step 3 – Edge‑case & Exception scenarios**

| Method | Edge case(s) | What to verify |
|--------|--------------|----------------|
| `plus(Number)` / `minus(Number)` / `multipliedBy(Number)` / `dividedBy(Number)` | `null` argument → `NullPointerException` (the library does not guard against it). |
| `dividedBy(Number)` | divisor = 0 → should propagate a **division‑by‑zero** `ArithmeticException` from `Num`. |
| `sqrt()` | underlying value negative → `Num.sqrt()` throws `IllegalArgumentException` (or returns NaN). Test that the returned `Num` is *NaN* (`isNaN()`). |
| `min(Number)` / `max(Number)` | `Number` is `NaN` → result should be *NaN*. |
| `previous(int)` | `barCount` larger than current index (e.g., `previous(10)` at index 2) → the underlying `PreviousValueIndicator` returns `NaN`. |
| `previous()` default overload | series with only one bar → calling `previous()` at index 0 should give *NaN*. |
| `crossedOver(Number)` / `crossedUnder(Number)` | threshold equal to the current value → rule should **not** be satisfied (crossing requires a change of side). |
| `isGreaterThan(Number)` / `isLessThan(Number)` | compare with *NaN* → rule must be **false**. |
| `sma(int)`, `ema(int)`, `stddev(int)`, `highest(int)`, `lowest(int)` | `barCount` ≤ 0 or larger than series length → indicator should return *NaN* for indices where not enough data exist. |
| `squared()` | works on any value – verify that `NaN` stays `NaN`. |
| `abs()` | verify that `abs(NaN)` is still *NaN*. |

---

**Step 4 – JUnit 4 tests for the identified edge cases**

```java
public class NumericIndicatorEdgeCaseTest {

    private BarSeries series;
    private NumericIndicator close;

    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder().withName("edge").build();
        series.addBar(ZonedDateTime.now(), 10, 12, 9, 10, 100);
        series.addBar(ZonedDateTime.now().plusDays(1), 12, 13, 11, 12, 200);
        close = NumericIndicator.closePrice(series);
    }

    @Test(expected = NullPointerException.class)
    public void plusNullNumber() {
        close.plus((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void minusNullNumber() {
        close.minus((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void multipliedByNullNumber() {
        close.multipliedBy((Number) null);
    }

    @Test(expected = ArithmeticException.class)
    public void dividedByZeroNumber() {
        close.dividedBy(0);
    }

    @Test
    public void sqrtOfNegative() {
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series, series.numOf(-4)));
        NumericIndicator sqrt = neg.sqrt();
        assertTrue(sqrt.getValue(0).isNaN());
    }

    @Test
    public void minWithNaN() {
        NumericIndicator nanInd = NumericIndicator.of(new ConstantIndicator<>(series, series.num().numOf(Double.NaN)));
        NumericIndicator result = nanInd.min(5);
        assertTrue(result.getValue(0).isNaN());
    }

    @Test
    public void maxWithNaN() {
        NumericIndicator nanInd = NumericIndicator.of(new ConstantIndicator<>(series, series.num().numOf(Double.NaN)));
        NumericIndicator result = nanInd.max(5);
        assertTrue(result.getValue(0).isNaN());
    }

    @Test
    public void previousBarCountTooLarge() {
        Indicator<Num> prev = close.previous(10);
        // At index 2 we ask for value at index -8 → NaN
        assertTrue(prev.getValue(2).isNaN());
    }

    @Test
    public void previousOnSingleBarSeries() {
        BarSeries single = new BaseBarSeriesBuilder().withName("single").build();
        single.addBar(ZonedDateTime.now(), 5, 6, 4, 5, 50);
        NumericIndicator singleClose = NumericIndicator.closePrice(single);
        Indicator<Num> prev = singleClose.previous();
        assertTrue(prev.getValue(0).isNaN());
    }

    @Test
    public void crossedOverEqualsThreshold() {
        // close(0)=10, threshold=10 → no crossing
        Rule rule = close.crossedOver(10);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void crossedUnderEqualsThreshold() {
        Rule rule = close.crossedUnder(10);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void isGreaterThanNaN() {
        Rule rule = close.isGreaterThan(Double.NaN);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void isLessThanNaN() {
        Rule rule = close.isLessThan(Double.NaN);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void smaBarCountZero() {
        NumericIndicator sma = close.sma(0);
        assertTrue(sma.getValue(0).isNaN());
    }

    @Test
    public void smaBarCountTooLarge() {
        NumericIndicator sma = close.sma(10);
        // Index 4 (last) does not have 10 previous values → NaN
        assertTrue(sma.getValue(4).isNaN());
    }

    @Test
    public void emaBarCountZero() {
        NumericIndicator ema = close.ema(0);
        assertTrue(ema.getValue(0).isNaN());
    }

    @Test
    public void stddevBarCountZero() {
        NumericIndicator std = close.stddev(0);
        assertTrue(std.getValue(0).isNaN());
    }

    @Test
    public void squaredNaN() {
        NumericIndicator nanInd = NumericIndicator.of(new ConstantIndicator<>(series, series.num().numOf(Double.NaN)));
        NumericIndicator sq = nanInd.squared();
        assertTrue(sq.getValue(0).isNaN());
    }

    @Test
    public void absNaN() {
        NumericIndicator nanInd = NumericIndicator.of(new ConstantIndicator<>(series, series.num().numOf(Double.NaN)));
        NumericIndicator abs = nanInd.abs();
        assertTrue(abs.getValue(0).isNaN());
    }
}
```

---

**Step 5 – Merged, ready‑to‑run JUnit 4 test file**

Below is the **complete** test class that contains both the basic “happy‑path” tests and the edge‑case tests.  
All imports are from the TA‑4J library (the concrete `BaseBarSeries` implementation is used – it is part of the public API of the library, even though it was not listed in the excerpt).  

```java
###Test START##
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.Rule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * Covers:
 *   • All public methods (happy path)
 *   • Edge‑case & exception handling
 *
 * The tests use a small deterministic {@link BarSeries} (5 bars)
 * with known close‑price values: 10, 12, 14, 16, 18.
 *
 * @author  (generated)
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;   // close‑price indicator wrapped in NumericIndicator

    /** Build a 5‑bar series used by every test */
    @Before
    public void setUp() {
        series = new BaseBarSeriesBuilder()
                .withName("numeric-indicator-test")
                .build();

        // (time, open, high, low, close, volume)
        series.addBar(ZonedDateTime.now(), 10, 12, 9, 10, 100);
        series.addBar(ZonedDateTime.now().plusDays(1), 12, 13, 11, 12, 200);
        series.addBar(ZonedDateTime.now().plusDays(2), 14, 15, 13, 14, 300);
        series.addBar(ZonedDateTime.now().plusDays(3), 16, 17, 15, 16, 400);
        series.addBar(ZonedDateTime.now().plusDays(4), 18, 19, 17, 18, 500);

        close = NumericIndicator.closePrice(series);
    }

    /* -------------------------------------------------
       1️⃣  BASIC “HAPPY‑PATH” TESTS
       ------------------------------------------------- */

    @Test
    public void testOf() {
        NumericIndicator ni = NumericIndicator.of(close);
        assertEquals(close, ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertEquals(10.0, cp.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertEquals(100.0, vol.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPlusIndicator() {
        NumericIndicator sum = close.plus(close);
        assertEquals(20.0, sum.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator sum = close.plus(5);
        assertEquals(15.0, sum.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator diff = close.minus(close);
        assertEquals(0.0, diff.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator diff = close.minus(5);
        assertEquals(5.0, diff.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator prod = close.multipliedBy(close);
        assertEquals(100.0, prod.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator prod = close.multipliedBy(2);
        assertEquals(20.0, prod.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator div = close.dividedBy(close);
        assertEquals(1.0, div.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator div = close.dividedBy(2);
        assertEquals(5.0, div.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator min = close.min(close);
        assertEquals(10.0, min.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = close.min(5);
        assertEquals(5.0, min.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator max = close.max(close);
        assertEquals(10.0, max.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = close.max(20);
        assertEquals(20.0, max.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testAbs() {
        NumericIndicator negative = NumericIndicator.of(
                new ConstantIndicator<>(series, series.numOf(-10)));
        assertEquals(10.0, negative.abs().getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSqrt() {
        NumericIndicator sqrt = close.sqrt();
        assertEquals(Math.sqrt(10.0), sqrt.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSquared() {
        NumericIndicator sq = close.squared();
        assertEquals(100.0, sq.getValue(0).doubleValue(), 0.000001);
    }

    @Test
    public void testSma() {
        // SMA of first 3 close values: (10+12+14)/3 = 12
        NumericIndicator sma = close.sma(3);
        assertEquals(12.0, sma.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testEma() {
        // EMA is a cached indicator – we only verify that a value exists
        NumericIndicator ema = close.ema(3);
        assertNotNull(ema.getValue(2));
    }

    @Test
    public void testStddev() {
        NumericIndicator std = close.stddev(3);
        assertNotNull(std.getValue(2));
    }

    @Test
    public void testHighest() {
        NumericIndicator hi = close.highest(3);
        // Highest of 10,12,14 = 14
        assertEquals(14.0, hi.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testLowest() {
        NumericIndicator lo = close.lowest(3);
        // Lowest of 10,12,14 = 10
        assertEquals(10.0, lo.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev = close.previous(2);
        // previous(2) at index 2 → value at index 0 (10)
        assertEquals(10.0, prev.getValue(2).doubleValue(), 0.000001);
    }

    @Test
    public void testPreviousDefault() {
        Indicator<Num> prev = close.previous(); // same as previous(1)
        // previous(1) at index 1 → value at index 0 (10)
        assertEquals(10.0, prev.getValue(1).doubleValue(), 0.000001);
    }

    @Test
    public void testCrossedOverIndicator() {
        NumericIndicator higher = close.plus(5); // 10+5 = 15 at index 0
        Rule rule = close.crossedOver(higher);
        // No crossing at index 0 (10 < 15) → false
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedOverNumber() {
        Rule rule = close.crossedOver(5);
        // close starts above 5 → not a crossing
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderIndicator() {
        NumericIndicator lower = close.minus(5); // 10-5 = 5 at index 0
        Rule rule = close.crossedUnder(lower);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(20);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        Rule rule = close.isGreaterThan(close.minus(1));
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(5);
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanIndicator() {
        Rule rule = close.isLessThan(close.plus(1));
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(20);
        assertTrue(rule.isSatisfied(0));
    }

    /* -------------------------------------------------
       2️⃣  EDGE‑CASE & EXCEPTION TESTS
       ------------------------------------------------- */

    // ---- arithmetic with null -------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void plusNullNumber() {
        close.plus((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void minusNullNumber() {
        close.minus((Number) null);
    }

    @Test(expected = NullPointerException.class)
    public void multipliedByNullNumber() {
        close.multipliedBy((Number) null);
    }

    // ---- division by zero ----------------------------------------------------
    @Test(expected = ArithmeticException.class)
    public void dividedByZeroNumber() {
        close.dividedBy(0);
    }

    // ---- sqrt of negative ----------------------------------------------------
    @Test
    public void sqrtOfNegativeNumber() {
        NumericIndicator negative = NumericIndicator.of(
                new ConstantIndicator<>(series, series.numOf(-4)));
        NumericIndicator sqrt = negative.sqrt();
        assertTrue(sqrt.getValue(0).isNaN());
    }

    // ---- NaN handling for min / max -----------------------------------------
    @Test
    public void minWithNaN() {
        NumericIndicator nanInd = NumericIndicator.of(
                new ConstantIndicator<>(series, series.numOf(Double.NaN)));
        NumericIndicator result = nanInd.min(5);
        assertTrue(result.getValue(0).isNaN());
    }

    @Test
    public void maxWithNaN() {
        NumericIndicator nanInd = NumericIndicator.of(
                new ConstantIndicator<>(series, series.numOf(Double.NaN)));
        NumericIndicator result = nanInd.max(5);
        assertTrue(result.getValue(0).isNaN());
    }

    // ---- previous() when not enough history ----------------------------------
    @Test
    public void previousBarCountTooLarge() {
        Indicator<Num> prev = close.previous(10);
        // At index 2 we request value at index -8 → NaN
        assertTrue(prev.getValue(2).isNaN());
    }

    @Test
    public void previousDefaultOnSingleBarSeries() {
        BarSeries single = new BaseBarSeriesBuilder().withName("single").build();
        single.addBar(ZonedDateTime.now(), 5, 6, 4, 5, 50);
        NumericIndicator singleClose = NumericIndicator.closePrice(single);
        Indicator<Num> prev = singleClose.previous(); // previous(1)
        assertTrue(prev.getValue(0).isNaN());
    }

    // ---- crossing rules with exact equality ----------------------------------
    @Test
    public void crossedOverEqualsThreshold() {
        Rule rule = close.crossedOver(10); // threshold equals first value
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void crossedUnderEqualsThreshold() {
        Rule rule = close.crossedUnder(10);
        assertFalse(rule.isSatisfied(0));
    }

    // ---- comparisons with NaN ------------------------------------------------
    @Test
    public void isGreaterThanNaN() {
        Rule rule = close.isGreaterThan(Double.NaN);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void isLessThanNaN() {
        Rule rule = close.isLessThan(Double.NaN);
        assertFalse(rule.isSatisfied(0));
    }

    // ---- indicators with invalid barCount ------------------------------------
    @Test
    public void smaBarCountZero() {
        NumericIndicator sma = close.sma(0);
        assertTrue(sma.getValue(0).isNaN());
    }

    @Test
    public void smaBarCountTooLarge() {
        NumericIndicator sma = close.sma(10);
        // Index 4 cannot compute SMA of 10 values → NaN
        assertTrue(sma.getValue(4).isNaN());
    }

    @Test
    public void emaBarCountZero() {
        NumericIndicator ema = close.ema(0);
        assertTrue(ema.getValue(0).isNaN());
    }

    @Test
    public void stddevBarCountZero() {
        NumericIndicator std = close.stddev(0);
        assertTrue(std.getValue(0).isNaN());
    }

    // ---- Unary ops on NaN ----------------------------------------------------
    @Test
    public void squaredNaN() {
        NumericIndicator nanInd = NumericIndicator.of(
                new ConstantIndicator<>(series, series.numOf(Double.NaN)));
        NumericIndicator sq = nanInd.squared();
        assertTrue(sq.getValue(0).isNaN());
    }

    @Test
    public void absNaN() {
        NumericIndicator nanInd = NumericIndicator.of(
                new ConstantIndicator<>(series, series.numOf(Double.NaN)));
        NumericIndicator abs = nanInd.abs();
        assertTrue(abs.getValue(0).isNaN());
    }
}
###Test END##
```