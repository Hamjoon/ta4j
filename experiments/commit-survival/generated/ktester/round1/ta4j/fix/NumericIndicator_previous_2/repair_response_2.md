```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseBar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * Unit tests for {@link NumericIndicator#previous()} and {@link NumericIndicator#previous(int)}.
 *
 * The test class has been rewritten to use JUnit 4 (which is available in the
 * compilation environment) and to avoid APIs that are not present in the
 * version of TA‑4J used for the kata (e.g. {@code BaseBarSeriesBuilder.withNumFunction}
 * and the {@code DecimalNum} constructors of {@code BaseBar}).
 */
public class NumericIndicator_previous_2_Test {

    /** Series used by all tests – a simple deterministic series of numbers. */
    private static BarSeries series;

    /** Close price indicator based on the series (used as delegate). */
    private static ClosePriceIndicator closePriceIndicator;

    /** Instance of the class under test. */
    private static NumericIndicator numericIndicator;

    /** Function converting a {@link Number} to {@link Num}. */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    @BeforeClass
    public static void setupBeforeAll() {
        // Build a deterministic series with the required values.
        // The current TA‑4J version does not expose a withNumFunction() method,
        // so we rely on the default Num implementation (DecimalNum) and add bars
        // using primitive double values.
        series = new BaseBarSeriesBuilder().withName("test-series").build();

        // Helper to add a bar with the same open/high/low/close/volume value.
        for (double v : new double[] {
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 }) {
            series.addBar(new BaseBar(
                    Duration.ofSeconds(1),
                    ZonedDateTime.now(),
                    v, // open
                    v, // high
                    v, // low
                    v, // close
                    v, // volume
                    0  // amount (not used in the tests)
            ));
        }

        closePriceIndicator = new ClosePriceIndicator(series);
        // Create the NumericIndicator wrapper that we will test.
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterClass
    public static void teardownAfterAll() {
        numericIndicator = null;
        closePriceIndicator = null;
        series = null;
    }

    /**
     * Helper stub indicator that simply returns its index as a {@link Num}.
     * Used for a few tests that need a lightweight, deterministic delegate.
     */
    private static Indicator<Num> indexIndicator() {
        return new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return DecimalNum.valueOf(index);
            }

            @Override
            public BarSeries getBarSeries() {
                // Not needed for the tests that use this stub
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "IndexIndicator";
            }
        };
    }

    @Test
    public void previousDefaultShouldReturnIndicatorShiftedByOne() {
        // The default previous() is defined as previous(1)
        Indicator<Num> previous = numericIndicator.previous();
        // The value at index i of the returned indicator should equal the original value at i‑1.
        Assert.assertEquals(closePriceIndicator.getValue(0), previous.getValue(1));
        Assert.assertEquals(closePriceIndicator.getValue(5), previous.getValue(6));
    }

    @Test
    public void previousWithBarCountShouldShiftAccordingly() {
        // Ask for a shift of 3 bars.
        Indicator<Num> previous3 = numericIndicator.previous(3);
        // Verify that the shift is exactly three positions.
        Assert.assertEquals(closePriceIndicator.getValue(3), previous3.getValue(6));
        Assert.assertEquals(closePriceIndicator.getValue(0), previous3.getValue(3));
    }

    @Test
    public void previousShouldReturnNaNWhenIndexIsOutOfBounds() {
        Indicator<Num> previous = numericIndicator.previous();
        // Index 0 would need a value from -1, therefore the implementation should return NaN.
        Num outOfRange = previous.getValue(0);
        Assert.assertTrue(outOfRange instanceof NaN);
    }

    @Test
    public void previousIsConvenienceWrapperForPreviousOne() {
        Indicator<Num> byDefault = numericIndicator.previous();
        Indicator<Num> byOne = numericIndicator.previous(1);
        for (int i = 0; i < series.getBarCount(); i++) {
            Assert.assertEquals(byOne.getValue(i), byDefault.getValue(i));
        }
    }

    @Test
    public void previousDoesNotMutateDelegate() {
        Num v3Before = closePriceIndicator.getValue(3);
        Num v7Before = closePriceIndicator.getValue(7);
        // side‑effect free call
        numericIndicator.previous();
        Assert.assertEquals(v3Before, closePriceIndicator.getValue(3));
        Assert.assertEquals(v7Before, closePriceIndicator.getValue(7));
    }

    @Test
    public void successivePreviousCallsReturnDistinctInstances() {
        Indicator<Num> first = numericIndicator.previous();
        Indicator<Num> second = numericIndicator.previous();
        Assert.assertNotSame(first, second);
    }

    @Test
    public void previousZeroIsIdentity() {
        Indicator<Num> identity = numericIndicator.previous(0);
        for (int i = 0; i < series.getBarCount(); i++) {
            Assert.assertEquals(closePriceIndicator.getValue(i), identity.getValue(i));
        }
    }

    @Test
    public void previousNegativeBarCountThrows() {
        int[] badValues = { -5, -1 };
        for (int barCount : badValues) {
            try {
                numericIndicator.previous(barCount);
                Assert.fail("previous(" + barCount + ") should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // expected
            }
        }
    }

    @Test
    public void previousWithNullDelegateThrowsNPE() {
        NumericIndicator nullNumeric = NumericIndicator.of(null);
        try {
            nullNumeric.previous();
            Assert.fail("Calling previous() on a NumericIndicator with null delegate should throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void previousShiftWithStubIndicator() {
        NumericIndicator stubNumeric = NumericIndicator.of(indexIndicator());
        Indicator<Num> shifted = stubNumeric.previous(3);
        // i >= 3 -> shifted value equals i-3
        for (int i = 3; i < 10; i++) {
            Assert.assertEquals(DecimalNum.valueOf(i - 3), shifted.getValue(i));
        }
        // i < 3 -> NaN
        for (int i = 0; i < 3; i++) {
            Assert.assertTrue(shifted.getValue(i) instanceof NaN);
        }
    }
}
```