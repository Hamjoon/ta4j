package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.MMAIndicator;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Public constructors</li>
 *   <li>Public accessor methods</li>
 *   <li>Basic functional correctness (ATR = MMA of TR)</li>
 *   <li>Edge cases such as empty series, barCount larger than series size,
 *       negative index and invalid barCount.</li>
 * </ul>
 */
public class ATRIndicatorTest {

    private BarSeries series;
    private static final Duration ONE_DAY = Duration.ofDays(1);

    /**
     * Helper to create a simple series with deterministic price data.
     * The series will contain {@code size} bars where each bar has:
     *   open = i, high = i+2, low = i-1, close = i+1
     */
    private BarSeries createSeries(int size) {
        BarSeries s = new BaseBarSeries("test-series");
        for (int i = 0; i < size; i++) {
            ZonedDateTime endTime = ZonedDateTime.now().plusDays(i);
            Num open = s.numOf(i);
            Num high = s.numOf(i + 2);
            Num low = s.numOf(i - 1);
            Num close = s.numOf(i + 1);
            s.addBar(endTime, open, high, low, close, s.numOf(0)); // volume = 0
        }
        return s;
    }

    @Before
    public void setUp() {
        // a series with 10 bars – enough for most tests
        series = createSeries(10);
    }

    /* ---------------------------------------------------------------------- */
    /*  List of public methods (extracted from the source)                    */
    /* ---------------------------------------------------------------------- */
    // public ATRIndicator(BarSeries series, int barCount)
    // public ATRIndicator(TRIndicator tr, int barCount)
    // public Num getValue(int index)
    // public int getUnstableBars()
    // public TRIndicator getTRIndicator()
    // public int getBarCount()
    // public String toString()

    /* ---------------------------------------------------------------------- */
    /*  Basic constructor tests                                               */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testConstructor_withBarSeries_andBarCount() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertNotNull("TRIndicator should be created", atr.getTRIndicator());
        assertEquals("Bar count should be stored correctly", barCount, atr.getBarCount());
        // unstable bars are defined as the barCount in this indicator
        assertEquals(barCount, atr.getUnstableBars());
    }

    @Test
    public void testConstructor_withTRIndicator_andBarCount() {
        int barCount = 4;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        assertSame("TRIndicator supplied in ctor must be returned", tr, atr.getTRIndicator());
        assertEquals(barCount, atr.getBarCount());
    }

    /* ---------------------------------------------------------------------- */
    /*  Functional correctness tests                                          */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetValue_equalsMMAofTR() {
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        TRIndicator tr = new TRIndicator(series);
        MMAIndicator mma = new MMAIndicator(tr, barCount);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = mma.getValue(i);
            Num actual = atr.getValue(i);
            assertEquals("ATR value must equal MMA(TR) at index " + i, expected, actual);
        }
    }

    @Test
    public void testBarCountOne_ATREqualsTR() {
        // With barCount = 1 the moving average reduces to the raw value
        int barCount = 1;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        TRIndicator tr = new TRIndicator(series);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("When barCount=1 ATR should equal TR", tr.getValue(i), atr.getValue(i));
        }
    }

    @Test
    public void testBarCountLargerThanSeriesSize() {
        // barCount larger than series length must not throw and must return a value
        int barCount = series.getBarCount() + 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        // simply verify that getValue works for the last index
        Num value = atr.getValue(series.getBarCount() - 1);
        assertNotNull("ATR value must be calculable even when barCount > series size", value);
    }

    /* ---------------------------------------------------------------------- */
    /*  Edge‑case and exception tests                                         */
    /* ---------------------------------------------------------------------- */

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_negativeIndex_throwsException() {
        ATRIndicator atr = new ATRIndicator(series, 3);
        atr.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_indexBeyondSeries_throwsException() {
        ATRIndicator atr = new ATRIndicator(series, 3);
        atr.getValue(series.getBarCount()); // equal to size -> out of bounds
    }

    @Test
    public void testToString_containsClassNameAndBarCount() {
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        String str = atr.toString();
        assertTrue("toString should contain class name", str.contains("ATRIndicator"));
        assertTrue("toString should contain barCount", str.contains("barCount: " + barCount));
    }

    @Test
    public void testEmptySeries_behaviour() {
        BarSeries empty = new BaseBarSeries("empty");
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(empty, barCount);
        // No bars – any call to getValue should throw IndexOutOfBoundsException
        try {
            atr.getValue(0);
            fail("Expected IndexOutOfBoundsException for empty series");
        } catch (IndexOutOfBoundsException ignored) {
        }
        // but getters must still work
        assertEquals(barCount, atr.getBarCount());
        assertNotNull(atr.getTRIndicator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_zeroBarCount_throwsException() {
        // Assuming the library does not accept a zero period for moving averages
        new ATRIndicator(series, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_negativeBarCount_throwsException() {
        new ATRIndicator(series, -5);
    }

    /* ---------------------------------------------------------------------- */
    /*  getUnstableBars should delegate to barCount                          */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetUnstableBars_equalsBarCount() {
        int barCount = 7;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals("Unstable bars must equal the barCount", barCount, atr.getUnstableBars());
    }
}
