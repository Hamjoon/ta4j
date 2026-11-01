```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
 * Tests for {@link NumericIndicator#previous(int)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_previous_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * Series used by all tests.
     */
    private static BarSeries series;

    /**
     * Close price indicator used as the delegate for {@link NumericIndicator}.
     */
    private static ClosePriceIndicator closePriceIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = DecimalNum::valueOf;
        // Build a simple series with deterministic values.
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (helps GC in large test suites).
        series = null;
        closePriceIndicator = null;
        numFunction = null;
    }

    private static BarSeries smallSeries() {
        // 10‑bar series: values 0..9
        return new MockBarSeries(numFunction, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    private static Stream<Arguments> largeBarCountProvider() {
        return Stream.of(
                // equal to series length
                Arguments.of(10),
                // greater than series length
                Arguments.of(15),
                // extreme positive value
                Arguments.of(Integer.MAX_VALUE));
    }

    @Test
    void shouldShiftValuesByOneBarWhenBarCountIsOne() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePriceIndicator);
        // when
        // shift by 1 bar
        NumericIndicator shifted = numeric.previous(1);
        // then
        // value at index 1 should be the original value at index 0
        assertEquals(closePriceIndicator.getValue(0), shifted.getValue(1));
    }

    @Test
    void shouldShiftValuesByGivenBarCount() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePriceIndicator);
        int barCount = 3;
        // when
        NumericIndicator shifted = numeric.previous(barCount);
        // then
        // value at index i + barCount should equal the original value at index i
        for (int i = 0; i < series.getBarCount() - barCount; i++) {
            Num expected = closePriceIndicator.getValue(i);
            Num actual = shifted.getValue(i + barCount);
            assertEquals(expected, actual,
                    () -> "Mismatch at original index " + i + " (expected " + expected + ", got " + actual + ")");
        }
    }

    @Test
    void previousReturnsIndicatorImplementingIndicatorInterface() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePriceIndicator);
        // when
        Indicator<Num> result = numeric.previous(2);
        // then
        assertTrue(result instanceof Indicator, "Result should implement Indicator");
    }

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

    @Test
    void shouldWrapWithPreviousValueIndicatorEvenWhenBarCountIsZero() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        NumericIndicator shifted = original.previous(0);
        Indicator<Num> delegate = shifted.delegate();
        assertTrue(delegate instanceof PreviousValueIndicator,
                "Delegate must be a PreviousValueIndicator even for barCount 0");
        // Verify the internal offset is indeed 0 via reflection (no public getter).
        PreviousValueIndicator pvi = (PreviousValueIndicator) delegate;
        try {
            Field offsetField = PreviousValueIndicator.class.getDeclaredField("barCount");
            offsetField.setAccessible(true);
            int offset = (int) offsetField.get(pvi);
            assertEquals(0, offset, "Internal offset should be 0");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Unable to inspect PreviousValueIndicator offset: " + e.getMessage());
        }
    }

    @Test
    void shouldAccumulateOffsetsWhenChained() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        // total offset 5
        NumericIndicator chained = original.previous(2).previous(3);
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

    @Test
    void shouldRejectIntegerMinValueBarCount() {
        NumericIndicator original = NumericIndicator.of(closePriceIndicator);
        assertThrows(IllegalArgumentException.class,
                () -> original.previous(Integer.MIN_VALUE),
                "Integer.MIN_VALUE should be considered invalid");
    }

    @Test
    void shouldThrowWhenDelegateIsNull() {
        // NumericIndicator.of(null) is expected to throw NPE/IllegalArgumentException.
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
            Constructor<NumericIndicator> ctor = NumericIndicator.class.getDeclaredConstructor(Indicator.class);
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