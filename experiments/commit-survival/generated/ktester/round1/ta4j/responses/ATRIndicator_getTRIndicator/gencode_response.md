```java
package org.ta4j.core.indicators;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link ATRIndicator#getTRIndicator()}.
 *
 * <p>These tests verify that the {@code getTRIndicator()} method returns the
 * correct {@link TRIndicator} instance for various construction scenarios,
 * series sizes and repeated invocations.</p>
 */
class ATRIndicator_getTRIndicator_Test {

    private static Function<Number, Num> numFunction;
    private BarSeries series;

    @BeforeAll
    static void setupBeforeAll() {
        // Use DecimalNum for numeric calculations throughout the tests
        numFunction = DecimalNum::valueOf;
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a tiny default series – individual tests may replace it with a
        // series of a different length using {@link #createSeries(int)}.
        series = new BaseBarSeriesBuilder()
                .withNumTypeOf(numFunction)
                .build();

        // Populate with three dummy bars (values are irrelevant for the getter).
        series.addBar(new MockBar(
                ZonedDateTime.now().minusSeconds(5), 0,
                12, 15, 8, 0, 0, 0, numFunction));
        series.addBar(new MockBar(
                ZonedDateTime.now().minusSeconds(4), 0,
                8, 11, 6, 0, 0, 0, numFunction));
        series.addBar(new MockBar(
                ZonedDateTime.now().minusSeconds(3), 0,
                15, 17, 14, 0, 0, 0, numFunction));
    }

    @AfterEach
    void teardownAfterEach() {
        // No external resources to clean up.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No static resources to clean up.
    }

    /* --------------------------------------------------------------------- *
     *  Existing straightforward tests (kept unchanged)                     *
     * --------------------------------------------------------------------- */

    @Test
    void testGetTRIndicator_WithBarSeriesConstructor() {
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(series, barCount);

        TRIndicator tr = atr.getTRIndicator();

        assertNotNull(tr, "TRIndicator should not be null");
        assertEquals(series, tr.getBarSeries(),
                "TRIndicator should be built on the same BarSeries passed to ATRIndicator");
    }

    @Test
    void testGetTRIndicator_WithTRConstructor() {
        int barCount = 3;
        TRIndicator providedTR = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(providedTR, barCount);

        TRIndicator tr = atr.getTRIndicator();

        assertSame(providedTR, tr,
                "ATRIndicator should return the exact TRIndicator instance supplied in the constructor");
        assertEquals(series, tr.getBarSeries(),
                "The provided TRIndicator should be associated with the original BarSeries");
    }

    /* --------------------------------------------------------------------- *
     *  Additional tests derived from the JSON specification                *
     * --------------------------------------------------------------------- */

    /**
     * Verify that multiple calls to {@code getTRIndicator()} on the same
     * {@link ATRIndicator} return the *same* {@link TRIndicator} instance.
     */
    @Test
    void testGetTRIndicator_MultipleCallsReturnSameInstance() {
        BarSeries bigSeries = createSeries(5);
        int barCount = 3;
        ATRIndicator atr = new ATRIndicator(bigSeries, barCount);

        TRIndicator first = atr.getTRIndicator();
        TRIndicator second = atr.getTRIndicator();

        assertNotNull(first, "First call should return a non‑null TRIndicator");
        assertSame(first, second, "Subsequent calls must return the identical TRIndicator object");
        assertEquals(bigSeries, first.getBarSeries(),
                "The TRIndicator must be bound to the original BarSeries");
    }

    /**
     * Ensure that constructing {@link ATRIndicator} with a {@code null}
     * {@link BarSeries} fails early (the constructor should throw an exception
     * before {@code getTRIndicator()} can be invoked).
     */
    @Test
    void testConstructor_NullBarSeriesThrowsException() {
        assertThrows(NullPointerException.class,
                () -> new ATRIndicator((BarSeries) null, 1),
                "Passing a null BarSeries to the constructor must throw NullPointerException");
    }

    /**
     * Validate that a minimal series (a single bar) still yields a functional
     * {@link TRIndicator}.
     */
    @Test
    void testGetTRIndicator_SingleBarSeries() {
        BarSeries singleBarSeries = createSeries(1);
        int barCount = 1;
        ATRIndicator atr = new ATRIndicator(singleBarSeries, barCount);

        TRIndicator tr = atr.getTRIndicator();

        assertNotNull(tr, "TRIndicator should not be null even for a single‑bar series");
        assertEquals(singleBarSeries, tr.getBarSeries(),
                "TRIndicator must reference the original single‑bar series");
    }

    /**
     * Confirm that a large series (100 bars) does not cause the ATRIndicator to
     * recreate the internal {@link TRIndicator}; the same instance should be
     * returned each time.
     */
    @Test
    void testGetTRIndicator_LargeSeriesSameInstance() {
        BarSeries largeSeries = createSeries(100);
        int barCount = 50;
        ATRIndicator atr = new ATRIndicator(largeSeries, barCount);

        TRIndicator first = atr.getTRIndicator();
        TRIndicator second = atr.getTRIndicator();

        assertNotNull(first, "TRIndicator should be instantiated for a large series");
        assertSame(first, second, "The same TRIndicator instance must be reused for large series");
        assertEquals(largeSeries, first.getBarSeries(),
                "TRIndicator must be linked to the original large BarSeries");
    }

    /* --------------------------------------------------------------------- *
     *  Helper utilities                                                    *
     * --------------------------------------------------------------------- */

    /**
     * Creates a {@link BarSeries} containing {@code size} dummy bars.
     * Prices are generated deterministically to keep the test repeatable.
     *
     * @param size number of bars to generate
     * @return a populated {@link BarSeries}
     */
    private static BarSeries createSeries(int size) {
        BarSeries s = new BaseBarSeriesBuilder()
                .withNumTypeOf(numFunction)
                .build();

        Random rnd = new Random(0); // fixed seed for reproducibility
        for (int i = 0; i < size; i++) {
            double open = 10 + rnd.nextDouble() * 5;
            double high = open + rnd.nextDouble() * 2;
            double low = open - rnd.nextDouble() * 2;
            double close = low + rnd.nextDouble() * (high - low);
            s.addBar(new MockBar(
                    ZonedDateTime.now().plusSeconds(i), 0,
                    open, high, low, close, 0, 0, numFunction));
        }
        return s;
    }
}
```