```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarBuilder;
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
 * Unit tests for {@link NumericIndicator#multipliedBy(Indicator)}.
 *
 * The original test class contained many commented‑out sections and relied on
 * {@code MockBarSeries}, which is not part of the public TA4J API.  This version
 * replaces the mock series with a real {@link BarSeries} built via the public
 * builders, restores the JUnit 5 annotations and makes the class public so
 * that the test runner can discover and execute the tests.
 */
public class NumericIndicatorMultipliedByTest {

    /** Shared series used by all tests. */
    private static BarSeries series;

    /** Indicator that returns the close price of {@link #series}. */
    private ClosePriceIndicator closePrice;

    /** EMA indicator used as a realistic dynamic operand. */
    private EMAIndicator ema;

    /** The {@link NumericIndicator} under test (wrapping {@code closePrice}). */
    private NumericIndicator numericIndicator;

    /* --------------------------------------------------------------------- */
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

    @BeforeAll
    static void setupBeforeAll() {
        // Build a deterministic series: 1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1,0,-1,-2
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new BaseBarSeriesBuilder().withName("test-series").build();

        double[] values = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (double v : values) {
            series.addBar(new BaseBarBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .closePrice(numFunction.apply(v))
                    .build());
        }
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
    }

    @BeforeEach
    void setupBeforeEach() {
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
    @Nested
    class ConstantMultiplication {

        @Test
        void constantTimesConstant_positive() {
            NumericIndicator left = constNum(3);
            Indicator<Num> right = new ConstantIndicator<>(series, DecimalNum.valueOf(2));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                assertEquals(DecimalNum.valueOf(6), result.getValue(i));
            }
        }

        @Test
        void constantTimesConstant_zero() {
            NumericIndicator left = constNum(0);
            Indicator<Num> right = new ConstantIndicator<>(series, DecimalNum.valueOf(5));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                assertEquals(DecimalNum.valueOf(0), result.getValue(i));
            }
        }

        @Test
        void constantTimesConstant_negative() {
            NumericIndicator left = constNum(-4);
            Indicator<Num> right = new ConstantIndicator<>(series, DecimalNum.valueOf(3));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                assertEquals(DecimalNum.valueOf(-12), result.getValue(i));
            }
        }
    }

    /* --------------------------------------------------------------------- */
    @Nested
    class VaryingWithConstant {

        private BarSeries smallSeries;

        @BeforeEach
        void init() {
            smallSeries = new BaseBarSeriesBuilder().withName("small").build();
            double[] vals = { 1, 2, 3 };
            for (double v : vals) {
                smallSeries.addBar(new BaseBarBuilder()
                        .timePeriod(Duration.ofDays(1))
                        .closePrice(DecimalNum.valueOf(v))
                        .build());
            }
        }

        @Test
        void constantLeft_variableRight() {
            NumericIndicator left = constNum(5);
            Indicator<Num> right = new ClosePriceIndicator(smallSeries);
            NumericIndicator result = left.multipliedBy(right);
            assertEquals(DecimalNum.valueOf(5), result.getValue(0));
            assertEquals(DecimalNum.valueOf(10), result.getValue(1));
            assertEquals(DecimalNum.valueOf(15), result.getValue(2));
        }

        @Test
        void variableLeft_constantRight() {
            Indicator<Num> left = new ClosePriceIndicator(
                    new BaseBarSeriesBuilder().withName("left")
                            .build()
                            .addBar(new BaseBarBuilder()
                                    .timePeriod(Duration.ofDays(1))
                                    .closePrice(DecimalNum.valueOf(2))
                                    .build())
                            .addBar(new BaseBarBuilder()
                                    .timePeriod(Duration.ofDays(1))
                                    .closePrice(DecimalNum.valueOf(4))
                                    .build())
                            .addBar(new BaseBarBuilder()
                                    .timePeriod(Duration.ofDays(1))
                                    .closePrice(DecimalNum.valueOf(6))
                                    .build()));
            NumericIndicator right = constNum(3);
            NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
            assertEquals(DecimalNum.valueOf(6), result.getValue(0));
            assertEquals(DecimalNum.valueOf(12), result.getValue(1));
            assertEquals(DecimalNum.valueOf(18), result.getValue(2));
        }
    }

    /* --------------------------------------------------------------------- */
    @Test
    void multipliedBy_withRealIndicator() {
        NumericIndicator result = numericIndicator.multipliedBy(ema);
        int[] indices = { 0, 8, series.getBarCount() - 1 };
        for (int i : indices) {
            Num expected = closePrice.getValue(i).multipliedBy(ema.getValue(i));
            Num actual = result.getValue(i);
            assertEquals(expected, actual);
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
            assertEquals(expected, actual,
                    "multipliedBy(mock) produced wrong value at index " + i);
        }
        Mockito.verify(mockIndicator, Mockito.atLeastOnce()).getValue(Mockito.anyInt());
    }

    @Test
    void multipliedBy_nullArgument_throwsNPE() {
        NumericIndicator left = constNum(1);
        assertThrows(NullPointerException.class, () -> left.multipliedBy((Indicator<Num>) null));
    }

    @Test
    void identityMultiplication_self() {
        NumericIndicator left = constNum(7);
        NumericIndicator result = left.multipliedBy(left);
        for (int i = 0; i < 5; i++) {
            assertEquals(DecimalNum.valueOf(49), result.getValue(i));
        }
    }

    @Test
    void varyingTimesVarying_closePriceAndVolume() {
        BarSeries volSeries = new BaseBarSeriesBuilder().withName("vol").build();
        double[] volValues = { 10, 20, 30 };
        for (double v : volValues) {
            volSeries.addBar(new BaseBarBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .closePrice(DecimalNum.valueOf(v)) // volume is stored in close price for the test
                    .build());
        }
        Indicator<Num> close = new ClosePriceIndicator(series);
        Indicator<Num> volume = new VolumeIndicator(volSeries);
        NumericIndicator result = NumericIndicator.of(close).multipliedBy(volume);
        assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        assertEquals(DecimalNum.valueOf(40), result.getValue(1));
        assertEquals(DecimalNum.valueOf(90), result.getValue(2));
    }

    @Test
    void varyingWithNegativesAndZeros() {
        BarSeries s1 = new BaseBarSeriesBuilder().withName("s1").build();
        double[] vals1 = { -5, 0, 5 };
        for (double v : vals1) {
            s1.addBar(new BaseBarBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .closePrice(DecimalNum.valueOf(v))
                    .build());
        }
        BarSeries s2 = new BaseBarSeriesBuilder().withName("s2").build();
        double[] vals2 = { 2, -2, 0 };
        for (double v : vals2) {
            s2.addBar(new BaseBarBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .closePrice(DecimalNum.valueOf(v))
                    .build());
        }
        Indicator<Num> left = new ClosePriceIndicator(s1);
        Indicator<Num> right = new ClosePriceIndicator(s2);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        assertEquals(DecimalNum.valueOf(-10), result.getValue(0));
        assertEquals(DecimalNum.valueOf(0), result.getValue(1));
        assertEquals(DecimalNum.valueOf(0), result.getValue(2));
    }

    @Test
    void derivedIndicator_highestValueTimesConstant() {
        BarSeries hvSeries = new BaseBarSeriesBuilder().withName("hv").build();
        double[] hvVals = { 1, 3, 2, 5 };
        for (double v : hvVals) {
            hvSeries.addBar(new BaseBarBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .closePrice(DecimalNum.valueOf(v))
                    .build());
        }
        Indicator<Num> highest = new HighestValueIndicator(new ClosePriceIndicator(hvSeries), 2);
        NumericIndicator result = NumericIndicator.of(highest).multipliedBy(constNum(2));
        assertEquals(DecimalNum.valueOf(2), result.getValue(0));
        assertEquals(DecimalNum.valueOf(6), result.getValue(1));
        assertEquals(DecimalNum.valueOf(6), result.getValue(2));
        assertEquals(DecimalNum.valueOf(10), result.getValue(3));
    }

    @Test
    void chainedOperations_constantPlusThenMultipliedByConstant() {
        NumericIndicator left = constNum(2).plus(constNum(3));
        NumericIndicator result = left.multipliedBy(constNum(4));
        for (int i = 0; i < 5; i++) {
            assertEquals(DecimalNum.valueOf(20), result.getValue(i));
        }
    }

    @Test
    void commutativity_check() {
        BarSeries cs = new BaseBarSeriesBuilder().withName("cs").build();
        double[] vals = { 1, 4 };
        for (double v : vals) {
            cs.addBar(new BaseBarBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .closePrice(DecimalNum.valueOf(v))
                    .build());
        }
        Indicator<Num> left = new ClosePriceIndicator(cs);
        Indicator<Num> right = new ConstantIndicator<>(cs, DecimalNum.valueOf(5));
        NumericIndicator aTimesB = NumericIndicator.of(left).multipliedBy(right);
        NumericIndicator bTimesA = NumericIndicator.of(right).multipliedBy(left);
        assertEquals(DecimalNum.valueOf(5), aTimesB.getValue(0));
        assertEquals(DecimalNum.valueOf(20), aTimesB.getValue(1));
        assertEquals(DecimalNum.valueOf(5), bTimesA.getValue(0));
        assertEquals(DecimalNum.valueOf(20), bTimesA.getValue(1));
    }

    @Test
    void mismatchedSeriesLength_throwsIndexOutOfBounds() {
        BarSeries seriesA = new BaseBarSeriesBuilder().withName("a").build();
        seriesA.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(1))
                .build());
        seriesA.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(2))
                .build());

        BarSeries seriesB = new BaseBarSeriesBuilder().withName("b").build();
        seriesB.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(10))
                .build());

        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        assertThrows(IndexOutOfBoundsException.class, () -> result.getValue(1));
    }

    @Test
    void differentBarSeries_throwsIllegalArgument() {
        BarSeries seriesA = new BaseBarSeriesBuilder().withName("a").build();
        seriesA.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(1))
                .build());
        seriesA.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(2))
                .build());
        seriesA.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(3))
                .build());

        BarSeries seriesB = new BaseBarSeriesBuilder().withName("b").build();
        seriesB.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(10))
                .build());
        seriesB.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(20))
                .build());
        seriesB.addBar(new BaseBarBuilder()
                .timePeriod(Duration.ofDays(1))
                .closePrice(DecimalNum.valueOf(30))
                .build());

        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        assertThrows(IllegalArgumentException.class,
                () -> NumericIndicator.of(left).multipliedBy(right));
    }

    @Test
    void nullDelegateIndicator_throwsNPE() {
        NumericIndicator nullDelegate = NumericIndicator.of((Indicator<Num>) null);
        Indicator<Num> other = constNum(5);
        NumericIndicator result = nullDelegate.multipliedBy(other);
        assertThrows(NullPointerException.class, () -> result.getValue(0));
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
        assertThrows(NullPointerException.class, () -> result.getValue(0));
    }
}
```