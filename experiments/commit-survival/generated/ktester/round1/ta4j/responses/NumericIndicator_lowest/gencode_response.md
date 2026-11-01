```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Additional tests for {@link NumericIndicator#lowest(int)} covering edge cases,
 * invalid arguments and usage on derived indicators.
 */
class NumericIndicator_lowest_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    @BeforeAll
    static void initNumFunction() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
    }

    /* --------------------------------------------------------------------- */
    /*  Positive, normal usage                                               */
    /* --------------------------------------------------------------------- */

    @Test
    void lowest_positive_barCount_calculatesCorrectValues() {
        // GIVEN a series with values [10, 8, 12, 5, 7]
        BarSeries series = new MockBarSeries(numFunction, 10, 8, 12, 5, 7);
        ClosePriceIndicator source = new ClosePriceIndicator(series);
        NumericIndicator numeric = NumericIndicator.of(source);

        // WHEN we request the lowest value over a window of 3 bars
        int barCount = 3;
        NumericIndicator lowest = numeric.lowest(barCount);

        // THEN the delegate must be a LowestValueIndicator
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator,
                "Delegate should be an instance of LowestValueIndicator");

        // AND the computed values must match the expected sequence:
        // [10, 8, 8, 5, 5] for indexes 0‑4
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(8),
                DecimalNum.valueOf(8),
                DecimalNum.valueOf(5),
                DecimalNum.valueOf(5)
        };
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i], lowest.getValue(i),
                    "Mismatch at index " + i);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Invalid arguments                                                    */
    /* --------------------------------------------------------------------- */

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void lowest_invalid_barCount_throwsIllegalArgumentException(int barCount) {
        // GIVEN a simple numeric indicator based on close price
        BarSeries series = new MockBarSeries(numFunction, 1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(series));

        // WHEN we call lowest with an illegal barCount
        // THEN an IllegalArgumentException is expected
        assertThrows(IllegalArgumentException.class,
                () -> numeric.lowest(barCount),
                "lowest(" + barCount + ") should throw IllegalArgumentException");
    }

    /* --------------------------------------------------------------------- */
    /*  Bar count larger than the series length                               */
    /* --------------------------------------------------------------------- */

    @Test
    void lowest_barCount_exceeds_series_usesAvailableValues() {
        // GIVEN a series with values [4, 9, 2, 7, 5]
        BarSeries series = new MockBarSeries(numFunction, 4, 9, 2, 7, 5);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(series));

        // WHEN we request a window larger than the series (barCount = 10)
        int barCount = 10;
        NumericIndicator lowest = numeric.lowest(barCount);

        // THEN the delegate is still a LowestValueIndicator
        assertTrue(lowest.delegate() instanceof LowestValueIndicator,
                "Delegate should be a LowestValueIndicator even when barCount > series length");

        // AND the values should be the minimum of all values seen so far:
        // Expected: [4, 4, 2, 2, 2]
        Num[] expected = {
                DecimalNum.valueOf(4),
                DecimalNum.valueOf(4),
                DecimalNum.valueOf(2),
                DecimalNum.valueOf(2),
                DecimalNum.valueOf(2)
        };
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i], lowest.getValue(i),
                    "Mismatch at index " + i);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Bar count of one (should mirror source)                               */
    /* --------------------------------------------------------------------- */

    @Test
    void lowest_barCount_one_mirrorsSourceValues() {
        // GIVEN a series with values [3, 6, 1, 8]
        BarSeries series = new MockBarSeries(numFunction, 3, 6, 1, 8);
        ClosePriceIndicator source = new ClosePriceIndicator(series);
        NumericIndicator numeric = NumericIndicator.of(source);

        // WHEN we ask for the lowest value with a window of 1
        NumericIndicator lowest = numeric.lowest(1);

        // THEN the result must be identical to the source values
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(source.getValue(i), lowest.getValue(i),
                    "Value at index " + i + " should be unchanged");
        }
    }

    /* --------------------------------------------------------------------- */
    /*  lowest() on a derived indicator (source.plus(2))                     */
    /* --------------------------------------------------------------------- */

    @Test
    void lowest_on_derived_indicator_calculatesCorrectWindow() {
        // GIVEN a series with values [5, 1, 4, 9, 2]
        BarSeries series = new MockBarSeries(numFunction, 5, 1, 4, 9, 2);
        ClosePriceIndicator source = new ClosePriceIndicator(series);
        // derived = source + 2
        NumericIndicator derived = NumericIndicator.of(source).plus(2);

        // WHEN we apply lowest(3) on the derived indicator
        NumericIndicator lowest = derived.lowest(3);

        // THEN the expected values are:
        // derived values: [7, 3, 6, 11, 4]
        // lowest(3) over them: [7, 3, 3, 3, 3]
        Num[] expected = {
                DecimalNum.valueOf(7),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(3),
                DecimalNum.valueOf(3)
        };
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i], lowest.getValue(i),
                    "Mismatch at index " + i);
        }
    }
}
```