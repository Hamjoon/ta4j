package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
// import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
// import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
// import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import java.time.Duration;
// import org.junit.jupiter.api.extension.ExtendWith;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.junit.Test;
import java.time.ZonedDateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;
import org.ta4j.core.BaseBar;

// import org.ta4j.core.mocks.MockBarSeries;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.Mockito;
@RunWith(Enclosed.class)
class NumericIndicator_multipliedBy_Test {

    /**
     * Shared series used by all tests.
     */
    private static BarSeries series;

    /**
     * Indicator that returns the close price of the series.
     */
    private ClosePriceIndicator closePrice;

    /**
     * EMA indicator used as a realistic dynamic operand.
     */
    private EMAIndicator ema;

    /**
     * The NumericIndicator under test (wrapping {@code closePrice}).
     */
    private NumericIndicator numericIndicator;

    /**
     * Simple constant {@link Indicator} implementation used in a few tests.
     */
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
            double[] vals = { 1, 2, 3 };
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
            double[] leftVals = { 2, 4, 6 };
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

    //     @BeforeAll
    static void setupBeforeAll() {
        // deterministic series: 1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1,0,-1,-2
        series = new BaseBarSeriesBuilder().withName("test-series").build();
        double[] values = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (double v : values) {
            addBar(series, v);
        }
    }

    //     @BeforeEach
    void setupBeforeEach() {
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    //     @AfterAll
    static void teardownAfterAll() {
        series = null;
    }

    /**
     * Helper to create a NumericIndicator from a constant value.
     */
    private static NumericIndicator constNum(double v) {
        return NumericIndicator.of(new ConstantIndicator<>(series, DecimalNum.valueOf(v)));
    }

    /**
     * Helper to add a simple bar (all prices equal) to a series.
     */
    private static void addBar(BarSeries series, double value) {
        BaseBar bar = new BaseBar(Duration.ofDays(1), ZonedDateTime.now(), // open
        DecimalNum.valueOf(value), // high
        DecimalNum.valueOf(value), // low
        DecimalNum.valueOf(value), // close
        DecimalNum.valueOf(value), // volume
        DecimalNum.valueOf(value));
        series.addBar(bar);
    }

    /* --------------------------------------------------------------------- */
    @Test
    void multipliedBy_withRealIndicator() {
        NumericIndicator result = numericIndicator.multipliedBy(ema);
        int[] indices = { 0, 8, series.getBarCount() - 1 };
        for (int i : indices) {
            Num expected = closePrice.getValue(i).multipliedBy(ema.getValue(i));
            Num actual = result.getValue(i);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    void multipliedBy_withMockedIndicator() {
        @SuppressWarnings("unchecked")
        Indicator<Num> mockIndicator = Mockito.mock(Indicator.class);
        Num constantFive = DecimalNum.valueOf(5);
        Mockito.when(mockIndicator.getValue(Mockito.anyInt())).thenReturn(constantFive);
        Mockito.when(mockIndicator.getBarSeries()).thenReturn(series);
        NumericIndicator result = numericIndicator.multipliedBy(mockIndicator);
        for (int i = 0; i < 5; i++) {
            Num expected = closePrice.getValue(i).multipliedBy(constantFive);
            Num actual = result.getValue(i);
            assertEquals(expected, actual, "multipliedBy(mock) produced wrong value at index " + i);
        }
        Mockito.verify(mockIndicator, Mockito.atLeastOnce()).getValue(Mockito.anyInt());
    }

    @Test
    void multipliedBy_nullArgument_throwsNPE() {
        NumericIndicator left = constNum(1);
        Assert.assertThrows(NullPointerException.class, () -> left.multipliedBy((Indicator<Num>) null));
    }

    @Test
    void identityMultiplication_self() {
        NumericIndicator left = constNum(7);
        NumericIndicator result = left.multipliedBy(left);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(49), result.getValue(i));
        }
    }

    @Test
    void varyingTimesVarying_closePriceAndVolume() {
        BarSeries volSeries = new BaseBarSeriesBuilder().withName("vol").build();
        double[] volValues = { 10, 20, 30 };
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
    void varyingWithNegativesAndZeros() {
        BarSeries s1 = new BaseBarSeriesBuilder().withName("s1").build();
        double[] vals1 = { -5, 0, 5 };
        for (double v : vals1) {
            addBar(s1, v);
        }
        BarSeries s2 = new BaseBarSeriesBuilder().withName("s2").build();
        double[] vals2 = { 2, -2, 0 };
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
    void derivedIndicator_highestValueTimesConstant() {
        BarSeries hvSeries = new BaseBarSeriesBuilder().withName("hv").build();
        double[] hvVals = { 1, 3, 2, 5 };
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
    void chainedOperations_constantPlusThenMultipliedByConstant() {
        NumericIndicator left = constNum(2).plus(constNum(3));
        NumericIndicator result = left.multipliedBy(constNum(4));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(DecimalNum.valueOf(20), result.getValue(i));
        }
    }

    @Test
    void commutativity_check() {
        BarSeries cs = new BaseBarSeriesBuilder().withName("cs").build();
        double[] vals = { 1, 4 };
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
    void mismatchedSeriesLength_throwsIndexOutOfBounds() {
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
    void differentBarSeries_throwsIllegalArgument() {
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
        Assert.assertThrows(IllegalArgumentException.class, () -> NumericIndicator.of(left).multipliedBy(right));
    }

    @Test
    void nullDelegateIndicator_throwsNPE() {
        NumericIndicator nullDelegate = NumericIndicator.of((Indicator<Num>) null);
        Indicator<Num> other = constNum(5);
        NumericIndicator result = nullDelegate.multipliedBy(other);
        Assert.assertThrows(NullPointerException.class, () -> result.getValue(0));
    }

    @Test
    void indicatorReturningNullValues_throwsNPE() {
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
}
