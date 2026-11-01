package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

public class BarSeriesManagerTest {

    /* --------------------------------------------------------------------- *
     *  Minimal in‑memory implementations required for the tests            *
     * --------------------------------------------------------------------- */

    /** Very small implementation of {@link Num} backed by a double. */
    private static final class SimpleNum implements Num {
        private final double value;

        private SimpleNum(double value) { this.value = value; }

        static final SimpleNum ZERO = new SimpleNum(0.0);
        static final SimpleNum ONE  = new SimpleNum(1.0);
        static final SimpleNum HUNDRED = new SimpleNum(100.0);

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
        @Override public boolean isZero() { return value == 0.0; }
        @Override public boolean isPositive() { return value > 0.0; }
        @Override public boolean isPositiveOrZero() { return value >= 0.0; }
        @Override public boolean isNegative() { return value < 0.0; }
        @Override public boolean isNegativeOrZero() { return value <= 0.0; }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, ((SimpleNum)other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum)other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= ((SimpleNum)other).value; }
        @Override public boolean isLessThan(Num other) { return this.value < ((SimpleNum)other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= ((SimpleNum)other).value; }
        @Override public Num min(Num other) { return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return this.isGreaterThan(other) ? this : other; }
        @Override public Function<Number, Num> function() { return SimpleNum::new; }
        @Override public Num numOf(Number number) { return new SimpleNum(number.doubleValue()); }
        @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN() { return Double.isNaN(value); }
        @Override public double doubleValue() { return value; }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) { return (obj instanceof SimpleNum) && isEqual((SimpleNum)obj); }
        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum)o).value); }
    }

    /** Minimal {@link Bar} implementation – only needed to satisfy the interface. */
    private static final class SimpleBar implements Bar {
        private final ZonedDateTime endTime;
        private final SimpleNum open, high, low, close, volume, amount;

        SimpleBar(ZonedDateTime endTime, double open, double high, double low, double close) {
            this.endTime = endTime;
            this.open = new SimpleNum(open);
            this.high = new SimpleNum(high);
            this.low = new SimpleNum(low);
            this.close = new SimpleNum(close);
            this.volume = SimpleNum.ZERO;
            this.amount = SimpleNum.ZERO;
        }

        @Override public ZonedDateTime getEndTime() { return endTime; }
        @Override public Num getOpenPrice() { return open; }
        @Override public Num getHighPrice() { return high; }
        @Override public Num getLowPrice()  { return low; }
        @Override public Num getClosePrice(){ return close; }
        @Override public Num getVolume()    { return volume; }
        @Override public Num getAmount()    { return amount; }
        // The rest of the Bar API is not required for the manager tests.
    }

    /** Very small in‑memory {@link BarSeries}. */
    private static final class SimpleBarSeries implements BarSeries {
        private final String name;
        private final List<Bar> bars = new ArrayList<>();

        SimpleBarSeries(String name) { this.name = name; }

        void addSimpleBar(Bar bar) { bars.add(bar); }

        @Override public String getName() { return name; }
        @Override public Num num() { return SimpleNum.ONE; }
        @Override public Bar getBar(int i) { return bars.get(i); }
        @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }
        @Override public int getBeginIndex() { return bars.isEmpty() ? 0 : 0; }
        @Override public int getEndIndex()   { return bars.isEmpty() ? -1 : bars.size() - 1; }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) {
            SimpleBarSeries sub = new SimpleBarSeries(name + "-sub");
            for (int i = startIndex; i <= endIndex && i < bars.size(); i++) {
                sub.addSimpleBar(bars.get(i));
            }
            return sub;
        }

        // ----- default methods that we do not need for the tests -----------------
        @Override public int getBarCount() { return bars.size(); }
        @Override public boolean isEmpty() { return bars.isEmpty(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(Bar bar) { bars.add(bar); }
        @Override public void addBar(Bar bar, boolean replace) { bars.add(bar); }
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                      Number lowPrice, Number closePrice) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                      Number lowPrice, Number closePrice, Number volume) {}
        @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                      Number lowPrice, Number closePrice, Number volume, Number amount) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Number openPrice, Number highPrice, Number lowPrice,
                                      Number closePrice, Number volume) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Number openPrice, Number highPrice, Number lowPrice,
                                      Number closePrice, Number volume, Number amount) {}
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
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Num openPrice, Num highPrice, Num lowPrice,
                                      Num closePrice, Num volume) {}
        @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                      Num openPrice, Num highPrice, Num lowPrice,
                                      Num closePrice, Num volume, Num amount) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(String tradeVolume, String tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(String price) {}
        @Override public void addPrice(Number price) {}
        @Override public Function<Number, Num> function() { return SimpleNum::new; }
        @Override public Num zero() { return SimpleNum.ZERO; }
        @Override public Num one() { return SimpleNum.ONE; }
        @Override public Num hundred() { return SimpleNum.HUNDRED; }
        @Override public Num numOf(Number number) { return new SimpleNum(number.doubleValue()); }
        @Override public Bar getFirstBar() { return bars.isEmpty() ? null : bars.get(0); }
        @Override public Bar getLastBar()  { return bars.isEmpty() ? null : bars.get(bars.size() - 1); }
    }

    /** Strategy that can be configured to return a predetermined boolean for `shouldOperate`. */
    private static final class DummyStrategy implements Strategy {
        private final Set<Integer> operateIndexes;
        private final TradeType openingType; // not used by the manager, only kept for completeness

        DummyStrategy(Set<Integer> operateIndexes) {
            this.operateIndexes = new HashSet<>(operateIndexes);
            this.openingType = TradeType.BUY;
        }

        @Override public String getName() { return "DummyStrategy"; }
        @Override public Rule getEntryRule() { return null; }
        @Override public Rule getExitRule() { return null; }
        @Override public Strategy and(Strategy strategy) { return this; }
        @Override public Strategy or(Strategy strategy) { return this; }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { return this; }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { return this; }
        @Override public Strategy opposite() { return this; }
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

    /** Execution model that records every call made to it. */
    private static final class RecordingExecutionModel implements TradeExecutionModel {
        final List<Integer> executedIndexes = new ArrayList<>();
        final List<Num> receivedAmounts = new ArrayList<>();

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            executedIndexes.add(index);
            receivedAmounts.add(amount);
            // Very simple execution: open a trade if none is open, otherwise close it.
            if (tradingRecord.getCurrentPosition() == null
                    || tradingRecord.getCurrentPosition().isClosed()) {
                tradingRecord.enter(index, barSeries.num().one(), amount);
            } else {
                tradingRecord.exit(index, barSeries.num().one(), amount);
            }
        }
    }

    private SimpleBarSeries series;
    private CostModel zeroCost;
    private RecordingExecutionModel execModel;

    @Before
    public void setUp() {
        series = new SimpleBarSeries("testSeries");
        // add 5 dummy bars (indexes 0‑4)
        for (int i = 0; i < 5; i++) {
            series.addSimpleBar(new SimpleBar(
                    ZonedDateTime.now().plusDays(i),
                    10 + i, 12 + i, 9 + i, 11 + i));
        }
        zeroCost = new ZeroCostModel();
        execModel = new RecordingExecutionModel();
    }

    /* --------------------------------------------------------------------- *
     *  Constructor & getter tests                                            *
     * --------------------------------------------------------------------- */

    @Test
    public void testFourArgConstructorAndGetters() {
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost, execModel);
        assertSame(series, mgr.getBarSeries());
        assertSame(zeroCost, mgr.getTransactionCostModel());
        assertSame(zeroCost, mgr.getHoldingCostModel());
    }

    @Test
    public void testThreeArgConstructorUsesDefaultExecutionModel() {
        BarSeriesManager mgr = new BarSeriesManager(series, zeroCost, zeroCost);
        assertNotNull(mgr.getBarSeries());
        assertNotNull(mgr.getTransactionCostModel());
        assertNotNull(mgr.getHoldingCostModel());
        // the default execution model is TradeOnNextOpenModel – we cannot inspect it,
        // but we can at least verify that the manager works (no NPE) when running.
        Strategy alwaysTrue = new DummyStrategy(Collections.singleton(0));
        TradingRecord rec = mgr.run(alwaysTrue);
        assertNotNull(rec);
    }

    @Test
    public void testSingleArgConstructorUsesZeroCostAndTradeOnNextOpen() {
        BarSeriesManager mgr = new BarSeriesManager(series);
        assertNotNull(mgr.getTransactionCostModel());
        assertNotNull(mgr.getHoldingCostModel());
        assertNotNull(mgr.getBarSeries());
    }

    @Test
    public void testTwoArgConstructorUsesZeroCostAndSuppliedExecutionModel() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        assertSame(execModel, mgr.tradeExecutionModel);
        assertNotNull(mgr.getTransactionCostModel());
        assertNotNull(mgr.getHoldingCostModel());
    }

    /* --------------------------------------------------------------------- *
     *  run(...) basic behaviour tests                                        *
     * --------------------------------------------------------------------- */

    @Test
    public void testRunWithDefaultBuy() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        // strategy will trigger on every index
        Set<Integer> allIndexes = new HashSet<>(Arrays.asList(0,1,2,3,4));
        Strategy strat = new DummyStrategy(allIndexes);
        TradingRecord rec = mgr.run(strat);
        // Expect 2 trades (enter+exit) per index because our dummy execution model
        // opens on first call and closes on second call for the same index.
        // With 5 indexes we therefore get 5 positions (each closed).
        assertEquals(5, rec.getPositionCount());
        assertTrue(rec.isClosed());
    }

    @Test
    public void testRunWithExplicitRange() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        // Only indexes 1,2 should be operated on
        Strategy strat = new DummyStrategy(new HashSet<>(Arrays.asList(1,2)));
        TradingRecord rec = mgr.run(strat, 1, 3); // finishIndex inclusive = 3
        // indexes 1 and 2 are within range, 3 is not operated (strategy false)
        assertEquals(2, rec.getPositionCount());
        assertTrue(rec.isClosed());
    }

    @Test
    public void testRunWithSellOpeningType() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        Strategy strat = new DummyStrategy(Collections.singleton(0));
        TradingRecord rec = mgr.run(strat, TradeType.SELL);
        assertEquals(TradeType.SELL, rec.getStartingType());
    }

    @Test
    public void testRunWithCustomAmountIsPassedToExecutionModel() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        SimpleNum customAmount = new SimpleNum(123.45);
        Strategy strat = new DummyStrategy(Collections.singleton(0));
        mgr.run(strat, TradeType.BUY, customAmount);
        assertFalse(execModel.receivedAmounts.isEmpty());
        assertEquals(customAmount, execModel.receivedAmounts.get(0));
    }

    @Test
    public void testRunFullSignatureWithAllParameters() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        SimpleNum amount = new SimpleNum(7.0);
        // Operate only on index 2
        Strategy strat = new DummyStrategy(Collections.singleton(2));
        TradingRecord rec = mgr.run(strat, TradeType.BUY, amount, 2, 2);
        assertEquals(1, rec.getPositionCount());
        assertEquals(2, (int)rec.getStartIndex());
        assertEquals(2, (int)rec.getEndIndex());
        assertEquals(amount, execModel.receivedAmounts.get(0));
    }

    /* --------------------------------------------------------------------- *
     *  Edge‑case tests                                                       *
     * --------------------------------------------------------------------- */

    @Test
    public void testRunOnEmptySeriesReturnsEmptyRecord() {
        SimpleBarSeries emptySeries = new SimpleBarSeries("empty");
        BarSeriesManager mgr = new BarSeriesManager(emptySeries, execModel);
        Strategy never = new DummyStrategy(Collections.emptySet());
        TradingRecord rec = mgr.run(never);
        assertEquals(0, rec.getPositionCount());
        assertTrue(rec.isClosed()); // no open position
    }

    @Test
    public void testRunWithStartGreaterThanFinishDoesNothing() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        Strategy always = new DummyStrategy(Collections.singleton(0));
        TradingRecord rec = mgr.run(always, TradeType.BUY, 0, 5); // start=5, finish=0
        assertEquals(0, rec.getPositionCount());
        assertTrue(rec.isClosed());
    }

    @Test(expected = NullPointerException.class)
    public void testRunWithNullStrategyThrowsNPE() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        mgr.run((Strategy) null);
    }

    @Test
    public void testRunWhenLastPositionStaysOpenAndCanBeClosedAfterEnd() {
        // Strategy enters on the last bar (index 4) and also signals operation on index 5 (which does not exist in the series)
        Set<Integer> indexes = new HashSet<>(Arrays.asList(4, 5));
        Strategy strat = new DummyStrategy(indexes);
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        TradingRecord rec = mgr.run(strat, 0, 4);
        // The dummy execution model will open on index 4, then the extra loop will try index 5.
        // Because our dummy execution model receives the call, the position will be closed.
        assertTrue(rec.isClosed());
        assertEquals(1, rec.getPositionCount());
    }

    @Test
    public void testRunWithNegativeIndexesAreClamped() {
        BarSeriesManager mgr = new BarSeriesManager(series, execModel);
        Strategy always = new DummyStrategy(new HashSet<>(Arrays.asList(0,1,2,3,4)));
        TradingRecord rec = mgr.run(always, TradeType.BUY, -10, -1);
        // runBeginIndex becomes 0 (max(-10, begin=0)), runEndIndex becomes -1 (min(-1, end=4))
        // Loop is skipped – no trades.
        assertEquals(0, rec.getPositionCount());
        assertTrue(rec.isClosed());
    }

    @Test
    public void testExecutionModelExceptionIsPropagated() {
        TradeExecutionModel exploding = (index, tr, bs, amount) -> {
            throw new IllegalStateException("boom");
        };
        BarSeriesManager mgr = new BarSeriesManager(series, exploding);
        Strategy always = new DummyStrategy(Collections.singleton(0));
        try {
            mgr.run(always);
            fail("Exception should have propagated");
        } catch (IllegalStateException e) {
            assertEquals("boom", e.getMessage());
        }
    }
}
