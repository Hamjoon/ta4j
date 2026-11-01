```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#squared()}.
 *
 * <p>The method {@code squared()} is defined as {@code return this.multipliedBy(this);}
 * therefore the resulting indicator must return the square of the original
 * indicator's value for each index.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_squared_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /** Series used in all tests. */
    private static BarSeries series;

    /** Simple delegate indicator (close price). */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator wrapping the delegate. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // 20 values – the same data set used in the original test suite.
        series = new MockBarSeries(
                NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setupBeforeEach() {
        // fresh NumericIndicator for each test method
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No mutable state to clean up at the moment.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing special to release.
    }

    @Test
    void squaredReturnsNewInstance() {
        NumericIndicator squared = numericIndicator.squared();
        assertNotNull(squared, "squared() must not return null");
        assertNotSame(numericIndicator, squared,
                "squared() should return a new NumericIndicator instance");
    }

    @Test
    void squaredValuesAreCorrect() {
        NumericIndicator squared = numericIndicator.squared();

        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i).multipliedBy(closePriceIndicator.getValue(i));
            Num actual = squared.getValue(i);
            assertEquals(expected, actual,
                    () -> "Incorrect squared value at index " + i + ": expected " + expected + " but was " + actual);
        }
    }

    @Test
    void squaredWorksWithNegativeAndZeroValues() {
        // The series already contains positive, zero and negative values.
        NumericIndicator squared = numericIndicator.squared();

        // Spot‑check a few interesting indices.
        int[] indices = {0, 8, 15, 16, 18, 19}; // 1, 9, 2, 1, -1, -2
        for (int i : indices) {
            Num value = closePriceIndicator.getValue(i);
            Num expected = value.multipliedBy(value);
            Num actual = squared.getValue(i);
            assertEquals(expected, actual,
                    () -> "Squared value mismatch at index " + i + " (value=" + value + ")");
        }
    }

    @Test
    void delegateOfSquaredIsAnIndicator() {
        NumericIndicator squared = numericIndicator.squared();
        Indicator<Num> delegate = squared.delegate();
        assertNotNull(delegate, "Delegate of the squared indicator must not be null");
        // The delegate should be a NumericIndicator (the result of multipliedBy(this))
        assertTrue(delegate instanceof NumericIndicator,
                "Delegate of squared() should be a NumericIndicator");
    }
}
```