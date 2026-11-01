###Test START##
```java
package org.ta4j.core.indicators;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.averages.MMAIndicator;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>Both public constructors</li>
 *   <li>Delegation of {@code getValue(int)}</li>
 *   <li>Unstable‑bars count</li>
 *   <li>Accessors {@code getTRIndicator()} and {@code getBarCount()}</li>
 *   <li>String representation</li>
 * </ul>
 *
 * The test data consist of a small {@link BarSeries} built with deterministic
 * price values so that the underlying {@link TRIndicator} and {@link MMAIndicator}
 * can be exercised without any external dependencies.
 */
public class ATRIndicatorTest {

    private static final int BAR_COUNT = 3;          // time‑frame for the ATR
    private BarSeries series;                        // the series used in all tests
    private TRIndicator trIndicator;                 // shared TR indicator
    private MMAIndicator expectedMMA;                // expected MMA (used for value comparison)

    /**
     * Creates a {@link BarSeries} with a handful of bars.  The series is built
     * using the library's concrete {@code BaseBarSeries} implementation (the
     * standard implementation provided by ta4j).  Each bar is created via the
     * series' {@code BarBuilder} so that all internal consistency checks are
     * honoured.
     */
    @Before
    public void setUp() {
        // ---------------------------------------------------------------------
        // 1️⃣  Build a concrete BarSeries (BaseBarSeries is the default concrete
        //     implementation in ta4j).  The builder pattern is used to keep the
        //     code concise and readable.
        // ---------------------------------------------------------------------
        series = new org.ta4j.core.BaseBarSeries("ATR‑test‑series");

        // Helper to create Num instances
        final org.ta4j.core.num.NumFactory nf = series.numFactory();

        // ---------------------------------------------------------------------
        // 2️⃣  Add a series of bars.  The values are chosen so that the true
        //     range (TR) is easy to calculate manually, but we do **not** hard‑code
        //     the expected ATR values – we let the library compute them and
        //     compare the result of ATRIndicator with a stand‑alone MMAIndicator.
        // ---------------------------------------------------------------------
        series.addBar(series.barBuilder()
                .openPrice(nf.numOf(10))
                .highPrice(nf.numOf(12))
                .lowPrice(nf.numOf(9))
                .closePrice(nf.numOf(11))
                .volume(nf.numOf(100))
                .build());

        series.addBar(series.barBuilder()
                .openPrice(nf.numOf(11))
                .highPrice(nf.numOf(13))
                .lowPrice(nf.numOf(10))
                .closePrice(nf.numOf(12))
                .volume(nf.numOf(150))
                .build());

        series.addBar(series.barBuilder()
                .openPrice(nf.numOf(12))
                .highPrice(nf.numOf(14))
                .lowPrice(nf.numOf(11))
                .closePrice(nf.numOf(13))
                .volume(nf.numOf(200))
                .build());

        series.addBar(series.barBuilder()
                .openPrice(nf.numOf(13))
                .highPrice(nf.numOf(15))
                .lowPrice(nf.numOf(12))
                .closePrice(nf.numOf(14))
                .volume(nf.numOf(250))
                .build());

        series.addBar(series.barBuilder()
                .openPrice(nf.numOf(14))
                .highPrice(nf.numOf(16))
                .lowPrice(nf.numOf(13))
                .closePrice(nf.numOf(15))
                .volume(nf.numOf(300))
                .build());

        // ---------------------------------------------------------------------
        // 3️⃣  Initialise the shared helpers used by several test cases.
        // ---------------------------------------------------------------------
        trIndicator = new TRIndicator(series);
        expectedMMA = new MMAIndicator(trIndicator, BAR_COUNT);
    }

    /**
     * Test the constructor that receives a {@link BarSeries} and a bar count.
     * The created {@link ATRIndicator} must delegate its {@code getValue}
     * computation to an internal {@link MMAIndicator}.
     */
    @Test
    public void testConstructorWithSeries() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);

        // Verify that the internal TR indicator is the one we expect.
        assertSame("TRIndicator should be the one created inside the ATRIndicator",
                atr.getTRIndicator().getBarSeries(), series);

        // Verify that the bar count accessor returns the correct value.
        assertEquals("Bar count must match the one supplied to the constructor",
                BAR_COUNT, atr.getBarCount());

        // Verify delegation of getValue().
        for (int i = 0; i < series.getBarCount(); i++) {
            Num atrValue = atr.getValue(i);
            Num mmaValue = expectedMMA.getValue(i);
            assertTrue("ATR value at index " + i + " must equal MMA value",
                    atrValue.isEqual(mmaValue));
        }
    }

    /**
     * Test the constructor that receives a pre‑built {@link TRIndicator}.
     * This mirrors the previous test but uses the alternative public constructor.
     */
    @Test
    public void testConstructorWithTRIndicator() {
        ATRIndicator atr = new ATRIndicator(trIndicator, BAR_COUNT);

        // The TRIndicator injected must be exactly the one we passed.
        assertSame("Injected TRIndicator must be returned by getTRIndicator()",
                trIndicator, atr.getTRIndicator());

        // Bar count must be forwarded correctly.
        assertEquals(BAR_COUNT, atr.getBarCount());

        // Delegation test – compare with a freshly built MMAIndicator.
        MMAIndicator directMMA = new MMAIndicator(trIndicator, BAR_COUNT);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertTrue("ATR value must equal MMA value for index " + i,
                    atr.getValue(i).isEqual(directMMA.getValue(i)));
        }
    }

    /**
     * The {@code getCountOfUnstableBars()} method must return the same value
     * as {@link #getBarCount()} because the ATR uses a moving‑average that
     * becomes stable only after the full period has been processed.
     */
    @Test
    public void testGetCountOfUnstableBars() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("Unstable bar count must equal the configured bar count",
                atr.getBarCount(), atr.getCountOfUnstableBars());
    }

    /**
     * Verify that {@code getTRIndicator()} returns the exact TRIndicator that
     * the ATRIndicator was built with.
     */
    @Test
    public void testGetTRIndicator() {
        ATRIndicator atr = new ATRIndicator(trIndicator, BAR_COUNT);
        assertSame("getTRIndicator() must return the original TRIndicator",
                trIndicator, atr.getTRIndicator());
    }

    /**
     * The {@code getBarCount()} accessor must simply forward the value used to
     * construct the internal {@link MMAIndicator}.
     */
    @Test
    public void testGetBarCount() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        assertEquals("getBarCount() must return the barCount supplied to the constructor",
                BAR_COUNT, atr.getBarCount());
    }

    /**
     * The {@code toString()} implementation should contain the simple class
     * name and the configured bar count.
     */
    @Test
    public void testToString() {
        ATRIndicator atr = new ATRIndicator(series, BAR_COUNT);
        String expected = "ATRIndicator barCount: " + BAR_COUNT;
        assertEquals("toString() format is not as expected", expected, atr.toString());
    }

    /**
     * Additional sanity check: ensure that the underlying {@link TRIndicator}
     * produces the expected true‑range values for our deterministic data set.
     * This is not a direct test of ATRIndicator but guarantees that the shared
     * dependency works as intended, which in turn validates the delegation
     * logic of the ATRIndicator tests.
     */
    @Test
    public void testTRIndicatorValues() {
        // Manually compute the first true range (index 0) – it is simply
        // high - low because there is no previous close.
        Num firstTR = trIndicator.getValue(0);
        Num expectedFirstTR = series.getBar(0).getHighPrice()
                .minus(series.getBar(0).getLowPrice());
        assertTrue("TR at index 0 must be high - low", firstTR.isEqual(expectedFirstTR));

        // For index 1 the true range is max{high‑low, |high‑prevClose|, |low‑prevClose|}
        Bar prevBar = series.getBar(0);
        Bar curBar = series.getBar(1);
        Num highLow = curBar.getHighPrice().minus(curBar.getLowPrice());
        Num highPrevClose = curBar.getHighPrice().minus(prevBar.getClosePrice()).abs();
        Num lowPrevClose = curBar.getLowPrice().minus(prevBar.getClosePrice()).abs();
        Num expectedTR1 = highLow.max(highPrevClose).max(lowPrevClose);
        assertTrue("TR at index 1 must match manual calculation",
                trIndicator.getValue(1).isEqual(expectedTR1));
    }
}
```
###Test END##