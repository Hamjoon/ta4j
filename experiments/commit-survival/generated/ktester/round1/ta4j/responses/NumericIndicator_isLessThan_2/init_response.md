```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.rules.Rule;

/**
 * Tests for {@link NumericIndicator#isLessThan(Number)}.
 *
 * <p>The test verifies that the method returns an {@link UnderIndicatorRule}
 * instance and that the rule evaluates correctly against a concrete
 * {@link BarSeries}.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_isLessThan_2_Test {

    /** Series used for all tests – values 1 … 9 then decreasing back to -2 */
    private static BarSeries series;
    /** Indicator that simply returns the close price of the series */
    private static ClosePriceIndicator closePriceIndicator;
    /** The NumericIndicator under test */
    private static NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Build a deterministic series using DecimalNum as the Num implementation
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(
                numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialisation required at the moment
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required at the moment
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (helpful for large test suites)
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
    }

    @Test
    void shouldReturnUnderIndicatorRule() {
        Rule rule = numericIndicator.isLessThan(5);
        assertNotNull(rule, "The rule must not be null");
        assertEquals(UnderIndicatorRule.class, rule.getClass(),
                "isLessThan(Number) should create an UnderIndicatorRule");
    }

    @Test
    void ruleShouldEvaluateCorrectly() {
        // The constant used inside the rule is 5 (as a Num)
        Rule rule = numericIndicator.isLessThan(5);
        assertTrue(rule.isSatisfied(0), "Value 1 < 5 → rule should be satisfied at index 0");
        assertTrue(rule.isSatisfied(3), "Value 4 < 5 → rule should be satisfied at index 3");
        assertFalse(rule.isSatisfied(5), "Value 6 > 5 → rule should NOT be satisfied at index 5");
        assertFalse(rule.isSatisfied(8), "Value 9 > 5 → rule should NOT be satisfied at index 8");
        // Edge case: exactly 5 – UnderIndicatorRule is strict (<), so it must be false
        assertFalse(rule.isSatisfied(4), "Value 5 == 5 → rule should NOT be satisfied at index 4");
    }

    @Test
    void ruleWorksWithNegativeNumbers() {
        Rule rule = numericIndicator.isLessThan(-1);
        // Index where value is -2 (last index)
        assertTrue(rule.isSatisfied(19), "Value -2 < -1 → rule should be satisfied at last index");
        // Index where value is -1 (second‑last index) – not satisfied because it's equal
        assertFalse(rule.isSatisfied(18), "Value -1 == -1 → rule should NOT be satisfied at index 18");
        // Positive values should never satisfy a rule with a negative threshold
        assertFalse(rule.isSatisfied(0), "Positive value should not satisfy a negative threshold");
    }

    @Test
    void ruleIsInstanceOfUnderIndicatorRuleWhenUsingIndicatorArgument() {
        // Use the same indicator as the right‑hand side to ensure the overload works
        Rule rule = numericIndicator.isLessThan(closePriceIndicator);
        assertEquals(UnderIndicatorRule.class, rule.getClass(),
                "isLessThan(Indicator) should also create an UnderIndicatorRule");
    }
}
```