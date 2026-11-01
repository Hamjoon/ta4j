package org.ta4j.core.analysis;

import org.junit.jupiter.api.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CashFlow_numOf_Test {

    /**
     * Simple conversion from {@link Number} to {@link Num} used by the mock series.
     */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    private BarSeries barSeries;

    private CashFlow cashFlow;

    @BeforeEach
    void setupBeforeEach() {
        // A small series is enough – the actual values are irrelevant for the
        // delegation test.
        barSeries = new MockBarSeries(NUM_FUNCTION, 1.0, 2.0, 3.0, 4.0, 5.0);
        // Use a simple Position; the constructor will calculate the cash‑flow
        // values, but that does not affect the behaviour of numOf().
        cashFlow = new CashFlow(barSeries, new Position());
        // A tiny series is sufficient – values are irrelevant for delegation.
        barSeries = new MockBarSeries(NUM_FUNCTION, 1.0, 2.0, 3.0, 4.0, 5.0);
        cashFlow = new CashFlow(barSeries, new Position());
    }

    /**
     * Provider for the basic delegation group (Integer, Double, BigDecimal, Long).
     */
    static Stream<Arguments> basicDelegationProvider() {
        return Stream.of(Arguments.of(42, DecimalNum.valueOf(42)), Arguments.of(3.1415, DecimalNum.valueOf(3.1415)), Arguments.of(new BigDecimal("123456.789"), DecimalNum.valueOf(123456.789)), Arguments.of(Long.valueOf(9_999_999_999L), DecimalNum.valueOf(9_999_999_999L)));
    }

    /**
     * Provider for primitive wrapper types.
     */
    static Stream<Arguments> wrapperTypesProvider() {
        return Stream.of(Arguments.of(Byte.valueOf((byte) 7), DecimalNum.valueOf(7)), Arguments.of(Short.valueOf((short) -12), DecimalNum.valueOf(-12)), Arguments.of(Float.valueOf(2.5f), DecimalNum.valueOf(2.5)));
    }

    /**
     * Provider for floating‑point edge cases (NaN & infinities).
     */
    static Stream<Arguments> floatingEdgeProvider() {
        return Stream.of(Arguments.of(Float.NaN, true, false, false), Arguments.of(Float.POSITIVE_INFINITY, false, true, false), Arguments.of(Float.NEGATIVE_INFINITY, false, false, true), Arguments.of(Double.NaN, true, false, false), Arguments.of(Double.POSITIVE_INFINITY, false, true, false), Arguments.of(Double.NEGATIVE_INFINITY, false, false, true));
    }

    /**
     * Provider for large integer types and boundary values.
     */
    static Stream<Arguments> largeIntegersProvider() {
        return Stream.of(Arguments.of(new BigInteger("9876543210123456789"), DecimalNum.valueOf(new BigDecimal("9876543210123456789"))), Arguments.of(Integer.valueOf(0), DecimalNum.valueOf(0)), Arguments.of(Long.valueOf(-123_456_789L), DecimalNum.valueOf(-123_456_789L)), Arguments.of(Integer.valueOf(Integer.MAX_VALUE), DecimalNum.valueOf(Integer.MAX_VALUE)), Arguments.of(Integer.valueOf(Integer.MIN_VALUE), DecimalNum.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    void testNumOfDelegatesToBarSeries() {
        // GIVEN
        // an arbitrary number
        Number input = 7.5;
        // the value the series would return
        Num expected = barSeries.numOf(input);
        // WHEN
        Num actual = cashFlow.numOf(input);
        // THEN
        assertEquals(expected, actual, () -> "CashFlow.numOf should return the same Num as BarSeries.numOf");
    }

    @Test
    void testNumOfWithInteger() {
        // GIVEN
        // integer input
        Number input = 3;
        Num expected = barSeries.numOf(input);
        // WHEN
        Num actual = cashFlow.numOf(input);
        // THEN
        assertEquals(expected, actual, () -> "CashFlow.numOf should correctly handle Integer inputs");
    }

    @Test
    void testNumOfWithZero() {
        // GIVEN
        Number input = 0;
        Num expected = barSeries.numOf(input);
        // WHEN
        Num actual = cashFlow.numOf(input);
        // THEN
        assertEquals(expected, actual, () -> "CashFlow.numOf should correctly handle zero");
    }

    @ParameterizedTest(name = "basic delegation – number={0}")
    @MethodSource("basicDelegationProvider")
    void testNumOf_DelegatesCorrectly_Basic(Number input, Num ignored) {
        // Expected value is obtained directly from the underlying series.
        Num expected = barSeries.numOf(input);
        Num actual = cashFlow.numOf(input);
        assertEquals(expected, actual, () -> "CashFlow.numOf should delegate to BarSeries.numOf and return the same Num");
    }

    @ParameterizedTest(name = "wrapper type – number={0}")
    @MethodSource("wrapperTypesProvider")
    void testNumOf_DelegatesCorrectly_WrapperTypes(Number input, Num ignored) {
        Num expected = barSeries.numOf(input);
        Num actual = cashFlow.numOf(input);
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "floating edge – number={0}")
    @MethodSource("floatingEdgeProvider")
    void testNumOf_DelegatesCorrectly_FloatingEdges(Number input, boolean expectNaN, boolean expectPosInf, boolean expectNegInf) {
        Num actual = cashFlow.numOf(input);
        if (expectNaN) {
            assertTrue(Double.isNaN(actual.doubleValue()), () -> "Result should be NaN for input " + input);
        } else if (expectPosInf) {
            assertTrue(Double.isInfinite(actual.doubleValue()) && actual.doubleValue() > 0, () -> "Result should be +∞ for input " + input);
        } else if (expectNegInf) {
            assertTrue(Double.isInfinite(actual.doubleValue()) && actual.doubleValue() < 0, () -> "Result should be -∞ for input " + input);
        } else {
            fail("Unexpected edge‑case configuration");
        }
    }

    @ParameterizedTest(name = "large integer – number={0}")
    @MethodSource("largeIntegersProvider")
    void testNumOf_DelegatesCorrectly_LargeIntegers(Number input, Num ignored) {
        Num expected = barSeries.numOf(input);
        Num actual = cashFlow.numOf(input);
        assertEquals(expected, actual);
    }

    @Test
    void testNumOf_NullArgument_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> cashFlow.numOf(null), "Passing null should result in NullPointerException from the delegate");
    }

    @Test
    void testNumOf_CustomNumberSubclass_DelegatesUnchanged() {
        Number custom = new Number() {

            @Override
            public int intValue() {
                return 42;
            }

            @Override
            public long longValue() {
                return 42L;
            }

            @Override
            public float floatValue() {
                return 42.0f;
            }

            @Override
            public double doubleValue() {
                return 42.0;
            }
        };
        Num expected = barSeries.numOf(custom);
        Num actual = cashFlow.numOf(custom);
        assertEquals(expected, actual);
    }

    @Test
    void testNumOf_CustomNumberThrowsException_Propagates() {
        Number badNumber = new Number() {

            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public long longValue() {
                return 0L;
            }

            @Override
            public float floatValue() {
                throw new IllegalArgumentException("invalid float");
            }

            @Override
            public double doubleValue() {
                throw new IllegalArgumentException("invalid double");
            }
        };
        // The underlying series will call doubleValue() → exception should bubble up.
        assertThrows(IllegalArgumentException.class, () -> cashFlow.numOf(badNumber), "Exception from custom Number should propagate");
    }

    @Test
    void testNumOf_NullBarSeries_ThrowsNullPointerException() {
        // Construct CashFlow with a null BarSeries (the constructor permits it in this test).
        CashFlow cfWithNullSeries = new CashFlow(null, new Position());
        assertThrows(NullPointerException.class, () -> cfWithNullSeries.numOf(42), "Calling numOf when barSeries is null must throw NPE");
    }

    @Test
    void testNumOf_BarSeriesThrowsRuntimeException_Propagates() {
        // Mock a BarSeries that throws IllegalStateException for a specific input.
        BarSeries mockedSeries = Mockito.mock(BarSeries.class);
        Number input = 123;
        Mockito.when(mockedSeries.numOf(input)).thenThrow(new IllegalStateException("mocked failure"));
        CashFlow cf = new CashFlow(mockedSeries, new Position());
        assertThrows(IllegalStateException.class, () -> cf.numOf(input), "Exception from BarSeries.numOf must not be swallowed");
    }
}
