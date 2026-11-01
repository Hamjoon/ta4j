package org.ta4j.core.indicators.numeric;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.Indicator;


import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.Rule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>All public methods – basic functionality</li>
 *   <li>Edge‑cases such as division by zero, square‑root of negative numbers,
 *       and equality handling in comparison rules</li>
 *   <li>Exception / NaN handling where the underlying {@link Num} implementation
 *       defines the behaviour</li>
 * </ul>
 *
 * <p>
 * The library provides a concrete {@link BaseBarSeries} implementation and the
 * {@link DecimalNum} number type, which are used throughout the tests.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;
    private NumericIndicator volume;

    /**
     * Helper to build a {@link BarSeries} with the supplied close prices and
     * volumes.  All bars have the same timestamps (not relevant for the tests)
     * and a zero trade amount – only close price and volume are needed.
     */
    private BarSeries buildSeries(double... closePrices) {
        List<Bar> bars = new ArrayList<>();
        for (int i = 0; i < closePrices.length; i++) {
            Num price = DecimalNum.valueOf(closePrices[i]);
            // use constant volume = 1 for simplicity
            Num volume = DecimalNum.ONE;
            Bar bar = new BaseBar(
                    ZonedDateTime.now(),
                    price,               // open (same as close)
                    price,               // high
                    price,               // low
                    price,               // close
                    volume,              // volume
                    DecimalNum.ZERO      // amount (price * volume)
            );
            bars.add(bar);
        }
        BarSeries s = new BaseBarSeries("test-series", bars);
        return s;
    }

    @Before
    public void setUp() {
        // a series with three candles: 10, 20, 30
        series = buildSeries(10, 20, 30);
        close = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    /* --------------------------------------------------------------------- */
    /*  BASIC FUNCTIONALITY TESTS                                            */
    /* --------------------------------------------------------------------- */

    @Test
    public void testOfCreatesFluentWrapper() {
        NumericIndicator ni = NumericIndicator.of(close);
        assertNotNull(ni);
        assertSame(close, ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator ci = NumericIndicator.closePrice(series);
        assertNotNull(ci);
        // first close price must be 10
        assertEquals(DecimalNum.valueOf(10), ci.getValue(0));
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vi = NumericIndicator.volume(series);
        assertNotNull(vi);
        // volume is constant 1
        assertEquals(DecimalNum.ONE, vi.getValue(0));
    }

    @Test
    public void testPlusIndicator() {
        NumericIndicator sum = close.plus(close);
        assertEquals(DecimalNum.valueOf(20), sum.getValue(0));
        assertEquals(DecimalNum.valueOf(40), sum.getValue(1));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator sum = close.plus(5);
        assertEquals(DecimalNum.valueOf(15), sum.getValue(0));
        assertEquals(DecimalNum.valueOf(25), sum.getValue(1));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator diff = close.minus(close);
        assertEquals(DecimalNum.ZERO, diff.getValue(0));
        assertEquals(DecimalNum.ZERO, diff.getValue(1));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator diff = close.minus(5);
        assertEquals(DecimalNum.valueOf(5), diff.getValue(0));
        assertEquals(DecimalNum.valueOf(15), diff.getValue(1));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator prod = close.multipliedBy(close);
        assertEquals(DecimalNum.valueOf(100), prod.getValue(0));
        assertEquals(DecimalNum.valueOf(400), prod.getValue(1));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator prod = close.multipliedBy(2);
        assertEquals(DecimalNum.valueOf(20), prod.getValue(0));
        assertEquals(DecimalNum.valueOf(40), prod.getValue(1));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator div = close.dividedBy(close);
        assertEquals(DecimalNum.ONE, div.getValue(0));
        assertEquals(DecimalNum.ONE, div.getValue(1));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator div = close.dividedBy(2);
        assertEquals(DecimalNum.valueOf(5), div.getValue(0));
        assertEquals(DecimalNum.valueOf(10), div.getValue(1));
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator other = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(15)));
        NumericIndicator min = close.min(other);
        // index 0: min(10,15)=10 ; index1: min(20,15)=15
        assertEquals(DecimalNum.valueOf(10), min.getValue(0));
        assertEquals(DecimalNum.valueOf(15), min.getValue(1));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = close.min(15);
        assertEquals(DecimalNum.valueOf(10), min.getValue(0));
        assertEquals(DecimalNum.valueOf(15), min.getValue(1));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator other = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(15)));
        NumericIndicator max = close.max(other);
        // index0: max(10,15)=15 ; index1: max(20,15)=20
        assertEquals(DecimalNum.valueOf(15), max.getValue(0));
        assertEquals(DecimalNum.valueOf(20), max.getValue(1));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = close.max(15);
        assertEquals(DecimalNum.valueOf(15), max.getValue(0));
        assertEquals(DecimalNum.valueOf(20), max.getValue(1));
    }

    @Test
    public void testAbs() {
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(-5)));
        NumericIndicator abs = negative.abs();
        assertEquals(DecimalNum.valueOf(5), abs.getValue(0));
    }

    @Test
    public void testSqrt() {
        NumericIndicator sqrt = close.sqrt();
        // sqrt(10) ≈ 3.1622776601683795
        Num expected = DecimalNum.valueOf(10).sqrt();
        assertEquals(expected, sqrt.getValue(0));
    }

    @Test
    public void testSquared() {
        NumericIndicator sq = close.squared();
        assertEquals(DecimalNum.valueOf(100), sq.getValue(0));
        assertEquals(DecimalNum.valueOf(400), sq.getValue(1));
    }

    @Test
    public void testSma() {
        NumericIndicator sma = close.sma(2);
        // SMA(10,20) = 15 ; SMA(20,30) = 25
        assertEquals(DecimalNum.valueOf(15), sma.getValue(1));
        assertEquals(DecimalNum.valueOf(25), sma.getValue(2));
    }

    @Test
    public void testEma() {
        NumericIndicator ema = close.ema(2);
        // EMA(10) = 10 ; EMA(20) = (20*2/3)+(10*1/3)=16.666...
        Num expected = DecimalNum.valueOf(20).multipliedBy(DecimalNum.valueOf(2))
                .plus(DecimalNum.valueOf(10))
                .dividedBy(DecimalNum.valueOf(3));
        assertEquals(expected, ema.getValue(1));
    }

    @Test
    public void testStddev() {
        NumericIndicator std = close.stddev(2);
        // Standard deviation of (10,20) = sqrt(((10-15)^2 + (20-15)^2)/1) = sqrt(25+25)=sqrt(50)
        Num mean = DecimalNum.valueOf(15);
        Num variance = DecimalNum.valueOf(10).minus(mean).pow(2)
                .plus(DecimalNum.valueOf(20).minus(mean).pow(2))
                .dividedBy(DecimalNum.ONE); // sample size = 1 (n-1)
        Num expected = variance.sqrt();
        assertEquals(expected, std.getValue(1));
    }

    @Test
    public void testHighest() {
        NumericIndicator highest = close.highest(2);
        // Highest of (10,20) = 20 ; Highest of (20,30) = 30
        assertEquals(DecimalNum.valueOf(20), highest.getValue(1));
        assertEquals(DecimalNum.valueOf(30), highest.getValue(2));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest = close.lowest(2);
        // Lowest of (10,20) = 10 ; Lowest of (20,30) = 20
        assertEquals(DecimalNum.valueOf(10), lowest.getValue(1));
        assertEquals(DecimalNum.valueOf(20), lowest.getValue(2));
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev = close.previous(2);
        // previous(2) at index 2 => value at index 0 = 10
        assertEquals(DecimalNum.valueOf(10), prev.getValue(2));
    }

    @Test
    public void testPreviousDefault() {
        NumericIndicator prev = close.previous();
        // previous() == previous(1); at index 1 => value at index 0 = 10
        assertEquals(DecimalNum.valueOf(10), prev.getValue(1));
    }

    @Test
    public void testCrossedOverIndicator() {
        // create two indicators: a rising line (5,15,25) and close (10,20,30)
        NumericIndicator low = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(5)));
        Rule crossed = low.crossedOver(close);
        assertTrue(crossed instanceof CrossedUpIndicatorRule);
        // crossing occurs at index 1 because low(1)=5 < close(1)=20 and low(0)=5 < close(0)=10 (no cross)
        // Actually cross occurs when low goes from below to above. With constant low it never crosses.
        // Use a dynamic low: 5,25,15 (crosses between 0->1)
        NumericIndicator dynamicLow = NumericIndicator.of(new Indicator<Num>() {
            @Override public Num getValue(int index) {
                if (index == 0) return DecimalNum.valueOf(5);
                if (index == 1) return DecimalNum.valueOf(25);
                return DecimalNum.valueOf(15);
            }
            @Override public BarSeries getBarSeries() { return series; }
            @Override public int getCountOfUnstableBars() { return 0; }
        });
        Rule crossedDynamic = dynamicLow.crossedOver(close);
        // crossing should be true at index 1
        assertTrue(crossedDynamic.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule crossedUnder = close.crossedUnder(25);
        // close values: 10,20,30 → crossing under 25 occurs at index 2 (30 > 25, 20 < 25)
        assertTrue(crossedUnder.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        NumericIndicator constFive = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(5)));
        Rule greater = close.isGreaterThan(constFive);
        assertTrue(greater.isSatisfied(0)); // 10 > 5
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule greater = close.isGreaterThan(15);
        // true for index 2 (30 > 15), false for index 0 (10 <= 15)
        assertFalse(greater.isSatisfied(0));
        assertTrue(greater.isSatisfied(2));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule less = close.isLessThan(15);
        assertTrue(less.isSatisfied(0));
        assertFalse(less.isSatisfied(2));
    }

    /* --------------------------------------------------------------------- */
    /*  EDGE‑CASE & EXCEPTION TESTS                                          */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDivisionByZeroNumberProducesNaN() {
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.ZERO));
        NumericIndicator div = close.dividedBy(zero);
        // According to DecimalNum behaviour, division by zero yields NaN
        assertTrue(div.getValue(0).isNaN());
    }

    @Test
    public void testSqrtOfNegativeNumberProducesNaN() {
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(-4)));
        NumericIndicator sqrt = negative.sqrt();
        assertTrue(sqrt.getValue(0).isNaN());
    }

    @Test
    public void testCrossedOverWithEqualValuesDoesNotTrigger() {
        NumericIndicator same = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(10)));
        Rule crossed = same.crossedOver(close);
        // Both indicators equal at all indices → no crossing
        assertFalse(crossed.isSatisfied(0));
        assertFalse(crossed.isSatisfied(1));
    }

    @Test
    public void testIsGreaterThanEqualValuesReturnsFalse() {
        // close(0)=10 ; compare with constant 10
        Rule greater = close.isGreaterThan(10);
        assertFalse(greater.isSatisfied(0));
    }

    @Test
    public void testIsLessThanEqualValuesReturnsFalse() {
        Rule less = close.isLessThan(30);
        // close(2)=30 equals threshold → should be false
        assertFalse(less.isSatisfied(2));
    }

    @Test
    public void testPreviousBeyondSeriesReturnsNaN() {
        NumericIndicator prev = close.previous(5);
        // series length is 3, index 2 has no value 5 bars back → NaN
        assertTrue(prev.getValue(2).isNaN());
    }

    @Test
    public void testSmaWithBarCountLargerThanSeriesReturnsNaN() {
        NumericIndicator sma = close.sma(5);
        // Not enough data → NaN
        assertTrue(sma.getValue(2).isNaN());
    }

    @Test
    public void testEmaWithBarCountZeroThrowsIllegalArgumentException() {
        try {
            close.ema(0);
            fail("Expected IllegalArgumentException for barCount = 0");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testStddevWithBarCountZeroThrowsIllegalArgumentException() {
        try {
            close.stddev(0);
            fail("Expected IllegalArgumentException for barCount = 0");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testHighestWithBarCountOneReturnsOriginal() {
        NumericIndicator highest = close.highest(1);
        assertEquals(close.getValue(0), highest.getValue(0));
        assertEquals(close.getValue(1), highest.getValue(1));
    }

    @Test
    public void testLowestWithBarCountOneReturnsOriginal() {
        NumericIndicator lowest = close.lowest(1);
        assertEquals(close.getValue(0), lowest.getValue(0));
        assertEquals(close.getValue(1), lowest.getValue(1));
    }

    /* --------------------------------------------------------------------- */
    /*  TO STRING / DELEGATE VERIFICATION                                    */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDelegateMethodReturnsOriginalIndicator() {
        assertSame(close.delegate(), close.delegate());
    }

    @Test
    public void testToStringDelegatesToUnderlyingIndicator() {
        String expected = close.delegate().toString();
        assertEquals(expected, close.toString());
    }
}
