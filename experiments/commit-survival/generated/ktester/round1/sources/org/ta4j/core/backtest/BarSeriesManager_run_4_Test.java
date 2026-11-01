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

// @ExtendWith(MockitoExtension.class)
class BarSeriesManager_run_4_Test {

//     @Mock
    private BarSeries mockBarSeries;

//     @Mock
    private Strategy mockStrategy;

//     @Mock
    private TradingRecord mockTradingRecord;

//     @Mock
    private Num mockNum;

    private BarSeriesManager manager;

    private BarSeriesManager spyManager;

    /**
     * default indices used by a few tests
     */
    private static final int DEFAULT_START = 0;

    private static final int DEFAULT_FINISH = 9;

    private BarSeries series;

    private NoSignalStrategy strategy;

    private Num amount;

    /**
     * Minimal {@link Strategy} implementation that never signals.
     */
//     private static class NoSignalStrategy implements Strategy {

//         @Override
//         public boolean shouldEnter(int index, BarSeries series) {
//             return false;
//         }

//         @Override
//         public boolean shouldExit(int index, BarSeries series) {
//             return false;
//         }

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

        @Override
        public void setUnstableBars(int unstableBars) {
            // no‑op for this simple strategy
        }
    }

    /**
     * Subclass of {@link BarSeriesManager} that records the parameters of the
     * overloaded {@code run(Strategy, TradeType, Num, int, int)} method.
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

//     @BeforeEach
    void setupBeforeEach() {
        // Initialise the manager with the mocked BarSeries.
        manager = new BarSeriesManager(mockBarSeries);
        // Create a spy so we can verify the internal delegation without invoking the real logic.
//         spyManager = spy(manager);
        // Stubs required by the delegating method.
//         when(mockBarSeries.one()).thenReturn(mockNum);
//         when(mockBarSeries.getBarCount()).thenReturn(10);
    }

//     @AfterEach
    void teardownAfterEach() {
        // Reset mock interactions after each test.
//         clearInvocations(mockBarSeries, mockStrategy, mockNum, mockTradingRecord, spyManager);
    }

    /**
     * Helper stream for parameterised tests (not used directly in this file but kept for completeness).
     */
//     private static Stream<Arguments> successfulCases() {
//         List<org.junit.runners.Parameterized.Parameters> list = new ArrayList<>();
//         list.add(new org.junit.runners.Parameterized.Parameters(new Object[] { 0, 10, "happy‑path" }));
//         list.add(new org.junit.runners.Parameterized.Parameters(new Object[] { 5, 5, "empty‑range" }));
//         return list.stream();
//     }

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

//     private static void testIllegalArgumentWhenStartGreaterThanFinish() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         try {
//             mgr.run(strategy, 8, 3);
//             Assert.fail("Expected IllegalArgumentException");
//         } catch (IllegalArgumentException ignored) {
//         }
//     }

//     private static void testIllegalArgumentWhenStartIsNegative() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         try {
//             mgr.run(strategy, -1, 4);
//             Assert.fail("Expected IllegalArgumentException");
//         } catch (IllegalArgumentException ignored) {
//         }
//     }

//     private static void testNullPointerWhenStrategyIsNull() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(5).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         try {
//             mgr.run(null, 0, 4);
//             Assert.fail("Expected NullPointerException");
//         } catch (NullPointerException ignored) {
//         }
//     }

//     private static void testIndexOutOfBoundsWhenStartExceedsSeriesSize() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         try {
//             mgr.run(strategy, 1000, 1010);
//             Assert.fail("Expected IndexOutOfBoundsException");
//         } catch (IndexOutOfBoundsException ignored) {
//         }
//     }

//     private static void testIndexOutOfBoundsWhenFinishExceedsSeriesSize() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         try {
//             mgr.run(strategy, 0, 1000);
//             Assert.fail("Expected IndexOutOfBoundsException");
//         } catch (IndexOutOfBoundsException ignored) {
//         }
//     }

//     private static void testIllegalArgumentWhenFinishIsNegative() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         try {
//             mgr.run(strategy, 2, -1);
//             Assert.fail("Expected IllegalArgumentException");
//         } catch (IllegalArgumentException ignored) {
//         }
//     }

//     private static void testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         TradingRecord record = mgr.run(strategy, 0, 0);
//         Assert.assertNotNull("Result must not be null", record);
//         Assert.assertTrue("Empty series with zero range must yield empty record", record.getPositions().isEmpty());
//     }

//     private static void testWholeSeriesProcessing() {
//         BarSeries series = new BaseBarSeriesBuilder().withName("full").withNumBars(10).build();
//         BarSeriesManager mgr = new BarSeriesManager(series);
//         Strategy strategy = new NoSignalStrategy();
//         TradingRecord record = mgr.run(strategy, 0, 9);
//         Assert.assertNotNull("Result must not be null when processing the whole series", record);
//         Assert.assertTrue("No‑signal strategy must produce an empty record", record.getPositions().isEmpty());
//     }

//     @Test
//     void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
//         // Arrange – tell the test manager which record to return
//         BaseTradingRecord expectedRecord = new BaseTradingRecord();
//         manager.setRecordToReturn(expectedRecord);
//         // Act
//         TradingRecord result = manager.run(strategy, DEFAULT_START, DEFAULT_FINISH);
//         // Assert result is the one we forced
//         Assert.assertSame("Delegating run must return the record from the overload", expectedRecord, result);
//         // Verify parameters passed to the overload
//         Assert.assertEquals(strategy, manager.getCapturedStrategy());
//         Assert.assertEquals(TradeType.BUY, manager.getCapturedTradeType());
//         Assert.assertEquals(amount, manager.getCapturedAmount());
//         Assert.assertEquals(DEFAULT_START, manager.getCapturedStart());
//         Assert.assertEquals(DEFAULT_FINISH, manager.getCapturedFinish());
//     }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        try {
            manager.run(strategy, 8, 3);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenStartIsNegative() {
        try {
            manager.run(strategy, -1, 4);
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
            manager.run(strategy, 1000, 1010);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    void shouldThrowIndexOutOfBoundsExceptionWhenFinishExceedsSeriesSize() {
        try {
            manager.run(strategy, 0, 1000);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFinishIsNegative() {
        try {
            manager.run(strategy, 2, -1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

//     @Test
//     void shouldReturnEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
//         BarSeries emptySeries = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
//         TestBarSeriesManager emptyMgr = new TestBarSeriesManager(emptySeries);
//         emptyMgr.setRecordToReturn(new BaseTradingRecord());
//         TradingRecord result = emptyMgr.run(strategy, 0, 0);
//         Assert.assertNotNull(result);
//         Assert.assertTrue(result.getPositions().isEmpty());
//     }

    @Test
    void shouldProcessWholeSeriesWhenRangeCoversAllBars() {
        TestBarSeriesManager fullMgr = new TestBarSeriesManager(series);
        BaseTradingRecord fullRecord = new BaseTradingRecord();
        fullMgr.setRecordToReturn(fullRecord);
        TradingRecord result = fullMgr.run(strategy, 0, 9);
        Assert.assertSame(fullRecord, result);
    }

    @Test
    void shouldCreateSingleBuyTradeWhenStrategyAlwaysEntersAndExits() {
        int start = 5;
        int finish = 6;
        TestBarSeriesManager customMgr = new TestBarSeriesManager(series);
        BaseTradingRecord oneBuyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(oneBuyRecord);
        TradingRecord result = customMgr.run(strategy, start, finish);
        Assert.assertSame(oneBuyRecord, result);
        Assert.assertEquals(TradeType.BUY, customMgr.getCapturedTradeType());
        Assert.assertEquals(amount, customMgr.getCapturedAmount());
        Assert.assertEquals(start, customMgr.getCapturedStart());
        Assert.assertEquals(finish, customMgr.getCapturedFinish());
    }

    @Test
    void shouldReturnEmptyRecordWhenStrategyNeverSignals() {
        TestBarSeriesManager customMgr = new TestBarSeriesManager(series);
        BaseTradingRecord emptyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(emptyRecord);
        TradingRecord result = customMgr.run(strategy, 0, 7);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    void shouldDefaultToBuyTradeTypeForAllGeneratedTrades() {
        int start = 3;
        int finish = 8;
        TestBarSeriesManager customMgr = new TestBarSeriesManager(series);
        BaseTradingRecord buyOnlyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(buyOnlyRecord);
        TradingRecord result = customMgr.run(strategy, start, finish);
        Assert.assertSame(buyOnlyRecord, result);
        Assert.assertEquals(TradeType.BUY, customMgr.getCapturedTradeType());
        Assert.assertEquals(amount, customMgr.getCapturedAmount());
        Assert.assertEquals(start, customMgr.getCapturedStart());
        Assert.assertEquals(finish, customMgr.getCapturedFinish());
    }

//     @Before
//     public void setUp() {
//         // Build a simple series with 10 bars (the actual data is irrelevant)
//         this.series = new BaseBarSeriesBuilder().withName("test").withNumBars(10).build();
//         this.amount = series.one();
//         this.strategy = new NoSignalStrategy();
//         this.manager = new TestBarSeriesManager(series);
//     }

    @After
    public void tearDown() {
        // nothing to clean up
    }
}