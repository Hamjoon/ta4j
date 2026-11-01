```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.mocks.MockBarSeries;          // <-- not available, replaced by real series below
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#volume(BarSeries)}.
 *
 * The original test suite used JUnit 5, Mockito and a {@code MockBarSeries}
 * class that are not present in the compilation environment.  This rewritten
 * version uses only JUnit 4 and the public API of ta4j to build the required
 * series.
 */
public class NumericIndicator_volume_Test {

    /** Simple {@link Num} factory used by the test series. */
    private static final Function<Number, Num> NUM_FACTORY = DecimalNum::valueOf;

    /** Series used for most test cases (volumes are the same as the close price). */
    private BarSeries series;

    /**
     * Creates a {@link BarSeries} whose bars have the given numeric values as
     * close price and volume.  All other price fields are set to the same value
     * for simplicity.
     */
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
        assertNotNull("Delegate of the created NumericIndicator must not be null",
                numericVolume.delegate());
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
        BarSeries emptySeries = createSeries(NUM_FACTORY); // no values
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

    @Test(expected = NullPointerException.class)
    public void volumeWithNullSeriesThrowsNullPointerException() {
        NumericIndicator.volume(null);
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

    @Test(expected = NullPointerException.class)
    public void ofWithNullDelegateThrowsNullPointerException() {
        NumericIndicator.of(null);
    }

    /**
     * A {@link BarSeries} that pretends to have a negative bar count.
     * This is used to verify that {@link VolumeIndicator} validates the bar
     * count and throws an {@link IllegalArgumentException}.
     */
    private static class NegativeBarCountSeries extends BaseBarSeries {
        NegativeBarCountSeries(Function<Number, Num> factory) {
            super("negative", factory);
        }

        @Override
        public int getBarCount() {
            return -1;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void volumeWithNegativeBarCountSeriesThrowsIllegalArgumentException() {
        BarSeries badSeries = new NegativeBarCountSeries(NUM_FACTORY);
        // The constructor of VolumeIndicator validates the bar count and should throw.
        NumericIndicator.volume(badSeries);
    }
}
```