```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#squared()}.
 *
 * The original test suite used JUnit 5 and Mockito.  To keep the project
 * self‑contained we rewrite the tests using JUnit 4 and avoid external
 * mocking frameworks.  All functionality that does not depend on Mockito
 * is retained.
 */
public class NumericIndicator_squared_Test {

    /**
     * Function used by {@link BaseBarSeriesBuilder} to create {@link Num}
     * instances.
     */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /**
     * Series used in most tests.
     */
    private static BarSeries series;

    /**
     * Simple delegate indicator (close price).
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * NumericIndicator wrapping the delegate.
     */
    private NumericIndicator numericIndicator;

    @BeforeClass
    public static void setupBeforeAll() {
        // 20 values – the same data set used in the original test suite.
        series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumFunction(NUM_FUNCTION)
                .build();

        for (int i = 0; i < 20; i++) {
            // values: 1,2,3,4,5,6,7,8,9,8,7,6,5,4,3,2,1,0,-1,-2
            int[] raw = {1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2};
            Num n = NUM_FUNCTION.apply(raw[i]);
            // all price fields are the same; volume is zero
            series.addBar(
                    ZonedDateTime.now().plusDays(i),
                    n, // open
                    n, // high
                    n, // low
                    n, // close
                    DecimalNum.ZERO // volume
            );
        }

        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @Before
    public void setupBeforeEach() {
        // fresh NumericIndicator for each test method
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @Test
    public void squaredReturnsNewInstance() {
        NumericIndicator squared = numericIndicator.squared();
        assertNotNull("squared() must not return null", squared);
        assertNotSame("squared() should return a new NumericIndicator instance",
                numericIndicator, squared);
    }

    @Test
    public void squaredValuesAreCorrect() {
        NumericIndicator squared = numericIndicator.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i).multipliedBy(closePriceIndicator.getValue(i));
            Num actual = squared.getValue(i);
            assertEquals("Incorrect squared value at index " + i, expected, actual);
        }
    }

    @Test
    public void squaredWorksWithNegativeAndZeroValues() {
        // The series already contains positive, zero and negative values.
        NumericIndicator squared = numericIndicator.squared();
        // Spot‑check a few interesting indices.
        // 0 → 1, 8 → 9, 15 → 2, 16 → 1, 18 → -1, 19 → -2
        int[] indices = {0, 8, 15, 16, 18, 19};
        for (int i : indices) {
            Num value = closePriceIndicator.getValue(i);
            Num expected = value.multipliedBy(value);
            Num actual = squared.getValue(i);
            assertEquals("Squared value mismatch at index " + i + " (value=" + value + ")",
                    expected, actual);
        }
    }

    @Test
    public void delegateOfSquaredIsAnIndicator() {
        NumericIndicator squared = numericIndicator.squared();
        Indicator<Num> delegate = squared.delegate();
        assertNotNull("Delegate of the squared indicator must not be null", delegate);
        // The delegate should be a NumericIndicator (the result of multipliedBy(this))
        assertTrue("Delegate of squared() should be a NumericIndicator",
                delegate instanceof NumericIndicator);
    }

    @Test
    public void squaredDoesNotMutateOriginalIndicator() {
        // Capture original values before calling squared()
        Num[] originalValues = new Num[series.getBarCount()];
        for (int i = 0; i < series.getBarCount(); i++) {
            originalValues[i] = numericIndicator.getValue(i);
        }
        // Invoke squared()
        numericIndicator.squared();
        // Verify original values are unchanged
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("Original indicator mutated at index " + i,
                    originalValues[i], numericIndicator.getValue(i));
        }
    }

    @Test
    public void doubleSquaredYieldsFourthPower() {
        // x^2
        NumericIndicator firstSquare = numericIndicator.squared();
        // (x^2)^2 = x^4
        NumericIndicator fourthPower = firstSquare.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.pow(4);
            Num actual = fourthPower.getValue(i);
            assertEquals("Fourth‑power mismatch at index " + i, expected, actual);
        }
    }

    @Test
    public void compositionWithMultipliedByWorksCorrectly() {
        // (2 * x) squared = 4 * x^2
        NumericIndicator multiplied = numericIndicator.multipliedBy(2);
        NumericIndicator composed = multiplied.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            // 4 * x^2
            Num expected = x.multipliedBy(x).multipliedBy(DecimalNum.valueOf(4));
            Num actual = composed.getValue(i);
            assertEquals("Composed square mismatch at index " + i, expected, actual);
        }
    }

    @Test
    public void squaredPropagatesBarSeriesReference() {
        NumericIndicator squared = numericIndicator.squared();
        assertSame("BarSeries reference should be propagated unchanged by squared()",
                series, squared.getBarSeries());
    }

    @Test
    public void squaredPropagatesUnstableBarsCount() {
        // Unstable bars are defined by the underlying indicator; for ClosePriceIndicator it is 0.
        NumericIndicator squared = numericIndicator.squared();
        assertEquals("Unstable bars count must be identical after squared()",
                numericIndicator.getUnstableBars(), squared.getUnstableBars());
    }

    @Test
    public void emptySeriesThrowsIndexOutOfBoundsOnGetValue() {
        // no data points
        BarSeries emptySeries = new BaseBarSeriesBuilder()
                .withName("empty")
                .withNumFunction(NUM_FUNCTION)
                .build(); // no bars added
        ClosePriceIndicator emptyDelegate = new ClosePriceIndicator(emptySeries);
        NumericIndicator emptyNumeric = NumericIndicator.of(emptyDelegate);
        NumericIndicator squared = emptyNumeric.squared();

        try {
            squared.getValue(0);
            fail("Accessing any index on an empty series must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }
}
```