```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ConstantIndicator;

import java.util.stream.Stream;

/**
 * Unit tests for {@link NumericIndicator#crossedUnder(Indicator)} and
 * {@link NumericIndicator#crossedUnder(Number)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedUnder_Test {

    /**
     * Mocked delegate for the {@link NumericIndicator} under test.
     */
    @Mock
    private Indicator<Num> delegateMock;

    /**
     * Mocked second indicator used as the “other” argument.
     */
    @Mock
    private Indicator<Num> otherMock;

    /**
     * Instance under test – created in each test to guarantee a fresh state.
     */
    private NumericIndicator numericIndicator;

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

    /**
     * Creates a {@link NumericIndicator} that returns the supplied integer
     * values (converted to {@link Num}) for each index.
     */
    private static NumericIndicator numericFromIntArray(int[] values) {
        Indicator<Num> raw = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                return Num.valueOf(values[index]);
            }

            @Override
            public BarSeries getBarSeries() {
                // Series is not used by the rule logic we test.
                return mock(BarSeries.class);
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        return NumericIndicator.of(raw);
    }

    /**
     * Creates a {@link NumericIndicator} that wraps a {@link ConstantIndicator}
     * with the given constant.
     */
    private static NumericIndicator constantNumeric(Number constant) {
        return NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(constant)));
    }

    private static Stream<Arguments> crossingScenarios() {
        return Stream.of(
                // this: [10, 8, 4]  other: constant 5  → cross under at index 2
                Arguments.of(new int[]{10, 8, 4}, constantNumeric(5), 2, true,
                        "crossed under at index 2 (previous > other, current < other)"),
                // this: [10, 8, 6]  other: constant 5  → never crosses under
                Arguments.of(new int[]{10, 8, 6}, constantNumeric(5), 2, false,
                        "no crossing – values stay above other"),
                // this: [7, 5, 5]   other: constant 5  → equal at index 2 → no crossing
                Arguments.of(new int[]{7, 5, 5}, constantNumeric(5), 2, false,
                        "equal values at index 2 do not count as crossing"),
                // this: [10, 8, 4]  other: custom [8,7,5] → cross under at index 2
                Arguments.of(new int[]{10, 8, 4}, numericFromIntArray(new int[]{8, 7, 5}), 2, true,
                        "custom other indicator, crossed under at index 2"),
                // this: [10, 8, 6]  other: custom [9,7,5] → never crosses under
                Arguments.of(new int[]{10, 8, 6}, numericFromIntArray(new int[]{9, 7, 5}), 2, false,
                        "custom other indicator, never crosses under"),
                // this: [5,5,4]    other: constant 5 → previous equal, then below → false
                Arguments.of(new int[]{5, 5, 4}, constantNumeric(5), 2, false,
                        "previous equal to other, then below → not a strict crossing"),
                // this: [-1,-3,-5] other: constant -2 → cross under at index 1
                Arguments.of(new int[]{-1, -3, -5}, constantNumeric(-2), 1, true,
                        "negative numbers, crossed under at index 1")
        );
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
        Number constant = 42;
        // Act
        Rule rule = numericIndicator.crossedUnder(constant);
        // Assert: correct rule type
        assertTrue(rule instanceof CrossedDownIndicatorRule,
                "crossedUnder(Number) should return a CrossedDownIndicatorRule");
        // The left side must be the NumericIndicator under test.
        CrossedDownIndicatorRule crossedRule = (CrossedDownIndicatorRule) rule;
        assertSame(numericIndicator, crossedRule.getLeft(),
                "Left side of the rule must be the NumericIndicator itself");
        // Right side should not be null
        assertNotNull(crossedRule.getRight(),
                "Right side (constant indicator) should be non‑null");
    }

    @Test
    void crossedUnder_nullIndicator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> numericIndicator.crossedUnder((Indicator<Num>) null),
                "crossedUnder(null) should throw NPE");
    }

    @Test
    void crossedUnder_nullNumber_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> numericIndicator.crossedUnder((Number) null),
                "crossedUnder(null) with Number should throw NPE");
    }

    @ParameterizedTest(name = "[{index}] {4}")
    @MethodSource("crossingScenarios")
    void crossedUnder_crossingLogic(int[] thisValues, NumericIndicator other,
                                    int indexToCheck, boolean expectedSatisfied,
                                    String description) {
        // Arrange
        NumericIndicator left = numericFromIntArray(thisValues);
        Rule rule = left.crossedUnder(other);
        // Act
        boolean satisfied = rule.isSatisfied(indexToCheck);
        // Assert
        assertEquals(expectedSatisfied, satisfied,
                "Rule satisfaction at index " + indexToCheck + " – " + description);
    }

    @Test
    void crossedUnder_firstIndexNeverSatisfied() {
        // this values: [4, 2, 1] (any values, we only care about index 0)
        NumericIndicator left = numericFromIntArray(new int[]{4, 2, 1});
        Rule rule = left.crossedUnder(constantNumeric(3));
        // The first index has no previous bar, therefore the rule must be false.
        assertFalse(rule.isSatisfied(0),
                "Rule must be false at index 0 (no previous value)");
    }

    @Test
    void crossedUnder_otherIndicatorThrowsException_onIsSatisfied() {
        // Indicator that throws IllegalStateException when getValue is called
        Indicator<Num> buggy = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                throw new IllegalStateException("forced failure");
            }

            @Override
            public BarSeries getBarSeries() {
                return mock(BarSeries.class);
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        NumericIndicator left = numericFromIntArray(new int[]{10, 8, 6});
        Rule rule = left.crossedUnder(NumericIndicator.of(buggy));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> rule.isSatisfied(1),
                "Exception from other indicator should propagate");
        assertEquals("forced failure", ex.getMessage());
    }

    @Test
    void crossedUnder_otherConstantIndicatorWithNullNum_throwsNPE() {
        // ConstantIndicator constructed with a null Num (simulated via subclass)
        ConstantIndicator<Num> nullConstant = new ConstantIndicator<>(null) {

            @Override
            public Num getValue(int index) {
                // force NPE later in rule evaluation
                return null;
            }
        };
        NumericIndicator left = numericFromIntArray(new int[]{10, 8, 6});
        Rule rule = left.crossedUnder(NumericIndicator.of(nullConstant));
        assertThrows(NullPointerException.class,
                () -> rule.isSatisfied(1),
                "Null Num inside constant indicator should cause NPE during evaluation");
    }

    @Test
    void crossedUnder_otherIndicatorShorterThanLeft_throwsIndexOutOfBounds() {
        // Other indicator has only two values, we will query index 2
        Indicator<Num> shortOther = new Indicator<Num>() {

            private final Num[] vals = {Num.valueOf(5), Num.valueOf(6)};

            @Override
            public Num getValue(int index) {
                // will throw ArrayIndexOutOfBoundsException for index 2
                return vals[index];
            }

            @Override
            public BarSeries getBarSeries() {
                return mock(BarSeries.class);
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        NumericIndicator left = numericFromIntArray(new int[]{10, 8, 6});
        Rule rule = left.crossedUnder(NumericIndicator.of(shortOther));
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> rule.isSatisfied(2),
                "Accessing beyond the other indicator's size should throw");
    }

    @Test
    void crossedUnder_otherIndicatorWithNullSeries_throwsNPE() {
        // Indicator that returns null BarSeries
        Indicator<Num> nullSeries = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                return Num.valueOf(1);
            }

            @Override
            public BarSeries getBarSeries() {
                // will cause NPE inside CrossedDownIndicatorRule
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        NumericIndicator left = numericFromIntArray(new int[]{10, 8, 6});
        Rule rule = left.crossedUnder(NumericIndicator.of(nullSeries));
        assertThrows(NullPointerException.class,
                () -> rule.isSatisfied(1),
                "Null BarSeries in other indicator should cause NPE during evaluation");
    }

    @Test
    void crossedUnder_numericIndicatorCreatedWithNullDelegate_throwsNPE() {
        // NumericIndicator.of(null) should produce a NumericIndicator whose delegate
        // is null; any rule creation that accesses the delegate will NPE.
        NumericIndicator nullDelegate = NumericIndicator.of(null);
        NumericIndicator other = constantNumeric(5);
        assertThrows(NullPointerException.class,
                () -> nullDelegate.crossedUnder(other),
                "Creating a rule with a NumericIndicator that has a null delegate must throw NPE");
    }
}
```