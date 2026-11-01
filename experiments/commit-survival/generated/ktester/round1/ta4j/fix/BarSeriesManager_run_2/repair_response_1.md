```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.util.stream.Stream;

/**
 * Unit tests for {@link BarSeriesManager#run(Strategy, TradeType)} delegating
 * to the overload that takes an amount.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_2_Test {

    /* --------------------------------------------------------------------- *
     * Mocked dependencies
     * --------------------------------------------------------------------- */
    @Mock
    private BarSeries barSeries;

    @Mock
    private Strategy strategy;

    @Mock
    private TradingRecord expectedRecord;

    @Mock
    private Num oneAmount;

    /* --------------------------------------------------------------------- *
     * Object under test (created as a spy)
     * --------------------------------------------------------------------- */
    private BarSeriesManager manager;

    // record returned by the 3‑arg overload
    @Mock
    private TradingRecord delegatedRecord;

    // values returned by BarSeries.one()
    @Mock
    private Num amountOne;

    @Mock
    private Num amountZero;

    @Mock
    private Num amountPositive;

    @Mock
    private Num amountNegative;

    @BeforeEach
    void setupBeforeEach() {
        // Create a spy – the real constructor is used, but we can stub overloads.
        manager = Mockito.spy(new BarSeriesManager(barSeries));
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset interactions after each test to keep tests independent.
        Mockito.reset(barSeries, strategy, expectedRecord, oneAmount, manager);
        Mockito.reset(barSeries, strategy, delegatedRecord, amountOne,
                amountZero, amountPositive, amountNegative, manager);
    }

    private static Stream<Arguments> provideDelegationScenarios() {
        // The actual Num instances will be supplied by the test method.
        return Stream.of(
                Arguments.of(TradeType.BUY, null),
                Arguments.of(TradeType.SELL, null)
        );
    }

    @Test
    void shouldDelegateRunToAmountBasedMethod_andReturnItsResult() {
        // GIVEN
        when(barSeries.one()).thenReturn(oneAmount);
        when(manager.run(strategy, TradeType.BUY, oneAmount)).thenReturn(expectedRecord);
        // WHEN
        TradingRecord actual = manager.run(strategy, TradeType.BUY);
        // THEN
        assertSame(expectedRecord, actual,
                "The run method should return the record from the delegated call");
        verify(barSeries, times(1)).one();
        verify(manager, times(1)).run(strategy, TradeType.BUY, oneAmount);
        verifyNoMoreInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("run(null, BUY) → NullPointerException")
    void run_nullStrategy_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> manager.run(null, TradeType.BUY),
                "A null Strategy must cause NPE before any delegation");
        verifyNoInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("run(mockStrategy, null) → NullPointerException")
    void run_nullTradeType_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> manager.run(strategy, null),
                "A null TradeType must cause NPE before any delegation");
        verifyNoInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("BarSeriesManager constructed with null BarSeries → NPE on run")
    void managerConstructedWithNullBarSeries_throwsNPE() {
        BarSeriesManager nullManager = new BarSeriesManager(null);
        assertThrows(NullPointerException.class,
                () -> nullManager.run(strategy, TradeType.BUY),
                "If the manager is built with a null BarSeries, run() must throw NPE");
    }

    @Test
    @DisplayName("BarSeries.one() returns null → NullPointerException")
    void barSeriesOneReturnsNull_throwsNPE() {
        when(barSeries.one()).thenReturn(null);
        assertThrows(NullPointerException.class,
                () -> manager.run(strategy, TradeType.BUY),
                "A null amount from barSeries.one() must cause NPE");
        verify(barSeries, times(1)).one();
        verifyNoMoreInteractions(barSeries);
        verifyNoInteractions(manager);
    }

    @ParameterizedTest(name = "[{index}] tradeType={0}, amount={1}")
    @MethodSource("provideDelegationScenarios")
    @DisplayName("Delegates to overload with amount from BarSeries.one()")
    void delegatesToThreeArgOverload(TradeType tradeType, Num amount) {
        // Arrange
        when(barSeries.one()).thenReturn(amount);
        when(manager.run(strategy, tradeType, amount)).thenReturn(delegatedRecord);
        // Act
        TradingRecord result = manager.run(strategy, tradeType);
        // Assert
        assertSame(delegatedRecord, result,
                "run(strategy, tradeType) must return the record from the delegated call");
        verify(barSeries, times(1)).one();
        verify(manager, times(1)).run(strategy, tradeType, amount);
        verifyNoMoreInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("Delegation with zero amount (Num.ZERO)")
    void delegationWithZeroAmount() {
        when(barSeries.one()).thenReturn(amountZero);
        when(manager.run(strategy, TradeType.BUY, amountZero)).thenReturn(delegatedRecord);
        TradingRecord actual = manager.run(strategy, TradeType.BUY);
        assertSame(delegatedRecord, actual);
        verify(barSeries).one();
        verify(manager).run(strategy, TradeType.BUY, amountZero);
        verifyNoMoreInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("Delegation with positive amount (e.g., 1000)")
    void delegationWithPositiveAmount() {
        when(barSeries.one()).thenReturn(amountPositive);
        when(manager.run(strategy, TradeType.SELL, amountPositive)).thenReturn(delegatedRecord);
        TradingRecord actual = manager.run(strategy, TradeType.SELL);
        assertSame(delegatedRecord, actual);
        verify(barSeries).one();
        verify(manager).run(strategy, TradeType.SELL, amountPositive);
        verifyNoMoreInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("Delegation with negative amount (e.g., -500)")
    void delegationWithNegativeAmount() {
        when(barSeries.one()).thenReturn(amountNegative);
        when(manager.run(strategy, TradeType.BUY, amountNegative)).thenReturn(delegatedRecord);
        TradingRecord actual = manager.run(strategy, TradeType.BUY);
        assertSame(delegatedRecord, actual);
        verify(barSeries).one();
        verify(manager).run(strategy, TradeType.BUY, amountNegative);
        verifyNoMoreInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("Exception from BarSeries.one() propagates")
    void exceptionFromBarSeriesOne_propagates() {
        IllegalStateException ise = new IllegalStateException("boom");
        when(barSeries.one()).thenThrow(ise);
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> manager.run(strategy, TradeType.SELL));
        assertSame(ise, thrown, "The original exception must be propagated unchanged");
        verify(barSeries).one();
        verifyNoInteractions(manager);
    }

    @Test
    @DisplayName("Exception from delegated overload propagates")
    void exceptionFromOverload_propagates() {
        when(barSeries.one()).thenReturn(amountOne);
        IllegalArgumentException iae = new IllegalArgumentException("bad amount");
        when(manager.run(strategy, TradeType.BUY, amountOne)).thenThrow(iae);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.BUY));
        assertSame(iae, thrown, "Exception from overload must be propagated unchanged");
        verify(barSeries).one();
        verify(manager).run(strategy, TradeType.BUY, amountOne);
        verifyNoMoreInteractions(barSeries, manager);
    }

    @Test
    @DisplayName("Spy verification – exactly one delegation call")
    void spyVerification_delegationCalledOnce() {
        BarSeriesManager spyManager = Mockito.spy(new BarSeriesManager(barSeries));
        when(barSeries.one()).thenReturn(amountOne);
        when(spyManager.run(strategy, TradeType.SELL, amountOne)).thenReturn(delegatedRecord);
        TradingRecord result = spyManager.run(strategy, TradeType.SELL);
        assertSame(delegatedRecord, result);
        InOrder inOrder = inOrder(barSeries, spyManager);
        inOrder.verify(barSeries).one();
        inOrder.verify(spyManager).run(strategy, TradeType.SELL, amountOne);
        verifyNoMoreInteractions(barSeries, spyManager);
    }

    @Test
    @DisplayName("Strategy that always enters – delegation still works")
    void strategyAlwaysEnter_delegates() {
        Strategy alwaysEnter = mock(Strategy.class);
        when(barSeries.one()).thenReturn(amountOne);
        when(manager.run(alwaysEnter, TradeType.BUY, amountOne)).thenReturn(delegatedRecord);
        TradingRecord actual = manager.run(alwaysEnter, TradeType.BUY);
        assertSame(delegatedRecord, actual);
        verify(barSeries).one();
        verify(manager).run(alwaysEnter, TradeType.BUY, amountOne);
    }

    @Test
    @DisplayName("Strategy that never enters – delegation still works")
    void strategyNeverEnter_delegates() {
        Strategy neverEnter = mock(Strategy.class);
        when(barSeries.one()).thenReturn(amountOne);
        when(manager.run(neverEnter, TradeType.SELL, amountOne)).thenReturn(delegatedRecord);
        TradingRecord actual = manager.run(neverEnter, TradeType.SELL);
        assertSame(delegatedRecord, actual);
        verify(barSeries).one();
        verify(manager).run(neverEnter, TradeType.SELL, amountOne);
    }
}
```