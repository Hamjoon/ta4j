package org.ta4j.core.analysis;
import org.ta4j.core.Trade.TradeType;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade;
import org.ta4j.core.TradeType;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;

/**
 * JUnit‑4 test suite for {@link CashFlow}.
 *
 * The tests are written in the same package (org.ta4j.core.analysis) so that
 * package‑private static methods can be accessed directly.
 *
 * The test cases cover:
 * <ul>
 *   <li>Public API sanity checks (getValue, getSize, getBarSeries, etc.)</li>
 *   <li>Correct cash‑flow computation for a simple long position</li>
 *   <li>Correct cash‑flow computation for a short position</li>
 *   <li>Behaviour when a position is not closed (exception case)</li>
 *   <li>Edge cases of {@code determineEndIndex}</li>
 *   <li>Correctness of {@code addCost} for long/short trades</li>
 * </ul>
 *
 * The library provides concrete implementations such as {@code BaseBar},
 * {@code BaseBarSeriesBuilder}, {@code BaseTradingRecord}, {@code Trade},
 * {@code TradeType} and {@code DecimalNum}. Those are used to build the
 * required objects without any mocking.
 */
public class CashFlowTest {

    private BarSeries series;
    private Num one;
    private Num two;
    private Num three;

    /** Build a 5‑bar series with deterministic close prices 1,2,3,4,5 */
    @Before
    public void setUp() {
        // Use DecimalNum (the default Num implementation in ta4j) for easy creation
        one = DecimalNum.valueOf(1);
        two = DecimalNum.valueOf(2);
        three = DecimalNum.valueOf(3);

        // Build a simple series (duration & timestamps are not relevant for calculations)
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        ZonedDateTime now = ZonedDateTime.now();

        series.addBar(new BaseBar(Duration.ofMinutes(1), now, one, one, one, one, one));
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.plusMinutes(1), two, two, two, two, one));
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.plusMinutes(2), three, three, three, three, one));
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.plusMinutes(3), three, three, three, three, one));
        series.addBar(new BaseBar(Duration.ofMinutes(1), now.plusMinutes(4), three, three, three, three, one));
    }

    /* --------------------------------------------------------------------- *
     *  Public API – basic sanity checks
     * --------------------------------------------------------------------- */

    @Test
    public void testGetBarSeriesAndSize() {
        Position closedPos = createClosedLongPosition(0, 2); // entry @0, exit @2
        CashFlow cf = new CashFlow(series, closedPos);

        assertSame("CashFlow should return the same BarSeries instance", series, cf.getBarSeries());
        assertEquals("Size must equal the series bar count", series.getBarCount(), cf.getSize());
        assertEquals("Unstable bars are defined as 0", 0, cf.getUnstableBars());
    }

    @Test
    public void testNumOfDelegatesToSeries() {
        Position closedPos = createClosedLongPosition(0, 1);
        CashFlow cf = new CashFlow(series, closedPos);

        Num five = cf.numOf(5);
        assertEquals("numOf should create a Num equal to series.numOf(5)", series.numOf(5), five);
    }

    /* --------------------------------------------------------------------- *
     *  Simple cash‑flow calculations – long position
     * --------------------------------------------------------------------- */

    @Test
    public void testCashFlowLongPositionSimple() {
        // Long: buy at bar 0 (price 1), sell at bar 2 (price 3)
        Position pos = createClosedLongPosition(0, 2);
        CashFlow cf = new CashFlow(series, pos);

        // values[0] = 1 (initial)
        assertEquals(one, cf.getValue(0));

        // After exit at index 2, cash flow = 3 / 1 = 3
        assertEquals(DecimalNum.valueOf(3), cf.getValue(2));

        // No change after the last bar – fillToTheEnd pads with last value
        for (int i = 3; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.valueOf(3), cf.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Simple cash‑flow calculations – short position
     * --------------------------------------------------------------------- */

    @Test
    public void testCashFlowShortPositionSimple() {
        // Short: sell (entry) at bar 0 price 3, buy (exit) at bar 2 price 1
        Position pos = createClosedShortPosition(0, 2);
        CashFlow cf = new CashFlow(series, pos);

        // Initial value = 1
        assertEquals(one, cf.getValue(0));

        // For a short trade the ratio is 2 - (exit/entry)
        // entry price = 3, exit price = 1  => ratio = 2 - (1/3) = 5/3 ≈ 1.6667
        Num expected = DecimalNum.valueOf(5).dividedBy(DecimalNum.valueOf(3));
        assertEquals(expected, cf.getValue(2));

        // Padding after the last bar
        for (int i = 3; i < series.getBarCount(); i++) {
            assertEquals(expected, cf.getValue(i));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Exception handling – position not closed
     * --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithOpenPositionThrows() {
        // Create a position that is opened (enter only, no exit)
        Position openPos = createOpenLongPosition(0);
        // This constructor expects a closed position – should throw
        new CashFlow(series, openPos);
    }

    /* --------------------------------------------------------------------- *
     *  Edge cases for determineEndIndex (static, package‑private)
     * --------------------------------------------------------------------- */

    @Test
    public void testDetermineEndIndexWhenExitBeforeFinal() {
        Position pos = createClosedLongPosition(1, 3); // exit at 3
        int finalIdx = 5;
        int maxIdx = series.getEndIndex(); // 4 (0‑based)

        int endIdx = CashFlow.determineEndIndex(pos, finalIdx, maxIdx);
        // exit index (3) is smaller than finalIdx (5) => should return 3
        assertEquals(3, endIdx);
    }

    @Test
    public void testDetermineEndIndexWhenFinalBeforeExit() {
        Position pos = createClosedLongPosition(1, 4); // exit at 4
        int finalIdx = 2;
        int maxIdx = series.getEndIndex(); // 4

        int endIdx = CashFlow.determineEndIndex(pos, finalIdx, maxIdx);
        // finalIdx (2) is smaller than exit index (4) => should return 2
        assertEquals(2, endIdx);
    }

    @Test
    public void testDetermineEndIndexWhenBeyondMaxIndex() {
        Position pos = createClosedLongPosition(0, 4); // exit at 4 (last bar)
        int finalIdx = 10; // beyond series length
        int maxIdx = series.getEndIndex(); // 4

        int endIdx = CashFlow.determineEndIndex(pos, finalIdx, maxIdx);
        // Should be capped at maxIdx (4)
        assertEquals(4, endIdx);
    }

    /* --------------------------------------------------------------------- *
     *  addCost static method – both trade directions
     * --------------------------------------------------------------------- */

    @Test
    public void testAddCostLongTrade() {
        Num raw = DecimalNum.valueOf(10);
        Num cost = DecimalNum.valueOf(2);
        Num result = CashFlow.addCost(raw, cost, true);
        assertEquals(DecimalNum.valueOf(8), result);
    }

    @Test
    public void testAddCostShortTrade() {
        Num raw = DecimalNum.valueOf(10);
        Num cost = DecimalNum.valueOf(2);
        Num result = CashFlow.addCost(raw, cost, false);
        assertEquals(DecimalNum.valueOf(12), result);
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods to build positions / trades
     * --------------------------------------------------------------------- */

    /**
     * Creates a closed long (BUY) position with entry at {@code entryIdx}
     * and exit at {@code exitIdx}.
     */
    private Position createClosedLongPosition(int entryIdx, int exitIdx) {
        Trade entry = new Trade(TradeType.BUY, series.getBar(entryIdx).getClosePrice(),
                DecimalNum.ONE); // amount = 1
        Trade exit = new Trade(TradeType.SELL, series.getBar(exitIdx).getClosePrice(),
                DecimalNum.ONE);
        return new Position(entry, exit);
    }

    /**
     * Creates a closed short (SELL) position.
     */
    private Position createClosedShortPosition(int entryIdx, int exitIdx) {
        Trade entry = new Trade(TradeType.SELL, series.getBar(entryIdx).getClosePrice(),
                DecimalNum.ONE);
        Trade exit = new Trade(TradeType.BUY, series.getBar(exitIdx).getClosePrice(),
                DecimalNum.ONE);
        return new Position(entry, exit);
    }

    /**
     * Creates an open long position (only entry trade).
     */
    private Position createOpenLongPosition(int entryIdx) {
        Trade entry = new Trade(TradeType.BUY, series.getBar(entryIdx).getClosePrice(),
                DecimalNum.ONE);
        // No exit trade – Position will be opened
        return new Position(entry, null);
    }

    /**
     * Creates a TradingRecord with a single open position (enter only) and
     * validates that {@link CashFlow} correctly handles the open position when
     * a final index is supplied.
     */
    @Test
    public void testCashFlowWithOpenPositionViaTradingRecord() {
        TradingRecord record = new BaseTradingRecord();
        // open a long position at index 0 (price 1)
        record.enter(0, series.getBar(0).getClosePrice(), DecimalNum.ONE);
        // do NOT exit – position stays open

        // finalIndex = 2 (we want to accrue cash flow up to bar 2)
        CashFlow cf = new CashFlow(series, record, 2);

        // initial value
        assertEquals(one, cf.getValue(0));

        // Since the position is long and never closed, the cash flow after
        // index 2 should be the entry price (1) minus holding cost.
        // Holding cost is zero (default CostModel is zero), therefore value stays 1.
        assertEquals(one, cf.getValue(2));
        // Padding after the last bar
        for (int i = 3; i < series.getBarCount(); i++) {
            assertEquals(one, cf.getValue(i));
        }
    }
}
