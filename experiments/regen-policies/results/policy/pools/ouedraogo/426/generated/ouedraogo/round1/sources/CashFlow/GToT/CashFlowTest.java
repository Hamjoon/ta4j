/**
 * JUnit‑4 test suite for {@link org.ta4j.core.analysis.CashFlow}.
 *
 * The tests cover:
 * 1. All public constructors (basic happy‑path and NPE edge cases).
 * 2. Public methods:
 *    - {@code calculatePosition}
 *    - {@code getValue}
 *    - {@code getCountOfUnstableBars}
 *    - {@code getBarSeries}
 *    - {@code getSize}
 *    - {@code getEquityCurveMode}
 * 3. Specific edge‑case scenarios for {@code calculatePosition}:
 *    • Position with no entry.
 *    • Entry index after the supplied {@code finalIndex}.
 *    • Entry equity equals zero (should be ignored).
 *    • MARK_TO_MARKET mode (long trade) – basic cash‑flow multiplication.
 *    • REALIZED mode – short trade handling.
 *    • Open position handling set to {@link OpenPositionHandling#IGNORE}.
 *
 * The test suite uses only the public API supplied in the prompt, builds real
 * {@link BarSeries}, {@link Trade}, {@link Position} and {@link CashFlow}
 * objects and never mocks or subclasses library classes.
 */
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Test class for {@link CashFlow}.
 */
public class CashFlowTest {

    /** Helper: creates a simple BarSeries with two bars (price 100, then 110). */
    private BarSeries createTwoBarSeries() {
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("testSeries")
                .build();

        // add two price points – the default BarBuilder creates a bar where open,
        // high, low and close are the supplied price.
        series.addPrice(100d); // index 0
        series.addPrice(110d); // index 1
        return series;
    }

    /** Helper: builds a long position (buy @0, sell @1). */
    private Position createLongPosition(BarSeries series) {
        Trade entry = Trade.buyAt(0, series);
        Trade exit = Trade.sellAt(1, series);
        return new Position(entry, exit);
    }

    /** Helper: builds a short position (sell @0, buy @1). */
    private Position createShortPosition(BarSeries series) {
        Trade entry = Trade.sellAt(0, series);
        Trade exit = Trade.buyAt(1, series);
        return new Position(entry, exit);
    }

    /* ---------------------------------------------------------------------- */
    /* 1. Constructors – happy path                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testConstructorWithBarSeriesAndTradingRecord() {
        BarSeries series = createTwoBarSeries();
        BaseTradingRecord record = new BaseTradingRecord(); // empty record
        CashFlow cf = new CashFlow(series, record);
        assertNotNull(cf);
        assertEquals(series.getBarCount(), cf.getSize());
        assertEquals(EquityCurveMode.MARK_TO_MARKET, cf.getEquityCurveMode());
    }

    @Test
    public void testConstructorWithAllParameters() {
        BarSeries series = createTwoBarSeries();
        BaseTradingRecord record = new BaseTradingRecord();
        CashFlow cf = new CashFlow(series, record, 1, EquityCurveMode.REALIZED,
                OpenPositionHandling.IGNORE);
        assertNotNull(cf);
        assertEquals(EquityCurveMode.REALIZED, cf.getEquityCurveMode());
    }

    /* ---------------------------------------------------------------------- */
    /* 2. Constructors – NPE edge cases                                        */
    /* ---------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testConstructorNullBarSeries() {
        new CashFlow(null, new BaseTradingRecord());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTradingRecord() {
        new CashFlow(createTwoBarSeries(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullEquityCurveMode() {
        new CashFlow(createTwoBarSeries(), new BaseTradingRecord(),
                0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOpenPositionHandling() {
        new CashFlow(createTwoBarSeries(), new BaseTradingRecord(),
                0, EquityCurveMode.MARK_TO_MARKET, null);
    }

    /* ---------------------------------------------------------------------- */
    /* 3. getValue – basic behaviour                                          */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetValueInitial() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());
        // values are initialised with Num.one()
        Num one = series.numFactory().one();
        assertEquals(one, cf.getValue(0));
        assertEquals(one, cf.getValue(1));
    }

    /* ---------------------------------------------------------------------- */
    /* 4. getCountOfUnstableBars – always zero                               */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetCountOfUnstableBars() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());
        assertEquals(0, cf.getCountOfUnstableBars());
    }

    /* ---------------------------------------------------------------------- */
    /* 5. getBarSeries – snapshot (different instance)                        */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetBarSeriesSnapshot() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());
        BarSeries snapshot = cf.getBarSeries();
        assertNotSame(series, snapshot);
        assertEquals(series.getName(), snapshot.getName());
        assertEquals(series.getBarCount(), snapshot.getBarCount());
    }

    /* ---------------------------------------------------------------------- */
    /* 6. getSize – delegates to underlying series                           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetSize() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());
        assertEquals(series.getBarCount(), cf.getSize());
    }

    /* ---------------------------------------------------------------------- */
    /* 7. getEquityCurveMode – returns the mode supplied at construction    */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetEquityCurveMode() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord(),
                0, EquityCurveMode.REALIZED);
        assertEquals(EquityCurveMode.REALIZED, cf.getEquityCurveMode());
    }

    /* ---------------------------------------------------------------------- */
    /* 8. calculatePosition – various edge cases                           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCalculatePositionNullEntry() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());

        // Position with no entry (empty constructor)
        Position emptyPos = new Position();
        cf.calculatePosition(emptyPos, series.getEndIndex());

        // Values must stay unchanged (still one)
        Num one = series.numFactory().one();
        assertEquals(one, cf.getValue(0));
        assertEquals(one, cf.getValue(1));
    }

    @Test
    public void testCalculatePositionEntryAfterFinalIndex() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());

        // Entry at index 2 (which does not exist); we simulate by creating a
        // position with entry index 2 manually.
        Trade entry = Trade.buyAt(2, series); // index 2 is beyond series end
        Trade exit = Trade.sellAt(2, series);
        Position pos = new Position(entry, exit);

        cf.calculatePosition(pos, 1); // finalIndex = 1 < entry index
        Num one = series.numFactory().one();
        assertEquals(one, cf.getValue(0));
        assertEquals(one, cf.getValue(1));
    }

    @Test
    public void testCalculatePositionEntryEquityZeroIsIgnored() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());

        // Force entry equity to zero by manually setting the stored value.
        // The internal list is private, but we can achieve the same effect
        // by using a startIndex that excludes the entry index.
        // Here we construct CashFlow with startIndex = 1 so that index 0 (entry)
        // is outside the materialised window.
        CashFlow cfWindowed = new CashFlow(series, new BaseTradingRecord(),
                1, series.getEndIndex(), EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        Position longPos = createLongPosition(series);
        cfWindowed.calculatePosition(longPos, series.getEndIndex());

        // Since entry equity is outside the window, the values remain 1.
        Num one = series.numFactory().one();
        assertEquals(one, cfWindowed.getValue(1));
    }

    @Test
    public void testCalculatePositionMarkToMarketLongTrade() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);

        Position longPos = createLongPosition(series);
        cf.calculatePosition(longPos, series.getEndIndex());

        Num one = series.numFactory().one();
        Num expectedRatio = series.numFactory().numOf(110d).dividedBy(series.numFactory().numOf(100d)); // 1.1

        // index 0 (entry) stays 1, index 1 multiplied by 1.1
        assertEquals(one, cf.getValue(0));
        assertEquals(one.multipliedBy(expectedRatio), cf.getValue(1));
    }

    @Test
    public void testCalculatePositionRealizedShortTrade() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord(),
                EquityCurveMode.REALIZED);

        Position shortPos = createShortPosition(series);
        cf.calculatePosition(shortPos, series.getEndIndex());

        Num one = series.numFactory().one();
        // For short trade, ratio = 2 - (exit/entry)
        Num entryPrice = series.numFactory().numOf(100d);
        Num exitPrice = series.numFactory().numOf(110d);
        Num ratio = entryPrice.getNumFactory().numOf(2).minus(exitPrice.dividedBy(entryPrice));

        // Expected: index 0 unchanged, index 1 multiplied by ratio
        assertEquals(one, cf.getValue(0));
        assertEquals(one.multipliedBy(ratio), cf.getValue(1));
    }

    @Test
    public void testCalculatePositionOpenPositionIgnored() {
        BarSeries series = createTwoBarSeries();
        // Build a trading record with an open position (no exit)
        Trade entry = Trade.buyAt(0, series);
        Position openPos = new Position(entry);
        BaseTradingRecord record = new BaseTradingRecord(openPos);

        // CashFlow with OpenPositionHandling.IGNORE
        CashFlow cf = new CashFlow(series, record,
                EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.IGNORE);

        // The open position should be ignored – values remain 1
        Num one = series.numFactory().one();
        assertEquals(one, cf.getValue(0));
        assertEquals(one, cf.getValue(1));
    }

    /* ---------------------------------------------------------------------- */
    /* 9. Internal helpers – ensure private utility methods behave as       */
    /*    expected indirectly via public API (e.g., out‑of‑range access).   */
    /* ---------------------------------------------------------------------- */

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfRangeThrows() {
        BarSeries series = createTwoBarSeries();
        CashFlow cf = new CashFlow(series, new BaseTradingRecord());
        cf.getValue(5); // series has only 2 bars -> should throw
    }
}
