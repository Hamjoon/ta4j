```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#plus(Indicator)}.
 *
 * The suite covers:
 * <ul>
 *   <li>Adding a constant indicator.</li>
 *   <li>Adding the indicator to itself.</li>
 *   <li>Passing {@code null} as the operand.</li>
 *   <li>Adding a {@link PreviousValueIndicator}.</li>
 *   <li>Propagation of exceptions thrown by the operand indicator.</li>
 * </ul>
 *
 * The original test (plusWithIndicator_shouldReturnSumOfValues) that uses an
 * {@link EMAIndicator} is retained unchanged.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_plus_Test {

    /** Function used by the mock series to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Shared series used by all tests. */
    private static BarSeries series;

    /** Simple close‑price indicator based on the series. */
    private static ClosePriceIndicator closePriceIndicator;

    /** EMA indicator used as the dynamic operand for {@code plus}. */
    private static EMAIndicator emaIndicator;

    /** The {@link NumericIndicator} under test (by default wraps close‑price). */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in Ta4j.
        numFunction = DecimalNum::valueOf;

        // Deterministic mock series – values are not used by the constant‑based tests.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);

        closePriceIndicator = new ClosePriceIndicator(series);
        emaIndicator = new EMAIndicator(closePriceIndicator, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // By default we work with a NumericIndicator that decorates the close‑price.
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test resources to release.
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
        emaIndicator = null;
        numFunction = null;
    }

    /* --------------------------------------------------------------------- */
    /* Existing test – kept for completeness                                  */
    /* --------------------------------------------------------------------- */
    @Test
    void plusWithIndicator_shouldReturnSumOfValues() {
        // Act: create a new NumericIndicator that adds the EMA indicator
        NumericIndicator result = numericIndicator.plus(emaIndicator);

        // Verify a few representative indexes (including the first, a middle, and the last)
        for (int index : new int[] {0, 5, 8, 15, series.getBarCount() - 1}) {
            Num expected = closePriceIndicator.getValue(index).plus(emaIndicator.getValue(index));
            Num actual = result.getValue(index);
            assertEquals(expected, actual,
                    () -> "Sum at index " + index + " is not as expected");
        }
    }

    /* --------------------------------------------------------------------- */
    /* New tests derived from the JSON specification                         */
    /* --------------------------------------------------------------------- */

    @Test
    void plusWithConstantIndicator_returnsConstantSum() {
        // Arrange: a NumericIndicator that always yields 3
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);

        // Operand: constant 5
        ConstantIndicator<Num> constFive = new ConstantIndicator<>(DecimalNum.valueOf(5));

        // Act
        NumericIndicator result = base.plus(constFive);

        // Assert: every index must return 8 (3 + 5)
        for (int i = 0; i < 10; i++) { // a few indexes are enough
            assertEquals(DecimalNum.valueOf(8), result.getValue(i),
                    () -> "Expected sum 8 at index " + i);
        }
    }

    @Test
    void plusWithSelf_returnsDoubleValues() {
        // Arrange: a NumericIndicator that always yields 3
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);

        // Act: add the indicator to itself
        NumericIndicator result = base.plus(base);

        // Assert: each index must return 6 (3 + 3)
        for (int i = 0; i < 10; i++) {
            assertEquals(DecimalNum.valueOf(6), result.getValue(i),
                    () -> "Expected doubled value 6 at index " + i);
        }
    }

    @Test
    void plusWithNull_throwsNullPointerException() {
        // Arrange: a simple NumericIndicator (any will do)
        NumericIndicator base = NumericIndicator.of(closePriceIndicator);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> base.plus(null),
                "Calling plus(null) should throw NullPointerException");
    }

    @Test
    void plusWithPreviousValueIndicator_returnsCorrectSum() {
        // Arrange: base indicator always 3
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);

        // Operand: PreviousValueIndicator wrapping a constant 2 (always returns 2)
        ConstantIndicator<Num> constTwo = new ConstantIndicator<>(DecimalNum.valueOf(2));
        PreviousValueIndicator<Num> prevTwo = new PreviousValueIndicator<>(constTwo);

        // Act
        NumericIndicator result = base.plus(prevTwo);

        // Assert: first two indexes should be 5 (3 + 2). Subsequent indexes are also 5
        // because PreviousValueIndicator returns the previous value of its wrapped indicator,
        // which is constantly 2.
        for (int i = 0; i < 5; i++) {
            assertEquals(DecimalNum.valueOf(5), result.getValue(i),
                    () -> "Expected sum 5 at index " + i);
        }
    }

    @Test
    void plusWithFaultyIndicator_propagatesException() {
        // Arrange: base indicator (constant 3) – value does not matter for the exception test
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);

        // Operand: custom indicator that throws IndexOutOfBoundsException for negative indexes
        Indicator<Num> faulty = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                if (index < 0) {
                    throw new IndexOutOfBoundsException("Negative index: " + index);
                }
                return DecimalNum.valueOf(0);
            }

            @Override
            public BarSeries getBarSeries() {
                return series;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "FaultyIndicator";
            }
        };

        // Act: create summed indicator
        NumericIndicator result = base.plus(faulty);

        // Assert: calling getValue with a negative index must propagate the same exception
        assertThrows(IndexOutOfBoundsException.class,
                () -> result.getValue(-1),
                "Exception from the operand indicator should be propagated");
    }
}
```