###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>All public methods (basic behaviour)</li>
 *   <li>Edge cases such as illegal constructor arguments and out‑of‑range indexes</li>
 *   <li>Exception handling verification</li>
 * </ul>
 *
 * <p>Only classes, constructors and methods that appear in the supplied source
 * code or in the allowed dependency signatures are used – no additional
 * third‑party mocking frameworks are employed.</p>
 */
public class ATRIndicatorTest {

    private static final int BAR_COUNT = 3;          // typical time‑frame for ATR
    private BarSeries series;                       // series used for all tests
    private TRIndicator trIndicator;                // true‑range indicator
    private ATRIndicator atrIndicator;              // indicator under test

    /**
     * Creates a {@link BarSeries} containing a deterministic set of bars.
     * The data are deliberately simple so that the true‑range values are easy
     * to verify manually if needed.
     */
    private BarSeries createSeries() {
        List<Bar> bars = new ArrayList<>();

        // (open, high, low, close, volume, time)
        bars.add(new BaseBar(ZonedDateTime.now().minusDays(5),
                DecimalNum.valueOf(10), DecimalNum.valueOf(12), DecimalNum.valueOf(9), DecimalNum.valueOf(10),
                DecimalNum.valueOf(0)));
        bars.add(new BaseBar(ZonedDateTime.now().minusDays(4),
                DecimalNum.valueOf(10), DecimalNum.valueOf(13), DecimalNum.valueOf(9), DecimalNum.valueOf(11),
                DecimalNum.valueOf(0)));
        bars.add(new BaseBar(ZonedDateTime.now().minusDays(3),
                DecimalNum.valueOf(11), DecimalNum.valueOf(14), DecimalNum.valueOf(10), DecimalNum.valueOf(12),
                DecimalNum.valueOf(0)));
        bars.add(new BaseBar(ZonedDateTime.now().minusDays(2),
                DecimalNum.valueOf(12), DecimalNum.valueOf(15), DecimalNum.valueOf(11), DecimalNum.valueOf(13),
                DecimalNum.valueOf(0)));
        bars.add(new BaseBar(ZonedDateTime.now().minusDays(1),
                DecimalNum.valueOf(13), DecimalNum.valueOf(16), DecimalNum.valueOf(12), DecimalNum.valueOf(14),
                DecimalNum.valueOf(0)));

        BaseBarSeries s = new BaseBarSeries("test-series", bars);
        return s;
    }

    @Before
    public void setUp() {
        series = createSeries();
        trIndicator = new TRIndicator(series);
        atrIndicator = new ATRIndicator(trIndicator, BAR_COUNT);
    }

    /* ---------------------------------------------------------------------- */
    /*  Basic public‑method tests                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetValueMatchesMMAIndicator() {
        // The ATRIndicator internally delegates to an MMAIndicator.
        // We create an independent MMAIndicator with the same parameters
        // and compare the values for every index of the series.
        MMAIndicator expectedMMA = new MMAIndicator(trIndicator, BAR_COUNT);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atrIndicator.getValue(i);
            Num mmaValue = expectedMMA.getValue(i);
            assertEquals("ATR value should match MMA value at index " + i,
                    mmaValue, atrValue);
        }
    }

    @Test
    public void testGetBarCount() {
        assertEquals("getBarCount must return the barCount supplied to the constructor",
                BAR_COUNT, atrIndicator.getBarCount());
    }

    @Test
    public void testGetTRIndicator() {
        assertSame("getTRIndicator must return the TRIndicator instance used at construction",
                trIndicator, atrIndicator.getTRIndicator());
    }

    @Test
    public void testGetCountOfUnstableBars() {
        // By design ATRIndicator reports its own barCount as the number of
        // unstable bars (see the implementation of getCountOfUnstableBars()).
        assertEquals("Unstable bar count should equal the time‑frame",
                BAR_COUNT, atrIndicator.getCountOfUnstableBars());
    }

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        String s = atrIndicator.toString();
        assertTrue("toString should contain class name", s.contains("ATRIndicator"));
        assertTrue("toString should contain barCount", s.contains(String.valueOf(BAR_COUNT)));
    }

    /* ---------------------------------------------------------------------- */
    /*  Edge‑case and exception handling tests                               */
    /* ---------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroBarCountThrows() {
        // MMAIndicator (the underlying component) validates that barCount > 0
        // and throws IllegalArgumentException when this contract is broken.
        new ATRIndicator(trIndicator, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeBarCountThrows() {
        new ATRIndicator(trIndicator, -5);
    }

    @Test
    public void testGetValueWithNegativeIndexThrows() {
        try {
            atrIndicator.getValue(-1);
            fail("Expected IndexOutOfBoundsException for negative index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetValueWithIndexBeyondSeriesThrows() {
        int outOfBounds = series.getBarCount(); // valid indexes are 0 .. size‑1
        try {
            atrIndicator.getValue(outOfBounds);
            fail("Expected IndexOutOfBoundsException for index >= series size");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testAtrWithBarCountLargerThanSeries() {
        // When barCount > series length the MMAIndicator still works (it just
        // averages whatever values are available). This test ensures no
        // unexpected exception is thrown and that a value is produced.
        int largeBarCount = series.getBarCount() + 10;
        ATRIndicator largeAtr = new ATRIndicator(trIndicator, largeBarCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num v = largeAtr.getValue(i);
            assertNotNull("ATR value should be non‑null even with large barCount", v);
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Constructor overload test (BarSeries + int)                           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testConstructorWithBarSeries() {
        ATRIndicator viaSeries = new ATRIndicator(series, BAR_COUNT);
        assertEquals("ATR constructed via BarSeries must have same barCount",
                BAR_COUNT, viaSeries.getBarCount());
        // Verify delegation works the same way as the TR‑based constructor
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(viaSeries.getValue(i), atrIndicator.getValue(i));
        }
    }
}
```
###Test END##