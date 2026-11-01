```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_multipliedBy_2_Test {

    // -----------------------------------------------------------------------
    // Shared test fixtures
    // -----------------------------------------------------------------------
    private static BarSeries series;
    private static ClosePriceIndicator closePriceIndicator;
    private static EMAIndicator emaIndicator;
    private static Function<Number, Num> numFunction = DecimalNum::valueOf;

    @BeforeAll
    static void setupBeforeAll() {
        // Initialise a mock BarSeries with deterministic numeric values
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        emaIndicator = new EMAIndicator(closePriceIndicator, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialisation required at the moment
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required at the moment
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static resources if needed (none for this test)
    }

    // -----------------------------------------------------------------------
    // Tests for NumericIndicator.multipliedBy(Number)
    // -----------------------------------------------------------------------
    @Test
    void multipliedByNumber_returnsCorrectStaticOperation() {
        // GIVEN
        NumericIndicator numericIndicator = NumericIndicator.of(closePriceIndicator);

        // WHEN
        NumericIndicator multiplied = numericIndicator.multipliedBy(5);

        // THEN
        assertEquals(numFunction.apply(1 * 5), multiplied.getValue(0));
        assertEquals(numFunction.apply(9 * 5), multiplied.getValue(8));
    }

    @Test
    void multipliedByIndicator_returnsCorrectDynamicOperation() {
        // GIVEN
        NumericIndicator numericIndicator = NumericIndicator.of(closePriceIndicator);

        // WHEN
        NumericIndicator multiplied = numericIndicator.multipliedBy(emaIndicator);

        // THEN
        Num expected0 = closePriceIndicator.getValue(0).multipliedBy(emaIndicator.getValue(0));
        Num expected8 = closePriceIndicator.getValue(8).multipliedBy(emaIndicator.getValue(8));

        assertEquals(expected0, multiplied.getValue(0));
        assertEquals(expected8, multiplied.getValue(8));
    }
}
```