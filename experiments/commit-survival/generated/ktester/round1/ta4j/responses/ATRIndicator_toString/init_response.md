```java
package org.ta4j.core.indicators;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

/**
 * Tests for {@link ATRIndicator#toString()}.
 */
@ExtendWith(MockitoExtension.class)
class ATRIndicator_toString_Test {

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialization required
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    /**
     * Verify that {@code toString()} returns the class name together with the bar count
     * when the indicator is created via the {@code (BarSeries, int)} constructor.
     */
    @Test
    void testToStringWithBarCountConstructor() {
        // Build a minimal BarSeries (the actual values are irrelevant for toString)
        BarSeries series = new BaseBarSeriesBuilder().build();
        series.addBar(new MockBar(ZonedDateTime.now(), Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN));
        series.addBar(new MockBar(ZonedDateTime.now().plusSeconds(1), Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN));

        int barCount = 7;
        ATRIndicator indicator = new ATRIndicator(series, barCount);

        String expected = "ATRIndicator barCount: " + barCount;
        assertEquals(expected, indicator.toString(),
                "toString() should include the simple class name and the configured bar count");
    }

    /**
     * Verify that {@code toString()} works correctly when the indicator is created
     * via the {@code (TRIndicator, int)} constructor.
     */
    @Test
    void testToStringWithTRIndicatorConstructor() {
        BarSeries series = new BaseBarSeriesBuilder().build();
        series.addBar(new MockBar(ZonedDateTime.now(), Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN, Num.NaN));

        TRIndicator tr = new TRIndicator(series);
        int barCount = 12;
        ATRIndicator indicator = new ATRIndicator(tr, barCount);

        String expected = "ATRIndicator barCount: " + barCount;
        assertEquals(expected, indicator.toString(),
                "toString() should reflect the bar count regardless of which constructor is used");
    }
}
```