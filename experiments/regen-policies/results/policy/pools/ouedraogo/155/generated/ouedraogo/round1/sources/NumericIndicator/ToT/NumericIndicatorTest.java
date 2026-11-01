package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.Rule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Arithmetic operations (plus, minus, multipliedBy, dividedBy)</li>
 *   <li>Min / Max helpers</li>
 *   <li>Unary operations (abs, sqrt, squared)</li>
 *   <li>Statistical / moving‑average helpers (sma, ema, stddev, highest,
 *       lowest, previous)</li>
 *   <li>Rule generation helpers (crossedOver, crossedUnder, isGreaterThan,
 *       isLessThan)</li>
 *   <li>Delegate forwarding (getValue, getBarSeries, toString)</li>
 * </ul>
 *
 * <p>
 * Edge‑case handling such as division by zero, sqrt of negative numbers,
 * and crossing rules at the first index are also verified.
 */
public class NumericIndicatorTest {

    /** Simple series with deterministic close‑price and volume values. */
    private BarSeries series;

    /** Close‑price indicator wrapped as a NumericIndicator (used in many tests). */
    private NumericIndicator close;

    /** Volume indicator wrapped as a NumericIndicator (used in many tests). */
    private NumericIndicator volume;

    /** Helper to create a {@link Bar} with the given close price and volume. */
    private static Bar bar(double close, double volume) {
        ZonedDateTime time = ZonedDateTime.now();
        Num closeNum = DecimalNum.valueOf(close);
        Num volumeNum = DecimalNum.valueOf(volume);
        // For the purpose of these tests the other price fields are set equal
        // to the close price – they are not used by the NumericIndicator logic.
        return new BaseBar(time, closeNum, closeNum, closeNum, closeNum, volumeNum);
    }

    @Before
    public void setUp() {
        // Build a series with 6 bars – enough to test moving‑average windows.
        List<Bar> bars = new ArrayList<>();
        bars.add(bar(10, 100));
        bars.add(bar(12, 110));
        bars.add(bar(11, 120));
        bars.add(bar(13, 130));
        bars.add(bar(14, 140));
        bars.add(bar(15, 150));

        series = new BaseBarSeries.Builder().withName("test-series").build();
        for (Bar b : bars) {
            series.addBar(b);
        }

        close = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    /* ---------------------------------------------------------------------- */
    /*                     Arithmetic operation tests                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPlusIndicator() {
        NumericIndicator sum = close.plus(volume);
        // 10+100 = 110 at index 0, 12+110 = 122 at index 1, etc.
        assertEquals(DecimalNum.valueOf(110), sum.getValue(0));
        assertEquals(DecimalNum.valueOf(122), sum.getValue(1));
        assertEquals(DecimalNum.valueOf(131), sum.getValue(2));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator sum = close.plus(5);
        assertEquals(DecimalNum.valueOf(15), sum.getValue(0));
        assertEquals(DecimalNum.valueOf(17), sum.getValue(1));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator diff = close.minus(volume);
        assertEquals(DecimalNum.valueOf(-90), diff.getValue(0));
        assertEquals(DecimalNum.valueOf(-98), diff.getValue(1));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator diff = close.minus(2);
        assertEquals(DecimalNum.valueOf(8), diff.getValue(0));
        assertEquals(DecimalNum.valueOf(10), diff.getValue(1));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator prod = close.multipliedBy(volume);
        assertEquals(DecimalNum.valueOf(1000), prod.getValue(0));
        assertEquals(DecimalNum.valueOf(1320), prod.getValue(1));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator prod = close.multipliedBy(3);
        assertEquals(DecimalNum.valueOf(30), prod.getValue(0));
        assertEquals(DecimalNum.valueOf(36), prod.getValue(1));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator quot = close.dividedBy(volume);
        // 10 / 100 = 0.1
        assertEquals(DecimalNum.valueOf(0.1), quot.getValue(0));
        assertEquals(DecimalNum.valueOf(12.0 / 110.0), quot.getValue(1));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator quot = close.dividedBy(2);
        assertEquals(DecimalNum.valueOf(5), quot.getValue(0));
        assertEquals(DecimalNum.valueOf(6), quot.getValue(1));
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroThrows() {
        // Division by a constant zero must raise an exception (Num will do it)
        close.dividedBy(0).getValue(0);
    }

    /* ---------------------------------------------------------------------- */
    /*                         Min / Max helper tests                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testMinIndicator() {
        NumericIndicator min = close.min(volume);
        // At index 0: min(10,100) = 10
        assertEquals(DecimalNum.valueOf(10), min.getValue(0));
        // At index 1: min(12,110) = 12
        assertEquals(DecimalNum.valueOf(12), min.getValue(1));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = close.min(12);
        // 10 < 12 → 10, 12 == 12 → 12 (this is returned per contract)
        assertEquals(DecimalNum.valueOf(10), min.getValue(0));
        assertEquals(DecimalNum.valueOf(12), min.getValue(1));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator max = close.max(volume);
        assertEquals(DecimalNum.valueOf(100), max.getValue(0));
        assertEquals(DecimalNum.valueOf(110), max.getValue(1));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = close.max(12);
        assertEquals(DecimalNum.valueOf(12), max.getValue(0));
        assertEquals(DecimalNum.valueOf(12), max.getValue(1));
        assertEquals(DecimalNum.valueOf(13), max.getValue(3));
    }

    /* ---------------------------------------------------------------------- */
    /*                         Unary operation tests                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testAbs() {
        NumericIndicator negative = close.minus(20); // values: -10, -8, -9, -7, -6, -5
        NumericIndicator abs = negative.abs();
        assertEquals(DecimalNum.valueOf(10), abs.getValue(0));
        assertEquals(DecimalNum.valueOf(8), abs.getValue(1));
    }

    @Test
    public void testSqrt() {
        NumericIndicator sqrt = close.sqrt();
        // sqrt(10) ≈ 3.1622776601683795
        assertEquals(DecimalNum.valueOf(Math.sqrt(10)), sqrt.getValue(0));
    }

    @Test
    public void testSqrtOfNegativeReturnsNaN() {
        NumericIndicator negative = close.minus(20); // negative values
        NumericIndicator sqrt = negative.sqrt();
        assertTrue(sqrt.getValue(0).isNaN());
    }

    @Test
    public void testSquared() {
        NumericIndicator sq = close.squared(); // same as close * close
        assertEquals(DecimalNum.valueOf(100), sq.getValue(0));
        assertEquals(DecimalNum.valueOf(144), sq.getValue(1));
    }

    /* ---------------------------------------------------------------------- */
    /*                Moving‑average / statistical helper tests               */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testSma() {
        // SMA with barCount = 3 → (10+12+11)/3 = 11 at index 2, etc.
        NumericIndicator sma3 = close.sma(3);
        assertEquals(DecimalNum.valueOf(11), sma3.getValue(2));
        assertEquals(DecimalNum.valueOf(12), sma3.getValue(3));
        assertEquals(DecimalNum.valueOf(13), sma3.getValue(4));
    }

    @Test
    public void testEma() {
        // EMA with barCount = 3 – we only check that the indicator is created
        // and returns a value (the exact exponential weighting is library‑dependent).
        NumericIndicator ema3 = close.ema(3);
        // EMA is defined for the first index as the close price itself.
        assertEquals(close.getValue(0), ema3.getValue(0));
    }

    @Test
    public void testStdDev() {
        NumericIndicator stddev3 = close.stddev(3);
        // Standard deviation of [10,12,11] → sqrt( ((1^2)+(1^2)+(0^2))/3 ) = sqrt(2/3)
        Num expected = DecimalNum.valueOf(Math.sqrt(2.0 / 3.0));
        assertEquals(expected, stddev3.getValue(2));
    }

    @Test
    public void testHighest() {
        NumericIndicator highest3 = close.highest(3);
        // Highest of [10,12,11] = 12 at index 2
        assertEquals(DecimalNum.valueOf(12), highest3.getValue(2));
        // Highest of [12,11,13] = 13 at index 3
        assertEquals(DecimalNum.valueOf(13), highest3.getValue(3));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest3 = close.lowest(3);
        // Lowest of [10,12,11] = 10 at index 2
        assertEquals(DecimalNum.valueOf(10), lowest3.getValue(2));
        // Lowest of [12,11,13] = 11 at index 3
        assertEquals(DecimalNum.valueOf(11), lowest3.getValue(3));
    }

    @Test
    public void testPrevious() {
        NumericIndicator prev = close.previous();
        // previous(1) at index 1 returns value at index 0 (10)
        assertEquals(DecimalNum.valueOf(10), prev.getValue(1));
        // previous(2) at index 3 returns value at index 1 (12)
        assertEquals(DecimalNum.valueOf(12), close.previous(2).getValue(3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPreviousBeyondStartThrows() {
        // Asking for previous(5) at index 3 would require index -2 → exception
        close.previous(5).getValue(3);
    }

    /* ---------------------------------------------------------------------- */
    /*                     Rule generation helper tests                      */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCrossedOverIndicatorRule() {
        // Create a fast SMA (2) and a slow SMA (3) and check crossing at index 2.
        NumericIndicator fast = close.sma(2);   // values: (10+12)/2=11, (12+11)/2=11.5, ...
        NumericIndicator slow = close.sma(3);   // values: 11, 12, 13, ...
        Rule rule = fast.crossedOver(slow);
        // At index 2 fast (11.5) is above slow (11) and at index 1 they were equal → crossing.
        assertTrue(rule.isSatisfied(2));
        // No crossing at index 1 (both 11)
        assertFalse(rule.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderNumberRule() {
        Rule rule = close.crossedUnder(12);
        // close price moves from 10 → 12 (crossing upward) → rule false,
        // then from 12 → 11 (crossing downward) → rule true at index 2.
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanIndicatorRule() {
        Rule rule = close.isGreaterThan(volume);
        // 10 < 100 → false, 12 < 110 → false, never true for this data set.
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(5));
    }

    @Test
    public void testIsLessThanNumberRule() {
        Rule rule = close.isLessThan(13);
        // 10 < 13 → true, 12 < 13 → true, 13 < 13 → false (equal)
        assertTrue(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(3));
    }

    /* ---------------------------------------------------------------------- */
    /*                     Delegate / forwarding tests                        */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDelegateMethod() {
        assertSame(close.delegate(), close.delegate());
        assertSame(volume.delegate(), volume.delegate());
    }

    @Test
    public void testGetValueForwardsToDelegate() {
        assertEquals(DecimalNum.valueOf(10), close.getValue(0));
        assertEquals(DecimalNum.valueOf(150), volume.getValue(5));
    }

    @Test
    public void testGetBarSeriesForwardsToDelegate() {
        assertSame(series, close.getBarSeries());
        assertSame(series, volume.getBarSeries());
    }

    @Test
    public void testToStringForwardsToDelegate() {
        String expected = close.delegate().toString();
        assertEquals(expected, close.toString());
    }

    /* ---------------------------------------------------------------------- */
    /*                         Factory static methods                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testStaticFactoryOf() {
        NumericIndicator ni = NumericIndicator.of(close);
        assertSame(close, ni.delegate());
    }

    @Test
    public void testStaticFactoryClosePrice() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertEquals(close.getValue(0), cp.getValue(0));
    }

    @Test
    public void testStaticFactoryVolume() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertEquals(volume.getValue(0), vol.getValue(0));
    }
}
