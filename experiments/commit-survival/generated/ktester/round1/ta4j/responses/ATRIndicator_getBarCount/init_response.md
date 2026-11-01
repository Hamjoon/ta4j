```java
package org.ta4j.core.indicators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;

/**
 * Tests for {@link ATRIndicator#getBarCount()}.
 *
 * The method simply delegates to the underlying {@link MMAIndicator}'s
 * {@code getBarCount()} which is the period supplied at construction time.
 * Therefore the test verifies that the value passed to the constructor is
 * returned unchanged.
 */
@ExtendWith(MockitoExtension.class)
class ATRIndicator_getBarCount_Test {

    private BarSeries series;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a minimal BarSeries – the actual bars are irrelevant for getBarCount()
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Add a few dummy bars just to have a non‑empty series (optional)
        series.addBar(new org.ta4j.core.mocks.MockBar(
                ZonedDateTime.now(), 0, 10, 12, 9, 0, 0, 0,
                org.ta4j.core.num.DecimalNum::valueOf));
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up per test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required
    }

    @Test
    void getBarCount_returnsBarCountProvidedInSeriesConstructor() {
        int expectedBarCount = 7;
        ATRIndicator atr = new ATRIndicator(series, expectedBarCount);
        assertEquals(expectedBarCount, atr.getBarCount(),
                "ATRIndicator#getBarCount() should return the barCount supplied to the constructor");
    }

    @Test
    void getBarCount_returnsBarCountProvidedInTRIndicatorConstructor() {
        int expectedBarCount = 13;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, expectedBarCount);
        assertEquals(expectedBarCount, atr.getBarCount(),
                "ATRIndicator#getBarCount() should return the barCount supplied when using the TRIndicator constructor");
    }
}
```