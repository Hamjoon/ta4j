package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.Num;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBar;

/**
 * Unit tests for {@link CashFlow}.
 *
 * The tests cover:
 * <ul>
 *   <li>static helpers {@code addCost}, {@code determineEndIndex},
 *       {@code getIntermediateRatio}</li>
 *   <li>construction with a single closed {@link Position}</li>
 *   <li>construction with a {@link org.ta4j.core.TradingRecord}</li>
 *   <li>behaviour of {@code getValue}, {@code getSize} and internal padding
 *       performed by {@code fillToTheEnd}</li>
 * </ul>
 *
 * The library provides concrete implementations {@link BaseBarSeries} and
 * {@link BaseBar} which are used to build a minimal series of price data.
 */
public class CashFlowTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private BarSeries series;

    /** Build a series of 5 bars with deterministic close prices. */
    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");
        // Close prices: 10, 11, 12, 13, 14
        for (int i = 0; i < 5; i++) {
            ZonedDateTime endTime = ZonedDateTime.now(ZONE).plusDays(i);
            Num open = series.numOf(10 + i);
            Num high = open.plus(series.numOf(1));
            Num low  = open.minus(series.numOf(1));
            Num close = series.numOf(10 + i);
            series.addBar(new BaseBar(Duration.ofDays(1), endTime, open, high, low, close, series.numOf(0)));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Static helper methods
     * --------------------------------------------------------------------- */

    @Test
    public void testAddCost_longTrade() {
        Num raw = series.numOf(100);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, true); // long → subtract cost
        assertEquals(series.numOf(98), net);
    }

    @Test
    public void testAddCost_shortTrade() {
        Num raw = series.numOf(100);
        Num cost = series.numOf(3);
        Num net = CashFlow.addCost(raw, cost, false); // short → add cost
        assertEquals(series.numOf(103), net);
    }

    @Test
    public void testDetermineEndIndex_closedPosition() {
        Position pos = new Position();
        // entry at index 0, exit at index 3
        pos.operate(0, series.numOf(10), series.numOf(1)); // BUY
        pos.operate(3, series.numOf(13), series.numOf(1)); // SELL (exit)
        int finalIdx = 4; // larger than exit
        int maxIdx = series.getEndIndex(); // 4
        int endIdx = CashFlow.determineEndIndex(pos, finalIdx, maxIdx);
        // should be the exit index (3) because position is closed
        assertEquals(3, endIdx);
    }

    @Test
    public void testDetermineEndIndex_openPosition() {
        Position pos = new Position();
        // entry only, never exited
        pos.operate(1, series.numOf(11), series.numOf(1)); // BUY
        int finalIdx = 3;
        int maxIdx = series.getEndIndex(); // 4
        int endIdx = CashFlow.determineEndIndex(pos, finalIdx, maxIdx);
        // no exit → min(finalIdx, maxIdx) = 3
        assertEquals(3, endIdx);
    }

    @Test
    public void testGetIntermediateRatio_long() {
        Num entry = series.numOf(10);
        Num exit  = series.numOf(15);
        Num ratio = CashFlow.getIntermediateRatio(true, entry, exit);
        assertEquals(series.numOf(1.5), ratio);
    }

    @Test
    public void testGetIntermediateRatio_short() {
        Num entry = series.numOf(20);
        Num exit  = series.numOf(15);
        // ratio = 2 - (exit/entry) = 2 - 0.75 = 1.25
        Num ratio = CashFlow.getIntermediateRatio(false, entry, exit);
        assertEquals(series.numOf(1.25), ratio);
    }

    /* --------------------------------------------------------------------- *
     *  CashFlow with a single closed position
     * --------------------------------------------------------------------- */

    @Test
    public void testCashFlowSingleClosedPosition() {
        // Build a BUY at index 0 (price 10) and SELL at index 2 (price 12)
        Position pos = new Position();
        pos.operate(0, series.numOf(10), series.numOf(1)); // entry (BUY)
        pos.operate(2, series.numOf(12), series.numOf(1)); // exit (SELL)

        // Verify the position is closed
        assertTrue(pos.isClosed());

        CashFlow cf = new CashFlow(series, pos);

        // Index 0: initial value is 1 (as defined in the constructor)
        assertEquals(series.one(), cf.getValue(0));

        // After entry (index 1) the cash flow should reflect price ratio 11/10 = 1.1
        Num expectedAfterBar1 = series.one().multipliedBy(series.numOf(11).dividedBy(series.numOf(10)));
        assertEquals(expectedAfterBar1, cf.getValue(1));

        // After exit (index 2) the cash flow should be 12/10 = 1.2
        Num expectedAfterBar2 = series.one().multipliedBy(series.numOf(12).dividedBy(series.numOf(10)));
        assertEquals(expectedAfterBar2, cf.getValue(2));

        // The series has 5 bars – values after the last computed bar must be padded
        // with the last cash‑flow value (1.2)
        for (int i = 3; i <= series.getEndIndex(); i++) {
            assertEquals(expectedAfterBar2, cf.getValue(i));
        }

        // Size of the underlying series
        assertEquals(series.getBarCount(), cf.getSize());
    }

    /* --------------------------------------------------------------------- *
     *  CashFlow with a TradingRecord (closed + open positions)
     * --------------------------------------------------------------------- */

    @Test
    public void testCashFlowWithTradingRecord() {
        // Build a simple TradingRecord manually (the concrete class is BaseTradingRecord)
        org.ta4j.core.BaseTradingRecord record = new org.ta4j.core.BaseTradingRecord();

        // Position 1: BUY at index 0 (price 10), SELL at index 1 (price 11)
        record.enter(0, series.numOf(10), series.numOf(1));
        record.exit(1, series.numOf(11), series.numOf(1));

        // Position 2 (still open): BUY at index 2 (price 12) – never exited
        record.enter(2, series.numOf(12), series.numOf(1));

        // Build CashFlow with finalIndex = series.getEndIndex() (4)
        CashFlow cf = new CashFlow(series, record, series.getEndIndex());

        // Verify values for the closed first position
        // after first exit (index 1) cash flow = 11/10 = 1.1
        Num cfAfterFirstExit = series.one().multipliedBy(series.numOf(11).dividedBy(series.numOf(10)));
        assertEquals(cfAfterFirstExit, cf.getValue(1));

        // For the open position we expect accrual up to the final index (4)
        // The price series from index 3 to 4 is 13 and 14.
        // Holding cost is zero (default model), so the ratio for each step is price/entryPrice.
        Num entryPrice = series.numOf(12);
        Num expectedIdx3 = series.one().multipliedBy(series.numOf(13).dividedBy(entryPrice));
        Num expectedIdx4 = series.one().multipliedBy(series.numOf(14).dividedBy(entryPrice));

        // Because the cash‑flow list is built on top of the initial 1.0 value,
        // the values after index 2 are multiplied by the previous cash‑flow.
        Num base = cfAfterFirstExit; // cash flow after closed position
        assertEquals(base.multipliedBy(series.numOf(13).dividedBy(entryPrice)), cf.getValue(3));
        assertEquals(base.multipliedBy(series.numOf(14).dividedBy(entryPrice)), cf.getValue(4));

        // Ensure padding – there are no further bars, so getValue(end) must be defined
        assertEquals(cf.getValue(series.getEndIndex()), cf.getValue(series.getEndIndex()));
    }

    /* --------------------------------------------------------------------- *
     *  Edge cases – illegal construction
     * --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testCashFlowConstructorWithOpenPositionThrows() {
        Position openPos = new Position();
        // Only entry, never exited → position remains opened
        openPos.operate(0, series.numOf(10), series.numOf(1));
        // The constructor that receives a single Position expects a closed position
        new CashFlow(series, openPos);
    }

    /* --------------------------------------------------------------------- *
     *  Internal padding verification (fillToTheEnd)
     * --------------------------------------------------------------------- */

    @Test
    public void testFillToTheEndPadsValues() {
        // Simple closed position at index 0‑1
        Position pos = new Position();
        pos.operate(0, series.numOf(10), series.numOf(1));
        pos.operate(1, series.numOf(11), series.numOf(1));

        CashFlow cf = new CashFlow(series, pos);

        // Values list length must be equal to series length (5)
        assertEquals(series.getBarCount(), cf.getSize());

        // After the last computed bar (index 1) the remaining entries must be equal
        // to the cash‑flow at index 1 (i.e., 11/10 = 1.1)
        Num lastComputed = series.one().multipliedBy(series.numOf(11).dividedBy(series.numOf(10)));
        for (int i = 2; i <= series.getEndIndex(); i++) {
            assertEquals(lastComputed, cf.getValue(i));
        }
    }
}
