```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_min_2_Test {

    /** Function used by the mock series to create {@link Num} instances. */
    private Function<Number, Num> numFunction;

    /** Series used for the tests (simple price series). */
    private BarSeries series;

    /** Close price indicator that will be wrapped by {@link NumericIndicator}. */
    private ClosePriceIndicator closePriceIndicator;

    /** The {@link NumericIndicator} instance under test. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Create a simple Num factory based on DecimalNum.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // A short series with deterministic values.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);

        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up after each test.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown needed.
    }

    /**
     * Verifies that {@link NumericIndicator#min(Number)} returns a new {@link NumericIndicator}
     * whose values are the minimum between the wrapped indicator and the supplied constant.
     */
    @Test
    void minWithConstantNumber() {
        // constant that is lower than some values and higher than others
        Number constant = 4;

        NumericIndicator minIndicator = numericIndicator.min(constant);
        assertNotSame(numericIndicator, minIndicator,
                "min(Number) should return a new NumericIndicator instance");

        for (int i = 0; i < series.getBarCount(); i++) {
            Num delegateValue = closePriceIndicator.getValue(i);
            double expected = Math.min(delegateValue.doubleValue(), constant.doubleValue());
            Num expectedNum = DecimalNum.valueOf(expected);
            assertEquals(expectedNum, minIndicator.getValue(i),
                    "Incorrect min value at index " + i);
        }
    }

    /**
     * When the constant is greater than every value of the delegate,
     * the result should be identical to the original indicator.
     */
    @Test
    void minWithConstantHigherThanAllValues() {
        Number constant = 100; // larger than any price in the series

        NumericIndicator minIndicator = numericIndicator.min(constant);

        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i); // delegate value should win
            assertEquals(expected, minIndicator.getValue(i),
                    "When constant is higher, min should equal delegate at index " + i);
        }
    }

    /**
     * When the constant is lower than every value of the delegate,
     * the result should be the constant for all indices.
     */
    @Test
    void minWithConstantLowerThanAllValues() {
        Number constant = -10; // lower than any price in the series

        NumericIndicator minIndicator = numericIndicator.min(constant);
        Num expected = DecimalNum.valueOf(constant.doubleValue());

        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(expected, minIndicator.getValue(i),
                    "When constant is lower, min should equal constant at index " + i);
        }
    }
}
```