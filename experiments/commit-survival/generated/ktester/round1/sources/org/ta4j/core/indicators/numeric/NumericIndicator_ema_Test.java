package org.ta4j.core.indicators.numeric;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.ta4j.core.TestUtils.assertNumEquals;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;

// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.params.provider.ValueSource;

// import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
// import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.numeric.NumericIndicator;

/**
 * Tests for {@link NumericIndicator#ema(int)}.
 */
// @ExtendWith(MockitoExtension.class)
class NumericIndicator_ema_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /**
     * Simple series with deterministic values.
     */
    private static BarSeries series;

    /**
     * Close‑price indicator based on {@code series}.
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * NumericIndicator wrapping the close‑price indicator.
     */
    private static NumericIndicator numericIndicator;

//     @BeforeAll
    static void setupBeforeAll() {
        // a short, deterministic series – the exact values are not important,
        // they just need to be stable for the EMA calculation.
//         series = new MockBarSeries(NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

//     @AfterAll
    static void teardownAfterAll() {
        // Release static references (helps the GC in large test suites).
        series = null;
        closePriceIndicator = null;
        numericIndicator = null;
    }

    /**
     * Helper to extract the private {@code barCount} field from EMAIndicator via reflection.
     */
    private static int getBarCountFromEma(EMAIndicator ema) {
        try {
            Field f = EMAIndicator.class.getDeclaredField("barCount");
            f.setAccessible(true);
            return (int) f.get(ema);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper to extract the source indicator used by an EMAIndicator via reflection.
     */
    private static Indicator<Num> getSourceFromEma(EMAIndicator ema) {
        try {
            Field f = EMAIndicator.class.getDeclaredField("indicator");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Indicator<Num> src = (Indicator<Num>) f.get(ema);
            return src;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

//     static Stream<Arguments> edgeBarCounts() {
//         return Stream.of(Arguments.of(Integer.MAX_VALUE), Arguments.of(1_000_000));
//     }

//     @Test
//     void emaReturnsNumericIndicatorWithEmaDelegate() {
//         final int barCount = 3;
//         // invoke the method under test
//         NumericIndicator emaNumeric = numericIndicator.ema(barCount);
//         // the returned object must not be null
//         assertNotNull(emaNumeric, "ema() should return a non‑null NumericIndicator");
//         // its delegate must be an EMAIndicator
//         Indicator<Num> delegate = emaNumeric.delegate();
//         assertNotNull(delegate, "Delegate of the returned NumericIndicator must not be null");
//         assertEquals(EMAIndicator.class, delegate.getClass(),
//                 "Delegate should be an instance of EMAIndicator");
//         // verify that the EMA values are the same as a directly created EMAIndicator
//         EMAIndicator expectedEma = new EMAIndicator(closePriceIndicator, barCount);
//         for (int i = 0; i < series.getBarCount(); i++) {
//             assertNumEquals(expectedEma.getValue(i), emaNumeric.getValue(i),
//                     "EMA value at index " + i + " differs from expected");
//         }
//     }

//     @Test
//     void emaWorksWithDifferentUnderlyingIndicators() {
//         // Use a different underlying indicator (e.g., HighestValueIndicator)
//         Indicator<Num> highest = new HighestValueIndicator(closePriceIndicator, 5);
//         NumericIndicator highestNumeric = NumericIndicator.of(highest);
//         final int barCount = 4;
//         NumericIndicator emaFromHighest = highestNumeric.ema(barCount);
//         // delegate type must still be EMAIndicator
//         assertEquals(EMAIndicator.class, emaFromHighest.delegate().getClass(),
//                 "Delegate should be EMAIndicator even when the underlying indicator is not a ClosePriceIndicator");
//         // compare values with a manually created EMAIndicator
//         EMAIndicator expected = new EMAIndicator(highest, barCount);
//         for (int i = 0; i < series.getBarCount(); i++) {
//             assertNumEquals(expected.getValue(i), emaFromHighest.getValue(i),
//                     "EMA value from highest indicator differs at index " + i);
//         }
//     }

    /* ---------- Valid barCount values (parameterized) ---------- */
//     @ParameterizedTest(name = "ema({0}) should wrap an EMAIndicator with the same barCount")
//     @ValueSource(ints = { 1, 5, 20 })
//     void emaValidBarCountCreatesCorrectWrapper(int barCount) {
//         NumericIndicator result = numericIndicator.ema(barCount);
//         assertNotNull(result, "Result must not be null");
//         assertEquals(EMAIndicator.class, result.delegate().getClass(),
//                 "Delegate must be an EMAIndicator");
//         // compare with a manually created EMAIndicator to ensure the barCount is honoured
//         EMAIndicator expected = new EMAIndicator(closePriceIndicator, barCount);
//         EMAIndicator actual = (EMAIndicator) result.delegate();
//         // verify barCount via reflection (ensures internal state matches)
//         assertEquals(getBarCountFromEma(expected), getBarCountFromEma(actual),
//                 "Internal barCount of EMAIndicator does not match the requested value");
//         // verify calculated values are identical
//         for (int i = 0; i < series.getBarCount(); i++) {
//             assertNumEquals(expected.getValue(i), result.getValue(i),
//                     "EMA value mismatch at index " + i);
//         }
//     }

    /* ---------- Invalid barCount values (exception handling) ---------- */
//     @ParameterizedTest(name = "ema({0}) must throw IllegalArgumentException")
//     @ValueSource(ints = { 0, -3, Integer.MIN_VALUE })
//     void emaInvalidBarCountThrowsException(int barCount) {
//         assertThrows(IllegalArgumentException.class,
//                 () -> numericIndicator.ema(barCount),
//                 "Calling ema(" + barCount + ") should throw IllegalArgumentException");
//     }

    /* ---------- Edge barCount values (no exception expected) ---------- */
//     @ParameterizedTest(name = "ema({0}) should not throw and return a valid wrapper")
//     @MethodSource("edgeBarCounts")
//     void emaEdgeBarCountDoesNotThrow(int barCount) {
//         assertDoesNotThrow(() -> {
//             NumericIndicator result = numericIndicator.ema(barCount);
//             assertNotNull(result);
//             assertEquals(EMAIndicator.class, result.delegate().getClass(),
//                     "Delegate must be EMAIndicator for edge barCount " + barCount);
//         });
//     }

    /* ---------- BarCount larger than series length ---------- */
//     @Test
//     void emaBarCountLargerThanSeriesLengthIsHandledGracefully() {
//         // definitely larger than series length
//         int barCount = series.getBarCount() + 10;
//         NumericIndicator result = numericIndicator.ema(barCount);
//         assertNotNull(result);
//         assertEquals(EMAIndicator.class, result.delegate().getClass(),
//                 "Delegate must be EMAIndicator even when barCount exceeds series length");
//         // No further value comparison – the EMAIndicator should still be constructible.
//     }

    /* ---------- Multiple ema calls produce independent instances ---------- */
//     @Test
//     void multipleEmaCallsReturnDistinctObjects() {
//         NumericIndicator first = numericIndicator.ema(5);
//         NumericIndicator second = numericIndicator.ema(5);
//         assertNotSame(first, second,
//                 "Two calls to ema(5) must return different NumericIndicator instances");
//         assertNotSame(first.delegate(), second.delegate(),
//                 "Delegates of the two EMA wrappers must be distinct objects");
//         // sanity check: both produce identical values
//         EMAIndicator expected = new EMAIndicator(closePriceIndicator, 5);
//         for (int i = 0; i < series.getBarCount(); i++) {
//             assertNumEquals(expected.getValue(i), first.getValue(i),
//                     "First EMA value mismatch at index " + i);
//             assertNumEquals(expected.getValue(i), second.getValue(i),
//                     "Second EMA value mismatch at index " + i);
//         }
//     }

    /* ---------- Delegate integrity after ema ---------- */
//     @Test
//     void originalNumericIndicatorDelegateRemainsUnchangedAfterEma() {
//         Indicator<Num> originalDelegate = numericIndicator.delegate();
//         assertEquals(closePriceIndicator, originalDelegate,
//                 "Before calling ema, delegate should be the original ClosePriceIndicator");
//         NumericIndicator emaWrapper = numericIndicator.ema(8);
//         // original NumericIndicator must stay unchanged
//         assertEquals(closePriceIndicator, numericIndicator.delegate(),
//                 "Original NumericIndicator's delegate must not be altered by ema()");
//         // the EMA wrapper's source indicator should be the original NumericIndicator
//         EMAIndicator emaDelegate = (EMAIndicator) emaWrapper.delegate();
//         Indicator<Num> source = getSourceFromEma(emaDelegate);
//         assertSame(numericIndicator, source,
//                 "The EMAIndicator inside the wrapper must use the original NumericIndicator as its source");
//     }

    /* ---------- Chaining ema calls ---------- */
//     @Test
//     void chainingEmaCreatesNestedEmaIndicators() {
//         int firstBarCount = 3;
//         int secondBarCount = 4;
//         NumericIndicator chained = numericIndicator.ema(firstBarCount).ema(secondBarCount);
//         // outermost delegate must be EMAIndicator with barCount = secondBarCount
//         EMAIndicator outerEma = (EMAIndicator) chained.delegate();
//         assertEquals(secondBarCount, getBarCountFromEma(outerEma),
//                 "Outer EMAIndicator must have barCount = " + secondBarCount);
//         // its source must be another EMAIndicator (the inner one)
//         Indicator<Num> innerSource = getSourceFromEma(outerEma);
//         assertTrue(innerSource instanceof EMAIndicator,
//                 "Inner source of outer EMA must be another EMAIndicator");
//         EMAIndicator innerEma = (EMAIndicator) innerSource;
//         assertEquals(firstBarCount, getBarCountFromEma(innerEma),
//                 "Inner EMAIndicator must have barCount = " + firstBarCount);
//         // inner EMA's source must be the original NumericIndicator
//         Indicator<Num> innermostSource = getSourceFromEma(innerEma);
//         assertSame(numericIndicator, innermostSource,
//                 "Innermost source must be the original NumericIndicator");
//     }
}