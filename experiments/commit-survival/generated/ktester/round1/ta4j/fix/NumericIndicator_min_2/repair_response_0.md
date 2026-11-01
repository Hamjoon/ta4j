```java
package org.ta4j.core.indicators.numeric;

import org.junit.*;
import static org.junit.Assert.*;

import org.ta4j.core.Indicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Tests for {@link NumericIndicator#min(Number)}.
 *
 * The original test used JUnit 5, Mockito and a {@code MockBarSeries}
 * class that are not available in the compilation environment.
 * This rewritten version uses JUnit 4 only and a tiny
 * {@code SimpleIndicator} implementation that supplies deterministic
 * {@link Num} values.
 */
public class NumericIndicator_min_2_Test {

    /** Function used to create {@link Num} instances from {@link Number}. */
    private Function<Number, Num> numFunction;

    /** Deterministic values used by the indicator. */
    private List<Num> values;

    /** Simple indicator that returns the values above. */
    private SimpleIndicator simpleIndicator;

    /** The {@link NumericIndicator} instance under test. */
    private NumericIndicator numericIndicator;

    @Before
    public void setup() {
        // Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // Build a short series of deterministic values.
        values = new ArrayList<>();
        int[] raw = {1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2};
        for (int v : raw) {
            values.add(numFunction.apply(v));
        }

        // Simple indicator that just returns the prepared values.
        simpleIndicator = new SimpleIndicator(values);
        numericIndicator = NumericIndicator.of(simpleIndicator);
    }

    /** Verifies that {@code min(Number)} returns a new indicator with the element‑wise minimum. */
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

    /** When the constant is greater than every delegate value, the result should be identical to the original indicator. */
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

    /** When the constant is lower than every delegate value, the result should be the constant for all indices. */
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

    /** The library should reject a {@code null} argument. */
    @Test(expected = RuntimeException.class)
    public void min_NullArgument_ThrowsException() {
        numericIndicator.min((Number) null);
    }

    /** {@code Double.NaN} is not a valid constant. */
    @Test(expected = IllegalArgumentException.class)
    public void min_NaNArgument_ThrowsIllegalArgumentException() {
        numericIndicator.min(Double.NaN);
    }

    /** Positive infinity is not allowed. */
    @Test(expected = IllegalArgumentException.class)
    public void min_InfiniteArgument_ThrowsIllegalArgumentException() {
        numericIndicator.min(Double.POSITIVE_INFINITY);
    }

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
        public String getName() {
            return "SimpleIndicator";
        }
    }
}
```