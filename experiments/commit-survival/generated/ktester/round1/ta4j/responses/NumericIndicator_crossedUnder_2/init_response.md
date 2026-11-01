```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

/**
 * Tests for {@link NumericIndicator#crossedUnder(Number)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedUnder_2_Test {

    /** series used by all tests – created once for speed */
    private static BarSeries series;
    /** close‑price indicator based on the series */
    private static ClosePriceIndicator closePrice;
    /** EMA indicator (barCount = 3) used for the dynamic overload */
    private static EMAIndicator ema;

    /** the NumericIndicator instance under test */
    private NumericIndicator numericIndicator;

    /** Helper to create {@link Num} objects from primitive numbers */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    @BeforeAll
    static void setupBeforeAll() {
        // values are the same as in the original NumericIndicatorTest
        series = new MockBarSeries(NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // no resources to release – method kept for symmetry with the template
    }

    @AfterAll
    static void teardownAfterAll() {
        // nothing to clean up globally
    }

    @Test
    void crossedUnderWithConstantNumber_returnsCrossedDownRule() {
        Rule rule = numericIndicator.crossedUnder(5);
        assertNotNull(rule, "Rule must not be null");
        assertEquals(CrossedDownIndicatorRule.class, rule.getClass(),
                "crossedUnder(Number) should create a CrossedDownIndicatorRule");
    }

    @Test
    void crossedUnderWithIndicator_returnsCrossedDownRule() {
        Rule rule = numericIndicator.crossedUnder(ema);
        assertNotNull(rule, "Rule must not be null");
        assertEquals(CrossedDownIndicatorRule.class, rule.getClass(),
                "crossedUnder(Indicator) should create a CrossedDownIndicatorRule");
    }

    @Test
    void crossedUnderConstantRule_behaviourIsCorrect() {
        // constant 5 – the series crosses under 5 at index 12 (value 5, previous 6)
        Rule rule = numericIndicator.crossedUnder(5);

        // before the crossing – should be false
        assertFalse(rule.isSatisfied(11), "Rule must be false before the crossing");

        // at the crossing point – should be true
        assertTrue(rule.isSatisfied(12), "Rule must be true at the crossing index");

        // after the crossing – should be false again
        assertFalse(rule.isSatisfied(13), "Rule must be false after the crossing");
    }

    @Test
    void crossedUnderDynamicRule_behaviourIsReasonable() {
        // Use the EMA indicator as the second operand.
        Rule rule = numericIndicator.crossedUnder(ema);

        // Find an index where the close price goes from being above the EMA
        // to being below it. With the chosen data the first such occurrence
        // is at index 12 (close = 5, EMA ≈ 6.0) – previous index 11 has close = 6.
        assertFalse(rule.isSatisfied(11), "EMA rule should be false before crossing");
        assertTrue(rule.isSatisfied(12), "EMA rule should be true at the crossing index");
        // After the crossing the condition is no longer a crossing
        assertFalse(rule.isSatisfied(13), "EMA rule should be false after crossing");
    }
}
```