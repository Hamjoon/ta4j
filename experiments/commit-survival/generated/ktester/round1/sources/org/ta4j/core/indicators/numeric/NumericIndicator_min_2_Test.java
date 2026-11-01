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

class NumericIndicator_min_2_Test {

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
     * Simple indicator that returns the deterministic values.
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

    //     @BeforeEach
    void setupBeforeEach() {
        // Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // A short series with deterministic values – not used by all tests,
        // but required to initialise the fields.
        //         series = new MockBarSeries(numFunction, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    /**
     * Verifies that {@link NumericIndicator#min(Number)} returns a new {@link NumericIndicator}
     * whose values are the minimum between the wrapped indicator and the supplied constant.
     */
    @Test
    void minWithConstantNumber() {
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

    /**
     * When the constant is greater than every value of the delegate,
     * the result should be identical to the original indicator.
     */
    @Test
    void minWithConstantHigherThanAllValues() {
        Number constant = 100;
        NumericIndicator minIndicator = numericIndicator.min(constant);
        for (int i = 0; i < values.size(); i++) {
            Num expected = simpleIndicator.getValue(i);
            assertEquals("When constant is higher, min should equal delegate at index " + i, expected, minIndicator.getValue(i));
        }
    }

    /**
     * When the constant is lower than every value of the delegate,
     * the result should be the constant for all indices.
     */
    @Test
    void minWithConstantLowerThanAllValues() {
        Number constant = -10;
        NumericIndicator minIndicator = numericIndicator.min(constant);
        Num expected = DecimalNum.valueOf(constant.doubleValue());
        for (int i = 0; i < values.size(); i++) {
            assertEquals("When constant is lower, min should equal constant at index " + i, expected, minIndicator.getValue(i));
        }
    }

    @Test
    void min_NullArgument_ThrowsException() {
        try {
            numericIndicator.min((Number) null);
            fail("Expected NullPointerException for null argument");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    void min_NaNArgument_ThrowsIllegalArgumentException() {
        try {
            numericIndicator.min(Double.NaN);
            fail("Expected IllegalArgumentException for NaN argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    void min_InfiniteArgument_ThrowsIllegalArgumentException() {
        try {
            numericIndicator.min(Double.POSITIVE_INFINITY);
            fail("Expected IllegalArgumentException for infinite argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Before
    public void setup() {
        // Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // Build a short series of deterministic values.
        values = new ArrayList<>();
        int[] raw = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (int v : raw) {
            values.add(numFunction.apply(v));
        }
        // Simple indicator that just returns the prepared values.
        simpleIndicator = new SimpleIndicator(values);
        numericIndicator = NumericIndicator.of(simpleIndicator);
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
     * Initialise the factory, the deterministic list of values and the
     * {@link NumericIndicator} based on those values.
     */
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
}
