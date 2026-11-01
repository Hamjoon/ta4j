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
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#plus(Indicator)}.
 *
 * The test creates a {@link NumericIndicator} based on a {@link ClosePriceIndicator}
 * and verifies that the {@code plus} operation with another {@link Indicator<Num>}
 * (an {@link EMAIndicator} in this case) yields the correct summed values.
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

    /** The {@link NumericIndicator} under test. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Create a Num factory that uses DecimalNum (the default implementation in Ta4j)
        numFunction = DecimalNum::valueOf;

        // Build a mock series with a deterministic set of values
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);

        // Initialise the helper indicators
        closePriceIndicator = new ClosePriceIndicator(series);
        emaIndicator = new EMAIndicator(closePriceIndicator, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // The NumericIndicator that decorates the close‑price indicator
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test resources to release
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nullify static fields for GC friendliness
        series = null;
        closePriceIndicator = null;
        emaIndicator = null;
        numFunction = null;
    }

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
}
```