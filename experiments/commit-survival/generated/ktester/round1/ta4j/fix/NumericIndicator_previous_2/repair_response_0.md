```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#previous()} and related overloads.
 *
 * <p>The original test suite was written for JUnit 5 and used Mockito.
 * This version has been adapted to JUnit 4 (which is available in the
 * compilation environment) and removes the unnecessary Mockito
 * dependency.</p>
 */
public class NumericIndicator_previous_2_Test {

    /** Series used by all tests – a simple deterministic series of numbers. */
    private static BarSeries series;

    /** Close price indicator based on the series (used as delegate). */
    private static ClosePriceIndicator closePriceIndicator;

    /** Instance of the class under test. */
    private static NumericIndicator numericIndicator;

    /** Function converting a {@link Number} to {@link Num}. */
    private static final Function<Number, Num> NUM_FUNCTION =
            n -> DecimalNum.valueOf(n.doubleValue());

    @BeforeClass
    public static void setupBeforeAll() {
        // Build a mock series with a known set of values.
        series = new MockBarSeries(NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        // Create the NumericIndicator wrapper that we will test.
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterClass
    public static void teardownAfterAll() {
        numericIndicator = null;
        closePriceIndicator = null;
        series = null;
    }

    /**
     * Helper stub indicator that simply returns its index as a {@link Num}.
     * Used for a few tests that need a lightweight, deterministic delegate.
     */
    private static Indicator<Num> indexIndicator() {
        return new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return DecimalNum.valueOf(index);
            }

            @Override
            public BarSeries getBarSeries() {
                // Not needed for the tests that use this stub
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "IndexIndicator";
            }
        };
    }

    @Test
    public void previousDefaultShouldReturnIndicatorShiftedByOne() {
        // The default previous() is defined as previous(1)
        Indicator<Num> previous = numericIndicator.previous();
        // The value at index i of the returned indicator should equal the original value at i‑1.
        assertEquals(closePriceIndicator.getValue(0), previous.getValue(1));
        assertEquals(closePriceIndicator.getValue(5), previous.getValue(6));
    }

    @Test
    public void previousWithBarCountShouldShiftAccordingly() {
        // Ask for a shift of 3 bars.
        Indicator<Num> previous3 = numericIndicator.previous(3);
        // Verify that the shift is exactly three positions.
        assertEquals(closePriceIndicator.getValue(3), previous3.getValue(6));
        assertEquals(closePriceIndicator.getValue(0), previous3.getValue(3));
    }

    @Test
    public void previousShouldReturnNaNWhenIndexIsOutOfBounds() {
        Indicator<Num> previous = numericIndicator.previous();
        // Index 0 would need a value from -1, therefore the implementation should return NaN.
        Num outOfRange = previous.getValue(0);
        assertTrue(outOfRange instanceof NaN);
    }

    @Test
    public void previousIsConvenienceWrapperForPreviousOne() {
        Indicator<Num> byDefault = numericIndicator.previous();
        Indicator<Num> byOne = numericIndicator.previous(1);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(byOne.getValue(i), byDefault.getValue(i));
        }
    }

    @Test
    public void previousDoesNotMutateDelegate() {
        Num v3Before = closePriceIndicator.getValue(3);
        Num v7Before = closePriceIndicator.getValue(7);
        numericIndicator.previous(); // side‑effect free call
        assertEquals(v3Before, closePriceIndicator.getValue(3));
        assertEquals(v7Before, closePriceIndicator.getValue(7));
    }

    @Test
    public void successivePreviousCallsReturnDistinctInstances() {
        Indicator<Num> first = numericIndicator.previous();
        Indicator<Num> second = numericIndicator.previous();
        assertNotSame(first, second);
    }

    @Test
    public void previousZeroIsIdentity() {
        Indicator<Num> identity = numericIndicator.previous(0);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePriceIndicator.getValue(i), identity.getValue(i));
        }
    }

    @Test
    public void previousNegativeBarCountThrows() {
        int[] badValues = { -5, -1 };
        for (int barCount : badValues) {
            try {
                numericIndicator.previous(barCount);
                fail("previous(" + barCount + ") should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                // expected
            }
        }
    }

    @Test
    public void previousWithNullDelegateThrowsNPE() {
        NumericIndicator nullNumeric = NumericIndicator.of(null);
        try {
            nullNumeric.previous();
            fail("Calling previous() on a NumericIndicator with null delegate should throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void previousShiftWithStubIndicator() {
        NumericIndicator stubNumeric = NumericIndicator.of(indexIndicator());
        Indicator<Num> shifted = stubNumeric.previous(3);
        // i >= 3 -> shifted value equals i-3
        for (int i = 3; i < 10; i++) {
            assertEquals(DecimalNum.valueOf(i - 3), shifted.getValue(i));
        }
        // i < 3 -> NaN
        for (int i = 0; i < 3; i++) {
            assertTrue(shifted.getValue(i) instanceof NaN);
        }
    }
}
```