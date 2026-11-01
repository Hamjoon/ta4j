```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

/**
 * Tests for {@link BarSeriesManager#run(Strategy, TradeType, Num)}.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_3_Test {

    @Mock
    private BarSeries barSeriesMock;

    @Mock
    private Strategy strategyMock;

    @Mock
    private Num amountMock;

    @Mock
    private TradingRecord tradingRecordMock;

    // The class under test – we will spy it to verify the internal delegation.
    private BarSeriesManager managerSpy;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Create a real instance of BarSeriesManager with the mocked BarSeries,
        // then spy it so we can stub/verify the protected overload.
        BarSeriesManager realManager = new BarSeriesManager(barSeriesMock);
        managerSpy = spy(realManager);

        // Default indices returned by the mocked BarSeries.
        when(barSeriesMock.getBeginIndex()).thenReturn(0);
        when(barSeriesMock.getEndIndex()).thenReturn(10);

        // Stub the overloaded run method that includes start/finish indices.
        // This prevents execution of the full back‑testing logic.
        doReturn(tradingRecordMock)
                .when(managerSpy)
                .run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(0), eq(10));
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset all mocks to avoid interference between tests.
        reset(barSeriesMock, strategyMock, amountMock, tradingRecordMock, managerSpy);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void shouldDelegateToRunWithIndicesAndReturnTradingRecord() {
        // GIVEN – the trade type and amount are prepared (TradeType is an enum, so we use a real value)
        TradeType tradeType = TradeType.BUY;

        // WHEN – invoking the method under test
        TradingRecord result = managerSpy.run(strategyMock, tradeType, amountMock);

        // THEN – the result should be the stubbed TradingRecord
        assertSame(tradingRecordMock, result, "The returned TradingRecord should be the stubbed instance");

        // Verify that the internal overload was called exactly once with the expected arguments.
        verify(managerSpy, times(1))
                .run(eq(strategyMock), eq(tradeType), eq(amountMock), eq(0), eq(10));
    }
}
```