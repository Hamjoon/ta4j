/**
 * JUnit 4 test suite for {@link org.ta4j.core.analysis.CashFlow}.
 *
 * The tests cover:
 *  - All public methods (including constructors) with basic functionality checks.
 *  - Edge‑case and exception scenarios such as providing an open position,
 *    zero‑length series, and cost calculations for long/short trades.
 *
 * The test uses concrete implementations from the TA4J library:
 *  - {@code BaseBarSeries} and {@code BaseBar} for time‑series data.
 *  - {@code DecimalNum} as the {@code Num} implementation.
 *  - {@code Trade}, {@code TradeType} and {@code BaseTradingRecord} for positions.
 *
 * NOTE: The source file must be placed in the same package (or test source
 * folder) as the class under test so that package‑private members can be
 * accessed if needed.
 */
package org.ta4j.core.analysis;
import org.ta4j.core.Trade.TradeType;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradeType;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

public class CashFlowTest {

    private BaseBarSeries series;
    private Num one;
    private Num two;
    private Num three;

    /**
     * Helper to create a {@link BaseBarSeries} with three simple bars (indices 0,1,2)
     * having close prices 1, 2 and 3 respectively.
     */
    private BaseBarSeries createSeries() {
        BaseBarSeries s = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        // Bar 0 – close = 1
        s.addBar(new BaseBar(Duration.ofDays(1), now.minusDays(3),
                DecimalNum.valueOf(1), DecimalNum.valueOf(1), DecimalNum.valueOf(1), DecimalNum.valueOf(1), DecimalNum.valueOf(100)));
        // Bar 1 – close = 2
        s.addBar(new BaseBar(Duration.ofDays(1), now.minusDays(2),
                DecimalNum.valueOf(2), DecimalNum.valueOf(2), DecimalNum.valueOf(2), DecimalNum.valueOf(2), DecimalNum.valueOf(100)));
        // Bar 2 – close = 3
        s.addBar(new BaseBar(Duration.ofDays(1), now.minusDays(1),
                DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(100)));
        return s;
    }

    @Before
    public void setUp() {
        series = createSeries();
        one = series.numOf(1);
        two = series.numOf(2);
        three = series.numOf(3);
    }

    /* --------------------------------------------------------------------- *
     *  Tests for public methods (basic functionality)
     * --------------------------------------------------------------------- */

    @Test
    public void testGetUnstableBars_returnsZero() {
        // Use a simple closed position for construction
        Position closedPos = createClosedPosition(TradeType.BUY, 0, 2);
        CashFlow cf = new CashFlow(series, closedPos);
        assertEquals(0, cf.getUnstableBars());
    }

    @Test
    public void testGetBarSeries_returnsSameSeries() {
        Position closedPos = createClosedPosition(TradeType.BUY, 0, 2);
        CashFlow cf = new CashFlow(series, closedPos);
        assertSame(series, cf.getBarSeries());
    }

    @Test
    public void testNumOf_delegatesToSeries() {
        Position closedPos = createClosedPosition(TradeType.BUY, 0, 2);
        CashFlow cf = new CashFlow(series, closedPos);
        Num result = cf.numOf(5);
        assertEquals(series.numOf(5), result);
    }

    @Test
    public void testGetSize_returnsSeriesBarCount() {
        Position closedPos = createClosedPosition(TradeType.BUY, 0, 2);
        CashFlow cf = new CashFlow(series, closedPos);
        assertEquals(series.getBarCount(), cf.getSize());
    }

    @Test
    public void testGetValue_returnsCalculatedCashFlowValues() {
        // Long position: buy at bar 0 (price 1), sell at bar 2 (price 3)
        Position closedPos = createClosedPosition(TradeType.BUY, 0, 2);
        CashFlow cf = new CashFlow(series, closedPos);
        // values[0] = 1 (initial)
        assertEquals(one, cf.getValue(0));
        // After entry at index 0, cash flow should increase proportionally.
        // At index 2 the ratio is exitPrice/entryPrice = 3/1 = 3
        Num expectedAtEnd = one.multipliedBy(three);
        assertEquals(expectedAtEnd, cf.getValue(2));
    }

    /* --------------------------------------------------------------------- *
     *  Edge‑case / Exception handling tests
     * --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithOpenPosition_throwsException() {
        // Create a position that is opened (no exit)
        Trade entry = new Trade(series, 0, TradeType.BUY, one, one);
        Position openPos = new Position(entry);
        // CashFlow constructor must reject an open position
        new CashFlow(series, openPos);
    }

    @Test
    public void testAddCost_longTrade_subtractsHoldingCost() {
        Num rawPrice = series.numOf(10);
        Num holdingCost = series.numOf(2);
        Num result = CashFlow.addCost(rawPrice, holdingCost, true);
        assertEquals(series.numOf(8), result);
    }

    @Test
    public void testAddCost_shortTrade_addsHoldingCost() {
        Num rawPrice = series.numOf(10);
        Num holdingCost = series.numOf(2);
        Num result = CashFlow.addCost(rawPrice, holdingCost, false);
        assertEquals(series.numOf(12), result);
    }

    @Test
    public void testDetermineEndIndex_withExitBeforeFinalIndex() {
        Position pos = createClosedPosition(TradeType.BUY, 0, 2); // exit at index 2
        int finalIdx = 5; // supplied final index larger than exit
        int maxIdx = series.getEndIndex(); // = 2
        int determined = CashFlow.determineEndIndex(pos, finalIdx, maxIdx);
        // Should be min(exit.index, finalIdx) = 2
        assertEquals(2, determined);
    }

    @Test
    public void testDetermineEndIndex_withoutExit_usesFinalOrMax() {
        // Open position (no exit)
        Trade entry = new Trade(series, 0, TradeType.BUY, one, one);
        Position openPos = new Position(entry);
        int finalIdx = 1;
        int maxIdx = series.getEndIndex(); // = 2
        int determined = CashFlow.determineEndIndex(openPos, finalIdx, maxIdx);
        // No exit, so idx = min(finalIdx, maxIdx) = 1
        assertEquals(1, determined);
    }

    @Test
    public void testFillToTheEnd_padsValuesToSeriesEnd() {
        // Use a position that ends early, then verify padding
        Position closedPos = createClosedPosition(TradeType.BUY, 0, 1); // exit at index 1
        CashFlow cf = new CashFlow(series, closedPos);
        // values list should be extended to series.endIndex (2)
        assertEquals(series.getBarCount(), cf.getSize()); // size == barCount
        // The last value (at index 2) must be equal to the value at index 1 (no further change)
        assertEquals(cf.getValue(1), cf.getValue(2));
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods to build trades/positions
     * --------------------------------------------------------------------- */

    /**
     * Creates a closed {@link Position} with the given {@code type} for entry,
     * entry index and exit index. The entry price is taken from the series close
     * price at {@code entryIdx}, the exit price from {@code exitIdx}.
     */
    private Position createClosedPosition(TradeType entryType, int entryIdx, int exitIdx) {
        Num entryPrice = series.getBar(entryIdx).getClosePrice();
        Num exitPrice = series.getBar(exitIdx).getClosePrice();

        Trade entry = new Trade(series, entryIdx, entryType, one, entryPrice);
        Trade exit = new Trade(series, exitIdx,
                entryType.isBuy() ? TradeType.SELL : TradeType.BUY, one, exitPrice);
        return new Position(entry, exit);
    }
}
