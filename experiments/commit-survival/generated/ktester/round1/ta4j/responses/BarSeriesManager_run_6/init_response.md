```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.Trade.TradeType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_6_Test {

    @Mock BarSeries barSeries;
    @Mock Strategy strategy;
    @Mock TradeExecutionModel tradeExecutionModel;
    @Mock CostModel transactionCostModel;
    @Mock CostModel holdingCostModel;
    @Mock Num amount;
    @Mock List<?> barData; // we only need its size

    BarSeriesManager manager;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // Common stubbing for the BarSeries used by all tests
        when(barSeries.getBeginIndex()).thenReturn(0);
        when(barSeries.getEndIndex()).thenReturn(5);
        when(barSeries.getBarData()).thenReturn(barData);
        // default size – individual tests can override
        when(barData.size()).thenReturn(6);

        manager = new BarSeriesManager(barSeries,
                transactionCostModel,
                holdingCostModel,
                tradeExecutionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks to avoid cross‑test interference
        reset(barSeries, strategy, tradeExecutionModel,
                transactionCostModel, holdingCostModel, amount, barData);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up
    }

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
}
```