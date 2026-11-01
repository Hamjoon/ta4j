```java
package org.ta4j.core.indicators;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.TRIndicator;

/**
 * Tests for {@link ATRIndicator#getUnstableBars()}.
 *
 * The method should simply return the bar count supplied at construction time.
 */
@ExtendWith(MockitoExtension.class)
class ATRIndicator_getUnstableBars_Test {

    /** Function used by the library to create {@link Num} instances. */
    private static Function<Number, Num> numFunction;

    /** A minimal series used for constructing the indicator. */
    private BarSeries series;

    @BeforeAll
    static void setupBeforeAll() {
        // Use DecimalNum (the default numeric type in ta4j) for the tests
        numFunction = n -> DecimalNum.valueOf(n);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple BarSeries with a single dummy bar – the actual values are irrelevant
        series = new BaseBarSeriesBuilder()
                .withNumTypeOf(numFunction)
                .build();

        series.addBar(new MockBar(
                ZonedDateTime.now(),
                0,                 // open price (unused)
                10,                // high price
                12,                // low price
                8,                 // close price
                0, 0, 0,           // volume, amount, etc. (unused)
                numFunction));
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to clean up for this simple test
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing to clean up at the class level
    }

    @Test
    void testGetUnstableBars_returnsBarCount() {
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals(barCount, atr.getUnstableBars(),
                "getUnstableBars() should return the bar count supplied to the constructor");
    }

    @Test
    void testGetUnstableBars_multipleBarCounts() {
        int[] barCounts = {1, 3, 10, 15};
        for (int barCount : barCounts) {
            ATRIndicator atr = new ATRIndicator(series, barCount);
            assertEquals(barCount, atr.getUnstableBars(),
                    "Unexpected unstable bar count for barCount = " + barCount);
        }
    }

    @Test
    void testGetUnstableBars_usingTRIndicatorConstructor() {
        int barCount = 7;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        assertEquals(barCount, atr.getUnstableBars(),
                "getUnstableBars() should work with the TRIndicator‑based constructor");
    }
}
```