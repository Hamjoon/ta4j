###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Both public constructors</li>
 *   <li>getBarCount()</li>
 *   <li>getCountOfUnstableBars()</li>
 *   <li>getValue(int) – behaviour before and after the unstable‑bars threshold</li>
 *   <li>getTRIndicator()</li>
 *   <li>toString()</li>
 * </ul>
 *
 * Real TA4J objects (BaseBarSeries, BaseBar, DecimalNum, etc.) are used – no
 * mocking or subclassing of library types.
 */
public class ATRIndicatorTest {

    /** A small series with deterministic price data (5 bars). */
    private BarSeries series;

    /** True‑range indicator built from the series – reused in several tests. */
    private TRIndicator trIndicator;

    /** Bar count (time‑frame) used for the ATR indicator in most tests. */
    private static final int BAR_COUNT = 3;

    @Before
    public void setUp() {
        // Build a series: open, high, low, close are all deterministic.
        series = new BaseBarSeries("test-series");
        // Helper to create a Num from a double.
        Num n = DecimalNum::valueOf;

        // Bar 0 (no previous bar – needed for TR calculation)
        series.addBar(new BaseBar(
                series.numFactory().numberOf(1), // duration (ignored)
                DecimalNum.valueOf(10), // open
                DecimalNum.valueOf(12), // high
                DecimalNum.valueOf(8),  // low
                DecimalNum.valueOf(11), // close
                DecimalNum.valueOf(0)   // volume (ignored)
        ));

        // Bar 1
        series.addBar(new BaseBar(
                series.numFactory().numberOf(1),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(9),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(0)
        ));

        // Bar 2
        series.addBar(new BaseBar(
                series.numFactory().numberOf(1),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(0)
        ));

        // Bar 3
        series.addBar(new BaseBar(
                series.numFactory().numberOf(1),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(11),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(0)
        ));

        // Bar 4
        series.addBar(new BaseBar(
                series.numFactory().numberOf(1),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(16),
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(0)
        ));

        // TRIndicator built from the series – this is the true‑range source.
        trIndicator = new TRIndicator(series);
    }

    /** Helper that creates an ATRIndicator using the series‑based constructor. */
    private ATRIndicator createAtrFromSeries() {
        return new ATRIndicator(series, BAR_COUNT);
    }

    /** Helper that creates an ATRIndicator using the TRIndicator‑based constructor. */
    private ATRIndicator createAtrFromTR() {
        return new ATRIndicator(trIndicator, BAR_COUNT);
    }

    @Test
    public void testBarCount() {
        ATRIndicator atr = createAtrFromSeries();
        assertEquals("Bar count must be the one supplied to the constructor",
                BAR_COUNT, atr.getBarCount());
    }

    @Test
    public void testGetCountOfUnstableBars() {
        ATRIndicator atr = createAtrFromSeries();
        // TRIndicator's unstable bars are the number of bars needed before a true range can be computed.
        int expected = trIndicator.getCountOfUnstableBars() + BAR_COUNT;
        assertEquals("Unstable bar count must be TR's unstable bars + barCount",
                expected, atr.getCountOfUnstableBars());
    }

    @Test
    public void testGetValueBeforeUnstableBarsReturnsNaN() {
        ATRIndicator atr = createAtrFromSeries();
        int unstable = atr.getCountOfUnstableBars();

        // All indexes strictly less than 'unstable' must return NaN.
        for (int i = 0; i < unstable; i++) {
            Num value = atr.getValue(i);
            assertTrue("Value at index " + i + " should be NaN", value.isNaN());
        }
    }

    @Test
    public void testGetValueAfterUnstableBarsMatchesMMAIndicator() {
        ATRIndicator atr = createAtrFromSeries();

        // The internal MMAIndicator is not directly accessible, but we can recreate an identical one.
        MMAIndicator expectedMma = new MMAIndicator(trIndicator, BAR_COUNT);
        int start = atr.getCountOfUnstableBars(); // first stable index

        for (int i = start; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMma.getValue(i);
            assertEquals("ATR value must equal the underlying MMA value at index " + i,
                    mmaValue, atrValue);
        }
    }

    @Test
    public void testGetTRIndicatorCreatesNewIndicatorWithSameSeries() {
        ATRIndicator atr = createAtrFromSeries();
        TRIndicator newTr = atr.getTRIndicator();

        assertNotNull("TRIndicator returned by getTRIndicator must not be null", newTr);
        assertSame("The TRIndicator must be built on the same BarSeries as the ATRIndicator",
                series, newTr.getBarSeries());
    }

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        ATRIndicator atr = createAtrFromSeries();
        String s = atr.toString();

        assertTrue("toString must contain the simple class name",
                s.contains("ATRIndicator"));
        assertTrue("toString must contain the bar count",
                s.contains("barCount: " + BAR_COUNT));
    }

    @Test
    public void testBothConstructorsYieldIdenticalBehaviour() {
        ATRIndicator atrFromSeries = createAtrFromSeries();
        ATRIndicator atrFromTR = createAtrFromTR();

        // They should have the same configuration.
        assertEquals(atrFromSeries.getBarCount(), atrFromTR.getBarCount());
        assertEquals(atrFromSeries.getCountOfUnstableBars(),
                atrFromTR.getCountOfUnstableBars());

        // Compare values for all possible indices.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num v1 = atrFromSeries.getValue(i);
            Num v2 = atrFromTR.getValue(i);
            assertEquals("Values from both constructors must be equal at index " + i,
                    v1, v2);
        }
    }
}
```
###Test END##