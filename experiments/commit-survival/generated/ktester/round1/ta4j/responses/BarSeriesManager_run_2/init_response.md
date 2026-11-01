```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

/**
 * Tests for {@link BarSeriesManager#run(Strategy, TradeType)}.
 *
 * The method under test simply delegates to {@code run(strategy, tradeType, barSeries.one())}.
 * The test verifies that delegation occurs correctly and that the returned {@link TradingRecord}
 * is the one produced by the delegated call.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_2_Test {

    /** Mocked dependencies */
    @Mock
    private BarSeries barSeries;

    @Mock
    private Strategy strategy;

    @Mock
    private TradingRecord expectedRecord;

    @Mock
    private Num oneAmount;

    /** The manager under test – created as a spy so we can stub the overloaded method */
    private BarSeriesManager manager;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the manager with the mocked BarSeries.
        manager = Mockito.spy(new BarSeriesManager(barSeries));
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset interactions after each test to keep tests independent.
        Mockito.reset(barSeries, strategy, expectedRecord, oneAmount, manager);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup needed.
    }

    @Test
    void shouldDelegateRunToAmountBasedMethod_andReturnItsResult() {
        // GIVEN
        // barSeries.one() should return a Num representing the amount "1".
        when(barSeries.one()).thenReturn(oneAmount);
        // The overloaded method should return the mocked TradingRecord.
        when(manager.run(strategy, TradeType.BUY, oneAmount)).thenReturn(expectedRecord);

        // WHEN
        TradingRecord actual = manager.run(strategy, TradeType.BUY);

        // THEN
        assertSame(expectedRecord, actual, "The run method should return the record from the delegated call");
        // Verify that the delegation used the correct amount.
        verify(barSeries, times(1)).one();
        verify(manager, times(1)).run(strategy, TradeType.BUY, oneAmount);
        // No other interactions should happen.
        verifyNoMoreInteractions(barSeries, manager);
    }
}
```