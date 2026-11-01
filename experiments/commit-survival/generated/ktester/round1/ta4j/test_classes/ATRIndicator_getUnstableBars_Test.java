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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.Spy;
import java.lang.reflect.Field;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ATRIndicator_getUnstableBars_Test {

    /**
     * Function used by the library to create {@link Num} instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * A minimal series used for constructing the indicator.
     */
    private BarSeries series;

    private static final class ArgumentsHolder {

        final int initialBarCount;

        final int expected;

        final boolean expectException;

        ArgumentsHolder(int initialBarCount, int expected) {
            this(initialBarCount, expected, false);
        }

        ArgumentsHolder(int initialBarCount, int expected, boolean expectException) {
            this.initialBarCount = initialBarCount;
            this.expected = expected;
            this.expectException = expectException;
        }

        @Override
        public String toString() {
            return "initialBarCount=" + initialBarCount + ", expected=" + expected + (expectException ? ", expectException=true" : "");
        }
    }

    @BeforeAll
    static void setupBeforeAll() {
        // Use DecimalNum (the default numeric type in ta4j) for the tests
        numFunction = n -> DecimalNum.valueOf(n);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple BarSeries with a single dummy bar – the actual values are irrelevant
        series = new BaseBarSeriesBuilder().withNumTypeOf(numFunction).build();
        series.addBar(new MockBar(ZonedDateTime.now(), // open price (unused)
        0, // high price
        10, // low price
        12, // close price
        8, // volume, amount, etc. (unused)
        0, // volume, amount, etc. (unused)
        0, // volume, amount, etc. (unused)
        0, numFunction));
    }

    private static Stream<ArgumentsHolder> initialBarCountProvider() {
        return Stream.of(new ArgumentsHolder(0, 0), new ArgumentsHolder(5, 5), new ArgumentsHolder(42, 42), new ArgumentsHolder(1000, 1000), // The negative case is expected to throw an exception, handled separately
        new ArgumentsHolder(-1, -1, true));
    }

    @Test
    void testGetUnstableBars_returnsBarCount() {
        int barCount = 5;
        ATRIndicator atr = new ATRIndicator(series, barCount);
        assertEquals(barCount, atr.getUnstableBars(), "getUnstableBars() should return the bar count supplied to the constructor");
    }

    @Test
    void testGetUnstableBars_multipleBarCounts() {
        int[] barCounts = { 1, 3, 10, 15 };
        for (int barCount : barCounts) {
            ATRIndicator atr = new ATRIndicator(series, barCount);
            assertEquals(barCount, atr.getUnstableBars(), "Unexpected unstable bar count for barCount = " + barCount);
        }
    }

    @Test
    void testGetUnstableBars_usingTRIndicatorConstructor() {
        int barCount = 7;
        TRIndicator tr = new TRIndicator(series);
        ATRIndicator atr = new ATRIndicator(tr, barCount);
        assertEquals(barCount, atr.getUnstableBars(), "getUnstableBars() should work with the TRIndicator‑based constructor");
    }

    @ParameterizedTest(name = "initialBarCount={0} → expected={1}")
    @MethodSource("initialBarCountProvider")
    void testGetUnstableBars_initialBarCounts(ArgumentsHolder holder) {
        // Populate the series with the required number of dummy bars
        for (int i = 0; i < holder.initialBarCount; i++) {
            series.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        // Construct the indicator with the (possibly empty) series
        ATRIndicator atr = new ATRIndicator(series, holder.initialBarCount);
        if (holder.expectException) {
            // The library is expected to guard against a negative bar count.
            assertThrows(IllegalArgumentException.class, atr::getUnstableBars, "Negative bar count should cause IllegalArgumentException");
        } else {
            assertEquals(holder.expected, atr.getUnstableBars(), "Unexpected unstable bar count for initialBarCount = " + holder.initialBarCount);
        }
    }

    @Test
    void testGetUnstableBars_dynamicSeriesGrowth() {
        // start with 3 bars
        for (int i = 0; i < 3; i++) {
            series.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        ATRIndicator atr = new ATRIndicator(series, 3);
        assertEquals(3, atr.getUnstableBars(), "Initial unstable bars should be 3");
        // add 2 more bars
        for (int i = 0; i < 2; i++) {
            series.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        assertEquals(5, atr.getUnstableBars(), "After adding 2 bars, unstable bars should reflect total count (5)");
    }

    @Test
    void testGetUnstableBars_dynamicSeriesGrowth_fromEmpty() {
        // start empty
        ATRIndicator atr = new ATRIndicator(series, 0);
        assertEquals(0, atr.getUnstableBars(), "Initially empty series should report 0");
        // add 7 bars
        for (int i = 0; i < 7; i++) {
            series.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        assertEquals(7, atr.getUnstableBars(), "After adding 7 bars, unstable bars should be 7");
    }

    @Test
    void testGetUnstableBars_seriesReplacement() throws Exception {
        // Build first series with 8 bars
        BarSeries firstSeries = new BaseBarSeriesBuilder().withNumTypeOf(numFunction).build();
        for (int i = 0; i < 8; i++) {
            firstSeries.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        ATRIndicator atr = new ATRIndicator(firstSeries, 8);
        assertEquals(8, atr.getUnstableBars(), "Initial series should report 8");
        // Build second series with 4 bars
        BarSeries secondSeries = new BaseBarSeriesBuilder().withNumTypeOf(numFunction).build();
        for (int i = 0; i < 4; i++) {
            secondSeries.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        // Replace the private 'series' field via reflection
        Field seriesField = AbstractIndicator.class.getDeclaredField("series");
        seriesField.setAccessible(true);
        seriesField.set(atr, secondSeries);
        // The indicator should now report the count of the new series
        assertEquals(4, atr.getUnstableBars(), "After series replacement, unstable bars should reflect the new series count (4)");
    }

    @Test
    void testGetUnstableBars_nullSeriesHandling() {
        assertThrows(NullPointerException.class, () -> new ATRIndicator(null, 5), "Constructing ATRIndicator with a null BarSeries should throw NullPointerException");
    }

    @Test
    void testGetUnstableBars_mockedNegativeBarCount() {
        // Prepare a valid series with 5 bars
        for (int i = 0; i < 5; i++) {
            series.addBar(new MockBar(ZonedDateTime.now(), 0, 10, 12, 8, 0, 0, 0, numFunction));
        }
        // Create a real instance and then spy it
        ATRIndicator real = new ATRIndicator(series, 5);
        ATRIndicator spy = Mockito.spy(real);
        // Force getBarCount() to return a negative value
        Mockito.doReturn(-3).when(spy).getBarCount();
        // The method is expected to guard against this illegal state
        assertThrows(IllegalArgumentException.class, spy::getUnstableBars, "Negative bar count reported by getBarCount() should cause IllegalArgumentException");
    }

    @Test
    void testGetUnstableBars_maxIntegerCount() {
        // We cannot really create a series with Integer.MAX_VALUE bars (memory limits),
        // but we can simulate the situation by mocking getBarCount().
        ATRIndicator real = new ATRIndicator(series, 0);
        ATRIndicator spy = Mockito.spy(real);
        Mockito.doReturn(Integer.MAX_VALUE).when(spy).getBarCount();
        assertEquals(Integer.MAX_VALUE, spy.getUnstableBars(), "getUnstableBars() should correctly return Integer.MAX_VALUE without overflow");
    }
}
