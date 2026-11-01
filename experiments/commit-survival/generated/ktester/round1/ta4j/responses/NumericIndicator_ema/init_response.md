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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#ema(int)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_ema_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /** Simple series with deterministic values. */
    private static BarSeries series;

    /** Close‑price indicator based on {@code series}. */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator wrapping the close‑price indicator. */
    private static NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // a short, deterministic series – the exact values are not important,
        // they just need to be stable for the EMA calculation.
        series = new MockBarSeries(
                NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2
        );
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test state needed at the moment.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup needed.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (helps the GC in large test suites).
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
    }

    @Test
    void emaReturnsNumericIndicatorWithEmaDelegate() {
        final int barCount = 3;

        // invoke the method under test
        NumericIndicator emaNumeric = numericIndicator.ema(barCount);

        // the returned object must not be null
        assertNotNull(emaNumeric, "ema() should return a non‑null NumericIndicator");

        // its delegate must be an EMAIndicator
        Indicator<Num> delegate = emaNumeric.delegate();
        assertNotNull(delegate, "Delegate of the returned NumericIndicator must not be null");
        assertEquals(EMAIndicator.class, delegate.getClass(),
                "Delegate should be an instance of EMAIndicator");

        // verify that the EMA values are the same as a directly created EMAIndicator
        EMAIndicator expectedEma = new EMAIndicator(closePriceIndicator, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expectedEma.getValue(i), emaNumeric.getValue(i),
                    "EMA value at index " + i + " differs from expected");
        }
    }

    @Test
    void emaWorksWithDifferentUnderlyingIndicators() {
        // Use a different underlying indicator (e.g., HighestValueIndicator)
        Indicator<Num> highest = new HighestValueIndicator(closePriceIndicator, 5);
        NumericIndicator highestNumeric = NumericIndicator.of(highest);

        final int barCount = 4;
        NumericIndicator emaFromHighest = highestNumeric.ema(barCount);

        // delegate type must still be EMAIndicator
        assertEquals(EMAIndicator.class, emaFromHighest.delegate().getClass(),
                "Delegate should be EMAIndicator even when the underlying indicator is not a ClosePriceIndicator");

        // compare values with a manually created EMAIndicator
        EMAIndicator expected = new EMAIndicator(highest, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected.getValue(i), emaFromHighest.getValue(i),
                    "EMA value from highest indicator differs at index " + i);
        }
    }
}
```