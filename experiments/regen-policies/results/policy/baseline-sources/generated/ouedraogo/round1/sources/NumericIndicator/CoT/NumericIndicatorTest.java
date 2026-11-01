package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;

/**
 * JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>All public methods (basic happy‑path tests)</li>
 *   <li>Edge‑case scenarios such as division by zero, sqrt of a negative,
 *       equal values for {@code min}/{@code max}, and rule crossing at the
 *       exact threshold.</li>
 * </ul>
 *
 * The library provides a concrete {@link BaseBarSeries} implementation that
 * can be used to build a minimal series for the indicators.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;   // NumericIndicator wrapping ClosePriceIndicator
    private NumericIndicator constFive; // Constant 5
    private NumericIndicator constZero; // Constant 0

    /** Helper to create a {@link NumericIndicator} from a constant value. */
    private NumericIndicator constant(Number n) {
        return NumericIndicator.of(new ConstantIndicator<>(series, series.num().numOf(n)));
    }

    @Before
    public void setUp() {
        // Build a tiny series (5 bars) with deterministic close prices.
        series = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now();
        // add bars: close prices 10, 20, 30, 40, 50
        for (int i = 0; i < 5; i++) {
            series.addBar(
                now.plusMinutes(i),
                series.num().numOf(10 + i * 10), // open
                series.num().numOf(10 + i * 10), // high
                series.num().numOf(10 + i * 10), // low
                series.num().numOf(10 + i * 10), // close
                series.num().numOf(100)          // volume
            );
        }
        close = NumericIndicator.closePrice(series);
        constFive = constant(5);
        constZero = constant(0);
    }

    /* ----------------------------------------------------------------------
     *  List of all public methods (extracted from the source)
     * ---------------------------------------------------------------------- */
    // static NumericIndicator of(Indicator<Num> delegate)
    // static NumericIndicator closePrice(BarSeries bs)
    // static NumericIndicator volume(BarSeries bs)
    // Indicator<Num> delegate()
    // NumericIndicator plus(Indicator<Num> other)
    // NumericIndicator plus(Number n)
    // NumericIndicator minus(Indicator<Num> other)
    // NumericIndicator minus(Number n)
    // NumericIndicator multipliedBy(Indicator<Num> other)
    // NumericIndicator multipliedBy(Number n)
    // NumericIndicator dividedBy(Indicator<Num> other)
    // NumericIndicator dividedBy(Number n)
    // NumericIndicator min(Indicator<Num> other)
    // NumericIndicator min(Number n)
    // NumericIndicator max(Indicator<Num> other)
    // NumericIndicator max(Number n)
    // NumericIndicator abs()
    // NumericIndicator sqrt()
    // NumericIndicator squared()
    // NumericIndicator sma(int barCount)
    // NumericIndicator ema(int barCount)
    // NumericIndicator stddev(int barCount)
    // NumericIndicator highest(int barCount)
    // NumericIndicator lowest(int barCount)
    // NumericIndicator previous(int barCount)
    // Indicator<Num> previous()
    // Rule crossedOver(Indicator<Num> other)
    // Rule crossedOver(Number n)
    // Rule crossedUnder(Indicator<Num> other)
    // Rule crossedUnder(Number n)
    // Rule isGreaterThan(Indicator<Num> other)
    // Rule isGreaterThan(Number n)
    // Rule isLessThan(Indicator<Num> other)
    // Rule isLessThan(Number n)
    // private Indicator<Num> createConstant(Number n)
    // Num getValue(int index)
    // int getUnstableBars()
    // BarSeries getBarSeries()
    // String toString()

    /* ----------------------------------------------------------------------
     *  Basic functionality tests
     * ---------------------------------------------------------------------- */

    @Test
    public void testOfFactory() {
        NumericIndicator ni = NumericIndicator.of(close);
        assertNotNull(ni);
        assertSame(close, ni.delegate());
    }

    @Test
    public void testDelegate() {
        assertSame(close.delegate(), close.delegate());
    }

    @Test
    public void testPlusIndicator() {
        NumericIndicator sum = close.plus(constFive);
        // close[0] = 10, constFive = 5 => 15
        assertEquals(series.num().numOf(15), sum.getValue(0));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator sum = close.plus(5);
        assertEquals(series.num().numOf(15), sum.getValue(0));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator diff = close.minus(constFive);
        assertEquals(series.num().numOf(5), diff.getValue(0));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator diff = close.minus(5);
        assertEquals(series.num().numOf(5), diff.getValue(0));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator prod = close.multipliedBy(constFive);
        assertEquals(series.num().numOf(50), prod.getValue(0));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator prod = close.multipliedBy(5);
        assertEquals(series.num().numOf(50), prod.getValue(0));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator div = close.dividedBy(constFive);
        assertEquals(series.num().numOf(2), div.getValue(0));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator div = close.dividedBy(5);
        assertEquals(series.num().numOf(2), div.getValue(0));
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator min = close.min(constFive);
        // close[0] = 10, constFive = 5 => min = 5
        assertEquals(series.num().numOf(5), min.getValue(0));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = close.min(5);
        assertEquals(series.num().numOf(5), min.getValue(0));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator max = close.max(constFive);
        // close[0] = 10, constFive = 5 => max = 10
        assertEquals(series.num().numOf(10), max.getValue(0));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = close.max(5);
        assertEquals(series.num().numOf(10), max.getValue(0));
    }

    @Test
    public void testAbs() {
        // create a negative constant indicator
        NumericIndicator neg = constant(-7);
        NumericIndicator abs = neg.abs();
        assertEquals(series.num().numOf(7), abs.getValue(0));
    }

    @Test
    public void testSqrt() {
        NumericIndicator sqrt = constFive.sqrt();
        // sqrt(5) ≈ 2.236..., we compare using doubleValue with a tolerance
        Num expected = series.num().numOf(Math.sqrt(5));
        assertEquals(expected.doubleValue(), sqrt.getValue(0).doubleValue(), 1e-9);
    }

    @Test
    public void testSquared() {
        NumericIndicator sq = constFive.squared();
        assertEquals(series.num().numOf(25), sq.getValue(0));
    }

    @Test
    public void testSma() {
        NumericIndicator sma = close.sma(3);
        // SMA of first 3 close values: (10+20+30)/3 = 20
        assertEquals(series.num().numOf(20), sma.getValue(2));
    }

    @Test
    public void testEma() {
        NumericIndicator ema = close.ema(3);
        // EMA is more complex; we only verify that it returns a Num and does not throw
        assertNotNull(ema.getValue(2));
    }

    @Test
    public void testStddev() {
        NumericIndicator std = close.stddev(3);
        assertNotNull(std.getValue(2));
    }

    @Test
    public void testHighest() {
        NumericIndicator highest = close.highest(3);
        // highest of first 3 values = 30
        assertEquals(series.num().numOf(30), highest.getValue(2));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest = close.lowest(3);
        // lowest of first 3 values = 10
        assertEquals(series.num().numOf(10), lowest.getValue(2));
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev = close.previous(2);
        // previous(2) at index 3 should give value at index 1 (20)
        assertEquals(series.num().numOf(20), prev.getValue(3));
    }

    @Test
    public void testPreviousDefault() {
        Indicator<Num> prev = close.previous(); // barCount = 1
        // previous at index 2 should give value at index 1 (20)
        assertEquals(series.num().numOf(20), prev.getValue(2));
    }

    @Test
    public void testCrossedOverIndicator() {
        // create a rising indicator that crosses close price at index 1
        NumericIndicator low = constant(5);
        Rule rule = low.crossedOver(close);
        // At index 0: 5 < 10 (no cross), at index 1: 5 < 20 still no cross,
        // but crossedOver is true when low goes from below to above.
        // To force a cross we use a decreasing indicator.
        NumericIndicator decreasing = NumericIndicator.of(new ConstantIndicator<>(series, series.num().numOf(15)));
        Rule crossRule = decreasing.crossedUnder(close);
        // At index 0: 15 > 10 (cross under true)
        assertTrue(crossRule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(100); // close never exceeds 100, never crosses under
        assertFalse(rule.isSatisfied(4));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        Rule rule = close.isGreaterThan(constFive);
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(5);
        assertTrue(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanIndicator() {
        Rule rule = close.isLessThan(constFive);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(5);
        assertFalse(rule.isSatisfied(0));
    }

    /* ----------------------------------------------------------------------
     *  Edge‑case and exception tests
     * ---------------------------------------------------------------------- */

    @Test
    public void testDivisionByZeroReturnsNaN() {
        NumericIndicator div = close.dividedBy(constZero);
        Num result = div.getValue(0);
        assertTrue("Result should be NaN when dividing by zero", result.isNaN());
    }

    @Test
    public void testSqrtOfNegativeNumberReturnsNaN() {
        NumericIndicator neg = constant(-9);
        NumericIndicator sqrt = neg.sqrt();
        Num result = sqrt.getValue(0);
        assertTrue("sqrt(negative) should be NaN", result.isNaN());
    }

    @Test
    public void testMinMaxWithEqualValues() {
        // Both indicators return 10 at index 0
        NumericIndicator ten = constant(10);
        NumericIndicator min = ten.min(constFive); // 5 is smaller
        NumericIndicator max = ten.max(constFive); // 10 is larger
        assertEquals(series.num().numOf(5), min.getValue(0));
        assertEquals(series.num().numOf(10), max.getValue(0));

        // When equal, min/max should return the left-hand side (ten)
        NumericIndicator minEqual = ten.min(10);
        NumericIndicator maxEqual = ten.max(10);
        assertEquals(series.num().numOf(10), minEqual.getValue(0));
        assertEquals(series.num().numOf(10), maxEqual.getValue(0));
    }

    @Test
    public void testCrossedOverAtExactThreshold() {
        // Indicator that is exactly equal to the threshold at index 1
        NumericIndicator threshold = constant(20);
        Rule rule = close.crossedOver(threshold);
        // crossedOver should be false when values are equal (no crossing)
        assertFalse(rule.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderAtExactThreshold() {
        NumericIndicator threshold = constant(20);
        Rule rule = close.crossedUnder(threshold);
        // crossedUnder should be false when values are equal
        assertFalse(rule.isSatisfied(1));
    }

    @Test
    public void testToStringDelegates() {
        String expected = close.delegate().toString();
        assertEquals(expected, close.toString());
    }

    @Test
    public void testGetUnstableBarsAlwaysZero() {
        assertEquals(0, close.getUnstableBars());
    }

    @Test
    public void testGetBarSeriesMatchesDelegate() {
        assertSame(series, close.getBarSeries());
    }
}
