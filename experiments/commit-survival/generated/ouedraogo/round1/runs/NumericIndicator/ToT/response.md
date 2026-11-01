###Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.TradingRecord;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>The tests are written from the point of view of three “experts”.  Each
 * expert contributed one test case per public method; the final suite merges
 * all contributions, adds edge‑case checks and ensures that the fluent API
 * behaves correctly.</p>
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;
    private NumericIndicator volume;

    @Before
    public void setUp() {
        // Build a tiny BarSeries (3 bars) with deterministic values.
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .withMaximumBarCount(10)
                .build();

        // Helper to create a Bar with given close and volume.
        ZonedDateTime now = ZonedDateTime.now();
        addBar(series, now.minusMinutes(3), 10, 12, 9, 11, 100);
        addBar(series, now.minusMinutes(2), 11, 13, 10, 12, 200);
        addBar(series, now.minusMinutes(1), 12, 14, 11, 13, 300);
        // Fluent wrappers
        close = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    /** Utility to add a bar to a series using {@link DecimalNum}. */
    private static void addBar(BarSeries s, ZonedDateTime endTime,
                              Number open, Number high, Number low,
                              Number close, Number volume) {
        Num openNum = DecimalNum.valueOf(open);
        Num highNum = DecimalNum.valueOf(high);
        Num lowNum  = DecimalNum.valueOf(low);
        Num closeNum = DecimalNum.valueOf(close);
        Num volNum  = DecimalNum.valueOf(volume);
        s.addBar(endTime, openNum, highNum, lowNum, closeNum, volNum);
    }

    /* --------------------------------------------------------------------- *
     *  Factory methods
     * --------------------------------------------------------------------- */

    @Test
    public void testOf() {
        NumericIndicator ni = NumericIndicator.of(close);
        assertSame("Delegate should be the same instance", close.delegate(), ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertTrue(cp.delegate() instanceof ClosePriceIndicator);
        assertEquals(series, cp.getBarSeries());
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertTrue(vol.delegate() instanceof VolumeIndicator);
        assertEquals(series, vol.getBarSeries());
    }

    /* --------------------------------------------------------------------- *
     *  Arithmetic operations (indicator & number overloads)
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusIndicator() {
        NumericIndicator result = close.plus(volume);
        // 11+100 = 111, 12+200 = 212, 13+300 = 313
        Num[] expected = {
                DecimalNum.valueOf(111),
                DecimalNum.valueOf(212),
                DecimalNum.valueOf(313)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator result = close.plus(5);
        // 11+5=16, 12+5=17, 13+5=18
        Num[] expected = {
                DecimalNum.valueOf(16),
                DecimalNum.valueOf(17),
                DecimalNum.valueOf(18)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator result = close.minus(volume);
        // 11-100 = -89, 12-200 = -188, 13-300 = -287
        Num[] expected = {
                DecimalNum.valueOf(-89),
                DecimalNum.valueOf(-188),
                DecimalNum.valueOf(-287)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator result = close.minus(2);
        // 11-2=9, 12-2=10, 13-2=11
        Num[] expected = {
                DecimalNum.valueOf(9),
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(11)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator result = close.multipliedBy(volume);
        // 11*100=1100, 12*200=2400, 13*300=3900
        Num[] expected = {
                DecimalNum.valueOf(1100),
                DecimalNum.valueOf(2400),
                DecimalNum.valueOf(3900)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator result = close.multipliedBy(3);
        // 11*3=33, 12*3=36, 13*3=39
        Num[] expected = {
                DecimalNum.valueOf(33),
                DecimalNum.valueOf(36),
                DecimalNum.valueOf(39)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testDividedByIndicator() {
        // Use volume as divisor – values are safe (non‑zero)
        NumericIndicator result = close.dividedBy(volume);
        // 11/100=0.11, 12/200=0.06, 13/300≈0.043333...
        Num[] expected = {
                DecimalNum.valueOf(0.11),
                DecimalNum.valueOf(0.06),
                DecimalNum.valueOf(0.04333333333333333)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].doubleValue(), result.getValue(i).doubleValue(), 1e-12);
        }
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator result = close.dividedBy(2);
        // 11/2=5.5, 12/2=6, 13/2=6.5
        Num[] expected = {
                DecimalNum.valueOf(5.5),
                DecimalNum.valueOf(6),
                DecimalNum.valueOf(6.5)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroNumber() {
        // Division by zero should throw an ArithmeticException from DecimalNum
        close.dividedBy(0).getValue(0);
    }

    /* --------------------------------------------------------------------- *
     *  Min / Max
     * --------------------------------------------------------------------- */

    @Test
    public void testMinIndicator() {
        // Compare close (11,12,13) with constant 12
        NumericIndicator const12 = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(12)));
        NumericIndicator result = close.min(const12);
        Num[] expected = {
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(12)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMinNumber() {
        NumericIndicator result = close.min(12);
        // Same expectation as above
        Num[] expected = {
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(12)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator const12 = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(12)));
        NumericIndicator result = close.max(const12);
        Num[] expected = {
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(13)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator result = close.max(12);
        Num[] expected = {
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(13)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Unary operations
     * --------------------------------------------------------------------- */

    @Test
    public void testAbs() {
        // Create a negative constant indicator
        NumericIndicator negative = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(-5)));
        NumericIndicator result = negative.abs();
        assertEquals(DecimalNum.valueOf(5), result.getValue(0));
    }

    @Test
    public void testSqrt() {
        NumericIndicator result = close.sqrt();
        // sqrt(11)≈3.31662479, sqrt(12)≈3.46410162, sqrt(13)≈3.60555128
        Num[] expected = {
                DecimalNum.valueOf(Math.sqrt(11)),
                DecimalNum.valueOf(Math.sqrt(12)),
                DecimalNum.valueOf(Math.sqrt(13))
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].doubleValue(), result.getValue(i).doubleValue(), 1e-12);
        }
    }

    @Test
    public void testSquared() {
        NumericIndicator result = close.squared();
        Num[] expected = {
                DecimalNum.valueOf(121), // 11*11
                DecimalNum.valueOf(144), // 12*12
                DecimalNum.valueOf(169)  // 13*13
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Cached indicators (SMA, EMA, StdDev, Highest, Lowest, Previous)
     * --------------------------------------------------------------------- */

    @Test
    public void testSma() {
        NumericIndicator sma = close.sma(2); // simple moving average of last 2 closes
        // (11+12)/2 = 11.5, (12+13)/2 = 12.5
        Num[] expected = {
                DecimalNum.valueOf(11.5),
                DecimalNum.valueOf(12.5)
        };
        // SMA starts from index 1 because it needs 2 values
        assertEquals(expected[0], sma.getValue(1));
        assertEquals(expected[1], sma.getValue(2));
    }

    @Test
    public void testEma() {
        NumericIndicator ema = close.ema(2);
        // EMA calculation using default smoothing (2/(n+1) = 0.666...)
        // First EMA = first close = 11
        // EMA2 = (12 - 11) * 0.666... + 11 = 11.666...
        // EMA3 = (13 - 11.666...) * 0.666... + 11.666... = 12.555...
        Num first = DecimalNum.valueOf(11);
        Num second = DecimalNum.valueOf(11).plus(
                DecimalNum.valueOf(12).minus(DecimalNum.valueOf(11))
                        .multipliedBy(DecimalNum.valueOf(2.0/3.0)));
        Num third = second.plus(
                DecimalNum.valueOf(13).minus(second)
                        .multipliedBy(DecimalNum.valueOf(2.0/3.0)));
        assertEquals(first, ema.getValue(0));
        assertEquals(second, ema.getValue(1));
        assertEquals(third, ema.getValue(2));
    }

    @Test
    public void testStdDev() {
        NumericIndicator stddev = close.stddev(3);
        // Standard deviation of {11,12,13} = sqrt( ((1^2)+(0^2)+(1^2))/3 ) = sqrt(2/3)
        double expected = Math.sqrt(2.0/3.0);
        assertEquals(DecimalNum.valueOf(expected).doubleValue(),
                stddev.getValue(2).doubleValue(),
                1e-12);
    }

    @Test
    public void testHighest() {
        NumericIndicator highest = close.highest(2);
        // For index 1: max(11,12)=12 ; index 2: max(12,13)=13
        assertEquals(DecimalNum.valueOf(12), highest.getValue(1));
        assertEquals(DecimalNum.valueOf(13), highest.getValue(2));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest = close.lowest(2);
        // For index 1: min(11,12)=11 ; index 2: min(12,13)=12
        assertEquals(DecimalNum.valueOf(11), lowest.getValue(1));
        assertEquals(DecimalNum.valueOf(12), lowest.getValue(2));
    }

    @Test
    public void testPreviousWithArgument() {
        NumericIndicator prev2 = close.previous(2);
        // previous(2) at index 2 should give value at index 0 -> 11
        assertEquals(DecimalNum.valueOf(11), prev2.getValue(2));
    }

    @Test
    public void testPreviousNoArgument() {
        Indicator<Num> prev1 = close.previous(); // previous(1)
        // At index 2 we expect value at index 1 -> 12
        assertEquals(DecimalNum.valueOf(12), prev1.getValue(2));
    }

    /* --------------------------------------------------------------------- *
     *  Rule factories
     * --------------------------------------------------------------------- */

    @Test
    public void testCrossedOverIndicator() {
        // Create a fast SMA (2) and a slow SMA (3) to force a crossover
        NumericIndicator fast = close.sma(2);
        NumericIndicator slow = close.sma(3);
        Rule rule = fast.crossedOver(slow);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // At index 2 fast (12.5) > slow (12) => crossover should be true
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedOverNumber() {
        Rule rule = close.crossedOver(12);
        // At index 2 close=13 > 12 and previous close=12 == 12 => crossing occurs
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderIndicator() {
        NumericIndicator fast = close.sma(2);
        NumericIndicator slow = close.sma(3);
        Rule rule = fast.crossedUnder(slow);
        assertTrue(rule instanceof CrossedDownIndicatorRule);
        // At index 1 fast (11.5) < slow (11.5) is false, but at index 0 we cannot test crossing.
        // Use index 2 where fast (12.5) > slow (12) => not crossed under.
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(12);
        // At index 0 close=11 < 12 but previous index does not exist → false
        // At index 1 close=12 == 12 → false
        // At index 2 close=13 > 12 → false
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        Rule rule = close.isGreaterThan(volume);
        // volume values are 100,200,300 – close never > volume → false
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(12);
        // close values: 11,12,13 → only index 2 satisfies
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testIsLessThanIndicator() {
        Rule rule = close.isLessThan(volume);
        // All close values are less than volume → true for all indices
        assertTrue(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(12);
        // close values: 11 (<12), 12 (!<12), 13 (!<12)
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
    }

    /* --------------------------------------------------------------------- *
     *  Delegated methods
     * --------------------------------------------------------------------- */

    @Test
    public void testGetValueDelegates() {
        // close is a wrapper around ClosePriceIndicator; verify delegation.
        assertEquals(DecimalNum.valueOf(11), close.getValue(0));
        assertEquals(DecimalNum.valueOf(12), close.getValue(1));
        assertEquals(DecimalNum.valueOf(13), close.getValue(2));
    }

    @Test
    public void testGetUnstableBars() {
        // NumericIndicator reports 0 unstable bars regardless of delegate.
        assertEquals(0, close.getUnstableBars());
    }

    @Test
    public void testGetBarSeries() {
        assertSame(series, close.getBarSeries());
    }

    @Test
    public void testToStringDelegates() {
        assertEquals(close.delegate().toString(), close.toString());
    }

    /* --------------------------------------------------------------------- *
     *  Helper class to build a BaseBarSeries (ta4j provides a builder)
     * --------------------------------------------------------------------- */

    /**
     * Minimal builder that mirrors {@code BaseBarSeriesBuilder} from ta4j.
     * It is included here to keep the test self‑contained and avoid external
     * factory usage that is not part of the listed API.
     */
    private static class BaseBarSeriesBuilder {
        private String name = "default";
        private int maxBarCount = Integer.MAX_VALUE;

        BaseBarSeriesBuilder withName(String name) {
            this.name = name;
            return this;
        }

        BaseBarSeriesBuilder withMaximumBarCount(int max) {
            this.maxBarCount = max;
            return this;
        }

        BarSeries build() {
            BaseBarSeries series = new BaseBarSeries(name);
            series.setMaximumBarCount(maxBarCount);
            return series;
        }
    }
}
```
###Test END##