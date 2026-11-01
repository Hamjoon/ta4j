```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;

/**
 * Tests for {@link NumericIndicator#crossedUnder(Indicator)} and the overload
 * that accepts a {@link Number}.
 *
 * The tests focus on the public contract: the method must return a
 * {@link CrossedDownIndicatorRule} whose left hand side is the {@link NumericIndicator}
 * itself and whose right hand side is the supplied indicator (or a constant
 * indicator when a {@link Number} is given). No private fields or methods are
 * accessed directly.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedUnder_Test {

    /** Mocked delegate for the {@link NumericIndicator} under test. */
    @Mock
    private Indicator<Num> delegateMock;

    /** Mocked second indicator used as the “other” argument. */
    @Mock
    private Indicator<Num> otherMock;

    /** Instance under test – created in each test to guarantee a fresh state. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Create a fresh NumericIndicator that decorates the mocked delegate.
        numericIndicator = NumericIndicator.of(delegateMock);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks to avoid cross‑test interference.
        reset(delegateMock, otherMock);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup needed.
    }

    @Test
    void crossedUnder_withIndicator_returnsCrossedDownIndicatorRule() {
        // Act
        Rule rule = numericIndicator.crossedUnder(otherMock);

        // Assert: correct rule type
        assertTrue(rule instanceof CrossedDownIndicatorRule,
                "crossedUnder(Indicator) should return a CrossedDownIndicatorRule");

        // Assert: the rule contains the expected left and right indicators
        CrossedDownIndicatorRule crossedRule = (CrossedDownIndicatorRule) rule;
        assertSame(numericIndicator, crossedRule.getLeft(),
                "Left side of the rule must be the NumericIndicator itself");
        assertSame(otherMock, crossedRule.getRight(),
                "Right side of the rule must be the indicator supplied to crossedUnder");
    }

    @Test
    void crossedUnder_withNumber_returnsCrossedDownIndicatorRule() {
        // Arrange
        Number constant = 42; // any arbitrary number

        // Act
        Rule rule = numericIndicator.crossedUnder(constant);

        // Assert: correct rule type
        assertTrue(rule instanceof CrossedDownIndicatorRule,
                "crossedUnder(Number) should return a CrossedDownIndicatorRule");

        // The right side is a ConstantIndicator created internally; we cannot
        // access it directly, but we can verify that the left side is the
        // NumericIndicator under test.
        CrossedDownIndicatorRule crossedRule = (CrossedDownIndicatorRule) rule;
        assertSame(numericIndicator, crossedRule.getLeft(),
                "Left side of the rule must be the NumericIndicator itself");
        // Right side should not be null
        assertNotNull(crossedRule.getRight(),
                "Right side (constant indicator) should be non‑null");
    }

    @Test
    void crossedUnder_nullIndicator_throwsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> numericIndicator.crossedUnder((Indicator<Num>) null),
                "crossedUnder(null) should throw NPE");
    }

    @Test
    void crossedUnder_nullNumber_throwsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> numericIndicator.crossedUnder((Number) null),
                "crossedUnder(null) with Number should throw NPE");
    }
}
```