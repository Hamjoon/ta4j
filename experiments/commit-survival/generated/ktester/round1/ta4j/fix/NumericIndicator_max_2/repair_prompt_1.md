
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.ta4j.core.mocks.MockBarSeries;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_max_2_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /**
     * Series used by all tests – the same data as in the original test suite.
     */
    private static BarSeries series;

    /**
     * Close price indicator that wraps the series.
     */
    private static ClosePriceIndicator closePrice;

    /**
     * EMA indicator used for the dynamic‑argument test.
     */
    private static EMAIndicator ema;

    /**
     * Instance under test – created before each test method.
     */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // values are the same as in the original NumericIndicatorTest
        series = new MockBarSeries(NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // create a fresh NumericIndicator for every test
        numericIndicator = NumericIndicator.of(closePrice);
    }

    /**
     * Provides a wide range of {@link Number} constants that are used to
     * verify the element‑wise maximum logic.
     */
    private static Stream<Arguments> provideConstantsForMax() {
        return Stream.of(
                // basic functionality
                Arguments.of(10), // greater than every delegate value
                Arguments.of(3),  // lower than every delegate value
                Arguments.of(5),  // equal to some delegate values
                // edge cases
                Arguments.of(0),          // zero
                Arguments.of(-5),         // negative constant
                Arguments.of(5.5),        // fractional double
                Arguments.of(Integer.MAX_VALUE), // huge positive int
                Arguments.of(Integer.MIN_VALUE), // huge negative int
                Arguments.of(9.223372036854776e+18), // Double representation of Long.MAX_VALUE
                Arguments.of(1.7976931348623157e+308), // Double.MAX_VALUE (≈∞)
                Arguments.of(Double.NEGATIVE_INFINITY), // negative infinity
                Arguments.of(Double.NaN),               // NaN
                Arguments.of(Double.POSITIVE_INFINITY) // positive infinity
        );
    }

    @Test
    void maxWithConstantNumber() {
        // given
        Number constant = 5;
        // when
        NumericIndicator maxIndicator = numericIndicator.max(constant);
        // then
        Num constNum = NUM_FUNCTION.apply(constant);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).max(constNum);
            assertEquals(expected, maxIndicator.getValue(i),
                    "max at index " + i + " should be the larger of close price and constant 5");
        }
    }

    @Test
    void maxWithAnotherIndicator() {
        // when
        NumericIndicator maxIndicator = numericIndicator.max(ema);
        // then
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).max(ema.getValue(i));
            assertEquals(expected, maxIndicator.getValue(i),
                    "max at index " + i + " should be the larger of close price and EMA");
        }
    }

    @Test
    void maxReturnsNumericIndicatorAndPreservesDelegateSeries() {
        // when
        NumericIndicator result = numericIndicator.max(10);
        // then
        assertNotNull(result);
        // the delegate of the resulting indicator must still be linked to the same series
        Indicator<Num> delegate = result.delegate();
        assertEquals(series, delegate.getBarSeries(),
                "The delegate of the max‑indicator should use the original BarSeries");
    }

    @ParameterizedTest(name = "max(Number) with constant {0}")
    @MethodSource("provideConstantsForMax")
    void maxWithVariousConstants_ShouldReturnCorrectElementWiseMaximum(Number constant) {
        // when
        NumericIndicator maxIndicator = numericIndicator.max(constant);
        // then – compare each index with the expected Num.max(...)
        Num constNum = NUM_FUNCTION.apply(constant);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num delegateValue = closePrice.getValue(i);
            Num expected = delegateValue.max(constNum);
            Num actual = maxIndicator.getValue(i);
            // Special handling for NaN: Num.max(NaN, x) returns NaN in DecimalNum
            if (Double.isNaN(constant.doubleValue())) {
                assertTrue(actual.isNaN(),
                        () -> "Expected NaN at index " + i + " when constant is NaN");
            } else {
                assertEquals(expected, actual,
                        () -> "Incorrect max at index " + i + " for constant " + constant);
            }
        }
    }

    /**
     * Tests that passing {@code null} as the constant triggers a {@link NullPointerException}.
     */
    @Test
    void maxWithNullConstant_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> numericIndicator.max((Number) null),
                "Calling max(null) must throw NullPointerException");
    }

    @Test
    void maxWhenDelegateIsConstantIndicatorAndConstantIsZero() {
        // delegate = constant 0
        ConstantIndicator constZero = new ConstantIndicator(series, NUM_FUNCTION.apply(0));
        NumericIndicator constNumeric = NumericIndicator.of(constZero);
        // constant argument = 0
        NumericIndicator result = constNumeric.max(0);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num actual = result.getValue(i);
            assertEquals(NUM_FUNCTION.apply(0), actual,
                    "When both sides are 0 the result must be 0 at index " + i);
        }
    }

    @Test
    void maxWhenDelegateIsConstantIndicatorAndConstantIsHigher() {
        // delegate = constant -20
        ConstantIndicator constMinus20 = new ConstantIndicator(series, NUM_FUNCTION.apply(-20));
        NumericIndicator constNumeric = NumericIndicator.of(constMinus20);
        // constant argument = -10 (higher than -20)
        NumericIndicator result = constNumeric.max(-10);
        Num expected = NUM_FUNCTION.apply(-10);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num actual = result.getValue(i);
            assertEquals(expected, actual,
                    "Result should be -10 for every index when delegate is -20 and constant is -10");
        }
    }

    @Test
    void maxWithIndicatorOverload_ShouldBehaveSameAsConstantVersion() {
        // Use the EMA indicator as the “other” argument
        NumericIndicator result = numericIndicator.max(ema);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).max(ema.getValue(i));
            assertEquals(expected, result.getValue(i),
                    "max with EMA indicator differs at index " + i);
        }
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_max_2_Test.java:24: error: ';' expected
import org.junit.jupiter.params.provider.MethodSource
                                                     ^
1 error

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.indicators.numeric.NumericIndicator': '\n * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides\n * methods to create rules and other "lightweight" indicators, using a\n * (hopefully) natural-looking and expressive series of method calls.\n * \n * <p>\n * Methods like plus(), minus() and sqrt() correspond directly to methods in the\n * {@code Num} interface. These methods create "lightweight" (not cached)\n * indicators to add, subtract, etc. Many methods are overloaded to accept\n * either {@code Indicator<Num>} or {@code Number} arguments.\n * \n * <p>\n * Methods like sma() and ema() simply create the corresponding indicator\n * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first\n * argument. These methods usually instantiate cached objects.\n * \n * <p>\n * Another set of methods, like crossedOver() and isGreaterThan() create Rule\n * objects. These are also overloaded to accept both {@code Indicator<Num>} and\n * {@code Number} arguments.\n ', 'api document of method max(Number)': '\n     * @param n the other number\n     * @return the greater of {@code this} and {@code n}; if they are equal,\n     *         {@code this} is returned.\n     ', 'return type': 'org.ta4j.core.indicators.numeric.NumericIndicator', 'dependent classes': 'class `org.ta4j.core.indicators.numeric.NumericIndicator`:\n\trelated functions:\n\t\tmethod `public NumericIndicator max(Number)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.max(Number)`, api document: \n     * @param n the other number\n     * @return the greater of {@code this} and {@code n}; if they are equal,\n     *         {@code this} is returned.\n     ;\n\t\tmethod `public NumericIndicator plus(Number)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.max(Number)`, api document: \n     * @param n the other number\n     * @return {@code this + n}, rounded as necessary\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


