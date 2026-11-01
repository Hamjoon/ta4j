```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_abs_Test {

    /**
     * Series containing both positive and negative values
     */
    private BarSeries series;

    /**
     * Indicator that simply returns the close price (i.e. the raw values)
     */
    private ClosePriceIndicator closePrice;

    /**
     * NumericIndicator wrapping the close‑price indicator
     */
    private NumericIndicator numericIndicator;

    @BeforeEach
    void setupBeforeEach() {
        // Build a mock series; the values deliberately cross zero
        series = new MockBarSeries(DecimalNum::valueOf,
                10, 5, 0, -3, -7, -2, 4, 8, -1, -6, 3, 0, -4, 7, -9, 2, -5, 6, -8, 1);
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    /**
     * Helper to create a ConstantIndicator bound to a dummy series
     */
    private ConstantIndicator constant(Num value) {
        // ConstantIndicator needs a series for context; we reuse the existing mock series
        return new ConstantIndicator(series, value);
    }

    private static Stream<Num[]> mixedValuesProvider() {
        return Stream.of(
                new Num[]{DecimalNum.valueOf(-5), DecimalNum.valueOf(0), DecimalNum.valueOf(7)},
                new Num[]{DecimalNum.valueOf(-123.456), DecimalNum.valueOf(0.0), DecimalNum.valueOf(987.654)},
                new Num[]{DecimalNum.valueOf(-0.0), DecimalNum.valueOf(0.0), DecimalNum.valueOf(0.0)}
        );
    }

    @Test
    void abs_shouldReturnAbsoluteValues() {
        // The method under test
        NumericIndicator absIndicator = numericIndicator.abs();
        // Verify that each value is the absolute value of the original indicator
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).abs();
            Num actual = absIndicator.getValue(i);
            assertEquals(expected, actual, "Absolute value mismatch at index " + i);
        }
    }

    @Test
    void abs_delegateShouldBeUnaryOperationIndicator() {
        // Ensure the returned delegate is the expected type (lightweight indicator)
        Indicator<Num> delegate = numericIndicator.abs().delegate();
        assertNotNull(delegate, "Delegate must not be null");
        // The concrete class is package‑private; we only assert it implements Indicator
        assertTrue(delegate instanceof Indicator, "Delegate must implement Indicator");
    }

    @Test
    void abs_negativeConstant_convertsToPositive() {
        // ConstantIndicator(-10) -> abs() should yield 10
        NumericIndicator original = NumericIndicator.of(constant(DecimalNum.valueOf(-10)));
        NumericIndicator abs = original.abs();
        assertEquals(DecimalNum.valueOf(10), abs.getValue(0), "Absolute value of -10 should be 10");
    }

    @Test
    void abs_mixedValues_correctAbsoluteResults() {
        // Indicator returning [-5, 0, 7]
        Indicator<Num> mixedIndicator = new Indicator<>() {

            @Override
            public Num getValue(int index) {
                return switch (index) {
                    case 0 -> DecimalNum.valueOf(-5);
                    case 1 -> DecimalNum.valueOf(0);
                    case 2 -> DecimalNum.valueOf(7);
                    default -> DecimalNum.valueOf(0);
                };
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
                return "MixedIndicator";
            }
        };
        NumericIndicator abs = NumericIndicator.of(mixedIndicator).abs();
        assertEquals(DecimalNum.valueOf(5), abs.getValue(0));
        assertEquals(DecimalNum.valueOf(0), abs.getValue(1));
        assertEquals(DecimalNum.valueOf(7), abs.getValue(2));
    }

    @Test
    void abs_alreadyPositive_isNoOp() {
        NumericIndicator original = NumericIndicator.of(constant(DecimalNum.valueOf(12)));
        NumericIndicator abs = original.abs();
        assertEquals(DecimalNum.valueOf(12), abs.getValue(0), "Positive constant should remain unchanged after abs()");
    }

    @Test
    void abs_zeroRemainsZero() {
        NumericIndicator original = NumericIndicator.of(constant(DecimalNum.valueOf(0)));
        NumericIndicator abs = original.abs();
        assertEquals(DecimalNum.valueOf(0), abs.getValue(0), "Zero should remain zero after abs()");
    }

    @Test
    void abs_idempotent_whenCalledTwice() {
        NumericIndicator once = numericIndicator.abs();
        NumericIndicator twice = once.abs();
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(once.getValue(i), twice.getValue(i),
                    "abs().abs() should be equal to abs() at index " + i);
        }
    }

    @Test
    void abs_doesNotMutateOriginalIndicator() {
        NumericIndicator original = NumericIndicator.of(closePrice);
        NumericIndicator abs = original.abs();
        // original should still return the raw (possibly negative) values
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), original.getValue(i),
                    "Original indicator must stay unchanged at index " + i);
            assertEquals(closePrice.getValue(i).abs(), abs.getValue(i),
                    "abs() must return absolute values at index " + i);
        }
    }

    @Test
    void abs_handlesExtremePositiveValue() {
        NumericIndicator original = NumericIndicator.of(constant(DecimalNum.valueOf(Long.MAX_VALUE)));
        NumericIndicator abs = original.abs();
        assertEquals(DecimalNum.valueOf(Long.MAX_VALUE), abs.getValue(0),
                "Extreme positive value must stay unchanged after abs()");
    }

    @Test
    void abs_normalisesNegativeZero() {
        // DecimalNum supports signed zero; create -0.0 via valueOf(-0.0)
        NumericIndicator original = NumericIndicator.of(constant(DecimalNum.valueOf(-0.0)));
        NumericIndicator abs = original.abs();
        // The library's Num.abs() returns a positive zero (value 0.0)
        assertEquals(DecimalNum.valueOf(0.0), abs.getValue(0),
                "Negative zero should become positive zero after abs()");
    }

    @Test
    void abs_propagatesNaN() {
        // DecimalNum.NaN is the representation of NaN in ta4j
        NumericIndicator original = NumericIndicator.of(constant(DecimalNum.NaN));
        NumericIndicator abs = original.abs();
        assertTrue(abs.getValue(0).isNaN(), "NaN should be propagated unchanged by abs()");
    }

    @Test
    void abs_preservesBarSeriesReference() {
        NumericIndicator abs = numericIndicator.abs();
        assertSame(series, abs.getBarSeries(),
                "BarSeries reference must be preserved after abs()");
        assertSame(series, numericIndicator.getBarSeries(),
                "Original indicator must still reference the same BarSeries");
    }

    @Test
    void abs_createsNewDelegateInstance() {
        NumericIndicator original = NumericIndicator.of(closePrice);
        NumericIndicator abs = original.abs();
        assertNotSame(original.delegate(), abs.delegate(),
                "Delegate of abs() must be a different object from the original delegate");
        // Both delegates should still produce identical absolute values
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).abs();
            assertEquals(expected, abs.getValue(i));
        }
    }

    @Test
    void of_nullDelegate_throwsException() {
        // The factory should reject a null delegate
        assertThrows(NullPointerException.class, () -> NumericIndicator.of(null),
                "Creating a NumericIndicator with a null delegate must throw NPE");
    }

    @Test
    void abs_whenDelegateReturnsNull_throwsNPE() {
        // Custom indicator that returns null for any index
        Indicator<Num> nullReturning = new Indicator<>() {

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

            @Override
            public String toString() {
                return "NullReturningIndicator";
            }
        };
        NumericIndicator wrapper = NumericIndicator.of(nullReturning);
        NumericIndicator abs = wrapper.abs();
        assertThrows(NullPointerException.class, () -> abs.getValue(0),
                "abs() should throw NPE when underlying delegate returns null");
    }

    @Test
    void abs_propagatesIndexOutOfBoundsException() {
        // Indicator that throws IndexOutOfBoundsException for negative indices
        Indicator<Num> throwingIndicator = new Indicator<>() {

            @Override
            public Num getValue(int index) {
                if (index < 0) {
                    throw new IndexOutOfBoundsException("negative index");
                }
                return DecimalNum.valueOf(1);
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
                return "ThrowingIndicator";
            }
        };
        NumericIndicator abs = NumericIndicator.of(throwingIndicator).abs();
        assertThrows(IndexOutOfBoundsException.class, () -> abs.getValue(-1),
                "abs() must propagate IndexOutOfBoundsException from delegate");
    }

    @Test
    void abs_propagatesCustomRuntimeException() {
        // Indicator that throws a custom unchecked exception
        RuntimeException custom = new RuntimeException("delegate failure");
        Indicator<Num> failingIndicator = new Indicator<>() {

            @Override
            public Num getValue(int index) {
                throw custom;
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
                return "FailingIndicator";
            }
        };
        NumericIndicator abs = NumericIndicator.of(failingIndicator).abs();
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> abs.getValue(0),
                "abs() must re‑throw the delegate's RuntimeException");
        assertSame(custom, thrown, "The same exception instance should be propagated");
    }

    @ParameterizedTest
    @MethodSource("mixedValuesProvider")
    void abs_parameterizedMixedValues(Num[] values) {
        // Build a simple indicator that returns the supplied array values
        Indicator<Num> custom = new Indicator<>() {

            @Override
            public Num getValue(int index) {
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
                return "ParamIndicator";
            }
        };
        NumericIndicator abs = NumericIndicator.of(custom).abs();
        for (int i = 0; i < values.length; i++) {
            Num expected = values[i].abs();
            assertEquals(expected, abs.getValue(i), "Absolute value mismatch at index " + i);
        }
    }
}
```