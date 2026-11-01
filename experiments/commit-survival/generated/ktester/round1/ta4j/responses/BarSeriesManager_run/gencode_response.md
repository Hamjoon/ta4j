```java
package org.ta4j.core.backtest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.ta4j.core.*;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * Unit tests for {@link BarSeriesManager#run(Strategy)}.
 *
 * <p>
 * The method under test is a thin delegator that forwards the call to the
 * two‑argument overload {@code run(strategy, TradeType.BUY)}.  Therefore the
 * tests focus on:
 * <ul>
 *   <li>correct delegation (already covered by a simple spy test),</li>
 *   <li>proper handling of {@code null} arguments,</li>
 *   <li>propagation of exceptions thrown by the overloaded method,</li>
 *   <li>that the method returns whatever the overloaded overload returns
 *       (different {@link TradingRecord} scenarios are simulated with mocks).</li>
 * </ul>
 *
 * <p>
 * The real back‑testing logic lives in the overloaded overloads; they are not
 * exercised here.  Instead we stub them to return a {@link TradingRecord}
 * representing each test case.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_Test {

    @Mock
    private BarSeries barSeries;               // injected into the manager

    @Mock
    private Strategy alwaysEnterExitStrategy; // strategy that always signals entry & exit

    @Mock
    private Strategy neverEnterStrategy;      // never signals entry

    @Mock
    private Strategy alwaysEnterNeverExitStrategy; // entry only

    @Mock
    private Strategy neverEnterAlwaysExitStrategy; // exit only

    @Mock
    private Strategy enterAndExitSameBarStrategy;  // entry & exit on same bar

    @Mock
    private Strategy exceptionThrowingStrategy;   // throws RuntimeException

    @Mock
    private TradingRecord emptyRecord;           // empty trading record

    @Mock
    private TradingRecord singleCompletedTradeRecord; // one completed BUY‑SELL trade

    @Mock
    private TradingRecord openBuyTradeRecord;    // one open BUY trade (no exit)

    private BarSeriesManager manager;            // system under test (SUT)

    @BeforeEach
    void setUp() {
        // Use a *spy* so we can stub the overloaded method while still calling the
        // real constructor of BarSeriesManager.
        manager = spy(new BarSeriesManager(barSeries));
    }

    @AfterEach
    void tearDown() {
        // Ensure no leftover stubbings affect other tests.
        clearInvocations(manager);
    }

    /* --------------------------------------------------------------------- *
     *  1.  Delegation test – already supplied in the skeleton.
     *  --------------------------------------------------------------------- */
    @Test
    void run_ShouldDelegateToRun_WithBuyTradeType() {
        // GIVEN – stub the overloaded method to return a known TradingRecord
        doReturn(emptyRecord).when(manager).run(eq(alwaysEnterExitStrategy), eq(TradeType.BUY));

        // WHEN
        TradingRecord actual = manager.run(alwaysEnterExitStrategy);

        // THEN
        assertSame(emptyRecord, actual,
                "run(Strategy) must return the TradingRecord produced by run(Strategy, TradeType.BUY)");
        verify(manager, times(1)).run(eq(alwaysEnterExitStrategy), eq(TradeType.BUY));
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

    /* --------------------------------------------------------------------- *
     *  4.  Behaviour based on different strategy stubs
     *      (the overloaded method is stubbed to return a TradingRecord that
     *       matches the description of each test case)
     *  --------------------------------------------------------------------- */

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

    private static Stream<Arguments> provideStrategyAndExpectedRecord() {
        // Mocks are created by the test class; they will be injected via the
        // test instance.  Here we only provide the *names* of the fields; the
        // actual objects will be resolved by the parameter resolver (JUnit 5
        // injects the mocks as method arguments because they are declared as
        // parameters of the test method).
        return Stream.of(
                // 1. AlwaysEnterExitStrategy → a non‑empty record (simulated by a mock)
                Arguments.of(
                        // strategy mock
                        new ParameterResolverStrategy("alwaysEnterExitStrategy"),
                        // expected record mock
                        new ParameterResolverRecord("nonEmptyRecord"),
                        "AlwaysEnterExitStrategy should produce a non‑empty TradingRecord"
                ),
                // 2. NeverEnterStrategy → empty record
                Arguments.of(
                        new ParameterResolverStrategy("neverEnterStrategy"),
                        new ParameterResolverRecord("emptyRecord"),
                        "NeverEnterStrategy should produce an empty TradingRecord"
                ),
                // 3. AlwaysEnterNeverExitStrategy → open BUY trade only
                Arguments.of(
                        new ParameterResolverStrategy("alwaysEnterNeverExitStrategy"),
                        new ParameterResolverRecord("openBuyTradeRecord"),
                        "AlwaysEnterNeverExitStrategy should produce a record with one open BUY trade"
                ),
                // 4. NeverEnterAlwaysExitStrategy → empty record (exits ignored)
                Arguments.of(
                        new ParameterResolverStrategy("neverEnterAlwaysExitStrategy"),
                        new ParameterResolverRecord("emptyRecord"),
                        "NeverEnterAlwaysExitStrategy should produce an empty TradingRecord"
                ),
                // 5. EnterAndExitSameBarStrategy → single completed BUY‑SELL trade
                Arguments.of(
                        new ParameterResolverStrategy("enterAndExitSameBarStrategy"),
                        new ParameterResolverRecord("singleCompletedTradeRecord"),
                        "EnterAndExitSameBarStrategy should produce one completed BUY‑SELL trade"
                )
        );
    }

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

        // All methods are irrelevant for the delegation test – they will never be
        // called because we only compare object identity.
        @Override public int getTradeCount() { return 0; }
        @Override public Trade getLastTrade() { return null; }
        @Override public boolean isClosed() { return false; }
        @Override public void enter(int index, Num price, Num amount) {}
        @Override public void exit(int index, Num price, Num amount) {}
        @Override public void operate(int index, Num price, Num amount) {}
        @Override public void operate(int index, Num price) {}
        @Override public void operate(int index) {}
        @Override public void reset() {}
        @Override public List<Trade> getTrades() { return null; }
        @Override public String toString() { return "TradingRecordMock[" + fieldName + "]"; }
    }
}
```