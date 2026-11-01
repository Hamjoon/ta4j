```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Constructor;

/**
 * Tests for {@link NumericIndicator#toString()} behaviour.
 */
class NumericIndicatorToStringTest {

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

        // Stub implementations – never used in the tests.
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

        ThrowingRuntimeExceptionIndicator(RuntimeException toThrow) {
            this.toThrow = toThrow;
        }

        @Override public String toString() { throw toThrow; }
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
        @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
    }

    /** Indicator that throws a supplied {@link Error} from {@code toString()}. */
    private static class ThrowingErrorIndicator implements Indicator<Num> {
        private final Error toThrow;

        ThrowingErrorIndicator(Error toThrow) {
            this.toThrow = toThrow;
        }

        @Override public String toString() { throw toThrow; }
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
        @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
    }

    /** User‑defined unchecked exception for testing propagation. */
    private static class CustomUncheckedException extends RuntimeException {
        CustomUncheckedException(String msg) {
            super(msg);
        }
    }

    @Test
    void toString_delegatesToUnderlyingIndicator() {
        Indicator<Num> delegate = new Indicator<Num>() {
            @Override public String toString() { return "MockIndicatorDelegate"; }
            @Override public BarSeries getBarSeries() { return null; }
            @Override public int getUnstableBars() { return 0; }
            @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
        };
        String expectedString = "MockIndicatorDelegate";
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        Assertions.assertEquals(expectedString, numericIndicator.toString(),
                "NumericIndicator.toString() must delegate to the underlying Indicator");
    }

    @Test
    void toString_constantIndicatorDelegate() {
        Indicator<Num> constant = new ConstantIndicator<>(DecimalNum.valueOf(42));
        String expected = constant.toString();
        NumericIndicator ni = NumericIndicator.of(constant);
        Assertions.assertEquals(expected, ni.toString(),
                "toString() should return the ConstantIndicator's string representation");
    }

    @Test
    void toString_customIndicatorDelegate() {
        Indicator<Num> custom = new Indicator<Num>() {
            @Override public String toString() { return "MyCustomIndicator"; }
            @Override public BarSeries getBarSeries() { return null; }
            @Override public int getUnstableBars() { return 0; }
            @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
        };
        String expected = "MyCustomIndicator";
        NumericIndicator ni = NumericIndicator.of(custom);
        Assertions.assertEquals(expected, ni.toString(),
                "toString() must return the custom Indicator's string");
    }

    @Test
    void toString_nestedNumericIndicator() {
        Indicator<Num> inner = NumericIndicator.of(new ConstantIndicator<>(DecimalNum.valueOf(7)));
        NumericIndicator outer = NumericIndicator.of(inner);
        String result = outer.toString();
        Assertions.assertEquals(inner.toString(), result,
                "Outer NumericIndicator should delegate through inner NumericIndicator");
    }

    @Test
    void toString_mutableDelegateReflectsStateChange() {
        MutableIndicator mutable = new MutableIndicator("initial");
        NumericIndicator ni = NumericIndicator.of(mutable);
        Assertions.assertEquals("initial", ni.toString());
        mutable.setRepresentation("updated");
        Assertions.assertEquals("updated", ni.toString(),
                "NumericIndicator.toString() must reflect delegate's updated state");
    }

    @Test
    void toString_delegateReturnsNull() {
        Indicator<Num> nullStringDelegate = new NullStringIndicator();
        NumericIndicator ni = NumericIndicator.of(nullStringDelegate);
        String result = ni.toString();
        Assertions.assertNull(result,
                "NumericIndicator.toString() should return null when delegate returns null");
    }

    @Test
    void toString_delegateIsAnonymous() {
        Indicator<Num> anonymousDelegate = new Indicator<Num>() {
            @Override public BarSeries getBarSeries() { return null; }
            @Override public int getUnstableBars() { return 0; }
            @Override public Num getValue(int index) { return DecimalNum.valueOf(0); }
        };
        String expected = anonymousDelegate.toString(); // default Object.toString()
        NumericIndicator ni = NumericIndicator.of(anonymousDelegate);
        String actual = ni.toString();
        Assertions.assertEquals(expected, actual,
                "NumericIndicator should return the default Object.toString() of an anonymous delegate");
    }

    @Test
    void toString_delegateThrowsRuntimeException() {
        RuntimeException ex = new RuntimeException("boom");
        Indicator<Num> throwing = new ThrowingRuntimeExceptionIndicator(ex);
        NumericIndicator ni = NumericIndicator.of(throwing);
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, ni::toString,
                "Exception from delegate's toString() must propagate");
        Assertions.assertSame(ex, thrown, "The same RuntimeException instance should be propagated");
    }

    @Test
    void toString_nullDelegateThrowsNPE() throws Exception {
        Constructor<NumericIndicator> ctor = NumericIndicator.class.getDeclaredConstructor(Indicator.class);
        ctor.setAccessible(true);
        NumericIndicator ni = ctor.newInstance((Indicator<Num>) null);
        Assertions.assertThrows(NullPointerException.class, ni::toString,
                "Calling toString() on a NumericIndicator with null delegate should throw NPE");
    }

    @Test
    void toString_delegateThrowsSpecificRuntimeExceptions() {
        IllegalArgumentException iae = new IllegalArgumentException("bad arg");
        Indicator<Num> illegalArgDelegate = new ThrowingRuntimeExceptionIndicator(iae);
        NumericIndicator niIae = NumericIndicator.of(illegalArgDelegate);
        IllegalArgumentException thrownIae = Assertions.assertThrows(IllegalArgumentException.class, niIae::toString,
                "IllegalArgumentException must propagate unchanged");
        Assertions.assertSame(iae, thrownIae, "IllegalArgumentException instance should be identical");

        CustomUncheckedException cue = new CustomUncheckedException("custom");
        Indicator<Num> customDelegate = new ThrowingRuntimeExceptionIndicator(cue);
        NumericIndicator niCue = NumericIndicator.of(customDelegate);
        CustomUncheckedException thrownCue = Assertions.assertThrows(CustomUncheckedException.class, niCue::toString,
                "Custom unchecked exception must propagate unchanged");
        Assertions.assertSame(cue, thrownCue, "CustomUncheckedException instance should be identical");
    }

    @Test
    void toString_delegateThrowsErrors() {
        AssertionError assertionError = new AssertionError("assertion failed");
        Indicator<Num> assertionDelegate = new ThrowingErrorIndicator(assertionError);
        NumericIndicator niAssert = NumericIndicator.of(assertionDelegate);
        AssertionError thrownAssert = Assertions.assertThrows(AssertionError.class, niAssert::toString,
                "AssertionError must propagate unchanged");
        Assertions.assertSame(assertionError, thrownAssert, "AssertionError instance should be identical");

        OutOfMemoryError oomError = new OutOfMemoryError("oom");
        Indicator<Num> oomDelegate = new ThrowingErrorIndicator(oomError);
        NumericIndicator niOom = NumericIndicator.of(oomDelegate);
        OutOfMemoryError thrownOom = Assertions.assertThrows(OutOfMemoryError.class, niOom::toString,
                "OutOfMemoryError must propagate unchanged");
        Assertions.assertSame(oomError, thrownOom, "OutOfMemoryError instance should be identical");
    }
}
```