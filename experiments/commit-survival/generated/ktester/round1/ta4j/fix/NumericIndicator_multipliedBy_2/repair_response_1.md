```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_multipliedBy_2_Test {

    // -----------------------------------------------------------------------
    private static BarSeries series;

    private static ClosePriceIndicator closePriceIndicator;

    private static EMAIndicator emaIndicator;

    private static Function<Number, Num> numFunction = DecimalNum::valueOf;

    @BeforeAll
    static void setupBeforeAll() {
        // Initialise a mock BarSeries with deterministic numeric values
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        emaIndicator = new EMAIndicator(closePriceIndicator, 3);
    }

    private static Stream<Arguments> provideNumbersForMultiplication() {
        return Stream.of(
                // Standard positive integer
                Arguments.of(5, "Standard case – multiply by positive integer 5"),
                // Edge case – zero
                Arguments.of(0, "Edge case – multiply by zero"),
                // Negative decimal
                Arguments.of(-3.2, "Negative non‑integer constant"),
                // Identity multiplication
                Arguments.of(1, "Identity case – multiply by 1"),
                // Negation
                Arguments.of(-1, "Negation case – multiply by -1"),
                // Very large long value
                Arguments.of(Long.MAX_VALUE, "Large positive integer – Long.MAX_VALUE"),
                // Small fractional BigDecimal
                Arguments.of(new BigDecimal("0.0000001"),
                        "Small fractional constant – BigDecimal 0.0000001"),
                // Positive infinity
                Arguments.of(Double.POSITIVE_INFINITY,
                        "Infinity constant – Double.POSITIVE_INFINITY"),
                // Very large BigInteger
                Arguments.of(new BigInteger("123456789012345678901234567890"),
                        "Very large integer – BigInteger")
        );
    }

    // -----------------------------------------------------------------------
    @Test
    void multipliedByNumber_returnsCorrectStaticOperation() {
        // GIVEN
        NumericIndicator numericIndicator = NumericIndicator.of(closePriceIndicator);
        // WHEN
        NumericIndicator multiplied = numericIndicator.multipliedBy(5);
        // THEN
        assertEquals(numFunction.apply(1 * 5), multiplied.getValue(0));
        assertEquals(numFunction.apply(9 * 5), multiplied.getValue(8));
    }

    @Test
    void multipliedByIndicator_returnsCorrectDynamicOperation() {
        // GIVEN
        NumericIndicator numericIndicator = NumericIndicator.of(closePriceIndicator);
        // WHEN
        NumericIndicator multiplied = numericIndicator.multipliedBy(emaIndicator);
        // THEN
        Num expected0 = closePriceIndicator.getValue(0).multipliedBy(emaIndicator.getValue(0));
        Num expected8 = closePriceIndicator.getValue(8).multipliedBy(emaIndicator.getValue(8));
        assertEquals(expected0, multiplied.getValue(0));
        assertEquals(expected8, multiplied.getValue(8));
    }

    // -----------------------------------------------------------------------
    @ParameterizedTest(name = "[{index}] n={0} → {1}")
    @MethodSource("provideNumbersForMultiplication")
    void multipliedByNumber_variousScenarios(Number n, String description) {
        // GIVEN
        NumericIndicator base = NumericIndicator.of(closePriceIndicator);
        // WHEN
        NumericIndicator result = base.multipliedBy(n);
        // THEN – verify a couple of representative indices (0, 8, 18)
        // Index 0
        Num expected0 = closePriceIndicator.getValue(0).multipliedBy(numFunction.apply(n));
        assertEquals(expected0, result.getValue(0),
                "Failed at index 0 for " + description);
        // Index 8 (a positive value in the series)
        Num expected8 = closePriceIndicator.getValue(8).multipliedBy(numFunction.apply(n));
        assertEquals(expected8, result.getValue(8),
                "Failed at index 8 for " + description);
        // Index 18 (a negative value in the series) – useful for sign‑related cases
        Num expected18 = closePriceIndicator.getValue(18).multipliedBy(numFunction.apply(n));
        assertEquals(expected18, result.getValue(18),
                "Failed at index 18 for " + description);
    }

    // -----------------------------------------------------------------------
    @Test
    void multipliedByNumber_NaNPropagation() {
        // GIVEN
        NumericIndicator base = NumericIndicator.of(closePriceIndicator);
        Double nan = Double.NaN;
        // WHEN
        NumericIndicator result = base.multipliedBy(nan);
        // THEN – every value should be NaN according to the Num implementation
        for (int i = 0; i < series.getBarCount(); i++) {
            Num value = result.getValue(i);
            assertTrue(value.isNaN(), "Expected NaN at index " + i);
        }
    }

    @Test
    void multipliedByNumber_nullArgumentThrowsNPE() {
        // GIVEN
        NumericIndicator base = NumericIndicator.of(closePriceIndicator);
        // WHEN / THEN
        assertThrows(NullPointerException.class,
                () -> base.multipliedBy(null),
                "Passing null should raise NullPointerException");
    }
}
```