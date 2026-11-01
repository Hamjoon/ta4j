package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.averages.MMAIndicator;

/**
 * JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>public constructors and their validation logic</li>
 *   <li>all public methods (getValue, getCountOfUnstableBars, getTRIndicator,
 *       getBarCount, toString)</li>
 *   <li>edge cases such as {@code null} arguments, indices before the unstable
 *       bar count, and out‑of‑range indices</li>
 * </ul>
 */
public class ATRIndicatorTest {

    /** a small series used for most tests (5 bars) */
    private BarSeries series;
    /** true‑range indicator built from the series */
    private TRIndicator tr;
    /** bar count (time‑frame) used for the ATR */
    private static final int BAR_COUNT = 3;

    @Before
    public void setUp() {
        // Build a deterministic series – the values are not important for the
        // logic we test, only that they are valid and produce a non‑zero TR.
        series = new BaseBarSeries("test-series");
        for (int i = 0; i < 5; i++) {
            // open, high, low, close, volume – simple incremental numbers
            Num open  = series.numFactory().numOf(10 + i);
            Num high  = series.numFactory().numOf(12 + i);
            Num low   = series.numFactory().numOf(9  + i);
            Num close = series.numFactory().numOf(11 + i);
            Num vol   = series.numFactory().numOf(1000);
            Bar bar = new BaseBar(
                    ZonedDateTime.now().plusDays(i),
                    open, high, low, close,
                    vol);
            series.addBar(bar);
        }
        tr = new TRIndicator(series);
    }

    /** ------------------------------------------------------------
     *  1. Constructor validation
     *  ------------------------------------------------------------ */
    @Test(expected = NullPointerException.class)
    public void constructor_nullTRIndicator_throwsNPE() {
        // The second constructor must reject a null TRIndicator
        new ATRIndicator((TRIndicator) null, BAR_COUNT);
    }

    @Test
    public void constructor_seriesAndBarCount_createsIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertNotNull(atr);
        assertEquals(BAR_COUNT, atr.getBarCount());
    }

    /** ------------------------------------------------------------
     *  2. getBarCount()
     *  ------------------------------------------------------------ */
    @Test
    public void getBarCount_returnsConstructorValue() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals(BAR_COUNT, atr.getBarCount());
    }

    /** ------------------------------------------------------------
     *  3. getTRIndicator()
     *  ------------------------------------------------------------ */
    @Test
    public void getTRIndicator_returnsIndicatorWithSameSeries() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        TRIndicator trFromAtr = atr.getTRIndicator();

        // The returned TRIndicator must work on the same BarSeries
        assertSame(series, trFromAtr.getBarSeries());
        // And its value for a valid index must equal the original TRIndicator’s value
        int validIdx = Math.max(0, tr.getCountOfUnstableBars());
        assertEquals(tr.getValue(validIdx), trFromAtr.getValue(validIdx));
    }

    /** ------------------------------------------------------------
     *  4. getCountOfUnstableBars()
     *  ------------------------------------------------------------ */
    @Test
    public void getCountOfUnstableBars_isSumOfTrueRangeAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        int expected = tr.getCountOfUnstableBars() + BAR_COUNT;
        assertEquals(expected, atr.getCountOfUnstableBars());
    }

    /** ------------------------------------------------------------
     *  5. getValue(int)
     *  ------------------------------------------------------------ */
    @Test
    public void getValue_beforeUnstableBars_returnsNaN() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        int unstable = atr.getCountOfUnstableBars();

        // Every index lower than the unstable count must return NaN
        for (int i = 0; i < unstable; i++) {
            assertTrue("index " + i + " should be NaN", atr.getValue(i).isNaN());
        }
    }

    @Test
    public void getValue_afterUnstableBars_matchesMMAIndicator() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        MMAIndicator mma = new MMAIndicator(tr, BAR_COUNT);

        int start = atr.getCountOfUnstableBars();               // first stable index
        int end   = series.getBarCount() - 1;                   // last available index

        for (int i = start; i <= end; i++) {
            Num atrVal = atr.getValue(i);
            Num mmaVal = mma.getValue(i);
            assertEquals("ATR and MMA values must match at index " + i, mmaVal, atrVal);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getValue_indexOutOfRange_throwsException() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // Use an index far beyond the series size
        int outOfRange = series.getBarCount() + 10;
        atr.getValue(outOfRange);
    }

    /** ------------------------------------------------------------
     *  6. toString()
     *  ------------------------------------------------------------ */
    @Test
    public void toString_containsClassNameAndBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String s = atr.toString();
        assertTrue(s.contains("ATRIndicator"));
        assertTrue(s.contains(String.valueOf(BAR_COUNT)));
    }

    /** ------------------------------------------------------------
     *  7. Edge case – barCount = 1 (minimum sensible window)
     *  ------------------------------------------------------------ */
    @Test
    public void constructor_barCountOne_behaviourIsCorrect() {
        int oneBar = 1;
        ATRIndicator atr = new ATRIndicator(series, oneBar);
        assertEquals(oneBar, atr.getBarCount());

        // With barCount = 1 the MMAIndicator behaves like a simple moving average
        // over a single value – i.e. it returns the raw true‑range.
        MMAIndicator mma = new MMAIndicator(tr, oneBar);
        int start = atr.getCountOfUnstableBars();
        for (int i = start; i < series.getBarCount(); i++) {
            assertEquals(mma.getValue(i), atr.getValue(i));
        }
    }

    /** ------------------------------------------------------------
     *  Helper methods (none needed for the current tests)
     *  ------------------------------------------------------------ */
}
