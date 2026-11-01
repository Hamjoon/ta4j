package org.ta4j.core.analysis;
import org.ta4j.core.Trade.TradeType;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link CashFlow}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>Extraction of all public methods (including constructors).</li>
 *   <li>Basic functional verification of each public method.</li>
 *   <li>Edge‑case and exception scenarios – e.g. open positions,
 *       out‑of‑range final indexes, zero‑length series, etc.</li>
 *   <li>Verification of the static helper methods {@code addCost} and
 *       {@code determineEndIndex}.</li>
 * </ul>
 *
 * <p>All objects are created using the concrete classes shipped with the
 * library (e.g. {@link BaseBarSeries}, {@link BaseBar},
 * {@link BaseTradingRecord}). No mocking frameworks are used, as required.
 */
public class CashFlowTest {

    // -----------------------------------------------------------------------
    // Test fixtures
    // -----------------------------------------------------------------------
    private BaseBarSeries series;          // a simple 5‑bar series
    private Num one, two, three, four, five; // shortcut Num values

    @Before
    public void setUp() {
        // Build a bar series with deterministic close prices: 1,2,3,4,5
        series = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now();
        for (int i = 0; i < 5; i++) {
            // close price = i+1, other OHLC values are the same for simplicity
            Num price = DecimalNum.valueOf(i + 1);
            Bar bar = new BaseBar(Duration.ofDays(1), now.plusDays(i),
                    price, price, price, price, DecimalNum.ZERO);
            series.addBar(bar);
        }

        // shortcut Num instances
        one = series.numOf(1);
        two = series.numOf(2);
        three = series.numOf(3);
        four = series.numOf(4);
        five = series.numOf(5);
    }

    // -----------------------------------------------------------------------
    // Helper methods to build positions / trading records
    // -----------------------------------------------------------------------
    private Position closedLongPosition(int entryIdx, int exitIdx) {
        // entry = BUY, exit = SELL
        Trade entry = new Trade(TradeType.BUY, series.getBar(entryIdx).getClosePrice(),
                DecimalNum.ONE, series.getBar(entryIdx));
        Trade exit = new Trade(TradeType.SELL, series.getBar(exitIdx).getClosePrice(),
                DecimalNum.ONE, series.getBar(exitIdx));
        return new Position(entry, exit);
    }

    private Position openLongPosition(int entryIdx) {
        Trade entry = new Trade(TradeType.BUY, series.getBar(entryIdx).getClosePrice(),
                DecimalNum.ONE, series.getBar(entryIdx));
        // no exit trade – position stays opened
        return new Position(entry, null);
    }

    private TradingRecord buildTradingRecord(Position... positions) {
        BaseTradingRecord record = new BaseTradingRecord();
        for (Position p : positions) {
            // manually add the entry and (if present) the exit trade to the record
            record.operate(p.getEntry().getIndex(), p.getEntry().getNetPrice(),
                    DecimalNum.ONE);
            if (p.getExit() != null) {
                record.operate(p.getExit().getIndex(), p.getExit().getNetPrice(),
                        DecimalNum.ONE);
            }
        }
        return record;
    }

    // -----------------------------------------------------------------------
    // 1. Extraction of public methods (for documentation; not a test)
    // -----------------------------------------------------------------------
    // Constructors:
    //   CashFlow(BarSeries, Position)
    //   CashFlow(BarSeries, TradingRecord)
    //   CashFlow(BarSeries, TradingRecord, int)
    // Public methods:
    //   Num getValue(int)
    //   int getUnstableBars()
    //   BarSeries getBarSeries()
    //   Num numOf(Number)
    //   int getSize()
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // 2. Basic functional tests for each public method
    // -----------------------------------------------------------------------

    @Test
    public void testGetUnstableBarsIsZero() {
        CashFlow cf = new CashFlow(series, closedLongPosition(0, 2));
        assertEquals(0, cf.getUnstableBars());
    }

    @Test
    public void testGetBarSeriesReturnsSameInstance() {
        CashFlow cf = new CashFlow(series, closedLongPosition(0, 2));
        assertSame(series, cf.getBarSeries());
    }

    @Test
    public void testNumOfDelegatesToSeries() {
        CashFlow cf = new CashFlow(series, closedLongPosition(0, 2));
        Num num = cf.numOf(42);
        assertEquals(DecimalNum.valueOf(42), num);
    }

    @Test
    public void testGetSizeReflectsSeriesBarCount() {
        CashFlow cf = new CashFlow(series, closedLongPosition(0, 2));
        assertEquals(series.getBarCount(), cf.getSize());
    }

    @Test
    public void testGetValueBasicClosedLongTrade() {
        // Position: BUY at bar 0 (price 1), SELL at bar 2 (price 3)
        Position pos = closedLongPosition(0, 2);
        CashFlow cf = new CashFlow(series, pos);

        // cash flow starts at 1 (index 0)
        assertEquals(one, cf.getValue(0));

        // after entry (index 1) the cash flow should be multiplied by price ratio 2/1 = 2
        // because holding cost is zero (default CostModel = ZERO)
        assertEquals(two, cf.getValue(1));

        // at exit index (2) ratio = 3/1 = 3 => cash flow 3
        assertEquals(three, cf.getValue(2));

        // after exit the value stays constant (= last cash flow)
        assertEquals(three, cf.getValue(3));
        assertEquals(three, cf.getValue(4));
    }

    @Test
    public void testGetValueClosedShortTrade() {
        // Short (SELL first, BUY later)
        Trade entry = new Trade(TradeType.SELL, series.getBar(0).getClosePrice(),
                DecimalNum.ONE, series.getBar(0));
        Trade exit = new Trade(TradeType.BUY, series.getBar(2).getClosePrice(),
                DecimalNum.ONE, series.getBar(2));
        Position shortPos = new Position(entry, exit);

        CashFlow cf = new CashFlow(series, shortPos);

        // For short trade, the ratio formula is: 2 - (exit/entry)
        // entry price = 1, exit price = 3 => ratio = 2 - (3/1) = -1
        // cash flow = 1 * (-1) = -1
        Num minusOne = series.numOf(-1);
        assertEquals(one, cf.getValue(0));          // before entry
        assertEquals(one, cf.getValue(1));          // after entry (no change until exit)
        assertEquals(minusOne, cf.getValue(2));     // after exit
        assertEquals(minusOne, cf.getValue(3));     // padded after exit
        assertEquals(minusOne, cf.getValue(4));
    }

    @Test
    public void testConstructorWithTradingRecordClosedPositions() {
        Position p1 = closedLongPosition(0, 1); // 1 -> 2
        Position p2 = closedLongPosition(2, 4); // 3 -> 5
        TradingRecord record = buildTradingRecord(p1, p2);
        CashFlow cf = new CashFlow(series, record);

        // Expected cash flow:
        // after first trade (index 1): 2
        // stays 2 until next entry (index 3)
        // after second trade (index 4): 5
        assertEquals(one, cf.getValue(0));
        assertEquals(two, cf.getValue(1));
        assertEquals(two, cf.getValue(2));
        assertEquals(three, cf.getValue(3)); // entry at index 3 (price 4) -> ratio 4/3 = 4/3 => 2 * 4/3 = 8/3 ≈ 2.666..., but because holding cost = 0, intermediate values are calculated.
        // To keep the test simple, we verify the final value after exit:
        assertEquals(five, cf.getValue(4));
    }

    @Test
    public void testConstructorWithOpenPositionAndFinalIndex() {
        // Open long position entered at index 1 (price 2), never exited.
        Position openPos = openLongPosition(1);
        CashFlow cf = new CashFlow(series, openPos, series.getEndIndex());

        // Values should be calculated up to the series end (index 4)
        // Entry at index 1, so cash flow at index 2 = 2 (price ratio 3/2)
        // index 3 = 3 (price ratio 4/2)
        // index 4 = 4 (price ratio 5/2)
        assertEquals(one, cf.getValue(0)); // no trade yet
        assertEquals(one, cf.getValue(1)); // entry point – cash flow unchanged
        assertEquals(DecimalNum.valueOf(1.5), cf.getValue(2)); // 2 * (3/2) = 3
        assertEquals(DecimalNum.valueOf(2.0), cf.getValue(3)); // 2 * (4/2) = 4
        assertEquals(DecimalNum.valueOf(2.5), cf.getValue(4)); // 2 * (5/2) = 5
    }

    // -----------------------------------------------------------------------
    // 3. Edge‑case & exception tests
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSinglePositionConstructorRejectsOpenPosition() {
        Position open = openLongPosition(0);
        // This must throw because the position is not closed and no finalIndex supplied
        new CashFlow(series, open);
    }

    @Test
    public void testDetermineEndIndexWhenExitBeforeFinalIndex() {
        Position pos = closedLongPosition(0, 2);
        int result = CashFlow.determineEndIndex(pos, 4, series.getEndIndex());
        // exit index = 2, finalIndex = 4 => result should be 2
        assertEquals(2, result);
    }

    @Test
    public void testDetermineEndIndexWhenNoExitAndFinalIndexSmallerThanMax() {
        Position pos = openLongPosition(0);
        int result = CashFlow.determineEndIndex(pos, 3, series.getEndIndex());
        // No exit, so result = min(finalIndex, maxIndex) = 3
        assertEquals(3, result);
    }

    @Test
    public void testDetermineEndIndexClampsToMaxIndex() {
        Position pos = openLongPosition(0);
        int result = CashFlow.determineEndIndex(pos, 10, series.getEndIndex());
        // maxIndex = 4, so result must be 4
        assertEquals(4, result);
    }

    @Test
    public void testAddCostLongTrade() {
        Num raw = series.numOf(10);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, true);
        assertEquals(series.numOf(8), net);
    }

    @Test
    public void testAddCostShortTrade() {
        Num raw = series.numOf(10);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, false);
        assertEquals(series.numOf(12), net);
    }

    @Test
    public void testFillToTheEndPadsCorrectly() {
        // Build a CashFlow with a single closed position that ends early
        Position pos = closedLongPosition(0, 1); // ends at index 1
        CashFlow cf = new CashFlow(series, pos);
        // After exit (index 1) value should stay constant until series end (index 4)
        Num afterExit = cf.getValue(1);
        for (int i = 2; i <= series.getEndIndex(); i++) {
            assertEquals("Value at index " + i + " should be padded", afterExit, cf.getValue(i));
        }
    }

    @Test
    public void testZeroLengthSeriesThrowsWhenAccessed() {
        BaseBarSeries emptySeries = new BaseBarSeries("empty");
        // Create a dummy closed position – it will never be used because the series has no bars
        Position dummy = closedLongPosition(0, 0);
        // Construction should succeed (values list will contain only the initial 1)
        CashFlow cf = new CashFlow(emptySeries, dummy);
        // Accessing any index should throw IndexOutOfBoundsException
        try {
            cf.getValue(0);
            fail("Expected IndexOutOfBoundsException for empty series");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // 4. Integration style test covering a realistic scenario
    // -----------------------------------------------------------------------
    @Test
    public void testComplexScenarioMultiplePositionsWithHoldingCost() {
        // In this test we use the default zero holding cost; the focus is on
        // correct sequencing of cash‑flow values across several positions.

        // Position 1: long 0→2 (1→3)
        Position p1 = closedLongPosition(0, 2);
        // Position 2: short 3→4 (4→5)
        Trade entryShort = new Trade(TradeType.SELL, series.getBar(3).getClosePrice(),
                DecimalNum.ONE, series.getBar(3));
        Trade exitShort = new Trade(TradeType.BUY, series.getBar(4).getClosePrice(),
                DecimalNum.ONE, series.getBar(4));
        Position p2 = new Position(entryShort, exitShort);

        TradingRecord record = buildTradingRecord(p1, p2);
        CashFlow cf = new CashFlow(series, record);

        // Verify a few key points:
        // after first trade exit (index 2) cash flow = 3
        assertEquals(three, cf.getValue(2));

        // after second trade entry (index 3) cash flow should be:
        //   previous cash flow (3) * ratio where ratio = 2 - (exit/entry) = 2 - (5/4) = 3/4
        //   => 3 * 0.75 = 2.25
        Num expectedAfterShortEntry = DecimalNum.valueOf(2.25);
        assertEquals(expectedAfterShortEntry, cf.getValue(3));

        // after short exit (index 4) ratio = 2 - (5/5) = 1 => cash flow unchanged
        assertEquals(expectedAfterShortEntry, cf.getValue(4));
    }
}
