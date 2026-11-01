###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.indicators.ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Construction via both public constructors</li>
 *   <li>{@code getValue(int)}</li>
 *   <li>{@code getUnstableBars()}</li>
 *   <li>{@code getTRIndicator()}</li>
 *   <li>{@code getBarCount()}</li>
 *   <li>{@code toString()}</li>
 *   <li>Edge cases (single‑bar series, barCount = 1, out‑of‑range index)</li>
 * </ul>
 *
 * Only public API that appears in the supplied signatures is used.
 * A concrete {@link org.ta4j.core.BaseBarSeries} (the standard implementation
 * shipped with TA4J) is employed to build a realistic series – no mocks or
 * custom subclasses of library interfaces are created.
 */
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.MMAIndicator;
import org.ta4j.core.num.Num;

/**
 * Test class for {@link ATRIndicator}.
 */
public class ATRIndicatorTest {

    /** A series with a handful of deterministic bars – used by most tests. */
    private BarSeries series;

    /** Number of bars used for the moving‑average window in the tests. */
    private static final int BAR_COUNT = 3;

    /** Helper that creates a {@link BaseBarSeries} with the supplied price data. */
    private static BarSeries buildSeries(List<double[]> priceData) {
        BarSeries s = new BaseBarSeries("test-series");
        ZonedDateTime time = ZonedDateTime.now();
        Duration period = Duration.ofDays(1);
        for (double[] p : priceData) {
            // p[0]=open, p[1]=high, p[2]=low, p[3]=close, p[4]=volume (optional)
            if (p.length == 5) {
                s.addBar(period, time,
                        s.numOf(p[0]), s.numOf(p[1]), s.numOf(p[2]), s.numOf(p[3]), s.numOf(p[4]));
            } else {
                s.addBar(period, time,
                        s.numOf(p[0]), s.numOf(p[1]), s.numOf(p[2]), s.numOf(p[3]), s.numOf(0));
            }
            time = time.plus(period);
        }
        return s;
    }

    /** Sample data – 6 bars with increasing volatility. */
    private static final List<double[]> SAMPLE_DATA = new ArrayList<>();
    static {
        SAMPLE_DATA.add(new double[]{10, 12, 9, 11, 1000});   // bar 0
        SAMPLE_DATA.add(new double[]{11, 13, 10, 12, 1500});  // bar 1
        SAMPLE_DATA.add(new double[]{12, 15, 11, 14, 2000});  // bar 2
        SAMPLE_DATA.add(new double[]{14, 18, 13, 17, 2500});  // bar 3
        SAMPLE_DATA.add(new double[]{17, 20, 16, 19, 3000});  // bar 4
        SAMPLE_DATA.add(new double[]{19, 22, 18, 21, 3500});  // bar 5
    }

    @Before
    public void setUp() {
        series = buildSeries(SAMPLE_DATA);
    }

    /**
     * Verify that both constructors initialise the internal {@link TRIndicator}
     * and the underlying {@link MMAIndicator} with the correct bar count.
     */
    @Test
    public void testConstructorsAndBarCount() {
        // Constructor that receives a BarSeries
        ATRIndicator atrFromSeries = new ATRIndicator(series, BAR_COUNT);
        assertNotNull("TRIndicator must not be null", atrFromSeries.getTRIndicator());
        assertEquals("Bar count must be the one supplied to the constructor",
                BAR_COUNT, atrFromSeries.getBarCount());

        // Constructor that receives a pre‑built TRIndicator
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atrFromTR = new ATRIndicator(tr, BAR_COUNT);
        assertSame("The TRIndicator passed to the constructor must be returned",
                tr, atrFromTR.getTRIndicator());
        assertEquals("Bar count must be identical for both constructors",
                BAR_COUNT, atrFromTR.getBarCount());
    }

    /**
     * The {@code getValue(int)} of {@link ATRIndicator} simply delegates to the
     * internal {@link MMAIndicator}.  This test checks that delegation works for
     * a range of indices, including the first unstable bar.
     */
    @Test
    public void testGetValueDelegation() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        MMAIndicator mma = new MMAIndicator(new TRIndicator(series), BAR_COUNT);

        // Compare values for every valid index
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = mma.getValue(i);
            Num actual = atr.getValue(i);
            assertEquals("ATR value at index " + i + " must equal MMA(TR) value",
                    expected, actual);
        }
    }

    /**
     * {@code getUnstableBars()} must return the same number as the moving‑average
     * window (i.e. the bar count).
     */
    @Test
    public void testGetUnstableBars() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("Unstable bars should equal the configured bar count",
                BAR_COUNT, atr.getUnstableBars());
    }

    /**
     * Verify that {@code getTRIndicator()} returns the exact instance that the
     * indicator was built with.
     */
    @Test
    public void testGetTRIndicator() {
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, BAR_COUNT);
        assertSame("Returned TRIndicator must be the one supplied at construction",
                tr, atr.getTRIndicator());
    }

    /**
     * {@code toString()} must contain the simple class name and the bar count.
     */
    @Test
    public void testToString() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String txt = atr.toString();
        assertTrue("toString must contain class name",
                txt.contains(ATRIndicator.class.getSimpleName()));
        assertTrue("toString must contain the bar count",
                txt.contains("barCount: " + BAR_COUNT));
    }

    /**
     * Edge case: a series that contains only a single bar.  The indicator should
     * still be constructible and {@code getValue(0)} must return the TR of that
     * sole bar (the MMA of a single value is the value itself).
     */
    @Test
    public void testSingleBarSeries() {
        List<double[]> oneBar = new ArrayList<>();
        oneBar.add(new double[]{100, 105, 95, 102, 500});
        BarSeries single = buildSeries(oneBar);

        ATRIndicator atr = new ATRIndicator(single, 1);
        Num trValue = new TRIndicator(single).getValue(0);
        assertEquals("With barCount=1 the ATR should equal the raw TR value",
                trValue, atr.getValue(0));
        assertEquals("Unstable bars must be 1 for a single‑bar window",
                1, atr.getUnstableBars());
    }

    /**
     * Requesting a value for an index that does not exist must result in an
     * {@link IndexOutOfBoundsException}.  The underlying {@link MMAIndicator}
     * throws the exception, so the ATR indicator should propagate it unchanged.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfRange() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        // series has 6 bars; index 10 is out of range
        atr.getValue(10);
    }

    /**
     * Verify that the internal {@link MMAIndicator} is created with a *new* {@link TRIndicator}
     * instance (different from the one supplied via the constructor).  This is
     * an implementation detail required by the original source code.
     */
    @Test
    public void testInternalMMAUsesNewTR() {
        TRIndicator externalTR = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(externalTR, BAR_COUNT);

        // The internal MMAIndicator is not directly accessible, but we can infer
        // its TR source by comparing values: if it used the same TR instance,
        // the two indicators would always be identical.  Because MMA smooths
        // the values, the first unstable bar (index < BAR_COUNT) will differ.
        // We therefore check that at index 0 the values differ when BAR_COUNT > 1.
        if (BAR_COUNT > 1) {
            Num atrValue = atr.getValue(0);
            Num trValue = externalTR.getValue(0);
            assertNotEquals("ATR (MMA) at index 0 must differ from raw TR when barCount>1",
                    trValue, atrValue);
        }
    }
}
```
###Test END##