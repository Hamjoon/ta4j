```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#getValue(int)}.
 *
 * The test creates a simple {@link MockBarSeries} with deterministic numeric
 * values, wraps a {@link ClosePriceIndicator} with {@link NumericIndicator},
 * and verifies that {@code getValue(int)} simply forwards the call to the
 * underlying delegate.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_getValue_Test {

    /** Series used by all tests – created once for the whole test class. */
    private static BarSeries series;

    /** Close‑price indicator that serves as the delegate for {@link NumericIndicator}. */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator instance under test. */
    private static NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Convert a Number to the library's Num implementation (DecimalNum).
        Function<Number, Num> numFunction = DecimalNum::valueOf;

        // Simple deterministic data set (the same values as in the original test suite).
        series = new MockBarSeries(
                numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );

        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialisation required at the moment.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required at the moment.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release references (helpful for large test suites).
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
    }

    @Test
    void getValue_delegatesToUnderlyingIndicator() {
        // Verify that for every index the value returned by NumericIndicator
        // equals the value returned by its delegate (ClosePriceIndicator).
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i);
            Num actual = numericIndicator.getValue(i);
            assertEquals(expected, actual,
                    "NumericIndicator.getValue(" + i + ") should delegate to ClosePriceIndicator");
        }
    }
}
```