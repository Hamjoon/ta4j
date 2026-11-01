```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Additional tests for {@link NumericIndicator#squared()} covering edge‑cases,
 * composition, error handling and metadata propagation.
 *
 * <p>The original test suite already verifies the basic arithmetic behaviour.
 * The tests below complement it with:</p>
 * <ul>
 *   <li>empty series handling,</li>
 *   <li>large numbers (no overflow),</li>
 *   <li>double‑squared (fourth power) composability,</li>
 *   <li>composition with other arithmetic operations,</li>
 *   <li>metadata propagation (BarSeries & unstable bars),</li>
 *   <li>error scenarios (null delegate, null Num, runtime exception).</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_squared_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /** Series used in most tests. */
    private static BarSeries series;

    /** Simple delegate indicator (close price). */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator wrapping the delegate. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // 20 values – the same data set used in the original test suite.
        series = new MockBarSeries(
                NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setupBeforeEach() {
        // fresh NumericIndicator for each test method
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No mutable state to clean up at the moment.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing special to release.
    }

    /* --------------------------------------------------------------------- */
    /* Existing basic tests (kept unchanged)                                 */
    /* --------------------------------------------------------------------- */

    @Test
    void squaredReturnsNewInstance() {
        NumericIndicator squared = numericIndicator.squared();
        assertNotNull(squared, "squared() must not return null");
        assertNotSame(numericIndicator, squared,
                "squared() should return a new NumericIndicator instance");
    }

    @Test
    void squaredValuesAreCorrect() {
        NumericIndicator squared = numericIndicator.squared();

        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i).multipliedBy(closePriceIndicator.getValue(i));
            Num actual = squared.getValue(i);
            assertEquals(expected, actual,
                    () -> "Incorrect squared value at index " + i + ": expected " + expected + " but was " + actual);
        }
    }

    @Test
    void squaredWorksWithNegativeAndZeroValues() {
        NumericIndicator squared = numericIndicator.squared();

        // Spot‑check a few interesting indices.
        int[] indices = {0, 8, 15, 16, 18, 19}; // 1, 9, 2, 1, -1, -2
        for (int i : indices) {
            Num value = closePriceIndicator.getValue(i);
            Num expected = value.multipliedBy(value);
            Num actual = squared.getValue(i);
            assertEquals(expected, actual,
                    () -> "Squared value mismatch at index " + i + " (value=" + value + ")");
        }
    }

    @Test
    void delegateOfSquaredIsAnIndicator() {
        NumericIndicator squared = numericIndicator.squared();
        Indicator<Num> delegate = squared.delegate();
        assertNotNull(delegate, "Delegate of the squared indicator must not be null");
        // The delegate should be a NumericIndicator (the result of multipliedBy(this))
        assertTrue(delegate instanceof NumericIndicator,
                "Delegate of squared() should be a NumericIndicator");
    }

    /* --------------------------------------------------------------------- */
    /* New tests – edge cases, composition, and error handling                */
    /* --------------------------------------------------------------------- */

    @Test
    void squaredDoesNotMutateOriginalIndicator() {
        // Capture original values before calling squared()
        Num[] originalValues = new Num[series.getBarCount()];
        for (int i = 0; i < series.getBarCount(); i++) {
            originalValues[i] = numericIndicator.getValue(i);
        }

        // Invoke squared()
        numericIndicator.squared();

        // Verify original values are unchanged
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(originalValues[i], numericIndicator.getValue(i),
                    () -> "Original indicator mutated at index " + i);
        }
    }

    @Test
    void doubleSquaredYieldsFourthPower() {
        NumericIndicator firstSquare = numericIndicator.squared(); // x^2
        NumericIndicator fourthPower = firstSquare.squared();      // (x^2)^2 = x^4

        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.pow(4); // Num supports pow()
            Num actual = fourthPower.getValue(i);
            assertEquals(expected, actual,
                    () -> "Fourth‑power mismatch at index " + i + ": expected " + expected + " but was " + actual);
        }
    }

    @Test
    void compositionWithMultipliedByWorksCorrectly() {
        // (2 * x) squared = 4 * x^2
        NumericIndicator multiplied = numericIndicator.multipliedBy(2);
        NumericIndicator composed = multiplied.squared();

        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.multipliedBy(x).multipliedBy(4); // 4 * x^2
            Num actual = composed.getValue(i);
            assertEquals(expected, actual,
                    () -> "Composed square mismatch at index " + i);
        }
    }

    @Test
    void squaredPropagatesBarSeriesReference() {
        NumericIndicator squared = numericIndicator.squared();
        assertSame(series, squared.getBarSeries(),
                "BarSeries reference should be propagated unchanged by squared()");
    }

    @Test
    void squaredPropagatesUnstableBarsCount() {
        // Unstable bars are defined by the underlying indicator; for ClosePriceIndicator it is 0.
        NumericIndicator squared = numericIndicator.squared();
        assertEquals(numericIndicator.getUnstableBars(), squared.getUnstableBars(),
                "Unstable bars count must be identical after squared()");
    }

    @Test
    void emptySeriesThrowsIndexOutOfBoundsOnGetValue() {
        BarSeries emptySeries = new MockBarSeries(NUM_FUNCTION); // no data points
        ClosePriceIndicator emptyDelegate = new ClosePriceIndicator(emptySeries);
        NumericIndicator emptyNumeric = NumericIndicator.of(emptyDelegate);
        NumericIndicator squared = emptyNumeric.squared();

        assertThrows(IndexOutOfBoundsException.class,
                () -> squared.getValue(0),
                "Accessing any index on an empty series must throw IndexOutOfBoundsException");
    }

    @ParameterizedTest(name = "large number {0} squared")
    @ValueSource(strings = {"10000000000"}) // 1e10
    void largeNumberDoesNotOverflow(String valueStr) {
        Num large = DecimalNum.valueOf(valueStr);
        Indicator<Num> mock = mock(Indicator.class);
        when(mock.getValue(0)).thenReturn(large);
        when(mock.getBarSeries()).thenReturn(series);
        when(mock.getUnstableBars()).thenReturn(0);

        NumericIndicator indicator = NumericIndicator.of(mock);
        NumericIndicator squared = indicator.squared();

        Num expected = large.multipliedBy(large); // should be 1e20
        Num actual = squared.getValue(0);
        assertEquals(expected, actual, "Large number squaring produced unexpected result");
    }

    @Test
    void fractionalNumbersPreservePrecision() {
        // Series: [1.5, -2.3]
        BarSeries fractionalSeries = new MockBarSeries(
                NUM_FUNCTION,
                1.5, -2.3);
        ClosePriceIndicator fractionalDelegate = new ClosePriceIndicator(fractionalSeries);
        NumericIndicator indicator = NumericIndicator.of(fractionalDelegate);
        NumericIndicator squared = indicator.squared();

        Num[] expected = {
                DecimalNum.valueOf(1.5).multipliedBy(DecimalNum.valueOf(1.5)),   // 2.25
                DecimalNum.valueOf(-2.3).multipliedBy(DecimalNum.valueOf(-2.3)) // 5.29
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], squared.getValue(i),
                    () -> "Fractional squaring mismatch at index " + i);
        }
    }

    @Test
    void nullDelegateCausesNullPointerException() {
        // Create a spy that returns null for delegate()
        NumericIndicator spy = spy(NumericIndicator.of(closePriceIndicator));
        doReturn(null).when(spy).delegate();

        assertThrows(NullPointerException.class,
                spy::squared,
                "Calling squared() on a NumericIndicator with a null delegate should throw NPE");
    }

    @Test
    void delegateReturnsNullNumThrowsNPE() {
        @SuppressWarnings("unchecked")
        Indicator<Num> nullNumDelegate = mock(Indicator.class);
        when(nullNumDelegate.getValue(anyInt())).thenReturn(null);
        when(nullNumDelegate.getBarSeries()).thenReturn(series);
        when(nullNumDelegate.getUnstableBars()).thenReturn(0);

        NumericIndicator indicator = NumericIndicator.of(nullNumDelegate);
        NumericIndicator squared = indicator.squared();

        assertThrows(NullPointerException.class,
                () -> squared.getValue(0),
                "Squaring when delegate returns null Num must throw NPE");
    }

    @Test
    void delegateRuntimeExceptionIsPropagated() {
        @SuppressWarnings("unchecked")
        Indicator<Num> explodingDelegate = mock(Indicator.class);
        when(explodingDelegate.getValue(anyInt())).thenThrow(new ArithmeticException("boom"));
        when(explodingDelegate.getBarSeries()).thenReturn(series);
        when(explodingDelegate.getUnstableBars()).thenReturn(0);

        NumericIndicator indicator = NumericIndicator.of(explodingDelegate);
        NumericIndicator squared = indicator.squared();

        ArithmeticException ex = assertThrows(ArithmeticException.class,
                () -> squared.getValue(0),
                "Exception from delegate should propagate unchanged");
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void mixedValuesAreHandledCorrectly() {
        // Series: [0, -3, 5, -1.2]
        BarSeries mixedSeries = new MockBarSeries(
                NUM_FUNCTION,
                0, -3, 5, -1.2);
        ClosePriceIndicator mixedDelegate = new ClosePriceIndicator(mixedSeries);
        NumericIndicator indicator = NumericIndicator.of(mixedDelegate);
        NumericIndicator squared = indicator.squared();

        Num[] expected = {
                DecimalNum.valueOf(0).multipliedBy(DecimalNum.valueOf(0)),          // 0
                DecimalNum.valueOf(-3).multipliedBy(DecimalNum.valueOf(-3)),        // 9
                DecimalNum.valueOf(5).multipliedBy(DecimalNum.valueOf(5)),          // 25
                DecimalNum.valueOf(-1.2).multipliedBy(DecimalNum.valueOf(-1.2))     // 1.44
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], squared.getValue(i),
                    () -> "Mixed value squaring mismatch at index " + i);
        }
    }
}
```