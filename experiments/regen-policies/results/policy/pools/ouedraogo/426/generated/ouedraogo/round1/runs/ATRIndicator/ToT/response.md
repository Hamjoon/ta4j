###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.indicators.ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Construction via the two public constructors (series‑based and TRIndicator‑based)</li>
 *   <li>Behaviour of {@code getValue(int)}</li>
 *   <li>Unstable‑bars handling</li>
 *   <li>Accessor methods {@code getCountOfUnstableBars()}, {@code getBarCount()},
 *       {@code getTRIndicator()} and {@code toString()}</li>
 *   <li>Negative / error scenarios (null arguments)</li>
 * </ul>
 *
 * The test uses the concrete implementations shipped with TA‑4J
 * (<code>BaseBarSeries</code>, <code>BaseBar</code>, etc.).  No mocking or
 * subclassing of library interfaces is performed – only public constructors
 * and methods that are part of the supplied API are used.
 */
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.averages.MMAIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

/**
 * Comprehensive unit tests for {@link ATRIndicator}.
 */
public class ATRIndicatorTest {

    /** Simple series with five identical bars (high=10, low=5, close=8). */
    private BarSeries series;
    /** Number factory for creating {@link Num} instances. */
    private NumFactory numFactory;

    @Before
    public void setUp() {
        // The concrete implementation used by TA‑4J – it implements BarSeries.
        series = new BaseBarSeries("test-series");
        numFactory = series.numFactory();

        // Build five identical bars.  The time period (duration) is not relevant for the
        // calculations, therefore we use a dummy value of 1 minute.
        for (int i = 0; i < 5; i++) {
            // BaseBar(timePeriod, openPrice, highPrice, lowPrice, closePrice, volume)
            Bar bar = new BaseBar(
                    // dummy duration – 1 minute (in seconds)
                    java.time.Duration.ofMinutes(1),
                    // open price (any value – not used by TR)
                    numFactory.numOf(8),
                    // high price
                    numFactory.numOf(10),
                    // low price
                    numFactory.numOf(5),
                    // close price
                    numFactory.numOf(8),
                    // volume – arbitrary non‑zero value
                    numFactory.numOf(1000)
            );
            series.addBar(bar);
        }
    }

    /**
     * Helper that creates an {@link ATRIndicator} with a given {@code barCount}.
     */
    private ATRIndicator createAtr(int barCount) {
        return new ATRIndicator(series, barCount);
    }

    /**
     * Helper that creates an {@link ATRIndicator} using an explicit {@link TRIndicator}.
     */
    private ATRIndicator createAtrWithTr(int barCount) {
        TRIndicator tr = new TRIndicator(series);
        return new ATRIndicator(tr, barCount);
    }

    /**
     * The true‑range for the series constructed in {@link #setUp()} is constant:
     * <pre>
     *   high – low = 10 – 5 = 5
     * </pre>
     * Because all bars are identical, the other two TR components
     * {@code |high‑prevClose|} and {@code |low‑prevClose|} are also 5,
     * therefore the TR value is always {@code 5}.
     *
     * The {@link MMAIndicator} (used internally) returns the same constant when
     * its input is constant, irrespective of the smoothing factor.  Consequently,
     * the expected ATR value after the unstable‑bars period is exactly {@code 5}.
     */
    private Num expectedConstantTR() {
        return numFactory.numOf(5);
    }

    /* --------------------------------------------------------------------- */
    /* 1. Constructor tests                                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void constructor_seriesAndBarCount_createsIndicator() {
        int barCount = 3;
        ATRIndicator atr = createAtr(barCount);
        assertNotNull("ATRIndicator must not be null", atr);
        assertEquals("Bar count must be stored correctly", barCount, atr.getBarCount());
        assertSame("BarSeries must be the one supplied to the constructor",
                series, atr.getBarSeries());
    }

    @Test
    public void constructor_trIndicatorAndBarCount_createsIndicator() {
        int barCount = 2;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        assertNotNull("ATRIndicator must not be null", atr);
        assertEquals("Bar count must be stored correctly", barCount, atr.getBarCount());
        assertSame("BarSeries must be derived from the TRIndicator",
                series, atr.getBarSeries());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_nullTRIndicator_throwsNPE() {
        // The validation performed in validatedConfig() should reject null.
        new ATRIndicator(null, 4);
    }

    /* --------------------------------------------------------------------- */
    /* 2. getCountOfUnstableBars tests                                        */
    /* --------------------------------------------------------------------- */

    @Test
    public void getCountOfUnstableBars_returnsSumOfTRUnstableAndBarCount() {
        // TRIndicator for our series has 1 unstable bar (the first one has no previous close)
        TRIndicator tr = new TRIndicator(series);
        int trUnstable = tr.getCountOfUnstableBars(); // expected 1
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        int expected = trUnstable + barCount;
        assertEquals("Unstable bars should be sum of TR's unstable bars and barCount",
                expected, atr.getCountOfUnstableBars());
    }

    /* --------------------------------------------------------------------- */
    /* 3. getValue tests                                                      */
    /* --------------------------------------------------------------------- */

    @Test
    public void getValue_beforeUnstableBars_returnsNaN() {
        int barCount = 2;
        ATRIndicator atr = createAtr(barCount);
        int unstable = atr.getCountOfUnstableBars();

        for (int i = 0; i < unstable; i++) {
            Num value = atr.getValue(i);
            assertTrue("Value at index " + i + " must be NaN", value.isNaN());
        }
    }

    @Test
    public void getValue_afterUnstableBars_returnsConstantTR() {
        int barCount = 2;
        ATRIndicator atr = createAtr(barCount);
        int unstable = atr.getCountOfUnstableBars();

        // For indices >= unstable the ATR should be the constant TR (=5)
        Num expected = expectedConstantTR();
        for (int i = unstable; i < series.getBarCount(); i++) {
            Num value = atr.getValue(i);
            assertEquals("ATR value at index " + i + " must equal constant TR",
                    0, expected.compareTo(value));
        }
    }

    @Test
    public void getValue_withExplicitTRIndicator_behavesIdentically() {
        int barCount = 3;
        ATRIndicator atrFromSeries = createAtr(barCount);
        ATRIndicator atrFromTR = createAtrWithTr(barCount);

        int unstable = atrFromSeries.getCountOfUnstableBars();
        Num expected = expectedConstantTR();

        for (int i = unstable; i < series.getBarCount(); i++) {
            Num v1 = atrFromSeries.getValue(i);
            Num v2 = atrFromTR.getValue(i);
            assertEquals("Both constructors must yield the same value at index " + i,
                    0, v1.compareTo(v2));
            assertEquals("Value must equal constant TR", 0, expected.compareTo(v1));
        }
    }

    /* --------------------------------------------------------------------- */
    /* 4. Accessor method tests                                               */
    /* --------------------------------------------------------------------- */

    @Test
    public void getBarCount_returnsConfiguredBarCount() {
        int barCount = 4;
        ATRIndicator atr = createAtr(barCount);
        assertEquals(barCount, atr.getBarCount());
    }

    @Test
    public void getTRIndicator_returnsNewTRIndicatorWithSameSeries() {
        int barCount = 3;
        ATRIndicator atr = createAtr(barCount);
        TRIndicator trFromAtr = atr.getTRIndicator();

        assertNotNull(trFromAtr);
        assertSame("TRIndicator must be based on the same BarSeries",
                series, trFromAtr.getBarSeries());

        // The new TRIndicator must compute the same constant TR values as the original.
        Num expected = expectedConstantTR();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num v = trFromAtr.getValue(i);
            assertEquals("TR value at index " + i + " must be constant",
                    0, expected.compareTo(v));
        }
    }

    @Test
    public void toString_containsClassNameAndBarCount() {
        int barCount = 5;
        ATRIndicator atr = createAtr(barCount);
        String str = atr.toString();
        assertTrue("toString should contain class name", str.contains("ATRIndicator"));
        assertTrue("toString should contain barCount value", str.contains(String.valueOf(barCount)));
    }

    /* --------------------------------------------------------------------- */
    /* 5. Edge / error scenarios                                               */
    /* --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void constructor_negativeBarCount_throwsException() {
        // MMAIndicator (the underlying moving‑average) validates the barCount.
        // Passing a negative value should therefore result in an IllegalArgumentException.
        new ATRIndicator(series, -1);
    }

    @Test
    public void getValue_atLastIndex_returnsSameAsPreviousWhenSeriesIsConstant() {
        int barCount = 2;
        ATRIndicator atr = createAtr(barCount);
        int unstable = atr.getCountOfUnstableBars();

        // The series has 5 bars; we check the last valid index (4)
        int lastIdx = series.getBarCount() - 1;
        Num lastValue = atr.getValue(lastIdx);
        Num previousValue = atr.getValue(lastIdx - 1);

        // With a constant input the MMA should not change between consecutive points.
        assertEquals("Last ATR value must equal the previous one for a constant series",
                0, lastValue.compareTo(previousValue));
    }
}
```
###Test END##