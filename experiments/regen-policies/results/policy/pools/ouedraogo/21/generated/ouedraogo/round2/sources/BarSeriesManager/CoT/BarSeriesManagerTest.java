package org.ta4j.core.backtest;
import org.ta4j.core.num.NumFactory;


import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * <p>
 * The tests use simple in‑memory implementations of the library interfaces
 * (BarSeries, Num, NumFactory, Strategy and TradeExecutionModel).  No mocking
 * framework is used – only concrete classes that are defined inside this test
 * file.
 * </p>
 */
public class BarSeriesManagerTest {

    /** Simple {@link Num} implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {

        private final BigDecimal value;

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        SimpleNum(Number n) {
            this.value = new BigDecimal(n.toString());
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return SimpleNumFactory.INSTANCE; }
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num plus(Num augend) { return new SimpleNum(value.add(((SimpleNum) augend).value)); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(value.subtract(((SimpleNum) subtrahend).value)); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(value.multiply(((SimpleNum) multiplicand).value)); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(value.divide(((SimpleNum) divisor).value, BigDecimal.ROUND_HALF_UP)); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(value.remainder(((SimpleNum) divisor).value)); }
        @Override public Num floor() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_FLOOR)); }
        @Override public Num ceil() { return new SimpleNum(value.setScale(0, BigDecimal.ROUND_CEILING)); }
        @Override public Num pow(int n) { return new SimpleNum(value.pow(n)); }
        @Override public Num pow(Num n) { return new SimpleNum(value.pow(((SimpleNum) n).value.intValue())); }
        @Override public Num log() { throw new UnsupportedOperationException(); }
        @Override public Num sqrt() { throw new UnsupportedOperationException(); }
        @Override public Num sqrt(int precision) { throw new UnsupportedOperationException(); }
        @Override public Num abs() { return new SimpleNum(value.abs()); }
        @Override public Num negate() { return new SimpleNum(value.negate()); }
        @Override public boolean isZero() { return value.compareTo(BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive() { return value.compareTo(BigDecimal.ZERO) > 0; }
        @Override public boolean isPositiveOrZero() { return value.compareTo(BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegative() { return value.compareTo(BigDecimal.ZERO) < 0; }
        @Override public boolean isNegativeOrZero() { return value.compareTo(BigDecimal.ZERO) <= 0; }
        @Override public boolean isEqual(Num other) { return value.compareTo(((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return value.compareTo(((SimpleNum) other).value) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return value.compareTo(((SimpleNum) other).value) >= 0; }
        @Override public boolean isLessThan(Num other) { return value.compareTo(((SimpleNum) other).value) < 0; }
        @Override public boolean isLessThanOrEqual(Num other) { return value.compareTo(((SimpleNum) other).value) <= 0; }
        @Override public Num min(Num other) { return value.compareTo(((SimpleNum) other).value) <= 0 ? this : other; }
        @Override public Num max(Num other) { return value.compareTo(((SimpleNum) other).value) >= 0 ? this : other; }
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public java.math.BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toPlainString(); }
        @Override public boolean equals(Object obj) { return (obj instanceof SimpleNum) && value.equals(((SimpleNum) obj).value); }
    }

    /** Factory returning {@link SimpleNum} instances. */
    private static class SimpleNumFactory implements org.ta4j.core.num.NumFactory {
        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();

        @Override public Num numOf(Number number) { return new SimpleNum(number); }
        @Override public Num one() { return new SimpleNum(BigDecimal.ONE); }
        @Override public Num zero() { return new SimpleNum(BigDecimal.ZERO); }
    }

    /** Very small in‑memory {@link BarSeries} implementation. Only the methods used by
     *  {@link BarSeriesManager} are implemented. */
    private static class SimpleBarSeries implements BarSeries {

        private final List<Num> prices;
        private final SimpleNumFactory factory = SimpleNumFactory.INSTANCE;

        SimpleBarSeries(List<Number> priceValues) {
            List<Num> tmp = new ArrayList<>();
            for (Number n : priceValues) {
                tmp.add(factory.numOf(n));
            }
            this.prices = Collections.unmodifiableList(tmp);
        }

        @Override public org.ta4j.core.num.NumFactory numFactory() { return factory; }
        @Override public org.ta4j.core.BaseBarConvertibleBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "simple-series"; }
        @Override public org.ta4j.core.Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return prices.size(); }
        @Override public List<org.ta4j.core.Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return prices.isEmpty() ? -1 : prices.size() - 1; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(org.ta4j.core.Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        // default methods are inherited from the interface – they are not needed in the tests
    }

    /** A {@link Strategy} that can be configured to return a predetermined
     *  value for {@link Strategy#shouldOperate(int, TradingRecord)}. */
    private static class SimpleStrategy implements Strategy {

        private final boolean operate;

        SimpleStrategy(boolean operate) {
            this.operate = operate;
        }

        @Override public String getName() { return "simple-strategy"; }
        @Override public org.ta4j.core.Rule getEntryRule() { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.Rule getExitRule() { throw new UnsupportedOperationException(); }
        @Override public Strategy and(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) { }
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operate;
        }
    }

    /** A {@link TradeExecutionModel} that simply records the indexes on which it
     *  was called and performs a naive open/close operation:
     *  <ul>
     *      <li>first call → opens a trade (enter)</li>
     *      <li>second call → closes the trade (exit)</li>
     *  </ul>
     */
    private static class RecordingTradeExecutionModel implements TradeExecutionModel {

        private final List<Integer> calledIndexes = new ArrayList<>();

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            calledIndexes.add(index);
            // Very simple logic: if there is no open position, enter; otherwise exit.
            if (tradingRecord.getCurrentPosition() == null || tradingRecord.getCurrentPosition().isClosed()) {
                tradingRecord.enter(index, barSeries.numFactory().one(), amount);
            } else {
                tradingRecord.exit(index, barSeries.numFactory().one(), amount);
            }
        }

        List<Integer> getCalledIndexes() {
            return calledIndexes;
        }
    }

    private BarSeries series;
    private CostModel zeroCost;

    @Before
    public void setUp() {
        // series with three price points (indices 0,1,2)
        series = new SimpleBarSeries(java.util.Arrays.asList(100, 101, 102));
        zeroCost = new ZeroCostModel();
    }

    /* --------------------------------------------------------------------- */
    /*  1.  Public method extraction (for documentation)                     */
    /* --------------------------------------------------------------------- */

    // The public methods of BarSeriesManager are:
    //   BarSeries getBarSeries()
    //   CostModel getTransactionCostModel()
    //   CostModel getHoldingCostModel()
    //   TradingRecord run(Strategy strategy)
    //   TradingRecord run(Strategy strategy, int startIndex, int finishIndex)
    //   TradingRecord run(Strategy strategy, TradeType tradeType)
    //   TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex)
    //   TradingRecord run(Strategy strategy, TradeType tradeType, Num amount)
    //   TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex)

    /* --------------------------------------------------------------------- */
    /*  2.  Basic functionality tests                                      */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetBarSeries() {
        BarSeriesManager mgr = new BarSeriesManager(series);
        assertSame("getBarSeries must return the instance supplied to the constructor", series, mgr.getBarSeries());
    }

    @Test
    public void testGetTransactionCostModel() {
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost);
        assertSame(zeroCost, mgr.getTransactionCostModel());
    }

    @Test
    public void testGetHoldingCostModel() {
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost);
        assertSame(zeroCost, mgr.getHoldingCostModel());
    }

    @Test
    public void testRunWithNeverOperatingStrategy() {
        // Strategy never returns true → no trades should be created
        Strategy never = new SimpleStrategy(false);
        BarSeriesManager mgr = new BarSeriesManager(series);
        TradingRecord record = mgr.run(never);
        assertEquals("No trades expected", 0, record.getTrades().size());
        assertTrue("Record should be considered closed when no open position exists", record.isClosed());
    }

    @Test
    public void testRunDefaultBuyOpensAndCloses() {
        // Strategy always returns true → the execution model will open on the first call
        // and close on the second call.
        Strategy always = new SimpleStrategy(true);
        RecordingTradeExecutionModel exec = new RecordingTradeExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost, exec);
        TradingRecord record = mgr.run(always);
        // Two calls expected: first index 0, second index 1 (or later) – we only care that at least two were called
        assertTrue("Execution model should have been called at least twice", exec.getCalledIndexes().size() >= 2);
        assertEquals("Exactly one position should have been created", 1, record.getPositions().size());
        assertTrue("The position must be closed after the run", record.isClosed());
    }

    @Test
    public void testRunWithCustomTradeTypeAndAmount() {
        Strategy always = new SimpleStrategy(true);
        RecordingTradeExecutionModel exec = new RecordingTradeExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost, exec);
        Num amount = series.numFactory().numOf(5);
        TradingRecord record = mgr.run(always, TradeType.SELL, amount);
        // The same expectations as the previous test – the trade type only influences the first trade.
        assertEquals(1, record.getPositions().size());
        assertTrue(record.isClosed());
        // Verify that the first trade stored in the record has the correct type (SELL)
        assertEquals(TradeType.SELL, record.getTrades().get(0).getType());
    }

    /* --------------------------------------------------------------------- */
    /*  3.  Edge‑case / exception tests                                      */
    /* --------------------------------------------------------------------- */

    @Test
    public void testRunWithStartAfterEndIndexes() {
        // start > finish – loop should not execute, record stays empty.
        Strategy always = new SimpleStrategy(true);
        BarSeriesManager mgr = new BarSeriesManager(series);
        TradingRecord record = mgr.run(always, 5, 2); // startIndex 5, finishIndex 2
        assertEquals("No trades expected when startIndex > finishIndex", 0, record.getTrades().size());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithStartBeyondSeries() {
        // start index larger than series end – nothing should happen.
        Strategy always = new SimpleStrategy(true);
        BarSeriesManager mgr = new BarSeriesManager(series);
        TradingRecord record = mgr.run(always, 10, 15);
        assertEquals(0, record.getTrades().size());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithNegativeIndexes() {
        // Negative indexes are tolerated – they are clamped to series begin.
        Strategy always = new SimpleStrategy(true);
        BarSeriesManager mgr = new BarSeriesManager(series);
        TradingRecord record = mgr.run(always, -5, -1);
        // Even though the indexes are negative, they are adjusted to 0..0 (single bar)
        // The execution model will be called once (open) but there is no later bar to close,
        // so the position stays open. The manager then tries to close it using extra bars
        // after the series end – there are none, so the position remains open.
        assertEquals(1, record.getPositions().size());
        assertFalse("Position should remain open because there is no further bar to close", record.isClosed());
    }

    @Test(expected = NullPointerException.class)
    public void testRunWithNullStrategyThrowsException() {
        BarSeriesManager mgr = new BarSeriesManager(series);
        mgr.run(null);
    }

    @Test
    public void testRunWithZeroAmountDoesNotFail() {
        Strategy always = new SimpleStrategy(true);
        BarSeriesManager mgr = new BarSeriesManager(series);
        Num zero = series.numFactory().zero();
        TradingRecord record = mgr.run(always, TradeType.BUY, zero);
        // Even with zero amount the manager should complete without throwing.
        // The execution model will still be called (it may open a trade with zero amount).
        assertNotNull(record);
    }

    @Test
    public void testRunWithCustomExecutionModelThatNeverCloses() {
        // Execution model opens a trade but never closes it.
        Strategy always = new SimpleStrategy(true);
        TradeExecutionModel neverClose = (index, tradingRecord, barSeries, amount) -> {
            if (tradingRecord.getCurrentPosition() == null) {
                tradingRecord.enter(index, barSeries.numFactory().one(), amount);
            }
            // deliberately do nothing on subsequent calls
        };
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost, neverClose);
        TradingRecord record = mgr.run(always);
        assertEquals(1, record.getPositions().size());
        assertFalse("Position must stay open because the execution model never closes it", record.isClosed());
    }

    @Test
    public void testRunWithPartialRange() {
        // Run only over a subset of the series (indexes 1..2)
        Strategy always = new SimpleStrategy(true);
        RecordingTradeExecutionModel exec = new RecordingTradeExecutionModel();
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost, exec);
        TradingRecord record = mgr.run(always, TradeType.BUY, 1, 2);
        // The execution model should have been called for indexes 1 and 2 only.
        List<Integer> called = exec.getCalledIndexes();
        assertTrue(called.contains(1));
        assertTrue(called.contains(2));
        // Ensure no call for index 0
        assertFalse(called.contains(0));
        // One position should be opened and then closed inside the range.
        assertEquals(1, record.getPositions().size());
        assertTrue(record.isClosed());
    }

}
