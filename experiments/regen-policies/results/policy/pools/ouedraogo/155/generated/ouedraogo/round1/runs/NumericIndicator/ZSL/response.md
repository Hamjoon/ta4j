###Test START##
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.rules.Rule;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>
 * The tests create a small {@link BarSeries} (3 bars) with deterministic price
 * and volume values using {@link DecimalNum}.  All public methods of
 * {@code NumericIndicator} are exercised, checking:
 *
 * <ul>
 *   <li>that the returned objects are of the expected type,</li>
 *   <li>that arithmetic operations produce the correct numeric results,</li>
 *   <li>that wrapper indicator factories (SMA, EMA, etc.) delegate correctly,</li>
 *   <li>that rule‑creation methods generate the proper rule type and evaluate
 *       as expected.</li>
 * </ul>
 *
 * The test suite purposefully avoids any mocking; all objects are real
 * instances from the TA4J library.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private NumericIndicator close;
    private NumericIndicator volume;

    @Before
    public void setUp() {
        // Build a simple series: 3 bars with known close prices and volumes
        series = new BaseBarSeries.Builder().withName("test-series").build();

        // Helper to create DecimalNum values
        Num p1 = DecimalNum.valueOf(10);
        Num p2 = DecimalNum.valueOf(20);
        Num p3 = DecimalNum.valueOf(30);

        Num v1 = DecimalNum.valueOf(1000);
        Num v2 = DecimalNum.valueOf(2000);
        Num v3 = DecimalNum.valueOf(3000);

        // Add bars (time, open, high, low, close, volume) – using the same
        // value for open/high/low/close for simplicity
        series.addBar(Bar.builder()
                .timePeriod(java.time.Duration.ofDays(1))
                .openPrice(p1)
                .highPrice(p1)
                .lowPrice(p1)
                .closePrice(p1)
                .volume(v1)
                .build());

        series.addBar(Bar.builder()
                .timePeriod(java.time.Duration.ofDays(1))
                .openPrice(p2)
                .highPrice(p2)
                .lowPrice(p2)
                .closePrice(p2)
                .volume(v2)
                .build());

        series.addBar(Bar.builder()
                .timePeriod(java.time.Duration.ofDays(1))
                .openPrice(p3)
                .highPrice(p3)
                .lowPrice(p3)
                .closePrice(p3)
                .volume(v3)
                .build());

        // Create NumericIndicator wrappers
        close = NumericIndicator.closePrice(series);
        volume = NumericIndicator.volume(series);
    }

    // -----------------------------------------------------------------------
    // Basic delegation tests
    // -----------------------------------------------------------------------
    @Test
    public void testDelegate() {
        assertNotNull(close.delegate());
        assertTrue(close.delegate() instanceof ClosePriceIndicator);
        assertEquals(series, close.getBarSeries());
    }

    // -----------------------------------------------------------------------
    // Arithmetic operations (Indicator overloads)
    // -----------------------------------------------------------------------
    @Test
    public void testPlusIndicator() {
        NumericIndicator result = close.plus(volume);
        // 10+1000 = 1010, 20+2000 = 2020, 30+3000 = 3030
        assertEquals(DecimalNum.valueOf(1010), result.getValue(0));
        assertEquals(DecimalNum.valueOf(2020), result.getValue(1));
        assertEquals(DecimalNum.valueOf(3030), result.getValue(2));
    }

    @Test
    public void testMinusIndicator() {
        NumericIndicator result = close.minus(volume);
        // 10-1000 = -990, etc.
        assertEquals(DecimalNum.valueOf(-990), result.getValue(0));
        assertEquals(DecimalNum.valueOf(-1980), result.getValue(1));
        assertEquals(DecimalNum.valueOf(-2970), result.getValue(2));
    }

    @Test
    public void testMultipliedByIndicator() {
        NumericIndicator result = close.multipliedBy(volume);
        // 10*1000 = 10000, etc.
        assertEquals(DecimalNum.valueOf(10000), result.getValue(0));
        assertEquals(DecimalNum.valueOf(40000), result.getValue(1));
        assertEquals(DecimalNum.valueOf(90000), result.getValue(2));
    }

    @Test
    public void testDividedByIndicator() {
        NumericIndicator result = volume.dividedBy(close);
        // 1000/10 = 100, etc.
        assertEquals(DecimalNum.valueOf(100), result.getValue(0));
        assertEquals(DecimalNum.valueOf(100), result.getValue(1));
        assertEquals(DecimalNum.valueOf(100), result.getValue(2));
    }

    @Test
    public void testMinIndicator() {
        NumericIndicator result = close.min(volume);
        // min(10,1000)=10, min(20,2000)=20, min(30,3000)=30
        assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        assertEquals(DecimalNum.valueOf(20), result.getValue(1));
        assertEquals(DecimalNum.valueOf(30), result.getValue(2));
    }

    @Test
    public void testMaxIndicator() {
        NumericIndicator result = close.max(volume);
        // max(10,1000)=1000, etc.
        assertEquals(DecimalNum.valueOf(1000), result.getValue(0));
        assertEquals(DecimalNum.valueOf(2000), result.getValue(1));
        assertEquals(DecimalNum.valueOf(3000), result.getValue(2));
    }

    // -----------------------------------------------------------------------
    // Unary operations
    // -----------------------------------------------------------------------
    @Test
    public void testAbs() {
        // Create a negative indicator by subtracting a larger constant
        NumericIndicator negative = close.minus(DecimalNum.valueOf(50));
        NumericIndicator abs = negative.abs();
        assertEquals(DecimalNum.valueOf(40), abs.getValue(0)); // |10-50| = 40
        assertEquals(DecimalNum.valueOf(30), abs.getValue(1));
        assertEquals(DecimalNum.valueOf(20), abs.getValue(2));
    }

    @Test
    public void testSqrt() {
        // sqrt of close prices: sqrt(10), sqrt(20), sqrt(30)
        NumericIndicator sqrt = close.sqrt();
        assertEquals(DecimalNum.valueOf(Math.sqrt(10)), sqrt.getValue(0));
        assertEquals(DecimalNum.valueOf(Math.sqrt(20)), sqrt.getValue(1));
        assertEquals(DecimalNum.valueOf(Math.sqrt(30)), sqrt.getValue(2));
    }

    @Test
    public void testSquared() {
        NumericIndicator squared = close.squared();
        // 10*10 = 100, 20*20 = 400, 30*30 = 900
        assertEquals(DecimalNum.valueOf(100), squared.getValue(0));
        assertEquals(DecimalNum.valueOf(400), squared.getValue(1));
        assertEquals(DecimalNum.valueOf(900), squared.getValue(2));
    }

    // -----------------------------------------------------------------------
    // Moving‑average and statistical indicators
    // -----------------------------------------------------------------------
    @Test
    public void testSma() {
        // SMA with barCount = 2
        NumericIndicator sma = close.sma(2);
        // Index 0: SMA = 10 (only one value)
        // Index 1: (10+20)/2 = 15
        // Index 2: (20+30)/2 = 25
        assertEquals(DecimalNum.valueOf(10), sma.getValue(0));
        assertEquals(DecimalNum.valueOf(15), sma.getValue(1));
        assertEquals(DecimalNum.valueOf(25), sma.getValue(2));
    }

    @Test
    public void testEma() {
        // EMA with barCount = 2, smoothing factor = 2/(2+1)=0.666...
        NumericIndicator ema = close.ema(2);
        // EMA[0] = close[0] = 10
        // EMA[1] = (close[1] * k) + (EMA[0] * (1-k))
        // k = 2/3
        Num k = DecimalNum.valueOf(2.0 / 3.0);
        Num ema0 = DecimalNum.valueOf(10);
        Num ema1 = close.getValue(1).multipliedBy(k).plus(ema0.multipliedBy(DecimalNum.ONE.minus(k)));
        Num ema2 = close.getValue(2).multipliedBy(k).plus(ema1.multipliedBy(DecimalNum.ONE.minus(k)));

        assertEquals(ema0, ema.getValue(0));
        assertEquals(ema1, ema.getValue(1));
        assertEquals(ema2, ema.getValue(2));
    }

    @Test
    public void testStddev() {
        // Standard deviation with barCount = 2
        NumericIndicator stddev = close.stddev(2);
        // For 2‑point windows, stddev = sqrt( ((x1-mean)^2 + (x2-mean)^2) / 2 )
        // Index 0: stddev = 0 (single value)
        // Index 1: values 10,20 -> mean 15 -> variance ((-5)^2 + 5^2)/2 = 25
        // stddev = 5
        // Index 2: values 20,30 -> mean 25 -> variance ((-5)^2 + 5^2)/2 = 25 -> stddev =5
        assertEquals(DecimalNum.valueOf(0), stddev.getValue(0));
        assertEquals(DecimalNum.valueOf(5), stddev.getValue(1));
        assertEquals(DecimalNum.valueOf(5), stddev.getValue(2));
    }

    @Test
    public void testHighestAndLowest() {
        NumericIndicator highest = close.highest(2);
        NumericIndicator lowest = close.lowest(2);

        // Highest of (10,20)=20; (20,30)=30
        assertEquals(DecimalNum.valueOf(10), highest.getValue(0)); // only one value
        assertEquals(DecimalNum.valueOf(20), highest.getValue(1));
        assertEquals(DecimalNum.valueOf(30), highest.getValue(2));

        // Lowest of (10,20)=10; (20,30)=20
        assertEquals(DecimalNum.valueOf(10), lowest.getValue(0));
        assertEquals(DecimalNum.valueOf(10), lowest.getValue(1));
        assertEquals(DecimalNum.valueOf(20), lowest.getValue(2));
    }

    @Test
    public void testPrevious() {
        NumericIndicator prev = close.previous();
        // previous(1) returns the value from the previous bar, or NaN for index 0
        assertTrue(prev.getValue(0).isNaN());
        assertEquals(DecimalNum.valueOf(10), prev.getValue(1));
        assertEquals(DecimalNum.valueOf(20), prev.getValue(2));
    }

    // -----------------------------------------------------------------------
    // Rule creation and evaluation
    // -----------------------------------------------------------------------
    @Test
    public void testCrossedOverRule() {
        // Define a simple moving average and test crossing
        NumericIndicator sma = close.sma(2);
        Rule crossedOver = close.crossedOver(sma);
        assertTrue(crossedOver instanceof CrossedUpIndicatorRule);
        // At index 0: both are equal -> not crossed
        // At index 1: close (20) > sma (15) and previous close (10) <= previous sma (10)
        // => crossed over should be true at index 1
        assertFalse(crossedOver.isSatisfied(0));
        assertTrue(crossedOver.isSatisfied(1));
        assertFalse(crossedOver.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderRule() {
        NumericIndicator sma = close.sma(2);
        Rule crossedUnder = close.crossedUnder(sma);
        assertTrue(crossedUnder instanceof CrossedDownIndicatorRule);
        // At index 0: equal -> not crossed
        // At index 2: close (30) > sma (25) => not crossed under; but at index 1 it also
        // is not crossed under. Hence all false.
        assertFalse(crossedUnder.isSatisfied(0));
        assertFalse(crossedUnder.isSatisfied(1));
        assertFalse(crossedUnder.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanRule() {
        Rule greaterThan15 = close.isGreaterThan(DecimalNum.valueOf(15));
        assertFalse(greaterThan15.isSatisfied(0)); // 10 <= 15
        assertTrue(greaterThan15.isSatisfied(1));  // 20 > 15
        assertTrue(greaterThan15.isSatisfied(2));  // 30 > 15
    }

    @Test
    public void testIsLessThanRule() {
        Rule lessThan25 = close.isLessThan(DecimalNum.valueOf(25));
        assertTrue(lessThan25.isSatisfied(0));   // 10 < 25
        assertTrue(lessThan25.isSatisfied(1));   // 20 < 25
        assertFalse(lessThan25.isSatisfied(2));  // 30 >= 25
    }

    // -----------------------------------------------------------------------
    // Numeric overloads with Number arguments (delegated to constant indicator)
    // -----------------------------------------------------------------------
    @Test
    public void testPlusNumber() {
        NumericIndicator result = close.plus(5);
        assertEquals(DecimalNum.valueOf(15), result.getValue(0));
        assertEquals(DecimalNum.valueOf(25), result.getValue(1));
        assertEquals(DecimalNum.valueOf(35), result.getValue(2));
    }

    @Test
    public void testMinusNumber() {
        NumericIndicator result = close.minus(5);
        assertEquals(DecimalNum.valueOf(5), result.getValue(0));
        assertEquals(DecimalNum.valueOf(15), result.getValue(1));
        assertEquals(DecimalNum.valueOf(25), result.getValue(2));
    }

    @Test
    public void testMultipliedByNumber() {
        NumericIndicator result = close.multipliedBy(2);
        assertEquals(DecimalNum.valueOf(20), result.getValue(0));
        assertEquals(DecimalNum.valueOf(40), result.getValue(1));
        assertEquals(DecimalNum.valueOf(60), result.getValue(2));
    }

    @Test
    public void testDividedByNumber() {
        NumericIndicator result = close.dividedBy(2);
        assertEquals(DecimalNum.valueOf(5), result.getValue(0));
        assertEquals(DecimalNum.valueOf(10), result.getValue(1));
        assertEquals(DecimalNum.valueOf(15), result.getValue(2));
    }

    @Test
    public void testMinNumber() {
        NumericIndicator result = close.min(25);
        assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        assertEquals(DecimalNum.valueOf(20), result.getValue(1));
        assertEquals(DecimalNum.valueOf(25), result.getValue(2));
    }

    @Test
    public void testMaxNumber() {
        NumericIndicator result = close.max(25);
        assertEquals(DecimalNum.valueOf(25), result.getValue(0));
        assertEquals(DecimalNum.valueOf(25), result.getValue(1));
        assertEquals(DecimalNum.valueOf(30), result.getValue(2));
    }

    @Test
    public void testCrossedOverNumber() {
        Rule rule = close.crossedOver(DecimalNum.valueOf(15));
        assertFalse(rule.isSatisfied(0));
        assertTrue(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testCrossedUnderNumber() {
        Rule rule = close.crossedUnder(DecimalNum.valueOf(25));
        // No crossing under because close is always <= 25 until index 2 where it exceeds
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void testIsGreaterThanNumber() {
        Rule rule = close.isGreaterThan(DecimalNum.valueOf(20));
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    public void testIsLessThanNumber() {
        Rule rule = close.isLessThan(DecimalNum.valueOf(20));
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
    }

}
```
###Test END##