```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#lowest(int)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_lowest_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by the tests. */
    private static BarSeries series;

    /** Close‑price indicator based on {@link #series}. */
    private static ClosePriceIndicator closePrice;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // A simple series with positive, zero and negative values.
        series = new MockBarSeries(
                numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );

        closePrice = new ClosePriceIndicator(series);
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
        // Release static references (optional, helps GC in long test suites).
        series = null;
        closePrice = null;
        numFunction = null;
    }

    @Test
    void lowestCreatesCorrectDelegateAndValues() {
        // GIVEN a NumericIndicator wrapping the close‑price indicator
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // WHEN we ask for the lowest value over a window of 3 bars
        int barCount = 3;
        NumericIndicator lowest = numeric.lowest(barCount);

        // THEN the underlying delegate must be a LowestValueIndicator
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator,
                "Delegate should be an instance of LowestValueIndicator");

        // AND the computed values must match those of a direct LowestValueIndicator
        LowestValueIndicator expected = new LowestValueIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected.getValue(i), lowest.getValue(i),
                    "Mismatch at index " + i);
        }
    }

    @Test
    void lowestWithBarCountGreaterThanSeriesLength() {
        // GIVEN a NumericIndicator
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // WHEN the barCount exceeds the series length
        int barCount = series.getBarCount() + 5;
        NumericIndicator lowest = numeric.lowest(barCount);

        // THEN the delegate is still a LowestValueIndicator
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);

        // AND the value at the last index must be the minimum of the whole series
        Num min = closePrice.getValue(0);
        for (int i = 1; i < series.getBarCount(); i++) {
            min = min.min(closePrice.getValue(i));
        }
        assertNumEquals(min, lowest.getValue(series.getBarCount() - 1));
    }
}
```