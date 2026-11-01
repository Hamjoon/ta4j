```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_6_Test {

    @Mock BarSeries barSeries;
    @Mock Strategy strategy;
    @Mock TradeExecutionModel tradeExecutionModel;
    @Mock CostModel transactionCostModel;
    @Mock CostModel holdingCostModel;
    @Mock Num amount;
    @Mock List<?> barData; // only size is needed

    BarSeriesManager manager;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // Default stubbing – individual tests may override
        when(barSeries.getBeginIndex()).thenReturn(0);
        when(barSeries.getEndIndex()).thenReturn(5);
        when(barSeries.getBarData()).thenReturn(barData);
        when(barData.size()).thenReturn(6);

        manager = new BarSeriesManager(barSeries,
                transactionCostModel,
                holdingCostModel,
                tradeExecutionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset all mocks to keep tests independent
        reset(barSeries, strategy, tradeExecutionModel,
                transactionCostModel, holdingCostModel, amount, barData);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up
    }

    /* --------------------------------------------------------------
     * Existing tests (kept unchanged)
     * -------------------------------------------------------------- */

    @Test
    void shouldDoNothingWhenStrategyNeverOperates() {
        // strategy.shouldOperate returns false for any index
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 0, 5);

        // No execution should have been triggered
        verify(tradeExecutionModel, never()).execute(anyInt(), any(), any(), any());

        // The returned record should contain no trades
        assertEquals(0, record.getTradeCount(),
                "TradingRecord should be empty when strategy never operates");
    }

    @Test
    void shouldExecuteTradeWhenStrategyOperatesWithinRange() {
        // Operate only on indices 2 and 4
        when(strategy.shouldOperate(eq(2), any(TradingRecord.class))).thenReturn(true);
        when(strategy.shouldOperate(eq(4), any(TradingRecord.class))).thenReturn(true);
        when(strategy.shouldOperate(argThat(i -> i != 2 && i != 4), any(TradingRecord.class)))
                .thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount, 0, 5);

        // Verify execute called exactly for the two expected indices
        verify(tradeExecutionModel, times(1)).execute(eq(2), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(eq(4), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(2)).execute(anyInt(), any(), any(), any());

        // Record should contain two trades (one per execution)
        assertEquals(2, record.getTradeCount(),
                "TradingRecord should contain two trades for indices 2 and 4");
    }

    @Test
    void shouldAttemptClosingPositionAfterEndIndexWhenOpen() {
        // Simulate a series larger than the run range
        when(barSeries.getEndIndex()).thenReturn(5);
        when(barData.size()).thenReturn(10); // seriesMaxSize = 10

        // Strategy does NOT operate inside the run range (0‑5)
        when(strategy.shouldOperate(intThat(i -> i >= 0 && i <= 5), any(TradingRecord.class)))
                .thenReturn(false);

        // Strategy operates on the first index after the run range (6)
        when(strategy.shouldOperate(eq(6), any(TradingRecord.class))).thenReturn(true);
        // No further operation
        when(strategy.shouldOperate(intThat(i -> i > 6), any(TradingRecord.class)))
                .thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 0, 5);

        // Verify that execute was called once for the extra index (6)
        verify(tradeExecutionModel, times(1))
                .execute(eq(6), any(), eq(barSeries), eq(amount));

        // Ensure no execution happened for indices 0‑5
        verify(tradeExecutionModel, never())
                .execute(intThat(i -> i >= 0 && i <= 5), any(), any(), any());

        // The record should contain a single trade (opened at index 6)
        assertEquals(1, record.getTradeCount(),
                "TradingRecord should contain one trade opened by the extra‑loop execution");
    }

    /* --------------------------------------------------------------
     * Additional test cases derived from the JSON specification
     * -------------------------------------------------------------- */

    @Test
    void basicFlow_neverOperates_indexBoundsOutsideSeries() {
        // startIndex < series begin, finishIndex > series end, strategy never operates
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, -5, 200);

        verify(tradeExecutionModel, never()).execute(anyInt(), any(), any(), any());
        assertTrue(record.isClosed(), "Record should be closed when nothing happened");
        assertEquals(0, record.getTradeCount(), "No trades should be recorded");
    }

    @Test
    void basicFlow_openCloseWithinRange() {
        // open at 3, close at 6 (both inside series 0‑5, so we need to extend series to 8)
        when(barSeries.getEndIndex()).thenReturn(8);
        when(barData.size()).thenReturn(9);

        Set<Integer> trueIdx = Set.of(3, 6);
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenAnswer(inv ->
                trueIdx.contains(inv.getArgument(0)));

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount, 2, 8);

        verify(tradeExecutionModel, times(1)).execute(eq(3), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(eq(6), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(2)).execute(anyInt(), any(), any(), any());

        assertTrue(record.isClosed(), "Record should be closed after matching open/close");
        assertEquals(1, record.getTradeCount(),
                "One completed trade should be present (open at 3, close at 6)");
    }

    @Test
    void basicFlow_openAtEndCloseAfter() {
        // series end = 5, open at 5, close at 6 (extra bar)
        when(barSeries.getEndIndex()).thenReturn(5);
        when(barData.size()).thenReturn(7); // seriesMaxSize = 7 (0‑6)

        Set<Integer> trueIdx = Set.of(5, 6);
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenAnswer(inv ->
                trueIdx.contains(inv.getArgument(0)));

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 0, barSeries.getEndIndex());

        verify(tradeExecutionModel, times(1)).execute(eq(5), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(eq(6), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(2)).execute(anyInt(), any(), any(), any());

        assertTrue(record.isClosed(), "Record must be closed after the extra‑bar close");
        assertEquals(1, record.getTradeCount(),
                "One completed trade should be recorded");
    }

    @Test
    void basicFlow_openAtEndNoCloseAfter() {
        // series end = 5, run finishes at index 3 (endIndex - 2), open at 3, never close
        when(barSeries.getEndIndex()).thenReturn(5);
        when(barData.size()).thenReturn(6);

        when(strategy.shouldOperate(eq(3), any(TradingRecord.class))).thenReturn(true);
        when(strategy.shouldOperate(argThat(i -> i != 3), any(TradingRecord.class))).thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount, 0, 3);

        verify(tradeExecutionModel, times(1)).execute(eq(3), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(anyInt(), any(), any(), any());

        assertFalse(record.isClosed(), "Record should remain open (position not closed)");
        assertEquals(0, record.getTradeCount(),
                "No completed trades because the position is still open");
    }

    @Test
    void basicFlow_operatesOnlyWithinBounds() {
        // Extend series to 10 so that index 7 is inside the calculated range
        when(barSeries.getEndIndex()).thenReturn(10);
        when(barData.size()).thenReturn(11);

        // Strategy true only at index 7
        when(strategy.shouldOperate(eq(7), any(TradingRecord.class))).thenReturn(true);
        when(strategy.shouldOperate(argThat(i -> i != 7), any(TradingRecord.class))).thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 5, 10);

        verify(tradeExecutionModel, times(1)).execute(eq(7), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(anyInt(), any(), any(), any());

        assertFalse(record.isClosed(), "Record should be open (only entry trade)");
        assertEquals(0, record.getTradeCount(),
                "No completed trades because the position never closed");
    }

    @Test
    void startIndexGreaterThanFinishIndex_noIteration() {
        // start > finish, strategy never operates (but even if it did, loop is skipped)
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(true);

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 10, 5);

        verify(tradeExecutionModel, never()).execute(anyInt(), any(), any(), any());
        assertTrue(record.isClosed(), "Record should be closed when loop never runs");
        assertEquals(0, record.getTradeCount(), "No trades should be recorded");
    }

    @Test
    void singleIndexOperation_openOnly() {
        // run limited to index 3, strategy true only at 3
        when(strategy.shouldOperate(eq(3), any(TradingRecord.class))).thenReturn(true);
        when(strategy.shouldOperate(argThat(i -> i != 3), any(TradingRecord.class))).thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount, 3, 3);

        verify(tradeExecutionModel, times(1)).execute(eq(3), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(anyInt(), any(), any(), any());

        assertFalse(record.isClosed(), "Record should stay open after a single entry");
        assertEquals(0, record.getTradeCount(),
                "No completed trades because there is no exit");
    }

    @Test
    void extraBarsSkippedWhenAlreadyClosedAtSeriesEnd() {
        // open at 1, close at series end (5)
        when(barSeries.getEndIndex()).thenReturn(5);
        when(barData.size()).thenReturn(6);

        Set<Integer> trueIdx = Set.of(1, 5);
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenAnswer(inv ->
                trueIdx.contains(inv.getArgument(0)));

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 0, 5);

        verify(tradeExecutionModel, times(1)).execute(eq(1), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(eq(5), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(2)).execute(anyInt(), any(), any(), any());

        // Ensure no extra‑bar execution (i > 5)
        verify(tradeExecutionModel, never()).execute(intThat(i -> i > 5), any(), any(), any());

        assertTrue(record.isClosed(), "Record should be closed after closing at the last bar");
        assertEquals(1, record.getTradeCount(),
                "One completed trade should be present");
    }

    @Test
    void zeroAmount_isForwardedToExecutionModel() {
        // strategy true at index 2, amount is zero (mocked)
        when(strategy.shouldOperate(eq(2), any(TradingRecord.class))).thenReturn(true);
        when(strategy.shouldOperate(argThat(i -> i != 2), any(TradingRecord.class))).thenReturn(false);

        TradingRecord record = manager.run(strategy, TradeType.SELL, amount, 1, 5);

        verify(tradeExecutionModel, times(1)).execute(eq(2), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(anyInt(), any(), any(), any());

        assertFalse(record.isClosed(), "Record stays open after a single entry");
        assertEquals(0, record.getTradeCount(),
                "No completed trades because the position never closed");
    }

    @Test
    void nullStrategy_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> manager.run(null, TradeType.BUY, amount, 0, barSeries.getEndIndex()),
                "Passing a null strategy should result in NPE");
    }

    @Test
    void consecutiveTrueSignals() {
        // strategy true on 2,3,4
        Set<Integer> trueIdx = Set.of(2, 3, 4);
        when(strategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenAnswer(inv ->
                trueIdx.contains(inv.getArgument(0)));

        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 1, 5);

        verify(tradeExecutionModel, times(1)).execute(eq(2), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(eq(3), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(1)).execute(eq(4), any(), eq(barSeries), eq(amount));
        verify(tradeExecutionModel, times(3)).execute(anyInt(), any(), any(), any());

        // After three calls: entry (2), exit (3), entry (4) => open position
        assertFalse(record.isClosed(), "Record should be open after an odd number of operations");
        assertEquals(1, record.getTradeCount(),
                "One completed trade (2‑3) should be recorded, the third entry is pending");
    }
}
```