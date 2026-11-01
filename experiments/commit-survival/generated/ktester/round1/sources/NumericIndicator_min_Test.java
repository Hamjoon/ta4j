package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import java.util.stream.Stream;
// import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
// import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
// import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.MethodSource;
// import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
// import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
// import static org.mockito.Mockito.*;
// import static org.ta4j.core.TestUtils.assertNumEquals;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.ta4j.core.TestUtils.assertNumEquals;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeAll;
import org.ta4j.core.BaseBar;
// import org.junit.jupiter.api.extension.ExtendWith;
import org.ta4j.core.BaseBarSeries;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.ParameterizedTest;
import static org.mockito.Mockito.*;

// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.ta4j.core.mocks.MockBarSeries;
/**
 * Tests for {@link NumericIndicator#min(Indicator)} and related overloads.
 */
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
        // Build a short series with deterministic close‑price values (0‑9).
        series = new BaseBarSeries();
        for (int i = 0; i < 10; i++) {
            Num price = numFunction.apply(i);
            // Only the close price matters for the tests; other fields are dummy.
            BaseBar bar = BaseBar.builder().timePeriod(Duration.ofDays(1)).closePrice(price).openPrice(price).highPrice(price).lowPrice(price).volume(DecimalNum.ONE).build();
            series.addBar(bar);
        }
        closePrice = new ClosePriceIndicator(series);
    }

    /* Helper for parameterised tests */
    private static Stream<Arguments> provideConstantIndicatorScenarios() {
        // Helper to fetch original close‑price value.
        Function<Integer, Num> original = i -> closePrice.getValue(i);
        return Stream.of(// constant = 10 (higher than any close price 0‑9) → expect original values
        Arguments.of(10, (Function<Integer, Num>) i -> original.apply(i), "Constant higher than all values (no change)"), // constant = -5 (lower than any close price) → expect constant everywhere
        Arguments.of(-5, (Function<Integer, Num>) i -> DecimalNum.valueOf(-5), "Constant lower than all values (always constant)"), // constant = 5 (some values lower, some higher) → mixed min
        Arguments.of(5, (Function<Integer, Num>) i -> original.apply(i).min(DecimalNum.valueOf(5)), "Constant in the middle (mixed min)"));
    }

    /**
     * Verify that {@code min(Number)} (which internally creates a {@link ConstantIndicator})
     * returns the element‑wise minimum between the wrapped indicator and the constant.
     */
    @Test
    void min_withConstantIndicator_returnsCorrectMinimum() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Number constant = 5;
        NumericIndicator result = numeric.min(constant);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).min(numFunction.apply(constant));
            assertEquals(expected, result.getValue(i), "min at index " + i + " did not match expected value");
        }
    }

    /**
     * Verify that {@code min(Indicator)} returns the element‑wise minimum between
     * the two indicators.
     */
    @Test
    void min_withIndicator_returnsCorrectMinimum(@Mock Indicator<Num> mocked) {
        // Mocked indicator that returns a decreasing sequence: 9‑i
        for (int i = 0; i < series.getBarCount(); i++) {
            when(mocked.getValue(i)).thenReturn(numFunction.apply(9 - i));
        }
        when(mocked.getBarSeries()).thenReturn(series);
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        NumericIndicator result = numeric.min(mocked);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num left = closePrice.getValue(i);
            Num right = mocked.getValue(i);
            Num expected = left.min(right);
            assertEquals(expected, result.getValue(i), "min at index " + i + " did not match expected value");
        }
        // Verify that the mocked indicator was queried for each index.
        verify(mocked, times(series.getBarCount())).getValue(anyInt());
    }

    /**
     * Parameterised test covering constant indicators that are
     * higher, lower, or mixed with respect to the original values.
     */
    @ParameterizedTest(name = "{index} => constant={0}, description={2}")
    @MethodSource("provideConstantIndicatorScenarios")
    void min_withConstantIndicator_basicFunctionality(Number constant, Function<Integer, Num> expectedProvider, String description) {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        ConstantIndicator<Num> other = new ConstantIndicator<>(series, numFunction.apply(constant));
        NumericIndicator result = numeric.min(other);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = expectedProvider.apply(i);
            assertEquals(expected, result.getValue(i), "Failed at index " + i + " for scenario: " + description);
        }
    }

    /**
     * Verify that passing the same indicator instance results in an indicator
     * that yields the same values as the original (min(x,x) = x).
     */
    @Test
    void min_withSameInstance_returnsIdenticalValues() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        NumericIndicator result = numeric.min(closePrice);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), result.getValue(i), "Value mismatch at index " + i + " when min is called with the same indicator");
        }
    }

    /**
     * Verify that passing {@code null} as the other indicator results in a
     * {@link NullPointerException}.
     */
    @Test
    void min_nullIndicator_throwsNullPointerException() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        assertThrows(NullPointerException.class, () -> numeric.min((Indicator<Num>) null), "Expected NullPointerException when other indicator is null");
    }

    /**
     * Verify that using an indicator based on a different {@link BarSeries}
     * throws {@link IllegalArgumentException}.
     */
    @Test
    void min_indicatorFromDifferentSeries_throwsIllegalArgumentException() {
        // Build a second series with different values.
        BarSeries otherSeries = new BaseBarSeries();
        for (int i = 100; i < 110; i++) {
            Num price = numFunction.apply(i);
            BaseBar bar = BaseBar.builder().timePeriod(Duration.ofDays(1)).closePrice(price).openPrice(price).highPrice(price).lowPrice(price).volume(DecimalNum.ONE).build();
            otherSeries.addBar(bar);
        }
        ClosePriceIndicator otherIndicator = new ClosePriceIndicator(otherSeries);
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        assertThrows(IllegalArgumentException.class, () -> numeric.min(otherIndicator), "Expected IllegalArgumentException for mismatching BarSeries");
    }

    /**
     * Verify that if the supplied indicator's series is shorter than the caller's,
     * an {@link IndexOutOfBoundsException} is thrown when accessing an index that
     * does not exist in the shorter series.
     */
    @Test
    void min_indicatorWithShorterSeries_throwsIndexOutOfBoundsException() {
        // Shorter series (only 5 bars).
        BarSeries shortSeries = new BaseBarSeries();
        for (int i = 0; i < 5; i++) {
            Num price = numFunction.apply(i);
            BaseBar bar = BaseBar.builder().timePeriod(Duration.ofDays(1)).closePrice(price).openPrice(price).highPrice(price).lowPrice(price).volume(DecimalNum.ONE).build();
            shortSeries.addBar(bar);
        }
        ClosePriceIndicator shortIndicator = new ClosePriceIndicator(shortSeries);
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        NumericIndicator result = numeric.min(shortIndicator);
        // 5
        int outOfBoundsIndex = shortSeries.getBarCount();
        assertThrows(IndexOutOfBoundsException.class, () -> result.getValue(outOfBoundsIndex), "Expected IndexOutOfBoundsException when accessing beyond the shorter series length");
    }

    /**
     * Additional scenario where the original series contains both positive and
     * negative values. This demonstrates the “mixed values” case.
     */
    @Test
    void min_withConstantZero_mixedPositiveNegativeValues() {
        // Series: -3, -1, 0, 2, 4
        BarSeries mixedSeries = new BaseBarSeries();
        int[] raw = new int[] { -3, -1, 0, 2, 4 };
        for (int v : raw) {
            Num price = numFunction.apply(v);
            BaseBar bar = BaseBar.builder().timePeriod(Duration.ofDays(1)).closePrice(price).openPrice(price).highPrice(price).lowPrice(price).volume(DecimalNum.ONE).build();
            mixedSeries.addBar(bar);
        }
        ClosePriceIndicator mixedClose = new ClosePriceIndicator(mixedSeries);
        NumericIndicator numeric = NumericIndicator.of(mixedClose);
        ConstantIndicator<Num> zeroConst = new ConstantIndicator<>(mixedSeries, DecimalNum.ZERO);
        NumericIndicator result = numeric.min(zeroConst);
        for (int i = 0; i < mixedSeries.getBarCount(); i++) {
            Num expected = mixedClose.getValue(i).min(DecimalNum.ZERO);
            assertEquals(expected, result.getValue(i), "Incorrect min at index " + i + " for mixed positive/negative series");
        }
    }
}
