### Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests use tiny in‑memory stub implementations of the library interfaces
 * (BarSeries, Strategy, TradeExecutionModel and Num).  They are deliberately
 * simple – they only implement the methods that are exercised by the
 * {@code BarSeriesManager} code – so that the behaviour of the manager can be
 * verified without pulling the whole ta4j library.
 */
public class BarSeriesManagerTest {

    /* --------------------------------------------------------------------- */
    /*  Minimal Num implementation (required by BarSeries)                 */
    /* --------------------------------------------------------------------- */
    private static class SimpleNum implements Num {
        private final double value;

        SimpleNum(double value) { this.value = value; }

        @Override public Num zero() { return new SimpleNum(0); }
        @Override public Num one() { return new SimpleNum(1); }
        @Override public Num hundred() { return new SimpleNum(100); }
        @Override public Number getDelegate() { return value; }
        @Override public String getName() { return Double.toString(value); }
        @Override public Num plus(Num augend) { return new SimpleNum(this.value + ((SimpleNum)augend).value); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value - ((SimpleNum)subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value * ((SimpleNum)multiplicand).value); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value / ((SimpleNum)divisor).value); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(this.value % ((SimpleNum)divisor).value); }
        @Override public Num floor() { return new SimpleNum(Math.floor(this.value)); }
        @Override public Num ceil() { return new SimpleNum(Math.ceil(this.value)); }
        @Override public Num pow(int n) { return new SimpleNum(Math.pow(this.value, n)); }
        @Override public Num pow(Num n) { return new SimpleNum(Math.pow(this.value, ((SimpleNum)n).value)); }
        @Override public Num log() { return new SimpleNum(Math.log(this.value)); }
        @Override public Num sqrt() { return new SimpleNum(Math.sqrt(this.value)); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs() { return new SimpleNum(Math.abs(this.value)); }
        @Override public Num negate() { return new SimpleNum(-this.value); }
        @Override public boolean isZero() { return this.value == 0; }
        @Override public boolean isPositive() { return this.value > 0; }
        @Override public boolean isPositiveOrZero() { return this.value >= 0; }
        @Override public boolean isNegative() { return this.value < 0; }
        @Override public boolean isNegativeOrZero() { return this.value <= 0; }
        @Override public boolean isEqual(Num other) { return this.value == ((SimpleNum)other).value; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum)other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= ((SimpleNum)other).value; }
        @Override public boolean isLessThan(Num other) { return this.value < ((SimpleNum)other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= ((SimpleNum)other).value; }
        @Override public Num min(Num other) { return this.value <= ((SimpleNum)other).value ? this : other; }
        @Override public Num max(Num other) { return this.value >= ((SimpleNum)other).value ? this : other; }
        @Override public java.util.function.Function<Number, Num> function() { return SimpleNum::new; }
        @Override public Num numOf(Number number) { return new SimpleNum(number.doubleValue()); }
        @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN() { return Double.isNaN(this.value); }
        @Override public double doubleValue() { return this.value; }
        @Override public int intValue() { return (int) this.value; }
        @Override public long longValue() { return (long) this.value; }
        @Override public float floatValue() { return (float) this.value; }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return obj instanceof SimpleNum && ((SimpleNum)obj).value == this.value;
        }
        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum)o).value); }
    }

    /* --------------------------------------------------------------------- */
    /*  Very small BarSeries stub (only the methods used by the manager)   */
    /* --------------------------------------------------------------------- */
    private static class SimpleBarSeries implements BarSeries {
        private final List<Object> bars = new ArrayList<>();

        SimpleBarSeries(int size) {
            for (int i = 0; i < size; i++) {
                bars.add(new Object()); // placeholder bar
            }
        }

        @Override public String getName() { return "simple-series"; }
        @Override public Num num() { return new SimpleNum(0); } // the factory; one() will be delegated
        @Override public Bar getBar(int i) { return null; }   // not used in tests
        @Override public int getBarCount() { return bars.size(); }
        @Override public List<Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return bars.isEmpty() ? -1 : bars.size() - 1; }
        // -----------------------------------------------------------------
        // The remaining default methods are left untouched – the interface
        // supplies default implementations for them.
        // -----------------------------------------------------------------
        @Override public void addBar(Bar bar) {}
        @Override public void addBar(Bar bar, boolean replace) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                     Number lowPrice, Number closePrice) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                     Number lowPrice, Number closePrice, Number volume) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                     Number lowPrice, Number closePrice, Number volume, Number amount) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice,
                                     Number highPrice, Number lowPrice, Number closePrice, Number volume) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Number openPrice,
                                     Number highPrice, Number lowPrice, Number closePrice, Number volume,
                                     Number amount) {}
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                     String lowPrice, String closePrice) {}
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                     String lowPrice, String closePrice, String volume) {}
        @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                     String lowPrice, String closePrice, String volume, String amount) {}
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                     Num lowPrice, Num closePrice, Num volume) {}
        @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                     Num lowPrice, Num closePrice, Num volume, Num amount) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice,
                                     Num highPrice, Num lowPrice, Num closePrice, Num volume) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice,
                                     Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(String tradeVolume, String tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(String price) {}
        @Override public void addPrice(Number price) {}
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return null; }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return 0; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
    }

    /* --------------------------------------------------------------------- */
    /*  Simple Strategy – behaviour is fully controllable from the test    */
    /* --------------------------------------------------------------------- */
    private static class SimpleStrategy implements Strategy {
        private final Set<Integer> operateIndexes;
        SimpleStrategy(Set<Integer> operateIndexes) {
            this.operateIndexes = operateIndexes;
        }
        @Override public String getName() { return "simple-strategy"; }
        @Override public Rule getEntryRule() { return null; }
        @Override public Rule getExitRule() { return null; }
        @Override public Strategy and(Strategy strategy) { return null; }
        @Override public Strategy or(Strategy strategy) { return null; }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy opposite() { return null; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
        @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operateIndexes.contains(index);
        }
        @Override public boolean shouldEnter(int index) { return false; }
        @Override public boolean shouldEnter(int index, TradingRecord tradingRecord) { return false; }
        @Override public boolean shouldExit(int index) { return false; }
        @Override public boolean shouldExit(int index, TradingRecord tradingRecord) { return false; }
    }

    /* --------------------------------------------------------------------- */
    /*  TradeExecutionModel that just records the indices it was asked to   */
    /*  execute.                                                             */
    /* --------------------------------------------------------------------- */
    private static class RecordingTradeExecutionModel implements TradeExecutionModel {
        final List<Integer> executedIndexes = new ArrayList<>();

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            executedIndexes.add(index);
            // Perform a very naive operation: if the current position is closed,
            // we open a new one; otherwise we close it.
            if (tradingRecord.isClosed()) {
                tradingRecord.enter(index, barSeries.num().one(), amount);
            } else {
                tradingRecord.exit(index, barSeries.num().one(), amount);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Test fixtures                                                       */
    /* --------------------------------------------------------------------- */
    private SimpleBarSeries series5;          // 5‑bar series (indexes 0‑4)
    private SimpleBarSeries emptySeries;      // series with 0 bars
    private CostModel zeroCost;
    private RecordingTradeExecutionModel execModel;

    @Before
    public void setUp() {
        series5 = new SimpleBarSeries(5);
        emptySeries = new SimpleBarSeries(0);
        zeroCost = new ZeroCostModel();
        execModel = new RecordingTradeExecutionModel();
    }

    /* --------------------------------------------------------------------- */
    /*  1. Public methods – signatures (extracted manually)                */
    /* --------------------------------------------------------------------- */
    // The signatures are listed here for documentation purposes.
    // --------------------------------------------------------------
    // BarSeries getBarSeries()
    // CostModel getTransactionCostModel()
    // CostModel getHoldingCostModel()
    // TradingRecord run(Strategy strategy)
    // TradingRecord run(Strategy strategy, int startIndex, int finishIndex)
    // TradingRecord run(Strategy strategy, TradeType tradeType)
    // TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex)
    // TradingRecord run(Strategy strategy, TradeType tradeType, Num amount)
    // TradingRecord run(Strategy strategy, TradeType tradeType, Num amount,
    //                  int startIndex, int finishIndex)

    /* --------------------------------------------------------------------- */
    /*  2. Basic functionality tests                                        */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetBarSeries() {
        BarSeriesManager mgr = new BarSeriesManager(series5);
        assertSame("getBarSeries should return the instance supplied to the constructor",
                series5, mgr.getBarSeries());
    }

    @Test
    public void testGetTransactionCostModel() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        assertSame(zeroCost, mgr.getTransactionCostModel());
    }

    @Test
    public void testGetHoldingCostModel() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        assertSame(zeroCost, mgr.getHoldingCostModel());
    }

    @Test
    public void testRunWithDefaultBuy() {
        // Strategy operates on every index (0‑4)
        SimpleStrategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(0,1,2,3,4)));
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        TradingRecord record = mgr.run(strategy);
        assertNotNull(record);
        // Because the strategy toggles entry/exit on each call, we expect
        // 5 executions -> 3 entries (0,2,4) and 2 exits (1,3) → 5 trades total.
        assertEquals(5, execModel.executedIndexes.size());
    }

    @Test
    public void testRunWithCustomTradeTypeAndAmount() {
        SimpleStrategy strategy = new SimpleStrategy(Collections.singleton(0));
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        Num amount = new SimpleNum(10);
        TradingRecord record = mgr.run(strategy, TradeType.SELL, amount);
        assertNotNull(record);
        // only index 0 should have been executed
        assertEquals(Collections.singletonList(0), execModel.executedIndexes);
    }

    @Test
    public void testRunWithExplicitStartAndFinishIndexes() {
        // Operate only on index 2 and 3
        SimpleStrategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(2,3)));
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        TradingRecord record = mgr.run(strategy, TradeType.BUY, 1, 3);
        assertNotNull(record);
        // Expected executions: 2 and 3
        assertEquals(Arrays.asList(2,3), execModel.executedIndexes);
    }

    /* --------------------------------------------------------------------- */
    /*  3. Edge‑case / exception tests                                      */
    /* --------------------------------------------------------------------- */

    @Test
    public void testRunWithStartIndexGreaterThanEndIndex() {
        SimpleStrategy strategy = new SimpleStrategy(Collections.singleton(0));
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        // start > finish → the manager will swap them via Math.max/min,
        // resulting in a run where runBeginIndex > runEndIndex → loop never runs.
        TradingRecord record = mgr.run(strategy, TradeType.BUY, 4, 2);
        assertNotNull(record);
        assertTrue("No execution should happen when startIndex > finishIndex", execModel.executedIndexes.isEmpty());
    }

    @Test
    public void testRunWithIndexesOutsideSeriesBounds() {
        SimpleStrategy strategy = new SimpleStrategy(new HashSet<>(Arrays.asList(0,1,2,3,4,5,6)));
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        // Provide indexes that exceed the series limits.
        TradingRecord record = mgr.run(strategy, TradeType.BUY, -10, 20);
        assertNotNull(record);
        // The manager clamps the range to 0‑4, therefore only those 5 indexes are processed.
        assertEquals(5, execModel.executedIndexes.size());
        assertEquals(Arrays.asList(0,1,2,3,4), execModel.executedIndexes);
    }

    @Test
    public void testRunOnEmptySeriesDoesNotThrow() {
        SimpleStrategy strategy = new SimpleStrategy(Collections.singleton(0));
        BarSeriesManager mgr = new BarSeriesManager(emptySeries, execModel);
        TradingRecord record = mgr.run(strategy);
        assertNotNull(record);
        // No bars → no execution
        assertTrue(execModel.executedIndexes.isEmpty());
    }

    @Test
    public void testRunWhenStrategyNeverOperates() {
        SimpleStrategy strategy = new SimpleStrategy(Collections.emptySet());
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        TradingRecord record = mgr.run(strategy);
        assertNotNull(record);
        assertTrue("Strategy never returns true → no trade execution expected", execModel.executedIndexes.isEmpty());
    }

    /* --------------------------------------------------------------------- */
    /*  4. Additional behavioural verification (closing open position)     */
    /* --------------------------------------------------------------------- */
    @Test
    public void testRunLeavesOpenPositionAndAttemptsCloseAfterEnd() {
        // Strategy operates only on the very last bar (index 4). It will open a trade
        // but never close it inside the main loop. After the loop the manager will
        // look one step beyond the series end to try to close the position.
        SimpleStrategy strategy = new SimpleStrategy(Collections.singleton(4));
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        TradingRecord record = mgr.run(strategy);
        // The execution model will have been called twice:
        //  - index 4  -> opens (enter)
        //  - index 5* -> manager's extra loop attempts a close (exit)
        // Since our SimpleBarSeries reports getBarData().size() == 0, the extra loop
        // will not run. To make the extra loop execute we need getBarData().size()
        // > endIndex. We therefore replace the series with a subclass that reports
        // a larger barData size.
        SimpleBarSeries seriesWithExtraData = new SimpleBarSeries(5) {
            @Override public List<Bar> getBarData() { return Arrays.asList(null,null,null,null,null,null); }
        };
        BarSeriesManager mgr2 = new BarSeriesManager(seriesWithExtraData, execModel);
        execModel.executedIndexes.clear();
        TradingRecord rec2 = mgr2.run(strategy);
        assertEquals("Two executions expected (enter at 4, close at 5)", 2, execModel.executedIndexes.size());
        assertEquals(Integer.valueOf(4), execModel.executedIndexes.get(0));
        assertEquals(Integer.valueOf(5), execModel.executedIndexes.get(1));
    }
}
```
### Test END##