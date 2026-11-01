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
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.Rule;
import org.ta4j.core.mocks.MockBarSeries;

/**
 * Tests for {@link NumericIndicator#isGreaterThan(Indicator)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_isGreaterThan_Test {

    /** shared series used by all tests */
    private static BarSeries series;

    /** NumericIndicator that decorates the close‑price indicator */
    private static NumericIndicator numericClose;

    /** Constant indicator used as the “other” operand */
    private static ConstantIndicator<Num> constantThree;

    @BeforeAll
    static void setupBeforeAll() {
        // Simple numeric factory – DecimalNum is the default implementation in TA4J
        Function<Number, Num> numFactory = n -> DecimalNum.valueOf(n.doubleValue());

        // BarSeries with deterministic close‑price values 1,2,3,4,5
        series = new MockBarSeries(numFactory, 1, 2, 3, 4, 5);

        // ClosePriceIndicator (delegated by NumericIndicator)
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        numericClose = NumericIndicator.of(closePrice);

        // Constant indicator that always returns 3
        constantThree = new ConstantIndicator<>(series, series.numOf(3));
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialisation required at the moment
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release references (help GC in long test suites)
        series = null;
        numericClose = null;
        constantThree = null;
    }

    @Test
    void shouldReturnOverIndicatorRule() {
        Rule rule = numericClose.isGreaterThan(constantThree);
        assertNotNull(rule, "Rule must not be null");
        assertEquals(OverIndicatorRule.class, rule.getClass(),
                "isGreaterThan(Indicator) should create an OverIndicatorRule");
    }

    @Test
    void ruleShouldBeSatisfiedWhenNumericIsGreaterThanOther() {
        Rule rule = numericClose.isGreaterThan(constantThree);

        // Index 0 → close price = 1, constant = 3 → false
        assertFalse(rule.isSatisfied(0), "Rule must be false when 1 <= 3");

        // Index 2 → close price = 3, constant = 3 → false (not greater)
        assertFalse(rule.isSatisfied(2), "Rule must be false when values are equal");

        // Index 3 → close price = 4, constant = 3 → true
        assertTrue(rule.isSatisfied(3), "Rule must be true when 4 > 3");

        // Index 4 → close price = 5, constant = 3 → true
        assertTrue(rule.isSatisfied(4), "Rule must be true when 5 > 3");
    }
}
```