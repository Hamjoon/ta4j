package org.ta4j.core.indicators.numeric;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Num;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

/**
 * Unit tests for {@link NumericIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Arithmetic combinators (plus, minus, multipliedBy, dividedBy)</li>
 *   <li>Min / Max helpers</li>
 *   <li>Unary helpers (abs, sqrt, squared)</li>
 *   <li>Factory methods for other indicators (sma, ema, stddev, highest, lowest, previous)</li>
 *   <li>Rule creation methods (crossedOver, crossedUnder, isGreaterThan, isLessThan)</li>
 * </ul>
 *
 * All tests use the real {@link BaseBarSeries} implementation (which is part of the
 * public TA‑4J API) and the concrete {@link ConstantIndicator} to avoid any mocking.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private ZonedDateTime now;

    @Before
    public void setUp() {
        // a tiny series with three bars – enough to test moving‑average windows etc.
        series = new BaseBarSeries("test-series");
        now = ZonedDateTime.now(ZoneId.of("UTC"));

        // Bar 0 : close = 1, volume = 100
        series.addBar(now.minusMinutes(3), 1, 2, 0.5, 1, 100);
        // Bar 1 : close = 3, volume = 300
        series.addBar(now.minusMinutes(2), 3, 4, 2.5, 3, 300);
        // Bar 2 : close = 5, volume = 500
        series.addBar(now.minusMinutes(1), 5, 6, 4.5, 5, 500);
    }

    /** Helper to obtain a {@link Num} from the series. */
    private Num n(Number value) {
        return series.numOf(value);
    }

    @Test
    public void testDelegateAndToString() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        assertSame(ClosePriceIndicator.class, close.delegate().getClass());
        assertEquals(close.delegate().toString(), close.toString());
    }

    @Test
    public void testPlusWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        // close + constant 2
        NumericIndicator plusConst = close.plus(2);
        assertEquals(n(3), plusConst.getValue(0)); // 1 + 2
        assertEquals(n(5), plusConst.getValue(1)); // 3 + 2

        // close + volume indicator (element‑wise addition)
        NumericIndicator volume = NumericIndicator.volume(series);
        NumericIndicator plusVol = close.plus(volume);
        // 1 (close) + 100 (volume) = 101, etc.
        assertEquals(n(101), plusVol.getValue(0));
        assertEquals(n(303), plusVol.getValue(1));
    }

    @Test
    public void testMinusWithIndicatorAndNumber() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator minusConst = close.minus(1);
        assertEquals(n(0), minusConst.getValue(0));
        assertEquals(n(2), minusConst.getValue(1));

        NumericIndicator volume = NumericIndicator.volume(series);
        NumericIndicator minusVol = close.minus(volume);
        assertEquals(n(-99), minusVol.getValue(0)); // 1 - 100
        assertEquals(n(-297), minusVol.getValue(1)); // 3 - 300
    }

    @Test
    public void testMultipliedByAndDividedBy() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator multConst = close.multipliedBy(2);
        assertEquals(n(2), multConst.getValue(0));
        assertEquals(n(6), multConst.getValue(1));

        NumericIndicator divConst = close.dividedBy(2);
        assertEquals(n(0.5), divConst.getValue(0));
        assertEquals(n(1.5), divConst.getValue(1));
    }

    @Test
    public void testMinAndMax() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        NumericIndicator minConst = close.min(2);
        assertEquals(n(1), minConst.getValue(0)); // min(1,2) = 1
        assertEquals(n(2), minConst.getValue(1)); // min(3,2) = 2

        NumericIndicator maxConst = close.max(4);
        assertEquals(n(4), maxConst.getValue(0)); // max(1,4) = 4
        assertEquals(n(4), maxConst.getValue(1)); // max(3,4) = 4
    }

    @Test
    public void testAbsSqrtSquared() {
        // Create an indicator that yields negative numbers using a constant -4
        ConstantIndicator<Num> negativeFour = new ConstantIndicator<>(series, n(-4));
        NumericIndicator neg = NumericIndicator.of(negativeFour);

        // abs
        NumericIndicator abs = neg.abs();
        assertEquals(n(4), abs.getValue(0));

        // sqrt (of positive number)
        NumericIndicator sqrt = NumericIndicator.of(new ConstantIndicator<>(series, n(9))).sqrt();
        assertEquals(n(3), sqrt.getValue(0));

        // squared (self multiplication)
        NumericIndicator squared = neg.squared();
        // (-4) * (-4) = 16
        assertEquals(n(16), squared.getValue(0));
    }

    @Test
    public void testSmaAndEma() {
        NumericIndicator close = NumericIndicator.closePrice(series);

        // SMA with barCount = 2 : (1+3)/2 = 2 , (3+5)/2 = 4
        NumericIndicator sma = close.sma(2);
        assertTrue(sma.delegate() instanceof SMAIndicator);
        assertEquals(n(2), sma.getValue(1));
        assertEquals(n(4), sma.getValue(2));

        // EMA with barCount = 2 : first EMA = close[0] = 1,
        // EMA1 = (close1 - EMA0) * k + EMA0 where k = 2/(2+1)=0.666...
        NumericIndicator ema = close.ema(2);
        assertTrue(ema.delegate() instanceof EMAIndicator);
        // The EMA implementation is deterministic; we compare with the library's own result
        Num expectedEma1 = close.getValue(1).multipliedBy(n(2.0 / (2 + 1))).plus(
                close.getValue(0).multipliedBy(n(1 - 2.0 / (2 + 1))));
        assertEquals(expectedEma1, ema.getValue(1));
    }

    @Test
    public void testStdDevHighestLowest() {
        NumericIndicator close = NumericIndicator.closePrice(series);

        // Standard deviation over 2 bars
        NumericIndicator stddev = close.stddev(2);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);
        // hand‑calculate stddev for bars 0‑1: values 1 and 3 -> mean 2, variance ((1-2)^2+(3-2)^2)/2 = 2, stddev = sqrt(2)
        Num expectedStd = n(2).sqrt();
        assertEquals(expectedStd, stddev.getValue(1));

        // Highest over 2 bars: max(1,3)=3, max(3,5)=5
        NumericIndicator highest = close.highest(2);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);
        assertEquals(n(3), highest.getValue(1));
        assertEquals(n(5), highest.getValue(2));

        // Lowest over 2 bars: min(1,3)=1, min(3,5)=3
        NumericIndicator lowest = close.lowest(2);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        assertEquals(n(1), lowest.getValue(1));
        assertEquals(n(3), lowest.getValue(2));
    }

    @Test
    public void testPreviousIndicators() {
        NumericIndicator close = NumericIndicator.closePrice(series);

        // previous(1) – should give the value of the preceding bar
        NumericIndicator prev1 = (NumericIndicator) close.previous(1);
        assertTrue(prev1.delegate() instanceof PreviousValueIndicator);
        assertEquals(close.getValue(0), prev1.getValue(1));
        assertEquals(close.getValue(1), prev1.getValue(2));

        // previous() – default is previous(1)
        Indicator<Num> prevDefault = close.previous();
        assertEquals(prev1.getValue(2), prevDefault.getValue(2));
    }

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // Build a series that crosses the threshold 2 between index 0 and 1
        BarSeries crossingSeries = new BaseBarSeries("cross");
        crossingSeries.addBar(now.minusMinutes(2), 1, 2, 0.5, 1, 100);
        crossingSeries.addBar(now.minusMinutes(1), 3, 4, 2.5, 3, 300);

        NumericIndicator close = NumericIndicator.closePrice(crossingSeries);

        // crossed over constant 2 -> should be true at index 1
        Rule crossedOver = close.crossedOver(2);
        assertTrue(crossedOver.isSatisfied(1));

        // crossed under constant 2 -> false in this case
        Rule crossedUnder = close.crossedUnder(2);
        assertFalse(crossedUnder.isSatisfied(1));

        // Now a series that goes from above to below 2
        BarSeries downSeries = new BaseBarSeries("down");
        downSeries.addBar(now.minusMinutes(2), 3, 4, 2.5, 3, 300);
        downSeries.addBar(now.minusMinutes(1), 1, 2, 0.5, 1, 100);
        NumericIndicator closeDown = NumericIndicator.closePrice(downSeries);
        Rule crossedUnder2 = closeDown.crossedUnder(2);
        assertTrue(crossedUnder2.isSatisfied(1));
        Rule crossedOver2 = closeDown.crossedOver(2);
        assertFalse(crossedOver2.isSatisfied(1));
    }

    @Test
    public void testIsGreaterThanAndIsLessThanRules() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        // close[0] = 1, close[1] = 3, close[2] = 5

        Rule gt2 = close.isGreaterThan(2);
        assertFalse(gt2.isSatisfied(0)); // 1 > 2 ? false
        assertTrue(gt2.isSatisfied(1));  // 3 > 2 ? true
        assertTrue(gt2.isSatisfied(2));  // 5 > 2 ? true

        Rule lt4 = close.isLessThan(4);
        assertTrue(lt4.isSatisfied(0));  // 1 < 4
        assertTrue(lt4.isSatisfied(1));  // 3 < 4
        assertFalse(lt4.isSatisfied(2)); // 5 < 4 ? false

        // Using another indicator (constant 3) as the comparator
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(series, n(3));
        Rule gtIndicator = close.isGreaterThan(constThree);
        assertFalse(gtIndicator.isSatisfied(0)); // 1 > 3 ?
        assertFalse(gtIndicator.isSatisfied(1)); // 3 > 3 ? false (equal)
        assertTrue(gtIndicator.isSatisfied(2));  // 5 > 3
    }
}
