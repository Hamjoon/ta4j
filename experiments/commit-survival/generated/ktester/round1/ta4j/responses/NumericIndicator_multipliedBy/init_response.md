```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#multipliedBy(Indicator)}.
 *
 * The test class follows the JUnit 5 + Mockito template provided.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_multipliedBy_Test {

    /** Shared series used by all tests. */
    private static BarSeries series;

    /** Indicator that returns the close price of the series. */
    private ClosePriceIndicator closePrice;

    /** EMA indicator used as a realistic dynamic operand. */
    private EMAIndicator ema;

    /** The NumericIndicator under test (wrapping {@code closePrice}). */
    private NumericIndicator numericIndicator;

    /**
     * Initialise the shared {@link BarSeries} once for the whole test suite.
     * The series contains a deterministic set of values so that the expected
     * results are easy to compute.
     */
    @BeforeAll
    static void setupBeforeAll() {
        // The MockBarSeries constructor expects a Function<Number,Num>
        // and a var‑args list of numbers (the close values).
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8,
                7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
    }

    /**
     * Set up fresh instances before each test method.
     */
    @BeforeEach
    void setupBeforeEach() {
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3); // a realistic dynamic operand
        numericIndicator = NumericIndicator.of(closePrice);
    }

    /**
     * Verify that {@code multipliedBy(Indicator)} correctly multiplies the
     * underlying values when the operand is a real indicator (EMA in this case).
     */
    @Test
    void multipliedBy_withRealIndicator() {
        NumericIndicator result = numericIndicator.multipliedBy(ema);

        // Spot‑check a few indices – the first, a middle one and the last.
        int[] indices = {0, 8, series.getBarCount() - 1};

        for (int i : indices) {
            Num expected = closePrice.getValue(i).multipliedBy(ema.getValue(i));
            Num actual = result.getValue(i);
            assertEquals(expected, actual,
                    "multipliedBy(EMA) produced wrong value at index " + i);
        }
    }

    /**
     * Verify that {@code multipliedBy(Indicator)} works with a mocked
     * {@link Indicator}. The mock returns a constant value so that the expected
     * result is easy to calculate.
     */
    @Test
    void multipliedBy_withMockedIndicator() {
        // Create a mock Indicator that always returns the constant value 5.
        @SuppressWarnings("unchecked")
        Indicator<Num> mockIndicator = mock(Indicator.class);
        Num constantFive = DecimalNum.valueOf(5);
        // Stub getValue for any index.
        when(mockIndicator.getValue(Mockito.anyInt())).thenReturn(constantFive);
        // The mock must also return the same BarSeries as the numericIndicator,
        // otherwise NumericIndicator may delegate to a different series.
        when(mockIndicator.getBarSeries()).thenReturn(series);

        NumericIndicator result = numericIndicator.multipliedBy(mockIndicator);

        // Verify a few indices – the multiplication should be value * 5.
        for (int i = 0; i < 5; i++) { // a small sample is enough
            Num expected = closePrice.getValue(i).multipliedBy(constantFive);
            Num actual = result.getValue(i);
            assertEquals(expected, actual,
                    "multipliedBy(mock) produced wrong value at index " + i);
        }

        // Ensure the mock was queried for the expected indices.
        verify(mockIndicator, atLeastOnce()).getValue(Mockito.anyInt());
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to release for this test suite.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Explicitly nullify the static reference to help GC in long test runs.
        series = null;
    }
}
```