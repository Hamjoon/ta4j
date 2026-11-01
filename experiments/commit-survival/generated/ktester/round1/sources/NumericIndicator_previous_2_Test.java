package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseBar;
import java.time.Duration;
import java.time.ZonedDateTime;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Assertions;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Assertions;
class NumericIndicator_previous_2_Test {

    /**
     * Series used by all tests – a simple deterministic series of numbers.
     */
    private static BarSeries series;

    /**
     * Close price indicator based on the series (used as delegate).
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * Instance of the class under test.
     */
    private static NumericIndicator numericIndicator;

    /**
     * Build a deterministic series with known values before any test runs.
     */
//     @BeforeAll
    static void setupBeforeAll() {
        series = new BaseBarSeriesBuilder().withName("test-series").build();
        // Add 20 bars with identical open/high/low/close/volume values.
        for (double v : new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 }) {
            series.addBar(new BaseBar(Duration.ofSeconds(1), ZonedDateTime.now(), // open
//             v, // high
            v, // low
            v, // close
            v, // volume
            v, // amount (not used in the tests)
            v, 0));
        }
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    //     @AfterAll
    static void teardownAfterAll() {
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
                // not needed for the stub
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

//     @Test
//     void previousDefaultShouldReturnIndicatorShiftedByOne() {
//         // The default previous() is defined as previous(1)
//         Indicator<Num> previous = numericIndicator.previous();
//         // The value at index i of the returned indicator should equal the original value at i‑1.
//         Assertions.assertEquals(closePriceIndicator.getValue(0), previous.getValue(1));
//         Assertions.assertEquals(closePriceIndicator.getValue(5), previous.getValue(6));
//     }

//     @Test
//     void previousWithBarCountShouldShiftAccordingly() {
//         // Ask for a shift of 3 bars.
//         Indicator<Num> previous3 = numericIndicator.previous(3);
//         // Verify that the shift is exactly three positions.
//         Assertions.assertEquals(closePriceIndicator.getValue(3), previous3.getValue(6));
//         Assertions.assertEquals(closePriceIndicator.getValue(0), previous3.getValue(3));
//     }

//     @Test
//     void previousIsConvenienceWrapperForPreviousOne() {
//         Indicator<Num> byDefault = numericIndicator.previous();
//         Indicator<Num> byOne = numericIndicator.previous(1);
//         for (int i = 0; i < series.getBarCount(); i++) {
//             Assertions.assertEquals(byOne.getValue(i), byDefault.getValue(i));
//         }
//     }

//     @Test
//     void previousDoesNotMutateDelegate() {
//         Num v3Before = closePriceIndicator.getValue(3);
//         Num v7Before = closePriceIndicator.getValue(7);
//         // side‑effect free call
//         numericIndicator.previous();
//         Assertions.assertEquals(v3Before, closePriceIndicator.getValue(3));
//         Assertions.assertEquals(v7Before, closePriceIndicator.getValue(7));
//     }

//     @Test
//     void successivePreviousCallsReturnDistinctInstances() {
//         Indicator<Num> first = numericIndicator.previous();
//         Indicator<Num> second = numericIndicator.previous();
//         Assertions.assertNotSame(first, second);
//     }

//     @Test
//     void previousZeroIsIdentity() {
//         Indicator<Num> identity = numericIndicator.previous(0);
//         for (int i = 0; i < series.getBarCount(); i++) {
//             Assertions.assertEquals(closePriceIndicator.getValue(i), identity.getValue(i));
//         }
//     }

//     @Test
//     void previousWithNullDelegateThrowsNPE() {
//         NumericIndicator nullNumeric = NumericIndicator.of(null);
//         Assertions.assertThrows(NullPointerException.class, nullNumeric::previous);
//     }

//     @Test
//     void previousShiftWithStubIndicator() {
//         NumericIndicator stubNumeric = NumericIndicator.of(indexIndicator());
//         Indicator<Num> shifted = stubNumeric.previous(3);
//         // i >= 3 -> shifted value equals i-3
//         for (int i = 3; i < 10; i++) {
//             Assertions.assertEquals(DecimalNum.valueOf(i - 3), shifted.getValue(i));
//         }
//         // i < 3 -> NaN
//         for (int i = 0; i < 3; i++) {
//             Assertions.assertTrue(shifted.getValue(i) instanceof NaN);
//         }
//     }

//     @Test
//     public void previousShouldReturnNaNWhenIndexIsOutOfBounds() {
//         Indicator<Num> previous = numericIndicator.previous();
//         // Index 0 would need a value from -1, therefore the implementation should return NaN.
//         Num outOfRange = previous.getValue(0);
//         Assertions.assertTrue(outOfRange instanceof NaN);
//     }
}