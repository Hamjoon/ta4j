```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseBar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * Tests for {@link NumericIndicator#previous()} and related overloads.
 */
class NumericIndicator_previous_2_Test {

    /**
     * Series used by all tests – a simple deterministic series of numbers.
     */
    private static BarSeries series;

    /**
     * Close price indicator based on the series (used as delegate).
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * Instance of the class under test.
     */
    private static NumericIndicator numericIndicator;

    /**
     * Function converting a {@link Number} to {@link Num}.
     */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    @BeforeAll
    static void setupBeforeAll() {
        // Build a deterministic series with the required values.
        series = new BaseBarSeriesBuilder()
                .withNumFunction(NUM_FUNCTION)
                .build();

        // Helper to add a bar with the same open/high/low/close value.
        for (double v : new double[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8,
                7, 6, 5, 4, 3, 2, 1, 0, -1, -2}) {
            series.addBar(new BaseBar(
                    Duration.ofSeconds(1),
                    ZonedDateTime.now(),
                    DecimalNum.valueOf(v), // open
                    DecimalNum.valueOf(v), // high
                    DecimalNum.valueOf(v), // low
                    DecimalNum.valueOf(v), // close
                    DecimalNum.valueOf(0)  // volume
            ));
        }

        closePriceIndicator = new ClosePriceIndicator(series);
        // Create the NumericIndicator wrapper that we will test.
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterAll
    static void teardownAfterAll() {
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
    void previousDefaultShouldReturnIndicatorShiftedByOne() {
        // The default previous() is defined as previous(1)
        Indicator<Num> previous = numericIndicator.previous();
        // The value at index i of the returned indicator should equal the original value at i‑1.
        Assertions.assertEquals(closePriceIndicator.getValue(0), previous.getValue(1));
        Assertions.assertEquals(closePriceIndicator.getValue(5), previous.getValue(6));
    }

    @Test
    void previousWithBarCountShouldShiftAccordingly() {
        // Ask for a shift of 3 bars.
        Indicator<Num> previous3 = numericIndicator.previous(3);
        // Verify that the shift is exactly three positions.
        Assertions.assertEquals(closePriceIndicator.getValue(3), previous3.getValue(6));
        Assertions.assertEquals(closePriceIndicator.getValue(0), previous3.getValue(3));
    }

    @Test
    void previousShouldThrowWhenIndexIsOutOfBounds() {
        Indicator<Num> previous = numericIndicator.previous();
        // The library returns NaN for out‑of‑range indices.
        Num outOfRange = previous.getValue(0);
        Assertions.assertTrue(outOfRange instanceof NaN,
                "previous().getValue(0) should be NaN because there is no value at index -1");
    }

    /**
     * Verify that {@code previous()} is merely a convenience wrapper for
     * {@code previous(1)} – the two indicators must produce identical values
     * for all valid indices.
     */
    @Test
    void previousIsConvenienceWrapperForPreviousOne() {
        Indicator<Num> byDefault = numericIndicator.previous();
        Indicator<Num> byOne = numericIndicator.previous(1);
        for (int i = 0; i < series.getBarCount(); i++) {
            Assertions.assertEquals(byOne.getValue(i), byDefault.getValue(i));
        }
    }

    /**
     * Ensure that invoking {@code previous()} does not mutate the original
     * delegate indicator.
     */
    @Test
    void previousDoesNotMutateDelegate() {
        Num v3Before = closePriceIndicator.getValue(3);
        Num v7Before = closePriceIndicator.getValue(7);
        // side‑effect free call
        numericIndicator.previous();
        Assertions.assertEquals(v3Before, closePriceIndicator.getValue(3));
        Assertions.assertEquals(v7Before, closePriceIndicator.getValue(7));
    }

    /**
     * Two successive calls to {@code previous()} should return distinct indicator
     * objects (no caching unless the implementation explicitly does so).
     */
    @Test
    void successivePreviousCallsReturnDistinctInstances() {
        Indicator<Num> first = numericIndicator.previous();
        Indicator<Num> second = numericIndicator.previous();
        Assertions.assertNotSame(first, second);
    }

    /**
     * {@code previous(0)} must behave exactly like the original delegate:
     * no shift, no NaN values.
     */
    @Test
    void previousZeroIsIdentity() {
        Indicator<Num> identity = numericIndicator.previous(0);
        for (int i = 0; i < series.getBarCount(); i++) {
            Assertions.assertEquals(closePriceIndicator.getValue(i), identity.getValue(i));
        }
    }

    /**
     * Negative barCount arguments must be rejected with an
     * {@link IllegalArgumentException}.
     */
    @ParameterizedTest
    @ValueSource(ints = { -5, -1 })
    void previousNegativeBarCountThrows(int barCount) {
        try {
            numericIndicator.previous(barCount);
            Assertions.fail("previous(" + barCount + ") should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Creating a {@link NumericIndicator} with a {@code null} delegate and
     * invoking {@code previous()} must result in a {@link NullPointerException}.
     */
    @Test
    void previousWithNullDelegateThrowsNPE() {
        NumericIndicator nullNumeric = NumericIndicator.of(null);
        try {
            nullNumeric.previous();
            Assertions.fail("Calling previous() on a NumericIndicator with null delegate should throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    /**
     * Using the simple stub, verify the shift semantics of {@code previous(3)}:
     * for i >= 3 the value equals i‑3, otherwise NaN.
     */
    @Test
    void previousShiftWithStubIndicator() {
        NumericIndicator stubNumeric = NumericIndicator.of(indexIndicator());
        Indicator<Num> shifted = stubNumeric.previous(3);
        // i >= 3 -> shifted value equals i-3
        for (int i = 3; i < 10; i++) {
            Assertions.assertEquals(DecimalNum.valueOf(i - 3), shifted.getValue(i));
        }
        // i < 3 -> NaN
        for (int i = 0; i < 3; i++) {
            Assertions.assertTrue(shifted.getValue(i) instanceof NaN);
        }
    }

    @Test
    public void previousShouldReturnNaNWhenIndexIsOutOfBounds() {
        Indicator<Num> previous = numericIndicator.previous();
        // Index 0 would need a value from -1, therefore the implementation should return NaN.
        Num outOfRange = previous.getValue(0);
        Assertions.assertTrue(outOfRange instanceof NaN);
    }
}
```