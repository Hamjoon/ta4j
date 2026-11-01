```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;

/**
 * Comprehensive tests for {@link NumericIndicator#sqrt()}.
 *
 * <p>The test suite covers:
 * <ul>
 *   <li>Standard positive values (including zero).</li>
 *   <li>Negative values – expecting {@link DecimalNum#NaN}.</li>
 *   <li>Mixed positive/negative sequences.</li>
 *   <li>Reference inequality – {@code sqrt()} must create a new instance.</li>
 *   <li>Behaviour when the underlying delegate is {@code null}.</li>
 *   <li>Behaviour when the delegate returns {@code null} as a {@link Num}.</li>
 * </ul>
 *
 * <p>All tests use only the public API of {@link NumericIndicator}.  When we need
 * to inspect the internal delegate we use the {@code delegate()} accessor and
 * reflection (no direct field access).
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_sqrt_Test {

    /** Mocked delegate used by the generic tests. */
    @Mock
    private Indicator<Num> delegate;

    /** The {@link NumericIndicator} under test (wraps {@code delegate}). */
    private NumericIndicator numericIndicator;

    /** Sample data (index → value) used by the generic mock. */
    private static final double[] VALUES = {
            0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0
    };

    @BeforeEach
    void setUp() {
        // Stub the mock to return the appropriate DecimalNum for each index.
        when(delegate.getValue(anyInt())).thenAnswer(invocation -> {
            int idx = invocation.getArgument(0);
            if (idx < 0 || idx >= VALUES.length) {
                return DecimalNum.NaN;
            }
            return DecimalNum.valueOf(VALUES[idx]);
        });
        // The NumericIndicator simply decorates the mock.
        numericIndicator = NumericIndicator.of(delegate);
    }

    @AfterEach
    void tearDown() {
        reset(delegate);
    }

    /* --------------------------------------------------------------------- */
    /* 1. Basic correctness – positive numbers (including zero)            */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_computesSquareRootCorrectly() {
        NumericIndicator sqrtIndicator = numericIndicator.sqrt();

        for (int i = 0; i < VALUES.length; i++) {
            Num expected = DecimalNum.valueOf(Math.sqrt(VALUES[i]));
            Num actual = sqrtIndicator.getValue(i);
            assertEquals(expected, actual,
                    "sqrt value at index " + i + " should be " + expected);
        }

        // Verify that the underlying delegate was queried exactly once per index.
        verify(delegate, times(VALUES.length)).getValue(anyInt());
    }

    /* --------------------------------------------------------------------- */
    /* 2. Constant indicator with non‑negative values                        */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_onConstantIndicator_returnsExpectedValues() {
        double[] original = {0.0, 1.0, 4.0, 9.0};
        Indicator<Num> constant = new ArrayIndicator(original);
        NumericIndicator ni = NumericIndicator.of(constant);
        NumericIndicator sqrt = ni.sqrt();

        for (int i = 0; i < original.length; i++) {
            Num expected = DecimalNum.valueOf(Math.sqrt(original[i]));
            Num actual = sqrt.getValue(i);
            assertEquals(expected, actual,
                    "sqrt of " + original[i] + " should be " + expected);
        }
    }

    /* --------------------------------------------------------------------- */
    /* 3. Edge case – all zeros                                            */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_allZeroValues_returnsZero() {
        double[] zeros = {0.0, 0.0, 0.0, 0.0};
        NumericIndicator sqrt = NumericIndicator.of(new ArrayIndicator(zeros)).sqrt();

        for (int i = 0; i < zeros.length; i++) {
            assertEquals(DecimalNum.ZERO, sqrt.getValue(i),
                    "sqrt of zero at index " + i + " must be zero");
        }
    }

    /* --------------------------------------------------------------------- */
    /* 4. Negative values – expect NaN                                      */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_negativeValues_yieldsNaN() {
        double[] negatives = {-4.0, -1.0, -9.0};
        NumericIndicator sqrt = NumericIndicator.of(new ArrayIndicator(negatives)).sqrt();

        for (int i = 0; i < negatives.length; i++) {
            assertTrue(sqrt.getValue(i).isNaN(),
                    "sqrt of negative number " + negatives[i] + " should be NaN");
        }
    }

    /* --------------------------------------------------------------------- */
    /* 5. Mixed positive and negative values                               */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_mixedValues_correctResults() {
        double[] mixed = {4.0, -1.0, 2.25, 0.0};
        NumericIndicator sqrt = NumericIndicator.of(new ArrayIndicator(mixed)).sqrt();

        Num[] expected = {
                DecimalNum.valueOf(2.0),
                DecimalNum.NaN,
                DecimalNum.valueOf(1.5),
                DecimalNum.ZERO
        };

        for (int i = 0; i < mixed.length; i++) {
            Num actual = sqrt.getValue(i);
            if (expected[i].isNaN()) {
                assertTrue(actual.isNaN(),
                        "expected NaN at index " + i + " for original " + mixed[i]);
            } else {
                assertEquals(expected[i], actual,
                        "unexpected sqrt result at index " + i);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* 6. Verify a new instance is created and delegate type                */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_createsNewInstanceAndUnaryOperationDelegate() {
        NumericIndicator sqrt = numericIndicator.sqrt();

        // Different object reference
        assertNotSame(numericIndicator, sqrt, "sqrt() must return a new NumericIndicator");

        // The delegate of the sqrt indicator must NOT be the original mock.
        Indicator<Num> sqrtDelegate = sqrt.delegate();
        assertNotSame(delegate, sqrtDelegate, "Delegate of sqrt indicator must differ from original delegate");

        // The delegate class name should contain "UnaryOperation" (implementation detail).
        String delegateClassName = sqrtDelegate.getClass().getSimpleName();
        assertTrue(delegateClassName.contains("UnaryOperation"),
                "Delegate should be a UnaryOperation (found: " + delegateClassName + ")");
    }

    /* --------------------------------------------------------------------- */
    /* 7. Null delegate – sqrt() should throw NPE                           */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_nullDelegate_throwsNullPointerException() {
        NumericIndicator withNull = NumericIndicator.of(null);
        assertThrows(NullPointerException.class,
                withNull::sqrt,
                "Calling sqrt() on a NumericIndicator with a null delegate must throw NPE");
    }

    /* --------------------------------------------------------------------- */
    /* 8. Delegate returns null Num – sqrt().getValue throws NPE          */
    /* --------------------------------------------------------------------- */
    @Test
    void sqrt_delegateReturnsNullValue_throwsNullPointerExceptionOnGet() {
        // Custom indicator that returns null for index 0.
        Indicator<Num> nullReturning = new Indicator<Num>() {
            @Override public Num getValue(int index) { return null; }
            @Override public BarSeries getBarSeries() { return null; }
            @Override public int getUnstableBars() { return 0; }
        };
        NumericIndicator sqrt = NumericIndicator.of(nullReturning).sqrt();

        assertThrows(NullPointerException.class,
                () -> sqrt.getValue(0),
                "sqrtIndicator.getValue(0) should throw NPE when underlying value is null");
    }

    /* --------------------------------------------------------------------- */
    /* Helper class – simple array‑backed Indicator for deterministic tests   */
    /* --------------------------------------------------------------------- */
    private static class ArrayIndicator implements Indicator<Num> {
        private final double[] data;

        ArrayIndicator(double[] data) {
            this.data = Arrays.copyOf(data, data.length);
        }

        @Override
        public Num getValue(int index) {
            if (index < 0 || index >= data.length) {
                return DecimalNum.NaN;
            }
            return DecimalNum.valueOf(data[index]);
        }

        @Override
        public BarSeries getBarSeries() {
            // Not required for sqrt tests.
            return null;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }
}
```