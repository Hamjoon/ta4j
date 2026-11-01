```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Tests for {@link NumericIndicator#isLessThan(Indicator)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_isLessThan_Test {

    @Mock
    private Indicator<Num> leftIndicator;   // delegate of the NumericIndicator under test

    @Mock
    private Indicator<Num> rightIndicator;  // indicator passed to isLessThan

    @Mock
    private BarSeries barSeries;            // required for some Indicator implementations (not used directly)

    private NumericIndicator numericIndicator; // the instance under test

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the NumericIndicator with the mocked left indicator
        numericIndicator = NumericIndicator.of(leftIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks after each test to avoid interaction leakage
        Mockito.reset(leftIndicator, rightIndicator, barSeries);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    @Test
    void shouldReturnUnderIndicatorRuleAndEvaluateTrueWhenLeftIsLessThanRight() {
        // GIVEN
        int index = 0;
        Num leftValue = DecimalNum.valueOf(1);
        Num rightValue = DecimalNum.valueOf(5);
        when(leftIndicator.getValue(index)).thenReturn(leftValue);
        when(rightIndicator.getValue(index)).thenReturn(rightValue);

        // WHEN
        Rule rule = numericIndicator.isLessThan(rightIndicator);

        // THEN
        assertTrue(rule instanceof UnderIndicatorRule, "Rule should be an instance of UnderIndicatorRule");
        assertTrue(rule.isSatisfied(index), "Rule should be satisfied when left < right");
    }

    @Test
    void shouldReturnUnderIndicatorRuleAndEvaluateFalseWhenLeftIsNotLessThanRight() {
        // GIVEN
        int index = 3;
        Num leftValue = DecimalNum.valueOf(10);
        Num rightValue = DecimalNum.valueOf(4);
        when(leftIndicator.getValue(index)).thenReturn(leftValue);
        when(rightIndicator.getValue(index)).thenReturn(rightValue);

        // WHEN
        Rule rule = numericIndicator.isLessThan(rightIndicator);

        // THEN
        assertTrue(rule instanceof UnderIndicatorRule, "Rule should be an instance of UnderIndicatorRule");
        assertFalse(rule.isSatisfied(index), "Rule should not be satisfied when left >= right");
    }

    @Test
    void shouldCreateRuleWithCorrectIndicators() {
        // GIVEN
        // No additional stubbing needed; we only verify that the rule holds the same
        // references as provided to isLessThan.

        // WHEN
        Rule rule = numericIndicator.isLessThan(rightIndicator);

        // THEN
        assertTrue(rule instanceof UnderIndicatorRule);
        UnderIndicatorRule underRule = (UnderIndicatorRule) rule;
        // The rule should keep the exact indicator instances we passed.
        assertSame(numericIndicator, underRule.getLeftIndicator(),
                "Left indicator of the rule should be the NumericIndicator itself");
        assertSame(rightIndicator, underRule.getRightIndicator(),
                "Right indicator of the rule should be the indicator supplied to isLessThan");
    }
}
```