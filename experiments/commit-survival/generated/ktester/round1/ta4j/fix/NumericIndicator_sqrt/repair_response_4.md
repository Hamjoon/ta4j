```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BarSeries;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link NumericIndicator#sqrt()}.
 *
 * The original version of this test class contained several issues that prevented
 * JUnit from discovering and executing the tests:
 *
 * <ul>
 *   <li>The class and its test methods were package‑private – JUnit 4 requires them
 *       to be {@code public}.</li>
 *   <li>JUnit 4 lifecycle annotations {@code @Before} and {@code @After} were
 *       commented out and the JUnit 5 names {@code @BeforeEach}/{@code @AfterEach}
 *       were used instead.</li>
 *   <li>Duplicate imports of {@code NaN} caused compilation warnings.</li>
 *   <li>The test that expects a {@code NullPointerException} did not declare the
 *       expected exception.</li>
 * </ul>
 *
 * All of these problems have been fixed so that the test suite now runs
 * correctly and validates the behaviour of {@code NumericIndicator.sqrt()}.
 */
public class NumericIndicator_sqrt_Test {

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    /**
     * Mock delegate that records the number of {@code getValue} calls.
     */
    private static class MockIndicator implements Indicator<Num> {

        private final double[] data;
        private int callCount = 0;

        MockIndicator(double[] data) {
            this.data = Arrays.copyOf(data, data.length);
        }

        @Override
        public Num getValue(int index) {
            callCount++;
            if (index < 0 || index >= data.length) {
                return NaN;
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

        int getCallCount() {
            return callCount;
        }

        void resetCallCount() {
            callCount = 0;
        }
    }

    /**
     * Simple {@link Indicator} implementation backed by a double array.
     */
    private static class ArrayIndicator implements Indicator<Num> {

        private final double[] data;

        ArrayIndicator(double[] data) {
            this.data = Arrays.copyOf(data, data.length);
        }

        @Override
        public Num getValue(int index) {
            if (index < 0 || index >= data.length) {
                return NaN;
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

    // -------------------------------------------------------------------------
    // Test data
    // -------------------------------------------------------------------------

    /**
     * Sample data (index → value) used by the mock.
     */
    private static final double[] VALUES = {
            0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0
    };

    private Indicator<Num> delegate;
    private NumericIndicator numericIndicator;

    @Before
    public void setupBeforeEach() {
        // Create a fresh mock for every test.
        delegate = new MockIndicator(VALUES);
        // The NumericIndicator simply decorates the mock.
        numericIndicator = NumericIndicator.of(delegate);
    }

    @After
    public void teardownAfterEach() {
        // Reset call count to avoid cross‑test interference.
        if (delegate instanceof MockIndicator) {
            ((MockIndicator) delegate).resetCallCount();
        }
    }

    // -------------------------------------------------------------------------
    // Test cases
    // -------------------------------------------------------------------------

    @Test
    public void sqrt_onConstantIndicator_returnsExpectedValues() {
        double[] original = {0.0, 1.0, 4.0, 9.0};
        Indicator<Num> constant = new ArrayIndicator(original);
        NumericIndicator ni = NumericIndicator.of(constant);
        NumericIndicator sqrt = ni.sqrt();

        for (int i = 0; i < original.length; i++) {
            Num expected = DecimalNum.valueOf(Math.sqrt(original[i]));
            Num actual = sqrt.getValue(i);
            assertEquals("sqrt of " + original[i] + " should be " + expected, expected, actual);
        }
    }

    @Test
    public void sqrt_allZeroValues_returnsZero() {
        double[] zeros = {0.0, 0.0, 0.0, 0.0};
        NumericIndicator sqrt = NumericIndicator.of(new ArrayIndicator(zeros)).sqrt();
        for (int i = 0; i < zeros.length; i++) {
            assertEquals("sqrt of zero at index " + i + " must be zero",
                    DecimalNum.ZERO, sqrt.getValue(i));
        }
    }

    @Test
    public void sqrt_negativeValues_yieldsNaN() {
        double[] negatives = {-4.0, -1.0, -9.0};
        NumericIndicator sqrt = NumericIndicator.of(new ArrayIndicator(negatives)).sqrt();
        for (int i = 0; i < negatives.length; i++) {
            assertTrue("sqrt of negative number " + negatives[i] + " should be NaN",
                    sqrt.getValue(i).isNaN());
        }
    }

    @Test
    public void sqrt_mixedValues_correctResults() {
        double[] mixed = {4.0, -1.0, 2.25, 0.0};
        NumericIndicator sqrt = NumericIndicator.of(new ArrayIndicator(mixed)).sqrt();
        Num[] expected = {
                DecimalNum.valueOf(2.0),
                NaN,
                DecimalNum.valueOf(1.5),
                DecimalNum.ZERO
        };
        for (int i = 0; i < mixed.length; i++) {
            Num actual = sqrt.getValue(i);
            if (expected[i].isNaN()) {
                assertTrue("expected NaN at index " + i + " for original " + mixed[i],
                        actual.isNaN());
            } else {
                assertEquals("unexpected sqrt result at index " + i,
                        expected[i], actual);
            }
        }
    }

    @Test
    public void sqrt_createsNewInstanceAndUnaryOperationDelegate() {
        NumericIndicator sqrt = numericIndicator.sqrt();

        // Different object reference
        assertNotSame("sqrt() must return a new NumericIndicator", numericIndicator, sqrt);

        // The delegate of the sqrt indicator must NOT be the original mock.
        Indicator<Num> sqrtDelegate = sqrt.delegate();
        assertNotSame("Delegate of sqrt indicator must differ from original delegate",
                delegate, sqrtDelegate);

        // The delegate class name should contain "UnaryOperation" (implementation detail).
        String delegateClassName = sqrtDelegate.getClass().getSimpleName();
        assertTrue("Delegate should be a UnaryOperation (found: " + delegateClassName + ")",
                delegateClassName.contains("UnaryOperation"));
    }

    @Test(expected = NullPointerException.class)
    public void sqrt_nullDelegate_throwsNullPointerException() {
        NumericIndicator withNull = NumericIndicator.of(null);
        // This call must throw NPE.
        withNull.sqrt();
    }

    @Test
    public void sqrt_delegateReturnsNullValue_throwsNullPointerExceptionOnGet() {
        // Custom indicator that returns null for any index.
        Indicator<Num> nullReturning = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return null;
            }

            @Override
            public BarSeries getBarSeries() {
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };

        NumericIndicator sqrt = NumericIndicator.of(nullReturning).sqrt();

        try {
            sqrt.getValue(0);
            fail("sqrtIndicator.getValue(0) should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void sqrt_computesSquareRootCorrectly() {
        // When
        NumericIndicator sqrtIndicator = numericIndicator.sqrt();

        // Then – verify each value is the square‑root of the original.
        for (int i = 0; i < VALUES.length; i++) {
            Num expected = DecimalNum.valueOf(Math.sqrt(VALUES[i]));
            Num actual = sqrtIndicator.getValue(i);
            assertEquals("sqrt value at index " + i + " should be " + expected,
                    expected, actual);
        }

        // Verify that the underlying delegate was queried the expected number
        // of times (once per index).
        assertEquals("Delegate should have been called once per index",
                VALUES.length, ((MockIndicator) delegate).getCallCount());
    }
}
```