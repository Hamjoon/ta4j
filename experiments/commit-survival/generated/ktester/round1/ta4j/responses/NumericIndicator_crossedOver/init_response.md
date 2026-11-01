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
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

/**
 * Tests for {@link NumericIndicator#crossedOver(Indicator)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedOver_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by the tests. */
    private static BarSeries series;

    /** A simple price indicator that will be wrapped by {@link NumericIndicator}. */
    private static ClosePriceIndicator closePriceIndicator;

    /** The {@link NumericIndicator} under test. */
    private static NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = DecimalNum::valueOf;

        // Build a tiny mock series – the concrete values are irrelevant for the
        // crossedOver rule, we only need a series that can produce Num objects.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);

        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialisation required at the moment.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required at the moment.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (helps the GC when many tests are run).
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
        numFunction = null;
    }

    /**
     * Verify that {@code crossedOver(Indicator)} creates a {@link CrossedUpIndicatorRule}
     * that wraps the current {@link NumericIndicator} and the supplied indicator.
     */
    @Test
    void shouldCreateCrossedUpIndicatorRuleWithIndicatorArgument() {
        // GIVEN – an EMA indicator that will be used as the “other” indicator
        Indicator<Num> ema = new EMAIndicator(closePriceIndicator, 3);

        // WHEN – we ask the numeric indicator to create a crossed‑over rule
        Rule rule = numericIndicator.crossedOver(ema);

        // THEN – the rule must be a CrossedUpIndicatorRule
        assertTrue(rule instanceof CrossedUpIndicatorRule,
                "crossedOver(Indicator) should return a CrossedUpIndicatorRule");

        // Additional sanity check: the rule should reference the same left indicator
        // (the NumericIndicator itself) and the provided right indicator.
        // The concrete rule class exposes the two indicators via getters.
        CrossedUpIndicatorRule crossedRule = (CrossedUpIndicatorRule) rule;
        assertSame(numericIndicator, crossedRule.getLeftIndicator(),
                "Left indicator of the rule must be the NumericIndicator itself");
        assertSame(ema, crossedRule.getRightIndicator(),
                "Right indicator of the rule must be the indicator supplied to crossedOver");
    }

    /**
     * Verify that {@code crossedOver(Number)} (the overload that delegates to the
     * Indicator version) also yields a {@link CrossedUpIndicatorRule}.
     */
    @Test
    void shouldCreateCrossedUpIndicatorRuleWithNumberArgument() {
        // GIVEN – a concrete number that will be wrapped in a ConstantIndicator
        Number constant = 5;

        // WHEN – we call the overload that accepts a Number
        Rule rule = numericIndicator.crossedOver(constant);

        // THEN – the returned rule must be a CrossedUpIndicatorRule
        assertTrue(rule instanceof CrossedUpIndicatorRule,
                "crossedOver(Number) should return a CrossedUpIndicatorRule");

        // The rule's right indicator should be a ConstantIndicator wrapping the same number.
        CrossedUpIndicatorRule crossedRule = (CrossedUpIndicatorRule) rule;
        assertSame(numericIndicator, crossedRule.getLeftIndicator(),
                "Left indicator of the rule must be the NumericIndicator itself");
        assertTrue(crossedRule.getRightIndicator() instanceof ConstantIndicator,
                "Right indicator of the rule must be a ConstantIndicator when a Number is supplied");
        ConstantIndicator<Num> constantIndicator = (ConstantIndicator<Num>) crossedRule.getRightIndicator();
        assertEquals(numFunction.apply(constant), constantIndicator.getValue(0),
                "ConstantIndicator must contain the supplied number");
    }
}
```