###Test START##
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.rules.*;

public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;

    @Before
    public void setUp() {
        // Build a simple series with three bars having close prices 1, 2, 3
        series = new BaseBarSeries();
        series.addBar(new BaseBarBuilder().closePrice(series.numFactory().numOf(1)).build());
        series.addBar(new BaseBarBuilder().closePrice(series.numFactory().numOf(2)).build());
        series.addBar(new BaseBarBuilder().closePrice(series.numFactory().numOf(3)).build());

        close = NumericIndicator.closePrice(series);
    }

    // -----------------------------------------------------------------
    // basic arithmetic operations
    // -----------------------------------------------------------------
    @Test
    public void testPlusWithIndicator() {
        NumericIndicator result = close.plus(close);
        assertNotNull(result);
        // value at index 2 should be 3 + 3 = 6
        Num expected = series.numFactory().numOf(6);
        assertEquals(0, expected.compareTo(result.getValue(2)));
    }

    @Test
    public void testPlusWithNumber() {
        NumericIndicator result = close.plus(5);
        assertNotNull(result);
        // value at index 1 should be 2 + 5 = 7
        Num expected = series.numFactory().numOf(7);
        assertEquals(0, expected.compareTo(result.getValue(1)));
    }

    @Test
    public void testMinusWithIndicator() {
        NumericIndicator result = close.minus(close);
        assertNotNull(result);
        // value at any index should be 0
        Num zero = series.numFactory().numOf(0);
        assertEquals(0, zero.compareTo(result.getValue(0)));
        assertEquals(0, zero.compareTo(result.getValue(2)));
    }

    @Test
    public void testMinusWithNumber() {
        NumericIndicator result = close.minus(1);
        assertNotNull(result);
        // index 0: 1 - 1 = 0
        Num zero = series.numFactory().numOf(0);
        assertEquals(0, zero.compareTo(result.getValue(0)));
    }

    @Test
    public void testMultipliedByWithIndicator() {
        NumericIndicator result = close.multipliedBy(close);
        assertNotNull(result);
        // index 2: 3 * 3 = 9
        Num expected = series.numFactory().numOf(9);
        assertEquals(0, expected.compareTo(result.getValue(2)));
    }

    @Test
    public void testMultipliedByWithNumber() {
        NumericIndicator result = close.multipliedBy(2);
        assertNotNull(result);
        // index 1: 2 * 2 = 4
        Num expected = series.numFactory().numOf(4);
        assertEquals(0, expected.compareTo(result.getValue(1)));
    }

    @Test
    public void testDividedByWithIndicator() {
        NumericIndicator result = close.dividedBy(close);
        assertNotNull(result);
        // any non‑zero division yields 1
        Num one = series.numFactory().numOf(1);
        assertEquals(0, one.compareTo(result.getValue(0)));
        assertEquals(0, one.compareTo(result.getValue(2)));
    }

    @Test
    public void testDividedByWithNumber() {
        NumericIndicator result = close.dividedBy(2);
        assertNotNull(result);
        // index 2: 3 / 2 = 1.5
        Num expected = series.numFactory().numOf(1.5);
        assertEquals(0, expected.compareTo(result.getValue(2)));
    }

    // -----------------------------------------------------------------
    // min / max
    // -----------------------------------------------------------------
    @Test
    public void testMinWithIndicator() {
        NumericIndicator constFive = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(5)));
        NumericIndicator result = close.min(constFive);
        // at index 0 min(1,5)=1 ; at index 2 min(3,5)=3
        assertEquals(0, close.getValue(0).compareTo(result.getValue(0)));
        assertEquals(0, close.getValue(2).compareTo(result.getValue(2)));
    }

    @Test
    public void testMinWithNumber() {
        NumericIndicator result = close.min(2);
        // values: [1,2,2]
        assertEquals(0, series.numFactory().numOf(1).compareTo(result.getValue(0)));
        assertEquals(0, series.numFactory().numOf(2).compareTo(result.getValue(1)));
        assertEquals(0, series.numFactory().numOf(2).compareTo(result.getValue(2)));
    }

    @Test
    public void testMaxWithIndicator() {
        NumericIndicator constTwo = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(2)));
        NumericIndicator result = close.max(constTwo);
        // values: [2,2,3]
        assertEquals(0, series.numFactory().numOf(2).compareTo(result.getValue(0)));
        assertEquals(0, series.numFactory().numOf(2).compareTo(result.getValue(1)));
        assertEquals(0, series.numFactory().numOf(3).compareTo(result.getValue(2)));
    }

    @Test
    public void testMaxWithNumber() {
        NumericIndicator result = close.max(2);
        // same as above
        assertEquals(0, series.numFactory().numOf(2).compareTo(result.getValue(0)));
        assertEquals(0, series.numFactory().numOf(2).compareTo(result.getValue(1)));
        assertEquals(0, series.numFactory().numOf(3).compareTo(result.getValue(2)));
    }

    // -----------------------------------------------------------------
    // unary operations
    // -----------------------------------------------------------------
    @Test
    public void testAbs() {
        // create an indicator with negative values
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(-4)));
        NumericIndicator result = neg.abs();
        Num four = series.numFactory().numOf(4);
        assertEquals(0, four.compareTo(result.getValue(0)));
        assertEquals(0, four.compareTo(result.getValue(1)));
    }

    @Test
    public void testSqrt() {
        NumericIndicator result = close.sqrt();
        // sqrt(1)=1, sqrt(2)=~1.4142, sqrt(3)=~1.7320
        Num sqrt1 = series.numFactory().numOf(1).sqrt();
        Num sqrt2 = series.numFactory().numOf(2).sqrt();
        Num sqrt3 = series.numFactory().numOf(3).sqrt();
        assertEquals(0, sqrt1.compareTo(result.getValue(0)));
        assertEquals(0, sqrt2.compareTo(result.getValue(1)));
        assertEquals(0, sqrt3.compareTo(result.getValue(2)));
    }

    @Test
    public void testSquared() {
        NumericIndicator result = close.squared();
        // 1^2=1, 2^2=4, 3^2=9
        assertEquals(0, series.numFactory().numOf(1).compareTo(result.getValue(0)));
        assertEquals(0, series.numFactory().numOf(4).compareTo(result.getValue(1)));
        assertEquals(0, series.numFactory().numOf(9).compareTo(result.getValue(2)));
    }

    // -----------------------------------------------------------------
    // statistical / aggregation indicators
    // -----------------------------------------------------------------
    @Test
    public void testSma() {
        NumericIndicator sma = close.sma(2);
        assertTrue(sma.delegate() instanceof SMAIndicator);
        // SMA(2) at index 1 = (1+2)/2 = 1.5 ; at index 2 = (2+3)/2 = 2.5
        Num sma1 = series.numFactory().numOf(1.5);
        Num sma2 = series.numFactory().numOf(2.5);
        assertEquals(0, sma1.compareTo(sma.getValue(1)));
        assertEquals(0, sma2.compareTo(sma.getValue(2)));
    }

    @Test
    public void testEma() {
        NumericIndicator ema = close.ema(2);
        assertTrue(ema.delegate() instanceof EMAIndicator);
        // EMA(2) with smoothing factor 2/(2+1)=0.666...
        // index 0 = 1
        // index 1 = 1* (1-α) + 2*α = 1*0.333... + 2*0.666... = 1.666...
        Num expected = series.numFactory().numOf(1.6666666666666667);
        assertEquals(0, expected.compareTo(ema.getValue(1)));
    }

    @Test
    public void testStddev() {
        NumericIndicator std = close.stddev(2);
        assertTrue(std.delegate() instanceof StandardDeviationIndicator);
        // For values [2,3] stddev = sqrt( ((2-2.5)^2 + (3-2.5)^2) / 2 ) = sqrt(0.25) = 0.5
        Num expected = series.numFactory().numOf(0.5);
        assertEquals(0, expected.compareTo(std.getValue(2)));
    }

    @Test
    public void testHighest() {
        NumericIndicator highest = close.highest(2);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);
        // highest of [2,3] = 3
        Num three = series.numFactory().numOf(3);
        assertEquals(0, three.compareTo(highest.getValue(2)));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest = close.lowest(2);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        // lowest of [2,3] = 2
        Num two = series.numFactory().numOf(2);
        assertEquals(0, two.compareTo(lowest.getValue(2)));
    }

    @Test
    public void testPrevious() {
        NumericIndicator prev = close.previous();
        assertTrue(prev.delegate() instanceof PreviousValueIndicator);
        // previous of index 2 = value at index 1 = 2
        Num two = series.numFactory().numOf(2);
        assertEquals(0, two.compareTo(prev.getValue(2)));
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev3 = close.previous(2);
        assertTrue(prev3.delegate() instanceof PreviousValueIndicator);
        // previous two steps from index 2 => index 0 = 1
        Num one = series.numFactory().numOf(1);
        assertEquals(0, one.compareTo(prev3.getValue(2)));
    }

    // -----------------------------------------------------------------
    // rule creation
    // -----------------------------------------------------------------
    @Test
    public void testCrossedOverIndicator() {
        NumericIndicator other = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(2)));
        Rule rule = close.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // At index 1 the close price moves from 1 (below 2) to 2 (equal) – not a cross up,
        // but at index 2 it moves from 2 (equal) to 3 (above) – should be satisfied.
        assertTrue(rule.isSatisfied(2));
        assertFalse(rule.isSatisfied(1));
    }

    @Test
    public void testCrossedUnderIndicator() {
        NumericIndicator other = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(2)));
        Rule rule = close.crossedUnder(other);
        assertTrue(rule instanceof CrossedDownIndicatorRule);
        // At index 1 the price goes from 1 (below) to 2 (equal) – not a cross down,
        // at index 2 from 2 (equal) to 3 (above) – also not a cross down.
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedOverNumber() {
        Rule rule = close.crossedOver(2);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(2);
        assertTrue(rule instanceof CrossedDownIndicatorRule);
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        NumericIndicator other = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(2)));
        Rule rule = close.isGreaterThan(other);
        assertTrue(rule instanceof OverIndicatorRule);
        assertTrue(rule.isSatisfied(2)); // 3 > 2
        assertFalse(rule.isSatisfied(0)); // 1 > 2 false
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(2);
        assertTrue(rule instanceof OverIndicatorRule);
        assertTrue(rule.isSatisfied(2));
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testIsLessThanIndicator() {
        NumericIndicator other = NumericIndicator.of(new ConstantIndicator<>(series, series.numFactory().numOf(2)));
        Rule rule = close.isLessThan(other);
        assertTrue(rule instanceof UnderIndicatorRule);
        assertTrue(rule.isSatisfied(0)); // 1 < 2
        assertFalse(rule.isSatisfied(2)); // 3 < 2 false
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(2);
        assertTrue(rule instanceof UnderIndicatorRule);
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(2));
    }

    // -----------------------------------------------------------------
    // delegate accessor and toString forwarding
    // -----------------------------------------------------------------
    @Test
    public void testDelegate() {
        Indicator<Num> del = close.delegate();
        assertSame(del, close.delegate());
        assertEquals(del.toString(), close.toString());
    }
}
###Test END##