```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive tests for {@link NumericIndicator#previous(int)}.
 *
 * <p>
 * The test suite covers:
 * <ul>
 *   <li>behaviour for normal, zero and negative {@code barCount}s,</li>
 *   <li>edge cases with very large {@code barCount}s,</li>
 *   <li>chaining of successive {@code previous()} calls,</li>
 *   <li>interaction with constant and {@code null} delegates,</li>
 *   <li>verification that the returned {@link NumericIndicator} really wraps a
 *   {@link PreviousValueIndicator}.</li>
 * </ul>
 *
 * <p>
 * The {@code @BeforeAll}/{@code @AfterAll} methods are left untouched as
 * required by the original framework.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_previous_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** Series used by most tests (20 bars). */
    private static BarSeries series;

    /** Close price indicator used as the delegate for {@link NumericIndicator}. */
    private static ClosePriceIndicator closePriceIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = DecimalNum::valueOf;

        // Build a simple series with deterministic values.
        series = new MockBarSeries(
                numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8,
                7, 6, 5, 4, 3, 2, 1, 0, -1, -2
        );

        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
        numFunction = null;
    }

    /* --------------------------------------------------------------------- */
    /*  Behaviour verification (barCount = 0, 1, 5, -1)                      */
    /* --------------------------------------------------------------------- */

    @Test
    void shouldReturnIdenticalValuesWhenBarCountIsZero() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        NumericIndicator shifted = original.previous(0);

        // Every index must return the same value as the original indicator.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(original.getValue(i), shifted.getValue(i),
                    () -> "Index " + i + " differs for barCount 0");
        }
    }

    @ParameterizedTest(name = "barCount={0} shifts values correctly")
    @ValueSource(ints = {1, 5})
    void shouldShiftValuesByBarCount(int barCount) {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        NumericIndicator shifted = original.previous(barCount);

        // For indices where the source index is non‑negative, compare shifted value.
        for (int i = barCount; i < series.getBarCount(); i++) {
            Num expected = original.getValue(i - barCount);
            Num actual = shifted.getValue(i);
            assertEquals(expected, actual,
                    () -> "Mismatch at index " + i + " (expected " + expected + ", got " + actual + ")");
        }

        // For the first {@code barCount} indices the contract of PreviousValueIndicator
        // returns the earliest available value (index 0). Verify that behaviour.
        Num earliest = original.getValue(0);
        for (int i = 0; i < barCount; i++) {
            assertEquals(earliest, shifted.getValue(i),
                    () -> "Earliest fallback failed at index " + i);
        }
    }

    @Test
    void shouldThrowExceptionForNegativeBarCount() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        assertThrows(IllegalArgumentException.class,
                () -> original.previous(-1),
                "Negative barCount should raise IllegalArgumentException");
    }

    /* --------------------------------------------------------------------- */
    /*  Large barCount handling (10, 15, Integer.MAX_VALUE)                */
    /* --------------------------------------------------------------------- */

    private static BarSeries smallSeries() {
        // 10‑bar series: values 0..9
        return new MockBarSeries(numFunction,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @ParameterizedTest(name = "large barCount={0}")
    @MethodSource("largeBarCountProvider")
    void shouldHandleVeryLargeBarCountGracefully(int barCount) {
        BarSeries small = smallSeries();
        NumericIndicator original = NumericIndicator.of(new ClosePriceIndicator(small));
        NumericIndicator shifted = original.previous(barCount);

        // For any index i, the expected value is the earliest (index 0) because
        // barCount >= series length.
        Num earliest = original.getValue(0);
        for (int i = 0; i < small.getBarCount(); i++) {
            assertEquals(earliest, shifted.getValue(i),
                    () -> "Index " + i + " should return earliest value for barCount " + barCount);
        }
    }

    private static Stream<Arguments> largeBarCountProvider() {
        return Stream.of(
                Arguments.of(10),                     // equal to series length
                Arguments.of(15),                     // greater than series length
                Arguments.of(Integer.MAX_VALUE)       // extreme positive value
        );
    }

    @Test
    void shouldWrapWithPreviousValueIndicatorEvenWhenBarCountIsZero() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        NumericIndicator shifted = original.previous(0);
        Indicator<Num> delegate = shifted.delegate();
        assertTrue(delegate instanceof PreviousValueIndicator,
                "Delegate must be a PreviousValueIndicator even for barCount 0");
        // Verify the internal offset is indeed 0 via reflection (no public getter).
        PreviousValueIndicator pvi = (PreviousValueIndicator) delegate;
        // The offset field is protected in the original class; we can access it via reflection.
        try {
            java.lang.reflect.Field offsetField = PreviousValueIndicator.class.getDeclaredField("barCount");
            offsetField.setAccessible(true);
            int offset = (int) offsetField.get(pvi);
            assertEquals(0, offset, "Internal offset should be 0");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Unable to inspect PreviousValueIndicator offset: " + e.getMessage());
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Chaining behaviour (previous(2).previous(3) == previous(5))          */
    /* --------------------------------------------------------------------- */

    @Test
    void shouldAccumulateOffsetsWhenChained() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        NumericIndicator chained = original.previous(2).previous(3); // total offset 5
        NumericIndicator single = original.previous(5);

        // For indices where source index is non‑negative, both must match.
        for (int i = 5; i < series.getBarCount(); i++) {
            assertEquals(single.getValue(i), chained.getValue(i),
                    () -> "Mismatch at index " + i + " for chained vs single offset");
        }

        // For the first five indices the contract returns the earliest value.
        Num earliest = original.getValue(0);
        for (int i = 0; i < 5; i++) {
            assertEquals(earliest, chained.getValue(i),
                    () -> "Earliest fallback failed at index " + i + " for chained version");
        }
    }

    @Test
    void singleOffsetEqualsChainedVersionForAllIndices() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        NumericIndicator single = original.previous(5);
        NumericIndicator chained = original.previous(2).previous(3);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(single.getValue(i), chained.getValue(i),
                    () -> "Values differ at index " + i);
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Constant delegate behaviour                                           */
    /* --------------------------------------------------------------------- */

    @Test
    void shouldReturnConstantValueRegardlessOfBarCount() {
        Num constant = DecimalNum.valueOf(42);
        ConstantIndicator constantIndicator = new ConstantIndicator(constant);
        NumericIndicator numeric = NumericIndicator.of(constantIndicator);

        // Test a few different barCounts, including a large one.
        int[] barCounts = {0, 1, 4, 10, Integer.MAX_VALUE};
        for (int barCount : barCounts) {
            NumericIndicator shifted = numeric.previous(barCount);
            for (int i = 0; i < series.getBarCount(); i++) {
                assertEquals(constant, shifted.getValue(i),
                        () -> "Constant value mismatch for barCount " + barCount + " at index " + i);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Extreme negative barCount (Integer.MIN_VALUE)                        */
    /* --------------------------------------------------------------------- */

    @Test
    void shouldRejectIntegerMinValueBarCount() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        assertThrows(IllegalArgumentException.class,
                () -> original.previous(Integer.MIN_VALUE),
                "Integer.MIN_VALUE should be considered invalid");
    }

    /* --------------------------------------------------------------------- */
    /*  Null delegate handling                                                */
    /* --------------------------------------------------------------------- */

    @Test
    void shouldThrowWhenDelegateIsNull() {
        // NumericIndicator.of(null) is expected to throw NPE/IllegalArgumentException.
        // Guard the creation to keep the test focused on the contract.
        assertThrows(NullPointerException.class,
                () -> NumericIndicator.of(null),
                "Creating a NumericIndicator with a null delegate should fail");
    }

    @Test
    void shouldThrowWhenCallingPreviousOnNullDelegateIndicator() {
        // If the library allowed creation with null (unlikely), calling previous must fail.
        // We simulate the scenario via reflection to be safe.
        NumericIndicator broken = null;
        try {
            java.lang.reflect.Constructor<NumericIndicator> ctor =
                    NumericIndicator.class.getDeclaredConstructor(Indicator.class);
            ctor.setAccessible(true);
            broken = ctor.newInstance((Object) null);
        } catch (Exception e) {
            // If construction itself fails, the test is satisfied.
            assertTrue(e instanceof IllegalArgumentException || e instanceof NullPointerException);
            return;
        }
        // If we managed to instantiate, calling previous must raise an exception.
        NumericIndicator finalBroken = broken;
        assertThrows(IllegalArgumentException.class,
                () -> finalBroken.previous(0),
                "previous() on a NumericIndicator with null delegate should fail");
    }
}
```