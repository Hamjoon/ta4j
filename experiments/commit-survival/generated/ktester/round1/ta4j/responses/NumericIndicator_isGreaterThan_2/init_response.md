```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.Rule;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;

/**
 * Tests for {@link NumericIndicator#isGreaterThan(Number)} and the overload that
 * accepts an {@link Indicator<Num>}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_isGreaterThan_2_Test {

    private static BarSeries series;
    private ClosePriceIndicator closePrice;
    private NumericIndicator numeric;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple series: 1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1,0,-1,-2
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        numeric = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up for this test class
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release
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
}
```