```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
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
 * Comprehensive tests for {@link NumericIndicator#crossedOver(Indicator)}.
 *
 * The test cases are derived from the JSON specification provided in the
 * assignment. They cover:
 * <ul>
 *   <li>basic behaviour and argument validation,</li>
 *   <li>different crossing scenarios,</li>
 *   <li>edge cases such as NaN, null values, mismatched series, and exceptions
 *       thrown by the supplied indicator.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_crossedOver_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by the tests. */
    private static BarSeries series;

    /** A simple price indicator that will be wrapped by {@link NumericIndicator}. */
    private static ClosePriceIndicator closePriceIndicator;

    /** The {@link NumericIndicator} under test – by default wraps the close price. */
    private static NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = DecimalNum::valueOf;

        // Build a tiny mock series – the concrete values are irrelevant for most
        // tests; we only need a series that can produce Num objects.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);

        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
        numFunction = null;
    }

    /* --------------------------------------------------------------------- */
    /* Helper indicator implementations                                        */
    /* --------------------------------------------------------------------- */

    /**
     * Simple indicator that returns a predefined array of Num values.
     */
    private static class ArrayIndicator implements Indicator<Num> {
        private final BarSeries series;
        private final Num[] values;

        ArrayIndicator(BarSeries series, Num... values) {
            this.series = series;
            this.values = values;
        }

        @Override
        public Num getValue(int index) {
            if (index < 0 || index >= values.length) {
                // out‑of‑range -> return NaN (behaviour of many built‑in indicators)
                return DecimalNum.NaN;
            }
            return values[index];
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public String toString() {
            return "ArrayIndicator";
        }
    }

    /**
     * Indicator that shifts another indicator by a given number of bars.
     * If the shifted index is out of bounds, {@code null} is returned.
     */
    private static class ShiftedIndicator implements Indicator<Num> {
        private final Indicator<Num> base;
        private final int shift;

        ShiftedIndicator(Indicator<Num> base, int shift) {
            this.base = base;
            this.shift = shift;
        }

        @Override
        public Num getValue(int index) {
            int shiftedIdx = index - shift;
            if (shiftedIdx < 0) {
                return null; // undefined value before the start of the series
            }
            return base.getValue(shiftedIdx);
        }

        @Override
        public BarSeries getBarSeries() {
            return base.getBarSeries();
        }

        @Override
        public int getUnstableBars() {
            return base.getUnstableBars();
        }
    }

    /**
     * Indicator whose {@code getValue} always throws a {@link RuntimeException}.
     */
    private static class ThrowingIndicator implements Indicator<Num> {
        private final BarSeries series;

        ThrowingIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            throw new RuntimeException("test");
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /**
     * Indicator that returns {@code null} for any index (invalid Num value).
     */
    private static class NullValueIndicator implements Indicator<Num> {
        private final BarSeries series;

        NullValueIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            return null;
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test groups – one method per logical group (as required)              */
    /* --------------------------------------------------------------------- */

    /** ------------------------------------------------------------------- *
     *  Group: crossedOver(Indicator<Num>) - basic functionality            *
     * -------------------------------------------------------------------- */
    @Test
    void shouldThrowNPEWhenOtherIndicatorIsNull() {
        // GIVEN
        Indicator<Num> nullIndicator = null;

        // WHEN / THEN
        assertThrows(NullPointerException.class,
                () -> numericIndicator.crossedOver(nullIndicator),
                "Passing a null Indicator should throw NullPointerException");
    }

    @Test
    void constantBothSameValue_NoCrossingAtIndexZero() {
        // NumericIndicator constant 5
        NumericIndicator constFive = NumericIndicator.of(
                new ConstantIndicator<>(series, numFunction.apply(5)));

        // other = constant 5
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(5));

        Rule rule = constFive.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(0),
                "When both indicators are constant 5 there is no upward crossing at index 0");
    }

    @Test
    void crossingFromBelowToAbove_ShouldBeSatisfied() {
        // NumericIndicator values [2, 4]
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(2),
                        numFunction.apply(4)));

        // other = constant 3
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(3));

        Rule rule = custom.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertTrue(rule.isSatisfied(1),
                "Crossing from 2 (below 3) to 4 (above 3) at index 1 must be satisfied");
    }

    @Test
    void neverCrossesAboveConstant_ShouldBeUnsatisfied() {
        // NumericIndicator values [6,5,4]
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(6),
                        numFunction.apply(5),
                        numFunction.apply(4)));

        // other = constant 7
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(7));

        Rule rule = custom.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(2),
                "NumericIndicator never rises above 7, rule must be unsatisfied at index 2");
    }

    @Test
    void sameInstanceAsOther_ShouldNeverBeSatisfied() {
        // other = same instance as numericIndicator
        Rule rule = numericIndicator.crossedOver(numericIndicator);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // any index – we pick 5
        assertFalse(rule.isSatisfied(5),
                "Crossing with the same instance cannot happen, rule must be false");
    }

    /** ------------------------------------------------------------------- *
     *  Group: crossedOver(Indicator<Num>) - multiple upward crossings    *
     * -------------------------------------------------------------------- */
    @Test
    void multipleCrossingsScenario() {
        // NumericIndicator values [3,5,2,6]
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(3),
                        numFunction.apply(5),
                        numFunction.apply(2),
                        numFunction.apply(6)));

        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(4));

        Rule rule = custom.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);

        // index 1 – crossing 3→5 (below→above)
        assertTrue(rule.isSatisfied(1), "Crossing at index 1 must be satisfied");

        // index 2 – 5→2 (above→below) – not a crossing up
        assertFalse(rule.isSatisfied(2), "No crossing up at index 2");

        // index 3 – 2→6 (below→above) – crossing up again
        assertTrue(rule.isSatisfied(3), "Crossing at index 3 must be satisfied");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator decreasing while this stays constant        *
     * -------------------------------------------------------------------- */
    @Test
    void constantIndicatorVsDecreasingOther_NoCrossing() {
        // NumericIndicator constant 5
        NumericIndicator constantFive = NumericIndicator.of(
                new ConstantIndicator<>(series, numFunction.apply(5)));

        // other = decreasing 6,5,4
        Indicator<Num> decreasing = new ArrayIndicator(series,
                numFunction.apply(6),
                numFunction.apply(5),
                numFunction.apply(4));

        Rule rule = constantFive.crossedOver(decreasing);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(0), "No crossing at index 0");
        assertFalse(rule.isSatisfied(2), "No crossing at index 2");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator contains NaN (invalid numeric value)          *
     * -------------------------------------------------------------------- */
    @Test
    void otherIndicatorReturnsNaN_ShouldThrowIllegalArgumentException() {
        // NumericIndicator constant 5
        NumericIndicator constantFive = NumericIndicator.of(
                new ConstantIndicator<>(series, numFunction.apply(5)));

        // other = constant NaN
        Indicator<Num> nanIndicator = new ConstantIndicator<>(series, DecimalNum.NaN);

        Rule rule = constantFive.crossedOver(nanIndicator);
        assertTrue(rule instanceof CrossedUpIndicatorRule);

        // Evaluation should fail with IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> rule.isSatisfied(0),
                "Rule evaluation with NaN should throw IllegalArgumentException");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator with same delegate but distinct instance       *
     * -------------------------------------------------------------------- */
    @Test
    void sameDelegateDifferentInstances_NoCrossing() {
        // Both wrap the same ClosePriceIndicator (the default series)
        NumericIndicator first = NumericIndicator.of(closePriceIndicator);
        NumericIndicator second = NumericIndicator.of(closePriceIndicator); // distinct wrapper

        Rule rule = first.crossedOver(second);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // any index – choose 3
        assertFalse(rule.isSatisfied(3),
                "Identical values from distinct wrappers cannot produce an upward crossing");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator lagging by one bar (offset series)            *
     * -------------------------------------------------------------------- */
    @Test
    void shiftedIndicator_FirstIndexCrossing() {
        // NumericIndicator values [1,2,3] – we can model this with a ConstantIndicator(1)
        // and then add 1 each bar using plus, but for simplicity we create an array.
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(1),
                        numFunction.apply(2),
                        numFunction.apply(3)));

        // other = same values shifted right by 1 (i.e., undefined at index 0)
        Indicator<Num> shifted = new ShiftedIndicator(custom, 1);

        Rule rule = custom.crossedOver(shifted);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // At index 0, shifted returns null (treated as "below"), numericIndicator goes from undefined to 1 => crossing up
        assertTrue(rule.isSatisfied(0), "Crossing at index 0 should be satisfied because other is undefined");
        // At index 1, both are 1 → no crossing
        assertFalse(rule.isSatisfied(1), "No crossing at index 1 because both values are equal");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator with mismatched BarSeries                     *
     * -------------------------------------------------------------------- */
    @Test
    void mismatchedSeries_ShouldThrowIllegalArgumentException() {
        // Build a second series with the same Num factory but different data
        BarSeries otherSeries = new MockBarSeries(numFunction, 10, 20, 30);

        Indicator<Num> other = new ConstantIndicator<>(otherSeries, numFunction.apply(5));

        // Construction of CrossedUpIndicatorRule validates series compatibility
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.crossedOver(other),
                "CrossedUpIndicatorRule must reject indicators from a different BarSeries");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator throws on getValue                             *
     * -------------------------------------------------------------------- */
    @Test
    void otherIndicatorThrowsOnGetValue_PropagatesException() {
        Indicator<Num> throwing = new ThrowingIndicator(series);
        Rule rule = numericIndicator.crossedOver(throwing);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rule.isSatisfied(0));
        assertEquals("test", ex.getMessage(),
                "Exception from the other indicator should propagate unchanged");
    }

    /** ------------------------------------------------------------------- *
     *  Group: other indicator returns null Num                                 *
     * -------------------------------------------------------------------- */
    @Test
    void otherIndicatorReturnsNullNum_ShouldThrowIllegalArgumentException() {
        Indicator<Num> nullVal = new NullValueIndicator(series);
        Rule rule = numericIndicator.crossedOver(nullVal);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertThrows(IllegalArgumentException.class,
                () -> rule.isSatisfied(0),
                "Rule evaluation with a null Num should throw IllegalArgumentException");
    }
}
```