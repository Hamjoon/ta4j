###Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.Position;

import java.util.*;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * <p>The test suite is written by three “experts”.  Each expert contributed
 * one test case that targets a different aspect of the manager (typical flow,
 * edge‑case handling and error handling).  All tests are combined into a
 * single, self‑contained file that does not rely on any external library code
 * besides the public APIs listed in the prompt.</p>
 */
public class BarSeriesManagerTest {

    /** Simple {@link Num} implementation based on {@code double}. */
    private static class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* ---------- factory helpers ---------- */
        static SimpleNum of(Number n) {
            return new SimpleNum(n.doubleValue());
        }

        /* ---------- Num contract (only the operations used in the tests) ---------- */
        @Override public Num zero()               { return SimpleNum.of(0); }
        @Override public Num one()                { return SimpleNum.of(1); }
        @Override public Num hundred()            { return SimpleNum.of(100); }
        @Override public Number getDelegate()     { return value; }
        @Override public String getName()         { return Double.toString(value); }
        @Override public Num plus(Num augend)     { return SimpleNum.of(this.value + ((SimpleNum)augend).value); }
        @Override public Num minus(Num subtrahend){ return SimpleNum.of(this.value - ((SimpleNum)subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand){ return SimpleNum.of(this.value * ((SimpleNum)multiplicand).value); }
        @Override public Num dividedBy(Num divisor){ return SimpleNum.of(this.value / ((SimpleNum)divisor).value); }
        @Override public Num remainder(Num divisor){ return SimpleNum.of(this.value % ((SimpleNum)divisor).value); }
        @Override public Num floor()              { return SimpleNum.of(Math.floor(value)); }
        @Override public Num ceil()               { return SimpleNum.of(Math.ceil(value)); }
        @Override public Num pow(int n)           { return SimpleNum.of(Math.pow(value, n)); }
        @Override public Num pow(Num n)           { return SimpleNum.of(Math.pow(value, ((SimpleNum)n).value)); }
        @Override public Num log()                { return SimpleNum.of(Math.log(value)); }
        @Override public Num sqrt()               { return SimpleNum.of(Math.sqrt(value)); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs()                { return SimpleNum.of(Math.abs(value)); }
        @Override public Num negate()             { return SimpleNum.of(-value); }
        @Override public boolean isZero()        { return value == 0.0; }
        @Override public boolean isPositive()    { return value > 0.0; }
        @Override public boolean isPositiveOrZero(){ return value >= 0.0; }
        @Override public boolean isNegative()    { return value < 0.0; }
        @Override public boolean isNegativeOrZero(){ return value <= 0.0; }
        @Override public boolean isEqual(Num other){ return Double.compare(this.value, ((SimpleNum)other).value) == 0; }
        @Override public boolean isGreaterThan(Num other){ return this.value > ((SimpleNum)other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other){ return this.value >= ((SimpleNum)other).value; }
        @Override public boolean isLessThan(Num other){ return this.value < ((SimpleNum)other).value; }
        @Override public boolean isLessThanOrEqual(Num other){ return this.value <= ((SimpleNum)other).value; }
        @Override public Num min(Num other){ return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other){ return this.isGreaterThan(other) ? this : other; }
        @Override public java.util.function.Function<Number, Num> function(){ return SimpleNum::of; }
        @Override public Num numOf(Number value){ return SimpleNum.of(value); }
        @Override public Num numOf(String value, int precision){ return SimpleNum.of(Double.parseDouble(value)); }
        @Override public boolean isNaN(){ return Double.isNaN(value); }
        @Override public double doubleValue(){ return value; }
        @Override public int intValue(){ return (int) value; }
        @Override public long longValue(){ return (long) value; }
        @Override public float floatValue(){ return (float) value; }
        @Override public int hashCode(){ return Double.hashCode(value); }
        @Override public String toString(){ return Double.toString(value); }
        @Override public boolean equals(Object obj){
            return (obj instanceof SimpleNum) && isEqual((SimpleNum)obj);
        }
        @Override public int compareTo(Num o){ return Double.compare(this.value, ((SimpleNum)o).value); }
    }

    /** Minimal {@link BarSeries} implementation – only the members used by
     *  {@link BarSeriesManager} are provided. */
    private static class SimpleBarSeries implements BarSeries {

        private final String name;
        private final List<Bar> bars = new ArrayList<>();

        SimpleBarSeries(String name) {
            this.name = name;
        }

        /* ---- required abstract members ---- */
        @Override public String getName() { return name; }
        @Override public Num num() { return SimpleNum.of(0); } // dummy – not used
        @Override public Bar getBar(int i) { return bars.get(i); }
        @Override public int getBarCount() { return bars.size(); }
        @Override public int getBeginIndex() { return bars.isEmpty() ? 0 : 0; }
        @Override public int getEndIndex()   { return bars.isEmpty() ? -1 : bars.size() - 1; }
        @Override public List<Bar> getBarData() { return Collections.unmodifiableList(bars); }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {/* no‑op */}
        @Override public int getRemovedBarsCount() { return 0; }

        /* ---- helper to fill the series ---- */
        void addDummyBar() {
            bars.add(new DummyBar());
        }

        /* ---- dummy Bar implementation (only to satisfy the generic type) ---- */
        private static class DummyBar implements Bar {
            // All methods of Bar are irrelevant for the tests; we leave them empty.
        }
    }

    /** Simple {@link Strategy} that triggers {@link #shouldOperate(int,TradingRecord)}
     *  only for a predefined set of indexes. */
    private static class IndexStrategy implements Strategy {

        private final Set<Integer> operateIndexes;
        private final String name;

        IndexStrategy(String name, int... indexes) {
            this.name = name;
            this.operateIndexes = new HashSet<>();
            for (int i : indexes) {
                operateIndexes.add(i);
            }
        }

        @Override public String getName() { return name; }
        @Override public Rule getEntryRule() { throw new UnsupportedOperationException(); }
        @Override public Rule getExitRule()  { throw new UnsupportedOperationException(); }
        @Override public Strategy and(Strategy s) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy s)  { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String n, Strategy s, int u) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String n, Strategy s, int u)  { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) { }
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operateIndexes.contains(index);
        }
    }

    /** Execution model that simply opens a position on the first call and
     *  closes it on the next call. */
    private static class SimpleExecutionModel implements TradeExecutionModel {

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // If there is no open position, open one; otherwise close it.
            if (tradingRecord.getCurrentPosition() == null) {
                tradingRecord.enter(index, amount, amount);
            } else {
                tradingRecord.exit(index, amount, amount);
            }
        }
    }

    private SimpleBarSeries series;
    private CostModel zeroCost;
    private TradeExecutionModel execModel;

    @Before
    public void setUp() {
        series = new SimpleBarSeries("test-series");
        // create a series with 5 dummy bars (indexes 0..4)
        for (int i = 0; i < 5; i++) {
            series.addDummyBar();
        }
        zeroCost = new ZeroCostModel();
        execModel = new SimpleExecutionModel();
    }

    /** --------------------------------------------------------------------
     *  Expert 1 – typical flow: a strategy that opens and then closes a trade.
     *  -------------------------------------------------------------------- */
    @Test
    public void testRunTypicalOpenClose() {
        // strategy will operate on index 1 (open) and index 2 (close)
        Strategy strategy = new IndexStrategy("open‑close", 1, 2);

        BarSeriesManager manager = new BarSeriesManager(series, zeroCost, zeroCost, execModel);
        TradingRecord record = manager.run(strategy, TradeType.BUY, SimpleNum.of(1), 0, series.getEndIndex());

        // one closed position expected
        assertEquals(1, record.getPositionCount());
        assertTrue(record.isClosed());

        // verify that the position was opened at index 1 and closed at index 2
        Position pos = record.getPositions().get(0);
        assertEquals(Integer.valueOf(1), pos.getEntry().getIndex());
        assertEquals(Integer.valueOf(2), pos.getExit().getIndex());
    }

    /** --------------------------------------------------------------------
     *  Expert 2 – edge case: start index greater than finish index (no iteration).
     *  -------------------------------------------------------------------- */
    @Test
    public void testRunStartAfterFinishProducesNoTrades() {
        Strategy strategy = new IndexStrategy("no‑op", 0, 1, 2, 3, 4);
        BarSeriesManager manager = new BarSeriesManager(series, zeroCost, zeroCost, execModel);

        // startIndex (4) > finishIndex (2) – the loop should never execute
        TradingRecord record = manager.run(strategy, TradeType.BUY, SimpleNum.of(1), 4, 2);

        assertEquals(0, record.getPositionCount());
        assertTrue(record.isClosed()); // no open position
    }

    /** --------------------------------------------------------------------
     *  Expert 3 – error handling: passing a null strategy must raise NPE.
     *  -------------------------------------------------------------------- */
    @Test(expected = NullPointerException.class)
    public void testRunWithNullStrategyThrowsException() {
        BarSeriesManager manager = new BarSeriesManager(series);
        manager.run(null); // should throw NPE inside the manager
    }

    /** --------------------------------------------------------------------
     *  Additional test: manager tries to close an open position after the
     *  series end using the extra‑bars loop.
     *  -------------------------------------------------------------------- */
    @Test
    public void testRunClosesPositionAfterSeriesEnd() {
        // series currently has 5 bars (0..4). Add two extra dummy bars to allow the
        // manager to look beyond the end index.
        series.addDummyBar(); // index 5
        series.addDummyBar(); // index 6

        // strategy will open on the last real bar (index 4) and close on index 5
        Strategy strategy = new IndexStrategy("close‑after‑end", 4, 5);
        BarSeriesManager manager = new BarSeriesManager(series, zeroCost, zeroCost, execModel);

        TradingRecord record = manager.run(strategy, TradeType.BUY, SimpleNum.of(1), 0, 4);

        // Expect one closed position (opened at 4, closed at 5)
        assertEquals(1, record.getPositionCount());
        Position pos = record.getPositions().get(0);
        assertEquals(Integer.valueOf(4), pos.getEntry().getIndex());
        assertEquals(Integer.valueOf(5), pos.getExit().getIndex());
    }
}
```
###Test END##