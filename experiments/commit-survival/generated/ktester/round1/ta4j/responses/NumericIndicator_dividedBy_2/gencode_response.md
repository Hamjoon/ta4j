```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.TestUtils;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

/**
 * Additional tests for {@link NumericIndicator#dividedBy(Number)} covering
 * branch, edge‑case and special‑floating‑value scenarios.
 *
 * The existing tests in this class already verify the normal behaviour with a
 * series‑based indicator and with a dynamic divisor (EMA).  The tests below
 * focus on constant indicators (value 10, 0 and -10) and on exceptional
 * inputs such as division by zero, {@code null}, NaN and infinities.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_dividedBy_2_Test {

    /** Series used for all tests */
    private BarSeries series;
    /** Close‑price indicator based on the series */
    private ClosePriceIndicator closePrice;
    /** EMA indicator used as a dynamic divisor */
    private EMAIndicator ema;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple numeric conversion using DecimalNum (the default Num implementation)
        Function<Number, Num> numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // A deterministic series – values are the same as in the original test suite
        series = new MockBarSeries(
                numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8,
                7, 6, 5, 4, 3, 2, 1, 0, -1, -2
        );

        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up per‑test at the moment
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required
    }

    /* --------------------------------------------------------------------- */
    /* Existing tests – kept unchanged                                         */
    /* --------------------------------------------------------------------- */

    @Test
    void dividedByNumber_staticDivisor() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // Divide by a constant number (2)
        NumericIndicator result = numeric.dividedBy(2);

        // Verify a few representative indices
        assertNumEquals(
                closePrice.getValue(0).dividedBy(DecimalNum.valueOf(2)),
                result.getValue(0)
        );
        assertNumEquals(
                closePrice.getValue(8).dividedBy(DecimalNum.valueOf(2)),
                result.getValue(8)
        );
        // Also test a negative value to ensure sign handling
        assertNumEquals(
                closePrice.getValue(18).dividedBy(DecimalNum.valueOf(2)),
                result.getValue(18)
        );
    }

    @Test
    void dividedByIndicator_dynamicDivisor() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // Divide by another indicator (EMA)
        NumericIndicator result = numeric.dividedBy(ema);

        // Verify a few representative indices
        assertNumEquals(
                closePrice.getValue(0).dividedBy(ema.getValue(0)),
                result.getValue(0)
        );
        assertNumEquals(
                closePrice.getValue(8).dividedBy(ema.getValue(8)),
                result.getValue(8)
        );
        // Verify the last index as well
        assertNumEquals(
                closePrice.getValue(19).dividedBy(ema.getValue(19)),
                result.getValue(19)
        );
    }

    @Test
    void dividedByNumber_andDividedByIndicator_returnSameDelegateType() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        Indicator<Num> staticDiv = numeric.dividedBy(5);
        Indicator<Num> dynamicDiv = numeric.dividedBy(ema);

        // The returned objects should still be instances of NumericIndicator
        assertTrue(staticDiv instanceof NumericIndicator,
                "Static division should return a NumericIndicator");
        assertTrue(dynamicDiv instanceof NumericIndicator,
                "Dynamic division should return a NumericIndicator");
    }

    /* --------------------------------------------------------------------- */
    /* New parameterised tests for dividedBy(Number)                         */
    /* --------------------------------------------------------------------- */

    /**
     * Provides successful division scenarios.
     *
     * Each argument consists of:
     * <ul>
     *   <li>base constant value (as double) – the value wrapped by the NumericIndicator</li>
     *   <li>divisor (Number) – can be Integer, Double, Float, BigDecimal, etc.</li>
     *   <li>expected result as a string that can be parsed to DecimalNum</li>
     * </ul>
     */
    private static Stream<Arguments> successfulDivisions() {
        return Stream.of(
                // ----- group: NumericIndicator.dividedBy(Number) - branch and edge cases
                Arguments.of(10.0, 2, "5"),
                Arguments.of(10.0, -2, "-5"),
                Arguments.of(10.0, 2.5, "4"),
                // ----- group: NumericIndicator.dividedBy(Number) - additional cases
                Arguments.of(0.0, 2, "0"),
                Arguments.of(0.0, -2, "0"),
                Arguments.of(-10.0, 2, "-5"),
                Arguments.of(-10.0, -2, "5"),
                Arguments.of(10.0, -2.5, "-4"),
                Arguments.of(10.0, 0.0001, "100000"),
                Arguments.of(10.0, 1000, "0.01"),
                Arguments.of(10.0, 2.0, "5"),
                Arguments.of(10.0, new BigDecimal("2"), "5"),
                Arguments.of(10.0, -0.5, "-20")
        );
    }

    @ParameterizedTest(name = "[{index}] base={0}, divisor={1} → expected={2}")
    @MethodSource("successfulDivisions")
    void dividedByNumber_successful(double baseValue, Number divisor, String expectedStr) {
        // Build a constant indicator with the supplied base value
        ConstantIndicator<Num> constant = new ConstantIndicator<>(series,
                DecimalNum.valueOf(baseValue));
        NumericIndicator numeric = NumericIndicator.of(constant);

        // Perform the division
        NumericIndicator result = numeric.dividedBy(divisor);

        // Expected value as Num
        Num expected = DecimalNum.valueOf(Double.parseDouble(expectedStr));

        // Since the source indicator is constant, any index works – use 0
        assertNumEquals(expected, result.getValue(0),
                () -> String.format("Division of %s by %s should be %s", baseValue, divisor, expectedStr));
    }

    /**
     * Provides division scenarios that must raise an exception.
     *
     * Each argument consists of:
     * <ul>
     *   <li>base constant value (double) – not used for the exception but required for the method signature</li>
     *   <li>divisor (Number) – the problematic input</li>
     *   <li>expected exception class</li>
     * </ul>
     */
    private static Stream<Arguments> exceptionalDivisions() {
        return Stream.of(
                // division by zero
                Arguments.of(10.0, 0, ArithmeticException.class),
                // null divisor
                Arguments.of(10.0, null, NullPointerException.class),
                // special floating values – should be rejected as non‑finite numbers
                Arguments.of(10.0, Double.NaN, IllegalArgumentException.class),
                Arguments.of(10.0, Double.POSITIVE_INFINITY, IllegalArgumentException.class),
                Arguments.of(10.0, Double.NEGATIVE_INFINITY, IllegalArgumentException.class),
                Arguments.of(10.0, Float.NaN, IllegalArgumentException.class),
                Arguments.of(10.0, Float.POSITIVE_INFINITY, IllegalArgumentException.class),
                Arguments.of(10.0, Float.NEGATIVE_INFINITY, IllegalArgumentException.class)
        );
    }

    @ParameterizedTest(name = "[{index}] base={0}, divisor={1} → throws {2}")
    @MethodSource("exceptionalDivisions")
    void dividedByNumber_exceptions(double baseValue, Number divisor,
                                   Class<? extends Throwable> expectedException) {
        ConstantIndicator<Num> constant = new ConstantIndicator<>(series,
                DecimalNum.valueOf(baseValue));
        NumericIndicator numeric = NumericIndicator.of(constant);

        // Verify that the expected exception is thrown when calling dividedBy
        assertThrows(expectedException, () -> numeric.dividedBy(divisor),
                () -> String.format("Expected %s when dividing %s by %s",
                        expectedException.getSimpleName(), baseValue, divisor));
    }
}
```