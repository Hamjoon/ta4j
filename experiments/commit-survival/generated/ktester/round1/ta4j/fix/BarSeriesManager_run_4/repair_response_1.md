```java
package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link BarSeriesManager#run(Strategy, int, int)} – the delegating
 * overload that opens a BUY trade with the series' {@code one()} amount.
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

    /** default indices used by a few tests */
    private static final int DEFAULT_START = 0;
    private static final int DEFAULT_FINISH = 9;

    /**
     * Minimal {@link Strategy} implementation that never signals.
     */
    private static class NoSignalStrategy implements Strategy {

        @Override
        public boolean shouldEnter(int index, BarSeries series) {
            return false;
        }

        @Override
        public boolean shouldExit(int index, BarSeries series) {
            return false;
        }

        @Override
        public String getName() {
            return "NoSignalStrategy";
        }
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the manager with the mocked BarSeries.
        manager = new BarSeriesManager(mockBarSeries);
        // Create a spy so we can verify the internal delegation without invoking the real logic.
        spyManager = spy(manager);

        // Stubs required by the delegating method.
        when(mockBarSeries.one()).thenReturn(mockNum);
        when(mockBarSeries.getBarCount()).thenReturn(10);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mock interactions after each test.
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
    }

    /** Helper stream for parameterised tests (not used directly in this file but kept for completeness). */
    private static Stream<Arguments> successfulCases() {
        return Stream.of(
                Arguments.of(0, 10, "happy‑path"),
                Arguments.of(5, 5, "empty‑range")
        );
    }

    /* --------------------------------------------------------------------- *
     *  Static “main‑style” sanity checks – they are not part of the JUnit run
     * --------------------------------------------------------------------- */
    public static void main(String[] args) {
        // Enable assertions when running from the command line:
        //   java -ea org.ta4j.core.backtest.BarSeriesManager_run_4_Test
        testDelegateToOverloadedRunWithBuyAndOneNum();
        testIllegalArgumentWhenStartGreaterThanFinish();
        testIllegalArgumentWhenStartIsNegative();
        testNullPointerWhenStrategyIsNull();
        testIndexOutOfBoundsWhenStartExceedsSeriesSize();
        testIndexOutOfBoundsWhenFinishExceedsSeriesSize();
        testIllegalArgumentWhenFinishIsNegative();
        testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero();
        testWholeSeriesProcessing();
        System.out.println("All tests passed.");
    }

    private static void testDelegateToOverloadedRunWithBuyAndOneNum() {
        BarSeries series = new BaseBarSeriesBuilder().withName("empty").build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        TradingRecord record = manager.run(strategy, 0, 0);
        assert record != null : "run should never return null";
        assert record.getPositions().isEmpty() : "Empty series + no‑signal strategy must yield no positions";
    }

    private static void testIllegalArgumentWhenStartGreaterThanFinish() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        boolean thrown = false;
        try {
            manager.run(strategy, 8, 3);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assert thrown : "startIndex > finishIndex must throw IllegalArgumentException";
    }

    private static void testIllegalArgumentWhenStartIsNegative() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        boolean thrown = false;
        try {
            manager.run(strategy, -1, 4);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assert thrown : "Negative startIndex must throw IllegalArgumentException";
    }

    private static void testNullPointerWhenStrategyIsNull() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumOfBars(5)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        boolean thrown = false;
        try {
            manager.run(null, 0, 4);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assert thrown : "Null strategy must throw NullPointerException";
    }

    private static void testIndexOutOfBoundsWhenStartExceedsSeriesSize() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        boolean thrown = false;
        try {
            manager.run(strategy, 1000, 1010);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        assert thrown : "startIndex beyond series size must throw IndexOutOfBoundsException";
    }

    private static void testIndexOutOfBoundsWhenFinishExceedsSeriesSize() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        boolean thrown = false;
        try {
            manager.run(strategy, 0, 1000);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        assert thrown : "finishIndex beyond series size must throw IndexOutOfBoundsException";
    }

    private static void testIllegalArgumentWhenFinishIsNegative() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        boolean thrown = false;
        try {
            manager.run(strategy, 2, -1);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assert thrown : "Negative finishIndex must throw IllegalArgumentException";
    }

    private static void testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("empty")
                .withNumOfBars(0)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        TradingRecord record = manager.run(strategy, 0, 0);
        assert record != null : "Result must not be null";
        assert record.getPositions().isEmpty() : "Empty series with zero range must yield empty record";
    }

    private static void testWholeSeriesProcessing() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("full")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        TradingRecord record = manager.run(strategy, 0, 9);
        assert record != null : "Result must not be null when processing the whole series";
        assert record.getPositions().isEmpty() : "No‑signal strategy must produce an empty record";
    }

    /* --------------------------------------------------------------------- *
     *                         JUnit 5 test methods
     * --------------------------------------------------------------------- */

    @Test
    void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
        // Arrange – stub the overloaded run to return a known TradingRecord
        doReturn(mockTradingRecord)
                .when(spyManager)
                .run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());

        // Act
        TradingRecord result = spyManager.run(mockStrategy, DEFAULT_START, DEFAULT_FINISH);

        // Assert
        assertSame(mockTradingRecord, result,
                "The delegating run method must return the TradingRecord produced by the overloaded variant.");

        verify(spyManager, times(1))
                .run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(DEFAULT_START), eq(DEFAULT_FINISH));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        int start = 8;
        int finish = 3;
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
        int start = 0;
        int finish = 9;
        TradingRecord fullRecord = new BaseTradingRecord();
        doReturn(fullRecord)
                .when(spyManager)
                .run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(fullRecord, result,
                "The delegating run should return the record produced by the overload");
    }

    @Test
    void shouldCreateSingleBuyTradeWhenStrategyAlwaysEntersAndExits() {
        int start = 5;
        int finish = 6;
        TradingRecord oneBuyRecord = new BaseTradingRecord();
        doReturn(oneBuyRecord)
                .when(spyManager)
                .run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(oneBuyRecord, result);
        verify(spyManager, times(1))
                .run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
    }

    @Test
    void shouldReturnEmptyRecordWhenStrategyNeverSignals() {
        int start = 0;
        int finish = 7;
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
        TradingRecord buyOnlyRecord = new BaseTradingRecord();
        doReturn(buyOnlyRecord)
                .when(spyManager)
                .run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));

        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(buyOnlyRecord, result);
        verify(spyManager, times(1))
                .run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
    }
}
```