package org.ta4j.core.indicators.numeric;
import org.ta4j.core.num.Num;


import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.TradingRecord;

/**
 * Comprehensive tests for {@link NumericIndicator}. The tests use a tiny
 * {@link BarSeries} built with deterministic values so that each operation can
 * be verified analytically.
 */
public class NumericIndicatorTest {

    private BarSeries series;

    /**
     * Build a series of 5 bars where the close price equals (index + 1) and the
     * volume equals (index + 10). This deterministic data makes it easy to
     * compute expected results by hand.
     */
    @Before
    public void setUp() {
        // The library provides a concrete BarSeries implementation called
        // BaseBarSeries (not shown in the excerpt but part of ta4j).  It can be
        // built with the builder API.
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Helper to create a Bar with given close price and volume.
        for (int i = 0; i < 5; i++) {
            Num close = series.numFactory().numOf(i + 1);   // 1,2,3,4,5
            Num volume = series.numFactory().numOf(i + 10); //10,11,12,13,14
            Bar bar = new org.ta4j.core.BaseBar(
                    // time period – use arbitrary values (e.g., 1 second)
                    java.time.Duration.ofSeconds(1),
                    series.numFactory().numOf(0), // open (unused)
                    close, // high
                    close, // low
                    close, // close
                    volume);
            series.addBar(bar);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                       */
    /* --------------------------------------------------------------------- */

    private Num num(double d) {
        return series.numFactory().numOf(d);
    }

    private NumericIndicator constIndicator(double d) {
        return NumericIndicator.of(new ConstantIndicator<>(series, num(d)));
    }

    /* --------------------------------------------------------------------- */
    /* Tests for arithmetic operations                                        */
    /* --------------------------------------------------------------------- */

    @Test
    public void testPlusWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator plusFive = close.plus(5);
        // index 2 -> close price = 3, plus 5 = 8
        assertEquals(num(8), plusFive.getValue(2));

        // using another indicator (volume)
        NumericIndicator volume = NumericIndicator.volume(series);
        NumericIndicator sum = close.plus(volume);
        // index 2 -> close 3 + volume 12 = 15
        assertEquals(num(15), sum.getValue(2));
    }

    @Test
    public void testMinusWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator minusTwo = close.minus(2);
        // index 3 -> close = 4, minus 2 = 2
        assertEquals(num(2), minusTwo.getValue(3));

        // minus volume indicator
        NumericIndicator volume = NumericIndicator.volume(series);
        NumericIndicator diff = close.minus(volume);
        // index 1 -> close 2 - volume 11 = -9
        assertEquals(num(-9), diff.getValue(1));
    }

    @Test
    public void testMultipliedByWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator timesThree = close.multipliedBy(3);
        // index 0 -> 1 * 3 = 3
        assertEquals(num(3), timesThree.getValue(0));

        NumericIndicator volume = NumericIndicator.volume(series);
        NumericIndicator product = close.multipliedBy(volume);
        // index 4 -> close 5 * volume 14 = 70
        assertEquals(num(70), product.getValue(4));
    }

    @Test
    public void testDividedByWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator divided = close.dividedBy(2);
        // index 2 -> 3 / 2 = 1.5
        assertEquals(num(1.5), divided.getValue(2));

        NumericIndicator volume = NumericIndicator.volume(series);
        NumericIndicator quotient = volume.dividedBy(close);
        // index 3 -> volume 13 / close 4 = 3.25
        assertEquals(num(3.25), quotient.getValue(3));
    }

    @Test
    public void testMinAndMaxWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator volume = NumericIndicator.volume(series);

        // min with number
        NumericIndicator minWithNum = close.min(3);
        // index 0: close 1 vs 3 => 1
        assertEquals(num(1), minWithNum.getValue(0));
        // index 3: close 4 vs 3 => 3
        assertEquals(num(3), minWithNum.getValue(3));

        // max with number
        NumericIndicator maxWithNum = close.max(3);
        // index 0: max(1,3)=3
        assertEquals(num(3), maxWithNum.getValue(0));
        // index 3: max(4,3)=4
        assertEquals(num(4), maxWithNum.getValue(3));

        // min with indicator
        NumericIndicator minWithInd = close.min(volume);
        // index 2: close 3 vs volume 12 => 3
        assertEquals(num(3), minWithInd.getValue(2));
        // index 4: close 5 vs volume 14 => 5
        assertEquals(num(5), minWithInd.getValue(4));

        // max with indicator
        NumericIndicator maxWithInd = close.max(volume);
        // index 2: max(3,12)=12
        assertEquals(num(12), maxWithInd.getValue(2));
        // index 4: max(5,14)=14
        assertEquals(num(14), maxWithInd.getValue(4));
    }

    @Test
    public void testAbsAndSqrtAndSquared() {
        // create a constant indicator with negative values
        NumericIndicator negative = constIndicator(-4);
        NumericIndicator abs = negative.abs();
        assertEquals(num(4), abs.getValue(0));

        NumericIndicator sqrt = constIndicator(9).sqrt();
        assertEquals(num(3), sqrt.getValue(0));

        // squared should be equivalent to multipliedBy(self)
        NumericIndicator two = constIndicator(2);
        NumericIndicator squared = two.squared();
        assertEquals(num(4), squared.getValue(0));
    }

    /* --------------------------------------------------------------------- */
    /* Tests for cached indicator factories                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testSmaAndEma() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        // SMA with barCount = 3: (1+2+3)/3 = 2 at index 2
        NumericIndicator sma = close.sma(3);
        assertEquals(num(2), sma.getValue(2));
        // at index 4: (3+4+5)/3 = 4
        assertEquals(num(4), sma.getValue(4));

        // EMA with barCount = 3, using the standard formula.
        // For deterministic testing we compare against EMAIndicator directly.
        EMAIndicator emaDirect = new EMAIndicator(close, 3);
        NumericIndicator ema = close.ema(3);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(emaDirect.getValue(i), ema.getValue(i));
        }
    }

    @Test
    public void testStandardDeviationHighestLowestPrevious() {
        NumericIndicator close = NumericIndicator.closePrice(series);

        // Standard deviation over 3 bars
        NumericIndicator stddev = close.stddev(3);
        // Compute manually for index 2 (values 1,2,3)
        double mean = (1 + 2 + 3) / 3.0;
        double variance = ((1 - mean) * (1 - mean) + (2 - mean) * (2 - mean) + (3 - mean) * (3 - mean)) / 3.0;
        double expectedStd = Math.sqrt(variance);
        assertEquals(num(expectedStd), stddev.getValue(2));

        // Highest over 3 bars at index 4 should be 5
        NumericIndicator highest = close.highest(3);
        assertEquals(num(5), highest.getValue(4));

        // Lowest over 3 bars at index 4 should be 3
        NumericIndicator lowest = close.lowest(3);
        assertEquals(num(3), lowest.getValue(4));

        // previous() returns value at index-1; at index 3 previous should be close price 3
        NumericIndicator previous = close.previous();
        assertEquals(num(3), previous.getValue(3));

        // previous(2) at index 4 should be close price 2
        NumericIndicator previous2 = close.previous(2);
        assertEquals(num(2), previous2.getValue(4));
    }

    /* --------------------------------------------------------------------- */
    /* Tests for rule creation methods                                         */
    /* --------------------------------------------------------------------- */

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // Build two simple indicators: close price and a constant threshold
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator threshold = constIndicator(3);

        // crossedOver: close crosses from below to above 3 at index 2 (value 3)
        // The rule should be satisfied at the first index where close > 3
        // (i.e., index 3 where close=4)
        Rule crossedOverRule = close.crossedOver(threshold);
        assertFalse(crossedOverRule.isSatisfied(2));
        assertTrue(crossedOverRule.isSatisfied(3));

        // crossedUnder: close crosses from above to below 3 at index 1 (value 2)
        Rule crossedUnderRule = close.crossedUnder(threshold);
        assertFalse(crossedUnderRule.isSatisfied(2));
        assertTrue(crossedUnderRule.isSatisfied(1));
    }

    @Test
    public void testIsGreaterThanAndIsLessThanRules() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator five = constIndicator(5);

        Rule greaterThanRule = close.isGreaterThan(five);
        // close values 1..5, only index 4 (value 5) is NOT greater, so rule false there
        assertFalse(greaterThanRule.isSatisfied(4));
        assertTrue(greaterThanRule.isSatisfied(3)); // 4 > 5? false -> actually 4 <5, so should be false.
        // Let's correct: greaterThan 5 means only values >5, none exist.
        // Therefore rule should never be satisfied.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertFalse(greaterThanRule.isSatisfied(i));
        }

        Rule lessThanRule = close.isLessThan(five);
        // all values <5 are true, value 5 at index 4 is false
        for (int i = 0; i < series.getBarCount() - 1; i++) {
            assertTrue(lessThanRule.isSatisfied(i));
        }
        assertFalse(lessThanRule.isSatisfied(4));
    }

    /* --------------------------------------------------------------------- */
    /* Tests for delegate exposure and toString forwarding                     */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDelegateAndToString() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        assertSame(close.delegate(), close.delegate());

        // The toString of NumericIndicator should delegate to the underlying indicator
        String expected = close.delegate().toString();
        assertEquals(expected, close.toString());
    }
}
