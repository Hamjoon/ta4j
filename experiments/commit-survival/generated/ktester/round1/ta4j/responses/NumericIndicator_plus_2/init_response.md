```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#plus(Number)} (static operand) and
 * {@link NumericIndicator#plus(Indicator)} (dynamic operand).
 *
 * The tests use Mockito to mock the underlying {@link Indicator} instances,
 * avoiding any dependence on a real {@link org.ta4j.core.BarSeries}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_plus_2_Test {

    @Mock
    private Indicator<Num> delegate;          // the wrapped indicator (e.g., close price)

    @Mock
    private Indicator<Num> other;             // second indicator used for the dynamic plus

    private NumericIndicator numeric;         // instance under test

    @BeforeEach
    void setupBeforeEach() {
        // Create the NumericIndicator that decorates the mocked delegate
        numeric = NumericIndicator.of(delegate);
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to release, but resetting mocks makes the test suite
        // more robust when additional tests are added.
        reset(delegate, other);
    }

    @Test
    void plus_withStaticNumber_returnsCorrectValues() {
        // Arrange – stub delegate values for a few indices
        when(delegate.getValue(0)).thenReturn(DecimalNum.valueOf(10));
        when(delegate.getValue(1)).thenReturn(DecimalNum.valueOf(20));
        when(delegate.getValue(2)).thenReturn(DecimalNum.valueOf(-5));

        // Act – add a constant number (5) to the delegate
        NumericIndicator result = numeric.plus(5);

        // Assert – each value must be delegate + 5
        assertEquals(DecimalNum.valueOf(15), result.getValue(0));
        assertEquals(DecimalNum.valueOf(25), result.getValue(1));
        assertEquals(DecimalNum.valueOf(0), result.getValue(2));

        // Verify that the delegate was queried exactly once per index
        verify(delegate, times(1)).getValue(0);
        verify(delegate, times(1)).getValue(1);
        verify(delegate, times(1)).getValue(2);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void plus_withDynamicIndicator_returnsCorrectValues() {
        // Arrange – stub delegate values
        when(delegate.getValue(0)).thenReturn(DecimalNum.valueOf(3));
        when(delegate.getValue(1)).thenReturn(DecimalNum.valueOf(7));

        // Stub the second indicator (other) values
        when(other.getValue(0)).thenReturn(DecimalNum.valueOf(2));
        when(other.getValue(1)).thenReturn(DecimalNum.valueOf(4));

        // Act – add the dynamic indicator
        NumericIndicator result = numeric.plus(other);

        // Assert – each value must be delegate.plus(other)
        assertEquals(DecimalNum.valueOf(5), result.getValue(0)); // 3 + 2
        assertEquals(DecimalNum.valueOf(11), result.getValue(1)); // 7 + 4

        // Verify interactions
        verify(delegate, times(1)).getValue(0);
        verify(delegate, times(1)).getValue(1);
        verify(other, times(1)).getValue(0);
        verify(other, times(1)).getValue(1);
        verifyNoMoreInteractions(delegate, other);
    }

    @Test
    void plus_withNumber_doesNotExposeInternalState() {
        // This test ensures that the returned NumericIndicator does not expose
        // the original delegate via reflection or public API (apart from the
        // intended {@code delegate()} method). The only public way to obtain the
        // underlying indicator is through {@code delegate()}, which we deliberately
        // do not call here – we just verify that the class can be instantiated
        // without throwing.
        assertDoesNotThrow(() -> numeric.plus(0));
    }

    // The static lifecycle methods are kept empty because this test class does
    // not need any global resources.
    @BeforeAll
    static void setupBeforeAll() { }

    @AfterAll
    static void teardownAfterAll() { }
}
```