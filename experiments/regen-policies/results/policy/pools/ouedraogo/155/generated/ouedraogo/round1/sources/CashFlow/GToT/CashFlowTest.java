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
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.AnalysisUtils;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link CashFlow}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>All public methods (including getters, size, and unstable‑bars count)</li>
 *   <li>Construction with a single closed {@link Position}</li>
 *   <li>Construction with a {@link TradingRecord} (multiple closed positions and an
 *       open position)</li>
 *   <li>Edge‑cases such as an empty {@link BarSeries}, a position that is still
 *       opened, and final indexes larger than the series length</li>
 *   <li>Correct handling of long and short trades via the private
 *       {@code getIntermediateRatio} method (accessed through reflection)</li>
 * </ul>
 * </p>
 *
 * <p>
 * All objects are created using the public API of the library – no mocking or
 * subclassing of library interfaces is performed.
 * </p>
 */
public class CashFlowTest {

    /** Simple 5‑bar series used by most tests */
    private BarSeries series;

    /** Helper to create a {@link Num} from a double */
    private Num n(double v) {
        return series.numFactory().numOf(v);
    }

    /** Build a bar with the same open/high/low/close price (no volume needed) */
    private Bar barAt(int index, double price) {
        // Bar constructor: (Duration, ZonedDateTime, open, high, low, close, volume)
        // We set all price fields to the same value for simplicity.
        return new BaseBar(Duration.ofDays(1),
                ZonedDateTime.now().plusDays(index),
                n(price), n(price), n(price), n(price), n(0));
    }

    @Before
    public void setUp() {
        series = new BaseBarSeries("test-series");
        // 5 bars with increasing close price 1..5
        for (int i = 0; i < 5; i++) {
            series.addBar(barAt(i, i + 1));
        }
    }

    /** -------------------------------------------------------------------- *
     *  PUBLIC METHOD TESTS
     *  -------------------------------------------------------------------- */

    @Test
    public void testGetBarSeries() {
        CashFlow cf = new CashFlow(series, new Position());
        assertSame("getBarSeries should return the series passed to the constructor",
                series, cf.getBarSeries());
    }

    @Test
    public void testGetCountOfUnstableBars() {
        CashFlow cf = new CashFlow(series, new Position());
        assertEquals("CashFlow never reports unstable bars", 0, cf.getCountOfUnstableBars());
    }

    @Test
    public void testGetSize() {
        CashFlow cf = new CashFlow(series, new Position());
        assertEquals("Size must equal the underlying series bar count",
                series.getBarCount(), cf.getSize());
    }

    @Test
    public void testGetValueAtZero() {
        CashFlow cf = new CashFlow(series, new Position());
        // The very first value is always 1 (numFactory().one())
        assertEquals("First cash‑flow value must be 1", series.numFactory().one(), cf.getValue(0));
    }

    /** -------------------------------------------------------------------- *
     *  CONSTRUCTOR WITH SINGLE CLOSED POSITION
     *  -------------------------------------------------------------------- */

    @Test
    public void testSingleClosedLongPosition() {
        // entry at index 0 (price 1), exit at index 3 (price 4)
        Trade entry = new Trade(TradeType.BUY, 0, n(1), n(1));
        Trade exit  = new Trade(TradeType.SELL, 3, n(4), n(1));
        Position pos = new Position(entry, exit);

        CashFlow cf = new CashFlow(series, pos);

        // cash‑flow should start with 1 and finish with the gross return of the trade
        // (4 / 1) = 4
        Num expectedFinal = n(4);
        assertEquals("Final cash‑flow value must equal gross return", expectedFinal,
                cf.getValue(cf.getSize() - 1));
    }

    @Test
    public void testSingleClosedShortPosition() {
        // Short: entry SELL at price 5 (index 0), exit BUY at price 2 (index 2)
        Trade entry = new Trade(TradeType.SELL, 0, n(5), n(1));
        Trade exit  = new Trade(TradeType.BUY, 2, n(2), n(1));
        Position pos = new Position(entry, exit);

        CashFlow cf = new CashFlow(series, pos);

        // For short trades the ratio is 2 - (exit/entry)
        // ratio = 2 - (2/5) = 2 - 0.4 = 1.6
        Num expectedFinal = n(1.6);
        assertEquals("Final cash‑flow for short trade", expectedFinal,
                cf.getValue(cf.getSize() - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithOpenPositionThrows() {
        // Position without an exit (still opened)
        Trade entry = new Trade(TradeType.BUY, 0, n(1), n(1));
        Position opened = new Position(entry, null);
        // should throw because the position is not closed
        new CashFlow(series, opened);
    }

    /** -------------------------------------------------------------------- *
     *  CONSTRUCTOR WITH TRADING RECORD (multiple positions)
     *  -------------------------------------------------------------------- */

    @Test
    public void testTradingRecordMultipleClosedPositions() {
        // Build a trading record with two closed positions:
        // 1) Long 1→3 (price 1 to 4)   ratio = 4
        // 2) Short 4→4 (price 5 to 5) ratio = 2 - (5/5) = 1
        Trade entry1 = new Trade(TradeType.BUY, 0, n(1), n(1));
        Trade exit1  = new Trade(TradeType.SELL, 3, n(4), n(1));
        Position pos1 = new Position(entry1, exit1);

        Trade entry2 = new Trade(TradeType.SELL, 4, n(5), n(1));
        Trade exit2  = new Trade(TradeType.BUY, 4, n(5), n(1));
        Position pos2 = new Position(entry2, exit2);

        TradingRecord record = new org.ta4j.core.BaseTradingRecord();
        record.operate(entry1.getIndex(), entry1.getNetPrice(), entry1.getAmount());
        record.operate(exit1.getIndex(), exit1.getNetPrice(), exit1.getAmount());
        record.operate(entry2.getIndex(), entry2.getNetPrice(), entry2.getAmount());
        record.operate(exit2.getIndex(), exit2.getNetPrice(), exit2.getAmount());

        CashFlow cf = new CashFlow(series, record);

        // After first position cash flow should be 4 (1 * 4)
        Num afterFirst = cf.getValue(3);
        assertEquals("Cash flow after first closed long position", n(4), afterFirst);

        // After second (short) position cash flow should stay 4 * 1 = 4
        Num afterSecond = cf.getValue(cf.getSize() - 1);
        assertEquals("Cash flow after short position with neutral price", n(4), afterSecond);
    }

    @Test
    public void testTradingRecordWithOpenPositionAccruedCashFlow() {
        // One closed long (0→2) and one open long (3→open)
        Trade entry1 = new Trade(TradeType.BUY, 0, n(1), n(1));
        Trade exit1  = new Trade(TradeType.SELL, 2, n(3), n(1));
        Position closed = new Position(entry1, exit1);

        Trade entry2 = new Trade(TradeType.BUY, 3, n(4), n(1));
        Position open = new Position(entry2, null); // still opened

        TradingRecord record = new org.ta4j.core.BaseTradingRecord();
        // add closed position
        record.operate(entry1.getIndex(), entry1.getNetPrice(), entry1.getAmount());
        record.operate(exit1.getIndex(), exit1.getNetPrice(), exit1.getAmount());
        // start open position
        record.operate(entry2.getIndex(), entry2.getNetPrice(), entry2.getAmount());

        // Use a finalIndex that points to the last bar (4)
        CashFlow cf = new CashFlow(series, record, series.getEndIndex());

        // Verify that cash flow has been accrued up to the final index (4)
        // Closed part: ratio = 3 / 1 = 3  → cash after index 2 = 3
        // Open part: entry price 4, last close price = 5 → ratio = 5/4 = 1.25
        // Expected final cash = 3 * 1.25 = 3.75
        Num expected = n(3.75);
        assertEquals("Final cash flow with an open position accrued to final index",
                expected, cf.getValue(cf.getSize() - 1));
    }

    /** -------------------------------------------------------------------- *
     *  PRIVATE STATIC METHOD getIntermediateRatio (reflection)
     *  -------------------------------------------------------------------- */

    @Test
    public void testGetIntermediateRatioLong() throws Exception {
        // Use reflection to access the private static method
        java.lang.reflect.Method method = CashFlow.class.getDeclaredMethod(
                "getIntermediateRatio", boolean.class, Num.class, Num.class);
        method.setAccessible(true);

        Num entry = n(2);
        Num exit  = n(5);
        Num ratio = (Num) method.invoke(null, true, entry, exit);
        // Long trade: ratio = exit / entry = 5 / 2 = 2.5
        assertEquals("Long trade ratio", n(2.5), ratio);
    }

    @Test
    public void testGetIntermediateRatioShort() throws Exception {
        java.lang.reflect.Method method = CashFlow.class.getDeclaredMethod(
                "getIntermediateRatio", boolean.class, Num.class, Num.class);
        method.setAccessible(true);

        Num entry = n(5);
        Num exit  = n(2);
        Num ratio = (Num) method.invoke(null, false, entry, exit);
        // Short trade: ratio = 2 - (exit/entry) = 2 - (2/5) = 1.6
        assertEquals("Short trade ratio", n(1.6), ratio);
    }

    /** -------------------------------------------------------------------- *
     *  EDGE CASES
     *  -------------------------------------------------------------------- */

    @Test
    public void testEmptySeries() {
        BarSeries empty = new BaseBarSeries("empty");
        // No bars – the CashFlow should still contain the initial 1 value
        CashFlow cf = new CashFlow(empty, new Position());
        assertEquals("Size of empty series", 0, cf.getSize());
        assertEquals("Value at index 0 should still be 1", empty.numFactory().one(), cf.getValue(0));
    }

    @Test
    public void testFinalIndexBeyondSeriesLength() {
        // Build a record with a closed position, then request a finalIndex > endIndex
        Trade entry = new Trade(TradeType.BUY, 0, n(1), n(1));
        Trade exit  = new Trade(TradeType.SELL, 2, n(3), n(1));
        Position pos = new Position(entry, exit);
        TradingRecord record = new org.ta4j.core.BaseTradingRecord();
        record.operate(entry.getIndex(), entry.getNetPrice(), entry.getAmount());
        record.operate(exit.getIndex(), exit.getNetPrice(), exit.getAmount());

        // finalIndex = 10 (far beyond series endIndex = 4)
        CashFlow cf = new CashFlow(series, record, 10);

        // The cash‑flow list must be padded up to the series end (index 4)
        assertEquals("Cash flow size must equal series size", series.getBarCount(), cf.getSize());
        // The last value should be the same as after the exit (3 / 1 = 3)
        assertEquals("Last cash‑flow value after padding", n(3), cf.getValue(cf.getSize() - 1));
    }

    /** -------------------------------------------------------------------- *
     *  INTERNAL Padding Verification (fillToTheEnd)
     *  -------------------------------------------------------------------- */

    @Test
    public void testFillToTheEndPadsWithLastValue() {
        // Create a CashFlow with a single closed position that ends at index 1
        Trade entry = new Trade(TradeType.BUY, 0, n(1), n(1));
        Trade exit  = new Trade(TradeType.SELL, 1, n(2), n(1));
        Position pos = new Position(entry, exit);
        CashFlow cf = new CashFlow(series, pos);

        // Values after index 1 should be constant (ratio = 2)
        Num expected = n(2); // 1 * (2/1)
        for (int i = 1; i < cf.getSize(); i++) {
            assertEquals("Value after fillToTheEnd should remain constant", expected, cf.getValue(i));
        }
    }
}
