package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import java.util.List;
import java.util.stream.Stream;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.params.provider.Arguments;
// import static org.mockito.Mockito.*;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.api.AfterEach;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.api.BeforeEach;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.api.Test;
// import static org.mockito.Mockito.*;

// import org.mockito.Mock;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mockito;
// import org.junit.jupiter.params.ParameterizedTest;
// import static org.mockito.Mockito.*;
// import org.junit.jupiter.params.provider.Arguments;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// @ExtendWith(MockitoExtension.class)
class NumericIndicator_isLessThan_Test {

    // delegate of the NumericIndicator under test
    private Indicator<Num> leftIndicator;

    // indicator passed to isLessThan
    private Indicator<Num> rightIndicator;

    // required for some Indicator implementations (not used directly)
    private BarSeries barSeries;

    // the instance under test
    private NumericIndicator numericIndicator;

//     @AfterEach
    void teardownAfterEach() {
        // Reset mocks after each test to avoid interaction leakage
//         reset(leftIndicator, rightIndicator, barSeries);
    }

    //     @BeforeEach
    void setupBeforeEach() {
        // Initialise the NumericIndicator with the mocked left indicator.
        numericIndicator = NumericIndicator.of(leftIndicator);
    }

    /**
     * Provides a matrix of left values (single or multi‑index), a constant right
     * indicator value, the index to evaluate and the expected outcome.
     */
//     private static Stream<Arguments> basicComparisonProvider() {
//         return Stream.of(// group: NumericIndicator.isLessThan - basic comparisons
//         Arguments.of(List.of(DecimalNum.valueOf(3)), DecimalNum.valueOf(5), 0, true, "3 < 5 → satisfied at index 0"), Arguments.of(List.of(DecimalNum.valueOf(3)), DecimalNum.valueOf(3), 0, false, "3 == 3 → not satisfied at index 0"), Arguments.of(List.of(DecimalNum.valueOf(3)), DecimalNum.valueOf(2), 0, false, "3 > 2 → not satisfied at index 0"), Arguments.of(List.of(DecimalNum.valueOf(3), DecimalNum.valueOf(4)), DecimalNum.valueOf(5), 1, true, "multi‑index: 4 < 5 → satisfied at index 1"), Arguments.of(List.of(DecimalNum.valueOf(3), DecimalNum.valueOf(4)), DecimalNum.valueOf(4), 1, false, "multi‑index: 4 == 4 → not satisfied at index 1"), // group: NumericIndicator.isLessThan - negative and zero values
//         Arguments.of(List.of(DecimalNum.valueOf(-2)), DecimalNum.valueOf(-1), 0, true, "-2 < -1 → satisfied at index 0"), Arguments.of(List.of(DecimalNum.valueOf(0)), DecimalNum.valueOf(0), 0, false, "0 == 0 → not satisfied at index 0"), Arguments.of(List.of(DecimalNum.valueOf(-0.5)), DecimalNum.valueOf(0), 0, true, "-0.5 < 0 → satisfied at index 0"), // group: NumericIndicator.isLessThan - fractional values
//         Arguments.of(List.of(DecimalNum.valueOf(2.4)), DecimalNum.valueOf(2.5), 0, true, "2.4 < 2.5 → satisfied at index 0"), Arguments.of(List.of(DecimalNum.valueOf(2.5)), DecimalNum.valueOf(2.5), 0, false, "2.5 == 2.5 → not satisfied at index 0"), // group: NumericIndicator.isLessThan - large numeric values
//         Arguments.of(List.of(DecimalNum.valueOf(1_000_000)), DecimalNum.valueOf(2_000_000), 0, true, "1 000 000 < 2 000 000 → satisfied at index 0"));
//     }

//     @Test
//     void shouldReturnUnderIndicatorRuleAndEvaluateTrueWhenLeftIsLessThanRight() {
//         // GIVEN
//         int index = 0;
//         Num leftValue = DecimalNum.valueOf(1);
//         Num rightValue = DecimalNum.valueOf(5);
//         when(leftIndicator.getValue(index)).thenReturn(leftValue);
//         when(rightIndicator.getValue(index)).thenReturn(rightValue);
//         // WHEN
//         Rule rule = numericIndicator.isLessThan(rightIndicator);
//         // THEN
//         assertTrue(rule instanceof UnderIndicatorRule, "Rule should be an instance of UnderIndicatorRule");
//         assertTrue(rule.isSatisfied(index), "Rule should be satisfied when left < right");
//     }

//     @Test
//     void shouldReturnUnderIndicatorRuleAndEvaluateFalseWhenLeftIsNotLessThanRight() {
//         // GIVEN
//         int index = 3;
//         Num leftValue = DecimalNum.valueOf(10);
//         Num rightValue = DecimalNum.valueOf(4);
//         when(leftIndicator.getValue(index)).thenReturn(leftValue);
//         when(rightIndicator.getValue(index)).thenReturn(rightValue);
//         // WHEN
//         Rule rule = numericIndicator.isLessThan(rightIndicator);
//         // THEN
//         assertTrue(rule instanceof UnderIndicatorRule, "Rule should be an instance of UnderIndicatorRule");
//         assertFalse(rule.isSatisfied(index), "Rule should not be satisfied when left >= right");
//     }

//     @Test
//     void shouldCreateRuleWithCorrectIndicators() {
//         // WHEN
//         Rule rule = numericIndicator.isLessThan(rightIndicator);
//         // THEN
//         assertTrue(rule instanceof UnderIndicatorRule);
//         UnderIndicatorRule underRule = (UnderIndicatorRule) rule;
//         // The rule should keep the exact indicator instances we passed.
//         assertSame(numericIndicator, underRule.getLeftIndicator(), "Left indicator of the rule should be the NumericIndicator itself");
//         assertSame(rightIndicator, underRule.getRightIndicator(), "Right indicator of the rule should be the indicator supplied to isLessThan");
//     }

//     @ParameterizedTest(name = "{4}")
//     @MethodSource("basicComparisonProvider")
//     void shouldEvaluateUnderIndicatorRuleCorrectly(List<Num> leftValues, Num rightValue, int indexToCheck, boolean expectedSatisfied, String description) {
//         // Stub the mocked left indicator for each index we will query.
//         for (int i = 0; i < leftValues.size(); i++) {
//             when(leftIndicator.getValue(i)).thenReturn(leftValues.get(i));
//         }
//         // The "other" indicator is a real ConstantIndicator – this exercises the
//         // rule with a genuine Indicator implementation.
//         Indicator<Num> other = new ConstantIndicator<>(rightValue);
//         // Build the rule via the method under test.
//         Rule rule = numericIndicator.isLessThan(other);
//         assertTrue(rule instanceof UnderIndicatorRule, "The rule returned must be an UnderIndicatorRule");
//         // Verify the satisfaction result matches the expectation.
//         assertEquals(expectedSatisfied, rule.isSatisfied(indexToCheck), description);
//     }

//     @Test
//     void shouldWorkWithVaryingOtherIndicator_ClosePrice() {
//         // GIVEN – a BarSeries whose close prices are [1, 2, 3]
//         BarSeries series = mock(BarSeries.class);
//         Bar bar0 = mock(Bar.class);
//         Bar bar1 = mock(Bar.class);
//         Bar bar2 = mock(Bar.class);
//         when(bar0.getClosePrice()).thenReturn(DecimalNum.valueOf(1));
//         when(bar1.getClosePrice()).thenReturn(DecimalNum.valueOf(2));
//         when(bar2.getClosePrice()).thenReturn(DecimalNum.valueOf(3));
//         when(series.getBarCount()).thenReturn(3);
//         when(series.getBar(0)).thenReturn(bar0);
//         when(series.getBar(1)).thenReturn(bar1);
//         when(series.getBar(2)).thenReturn(bar2);
//         // Left side of the rule: ClosePriceIndicator wrapping the series.
//         Indicator<Num> closePrice = new ClosePriceIndicator(series);
//         NumericIndicator numeric = NumericIndicator.of(closePrice);
//         // Right side: constant value 2
//         Indicator<Num> other = new ConstantIndicator<>(DecimalNum.valueOf(2));
//         // WHEN
//         Rule rule = numeric.isLessThan(other);
//         // THEN – index 0 (1 < 2) is satisfied, index 2 (3 < 2) is not.
//         assertTrue(rule.isSatisfied(0), "At index 0 the close price 1 is less than 2");
//         assertFalse(rule.isSatisfied(2), "At index 2 the close price 3 is not less than 2");
//     }

//     @Test
//     void shouldThrowNullPointerExceptionWhenOtherIsNull() {
//         assertThrows(NullPointerException.class, () -> numericIndicator.isLessThan(null), "Passing null to isLessThan must throw NullPointerException");
//     }

//     @Test
//     void shouldThrowIllegalArgumentExceptionWhenBarSeriesMismatch() {
//         // Left indicator built on seriesA
//         BarSeries seriesA = mock(BarSeries.class);
//         Indicator<Num> left = new ClosePriceIndicator(seriesA);
//         NumericIndicator numeric = NumericIndicator.of(left);
//         // Right indicator built on a *different* seriesB
//         BarSeries seriesB = mock(BarSeries.class);
//         Indicator<Num> right = new ClosePriceIndicator(seriesB);
//         // UnderIndicatorRule validates that both indicators belong to the same series.
//         assertThrows(IllegalArgumentException.class, () -> numeric.isLessThan(right), "Mismatched BarSeries must cause IllegalArgumentException");
//     }
}