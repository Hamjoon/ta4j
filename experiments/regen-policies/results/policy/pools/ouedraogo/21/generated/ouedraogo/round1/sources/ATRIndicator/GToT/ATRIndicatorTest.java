package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

/**
 * JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Public API extraction and basic functional verification</li>
 *   <li>Edge‑case handling (zero / negative barCount, unstable bars)</li>
 *   <li>Correct delegation to the internal {@link TRIndicator} and {@link MMAIndicator}</li>
 *   <li>String representation</li>
 * </ul>
 *
 * All objects are created using only the constructors and methods listed in the
 * supplied API description – no mocking or subclassing of library interfaces is
 * performed.
 */
public class ATRIndicatorTest {

    private BarSeries series;
    private Bar bar0, bar1, bar2;
    private Num open0, high0, low0, close0;
    private Num open1, high1, low1, close1;
    private Num open2, high2, low2, close2;

    /**
     * Builds a small {@link BarSeries} with three deterministic bars.
     *
     * <pre>
     * Index 0 : open=10, high=15, low=5,  close=12
     * Index 1 : open=12, high=18, low=11, close=17
     * Index 2 : open=17, high=20, low=16, close=19
     * </pre>
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");

        // Helper to obtain Num instances from the series’ factory
        DecimalNum numFactory = DecimalNum.valueOf(0).getNumFactory();

        open0 = numFactory.numOf(10);
        high0 = numFactory.numOf(15);
        low0  = numFactory.numOf(5);
        close0= numFactory.numOf(12);
        bar0 = new BaseBar(Duration.ofMinutes(1), ZonedDateTime.now(),
                open0, high0, low0, close0, numFactory.numOf(0));

        open1 = numFactory.numOf(12);
        high1 = numFactory.numOf(18);
        low1  = numFactory.numOf(11);
        close1= numFactory.numOf(17);
        bar1 = new BaseBar(Duration.ofMinutes(1), ZonedDateTime.now().plusMinutes(1),
                open1, high1, low1, close1, numFactory.numOf(0));

        open2 = numFactory.numOf(17);
        high2 = numFactory.numOf(20);
        low2  = numFactory.numOf(16);
        close2= numFactory.numOf(19);
        bar2 = new BaseBar(Duration.ofMinutes(1), ZonedDateTime.now().plusMinutes(2),
                open2, high2, low2, close2, numFactory.numOf(0));

        series.addBar(bar0);
        series.addBar(bar1);
        series.addBar(bar2);
    }

    /** -------------------------------------------------------------------- *
     *  PUBLIC METHOD: getValue(int)
     *  -------------------------------------------------------------------- */
    @Test
    public void testGetValue_WithBarCountOne_EqualsTR() {
        // ATR with a period of 1 must be identical to the TR value
        int barCount = 1;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        TRIndicator tr = atr.getTRIndicator();

        // verify for each index that ATR == TR
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num trValue  = tr.getValue(i);
            assertEquals("ATR should equal TR when barCount=1 at index " + i,
                    trValue, atrValue);
        }
    }

    @Test
    public void testGetValue_WithBarCountThree_ComputesMMA() {
        // With a period > 1 we cannot know the exact numeric result without
        // reproducing the MMA algorithm, but we can at least assert that the
        // value is non‑negative and that it equals the underlying MMAIndicator.
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        MMAIndicator mma = (MMAIndicator) atr.averageTrueRangeIndicator; // package‑private access via reflection

        // Use reflection to fetch the private field (allowed for test purposes)
        try {
            java.lang.reflect.Field field = ATRIndicator.class.getDeclaredField("averageTrueRangeIndicator");
            field.setAccessible(true);
            MMAIndicator internalMMA = (MMAIndicator) field.get(atr);
            for (int i = 0; i < series.getBarCount(); i++) {
                Num atrValue = atr.getValue(i);
                Num mmaValue = internalMMA.getValue(i);
                assertEquals("ATR should delegate to MMAIndicator", mmaValue, atrValue);
                assertTrue("ATR value must be non‑negative", atrValue.isPositiveOrZero());
            }
        } catch (Exception e) {
            fail("Reflection error while accessing internal MMAIndicator: " + e);
        }
    }

    /** -------------------------------------------------------------------- *
     *  PUBLIC METHOD: getUnstableBars()
     *  -------------------------------------------------------------------- */
    @Test
    public void testGetUnstableBars_ReturnsBarCount() {
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("Unstable bars should equal the configured barCount",
                barCount, atr.getUnstableBars());
    }

    /** -------------------------------------------------------------------- *
     *  PUBLIC METHOD: getTRIndicator()
     *  -------------------------------------------------------------------- */
    @Test
    public void testGetTRIndicator_ReturnsSameInstance() {
        ATRIndicator atr = new ATRIndicator(series, 5);
        TRIndicator tr = atr.getTRIndicator();
        assertNotNull("TRIndicator must not be null", tr);
        // The TRIndicator must be built on the same series as the ATRIndicator
        assertSame("TRIndicator should reference the same BarSeries",
                series, tr.getBarSeries());
    }

    /** -------------------------------------------------------------------- *
     *  PUBLIC METHOD: getBarCount()
     *  -------------------------------------------------------------------- */
    @Test
    public void testGetBarCount_DelegatesToMMAIndicator() {
        int barCount = 4;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("getBarCount should return the MMA period",
                barCount, atr.getBarCount());
    }

    /** -------------------------------------------------------------------- *
     *  PUBLIC METHOD: toString()
     *  -------------------------------------------------------------------- */
    @Test
    public void testToString_IncludesClassNameAndBarCount() {
        int barCount = 6;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        String txt = atr.toString();
        assertTrue("toString must contain class name",
                txt.contains("ATRIndicator"));
        assertTrue("toString must contain the bar count",
                txt.contains(String.valueOf(barCount)));
    }

    /** -------------------------------------------------------------------- *
     *  EDGE CASES & EXCEPTION HANDLING
     *  -------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeBarCount_ThrowsException() {
        // MMAIndicator validates that barCount > 0; passing a negative value must
        // result in an IllegalArgumentException.
        new ATRIndicator(series, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroBarCount_ThrowsException() {
        // Zero is also invalid for a moving‑average period.
        new ATRIndicator(series, 0);
    }

    @Test
    public void testGetValue_IndexOutOfBounds_ThrowsException() {
        ATRIndicator atr = new ATRIndicator(series, 2);
        int invalidIndex = series.getBarCount(); // one past last valid index
        try {
            atr.getValue(invalidIndex);
            fail("Expected IndexOutOfBoundsException for index " + invalidIndex);
        } catch (IndexOutOfBoundsException e) {
            // expected path
        }
    }

    /** -------------------------------------------------------------------- *
     *  INTERNAL CONSISTENCY – verify that the ATRIndicator really *is* an
     *  Indicator (inherits from AbstractIndicator) and therefore returns the
     *  correct BarSeries.
     *  -------------------------------------------------------------------- */
    @Test
    public void testIndicatorContract_GetBarSeries() {
        ATRIndicator atr = new ATRIndicator(series, 3);
        assertSame("Indicator should expose the original BarSeries",
                series, atr.getBarSeries());
    }
}
