package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBar;
import java.time.Duration;
import java.time.ZonedDateTime;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.junit.jupiter.MockitoExtension;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import static org.mockito.Mockito.when;
// @ExtendWith(MockitoExtension.class)
class NumericIndicator_getBarSeries_Test {

    //     @Mock
    private BarSeries // mocked BarSeries used by the delegate
    mockSeries;

    //     @Mock
    private Indicator<Num> // mocked delegate Indicator
    mockDelegate;

    // instance under test
    private NumericIndicator numericIndicator;

    /**
     * Simple stub that returns a predefined series (or {@code null}).
     */
    private static class SimpleIndicator implements Indicator<Num> {

        private final BarSeries series;

        SimpleIndicator(BarSeries series) {
            this.series = series;
        }

        @Override
        public Num getValue(int index) {
            // The value is irrelevant for the getBarSeries() tests.
            return DecimalNum.ZERO;
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

    /**
     * Indicator that returns two different series on successive calls.
     */
    private static class ChangingIndicator implements Indicator<Num> {

        private final BarSeries first;

        private final BarSeries second;

        private int call = 0;

        ChangingIndicator(BarSeries first, BarSeries second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Num getValue(int index) {
            return DecimalNum.ZERO;
        }

        @Override
        public BarSeries getBarSeries() {
            return (call++ == 0) ? first : second;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /**
     * Indicator whose {@code getBarSeries()} method throws a supplied throwable.
     */
    private static class ExceptionThrowingIndicator implements Indicator<Num> {

        private final Throwable toThrow;

        ExceptionThrowingIndicator(Throwable toThrow) {
            this.toThrow = toThrow;
        }

        @Override
        public Num getValue(int index) {
            return DecimalNum.ZERO;
        }

        @Override
        public BarSeries getBarSeries() {
            if (toThrow instanceof RuntimeException) {
                throw (RuntimeException) toThrow;
            } else if (toThrow instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) toThrow;
            } else if (toThrow instanceof AssertionError) {
                throw (AssertionError) toThrow;
            } else {
                // wrap any other throwable in a RuntimeException
                throw new RuntimeException(toThrow);
            }
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    private static void assertSame(Object expected, Object actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " Expected same instance but got different.");
        }
    }

    private static void assertNotSame(Object a, Object b, String msg) {
        if (a == b) {
            throw new AssertionError(msg + " Expected different instances but both are same.");
        }
    }

    private static void assertNull(Object obj, String msg) {
        if (obj != null) {
            throw new AssertionError(msg);
        }
    }

    private static void assertEquals(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " Expected: " + expected + " but was: " + actual);
        }
    }

    private static <T extends Throwable> void assertThrows(Class<T> expected, Runnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                // success
                return;
            }
            throw new AssertionError(msg + " Thrown " + t.getClass() + " but expected " + expected, t);
        }
        throw new AssertionError(msg + " Expected exception " + expected + " but none was thrown.");
    }

//     @Test
//     void shouldReturnDelegateBarSeries() {
//         BarSeries series = new BaseBarSeries("mock-series");
//         Indicator<Num> delegate = new SimpleIndicator(series);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         BarSeries result = ni.getBarSeries();
//         assertSame(series, result, "NumericIndicator#getBarSeries() must return the BarSeries supplied by its delegate");
//     }

//     @Test
//     void shouldReturnSameBarSeriesOnRepeatedCalls() {
//         BarSeries series = new BaseBarSeries("series");
//         Indicator<Num> delegate = new SimpleIndicator(series);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         BarSeries firstCall = ni.getBarSeries();
//         BarSeries secondCall = ni.getBarSeries();
//         assertSame(firstCall, secondCall, "Repeated calls to getBarSeries() should return the same instance");
//     }

//     @Test
//     void shouldPropagateNullBarSeriesFromDelegate() {
//         Indicator<Num> delegate = new SimpleIndicator(null);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         BarSeries result = ni.getBarSeries();
//         assertNull(result, "When the delegate returns null, NumericIndicator#getBarSeries() must also return null");
//     }

//     @Test
//     void shouldForwardDynamicBarSeriesInstances() {
//         BarSeries first = new BaseBarSeries("first");
//         BarSeries second = new BaseBarSeries("second");
//         Indicator<Num> delegate = new ChangingIndicator(first, second);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         BarSeries call1 = ni.getBarSeries();
//         BarSeries call2 = ni.getBarSeries();
//         assertSame(first, call1, "First call should return the first BarSeries instance");
//         assertSame(second, call2, "Second call should return the second (different) BarSeries instance");
//         assertNotSame(call1, call2, "When delegate supplies new instances, NumericIndicator must not cache them");
//     }

//     @Test
//     void shouldPropagateRuntimeExceptionFromDelegate() {
//         RuntimeException ex = new RuntimeException("delegate failure");
//         Indicator<Num> delegate = new ExceptionThrowingIndicator(ex);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         assertThrows(RuntimeException.class, ni::getBarSeries, "NumericIndicator#getBarSeries() must let RuntimeException from delegate bubble up");
//     }

//     @Test
//     void shouldPropagateIllegalArgumentExceptionFromDelegate() {
//         IllegalArgumentException ex = new IllegalArgumentException("bad argument");
//         Indicator<Num> delegate = new ExceptionThrowingIndicator(ex);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         assertThrows(IllegalArgumentException.class, ni::getBarSeries, "IllegalArgumentException from delegate must be propagated unchanged");
//     }

//     @Test
//     void shouldPropagateAssertionErrorFromDelegate() {
//         AssertionError err = new AssertionError("assertion failed");
//         Indicator<Num> delegate = new ExceptionThrowingIndicator(err);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         assertThrows(AssertionError.class, ni::getBarSeries, "AssertionError from delegate must be propagated unchanged");
//     }

//     @Test
//     void shouldExposeLiveReferenceWhenSeriesIsMutated() {
//         // real mutable series
//         BaseBarSeries realSeries = new BaseBarSeries("test-series");
//         Indicator<Num> delegate = new SimpleIndicator(realSeries);
//         NumericIndicator ni = NumericIndicator.of(delegate);
//         BarSeries obtained = ni.getBarSeries();
//         assertSame(realSeries, obtained, "Initial call must return the concrete BarSeries instance");
//         // add a dummy bar (using primitive double values – BaseBar does not accept DecimalNum)
//         BaseBar dummyBar = new BaseBar(Duration.ofSeconds(1), ZonedDateTime.now(), // open
//         1.0, // high
//         100.0, // low
//         110.0, // close
//         90.0, // volume
//         105.0);
//         realSeries.addBar(dummyBar);
//         BarSeries afterMutation = ni.getBarSeries();
//         assertEquals(realSeries.getBarCount(), afterMutation.getBarCount(), "After mutation, getBarSeries() must return the same live BarSeries instance");
//         assertSame(realSeries, afterMutation, "Reference equality must be preserved after the underlying series is mutated");
//     }

//     @Test
//     void shouldThrowNullPointerExceptionWhenDelegateIsNull() throws Exception {
//         // Use reflection to invoke the private constructor that accepts a delegate.
//         var ctor = NumericIndicator.class.getDeclaredConstructor(Indicator.class);
//         ctor.setAccessible(true);
//         NumericIndicator nullDelegateIndicator = ctor.newInstance((Object) null);
//         assertThrows(NullPointerException.class, nullDelegateIndicator::getBarSeries, "Calling getBarSeries() on a NumericIndicator constructed with a null delegate must throw NPE");
//     }
}