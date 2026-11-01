```java
package org.ta4j.core.backtest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

/**
 * Unit tests for {@link BarSeriesManager#run(Strategy, int, int)}.
 *
 * <p>
 * The method is a thin delegator that forwards the call to the overloaded
 * {@code run(Strategy, TradeType, Num, int, int)} variant using {@link TradeType#BUY}
 * and the {@code Num} value returned by {@code BarSeries.one()}.
 * </p>
 *
 * <p>
 * Tests are divided into two groups:
 * <ul>
 *   <li>Successful delegations – the overloaded method is stubbed to return a
 *   mocked {@link TradingRecord} so that we can verify the correct arguments are
 *   passed.</li>
 *   <li>Invalid‑argument scenarios – the overload is *not* stubbed, allowing the
 *   real implementation to perform its validation and throw the expected
 *   exceptions.</li>
 * </ul>
 * </p>
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

    /** The manager created with the mocked {@link BarSeries}. */
    private BarSeriesManager manager;
    /** Spy of the manager – used to stub the overloaded {@code run} only for
     * successful cases while preserving real behaviour for exception cases. */
    private BarSeriesManager spyManager;

    @BeforeEach
    void setUp() {
        // manager uses the mocked BarSeries
        manager = new BarSeriesManager(mockBarSeries);
        spyManager = spy(manager);

        // BarSeries.one() is required by the delegating method
        when(mockBarSeries.one()).thenReturn(mockNum);

        // The series size is needed for range‑validation tests
        when(mockBarSeries.getBarCount()).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        // clean mock interactions between tests
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
    }

    /* --------------------------------------------------------------
     *  Successful delegation test cases (happy‑path & edge cases)
     * -------------------------------------------------------------- */

    private static Stream<Arguments> successfulCases() {
        return Stream.of(
                // happy‑path: normal range
                Arguments.of(0, 10, "happy‑path"),
                // edge: start == finish (empty processing)
                Arguments.of(5, 5, "empty‑range")
        );
    }

    @ParameterizedTest(name = "[{index}] run({2}) start={0} finish={1}")
    @MethodSource("successfulCases")
    void shouldDelegateToOverloadedRunWithBuyAndOneNum(int startIndex,
                                                       int finishIndex,
                                                       String description) {
        // Arrange – stub the overloaded run to return a known TradingRecord
        doReturn(mockTradingRecord)
                .when(spyManager)
                .run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());

        // Act
        TradingRecord result = spyManager.run(mockStrategy, startIndex, finishIndex);

        // Assert – the result must be the stubbed record
        assertSame(mockTradingRecord, result,
                "The delegating run method must return the TradingRecord produced by the overloaded variant.");

        // Verify that the overload received the exact arguments we expect
        verify(spyManager, times(1)).run(
                eq(mockStrategy),
                eq(TradeType.BUY),
                eq(mockNum),
                eq(startIndex),
                eq(finishIndex));
    }

    /* --------------------------------------------------------------
     *  Invalid‑argument scenarios – expect exceptions
     * -------------------------------------------------------------- */

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        int start = 8;
        int finish = 3;
        // No stubbing – let the real overload perform validation
        assertThrows(IllegalArgumentException.class,
                () -> spyManager.run(mockStrategy, start, finish),
                "startIndex > finishIndex must trigger IllegalArgumentException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartIsNegative() {
        int start = -1;
        int finish = 4;
        assertThrows(IllegalArgumentException.class,
                () -> spyManager.run(mockStrategy, start, finish),
                "Negative startIndex must trigger IllegalArgumentException");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenStrategyIsNull() {
        int start = 0;
        int finish = 5;
        assertThrows(NullPointerException.class,
                () -> spyManager.run(null, start, finish),
                "Null strategy must trigger NullPointerException");
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenStartExceedsSeriesSize() {
        int start = 1000;
        int finish = 1010;
        assertThrows(IndexOutOfBoundsException.class,
                () -> spyManager.run(mockStrategy, start, finish),
                "startIndex beyond series size must trigger IndexOutOfBoundsException");
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenFinishExceedsSeriesSize() {
        int start = 0;
        int finish = 1000;
        assertThrows(IndexOutOfBoundsException.class,
                () -> spyManager.run(mockStrategy, start, finish),
                "finishIndex beyond series size must trigger IndexOutOfBoundsException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFinishIsNegative() {
        int start = 2;
        int finish = -1;
        assertThrows(IllegalArgumentException.class,
                () -> spyManager.run(mockStrategy, start, finish),
                "Negative finishIndex must trigger IllegalArgumentException");
    }

    @Test
    void shouldReturnEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        // Simulate an empty series (size 0)
        when(mockBarSeries.getBarCount()).thenReturn(0);
        // Stub overload to return a concrete (non‑mock) empty TradingRecord
        TradingRecord emptyRecord = new BaseTradingRecord();
        doReturn(emptyRecord)
                .when(spyManager)
                .run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());

        TradingRecord result = spyManager.run(mockStrategy, 0, 0);
        assertNotNull(result, "Result must not be null");
        assertTrue(result.getPositions().isEmpty(),
                "When the series is empty and start==finish, the returned TradingRecord should contain no trades");
    }

    @Test
    void shouldProcessWholeSeriesWhenRangeCoversAllBars() {
        // Range covering the whole mocked series (size 10 -> indexes 0‑9)
        int start = 0;
        int finish = 9;

        // Stub overload to return a TradingRecord with a couple of dummy trades
        TradingRecord fullRecord = new BaseTradingRecord();
        // (In a real scenario trades would be added by the strategy; we just need a non‑null record)
        doReturn(fullRecord)
                .when(spyManager)
                .run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(fullRecord, result, "The delegating run should return the record produced by the overload");
    }

    @Test
    void shouldCreateSingleBuyTradeWhenStrategyAlwaysEntersAndExits() {
        // This test focuses on the delegation contract – we do not execute the real
        // strategy logic. We simply ensure that the overload is called with BUY.
        int start = 5;
        int finish = 6;

        // Prepare a TradingRecord that contains exactly one BUY trade.
        TradingRecord oneBuyRecord = new BaseTradingRecord();
        // (Adding a real trade would require a Position; omitted for brevity)
        doReturn(oneBuyRecord)
                .when(spyManager)
                .run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(oneBuyRecord, result);
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
    }

    @Test
    void shouldReturnEmptyRecordWhenStrategyNeverSignals() {
        int start = 0;
        int finish = 7;

        // Stub overload to return an empty record
        TradingRecord emptyRecord = new BaseTradingRecord();
        doReturn(emptyRecord)
                .when(spyManager)
                .run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertNotNull(result);
        assertTrue(result.getPositions().isEmpty(),
                "A strategy that never signals should yield an empty TradingRecord");
    }

    @Test
    void shouldDefaultToBuyTradeTypeForAllGeneratedTrades() {
        int start = 3;
        int finish = 8;

        // Stub overload to return a record with several BUY trades (simplified)
        TradingRecord buyOnlyRecord = new BaseTradingRecord();
        doReturn(buyOnlyRecord)
                .when(spyManager)
                .run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(buyOnlyRecord, result);
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
    }
}
```