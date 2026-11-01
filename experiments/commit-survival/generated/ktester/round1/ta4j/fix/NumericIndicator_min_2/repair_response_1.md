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
import org.ta4j.core.mocks.MockBarSeries;

/**
 * Tests for {@link NumericIndicator#min(Number)}.
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
        public String getName() {
            return "SimpleIndicator";
        }

        @Override
        public int getUnstableBars() {
            // No unstable bars for this simple implementation.
            return 0;
        }
    }

    /**
     * Initialise fields that are required for all tests.
     */
    @Before
    public void setUpCommon() {
        // Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // Short deterministic series – used by some tests.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    /**
     * Initialise the deterministic list of values and the simple indicator.
     * This runs after {@link #setUpCommon()} and overwrites {@code numericIndicator}
     * with an indicator based on the prepared list.
     */
    @Before
    public void setUpValues() {
        values = new ArrayList<>();
        int[] raw = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (int v : raw) {
            values.add(numFunction.apply(v));
        }
        simpleIndicator = new SimpleIndicator(values);
        numericIndicator = NumericIndicator.of(simpleIndicator);
    }

    /**
     * Verifies that {@link NumericIndicator#min(Number)} returns a new {@link NumericIndicator}
     * whose values are the minimum between the wrapped indicator and the supplied constant.
     */
    @Test
    public void minWithConstantNumber() {
        Number constant = 4;
        NumericIndicator minIndicator = numericIndicator.min(constant);
        assertNotSame("min(Number) should return a new instance", numericIndicator, minIndicator);
        for (int i = 0; i < values.size(); i++) {
            Num delegateValue = simpleIndicator.getValue(i);
            double expected = Math.min(delegateValue.doubleValue(), constant.doubleValue());
            Num expectedNum = DecimalNum.valueOf(expected);
            assertEquals("Incorrect min value at index " + i,
                    expectedNum, minIndicator.getValue(i));
        }
    }

    /**
     * When the constant is greater than every value of the delegate,
     * the result should be identical to the original indicator.
     */
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

    /**
     * When the constant is lower than every value of the delegate,
     * the result should be the constant for all indices.
     */
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

    /**
     * Basic functionality test for a delegate series containing only positive numbers (10, 12, 15).
     * The test iterates over several constant / expected‑value triples.
     */
    @Test
    public void minBasicFunctionality_NumberConstant() {
        // constants and the expected results for indices 0,1,2
        double[][] data = {
                // constant, expected0, expected1, expected2
                { 5, 5, 5, 5 },
                { 20, 10, 12, 15 },
                { 12, 10, 12, 12 },
                { -3, -3, -3, -3 },
                { 0, 0, 0, 0 }
        };
        BarSeries posSeries = new MockBarSeries(numFunction, 10, 12, 15);
        ClosePriceIndicator posIndicator = new ClosePriceIndicator(posSeries);
        NumericIndicator indicator = NumericIndicator.of(posIndicator);

        for (double[] row : data) {
            double constant = row[0];
            NumericIndicator minIndicator = indicator.min(constant);
            assertEquals(DecimalNum.valueOf(row[1]), minIndicator.getValue(0),
                    "Incorrect min at index 0 for constant " + constant);
            assertEquals(DecimalNum.valueOf(row[2]), minIndicator.getValue(1),
                    "Incorrect min at index 1 for constant " + constant);
            assertEquals(DecimalNum.valueOf(row[3]), minIndicator.getValue(2),
                    "Incorrect min at index 2 for constant " + constant);
        }
    }

    /**
     * Mixed delegate values test for a series containing -5, -2, 3.
     */
    @Test
    public void minMixedDelegateValues_NumberConstant() {
        double[][] data = {
                // constant, expected0, expected1, expected2
                { 13, -5, -2, 3 },
                { -2, -5, -2, -2 },
                { 0, -5, -2, 0 }
        };
        BarSeries mixedSeries = new MockBarSeries(numFunction, -5, -2, 3);
        ClosePriceIndicator mixedIndicator = new ClosePriceIndicator(mixedSeries);
        NumericIndicator indicator = NumericIndicator.of(mixedIndicator);

        for (double[] row : data) {
            double constant = row[0];
            NumericIndicator minIndicator = indicator.min(constant);
            assertEquals(DecimalNum.valueOf(row[1]), minIndicator.getValue(0),
                    "Incorrect min at index 0 for constant " + constant);
            assertEquals(DecimalNum.valueOf(row[2]), minIndicator.getValue(1),
                    "Incorrect min at index 1 for constant " + constant);
            assertEquals(DecimalNum.valueOf(row[3]), minIndicator.getValue(2),
                    "Incorrect min at index 2 for constant " + constant);
        }
    }

    @Test(expected = NullPointerException.class)
    public void min_NullArgument_ThrowsException() {
        numericIndicator.min((Number) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void min_NaNArgument_ThrowsIllegalArgumentException() {
        numericIndicator.min(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void min_InfiniteArgument_ThrowsIllegalArgumentException() {
        numericIndicator.min(Double.POSITIVE_INFINITY);
    }
}
```