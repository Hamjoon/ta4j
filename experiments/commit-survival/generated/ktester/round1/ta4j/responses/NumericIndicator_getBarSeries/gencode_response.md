```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link NumericIndicator#getBarSeries()}.
 *
 * <p>The original test suite already covered the basic delegation scenario.
 * The additional tests below address the full matrix of cases described in the
 * supplied JSON – null‑delegate handling, exception propagation, dynamic
 * delegate behaviour, and reference‑equality guarantees.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_getBarSeries_Test {

    @Mock
    private BarSeries mockSeries;          // mocked BarSeries used by the delegate
    @Mock
    private Indicator<Num> mockDelegate; // mocked delegate Indicator

    private NumericIndicator numericIndicator; // instance under test

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for this test suite
    }

    @BeforeEach
    void setupBeforeEach() {
        // Stub the delegate to return the mocked BarSeries
        when(mockDelegate.getBarSeries()).thenReturn(mockSeries);
        // Build the NumericIndicator using the public factory method
        numericIndicator = NumericIndicator.of(mockDelegate);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    /* --------------------------------------------------------------
     *  Existing simple delegation tests (kept unchanged)
     * -------------------------------------------------------------- */
    @Test
    void shouldReturnDelegateBarSeries() {
        // Act
        BarSeries result = numericIndicator.getBarSeries();

        // Assert
        assertSame(mockSeries, result,
                "NumericIndicator#getBarSeries() must return the BarSeries supplied by its delegate");
    }

    @Test
    void shouldReturnSameBarSeriesOnRepeatedCalls() {
        // Act
        BarSeries firstCall = numericIndicator.getBarSeries();
        BarSeries secondCall = numericIndicator.getBarSeries();

        // Assert
        assertSame(firstCall, secondCall,
                "Repeated calls to getBarSeries() should return the same instance");
    }

    /* --------------------------------------------------------------
     *  Additional scenarios derived from the JSON test matrix
     * -------------------------------------------------------------- */

    @Test
    void shouldPropagateNullBarSeriesFromDelegate() {
        // Arrange: delegate returns null
        when(mockDelegate.getBarSeries()).thenReturn(null);
        // Act
        BarSeries result = numericIndicator.getBarSeries();
        // Assert
        assertNull(result, "When the delegate returns null, NumericIndicator#getBarSeries() must also return null");
    }

    @Test
    void shouldForwardDynamicBarSeriesInstances() {
        // Arrange: each call returns a new mock BarSeries
        BarSeries first = mock(BarSeries.class);
        BarSeries second = mock(BarSeries.class);
        when(mockDelegate.getBarSeries()).thenReturn(first, second);
        // Act
        BarSeries call1 = numericIndicator.getBarSeries();
        BarSeries call2 = numericIndicator.getBarSeries();
        // Assert
        assertSame(first, call1, "First call should return the first BarSeries instance");
        assertSame(second, call2, "Second call should return the second (different) BarSeries instance");
        assertNotSame(call1, call2, "When delegate supplies new instances, NumericIndicator must not cache them");
    }

    @Test
    void shouldPropagateRuntimeExceptionFromDelegate() {
        // Arrange: delegate throws a RuntimeException
        RuntimeException ex = new RuntimeException("delegate failure");
        when(mockDelegate.getBarSeries()).thenThrow(ex);
        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> numericIndicator.getBarSeries(),
                "NumericIndicator#getBarSeries() must let RuntimeException from delegate bubble up");
        assertSame(ex, thrown, "The same exception instance should be propagated");
    }

    @Test
    void shouldPropagateIllegalArgumentExceptionFromDelegate() {
        // Arrange: delegate throws IllegalArgumentException
        IllegalArgumentException ex = new IllegalArgumentException("bad argument");
        when(mockDelegate.getBarSeries()).thenThrow(ex);
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> numericIndicator.getBarSeries(),
                "IllegalArgumentException from delegate must be propagated unchanged");
        assertSame(ex, thrown);
    }

    @Test
    void shouldPropagateAssertionErrorFromDelegate() {
        // Arrange: delegate throws AssertionError (an Error, not an Exception)
        AssertionError err = new AssertionError("assertion failed");
        when(mockDelegate.getBarSeries()).thenThrow(err);
        // Act & Assert
        AssertionError thrown = assertThrows(AssertionError.class,
                () -> numericIndicator.getBarSeries(),
                "AssertionError from delegate must be propagated unchanged");
        assertSame(err, thrown);
    }

    @Test
    void shouldExposeLiveReferenceWhenSeriesIsMutated() {
        // Arrange: use a real (minimal) BarSeries implementation.
        // The library provides a concrete DefaultBarSeries; if unavailable,
        // a simple anonymous implementation suffices for the test.
        BarSeries realSeries = new org.ta4j.core.BaseBarSeries("test-series");
        when(mockDelegate.getBarSeries()).thenReturn(realSeries);
        // Act: obtain the series via NumericIndicator
        BarSeries obtained = numericIndicator.getBarSeries();
        assertSame(realSeries, obtained, "Initial call must return the concrete BarSeries instance");
        // Mutate the underlying series (add a dummy Bar)
        org.ta4j.core.BaseBar dummyBar = new org.ta4j.core.BaseBar(
                org.ta4j.core.num.DecimalNum.valueOf(1), // end time (placeholder)
                org.ta4j.core.num.DecimalNum.valueOf(100), // open price
                org.ta4j.core.num.DecimalNum.valueOf(110), // max price
                org.ta4j.core.num.DecimalNum.valueOf(90),  // min price
                org.ta4j.core.num.DecimalNum.valueOf(105), // close price
                org.ta4j.core.num.DecimalNum.valueOf(1000) // volume
        );
        realSeries.addBar(dummyBar);
        // Act again
        BarSeries afterMutation = numericIndicator.getBarSeries();
        // Assert: the series reflects the added bar (size increased)
        assertEquals(realSeries.getBarCount(), afterMutation.getBarCount(),
                "After mutation, getBarSeries() must return the same live BarSeries instance");
        assertSame(realSeries, afterMutation,
                "Reference equality must be preserved after the underlying series is mutated");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenDelegateIsNull() throws Exception {
        // Arrange: create a NumericIndicator instance with a null delegate via reflection.
        // The constructor is package‑private/protected; reflection bypasses visibility.
        Constructor<NumericIndicator> ctor = NumericIndicator.class.getDeclaredConstructor(Indicator.class);
        ctor.setAccessible(true);
        NumericIndicator nullDelegateIndicator = ctor.newInstance((Object) null);
        // Act & Assert
        assertThrows(NullPointerException.class,
                nullDelegateIndicator::getBarSeries,
                "Calling getBarSeries() on a NumericIndicator constructed with a null delegate must throw NPE");
    }
}
```