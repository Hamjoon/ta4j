package org.ta4j.core.indicators;

import org.junit.Before;
import org.junit.Test;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.MMAIndicator;

import static org.junit.Assert.*;

/**
 * Test suite for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Both public constructors</li>
 *   <li>Delegation of {@code getValue}</li>
 *   <li>Correct unstable‑bars handling</li>
 *   <li>Accessors {@code getTRIndicator} and {@code getBarCount}</li>
 *   <li>String representation</li>
 * </ul>
 *
 * The test uses the concrete {@code BaseBarSeries} implementation that ships
 * with ta4j (it is part of the public API of the library).  No mocking or
 * subclassing of library interfaces is performed.
 */
public class ATRIndicatorTest {

    /** A simple series with deterministic price data used by all tests. */
    private BarSeries series;

    /** Number of bars in the series – kept small for readability. */
    private static final int SERIES_SIZE = 6;

    /**
     * Build a {@link BarSeries} containing a few bars with known OHLC values.
     * The series is built once before every test method.
     */
    @Before
    public void setUp() {
        // The concrete implementation of BarSeries in ta4j is {@code BaseBarSeries}.
        // It can be created through its builder.
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("ATR test series")
                .build();

        // Helper to convert primitive numbers to the library's {@code Num}.
        // The default implementation uses {@code DecimalNum}.
        // Prices are chosen so that the true‑range calculation is easy to verify.
        double[] opens  = {10, 11, 12, 13, 14, 15};
        double[] highs  = {12, 13, 14, 15, 16, 17};
        double[] lows   = {9, 10, 11, 12, 13, 14};
        double[] closes = {11, 12, 13, 14, 15, 16};

        for (int i = 0; i < SERIES_SIZE; i++) {
            Num open   = DecimalNum.valueOf(opens[i]);
            Num high   = DecimalNum.valueOf(highs[i]);
            Num low    = DecimalNum.valueOf(lows[i]);
            Num close  = DecimalNum.valueOf(closes[i]);
            Num volume = DecimalNum.valueOf(1000); // arbitrary, not used by ATR

            // {@code BaseBar} is the concrete implementation of {@code Bar}.
            org.ta4j.core.BaseBar bar = new org.ta4j.core.BaseBar(
                    // duration – not relevant for the calculations, use 1 minute
                    java.time.Duration.ofMinutes(1),
                    // end time – also irrelevant, use index as seconds
                    java.time.ZonedDateTime.now().plusSeconds(i),
                    open, high, low, close, volume);
            series.addBar(bar);
        }
    }

    /**
     * Verify that the constructor which receives a {@link BarSeries} creates
     * an internal {@link TRIndicator} and a {@link MMAIndicator} with the
     * correct bar count.
     */
    @Test
    public void testConstructorWithSeries() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // The internal TRIndicator must reference the same series.
        assertNotNull("TRIndicator should not be null", atr.getTRIndicator());
        assertSame("TRIndicator must be built on the provided series",
                series, atr.getTRIndicator().getBarSeries());

        // Bar count accessor must return the value given to the constructor.
        assertEquals("Bar count must match the value passed to the constructor",
                barCount, atr.getBarCount());

        // Unstable bars are defined as the bar count.
        assertEquals("Unstable bars should equal bar count", barCount, atr.getUnstableBars());
    }

    /**
     * Verify that the constructor which receives a pre‑built {@link TRIndicator}
     * keeps the same instance and respects the supplied bar count.
     */
    @Test
    public void testConstructorWithTRIndicator() {
        int barCount = 2;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);

        // The getter must return exactly the TRIndicator instance supplied.
        assertSame("ATRIndicator must keep the supplied TRIndicator", tr, atr.getTRIndicator());

        // Bar count accessor must reflect the constructor argument.
        assertEquals(barCount, atr.getBarCount());
    }

    /**
     * The core behaviour of {@code ATRIndicator} is to delegate the value
     * computation to an internal {@link MMAIndicator} that smooths the true
     * range.  The test creates a reference MMAIndicator and checks that both
     * objects return identical values for every index.
     */
    @Test
    public void testGetValueDelegation() {
        int barCount = 4;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        // Reference calculation using the same TRIndicator and bar count.
        MMAIndicator reference = new MMAIndicator(new TRIndicator(series), barCount);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num refValue = reference.getValue(i);
            // Compare using double precision; the library guarantees that
            // the underlying numeric implementation (DecimalNum) provides
            // a reliable double conversion.
            assertEquals("ATR value must equal reference MMA value at index " + i,
                    refValue.doubleValue(), atrValue.doubleValue(), 1e-10);
        }
    }

    /**
     * {@code getUnstableBars()} must return the same value as {@code getBarCount()}.
     */
    @Test
    public void testGetUnstableBars() {
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("Unstable bars should be equal to the bar count",
                atr.getBarCount(), atr.getUnstableBars());
    }

    /**
     * The {@code toString()} implementation should contain the simple class name
     * and the configured bar count.
     */
    @Test
    public void testToStringContainsClassNameAndBarCount() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        String txt = atr.toString();

        assertTrue("toString must contain class name",
                txt.contains(ATRIndicator.class.getSimpleName()));
        assertTrue("toString must contain the bar count",
                txt.contains("barCount: " + barCount));
    }

    /**
     * Edge case: a bar count of {@code 1} should still work and the ATR value
     * must equal the raw true‑range at each index (because a 1‑period moving
     * average returns the original series).
     */
    @Test
    public void testBarCountOneReturnsRawTR() {
        int barCount = 1;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        TRIndicator tr = new TRIndicator(series);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num trValue = tr.getValue(i);
            assertEquals("With barCount=1 ATR must equal raw TR at index " + i,
                    trValue.doubleValue(), atrValue.doubleValue(), 1e-10);
        }
    }
}
