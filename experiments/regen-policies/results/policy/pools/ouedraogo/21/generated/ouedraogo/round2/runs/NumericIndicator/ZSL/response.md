###Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Arithmetic fluent methods (plus, minus, multipliedBy, dividedBy)</li>
 *   <li>Min / Max helpers</li>
 *   <li>Unary helpers (abs, sqrt, squared)</li>
 *   <li>Indicator factories (sma, ema, stddev, highest, lowest, previous)</li>
 *   <li>Rule factories (crossedOver, crossedUnder, isGreaterThan, isLessThan)</li>
 * </ul>
 *
 * A {@link BaseBarSeries} together with its {@link Num} factory is used
 * throughout the tests – this is the concrete {@link BarSeries} implementation
 * shipped with ta4j.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private Num n0, n1, n2, n3, n4, n5;

    @Before
    public void setUp() {
        // a series with 6 bars – the actual bars are not used by the ConstantIndicator,
        // we only need a series to provide a NumFactory.
        series = new BaseBarSeries();

        // convenient Num constants
        n0 = series.numFactory().numOf(0);
        n1 = series.numFactory().numOf(1);
        n2 = series.numFactory().numOf(2);
        n3 = series.numFactory().numOf(3);
        n4 = series.numFactory().numOf(4);
        n5 = series.numFactory().numOf(5);
    }

    /** Helper to create a ConstantIndicator wrapped into a NumericIndicator. */
    private NumericIndicator constNum(double value) {
        return NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(value)));
    }

    /** Helper to create a simple Indicator that returns the index as Num. */
    private NumericIndicator indexIndicator() {
        return NumericIndicator.of((index) -> series.numFactory().numOf(index));
    }

    /* --------------------------------------------------------------------- *
     *  Arithmetic operations
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusIndicatorAndNumber() {
        NumericIndicator a = constNum(2);
        NumericIndicator b = constNum(3);
        NumericIndicator result1 = a.plus(b);
        NumericIndicator result2 = a.plus(5); // uses Number overload

        assertEquals(series.numFactory().numOf(5), result1.getValue(0));
        assertEquals(series.numFactory().numOf(7), result2.getValue(0));
    }

    @Test
    public void testMinusIndicatorAndNumber() {
        NumericIndicator a = constNum(10);
        NumericIndicator b = constNum(4);
        NumericIndicator result1 = a.minus(b);
        NumericIndicator result2 = a.minus(3); // Number overload

        assertEquals(series.numFactory().numOf(6), result1.getValue(0));
        assertEquals(series.numFactory().numOf(7), result2.getValue(0));
    }

    @Test
    public void testMultipliedByIndicatorAndNumber() {
        NumericIndicator a = constNum(2);
        NumericIndicator b = constNum(5);
        NumericIndicator result1 = a.multipliedBy(b);
        NumericIndicator result2 = a.multipliedBy(4);

        assertEquals(series.numFactory().numOf(10), result1.getValue(0));
        assertEquals(series.numFactory().numOf(8), result2.getValue(0));
    }

    @Test
    public void testDividedByIndicatorAndNumber() {
        NumericIndicator a = constNum(20);
        NumericIndicator b = constNum(4);
        NumericIndicator result1 = a.dividedBy(b);
        NumericIndicator result2 = a.dividedBy(2);

        assertEquals(series.numFactory().numOf(5), result1.getValue(0));
        assertEquals(series.numFactory().numOf(10), result2.getValue(0));
    }

    /* --------------------------------------------------------------------- *
     *  Min / Max helpers
     * --------------------------------------------------------------------- */

    @Test
    public void testMinAndMaxWithIndicatorAndNumber() {
        NumericIndicator a = constNum(7);
        NumericIndicator b = constNum(3);
        NumericIndicator c = constNum(9);

        // min / max with other indicators
        assertEquals(series.numFactory().numOf(3), a.min(b).getValue(0));
        assertEquals(series.numFactory().numOf(7), a.max(b).getValue(0));

        // min / max with Number
        assertEquals(series.numFactory().numOf(7), a.min(8).getValue(0));
        assertEquals(series.numFactory().numOf(9), a.max(8).getValue(0));
    }

    /* --------------------------------------------------------------------- *
     *  Unary helpers
     * --------------------------------------------------------------------- */

    @Test
    public void testAbsAndSqrtAndSquared() {
        NumericIndicator negative = constNum(-4);
        NumericIndicator positive = constNum(9);

        // abs
        assertEquals(series.numFactory().numOf(4), negative.abs().getValue(0));

        // sqrt
        assertEquals(series.numFactory().numOf(3), positive.sqrt().getValue(0));

        // squared (should be the same as multipliedBy itself)
        NumericIndicator squared = positive.squared();
        assertEquals(series.numFactory().numOf(81), squared.getValue(0));
    }

    /* --------------------------------------------------------------------- *
     *  Indicator factories
     * --------------------------------------------------------------------- */

    @Test
    public void testSma() {
        // series: 1,2,3,4,5
        NumericIndicator idx = indexIndicator(); // values 0..5
        NumericIndicator sma3 = idx.sma(3); // SMA over 3 bars

        // SMA at index 2 -> average of 0,1,2 = 1
        assertEquals(series.numFactory().numOf(1), sma3.getValue(2));

        // SMA at index 4 -> average of 2,3,4 = 3
        assertEquals(series.numFactory().numOf(3), sma3.getValue(4));
    }

    @Test
    public void testEma() {
        // Use a deterministic series: 1,2,3,4,5
        NumericIndicator idx = indexIndicator(); // 0..5
        NumericIndicator ema3 = idx.ema(3); // EMA with barCount 3

        // EMA initialization: first value equals first indicator value (0)
        assertEquals(series.numFactory().numOf(0), ema3.getValue(0));

        // Subsequent values can be compared against a manually computed EMA
        // EMA formula: EMA_t = (value_t * k) + (EMA_{t-1} * (1 - k))
        // where k = 2 / (barCount + 1) = 0.5 for barCount=3
        double k = 2.0 / (3 + 1);
        double emaPrev = 0; // EMA_0
        for (int i = 1; i <= 4; i++) {
            double value = i; // index indicator returns i
            emaPrev = value * k + emaPrev * (1 - k);
            Num expected = series.numFactory().numOf(emaPrev);
            assertEquals("EMA at index " + i, expected, ema3.getValue(i));
        }
    }

    @Test
    public void testStandardDeviation() {
        // series: 0,1,2,3,4
        NumericIndicator idx = indexIndicator();
        NumericIndicator stddev3 = idx.stddev(3);

        // For index 2 (values 0,1,2) stddev = sqrt( ((0-1)^2 + (1-1)^2 + (2-1)^2) / 3 )
        // variance = (1 + 0 + 1) / 3 = 2/3 ; stddev = sqrt(2/3)
        Num expected = series.numFactory()
                .numOf(Math.sqrt(2.0 / 3.0));
        assertEquals(expected.doubleValue(), stddev3.getValue(2).doubleValue(), 1e-9);
    }

    @Test
    public void testHighestAndLowest() {
        // series: 5,1,3,7,2
        NumericIndicator custom = NumericIndicator.of((index) -> {
            switch (index) {
                case 0: return series.numFactory().numOf(5);
                case 1: return series.numFactory().numOf(1);
                case 2: return series.numFactory().numOf(3);
                case 3: return series.numFactory().numOf(7);
                case 4: return series.numFactory().numOf(2);
                default: return series.numFactory().numOf(0);
            }
        });

        NumericIndicator highest3 = custom.highest(3);
        NumericIndicator lowest3 = custom.lowest(3);

        // highest over indices 1..3 (1,3,7) -> 7
        assertEquals(series.numFactory().numOf(7), highest3.getValue(3));

        // lowest over indices 2..4 (3,7,2) -> 2
        assertEquals(series.numFactory().numOf(2), lowest3.getValue(4));
    }

    @Test
    public void testPrevious() {
        NumericIndicator idx = indexIndicator();
        NumericIndicator prev = idx.previous(2); // value two steps back

        // At index 3 -> previous(2) should be value at index 1 (=1)
        assertEquals(series.numFactory().numOf(1), prev.getValue(3));

        // The convenience method previous() uses barCount = 1
        NumericIndicator prev1 = idx.previous();
        assertEquals(series.numFactory().numOf(2), prev1.getValue(3));
    }

    /* --------------------------------------------------------------------- *
     *  Rule factories
     * --------------------------------------------------------------------- */

    @Test
    public void testCrossedOverAndCrossedUnder() {
        // Build two indicators:
        //   a = index (0,1,2,3,4)
        //   b = index shifted by -2 ( -2,-1,0,1,2 )
        NumericIndicator a = indexIndicator();
        NumericIndicator b = NumericIndicator.of((idx) -> series.numFactory().numOf(idx - 2));

        // a crosses over b at index 2 (a=2, b=0) and stays above afterwards.
        Rule crossedOverRule = a.crossedOver(b);
        Rule crossedUnderRule = a.crossedUnder(b);

        // Before crossing (index 1) -> false
        assertFalse(crossedOverRule.isSatisfied(1));
        assertFalse(crossedUnderRule.isSatisfied(1));

        // At crossing (index 2) -> true for crossedOver, false for crossedUnder
        assertTrue(crossedOverRule.isSatisfied(2));
        assertFalse(crossedUnderRule.isSatisfied(2));

        // After crossing (index 3) -> false for both (no new crossing)
        assertFalse(crossedOverRule.isSatisfied(3));
        assertFalse(crossedUnderRule.isSatisfied(3));
    }

    @Test
    public void testIsGreaterThanAndIsLessThan() {
        NumericIndicator a = constNum(5);
        NumericIndicator b = constNum(3);
        NumericIndicator c = constNum(5);

        // Indicator vs Indicator
        Rule greaterRule = a.isGreaterThan(b);
        Rule lessRule = b.isLessThan(a);
        assertTrue(greaterRule.isSatisfied(0));
        assertTrue(lessRule.isSatisfied(0));

        // Equality should be false for strict comparisons
        assertFalse(a.isGreaterThan(c).isSatisfied(0));
        assertFalse(a.isLessThan(c).isSatisfied(0));

        // Indicator vs Number overloads
        assertTrue(a.isGreaterThan(4).isSatisfied(0));
        assertFalse(a.isLessThan(4).isSatisfied(0));
    }

    /* --------------------------------------------------------------------- *
     *  Miscellaneous getters
     * --------------------------------------------------------------------- */

    @Test
    public void testDelegateAndToString() {
        NumericIndicator ind = constNum(42);
        assertSame(ind.delegate(), ind.delegate());
        assertEquals(ind.delegate().toString(), ind.toString());
    }
}
```
###Test END##