```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private static final int BAR_COUNT = 4;               // window size for the test
    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a deterministic series (the same values used in other tests)
        Function<Number, Num> numFunction = Num::valueOf; // default Num implementation
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    @Test
    void highest_returnsCorrectIndicatorAndValues() {
        // Act: create the highest‑value indicator via the fluent API
        NumericIndicator highest = numericIndicator.highest(BAR_COUNT);

        // The underlying delegate must be a HighestValueIndicator
        Indicator<Num> delegate = highest.delegate();
        assertTrue(delegate instanceof HighestValueIndicator,
                "Delegate should be an instance of HighestValueIndicator");

        // Verify the computed values against a manual highest‑over‑window calculation
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = computeHighest(i);
            Num actual = highest.getValue(i);
            assertNumEquals(expected, actual,
                    "Highest value mismatch at index " + i);
        }
    }

    /**
     * Helper that manually computes the highest value of {@code closePrice}
     * over the last {@code BAR_COUNT} bars ending at {@code index}.
     */
    private Num computeHighest(int index) {
        int start = Math.max(0, index - BAR_COUNT + 1);
        Num max = closePrice.getValue(start);
        for (int j = start + 1; j <= index; j++) {
            Num current = closePrice.getValue(j);
            if (current.isGreaterThan(max)) {
                max = current;
            }
        }
        return max;
    }
}
```