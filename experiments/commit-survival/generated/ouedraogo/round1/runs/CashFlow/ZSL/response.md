###Test START##
```java
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.BaseTrade;

/**
 * Comprehensive JUnit‑4 test suite for {@link CashFlow}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>static utility methods {@code addCost}, {@code getIntermediateRatio}
 *       and {@code determineEndIndex}</li>
 *   <li>construction with a single closed {@link Position}</li>
 *   <li>construction with a {@link TradingRecord} that contains an open
 *       position (accrued cash‑flow up to a provided final index)</li>
 *   <li>basic accessor methods {@code getSize}, {@code getBarSeries},
 *       {@code numOf} and {@code getUnstableBars}</li>
 *   <li>exception handling for an open position passed to the
 *       {@code CashFlow(BarSeries, Position)} constructor</li>
 * </ul>
 *
 * <p>All numeric assertions use {@link Num#doubleValue()} with a small delta
 * to avoid floating‑point noise.
 */
public class CashFlowTest {

    private static final double DELTA = 1e-10;

    /** Simple series with deterministic close prices. */
    private BarSeries series;

    /** Close prices used for the series (index → price). */
    private static final double[] CLOSE_PRICES = { 100, 102, 105, 108, 110 };

    @Before
    public void setUp() {
        // Build a 5‑bar series; each bar is 1‑day apart.
        series = new BaseBarSeries("test-series");
        ZonedDateTime start = ZonedDateTime.now();
        for (int i = 0; i < CLOSE_PRICES.length; i++) {
            Bar bar = new BaseBar(Duration.ofDays(1), start.plusDays(i),
                    DecimalNum.valueOf(CLOSE_PRICES[i]),    // open
                    DecimalNum.valueOf(CLOSE_PRICES[i] + 1), // high
                    DecimalNum.valueOf(CLOSE_PRICES[i] - 1), // low
                    DecimalNum.valueOf(CLOSE_PRICES[i]),    // close
                    DecimalNum.valueOf(0));                 // volume (irrelevant)
            series.addBar(bar);
        }
        // sanity check
        assertEquals(CLOSE_PRICES.length, series.getBarCount());
    }

    /* --------------------------------------------------------------------- *
     *  Static utility methods
     * --------------------------------------------------------------------- */

    @Test
    public void testAddCost_LongTrade() {
        Num raw = series.numOf(100);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, true);
        assertEquals(98.0, net.doubleValue(), DELTA);
    }

    @Test
    public void testAddCost_ShortTrade() {
        Num raw = series.numOf(100);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, false);
        assertEquals(102.0, net.doubleValue(), DELTA);
    }

    @Test
    public void testGetIntermediateRatio_LongTrade() {
        Num entry = series.numOf(100);
        Num intermediate = series.numOf(110);
        Num ratio = CashFlow.getIntermediateRatio(true, entry, intermediate);
        assertEquals(1.10, ratio.doubleValue(), DELTA);
    }

    @Test
    public void testGetIntermediateRatio_ShortTrade() {
        Num entry = series.numOf(100);
        Num intermediate = series.numOf(90);
        Num ratio = CashFlow.getIntermediateRatio(false, entry, intermediate);
        // ratio = 2 - (intermediate/entry) = 2 - 0.9 = 1.1
        assertEquals(1.10, ratio.doubleValue(), DELTA);
    }

    @Test
    public void testDetermineEndIndex_PositionClosedBeforeFinal() {
        // closed position: exit index = 3, finalIndex = 4, maxIndex = 4
        Position pos = createClosedPosition(1, 3);
        int end = CashFlow.determineEndIndex(pos, 4, 4);
        assertEquals(3, end);
    }

    @Test
    public void testDetermineEndIndex_PositionOpen_FinalLimits() {
        // open position (no exit), finalIndex = 2, maxIndex = 4
        Position pos = createOpenPosition(1);
        int end = CashFlow.determineEndIndex(pos, 2, 4);
        assertEquals(2, end);
    }

    @Test
    public void testDetermineEndIndex_PositionOpen_MaxLimits() {
        // open position, finalIndex beyond maxIndex
        Position pos = createOpenPosition(1);
        int end = CashFlow.determineEndIndex(pos, 10, 4);
        assertEquals(4, end);
    }

    /* --------------------------------------------------------------------- *
     *  CashFlow with a single closed Position
     * --------------------------------------------------------------------- */

    @Test
    public void testCashFlowSingleClosedPosition() {
        // entry at index 1 (price 102), exit at index 3 (price 108)
        Position position = createClosedPosition(1, 3);
        CashFlow cf = new CashFlow(series, position);

        // size must equal series size
        assertEquals(series.getBarCount(), cf.getSize());

        // cash flow before entry (index 0) should be the initial value 1
        assertEquals(1.0, cf.getValue(0).doubleValue(), DELTA);

        // compute expected ratio manually:
        // holding cost = 0 (default CostModel returns zero)
        // avgCost = 0
        // long trade: ratio = close_i / entryPrice
        Num entryPrice = series.getBar(1).getClosePrice(); // 102
        // index 2 (price 105)
        Num expected2 = cf.getValue(1).multipliedBy(series.getBar(2).getClosePrice().dividedBy(entryPrice));
        assertEquals(expected2.doubleValue(), cf.getValue(2).doubleValue(), DELTA);

        // index 3 (exit price 108) – final cash flow value
        Num expected3 = cf.getValue(1).multipliedBy(series.getBar(3).getClosePrice().dividedBy(entryPrice));
        assertEquals(expected3.doubleValue(), cf.getValue(3).doubleValue(), DELTA);

        // indices after the exit are padded with the last value (index 3)
        for (int i = 4; i < cf.getSize(); i++) {
            assertEquals(cf.getValue(3).doubleValue(), cf.getValue(i).doubleValue(), DELTA);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCashFlowConstructorWithOpenPositionThrows() {
        Position open = createOpenPosition(0);
        // should throw because the position is not closed
        new CashFlow(series, open);
    }

    /* --------------------------------------------------------------------- *
     *  CashFlow with a TradingRecord (including an open position)
     * --------------------------------------------------------------------- */

    @Test
    public void testCashFlowWithTradingRecordAndOpenPosition() {
        // Build a trading record:
        //   - first closed long position: entry idx 0, exit idx 2
        //   - second open long position: entry idx 3, no exit
        BaseTradingRecord record = new BaseTradingRecord(TradeType.BUY);

        // first position (closed)
        record.operate(0, series.getBar(0).getClosePrice(), series.numOf(1)); // entry
        record.operate(2, series.getBar(2).getClosePrice(), series.numOf(1)); // exit

        // second position (open)
        record.operate(3, series.getBar(3).getClosePrice(), series.numOf(1)); // entry, no exit

        // final index for cash‑flow accrual = 4 (last bar)
        CashFlow cf = new CashFlow(series, record, 4);

        // Verify size
        assertEquals(series.getBarCount(), cf.getSize());

        // First closed position: cash‑flow should be computed up to its exit (idx 2)
        Num entry0 = series.getBar(0).getClosePrice(); // 100
        Num exit2 = series.getBar(2).getClosePrice(); // 105
        Num expectedAfterFirstExit = cf.getValue(0).multipliedBy(exit2.dividedBy(entry0));
        assertEquals(expectedAfterFirstExit.doubleValue(), cf.getValue(2).doubleValue(), DELTA);

        // Second (open) position: we accrue until finalIndex = 4
        // entry at idx 3 price = 108
        Num entry3 = series.getBar(3).getClosePrice(); // 108
        // intermediate at idx 4 price = 110
        Num price4 = series.getBar(4).getClosePrice(); // 110
        Num ratio = price4.dividedBy(entry3); // long trade
        Num expectedIdx4 = cf.getValue(3).multipliedBy(ratio);
        assertEquals(expectedIdx4.doubleValue(), cf.getValue(4).doubleValue(), DELTA);

        // Padding after final index (none in this case because finalIndex == last bar)
        // but we still assert that values after index 4 (none) would stay constant.
    }

    /* --------------------------------------------------------------------- *
     *  Accessor methods
     * --------------------------------------------------------------------- */

    @Test
    public void testGetBarSeriesAndSize() {
        Position position = createClosedPosition(1, 3);
        CashFlow cf = new CashFlow(series, position);
        assertSame(series, cf.getBarSeries());
        assertEquals(series.getBarCount(), cf.getSize());
    }

    @Test
    public void testNumOfDelegatesToSeries() {
        Position position = createClosedPosition(1, 3);
        CashFlow cf = new CashFlow(series, position);
        Num n = cf.numOf(42);
        assertEquals(42.0, n.doubleValue(), DELTA);
        // ensure the Num instance belongs to the series (same type)
        assertTrue(n.getClass().isAssignableFrom(series.numOf(0).getClass()));
    }

    @Test
    public void testGetUnstableBarsIsZero() {
        Position position = createClosedPosition(1, 3);
        CashFlow cf = new CashFlow(series, position);
        assertEquals(0, cf.getUnstableBars());
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods to create positions without dealing with the full
     *  Trade/CostModel API.
     * --------------------------------------------------------------------- */

    /**
     * Creates a closed long {@link Position} with entry at {@code entryIdx}
     * and exit at {@code exitIdx}.  Uses the series' close price for both
     * entry and exit and a zero holding‑cost model (default).
     */
    private Position createClosedPosition(int entryIdx, int exitIdx) {
        Trade entry = new BaseTrade(TradeType.BUY, entryIdx, series.getBar(entryIdx).getClosePrice(),
                series.numOf(1));
        Trade exit = new BaseTrade(TradeType.SELL, exitIdx, series.getBar(exitIdx).getClosePrice(),
                series.numOf(1));
        return new Position(entry, exit);
    }

    /**
     * Creates an open long {@link Position} with entry at {@code entryIdx}
     * and no exit.
     */
    private Position createOpenPosition(int entryIdx) {
        Trade entry = new BaseTrade(TradeType.BUY, entryIdx, series.getBar(entryIdx).getClosePrice(),
                series.numOf(1));
        // exit == null → open position
        return new Position(entry, null);
    }
}
```
###Test END##