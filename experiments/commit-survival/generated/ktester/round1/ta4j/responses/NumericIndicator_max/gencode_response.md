```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;
import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive tests for {@link NumericIndicator#max(Indicator)}.
 *
 * <p>The test suite covers:
 * <ul>
 *   <li>element‑wise maximum for constant indicators (identical, left larger,
 *       right larger, mixed)</li>
 *   <li>special values handling (NaN, infinities, negative numbers)</li>
 *   <li>null argument handling</li>
 *   <li>behaviour when the same instance is used as both operands</li>
 *   <li>commutativity of the operation</li>
 *   <li>validation that mismatched {@link BarSeries} cause an
 *       {@link IllegalArgumentException}</li>
 * </ul>
 * </p>
 *
 * <p>Mockito is used to mock {@link Indicator} instances where a lightweight
 * stub is sufficient; for constant series we also use the real
 * {@link ConstantIndicator} implementation.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_max_Test {

    /** Helper to create {@link Num} from primitive numbers. */
    private static final Function<Number, Num> NUM_FACTORY = DecimalNum::valueOf;

    /** Mocked BarSeries used for most tests (same series for both operands). */
    @Mock
    private BarSeries sameSeries;

    /** Mocked BarSeries used for the mismatched‑series test. */
    @Mock
    private BarSeries otherSeries;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up.
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                       */
    /* --------------------------------------------------------------------- */

    /**
     * Creates a mocked {@link Indicator} that returns the supplied {@code values}
     * for indexes {@code 0..values.length-1} and reports the given {@code series}
     * as its {@link Indicator#getBarSeries()}.
     */
    private static Indicator<Num> mockIndicator(BarSeries series, Num... values) {
        Indicator<Num> mock = mock(Indicator.class);
        when(mock.getBarSeries()).thenReturn(series);
        for (int i = 0; i < values.length; i++) {
            when(mock.getValue(i)).thenReturn(values[i]);
        }
        // Any out‑of‑range index returns NaN (the default behaviour of many
        // ta4j indicators). This keeps the mock simple.
        when(mock.getValue(anyInt())).thenAnswer(invocation -> {
            int idx = invocation.getArgument(0);
            return (idx >= 0 && idx < values.length) ? values[idx] : DecimalNum.NaN;
        });
        return mock;
    }

    /**
     * Convenience wrapper that creates a {@link ConstantIndicator} based on the
     * supplied {@code series} and a single constant {@code value}.
     */
    private static ConstantIndicator constant(BarSeries series, Num value) {
        return new ConstantIndicator(series, value);
    }

    /* --------------------------------------------------------------------- */
    /* Parameterised tests for the basic constant‑value scenarios            */
    /* --------------------------------------------------------------------- */

    /**
     * Data provider for the basic constant‑value scenarios.
     *
     * Each {@code Object[]} contains:
     * <ul>
     *   <li>values of the left (this) indicator</li>
     *   <li>values of the right (other) indicator</li>
     *   <li>expected element‑wise maximum</li>
     *   <li>description for readability</li>
     * </ul>
     */
    private static Stream<Arguments> constantScenarios() {
        return Stream.of(
                // identical constant values
                Arguments.of(
                        new double[]{5, 5, 5},
                        new double[]{5, 5, 5},
                        new double[]{5, 5, 5},
                        "identical constant values"
                ),
                // left indicator larger at all indices
                Arguments.of(
                        new double[]{10, 20, 30},
                        new double[]{1, 2, 3},
                        new double[]{10, 20, 30},
                        "left indicator dominates"
                ),
                // right indicator larger at all indices
                Arguments.of(
                        new double[]{10, 20, 30},
                        new double[]{100, 200, 300},
                        new double[]{100, 200, 300},
                        "right indicator dominates"
                ),
                // mixed dominance
                Arguments.of(
                        new double[]{10, 20, 30},
                        new double[]{5, 25, 15},
                        new double[]{10, 25, 30},
                        "mixed dominance"
                )
        );
    }

    @ParameterizedTest(name = "max constant scenario – {3}")
    @MethodSource("constantScenarios")
    void max_constantIndicators_returnsElementWiseMaximum(double[] leftVals,
                                                          double[] rightVals,
                                                          double[] expectedVals,
                                                          String description) {
        // Build mocked indicators for both sides (same BarSeries)
        Indicator<Num> left = mockIndicator(sameSeries,
                toNumArray(leftVals));
        Indicator<Num> right = mockIndicator(sameSeries,
                toNumArray(rightVals));

        NumericIndicator numericLeft = NumericIndicator.of(left);

        NumericIndicator result = numericLeft.max(right);

        assertNotNull(result, "Result must not be null");
        for (int i = 0; i < expectedVals.length; i++) {
            Num expected = NUM_FACTORY.apply(expectedVals[i]);
            assertEquals(expected, result.getValue(i),
                    () -> "index " + i + " – expected " + expected + " but got " + result.getValue(i));
        }
    }

    // ----------------------------------------------------------------------
    // Special‑value scenarios (NaN, infinities, negative numbers, etc.)
    // ----------------------------------------------------------------------

    @Test
    void max_withNaNInOther_propagatesNaNAccordingToNumSemantics() {
        // this indicator: [NaN, 0, 5]
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(Double.NaN),
                NUM_FACTORY.apply(0),
                NUM_FACTORY.apply(5));

        // other indicator: [1, 2, 4]
        Indicator<Num> right = mockIndicator(sameSeries,
                NUM_FACTORY.apply(1),
                NUM_FACTORY.apply(2),
                NUM_FACTORY.apply(4));

        NumericIndicator result = NumericIndicator.of(left).max(right);

        assertEquals(NUM_FACTORY.apply(Double.NaN), result.getValue(0),
                "NaN should propagate when present on the left side");
        assertEquals(NUM_FACTORY.apply(2), result.getValue(1));
        assertEquals(NUM_FACTORY.apply(5), result.getValue(2));
    }

    @Test
    void max_withNaNInThis_propagatesNaNAccordingToNumSemantics() {
        // this indicator: [NaN, 2, 4]
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(Double.NaN),
                NUM_FACTORY.apply(2),
                NUM_FACTORY.apply(4));

        // other indicator: [1, 2, 4]
        Indicator<Num> right = mockIndicator(sameSeries,
                NUM_FACTORY.apply(1),
                NUM_FACTORY.apply(2),
                NUM_FACTORY.apply(4));

        NumericIndicator result = NumericIndicator.of(left).max(right);

        assertEquals(NUM_FACTORY.apply(Double.NaN), result.getValue(0),
                "NaN should propagate when present on the left side");
        assertEquals(NUM_FACTORY.apply(2), result.getValue(1));
        assertEquals(NUM_FACTORY.apply(4), result.getValue(2));
    }

    @Test
    void max_withInfinities_andNegativeNumbers() {
        // this indicator: [-5, -5, -5]
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(-5),
                NUM_FACTORY.apply(-5),
                NUM_FACTORY.apply(-5));

        // other indicator: [100, POSITIVE_INFINITY, NEGATIVE_INFINITY]
        Indicator<Num> right = mockIndicator(sameSeries,
                NUM_FACTORY.apply(100),
                NUM_FACTORY.apply(Double.POSITIVE_INFINITY),
                NUM_FACTORY.apply(Double.NEGATIVE_INFINITY));

        NumericIndicator result = NumericIndicator.of(left).max(right);

        assertEquals(NUM_FACTORY.apply(100), result.getValue(0));
        assertEquals(NUM_FACTORY.apply(Double.POSITIVE_INFINITY), result.getValue(1));
        assertEquals(NUM_FACTORY.apply(-5), result.getValue(2));
    }

    @Test
    void max_withNegativeNumbers() {
        // this indicator: [-10, -3, 0]
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(-10),
                NUM_FACTORY.apply(-3),
                NUM_FACTORY.apply(0));

        // other indicator: [-20, -3, -1]
        Indicator<Num> right = mockIndicator(sameSeries,
                NUM_FACTORY.apply(-20),
                NUM_FACTORY.apply(-3),
                NUM_FACTORY.apply(-1));

        NumericIndicator result = NumericIndicator.of(left).max(right);

        assertEquals(NUM_FACTORY.apply(-10), result.getValue(0));
        assertEquals(NUM_FACTORY.apply(-3), result.getValue(1));
        assertEquals(NUM_FACTORY.apply(0), result.getValue(2));
    }

    @Test
    void max_withMixedNaNAndInfinities() {
        // this indicator: [NEGATIVE_INFINITY, 0, NEGATIVE_INFINITY]
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(Double.NEGATIVE_INFINITY),
                NUM_FACTORY.apply(0),
                NUM_FACTORY.apply(Double.NEGATIVE_INFINITY));

        // other indicator: [POSITIVE_INFINITY, 0, NaN]
        Indicator<Num> right = mockIndicator(sameSeries,
                NUM_FACTORY.apply(Double.POSITIVE_INFINITY),
                NUM_FACTORY.apply(0),
                NUM_FACTORY.apply(Double.NaN));

        NumericIndicator result = NumericIndicator.of(left).max(right);

        // According to Num semantics: NaN > any number, but max(NaN, x) = NaN
        assertEquals(NUM_FACTORY.apply(Double.NaN), result.getValue(0));
        assertEquals(NUM_FACTORY.apply(0), result.getValue(1));
        assertEquals(NUM_FACTORY.apply(Double.NaN), result.getValue(2));
    }

    // ----------------------------------------------------------------------
    // Edge cases: null argument, same instance, commutativity, different series
    // ----------------------------------------------------------------------

    @Test
    void max_nullArgument_throwsNullPointerException() {
        NumericIndicator left = NumericIndicator.of(mockIndicator(sameSeries,
                NUM_FACTORY.apply(1)));

        assertThrows(NullPointerException.class,
                () -> left.max(null),
                "Passing null should result in NPE");
    }

    @Test
    void max_sameInstance_returnsIdenticalValues() {
        // this indicator: [10, -20, 30]
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(10),
                NUM_FACTORY.apply(-20),
                NUM_FACTORY.apply(30));

        NumericIndicator numeric = NumericIndicator.of(left);

        NumericIndicator result = numeric.max(numeric); // same instance

        assertEquals(NUM_FACTORY.apply(10), result.getValue(0));
        assertEquals(NUM_FACTORY.apply(-20), result.getValue(1));
        assertEquals(NUM_FACTORY.apply(30), result.getValue(2));
    }

    @Test
    void max_commutativity_producesIdenticalResults() {
        // a: [5, 15, 25]
        Indicator<Num> a = mockIndicator(sameSeries,
                NUM_FACTORY.apply(5),
                NUM_FACTORY.apply(15),
                NUM_FACTORY.apply(25));

        // b: [10, 10, 30]
        Indicator<Num> b = mockIndicator(sameSeries,
                NUM_FACTORY.apply(10),
                NUM_FACTORY.apply(10),
                NUM_FACTORY.apply(30));

        NumericIndicator na = NumericIndicator.of(a);
        NumericIndicator nb = NumericIndicator.of(b);

        NumericIndicator maxAB = na.max(b);
        NumericIndicator maxBA = nb.max(a);

        for (int i = 0; i < 3; i++) {
            assertEquals(maxAB.getValue(i), maxBA.getValue(i),
                    "Commutative result differs at index " + i);
        }
    }

    @Test
    void max_differentBarSeries_throwsIllegalArgumentException() {
        // left indicator built on series A
        Indicator<Num> left = mockIndicator(sameSeries,
                NUM_FACTORY.apply(10),
                NUM_FACTORY.apply(20),
                NUM_FACTORY.apply(30));

        // right indicator built on a *different* series
        Indicator<Num> right = mockIndicator(otherSeries,
                NUM_FACTORY.apply(10),
                NUM_FACTORY.apply(20),
                NUM_FACTORY.apply(30));

        NumericIndicator numericLeft = NumericIndicator.of(left);

        assertThrows(IllegalArgumentException.class,
                () -> numericLeft.max(right),
                "Mismatched BarSeries should cause IllegalArgumentException");
    }

    // ----------------------------------------------------------------------
    // Utility conversion method
    // ----------------------------------------------------------------------
    private static Num[] toNumArray(double[] doubles) {
        Num[] nums = new Num[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            nums[i] = NUM_FACTORY.apply(doubles[i]);
        }
        return nums;
    }
}
```