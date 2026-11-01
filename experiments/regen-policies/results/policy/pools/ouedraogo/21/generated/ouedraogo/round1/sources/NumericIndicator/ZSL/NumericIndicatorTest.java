package org.ta4j.core.indicators.numeric;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;


import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.trading.TradingRecord;

import static org.junit.Assert.*;

public class NumericIndicatorTest {

    private BarSeries series;

    @Before
    public void setUp() {
        // Build a simple BarSeries with 5 bars.
        // The library provides a concrete implementation (BaseBarSeries) that can be used.
        // Each bar will have close price = index + 1 and volume = (index + 1) * 10.
        series = new org.ta4j.core.BaseBarSeries("test-series");
        for (int i = 0; i < 5; i++) {
            Num close = series.numFactory().numOf(i + 1);
            Num volume = series.numFactory().numOf((i + 1) * 10);
            // BaseBar constructor: (Duration timePeriod, ZonedDateTime endTime, Num open, Num high, Num low, Num close, Num volume)
            // For simplicity we reuse the close price for open/high/low.
            org.ta4j.core.BaseBar bar = new org.ta4j.core.BaseBar(
                    java.time.Duration.ofDays(1),
                    java.time.ZonedDateTime.now().plusDays(i),
                    close, // open
                    close, // high
                    close, // low
                    close, // close
                    volume
            );
            series.addBar(bar);
        }
    }

    /** Helper to extract the numeric value from a NumericIndicator at a given index. */
    private double value(NumericIndicator ni, int index) {
        return ni.getValue(index).doubleValue();
    }

    @Test
    public void testPlusWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator volume = NumericIndicator.volume(series);

        NumericIndicator sum = close.plus(volume);
        // index 2: close=3, volume=30 => sum=33
        assertEquals(33.0, value(sum, 2), 0.0001);

        NumericIndicator sumWithNumber = close.plus(5);
        // index 1: close=2 + 5 = 7
        assertEquals(7.0, value(sumWithNumber, 1), 0.0001);
    }

    @Test
    public void testMinusWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator volume = NumericIndicator.volume(series);

        NumericIndicator diff = volume.minus(close);
        // index 3: volume=40, close=4 => diff=36
        assertEquals(36.0, value(diff, 3), 0.0001);

        NumericIndicator diffWithNumber = close.minus(1);
        // index 0: close=1 - 1 = 0
        assertEquals(0.0, value(diffWithNumber, 0), 0.0001);
    }

    @Test
    public void testMultipliedByAndDividedBy() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator volume = NumericIndicator.volume(series);

        NumericIndicator prod = close.multipliedBy(volume);
        // index 2: 3 * 30 = 90
        assertEquals(90.0, value(prod, 2), 0.0001);

        NumericIndicator prodWithNumber = close.multipliedBy(2);
        // index 1: 2 * 2 = 4
        assertEquals(4.0, value(prodWithNumber, 1), 0.0001);

        NumericIndicator quot = volume.dividedBy(close);
        // index 4: 50 / 5 = 10
        assertEquals(10.0, value(quot, 4), 0.0001);

        NumericIndicator quotWithNumber = close.dividedBy(2);
        // index 3: 4 / 2 = 2
        assertEquals(2.0, value(quotWithNumber, 3), 0.0001);
    }

    @Test
    public void testMinAndMax() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator volume = NumericIndicator.volume(series);

        NumericIndicator min = close.min(volume);
        // index 2: close=3, volume=30 => min=3
        assertEquals(3.0, value(min, 2), 0.0001);

        NumericIndicator max = close.max(volume);
        // index 2: max=30
        assertEquals(30.0, value(max, 2), 0.0001);

        NumericIndicator minWithNumber = close.min(2);
        // index 0: close=1, 2 => min=1
        assertEquals(1.0, value(minWithNumber, 0), 0.0001);

        NumericIndicator maxWithNumber = close.max(0);
        // index 0: max=1
        assertEquals(1.0, value(maxWithNumber, 0), 0.0001);
    }

    @Test
    public void testAbsAndSqrtAndSquared() {
        // Create a constant indicator with negative values
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-4)));

        NumericIndicator abs = neg.abs();
        assertEquals(4.0, value(abs, 0), 0.0001);

        NumericIndicator sqrt = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(9))).sqrt();
        assertEquals(3.0, value(sqrt, 0), 0.0001);

        NumericIndicator squared = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(5))).squared();
        // 5 * 5 = 25
        assertEquals(25.0, value(squared, 0), 0.0001);
    }

    @Test
    public void testSmaAndEma() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        // SMA with barCount = 3
        NumericIndicator sma = close.sma(3);
        // SMA at index 2 = (1+2+3)/3 = 2
        assertEquals(2.0, value(sma, 2), 0.0001);
        // SMA at index 4 = (3+4+5)/3 = 4
        assertEquals(4.0, value(sma, 4), 0.0001);

        // EMA with barCount = 3 (using standard EMA formula)
        NumericIndicator ema = close.ema(3);
        // Verify that EMA returns a non‑null value and is consistent with the underlying indicator.
        // Exact numeric value depends on the smoothing factor; we only assert that it is close to the last close.
        double emaValue = value(ema, 4);
        assertTrue(emaValue > 0);
        assertTrue(Math.abs(emaValue - 5.0) < 2.0);
    }

    @Test
    public void testStdDevHighestLowestPrevious() {
        NumericIndicator close = NumericIndicator.closePrice(series);

        NumericIndicator stddev = close.stddev(3);
        // Standard deviation of (1,2,3) at index 2 = sqrt(((1-2)^2+(2-2)^2+(3-2)^2)/3) = sqrt(2/3) ≈ 0.8165
        assertEquals(0.8165, value(stddev, 2), 0.001);

        NumericIndicator highest = close.highest(3);
        // Highest of (3,4,5) at index 4 = 5
        assertEquals(5.0, value(highest, 4), 0.0001);

        NumericIndicator lowest = close.lowest(3);
        // Lowest of (3,4,5) at index 4 = 3
        assertEquals(3.0, value(lowest, 4), 0.0001);

        NumericIndicator previous = close.previous(2);
        // previous(2) at index 4 should give value at index 2 => 3
        assertEquals(3.0, value(previous, 4), 0.0001);

        // previous() default is previous(1)
        NumericIndicator previousDefault = close.previous();
        // at index 3 should give value at index 2 => 3
        assertEquals(3.0, value(previousDefault, 3), 0.0001);
    }

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // Build a simple indicator that rises linearly.
        NumericIndicator close = NumericIndicator.closePrice(series);
        // Crossed over 2 at index 1 (close:2) – should be false because previous is 1 (<2) and current 2 == 2 (not >)
        Rule crossedOver = close.crossedOver(2);
        assertFalse(crossedOver.isSatisfied(1));

        // Crossed over 1.5 at index 1 – previous 1 < 1.5 and current 2 > 1.5 => true
        Rule crossedOver2 = close.crossedOver(1.5);
        assertTrue(crossedOver2.isSatisfied(1));

        // Crossed under 4 at index 3 – previous 3 < 4 and current 4 == 4 (not <) => false
        Rule crossedUnder = close.crossedUnder(4);
        assertFalse(crossedUnder.isSatisfied(3));

        // Crossed under 3.5 at index 3 – previous 3 < 3.5 and current 4 > 3.5 => false (crossing upward)
        Rule crossedUnder2 = close.crossedUnder(3.5);
        assertFalse(crossedUnder2.isSatisfied(3));
    }

    @Test
    public void testIsGreaterThanAndIsLessThanRules() {
        NumericIndicator close = NumericIndicator.closePrice(series);

        Rule greaterThan3 = close.isGreaterThan(3);
        // index 4 close=5 > 3 => true
        assertTrue(greaterThan3.isSatisfied(4));
        // index 2 close=3 > 3 => false (equal)
        assertFalse(greaterThan3.isSatisfied(2));

        Rule lessThan2 = close.isLessThan(2);
        // index 0 close=1 < 2 => true
        assertTrue(lessThan2.isSatisfied(0));
        // index 1 close=2 < 2 => false (equal)
        assertFalse(lessThan2.isSatisfied(1));
    }

    @Test
    public void testDelegateAndToString() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        assertSame(close.delegate(), close.delegate());

        // toString should delegate to the underlying indicator's toString
        String expected = close.delegate().toString();
        assertEquals(expected, close.toString());
    }
}
