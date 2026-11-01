package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
class CashFlow_getUnstableBars_Test {

    /**
     * Function used to create {@link Num} instances from primitive numbers.
     */
    private static Function<Number, Num> numFunction;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation used by the library.
        numFunction = DecimalNum::valueOf;
    }

    @Test
    void getUnstableBars_returnsZero_whenCreatedWithBarSeriesAndPosition() {
        // given
        BarSeries series = new MockBarSeries(numFunction, 1d, 2d, 3d);
        // default BUY position with no trades
        Position position = new Position();
        // when
        CashFlow cashFlow = new CashFlow(series, position);
        int unstable = cashFlow.getUnstableBars();
        // then
        assertEquals(0, unstable, "CashFlow#getUnstableBars() should return 0 for the Position constructor");
    }

    @Test
    void getUnstableBars_returnsZero_whenCreatedWithBarSeriesAndTradingRecord() {
        // given
        BarSeries series = new MockBarSeries(numFunction, 10d, 20d, 30d);
        // empty record (no positions)
        TradingRecord record = new TradingRecord();
        // when
        CashFlow cashFlow = new CashFlow(series, record);
        int unstable = cashFlow.getUnstableBars();
        // then
        assertEquals(0, unstable, "CashFlow#getUnstableBars() should return 0 for the TradingRecord constructor");
    }

    @Test
    void getUnstableBars_returnsZero_whenCreatedWithBarSeriesTradingRecordAndFinalIndex() {
        // given
        BarSeries series = new MockBarSeries(numFunction, // a relatively long series to ensure the final index is meaningful
        5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d);
        // empty record
        TradingRecord record = new TradingRecord();
        // use the series' natural end index
        int finalIndex = series.getEndIndex();
        // when
        CashFlow cashFlow = new CashFlow(series, record, finalIndex);
        int unstable = cashFlow.getUnstableBars();
        // then
        assertEquals(0, unstable, "CashFlow#getUnstableBars() should return 0 for the three‑argument constructor");
    }

    /**
     * Verify that repeated calls always return the constant 0.
     */
    @Test
    void getUnstableBars_isIdempotent_multipleCalls() {
        BarSeries series = new MockBarSeries(numFunction, 1d, 2d, 3d);
        CashFlow cashFlow = new CashFlow(series, new Position());
        // call several times
        for (int i = 0; i < 5; i++) {
            assertEquals(0, cashFlow.getUnstableBars(), "Repeated call #" + i + " should still return 0");
        }
    }

    /**
     * Two independent instances must both return 0.
     */
    @Test
    void getUnstableBars_multipleInstances_eachReturnsZero() {
        BarSeries series1 = new MockBarSeries(numFunction, 1d, 2d);
        BarSeries series2 = new MockBarSeries(numFunction, 10d, 20d, 30d);
        CashFlow cf1 = new CashFlow(series1, new Position());
        CashFlow cf2 = new CashFlow(series2, new TradingRecord());
        assertAll(() -> assertEquals(0, cf1.getUnstableBars(), "First CashFlow instance should return 0"), () -> assertEquals(0, cf2.getUnstableBars(), "Second CashFlow instance should return 0"));
    }

    /**
     * Prior invocation of other public methods must not affect the result.
     */
    @Test
    void getUnstableBars_afterOtherMethodCalls_stillZero() {
        BarSeries series = new MockBarSeries(numFunction, 5d, 6d, 7d);
        CashFlow cashFlow = new CashFlow(series, new Position());
        // invoke getValue on a valid index
        Num value = cashFlow.getValue(series.getBeginIndex());
        assertNotNull(value, "getValue should return a non‑null Num");
        // invoke the static helper addCost (arbitrary numbers)
        Num raw = numFunction.apply(100);
        Num cost = numFunction.apply(2);
        Num added = CashFlow.addCost(raw, cost, true);
        assertNotNull(added, "addCost should return a Num");
        // finally, verify getUnstableBars still returns 0
        assertEquals(0, cashFlow.getUnstableBars(), "After other method calls, getUnstableBars must still return 0");
    }

    /**
     * Concurrency test – multiple threads call getUnstableBars on the same instance.
     */
    @Test
    void getUnstableBars_concurrentAccess_returnsZeroForAllThreads() throws Exception {
        BarSeries series = new MockBarSeries(numFunction, 1d, 2d, 3d, 4d);
        CashFlow cashFlow = new CashFlow(series, new Position());
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> cashFlow.getUnstableBars());
        }
        List<Future<Integer>> futures = executor.invokeAll(tasks);
        executor.shutdownNow();
        for (Future<Integer> f : futures) {
            assertEquals(0, f.get(), "Each concurrent call should return 0");
        }
    }

    /**
     * Even after an exception from another method, getUnstableBars must stay unaffected.
     */
    @Test
    void getUnstableBars_afterExceptionScenario_stillZero() {
        BarSeries series = new MockBarSeries(numFunction, 1d, 2d, 3d);
        CashFlow cashFlow = new CashFlow(series, new Position());
        // provoke an out‑of‑range exception from getValue
        int outOfRangeIndex = series.getEndIndex() + 10;
        assertThrows(IndexOutOfBoundsException.class, () -> cashFlow.getValue(outOfRangeIndex), "Calling getValue with an invalid index should throw");
        // after catching the exception, getUnstableBars must still be 0
        assertEquals(0, cashFlow.getUnstableBars(), "After an exception, getUnstableBars must still return 0");
    }

    /**
     * Parameterized version of the idempotence test – runs the same
     * verification for several different series lengths.
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 5, 10 })
    void getUnstableBars_idempotent_withVariousSeriesLengths(int length) {
        // build a series with 'length' identical bars
        double[] prices = new double[length];
        for (int i = 0; i < length; i++) {
            prices[i] = i + 1.0;
        }
        BarSeries series = new MockBarSeries(numFunction, prices);
        CashFlow cashFlow = new CashFlow(series, new Position());
        // call three times – all must be 0
        assertEquals(0, cashFlow.getUnstableBars());
        assertEquals(0, cashFlow.getUnstableBars());
        assertEquals(0, cashFlow.getUnstableBars());
    }
}
