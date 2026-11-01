```java
package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BarSeriesManager#run(Strategy, int, int)}.
 */
public class BarSeriesManager_run_4_Test {

    private BarSeries mockBarSeries;
    private Strategy mockStrategy;
    private TradingRecord mockTradingRecord;
    private Num mockNum;
    private BarSeriesManager manager;

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

    @Before
    public void setUp() {
        // Build a simple series with 10 bars (the actual data is irrelevant for these tests)
        this.mockBarSeries = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumBars(10)
                .build();

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
    public void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
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
    public void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        try {
            manager.run(mockStrategy, 8, 3);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStartIsNegative() {
        try {
            manager.run(mockStrategy, -1, 4);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenStrategyIsNull() {
        try {
            manager.run(null, 0, 5);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void shouldThrowIndexOutOfBoundsExceptionWhenStartExceedsSeriesSize() {
        try {
            manager.run(mockStrategy, 1000, 1010);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void shouldThrowIndexOutOfBoundsExceptionWhenFinishExceedsSeriesSize() {
        try {
            manager.run(mockStrategy, 0, 1000);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenFinishIsNegative() {
        try {
            manager.run(mockStrategy, 2, -1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void shouldReturnEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        // Simulate an empty series by creating a new manager with a zero‑size series
        BarSeries emptySeries = new BaseBarSeriesBuilder().withName("empty").withNumBars(0).build();
        TestBarSeriesManager emptyMgr = new TestBarSeriesManager(emptySeries);
        emptyMgr.setRecordToReturn(new BaseTradingRecord());
        TradingRecord result = emptyMgr.run(mockStrategy, 0, 0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    public void shouldProcessWholeSeriesWhenRangeCoversAllBars() {
        TestBarSeriesManager fullMgr = new TestBarSeriesManager(mockBarSeries);
        TradingRecord fullRecord = new BaseTradingRecord();
        fullMgr.setRecordToReturn(fullRecord);
        TradingRecord result = fullMgr.run(mockStrategy, 0, 9);
        Assert.assertSame(fullRecord, result);
    }

    @Test
    public void shouldCreateSingleBuyTradeWhenStrategyAlwaysEntersAndExits() {
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
    public void shouldReturnEmptyRecordWhenStrategyNeverSignals() {
        TestBarSeriesManager customMgr = new TestBarSeriesManager(mockBarSeries);
        TradingRecord emptyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(emptyRecord);
        TradingRecord result = customMgr.run(mockStrategy, 0, 7);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    public void shouldDefaultToBuyTradeTypeForAllGeneratedTrades() {
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
}
```