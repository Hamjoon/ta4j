###Test START##
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.TradingRecord;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * The tests cover:
 * <ul>
 *   <li>All arithmetic fluent methods (plus, minus, multipliedBy, dividedBy,
 *       min, max, abs, sqrt, squared)</li>
 *   <li>Factory methods that create other indicators (sma, ema, stddev,
 *       highest, lowest, previous, closePrice, volume)</li>
 *   <li>Rule‑creation methods (crossedOver, crossedUnder, isGreaterThan,
 *       isLessThan) – both the returned type and basic satisfaction logic.</li>
 * </ul>
 *
 * A minimal {@link BarSeries} implementation (the concrete {@code BaseBarSeries}
 * from the TA4J library) is used to obtain a {@link Num} factory.  All
 * indicators are built on top of {@link ConstantIndicator} to keep the data
 * deterministic and avoid any external dependencies.
 */
public class NumericIndicatorTest {

    private BarSeries series;
    private Num five;
    private Num three;
    private Num minusTwo;

    @Before
    public void setUp() {
        // BaseBarSeries is the standard concrete implementation of BarSeries.
        // It provides a NumFactory that can create Num instances.
        series = new org.ta4j.core.BaseBarSeries();
        five = series.numFactory().numOf(5);
        three = series.numFactory().numOf(3);
        minusTwo = series.numFactory().numOf(-2);
    }

    /** Helper to create a ConstantIndicator with the given Num value. */
    private ConstantIndicator<Num> constant(Num value) {
        return new ConstantIndicator<>(series, value);
    }

    /** Helper to compare two Num values with a tolerance. */
    private void assertNumEquals(Num expected, Num actual) {
        assertEquals(expected.doubleValue(), actual.doubleValue(), 1e-8);
    }

    @Test
    public void testArithmeticWithIndicators() {
        NumericIndicator a = NumericIndicator.of(constant(five));
        NumericIndicator b = NumericIndicator.of(constant(three));

        // plus
        NumericIndicator plus = a.plus(b);
        assertNumEquals(series.numFactory().numOf(8), plus.getValue(0));

        // minus
        NumericIndicator minus = a.minus(b);
        assertNumEquals(series.numFactory().numOf(2), minus.getValue(0));

        // multipliedBy
        NumericIndicator mul = a.multipliedBy(b);
        assertNumEquals(series.numFactory().numOf(15), mul.getValue(0));

        // dividedBy
        NumericIndicator div = a.dividedBy(b);
        assertNumEquals(series.numFactory().numOf(5.0 / 3.0), div.getValue(0));

        // min
        NumericIndicator min = a.min(b);
        assertNumEquals(three, min.getValue(0));

        // max
        NumericIndicator max = a.max(b);
        assertNumEquals(five, max.getValue(0));
    }

    @Test
    public void testArithmeticWithNumbers() {
        NumericIndicator a = NumericIndicator.of(constant(minusTwo));

        // plus(Number)
        NumericIndicator plus = a.plus(7);
        assertNumEquals(series.numFactory().numOf(5), plus.getValue(0));

        // minus(Number)
        NumericIndicator minus = a.minus(4);
        assertNumEquals(series.numFactory().numOf(-6), minus.getValue(0));

        // multipliedBy(Number)
        NumericIndicator mul = a.multipliedBy(3);
        assertNumEquals(series.numFactory().numOf(-6), mul.getValue(0));

        // dividedBy(Number)
        NumericIndicator div = a.dividedBy(2);
        assertNumEquals(series.numFactory().numOf(-1), div.getValue(0));
    }

    @Test
    public void testUnaryOperations() {
        NumericIndicator negative = NumericIndicator.of(constant(minusTwo));

        // abs()
        NumericIndicator abs = negative.abs();
        assertNumEquals(series.numFactory().numOf(2), abs.getValue(0));

        // sqrt() – use a positive value
        NumericIndicator pos = NumericIndicator.of(constant(five));
        NumericIndicator sqrt = pos.sqrt();
        assertNumEquals(series.numFactory().numOf(Math.sqrt(5.0)), sqrt.getValue(0));

        // squared() – should be equivalent to multipliedBy(self)
        NumericIndicator squared = pos.squared();
        assertNumEquals(series.numFactory().numOf(25), squared.getValue(0));
    }

    @Test
    public void testIndicatorFactories() {
        NumericIndicator close = NumericIndicator.closePrice(series);
        assertTrue(close.delegate() instanceof ClosePriceIndicator);

        NumericIndicator volume = NumericIndicator.volume(series);
        assertTrue(volume.delegate() instanceof VolumeIndicator);

        NumericIndicator base = NumericIndicator.of(constant(five));

        // SMA
        NumericIndicator sma = base.sma(3);
        assertTrue(sma.delegate() instanceof SMAIndicator);

        // EMA
        NumericIndicator ema = base.ema(3);
        assertTrue(ema.delegate() instanceof EMAIndicator);

        // Standard Deviation
        NumericIndicator stddev = base.stddev(3);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator);

        // Highest
        NumericIndicator highest = base.highest(3);
        assertTrue(highest.delegate() instanceof HighestValueIndicator);

        // Lowest
        NumericIndicator lowest = base.lowest(3);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);

        // Previous (with explicit barCount)
        NumericIndicator prev3 = base.previous(3);
        assertTrue(prev3.delegate() instanceof PreviousValueIndicator);

        // Previous (default = 1)
        Indicator<Num> prev1 = base.previous();
        assertTrue(prev1 instanceof PreviousValueIndicator);
        assertEquals(1, ((PreviousValueIndicator) prev1).getUnstableBars()); // default barCount is 1
    }

    @Test
    public void testCrossedOverRule() {
        NumericIndicator fast = NumericIndicator.of(constant(three));
        NumericIndicator slow = NumericIndicator.of(constant(five));

        // crossedOver with another indicator
        CrossedUpIndicatorRule rule = (CrossedUpIndicatorRule) fast.crossedOver(slow);
        assertNotNull(rule);
        // With constant values, the rule should never be satisfied.
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(0, (TradingRecord) null));

        // crossedOver with a Number
        CrossedUpIndicatorRule ruleNum = (CrossedUpIndicatorRule) fast.crossedOver(4);
        assertNotNull(ruleNum);
        // 3 never crosses over 4
        assertFalse(ruleNum.isSatisfied(0));
    }

    @Test
    public void testCrossedUnderRule() {
        NumericIndicator fast = NumericIndicator.of(constant(five));
        NumericIndicator slow = NumericIndicator.of(constant(three));

        CrossedDownIndicatorRule rule = (CrossedDownIndicatorRule) fast.crossedUnder(slow);
        assertNotNull(rule);
        // Constant values never cross under.
        assertFalse(rule.isSatisfied(0));
        assertFalse(rule.isSatisfied(0, (TradingRecord) null));

        CrossedDownIndicatorRule ruleNum = (CrossedDownIndicatorRule) fast.crossedUnder(6);
        assertNotNull(ruleNum);
        // 5 never crosses under 6
        assertFalse(ruleNum.isSatisfied(0));
    }

    @Test
    public void testGreaterThanAndLessThanRules() {
        NumericIndicator a = NumericIndicator.of(constant(five));
        NumericIndicator b = NumericIndicator.of(constant(three));

        // isGreaterThan with indicator
        OverIndicatorRule gtRule = (OverIndicatorRule) a.isGreaterThan(b);
        assertTrue(gtRule.isSatisfied(0));

        // isGreaterThan with number
        OverIndicatorRule gtNumRule = (OverIndicatorRule) a.isGreaterThan(4);
        assertTrue(gtNumRule.isSatisfied(0));

        // isLessThan with indicator
        UnderIndicatorRule ltRule = (UnderIndicatorRule) b.isLessThan(a);
        assertTrue(ltRule.isSatisfied(0));

        // isLessThan with number
        UnderIndicatorRule ltNumRule = (UnderIndicatorRule) b.isLessThan(4);
        assertTrue(ltNumRule.isSatisfied(0));
    }

    @Test
    public void testDelegateAndToString() {
        ConstantIndicator<Num> constInd = constant(five);
        NumericIndicator ni = NumericIndicator.of(constInd);

        // delegate() should return the original indicator
        assertSame(constInd, ni.delegate());

        // toString() should delegate to the underlying indicator's toString()
        assertEquals(constInd.toString(), ni.toString());
    }
}
###Test END##