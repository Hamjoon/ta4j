
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.stream.Stream;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;

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

    /**
     * default indices used by a few tests
     */
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

        @Override
        public boolean isUnstableAt(int index) {
            return false;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /**
     * Subclass of {@link BarSeriesManager} that records the parameters of the
     * overloaded {@code run(Strategy, TradeType, Num, int, int)} method.
     * This allows us to verify that the delegating method forwards the call
     * correctly without using a mocking framework.
     */
    private static class TestBarSeriesManager extends BarSeriesManager {

        private Strategy capturedStrategy;

        private TradeType capturedTradeType;

        private Num capturedAmount;

        private int capturedStart;

        private int capturedFinish;

        private TradingRecord recordToReturn;

        TestBarSeriesManager(BarSeries series) {
            super(series);
        }

        void setRecordToReturn(TradingRecord record) {
            this.recordToReturn = record;
        }

        @Override
        public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex) {
            this.capturedStrategy = strategy;
            this.capturedTradeType = tradeType;
            this.capturedAmount = amount;
            this.capturedStart = startIndex;
            this.capturedFinish = finishIndex;
            return recordToReturn != null ? recordToReturn : new BaseTradingRecord();
        }

        // getters for verification
        Strategy getCapturedStrategy() {
            return capturedStrategy;
        }

        TradeType getCapturedTradeType() {
            return capturedTradeType;
        }

        Num getCapturedAmount() {
            return capturedAmount;
        }

        int getCapturedStart() {
            return capturedStart;
        }

        int getCapturedFinish() {
            return capturedFinish;
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

    /**
     * Helper stream for parameterised tests (not used directly in this file but kept for completeness).
     */
    private static Stream<Arguments> successfulCases() {
        List<org.junit.runners.Parameterized.Parameters> list = new ArrayList<>();
        list.add(new org.junit.runners.Parameterized.Parameters(new Object[] { 0, 10, "happy‑path" }));
        list.add(new org.junit.runners.Parameterized.Parameters(new Object[] { 5, 5, "empty‑range" }));
        return list.stream();
    }

    /* --------------------------------------------------------------------- *
     *  Static “main‑style” sanity checks – they are not part of the JUnit run
     * --------------------------------------------------------------------- */
    public static void main(String[] args) {
        testDelegateToOverloadedRunWithBuyAndOneNum();
        testIllegalArgumentWhenStartGreaterThanFinish();
        testIllegalArgumentWhenStartIsNegative();
        testNullPointerWhenStrategyIsNull();
        testIndexOutOfBoundsWhenStartExceedsSeriesSize();
        testIndexOutOfBoundsWhenFinishExceedsSeriesSize();
        testIllegalArgumentWhenFinishIsNegative();
        testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero();
        testWholeSeriesProcessing();
        System.out.println("All manual checks passed.");
    }

    private static void testDelegateToOverloadedRunWithBuyAndOneNum() {
        BarSeries series = new BaseBarSeriesBuilder().withName("empty").build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        TradingRecord record = mgr.run(strategy, 0, 0);
        Assert.assertNotNull("run should never return null", record);
        Assert.assertTrue("Empty series + no‑signal strategy must yield no positions", record.getPositions().isEmpty());
    }

    private static void testIllegalArgumentWhenStartGreaterThanFinish() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        try {
            mgr.run(strategy, 8, 3);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void testIllegalArgumentWhenStartIsNegative() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        try {
            mgr.run(strategy, -1, 4);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void testNullPointerWhenStrategyIsNull() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(5).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        try {
            mgr.run(null, 0, 4);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
        }
    }

    private static void testIndexOutOfBoundsWhenStartExceedsSeriesSize() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        try {
            mgr.run(strategy, 1000, 1010);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    private static void testIndexOutOfBoundsWhenFinishExceedsSeriesSize() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        try {
            mgr.run(strategy, 0, 1000);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    private static void testIllegalArgumentWhenFinishIsNegative() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        try {
            mgr.run(strategy, 2, -1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        BarSeries series = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        TradingRecord record = mgr.run(strategy, 0, 0);
        Assert.assertNotNull("Result must not be null", record);
        Assert.assertTrue("Empty series with zero range must yield empty record", record.getPositions().isEmpty());
    }

    private static void testWholeSeriesProcessing() {
        BarSeries series = new BaseBarSeriesBuilder().withName("full").withNumBars(10).build();
        BarSeriesManager mgr = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();
        TradingRecord record = mgr.run(strategy, 0, 9);
        Assert.assertNotNull("Result must not be null when processing the whole series", record);
        Assert.assertTrue("No‑signal strategy must produce an empty record", record.getPositions().isEmpty());
    }

    @Test
    void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
        // Arrange – tell the test manager which record to return
        ((TestBarSeriesManager) manager).setRecordToReturn(mockTradingRecord);
        // Act
        TradingRecord result = manager.run(mockStrategy, DEFAULT_START, DEFAULT_FINISH);
        // Assert result is the one we forced
        Assert.assertSame("Delegating run must return the record from the overload", mockTradingRecord, result);
        // Verify parameters passed to the overload
        TestBarSeriesManager testMgr = (TestBarSeriesManager) manager;
        Assert.assertEquals(mockStrategy, testMgr.getCapturedStrategy());
        Assert.assertEquals(TradeType.BUY, testMgr.getCapturedTradeType());
        Assert.assertEquals(mockNum, testMgr.getCapturedAmount());
        Assert.assertEquals(DEFAULT_START, testMgr.getCapturedStart());
        Assert.assertEquals(DEFAULT_FINISH, testMgr.getCapturedFinish());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        try {
            manager.run(mockStrategy, 8, 3);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartIsNegative() {
        try {
            manager.run(mockStrategy, -1, 4);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void shouldThrowNullPointerExceptionWhenStrategyIsNull() {
        try {
            manager.run(null, 0, 5);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenStartExceedsSeriesSize() {
        try {
            manager.run(mockStrategy, 1000, 1010);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenFinishExceedsSeriesSize() {
        try {
            manager.run(mockStrategy, 0, 1000);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFinishIsNegative() {
        try {
            manager.run(mockStrategy, 2, -1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void shouldReturnEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        // Simulate an empty series by creating a new manager with a zero‑size series
        BarSeries emptySeries = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
        TestBarSeriesManager emptyMgr = new TestBarSeriesManager(emptySeries);
        emptyMgr.setRecordToReturn(new BaseTradingRecord());
        TradingRecord result = emptyMgr.run(mockStrategy, 0, 0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    void shouldProcessWholeSeriesWhenRangeCoversAllBars() {
        TestBarSeriesManager fullMgr = new TestBarSeriesManager(mockBarSeries);
        TradingRecord fullRecord = new BaseTradingRecord();
        fullMgr.setRecordToReturn(fullRecord);
        TradingRecord result = fullMgr.run(mockStrategy, 0, 9);
        Assert.assertSame(fullRecord, result);
    }

    @Test
    void shouldCreateSingleBuyTradeWhenStrategyAlwaysEntersAndExits() {
        int start = 5;
        int finish = 6;
        TestBarSeriesManager customMgr = new TestBarSeriesManager(mockBarSeries);
        TradingRecord oneBuyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(oneBuyRecord);
        TradingRecord result = customMgr.run(mockStrategy, start, finish);
        Assert.assertSame(oneBuyRecord, result);
        Assert.assertEquals(TradeType.BUY, customMgr.getCapturedTradeType());
        Assert.assertEquals(mockNum, customMgr.getCapturedAmount());
        Assert.assertEquals(start, customMgr.getCapturedStart());
        Assert.assertEquals(finish, customMgr.getCapturedFinish());
    }

    @Test
    void shouldReturnEmptyRecordWhenStrategyNeverSignals() {
        TestBarSeriesManager customMgr = new TestBarSeriesManager(mockBarSeries);
        TradingRecord emptyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(emptyRecord);
        TradingRecord result = customMgr.run(mockStrategy, 0, 7);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    void shouldDefaultToBuyTradeTypeForAllGeneratedTrades() {
        int start = 3;
        int finish = 8;
        TestBarSeriesManager customMgr = new TestBarSeriesManager(mockBarSeries);
        TradingRecord buyOnlyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(buyOnlyRecord);
        TradingRecord result = customMgr.run(mockStrategy, start, finish);
        Assert.assertSame(buyOnlyRecord, result);
        Assert.assertEquals(TradeType.BUY, customMgr.getCapturedTradeType());
        Assert.assertEquals(mockNum, customMgr.getCapturedAmount());
        Assert.assertEquals(start, customMgr.getCapturedStart());
        Assert.assertEquals(finish, customMgr.getCapturedFinish());
    }

    @Before
    public void setUp() {
        // Build a simple series with 10 bars (the actual data is irrelevant for these tests)
        this.mockBarSeries = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
        // Use the real series' one() as the amount
        this.mockNum = mockBarSeries.one();
        // Simple strategy that never signals
        this.mockStrategy = new NoSignalStrategy();
        // A dummy trading record that can be returned by the test manager
        this.mockTradingRecord = new BaseTradingRecord();
        // Initialise the test manager (a subclass that records calls)
        this.manager = new TestBarSeriesManager(mockBarSeries);
    }

    @After
    public void tearDown() {
        // No special clean‑up required
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:16: error: package org.mockito does not exist
import static org.mockito.Mockito.*;
                         ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:17: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:20: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:21: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.Arguments;
                                        ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:23: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:160: error: cannot find symbol
    private static Stream<Arguments> successfulCases() {
                          ^
  symbol:   class Arguments
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:19: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:23: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:26: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_run_4_Test
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
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:140: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:151: error: cannot find symbol
    @AfterEach
     ^
  symbol:   class AfterEach
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:52: error: NoSignalStrategy is not abstract and does not override abstract method setUnstableBars(int) in Strategy
    private static class NoSignalStrategy implements Strategy {
                   ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:54: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:59: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:145: error: cannot find symbol
        spyManager = spy(manager);
                     ^
  symbol:   method spy(BarSeriesManager)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:147: error: cannot find symbol
        when(mockBarSeries.one()).thenReturn(mockNum);
        ^
  symbol:   method when(Num)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:148: error: cannot find symbol
        when(mockBarSeries.getBarCount()).thenReturn(10);
        ^
  symbol:   method when(int)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:154: error: cannot find symbol
        clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
        ^
  symbol:   method clearInvocations(BarSeries,Strategy,Num,TradingRecord,BarSeriesManager)
  location: class BarSeriesManager_run_4_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:162: error: Parameters is abstract; cannot be instantiated
        list.add(new org.junit.runners.Parameterized.Parameters(new Object[] { 0, 10, "happy‑path" }));
                 ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:163: error: Parameters is abstract; cannot be instantiated
        list.add(new org.junit.runners.Parameterized.Parameters(new Object[] { 5, 5, "empty‑range" }));
                 ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:193: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:204: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:215: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(5).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:225: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:236: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:247: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:258: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
                                                                       ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:267: error: cannot find symbol
        BarSeries series = new BaseBarSeriesBuilder().withName("full").withNumBars(10).build();
                                                                      ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:349: error: cannot find symbol
        BarSeries emptySeries = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
                                                                            ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/backtest/BarSeriesManager_run_4_Test.java:409: error: cannot find symbol
        this.mockBarSeries = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
                                                                        ^
  symbol:   method withNumBars(int)
  location: class BaseBarSeriesBuilder
33 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.backtest.BarSeriesManager': '\n * A manager for {@link BarSeries} objects used for backtesting. Allows to run a\n * {@link Strategy trading strategy} over the managed bar series.\n ', 'api document of method run(Strategy, int, int)': '\n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a {@link TradeType} BUY trade.\n     *\n     * @param strategy    the trading strategy\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ', 'return type': 'org.ta4j.core.TradingRecord', 'dependent classes': 'class `org.ta4j.core.Strategy`:\n\t\nclass `org.ta4j.core.Trade.TradeType`:\n\tdependent fields:\n\t\torg.ta4j.core.Trade.TradeType org.ta4j.core.Trade.TradeType.BUY;\n\t\nclass `org.ta4j.core.backtest.BarSeriesManager`:\n\tdependent fields:\n\t\torg.ta4j.core.BarSeries org.ta4j.core.backtest.BarSeriesManager.barSeries;\n\trelated functions:\n\t\tmethod `public TradingRecord run(Strategy, TradeType, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a trade of {@link TradeType tradeType}.\n     *\n     * @param strategy    the trading strategy\n     * @param tradeType   the {@link TradeType} used to open the position\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public TradingRecord run(Strategy, TradeType, Num)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * Runs the provided strategy over the managed series.\n     *\n     * @param strategy  the trading strategy\n     * @param tradeType the {@link TradeType} used to open the position\n     * @param amount    the amount used to open/close the trades\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public TradingRecord run(Strategy, TradeType)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * Runs the provided strategy over the managed series.\n     *\n     * Opens the position with a trade of {@link TradeType tradeType}.\n     *\n     * @param strategy  the trading strategy\n     * @param tradeType the {@link TradeType} used to open the position\n     * @return the trading record coming from the run\n     ;\nclass `org.ta4j.core.criteria.NumberOfPositionsCriterion`:\n\trelated functions:\n\t\tmethod `public Num calculate(BarSeries, Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`;\nclass `org.ta4j.core.Indicator`:\n\trelated functions:\n\t\tmethod ` Num one()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: \n     * @return the Num of 1\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


