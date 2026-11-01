package org.ta4j.core.indicators.numeric;

// import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
// import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

/**
 * Tests for {@link NumericIndicator#crossedOver(Number)} and its overloads.
 */
// @ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedOver_2_Test {

    /**
     * Simple conversion from {@link Number} to {@link Num}.
     */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private EMAIndicator ema;
    private NumericIndicator numericIndicator;

//     @BeforeEach
    void setupBeforeEach() {
        // Series: 4, 6 – this creates a clear crossing of the constant 5
        // (below 5 at index 0, above 5 at index 1).
//         series = new MockBarSeries(NUM_FUNCTION, 4, 6);
        closePrice = new ClosePriceIndicator(series);
        // EMA with a short period to have a deterministic value for the second test.
        ema = new EMAIndicator(closePrice, 2);
        numericIndicator = NumericIndicator.of(closePrice);
    }

//     private static Stream<Arguments> basicCrossOverProvider() {
//         return Stream.of(
//                 // 5 – crossing from 4 (≤5) to 6 (>5) at index 1
//                 Arguments.of(5, new double[]{3, 4, 6}, new int[]{2}),
//                 // -3 – crossing from -4 (≤-3) to -2 (> -3) at index 2
//                 Arguments.of(-3, new double[]{-5, -4, -2}, new int[]{2}),
//                 // 0 – crossing only at index 2
//                 Arguments.of(0, new double[]{-1, -0.5, 0.2}, new int[]{2}),
//                 // 1000 – never crosses (all values far below)
//                 Arguments.of(1000, new double[]{100, 200, 300}, new int[]{}),
//                 // -1000 – never crosses (all values far above)
//                 Arguments.of(-1000, new double[]{0, 1, 2}, new int[]{}),
//                 // 3.5 – crossing at index 2 (3.4 ≤ 3.5 → 3.6 > 3.5)
//                 Arguments.of(3.5, new double[]{3.5, 3.4, 3.6}, new int[]{2}),
//                 // Double.POSITIVE_INFINITY – never satisfied
//                 Arguments.of(Double.POSITIVE_INFINITY, new double[]{1, 2, 3}, new int[]{}),
//                 // Double.NEGATIVE_INFINITY – never satisfied (always > -∞)
//                 Arguments.of(Double.NEGATIVE_INFINITY, new double[]{-5, -4, -3}, new int[]{}),
//                 // Integer.MAX_VALUE – never satisfied
//                 Arguments.of(Integer.MAX_VALUE, new double[]{100, 200, 300}, new int[]{}),
//                 // Multiple up‑crossings over 0
//                 Arguments.of(0, new double[]{-1, 0.1, -0.2, 0.5}, new int[]{1, 3}),
//                 // Long constant 5L – crossing at index 1
//                 Arguments.of(5L, new double[]{4, 6}, new int[]{1})
//         );
//     }

    private static boolean contains(int[] array, int value) {
        return Arrays.stream(array).anyMatch(i -> i == value);
    }

//     private static Stream<Arguments> exceptionCrossOverProvider() {
//         return Stream.of(
//                 // null Number
//                 Arguments.of(null, NullPointerException.class),
//                 // NaN – illegal argument
//                 Arguments.of(Double.NaN, IllegalArgumentException.class),
//                 // Positive infinity – illegal argument
//                 Arguments.of(Double.POSITIVE_INFINITY, IllegalArgumentException.class),
//                 // Negative infinity – illegal argument
//                 Arguments.of(Double.NEGATIVE_INFINITY, IllegalArgumentException.class)
//         );
//     }

//     @Test
//     void crossedOverWithConstantNumber() {
//         // Act
//         Rule rule = numericIndicator.crossedOver(5);
//         // Assert rule type
//         assertTrue(rule instanceof CrossedUpIndicatorRule,
//                 "crossedOver(Number) should return a CrossedUpIndicatorRule");
//         // The series goes from 4 (below 5) to 6 (above 5). The crossing happens
//         // between index 0 and 1, therefore the rule must be satisfied at index 1.
//         assertFalse(rule.isSatisfied(0), "Rule must not be satisfied before the crossing");
//         assertTrue(rule.isSatisfied(1), "Rule must be satisfied when the value crosses above 5");
//     }

//     @Test
//     void crossedOverWithIndicator() {
//         // Act
//         Rule rule = numericIndicator.crossedOver(ema);
//         // Verify returned type
//         assertTrue(rule instanceof CrossedUpIndicatorRule,
//                 "crossedOver(Indicator) should return a CrossedUpIndicatorRule");
//     }

    /* --------------------------------------------------------------
     *  Basic functionality – a rule that should become satisfied when
     *  the underlying indicator value crosses the supplied constant.
     * -------------------------------------------------------------- */
//     @ParameterizedTest(name = "{index}: constant={0}, series={1}, satisfied={2}")
//     @MethodSource("basicCrossOverProvider")
//     void crossedOver_Number_Basic(Number constant, double[] seriesValues, int[] satisfiedIndices) {
//         // Build a series that matches the test case
//         series = new MockBarSeries(NUM_FUNCTION, seriesValues);
//         closePrice = new ClosePriceIndicator(series);
//         numericIndicator = NumericIndicator.of(closePrice);
//         // Act
//         Rule rule = numericIndicator.crossedOver(constant);
//         // -----------------------------------------------------------------
//         // Assertions
//         // -----------------------------------------------------------------
//         assertNotNull(rule, "crossedOver should never return null");
//         assertTrue(rule instanceof CrossedUpIndicatorRule,
//                 "crossedOver(Number) must return a CrossedUpIndicatorRule");
//         // Verify rule satisfaction for each index of the series
//         for (int i = 0; i < seriesValues.length; i++) {
//             boolean expected = contains(satisfiedIndices, i);
//             assertEquals(expected, rule.isSatisfied(i),
//                     () -> String.format("Rule satisfaction at index %d (constant=%s)", i, constant));
//         }
//     }

    /* --------------------------------------------------------------
     *  Exception scenarios – null and non‑finite numbers.
     * -------------------------------------------------------------- */
//     @ParameterizedTest(name = "{index}: constant={0}")
//     @MethodSource("exceptionCrossOverProvider")
//     void crossedOver_Number_Exceptions(Number constant, Class<? extends Throwable> expectedException) {
//         // Use a simple series – the content does not matter because we expect an exception
//         series = new MockBarSeries(NUM_FUNCTION, 1, 2);
//         closePrice = new ClosePriceIndicator(series);
//         numericIndicator = NumericIndicator.of(closePrice);
//         // Act & Assert
//         assertThrows(expectedException,
//                 () -> numericIndicator.crossedOver(constant),
//                 () -> "crossedOver should throw " + expectedException.getSimpleName() + " for constant: " + constant);
//     }

    /* --------------------------------------------------------------
     *  Overload that accepts an Indicator – simple type check.
     * -------------------------------------------------------------- */
//     @Test
    void crossedOver_WithIndicator_ReturnsCrossedUpRule() {
        // Using the EMA indicator defined in @BeforeEach
        Rule rule = numericIndicator.crossedOver(ema);
//         assertNotNull(rule, "crossedOver(Indicator) must not return null");
//         assertTrue(rule instanceof CrossedUpIndicatorRule,
                "crossedOver(Indicator) should return a CrossedUpIndicatorRule");
    }
}