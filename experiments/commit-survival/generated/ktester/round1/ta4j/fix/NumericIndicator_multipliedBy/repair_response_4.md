```java
package org.ta4j.core.indicators.numeric;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#multipliedBy(Indicator)}.
 */
@RunWith(Enclosed.class)
public class NumericIndicator_multipliedBy_Test {

    /* --------------------------------------------------------------------- */
    /** Helper to add a simple bar (all prices equal) to a series. */
    private static void addBar(BarSeries series, double value) {
        BaseBar bar = new BaseBar(
                Duration.ofDays(1),
                ZonedDateTime.now(),
                DecimalNum.valueOf(value), // open
                DecimalNum.valueOf(value), // high
                DecimalNum.valueOf(value), // low
                DecimalNum.valueOf(value), // close
                DecimalNum.valueOf(value)  // volume
        );
        series.addBar(bar);
    }

    /* --------------------------------------------------------------------- */
    public static class ConstantMultiplication {

        @Test
        public void constantTimesConstant_positive() {
            NumericIndicator left = constNum(3);
            Indicator<Num> right = new ConstantIndicator<>(series, DecimalNum.valueOf(2));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(i));
            }
        }

        @Test
        public void constantTimesConstant_zero() {
            NumericIndicator left = constNum(0);
            Indicator<Num> right = new ConstantIndicator<>(series, DecimalNum.valueOf(5));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                Assert.assertEquals(DecimalNum.valueOf(0), result.getValue(i));
            }
        }

        @Test
        public void constantTimesConstant_negative() {
            NumericIndicator left = constNum(-4);
            Indicator<Num> right = new ConstantIndicator<>(series, DecimalNum.valueOf(3));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                Assert.assertEquals(DecimalNum.valueOf(-12), result.getValue(i));
            }
        }
    }

    /* --------------------------------------------------------------------- */
    public static class VaryingWithConstant {

        private BarSeries smallSeries;

        @Before
        public void init() {
            smallSeries = new BaseBarSeriesBuilder().withName("small").build();
            double[] vals = {1, 2, 3};
            for (double v : vals) {
                addBar(smallSeries, v);
            }
        }

        @Test
        public void constantLeft_variableRight() {
            NumericIndicator left = constNum(5);
            Indicator<Num> right = new ClosePriceIndicator(smallSeries);
            NumericIndicator result = left.multipliedBy(right);
            Assert.assertEquals(DecimalNum.valueOf(5), result.getValue(0));
            Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(1));
            Assert.assertEquals(DecimalNum.valueOf(15), result.getValue(2));
        }

        @Test
        public void variableLeft_constantRight() {
            BarSeries leftSeries = new BaseBarSeriesBuilder().withName("left").build();
            double[] leftVals = {2, 4, 6};
            for (double v : leftVals) {
                addBar(leftSeries, v);
            }
            Indicator<Num> left = new ClosePriceIndicator(leftSeries);
            NumericIndicator right = constNum(3);
            NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
            Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(0));
            Assert.assertEquals(DecimalNum.valueOf(12), result.getValue(1));
            Assert.assertEquals(DecimalNum.valueOf(18), result.getValue(2));
        }
    }

    /* --------------------------------------------------------------------- */
    private static BarSeries series;
    private ClosePriceIndicator closePrice;
    private EMAIndicator ema;
    private NumericIndicator numericIndicator;

    @BeforeClass
    public static void setupBeforeAll() {
        // deterministic series: 1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1,0,-1,-2
        series = new BaseBarSeriesBuilder().withName("test-series").build();
        double[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2};
        for (double v : values) {
            addBar(series, v);
        }
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

    /**
     * Helper to create a {@link NumericIndicator} from a constant value.
     */
    private static NumericIndicator constNum(double v) {
        return NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(v)));
    }

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

    @Test
    public void multipliedBy_withConstantIndicator() {
        Indicator<Num> constantFive = new ConstantIndicator<>(series, DecimalNum.valueOf(5));
        NumericIndicator result = numericIndicator.multipliedBy(constantFive);
        for (int i = 0; i < 5; i++) {
            Num expected = closePrice.getValue(i).multipliedBy(DecimalNum.valueOf(5));
            Num actual = result.getValue(i);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void multipliedBy_nullArgument_throwsNPE() {
        NumericIndicator left = constNum(1);
        Assert.assertThrows(NullPointerException.class, () -> left.multipliedBy((Indicator<Num>) null));
    }

    @Test
    public void identityMultiplication_self() {
        NumericIndicator left = constNum(7);
        NumericIndicator result = left.multipliedBy(left);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(49), result.getValue(i));
        }
    }

    @Test
    public void varyingTimesVarying_closePriceAndVolume() {
        BarSeries volSeries = new BaseBarSeriesBuilder().withName("vol").build();
        double[] volValues = {10, 20, 30};
        for (double v : volValues) {
            addBar(volSeries, v);
        }
        Indicator<Num> close = new ClosePriceIndicator(series);
        Indicator<Num> volume = new VolumeIndicator(volSeries);
        NumericIndicator result = NumericIndicator.of(close).multipliedBy(volume);
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(40), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(90), result.getValue(2));
    }

    @Test
    public void varyingWithNegativesAndZeros() {
        BarSeries s1 = new BaseBarSeriesBuilder().withName("s1").build();
        double[] vals1 = {-5, 0, 5};
        for (double v : vals1) {
            addBar(s1, v);
        }
        BarSeries s2 = new BaseBarSeriesBuilder().withName("s2").build();
        double[] vals2 = {2, -2, 0};
        for (double v : vals2) {
            addBar(s2, v);
        }
        Indicator<Num> left = new ClosePriceIndicator(s1);
        Indicator<Num> right = new ClosePriceIndicator(s2);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        Assert.assertEquals(DecimalNum.valueOf(-10), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(0), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(0), result.getValue(2));
    }

    @Test
    public void derivedIndicator_highestValueTimesConstant() {
        BarSeries hvSeries = new BaseBarSeriesBuilder().withName("hv").build();
        double[] hvVals = {1, 3, 2, 5};
        for (double v : hvVals) {
            addBar(hvSeries, v);
        }
        Indicator<Num> highest = new HighestValueIndicator(new ClosePriceIndicator(hvSeries), 2);
        NumericIndicator result = NumericIndicator.of(highest).multipliedBy(constNum(2));
        Assert.assertEquals(DecimalNum.valueOf(2), result.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(6), result.getValue(2));
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(3));
    }

    @Test
    public void chainedOperations_constantPlusThenMultipliedByConstant() {
        NumericIndicator left = constNum(2).plus(constNum(3));
        NumericIndicator result = left.multipliedBy(constNum(4));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(20), result.getValue(i));
        }
    }

    @Test
    public void commutativity_check() {
        BarSeries cs = new BaseBarSeriesBuilder().withName("cs").build();
        double[] vals = {1, 4};
        for (double v : vals) {
            addBar(cs, v);
        }
        Indicator<Num> left = new ClosePriceIndicator(cs);
        Indicator<Num> right = new ConstantIndicator<>(cs, DecimalNum.valueOf(5));
        NumericIndicator aTimesB = NumericIndicator.of(left).multipliedBy(right);
        NumericIndicator bTimesA = NumericIndicator.of(right).multipliedBy(left);
        Assert.assertEquals(DecimalNum.valueOf(5), aTimesB.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(20), aTimesB.getValue(1));
        Assert.assertEquals(DecimalNum.valueOf(5), bTimesA.getValue(0));
        Assert.assertEquals(DecimalNum.valueOf(20), bTimesA.getValue(1));
    }

    @Test
    public void mismatchedSeriesLength_throwsIndexOutOfBounds() {
        BarSeries seriesA = new BaseBarSeriesBuilder().withName("a").build();
        addBar(seriesA, 1);
        addBar(seriesA, 2);
        BarSeries seriesB = new BaseBarSeriesBuilder().withName("b").build();
        addBar(seriesB, 10);
        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        Assert.assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> result.getValue(1));
    }

    @Test
    public void differentBarSeries_throwsIllegalArgument() {
        BarSeries seriesA = new BaseBarSeriesBuilder().withName("a").build();
        addBar(seriesA, 1);
        addBar(seriesA, 2);
        addBar(seriesA, 3);
        BarSeries seriesB = new BaseBarSeriesBuilder().withName("b").build();
        addBar(seriesB, 10);
        addBar(seriesB, 20);
        addBar(seriesB, 30);
        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        Assert.assertThrows(IllegalArgumentException.class,
                () -> NumericIndicator.of(left).multipliedBy(right));
    }

    @Test
    public void nullDelegateIndicator_throwsNPE() {
        NumericIndicator nullDelegate = NumericIndicator.of((Indicator<Num>) null);
        Indicator<Num> other = constNum(5);
        NumericIndicator result = nullDelegate.multipliedBy(other);
        Assert.assertThrows(NullPointerException.class, () -> result.getValue(0));
    }

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
        Assert.assertThrows(NullPointerException.class, () -> result.getValue(0));
    }
}
```