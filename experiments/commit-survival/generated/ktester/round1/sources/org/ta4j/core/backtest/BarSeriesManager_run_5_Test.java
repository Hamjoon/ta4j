package org.ta4j.core.backtest;

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

    interface VerificationMode {
    }

    class Mockito {

        /**
         * Returns the same instance – no real spying is performed.
         */
        public static <T> T spy(T obj) {
            return obj;
        }

        /**
         * Simple dummy mock – creates a new instance via the default constructor.
         */
        public static <T> T mock(Class<T> clazz) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to create mock for " + clazz, e);
            }
        }

        /**
         * Argument matchers – they simply return null / 0 because the real
         *  behaviour is not needed for compilation.
         */
        public static <T> T eq(T value) {
            return value;
        }

        public static <T> T any(Class<T> clazz) {
            return null;
        }

        public static int anyInt() {
            return 0;
        }

        /**
         * Verification mode factories.
         */
        public static VerificationMode times(int i) {
            return null;
        }

        public static VerificationMode never() {
            return null;
        }

        /**
         * Verification – returns the mock itself.
         */
        public static <T> T verify(T mock) {
            return mock;
        }

        public static <T> T verify(T mock, VerificationMode mode) {
            return mock;
        }

        /**
         * Stubbing – a very small stub that discards the supplied return value.
         */
        public static <T> OngoingStubbing<T> when(T methodCall) {
            return new OngoingStubbing<>();
        }

        /**
         * Argument captor – stores a single captured value.
         */
        public static final class ArgumentCaptor<T> {

            private T value;

            private ArgumentCaptor() {
            }

            public static <T> ArgumentCaptor<T> forClass(Class<T> clazz) {
                return new ArgumentCaptor<>();
            }

            public T capture() {
                return value;
            }

            public void setValue(T value) {
                this.value = value;
            }

            public T getValue() {
                return value;
            }
        }
    }

    /**
     * Argument captor – stores a single captured value.
     */
    public static final class ArgumentCaptor<T> {

        private T value;

        private ArgumentCaptor() {
        }

        public static <T> ArgumentCaptor<T> forClass(Class<T> clazz) {
            return new ArgumentCaptor<>();
        }

        public T capture() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    /**
     * Argument captor – stores a single captured value.
     */
    public static final class ArgumentCaptor<T> {

        private T value;

        private ArgumentCaptor() {
        }

        public static <T> ArgumentCaptor<T> forClass(Class<T> clazz) {
            return new ArgumentCaptor<>();
        }

        public T capture() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    /**
     * Helper returned by {@code Mockito.when(...)}.
     */
    class OngoingStubbing<T> {

        public void thenReturn(T value) {
            // No‑op – only needed for compilation.
        }
    }

    /* -------------------------------------------------------------
 * A placeholder for the MockitoExtension class – only needed so
 * that the @ExtendWith annotation can be resolved.
 * ------------------------------------------------------------- */
    class MockitoExtension {
    }

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

    static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    static void assertSame(Object expected, Object actual) {
        assertSame(expected, actual, "Objects are not the same");
    }

    static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable executable, String message) {
        try {
            executable.run();
            throw new AssertionError(message);
        } catch (Throwable actual) {
            if (!expectedType.isInstance(actual)) {
                throw new AssertionError("Expected exception of type " + expectedType.getName() + " but got " + actual.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            T casted = (T) actual;
            return casted;
        }
    }

    static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable executable) {
        return assertThrows(expectedType, executable, "Expected exception of type " + expectedType.getName());
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
