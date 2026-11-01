```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional unit tests for {@link NumericIndicator#min(Number)}.
 *
 * <p>These tests complement the basic checks already present in the original
 * test class. They cover:
 * <ul>
 *   <li>Basic functionality with a simple positive series.</li>
 *   <li>Mixed positive/negative series.</li>
 *   <li>Exception handling for null, NaN and infinite values.</li>
 * </ul>
 *
 * <p>The {@code @BeforeEach} method from the original framework is reused to
 * initialise a default series (1‑20). For the specific test groups we create
 * dedicated series inside the test methods to keep the scenarios isolated.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_min_Additional_Test {

    /** Function used by the mock series to create {@link Num} instances. */
    private Function<Number, Num> numFunction;

    /** Series used for the generic tests (simple price series). */
    private BarSeries series;

    /** Close price indicator that will be wrapped by {@link NumericIndicator}. */
    private ClosePriceIndicator closePriceIndicator;

    /** The {@link NumericIndicator} instance under test. */
    private NumericIndicator numericIndicator;

    @BeforeEach
    void setupBeforeEach() {
        // Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // A short series with deterministic values – not used by all tests,
        // but required to initialise the fields.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);

        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    /* -------------------------------------------------------------
     *  BASIC FUNCTIONALITY – constant compared to a positive series
     * ------------------------------------------------------------- */

    /**
     * Parameterized test that verifies {@link NumericIndicator#min(Number)} for
     * a delegate series containing only positive numbers (10, 12, 15).
     *
     * @param constant the constant supplied to {@code min}
     * @param expected0 expected value at index 0
     * @param expected1 expected value at index 1
     * @param expected2 expected value at index 2
     */
    @ParameterizedTest(name = "[{index}] constant={0}")
    @CsvSource({
            // constant smaller than every delegate value → constant wins everywhere
            "5, 5, 5, 5",
            // constant larger than every delegate value → delegate wins everywhere
            "20, 10, 12, 15",
            // constant equal to delegate value at index 1
            "12, 10, 12, 12",
            // negative constant – always smaller than positive delegate values
            "-3, -3, -3, -3",
            // zero constant – smaller than all positive delegate values
            "0, 0, 0, 0"
    })
    void minBasicFunctionality_NumberConstant(double constant,
                                               double expected0,
                                               double expected1,
                                               double expected2) {
        // ---- arrange: create a series with values 10, 12, 15 ----
        BarSeries posSeries = new MockBarSeries(numFunction, 10, 12, 15);
        ClosePriceIndicator posIndicator = new ClosePriceIndicator(posSeries);
        NumericIndicator indicator = NumericIndicator.of(posIndicator);

        // ---- act ----
        NumericIndicator minIndicator = indicator.min(constant);

        // ---- assert ----
        assertEquals(DecimalNum.valueOf(expected0), minIndicator.getValue(0),
                "Incorrect min at index 0");
        assertEquals(DecimalNum.valueOf(expected1), minIndicator.getValue(1),
                "Incorrect min at index 1");
        assertEquals(DecimalNum.valueOf(expected2), minIndicator.getValue(2),
                "Incorrect min at index 2");
    }

    /* -------------------------------------------------------------
     *  MIXED DELEGATE VALUES – negative and positive numbers
     * ------------------------------------------------------------- */

    /**
     * Parameterized test for a delegate series containing negative and positive
     * numbers ({@code -5, -2, 3}). The constant is varied to verify the element‑wise
     * minimum logic.
     *
     * @param constant the constant supplied to {@code min}
     * @param expected0 expected value at index 0
     * @param expected1 expected value at index 1
     * @param expected2 expected value at index 2
     */
    @ParameterizedTest(name = "[{index}] constant={0}")
    @CsvSource({
            // constant lies between min and max → mix of delegate and constant
            "13, -5, -2, 3",
            // constant higher than the most negative value but lower than positive
            "-2, -5, -2, -2",
            // zero constant – selected only when it is smaller than delegate
            "0, -5, -2, 0"
    })
    void minMixedDelegateValues_NumberConstant(double constant,
                                               double expected0,
                                               double expected1,
                                               double expected2) {
        // ---- arrange: series with -5, -2, 3 ----
        BarSeries mixedSeries = new MockBarSeries(numFunction, -5, -2, 3);
        ClosePriceIndicator mixedIndicator = new ClosePriceIndicator(mixedSeries);
        NumericIndicator indicator = NumericIndicator.of(mixedIndicator);

        // ---- act ----
        NumericIndicator minIndicator = indicator.min(constant);

        // ---- assert ----
        assertEquals(DecimalNum.valueOf(expected0), minIndicator.getValue(0),
                "Incorrect min at index 0");
        assertEquals(DecimalNum.valueOf(expected1), minIndicator.getValue(1),
                "Incorrect min at index 1");
        assertEquals(DecimalNum.valueOf(expected2), minIndicator.getValue(2),
                "Incorrect min at index 2");
    }

    /* -------------------------------------------------------------
     *  EXCEPTION HANDLING
     * ------------------------------------------------------------- */

    @Test
    void min_NullArgument_ThrowsException() {
        // The library is expected to reject a null argument – we accept either
        // NullPointerException or IllegalArgumentException.
        assertThrows(RuntimeException.class, () -> numericIndicator.min((Number) null),
                "Calling min(null) should throw an exception");
    }

    @Test
    void min_NaNArgument_ThrowsIllegalArgumentException() {
        // Double.NaN is not a valid constant for ConstantIndicator.
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.min(Double.NaN),
                "NaN constant should cause IllegalArgumentException");
    }

    @Test
    void min_InfiniteArgument_ThrowsIllegalArgumentException() {
        // Positive infinity is not allowed.
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.min(Double.POSITIVE_INFINITY),
                "Infinite constant should cause IllegalArgumentException");
    }
}
```