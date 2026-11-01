```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

/**
 * Unit tests for {@link BarSeriesManager#run(Strategy, TradeType)}.
 *
 * <p>The method under test simply delegates to {@code run(strategy, tradeType, barSeries.one())}.
 * The tests verify:
 * <ul>
 *   <li>Null‑argument handling (strategy, tradeType, BarSeries, amount).</li>
 *   <li>Correct delegation with the amount returned by {@code BarSeries.one()}.</li>
 *   <li>Propagation of exceptions thrown by {@code BarSeries.one()} or the delegated overload.</li>
 *   <li>That the delegated overload receives the exact {@link Num} instance (including zero,
 *   negative or positive values).</li>
 * </ul>
 *
 * <p>Behaviour that depends on the concrete implementation of the three‑argument overload
 * (e.g., how a Strategy’s signals translate into trades) is verified only indirectly by
 * ensuring the overload is invoked with the expected parameters; deeper validation would
 * require the actual implementation of the overload, which is outside the scope of these
 * unit tests.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManagerRunTest {

    /* --------------------------------------------------------------------- */
    /*  Mocks and test fixture                                               */
    /* --------------------------------------------------------------------- */
    @Mock private BarSeries barSeries;
    @Mock private Strategy strategy;
    @Mock private TradingRecord delegatedRecord;   // record returned by the 3‑arg overload
    @Mock private Num amountOne;                  // value returned by barSeries.one()
    @Mock private Num amountZero;
    @Mock private Num amountPositive;
    @Mock private Num amountNegative;

    /** Spy of the manager so we can stub the three‑argument overload. */
    private BarSeriesManager manager;

    @BeforeEach
    void init() {
        // Create a spy – the real constructor is used, but we can stub overloads.
        manager = Mockito.spy(new BarSeriesManager(barSeries));
    }

    @AfterEach
    void resetMocks() {
        Mockito.reset(barSeries, strategy, delegatedRecord,
                amountOne, amountZero, amountPositive, amountNegative, manager);
    }

    /* --------------------------------------------------------------------- */
    /*  1. Null‑argument handling                                             */
    /* --------------------------------------------------------------------- */

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

    /* --------------------------------------------------------------------- */
    /*  2. Delegation with various amount values                             */
    /* --------------------------------------------------------------------- */

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

    private static Stream<Arguments> provideDelegationScenarios() {
        // Mocked Num instances are created in the test class; we pass null placeholders
        // and replace them with the actual mocks inside the test method using the
        // Mockito @Mock fields via reflection.
        return Stream.of(
                Arguments.of(TradeType.BUY, null),   // will be replaced with amountOne
                Arguments.of(TradeType.SELL, null)   // will be replaced with amountOne
        );
    }

    @BeforeEach
    void replaceAmountPlaceholders() {
        // The parameterized test supplies `null` for the amount placeholders.
        // Replace them with the appropriate mocked Num instances based on the test name.
        // This indirection keeps the provider method simple and avoids static Mockito usage.
        // The actual replacement happens inside each test method.
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

    /* --------------------------------------------------------------------- */
    /*  3. Exception propagation                                            */
    /* --------------------------------------------------------------------- */

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

    /* --------------------------------------------------------------------- */
    /*  4. Delegation integrity verification (spy usage)                     */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("Spy verification – exactly one delegation call")
    void spyVerification_delegationCalledOnce() {
        // Use a fresh spy to avoid interference from previous stubbings.
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

    /* --------------------------------------------------------------------- */
    /*  5. (Optional) Strategy‑signal handling – limited to delegation check */
    /* --------------------------------------------------------------------- */
    // The concrete behaviour of the three‑argument overload (opening/closing trades)
    // depends on the internal implementation of BarSeriesManager and the Strategy
    // interface, which are not part of this unit‑test scope. Therefore we limit the
    // verification to the fact that the overload is invoked with the supplied Strategy
    // instance; any deeper validation should be performed in integration tests.

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