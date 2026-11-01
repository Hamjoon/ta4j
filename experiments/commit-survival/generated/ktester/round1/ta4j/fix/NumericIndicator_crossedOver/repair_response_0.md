```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.indicators.numeric.NumericIndicator;

/**
 * Unit tests for {@link NumericIndicator#crossedOver(Indicator)} and its overloads.
 *
 * The original test suite used JUnit 5 and several helper classes that are not
 * available in this execution environment.  The test has been rewritten to
 * depend only on JUnit 4 and on core Ta4j classes that are present in the
 * class‑path.
 */
public class NumericIndicator_crossedOver_Test {

    /** Function used to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by the tests. */
    private static BarSeries series;

    /** A simple price indicator that will be wrapped by {@link NumericIndicator}. */
    private static ClosePriceIndicator closePriceIndicator;

    /** The {@link NumericIndicator} under test. */
    private static NumericIndicator numericIndicator;

    /** Helper to build a tiny series whose close price equals the supplied numbers. */
    private static BarSeries createSeries(Number... values) {
        BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder().withName("mock");
        BaseBarSeries s = builder.build();
        for (int i = 0; i < values.length; i++) {
            double v = values[i].doubleValue();
            // use the same value for open, high, low and close – only close is relevant
            s.addBar(new BaseBar(
                    ZonedDateTime.now().plusSeconds(i),
                    v, v, v, v,
                    0));
        }
        return s;
    }

    @BeforeClass
    public static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = DecimalNum::valueOf;
        // Build a tiny series – the concrete values are irrelevant for the
        // crossedOver rule, we only need a series that can produce Num objects.
        series = createSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterClass
    public static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
        numFunction = null;
    }

    /** Simple indicator that returns a predefined array of Num values. */
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

    /** Indicator that shifts another indicator by a given number of bars. */
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
                // undefined value before the start of the series
                return null;
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

    /** Indicator whose {@code getValue} always throws a {@link RuntimeException}. */
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

    /** Indicator that returns {@code null} for any index (invalid Num value). */
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

    /** Helper to emulate JUnit‑5's {@code assertThrows} using JUnit‑4. */
    private static <T extends Throwable> void assertThrows(Class<T> expected, Runnable executable) {
        try {
            executable.run();
            fail("Expected exception of type " + expected.getName());
        } catch (Throwable actual) {
            if (!expected.isInstance(actual)) {
                fail("Unexpected exception type, expected " + expected.getName()
                        + " but got " + actual.getClass().getName());
            }
        }
    }

    /* --------------------------------------------------------------------- *
     *  Tests for crossedOver(Indicator) – basic functionality
     * --------------------------------------------------------------------- */

    @Test
    public void shouldCreateCrossedUpIndicatorRuleWithIndicatorArgument() {
        // GIVEN – an EMA indicator that will be used as the “other” indicator
        Indicator<Num> ema = new EMAIndicator(closePriceIndicator, 3);
        // WHEN – we ask the numeric indicator to create a crossed‑over rule
        Rule rule = numericIndicator.crossedOver(ema);
        // THEN – the rule must be a CrossedUpIndicatorRule
        assertTrue(rule instanceof CrossedUpIndicatorRule);
    }

    @Test
    public void shouldCreateCrossedUpIndicatorRuleWithNumberArgument() {
        // GIVEN – a concrete number that will be wrapped in a ConstantIndicator
        Number constant = 5;
        // WHEN – we call the overload that accepts a Number
        Rule rule = numericIndicator.crossedOver(constant);
        // THEN – the returned rule must be a CrossedUpIndicatorRule
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        // The right indicator should be a ConstantIndicator containing the same value
        CrossedUpIndicatorRule crossedRule = (CrossedUpIndicatorRule) rule;
        assertTrue(crossedRule.getRightIndicator() instanceof ConstantIndicator);
        ConstantIndicator<Num> ci = (ConstantIndicator<Num>) crossedRule.getRightIndicator();
        assertEquals(numFunction.apply(constant), ci.getValue(0));
    }

    @Test
    public void shouldThrowNPEWhenOtherIndicatorIsNull() {
        Indicator<Num> nullIndicator = null;
        assertThrows(NullPointerException.class,
                () -> numericIndicator.crossedOver(nullIndicator));
    }

    @Test
    public void constantBothSameValue_NoCrossingAtIndexZero() {
        NumericIndicator constFive = NumericIndicator.of(
                new ConstantIndicator<>(series, numFunction.apply(5)));
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(5));
        Rule rule = constFive.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(0));
    }

    @Test
    public void crossingFromBelowToAbove_ShouldBeSatisfied() {
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(2), numFunction.apply(4)));
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(3));
        Rule rule = custom.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertTrue(rule.isSatisfied(1));
    }

    @Test
    public void neverCrossesAboveConstant_ShouldBeUnsatisfied() {
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(6), numFunction.apply(5), numFunction.apply(4)));
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(7));
        Rule rule = custom.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(2));
    }

    @Test
    public void sameInstanceAsOther_ShouldNeverBeSatisfied() {
        Rule rule = numericIndicator.crossedOver(numericIndicator);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(5));
    }

    /* --------------------------------------------------------------------- *
     *  Multiple upward crossings
     * --------------------------------------------------------------------- */

    @Test
    public void multipleCrossingsScenario() {
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(3), numFunction.apply(5),
                        numFunction.apply(2), numFunction.apply(6)));
        Indicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(4));
        Rule rule = custom.crossedOver(other);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertTrue(rule.isSatisfied(1));
        assertFalse(rule.isSatisfied(2));
        assertTrue(rule.isSatisfied(3));
    }

    /* --------------------------------------------------------------------- *
     *  Constant indicator vs decreasing other
     * --------------------------------------------------------------------- */

    @Test
    public void constantIndicatorVsDecreasingOther_NoCrossing() {
        NumericIndicator constantFive = NumericIndicator.of(
                new ConstantIndicator<>(series, numFunction.apply(5)));
        Indicator<Num> decreasing = new ArrayIndicator(series,
                numFunction.apply(6), numFunction.apply(5), numFunction.apply(4));
        Rule rule = constantFive.crossedOver(decreasing);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(2));
    }

    /* --------------------------------------------------------------------- *
     *  Other indicator contains NaN
     * --------------------------------------------------------------------- */

    @Test
    public void otherIndicatorReturnsNaN_ShouldThrowIllegalArgumentException() {
        NumericIndicator constantFive = NumericIndicator.of(
                new ConstantIndicator<>(series, numFunction.apply(5)));
        Indicator<Num> nanIndicator = new ConstantIndicator<>(series, DecimalNum.NaN);
        Rule rule = constantFive.crossedOver(nanIndicator);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertThrows(IllegalArgumentException.class, () -> rule.isSatisfied(0));
    }

    /* --------------------------------------------------------------------- *
     *  Same delegate different instances
     * --------------------------------------------------------------------- */

    @Test
    public void sameDelegateDifferentInstances_NoCrossing() {
        NumericIndicator first = NumericIndicator.of(closePriceIndicator);
        NumericIndicator second = NumericIndicator.of(closePriceIndicator);
        Rule rule = first.crossedOver(second);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertFalse(rule.isSatisfied(3));
    }

    /* --------------------------------------------------------------------- *
     *  Other indicator lagging by one bar (offset series)
     * --------------------------------------------------------------------- */

    @Test
    public void shiftedIndicator_FirstIndexCrossing() {
        NumericIndicator custom = NumericIndicator.of(
                new ArrayIndicator(series,
                        numFunction.apply(1), numFunction.apply(2), numFunction.apply(3)));
        Indicator<Num> shifted = new ShiftedIndicator(custom, 1);
        Rule rule = custom.crossedOver(shifted);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertTrue(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(1));
    }

    /* --------------------------------------------------------------------- *
     *  Mismatched BarSeries
     * --------------------------------------------------------------------- */

    @Test
    public void mismatchedSeries_ShouldThrowIllegalArgumentException() {
        BarSeries otherSeries = createSeries(10, 20, 30);
        Indicator<Num> other = new ConstantIndicator<>(otherSeries, numFunction.apply(5));
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.crossedOver(other));
    }

    /* --------------------------------------------------------------------- *
     *  Other indicator throws on getValue
     * --------------------------------------------------------------------- */

    @Test
    public void otherIndicatorThrowsOnGetValue_PropagatesException() {
        Indicator<Num> throwing = new ThrowingIndicator(series);
        Rule rule = numericIndicator.crossedOver(throwing);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertThrows(RuntimeException.class, () -> rule.isSatisfied(0));
    }

    /* --------------------------------------------------------------------- *
     *  Other indicator returns null Num
     * --------------------------------------------------------------------- */

    @Test
    public void otherIndicatorReturnsNullNum_ShouldThrowIllegalArgumentException() {
        Indicator<Num> nullVal = new NullValueIndicator(series);
        Rule rule = numericIndicator.crossedOver(nullVal);
        assertTrue(rule instanceof CrossedUpIndicatorRule);
        assertThrows(IllegalArgumentException.class, () -> rule.isSatisfied(0));
    }
}
```