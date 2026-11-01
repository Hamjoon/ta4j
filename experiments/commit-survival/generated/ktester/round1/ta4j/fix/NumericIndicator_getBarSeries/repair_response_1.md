```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBar;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Tests for {@link NumericIndicator#getBarSeries()}.
 * <p>
 * The tests are written without external testing frameworks (JUnit, Mockito) to
 * keep the source self‑contained and compilable in the current environment.
 */
class NumericIndicator_getBarSeries_Test {

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
            return null;
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public int getBarCount() {
            return series == null ? 0 : series.getBarCount();
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
            return null;
        }

        @Override
        public BarSeries getBarSeries() {
            return (call++ == 0) ? first : second;
        }

        @Override
        public int getBarCount() {
            return 0;
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
            return null;
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
        public int getBarCount() {
            return 0;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /* --------------------------------------------------------------------- *
     *                     Simple assertion helpers                           *
     * --------------------------------------------------------------------- */
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

    /* --------------------------------------------------------------------- *
     *                                 Tests                                 *
     * --------------------------------------------------------------------- */
    public static void main(String[] args) throws Exception {
        shouldReturnDelegateBarSeries();
        shouldReturnSameBarSeriesOnRepeatedCalls();
        shouldPropagateNullBarSeriesFromDelegate();
        shouldForwardDynamicBarSeriesInstances();
        shouldPropagateRuntimeExceptionFromDelegate();
        shouldPropagateIllegalArgumentExceptionFromDelegate();
        shouldPropagateAssertionErrorFromDelegate();
        shouldExposeLiveReferenceWhenSeriesIsMutated();
        shouldThrowNullPointerExceptionWhenDelegateIsNull();
        System.out.println("All tests passed.");
    }

    static void shouldReturnDelegateBarSeries() {
        BarSeries mockSeries = new BaseBarSeries("mock-series");
        Indicator<Num> delegate = new SimpleIndicator(mockSeries);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        BarSeries result = numericIndicator.getBarSeries();
        assertSame(mockSeries, result,
                "NumericIndicator#getBarSeries() must return the BarSeries supplied by its delegate");
    }

    static void shouldReturnSameBarSeriesOnRepeatedCalls() {
        BarSeries series = new BaseBarSeries("series");
        Indicator<Num> delegate = new SimpleIndicator(series);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        BarSeries firstCall = numericIndicator.getBarSeries();
        BarSeries secondCall = numericIndicator.getBarSeries();
        assertSame(firstCall, secondCall,
                "Repeated calls to getBarSeries() should return the same instance");
    }

    static void shouldPropagateNullBarSeriesFromDelegate() {
        Indicator<Num> delegate = new SimpleIndicator(null);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        BarSeries result = numericIndicator.getBarSeries();
        assertNull(result,
                "When the delegate returns null, NumericIndicator#getBarSeries() must also return null");
    }

    static void shouldForwardDynamicBarSeriesInstances() {
        BarSeries first = new BaseBarSeries("first");
        BarSeries second = new BaseBarSeries("second");
        Indicator<Num> delegate = new ChangingIndicator(first, second);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        BarSeries call1 = numericIndicator.getBarSeries();
        BarSeries call2 = numericIndicator.getBarSeries();
        assertSame(first, call1, "First call should return the first BarSeries instance");
        assertSame(second, call2, "Second call should return the second (different) BarSeries instance");
        assertNotSame(call1, call2, "When delegate supplies new instances, NumericIndicator must not cache them");
    }

    static void shouldPropagateRuntimeExceptionFromDelegate() {
        RuntimeException ex = new RuntimeException("delegate failure");
        Indicator<Num> delegate = new ExceptionThrowingIndicator(ex);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        assertThrows(RuntimeException.class, numericIndicator::getBarSeries,
                "NumericIndicator#getBarSeries() must let RuntimeException from delegate bubble up");
    }

    static void shouldPropagateIllegalArgumentExceptionFromDelegate() {
        IllegalArgumentException ex = new IllegalArgumentException("bad argument");
        Indicator<Num> delegate = new ExceptionThrowingIndicator(ex);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        assertThrows(IllegalArgumentException.class, numericIndicator::getBarSeries,
                "IllegalArgumentException from delegate must be propagated unchanged");
    }

    static void shouldPropagateAssertionErrorFromDelegate() {
        AssertionError err = new AssertionError("assertion failed");
        Indicator<Num> delegate = new ExceptionThrowingIndicator(err);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        assertThrows(AssertionError.class, numericIndicator::getBarSeries,
                "AssertionError from delegate must be propagated unchanged");
    }

    static void shouldExposeLiveReferenceWhenSeriesIsMutated() {
        // real mutable series
        BaseBarSeries realSeries = new BaseBarSeries("test-series");
        Indicator<Num> delegate = new SimpleIndicator(realSeries);
        NumericIndicator numericIndicator = NumericIndicator.of(delegate);
        BarSeries obtained = numericIndicator.getBarSeries();
        assertSame(realSeries, obtained,
                "Initial call must return the concrete BarSeries instance");

        // add a dummy bar (using primitive double values – BaseBar does not accept DecimalNum)
        BaseBar dummyBar = new BaseBar(
                Duration.ofSeconds(1),
                ZonedDateTime.now(),
                1.0,   // open
                100.0, // high
                110.0, // low
                90.0,  // close
                105.0  // volume
        );
        realSeries.addBar(dummyBar);

        BarSeries afterMutation = numericIndicator.getBarSeries();
        assertEquals(realSeries.getBarCount(), afterMutation.getBarCount(),
                "After mutation, getBarSeries() must return the same live BarSeries instance");
        assertSame(realSeries, afterMutation,
                "Reference equality must be preserved after the underlying series is mutated");
    }

    static void shouldThrowNullPointerExceptionWhenDelegateIsNull() throws Exception {
        Constructor<NumericIndicator> ctor = NumericIndicator.class.getDeclaredConstructor(Indicator.class);
        ctor.setAccessible(true);
        NumericIndicator nullDelegateIndicator = ctor.newInstance((Object) null);
        assertThrows(NullPointerException.class, nullDelegateIndicator::getBarSeries,
                "Calling getBarSeries() on a NumericIndicator constructed with a null delegate must throw NPE");
    }
}
```