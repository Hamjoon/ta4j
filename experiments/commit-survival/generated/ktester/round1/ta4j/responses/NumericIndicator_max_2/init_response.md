```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#max(Number)} (the overload that receives a
 * {@link Number} argument).
 *
 * The test creates a {@link NumericIndicator} based on a {@link ClosePriceIndicator}
 * and verifies that the returned indicator correctly returns the element‑wise
 * maximum between the original values and the constant (or another indicator).
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_max_2_Test {

    /** Function used by {@link MockBarSeries} to create {@link Num} instances. */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /** Series used by all tests – the same data as in the original test suite. */
    private static BarSeries series;

    /** Close price indicator that wraps the series. */
    private static ClosePriceIndicator closePrice;

    /** EMA indicator used for the dynamic‑argument test. */
    private static EMAIndicator ema;

    /** Instance under test – created before each test method. */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // values are the same as in the original NumericIndicatorTest
        series = new MockBarSeries(
                NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9,
                8, 7, 6, 5, 4, 3, 2, 1,
                0, -1, -2
        );
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // create a fresh NumericIndicator for every test
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @AfterEach
    void teardownAfterEach() {
        // nothing to clean up for now
    }

    @AfterAll
    static void teardownAfterAll() {
        // nothing to clean up for now
    }

    @Test
    void maxWithConstantNumber() {
        // given
        Number constant = 5;

        // when
        NumericIndicator maxIndicator = numericIndicator.max(constant);

        // then
        Num constNum = NUM_FUNCTION.apply(constant);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).max(constNum);
            assertEquals(expected, maxIndicator.getValue(i),
                    "max at index " + i + " should be the larger of close price and constant 5");
        }
    }

    @Test
    void maxWithAnotherIndicator() {
        // when
        NumericIndicator maxIndicator = numericIndicator.max(ema);

        // then
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePrice.getValue(i).max(ema.getValue(i));
            assertEquals(expected, maxIndicator.getValue(i),
                    "max at index " + i + " should be the larger of close price and EMA");
        }
    }

    @Test
    void maxReturnsNumericIndicatorAndPreservesDelegateSeries() {
        // when
        NumericIndicator result = numericIndicator.max(10);

        // then
        assertNotNull(result);
        // the delegate of the resulting indicator must still be linked to the same series
        Indicator<Num> delegate = result.delegate();
        assertEquals(series, delegate.getBarSeries(),
                "The delegate of the max‑indicator should use the original BarSeries");
    }
}
```