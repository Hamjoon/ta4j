```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive unit tests for {@link NumericIndicator#plus(Number)}.
 *
 * <p>
 * The test suite covers:
 * <ul>
 *   <li>basic arithmetic with various numeric types (int, double, BigDecimal)</li>
 *   <li>handling of special values (null, NaN, infinities)</li>
 *   <li>boundary values (Integer.MAX_VALUE / MIN_VALUE)</li>
 *   <li>different {@link Number} subclasses (Float, Short, Byte, BigInteger)</li>
 *   <li>object identity / immutability guarantees</li>
 * </ul>
 *
 * <p>All interactions with the underlying {@link Indicator} are mocked with
 * Mockito, so the tests are fast and deterministic.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_plus_2_Test {

    @Mock
    private Indicator<Num> delegate; // the wrapped indicator (e.g., close price)

    @Mock
    private Indicator<Num> other;    // second indicator used for the dynamic plus

    private NumericIndicator numeric; // instance under test

    @BeforeEach
    void setupBeforeEach() {
        // Create the NumericIndicator that decorates the mocked delegate
        numeric = NumericIndicator.of(delegate);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks to avoid cross‑test contamination
        reset(delegate, other);
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
        Num expected = delegateValue.plus(DecimalNum.valueOf(n));
        assertEquals(expected, result.getValue(0),
                () -> "Result should be delegate (" + delegateValue + ") plus " + n);

        // Verify single interaction with the delegate
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    private static Stream<Arguments> basicArithmeticProvider() {
        Num base = DecimalNum.valueOf(100); // common delegate value for all cases
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

    /* --------------------------------------------------------------
     *  Null and special numeric values
     * -------------------------------------------------------------- */
    @Test
    void plus_withNull_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> numeric.plus(null),
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
        Num expected = DecimalNum.valueOf(n);
        assertEquals(expected, result.getValue(0),
                () -> "Result should be " + n);
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    private static Stream<Arguments> integerBoundaryProvider() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE)
        );
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
        Num expected = DecimalNum.valueOf(10).plus(DecimalNum.valueOf(n));
        assertEquals(expected, result.getValue(0),
                () -> "Result should be 10 + " + n);
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    private static Stream<Arguments> variousNumberSubclassesProvider() {
        return Stream.of(
                Arguments.of(new Float(1.5f)),                         // Float
                Arguments.of((short) 2),                               // Short
                Arguments.of((byte) -4),                               // Byte
                Arguments.of(new BigInteger("98765432109876543210")),  // BigInteger
                Arguments.of(new BigDecimal("0.0000001")),             // small‑scale BigDecimal
                Arguments.of(new BigDecimal("1234567890.123456789"))   // high‑precision BigDecimal
        );
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
        assertNotSame(numeric, result, "plus(Number) must return a new NumericIndicator instance");

        // The original delegate must still return the original value (unchanged)
        assertEquals(DecimalNum.valueOf(42), numeric.getValue(0),
                "Original NumericIndicator should remain unchanged");

        // The result must reflect the addition
        Num expected = DecimalNum.valueOf(42).plus(DecimalNum.valueOf(7));
        assertEquals(expected, result.getValue(0), "Result should be original + 7");

        // Verify interactions – each indicator queried exactly once
        verify(delegate, times(1)).getValue(0);
        verifyNoMoreInteractions(delegate);
    }

    // -----------------------------------------------------------------
    //  Lifecycle hooks required by the original framework (kept empty)
    // -----------------------------------------------------------------
    @BeforeAll
    static void setupBeforeAll() { }

    @AfterAll
    static void teardownAfterAll() { }
}
```