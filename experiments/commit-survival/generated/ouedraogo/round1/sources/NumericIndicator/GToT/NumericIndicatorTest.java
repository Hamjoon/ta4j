package org.ta4j.core.indicators.numeric;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Num;
import org.ta4j.core.Rule;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.trading.TradingRecord;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>The test class covers:
 * <ul>
 *   <li>All public methods (static factories, arithmetic, aggregation, rule creation,
 *       and delegating methods).</li>
 *   <li>Typical use‑cases (e.g. plus, minus, sma, crossedOver).</li>
 *   <li>Edge‑cases and exception scenarios (division by zero, sqrt of a negative
 *       number, invalid barCount arguments, equality in min/max, etc.).</li>
 * </ul>
 *
 * <p>Only classes, constructors and methods that appear in the source of
 * {@code NumericIndicator} or in the provided public API signatures are used.
 * No mocking or subclassing of library interfaces is performed.
 */
public class NumericIndicatorTest {

    /** Simple bar series with three bars used by all tests. */
    private BarSeries series;

    /** Close price indicator (1, 2, 3) wrapped as a NumericIndicator. */
    private NumericIndicator close;

    /** Helper to create a {@link Num} from a {@link Number} using the series. */
    private Num numOf(Number n) {
        return series.num().numOf(n);
    }

    @Before
    public void setUp() {
        // Build a minimal BarSeries (BaseBarSeries is the concrete implementation in TA4J)
        series = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now();

        // create three bars with close prices 1, 2, 3
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.minusMinutes(3),
                numOf(10), numOf(10), numOf(10), numOf(1), numOf(1000)));
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.minusMinutes(2),
                numOf(20), numOf(20), numOf(20), numOf(2), numOf(2000)));
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.minusMinutes(1),
                numOf(30), numOf(30), numOf(30), numOf(3), numOf(3000)));

        // Base NumericIndicator wrapping the ClosePriceIndicator
        close = NumericIndicator.closePrice(series);
    }

    /* ---------------------------------------------------------------------- */
    /*  Static factory methods                                                */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOfCreatesNumericIndicator() {
        Indicator<Num> constInd = new ConstantIndicator<>(series, numOf(5));
        NumericIndicator ni = NumericIndicator.of(constInd);
        assertNotNull(ni);
        assertSame(constInd, ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertNotNull(cp);
        // first close price is 1
        assertEquals(numOf(1), cp.getValue(0));
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertNotNull(vol);
        // first volume is 1000
        assertEquals(numOf(1000), vol.getValue(0));
    }

    /* ---------------------------------------------------------------------- */
    /*  Delegating methods                                                    */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDelegate() {
        assertSame(close.delegate(), close.delegate());
    }

    @Test
    public void testGetValueDelegatesCorrectly() {
        assertEquals(numOf(2), close.getValue(1));
    }

    @Test
    public void testGetUnstableBarsIsZero() {
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

    /* ---------------------------------------------------------------------- */
    /*  Arithmetic operations                                                 */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPlusIndicator() {
        NumericIndicator plus = close.plus(close);
        // (1+1)=2, (2+2)=4, (3+3)=6
        assertEquals(numOf(2), plus.getValue(0));
        assertEquals(numOf(4), plus.getValue(1));
        assertEquals(numOf(6), plus.getValue(2));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator plus = close.plus(5);
        assertEquals(numOf(6), plus.getValue(0));
        assertEquals(numOf(7), plus.getValue(1));
        assertEquals(numOf(8), plus.getValue(2));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator minus = close.minus(close);
        assertEquals(numOf(0), minus.getValue(0));
        assertEquals(numOf(0), minus.getValue(1));
        assertEquals(numOf(0), minus.getValue(2));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator minus = close.minus(1);
        assertEquals(numOf(0), minus.getValue(0));
        assertEquals(numOf(1), minus.getValue(1));
        assertEquals(numOf(2), minus.getValue(2));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator mul = close.multipliedBy(close);
        // 1*1=1, 2*2=4, 3*3=9
        assertEquals(numOf(1), mul.getValue(0));
        assertEquals(numOf(4), mul.getValue(1));
        assertEquals(numOf(9), mul.getValue(2));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator mul = close.multipliedBy(3);
        assertEquals(numOf(3), mul.getValue(0));
        assertEquals(numOf(6), mul.getValue(1));
        assertEquals(numOf(9), mul.getValue(2));
    }

    @Test
    public void testDividedByIndicator() {
        // create indicator 1,2,3 and divide by itself -> 1
        NumericIndicator div = close.dividedBy(close);
        assertEquals(numOf(1), div.getValue(0));
        assertEquals(numOf(1), div.getValue(1));
        assertEquals(numOf(1), div.getValue(2));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator div = close.dividedBy(2);
        assertEquals(numOf(0.5), div.getValue(0));
        assertEquals(numOf(1), div.getValue(1));
        assertEquals(numOf(1.5), div.getValue(2));
    }

    @Test
    public void testDividedByZeroProducesNaN() {
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series, numOf(0)));
        NumericIndicator div = close.dividedBy(zero);
        // division by zero yields NaN according to TA4J's Num implementation
        assertTrue(div.getValue(0).isNaN());
        assertTrue(div.getValue(1).isNaN());
        assertTrue(div.getValue(2).isNaN());
    }

    @Test
    public void testMinIndicator() {
        // constant 2 indicator
        NumericIndicator constTwo = NumericIndicator.of(new ConstantIndicator<>(series, numOf(2)));
        NumericIndicator min = close.min(constTwo);
        // min(1,2)=1, min(2,2)=2, min(3,2)=2
        assertEquals(numOf(1), min.getValue(0));
        assertEquals(numOf(2), min.getValue(1));
        assertEquals(numOf(2), min.getValue(2));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = close.min(2);
        assertEquals(numOf(1), min.getValue(0));
        assertEquals(numOf(2), min.getValue(1));
        assertEquals(numOf(2), min.getValue(2));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator constTwo = NumericIndicator.of(new ConstantIndicator<>(series, numOf(2)));
        NumericIndicator max = close.max(constTwo);
        // max(1,2)=2, max(2,2)=2, max(3,2)=3
        assertEquals(numOf(2), max.getValue(0));
        assertEquals(numOf(2), max.getValue(1));
        assertEquals(numOf(3), max.getValue(2));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = close.max(2);
        assertEquals(numOf(2), max.getValue(0));
        assertEquals(numOf(2), max.getValue(1));
        assertEquals(numOf(3), max.getValue(2));
    }

    @Test
    public void testAbs() {
        // create negative indicator: -1, -2, -3
        NumericIndicator negative = NumericIndicator.of(
                new ConstantIndicator<>(series, numOf(-1)).minus(close));
        NumericIndicator abs = negative.abs();
        assertEquals(numOf(1), abs.getValue(0));
        assertEquals(numOf(2), abs.getValue(1));
        assertEquals(numOf(3), abs.getValue(2));
    }

    @Test
    public void testSqrtPositive() {
        NumericIndicator sqrt = close.sqrt();
        // sqrt(1)=1, sqrt(2)=~1.4142, sqrt(3)=~1.7320
        assertEquals(numOf(1).doubleValue(), sqrt.getValue(0).doubleValue(), 1e-12);
        assertEquals(Math.sqrt(2), sqrt.getValue(1).doubleValue(), 1e-12);
        assertEquals(Math.sqrt(3), sqrt.getValue(2).doubleValue(), 1e-12);
    }

    @Test
    public void testSqrtNegativeProducesNaN() {
        NumericIndicator negative = NumericIndicator.of(
                new ConstantIndicator<>(series, numOf(-1)).minus(close)); // -1-1 = -2, etc.
        NumericIndicator sqrt = negative.sqrt();
        assertTrue(sqrt.getValue(0).isNaN());
        assertTrue(sqrt.getValue(1).isNaN());
        assertTrue(sqrt.getValue(2).isNaN());
    }

    @Test
    public void testSquared() {
        NumericIndicator squared = close.squared();
        // 1*1=1, 2*2=4, 3*3=9
        assertEquals(numOf(1), squared.getValue(0));
        assertEquals(numOf(4), squared.getValue(1));
        assertEquals(numOf(9), squared.getValue(2));
    }

    /* ---------------------------------------------------------------------- */
    /*  Aggregation indicators                                                */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testSma() {
        NumericIndicator sma = close.sma(2);
        // SMA(2) at index 0 = 1 (only one value available)
        // SMA(2) at index 1 = (1+2)/2 = 1.5
        // SMA(2) at index 2 = (2+3)/2 = 2.5
        assertEquals(numOf(1), sma.getValue(0));
        assertEquals(numOf(1.5), sma.getValue(1));
        assertEquals(numOf(2.5), sma.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmaWithZeroBarCountThrows() {
        close.sma(0);
    }

    @Test
    public void testEma() {
        // EMA with barCount 2 – values can be verified against manual calculation
        NumericIndicator ema = close.ema(2);
        // First value = close[0] = 1
        assertEquals(numOf(1), ema.getValue(0));
        // Subsequent values follow EMA formula; we just assert they are not null
        assertNotNull(ema.getValue(1));
        assertNotNull(ema.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmaWithNegativeBarCountThrows() {
        close.ema(-5);
    }

    @Test
    public void testStddev() {
        NumericIndicator std = close.stddev(2);
        // Standard deviation of a single value is 0
        assertEquals(numOf(0), std.getValue(0));
        // For index 1 and 2 we just check that a non‑null value is produced
        assertNotNull(std.getValue(1));
        assertNotNull(std.getValue(2));
    }

    @Test
    public void testHighest() {
        NumericIndicator high = close.highest(2);
        // Highest of [1] = 1, of [1,2] = 2, of [2,3] = 3
        assertEquals(numOf(1), high.getValue(0));
        assertEquals(numOf(2), high.getValue(1));
        assertEquals(numOf(3), high.getValue(2));
    }

    @Test
    public void testLowest() {
        NumericIndicator low = close.lowest(2);
        // Lowest of [1] = 1, of [1,2] = 1, of [2,3] = 2
        assertEquals(numOf(1), low.getValue(0));
        assertEquals(numOf(1), low.getValue(1));
        assertEquals(numOf(2), low.getValue(2));
    }

    @Test
    public void testPreviousWithBarCount() {
        NumericIndicator prev = close.previous(2);
        // previous(2) at index 0 = NaN (not enough history)
        assertTrue(prev.getValue(0).isNaN());
        // at index 2 -> value at index 0 = 1
        assertEquals(numOf(1), prev.getValue(2));
    }

    @Test
    public void testPreviousDefault() {
        Indicator<Num> prev = close.previous(); // barCount = 1
        // previous(1) at index 1 -> value at index 0 = 1
        assertEquals(numOf(1), prev.getValue(1));
        // at index 0 -> NaN
        assertTrue(prev.getValue(0).isNaN());
    }

    /* ---------------------------------------------------------------------- */
    /*  Rule creation and evaluation                                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCrossedOverIndicator() {
        // indicator that is always lower than close (constant 0)
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series, numOf(0)));
        Rule crossed = close.crossedOver(zero);
        // At index 0 the close (1) is already above zero, but a crossing occurs
        // only when the previous value was <= threshold and current > threshold.
        // Because previous value for index 0 is NaN, rule should be false.
        assertFalse(crossed.isSatisfied(0));
        // At index 1 the previous value (1) > 0 and current (2) > 0 -> no crossing
        assertFalse(crossed.isSatisfied(1));
        // For a real crossing test we need a series that goes from below to above.
        BarSeries series2 = new BaseBarSeries("cross");
        series2.addBar(new BaseBar(Duration.ofMinutes(1), now.minusMinutes(3),
                numOf(10), numOf(10), numOf(10), numOf(1), numOf(100)));
        series2.addBar(new BaseBar(Duration.ofMinutes(1), now.minusMinutes(2),
                numOf(20), numOf(20), numOf(20), numOf(2), numOf(200)));
        NumericIndicator ind = NumericIndicator.closePrice(series2);
        NumericIndicator thresh = NumericIndicator.of(new ConstantIndicator<>(series2, numOf(1.5)));
        Rule cross = ind.crossedOver(thresh);
        // index 0: value 1 <= 1.5, no crossing
        assertFalse(cross.isSatisfied(0));
        // index 1: value 2 > 1.5 and previous 1 <= 1.5 -> crossing occurs
        assertTrue(cross.isSatisfied(1));
    }

    @Test
    public void testCrossedOverNumber() {
        Rule crossed = close.crossedOver(2);
        // close values: 1,2,3 ; threshold 2
        // crossing occurs at index 2 (value 3 > 2, previous 2 <= 2)
        assertFalse(crossed.isSatisfied(0));
        assertFalse(crossed.isSatisfied(1));
        assertTrue(crossed.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderIndicator() {
        NumericIndicator high = NumericIndicator.of(new ConstantIndicator<>(series, numOf(10)));
        Rule crossedUnder = close.crossedUnder(high);
        // Close (1,2,3) is always under 10, but crossing occurs only when it goes
        // from above to below. Since it never was above, rule should always be false.
        assertFalse(crossedUnder.isSatisfied(0));
        assertFalse(crossedUnder.isSatisfied(1));
        assertFalse(crossedUnder.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule crossedUnder = close.crossedUnder(2);
        // threshold 2, values: 1,2,3
        // crossing under occurs at index 0? No previous value.
        // At index 1: previous 1 < 2 and current 2 == 2 -> not a crossing (needs <)
        // At index 2: previous 2 >= 2 and current 3 > 2 -> not a crossing.
        assertFalse(crossedUnder.isSatisfied(0));
        assertFalse(crossedUnder.isSatisfied(1));
        assertFalse(crossedUnder.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanIndicator() {
        NumericIndicator constOne = NumericIndicator.of(new ConstantIndicator<>(series, numOf(1)));
        Rule gt = close.isGreaterThan(constOne);
        // values 1,2,3 > 1 ? false, true, true
        assertFalse(gt.isSatisfied(0));
        assertTrue(gt.isSatisfied(1));
        assertTrue(gt.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule gt = close.isGreaterThan(2);
        // 1>2 false, 2>2 false, 3>2 true
        assertFalse(gt.isSatisfied(0));
        assertFalse(gt.isSatisfied(1));
        assertTrue(gt.isSatisfied(2));
    }

    @Test
    public void testIsLessThanIndicator() {
        NumericIndicator constTwo = NumericIndicator.of(new ConstantIndicator<>(series, numOf(2)));
        Rule lt = close.isLessThan(constTwo);
        // 1<2 true, 2<2 false, 3<2 false
        assertTrue(lt.isSatisfied(0));
        assertFalse(lt.isSatisfied(1));
        assertFalse(lt.isSatisfied(2));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule lt = close.isLessThan(2);
        // same as above
        assertTrue(lt.isSatisfied(0));
        assertFalse(lt.isSatisfied(1));
        assertFalse(lt.isSatisfied(2));
    }

    /* ---------------------------------------------------------------------- */
    /*  Edge cases for constructors / illegal arguments                     */
    /* ---------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testHighestWithNegativeBarCountThrows() {
        close.highest(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowestWithZeroBarCountThrows() {
        close.lowest(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreviousWithNegativeBarCountThrows() {
        close.previous(-3);
    }

    /* ---------------------------------------------------------------------- */
    /*  Equality handling in min / max when values are identical            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testMinWhenEqualReturnsThis() {
        // both indicators have same values
        NumericIndicator other = NumericIndicator.closePrice(series);
        NumericIndicator min = close.min(other);
        // Since they are equal, the implementation should return 'this' indicator.
        // We verify that the returned instance is the same object.
        assertSame(close, min);
    }

    @Test
    public void testMaxWhenEqualReturnsThis() {
        NumericIndicator other = NumericIndicator.closePrice(series);
        NumericIndicator max = close.max(other);
        assertSame(close, max);
    }

    /* ---------------------------------------------------------------------- */
    /*  Helper method to compare Num values with tolerance when necessary   */
    /* ---------------------------------------------------------------------- */

    private void assertNumEquals(double expected, Num actual) {
        assertEquals(expected, actual.doubleValue(), 1e-12);
    }
}
