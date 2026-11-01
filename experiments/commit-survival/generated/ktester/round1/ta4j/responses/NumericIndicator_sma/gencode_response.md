```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive tests for {@link NumericIndicator#sma(int)}.
 *
 * <p>
 * The test suite covers:
 * <ul>
 *   <li>valid periods (including the special case {@code barCount == 1})</li>
 *   <li>error handling for non‑positive periods</li>
 *   <li>delegate integrity and series propagation</li>
 *   <li>behaviour when the period exceeds the series length</li>
 *   <li>independence of multiple invocations</li>
 *   <li>immutability of the original indicator</li>
 *   <li>string representation</li>
 *   <li>nested SMA composition</li>
 *   <li>failure when the underlying delegate is {@code null}</li>
 *   <li>failure when the underlying delegate has a {@code null} BarSeries</li>
 *   <li>propagation of runtime exceptions from the wrapped indicator</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_sma_Test {

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Deterministic series – values are simple integers for easy manual verification.
        series = new MockBarSeries(DecimalNum::valueOf,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up for this test suite.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown needed.
    }

    /* --------------------------------------------------------------------- */
    /*  Existing tests (kept unchanged)                                      */
    /* --------------------------------------------------------------------- */

    @Test
    void smaCreatesCorrectDelegate() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        // The underlying delegate should be an SMAIndicator.
        Indicator<Num> delegate = sma.delegate();
        assertEquals(SMAIndicator.class, delegate.getClass(),
                "sma() should wrap an SMAIndicator");
    }

    @Test
    void smaValuesMatchDirectSMAIndicator() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        SMAIndicator directSma = new SMAIndicator(closePrice, barCount);

        // Compare each value produced by the fluent wrapper with the direct SMAIndicator.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = directSma.getValue(i);
            Num actual = sma.getValue(i);
            assertEquals(expected, actual,
                    () -> "Mismatch at index " + i + ": expected " + expected + " but got " + actual);
        }
    }

    @Test
    void smaHandlesInsufficientDataGracefully() {
        int barCount = 5;
        NumericIndicator sma = numericIndicator.sma(barCount);
        SMAIndicator directSma = new SMAIndicator(closePrice, barCount);

        // For indices smaller than barCount-1 the SMAIndicator returns NaN.
        for (int i = 0; i < barCount - 1; i++) {
            Num expected = directSma.getValue(i);
            Num actual = sma.getValue(i);
            assertTrue(expected instanceof org.ta4j.core.num.NaN,
                    "Direct SMAIndicator should return NaN for index " + i);
            assertTrue(actual instanceof org.ta4j.core.num.NaN,
                    "NumericIndicator.sma() should also return NaN for index " + i);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  New tests – valid barCount values (parameterised)                     */
    /* --------------------------------------------------------------------- */

    @ParameterizedTest(name = "sma({0}) should compute correct SMA")
    @CsvSource({
            "5, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -0.5, -1.5",
            "1, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0"
    })
    void smaValidBarCount_ComputesExpectedValues(int barCount, @ConvertWith(NumArrayConverter.class) Num[] expected) {
        NumericIndicator sma = numericIndicator.sma(barCount);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i),
                    () -> "Unexpected SMA value at index " + i + " for barCount=" + barCount);
        }
    }

    /**
     * Converter that turns a comma‑separated list of numbers into an array of {@link Num}.
     */
    public static class NumArrayConverter extends SimpleArgumentConverter {
        @Override
        protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
            if (source instanceof String && targetType == Num[].class) {
                String[] parts = ((String) source).split("\\s*,\\s*");
                Num[] nums = new Num[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    nums[i] = DecimalNum.valueOf(parts[i]);
                }
                return nums;
            }
            throw new ArgumentConversionException("Cannot convert " + source + " to Num[]");
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Edge / error cases                                                    */
    /* --------------------------------------------------------------------- */

    @ParameterizedTest(name = "sma({0}) must throw IllegalArgumentException")
    @ValueSource(ints = {0, -3})
    void smaInvalidBarCount_ThrowsException(int barCount) {
        assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.sma(barCount),
                "sma(" + barCount + ") should reject non‑positive periods");
    }

    /* --------------------------------------------------------------------- */
    /*  Delegate integrity                                                    */
    /* --------------------------------------------------------------------- */

    @Test
    void smaDelegateIntegrity() {
        int barCount = 10;
        NumericIndicator sma = numericIndicator.sma(barCount);
        Indicator<Num> delegate = sma.delegate();

        assertTrue(delegate instanceof SMAIndicator, "Delegate must be an SMAIndicator");
        assertSame(series, delegate.getBarSeries(),
                "The SMAIndicator must reference the same BarSeries as the original indicator");
    }

    /* --------------------------------------------------------------------- */
    /*  Period larger than series length                                      */
    /* --------------------------------------------------------------------- */

    @Test
    void smaBarCountLargerThanSeriesLength() {
        // Series with exactly four values.
        BarSeries shortSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));

        int barCount = 10; // larger than series length
        NumericIndicator sma = base.sma(barCount);
        // Expected progressive averages: 10, 15, 20, 25
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(20),
                DecimalNum.valueOf(25)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i),
                    "Unexpected SMA value at index " + i + " when barCount > series length");
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Period equal to series length                                          */
    /* --------------------------------------------------------------------- */

    @Test
    void smaBarCountEqualToSeriesLength() {
        BarSeries shortSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));

        int barCount = 4; // exactly the series length
        NumericIndicator sma = base.sma(barCount);
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(20),
                DecimalNum.valueOf(25)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i),
                    "Unexpected SMA value at index " + i + " when barCount == series length");
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Multiple independent invocations                                      */
    /* --------------------------------------------------------------------- */

    @Test
    void smaMultipleIndependentInvocations() {
        int barCount = 3;
        NumericIndicator sma1 = numericIndicator.sma(barCount);
        NumericIndicator sma2 = numericIndicator.sma(barCount);

        // Different objects
        assertNotSame(sma1, sma2, "Two calls to sma() must return distinct NumericIndicator instances");
        assertNotSame(sma1.delegate(), sma2.delegate(),
                "Each NumericIndicator must wrap its own SMAIndicator instance");

        // Values must be identical
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(sma1.getValue(i), sma2.getValue(i),
                    "SMA values differ at index " + i + " between two independent wrappers");
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Original indicator immutability                                       */
    /* --------------------------------------------------------------------- */

    @Test
    void smaDoesNotMutateOriginalIndicator() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        // Force evaluation of SMA to ensure any side‑effects would have happened.
        sma.getValue(series.getBarCount() - 1);

        // Original indicator must still return the raw close price.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), numericIndicator.getValue(i),
                    "Original NumericIndicator mutated after sma() call at index " + i);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  toString representation                                               */
    /* --------------------------------------------------------------------- */

    @Test
    void smaToStringContainsDelegateInfo() {
        int barCount = 5;
        NumericIndicator sma = numericIndicator.sma(barCount);
        String str = sma.toString();
        assertTrue(str.contains("SMAIndicator"),
                "toString() should contain the delegate class name");
        assertTrue(str.contains(String.valueOf(barCount)),
                "toString() should contain the SMA period");
    }

    /* --------------------------------------------------------------------- */
    /*  Nested SMA composition                                                 */
    /* --------------------------------------------------------------------- */

    @Test
    void nestedSmaCompositionProducesDoubleSmoothedSeries() {
        BarSeries shortSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));

        // First SMA(3) → [10, 15, 20, 30]
        NumericIndicator firstSma = base.sma(3);
        // Then SMA(2) on the result → [10, 12.5, 17.5, 25]
        NumericIndicator secondSma = firstSma.sma(2);

        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(12.5),
                DecimalNum.valueOf(17.5),
                DecimalNum.valueOf(25)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], secondSma.getValue(i),
                    "Incorrect double‑smoothed value at index " + i);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Null delegate exception                                                */
    /* --------------------------------------------------------------------- */

    @Test
    void smaWithNullDelegateThrowsNPE() {
        NumericIndicator nullWrapped = NumericIndicator.of(null);
        assertThrows(NullPointerException.class,
                () -> nullWrapped.sma(5),
                "Calling sma() on a NumericIndicator with a null delegate must throw NPE");
    }

    /* --------------------------------------------------------------------- */
    /*  Delegate with null BarSeries                                            */
    /* --------------------------------------------------------------------- */

    @Test
    void smaWithDelegateHavingNullBarSeriesThrowsException() {
        // Mock an Indicator whose getBarSeries() returns null.
        @SuppressWarnings("unchecked")
        Indicator<Num> badIndicator = mock(Indicator.class);
        when(badIndicator.getBarSeries()).thenReturn(null);
        when(badIndicator.getValue(anyInt())).thenReturn(DecimalNum.ZERO); // irrelevant

        NumericIndicator wrapper = NumericIndicator.of(badIndicator);
        // SMAIndicator validates the series in its constructor, leading to NPE/IAE.
        assertThrows(RuntimeException.class,
                () -> wrapper.sma(3),
                "SMA creation should fail when the underlying BarSeries is null");
    }

    /* --------------------------------------------------------------------- */
    /*  Runtime exception propagation from wrapped indicator                  */
    /* --------------------------------------------------------------------- */

    @Test
    void smaPropagatesRuntimeExceptionFromUnderlyingIndicator() {
        // Custom Indicator that throws IllegalStateException for index >= 2.
        Indicator<Num> flaky = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                if (index >= 2) {
                    throw new IllegalStateException("Deliberate failure at index " + index);
                }
                return DecimalNum.valueOf(index + 1); // simple deterministic value
            }

            @Override
            public BarSeries getBarSeries() {
                return series; // a valid series – only getValue is problematic
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };

        NumericIndicator wrapper = NumericIndicator.of(flaky);
        NumericIndicator sma = wrapper.sma(4);

        // Index 0 and 1 should work fine.
        assertDoesNotThrow(() -> sma.getValue(0));
        assertDoesNotThrow(() -> sma.getValue(1));

        // Index 2 must propagate the IllegalStateException.
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> sma.getValue(2),
                "SMA wrapper must not swallow exceptions from the wrapped indicator");
        assertTrue(ex.getMessage().contains("Deliberate failure"),
                "Exception message should be propagated unchanged");
    }
}
```