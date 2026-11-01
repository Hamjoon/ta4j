```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.function.Function;

/**
 * Additional tests for {@link NumericIndicator#previous()} and
 * {@link NumericIndicator#previous(int)} covering equality, edge‑cases,
 * argument validation and null‑delegate handling.
 *
 * The original test class already contains basic shift checks; the tests below
 * complement them to satisfy the full JSON test‑case specification.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_previous_2_Test {

    /** Series used by the default tests – a deterministic series of numbers. */
    private static BarSeries series;

    /** Close price indicator based on the series (used as delegate). */
    private static ClosePriceIndicator closePriceIndicator;

    /** Instance of the class under test. */
    private static NumericIndicator numericIndicator;

    /** Function converting a {@link Number} to {@link Num}. */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    @BeforeAll
    static void setupBeforeAll() {
        // Build a mock series with a known set of values.
        series = new MockBarSeries(
                NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );

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

    /* --------------------------------------------------------------------- */
    /* Existing tests (kept unchanged)                                      */
    /* --------------------------------------------------------------------- */

    @Test
    void previousDefaultShouldReturnIndicatorShiftedByOne() {
        Indicator<Num> previous = numericIndicator.previous();

        assertEquals(closePriceIndicator.getValue(0), previous.getValue(1),
                "previous().getValue(1) must equal original value at index 0");

        assertEquals(closePriceIndicator.getValue(5), previous.getValue(6),
                "previous().getValue(6) must equal original value at index 5");
    }

    @Test
    void previousWithBarCountShouldShiftAccordingly() {
        Indicator<Num> previous3 = numericIndicator.previous(3);

        assertEquals(closePriceIndicator.getValue(3), previous3.getValue(6),
                "previous(3).getValue(6) must equal original value at index 3");

        assertEquals(closePriceIndicator.getValue(0), previous3.getValue(3),
                "previous(3).getValue(3) must equal original value at index 0");
    }

    @Test
    void previousShouldThrowWhenIndexIsOutOfBounds() {
        Indicator<Num> previous = numericIndicator.previous();

        Num outOfRange = previous.getValue(0);
        assertTrue(outOfRange instanceof NaN,
                "previous().getValue(0) should be NaN because there is no value at index -1");
    }

    /* --------------------------------------------------------------------- */
    /* New tests derived from the JSON specification                         */
    /* --------------------------------------------------------------------- */

    /**
     * Verify that {@code previous()} is merely a convenience wrapper for
     * {@code previous(1)} – the two indicators must produce identical values
     * for all valid indices.
     */
    @Test
    void previousIsConvenienceWrapperForPreviousOne() {
        Indicator<Num> byDefault = numericIndicator.previous();
        Indicator<Num> byOne = numericIndicator.previous(1);

        // Compare a handful of indices (including the edge where the result is NaN)
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(byOne.getValue(i), byDefault.getValue(i),
                    () -> "previous() and previous(1) differ at index " + i);
        }
    }

    /**
     * Ensure that invoking {@code previous()} does not mutate the original
     * delegate indicator.
     */
    @Test
    void previousDoesNotMutateDelegate() {
        // Capture a few original values before calling previous()
        Num v3Before = closePriceIndicator.getValue(3);
        Num v7Before = closePriceIndicator.getValue(7);

        // Call previous() – we do not keep the result because we only care about side‑effects
        numericIndicator.previous();

        // Values must stay the same after the call
        assertEquals(v3Before, closePriceIndicator.getValue(3),
                "Delegate value at index 3 changed after previous()");
        assertEquals(v7Before, closePriceIndicator.getValue(7),
                "Delegate value at index 7 changed after previous()");
    }

    /**
     * Two successive calls to {@code previous()} should return distinct indicator
     * objects (no caching unless the implementation explicitly does so).
     */
    @Test
    void successivePreviousCallsReturnDistinctInstances() {
        Indicator<Num> first = numericIndicator.previous();
        Indicator<Num> second = numericIndicator.previous();

        assertNotSame(first, second, "Two calls to previous() returned the same instance");
    }

    /**
     * {@code previous(0)} must behave exactly like the original delegate:
     * no shift, no NaN values.
     */
    @Test
    void previousZeroIsIdentity() {
        Indicator<Num> identity = numericIndicator.previous(0);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePriceIndicator.getValue(i), identity.getValue(i),
                    () -> "previous(0) differs from delegate at index " + i);
        }
    }

    /**
     * Negative barCount arguments must be rejected with an
     * {@link IllegalArgumentException}.
     */
    @ParameterizedTest
    @ValueSource(ints = { -5, -1 })
    void previousNegativeBarCountThrows(int barCount) {
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.previous(barCount),
                "previous(" + barCount + ") should throw IllegalArgumentException");
    }

    /**
     * Creating a {@link NumericIndicator} with a {@code null} delegate and
     * invoking {@code previous()} must result in a {@link NullPointerException}.
     */
    @Test
    void previousWithNullDelegateThrowsNPE() {
        NumericIndicator nullNumeric = NumericIndicator.of(null);
        assertThrows(NullPointerException.class,
                nullNumeric::previous,
                "Calling previous() on a NumericIndicator with null delegate should throw NPE");
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
            assertEquals(DecimalNum.valueOf(i - 3), shifted.getValue(i),
                    "previous(3) should shift index " + i);
        }

        // i < 3 -> NaN
        for (int i = 0; i < 3; i++) {
            assertTrue(shifted.getValue(i) instanceof NaN,
                    "previous(3) should return NaN for index " + i);
        }
    }
}
```