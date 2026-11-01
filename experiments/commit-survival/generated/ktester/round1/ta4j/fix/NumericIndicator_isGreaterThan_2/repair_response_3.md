```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;

/**
 * Tests for {@link NumericIndicator#isGreaterThan(Number)}.
 */
class NumericIndicator_isGreaterThan_2_Test {

    private static BarSeries series;

    private ClosePriceIndicator closePrice;

    private NumericIndicator numeric;

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple series: 1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1,0,-1,-2
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new BaseBarSeries("test");

        int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2};
        for (int v : values) {
            Num n = DecimalNum.valueOf(v);
            // use the same value for open/high/low/close, volume = 1
            BaseBar bar = new BaseBar(
                    Duration.ofSeconds(1),
                    ZonedDateTime.now(),
                    n, // open
                    n, // high
                    n, // low
                    n, // close
                    DecimalNum.ONE // volume
            );
            series.addBar(bar);
        }

        closePrice = new ClosePriceIndicator(series);
        numeric = NumericIndicator.of(closePrice);
    }

    /**
     * Provides a stream of arguments: the Number to pass, the expected {@link Num}
     * value inside the generated {@link ConstantIndicator}, and a short label
     * for readability.
     */
    private static Stream<Arguments> numberProvider() {
        return Stream.of(
                Arguments.of(10, DecimalNum.valueOf(10), "int 10"),
                Arguments.of(0, DecimalNum.valueOf(0), "int 0"),
                Arguments.of(-5, DecimalNum.valueOf(-5), "int -5"),
                Arguments.of(3.14159, DecimalNum.valueOf(3.14159), "double 3.14159"),
                Arguments.of(Long.MAX_VALUE, DecimalNum.valueOf(Long.MAX_VALUE), "Long.MAX_VALUE"),
                Arguments.of(2.5f, DecimalNum.valueOf(2.5f), "float 2.5f"),
                Arguments.of(new BigDecimal("12345.6789"),
                        DecimalNum.valueOf(new BigDecimal("12345.6789")), "BigDecimal"),
                Arguments.of(new BigInteger("999999999999999999"),
                        DecimalNum.valueOf(new BigInteger("999999999999999999")), "BigInteger"),
                Arguments.of((short) 7, DecimalNum.valueOf(7), "short 7"),
                Arguments.of((byte) -2, DecimalNum.valueOf(-2), "byte -2"),
                Arguments.of(Integer.MAX_VALUE, DecimalNum.valueOf(Integer.MAX_VALUE), "Integer.MAX_VALUE"),
                Arguments.of(Integer.MIN_VALUE, DecimalNum.valueOf(Integer.MIN_VALUE), "Integer.MIN_VALUE"),
                Arguments.of(Double.MAX_VALUE, DecimalNum.valueOf(Double.MAX_VALUE), "Double.MAX_VALUE"),
                Arguments.of(Double.NaN, DecimalNum.valueOf(Double.NaN), "Double.NaN"),
                Arguments.of(Double.NEGATIVE_INFINITY, DecimalNum.valueOf(Double.NEGATIVE_INFINITY),
                        "Double.NEGATIVE_INFINITY"),
                Arguments.of(Double.POSITIVE_INFINITY, DecimalNum.valueOf(Double.POSITIVE_INFINITY),
                        "Double.POSITIVE_INFINITY"),
                Arguments.of(Float.NaN, DecimalNum.valueOf(Float.NaN), "Float.NaN"),
                Arguments.of(Float.POSITIVE_INFINITY, DecimalNum.valueOf(Float.POSITIVE_INFINITY),
                        "Float.POSITIVE_INFINITY"));
    }

    /**
     * Helper that uses reflection to obtain the constant (right‑hand) indicator
     * stored inside an {@link OverIndicatorRule}.
     */
    private Indicator<Num> extractConstantIndicator(Rule rule) throws Exception {
        Class<?> clazz = rule.getClass();
        Field field;
        try {
            // ta4j 0.14+
            field = clazz.getDeclaredField("threshold");
        } catch (NoSuchFieldException e1) {
            try {
                // older versions
                field = clazz.getDeclaredField("rightIndicator");
            } catch (NoSuchFieldException e2) {
                throw new IllegalStateException("Unable to locate constant indicator field in OverIndicatorRule", e2);
            }
        }
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Indicator<Num> constant = (Indicator<Num>) field.get(rule);
        return constant;
    }

    /**
     * Returns the first index where the series value is strictly less than the
     * supplied {@link Num}. If none exists, returns -1.
     */
    private int findFirstIndexWhereSeriesIsLessThan(Num value) {
        for (int i = 0; i < series.getBarCount(); i++) {
            if (closePrice.getValue(i).isLessThan(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first index where the series value is strictly greater than the
     * supplied {@link Num}. If none exists, returns -1.
     */
    private int findFirstIndexWhereSeriesIsGreaterThan(Num value) {
        for (int i = 0; i < series.getBarCount(); i++) {
            if (closePrice.getValue(i).isGreaterThan(value)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void isGreaterThan_withNumber_returnsOverIndicatorRule_andEvaluatesCorrectly() {
        // Act
        Rule rule = numeric.isGreaterThan(5);
        // Assert type
        assertTrue(rule instanceof OverIndicatorRule,
                "isGreaterThan(Number) should return an OverIndicatorRule");
        // The first close price is 1 (<=5) → rule not satisfied
        assertFalse(rule.isSatisfied(0), "Rule should be false when value <= 5");
        // At index 8 the close price is 9 (>5) → rule satisfied
        assertTrue(rule.isSatisfied(8), "Rule should be true when value > 5");
    }

    @Test
    void isGreaterThan_withIndicator_returnsOverIndicatorRule_andEvaluatesCorrectly() {
        // Arrange: a constant indicator that always returns 5
        ConstantIndicator<Num> constFive = new ConstantIndicator<>(series, DecimalNum.valueOf(5));
        // Act
        Rule rule = numeric.isGreaterThan(constFive);
        // Assert type
        assertTrue(rule instanceof OverIndicatorRule,
                "isGreaterThan(Indicator) should return an OverIndicatorRule");
        // Index 0: close price 1 <= 5 → false
        assertFalse(rule.isSatisfied(0), "Rule should be false when close price <= constant");
        // Index 8: close price 9 > 5 → true
        assertTrue(rule.isSatisfied(8), "Rule should be true when close price > constant");
    }

    @ParameterizedTest(name = "[{index}] isGreaterThan({2})")
    @MethodSource("numberProvider")
    void isGreaterThan_withVariousNumbers_returnsOverIndicatorRule(Number n, Num expectedNum, String label)
            throws Exception {
        // Act
        Rule rule = numeric.isGreaterThan(n);
        // Verify the returned rule type
        assertTrue(rule instanceof OverIndicatorRule,
                "For input " + label + " the method should return an OverIndicatorRule");

        // Extract the constant indicator that backs the rule via reflection.
        Indicator<Num> constantIndicator = extractConstantIndicator(rule);
        // The constant indicator should return the same value for any index.
        Num actual = constantIndicator.getValue(0);

        // Comparison logic – special handling for NaN because NaN != NaN.
        if (expectedNum.isNaN()) {
            assertTrue(actual.isNaN(), "Constant indicator should contain NaN for input " + label);
        } else if (expectedNum.isPositiveInfinity()) {
            assertTrue(actual.isPositiveInfinity(),
                    "Constant indicator should contain +Infinity for input " + label);
        } else if (expectedNum.isNegativeInfinity()) {
            assertTrue(actual.isNegativeInfinity(),
                    "Constant indicator should contain -Infinity for input " + label);
        } else {
            assertEquals(expectedNum, actual,
                    "Constant indicator value mismatch for input " + label);
        }

        // Additional behavioural check: the rule must be false for an index where
        // the series value is lower than the constant and true when it is higher.
        int lowerIdx = findFirstIndexWhereSeriesIsLessThan(expectedNum);
        int higherIdx = findFirstIndexWhereSeriesIsGreaterThan(expectedNum);
        if (lowerIdx >= 0) {
            assertFalse(rule.isSatisfied(lowerIdx),
                    "Rule should be false at index " + lowerIdx + " where series value <= constant");
        }
        if (higherIdx >= 0) {
            assertTrue(rule.isSatisfied(higherIdx),
                    "Rule should be true at index " + higherIdx + " where series value > constant");
        }
    }

    /**
     * Null argument must lead to an exception (the implementation guards
     * against it). The exact exception type is not part of the public contract,
     * so we accept either {@link NullPointerException} or {@link IllegalArgumentException}.
     */
    @Test
    void isGreaterThan_withNullNumber_throwsException() {
        assertThrows(RuntimeException.class, () -> numeric.isGreaterThan((Number) null),
                "Calling isGreaterThan(null) should throw an exception");
    }
}
```