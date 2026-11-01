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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#minus(Number)} and {@link NumericIndicator#minus(Indicator)}.
 * The test mirrors the style of the original {@code NumericIndicatorTest} but uses JUnit 5
 * and Mockito (although no mocks are required for this particular method).
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_minus_2_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /** Series containing a deterministic set of values. */
    private static BarSeries series;

    /** Indicator that provides the close price values of {@link #series}. */
    private ClosePriceIndicator closePrice;

    /** EMA indicator used for the dynamic “minus” operation. */
    private EMAIndicator ema;

    /** The {@link NumericIndicator} under test. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple deterministic series (the same data as in the original tests).
        series = new MockBarSeries(
                NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void minusWithStaticNumber() {
        // minus(5) – static subtraction
        NumericIndicator result = numericIndicator.minus(5);

        // Verify a few sample points
        assertNumEquals(1 - 5, result.getValue(0));
        assertNumEquals(9 - 5, result.getValue(8));
        assertNumEquals((-2) - 5, result.getValue(series.getBarCount() - 1));
    }

    @Test
    void minusWithDynamicIndicator() {
        // minus(ema) – subtraction where the right‑hand side is another indicator
        NumericIndicator result = numericIndicator.minus(ema);

        // Compare each value with the expected Num operation
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).minus(ema.getValue(i));
            assertNumEquals(expected, result.getValue(i));
        }
    }
}
```