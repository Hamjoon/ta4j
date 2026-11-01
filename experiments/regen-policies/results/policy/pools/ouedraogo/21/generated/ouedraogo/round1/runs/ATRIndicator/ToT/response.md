###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Construction via both constructors</li>
 *   <li>Correct delegation to {@link MMAIndicator}</li>
 *   <li>Returned bar‑count, unstable‑bars and {@link TRIndicator} instance</li>
 *   <li>String representation</li>
 *   <li>Edge cases (single‑bar series, barCount = 1)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Only classes, constructors and methods that appear in the supplied source
 * or in the listed dependency signatures are used.  No mocking or subclassing
 * of library interfaces is performed.
 * </p>
 */
public class ATRIndicatorTest {

    /** Series used for most tests (5 bars, deterministic prices). */
    private BarSeries series;

    /** Bar‑count for the ATR (moving‑average) window. */
    private static final int BAR_COUNT = 3;

    /** Helper to create a {@link Bar} with the given OHLC values. */
    private static Bar barOf(double open, double high, double low, double close) {
        // TA4J uses {@link DecimalNum} as the default {@link Num} implementation.
        Num o = DecimalNum.valueOf(open);
        Num h = DecimalNum.valueOf(high);
        Num l = DecimalNum.valueOf(low);
        Num c = DecimalNum.valueOf(close);
        // The time component is irrelevant for the calculations, so we use now().
        return new BaseBar(ZonedDateTime.now(), o, h, l, c, DecimalNum.valueOf(0), DecimalNum.valueOf(0));
    }

    @Before
    public void setUp() {
        // Build a deterministic series of 5 bars.
        series = new BaseBarSeries("test-series");
        series.addBar(barOf(10, 12, 9, 11)); // index 0
        series.addBar(barOf(11, 13, 10, 12)); // index 1
        series.addBar(barOf(12, 14, 11, 13)); // index 2
        series.addBar(barOf(13, 15, 12, 14)); // index 3
        series.addBar(barOf(14, 16, 13, 15)); // index 4
    }

    /** --------------------------------------------------------------------- *
     *  Test construction using the (BarSeries, int) constructor.
     *  --------------------------------------------------------------------- */
    @Test
    public void testConstructorWithSeries() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);

        // The internal TRIndicator must refer to the same series.
        assertSame(series, atr.getTRIndicator().getBarSeries());

        // Bar‑count must be propagated correctly.
        assertEquals(BAR_COUNT, atr.getBarCount());

        // Unstable bars are defined as the bar‑count.
        assertEquals(BAR_COUNT, atr.getUnstableBars());

        // toString must contain class name and barCount.
        String s = atr.toString();
        assertTrue(s.contains("ATRIndicator"));
        assertTrue(s.contains(String.valueOf(BAR_COUNT)));
    }

    /** --------------------------------------------------------------------- *
     *  Test construction using the (TRIndicator, int) constructor.
     *  --------------------------------------------------------------------- */
    @Test
    public void testConstructorWithTRIndicator() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);

        // The stored TRIndicator must be the same instance we passed.
        assertSame(tr, atr.getTRIndicator());

        // Bar‑count must be identical.
        assertEquals(BAR_COUNT, atr.getBarCount());
    }

    /** --------------------------------------------------------------------- *
     *  Verify that {@code getValue(int)} simply forwards to the underlying
     *  {@link MMAIndicator}.  The test computes the expected value using a
     *  fresh MMAIndicator built with the same TRIndicator and compares.
     *  --------------------------------------------------------------------- */
    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        MMAIndicator expectedMMA = new MMAIndicator(new TRIndicator(series), BAR_COUNT);

        int end = series.getBarCount();
        for (int i = 0; i < end; i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMMA.getValue(i);
            assertEquals("ATR value at index " + i + " differs from expected MMA value",
                    mmaValue, atrValue);
        }
    }

    /** --------------------------------------------------------------------- *
     *  Edge case: a series containing a single bar.  With {@code barCount = 1}
     *  the ATR should be exactly the first TR value (no averaging needed).
     *  --------------------------------------------------------------------- */
    @Test
    public void testSingleBarSeriesBarCountOne() {
        BarSeries singleBarSeries = new BaseBarSeries("single");
        singleBarSeries.addBar(barOf(5, 7, 4, 6)); // only bar, index 0

        ATRIndicator atr = new ATRIndicator(singleBarSeries, 1);
        TRIndicator tr = new TRIndicator(singleBarSeries);

        // For a single bar the TR is max(high‑low, |high‑prevClose|, |low‑prevClose|)
        // but there is no previous close, so it reduces to high‑low.
        Num expectedTR = DecimalNum.valueOf(7).minus(DecimalNum.valueOf(4)); // 3
        Num atrValue = atr.getValue(0);
        Num trValue = tr.getValue(0);
        assertEquals("ATR should equal TR for the first bar when barCount=1", trValue, atrValue);
        assertEquals("TR of the single bar is high‑low", expectedTR, trValue);
    }

    /** --------------------------------------------------------------------- *
     *  Edge case: barCount larger than the number of bars in the series.
     *  The MMAIndicator (and therefore ATR) must still return a value for every
     *  index without throwing.  The test simply checks that no exception is
     *  thrown and that the returned value is non‑null.
     *  --------------------------------------------------------------------- */
    @Test
    public void testBarCountLargerThanSeries() {
        int hugeBarCount = 10; // series has only 5 bars
        ATRIndicator atr = new ATRIndicator(series, hugeBarCount);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num value = atr.getValue(i);
            assertNotNull("ATR value must not be null even when barCount > series size", value);
        }
    }

    /** --------------------------------------------------------------------- *
     *  Verify that the indicator works correctly when the underlying series
     *  is extended after construction.  Adding a new bar should be reflected
     *  in subsequent {@code getValue} calls.
     *  --------------------------------------------------------------------- */
    @Test
    public void testSeriesExtensionAfterConstruction() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        int originalSize = series.getBarCount();

        // Add a new bar.
        series.addBar(barOf(15, 17, 14, 16));
        int newSize = series.getBarCount();
        assertEquals(originalSize + 1, newSize);

        // The ATR value for the newly added index must be computable and non‑null.
        Num newAtrValue = atr.getValue(newSize - 1);
        assertNotNull("ATR value for newly added bar must be non‑null", newAtrValue);
    }

    /** --------------------------------------------------------------------- *
     *  Ensure that {@code getBarCount()} and {@code getUnstableBars()} stay in
     *  sync when the underlying MMAIndicator is queried directly.
     *  --------------------------------------------------------------------- */
    @Test
    public void testBarCountAndUnstableBarsConsistency() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals(atr.getBarCount(), atr.getUnstableBars());
    }

    /** --------------------------------------------------------------------- *
     *  Defensive test: passing a null {@link TRIndicator} to the constructor
     *  must result in a {@link NullPointerException}.  This validates the
     *  contract that the class does not accept null dependencies.
     *  --------------------------------------------------------------------- */
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullTRIndicator() {
        new ATRIndicator(null, BAR_COUNT);
    }

    /** --------------------------------------------------------------------- *
     *  Defensive test: passing a null {@link BarSeries} to the (BarSeries,int)
     *  constructor must also throw a {@link NullPointerException}.
     *  --------------------------------------------------------------------- */
    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullSeries() {
        new ATRIndicator((BarSeries) null, BAR_COUNT);
    }
}
```
###Test END##