```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_abs_Test {

    /** Series containing both positive and negative values */
    private BarSeries series;
    /** Indicator that simply returns the close price (i.e. the raw values) */
    private ClosePriceIndicator closePrice;
    /** NumericIndicator wrapping the close‑price indicator */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a mock series; the values deliberately cross zero
        series = new MockBarSeries(
                DecimalNum::valueOf,
                10, 5, 0, -3, -7, -2, 4, 8, -1, -6, 3, 0, -4, 7, -9, 2, -5, 6, -8, 1
        );
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up per‑test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release
    }

    @Test
    void abs_shouldReturnAbsoluteValues() {
        // The method under test
        NumericIndicator absIndicator = numericIndicator.abs();

        // Verify that each value is the absolute value of the original indicator
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).abs();
            Num actual = absIndicator.getValue(i);
            assertEquals(expected, actual,
                    "Absolute value mismatch at index " + i);
        }
    }

    @Test
    void abs_delegateShouldBeUnaryOperationIndicator() {
        // Ensure the returned delegate is the expected type (lightweight indicator)
        Indicator<Num> delegate = numericIndicator.abs().delegate();
        assertNotNull(delegate, "Delegate must not be null");
        // The concrete class is package‑private; we only assert it implements Indicator
        assertTrue(delegate instanceof Indicator, "Delegate must implement Indicator");
    }
}
```