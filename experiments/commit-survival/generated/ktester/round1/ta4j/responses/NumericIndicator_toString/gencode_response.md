```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;

import java.lang.reflect.Constructor;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive tests for {@link NumericIndicator#toString()}.
 *
 * <p>The {@code toString()} method must simply delegate to the wrapped
 * {@link Indicator}. The test suite covers a wide range of delegate
 * behaviours: constant indicators, custom implementations, Mockito mocks,
 * nested {@code NumericIndicator}s, mutable delegates, null delegates,
 * delegates that return {@code null}, lambda‑based delegates and delegates
 * that throw various {@link Throwable}s.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_toString_Test {

    @Mock
    private Indicator<Num> mockDelegate; // used in a few tests

    /* --------------------------------------------------------------------- */
    /* Helper classes used in several test cases                           */
    /* --------------------------------------------------------------------- */

    /** Simple mutable indicator whose {@code toString()} reflects an internal field. */
    private static class MutableIndicator implements Indicator<Num> {
        private String representation;

        MutableIndicator(String initial) {
            this.representation = initial;
        }

        void setRepresentation(String newRep) {
            this.representation = newRep;
        }

        @Override
        public String toString() {
            return representation;
        }

        // Minimal stub implementations – they are never used by the tests.
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
        @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
    }

    /** Indicator whose {@code toString()} always returns {@code null}. */
    private static class NullStringIndicator implements Indicator<Num> {
        @Override public String toString() { return null; }
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
        @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
    }

    /** Indicator that throws a supplied {@link RuntimeException} from {@code toString()}. */
    private static class ThrowingRuntimeExceptionIndicator implements Indicator<Num> {
        private final RuntimeException toThrow;
        ThrowingRuntimeExceptionIndicator(RuntimeException toThrow) { this.toThrow = toThrow; }
        @Override public String toString() { throw toThrow; }
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
        @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
    }

    /** Indicator that throws a supplied {@link Error} from {@code toString()}. */
    private static class ThrowingErrorIndicator implements Indicator<Num> {
        private final Error toThrow;
        ThrowingErrorIndicator(Error toThrow) { this.toThrow = toThrow; }
        @Override public String toString() { throw toThrow; }
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
        @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
    }

    /** User‑defined unchecked exception for testing propagation. */
    private static class CustomUncheckedException extends RuntimeException {
        CustomUncheckedException(String msg) { super(msg); }
    }

    /* --------------------------------------------------------------------- */
    /* Lifecycle methods – keep them untouched as per the requirement       */
    /* --------------------------------------------------------------------- */

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // MockitoExtension already initialises @Mock fields before each test.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    /* --------------------------------------------------------------------- */
    /* Test cases – one method per logical group                           */
    /* --------------------------------------------------------------------- */

    @Test
    void toString_delegatesToUnderlyingIndicator() {
        // GIVEN
        String expected = "MockIndicatorDelegate";
        when(mockDelegate.toString()).thenReturn(expected);

        // WHEN
        NumericIndicator numericIndicator = NumericIndicator.of(mockDelegate);

        // THEN
        assertEquals(expected, numericIndicator.toString(),
                "NumericIndicator.toString() must delegate to the underlying Indicator");
    }

    @Test
    void toString_constantIndicatorDelegate() {
        // GIVEN a ConstantIndicator (provided by the library) as delegate
        Indicator<Num> constant = new org.ta4j.core.indicators.helpers.ConstantIndicator<>(DecimalNum.valueOf(42));
        String expected = constant.toString();

        // WHEN
        NumericIndicator ni = NumericIndicator.of(constant);

        // THEN
        assertEquals(expected, ni.toString(),
                "toString() should return the ConstantIndicator's string representation");
    }

    @Test
    void toString_customIndicatorDelegate() {
        // GIVEN a custom Indicator with a fixed toString()
        Indicator<Num> custom = new Indicator<Num>() {
            @Override public String toString() { return "MyCustomIndicator"; }
            @Override public BarSeries getBarSeries() { return null; }
            @Override public int getUnstableBars() { return 0; }
            @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
        };
        String expected = "MyCustomIndicator";

        // WHEN
        NumericIndicator ni = NumericIndicator.of(custom);

        // THEN
        assertEquals(expected, ni.toString(),
                "toString() must return the custom Indicator's string");
    }

    @Test
    void toString_nestedNumericIndicator() {
        // GIVEN: inner NumericIndicator wraps a ConstantIndicator
        Indicator<Num> inner = NumericIndicator.of(
                new org.ta4j.core.indicators.helpers.ConstantIndicator<>(DecimalNum.valueOf(7))
        );
        // OUTER wraps the inner NumericIndicator
        NumericIndicator outer = NumericIndicator.of(inner);

        // WHEN
        String result = outer.toString();

        // THEN
        assertEquals(inner.toString(), result,
                "Outer NumericIndicator should delegate through inner NumericIndicator");
    }

    @Test
    void toString_mutableDelegateReflectsStateChange() {
        // GIVEN a mutable delegate whose toString() reflects an internal field
        MutableIndicator mutable = new MutableIndicator("initial");
        NumericIndicator ni = NumericIndicator.of(mutable);

        // sanity check initial state
        assertEquals("initial", ni.toString());

        // WHEN the internal state changes
        mutable.setRepresentation("updated");

        // THEN the NumericIndicator reflects the new string (no caching)
        assertEquals("updated", ni.toString(),
                "NumericIndicator.toString() must reflect delegate's updated state");
    }

    @Test
    void toString_delegateReturnsNull() {
        // GIVEN a delegate whose toString() returns null
        Indicator<Num> nullStringDelegate = new NullStringIndicator();

        // WHEN
        NumericIndicator ni = NumericIndicator.of(nullStringDelegate);
        String result = ni.toString();

        // THEN
        assertNull(result, "NumericIndicator.toString() should return null when delegate returns null");
    }

    @Test
    void toString_delegateIsLambda() {
        // GIVEN a delegate created via an anonymous class (no overridden toString())
        Indicator<Num> lambdaDelegate = new Indicator<Num>() {
            @Override public BarSeries getBarSeries() { return null; }
            @Override public int getUnstableBars() { return 0; }
            @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
            // No toString() override – inherits Object's implementation
        };
        String expected = lambdaDelegate.toString(); // default Object.toString()

        // WHEN
        NumericIndicator ni = NumericIndicator.of(lambdaDelegate);
        String actual = ni.toString();

        // THEN
        assertEquals(expected, actual,
                "NumericIndicator should return the default Object.toString() of a lambda/anonymous delegate");
    }

    @Test
    void toString_delegateThrowsRuntimeException() {
        // GIVEN a delegate that throws a generic RuntimeException from toString()
        RuntimeException ex = new RuntimeException("boom");
        Indicator<Num> throwing = new ThrowingRuntimeExceptionIndicator(ex);

        // WHEN / THEN
        NumericIndicator ni = NumericIndicator.of(throwing);
        RuntimeException thrown = assertThrows(RuntimeException.class, ni::toString,
                "Exception from delegate's toString() must propagate");
        assertSame(ex, thrown, "The same RuntimeException instance should be propagated");
    }

    @Test
    void toString_nullDelegateThrowsNPE() throws Exception {
        // GIVEN a NumericIndicator constructed via reflection with a null delegate
        Constructor<NumericIndicator> ctor = NumericIndicator.class
                .getDeclaredConstructor(Indicator.class);
        ctor.setAccessible(true);
        NumericIndicator ni = ctor.newInstance((Indicator<Num>) null);

        // WHEN / THEN
        assertThrows(NullPointerException.class, ni::toString,
                "Calling toString() on a NumericIndicator with null delegate should throw NPE");
    }

    @Test
    void toString_delegateThrowsSpecificRuntimeExceptions() {
        // IllegalArgumentException case
        IllegalArgumentException iae = new IllegalArgumentException("bad arg");
        Indicator<Num> illegalArgDelegate = new ThrowingRuntimeExceptionIndicator(iae);
        NumericIndicator niIae = NumericIndicator.of(illegalArgDelegate);
        IllegalArgumentException thrownIae = assertThrows(IllegalArgumentException.class,
                niIae::toString, "IllegalArgumentException must propagate unchanged");
        assertSame(iae, thrownIae);

        // Custom unchecked exception case
        CustomUncheckedException cue = new CustomUncheckedException("custom");
        Indicator<Num> customDelegate = new ThrowingRuntimeExceptionIndicator(cue);
        NumericIndicator niCue = NumericIndicator.of(customDelegate);
        CustomUncheckedException thrownCue = assertThrows(CustomUncheckedException.class,
                niCue::toString, "Custom unchecked exception must propagate unchanged");
        assertSame(cue, thrownCue);
    }

    @Test
    void toString_delegateThrowsErrors() {
        // AssertionError case
        AssertionError assertionError = new AssertionError("assertion failed");
        Indicator<Num> assertionDelegate = new ThrowingErrorIndicator(assertionError);
        NumericIndicator niAssert = NumericIndicator.of(assertionDelegate);
        AssertionError thrownAssert = assertThrows(AssertionError.class,
                niAssert::toString, "AssertionError must propagate unchanged");
        assertSame(assertionError, thrownAssert);

        // OutOfMemoryError case
        OutOfMemoryError oomError = new OutOfMemoryError("oom");
        Indicator<Num> oomDelegate = new ThrowingErrorIndicator(oomError);
        NumericIndicator niOom = NumericIndicator.of(oomDelegate);
        OutOfMemoryError thrownOom = assertThrows(OutOfMemoryError.class,
                niOom::toString, "OutOfMemoryError must propagate unchanged");
        assertSame(oomError, thrownOom);
    }
}
```