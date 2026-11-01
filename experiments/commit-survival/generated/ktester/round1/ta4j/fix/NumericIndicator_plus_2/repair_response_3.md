```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

/**
 * Tests for {@link NumericIndicator#plus(Number)} and related overloads.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_plus_2_Test {

    @Mock
    private Indicator<Num> delegate; // the wrapped indicator (e.g., close price)

    @Mock
    private Indicator<Num> other; // second indicator used for the dynamic plus

    // instance under test
    private NumericIndicator numeric;

    @BeforeEach
    void setupBeforeEach() {
        // Create the NumericIndicator that decorates the mocked delegate
        numeric = NumericIndicator.of(delegate);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks to keep the test suite robust
        reset(delegate, other);
    }

    private static Stream<Arguments> basicArithmeticProvider() {
        // common delegate value for all cases
        Num base = DecimalNum.valueOf(100);
        return Stream.of(
                // positive int
                Arguments.of(5, base),
                // negative int (subtraction)
                Arguments.of(-3, base),
                // zero (no change)
                Arguments.of(0, base),
                // double with fractional part
                Arguments.of(2.75, base),
                // large BigDecimal (big‑integer precision)
                Arguments.of(new BigDecimal("12345678901234567890"), base)
        );
    }

    private static Stream<Arguments> integerBoundaryProvider() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE)
        );
    }

    private static Stream<Arguments> variousNumberSubclassesProvider() {
        return Stream.of(
                // Float
                Arguments.of(new Float(1.5f)),
                // Short
                Arguments.of((short) 2),
                // Byte
                Arguments.of((byte) -4),
                // BigInteger
                Arguments.of(new BigInteger("98765432109876543210")),
                // small‑scale BigDecimal
                Arguments.of(new BigDecimal("0.0000001")),
                // high‑precision BigDecimal
                Arguments.of(new BigDecimal("1234567890.123456789"))
        );
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
        assertEquals(DecimalNum.valueOf(5), result.getValue(0));   // 3 + 2
        assertEquals(DecimalNum.valueOf(11), result.getValue(1));  // 7 + 4
        // Verify interactions
        verify(delegate, times(1)).getValue(0);
        verify(delegate, times(1)).getValue(1);
        verify(other, times(1)).getValue(0);
        verify(other, times(1)).getValue(1);
        verifyNoMoreInteractions(delegate, other);
    }

    @Test
    void plus_withNumber_doesNotExposeInternalState() {
        // Ensure that creating a new indicator does not throw any unexpected exception.
        assertDoesNotThrow(() -> numeric.plus(0));
    }

    /* --------------------------------------------------------------
     *  Basic arithmetic behavior – parameterized
     * -------------------------------------------------------------- */
    @ParameterizedTest(name = "{index} : plus({0})")
    @MethodSource("basicArithmeticProvider")
    void plus_withStaticNumber_basicArithmetic(Number n, Num delegateValue) {
        // Arrange
        when(delegate.getValue(0)).thenReturn(delegateValue);
        // Act
        NumericIndicator result = numeric.plus(n);
        // Assert – expected = delegate + n (using DecimalNum arithmetic)
        Num expected = delegateValue.plus(DecimalNum.valueOf(n.doubleValue()));
        assertEquals(expected, result.getValue(0),
                () -> "Result should be delegate (" + delegateValue + ") plus " + n);
        // Verify single interaction with the delegate
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    /* --------------------------------------------------------------
     *  Null and special numeric values
     * -------------------------------------------------------------- */
    @Test
    void plus_withNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> numeric.plus(null),
                "plus(null) must throw NullPointerException");
    }

    @ParameterizedTest(name = "{index} : plus({0})")
    @ValueSource(doubles = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
    void plus_withSpecialDoubleValues(double special) {
        // Arrange – any delegate value (e.g., 1)
        when(delegate.getValue(0)).thenReturn(DecimalNum.valueOf(1));
        // Act
        NumericIndicator result = numeric.plus(special);
        // Assert – the special value should propagate through the Num implementation
        Num expected = DecimalNum.valueOf(1).plus(DecimalNum.valueOf(special));
        assertEquals(expected, result.getValue(0),
                () -> "Result should be delegate + " + special);
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    /* --------------------------------------------------------------
     *  Integer boundary values
     * -------------------------------------------------------------- */
    @ParameterizedTest(name = "plus(Integer.MAX_VALUE) and plus(Integer.MIN_VALUE)")
    @MethodSource("integerBoundaryProvider")
    void plus_withIntegerBoundaries(Number n) {
        // Arrange – delegate value 0 for simplicity
        when(delegate.getValue(0)).thenReturn(DecimalNum.ZERO);
        // Act
        NumericIndicator result = numeric.plus(n);
        // Assert – result should equal the constant itself (0 + n = n)
        Num expected = DecimalNum.valueOf(n.doubleValue());
        assertEquals(expected, result.getValue(0),
                () -> "Result should be " + n);
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    /* --------------------------------------------------------------
     *  Various Number subclasses
     * -------------------------------------------------------------- */
    @ParameterizedTest(name = "plus({0})")
    @MethodSource("variousNumberSubclassesProvider")
    void plus_withVariousNumberSubclasses(Number n) {
        // Arrange – use a non‑trivial delegate value
        when(delegate.getValue(0)).thenReturn(DecimalNum.valueOf(10));
        // Act
        NumericIndicator result = numeric.plus(n);
        // Assert – delegate + n using DecimalNum arithmetic
        Num expected = DecimalNum.valueOf(10).plus(DecimalNum.valueOf(n.doubleValue()));
        assertEquals(expected, result.getValue(0),
                () -> "Result should be 10 + " + n);
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    /* --------------------------------------------------------------
     *  Object identity & immutability
     * -------------------------------------------------------------- */
    @Test
    void plus_createsNewInstance_andLeavesOriginalUnchanged() {
        // Arrange – stub a delegate value
        when(delegate.getValue(0)).thenReturn(DecimalNum.valueOf(42));
        // Act
        NumericIndicator result = numeric.plus(7);
        // Assert – a new object is returned
        assertNotSame(numeric, result,
                "plus(Number) must return a new NumericIndicator instance");
        // The original delegate must still return the original value (unchanged)
        assertEquals(DecimalNum.valueOf(42), numeric.getValue(0),
                "Original NumericIndicator should remain unchanged");
        // The result must reflect the addition
        Num expected = DecimalNum.valueOf(42).plus(DecimalNum.valueOf(7));
        assertEquals(expected, result.getValue(0),
                "Result should be original + 7");
        // Verify interactions – each indicator queried exactly once
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }
}
```