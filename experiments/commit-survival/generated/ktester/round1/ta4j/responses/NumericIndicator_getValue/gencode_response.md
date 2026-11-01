```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.mockito.Mock;

/**
 * Comprehensive tests for {@link NumericIndicator#getValue(int)}.
 *
 * <p>The original simple delegation test is kept, and a large set of
 * additional scenarios (exception propagation, null handling, special
 * {@link Num} values, reference equality, mutable delegate state, etc.) are
 * covered using Mockito‑based mocks.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_getValue_Test {

    /** Series used by the “real‑delegate” tests – created once for the whole test class. */
    private static BarSeries series;

    /** Close‑price indicator that serves as the delegate for the real‑delegate tests. */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator instance under test for the real‑delegate tests. */
    private static NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Deterministic data set – the same values used in the original test suite.
        series = new MockBarSeries(
                DecimalNum::valueOf,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );

        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
    }

    /* --------------------------------------------------------------------- *
     *  1️⃣  Simple delegation test (already present in the original suite) *
     * --------------------------------------------------------------------- */
    @Test
    void getValue_delegatesToUnderlyingIndicator() {
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i);
            Num actual = numericIndicator.getValue(i);
            assertEquals(expected, actual,
                    "NumericIndicator.getValue(" + i + ") should delegate to ClosePriceIndicator");
        }
    }

    /* --------------------------------------------------------------------- *
     *  2️⃣  Parameterised tests for the remaining groups (mocked delegate)   *
     * --------------------------------------------------------------------- */

    // --------------------------------------------------------------------- //
    //   2.1  Delegation – normal values, null, special Num implementations   //
    // --------------------------------------------------------------------- //
    @ParameterizedTest(name = "{index} ⇒ index={0}, description={2}")
    @MethodSource("provideNormalAndSpecialValues")
    void getValue_normalAndSpecialValues(int index,
                                         Num expectedNum,
                                         String description) {
        // Mock delegate that returns the supplied Num (may be null or special)
        @SuppressWarnings("unchecked")
        Indicator<Num> mockDelegate = mock(Indicator.class);
        when(mockDelegate.getValue(index)).thenReturn(expectedNum);

        NumericIndicator ni = NumericIndicator.of(mockDelegate);
        Num actual = ni.getValue(index);
        assertSame(expectedNum, actual,
                "For index " + index + " (" + description + ") the returned Num must be the exact same instance.");
    }

    private static Stream<Arguments> provideNormalAndSpecialValues() {
        return Stream.of(
                // regular value
                Arguments.of(0, DecimalNum.valueOf(1), "regular value at index 0"),
                // zero value
                Arguments.of(2, DecimalNum.valueOf(0), "zero value at index 2"),
                // null value
                Arguments.of(3, null, "null value returned by delegate"),
                // different Num implementation (mocked Num)
                Arguments.of(4, mock(Num.class), "custom Num implementation (mock)"),
                // NaN
                Arguments.of(6, DecimalNum.NaN, "NaN value"),
                // Positive infinity
                Arguments.of(7, DecimalNum.POSITIVE_INFINITY, "positive infinity")
        );
    }

    // --------------------------------------------------------------------- //
    //   2.2  Exception propagation (IndexOutOfBounds, custom unchecked)    //
    // --------------------------------------------------------------------- //
    @ParameterizedTest(name = "{index} ⇒ index={0}, expected={1}")
    @MethodSource("provideExceptionScenarios")
    void getValue_exceptionPropagation(int index,
                                       Class<? extends Throwable> expectedException,
                                       String description) {
        @SuppressWarnings("unchecked")
        Indicator<Num> mockDelegate = mock(Indicator.class);
        when(mockDelegate.getValue(index)).thenThrow(expectedException);

        NumericIndicator ni = NumericIndicator.of(mockDelegate);
        assertThrows(expectedException, () -> ni.getValue(index),
                "Index " + index + " (" + description + ") must propagate " + expectedException.getSimpleName());
    }

    private static Stream<Arguments> provideExceptionScenarios() {
        return Stream.of(
                Arguments.of(-1, IndexOutOfBoundsException.class, "negative index"),
                Arguments.of(10, IndexOutOfBoundsException.class, "index equal to series size"),
                Arguments.of(Integer.MAX_VALUE, IndexOutOfBoundsException.class, "extreme integer index"),
                Arguments.of(1, CustomRuntimeException.class, "custom unchecked exception")
        );
    }

    // --------------------------------------------------------------------- //
    //   2.3  Reference‑equality verification                               //
    // --------------------------------------------------------------------- //
    @Test
    void getValue_referenceEquality() {
        Num specialNum = DecimalNum.valueOf(42);
        @SuppressWarnings("unchecked")
        Indicator<Num> mockDelegate = mock(Indicator.class);
        when(mockDelegate.getValue(2)).thenReturn(specialNum);

        NumericIndicator ni = NumericIndicator.of(mockDelegate);
        Num actual = ni.getValue(2);
        assertSame(specialNum, actual,
                "The returned Num must be the exact same object instance as provided by the delegate.");
    }

    // --------------------------------------------------------------------- //
    //   2.4  Immutable snapshot with mutable delegate state                  //
    // --------------------------------------------------------------------- //
    @Test
    void getValue_immutableSnapshotDespiteMutableDelegate() {
        // Use an AtomicReference to simulate mutable internal state of the delegate.
        java.util.concurrent.atomic.AtomicReference<Num> mutableRef = new java.util.concurrent.atomic.AtomicReference<>(DecimalNum.valueOf(55));

        @SuppressWarnings("unchecked")
        Indicator<Num> mockDelegate = mock(Indicator.class);
        when(mockDelegate.getValue(3)).thenAnswer(invocation -> mutableRef.get());

        NumericIndicator ni = NumericIndicator.of(mockDelegate);

        // First call – should capture 55
        Num first = ni.getValue(3);
        assertEquals(DecimalNum.valueOf(55), first, "First call must return the initial value 55.");

        // Change the underlying mutable reference
        mutableRef.set(DecimalNum.valueOf(99));

        // Second call – the delegate now returns 99, but we assert that the *first* result stayed unchanged.
        Num second = ni.getValue(3);
        assertEquals(DecimalNum.valueOf(99), second, "Second call reflects the mutated delegate state.");
        assertEquals(DecimalNum.valueOf(55), first, "Previously captured value must remain 55 (no caching).");
    }

    // --------------------------------------------------------------------- //
    //   2.5  Null delegate propagation (NumericIndicator.of(null))        //
    // --------------------------------------------------------------------- //
    @Test
    void getValue_nullDelegateThrowsNPE() {
        NumericIndicator ni = NumericIndicator.of(null);
        assertThrows(NullPointerException.class, () -> ni.getValue(0),
                "Calling getValue on a NumericIndicator with a null delegate must throw NPE.");
    }

    /* --------------------------------------------------------------------- *
     *  Helper exception class for the custom unchecked‑exception test.     *
     * --------------------------------------------------------------------- */
    static class CustomRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        CustomRuntimeException(String message) { super(message); }
    }
}
```