```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

/**
 * Comprehensive tests for {@link NumericIndicator#crossedUnder(Number)}.
 *
 * <p>
 * The tests are driven by the JSON test‑case description supplied with the
 * kata.  Each {@code @ParameterizedTest} represents a logical group of
 * scenarios; the {@code MethodSource}s build the required {@link BarSeries}
 * and expected crossing indexes.
 * </p>
 */
@DisplayName("NumericIndicator.crossedUnder(Number) – behaviour")
class NumericIndicatorCrossedUnderTest {

    /** Helper to create {@link Num} objects from primitive numbers */
    private static final java.util.function.Function<Number, Num> NUM_FUNCTION =
            n -> DecimalNum.valueOf(n.doubleValue());

    /* --------------------------------------------------------------------- *
     *  Helper methods
     * --------------------------------------------------------------------- */

    /** Creates a {@link NumericIndicator} that wraps a {@link ClosePriceIndicator}
     *  built from the supplied numeric values. */
    private static NumericIndicator indicatorFromValues(Number... values) {
        BarSeries series = new MockBarSeries(NUM_FUNCTION, values);
        return NumericIndicator.of(new ClosePriceIndicator(series));
    }

    /** Returns true if the supplied index is listed in the expectedIndexes array. */
    private static boolean isExpected(int index, int[] expectedIndexes) {
        for (int i : expectedIndexes) {
            if (i == index) {
                return true;
            }
        }
        return false;
    }

    /* --------------------------------------------------------------------- *
     *  Parameterized tests – normal and edge behaviour
     * --------------------------------------------------------------------- */

    /**
     * Core crossing tests (positive, negative, zero, large constants, multiple
     * crossings, equality edge cases, etc.).
     *
     * Each argument contains:
     * <ul>
     *   <li>the constant {@code Number n} passed to {@code crossedUnder}</li>
     *   <li>the series values (as {@code Number[]})</li>
     *   <li>the indexes at which the rule must be satisfied</li>
     * </ul>
     */
    @ParameterizedTest(name = "{index} ⇒ n={0}, values={1}, expected={2}")
    @MethodSource("provideCrossedUnderScenarios")
    void crossedUnder_behaviour(Number n, Number[] seriesValues, int[] expectedIndexes) {
        // Arrange
        NumericIndicator numeric = indicatorFromValues(seriesValues);
        Rule rule = numeric.crossedUnder(n);

        // Assert rule type
        assertNotNull(rule, "Rule must not be null");
        assertEquals(CrossedDownIndicatorRule.class, rule.getClass(),
                "crossedUnder(Number) should return a CrossedDownIndicatorRule");

        // Verify satisfaction at every index of the series
        for (int i = 0; i < seriesValues.length; i++) {
            boolean expected = isExpected(i, expectedIndexes);
            assertEquals(expected, rule.isSatisfied(i),
                    () -> String.format("Rule satisfaction mismatch at index %d (value=%s, n=%s)",
                            i, seriesValues[i], n));
        }
    }

    /**
     * Provides the normal and edge test data described in the JSON spec.
     */
    private static Stream<Arguments> provideCrossedUnderScenarios() {
        return Stream.of(
                // 1. basic crossing 10 -> [12,11,9,8] => true at index 2 (value 9)
                Arguments.of(10, new Number[]{12, 11, 9, 8}, new int[]{2}),
                // 2. negative constant -5 -> [-3,-4,-6,-7] => true at index 2
                Arguments.of(-5, new Number[]{-3, -4, -6, -7}, new int[]{2}),
                // 3. zero constant -> [1,0.5,-0.1,-0.5] => true at index 2
                Arguments.of(0, new Number[]{1, 0.5, -0.1, -0.5}, new int[]{2}),
                // 4. never crossing 15 -> [13,14,15,16] => no true
                Arguments.of(15, new Number[]{13, 14, 15, 16}, new int[]{}),
                // 5. equality edge – previous == constant, current < constant
                //    values [11,10,9] => true at index 2 only
                Arguments.of(10, new Number[]{11, 10, 9}, new int[]{2}),
                // 6. equality edge – start equal then below, should NOT fire
                //    values [10,9] => no true
                Arguments.of(10, new Number[]{10, 9}, new int[]{}),
                // 7. single‑element series – never fires
                Arguments.of(10, new Number[]{12}, new int[]{}),
                // 8. empty series – never fires (empty array)
                Arguments.of(10, new Number[]{}, new int[]{}),
                // 9. very large constant – crossing occurs
                Arguments.of(1_000_000_000_000L,
                        new Number[]{1_000_000_000_001L, 999_999_999_999L},
                        new int[]{1}),
                // 10. positive infinity – never fires
                Arguments.of(Double.POSITIVE_INFINITY,
                        new Number[]{1e9, 2e9, 3e9},
                        new int[]{}),
                // 11. multiple crossings – [12,9,11,8,10,7] => true at 1,3,5
                Arguments.of(10, new Number[]{12, 9, 11, 8, 10, 7}, new int[]{1, 3, 5}),
                // 12. BigDecimal constant – same as test 1 but with BigDecimal
                Arguments.of(new BigDecimal("10"),
                        new Number[]{12, 11, 9}, new int[]{2})
        );
    }

    /* --------------------------------------------------------------------- *
     *  Exceptional cases – null, NaN, etc.
     * --------------------------------------------------------------------- */

    @Test
    @DisplayName("crossedUnder(null) → NullPointerException")
    void crossedUnder_nullNumber_throwsNPE() {
        NumericIndicator numeric = indicatorFromValues(1, 2, 3);
        assertThrows(NullPointerException.class,
                () -> numeric.crossedUnder((Number) null),
                "Passing null should raise NullPointerException");
    }

    @Test
    @DisplayName("crossedUnder(Double.NaN) → IllegalArgumentException")
    void crossedUnder_NaN_throwsIllegalArgument() {
        NumericIndicator numeric = indicatorFromValues(1, 2, 3);
        assertThrows(IllegalArgumentException.class,
                () -> numeric.crossedUnder(Double.NaN),
                "NaN should be rejected by createConstant");
    }

    /* --------------------------------------------------------------------- *
     *  Additional sanity check – rule instance type for exceptional paths
     * --------------------------------------------------------------------- */

    @Test
    @DisplayName("crossedUnder(BigDecimal) still returns CrossedDownIndicatorRule")
    void crossedUnder_BigDecimal_returnsCorrectRule() {
        NumericIndicator numeric = indicatorFromValues(12, 11, 9);
        Rule rule = numeric.crossedUnder(new BigDecimal("10"));
        assertNotNull(rule);
        assertEquals(CrossedDownIndicatorRule.class, rule.getClass(),
                "BigDecimal constant must still produce a CrossedDownIndicatorRule");
        // Verify the crossing occurs at index 2 as per the data set
        assertTrue(rule.isSatisfied(2), "Rule should be satisfied at the crossing index");
    }
}
```