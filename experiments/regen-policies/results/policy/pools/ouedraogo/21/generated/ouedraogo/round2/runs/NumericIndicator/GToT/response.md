###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.indicators.numeric.NumericIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public methods (basic functionality)</li>
 *   <li>Edge‑case scenarios (division by zero, negative square‑root, equal values for min/max,
 *       insufficient history for previous indicator, etc.)</li>
 *   <li>Rule creation and basic rule satisfaction</li>
 * </ul>
 *
 * The test data consist of a small {@code BarSeries} (5 bars) with deterministic close
 * prices and volumes so that the expected results of the various indicators are easy to
 * compute by hand.
 *
 * Note: The TA‑4J library provides concrete implementations such as {@code BaseBarSeries},
 * {@code BaseBar}, {@code DecimalNum} and {@code DecimalNumFactory}.  Those are used here
 * because they are part of the public API of the library (they are not defined in the
 * source snippet but are available in the same artifact).  No mocking frameworks are
 * used – all objects are created through real constructors.
 */
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;

/**
 * Test class for {@link NumericIndicator}.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;   // close price indicator wrapped as NumericIndicator
    private NumericIndicator volume;  // volume indicator wrapped as NumericIndicator

    /**
     * Builds a tiny series (5 bars) with known close prices and volumes:
     *
     * <pre>
     * index | close | volume
     * ------+-------+-------
     *   0   |  10   |   100
     *   1   |  12   |   150
     *   2   |  11   |   120
     *   3   |  13   |   130
     *   4   |  14   |   160
     * </pre>
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");
        ZonedDateTime time = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        // Helper to create a bar with only close price and volume (other fields are dummy)
        for (int i = 0; i < 5; i++) {
            Num closePrice = DecimalNum.valueOf(10 + i + (i == 2 ? 1 : 0)); // 10,12,11,13,14
            Num volumeValue = DecimalNum.valueOf(100 + (i * 10) + (i == 1 ? 40 : 0)); // 100,150,120,130,160
            Bar bar = new BaseBar(time.plusDays(i), closePrice, closePrice, closePrice, closePrice, volumeValue);
            series.addBar(bar);
        }
        close = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    /* ----------------------------------------------------------------------
     *  Helper methods for assertions on Num values
     * ---------------------------------------------------------------------- */
    private void assertNumEquals(double expected, Num actual) {
        // DecimalNum implements equals based on exact value; we use a tolerance for safety
        assertEquals(expected, actual.doubleValue(), 1e-12);
    }

    /* ----------------------------------------------------------------------
     *  Public method list (extracted from the source)
     *
     *  static NumericIndicator of(Indicator<Num> delegate)
     *  static NumericIndicator closePrice(BarSeries bs)
     *  static NumericIndicator volume(BarSeries bs)
     *
     *  Indicator<Num> delegate()
     *  NumericIndicator plus(Indicator<Num> other)
     *  NumericIndicator plus(Number n)
     *  NumericIndicator minus(Indicator<Num> other)
     *  NumericIndicator minus(Number n)
     *  NumericIndicator multipliedBy(Indicator<Num> other)
     *  NumericIndicator multipliedBy(Number n)
     *  NumericIndicator dividedBy(Indicator<Num> other)
     *  NumericIndicator dividedBy(Number n)
     *  NumericIndicator min(Indicator<Num> other)
     *  NumericIndicator min(Number n)
     *  NumericIndicator max(Indicator<Num> other)
     *  NumericIndicator max(Number n)
     *  NumericIndicator abs()
     *  NumericIndicator sqrt()
     *  NumericIndicator squared()
     *  NumericIndicator sma(int barCount)
     *  NumericIndicator ema(int barCount)
     *  NumericIndicator stddev(int barCount)
     *  NumericIndicator highest(int barCount)
     *  NumericIndicator lowest(int barCount)
     *  NumericIndicator previous(int barCount)
     *  Indicator<Num> previous()
     *  Rule crossedOver(Indicator<Num> other)
     *  Rule crossedOver(Number n)
     *  Rule crossedUnder(Indicator<Num> other)
     *  Rule crossedUnder(Number n)
     *  Rule isGreaterThan(Indicator<Num> other)
     *  Rule isGreaterThan(Number n)
     *  Rule isLessThan(Indicator<Num> other)
     *  Rule isLessThan(Number n)
     *
     *  Num getValue(int index)
     *  int getUnstableBars()
     *  BarSeries getBarSeries()
     *  String toString()
     * ---------------------------------------------------------------------- */

    /* ----------------------------------------------------------------------
     *  BASIC FUNCTIONALITY TESTS
     * ---------------------------------------------------------------------- */

    @Test
    public void testDelegate() {
        assertSame(close.delegate(), close.delegate());
        assertEquals(close.delegate().getClass(), ClosePriceIndicator.class);
    }

    @Test
    public void testPlusIndicator() {
        NumericIndicator result = close.plus(volume);
        // close(0)=10, volume(0)=100 => 110
        assertNumEquals(110, result.getValue(0));
        // close(4)=14, volume(4)=160 => 174
        assertNumEquals(174, result.getValue(4));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator result = close.plus(5);
        assertNumEquals(15, result.getValue(0)); // 10 + 5
        assertNumEquals(19, result.getValue(4)); // 14 + 5
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator result = close.minus(volume);
        assertNumEquals(-90, result.getValue(0)); // 10 - 100
        assertNumEquals(-146, result.getValue(4)); // 14 - 160
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator result = close.minus(3);
        assertNumEquals(7, result.getValue(0));   // 10 - 3
        assertNumEquals(11, result.getValue(4));  // 14 - 3
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator result = close.multipliedBy(volume);
        assertNumEquals(1000, result.getValue(0)); // 10 * 100
        assertNumEquals(2240, result.getValue(4)); // 14 * 160
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator result = close.multipliedBy(2);
        assertNumEquals(20, result.getValue(0));
        assertNumEquals(28, result.getValue(4));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator result = volume.dividedBy(close);
        // 100 / 10 = 10
        assertNumEquals(10, result.getValue(0));
        // 160 / 14 ≈ 11.428571...
        assertNumEquals(160.0 / 14.0, result.getValue(4));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator result = close.dividedBy(2);
        assertNumEquals(5, result.getValue(0));
        assertNumEquals(7, result.getValue(4));
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator result = close.min(volume);
        // At index 0: min(10,100)=10
        assertNumEquals(10, result.getValue(0));
        // At index 4: min(14,160)=14
        assertNumEquals(14, result.getValue(4));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator result = close.min(12);
        // Index 0: min(10,12)=10
        assertNumEquals(10, result.getValue(0));
        // Index 1: min(12,12)=12 (equal case – should return this)
        assertNumEquals(12, result.getValue(1));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator result = close.max(volume);
        // Index 0: max(10,100)=100
        assertNumEquals(100, result.getValue(0));
        // Index 4: max(14,160)=160
        assertNumEquals(160, result.getValue(4));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator result = close.max(12);
        // Index 0: max(10,12)=12
        assertNumEquals(12, result.getValue(0));
        // Index 1: max(12,12)=12 (equal case – should return this)
        assertNumEquals(12, result.getValue(1));
    }

    @Test
    public void testAbs() {
        // Create a negative indicator by subtracting a bigger constant
        NumericIndicator negative = close.minus(20); // values: -10,-8,-9,-7,-6
        NumericIndicator abs = negative.abs();
        assertNumEquals(10, abs.getValue(0));
        assertNumEquals(6, abs.getValue(4));
    }

    @Test
    public void testSqrt() {
        NumericIndicator result = close.sqrt();
        assertNumEquals(Math.sqrt(10), result.getValue(0));
        assertNumEquals(Math.sqrt(14), result.getValue(4));
    }

    @Test
    public void testSquared() {
        NumericIndicator result = close.squared();
        assertNumEquals(100, result.getValue(0));
        assertNumEquals(196, result.getValue(4));
    }

    @Test
    public void testSma() {
        NumericIndicator sma3 = close.sma(3);
        // SMA(3) at index 2 = (10+12+11)/3 = 11
        assertNumEquals(11, sma3.getValue(2));
        // SMA(3) at index 4 = (11+13+14)/3 = 12.666...
        assertNumEquals((11 + 13 + 14) / 3.0, sma3.getValue(4));
    }

    @Test
    public void testEma() {
        // EMA with barCount=2 (simple exponential smoothing)
        NumericIndicator ema2 = close.ema(2);
        // EMA(0) = close(0) = 10
        assertNumEquals(10, ema2.getValue(0));
        // EMA(1) = (close(1) * 2/3) + (EMA(0) * 1/3) = 12*0.666... + 10*0.333...
        double expectedEma1 = 12 * (2.0 / (2 + 1)) + 10 * (1.0 / (2 + 1));
        assertNumEquals(expectedEma1, ema2.getValue(1));
    }

    @Test
    public void testStdDev() {
        NumericIndicator stddev2 = close.stddev(2);
        // For barCount=2, standard deviation at index 1 is sqrt(((10-11)^2 + (12-11)^2)/2) = sqrt(2/2) = 1
        assertNumEquals(1.0, stddev2.getValue(1));
    }

    @Test
    public void testHighest() {
        NumericIndicator highest3 = close.highest(3);
        // Highest among (10,12,11) = 12 at index 2
        assertNumEquals(12, highest3.getValue(2));
        // Highest among (11,13,14) = 14 at index 4
        assertNumEquals(14, highest3.getValue(4));
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest3 = close.lowest(3);
        // Lowest among (10,12,11) = 10 at index 2
        assertNumEquals(10, lowest3.getValue(2));
        // Lowest among (11,13,14) = 11 at index 4
        assertNumEquals(11, lowest3.getValue(4));
    }

    @Test
    public void testPreviousWithArgument() {
        NumericIndicator prev2 = close.previous(2);
        // At index 2, previous(2) returns value at index 0 = 10
        assertNumEquals(10, prev2.getValue(2));
        // At index 4, previous(2) returns value at index 2 = 11
        assertNumEquals(11, prev2.getValue(4));
    }

    @Test
    public void testPreviousDefault() {
        Indicator<Num> prev1 = close.previous(); // barCount = 1
        // At index 3, previous(1) returns value at index 2 = 11
        assertNumEquals(11, prev1.getValue(3));
    }

    /* ----------------------------------------------------------------------
     *  EDGE CASES & EXCEPTION SCENARIOS
     * ---------------------------------------------------------------------- */

    @Test
    public void testDivisionByZeroNumber() {
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.ZERO));
        NumericIndicator result = close.dividedBy(zero);
        // Division by zero yields NaN according to DecimalNum implementation
        assertTrue(result.getValue(0).isNaN());
        assertTrue(result.getValue(4).isNaN());
    }

    @Test
    public void testDivisionByZeroIndicator() {
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.ZERO));
        NumericIndicator result = close.dividedBy(zero);
        assertTrue(result.getValue(2).isNaN());
    }

    @Test
    public void testSqrtOfNegativeNumber() {
        NumericIndicator negative = close.minus(20); // values are negative
        NumericIndicator sqrt = negative.sqrt();
        // sqrt of negative numbers yields NaN
        assertTrue(sqrt.getValue(0).isNaN());
    }

    @Test
    public void testMinMaxWhenEqual() {
        NumericIndicator equal = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(5)));
        NumericIndicator min = equal.min(5);
        NumericIndicator max = equal.max(5);
        // When equal, the original indicator should be returned (value 5)
        assertNumEquals(5, min.getValue(0));
        assertNumEquals(5, max.getValue(0));
    }

    @Test
    public void testPreviousInsufficientHistory() {
        // previous(5) on a series of length 5 -> index 0 has no predecessor
        NumericIndicator prev5 = close.previous(5);
        // According to TA‑4J, when there is no previous value, the indicator returns NaN
        assertTrue(prev5.getValue(0).isNaN());
        // Index 4 has predecessor at index -1 => also NaN
        assertTrue(prev5.getValue(4).isNaN());
    }

    /* ----------------------------------------------------------------------
     *  RULE CREATION AND BASIC SATISFACTION TESTS
     * ---------------------------------------------------------------------- */

    @Test
    public void testCrossedOverIndicator() {
        // Create a simple indicator that is always 12
        NumericIndicator constant12 = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(12)));
        Rule rule = close.crossedOver(constant12);
        // crossed over occurs when close goes from <=12 to >12
        // close values: 10,12,11,13,14 => crossing between index 2 (11) and 3 (13)
        assertFalse(rule.isSatisfied(2));
        assertTrue(rule.isSatisfied(3));
    }

    @Test
    public void testCrossedOverNumber() {
        Rule rule = close.crossedOver(12);
        assertFalse(rule.isSatisfied(2));
        assertTrue(rule.isSatisfied(3));
    }

    @Test
    public void testCrossedUnderIndicator() {
        NumericIndicator constant12 = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(12)));
        Rule rule = close.crossedUnder(constant12);
        // crossed under occurs when close goes from >=12 to <12
        // crossing between index 1 (12) and 2 (11)
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(12);
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        NumericIndicator constant11 = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(11)));
        Rule rule = close.isGreaterThan(constant11);
        // close > 11 from index 1 onward except index 2 (11)
        assertFalse(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
        assertTrue(rule.isSatisfied(3));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(11);
        assertFalse(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsLessThanIndicator() {
        NumericIndicator constant12 = NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(12)));
        Rule rule = close.isLessThan(constant12);
        // close < 12 at index 0,2,4 (4 is 14 >12 so false)
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
        assertFalse(rule.isSatisfied(3));
        assertFalse(rule.isSatisfied(4));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(12);
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    /* ----------------------------------------------------------------------
     *  TO_STRING & UNSTABLE BARS
     * ---------------------------------------------------------------------- */

    @Test
    public void testToStringDelegates() {
        // toString of NumericIndicator should be the delegate's toString
        String expected = close.delegate().toString();
        assertEquals(expected, close.toString());
    }

    @Test
    public void testGetUnstableBars() {
        // NumericIndicator reports 0 unstable bars (it does not cache)
        assertEquals(0, close.getUnstableBars());
    }
}
```
###Test END##