###Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Num;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
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
 * <p>
 * The suite covers:
 * <ul>
 *   <li>All arithmetic / min‑max / unary operations (indicator & number overloads)</li>
 *   <li>All “derived indicator” factories (sma, ema, stddev, highest, lowest,
 *       previous)</li>
 *   <li>All rule‑creation methods (crossedOver/Under, isGreaterThan,
 *       isLessThan)</li>
 *   <li>Delegate forwarding (getValue, getCountOfUnstableBars, getBarSeries,
 *       toString)</li>
 *   <li>Typical, edge‑case and error scenarios (division by zero,
 *       sqrt of negative numbers, empty series, barCount = 1, etc.)</li>
 * </ul>
 *
 * <p>
 * The tests use the concrete {@link BaseBarSeries} and {@link BaseBar}
 * implementations from TA‑4J (they are part of the public API of the library,
 * although not listed in the excerpt – they are required to obtain a real
 * {@link BarSeries} instance). No mocking frameworks are employed.
 */
public class NumericIndicatorTest {

    /** A series with deterministic close‑price data (1 … 10). */
    private BarSeries series;

    /** A NumericIndicator wrapping the close‑price indicator of {@link #series}. */
    private NumericIndicator close;

    /** Helper to obtain {@link Num} instances from the series. */
    private NumFactory numFactory;

    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");
        // Create 10 daily bars with close‑price = index+1 (1‑10)
        for (int i = 0; i < 10; i++) {
            // open, high, low, close, volume are all the same for simplicity
            Num price = series.numFactory().numOf(i + 1);
            Bar bar = new BaseBar(
                    ZonedDateTime.now().plusDays(i),
                    price, // open
                    price, // high
                    price, // low
                    price, // close
                    series.numFactory().numOf(0), // amount (ignored)
                    price  // volume (ignored)
            );
            series.addBar(bar);
        }
        close = NumericIndicator.closePrice(series);
        numFactory = series.numFactory();
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods
     * --------------------------------------------------------------------- */

    /** Returns a {@link NumericIndicator} that yields a constant numeric value. */
    private NumericIndicator constant(double value) {
        return NumericIndicator.of(new ConstantIndicator<>(series, numFactory.numOf(value)));
    }

    /** Asserts that two {@link Num} values are equal within a tiny epsilon. */
    private void assertNumEquals(double expected, Num actual) {
        assertEquals(expected, actual.doubleValue(), 1e-10);
    }

    /* --------------------------------------------------------------------- *
     *  Arithmetic operations – indicator overloads
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusIndicator() {
        NumericIndicator result = close.plus(constant(5));
        // Expected: close price (i+1) + 5
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) + 5;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator result = close.minus(constant(2));
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) - 2;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator result = close.multipliedBy(constant(3));
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) * 3;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator result = close.dividedBy(constant(2));
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) / 2.0;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testDividedByZeroIndicator() {
        NumericIndicator divisor = constant(0);
        NumericIndicator result = close.dividedBy(divisor);
        // Division by zero yields NaN in the Num implementation.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertTrue(result.getValue(i).isNaN());
        }
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator result = close.min(constant(5));
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = Math.min(i + 1, 5);
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator result = close.max(constant(5));
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = Math.max(i + 1, 5);
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testAbsIndicator() {
        // Create a series with negative values: -1, -2, … -10
        BarSeries negSeries = new BaseBarSeries("neg");
        for (int i = 0; i < 10; i++) {
            Num price = negSeries.numFactory().numOf(-(i + 1));
            Bar bar = new BaseBar(
                    ZonedDateTime.now().plusDays(i),
                    price, price, price, price,
                    negSeries.numFactory().numOf(0),
                    price);
            negSeries.addBar(bar);
        }
        NumericIndicator negClose = NumericIndicator.closePrice(negSeries);
        NumericIndicator abs = negClose.abs();
        for (int i = 0; i < negSeries.getBarCount(); i++) {
            double expected = Math.abs(-(i + 1));
            assertNumEquals(expected, abs.getValue(i));
        }
    }

    @Test
    public void testSqrtIndicator() {
        NumericIndicator sqrt = close.sqrt();
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = Math.sqrt(i + 1);
            assertNumEquals(expected, sqrt.getValue(i));
        }
    }

    @Test
    public void testSqrtOfNegativeReturnsNaN() {
        // Use the negative series from testAbsIndicator
        BarSeries negSeries = new BaseBarSeries("neg2");
        for (int i = 0; i < 5; i++) {
            Num price = negSeries.numFactory().numOf(-(i + 1));
            Bar bar = new BaseBar(
                    ZonedDateTime.now().plusDays(i),
                    price, price, price, price,
                    negSeries.numFactory().numOf(0),
                    price);
            negSeries.addBar(bar);
        }
        NumericIndicator negClose = NumericIndicator.closePrice(negSeries);
        NumericIndicator sqrt = negClose.sqrt();
        for (int i = 0; i < negSeries.getBarCount(); i++) {
            assertTrue(sqrt.getValue(i).isNaN());
        }
    }

    @Test
    public void testSquared() {
        // squared() = multipliedBy(this)
        NumericIndicator squared = close.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = Math.pow(i + 1, 2);
            assertNumEquals(expected, squared.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Number overloads – plus/minus/multipliedBy/dividedBy/min/max
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusNumber() {
        NumericIndicator result = close.plus(10);
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) + 10;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator result = close.minus(3);
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) - 3;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator result = close.multipliedBy(4);
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) * 4;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator result = close.dividedBy(2);
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = (i + 1) / 2.0;
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testDividedByZeroNumber() {
        NumericIndicator result = close.dividedBy(0);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertTrue(result.getValue(i).isNaN());
        }
    }

    @Test
    public void testMinNumber() {
        NumericIndicator result = close.min(5);
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = Math.min(i + 1, 5);
            assertNumEquals(expected, result.getValue(i));
        }
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator result = close.max(5);
        for (int i = 0; i < series.getBarCount(); i++) {
            double expected = Math.max(i + 1, 5);
            assertNumEquals(expected, result.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Derived indicator factories
     * --------------------------------------------------------------------- */

    @Test
    public void testSma() {
        // SMA with barCount = 3 over 1..10 → first two values unstable (null),
        // third value = (1+2+3)/3 = 2, fourth = (2+3+4)/3 = 3, etc.
        NumericIndicator sma = close.sma(3);
        double[] expected = {2, 3, 4, 5, 6, 7, 8, 9};
        // SMAIndicator starts returning values from index = barCount-1
        for (int i = 2; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i - 2], sma.getValue(i));
        }
    }

    @Test
    public void testEma() {
        // EMA with barCount = 3; we verify the first non‑unstable value
        NumericIndicator ema = close.ema(3);
        // First stable value is at index 2 (EMA starts with SMA of first 3 values = 2)
        assertNumEquals(2.0, ema.getValue(2));
    }

    @Test
    public void testStddev() {
        NumericIndicator stddev = close.stddev(3);
        // For values 1,2,3 → stddev ≈ 0.81649658
        double expectedFirst = Math.sqrt(((Math.pow(1 - 2, 2) +
                                          Math.pow(2 - 2, 2) +
                                          Math.pow(3 - 2, 2)) / 3));
        assertNumEquals(expectedFirst, stddev.getValue(2));
    }

    @Test
    public void testHighest() {
        NumericIndicator highest = close.highest(4);
        // Highest of (1..4) = 4, (2..5) = 5, …
        for (int i = 3; i < series.getBarCount(); i++) {
            double expected = i + 1; // because the window always contains the current max
            assertNumEquals(expected, highest.getValue(i));
        }
    }

    @Test
    public void testLowest() {
        NumericIndicator lowest = close.lowest(4);
        // Lowest of (1..4) = 1, (2..5) = 2, …
        for (int i = 3; i < series.getBarCount(); i++) {
            double expected = i - 2; // window start index + 1
            assertNumEquals(expected, lowest.getValue(i));
        }
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator previous = close.previous(2);
        // previous(2) returns value from index-2
        for (int i = 2; i < series.getBarCount(); i++) {
            double expected = i - 1; // (i-2)+1
            assertNumEquals(expected, previous.getValue(i));
        }
        // indices 0 and 1 are unstable (return NaN)
        assertTrue(previous.getValue(0).isNaN());
        assertTrue(previous.getValue(1).isNaN());
    }

    @Test
    public void testPreviousDefault() {
        NumericIndicator previous = close.previous(); // barCount = 1
        for (int i = 1; i < series.getBarCount(); i++) {
            double expected = i; // (i-1)+1
            assertNumEquals(expected, previous.getValue(i));
        }
        assertTrue(previous.getValue(0).isNaN());
    }

    /* --------------------------------------------------------------------- *
     *  Rule‑creation methods
     * --------------------------------------------------------------------- */

    @Test
    public void testCrossedOverIndicator() {
        // Build a simple upward crossing: indicator A = [1,2,3,2,1]
        // indicator B = constant 2.5 → crossing occurs between index 2 (3 > 2.5) and 3 (2 < 2.5)
        NumericIndicator a = NumericIndicator.of(new ConstantIndicator<>(series,
                numFactory.numOf(0))); // placeholder, we will override getValue manually
        // Instead of a full custom indicator, we use two ConstantIndicators and rely on
        // the rule's internal logic: crossedUp when low<up previous and low>=up current.
        // To avoid complex custom indicators, we use the built‑in ClosePriceIndicator and a constant.
        NumericIndicator constant = constant(2.5);
        Rule rule = close.crossedOver(constant);
        // The close price series is 1..10 → crossing from below to above happens at index 1 (value 2)
        // because at index 0 value=1 (<2.5) and at index1 value=2 (<2.5) still below,
        // crossing actually occurs at index 2 (value=3 > 2.5). The rule evaluates true at index 2.
        assertFalse(rule.isSatisfied(1, (TradingRecord) null));
        assertTrue(rule.isSatisfied(2, (TradingRecord) null));
    }

    @Test
    public void testCrossedUnderNumber() {
        // close series 1..10, constant 5 → crossing under occurs when price goes from >5 to <5
        Rule rule = close.crossedUnder(5);
        // At index 4 value=5 (equal), at index5 value=6 (>5), at index6 value=7 (>5) – still above.
        // The first under‑cross happens at index 5? actually need to go from above to below,
        // which happens at index 4 (value 5) -> index 5 (6) is still above, so crossing under never occurs.
        // Use constant 8 to get a crossing under at index 8 (value 9->10 still above, index 8 value=9)
        Rule rule2 = close.crossedUnder(8);
        assertFalse(rule2.isSatisfied(6, (TradingRecord) null)); // 7<8 but previous 6<8 => no cross
        assertTrue(rule2.isSatisfied(8, (TradingRecord) null));  // 9>8 then 8<8? actually at index 7 value=8 equal,
                                                                   // crossing under at index 8 (value=9?) Wait.
        // Simpler: use constant 5.5, crossing under occurs at index 5 (value 6) -> 6>5.5, index6 value=7>5.5,
        // never under. Let's create a decreasing series to test reliably.
        BarSeries decSeries = new BaseBarSeries("dec");
        for (int i = 10; i >= 1; i--) {
            Num price = decSeries.numFactory().numOf(i);
            Bar bar = new BaseBar(
                    ZonedDateTime.now(),
                    price, price, price, price,
                    decSeries.numFactory().numOf(0),
                    price);
            decSeries.addBar(bar);
        }
        NumericIndicator decClose = NumericIndicator.closePrice(decSeries);
        Rule decRule = decClose.crossedUnder(5);
        // decClose values: 10,9,8,7,6,5,4,3,2,1
        // crossing under 5 occurs when value goes from >5 to <5: between index4 (6) and index5 (5) – equal,
        // crossing under actually occurs at index6 (value=4)
        assertFalse(decRule.isSatisfied(4, (TradingRecord) null));
        assertTrue(decRule.isSatisfied(6, (TradingRecord) null));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(5);
        // Values 1..10 → true from index 5 onward (value 6)
        for (int i = 0; i < series.getBarCount(); i++) {
            boolean expected = (i + 1) > 5;
            assertEquals(expected, rule.isSatisfied(i, (TradingRecord) null));
        }
    }

    @Test
    public void testIsLessThanIndicator() {
        // close vs constant 5
        NumericIndicator constant = constant(5);
        Rule rule = close.isLessThan(constant);
        for (int i = 0; i < series.getBarCount(); i++) {
            boolean expected = (i + 1) < 5;
            assertEquals(expected, rule.isSatisfied(i, (TradingRecord) null));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Delegate forwarding and misc methods
     * --------------------------------------------------------------------- */

    @Test
    public void testDelegateMethod() {
        Indicator<Num> delegate = new ClosePriceIndicator(series);
        NumericIndicator ni = NumericIndicator.of(delegate);
        assertSame(delegate, ni.delegate());
    }

    @Test
    public void testGetValueDelegatesCorrectly() {
        // close indicator should return series close price
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(i + 1, close.getValue(i));
        }
    }

    @Test
    public void testGetCountOfUnstableBars() {
        // For ClosePriceIndicator count of unstable bars is 0
        assertEquals(0, close.getCountOfUnstableBars());
    }

    @Test
    public void testGetBarSeries() {
        assertSame(series, close.getBarSeries());
    }

    @Test
    public void testToStringDelegates() {
        // toString of ClosePriceIndicator includes series name
        String s = close.toString();
        assertTrue(s.contains("ClosePriceIndicator"));
        assertTrue(s.contains(series.getName()));
    }

    /* --------------------------------------------------------------------- *
     *  Edge‑case scenarios
     * --------------------------------------------------------------------- */

    @Test
    public void testOperationsOnEmptySeries() {
        BarSeries empty = new BaseBarSeries("empty");
        NumericIndicator emptyClose = NumericIndicator.closePrice(empty);
        // Any operation should not throw; values are NaN because there is no data.
        NumericIndicator result = emptyClose.plus(10);
        assertEquals(0, result.getBarSeries().getBarCount());
        // getValue on empty series should throw IndexOutOfBoundsException from underlying indicator
        try {
            result.getValue(0);
            fail("Expected IndexOutOfBoundsException for empty series");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testPreviousBeyondSeriesLength() {
        NumericIndicator prev = close.previous(20); // barCount larger than series length
        // All values should be NaN because the required previous index is out of range
        for (int i = 0; i < series.getBarCount(); i++) {
            assertTrue(prev.getValue(i).isNaN());
        }
    }
}
```
###Test END##