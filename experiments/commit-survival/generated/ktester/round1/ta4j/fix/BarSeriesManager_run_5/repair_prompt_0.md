
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
import org.mockito.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;

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

    // run method without invoking the real implementation.
    private BarSeriesManager manager;

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the spy with the constructor that only receives a BarSeries.
        manager = Mockito.spy(new BarSeriesManager(barSeries));
        // Stub BarSeries.one() to return a deterministic Num instance.
        when(barSeries.one()).thenReturn(amount);
        // Stub the overloaded run(...) that the method under test should delegate to.
        // Use any() for the TradeType argument because we will verify the exact call later.
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
        // Create a spy that calls the real constructor but allows us to stub methods.
        manager = Mockito.spy(new BarSeriesManager(barSeries));
        // Default behaviour: barSeries.one() returns a deterministic Num instance.
        when(barSeries.one()).thenReturn(amount);
        // Assume a series size of 30 bars for bounds‑checking tests.
        when(barSeries.getBarCount()).thenReturn(30);
        // “happy‑path” tests.
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
    }

    @Test
    void shouldDelegateToOverloadedRunWithBarSeriesOneAmount() {
        // GIVEN
        // typical enum value; adjust if different
        TradeType tradeType = TradeType.BUY;
        int startIndex = 0;
        int finishIndex = 10;
        // WHEN
        TradingRecord result = manager.run(strategy, tradeType, startIndex, finishIndex);
        // THEN
        assertSame(tradingRecord, result, "The run method should return the TradingRecord produced by the overloaded method.");
        // Verify that the overloaded method was invoked exactly once with the expected arguments.
        verify(manager, times(1)).run(strategy, tradeType, amount, startIndex, finishIndex);
    }

    @ParameterizedTest(name = "[{index}] tradeType={0}, start={1}, finish={2}")
    @CsvSource({ "BUY, 0, 10", "SELL, 5, 15", "BUY, 3, 3" })
    void shouldDelegateToOverloadedRunWhenArgumentsAreValid(TradeType tradeType, int startIndex, int finishIndex) {
        // WHEN
        TradingRecord result = manager.run(strategy, tradeType, startIndex, finishIndex);
        // THEN
        assertSame(tradingRecord, result, "The method should return the TradingRecord produced by the overloaded call.");
        verify(manager, times(1)).run(strategy, tradeType, amount, startIndex, finishIndex);
    }

    @Test
    void shouldPassExactAmountReturnedByBarSeriesOne() {
        // Arrange – a concrete Num mock that we can capture.
        Num specificAmount = mock(Num.class);
        when(barSeries.one()).thenReturn(specificAmount);
        // Capture the Num argument passed to the overloaded method.
        ArgumentCaptor<Num> amountCaptor = ArgumentCaptor.forClass(Num.class);
        when(manager.run(eq(strategy), any(TradeType.class), eq(specificAmount), anyInt(), anyInt())).thenReturn(tradingRecord);
        // WHEN
        TradingRecord result = manager.run(strategy, TradeType.BUY, 2, 5);
        // THEN
        assertSame(tradingRecord, result);
        verify(manager).run(eq(strategy), eq(TradeType.BUY), amountCaptor.capture(), eq(2), eq(5));
        assertSame(specificAmount, amountCaptor.getValue(), "The amount passed to the overload must be exactly the value returned by barSeries.one().");
    }

    @Test
    void shouldThrowWhenStartIndexIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, -1, 5), "Negative startIndex must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenFinishIndexIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.SELL, 0, -2), "Negative finishIndex must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenStartGreaterThanFinish() {
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.SELL, 8, 7), "startIndex > finishIndex must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenFinishIndexBeyondSeriesSize() {
        // barSeries.getBarCount() is stubbed to 30 in setUp()
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, 0, 1000), "finishIndex beyond series size must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenStartIndexBeyondSeriesSize() {
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, 50, 55), "startIndex beyond series size must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> manager.run(null, TradeType.BUY, 0, 5), "Null strategy must trigger NullPointerException before delegation.");
    }

    @Test
    void shouldThrowWhenTradeTypeIsNull() {
        assertThrows(NullPointerException.class, () -> manager.run(strategy, null, 0, 5), "Null tradeType must trigger NullPointerException before delegation.");
    }

    @Test
    void shouldThrowWhenBarSeriesIsNull() {
        // Construct a manager with a null BarSeries (no spying needed).
        BarSeriesManager nullSeriesManager = new BarSeriesManager(null);
        assertThrows(NullPointerException.class, () -> nullSeriesManager.run(strategy, TradeType.BUY, 0, 5), "Calling run on a manager built with null BarSeries must NPE.");
    }

    @Test
    void shouldThrowWhenBarSeriesOneReturnsNull() {
        // barSeries.one() returns null instead of a Num.
        when(barSeries.one()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> manager.run(strategy, TradeType.BUY, 1, 4), "Null Num from barSeries.one() must propagate as NullPointerException.");
    }

    @Test
    void overloadShouldNotBeCalledWhenArgumentsAreInvalid() {
        // Invalid range – expect exception before delegation.
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, -1, 5));
        // Verify that the overloaded 5‑argument method was never called.
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:4: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:11: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:12: error: package org.mockito does not exist
import static org.mockito.Mockito.*;
                         ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:13: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:14: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.CsvSource;
                                        ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:15: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:16: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:18: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.CsvSource;
                                        ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:19: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:23: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:5: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:20: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:21: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:23: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:26: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:29: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:32: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:35: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:41: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:75: error: cannot find symbol
    @ParameterizedTest(name = "[{index}] tradeType={0}, start={1}, finish={2}")
     ^
  symbol:   class ParameterizedTest
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:76: error: cannot find symbol
    @CsvSource({ "BUY, 0, 10", "SELL, 5, 15", "BUY, 3, 3" })
     ^
  symbol:   class CsvSource
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:44: error: cannot find symbol
        manager = Mockito.spy(new BarSeriesManager(barSeries));
                  ^
  symbol:   variable Mockito
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:46: error: cannot find symbol
        when(barSeries.one()).thenReturn(amount);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:49: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                         ^
  symbol:   method eq(Strategy)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:49: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                       ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:49: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                             ^
  symbol:   method eq(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:49: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                                         ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:49: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                                                   ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:51: error: cannot find symbol
        manager = Mockito.spy(new BarSeriesManager(barSeries));
                  ^
  symbol:   variable Mockito
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:53: error: cannot find symbol
        when(barSeries.one()).thenReturn(amount);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:55: error: cannot find symbol
        when(barSeries.getBarCount()).thenReturn(30);
        ^
  symbol:   method when(int)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:57: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                         ^
  symbol:   method eq(Strategy)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:57: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                       ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:57: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                             ^
  symbol:   method eq(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:57: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                                         ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:57: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(amount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                                                   ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:70: error: cannot find symbol
        assertSame(tradingRecord, result, "The run method should return the TradingRecord produced by the overloaded method.");
        ^
  symbol:   method assertSame(TradingRecord,TradingRecord,String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:72: error: cannot find symbol
        verify(manager, times(1)).run(strategy, tradeType, amount, startIndex, finishIndex);
                        ^
  symbol:   method times(int)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:81: error: cannot find symbol
        assertSame(tradingRecord, result, "The method should return the TradingRecord produced by the overloaded call.");
        ^
  symbol:   method assertSame(TradingRecord,TradingRecord,String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:82: error: cannot find symbol
        verify(manager, times(1)).run(strategy, tradeType, amount, startIndex, finishIndex);
                        ^
  symbol:   method times(int)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:88: error: cannot find symbol
        Num specificAmount = mock(Num.class);
                             ^
  symbol:   method mock(Class<Num>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:89: error: cannot find symbol
        when(barSeries.one()).thenReturn(specificAmount);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:91: error: cannot find symbol
        ArgumentCaptor<Num> amountCaptor = ArgumentCaptor.forClass(Num.class);
        ^
  symbol:   class ArgumentCaptor
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:91: error: cannot find symbol
        ArgumentCaptor<Num> amountCaptor = ArgumentCaptor.forClass(Num.class);
                                           ^
  symbol:   variable ArgumentCaptor
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:92: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(specificAmount), anyInt(), anyInt())).thenReturn(tradingRecord);
                         ^
  symbol:   method eq(Strategy)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:92: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(specificAmount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                       ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:92: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(specificAmount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                             ^
  symbol:   method eq(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:92: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(specificAmount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                                                 ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:92: error: cannot find symbol
        when(manager.run(eq(strategy), any(TradeType.class), eq(specificAmount), anyInt(), anyInt())).thenReturn(tradingRecord);
                                                                                           ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:96: error: cannot find symbol
        assertSame(tradingRecord, result);
        ^
  symbol:   method assertSame(TradingRecord,TradingRecord)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:97: error: cannot find symbol
        verify(manager).run(eq(strategy), eq(TradeType.BUY), amountCaptor.capture(), eq(2), eq(5));
        ^
  symbol:   method verify(BarSeriesManager)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:97: error: cannot find symbol
        verify(manager).run(eq(strategy), eq(TradeType.BUY), amountCaptor.capture(), eq(2), eq(5));
                            ^
  symbol:   method eq(Strategy)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:97: error: cannot find symbol
        verify(manager).run(eq(strategy), eq(TradeType.BUY), amountCaptor.capture(), eq(2), eq(5));
                                          ^
  symbol:   method eq(TradeType)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:97: error: cannot find symbol
        verify(manager).run(eq(strategy), eq(TradeType.BUY), amountCaptor.capture(), eq(2), eq(5));
                                                                                     ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:97: error: cannot find symbol
        verify(manager).run(eq(strategy), eq(TradeType.BUY), amountCaptor.capture(), eq(2), eq(5));
                                                                                            ^
  symbol:   method eq(int)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:103: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, -1, 5), "Negative startIndex must trigger IllegalArgumentException.");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->manage[...]1, 5),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:108: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.SELL, 0, -2), "Negative finishIndex must trigger IllegalArgumentException.");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->manage[...], -2),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:113: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.SELL, 8, 7), "startIndex > finishIndex must trigger IllegalArgumentException.");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->manage[...]8, 7),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:119: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, 0, 1000), "finishIndex beyond series size must trigger IllegalArgumentException.");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->manage[...]1000),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:124: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, 50, 55), "startIndex beyond series size must trigger IllegalArgumentException.");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->manage[...], 55),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:129: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> manager.run(null, TradeType.BUY, 0, 5), "Null strategy must trigger NullPointerException before delegation.");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->manage[...]0, 5),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:134: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> manager.run(strategy, null, 0, 5), "Null tradeType must trigger NullPointerException before delegation.");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->manage[...]0, 5),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:141: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> nullSeriesManager.run(strategy, TradeType.BUY, 0, 5), "Calling run on a manager built with null BarSeries must NPE.");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->nullSe[...]0, 5),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:147: error: cannot find symbol
        when(barSeries.one()).thenReturn(null);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:148: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> manager.run(strategy, TradeType.BUY, 1, 4), "Null Num from barSeries.one() must propagate as NullPointerException.");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->manage[...]1, 4),String)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:154: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> manager.run(strategy, TradeType.BUY, -1, 5));
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->manage[...]1, 5))
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:156: error: cannot find symbol
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                        ^
  symbol:   method never()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:156: error: cannot find symbol
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                     ^
  symbol:   method any(Class<Strategy>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:156: error: cannot find symbol
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                          ^
  symbol:   method any(Class<TradeType>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:156: error: cannot find symbol
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                ^
  symbol:   method any(Class<Num>)
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:156: error: cannot find symbol
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_5_Test.java:156: error: cannot find symbol
        verify(manager, never()).run(any(Strategy.class), any(TradeType.class), any(Num.class), anyInt(), anyInt());
                                                                                                          ^
  symbol:   method anyInt()
  location: class BarSeriesManager_run_5_Test
73 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.backtest.BarSeriesManager': '\n * A manager for {@link BarSeries} objects used for backtesting. Allows to run a\n * {@link Strategy trading strategy} over the managed bar series.\n ', 'api document of method run(Strategy, TradeType, int, int)': '\n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a trade of {@link TradeType tradeType}.\n     *\n     * @param strategy    the trading strategy\n     * @param tradeType   the {@link TradeType} used to open the position\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ', 'return type': 'org.ta4j.core.TradingRecord', 'dependent classes': 'class `org.ta4j.core.Strategy`:\n\t\nclass `org.ta4j.core.Trade.TradeType`:\n\t\nclass `org.ta4j.core.backtest.BarSeriesManager`:\n\tdependent fields:\n\t\torg.ta4j.core.BarSeries org.ta4j.core.backtest.BarSeriesManager.barSeries;\n\trelated functions:\n\t\tmethod `public TradingRecord run(Strategy, TradeType)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`, api document: \n     * Runs the provided strategy over the managed series.\n     *\n     * Opens the position with a trade of {@link TradeType tradeType}.\n     *\n     * @param strategy  the trading strategy\n     * @param tradeType the {@link TradeType} used to open the position\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public BarSeries getBarSeries()` returns `org.ta4j.core.BarSeries`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`, api document: \n     * @return the managed bar series\n     ;\n\t\tmethod `public TradingRecord run(Strategy, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a {@link TradeType} BUY trade.\n     *\n     * @param strategy    the trading strategy\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public TradingRecord run(Strategy, TradeType, Num)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`, api document: \n     * Runs the provided strategy over the managed series.\n     *\n     * @param strategy  the trading strategy\n     * @param tradeType the {@link TradeType} used to open the position\n     * @param amount    the amount used to open/close the trades\n     * @return the trading record coming from the run\n     ;\nclass `org.ta4j.core.criteria.NumberOfPositionsCriterion`:\n\trelated functions:\n\t\tmethod `public Num calculate(BarSeries, Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`;\nclass `org.ta4j.core.Indicator`:\n\trelated functions:\n\t\tmethod ` Num one()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`, api document: \n     * @return the Num of 1\n     ;\nclass `org.ta4j.core.criteria.NumberOfWinningPositionsCriterion`:\n\trelated functions:\n\t\tmethod `public Num calculate(BarSeries, Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, int, int)`;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


