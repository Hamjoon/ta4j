```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Additional tests for {@link NumericIndicator#multipliedBy(Indicator)} covering
 * the extensive JSON test matrix.
 *
 * The original template already provides two basic tests; the methods below
 * extend the coverage to constant indicators, varying indicators, edge‑cases,
 * and error handling.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_multipliedBy_Test {

    /** Shared series used by many tests. */
    private static BarSeries series;

    /** Indicator that returns the close price of the series. */
    private ClosePriceIndicator closePrice;

    /** EMA indicator used as a realistic dynamic operand. */
    private EMAIndicator ema;

    /** The NumericIndicator under test (wrapping {@code closePrice}). */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8,
                7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
    }

    @BeforeEach
    void setupBeforeEach() {
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    /* --------------------------------------------------------------------- */
    /* Existing tests from the template (kept unchanged)                     */
    /* --------------------------------------------------------------------- */

    @Test
    void multipliedBy_withRealIndicator() {
        NumericIndicator result = numericIndicator.multipliedBy(ema);
        int[] indices = {0, 8, series.getBarCount() - 1};
        for (int i : indices) {
            Num expected = closePrice.getValue(i).multipliedBy(ema.getValue(i));
            Num actual = result.getValue(i);
            assertEquals(expected, actual,
                    "multipliedBy(EMA) produced wrong value at index " + i);
        }
    }

    @Test
    void multipliedBy_withMockedIndicator() {
        @SuppressWarnings("unchecked")
        Indicator<Num> mockIndicator = mock(Indicator.class);
        Num constantFive = DecimalNum.valueOf(5);
        when(mockIndicator.getValue(anyInt())).thenReturn(constantFive);
        when(mockIndicator.getBarSeries()).thenReturn(series);

        NumericIndicator result = numericIndicator.multipliedBy(mockIndicator);
        for (int i = 0; i < 5; i++) {
            Num expected = closePrice.getValue(i).multipliedBy(constantFive);
            Num actual = result.getValue(i);
            assertEquals(expected, actual,
                    "multipliedBy(mock) produced wrong value at index " + i);
        }
        verify(mockIndicator, atLeastOnce()).getValue(anyInt());
    }

    /* --------------------------------------------------------------------- */
    /* New tests covering the JSON matrix                                   */
    /* --------------------------------------------------------------------- */

    /** Helper to create a NumericIndicator from a constant value. */
    private static NumericIndicator constNum(double v) {
        return NumericIndicator.of(new ConstantIndicator<>(DecimalNum.valueOf(v)));
    }

    @Nested
    @DisplayName("Basic multiplication with constant indicators")
    class ConstantMultiplication {

        @Test
        void constantTimesConstant_positive() {
            NumericIndicator left = constNum(3);
            Indicator<Num> right = new ConstantIndicator<>(DecimalNum.valueOf(2));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                assertEquals(DecimalNum.valueOf(6), result.getValue(i));
            }
        }

        @Test
        void constantTimesConstant_zero() {
            NumericIndicator left = constNum(0);
            Indicator<Num> right = new ConstantIndicator<>(DecimalNum.valueOf(5));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                assertEquals(DecimalNum.valueOf(0), result.getValue(i));
            }
        }

        @Test
        void constantTimesConstant_negative() {
            NumericIndicator left = constNum(-4);
            Indicator<Num> right = new ConstantIndicator<>(DecimalNum.valueOf(3));
            NumericIndicator result = left.multipliedBy(right);
            for (int i = 0; i < 5; i++) {
                assertEquals(DecimalNum.valueOf(-12), result.getValue(i));
            }
        }
    }

    @Nested
    @DisplayName("Multiplication with a varying indicator (ClosePriceIndicator)")
    class VaryingWithConstant {

        private BarSeries smallSeries;

        @BeforeEach
        void init() {
            smallSeries = new MockBarSeries(DecimalNum::valueOf, 1, 2, 3);
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
                    new MockBarSeries(DecimalNum::valueOf, 2, 4, 6));
            NumericIndicator right = constNum(3);
            NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
            assertEquals(DecimalNum.valueOf(6), result.getValue(0));
            assertEquals(DecimalNum.valueOf(12), result.getValue(1));
            assertEquals(DecimalNum.valueOf(18), result.getValue(2));
        }
    }

    @Test
    @DisplayName("Null argument handling")
    void multipliedBy_nullArgument_throwsNPE() {
        NumericIndicator left = constNum(1);
        assertThrows(NullPointerException.class, () -> left.multipliedBy(null));
    }

    @Test
    @DisplayName("Identity multiplication (self * self)")
    void identityMultiplication_self() {
        NumericIndicator left = constNum(7);
        NumericIndicator result = left.multipliedBy(left);
        for (int i = 0; i < 5; i++) {
            assertEquals(DecimalNum.valueOf(49), result.getValue(i));
        }
    }

    @Test
    @DisplayName("Multiplication of two varying indicators (ClosePrice * Volume)")
    void varyingTimesVarying_closePriceAndVolume() {
        BarSeries volSeries = new MockBarSeries(DecimalNum::valueOf,
                10, 20, 30);
        Indicator<Num> close = new ClosePriceIndicator(series); // series has at least 3 bars
        Indicator<Num> volume = new VolumeIndicator(volSeries);
        NumericIndicator result = NumericIndicator.of(close).multipliedBy(volume);
        assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        assertEquals(DecimalNum.valueOf(40), result.getValue(1));
        assertEquals(DecimalNum.valueOf(90), result.getValue(2));
    }

    @Test
    @DisplayName("Multiplication with negative and zero values in varying indicators")
    void varyingWithNegativesAndZeros() {
        BarSeries s1 = new MockBarSeries(DecimalNum::valueOf, -5, 0, 5);
        BarSeries s2 = new MockBarSeries(DecimalNum::valueOf, 2, -2, 0);
        Indicator<Num> left = new ClosePriceIndicator(s1);
        Indicator<Num> right = new ClosePriceIndicator(s2);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        assertEquals(DecimalNum.valueOf(-10), result.getValue(0));
        assertEquals(DecimalNum.valueOf(0), result.getValue(1));
        assertEquals(DecimalNum.valueOf(0), result.getValue(2));
    }

    @Test
    @DisplayName("Multiplication involving derived indicator (HighestValueIndicator)")
    void derivedIndicator_highestValueTimesConstant() {
        BarSeries hvSeries = new MockBarSeries(DecimalNum::valueOf, 1, 3, 2, 5);
        Indicator<Num> highest = new HighestValueIndicator(hvSeries, 2);
        NumericIndicator result = NumericIndicator.of(highest).multipliedBy(constNum(2));
        // highest values: [1,3,3,5] -> multiplied by 2 => [2,6,6,10]
        assertEquals(DecimalNum.valueOf(2), result.getValue(0));
        assertEquals(DecimalNum.valueOf(6), result.getValue(1));
        assertEquals(DecimalNum.valueOf(6), result.getValue(2));
        assertEquals(DecimalNum.valueOf(10), result.getValue(3));
    }

    @Test
    @DisplayName("Chained operations resulting indicator multiplication")
    void chainedOperations_constantPlusThenMultipliedByConstant() {
        NumericIndicator left = constNum(2).plus(constNum(3)); // (2+3)=5
        NumericIndicator result = left.multipliedBy(constNum(4)); // 5*4=20
        for (int i = 0; i < 5; i++) {
            assertEquals(DecimalNum.valueOf(20), result.getValue(i));
        }
    }

    @Test
    @DisplayName("Commutativity check between two indicators")
    void commutativity_check() {
        BarSeries cs = new MockBarSeries(DecimalNum::valueOf, 1, 4);
        Indicator<Num> left = new ClosePriceIndicator(cs);
        Indicator<Num> right = new ConstantIndicator<>(DecimalNum.valueOf(5));

        NumericIndicator aTimesB = NumericIndicator.of(left).multipliedBy(right);
        NumericIndicator bTimesA = NumericIndicator.of(right).multipliedBy(left);

        assertEquals(DecimalNum.valueOf(5), aTimesB.getValue(0));
        assertEquals(DecimalNum.valueOf(20), aTimesB.getValue(1));

        assertEquals(DecimalNum.valueOf(5), bTimesA.getValue(0));
        assertEquals(DecimalNum.valueOf(20), bTimesA.getValue(1));
    }

    @Test
    @DisplayName("Mismatched series length handling")
    void mismatchedSeriesLength_throwsIndexOutOfBounds() {
        BarSeries seriesA = new MockBarSeries(DecimalNum::valueOf, 1, 2);
        BarSeries seriesB = new MockBarSeries(DecimalNum::valueOf, 10);
        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        NumericIndicator result = NumericIndicator.of(left).multipliedBy(right);
        // index 0 is fine, index 1 should fail because seriesB has no bar 1
        assertEquals(DecimalNum.valueOf(10), result.getValue(0));
        assertThrows(IndexOutOfBoundsException.class, () -> result.getValue(1));
    }

    @Test
    @DisplayName("Different BarSeries handling")
    void differentBarSeries_throwsIllegalArgument() {
        BarSeries seriesA = new MockBarSeries(DecimalNum::valueOf, 1, 2, 3);
        BarSeries seriesB = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30);
        Indicator<Num> left = new ClosePriceIndicator(seriesA);
        Indicator<Num> right = new ClosePriceIndicator(seriesB);
        // The library is expected to reject mixing different series.
        assertThrows(IllegalArgumentException.class,
                () -> NumericIndicator.of(left).multipliedBy(right));
    }

    @Test
    @DisplayName("Null delegate indicator")
    void nullDelegateIndicator_throwsNPE() {
        NumericIndicator nullDelegate = NumericIndicator.of(null);
        Indicator<Num> other = constNum(5);
        // The NPE may be thrown during multiplication or when accessing a value.
        NumericIndicator result = nullDelegate.multipliedBy(other);
        assertThrows(NullPointerException.class, () -> result.getValue(0));
    }

    @Test
    @DisplayName("Indicator returning null values")
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
        };
        NumericIndicator left = constNum(4);
        NumericIndicator result = left.multipliedBy(nullReturning);
        assertThrows(NullPointerException.class, () -> result.getValue(0));
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to release for this test suite.
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
    }
}
```