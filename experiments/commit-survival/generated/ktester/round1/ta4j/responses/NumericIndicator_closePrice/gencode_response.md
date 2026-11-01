```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive tests for {@link NumericIndicator#closePrice(BarSeries)}.
 *
 * <p>Each test case described in the provided JSON is covered.
 * Parameterized tests are used where the logic is identical for several
 * series configurations.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_closePrice_Test {

    /** Series used for most tests – re‑initialized in each test method. */
    private BarSeries series;

    /* --------------------------------------------------------------------- */
    /* Helper methods to build various BarSeries configurations            */
    /* --------------------------------------------------------------------- */

    private static BarSeries validBarSeries() {
        // Two bars with close prices 100.5 and 101.2
        return new MockBarSeries(DecimalNum::valueOf, 100.5, 101.2);
    }

    private static BarSeries singleBarSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 55.0);
    }

    private static BarSeries emptyBarSeries() {
        return new MockBarSeries(DecimalNum::valueOf); // no values
    }

    private static BarSeries fiveBarSeries() {
        // 10, 20, 30.5, 40, 50
        return new MockBarSeries(DecimalNum::valueOf, 10.0, 20.0, 30.5, 40.0, 50.0);
    }

    private static BarSeries extremeValueSeries() {
        return new MockBarSeries(DecimalNum::valueOf,
                Double.MAX_VALUE,
                Double.MIN_VALUE,
                0.0,
                -12345.678);
    }

    private static BarSeries constantSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 42.42, 42.42, 42.42);
    }

    private static BarSeries nanCloseSeries() {
        // First bar normal, second bar NaN close price
        return new MockBarSeries(num -> {
            // MockBarSeries constructor with a lambda that creates Num from double
            // The library already treats NaN via DecimalNum.NaN
            return DecimalNum.valueOf(num);
        }, 10.0, Double.NaN);
    }

    private static BarSeries dynamicSeries() {
        // Start with two bars; later we will append a third bar via a mock.
        return new MockBarSeries(DecimalNum::valueOf, 10.0, 20.0);
    }

    private static BarSeries seriesWithNullBar() {
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(0)).thenReturn(null);
        return mockSeries;
    }

    private static BarSeries seriesWithBarHavingNullClose() {
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
        Bar mockBar = Mockito.mock(Bar.class);
        Mockito.when(mockBar.getClosePrice()).thenReturn(null);
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(0)).thenReturn(mockBar);
        return mockSeries;
    }

    private static BarSeries faultySeriesThrowsOnAccess() {
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(Mockito.anyInt()))
                .thenThrow(new RuntimeException("Faulty series access"));
        return mockSeries;
    }

    /* --------------------------------------------------------------------- */
    /* Test cases                                                            */
    /* --------------------------------------------------------------------- */

    @Test
    void closePriceCreatesClosePriceIndicator() {
        series = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40, 50);
        NumericIndicator numericIndicator = NumericIndicator.closePrice(series);

        assertNotNull(numericIndicator);
        assertEquals(ClosePriceIndicator.class,
                numericIndicator.delegate().getClass(),
                "Delegate should be a ClosePriceIndicator");

        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getBar(i).getClosePrice();
            Num actual = numericIndicator.getValue(i);
            assertEquals(0, expected.compareTo(actual),
                    "Value at index " + i + " must match the series close price");
        }

        assertSame(series, numericIndicator.getBarSeries(),
                "NumericIndicator must propagate the original BarSeries");
    }

    @Test
    void closePriceTypicalTwoBars() {
        series = validBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(100.5), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(101.2), ni.getValue(1));
    }

    @Test
    void closePriceSingleBar() {
        series = singleBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(55.0), ni.getValue(0));
    }

    @Test
    void closePriceEmptySeriesThrows() {
        series = emptyBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(IndexOutOfBoundsException.class, () -> ni.getValue(0));
    }

    @Test
    void closePriceNullSeriesThrows() {
        assertThrows(NullPointerException.class,
                () -> NumericIndicator.closePrice(null));
    }

    @Test
    void closePriceFiveBarSeriesValues() {
        series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(30.5), ni.getValue(2));
        assertEquals(DecimalNum.valueOf(50.0), ni.getValue(4));
    }

    @Test
    void closePriceExtremeValues() {
        series = extremeValueSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(Double.MAX_VALUE), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(Double.MIN_VALUE), ni.getValue(1));
        assertEquals(DecimalNum.valueOf(0.0), ni.getValue(2));
        assertEquals(DecimalNum.valueOf(-12345.678), ni.getValue(3));
    }

    @Test
    void closePriceConstantSeries() {
        series = constantSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.valueOf(42.42), ni.getValue(i));
        }
    }

    @Test
    void closePriceDynamicSeriesReflectsAppend() {
        // start with two bars
        BarSeries mutableSeries = dynamicSeries();
        NumericIndicator ni = NumericIndicator.closePrice(mutableSeries);

        // Append a third bar (close price 200.0) using a Mockito mock Bar
        Bar newBar = Mockito.mock(Bar.class);
        Mockito.when(newBar.getClosePrice()).thenReturn(DecimalNum.valueOf(200.0));
        // MockBarSeries implements BarSeries, so we can cast and add
        ((MockBarSeries) mutableSeries).addBar(newBar);

        assertEquals(DecimalNum.valueOf(200.0), ni.getValue(2),
                "NumericIndicator must reflect newly added bar");
    }

    @Test
    void closePriceDistinctInstances() {
        series = fiveBarSeries();
        NumericIndicator ni1 = NumericIndicator.closePrice(series);
        NumericIndicator ni2 = NumericIndicator.closePrice(series);
        assertNotSame(ni1, ni2, "Two calls must return distinct NumericIndicator objects");
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(ni1.getValue(i), ni2.getValue(i),
                    "Both instances should produce identical values at index " + i);
        }
    }

    @Test
    void closePricePropagatesSeriesAndUnstableBars() {
        series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertSame(series, ni.getBarSeries(),
                "NumericIndicator must return the original BarSeries");
        assertEquals(0, ni.getUnstableBars(),
                "ClosePriceIndicator is stable – unstable bars should be 0");
    }

    @Test
    void closePriceNaNCloseSeries() {
        series = nanCloseSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        // first value normal
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        // second value should be NaN
        assertTrue(ni.getValue(1).isNaN(),
                "Close price NaN must be propagated as Num.NaN");
    }

    @Test
    void closePriceSeriesWithNullBarThrowsOnAccess() {
        series = seriesWithNullBar();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
    }

    @Test
    void closePriceBarWithNullCloseThrowsOnAccess() {
        series = seriesWithBarHavingNullClose();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
    }

    @Test
    void closePriceFaultySeriesPropagatesException() {
        series = faultySeriesThrowsOnAccess();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ni.getValue(0));
        assertEquals("Faulty series access", ex.getMessage());
    }
}
```