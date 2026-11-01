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
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#minus(Indicator)}.
 *
 * The tests verify both the static (Number) and dynamic (Indicator) overloads.
 * They use a small {@link MockBarSeries} with deterministic values so that the
 * expected results can be calculated manually.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_minus_Test {

    /** Series used by all tests – created once for the whole test class. */
    private static BarSeries series;

    /** Close‑price indicator that will be wrapped by {@link NumericIndicator}. */
    private static ClosePriceIndicator closePrice;

    /** EMA indicator used as a dynamic operand in the {@code minus} test. */
    private static EMAIndicator ema;

    /** Helper to convert primitive numbers to {@link Num}. */
    private static Function<Number, Num> numFunction;

    /** The {@link NumericIndicator} instance under test. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Convert a Number to the library's Num implementation (DecimalNum)
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // Create a deterministic series: 1, 2, 3, …, 9, 8, 7, …, -2
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);

        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Wrap the close‑price indicator with the fluent NumericIndicator
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // No mutable state to clean up between tests
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (helpful for large test suites)
        series = null;
        closePrice = null;
        ema = null;
        numFunction = null;
    }

    @Test
    void minusWithNumber() {
        // static subtraction:  value - 5
        NumericIndicator result = numericIndicator.minus(5);
        assertNumEquals(1 - 5, result.getValue(0));
        assertNumEquals(9 - 5, result.getValue(8));
        assertNumEquals(-2 - 5, result.getValue(series.getBarCount() - 1));
    }

    @Test
    void minusWithIndicator() {
        // dynamic subtraction:  value - ema(value)
        NumericIndicator result = numericIndicator.minus(ema);
        // index 0
        Num expected0 = closePrice.getValue(0).minus(ema.getValue(0));
        assertNumEquals(expected0, result.getValue(0));

        // index 8 (the peak of the series)
        Num expected8 = closePrice.getValue(8).minus(ema.getValue(8));
        assertNumEquals(expected8, result.getValue(8));

        // a later index to ensure the operation works throughout the series
        int idx = 15;
        Num expectedIdx = closePrice.getValue(idx).minus(ema.getValue(idx));
        assertNumEquals(expectedIdx, result.getValue(idx));
    }

    @Test
    void minusUsesConstantIndicatorInternally() {
        // The overload that accepts a Number delegates to the Indicator overload
        // using a ConstantIndicator. We verify the behaviour is identical.
        NumericIndicator viaNumber = numericIndicator.minus(7);
        NumericIndicator viaIndicator = numericIndicator.minus(new ConstantIndicator<>(closePrice, numFunction.apply(7)));

        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(viaNumber.getValue(i), viaIndicator.getValue(i));
        }
    }
}
```