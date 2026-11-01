package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;


import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>The tests are written from the point of view of three independent experts,
 * each proposing one test per public method of {@code ATRIndicator}.  The
 * suite therefore covers:</p>
 *
 * <ul>
 *   <li>Typical usage – checking that {@code getValue} returns the same value
 *       as the underlying {@link MMAIndicator}.</li>
 *   <li>Edge cases – very short series, empty series and a bar‑count of {@code 1}.</li>
 *   <li>Error scenarios – requesting a value at an out‑of‑range index.</li>
 *   <li>Behaviour of accessor methods {@code getBarCount},
 *       {@code getTRIndicator}, {@code getUnstableBars} and {@code toString}.</li>
 * </ul>
 *
 * <p>No mocking or subclassing of library interfaces is performed; the tests
 * use the concrete {@link BaseBarSeries} and {@link BaseBar} classes that are
 * part of TA4J.</p>
 */
public class ATRIndicatorTest {

    private BarSeries series;
    private static final ZoneId ZONE = ZoneId.of("UTC");

    /**
     * Builds a {@link BarSeries} with three deterministic bars.
     *
     * <pre>
     * Bar 0 : O=10, H=15, L=9,  C=14
     * Bar 1 : O=14, H=16, L=13, C=15
     * Bar 2 : O=15, H=18, L=14, C=17
     * </pre>
     *
     * These values are sufficient to compute a known true‑range and, via the
     * {@link MMAIndicator}, a known average true range.
     */
    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");
        // Bar 0 (no previous close, TR = high‑low)
        series.addBar(new BaseBar(
                ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZONE),
                DecimalNum.valueOf(10),   // open
                DecimalNum.valueOf(15),   // high
                DecimalNum.valueOf(9),    // low
                DecimalNum.valueOf(14),  // close
                DecimalNum.valueOf(0)     // volume (irrelevant for TR)
        ));
        // Bar 1
        series.addBar(new BaseBar(
                ZonedDateTime.of(2021, 1, 2, 0, 0, 0, 0, ZONE),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(16),
                DecimalNum.valueOf(13),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(0)
        ));
        // Bar 2
        series.addBar(new BaseBar(
                ZonedDateTime.of(2021, 1, 3, 0, 0, 0, 0, ZONE),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(18),
                DecimalNum.valueOf(14),
                DecimalNum.valueOf(17),
                DecimalNum.valueOf(0)
        ));
    }

    /* --------------------------------------------------------------------- *
     *  Expert 1 – test for getValue()
     * --------------------------------------------------------------------- */
    @Test
    public void testGetValueEqualsMMAIndicator() {
        int barCount = 2;                         // simple 2‑period ATR
        ATRIndicator atr = new ATRIndicator(series, barCount);
        MMAIndicator expectedMMA = new MMAIndicator(new TRIndicator(series), barCount);

        // Compare value for each index that exists in the series
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMMA.getValue(i);
            assertEquals("ATR value differs from MMA at index " + i, mmaValue, atrValue);
        }
    }

    /* --------------------------------------------------------------------- *
     *  Expert 2 – test for getBarCount() and getUnstableBars()
     * --------------------------------------------------------------------- */
    @Test
    public void testBarCountAndUnstableBars() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // The bar count reported by ATR must be the one given to the constructor
        assertEquals("ATR.getBarCount() should return the constructor barCount",
                barCount, atr.getBarCount());

        // Unstable bars are defined as the same number (the indicator needs that
        // many preceding values before becoming stable)
        assertEquals("ATR.getUnstableBars() should be equal to barCount",
                barCount, atr.getUnstableBars());
    }

    /* --------------------------------------------------------------------- *
     *  Expert 3 – test for getTRIndicator(), toString() and error handling
     * --------------------------------------------------------------------- */
    @Test
    public void testGetTRIndicatorAndToString() {
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        TRIndicator tr = atr.getTRIndicator();

        // The TRIndicator inside ATR must reference the same BarSeries
        assertSame("TRIndicator should be built on the same series",
                series, tr.getBarSeries());

        // toString must contain the class name and the barCount
        String s = atr.toString();
        assertTrue("toString() must contain class name", s.contains("ATRIndicator"));
        assertTrue("toString() must contain barCount", s.contains("barCount: " + barCount));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithInvalidIndex() {
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        // Requesting an index beyond the series length must throw an exception
        atr.getValue(series.getBarCount()); // out of range
    }

    /* --------------------------------------------------------------------- *
     *  Additional edge‑case tests (not tied to a specific expert)
     * --------------------------------------------------------------------- */
    @Test
    public void testSingleBarSeries() {
        BarSeries singleBarSeries = new BaseBarSeries("single");
        singleBarSeries.addBar(new BaseBar(
                ZonedDateTime.now(),
                DecimalNum.valueOf(100),
                DecimalNum.valueOf(110),
                DecimalNum.valueOf(95),
                DecimalNum.valueOf(105),
                DecimalNum.valueOf(0)
        ));

        int barCount = 1;
        ATRIndicator atr = new ATRIndicator(singleBarSeries, barCount);
        // With only one bar, the true range equals high‑low; MMA with barCount=1
        // is simply the current TR value.
        Num expected = new TRIndicator(singleBarSeries).getValue(0);
        assertEquals(expected, atr.getValue(0));
    }

    @Test
    public void testEmptySeriesBehaviour() {
        BarSeries emptySeries = new BaseBarSeries("empty");
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(emptySeries, barCount);
        // Any call to getValue should fail because there is no bar at index 0
        try {
            atr.getValue(0);
            fail("Expected IndexOutOfBoundsException for empty series");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithExplicitTRIndicator() {
        TRIndicator tr = new TRIndicator(series);
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        // The internal TR must be exactly the one we passed
        assertSame("ATR should keep the supplied TRIndicator", tr, atr.getTRIndicator());
        // The barCount must still be honoured
        assertEquals(barCount, atr.getBarCount());
    }
}
