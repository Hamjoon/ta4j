package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.TRIndicator;

/**
 * Comprehensive JUnit‑4 test suite for {@link ATRIndicator}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Construction via both public constructors.</li>
 *   <li>Correct calculation of the number of unstable bars.</li>
 *   <li>Behaviour of {@link #getValue(int)} for stable and unstable indices
 *       (including the {@code NaN} contract).</li>
 *   <li>Returned {@link TRIndicator} instance.</li>
 *   <li>Correct exposure of the configured {@code barCount}.</li>
 *   <li>String representation.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The test data consist of a small {@link BaseBarSeries} with deterministic
 * price values so that the true‑range (TR) and consequently the average true
 * range (ATR) can be reasoned about manually.
 * </p>
 */
public class ATRIndicatorTest {

    /** Helper to create a {@link BaseBarSeries} with deterministic bars. */
    private BarSeries buildSeries() {
        BarSeries series = new BaseBarSeries("test-series");

        // 5 bars – the values are chosen so that the true‑range is easy to compute.
        //   open   high   low   close   volume
        // 0 10.0   12.0   9.0   11.0    100
        // 1 11.0   13.0   10.0  12.0    110
        // 2 12.0   14.0   11.0  13.0    120
        // 3 13.0   15.0   12.0  14.0    130
        // 4 14.0   16.0   13.0  15.0    140
        //
        // The true‑range for each index i (i > 0) is:
        //   max(high_i - low_i,
        //       |high_i - close_{i‑1}|,
        //       |low_i  - close_{i‑1}|)
        // For the data above all three terms are equal to (high_i - low_i)
        // = 2.0, therefore TR = 2.0 for every index ≥ 1.
        // Index 0 has a TR of 0 (by definition of the library).

        List<Num> opens  = List.of(num(10), num(11), num(12), num(13), num(14));
        List<Num> highs  = List.of(num(12), num(13), num(14), num(15), num(16));
        List<Num> lows   = List.of(num(9),  num(10), num(11), num(12), num(13));
        List<Num> closes = List.of(num(11), num(12), num(13), num(14), num(15));
        List<Num> volumes= List.of(num(100),num(110),num(120),num(130),num(140));

        ZonedDateTime time = ZonedDateTime.now();

        for (int i = 0; i < opens.size(); i++) {
            BaseBar bar = BaseBar.builder()
                    .timePeriod(Duration.ofDays(1))
                    .endTime(time.plusDays(i))
                    .openPrice(opens.get(i))
                    .highPrice(highs.get(i))
                    .lowPrice(lows.get(i))
                    .closePrice(closes.get(i))
                    .volume(volumes.get(i))
                    .build();
            series.addBar(bar);
        }
        return series;
    }

    /** Shortcut to obtain a {@link DecimalNum} from a primitive double. */
    private static Num num(double value) {
        return DecimalNum.valueOf(value);
    }

    @Test
    public void testConstructionWithBarSeries() {
        BarSeries series = buildSeries();
        int barCount = 3;

        ATRIndicator atr = new ATRIndicator(series, barCount);

        // The internal TR indicator must be built from the same series
        assertNotNull(atr.getTRIndicator());
        assertSame(series, atr.getTRIndicator().getBarSeries());

        // Bar count getter must reflect the constructor argument
        assertEquals(barCount, atr.getBarCount());

        // toString must contain the class name and the bar count
        String txt = atr.toString();
        assertTrue(txt.contains("ATRIndicator"));
        assertTrue(txt.contains(String.valueOf(barCount)));
    }

    @Test
    public void testConstructionWithTRIndicator() {
        BarSeries series = buildSeries();
        TRIndicator tr = new TRIndicator(series);
        int barCount = 2;

        ATRIndicator atr = new ATRIndicator(tr, barCount);

        // The returned TRIndicator from the ATR must be a *new* instance
        // (the contract of getTRIndicator()), but based on the same series.
        TRIndicator inner = atr.getTRIndicator();
        assertNotNull(inner);
        assertNotSame(tr, inner); // a fresh instance
        assertSame(series, inner.getBarSeries());
    }

    @Test
    public void testCountOfUnstableBars() {
        BarSeries series = buildSeries();
        int barCount = 2;

        ATRIndicator atr = new ATRIndicator(series, barCount);

        // TRIndicator's own unstable bars are defined by the library.
        // For a freshly built TRIndicator it is 1 (the first bar has no previous close).
        int trUnstable = new TRIndicator(series).getCountOfUnstableBars();

        int expectedUnstable = trUnstable + barCount;
        assertEquals(expectedUnstable, atr.getCountOfUnstableBars());
    }

    @Test
    public void testGetValue_UnstableIndicesReturnNaN() {
        BarSeries series = buildSeries();
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        int unstable = atr.getCountOfUnstableBars();

        // All indices strictly lower than the unstable count must return NaN
        for (int i = 0; i < unstable; i++) {
            Num value = atr.getValue(i);
            assertTrue("Index " + i + " should be NaN", value.isNaN());
        }
    }

    @Test
    public void testGetValue_StableIndicesReturnNumericValue() {
        BarSeries series = buildSeries();
        int barCount = 2;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        int unstable = atr.getCountOfUnstableBars();
        int lastIndex = series.getBarCount() - 1;

        // For our deterministic data the true‑range (TR) from index 1 onward is 2.0.
        // The MMA (modified moving average) with period = barCount (2) will therefore
        // converge to 2.0 after the first stable value.
        // We simply verify that the returned value is a finite number and equals 2.0
        // for the last index (which is well beyond the warm‑up period).

        Num lastValue = atr.getValue(lastIndex);
        assertFalse("Last value must not be NaN", lastValue.isNaN());
        assertTrue("Last value must be finite", !lastValue.isNaN() && !lastValue.isInfinite());

        // Expected ATR after the warm‑up period is 2.0 (the constant true‑range)
        Num expected = num(2.0);
        // Allow a tiny tolerance because the MMA uses the underlying Num implementation
        assertEquals(0, expected.compareTo(lastValue));
    }

    @Test
    public void testGetTRIndicatorCreatesFreshInstance() {
        BarSeries series = buildSeries();
        ATRIndicator atr = new ATRIndicator(series, 1);

        TRIndicator tr1 = atr.getTRIndicator();
        TRIndicator tr2 = atr.getTRIndicator();

        // Each call must produce a new object (the method documentation says “a true range indicator for this indicator's bar series”)
        assertNotSame(tr1, tr2);
        assertSame(series, tr1.getBarSeries());
        assertSame(series, tr2.getBarSeries());
    }

    @Test
    public void testToStringFormat() {
        BarSeries series = buildSeries();
        int barCount = 4;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        String s = atr.toString();
        assertEquals("ATRIndicator barCount: 4", s);
    }
}
