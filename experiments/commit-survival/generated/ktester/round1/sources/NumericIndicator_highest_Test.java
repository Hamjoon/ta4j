package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#highest(int)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_highest_Test {

    /** window size for the test */
    private static final int BAR_COUNT = 4;

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private NumericIndicator numericIndicator;

    /** used by the original test method */
    private static final int DEFAULT_BAR_COUNT = 4;

    @BeforeEach
    void setupBeforeEach() {
        // Build a deterministic series (the same values used in other tests)
        Function<Number, Num> numFunction = Num::valueOf;
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    /**
     * Helper that manually computes the highest value of {@code closePrice}
     * over the last {@code barCount} bars ending at {@code index}.
     */
    private Num computeHighest(int index, int barCount) {
        int start = Math.max(0, index - barCount + 1);
        Num max = closePrice.getValue(start);
        for (int j = start + 1; j <= index; j++) {
            Num current = closePrice.getValue(j);
            if (current.isGreaterThan(max)) {
                max = current;
            }
        }
        return max;
    }

    /* --------------------------------------------------------------
     *  4. Specific series [1,3,2,5,4] – verify exact values for barCount 5 & 10
     * -------------------------------------------------------------- */
    private static Stream<Arguments> shortSeriesProvider() {
        // series values, barCount, expected values array
        return Stream.of(
                Arguments.of(new int[]{1, 3, 2, 5, 4}, 5, new int[]{1, 3, 3, 5, 5}),
                Arguments.of(new int[]{1, 3, 2, 5, 4}, 10, new int[]{1, 3, 3, 5, 5})
        );
    }

    private static Number[] toObjectArray(int[] primitive) {
        Number[] arr = new Number[primitive.length];
        for (int i = 0; i < primitive.length; i++) {
            arr[i] = primitive[i];
        }
        return arr;
    }

    @Test
    void highest_returnsCorrectIndicatorAndValues() {
        // Act: create the highest‑value indicator via the fluent API
        NumericIndicator highest = numericIndicator.highest(DEFAULT_BAR_COUNT);
        // The underlying delegate must be a HighestValueIndicator
        Indicator<Num> delegate = highest.delegate();
        assertTrue(delegate instanceof HighestValueIndicator,
                "Delegate should be an instance of HighestValueIndicator");
        // Verify the computed values against a manual highest‑over‑window calculation
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = computeHighest(i, DEFAULT_BAR_COUNT);
            Num actual = highest.getValue(i);
            assertNumEquals(expected, actual,
                    "Highest value mismatch at index " + i);
        }
    }

    /* --------------------------------------------------------------
     *  1. Normal operation – positive barCount (5)
     * -------------------------------------------------------------- */
    @Test
    void highest_withPositiveBarCount_returnsNumericIndicatorWithHighestValueIndicator() {
        int barCount = 5;
        NumericIndicator highest = numericIndicator.highest(barCount);
        assertNotNull(highest, "Returned NumericIndicator must not be null");
        assertTrue(highest.delegate() instanceof HighestValueIndicator,
                "Delegate should be an instance of HighestValueIndicator");
    }

    /* --------------------------------------------------------------
     *  2. Edge cases – zero or negative barCount
     * -------------------------------------------------------------- */
    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    void highest_withNonPositiveBarCount_throwsException(int barCount) {
        // HighestValueIndicator validates the barCount and throws an IllegalArgumentException.
        // The NumericIndicator.highest method does not perform its own validation,
        // therefore the exception propagates from the constructor.
        assertThrows(RuntimeException.class,
                () -> numericIndicator.highest(barCount),
                "Expected an exception for barCount = " + barCount);
    }

    /* --------------------------------------------------------------
     *  3. barCount = 1 – the indicator should behave exactly like the source
     * -------------------------------------------------------------- */
    @Test
    void highest_barCountOne_behavesIdenticallyToSource() {
        NumericIndicator highest = numericIndicator.highest(1);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = numericIndicator.getValue(i);
            Num actual = highest.getValue(i);
            assertNumEquals(expected, actual,
                    "Value mismatch at index " + i + " for barCount = 1");
        }
    }

    @ParameterizedTest
    @MethodSource("shortSeriesProvider")
    void highest_onShortSeries_returnsExpectedValues(int[] values, int barCount, int[] expected) {
        // Build a short deterministic series
        Function<Number, Num> numFunction = Num::valueOf;
        BarSeries shortSeries = new MockBarSeries(numFunction, toObjectArray(values));
        ClosePriceIndicator cp = new ClosePriceIndicator(shortSeries);
        NumericIndicator src = NumericIndicator.of(cp);
        NumericIndicator highest = src.highest(barCount);
        for (int i = 0; i < values.length; i++) {
            Num actual = highest.getValue(i);
            Num exp = Num.valueOf(expected[i]);
            assertNumEquals(exp, actual,
                    String.format("Incorrect highest value at index %d (barCount=%d)", i, barCount));
        }
    }

    /* --------------------------------------------------------------
     *  5. Ensure the source NumericIndicator is not mutated
     * -------------------------------------------------------------- */
    @Test
    void highest_isNonMutating_sourceRemainsUnchanged() {
        // Capture a value from the source before invoking highest()
        // index 3 corresponds to value 4 in the default series
        Num before = numericIndicator.getValue(3);
        NumericIndicator highest = numericIndicator.highest(3);
        // Use the highest indicator to avoid compiler warnings about unused variable
        assertNotNull(highest);
        // Verify the source still returns the original value
        Num after = numericIndicator.getValue(3);
        assertNumEquals(before, after,
                "Source NumericIndicator should remain unchanged after highest()");
    }

    /* --------------------------------------------------------------
     *  6. Verify internal barCount via behaviour (barCount = 4)
     * -------------------------------------------------------------- */
    @Test
    void highest_delegateHasCorrectBarCount_viaBehaviour() {
        int barCount = 4;
        NumericIndicator highest = numericIndicator.highest(barCount);
        // The highest value at index 3 should be the max of the first 4 values: 1,2,3,4 => 4
        Num expected = Num.valueOf(4);
        Num actual = highest.getValue(3);
        assertNumEquals(expected, actual,
                "Delegate should use the exact barCount supplied (4)");
    }

    /* --------------------------------------------------------------
     *  7. Null delegate – expect NullPointerException
     * -------------------------------------------------------------- */
    @Test
    void highest_onNumericIndicatorWithNullDelegate_throwsNullPointerException() {
        // Create a NumericIndicator with a null internal delegate
        NumericIndicator nullDelegateIndicator = NumericIndicator.of(null);
        assertThrows(NullPointerException.class,
                () -> nullDelegateIndicator.highest(5),
                "Calling highest() on a NumericIndicator with a null delegate should throw NPE");
    }
}