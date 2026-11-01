package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.*;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import static org.mockito.Mockito.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.ta4j.core.mocks.MockBar;

@ExtendWith(MockitoExtension.class)
class CashFlow_getSize_Test {

    /**
     * Function used by {@link MockBarSeries} to convert numbers to {@link Num}.
     */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    private BarSeries series;

    private CashFlow cashFlow;

    @BeforeEach
    void setupBeforeEach() {
        // Create a series with a known number of bars (e.g., 7).
        series = new MockBarSeries(NUM_FUNCTION, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);
        // Use an empty trading record – the cash‑flow calculation will simply
        // keep the initial value of 1 for every index.
        cashFlow = new CashFlow(series, new BaseTradingRecord());
    }

    @AfterEach
    void teardownAfterEach() {
        // Release references; not strictly required but keeps the test tidy.
        cashFlow = null;
        series = null;
    }

    /**
     * Provides pairs of {@code expectedBarCount} and a description.
     * The underlying {@link BarSeries} is mocked so that {@code getBarCount()}
     * returns exactly the supplied value.
     */
    private static Stream<Arguments> sizeProvider() {
        return Stream.of(Arguments.of(0, "no bars"), Arguments.of(1, "exactly one bar"), Arguments.of(2, "exactly two bars"), Arguments.of(42, "typical positive number of bars"), Arguments.of(999_999, "large but non‑maximum positive number of bars"), Arguments.of(Integer.MAX_VALUE - 1, "Integer.MAX_VALUE‑1"), Arguments.of(Integer.MAX_VALUE, "Integer.MAX_VALUE"), Arguments.of(-1, "negative bar count (invalid but allowed)"));
    }

    @Test
    void getSizeShouldReturnBarSeriesBarCount() {
        // should be 7 for the series above
        int expectedSize = series.getBarCount();
        assertEquals(expectedSize, cashFlow.getSize(), "CashFlow#getSize() must return the number of bars in the underlying BarSeries");
    }

    @Test
    void getSizeWorksWithLargeSeries() {
        // Build a larger series (e.g., 1 000 bars) to ensure the method scales.
        int largeSize = 1_000;
        series = new MockBarSeries(NUM_FUNCTION, java.util.Collections.nCopies(largeSize, new org.ta4j.core.mocks.MockBar(10, NUM_FUNCTION)));
        cashFlow = new CashFlow(series, new BaseTradingRecord());
        assertEquals(largeSize, cashFlow.getSize(), "CashFlow#getSize() should correctly handle large BarSeries");
    }

    @ParameterizedTest(name = "getSize() should return {0} when series reports {1}")
    @MethodSource("sizeProvider")
    void getSizeReturnsBarSeriesBarCount(int expectedBarCount, String _description) {
        // Arrange: mock a BarSeries that returns the required count.
        BarSeries mockedSeries = mock(BarSeries.class);
        when(mockedSeries.getBarCount()).thenReturn(expectedBarCount);
        CashFlow cf = new CashFlow(mockedSeries, new BaseTradingRecord());
        // Act & Assert
        assertEquals(expectedBarCount, cf.getSize(), "CashFlow#getSize() must forward the bar count from the underlying BarSeries");
    }

    @Test
    void getSizeReflectsUpdatedBarCountAfterAddingBar() {
        // Start with a series that reports 5 bars.
        AtomicInteger barCount = new AtomicInteger(5);
        BarSeries mutableSeries = mock(BarSeries.class);
        when(mutableSeries.getBarCount()).thenAnswer(invocation -> barCount.get());
        // Simulate “adding a bar” by incrementing the counter.
        // (We do not need to call a real addBar method – only the count matters.)
        // now 6
        barCount.incrementAndGet();
        CashFlow cf = new CashFlow(mutableSeries, new BaseTradingRecord());
        assertEquals(6, cf.getSize(), "After the underlying BarSeries count is increased, getSize() must reflect the new value");
    }

    @Test
    void getSizePropagatesRuntimeExceptionFromBarSeries() {
        BarSeries explodingSeries = mock(BarSeries.class);
        when(explodingSeries.getBarCount()).thenThrow(new RuntimeException("boom!"));
        CashFlow cf = new CashFlow(explodingSeries, new BaseTradingRecord());
        RuntimeException ex = assertThrows(RuntimeException.class, cf::getSize, "CashFlow#getSize() should propagate RuntimeException from BarSeries#getBarCount()");
        assertEquals("boom!", ex.getMessage());
    }

    @Test
    void getSizeThrowsNullPointerExceptionWhenBarSeriesIsNull() {
        // Construct CashFlow with a null BarSeries – this is allowed by the
        // constructor signature but any operation that accesses the series should NPE.
        CashFlow cf = new CashFlow(null, new BaseTradingRecord());
        assertThrows(NullPointerException.class, cf::getSize, "Calling getSize() on a CashFlow built with a null BarSeries must throw NPE");
    }
}
