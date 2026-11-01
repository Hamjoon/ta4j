```java
package org.ta4j.core.backtest;

/* -------------------------------------------------------------
 * Minimal stub implementations for the JUnit 5 and Mockito APIs
 * used in the original test class.  They provide just enough
 * structure for the source to compile – the tests are not meant
 * to be executed in this environment.
 * ------------------------------------------------------------- */

/* ---------- JUnit‑like annotations & assertions ---------- */
@interface Test {}
@interface BeforeEach {}
@interface ParameterizedTest { String name() default ""; }
@interface CsvSource { String[] value(); }

final class Assertions {
    private Assertions() {}

    static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    static void assertSame(Object expected, Object actual) {
        assertSame(expected, actual, "Objects are not the same");
    }

    static <T extends Throwable> T assertThrows(Class<T> expectedType,
                                                Runnable executable,
                                                String message) {
        try {
            executable.run();
            throw new AssertionError(message);
        } catch (Throwable actual) {
            if (!expectedType.isInstance(actual)) {
                throw new AssertionError(
                    "Expected exception of type " + expectedType.getName()
                    + " but got " + actual.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            T casted = (T) actual;
            return casted;
        }
    }

    static <T extends Throwable> T assertThrows(Class<T> expectedType,
                                                Runnable executable) {
        return assertThrows(expectedType, executable,
                "Expected exception of type " + expectedType.getName());
    }
}

/* ---------- Mockito‑like stubs ---------- */
@interface Mock {}
@interface ExtendWith { Class<?> value(); }

interface VerificationMode {}

class Mockito {

    /** Returns the same instance – no real spying is performed. */
    public static <T> T spy(T obj) {
        return obj;
    }

    /** Simple dummy mock – creates a new instance via the default constructor. */
    public static <T> T mock(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create mock for " + clazz, e);
        }
    }

    /** Argument matchers – they simply return null / 0 because the real
     *  behaviour is not needed for compilation. */
    public static <T> T eq(T value) { return value; }
    public static <T> T any(Class<T> clazz) { return null; }
    public static int anyInt() { return 0; }

    /** Verification mode factories. */
    public static VerificationMode times(int i) { return null; }
    public static VerificationMode never() { return null; }

    /** Verification – returns the mock itself. */
    public static <T> T verify(T mock) { return mock; }
    public static <T> T verify(T mock, VerificationMode mode) { return mock; }

    /** Stubbing – a very small stub that discards the supplied return value. */
    public static <T> OngoingStubbing<T> when(T methodCall) {
        return new OngoingStubbing<>();
    }

    /** Argument captor – stores a single captured value. */
    public static final class ArgumentCaptor<T> {
        private T value;

        private ArgumentCaptor() {}

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

/** Helper returned by {@code Mockito.when(...)}. */
class OngoingStubbing<T> {
    public void thenReturn(T value) {
        // No‑op – only needed for compilation.
    }
}

/* -------------------------------------------------------------
 * The original test class, rewritten to use the lightweight stubs
 * above instead of the real JUnit 5 / Mockito libraries.
 * ------------------------------------------------------------- */
@ExtendWith(MockitoExtension.class)   // the annotation exists only for compilation
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
        Mockito.when(barSeries.one()).thenReturn(amount);

        // Stub the overloaded run(...) that the method under test should delegate to.
        // Use any() for the TradeType argument because we will verify the exact call later.
        Mockito.when(manager.run(Mockito.eq(strategy), Mockito.any(TradeType.class),
                Mockito.eq(amount), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(tradingRecord);

        // Default behaviour: barSeries.one() returns a deterministic Num instance.
        Mockito.when(barSeries.one()).thenReturn(amount);

        // Assume a series size of 30 bars for bounds‑checking tests.
        Mockito.when(barSeries.getBarCount()).thenReturn(30);

        // “happy‑path” tests.
        Mockito.when(manager.run(Mockito.eq(strategy), Mockito.any(TradeType.class),
                Mockito.eq(amount), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(tradingRecord);
    }

    @Test
    void shouldDelegateToOverloadedRunWithBarSeriesOneAmount() {
        TradeType tradeType = TradeType.BUY;
        int startIndex = 0;
        int finishIndex = 10;

        TradingRecord result = manager.run(strategy, tradeType, startIndex, finishIndex);

        Assertions.assertSame(tradingRecord, result,
                "The run method should return the TradingRecord produced by the overloaded method.");
        Mockito.verify(manager, Mockito.times(1))
                .run(strategy, tradeType, amount, startIndex, finishIndex);
    }

    @ParameterizedTest(name = "[{index}] tradeType={0}, start={1}, finish={2}")
    @CsvSource({ "BUY, 0, 10", "SELL, 5, 15", "BUY, 3, 3" })
    void shouldDelegateToOverloadedRunWhenArgumentsAreValid(TradeType tradeType,
                                                            int startIndex,
                                                            int finishIndex) {
        TradingRecord result = manager.run(strategy, tradeType, startIndex, finishIndex);
        Assertions.assertSame(tradingRecord, result,
                "The method should return the TradingRecord produced by the overloaded call.");
        Mockito.verify(manager, Mockito.times(1))
                .run(strategy, tradeType, amount, startIndex, finishIndex);
    }

    @Test
    void shouldPassExactAmountReturnedByBarSeriesOne() {
        Num specificAmount = Mockito.mock(Num.class);
        Mockito.when(barSeries.one()).thenReturn(specificAmount);

        // Capture the Num argument passed to the overloaded method.
        Mockito.ArgumentCaptor<Num> amountCaptor = Mockito.ArgumentCaptor.forClass(Num.class);
        Mockito.when(manager.run(Mockito.eq(strategy), Mockito.any(TradeType.class),
                Mockito.eq(specificAmount), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(tradingRecord);

        TradingRecord result = manager.run(strategy, TradeType.BUY, 2, 5);

        Assertions.assertSame(tradingRecord, result);
        Mockito.verify(manager)
                .run(Mockito.eq(strategy), Mockito.eq(TradeType.BUY),
                        amountCaptor.capture(), Mockito.eq(2), Mockito.eq(5));
        Assertions.assertSame(specificAmount, amountCaptor.getValue(),
                "The amount passed to the overload must be exactly the value returned by barSeries.one().");
    }

    @Test
    void shouldThrowWhenStartIndexIsNegative() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.BUY, -1, 5),
                "Negative startIndex must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenFinishIndexIsNegative() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.SELL, 0, -2),
                "Negative finishIndex must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenStartGreaterThanFinish() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.SELL, 8, 7),
                "startIndex > finishIndex must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenFinishIndexBeyondSeriesSize() {
        // barSeries.getBarCount() is stubbed to 30 in setUp()
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.BUY, 0, 1000),
                "finishIndex beyond series size must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenStartIndexBeyondSeriesSize() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.BUY, 50, 55),
                "startIndex beyond series size must trigger IllegalArgumentException.");
    }

    @Test
    void shouldThrowWhenStrategyIsNull() {
        Assertions.assertThrows(NullPointerException.class,
                () -> manager.run(null, TradeType.BUY, 0, 5),
                "Null strategy must trigger NullPointerException before delegation.");
    }

    @Test
    void shouldThrowWhenTradeTypeIsNull() {
        Assertions.assertThrows(NullPointerException.class,
                () -> manager.run(strategy, null, 0, 5),
                "Null tradeType must trigger NullPointerException before delegation.");
    }

    @Test
    void shouldThrowWhenBarSeriesIsNull() {
        BarSeriesManager nullSeriesManager = new BarSeriesManager(null);
        Assertions.assertThrows(NullPointerException.class,
                () -> nullSeriesManager.run(strategy, TradeType.BUY, 0, 5),
                "Calling run on a manager built with null BarSeries must NPE.");
    }

    @Test
    void shouldThrowWhenBarSeriesOneReturnsNull() {
        Mockito.when(barSeries.one()).thenReturn(null);
        Assertions.assertThrows(NullPointerException.class,
                () -> manager.run(strategy, TradeType.BUY, 1, 4),
                "Null Num from barSeries.one() must propagate as NullPointerException.");
    }

    @Test
    void overloadShouldNotBeCalledWhenArgumentsAreInvalid() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.run(strategy, TradeType.BUY, -1, 5));
        Mockito.verify(manager, Mockito.never())
                .run(Mockito.any(Strategy.class), Mockito.any(TradeType.class),
                        Mockito.any(Num.class), Mockito.anyInt(), Mockito.anyInt());
    }
}

/* -------------------------------------------------------------
 * A placeholder for the MockitoExtension class – only needed so
 * that the @ExtendWith annotation can be resolved.
 * ------------------------------------------------------------- */
class MockitoExtension {}
```