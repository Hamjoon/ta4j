package org.ta4j.core.indicators;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.num.Num;
import java.time.ZonedDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ATRIndicator_toString_Test {

    /**
     * Supplies the test data for {@link #testToStringWithVariousBarCounts(int, String)}.
     * The data mirrors the JSON test cases supplied in the task description.
     */
    private static Stream<Arguments> provideBarCountCases() {
        return Stream.of(// Group: ATRIndicator.toString() format verification
        Arguments.of(0, "ATRIndicator barCount: 0"), Arguments.of(1, "ATRIndicator barCount: 1"), Arguments.of(14, "ATRIndicator barCount: 14"), Arguments.of(Integer.MAX_VALUE, "ATRIndicator barCount: " + Integer.MAX_VALUE), Arguments.of(-5, "ATRIndicator barCount: -5"), // Group: ATRIndicator.toString() additional format verification
        Arguments.of(2, "ATRIndicator barCount: 2"), Arguments.of(Integer.MIN_VALUE, "ATRIndicator barCount: " + Integer.MIN_VALUE), // The case where a non‑null BarSeries is present (already satisfied by using a real series)
        Arguments.of(0, "ATRIndicator barCount: 0"));
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
        assertEquals(expected, indicator.toString(), "toString() should include the simple class name and the configured bar count");
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
        assertEquals(expected, indicator.toString(), "toString() should reflect the bar count regardless of which constructor is used");
    }

    /**
     * Parameterized test covering a wide range of {@code barCount} values.
     * The indicator is instantiated with a (BarSeries, int) constructor for all cases.
     */
    @ParameterizedTest(name = "[{index}] barCount={0} → expected=\"{1}\"")
    @MethodSource("provideBarCountCases")
    void testToStringWithVariousBarCounts(int barCount, String expected) {
        // An empty but non‑null BarSeries is sufficient – the series content does not affect toString().
        BarSeries series = new BaseBarSeriesBuilder().build();
        ATRIndicator indicator = new ATRIndicator(series, barCount);
        assertEquals(expected, indicator.toString(), "toString() should correctly concatenate the class name and the provided barCount");
    }
}
