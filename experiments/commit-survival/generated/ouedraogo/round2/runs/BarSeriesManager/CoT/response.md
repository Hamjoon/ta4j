###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link BarSeriesManager}.
 *
 * The tests use very small stub implementations of the TA‑4J interfaces
 * (BarSeries, Strategy, TradeExecutionModel, CostModel, Num) so that they
 * can be compiled and executed without pulling the whole library.
 *
 * The goal is to exercise every public method of {@code BarSeriesManager},
 * covering normal behaviour as well as edge cases such as empty series,
 * out‑of‑range indexes and strategies that never trigger.
 */
public class BarSeriesManagerTest {

    /** Simple numeric implementation used by the stubs */
    private static final class SimpleNum implements Num {

        private static final SimpleNum ZERO = new SimpleNum(0d);
        private static final SimpleNum ONE = new SimpleNum(1d);
        private static final SimpleNum HUNDRED = new SimpleNum(100d);

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        static SimpleNum valueOf(double v) {
            return new SimpleNum(v);
        }

        @Override
        public Num zero() {
            return ZERO;
        }

        @Override
        public Num one() {
            return ONE;
        }

        @Override
        public Num hundred() {
            return HUNDRED;
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public String getName() {
            return Double.toString(value);
        }

        @Override
        public Num plus(Num augend) {
            return valueOf(this.value + ((SimpleNum) augend).value);
        }

        @Override
        public Num minus(Num subtrahend) {
            return valueOf(this.value - ((SimpleNum) subtrahend).value);
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return valueOf(this.value * ((SimpleNum) multiplicand).value);
        }

        @Override
        public Num dividedBy(Num divisor) {
            return valueOf(this.value / ((SimpleNum) divisor).value);
        }

        @Override
        public Num remainder(Num divisor) {
            return valueOf(this.value % ((SimpleNum) divisor).value);
        }

        @Override
        public Num floor() {
            return valueOf(Math.floor(value));
        }

        @Override
        public Num ceil() {
            return valueOf(Math.ceil(value));
        }

        @Override
        public Num pow(int n) {
            return valueOf(Math.pow(value, n));
        }

        @Override
        public Num pow(Num n) {
            return valueOf(Math.pow(value, ((SimpleNum) n).value));
        }

        @Override
        public Num log() {
            return valueOf(Math.log(value));
        }

        @Override
        public Num sqrt() {
            return valueOf(Math.sqrt(value));
        }

        @Override
        public Num sqrt(int precision) {
            return sqrt();
        }

        @Override
        public Num abs() {
            return valueOf(Math.abs(value));
        }

        @Override
        public Num negate() {
            return valueOf(-value);
        }

        @Override
        public boolean isZero() {
            return value == 0d;
        }

        @Override
        public boolean isPositive() {
            return value > 0d;
        }

        @Override
        public boolean isPositiveOrZero() {
            return value >= 0d;
        }

        @Override
        public boolean isNegative() {
            return value < 0d;
        }

        @Override
        public boolean isNegativeOrZero() {
            return value <= 0d;
        }

        @Override
        public boolean isEqual(Num other) {
            return Double.compare(value, ((SimpleNum) other).value) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return value > ((SimpleNum) other).value;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return value >= ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThan(Num other) {
            return value < ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return value <= ((SimpleNum) other).value;
        }

        @Override
        public Num min(Num other) {
            return value <= ((SimpleNum) other).value ? this : other;
        }

        @Override
        public Num max(Num other) {
            return value >= ((SimpleNum) other).value ? this : other;
        }

        @Override
        public java.util.function.Function<Number, Num> function() {
            return n -> valueOf(n.doubleValue());
        }

        @Override
        public Num numOf(Number number) {
            return valueOf(number.doubleValue());
        }

        @Override
        public Num numOf(String value, int precision) {
            return valueOf(Double.parseDouble(value));
        }

        @Override
        public boolean isNaN() {
            return Double.isNaN(value);
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public int intValue() {
            return (int) value;
        }

        @Override
        public long longValue() {
            return (long) value;
        }

        @Override
        public float floatValue() {
            return (float) value;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }

        @Override
        public int compareTo(Num o) {
            return Double.compare(this.value, ((SimpleNum) o).value);
        }
    }

    /** Minimal Bar implementation – only needed for the BarSeries contract */
    private static final class SimpleBar {
        // No fields required for the current tests
    }

    /** Very small BarSeries implementation sufficient for the manager tests */
    private static final class SimpleBarSeries implements BarSeries {

        private final List<SimpleBar> bars = new ArrayList<>();

        /** Adds a dummy bar – the actual content is irrelevant for the manager */
        void addDummyBar() {
            bars.add(new SimpleBar());
        }

        @Override
        public String getName() {
            return "simple-series";
        }

        @Override
        public Num num() {
            return SimpleNum.ONE;
        }

        @Override
        public Bar getBar(int i) {
            return new SimpleBar(); // not used in the manager
        }

        @Override
        public int getBarCount() {
            return bars.size();
        }

        @Override
        public List getBarData() {
            return Collections.unmodifiableList(bars);
        }

        @Override
        public int getBeginIndex() {
            return bars.isEmpty() ? 0 : 0;
        }

        @Override
        public int getEndIndex() {
            return bars.isEmpty() ? -1 : bars.size() - 1;
        }

        @Override
        public Num zero() {
            return SimpleNum.ZERO;
        }

        @Override
        public Num one() {
            return SimpleNum.ONE;
        }

        @Override
        public Num hundred() {
            return SimpleNum.HUNDRED;
        }

        @Override
        public Num numOf(Number number) {
            return SimpleNum.valueOf(number.doubleValue());
        }

        // -----------------------------------------------------------------
        // The remaining default methods are not required for the tests.
        // -----------------------------------------------------------------
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return 0; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
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
                Number highPrice, Number lowPrice, Number closePrice, Number volume, Number amount) {}
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
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return this; }
        @Override public boolean isEmpty() { return bars.isEmpty(); }
    }

    /** Strategy that decides whether to operate on a given index */
    private static final class SimpleStrategy implements Strategy {

        private final boolean[] operateAt; // true -> shouldOperate

        SimpleStrategy(boolean[] operateAt) {
            this.operateAt = operateAt.clone();
        }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return index >= 0 && index < operateAt.length && operateAt[index];
        }

        // -----------------------------------------------------------------
        // Remaining methods are not needed for the manager tests.
        // -----------------------------------------------------------------
        @Override public String getName() { return "simple"; }
        @Override public org.ta4j.core.Rule getEntryRule() { return null; }
        @Override public org.ta4j.core.Rule getExitRule() { return null; }
        @Override public Strategy and(Strategy strategy) { return null; }
        @Override public Strategy or(Strategy strategy) { return null; }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { return null; }
        @Override public Strategy opposite() { return null; }
        @Override public void setUnstableBars(int unstableBars) {}
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
    }

    /** TradeExecutionModel that simply calls enter/exit on the TradingRecord */
    private static final class SimpleTradeExecutionModel implements TradeExecutionModel {

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // Very naive implementation:
            // if there is no open position → enter, otherwise → exit.
            if (tradingRecord.getCurrentPosition() == null) {
                tradingRecord.enter(index, amount, amount);
            } else {
                tradingRecord.exit(index, amount, amount);
            }
        }
    }

    private SimpleBarSeries series;
    private BarSeriesManager manager;

    @Before
    public void setUp() {
        series = new SimpleBarSeries();
        // create a series with 5 dummy bars (indexes 0‑4)
        for (int i = 0; i < 5; i++) {
            series.addDummyBar();
        }
        manager = new BarSeriesManager(series);
    }

    // -----------------------------------------------------------------
    // 1.  List of public methods (extracted from BarSeriesManager)
    // -----------------------------------------------------------------
    // public BarSeries getBarSeries()
    // public CostModel getTransactionCostModel()
    // public CostModel getHoldingCostModel()
    // public TradingRecord run(Strategy strategy)
    // public TradingRecord run(Strategy strategy, int startIndex, int finishIndex)
    // public TradingRecord run(Strategy strategy, TradeType tradeType)
    // public TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex)
    // public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount)
    // public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex)

    // -----------------------------------------------------------------
    // 2.  Basic test cases for each public method
    // -----------------------------------------------------------------

    @Test
    public void testGetBarSeries() {
        assertSame("getBarSeries should return the series supplied to the constructor",
                series, manager.getBarSeries());
    }

    @Test
    public void testGetTransactionCostModel() {
        assertNotNull("Transaction cost model must not be null", manager.getTransactionCostModel());
        assertTrue("Default transaction cost model should be a ZeroCostModel",
                manager.getTransactionCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testGetHoldingCostModel() {
        assertNotNull("Holding cost model must not be null", manager.getHoldingCostModel());
        assertTrue("Default holding cost model should be a ZeroCostModel",
                manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testRunDefaultBuy_NoSignals() {
        // Strategy never triggers – all false
        Strategy never = new SimpleStrategy(new boolean[5]);
        TradingRecord record = manager.run(never);
        assertTrue("Record should have no positions", record.getPositions().isEmpty());
        assertFalse("Record should not be closed (no trade at all)", record.isClosed());
    }

    @Test
    public void testRunWithCustomExecutionModel_EnterAndExit() {
        // Strategy triggers on index 0 (enter) and 1 (exit)
        boolean[] signals = new boolean[]{true, true, false, false, false};
        Strategy strat = new SimpleStrategy(signals);
        // replace default execution model with our simple one
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        TradingRecord record = customMgr.run(strat);
        assertEquals("One closed position expected", 1, record.getPositionCount());
        assertTrue("The record must be closed after exit", record.isClosed());
    }

    @Test
    public void testRunWithStartAndFinishIndexes() {
        // Signals on indexes 2 and 3 only
        boolean[] signals = new boolean[]{false, false, true, true, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        TradingRecord record = customMgr.run(strat, 2, 3);
        // Expect a single closed position (enter at 2, exit at 3)
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithTradeTypeSell() {
        // Use SELL as the opening trade type
        boolean[] signals = new boolean[]{true, true, false, false, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        TradingRecord record = customMgr.run(strat, TradeType.SELL);
        assertEquals(1, record.getPositionCount());
        // The starting type should be SELL
        assertEquals(TradeType.SELL, record.getStartingType());
    }

    @Test
    public void testRunWithAmountParameter() {
        boolean[] signals = new boolean[]{true, true, false, false, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        Num customAmount = SimpleNum.valueOf(2.5);
        TradingRecord record = customMgr.run(strat, TradeType.BUY, customAmount);
        assertEquals(1, record.getPositionCount());
        // amount does not affect the stub execution model, but we ensure the call succeeds
    }

    // -----------------------------------------------------------------
    // 3.  Edge‑case & exception handling tests
    // -----------------------------------------------------------------

    @Test
    public void testRunStartIndexBeforeSeriesBegin() {
        boolean[] signals = new boolean[]{true, true, false, false, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        // startIndex is -10, should be normalized to 0
        TradingRecord record = customMgr.run(strat, TradeType.BUY, -10, 1);
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunFinishIndexAfterSeriesEnd() {
        boolean[] signals = new boolean[]{false, false, true, true, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        // finishIndex beyond the last bar (e.g., 100)
        TradingRecord record = customMgr.run(strat, TradeType.BUY, 2, 100);
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunEmptySeries() {
        SimpleBarSeries emptySeries = new SimpleBarSeries(); // no bars
        BarSeriesManager emptyMgr = new BarSeriesManager(emptySeries,
                new SimpleTradeExecutionModel());

        boolean[] signals = new boolean[0]; // no indexes exist
        Strategy strat = new SimpleStrategy(signals);

        TradingRecord record = emptyMgr.run(strat);
        // No bars → no trades, but also no exception
        assertTrue(record.getPositions().isEmpty());
        assertFalse(record.isClosed());
    }

    @Test
    public void testRunStrategyNeverClosesLastPosition() {
        // Strategy only enters at index 0 and never signals exit
        boolean[] signals = new boolean[]{true, false, false, false, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        TradingRecord record = customMgr.run(strat);
        // The position stays open because there is no exit signal.
        assertEquals(1, record.getPositionCount());
        assertFalse("The record should be open (not closed)", record.isClosed());
    }

    @Test
    public void testRunStrategyClosesAfterEndIndex() {
        // Enter at last bar (index 4) and no exit inside the range.
        // The manager should try to close after the end index using extra bars.
        boolean[] signals = new boolean[]{false, false, false, false, true};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager customMgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        TradingRecord record = customMgr.run(strat, 4, 4);
        // Since there are no bars after index 4, the position remains open.
        assertEquals(1, record.getPositionCount());
        assertFalse(record.isClosed());
    }

    // -----------------------------------------------------------------
    // 4.  Combined test exercising the full overloaded run method chain
    // -----------------------------------------------------------------
    @Test
    public void testRunFullOverloadChain() {
        boolean[] signals = new boolean[]{true, true, false, false, false};
        Strategy strat = new SimpleStrategy(signals);
        BarSeriesManager mgr = new BarSeriesManager(series,
                new SimpleTradeExecutionModel());

        // Use the most specific overload
        TradingRecord rec = mgr.run(strat, TradeType.BUY, SimpleNum.ONE, 0, 4);
        assertEquals(1, rec.getPositionCount());
        assertTrue(rec.isClosed());
    }
}
```
###Test END##