```java
package org.ta4j.core.indicators.numeric;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.mocks.MockBarSeries;          // <-- keep if the class exists in the project; otherwise remove
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#volume(BarSeries)}.
 */
public class NumericIndicator_volume_Test {

    /** Simple Num factory used by {@link MockBarSeries} (if available). */
    private static final Function<Number, Num> NUM_FACTORY = DecimalNum::valueOf;

    /** Series used for most test cases. */
    private BarSeries series;

    /** Helper to create a deterministic series where each bar has the same value for
     * open, high, low, close and volume. */
    private static BarSeries createSeries(Function<Number, Num> factory, int... values) {
        BaseBarSeries s = new BaseBarSeries("test", factory);
        ZonedDateTime time = ZonedDateTime.now();
        for (int v : values) {
            Num n = factory.apply(v);
            // a bar with identical open/high/low/close/volume values
            Bar bar = new BaseBar(Duration.ofMinutes(1), time, n, n, n, n, n);
            s.addBar(bar);
            time = time.plusMinutes(1);
        }
        return s;
    }

    @Before
    public void setUp() {
        // deterministic series: close price = volume = 10,20,...,100
        series = createSeries(NUM_FACTORY, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
    }

    @Test
    public void volumeCreatesNumericIndicatorWithVolumeDelegate() {
        // When
        NumericIndicator numericVolume = NumericIndicator.volume(series);
        // Then
        assertNotNull("NumericIndicator.volume(...) must not return null", numericVolume);
        assertNotNull("Delegate of the created NumericIndicator must not be null", numericVolume.delegate());
        assertTrue("Delegate should be an instance of VolumeIndicator",
                numericVolume.delegate() instanceof VolumeIndicator);
        // Compare values with a directly created VolumeIndicator
        VolumeIndicator directVolume = new VolumeIndicator(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = directVolume.getValue(i);
            Num actual = numericVolume.getValue(i);
            assertEquals("NumericIndicator volume value at index " + i + " must match VolumeIndicator",
                    expected, actual);
        }
    }

    @Test
    public void volumeWithEmptySeriesCreatesDelegateButAccessThrows() {
        // no values
        BarSeries emptySeries = createSeries(NUM_FACTORY);
        NumericIndicator numericVolume = NumericIndicator.volume(emptySeries);
        assertNotNull("NumericIndicator must not be null", numericVolume);
        assertTrue("Delegate should still be a VolumeIndicator",
                numericVolume.delegate() instanceof VolumeIndicator);
        // Accessing any index must throw IndexOutOfBoundsException (behaviour of VolumeIndicator)
        try {
            numericVolume.getValue(0);
            fail("Calling getValue on an empty series must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void volumeWithNullSeriesThrowsNullPointerException() {
        try {
            NumericIndicator.volume(null);
            fail("Calling volume(null) must throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void volumeWithSingleBarSeriesForwardsValueAndSeriesReference() {
        BarSeries singleBarSeries = createSeries(NUM_FACTORY, 42);
        NumericIndicator numericVolume = NumericIndicator.volume(singleBarSeries);
        assertNotNull("NumericIndicator must not be null", numericVolume);
        assertEquals("NumericIndicator must preserve the original BarSeries reference",
                singleBarSeries, numericVolume.getBarSeries());
        assertEquals("The single volume value must be correctly forwarded",
                DecimalNum.valueOf(42), numericVolume.getValue(0));
    }
}
```