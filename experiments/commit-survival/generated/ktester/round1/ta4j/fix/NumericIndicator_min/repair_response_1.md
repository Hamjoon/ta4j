```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_min_Test {

    /**
     * Series used for the “real” indicator (close price).
     */
    private static BarSeries series;

    /**
     * Close price indicator that will act as the left operand of {@code min}.
     */
    private static ClosePriceIndicator closePrice;

    /**
     * Function converting {@link Number} to {@link Num}.
     */
    private static Function<Number, Num> numFunction;

    @BeforeAll
    static void setupBeforeAll() {
        // Simple DecimalNum based conversion – sufficient for the tests.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // A short series with deterministic values (0‑9).
        series = new MockBarSeries(numFunction, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        closePrice = new ClosePriceIndicator(series);
    }

    private static Stream<Arguments> provideConstantIndicatorScenarios() {
        // Helper to fetch original close‑price value.
        Function<Integer, Num> original = i -> closePrice.getValue(i);
        return Stream.of(
                // constant = 10 (higher than any close price 0‑9) → expect original values
                Arguments.of(10, (Function<Integer, Num>) i -> original.apply(i),
                        "Constant higher than all values (no change)"),
                // constant = -5 (lower than any close price) → expect constant everywhere
                Arguments.of(-5, (Function<Integer, Num>) i -> DecimalNum.valueOf(-5),
                        "Constant lower than all values (always constant)"),
                // constant = 5 (some values lower, some higher) → mixed min
                Arguments.of(5, (Function<Integer, Num>) i -> original.apply(i).min(DecimalNum.valueOf(5)),
                        "Constant in the middle (mixed min)")
        );
    }

    /**
     * Verify that {@code min(Number)} (which internally creates a {@link ConstantIndicator})
     * returns the element‑wise minimum between the wrapped indicator and the constant.
     */
    @Test
    void min_withConstantIndicator_returnsCorrectMinimum() {
        // NumericIndicator wrapping the close‑price indicator.
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // Use a constant that is lower than some values and higher than others.
        Number constant = 5;
        NumericIndicator result = numeric.min(constant);
        // Expected value is the minimum between close price and 5.
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).min(numFunction.apply(constant));
            assertNumEquals(expected, result.getValue(i),
                    "min at index " + i + " did not match expected value");
        }
    }

    /**
     * Verify that {@code min(Indicator)} returns the element‑wise minimum between
     * the two indicators.
     *
     * The second indicator is mocked to return a predictable sequence of numbers.
     */
    @Test
    void min_withIndicator_returnsCorrectMinimum() {
        // Mocked indicator that returns a decreasing sequence: 9‑i
        @SuppressWarnings("unchecked")
        Indicator<Num> mocked = mock(Indicator.class);
        for (int i = 0; i < series.getBarCount(); i++) {
            when(mocked.getValue(i)).thenReturn(numFunction.apply(9 - i));
        }
        // The mocked indicator must also return the same BarSeries (required by many
        // internal operations). We simply forward the call.
        when(mocked.getBarSeries()).thenReturn(series);
        // NumericIndicator wrapping the close‑price indicator (0‑9).
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // Apply min with the mocked indicator.
        NumericIndicator result = numeric.min(mocked);
        // Expected value is the element‑wise minimum of the two sequences.
        for (int i = 0; i < series.getBarCount(); i++) {
            // 0,1,2,...
            Num left = closePrice.getValue(i);
            // 9,8,7,...
            Num right = mocked.getValue(i);
            Num expected = left.min(right);
            assertNumEquals(expected, result.getValue(i),
                    "min at index " + i + " did not match expected value");
        }
        // Verify that the mocked indicator was queried for each index.
        verify(mocked, times(series.getBarCount())).getValue(anyInt());
    }

    /**
     * Parameterised test covering constant indicators that are
     * <ul>
     *   <li>higher than all original values (expect original values unchanged)</li>
     *   <li>lower than all original values (expect constant value everywhere)</li>
     *   <li>equal to some original values (expect original values unchanged)</li>
     *   <li>mixed (positive & negative) – exercised with a custom series.</li>
     * </ul>
     */
    @ParameterizedTest(name = "{index} => constant={0}, description={2}")
    @MethodSource("provideConstantIndicatorScenarios")
    void min_withConstantIndicator_basicFunctionality(Number constant,
                                                       Function<Integer, Num> expectedProvider,
                                                       String description) {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        ConstantIndicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(constant));
        NumericIndicator result = numeric.min(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = expectedProvider.apply(i);
            assertNumEquals(expected, result.getValue(i),
                    "Failed at index " + i + " for scenario: " + description);
        }
    }

    /**
     * Verify that passing the same indicator instance results in an indicator
     * that yields the same values as the original (min(x,x) = x).
     */
    @Test
    void min_withSameInstance_returnsIdenticalValues() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // same underlying indicator
        NumericIndicator result = numeric.min(closePrice);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(closePrice.getValue(i), result.getValue(i),
                    "Value mismatch at index " + i + " when min is called with the same indicator");
        }
    }

    /**
     * Verify that passing {@code null} as the other indicator results in a
     * {@link NullPointerException}.
     */
    @Test
    void min_nullIndicator_throwsNullPointerException() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        assertThrows(NullPointerException.class,
                () -> numeric.min((Indicator<Num>) null),
                "Expected NullPointerException when other indicator is null");
    }

    /**
     * Verify that using an indicator based on a different {@link BarSeries}
     * throws {@link IllegalArgumentException}.
     */
    @Test
    void min_indicatorFromDifferentSeries_throwsIllegalArgumentException() {
        // Build a second series with different values.
        BarSeries otherSeries = new MockBarSeries(numFunction, 100, 101, 102, 103, 104,
                105, 106, 107, 108, 109);
        ClosePriceIndicator otherIndicator = new ClosePriceIndicator(otherSeries);
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // The BinaryOperation.min factory validates series compatibility.
        assertThrows(IllegalArgumentException.class,
                () -> numeric.min(otherIndicator),
                "Expected IllegalArgumentException for mismatching BarSeries");
    }

    /**
     * Verify that if the supplied indicator's series is shorter than the caller's,
     * an {@link IndexOutOfBoundsException} is thrown when accessing an index that
     * does not exist in the shorter series.
     */
    @Test
    void min_indicatorWithShorterSeries_throwsIndexOutOfBoundsException() {
        // Shorter series (only 5 bars).
        BarSeries shortSeries = new MockBarSeries(numFunction, 0, 1, 2, 3, 4);
        ClosePriceIndicator shortIndicator = new ClosePriceIndicator(shortSeries);
        // 10‑bar series
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        NumericIndicator result = numeric.min(shortIndicator);
        // Access an index that exists only in the longer series.
        // 5, valid for closePrice but not for shortIndicator
        int outOfBoundsIndex = shortSeries.getBarCount();
        assertThrows(IndexOutOfBoundsException.class,
                () -> result.getValue(outOfBoundsIndex),
                "Expected IndexOutOfBoundsException when accessing beyond the shorter series length");
    }

    /**
     * Additional scenario where the original series contains both positive and
     * negative values. This demonstrates the “mixed values” case described in
     * the JSON test description.
     */
    @Test
    void min_withConstantZero_mixedPositiveNegativeValues() {
        // Series: -3, -1, 0, 2, 4
        BarSeries mixedSeries = new MockBarSeries(numFunction, -3, -1, 0, 2, 4);
        ClosePriceIndicator mixedClose = new ClosePriceIndicator(mixedSeries);
        NumericIndicator numeric = NumericIndicator.of(mixedClose);
        ConstantIndicator<Num> zeroConst = new ConstantIndicator<>(mixedSeries, DecimalNum.valueOf(0));
        NumericIndicator result = numeric.min(zeroConst);
        for (int i = 0; i < mixedSeries.getBarCount(); i++) {
            Num expected = mixedClose.getValue(i).min(DecimalNum.ZERO);
            assertNumEquals(expected, result.getValue(i),
                    "Incorrect min at index " + i + " for mixed positive/negative series");
        }
    }
}
```