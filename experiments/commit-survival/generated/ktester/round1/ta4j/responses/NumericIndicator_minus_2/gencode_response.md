```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive tests for {@link NumericIndicator#minus(Number)}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>different numeric types (int, long, double, float, short, byte, BigDecimal, BigInteger)</li>
 *   <li>edge values (zero, negative, NaN, infinities)</li>
 *   <li>null argument handling</li>
 *   <li>exception propagation from malformed {@link Number} implementations</li>
 *   <li>overflow scenarios</li>
 *   <li>method chaining (minus followed by plus)</li>
 *   <li>use with a derived indicator (SMA)</li>
 * </ul>
 *
 * The delegate indicator is mocked with Mockito to return deterministic values.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_minus_2_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static final java.util.function.Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /** Series used for the SMA‑related test. */
    private static BarSeries series;

    /** Close price indicator based on {@link #series}. */
    private static ClosePriceIndicator closePrice;

    /** Mocked delegate indicator – the “left‑hand side” of the subtraction. */
    private Indicator<Num> delegateMock;

    /** The {@link NumericIndicator} under test. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void beforeAll() {
        // deterministic series for SMA test (values 10,20,30,…)
        series = new MockBarSeries(
                NUM_FUNCTION,
                10, 20, 30, 40, 50, 60, 70, 80, 90, 100
        );
        closePrice = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setUp() {
        // By default the delegate returns 100 at index 0 – tests can override.
        delegateMock = mock(Indicator.class);
        when(delegateMock.getValue(anyInt())).thenAnswer(invocation -> DecimalNum.valueOf(100));
        numericIndicator = NumericIndicator.of(delegateMock);
    }

    /* --------------------------------------------------------------------- */
    /*  Parameterised tests for the most common numeric types                */
    /* --------------------------------------------------------------------- */

    /** Supplies a variety of {@link Number} instances together with the expected result. */
    private static Stream<Arguments> numberProvider() {
        return Stream.of(
                // positive integer
                Arguments.of(5, DecimalNum.valueOf(95)),
                // negative double
                Arguments.of(-4.2, DecimalNum.valueOf(104.2)),
                // zero
                Arguments.of(0, DecimalNum.valueOf(100)),
                // long
                Arguments.of(1234567890123L, DecimalNum.valueOf(100 - 1234567890123L)),
                // float
                Arguments.of(3.14f, DecimalNum.valueOf(96.86)),
                // short (auto‑boxed)
                Arguments.of((short) 7, DecimalNum.valueOf(93)),
                // byte (negative)
                Arguments.of((byte) -2, DecimalNum.valueOf(102)),
                // BigDecimal (large but still representable)
                Arguments.of(new BigDecimal("100000000"), DecimalNum.valueOf(0)),
                // BigInteger
                Arguments.of(new BigInteger("12345678901234567890"), DecimalNum.valueOf(-12345678901234567790L)),
                // AtomicInteger (custom subclass)
                Arguments.of(new AtomicInteger(10), DecimalNum.valueOf(90))
        );
    }

    @ParameterizedTest(name = "minus({0}) should yield {1}")
    @MethodSource("numberProvider")
    void minusWithVariousNumberTypes(Number n, Num expected) {
        // The delegate returns 100 for any index (configured in @BeforeEach)
        NumericIndicator result = numericIndicator.minus(n);
        assertNumEquals(expected, result.getValue(0),
                () -> "Result of minus(" + n + ") is not as expected");
    }

    /* --------------------------------------------------------------------- */
    /*  Special edge‑case values                                            */
    /* --------------------------------------------------------------------- */

    @Test
    void minusWithNaNConstant() {
        NumericIndicator result = numericIndicator.minus(Double.NaN);
        assertTrue(result.getValue(0).isNaN(),
                "Result should be NaN when subtracting NaN");
    }

    @Test
    void minusWithPositiveInfinityConstant() {
        NumericIndicator result = numericIndicator.minus(Double.POSITIVE_INFINITY);
        assertTrue(result.getValue(0).isNegativeInfinity(),
                "Finite minus +Infinity must be -Infinity");
    }

    @Test
    void minusWithNullArgument() {
        assertThrows(NullPointerException.class,
                () -> numericIndicator.minus((Number) null),
                "Passing null should raise NullPointerException");
    }

    @Test
    void minusWithOverflowBigDecimal() {
        // 1E+10000 is far beyond DecimalNum's range → should throw ArithmeticException
        BigDecimal huge = new BigDecimal("1E+10000");
        assertThrows(ArithmeticException.class,
                () -> numericIndicator.minus(huge),
                "Overflowing BigDecimal must cause an ArithmeticException");
    }

    /* --------------------------------------------------------------------- */
    /*  Custom Number that throws on conversion                              */
    /* --------------------------------------------------------------------- */

    /** A {@link Number} implementation that throws when its value is accessed. */
    private static class BadNumber extends Number {
        @Override public int intValue() { throw new RuntimeException("bad int"); }
        @Override public long longValue() { throw new RuntimeException("bad long"); }
        @Override public float floatValue() { throw new RuntimeException("bad float"); }
        @Override public double doubleValue() { throw new RuntimeException("bad double"); }
    }

    @Test
    void minusWithBadNumberThrows() {
        BadNumber bad = new BadNumber();
        assertThrows(RuntimeException.class,
                () -> numericIndicator.minus(bad),
                "Exception from Number.doubleValue() must propagate");
    }

    /* --------------------------------------------------------------------- */
    /*  Chaining minus -> plus                                               */
    /* --------------------------------------------------------------------- */

    @Test
    void chainingMinusThenPlus() {
        // delegate returns 50 at index 0 for this specific test
        when(delegateMock.getValue(0)).thenReturn(DecimalNum.valueOf(50));
        NumericIndicator result = numericIndicator.minus(5).plus(3);
        assertNumEquals(48, result.getValue(0));
    }

    /* --------------------------------------------------------------------- */
    /*  Using minus on a derived indicator (SMA)                             */
    /* --------------------------------------------------------------------- */

    @Test
    void minusOnDerivedSMAIndicator() {
        // SMA of closePrice with period 3: at index 2 the average of (10,20,30) = 20
        SMAIndicator sma = new SMAIndicator(closePrice, 3);
        NumericIndicator smaNumeric = NumericIndicator.of(sma);
        NumericIndicator result = smaNumeric.minus(2.5);
        Num expected = sma.getValue(2).minus(DecimalNum.valueOf(2.5));
        assertNumEquals(expected, result.getValue(2));
    }

    /* --------------------------------------------------------------------- */
    /*  Original simple tests (kept for completeness)                       */
    /* --------------------------------------------------------------------- */

    @Test
    void minusWithStaticNumber_simple() {
        // delegate returns 100 at index 0 (default)
        NumericIndicator result = numericIndicator.minus(5);
        assertNumEquals(95, result.getValue(0));
    }

    @Test
    void minusWithDynamicIndicator_simple() {
        // Use a real EMA indicator as the right‑hand side
        EMAIndicator ema = new EMAIndicator(closePrice, 3);
        NumericIndicator result = numericIndicator.minus(ema);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).minus(ema.getValue(i));
            assertNumEquals(expected, result.getValue(i));
        }
    }
}
```