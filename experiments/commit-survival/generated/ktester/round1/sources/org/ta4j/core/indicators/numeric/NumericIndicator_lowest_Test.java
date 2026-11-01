package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.time.ZonedDateTime;
import java.time.Duration;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.ValueSource;

class NumericIndicator_lowest_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * Series used by the tests.
     */
    private static BarSeries series;

    /**
     * Close‑price indicator based on {@link #series}.
     */
    private static ClosePriceIndicator closePrice;

    //     @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // A simple series with positive, zero and negative values.
        series = buildSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
    }

    //     @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePrice = null;
        numFunction = null;
    }

    /**
     * Build a simple {@link BarSeries} from the supplied double values.
     */
    private static BarSeries buildSeries(double... values) {
        BarSeries s = new BaseBarSeriesBuilder().withName("test").build();
        ZonedDateTime time = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (double v : values) {
            Num n = numFunction.apply(v);
            // For simplicity all price fields are the same and volume is zero
            BaseBar bar = new // open
            BaseBar(// open
            barDuration, // open
            time, // high
            n, // low
            n, // close
            n, // volume
            n, // amount (price * volume, not used here)
            n, DecimalNum.ZERO);
            s.addBar(bar);
            time = time.plusMinutes(1);
        }
        return s;
    }

    /* --------------------------------------------------------------------- */
    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("assertTrue failed");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("assertEquals failed – expected: " + expected + ", actual: " + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                // success
                return;
            }
            throw new AssertionError("assertThrows failed – expected exception of type " + expectedType.getName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError("assertThrows failed – expected exception of type " + expectedType.getName() + " but no exception was thrown");
    }

//     @Test
//     void lowestCreatesCorrectDelegateAndValues() {
//         NumericIndicator numeric = NumericIndicator.of(closePrice);
//         int barCount = 3;
//         NumericIndicator lowest = numeric.lowest(barCount);
//         Indicator<Num> delegate = lowest.delegate();
//         Assertions.assertTrue(delegate instanceof LowestValueIndicator);
//         LowestValueIndicator expected = new LowestValueIndicator(closePrice, barCount);
//         for (int i = 0; i < series.getBarCount(); i++) {
//             Assertions.assertEquals(expected.getValue(i), lowest.getValue(i));
//         }
//     }

//     @Test
//     void lowestWithBarCountGreaterThanSeriesLength() {
//         NumericIndicator numeric = NumericIndicator.of(closePrice);
//         int barCount = series.getBarCount() + 5;
//         NumericIndicator lowest = numeric.lowest(barCount);
//         Assertions.assertTrue(lowest.delegate() instanceof LowestValueIndicator);
//         Num min = closePrice.getValue(0);
//         for (int i = 1; i < series.getBarCount(); i++) {
//             min = min.min(closePrice.getValue(i));
//         }
//         Assertions.assertEquals(min, lowest.getValue(series.getBarCount() - 1));
//     }

//     @Test
//     void lowest_positive_barCount_calculatesCorrectValues() {
//         BarSeries s = buildSeries(10, 8, 12, 5, 7);
//         ClosePriceIndicator source = new ClosePriceIndicator(s);
//         NumericIndicator numeric = NumericIndicator.of(source);
//         NumericIndicator lowest = numeric.lowest(3);
//         Indicator<Num> delegate = lowest.delegate();
//         Assertions.assertTrue(delegate instanceof LowestValueIndicator);
//         Num[] expected = { DecimalNum.valueOf(10), DecimalNum.valueOf(8), DecimalNum.valueOf(8), DecimalNum.valueOf(5), DecimalNum.valueOf(5) };
//         for (int i = 0; i < s.getBarCount(); i++) {
//             Assertions.assertEquals(expected[i], lowest.getValue(i));
//         }
//     }

//     @ParameterizedTest
//     @ValueSource(ints = { 0, -5 })
//     void lowest_invalid_barCount_throwsIllegalArgumentException(int barCount) {
//         BarSeries s = buildSeries(1, 2, 3);
//         NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
//         Assertions.assertThrows(IllegalArgumentException.class, () -> numeric.lowest(barCount));
//     }

//     @Test
//     void lowest_barCount_exceeds_series_usesAvailableValues() {
//         BarSeries s = buildSeries(4, 9, 2, 7, 5);
//         NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
//         NumericIndicator lowest = numeric.lowest(10);
//         Assertions.assertTrue(lowest.delegate() instanceof LowestValueIndicator);
//         Num[] expected = { DecimalNum.valueOf(4), DecimalNum.valueOf(4), DecimalNum.valueOf(2), DecimalNum.valueOf(2), DecimalNum.valueOf(2) };
//         for (int i = 0; i < s.getBarCount(); i++) {
//             Assertions.assertEquals(expected[i], lowest.getValue(i));
//         }
//     }

//     @Test
//     void lowest_barCount_one_mirrorsSourceValues() {
//         BarSeries s = buildSeries(3, 6, 1, 8);
//         ClosePriceIndicator source = new ClosePriceIndicator(s);
//         NumericIndicator numeric = NumericIndicator.of(source);
//         NumericIndicator lowest = numeric.lowest(1);
//         for (int i = 0; i < s.getBarCount(); i++) {
//             Assertions.assertEquals(source.getValue(i), lowest.getValue(i));
//         }
//     }

//     @Test
//     void lowest_on_derived_indicator_calculatesCorrectWindow() {
//         BarSeries s = buildSeries(5, 1, 4, 9, 2);
//         ClosePriceIndicator source = new ClosePriceIndicator(s);
//         NumericIndicator derived = NumericIndicator.of(source).plus(2);
//         NumericIndicator lowest = derived.lowest(3);
//         Num[] expected = { DecimalNum.valueOf(7), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3) };
//         for (int i = 0; i < s.getBarCount(); i++) {
//             Assertions.assertEquals(expected[i], lowest.getValue(i));
//         }
//     }

//     @Test
//     public void lowest_invalid_barCount_zero_throwsIllegalArgumentException() {
//         BarSeries s = buildSeries(1, 2, 3);
//         NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
//         Assertions.assertThrows(IllegalArgumentException.class, () -> numeric.lowest(0));
//     }

//     @Test
//     public void lowest_invalid_barCount_negative_throwsIllegalArgumentException() {
//         BarSeries s = buildSeries(1, 2, 3);
//         NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
//         Assertions.assertThrows(IllegalArgumentException.class, () -> numeric.lowest(-5));
//     }
}