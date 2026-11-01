###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.averages.MMAIndicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.CachedIndicator;
import org.ta4j.core.indicators.helpers.AbstractEMAIndicator;

/**
 * JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Construction via both constructors (with {@link BarSeries} and with an existing {@link TRIndicator}).</li>
 *   <li>Public getters: {@code getTRIndicator()}, {@code getBarCount()}, {@code getCountOfUnstableBars()}.</li>
 *   <li>Delegation of {@code getValue(int)} to the underlying {@link MMAIndicator}.</li>
 *   <li>Correct {@code toString()} format.</li>
 *   <li>Behaviour with illegal arguments (null series, non‑positive bar count).</li>
 * </ul>
 * </p>
 *
 * <p>
 * The test data uses a small {@link BarSeries} (5 bars) with deterministic price
 * values so that the {@link TRIndicator} and, consequently, the {@link ATRIndicator}
 * produce reproducible results.
 * </p>
 */
public class ATRIndicatorTest {

    /** A simple series with 5 bars used by all tests. */
    private BarSeries series;

    /** The true‑range indicator built from {@link #series}. */
    private TRIndicator trIndicator;

    /** Bar count used for the ATR / MMA calculations. */
    private static final int BAR_COUNT = 3;

    @Before
    public void setUp() {
        // Build a series with deterministic OHLC values.
        // The concrete implementation used by the library is BaseBarSeries.
        // It is part of the public API of ta4j, therefore permissible.
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Helper to create Num instances (DecimalNum is the default implementation).
        // Using DecimalNum directly avoids the need for a NumFactory reference.
        // All prices are chosen so that the true range can be calculated by hand.
        addBar(ZonedDateTime.now().minusDays(5), 10, 12, 9, 11);
        addBar(ZonedDateTime.now().minusDays(4), 11, 13, 10, 12);
        addBar(ZonedDateTime.now().minusDays(3), 12, 14, 11, 13);
        addBar(ZonedDateTime.now().minusDays(2), 13, 15, 12, 14);
        addBar(ZonedDateTime.now().minusDays(1), 14, 16, 13, 15);

        trIndicator = new TRIndicator(series);
    }

    /** Convenience wrapper to add a bar to {@link #series}. */
    private void addBar(ZonedDateTime time, double open, double high, double low, double close) {
        Bar bar = new org.ta4j.core.BaseBar(
                time,
                DecimalNum.valueOf(open),
                DecimalNum.valueOf(high),
                DecimalNum.valueOf(low),
                DecimalNum.valueOf(close),
                DecimalNum.ZERO); // volume = 0 (not used for TR)
        series.addBar(bar);
    }

    /** --------------------------------------------------------------------- **/
    @Test
    public void testConstructorWithSeriesCreatesTRIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // The internal TRIndicator must be built from the same series.
        assertNotNull(atr.getTRIndicator());
        assertSame(series, atr.getTRIndicator().getBarSeries());
    }

    @Test
    public void testConstructorWithExistingTRIndicatorKeepsReference() {
        TRIndicator customTr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(customTr, BAR_COUNT);
        assertSame(customTr, atr.getTRIndicator());
    }

    /** --------------------------------------------------------------------- **/
    @Test
    public void testGetBarCountReturnsConstructorValue() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals(BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testGetCountOfUnstableBarsEqualsBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals(BAR_COUNT, atr.getCountOfUnstableBars());
    }

    /** --------------------------------------------------------------------- **/
    @Test
    public void testToStringContainsClassNameAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue(s.contains("ATRIndicator"));
        assertTrue(s.contains("barCount: " + BAR_COUNT));
    }

    /** --------------------------------------------------------------------- **/
    @Test
    public void testGetValueDelegatesToMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // Build a stand‑alone MMAIndicator with the same TRIndicator for expected values.
        MMAIndicator expectedMma = new MMAIndicator(trIndicator, BAR_COUNT);

        // Verify the delegation for each index of the series.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMma.getValue(i);
            assertEquals("ATR value at index " + i + " differs from MMA value", mmaValue, atrValue);
        }
    }

    /** --------------------------------------------------------------------- **/
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsZeroBarCount() {
        // MMAIndicator validates the period; a zero period must raise an exception.
        new ATRIndicator(series, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNegativeBarCount() {
        new ATRIndicator(series, -5);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullSeriesThrowsNPE() {
        new ATRIndicator((BarSeries) null, BAR_COUNT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullTRIndicatorThrowsNPE() {
        new ATRIndicator((TRIndicator) null, BAR_COUNT);
    }

    /** --------------------------------------------------------------------- **/
    @Test
    public void testATRValuesAgainstManualMMAComputation() {
        // Manual calculation of the MMA (modified moving average) for the chosen data.
        // First, compute the true‑range (TR) for each bar.
        Num[] tr = new Num[series.getBarCount()];
        for (int i = 0; i < series.getBarCount(); i++) {
            tr[i] = trIndicator.getValue(i);
        }

        // MMA algorithm (as implemented in ta4j):
        //   prev = tr[0];
        //   for i = 1 .. n-1:
        //       prev = prev.plus(tr[i].minus(prev).dividedBy(barCount));
        // The first BAR_COUNT‑1 values are the simple average of the first i+1 TRs.
        Num[] expectedAtr = new Num[tr.length];
        Num sum = DecimalNum.ZERO;
        for (int i = 0; i < tr.length; i++) {
            sum = sum.plus(tr[i]);
            if (i + 1 < BAR_COUNT) {
                // simple average of the first (i+1) values
                expectedAtr[i] = sum.dividedBy(DecimalNum.valueOf(i + 1));
            } else if (i + 1 == BAR_COUNT) {
                // first MMA value = simple average of the first BAR_COUNT TRs
                expectedAtr[i] = sum.dividedBy(DecimalNum.valueOf(BAR_COUNT));
            } else {
                // MMA recurrence: prev + (tr[i] - prev) / BAR_COUNT
                expectedAtr[i] = expectedAtr[i - 1].plus(
                        tr[i].minus(expectedAtr[i - 1]).dividedBy(DecimalNum.valueOf(BAR_COUNT)));
            }
        }

        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("ATR value at index " + i + " does not match manual MMA",
                    expectedAtr[i], atr.getValue(i));
        }
    }
}
```
###Test END##