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

/**
 * Unit tests for {@link BarSeriesManager#run(Strategy, int, int)}.
 *
 * The tests avoid external libraries (Mockito, JUnit‑Jupiter) and rely only on
 * the TA4J core classes and JUnit 4.
 */
public class BarSeriesManager_run_4_Test {

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
        public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount,
                                int startIndex, int finishIndex) {
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

    private BarSeries series;
    private NoSignalStrategy strategy;
    private Num amount;
    private TestBarSeriesManager manager;

    @Before
    public void setUp() {
        // Build a simple series with 10 bars (the actual data is irrelevant)
        this.series = new BaseBarSeriesBuilder()
                .withName("test")
                .withNumBars(10)
                .build();

        this.amount = series.one();
        this.strategy = new NoSignalStrategy();
        this.manager = new TestBarSeriesManager(series);
    }

    @After
    public void tearDown() {
        // nothing to clean up
    }

    @Test
    public void shouldDelegateToOverloadedRunWithBuyAndOneNum() {
        // Arrange – tell the test manager which record to return
        BaseTradingRecord expectedRecord = new BaseTradingRecord();
        manager.setRecordToReturn(expectedRecord);

        // Act
        TradingRecord result = manager.run(strategy, DEFAULT_START, DEFAULT_FINISH);

        // Assert result is the one we forced
        Assert.assertSame("Delegating run must return the record from the overload",
                expectedRecord, result);

        // Verify parameters passed to the overload
        Assert.assertEquals(strategy, manager.getCapturedStrategy());
        Assert.assertEquals(TradeType.BUY, manager.getCapturedTradeType());
        Assert.assertEquals(amount, manager.getCapturedAmount());
        Assert.assertEquals(DEFAULT_START, manager.getCapturedStart());
        Assert.assertEquals(DEFAULT_FINISH, manager.getCapturedFinish());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStartGreaterThanFinish() {
        try {
            manager.run(strategy, 8, 3);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenStartIsNegative() {
        try {
            manager.run(strategy, -1, 4);
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
            manager.run(strategy, 1000, 1010);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void shouldThrowIndexOutOfBoundsExceptionWhenFinishExceedsSeriesSize() {
        try {
            manager.run(strategy, 0, 1000);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenFinishIsNegative() {
        try {
            manager.run(strategy, 2, -1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void shouldReturnEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        BarSeries emptySeries = new BaseBarSeriesBuilder()
                .withName("empty")
                .withNumBars(0)
                .build();
        TestBarSeriesManager emptyMgr = new TestBarSeriesManager(emptySeries);
        emptyMgr.setRecordToReturn(new BaseTradingRecord());

        TradingRecord result = emptyMgr.run(strategy, 0, 0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    public void shouldProcessWholeSeriesWhenRangeCoversAllBars() {
        TestBarSeriesManager fullMgr = new TestBarSeriesManager(series);
        BaseTradingRecord fullRecord = new BaseTradingRecord();
        fullMgr.setRecordToReturn(fullRecord);

        TradingRecord result = fullMgr.run(strategy, 0, 9);
        Assert.assertSame(fullRecord, result);
    }

    @Test
    public void shouldCreateSingleBuyTradeWhenStrategyAlwaysEntersAndExits() {
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
    public void shouldReturnEmptyRecordWhenStrategyNeverSignals() {
        TestBarSeriesManager customMgr = new TestBarSeriesManager(series);
        BaseTradingRecord emptyRecord = new BaseTradingRecord();
        customMgr.setRecordToReturn(emptyRecord);

        TradingRecord result = customMgr.run(strategy, 0, 7);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getPositions().isEmpty());
    }

    @Test
    public void shouldDefaultToBuyTradeTypeForAllGeneratedTrades() {
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
}
```