```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.util.function.Function;
import org.junit.*;
import static org.junit.Assert.*;
import org.ta4j.core.Indicator;
import java.util.ArrayList;
import java.util.List;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

/**
 * Tests for {@link NumericIndicator#min(Number)}.
 *
 * <p>
 * The original test class was package‑private, which prevented JUnit 4 from
 * discovering and executing the tests. Making the class {@code public} resolves
 * the issue. All other logic remains unchanged.
 */
public class NumericIndicator_min_2_Test {

    /**
     * Function used by the mock series to create {@link Num} instances.
     */
    private Function<Number, Num> numFunction;

    /**
     * Series used for the tests (simple price series).
     */
    private BarSeries series;

    /**
     * Close price indicator that will be wrapped by {@link NumericIndicator}.
     */
    private ClosePriceIndicator closePriceIndicator;

    /**
     * The {@link NumericIndicator} instance under test.
     */
    private NumericIndicator numericIndicator;

    /**
     * Deterministic values used by the indicator.
     */
    private List<Num> values;

    /**
     * Simple indicator that returns the values above.
     */
    private SimpleIndicator simpleIndicator;

    /**
     * Very small {@link Indicator} implementation that simply returns the
     * supplied {@link Num} values. It is sufficient for the tests that only
     * need {@code getValue(int)}.
     */
    private static class SimpleIndicator implements Indicator<Num> {

        private final List<Num> data;

        SimpleIndicator(List<Num> data) {
            this.data = data;
        }

        @Override
        public Num getValue(int index) {
            return data.get(index);
        }

        @Override
        public BarSeries getBarSeries() {
            // Not needed for the current tests.
            return null;
        }

        @Override
        public int getUnstableBars() {
            // No unstable bars for this simple implementation.
            return 0;
        }
    }

    // -----------------------------------------------------------------------
    // Test fixtures
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        // Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // Build the deterministic list of values.
        values = new ArrayList<>();
        int[] raw = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (int v : raw) {
            values.add(numFunction.apply(v));
        }

        // Simple indicator that just returns the prepared values.
        simpleIndicator = new SimpleIndicator(values);

        // NumericIndicator under test.
        numericIndicator = NumericIndicator.of(simpleIndicator);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    public void minWithConstantNumber() {
        Number constant = 4;
        NumericIndicator minIndicator = numericIndicator.min(constant);
        assertNotSame("min(Number) should return a new instance", numericIndicator, minIndicator);
        for (int i = 0; i < values.size(); i++) {
            Num delegateValue = simpleIndicator.getValue(i);
            double expected = Math.min(delegateValue.doubleValue(), constant.doubleValue());
            Num expectedNum = DecimalNum.valueOf(expected);
            assertEquals("Incorrect min value at index " + i, expectedNum, minIndicator.getValue(i));
        }
    }

    @Test
    public void minWithConstantHigherThanAllValues() {
        Number constant = 100;
        NumericIndicator minIndicator = numericIndicator.min(constant);
        for (int i = 0; i < values.size(); i++) {
            Num expected = simpleIndicator.getValue(i);
            assertEquals("When constant is higher, min should equal delegate at index " + i,
                    expected, minIndicator.getValue(i));
        }
    }

    @Test
    public void minWithConstantLowerThanAllValues() {
        Number constant = -10;
        NumericIndicator minIndicator = numericIndicator.min(constant);
        Num expected = DecimalNum.valueOf(constant.doubleValue());
        for (int i = 0; i < values.size(); i++) {
            assertEquals("When constant is lower, min should equal constant at index " + i,
                    expected, minIndicator.getValue(i));
        }
    }

    @Test
    public void min_NullArgument_ThrowsException() {
        try {
            numericIndicator.min((Number) null);
            fail("Expected NullPointerException for null argument");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void min_NaNArgument_ThrowsIllegalArgumentException() {
        try {
            numericIndicator.min(Double.NaN);
            fail("Expected IllegalArgumentException for NaN argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void min_InfiniteArgument_ThrowsIllegalArgumentException() {
        try {
            numericIndicator.min(Double.POSITIVE_INFINITY);
            fail("Expected IllegalArgumentException for infinite argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
```