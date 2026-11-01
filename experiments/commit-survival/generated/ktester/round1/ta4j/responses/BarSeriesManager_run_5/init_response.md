```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_5_Test {

    @Mock
    private BarSeries barSeries;

    @Mock
    private Strategy strategy;

    @Mock
    private Num amount;

    @Mock
    private TradingRecord tradingRecord;

    // The manager we are testing – we will spy it so that we can stub the overloaded
    // run method without invoking the real implementation.
    private BarSeriesManager manager;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for this test class.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the spy with the constructor that only receives a BarSeries.
        manager = Mockito.spy(new BarSeriesManager(barSeries));

        // Stub BarSeries.one() to return a deterministic Num instance.
        when(barSeries.one()).thenReturn(amount);

        // Stub the overloaded run(...) that the method under test should delegate to.
        // Use any() for the TradeType argument because we will verify the exact call later.
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt()))
                .thenReturn(tradingRecord);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void shouldDelegateToOverloadedRunWithBarSeriesOneAmount() {
        // GIVEN
        TradeType tradeType = TradeType.BUY; // typical enum value; adjust if different
        int startIndex = 0;
        int finishIndex = 10;

        // WHEN
        TradingRecord result = manager.run(strategy, tradeType, startIndex, finishIndex);

        // THEN
        assertSame(tradingRecord, result,
                "The run method should return the TradingRecord produced by the overloaded method.");

        // Verify that the overloaded method was invoked exactly once with the expected arguments.
        verify(manager, times(1)).run(strategy, tradeType, amount, startIndex, finishIndex);
    }
}
```