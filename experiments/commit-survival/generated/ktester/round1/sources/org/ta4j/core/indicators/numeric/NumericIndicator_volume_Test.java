package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Bar;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

class NumericIndicator_volume_Test {

    /**
     * Simple Num factory used by {@link MockBarSeries}.
     */
    private static final Function<Number, Num> NUM_FACTORY = DecimalNum::valueOf;

    /**
     * Series used for all test cases.
     */
    private BarSeries series;

    /**
     * A {@link BarSeries} that pretends to have a negative bar count.
     * Used to verify that {@link VolumeIndicator} validates the bar count and
     * throws an {@link IllegalArgumentException}.
     */
    private static class NegativeBarCountSeries extends BaseBarSeries {

        NegativeBarCountSeries(Function<Number, Num> factory) {
            // BaseBarSeries has a constructor that only needs the name.
            super("negative");
        }

        @Override
        public int getBarCount() {
            return -1;
        }
    }

    //     @BeforeEach
    void setupBeforeEach() {
        // Create a deterministic series (close prices are also used as volume in the mock).
        // The actual numeric values are irrelevant – we only need a consistent series.
        //         series = new MockBarSeries(NUM_FACTORY, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
    }

    /**
     * Creates a {@link BarSeries} whose bars have the given numeric values as
     * close price and volume.  All other price fields are set to the same value
     * for simplicity.
     */
    private static BarSeries createSeries(Function<Number, Num> factory, int... values) {
        BaseBarSeries s = new BaseBarSeries("test");
        ZonedDateTime time = ZonedDateTime.now();
        for (int v : values) {
            Num n = factory.apply(v);
            // All price fields (open, high, low, close) and volume/amount are set to the same value.
            Bar bar = new BaseBar(Duration.ofMinutes(1), time, // open
            n.doubleValue(), // high
            n.doubleValue(), // low
            n.doubleValue(), // close
            n.doubleValue(), // volume
            n.doubleValue(), // amount (not relevant for the tests)
            n.doubleValue());
            s.addBar(bar);
            time = time.plusMinutes(1);
        }
        return s;
    }

    @Test
    void volumeCreatesNumericIndicatorWithVolumeDelegate() {
        // When
        NumericIndicator numericVolume = NumericIndicator.volume(series);
        // Then
        assertNotNull("NumericIndicator.volume(...) must not return null", numericVolume);
        assertNotNull("Delegate of the created NumericIndicator must not be null", numericVolume.delegate());
        assertTrue("Delegate should be an instance of VolumeIndicator", numericVolume.delegate() instanceof VolumeIndicator);
        // Compare values with a directly created VolumeIndicator
        VolumeIndicator directVolume = new VolumeIndicator(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = directVolume.getValue(i);
            Num actual = numericVolume.getValue(i);
            assertEquals("NumericIndicator volume value at index " + i + " must match VolumeIndicator", expected, actual);
        }
    }

    @Test
    void volumeWithEmptySeriesCreatesDelegateButAccessThrows() {
        BarSeries emptySeries = createSeries(NUM_FACTORY);
        NumericIndicator numericVolume = NumericIndicator.volume(emptySeries);
        assertNotNull("NumericIndicator must not be null", numericVolume);
        assertTrue("Delegate should still be a VolumeIndicator", numericVolume.delegate() instanceof VolumeIndicator);
        // Accessing any index must throw IndexOutOfBoundsException (behaviour of VolumeIndicator)
        try {
            numericVolume.getValue(0);
            fail("Calling getValue on an empty series must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    void volumeWithNullSeriesThrowsNullPointerException() {
        try {
            NumericIndicator.volume(null);
            fail("Calling volume(null) must throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    void volumeWithSingleBarSeriesForwardsValueAndSeriesReference() {
        BarSeries singleBarSeries = createSeries(NUM_FACTORY, 42);
        NumericIndicator numericVolume = NumericIndicator.volume(singleBarSeries);
        assertNotNull("NumericIndicator must not be null", numericVolume);
        assertEquals("NumericIndicator must preserve the original BarSeries reference", singleBarSeries, numericVolume.getBarSeries());
        assertEquals("The single volume value must be correctly forwarded", DecimalNum.valueOf(42), numericVolume.getValue(0));
    }

    @Test
    void ofWithNullDelegateThrowsNullPointerException() {
        try {
            NumericIndicator.of(null);
            fail("Calling of(null) must throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Before
    public void setUp() {
        // deterministic series: close price = volume = 10,20,...,100
        series = createSeries(NUM_FACTORY, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
    }
}
