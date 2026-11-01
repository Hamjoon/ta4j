package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Bar;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public methods (including static factories)</li>
 *   <li>Normal functional behaviour</li>
 *   <li>Edge cases such as division by zero, sqrt of a negative number,
 *       equality handling in min/max, and rule evaluation when no crossing
 *       occurs.</li>
 * </ul>
 *
 * A lightweight {@link BaseBarSeries} is used as the concrete {@link BarSeries}
 * implementation. Bars are built with simple price/volume values so that the
 * expected Num results are easy to compute.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator price;   // close price indicator wrapped as NumericIndicator
    private NumericIndicator volume;  // volume indicator wrapped as NumericIndicator

    @Before
    public void setUp() {
        // Build a tiny series (5 bars) with deterministic close prices and volumes.
        series = new BaseBarSeries("test-series");
        // BarBuilder creates bars; we use DecimalNum for numeric values.
        // Prices: 10, 20, 30, 40, 50
        // Volumes: 100, 200, 300, 400, 500
        for (int i = 0; i < 5; i++) {
            Num price = series.numFactory().numOf(10 * (i + 1));
            Num volume = series.numFactory().numOf(100 * (i + 1));
            Bar bar = series.barBuilder()
                    .openPrice(price)
                    .highPrice(price.plus(price))      // just dummy values
                    .lowPrice(price.minus(price))
                    .closePrice(price)
                    .volume(volume)
                    .build();
            series.addBar(bar);
        }

        price = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    /* --------------------------------------------------------------------- */
    /* 1.  Static factory methods                                            */
    /* --------------------------------------------------------------------- */

    @Test
    public void testOfFactory() {
        NumericIndicator ni = NumericIndicator.of(price);
        assertNotNull(ni);
        assertSame(price, ni.delegate());
    }

    @Test
    public void testClosePriceFactory() {
        NumericIndicator cp = NumericIndicator.closePrice(series);
        assertNotNull(cp);
        // first close price should be 10
        assertEquals(series.numFactory().numOf(10), cp.getValue(0));
    }

    @Test
    public void testVolumeFactory() {
        NumericIndicator vol = NumericIndicator.volume(series);
        assertNotNull(vol);
        // first volume should be 100
        assertEquals(series.numFactory().numOf(100), vol.getValue(0));
    }

    /* --------------------------------------------------------------------- */
    /* 2.  Arithmetic operations                                             */
    /* --------------------------------------------------------------------- */

    @Test
    public void testPlusIndicator() {
        NumericIndicator sum = price.plus(volume);
        // index 2: price=30, volume=300 => sum=330
        assertEquals(series.numFactory().numOf(330), sum.getValue(2));
    }

    @Test
    public void testPlusNumber() {
        NumericIndicator sum = price.plus(5);
        // index 1: price=20 + 5 = 25
        assertEquals(series.numFactory().numOf(25), sum.getValue(1));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator diff = price.minus(volume);
        // index 3: price=40 - volume=400 = -360
        assertEquals(series.numFactory().numOf(-360), diff.getValue(3));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator diff = price.minus(5);
        // index 0: 10 - 5 = 5
        assertEquals(series.numFactory().numOf(5), diff.getValue(0));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator prod = price.multipliedBy(volume);
        // index 2: 30 * 300 = 9000
        assertEquals(series.numFactory().numOf(9000), prod.getValue(2));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator prod = price.multipliedBy(2);
        // index 4: 50 * 2 = 100
        assertEquals(series.numFactory().numOf(100), prod.getValue(4));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator quot = price.dividedBy(volume);
        // index 1: 20 / 200 = 0.1
        Num expected = series.numFactory().numOf(20).dividedBy(series.numFactory().numOf(200));
        assertEquals(expected, quot.getValue(1));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator quot = price.dividedBy(2);
        // index 3: 40 / 2 = 20
        assertEquals(series.numFactory().numOf(20), quot.getValue(3));
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroNumber() {
        // Division by zero should raise an exception from the underlying Num implementation.
        price.dividedBy(0).getValue(0);
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZeroIndicator() {
        NumericIndicator zeroConst = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(0)));
        price.dividedBy(zeroConst).getValue(0);
    }

    /* --------------------------------------------------------------------- */
    /* 3.  Min / Max                                                       */
    /* --------------------------------------------------------------------- */

    @Test
    public void testMinIndicator() {
        NumericIndicator min = price.min(volume);
        // index 2: price=30, volume=300 => min = 30
        assertEquals(series.numFactory().numOf(30), min.getValue(2));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator min = price.min(25);
        // index 1: price=20, number=25 => min = 20
        assertEquals(series.numFactory().numOf(20), min.getValue(1));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator max = price.max(volume);
        // index 2: price=30, volume=300 => max = 300
        assertEquals(series.numFactory().numOf(300), max.getValue(2));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator max = price.max(25);
        // index 0: price=10, number=25 => max = 25
        assertEquals(series.numFactory().numOf(25), max.getValue(0));
    }

    @Test
    public void testMinEqualityReturnsThis() {
        // price and constant 20 are equal at index 1; min should return price (i.e., same value)
        NumericIndicator min = price.min(20);
        assertEquals(series.numFactory().numOf(20), min.getValue(1));
    }

    @Test
    public void testMaxEqualityReturnsThis() {
        // price and constant 30 are equal at index 2; max should return price
        NumericIndicator max = price.max(30);
        assertEquals(series.numFactory().numOf(30), max.getValue(2));
    }

    /* --------------------------------------------------------------------- */
    /* 4.  Unary operations                                                */
    /* --------------------------------------------------------------------- */

    @Test
    public void testAbs() {
        // Create a negative constant indicator
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-42)));
        NumericIndicator abs = neg.abs();
        assertEquals(series.numFactory().numOf(42), abs.getValue(0));
    }

    @Test
    public void testSqrt() {
        NumericIndicator sqrt = price.sqrt();
        // index 0: sqrt(10) ≈ 3.1622776601683795
        Num expected = series.numFactory().numOf(10).sqrt();
        assertEquals(expected, sqrt.getValue(0));
    }

    @Test(expected = ArithmeticException.class)
    public void testSqrtOfNegativeNumber() {
        NumericIndicator neg = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(-9)));
        // sqrt of a negative number should raise an exception
        neg.sqrt().getValue(0);
    }

    @Test
    public void testSquared() {
        NumericIndicator sq = price.squared();
        // index 3: 40 * 40 = 1600
        assertEquals(series.numFactory().numOf(1600), sq.getValue(3));
    }

    /* --------------------------------------------------------------------- */
    /* 5.  Moving‑average / statistical indicators                         */
    /* --------------------------------------------------------------------- */

    @Test
    public void testSma() {
        // SMA of 3 periods: for index 2 the average of 10,20,30 = 20
        NumericIndicator sma = price.sma(3);
        assertEquals(series.numFactory().numOf(20), sma.getValue(2));
    }

    @Test
    public void testEma() {
        // EMA with period 3; the exact value depends on the EMA formula.
        // We check that the indicator is created and returns a Num.
        NumericIndicator ema = price.ema(3);
        assertNotNull(ema.getValue(4));
    }

    @Test
    public void testStddev() {
        // Standard deviation of 3 periods at index 2 (values 10,20,30)
        // StdDev = sqrt( ((10-20)^2 + (20-20)^2 + (30-20)^2) / 3 )
        NumericIndicator stddev = price.stddev(3);
        Num mean = series.numFactory().numOf(20);
        Num sumSq = series.numFactory().numOf(100).plus(series.numFactory().numOf(0))
                .plus(series.numFactory().numOf(100));
        Num variance = sumSq.dividedBy(series.numFactory().numOf(3));
        Num expected = variance.sqrt();
        assertEquals(expected, stddev.getValue(2));
    }

    @Test
    public void testHighest() {
        // Highest of last 3 values at index 4: values = 30,40,50 => highest = 50
        NumericIndicator highest = price.highest(3);
        assertEquals(series.numFactory().numOf(50), highest.getValue(4));
    }

    @Test
    public void testLowest() {
        // Lowest of last 3 values at index 4: values = 30,40,50 => lowest = 30
        NumericIndicator lowest = price.lowest(3);
        assertEquals(series.numFactory().numOf(30), lowest.getValue(4));
    }

    @Test
    public void testPreviousWithBarCount() {
        // previous(2) at index 3 should give value from index 1 (price=20)
        NumericIndicator prev = price.previous(2);
        assertEquals(series.numFactory().numOf(20), prev.getValue(3));
    }

    @Test
    public void testPreviousDefault() {
        // previous() == previous(1)
        NumericIndicator prev = price.previous();
        assertEquals(series.numFactory().numOf(40), prev.getValue(4)); // index 4 => previous index 3
    }

    /* --------------------------------------------------------------------- */
    /* 6.  Rule creation and edge evaluation                               */
    /* --------------------------------------------------------------------- */

    @Test
    public void testCrossedOverIndicatorRule() {
        // Create a simple increasing indicator (price) and a constant lower threshold.
        Rule rule = price.crossedOver(15); // crosses 15 at index 1 (price 20 > 15, previous 10 < 15)
        // Rule.isSatisfied requires a TradingRecord; we can use null because the
        // underlying rule implementation ignores it for the simple overload.
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderIndicatorRule() {
        Rule rule = price.crossedUnder(45); // crosses under 45 at index 3 (price 40 < 45, previous 50 > 45)
        assertTrue(rule.isSatisfied(3));
        assertFalse(rule.isSatisfied(4));
    }

    @Test
    public void testCrossedOverNoCross() {
        // price never crosses 100
        Rule rule = price.crossedOver(100);
        assertFalse(rule.isSatisfied(4));
    }

    @Test
    public void testIsGreaterThanIndicatorRule() {
        Rule rule = price.isGreaterThan(volume);
        // At index 4: price 50 vs volume 500 => false
        // At index 0: price 10 vs volume 100 => false
        // Let's create a case where price > volume.
        NumericIndicator highPrice = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(600)));
        Rule rule2 = highPrice.isGreaterThan(volume);
        assertTrue(rule2.isSatisfied(0));
    }

    @Test
    public void testIsLessThanIndicatorRule() {
        Rule rule = price.isLessThan(volume);
        // At index 2: price 30 vs volume 300 => true
        assertTrue(rule.isSatisfied(2));
        // At index 4: price 50 vs volume 500 => true
        assertTrue(rule.isSatisfied(4));
    }

    @Test
    public void testIsGreaterThanNumberRule() {
        Rule rule = price.isGreaterThan(25);
        // Index 3 price=40 > 25 => true
        assertTrue(rule.isSatisfied(3));
        // Index 1 price=20 <= 25 => false
        assertFalse(rule.isSatisfied(1));
    }

    @Test
    public void testIsLessThanNumberRule() {
        Rule rule = price.isLessThan(35);
        // Index 2 price=30 < 35 => true
        assertTrue(rule.isSatisfied(2));
        // Index 4 price=50 >= 35 => false
        assertFalse(rule.isSatisfied(4));
    }

    /* --------------------------------------------------------------------- */
    /* 7.  Delegate and toString forwarding                                  */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDelegateMethod() {
        assertSame(price.delegate(), price.delegate());
    }

    @Test
    public void testToStringForwarding() {
        // The toString of NumericIndicator should equal the delegate's toString.
        assertEquals(price.delegate().toString(), price.toString());
    }

    /* --------------------------------------------------------------------- */
    /* 8.  Edge cases for numeric operations                                 */
    /* --------------------------------------------------------------------- */

    @Test
    public void testPlusWithLargeNumbers() {
        // Test that addition does not overflow the underlying Num implementation.
        Num big = series.numFactory().numOf(new BigDecimal("1e30"));
        NumericIndicator bigConst = NumericIndicator.of(new ConstantIndicator<>(series, big));
        NumericIndicator sum = bigConst.plus(big);
        Num expected = big.plus(big);
        assertEquals(expected, sum.getValue(0));
    }

    @Test
    public void testMinusResultingZero() {
        NumericIndicator diff = price.minus(price);
        assertTrue(diff.getValue(2).isZero());
    }

    @Test
    public void testDividedByOne() {
        NumericIndicator quot = price.dividedBy(1);
        assertEquals(price.getValue(3), quot.getValue(3));
    }

    @Test
    public void testSqrtZero() {
        NumericIndicator zero = NumericIndicator.of(new ConstantIndicator<>(series,
                series.numFactory().numOf(0)));
        assertTrue(zero.sqrt().getValue(0).isZero());
    }
}
