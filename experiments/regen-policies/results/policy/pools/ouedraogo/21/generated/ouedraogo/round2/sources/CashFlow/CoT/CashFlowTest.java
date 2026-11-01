package org.ta4j.core.analysis;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Trade.TradeType;


import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradeType;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.DefaultNumFactory;

/**
 * Unit tests for {@link CashFlow}.
 *
 * The tests are written in the same package (org.ta4j.core.analysis) so that
 * package‑private static methods {@code addCost} and {@code determineEndIndex}
 * can be accessed directly.
 *
 * The library provides concrete implementations for the required TA4J classes
 * (BaseBarSeries, BaseBar, Trade, Position, TradingRecord, etc.).  Those
 * implementations are used to build realistic scenarios without any mocking.
 */
public class CashFlowTest {

    private BarSeries series;
    private NumFactory numFactory;

    @Before
    public void setUp() {
        // Use DecimalNum (high‑precision implementation) for deterministic arithmetic
        numFactory = new DefaultNumFactory();

        // Build a simple bar series with 6 bars (indices 0‑5)
        series = new BaseBarSeries.Builder()
                .withName("test-series")
                .withNumFactory(numFactory)
                .build();

        // Add dummy bars – close price = index + 1 (i.e. 1,2,3,4,5,6)
        for (int i = 0; i < 6; i++) {
            Bar bar = new BaseBar(
                    // duration is irrelevant for these tests
                    java.time.Duration.ofDays(1),
                    series.getBarData().size(),
                    numFactory.numOf(i + 1), // open price
                    numFactory.numOf(i + 1), // high
                    numFactory.numOf(i + 1), // low
                    numFactory.numOf(i + 1), // close
                    numFactory.numOf(0),      // volume
                    numFactory.numOf(0)       // amount (not used)
            );
            series.addBar(bar);
        }
    }

    /* ----------------------------------------------------------------------
     *  PUBLIC METHOD LIST (extracted from CashFlow)
     * ----------------------------------------------------------------------
     *
     *  public CashFlow(BarSeries barSeries, Position position)
     *  public CashFlow(BarSeries barSeries, TradingRecord tradingRecord)
     *  public CashFlow(BarSeries barSeries, TradingRecord tradingRecord, int finalIndex)
     *
     *  @Override public Num getValue(int index)
     *  @Override public int getUnstableBars()
     *  @Override public BarSeries getBarSeries()
     *
     *  public int getSize()
     *
     *  private void calculate(Position position)
     *  private void calculate(Position position, int finalIndex)
     *  private static Num getIntermediateRatio(boolean isLongTrade, Num entryPrice, Num exitPrice)
     *  private void calculate(TradingRecord tradingRecord)
     *  private void calculate(TradingRecord tradingRecord, int finalIndex)
     *
     *  static Num addCost(Num rawPrice, Num holdingCost, boolean isLongTrade)
     *  private void fillToTheEnd(int endIndex)
     *  static int determineEndIndex(Position position, int finalIndex, int maxIndex)
     */

    /* ----------------------------------------------------------------------
     *  BASIC FUNCTIONALITY TESTS
     * ---------------------------------------------------------------------- */

    @Test
    public void testGetBarSeriesAndSize() {
        CashFlow cf = new CashFlow(series, new Position(TradeType.BUY));
        assertSame("CashFlow should return the same BarSeries instance", series, cf.getBarSeries());
        assertEquals("Size must match series bar count", series.getBarCount(), cf.getSize());
    }

    @Test
    public void testGetUnstableBarsIsZero() {
        CashFlow cf = new CashFlow(series, new Position(TradeType.BUY));
        assertEquals("Unstable bars should be 0 for CashFlow", 0, cf.getUnstableBars());
    }

    @Test
    public void testSingleClosedPositionCashFlow() {
        // Build a closed BUY position: entry at index 1 (price 2), exit at index 4 (price 5)
        Trade entry = new Trade(TradeType.BUY, 1, numFactory.numOf(2), numFactory.numOf(1));
        Trade exit = new Trade(TradeType.SELL, 4, numFactory.numOf(5), numFactory.numOf(1));
        Position position = new Position(entry, exit);

        CashFlow cf = new CashFlow(series, position);

        // The first value is always 1 (as set by the constructor)
        assertEquals(numFactory.one(), cf.getValue(0));

        // Verify that the cash‑flow at the exit index reflects the price ratio 5/2 = 2.5
        Num expectedAtExit = numFactory.one().multipliedBy(numFactory.numOf(5).dividedBy(numFactory.numOf(2)));
        assertEquals("Cash flow at exit index must equal entry value * (exit/entry)", expectedAtExit, cf.getValue(4));

        // Values after the exit should stay constant (no further accrual)
        Num afterExit = cf.getValue(5);
        assertEquals("Values after the exit must stay constant", afterExit, cf.getValue(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithOpenPositionThrowsException() {
        // Create a position that has only an entry (no exit) → still opened
        Trade entry = new Trade(TradeType.BUY, 2, numFactory.numOf(3), numFactory.numOf(1));
        Position openPos = new Position(entry, null);
        // The constructor must reject an opened position
        new CashFlow(series, openPos);
    }

    @Test
    public void testCashFlowWithTradingRecordContainingOpenPosition() {
        // Build a trading record with one closed BUY position and one open SELL position
        TradingRecord record = new BaseTradingRecord();

        // Closed BUY: entry 0 price 1, exit 2 price 3
        record.operate(0, numFactory.numOf(1), numFactory.numOf(1)); // BUY
        record.operate(2, numFactory.numOf(3), numFactory.numOf(1)); // SELL (close)

        // Open SELL: entry 3 price 4 (no exit)
        record.operate(3, numFactory.numOf(4), numFactory.numOf(1)); // SELL

        // Build CashFlow up to index 5 (the end of the series)
        CashFlow cf = new CashFlow(series, record, series.getEndIndex());

        // Verify that values after the open position are still calculated (accrued)
        // The open position has a negative ratio (short trade). The ratio after
        // the last bar should be: 2 - (price / entryPrice)
        Num entryPrice = numFactory.numOf(4);
        Num lastBarClose = series.getBar(series.getEndIndex()).getClosePrice(); // price 6
        Num ratio = entryPrice.getNumFactory().numOf(2).minus(lastBarClose.dividedBy(entryPrice));
        Num expected = numFactory.one().multipliedBy(ratio);
        assertEquals("Cash flow after open short position must match short‑trade ratio", expected, cf.getValue(series.getEndIndex()));
    }

    /* ----------------------------------------------------------------------
     *  EDGE CASE & EXCEPTION TESTS (static helpers)
     * ---------------------------------------------------------------------- */

    @Test
    public void testAddCostLongTrade() {
        Num rawPrice = numFactory.numOf(10);
        Num holdingCost = numFactory.numOf(2);
        Num result = CashFlow.addCost(rawPrice, holdingCost, true);
        assertEquals("Long trade should subtract holding cost", numFactory.numOf(8), result);
    }

    @Test
    public void testAddCostShortTrade() {
        Num rawPrice = numFactory.numOf(10);
        Num holdingCost = numFactory.numOf(2);
        Num result = CashFlow.addCost(rawPrice, holdingCost, false);
        assertEquals("Short trade should add holding cost", numFactory.numOf(12), result);
    }

    @Test
    public void testDetermineEndIndexUsesExitIfEarlier() {
        // Position with exit at index 3, finalIndex passed as 5, maxIndex 10
        Trade entry = new Trade(TradeType.BUY, 1, numFactory.numOf(2), numFactory.numOf(1));
        Trade exit = new Trade(TradeType.SELL, 3, numFactory.numOf(4), numFactory.numOf(1));
        Position pos = new Position(entry, exit);
        int result = CashFlow.determineEndIndex(pos, 5, 10);
        assertEquals("Should use the exit index when it is earlier than finalIndex", 3, result);
    }

    @Test
    public void testDetermineEndIndexCappedByMaxIndex() {
        // Position without exit, finalIndex 15, maxIndex 8
        Trade entry = new Trade(TradeType.BUY, 2, numFactory.numOf(3), numFactory.numOf(1));
        Position pos = new Position(entry, null);
        int result = CashFlow.determineEndIndex(pos, 15, 8);
        assertEquals("Result must not exceed maxIndex", 8, result);
    }

    @Test
    public void testFillToTheEndPadsValues() {
        // Build a CashFlow with only the initial value (size = 1)
        CashFlow cf = new CashFlow(series, new Position(TradeType.BUY));
        // Force a small endIndex to trigger padding
        cf.fillToTheEnd(4);
        // All indices 0‑4 must contain the same value (the initial 1)
        Num first = cf.getValue(0);
        for (int i = 0; i <= 4; i++) {
            assertEquals("All padded values must equal the initial value", first, cf.getValue(i));
        }
        // Verify that the list length matches series length (6)
        assertEquals("After padding, internal list size must equal series bar count", series.getBarCount(), cf.values.size());
    }

    /* ----------------------------------------------------------------------
     *  INTERNAL LOGIC VERIFICATION (via reflection for private methods)
     * ---------------------------------------------------------------------- */

    @Test
    public void testGetIntermediateRatioLongTrade() throws Exception {
        // Access the private static method using reflection
        java.lang.reflect.Method m = CashFlow.class.getDeclaredMethod("getIntermediateRatio", boolean.class, Num.class, Num.class);
        m.setAccessible(true);
        Num entry = numFactory.numOf(2);
        Num exit = numFactory.numOf(5);
        Num ratio = (Num) m.invoke(null, true, entry, exit);
        assertEquals(numFactory.numOf(5).dividedBy(entry), ratio);
    }

    @Test
    public void testGetIntermediateRatioShortTrade() throws Exception {
        java.lang.reflect.Method m = CashFlow.class.getDeclaredMethod("getIntermediateRatio", boolean.class, Num.class, Num.class);
        m.setAccessible(true);
        Num entry = numFactory.numOf(4);
        Num exit = numFactory.numOf(1);
        Num expected = entry.getNumFactory().numOf(2).minus(exit.dividedBy(entry));
        Num ratio = (Num) m.invoke(null, false, entry, exit);
        assertEquals(expected, ratio);
    }

    /* ----------------------------------------------------------------------
     *  EDGE CASE: Empty BarSeries with empty TradingRecord
     * ---------------------------------------------------------------------- */

    @Test
    public void testCashFlowWithEmptySeriesAndEmptyRecord() {
        BarSeries emptySeries = new BaseBarSeries.Builder()
                .withName("empty")
                .withNumFactory(numFactory)
                .build();

        // Empty TradingRecord implementation – the library provides a default one
        TradingRecord emptyRecord = new BaseTradingRecord();

        CashFlow cf = new CashFlow(emptySeries, emptyRecord);
        assertEquals("Size of empty series must be 0", 0, cf.getSize());
        // The internal values list contains only the initial "1" value
        assertEquals("Value list must contain exactly one element for empty series", 1, cf.values.size());
        assertEquals(numFactory.one(), cf.getValue(0));
    }
}
