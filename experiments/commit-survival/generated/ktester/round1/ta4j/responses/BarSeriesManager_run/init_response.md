```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;

/**
 * Tests for {@link BarSeriesManager#run(Strategy)}.
 *
 * The test verifies that the single‑argument {@code run} method delegates to the
 * two‑argument overload using {@link TradeType#BUY} as the default trade type.
 * No private members of {@code BarSeriesManager} are accessed.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_Test {

    @Mock
    private BarSeries barSeries;          // dependency injected into the manager

    @Mock
    private Strategy strategy;           // strategy passed to the run method

    @Mock
    private TradingRecord expectedRecord; // the record we expect the overloaded method to return

    private BarSeriesManager manager;    // system under test (SUT)

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Create a *spy* of the real manager so we can stub the overloaded method
        // while still exercising the public API.
        manager = spy(new BarSeriesManager(barSeries));
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset the spy to avoid cross‑test interference.
        clearInvocations(manager);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up.
    }

    @Test
    void run_ShouldDelegateToRun_WithBuyTradeType() {
        // GIVEN – the overloaded method is stubbed to return a known TradingRecord
        doReturn(expectedRecord)
                .when(manager)
                .run(eq(strategy), eq(TradeType.BUY));

        // WHEN – the single‑argument run method is invoked
        TradingRecord actual = manager.run(strategy);

        // THEN – it should return the stubbed record and delegate correctly
        assertSame(expectedRecord, actual,
                "run(Strategy) must return the TradingRecord produced by run(Strategy, TradeType.BUY)");
        verify(manager, times(1)).run(eq(strategy), eq(TradeType.BUY));
    }
}
```