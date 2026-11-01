package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

/**
 * Unit tests for {@link BarSeriesManager}. The tests use only the public
 * constructors and methods that are part of the source code provided in the
 * exercise.
 */
public class BarSeriesManagerTest {

    /** Helper to build a tiny BarSeries with three deterministic bars. */
    private BarSeries buildSampleSeries() {
        BarSeries series = new BaseBarSeries("sample");
        // create three 1‑day bars with increasing close prices
        for (int i = 0; i < 3; i++) {
            Num open = DecimalNum.valueOf(10 + i);
            Num high = DecimalNum.valueOf(11 + i);
            Num low = DecimalNum.valueOf(9 + i);
            Num close = DecimalNum.valueOf(10 + i);
            Num volume = DecimalNum.valueOf(100);
            Bar bar = series.barBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .closePrice(close)
                    .volume(volume)
                    .build();
            series.addBar(bar);
        }
        return series;
    }

    /** Simple {@link Strategy} that never triggers any operation. */
    private Strategy neverOperateStrategy() {
        return new Strategy() {
            @Override public String getName() { return "never"; }
            @Override public Rule getEntryRule() { return null; }
            @Override public Rule getExitRule() { return null; }
            @Override public Strategy and(Strategy s) { return null; }
            @Override public Strategy or(Strategy s) { return null; }
            @Override public Strategy and(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy or(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy opposite() { return null; }
            @Override public void setUnstableBars(int unstableBars) {}
            @Override public int getUnstableBars() { return 0; }
            @Override public boolean isUnstableAt(int index) { return false; }
            @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                return false; // never operate
            }
        };
    }

    /** Simple {@link Strategy} that operates on every index. */
    private Strategy alwaysOperateStrategy() {
        return new Strategy() {
            @Override public String getName() { return "always"; }
            @Override public Rule getEntryRule() { return null; }
            @Override public Rule getExitRule() { return null; }
            @Override public Strategy and(Strategy s) { return null; }
            @Override public Strategy or(Strategy s) { return null; }
            @Override public Strategy and(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy or(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy opposite() { return null; }
            @Override public void setUnstableBars(int unstableBars) {}
            @Override public int getUnstableBars() { return 0; }
            @Override public boolean isUnstableAt(int index) { return false; }
            @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                return true; // operate on every bar
            }
        };
    }

    /**
     * Simple {@link TradeExecutionModel} that performs a real entry/exit on the
     * {@link TradingRecord}. It uses the bar close price as the execution price
     * and the supplied {@code amount}.
     */
    private static class SimpleExecutionModel implements TradeExecutionModel {
        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            Num price = barSeries.getBar(index).getClosePrice();
            // If we have no open position, try to enter; otherwise try to exit.
            if (tradingRecord.getCurrentPosition() == null
                    || tradingRecord.getCurrentPosition().isClosed()) {
                tradingRecord.enter(index, price, amount);
            } else {
                tradingRecord.exit(index, price, amount);
            }
        }
    }

    /** Test that the default constructor wires the expected default components. */
    @Test
    public void testDefaultConstructorAndGetters() {
        BarSeries series = buildSampleSeries();
        BarSeriesManager manager = new BarSeriesManager(series);

        assertSame("BarSeries should be the same instance passed to the constructor",
                series, manager.getBarSeries());

        assertTrue("Default transaction cost model must be a ZeroCostModel",
                manager.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue("Default holding cost model must be a ZeroCostModel",
                manager.getHoldingCostModel() instanceof ZeroCostModel);
    }

    /** Test the constructor that receives a custom {@link TradeExecutionModel}. */
    @Test
    public void testConstructorWithCustomExecutionModel() {
        BarSeries series = buildSampleSeries();
        TradeExecutionModel customModel = new SimpleExecutionModel();
        BarSeriesManager manager = new BarSeriesManager(series, customModel);

        // internal state is not directly visible; we just ensure the manager can run
        // without throwing.
        TradingRecord rec = manager.run(neverOperateStrategy());
        assertNotNull(rec);
    }

    /** Running a strategy that never operates must return an empty (closed) record. */
    @Test
    public void testRunNeverOperates() {
        BarSeries series = buildSampleSeries();
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        TradingRecord record = manager.run(neverOperateStrategy());

        assertTrue("TradingRecord should be closed when nothing was opened", record.isClosed());
        assertEquals("No positions should be recorded", 0, record.getPositions().size());
        assertEquals("Start index should equal series begin index",
                Integer.valueOf(series.getBeginIndex()), record.getStartIndex());
        assertEquals("End index should equal series end index",
                Integer.valueOf(series.getEndIndex()), record.getEndIndex());
    }

    /** Running a strategy that always operates should create a position that is
     * opened on the first bar and closed on the second bar (because the execution
     * model alternates between entry and exit). */
    @Test
    public void testRunAlwaysOperatesSinglePosition() {
        BarSeries series = buildSampleSeries(); // 3 bars: indexes 0,1,2
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        TradingRecord record = manager.run(alwaysOperateStrategy());

        // The SimpleExecutionModel will enter on index 0, exit on index 1,
        // then try to enter again on index 2 (but there is no following bar to exit,
        // so the position stays open). The manager’s post‑run logic will try to close
        // it using the next bar (index 3) – which does not exist – therefore the
        // last position remains open.
        List<Position> positions = record.getPositions();
        assertEquals("Two positions should have been created (enter‑exit and a final open)", 2, positions.size());

        Position first = positions.get(0);
        assertTrue("First position must be closed", first.isClosed());

        Position second = positions.get(1);
        assertFalse("Second position must be still open", second.isClosed());

        // Verify that the entry trade type respects the default BUY opening type
        assertEquals("First position entry type must be BUY", TradeType.BUY, first.getEntry().getType());
        // The second position entry type must also be BUY (the manager always opens with BUY)
        assertEquals("Second position entry type must be BUY", TradeType.BUY, second.getEntry().getType());
    }

    /** Run with an explicit start / finish range that does not cover the whole series. */
    @Test
    public void testRunWithCustomRange() {
        BarSeries series = buildSampleSeries(); // indexes 0..2
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        // Run only on the middle bar (index 1)
        TradingRecord record = manager.run(alwaysOperateStrategy(), TradeType.BUY,
                series.numFactory().one(), 1, 1);

        // Only one iteration → a single entry trade (no exit because there is no
        // subsequent iteration inside the range).
        List<Position> positions = record.getPositions();
        assertEquals("Exactly one position should be created", 1, positions.size());

        Position pos = positions.get(0);
        assertFalse("The position must be open (no exit performed)", pos.isClosed());

        // The manager should attempt to close the open position after the loop,
        // because the runEndIndex equals series.getEndIndex() is false (runEndIndex=1,
        // series end=2). Hence the extra‑loop will be executed and the position will
        // be closed on index 2.
        assertTrue("After the extra‑loop the position must be closed", pos.isClosed());
    }

    /** Verify that the manager tries to close an open position after the run when
     * the run finishes at the series end and there are further bars in the series. */
    @Test
    public void testRunClosesOpenPositionAfterEndIndex() {
        // Build a series with 5 bars – we will run only on the first three.
        BarSeries series = new BaseBarSeries("extended");
        for (int i = 0; i < 5; i++) {
            Num price = DecimalNum.valueOf(10 + i);
            Bar bar = series.barBuilder()
                    .timePeriod(Duration.ofDays(1))
                    .openPrice(price)
                    .highPrice(price.plus(DecimalNum.valueOf(1)))
                    .lowPrice(price.minus(DecimalNum.valueOf(1)))
                    .closePrice(price)
                    .volume(DecimalNum.valueOf(100))
                    .build();
            series.addBar(bar);
        }

        // Strategy that opens a position on the first bar and never closes it.
        Strategy openOnly = new Strategy() {
            @Override public String getName() { return "openOnly"; }
            @Override public Rule getEntryRule() { return null; }
            @Override public Rule getExitRule() { return null; }
            @Override public Strategy and(Strategy s) { return null; }
            @Override public Strategy or(Strategy s) { return null; }
            @Override public Strategy and(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy or(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy opposite() { return null; }
            @Override public void setUnstableBars(int unstableBars) {}
            @Override public int getUnstableBars() { return 0; }
            @Override public boolean isUnstableAt(int index) { return false; }
            @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                // Operate only on index 0 (enter) and on index 3 (close)
                return index == 0 || index == 3;
            }
        };

        // Execution model that simply forwards to the TradingRecord.
        SimpleExecutionModel execModel = new SimpleExecutionModel();

        BarSeriesManager manager = new BarSeriesManager(series, execModel);

        // Run on indexes 0‑2 (the third bar is index 2). The position will be opened
        // on index 0 and stay open because the strategy does not request an exit
        // inside the range. After the main loop, the manager will look at index 3
        // (the first bar after the run) and close the position.
        TradingRecord record = manager.run(openOnly, TradeType.BUY,
                series.numFactory().one(), 0, 2);

        List<Position> positions = record.getPositions();
        assertEquals("Only one position should be present", 1, positions.size());

        Position pos = positions.get(0);
        assertTrue("The position must be closed by the post‑run extra loop", pos.isClosed());

        // Verify that the exit happened at index 3 (the first bar after the run)
        assertEquals("Exit index must be 3", Integer.valueOf(3), pos.getExit().getIndex());
    }

    /** Ensure that the overload taking only a {@link Strategy} delegates to the
     * BUY‑opening overload. */
    @Test
    public void testRunConvenienceMethodDelegatesToBuy() {
        BarSeries series = buildSampleSeries();
        BarSeriesManager manager = new BarSeriesManager(series, new SimpleExecutionModel());

        // Use a strategy that opens and closes on the first two bars.
        Strategy toggle = new Strategy() {
            @Override public String getName() { return "toggle"; }
            @Override public Rule getEntryRule() { return null; }
            @Override public Rule getExitRule() { return null; }
            @Override public Strategy and(Strategy s) { return null; }
            @Override public Strategy or(Strategy s) { return null; }
            @Override public Strategy and(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy or(String name, Strategy s, int unstableBars) { return null; }
            @Override public Strategy opposite() { return null; }
            @Override public void setUnstableBars(int unstableBars) {}
            @Override public int getUnstableBars() { return 0; }
            @Override public boolean isUnstableAt(int index) { return false; }
            @Override public boolean shouldOperate(int index, TradingRecord tradingRecord) {
                // Operate on index 0 (enter) and index 1 (exit)
                return index == 0 || index == 1;
            }
        };

        TradingRecord record = manager.run(toggle); // uses BUY by default

        Position position = record.getPositions().get(0);
        assertEquals("Entry trade must be BUY", TradeType.BUY, position.getEntry().getType());
        assertEquals("Exit trade must be SELL (complement of BUY)", TradeType.SELL, position.getExit().getType());
    }

    /** Verify that the manager correctly stores the custom cost models supplied
     * via the full constructor. */
    @Test
    public void testCustomCostModelsAreReturned() {
        BarSeries series = buildSampleSeries();
        CostModel txCost = new ZeroCostModel(); // using the concrete class directly
        CostModel holdCost = new ZeroCostModel();

        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost, new SimpleExecutionModel());

        assertSame("Transaction cost model must be the one supplied in the constructor",
                txCost, manager.getTransactionCostModel());
        assertSame("Holding cost model must be the one supplied in the constructor",
                holdCost, manager.getHoldingCostModel());
    }
}
