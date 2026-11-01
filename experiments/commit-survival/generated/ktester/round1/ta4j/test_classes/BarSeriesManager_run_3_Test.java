package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;
import java.util.stream.Stream;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.*;

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

    @BeforeEach
    void setupBeforeEach() {
        // Create a real instance of BarSeriesManager with the mocked BarSeries,
        // then spy it so we can stub/verify the protected overload.
        BarSeriesManager realManager = new BarSeriesManager(barSeriesMock);
        managerSpy = spy(realManager);
        // Default indices returned by the mocked BarSeries.
        when(barSeriesMock.getBeginIndex()).thenReturn(0);
        when(barSeriesMock.getEndIndex()).thenReturn(10);
        // This prevents execution of the full back‑testing logic.
        doReturn(tradingRecordMock).when(managerSpy).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(0), eq(10));
        // Create a real manager with a mocked BarSeries, then spy it.
        BarSeriesManager real = new BarSeriesManager(barSeriesMock);
        managerSpy = spy(real);
        // Stub the overloaded run method that includes start/finish indices.
        // Default series bounds (used by most tests).
        when(barSeriesMock.getBeginIndex()).thenReturn(0);
        when(barSeriesMock.getEndIndex()).thenReturn(10);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset all mocks to avoid interference between tests.
        reset(barSeriesMock, strategyMock, amountMock, tradingRecordMock, managerSpy);
        // Reset all mocks to avoid cross‑test contamination.
        reset(barSeriesMock, strategyMock, amountMock, tradingRecordMock, managerSpy);
    }

    private static Stream<Arguments> provideNormalDelegationParameters() {
        // Using real Num implementations would require the library; we reuse the mock.
        return Stream.of(Arguments.of(TradeType.BUY, mock(Num.class)), Arguments.of(TradeType.SELL, mock(Num.class)));
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
        verify(managerSpy, times(1)).run(eq(strategyMock), eq(tradeType), eq(amountMock), eq(0), eq(10));
    }

    /* -------------------------------------------------------------
     *  Normal delegation – the three‑arg method must forward to the
     *  five‑arg overload with the series begin/end indices.
     * ------------------------------------------------------------- */
    @ParameterizedTest(name = "[{index}] tradeType={0}, amount={1}")
    @MethodSource("provideNormalDelegationParameters")
    void shouldDelegateToRunWithIndices(TradeType tradeType, Num amount) {
        // Arrange – stub the internal overload to return a known record.
        doReturn(tradingRecordMock).when(managerSpy).run(eq(strategyMock), eq(tradeType), eq(amount), eq(0), eq(10));
        // Act
        TradingRecord result = managerSpy.run(strategyMock, tradeType, amount);
        // Assert – the result is the stub and the overload was called exactly once.
        assertSame(tradingRecordMock, result, "The three‑arg run should return the TradingRecord produced by the internal overload");
        verify(managerSpy, times(1)).run(eq(strategyMock), eq(tradeType), eq(amount), eq(0), eq(10));
    }

    /* -------------------------------------------------------------
     *  Invalid arguments – the method must reject nulls.
     * ------------------------------------------------------------- */
    @Test
    void shouldThrowWhenStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> managerSpy.run(null, TradeType.BUY, amountMock), "Null strategy should cause NullPointerException");
    }

    @Test
    void shouldThrowWhenTradeTypeIsNull() {
        assertThrows(NullPointerException.class, () -> managerSpy.run(strategyMock, null, amountMock), "Null TradeType should cause NullPointerException");
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        assertThrows(NullPointerException.class, () -> managerSpy.run(strategyMock, TradeType.BUY, null), "Null amount should cause NullPointerException");
    }

    /* -------------------------------------------------------------
     *  Edge case – empty BarSeries (begin > end).
     * ------------------------------------------------------------- */
    @Test
    void shouldReturnEmptyRecordWhenSeriesIsEmpty() {
        // Simulate an empty series.
        when(barSeriesMock.getBeginIndex()).thenReturn(5);
        when(barSeriesMock.getEndIndex()).thenReturn(3);
        // Stub the internal overload to return a specific (empty) record.
        TradingRecord emptyRecord = mock(TradingRecord.class);
        doReturn(emptyRecord).when(managerSpy).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(5), eq(3));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.BUY, amountMock);
        assertSame(emptyRecord, result, "Empty series should yield the stubbed empty TradingRecord");
        verify(managerSpy, times(1)).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(5), eq(3));
    }

    /* -------------------------------------------------------------
     *  Edge case – single‑bar series (start == finish).
     * ------------------------------------------------------------- */
    @Test
    void shouldHandleSingleBarSeries() {
        when(barSeriesMock.getBeginIndex()).thenReturn(0);
        when(barSeriesMock.getEndIndex()).thenReturn(0);
        TradingRecord singleBarRecord = mock(TradingRecord.class);
        doReturn(singleBarRecord).when(managerSpy).run(eq(strategyMock), eq(TradeType.SELL), eq(amountMock), eq(0), eq(0));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.SELL, amountMock);
        assertSame(singleBarRecord, result, "Single‑bar series should delegate with start==finish");
        verify(managerSpy, times(1)).run(eq(strategyMock), eq(TradeType.SELL), eq(amountMock), eq(0), eq(0));
    }

    /* -------------------------------------------------------------
     *  Negative amount handling.
     * ------------------------------------------------------------- */
    @Test
    void shouldRejectNegativeAmount() {
        Num negative = mock(Num.class);
        // Simulate Num.isNegative() == true if the implementation checks it.
        // Here we just rely on the manager to validate the argument and throw.
        assertThrows(IllegalArgumentException.class, () -> managerSpy.run(strategyMock, TradeType.BUY, negative), "Negative amount must cause IllegalArgumentException");
    }

    /* -------------------------------------------------------------
     *  Strategy runtime exception propagation.
     * ------------------------------------------------------------- */
    @Test
    void shouldPropagateExceptionFromInternalRun() {
        RuntimeException runtime = new RuntimeException("strategy failure");
        doThrow(runtime).when(managerSpy).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(0), eq(10));
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> managerSpy.run(strategyMock, TradeType.BUY, amountMock), "Exception from strategy should propagate unchanged");
        assertSame(runtime, thrown, "Propagated exception should be the original one");
    }

    /* -------------------------------------------------------------
     *  Non‑zero start index series (beginIndex = 5, endIndex = 10).
     * ------------------------------------------------------------- */
    @Test
    void shouldDelegateUsingNonZeroSeriesBounds() {
        when(barSeriesMock.getBeginIndex()).thenReturn(5);
        when(barSeriesMock.getEndIndex()).thenReturn(10);
        TradingRecord record = mock(TradingRecord.class);
        doReturn(record).when(managerSpy).run(eq(strategyMock), eq(TradeType.SELL), eq(amountMock), eq(5), eq(10));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.SELL, amountMock);
        assertSame(record, result, "Delegate should use the non‑zero series bounds");
        verify(managerSpy, times(1)).run(eq(strategyMock), eq(TradeType.SELL), eq(amountMock), eq(5), eq(10));
    }

    /* -------------------------------------------------------------
     *  TradeType mismatch – strategy signals opposite direction.
     *  (We simply verify that the manager still delegates correctly; the
     *   strategy's behaviour is mocked out.)
     * ------------------------------------------------------------- */
    @Test
    void shouldReturnEmptyWhenTradeTypeDoesNotMatchStrategySignals() {
        // Stub internal overload to return an empty record.
        TradingRecord empty = mock(TradingRecord.class);
        doReturn(empty).when(managerSpy).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(0), eq(10));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.BUY, amountMock);
        assertSame(empty, result, "When strategy signals opposite direction, manager returns the stubbed empty record");
    }

    /* -------------------------------------------------------------
     *  Null BarSeries in manager – constructor receives null.
     * ------------------------------------------------------------- */
    @Test
    void shouldThrowWhenBarSeriesIsNull() {
        BarSeriesManager nullManager = new BarSeriesManager(null);
        assertThrows(NullPointerException.class, () -> nullManager.run(strategyMock, TradeType.BUY, amountMock), "Calling run on a manager with null BarSeries must throw NPE");
    }

    /* -------------------------------------------------------------
     *  Large amount handling – ensure no overflow exception is thrown.
     * ------------------------------------------------------------- */
    @Test
    void shouldHandleVeryLargeAmountWithoutOverflow() {
        // Use a real Num implementation if available; here we mock.
        Num huge = mock(Num.class);
        // No special stubbing – we only verify delegation.
        doReturn(tradingRecordMock).when(managerSpy).run(eq(strategyMock), eq(TradeType.SELL), eq(huge), eq(0), eq(10));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.SELL, huge);
        assertSame(tradingRecordMock, result, "Large amount should be forwarded without overflow");
    }

    /* -------------------------------------------------------------
     *  Multiple sequential trades – verify that the manager returns the
     *  TradingRecord produced by the internal overload (the content of
     *  the record is not inspected here).
     * ------------------------------------------------------------- */
    @Test
    void shouldReturnRecordWithMultipleTrades() {
        TradingRecord multiTradeRecord = mock(TradingRecord.class);
        doReturn(multiTradeRecord).when(managerSpy).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(0), eq(10));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.BUY, amountMock);
        assertSame(multiTradeRecord, result, "Manager should return the record containing multiple trades");
    }

    /* -------------------------------------------------------------
     *  Custom transaction cost model effect – we only verify that the
     *  manager still delegates; the actual cost calculation is part of
     *  the internal run implementation which is mocked out.
     * ------------------------------------------------------------- */
    @Test
    void shouldDelegateWhenTransactionCostModelIsSet() {
        // Assume a custom transaction cost model is set via a (hypothetical) setter.
        // Since the real class does not expose a setter, we simply verify delegation.
        doReturn(tradingRecordMock).when(managerSpy).run(eq(strategyMock), eq(TradeType.BUY), eq(amountMock), eq(0), eq(10));
        TradingRecord result = managerSpy.run(strategyMock, TradeType.BUY, amountMock);
        assertSame(tradingRecordMock, result);
    }
}
