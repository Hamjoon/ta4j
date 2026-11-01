```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#min(Indicator)}.
 *
 * The tests verify that the resulting {@link NumericIndicator} returns the
 * element‑wise minimum of the two underlying indicators.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_min_Test {

    /** Series used for the “real” indicator (close price). */
    private static BarSeries series;

    /** Close price indicator that will act as the left operand of {@code min}. */
    private static ClosePriceIndicator closePrice;

    /** Function converting {@link Number} to {@link Num}. */
    private static Function<Number, Num> numFunction;

    @BeforeAll
    static void setupBeforeAll() {
        // Simple DecimalNum based conversion – sufficient for the tests.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // A short series with deterministic values (0‑9).
        series = new MockBarSeries(numFunction,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        closePrice = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test state needed at the moment.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing to clean up globally.
    }

    /**
     * Verify that {@code min(Number)} (which internally creates a {@link ConstantIndicator})
     * returns the element‑wise minimum between the wrapped indicator and the constant.
     */
    @Test
    void min_withConstantIndicator_returnsCorrectMinimum() {
        // NumericIndicator wrapping the close‑price indicator.
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // Use a constant that is lower than some values and higher than others.
        Number constant = 5;
        NumericIndicator result = numeric.min(constant);

        // Expected value is the minimum between close price and 5.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).min(numFunction.apply(constant));
            assertNumEquals(expected, result.getValue(i),
                    "min at index " + i + " did not match expected value");
        }
    }

    /**
     * Verify that {@code min(Indicator)} returns the element‑wise minimum between
     * the two indicators.
     *
     * The second indicator is mocked to return a predictable sequence of numbers.
     */
    @Test
    void min_withIndicator_returnsCorrectMinimum() {
        // Mocked indicator that returns a decreasing sequence: 9‑i
        @SuppressWarnings("unchecked")
        Indicator<Num> mocked = mock(Indicator.class);
        for (int i = 0; i < series.getBarCount(); i++) {
            when(mocked.getValue(i)).thenReturn(numFunction.apply(9 - i));
        }
        // The mocked indicator must also return the same BarSeries (required by many
        // internal operations). We simply forward the call.
        when(mocked.getBarSeries()).thenReturn(series);

        // NumericIndicator wrapping the close‑price indicator (0‑9).
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // Apply min with the mocked indicator.
        NumericIndicator result = numeric.min(mocked);

        // Expected value is the element‑wise minimum of the two sequences.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num left = closePrice.getValue(i);          // 0,1,2,...
            Num right = mocked.getValue(i);            // 9,8,7,...
            Num expected = left.min(right);
            assertNumEquals(expected, result.getValue(i),
                    "min at index " + i + " did not match expected value");
        }

        // Verify that the mocked indicator was queried for each index.
        verify(mocked, times(series.getBarCount())).getValue(anyInt());
    }
}
```