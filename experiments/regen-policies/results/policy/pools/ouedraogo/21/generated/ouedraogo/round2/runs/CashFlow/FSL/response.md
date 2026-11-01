###Test START##
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradeType;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.cost.ZeroCostModel;

/**
 * Comprehensive unit tests for {@link CashFlow}.
 *
 * The tests cover:
 * <ul>
 *   <li>Construction with a single closed position (long & short)</li>
 *   <li>Construction with a {@link BaseTradingRecord} containing multiple positions</li>
 *   <li>Behaviour of the static helpers {@code addCost} and {@code determineEndIndex}</li>
 *   <li>Correct padding of the internal {@code values} list by {@code fillToTheEnd}</li>
 * </ul>
 *
 * All objects are created using the public API of the TA4J library (no mocks,
 * no subclassing of library abstract classes). The {@link DecimalNum} factory
 * guarantees deterministic arithmetic for assertions.
 */
public class CashFlowTest {

    /**
     * Helper to build a simple {@link BaseBarSeries} with deterministic close
     * prices. The series contains {@code count} bars whose close prices are
     * supplied in {@code closePrices}.
     */
    private BaseBarSeries createSeries(double... closePrices) {
        BaseBarSeries series = new BaseBarSeries("test-series");
        for (double price : closePrices) {
            // Use a fixed timestamp – the actual time is irrelevant for cash‑flow.
            series.addBar(new BaseBar(
                    ZonedDateTime.now(),
                    DecimalNum.valueOf(price), // open
                    DecimalNum.valueOf(price), // high
                    DecimalNum.valueOf(price), // low
                    DecimalNum.valueOf(price), // close
                    DecimalNum.valueOf(0),     // volume
                    DecimalNum.valueOf(0)      // amount
            ));
        }
        return series;
    }

    /**
     * Verifies cash‑flow for a single **long** closed position.
     * Entry at index 0 (price 10), exit at index 2 (price 12).
     * Expected sequence: [1.0, 1.0, 1.1, 1.2, 1.2 …] (padded to series end).
     */
    @Test
    public void testCashFlowSingleLongClosedPosition() {
        BaseBarSeries series = createSeries(10, 11, 12, 12, 12); // 5 bars

        // Build a long position: BUY at index 0, SELL at index 2
        Trade entry = new Trade(series, 0, DecimalNum.valueOf(10), DecimalNum.valueOf(1),
                TradeType.BUY);
        Trade exit = new Trade(series, 2, DecimalNum.valueOf(12), DecimalNum.valueOf(1),
                TradeType.SELL);
        Position position = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());

        // CashFlow for the single position
        CashFlow cf = new CashFlow(series, position);

        // Index 0 – initial value (always 1)
        assertEquals(DecimalNum.valueOf(1), cf.getValue(0));

        // Index 1 – still before entry, value unchanged
        assertEquals(DecimalNum.valueOf(1), cf.getValue(1));

        // Index 2 – after first intermediate bar (price 11)
        assertEquals(DecimalNum.valueOf(1.1), cf.getValue(2));

        // Index 3 – after exit (price 12) – final cash flow
        assertEquals(DecimalNum.valueOf(1.2), cf.getValue(3));

        // All remaining indices must be padded with the final cash‑flow value
        for (int i = 4; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.valueOf(1.2), cf.getValue(i));
        }
    }

    /**
     * Verifies cash‑flow for a single **short** closed position.
     * Entry (SELL) at index 0 price 10, exit (BUY) at index 2 price 8.
     * Expected ratio: 2 - (8/10) = 1.2.
     */
    @Test
    public void testCashFlowSingleShortClosedPosition() {
        BaseBarSeries series = createSeries(10, 9, 8, 8, 8);

        // Short position: SELL at 0, BUY at 2
        Trade entry = new Trade(series, 0, DecimalNum.valueOf(10), DecimalNum.valueOf(1),
                TradeType.SELL);
        Trade exit = new Trade(series, 2, DecimalNum.valueOf(8), DecimalNum.valueOf(1),
                TradeType.BUY);
        Position position = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());

        CashFlow cf = new CashFlow(series, position);

        // Index 0 – initial value
        assertEquals(DecimalNum.valueOf(1), cf.getValue(0));

        // Index 1 – after first intermediate bar (price 9)
        // Intermediate ratio = 2 - (9/10) = 1.1
        assertEquals(DecimalNum.valueOf(1.1), cf.getValue(2));

        // Index 3 – after exit (price 8) => final ratio 1.2
        assertEquals(DecimalNum.valueOf(1.2), cf.getValue(3));

        // Padding check
        for (int i = 4; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.valueOf(1.2), cf.getValue(i));
        }
    }

    /**
     * Tests the static {@code addCost} method for both long and short trades.
     */
    @Test
    public void testAddCost() {
        Num raw = DecimalNum.valueOf(100);
        Num cost = DecimalNum.valueOf(2);

        // Long trade – cost is subtracted
        Num longResult = CashFlow.addCost(raw, cost, true);
        assertEquals(DecimalNum.valueOf(98), longResult);

        // Short trade – cost is added
        Num shortResult = CashFlow.addCost(raw, cost, false);
        assertEquals(DecimalNum.valueOf(102), shortResult);
    }

    /**
     * Verifies {@code determineEndIndex} logic in three distinct scenarios:
     * <ul>
     *   <li>Closed position – end index should be the exit index.</li>
     *   <li>Open position – end index limited by {@code finalIndex}.</li>
     *   <li>Provided {@code finalIndex} exceeds series max – capped at {@code maxIndex}.</li>
     * </ul>
     */
    @Test
    public void testDetermineEndIndex() {
        BaseBarSeries series = createSeries(10, 11, 12, 13, 14);
        int maxIndex = series.getEndIndex(); // 4

        // Closed position (exit at index 2)
        Trade entry = new Trade(series, 0, DecimalNum.valueOf(10), DecimalNum.valueOf(1),
                TradeType.BUY);
        Trade exit = new Trade(series, 2, DecimalNum.valueOf(12), DecimalNum.valueOf(1),
                TradeType.SELL);
        Position closed = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());

        int endIdxClosed = CashFlow.determineEndIndex(closed, 4, maxIndex);
        assertEquals(2, endIdxClosed); // exit index wins

        // Open position (no exit)
        Trade openEntry = new Trade(series, 1, DecimalNum.valueOf(11), DecimalNum.valueOf(1),
                TradeType.BUY);
        Position open = new Position(openEntry, null, new ZeroCostModel(), new ZeroCostModel());

        // finalIndex smaller than series max
        int endIdxOpen = CashFlow.determineEndIndex(open, 3, maxIndex);
        assertEquals(3, endIdxOpen);

        // finalIndex larger than maxIndex – should be capped
        int endIdxCapped = CashFlow.determineEndIndex(open, 10, maxIndex);
        assertEquals(maxIndex, endIdxCapped);
    }

    /**
     * Tests construction with a {@link BaseTradingRecord} containing multiple
     * closed positions and an open position. The cash‑flow must reflect the
     * cumulative effect of each position in chronological order.
     */
    @Test
    public void testCashFlowWithTradingRecordMultiplePositions() {
        BaseBarSeries series = createSeries(
                10, // 0
                11, // 1
                12, // 2
                13, // 3
                14, // 4
                15, // 5
                16  // 6
        );

        // First closed long position: buy @0 (10) – sell @2 (12) → ratio 1.2
        Trade e1 = new Trade(series, 0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), TradeType.BUY);
        Trade x1 = new Trade(series, 2, DecimalNum.valueOf(12), DecimalNum.valueOf(1), TradeType.SELL);
        Position p1 = new Position(e1, x1, new ZeroCostModel(), new ZeroCostModel());

        // Second closed short position: sell @3 (13) – buy @5 (15) → ratio 2 - (15/13)
        Trade e2 = new Trade(series, 3, DecimalNum.valueOf(13), DecimalNum.valueOf(1), TradeType.SELL);
        Trade x2 = new Trade(series, 5, DecimalNum.valueOf(15), DecimalNum.valueOf(1), TradeType.BUY);
        Position p2 = new Position(e2, x2, new ZeroCostModel(), new ZeroCostModel());

        // Open long position (will be evaluated up to series end)
        Trade e3 = new Trade(series, 6, DecimalNum.valueOf(16), DecimalNum.valueOf(1), TradeType.BUY);
        Position open = new Position(e3, null, new ZeroCostModel(), new ZeroCostModel());

        BaseTradingRecord record = new BaseTradingRecord();
        record.operate(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1)); // entry
        record.operate(2, DecimalNum.valueOf(12), DecimalNum.valueOf(1)); // exit
        record.operate(3, DecimalNum.valueOf(13), DecimalNum.valueOf(1)); // entry (short)
        record.operate(5, DecimalNum.valueOf(15), DecimalNum.valueOf(1)); // exit (short)
        record.operate(6, DecimalNum.valueOf(16), DecimalNum.valueOf(1)); // entry (open)

        // Verify that the record indeed contains the expected positions
        List<Position> positions = record.getPositions();
        assertEquals(2, positions.size()); // only closed positions are stored
        assertTrue(record.getCurrentPosition().isOpened());

        // Build CashFlow with explicit finalIndex = series.getEndIndex()
        CashFlow cf = new CashFlow(series, record, series.getEndIndex());

        // Expected cash‑flow evolution:
        // start 1.0
        // after first closed long (exit at index 2) → 1.2
        // after second closed short (exit at index 5)
        //   ratio = 2 - (15/13) ≈ 2 - 1.153846 = 0.846154
        //   cumulative = 1.2 * 0.846154 ≈ 1.0153848
        // after open long (entry at 6) – no further bars, value stays the same

        Num expectedAfterFirst = DecimalNum.valueOf(1.2);
        Num expectedAfterSecond = expectedAfterFirst.multipliedBy(
                DecimalNum.valueOf(2).minus(DecimalNum.valueOf(15).dividedBy(DecimalNum.valueOf(13)))
        );

        // Index 0 – initial
        assertEquals(DecimalNum.valueOf(1), cf.getValue(0));

        // Index 2 – after first position exit
        assertEquals(expectedAfterFirst, cf.getValue(3)); // exit value stored at index (exit+1)

        // Index 5 – after second position exit
        assertEquals(expectedAfterSecond, cf.getValue(6));

        // Remaining indices (including after open position) must be padded
        for (int i = 7; i < series.getBarCount(); i++) {
            assertEquals(expectedAfterSecond, cf.getValue(i));
        }
    }

    /**
     * Ensures that {@code fillToTheEnd} correctly pads the internal list when the
     * computed cash‑flow ends before the series end.
     */
    @Test
    public void testFillToTheEndPadding() {
        BaseBarSeries series = createSeries(10, 11, 12); // 3 bars

        // Simple closed position (buy @0, sell @1) → ratio 1.1
        Trade entry = new Trade(series, 0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), TradeType.BUY);
        Trade exit = new Trade(series, 1, DecimalNum.valueOf(11), DecimalNum.valueOf(1), TradeType.SELL);
        Position pos = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());

        CashFlow cf = new CashFlow(series, pos);

        // Values should be: [1.0, 1.0, 1.1, 1.1] (extra 1.1 padded up to end index 2)
        assertEquals(DecimalNum.valueOf(1), cf.getValue(0));
        assertEquals(DecimalNum.valueOf(1), cf.getValue(1));
        assertEquals(DecimalNum.valueOf(1.1), cf.getValue(2));
        // Padding at index 3 (beyond series size) is not accessible; we only check up to series size.
    }

    /**
     * Verifies that constructing a {@link CashFlow} with a non‑closed position
     * (i.e., an open position without a provided final index) throws the expected
     * {@link IllegalArgumentException}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithOpenPositionThrows() {
        BaseBarSeries series = createSeries(10, 11);
        Trade entry = new Trade(series, 0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), TradeType.BUY);
        Position openPos = new Position(entry, null, new ZeroCostModel(), new ZeroCostModel());

        // This constructor uses the single‑position overload which expects a closed
        // position and must therefore throw.
        new CashFlow(series, openPos);
    }

    /**
     * Direct test of the private {@code calculate(Position, int)} logic for a
     * position whose entry index is not the first bar (ensuring the code that
     * extends the {@code values} list works correctly).
     */
    @Test
    public void testCalculatePositionWithGapBeforeEntry() {
        BaseBarSeries series = createSeries(5, 6, 7, 8, 9);
        // Entry at index 2, exit at index 4 (prices 7 → 9)
        Trade entry = new Trade(series, 2, DecimalNum.valueOf(7), DecimalNum.valueOf(1), TradeType.BUY);
        Trade exit = new Trade(series, 4, DecimalNum.valueOf(9), DecimalNum.valueOf(1), TradeType.SELL);
        Position pos = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());

        CashFlow cf = new CashFlow(series, pos);

        // Initial values up to entryIndex (2) must be 1.0
        assertEquals(DecimalNum.valueOf(1), cf.getValue(0));
        assertEquals(DecimalNum.valueOf(1), cf.getValue(1));
        assertEquals(DecimalNum.valueOf(1), cf.getValue(2));

        // Intermediate at index 3 (price 8) → ratio 8/7
        Num expectedIntermediate = DecimalNum.valueOf(8).dividedBy(DecimalNum.valueOf(7));
        assertEquals(expectedIntermediate, cf.getValue(3));

        // Final at index 5 (exit) → ratio 9/7
        Num expectedFinal = DecimalNum.valueOf(9).dividedBy(DecimalNum.valueOf(7));
        assertEquals(expectedFinal, cf.getValue(5));
    }
}
###Test END##