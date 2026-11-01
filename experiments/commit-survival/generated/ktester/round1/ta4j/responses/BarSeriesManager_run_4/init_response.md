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
 * Tests for {@link BarSeriesManager#run(Strategy, int, int)}.
 * The test verifies that the method delegates to the overloaded
 * {@code run(Strategy, TradeType, Num, int, int)} variant with the expected
 * parameters (BUY trade type and the result of {@code BarSeries.one()}).
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_4_Test {

    @Mock
    private BarSeries mockBarSeries;

    @Mock
    private Strategy mockStrategy;

    @Mock
    private TradingRecord mockTradingRecord;

    @Mock
    private Num mockNum;

    private BarSeriesManager manager;
    private BarSeriesManager spyManager;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for this test class.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the manager with the mocked BarSeries.
        manager = new BarSeriesManager(mockBarSeries);
        // Create a spy so we can verify the internal delegation without invoking the real logic.
        spyManager = spy(manager);

        // Stub BarSeries.one() to return a mocked Num instance.
        when(mockBarSeries.one()).thenReturn(mockNum);

        // Stub the overloaded run method to return a mocked TradingRecord.
        // This prevents execution of the full back‑testing logic which is out of scope.
        when(spyManager.run(
                any(Strategy.class),
                any(TradeType.class),
                any(Num.class),
                anyInt(),
                anyInt()))
                .thenReturn(mockTradingRecord);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks after each test to avoid interactions bleeding between tests.
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
        // GIVEN
        int startIndex = 3;
        int finishIndex = 7;

        // WHEN
        TradingRecord result = spyManager.run(mockStrategy, startIndex, finishIndex);

        // THEN
        assertSame(mockTradingRecord, result,
                "The run method should return the TradingRecord from the overloaded variant.");
        // Verify that the overloaded method was called with the correct arguments.
        verify(spyManager, times(1)).run(
                eq(mockStrategy),
                eq(TradeType.BUY),
                eq(mockNum),
                eq(startIndex),
                eq(finishIndex));
    }
}
```