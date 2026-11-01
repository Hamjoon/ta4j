```java
package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#multipliedBy(Indicator)}.
 *
 * The original test class mixed JUnit 4 and JUnit 5 annotations and used
 * Mockito, which are not available in the compilation environment.  This
 * rewritten version relies solely on JUnit 4 and simple stub indicators,
 * keeping the original test logic intact.
 */
public class NumericIndicator_multipliedBy_Test {

    /** Shared series used by several tests. */
    private static BarSeries series;

    /** Indicator that returns the close price of {@link #series}. */
    private ClosePriceIndicator closePrice;

    /** EMA indicator used as a realistic dynamic operand. */
    private EMAIndicator ema;

    /** The {@link NumericIndicator} under test (wrapping {@code closePrice}). */
    private NumericIndicator numericIndicator;

    /* --------------------------------------------------------------------- */
    /*  Simple stub that always returns the same {@link Num} value.          */
    /* --------------------------------------------------------------------- */
    private static class ConstantNumIndicator implements Indicator<Num> {

        private final Num constant;
        private final BarSeries series;

        ConstantNumIndicator(Num constant, BarSeries series) {
            this.constant = constant;
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            return constant;
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
    /*  JUnit lifecycle methods                                              */
    /* --------------------------------------------------------------------- */
    @BeforeClass
    public static void setupBeforeAll() {
        // MockBarSeries expects a Function<Number,Num> and a var‑args list of numbers.
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
    }

    @AfterClass
    public static void teardownAfterAll() {
        series = null;
    }

    @Before
    public void setupBeforeEach() {
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    /* --------------------------------------------------------------------- */
    /*  Helper to create a NumericIndicator from a constant value.          */
    /* --------------------------------------------------------------------- */
    private static NumericIndicator constNum(double v) {
        return NumericIndicator.of(new ConstantIndicator<Num>(DecimalNum.valueOf(v)));
    }

    /* --------------------------------------------------------------------- */
    /*  Basic multiplication with constant indicators                         */
    /* --------------------------------------------------------------------- */
    @Test
    public void constantTimesConstant_positive() {
        NumericIndicator left = constNum(3);
        Indicator<Num> right = new ConstantIndicator<Num>(DecimalNum.valueOf(2));
        NumericIndicator result = left.multipliedBy(right);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(i));
        }
    }

    @Test
    public void constantTimesConstant_zero() {
        NumericIndicator left = constNum(0);
        Indicator<Num> right = new ConstantIndicator<Num>(DecimalNum.valueOf(5));
        NumericIndicator result = left.multipliedBy(right);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(0), result.getValue(i));
        }
    }

    @Test
    public void constantTimesConstant_negative() {
        NumericIndicator left = constNum(-4);
        Indicator<Num> right = new ConstantIndicator<Num>(DecimalNum.valueOf(3));
        NumericIndicator result = left.multipliedBy(right);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(-12), result.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Multiplication with a varying indicator (ClosePriceIndicator)        */
    /* --------------------------------------------------------------------- */
    @Test
    public void constantLeft_variableRight() {
        BarSeries smallSeries = new MockBarSeries(DecimalNum::valueOf, 1, 2, 3);
        NumericIndicator left = constNum(5);
        Indicator<Num> right = new ClosePriceIndicator(smallSeries);
        NumericIndicator result = left.multipliedBy(right);
        Assert.assertEquals(DecimalNum.valueOf(5), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(15), result.getValue(2));
    }

    @Test
    public void variableLeft_constantRight() {
        Indicator<Num> left = new ClosePriceIndicator(
                new MockBarSeries(DecimalNum::valueOf, 2, 4, 6));
        NumericIndicator right = constNum(3);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(12), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(18), result.getValue(2));
    }

    /* --------------------------------------------------------------------- */
    /*  Real indicator (EMA) multiplication                                   */
    /* --------------------------------------------------------------------- */
    @Test
    public void multipliedBy_withRealIndicator() {
        NumericIndicator result = numericIndicator.multipliedBy(ema);
        int[] indices = {0, 8, series.getBarCount() - 1};
        for (int i : indices) {
            Num expected = closePrice.getValue(i).multipliedBy(ema.getValue(i));
            Num actual = result.getValue(i);
            Assert.assertEquals(expected, actual);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Stub indicator (constant) multiplication                               */
    /* --------------------------------------------------------------------- */
    @Test
    public void multipliedBy_withStubbedConstantIndicator() {
        ConstantNumIndicator stub = new ConstantNumIndicator(DecimalNum.valueOf(5), series);
        NumericIndicator result = numericIndicator.multipliedBy(stub);
        for (int i = 0; i < 5; i++) {
            Num expected = closePrice.getValue(i).multipliedBy(DecimalNum.valueOf(5));
            Num actual = result.getValue(i);
            Assert.assertEquals(expected, actual);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Null argument handling                                                */
    /* --------------------------------------------------------------------- */
    @Test
    public void multipliedBy_nullArgument_throwsNPE() {
        NumericIndicator left = constNum(1);
        try {
            left.multipliedBy((Indicator<Num>) null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
            // expected
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Identity multiplication (self * self)                                 */
    /* --------------------------------------------------------------------- */
    @Test
    public void identityMultiplication_self() {
        NumericIndicator left = constNum(7);
        NumericIndicator result = left.multipliedBy(left);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(49), result.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Multiplication of two varying indicators (ClosePrice * Volume)      */
    /* --------------------------------------------------------------------- */
    @Test
    public void varyingTimesVarying_closePriceAndVolume() {
        BarSeries volSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30);
        Indicator<Num> close = new ClosePriceIndicator(series);
        Indicator<Num> volume = new VolumeIndicator(volSeries);
        NumericIndicator result = NumericIndicator.of(close).multipliedBy(volume);
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(40), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(90), result.getValue(2));
    }

    /* --------------------------------------------------------------------- */
    /*  Multiplication with negative and zero values                         */
    /* --------------------------------------------------------------------- */
    @Test
    public void varyingWithNegativesAndZeros() {
        BarSeries s1 = new MockBarSeries(DecimalNum::valueOf, -5, 0, 5);
        BarSeries s2 = new MockBarSeries(DecimalNum::valueOf, 2, -2, 0);
        Indicator<Num> left = new ClosePriceIndicator(s1);
        Indicator<Num> right = new ClosePriceIndicator(s2);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        Assert.assertEquals(DecimalNum.valueOf(-10), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(0), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(0), result.getValue(2));
    }

    /* --------------------------------------------------------------------- */
    /*  Derived indicator (HighestValueIndicator) multiplication               */
    /* --------------------------------------------------------------------- */
    @Test
    public void derivedIndicator_highestValueTimesConstant() {
        BarSeries hvSeries = new MockBarSeries(DecimalNum::valueOf, 1, 3, 2, 5);
        Indicator<Num> highest = new HighestValueIndicator(
                new ClosePriceIndicator(hvSeries), 2);
        NumericIndicator result = NumericIndicator.of(highest).multipliedBy(constNum(2));
        // highest values: [1,3,3,5] -> multiplied by 2 => [2,6,6,10]
        Assert.assertEquals(DecimalNum.valueOf(2), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(2));
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(3));
    }

    /* --------------------------------------------------------------------- */
    /*  Chained operations                                                    */
    /* --------------------------------------------------------------------- */
    @Test
    public void chainedOperations_constantPlusThenMultipliedByConstant() {
        // (2+3)=5
        NumericIndicator left = constNum(2).plus(constNum(3));
        // 5*4=20
        NumericIndicator result = left.multipliedBy(constNum(4));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(20), result.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Commutativity check between two indicators                             */
    /* --------------------------------------------------------------------- */
    @Test
    public void commutativity_check() {
        BarSeries cs = new MockBarSeries(DecimalNum::valueOf, 1, 4);
        Indicator<Num> left = new ClosePriceIndicator(cs);
        Indicator<Num> right = new ConstantIndicator<Num>(DecimalNum.valueOf(5));
        NumericIndicator aTimesB = NumericIndicator.of(left).multipliedBy(right);
        NumericIndicator bTimesA = NumericIndicator.of(right).multipliedBy(left);
        Assert.assertEquals(DecimalNum.valueOf(5), aTimesB.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(20), aTimesB.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(5), bTimesA.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(20), bTimesA.getValue(1));
    }

    /* --------------------------------------------------------------------- */
    /*  Mismatched series length handling                                      */
    /* --------------------------------------------------------------------- */
    @Test
    public void mismatchedSeriesLength_throwsIndexOutOfBounds() {
        BarSeries seriesA = new MockBarSeries(DecimalNum::valueOf, 1, 2);
        BarSeries seriesB = new MockBarSeries(DecimalNum::valueOf, 10);
        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        // index 0 is fine
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        // index 1 should fail because seriesB has no bar 1
        try {
            result.getValue(1);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
            // expected
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Different BarSeries handling                                           */
    /* --------------------------------------------------------------------- */
    @Test
    public void differentBarSeries_throwsIllegalArgument() {
        BarSeries seriesA = new MockBarSeries(DecimalNum::valueOf, 1, 2, 3);
        BarSeries seriesB = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30);
        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        try {
            NumericIndicator.of(left).multipliedBy(right);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // expected
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Null delegate indicator                                                 */
    /* --------------------------------------------------------------------- */
    @Test
    public void nullDelegateIndicator_throwsNPE() {
        NumericIndicator nullDelegate = NumericIndicator.of((Indicator<Num>) null);
        Indicator<Num> other = constNum(5);
        NumericIndicator result = nullDelegate.multipliedBy(other);
        try {
            result.getValue(0);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
            // expected
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Indicator returning null values                                        */
    /* --------------------------------------------------------------------- */
    @Test
    public void indicatorReturningNullValues_throwsNPE() {
        Indicator<Num> nullReturning = new Indicator<Num>() {
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
        };
        NumericIndicator left = constNum(4);
        NumericIndicator result = left.multipliedBy(nullReturning);
        try {
            result.getValue(0);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
            // expected
        }
    }
}
```