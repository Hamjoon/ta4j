
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
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
import static org.mockito.ArgumentMatchers.*;
import java.util.stream.Stream;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;
import org.ta4j.core.BaseTradingRecord;
import org.junit.jupiter.api.*;

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
        when(spyManager.run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt())).thenReturn(mockTradingRecord);
        // manager uses the mocked BarSeries
        manager = new BarSeriesManager(mockBarSeries);
        spyManager = spy(manager);
        // BarSeries.one() is required by the delegating method
        when(mockBarSeries.one()).thenReturn(mockNum);
        // The series size is needed for range‑validation tests
        when(mockBarSeries.getBarCount()).thenReturn(10);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks after each test to avoid interactions bleeding between tests.
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
        // clean mock interactions between tests
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
    }

    private static Stream<Arguments> successfulCases() {
        return Stream.of(// happy‑path: normal range
        Arguments.of(0, 10, "happy‑path"), // edge: start == finish (empty processing)
        Arguments.of(5, 5, "empty‑range"));
    }

    @Test
    void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
        // Arrange – stub the overloaded run to return a known TradingRecord
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        // Act
        TradingRecord result = spyManager.run(mockStrategy, startIndex, finishIndex);
        // Assert – the result must be the stubbed record
        assertSame(mockTradingRecord, result, "The delegating run method must return the TradingRecord produced by the overloaded variant.");
        // Verify that the overload received the exact arguments we expect
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        int start = 8;
        int finish = 3;
        // No stubbing – let the real overload perform validation
        assertThrows(IllegalArgumentException.class, () -> spyManager.run(mockStrategy, start, finish), "startIndex > finishIndex must trigger IllegalArgumentException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartIsNegative() {
        int start = -1;
        int finish = 4;
        assertThrows(IllegalArgumentException.class, () -> spyManager.run(mockStrategy, start, finish), "Negative startIndex must trigger IllegalArgumentException");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenStrategyIsNull() {
        int start = 0;
        int finish = 5;
        assertThrows(NullPointerException.class, () -> spyManager.run(null, start, finish), "Null strategy must trigger NullPointerException");
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenStartExceedsSeriesSize() {
        int start = 1000;
        int finish = 1010;
        assertThrows(IndexOutOfBoundsException.class, () -> spyManager.run(mockStrategy, start, finish), "startIndex beyond series size must trigger IndexOutOfBoundsException");
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenFinishExceedsSeriesSize() {
        int start = 0;
        int finish = 1000;
        assertThrows(IndexOutOfBoundsException.class, () -> spyManager.run(mockStrategy, start, finish), "finishIndex beyond series size must trigger IndexOutOfBoundsException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFinishIsNegative() {
        int start = 2;
        int finish = -1;
        assertThrows(IllegalArgumentException.class, () -> spyManager.run(mockStrategy, start, finish), "Negative finishIndex must trigger IllegalArgumentException");
    }

    @Test
    void shouldReturnEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        // Simulate an empty series (size 0)
        when(mockBarSeries.getBarCount()).thenReturn(0);
        // Stub overload to return a concrete (non‑mock) empty TradingRecord
        TradingRecord emptyRecord = new BaseTradingRecord();
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        TradingRecord result = spyManager.run(mockStrategy, 0, 0);
        assertNotNull(result, "Result must not be null");
        assertTrue(result.getPositions().isEmpty(), "When the series is empty and start==finish, the returned TradingRecord should contain no trades");
    }

    @Test
    void shouldProcessWholeSeriesWhenRangeCoversAllBars() {
        // Range covering the whole mocked series (size 10 -> indexes 0‑9)
        int start = 0;
        int finish = 9;
        // Stub overload to return a TradingRecord with a couple of dummy trades
        TradingRecord fullRecord = new BaseTradingRecord();
        // (In a real scenario trades would be added by the strategy; we just need a non‑null record)
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
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
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
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
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertNotNull(result);
        assertTrue(result.getPositions().isEmpty(), "A strategy that never signals should yield an empty TradingRecord");
    }

    @Test
    void shouldDefaultToBuyTradeTypeForAllGeneratedTrades() {
        int start = 3;
        int finish = 8;
        // Stub overload to return a record with several BUY trades (simplified)
        TradingRecord buyOnlyRecord = new BaseTradingRecord();
        doReturn(buyOnlyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
        TradingRecord result = spyManager.run(mockStrategy, start, finish);
        assertSame(buyOnlyRecord, result);
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:4: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:5: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:7: error: package org.mockito does not exist
import static org.mockito.Mockito.*;
                         ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:13: error: package org.mockito does not exist
import static org.mockito.ArgumentMatchers.*;
                         ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:17: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:18: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:21: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:22: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.Arguments;
                                        ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:26: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:73: error: cannot find symbol
    private static Stream<Arguments> successfulCases() {
                          ^
  symbol:   class Arguments
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:6: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:15: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:16: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:20: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:24: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:26: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:29: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:32: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:35: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:38: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:45: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:65: error: cannot find symbol
    @AfterEach
     ^
  symbol:   class AfterEach
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:50: error: cannot find symbol
        spyManager = spy(manager);
                     ^
  symbol:   method spy(BarSeriesManager)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:52: error: cannot find symbol
        when(mockBarSeries.one()).thenReturn(mockNum);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:55: error: cannot find symbol
        when(spyManager.run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt())).thenReturn(mockTradingRecord);
                            ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:55: error: cannot find symbol
        when(spyManager.run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt())).thenReturn(mockTradingRecord);
                                                 ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:55: error: cannot find symbol
        when(spyManager.run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt())).thenReturn(mockTradingRecord);
                                                                       ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:55: error: cannot find symbol
        when(spyManager.run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt())).thenReturn(mockTradingRecord);
                                                                                       ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:55: error: cannot find symbol
        when(spyManager.run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt())).thenReturn(mockTradingRecord);
                                                                                                 ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:58: error: cannot find symbol
        spyManager = spy(manager);
                     ^
  symbol:   method spy(BarSeriesManager)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:60: error: cannot find symbol
        when(mockBarSeries.one()).thenReturn(mockNum);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:62: error: cannot find symbol
        when(mockBarSeries.getBarCount()).thenReturn(10);
        ^
  symbol:   method when(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:68: error: cannot find symbol
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
        ^
  symbol:   method clearInvocations(BarSeries,Strategy,Num,TradingRecord,BarSeriesManager)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:70: error: cannot find symbol
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
        ^
  symbol:   method clearInvocations(BarSeries,Strategy,Num,TradingRecord,BarSeriesManager)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:75: error: cannot find symbol
        Arguments.of(0, 10, "happy‑path"), // edge: start == finish (empty processing)
        ^
  symbol:   variable Arguments
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:76: error: cannot find symbol
        Arguments.of(5, 5, "empty‑range"));
        ^
  symbol:   variable Arguments
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:82: error: cannot find symbol
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        ^
  symbol:   method doReturn(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:82: error: cannot find symbol
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                         ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:82: error: cannot find symbol
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                              ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:82: error: cannot find symbol
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                    ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:82: error: cannot find symbol
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                                    ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:82: error: cannot find symbol
        doReturn(mockTradingRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                                              ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:84: error: cannot find symbol
        TradingRecord result = spyManager.run(mockStrategy, startIndex, finishIndex);
                                                            ^
  symbol:   variable startIndex
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:84: error: cannot find symbol
        TradingRecord result = spyManager.run(mockStrategy, startIndex, finishIndex);
                                                                        ^
  symbol:   variable finishIndex
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:86: error: cannot find symbol
        assertSame(mockTradingRecord, result, "The delegating run method must return the TradingRecord produced by the overloaded variant.");
        ^
  symbol:   method assertSame(TradingRecord,TradingRecord,String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:88: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
                           ^
  symbol:   method times(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:88: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
                                         ^
  symbol:   method eq(Strategy)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:88: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
                                                           ^
  symbol:   method eq(TradeType)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:88: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
                                                                              ^
  symbol:   method eq(Num)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:88: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
                                                                                              ^
  symbol:   variable startIndex
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:88: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(startIndex), eq(finishIndex));
                                                                                                              ^
  symbol:   variable finishIndex
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:96: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> spyManager.run(mockStrategy, start, finish), "startIndex > finishIndex must trigger IllegalArgumentException");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->spyMan[...]nish),String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:103: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> spyManager.run(mockStrategy, start, finish), "Negative startIndex must trigger IllegalArgumentException");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->spyMan[...]nish),String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:110: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> spyManager.run(null, start, finish), "Null strategy must trigger NullPointerException");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->spyMan[...]nish),String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:117: error: cannot find symbol
        assertThrows(IndexOutOfBoundsException.class, () -> spyManager.run(mockStrategy, start, finish), "startIndex beyond series size must trigger IndexOutOfBoundsException");
        ^
  symbol:   method assertThrows(Class<IndexOutOfBoundsException>,()->spyMan[...]nish),String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:124: error: cannot find symbol
        assertThrows(IndexOutOfBoundsException.class, () -> spyManager.run(mockStrategy, start, finish), "finishIndex beyond series size must trigger IndexOutOfBoundsException");
        ^
  symbol:   method assertThrows(Class<IndexOutOfBoundsException>,()->spyMan[...]nish),String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:131: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> spyManager.run(mockStrategy, start, finish), "Negative finishIndex must trigger IllegalArgumentException");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->spyMan[...]nish),String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:137: error: cannot find symbol
        when(mockBarSeries.getBarCount()).thenReturn(0);
        ^
  symbol:   method when(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        ^
  symbol:   method doReturn(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                   ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                        ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                              ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                              ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                                        ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:142: error: cannot find symbol
        assertNotNull(result, "Result must not be null");
        ^
  symbol:   method assertNotNull(TradingRecord,String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:143: error: cannot find symbol
        assertTrue(result.getPositions().isEmpty(), "When the series is empty and start==finish, the returned TradingRecord should contain no trades");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        ^
  symbol:   method doReturn(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                  ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                       ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                             ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                             ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        doReturn(fullRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                                       ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:156: error: cannot find symbol
        assertSame(fullRecord, result, "The delegating run should return the record produced by the overload");
        ^
  symbol:   method assertSame(TradingRecord,TradingRecord,String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:168: error: cannot find symbol
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
        ^
  symbol:   method doReturn(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:168: error: cannot find symbol
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                    ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:168: error: cannot find symbol
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                         ^
  symbol:   method eq(TradeType)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:168: error: cannot find symbol
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                                            ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:168: error: cannot find symbol
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                                                            ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:168: error: cannot find symbol
        doReturn(oneBuyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                                                                       ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:170: error: cannot find symbol
        assertSame(oneBuyRecord, result);
        ^
  symbol:   method assertSame(TradingRecord,TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:171: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
                           ^
  symbol:   method times(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:171: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
                                         ^
  symbol:   method eq(Strategy)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:171: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
                                                           ^
  symbol:   method eq(TradeType)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:171: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
                                                                              ^
  symbol:   method eq(Num)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:171: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
                                                                                           ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:171: error: cannot find symbol
        verify(spyManager, times(1)).run(eq(mockStrategy), eq(TradeType.BUY), eq(mockNum), eq(start), eq(finish));
                                                                                                      ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:180: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
        ^
  symbol:   method doReturn(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:180: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                   ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:180: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                        ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:180: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                              ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:180: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                              ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:180: error: cannot find symbol
        doReturn(emptyRecord).when(spyManager).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                                        ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:182: error: cannot find symbol
        assertNotNull(result);
        ^
  symbol:   method assertNotNull(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:183: error: cannot find symbol
        assertTrue(result.getPositions().isEmpty(), "A strategy that never signals should yield an empty TradingRecord");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:192: error: cannot find symbol
        doReturn(buyOnlyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
        ^
  symbol:   method doReturn(TradingRecord)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:192: error: cannot find symbol
        doReturn(buyOnlyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                     ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:192: error: cannot find symbol
        doReturn(buyOnlyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                          ^
  symbol:   method eq(TradeType)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:192: error: cannot find symbol
        doReturn(buyOnlyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                                             ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:192: error: cannot find symbol
        doReturn(buyOnlyRecord).when(spyManager).run(any(Strategy.class), eq(TradeType.BUY), any(Num.class), eq(start), eq(finish));
                                                                                                             ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_4_Test
100 errors
only showing the first 100 errors, of 108 total; use -Xmaxerrs if you would like to see more

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.backtest.BarSeriesManager': '\n * A manager for {@link BarSeries} objects used for backtesting. Allows to run a\n * {@link Strategy trading strategy} over the managed bar series.\n ', 'api document of method run(Strategy, int, int)': '\n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a {@link TradeType} BUY trade.\n     *\n     * @param strategy    the trading strategy\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ', 'return type': 'org.ta4j.core.TradingRecord', 'dependent classes': 'class `org.ta4j.core.Strategy`:\n\t\nclass `org.ta4j.core.Trade.TradeType`:\n\tdependent fields:\n\t\torg.ta4j.core.Trade.TradeType org.ta4j.core.Trade.TradeType.BUY;\n\t\nclass `org.ta4j.core.backtest.BarSeriesManager`:\n\tdependent fields:\n\t\torg.ta4j.core.BarSeries org.ta4j.core.backtest.BarSeriesManager.barSeries;\n\trelated functions:\n\t\tmethod `public TradingRecord run(Strategy, TradeType, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a trade of {@link TradeType tradeType}.\n     *\n     * @param strategy    the trading strategy\n     * @param tradeType   the {@link TradeType} used to open the position\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public TradingRecord run(Strategy, TradeType, Num)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * Runs the provided strategy over the managed series.\n     *\n     * @param strategy  the trading strategy\n     * @param tradeType the {@link TradeType} used to open the position\n     * @param amount    the amount used to open/close the trades\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public TradingRecord run(Strategy, TradeType)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * Runs the provided strategy over the managed series.\n     *\n     * Opens the position with a trade of {@link TradeType tradeType}.\n     *\n     * @param strategy  the trading strategy\n     * @param tradeType the {@link TradeType} used to open the position\n     * @return the trading record coming from the run\n     ;\nclass `org.ta4j.core.criteria.NumberOfPositionsCriterion`:\n\trelated functions:\n\t\tmethod `public Num calculate(BarSeries, Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`;\nclass `org.ta4j.core.Indicator`:\n\trelated functions:\n\t\tmethod ` Num one()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * @return the Num of 1\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


