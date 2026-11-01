```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

/**
 * Tests for {@link NumericIndicator#crossedOver(Number)} (and the overload that
 * accepts an {@link org.ta4j.core.Indicator}).
 *
 * The tests focus on:
 * <ul>
 *   <li>the returned {@link Rule} type (should be {@link CrossedUpIndicatorRule})</li>
 *   <li>the logical behaviour of the rule when the underlying series actually
 *       crosses the constant value</li>
 * </ul>
 *
 * No private members of {@link NumericIndicator} or its dependencies are accessed.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedOver_2_Test {

    /** Simple conversion from {@link Number} to {@link Num}. */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private EMAIndicator ema;
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Series: 4, 6 – this creates a clear crossing of the constant 5
        // (below 5 at index 0, above 5 at index 1).
        series = new MockBarSeries(NUM_FUNCTION, 4, 6);
        closePrice = new ClosePriceIndicator(series);
        // EMA with a short period to have a deterministic value for the second test.
        ema = new EMAIndicator(closePrice, 2);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up; objects are GC‑ed.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required.
    }

    @Test
    void crossedOverWithConstantNumber() {
        // Act
        Rule rule = numericIndicator.crossedOver(5);

        // Assert rule type
        assertTrue(rule instanceof CrossedUpIndicatorRule,
                "crossedOver(Number) should return a CrossedUpIndicatorRule");

        // The series goes from 4 (below 5) to 6 (above 5). The crossing happens
        // between index 0 and 1, therefore the rule must be satisfied at index 1.
        assertFalse(rule.isSatisfied(0), "Rule must not be satisfied before the crossing");
        assertTrue(rule.isSatisfied(1), "Rule must be satisfied when the value crosses above 5");
    }

    @Test
    void crossedOverWithIndicator() {
        // Act
        Rule rule = numericIndicator.crossedOver(ema);

        // Assert rule type – we do not evaluate the rule's logic here because the EMA
        // value depends on the EMA calculation; the purpose of this test is to verify
        // the correct overload and returned type.
        assertTrue(rule instanceof CrossedUpIndicatorRule,
                "crossedOver(Indicator) should return a CrossedUpIndicatorRule");
    }
}
```