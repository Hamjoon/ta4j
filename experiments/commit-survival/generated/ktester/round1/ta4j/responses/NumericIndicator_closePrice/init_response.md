```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#closePrice(BarSeries)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_closePrice_Test {

    /** Series used for all tests in this class. */
    private BarSeries series;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple mock series with known close‑price values.
        // DecimalNum::valueOf converts a Number into the library's Num implementation.
        series = new MockBarSeries(DecimalNum::valueOf,
                10, 20, 30, 40, 50); // 5 bars with close prices 10‑50
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up for the mock series.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release.
    }

    @Test
    void closePriceCreatesClosePriceIndicator() {
        // when
        NumericIndicator numericIndicator = NumericIndicator.closePrice(series);

        // then
        assertNotNull(numericIndicator, "NumericIndicator should not be null");
        // the underlying delegate must be a ClosePriceIndicator
        assertEquals(ClosePriceIndicator.class, numericIndicator.delegate().getClass(),
                "Delegate should be a ClosePriceIndicator");

        // verify that the values are the same as the series' close prices
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getBar(i).getClosePrice();
            Num actual = numericIndicator.getValue(i);
            assertEquals(0, expected.compareTo(actual),
                    "Value at index " + i + " should match the series close price");
        }

        // also ensure the series reference is correctly propagated
        assertSame(series, numericIndicator.getBarSeries(),
                "BarSeries returned by NumericIndicator should be the original series");
    }
}
```