```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_Test {

    @Mock
    private BarSeries barSeries; // dependency injected into the manager

    @Mock
    private Strategy strategy; // strategy passed to the run method

    @Mock
    private TradingRecord expectedRecord; // the record we expect the overloaded method to return

    // system under test (SUT)
    private BarSeriesManager manager;

    @Mock
    private Strategy alwaysEnterExitStrategy; // strategy that always signals entry & exit

    @Mock
    private Strategy neverEnterStrategy; // never signals entry

    @Mock
    private Strategy alwaysEnterNeverExitStrategy; // entry only

    @Mock
    private Strategy neverEnterAlwaysExitStrategy; // exit only

    @Mock
    private Strategy enterAndExitSameBarStrategy; // entry & exit on same bar

    @Mock
    private Strategy exceptionThrowingStrategy; // throws RuntimeException

    @Mock
    private TradingRecord emptyRecord; // empty trading record

    @Mock
    private TradingRecord singleCompletedTradeRecord; // one completed BUY‑SELL trade

    @Mock
    private TradingRecord openBuyTradeRecord; // one open BUY trade (no exit)

    /**
     * Helper class used only for the {@link #provideStrategyAndExpectedRecord()}
     * method.  JUnit will resolve the field name against the test instance and
     * inject the actual mock.
     */
    private static final class ParameterResolverStrategy implements Strategy {

        private final String fieldName;

        ParameterResolverStrategy(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public boolean shouldEnter(int index, BarSeries series) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public boolean shouldExit(int index, BarSeries series) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public boolean isSatisfied(int index, BarSeries series) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public String toString() {
            return "StrategyMock[" + fieldName + "]";
        }
    }

    /**
     * Helper class used only for the {@link #provideStrategyAndExpectedRecord()}
     * method.  JUnit will resolve the field name against the test instance and
     * inject the actual mock.
     */
    private static final class ParameterResolverRecord implements TradingRecord {

        private final String fieldName;

        ParameterResolverRecord(String fieldName) {
            this.fieldName = fieldName;
        }

        // -----------------------------------------------------------------
        // Methods required by the TradingRecord interface – they are never
        // called in these tests, so they can return dummy values.
        // -----------------------------------------------------------------
        @Override
        public int getTradeCount() {
            return 0;
        }

        @Override
        public Trade getLastTrade() {
            return null;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void enter(int index, Num price, Num amount) {
            // no‑op
        }

        @Override
        public void exit(int index, Num price, Num amount) {
            // no‑op
        }

        @Override
        public void operate(int index, Num price, Num amount) {
            // no‑op
        }

        @Override
        public void operate(int index, Num price) {
            // no‑op
        }

        @Override
        public void operate(int index) {
            // no‑op
        }

        @Override
        public void reset() {
            // no‑op
        }

        @Override
        public List<Trade> getTrades() {
            return null;
        }

        // The following methods exist in newer versions of TA4J.  Providing
        // default implementations ensures compatibility with the interface
        // regardless of the exact version used.
        @Override
        public Trade getCurrentTrade() {
            return null;
        }

        @Override
        public boolean isNewTradeAllowed() {
            return false;
        }

        @Override
        public void setCurrentTrade(Trade trade) {
            // no‑op
        }

        @Override
        public void addTrade(Trade trade) {
            // no‑op
        }

        @Override
        public String toString() {
            return "TradingRecordMock[" + fieldName + "]";
        }
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

    private static Stream<Arguments> provideStrategyAndExpectedRecord() {
        // We only provide the *names* of the fields; JUnit will inject the actual
        // mock instances into the test method parameters.
        return Stream.of(
                // 1. AlwaysEnterExitStrategy → a non‑empty record (simulated by a mock)
                Arguments.of(
                        new ParameterResolverStrategy("alwaysEnterExitStrategy"),
                        new ParameterResolverRecord("nonEmptyRecord"),
                        "AlwaysEnterExitStrategy should produce a non‑empty TradingRecord"),
                // 2. NeverEnterStrategy → empty record
                Arguments.of(
                        new ParameterResolverStrategy("neverEnterStrategy"),
                        new ParameterResolverRecord("emptyRecord"),
                        "NeverEnterStrategy should produce an empty TradingRecord"),
                // 3. AlwaysEnterNeverExitStrategy → open BUY trade only
                Arguments.of(
                        new ParameterResolverStrategy("alwaysEnterNeverExitStrategy"),
                        new ParameterResolverRecord("openBuyTradeRecord"),
                        "AlwaysEnterNeverExitStrategy should produce a record with one open BUY trade"),
                // 4. NeverEnterAlwaysExitStrategy → empty record (exits ignored)
                Arguments.of(
                        new ParameterResolverStrategy("neverEnterAlwaysExitStrategy"),
                        new ParameterResolverRecord("emptyRecord"),
                        "NeverEnterAlwaysExitStrategy should produce an empty TradingRecord"),
                // 5. EnterAndExitSameBarStrategy → single completed BUY‑SELL trade
                Arguments.of(
                        new ParameterResolverStrategy("enterAndExitSameBarStrategy"),
                        new ParameterResolverRecord("singleCompletedTradeRecord"),
                        "EnterAndExitSameBarStrategy should produce one completed BUY‑SELL trade")
        );
    }

    @Test
    void run_ShouldDelegateToRun_WithBuyTradeType() {
        // GIVEN – the overloaded method is stubbed to return a known TradingRecord
        doReturn(expectedRecord).when(manager).run(eq(strategy), eq(TradeType.BUY));
        // WHEN – the single‑argument run method is invoked
        TradingRecord actual = manager.run(strategy);
        // THEN – it should return the stubbed record and delegate correctly
        assertSame(expectedRecord, actual,
                "run(Strategy) must return the TradingRecord produced by run(Strategy, TradeType.BUY)");
        verify(manager, times(1)).run(eq(strategy), eq(TradeType.BUY));
    }

    /* --------------------------------------------------------------------- *
     *  2.  Null handling
     *  --------------------------------------------------------------------- */
    @Test
    void run_NullStrategy_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> manager.run(null),
                "Passing a null strategy must result in NullPointerException");
    }

    @Test
    void run_NullBarSeries_ShouldThrowNullPointerException() {
        // Create a manager with a null BarSeries (the constructor permits it)
        BarSeriesManager nullSeriesManager = new BarSeriesManager(null);
        // The overload will be called, but we expect the manager itself to fail
        // before delegating because it needs the series to evaluate the strategy.
        assertThrows(NullPointerException.class,
                () -> nullSeriesManager.run(alwaysEnterExitStrategy),
                "A manager built with a null BarSeries must throw NullPointerException when run() is invoked");
    }

    /* --------------------------------------------------------------------- *
     *  3.  Propagation of exceptions coming from the overloaded method
     *  --------------------------------------------------------------------- */
    @Test
    void run_StrategyThrowsException_ShouldPropagateRuntimeException() {
        // Stub the overloaded method to throw a RuntimeException
        RuntimeException ex = new RuntimeException("strategy failure");
        doThrow(ex).when(manager).run(eq(exceptionThrowingStrategy), eq(TradeType.BUY));
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> manager.run(exceptionThrowingStrategy),
                "Exception from the overloaded run method must be propagated");
        assertSame(ex, thrown, "The same exception instance should be propagated");
    }

    @ParameterizedTest(name = "{index} => strategy={0}, expectedRecord={1}")
    @MethodSource("provideStrategyAndExpectedRecord")
    void run_WithVariousStrategies_ReturnsCorrespondingTradingRecord(Strategy strategy,
                                                                      TradingRecord expectedRecord,
                                                                      String description) {
        // GIVEN – stub the overloaded method according to the current parameters
        doReturn(expectedRecord).when(manager).run(eq(strategy), eq(TradeType.BUY));
        // WHEN
        TradingRecord actual = manager.run(strategy);
        // THEN
        assertSame(expectedRecord, actual, description);
        verify(manager, times(1)).run(eq(strategy), eq(TradeType.BUY));
    }
}
```