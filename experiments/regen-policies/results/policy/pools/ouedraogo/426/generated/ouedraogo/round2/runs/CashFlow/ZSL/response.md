###Test START##
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Comprehensive JUnit‑4 test suite for {@link CashFlow}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>All public constructors (different signatures).</li>
 *   <li>Behaviour of {@code getValue}, {@code getSize},
 *       {@code getEquityCurveMode} and {@code getBarSeries}.</li>
 *   <li>Correct cash‑flow calculation for both {@code MARK_TO_MARKET}
 *       and {@code REALIZED} modes, with open‑position handling
 *       {@code MARK_TO_MARKET} and {@code IGNORE}.</li>
 *   <li>Windowed construction (start / end index bounds).</li>
 * </ul>
 *
 * <p>Only the public API listed in the prompt is used – no mocking,
 * no subclassing and no external library calls.
 */
public class CashFlowTest {

    private BarSeries series;
    private NumFactory nf;

    @Before
    public void setUp() {
        // Build a simple series with five bars (close prices 100,101,102,103,104)
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .build();
        nf = series.numFactory();

        series.addPrice(nf.numOf(100)); // index 0
        series.addPrice(nf.numOf(101)); // index 1
        series.addPrice(nf.numOf(102)); // index 2
        series.addPrice(nf.numOf(103)); // index 3
        series.addPrice(nf.numOf(104)); // index 4
    }

    /**
     * Helper to compare Num values with a tolerance.
     */
    private void assertNumEquals(double expected, Num actual) {
        assertEquals(expected, actual.doubleValue(), 1e-8);
    }

    /**
     * Creates a simple long position: entry at index 0 (price 100), exit at index 2 (price 102).
     */
    private Position createClosedLongPosition() {
        Trade entry = Trade.buyAt(0, series);
        Trade exit = Trade.sellAt(2, series);
        return new Position(entry, exit);
    }

    /**
     * Creates a simple short position: entry (sell) at index 1 (price 101), exit (buy) at index 3 (price 103).
     */
    private Position createClosedShortPosition() {
        Trade entry = Trade.sellAt(1, series);
        Trade exit = Trade.buyAt(3, series);
        return new Position(entry, exit);
    }

    /**
     * Builds a TradingRecord containing a single closed long position.
     */
    private BaseTradingRecord buildLongTradingRecord() {
        BaseTradingRecord record = new BaseTradingRecord();
        // long: buy at 0, sell at 2
        record.enter(0, nf.numOf(100), nf.one());
        record.exit(2, nf.numOf(102), nf.one());
        return record;
    }

    /**
     * Builds a TradingRecord containing a single closed short position.
     */
    private BaseTradingRecord buildShortTradingRecord() {
        BaseTradingRecord record = new BaseTradingRecord();
        // short: sell at 1, buy at 3
        record.enter(1, nf.numOf(101), nf.one()); // sell (entry)
        record.exit(3, nf.numOf(103), nf.one()); // buy (exit)
        return record;
    }

    @Test
    public void testConstructorWithSeriesAndPositionDefaultMode() {
        Position pos = createClosedLongPosition();
        CashFlow cf = new CashFlow(series, pos);
        assertEquals(EquityCurveMode.MARK_TO_MARKET, cf.getEquityCurveMode());
        assertEquals(series.getBarCount(), cf.getSize());

        // Expected cash‑flow:
        // index 0 -> entry, value stays 1 (no multiplication yet)
        // index 1 -> price 101 / 100 = 1.01
        // index 2 -> price 102 / 100 = 1.02
        // index 3,4 -> same as exit ratio 1.02 (multiplied for remaining bars)
        assertNumEquals(1.0, cf.getValue(0));
        assertNumEquals(1.01, cf.getValue(1));
        assertNumEquals(1.02, cf.getValue(2));
        assertNumEquals(1.02, cf.getValue(3));
        assertNumEquals(1.02, cf.getValue(4));
    }

    @Test
    public void testConstructorWithSeriesAndPositionRealizedMode() {
        Position pos = createClosedLongPosition();
        CashFlow cf = new CashFlow(series, pos, EquityCurveMode.REALIZED);
        assertEquals(EquityCurveMode.REALIZED, cf.getEquityCurveMode());

        // With REALIZED mode and no holding cost, the ratio equals exit/entry.
        // The multiplication starts at the exit index (2) and propagates forward.
        assertNumEquals(1.0, cf.getValue(0));
        assertNumEquals(1.0, cf.getValue(1));
        assertNumEquals(1.02, cf.getValue(2));
        assertNumEquals(1.02, cf.getValue(3));
        assertNumEquals(1.02, cf.getValue(4));
    }

    @Test
    public void testLongPositionMarkToMarketWithIgnoreOpenPositions() {
        BaseTradingRecord record = buildLongTradingRecord();
        CashFlow cf = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.IGNORE);
        // Same expected values as the simple constructor test.
        assertNumEquals(1.0, cf.getValue(0));
        assertNumEquals(1.01, cf.getValue(1));
        assertNumEquals(1.02, cf.getValue(2));
        assertNumEquals(1.02, cf.getValue(3));
        assertNumEquals(1.02, cf.getValue(4));
    }

    @Test
    public void testShortPositionMarkToMarket() {
        Position shortPos = createClosedShortPosition();
        CashFlow cf = new CashFlow(series, shortPos, EquityCurveMode.MARK_TO_MARKET);
        // For a short trade, the ratio is 2 - (exit/entry)
        // entry price = 101, exit price = 103
        // ratio = 2 - (103/101) = 2 - 1.01980198 = 0.98019802
        double expectedRatio = 2.0 - (103.0 / 101.0);
        // Values before entry (index 0) stay 1
        assertNumEquals(1.0, cf.getValue(0));
        // At entry index 1, the seed ratio is applied (windowStartSeeded logic)
        // Since entry index == windowStartIndex, multiplication starts from index 2
        assertNumEquals(1.0, cf.getValue(1));
        // Index 2 (first bar after entry) gets multiplied
        assertNumEquals(expectedRatio, cf.getValue(2));
        // All subsequent bars keep the same ratio
        assertNumEquals(expectedRatio, cf.getValue(3));
        assertNumEquals(expectedRatio, cf.getValue(4));
    }

    @Test
    public void testWindowedConstruction() {
        // Use startIndex = 2, endIndex = 4 => only indices 2,3,4 are materialised.
        BaseTradingRecord record = buildLongTradingRecord();
        CashFlow cf = new CashFlow(series, record, 2, 4, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);
        // Size of internal values list should be 3 (indices 2..4)
        // getValue(2) corresponds to first stored element.
        assertNumEquals(1.02, cf.getValue(2)); // exit ratio applied at index 2
        assertNumEquals(1.02, cf.getValue(3));
        assertNumEquals(1.02, cf.getValue(4));
        // Accessing an index outside the window should still map to the snapshot series
        // but the underlying list does not contain those indices, so getValue will throw
        // IndexOutOfBoundsException – we verify that behaviour.
        try {
            cf.getValue(1);
            fail("Expected IndexOutOfBoundsException for index outside the window");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetBarSeriesReturnsSnapshot() {
        BaseTradingRecord record = buildLongTradingRecord();
        CashFlow cf = new CashFlow(series, record);
        BarSeries snapshot = cf.getBarSeries();
        // The snapshot must be a different instance.
        assertNotSame(series, snapshot);
        // But it must contain the same data (same bar count and close prices).
        assertEquals(series.getBarCount(), snapshot.getBarCount());
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(series.getBar(i).getClosePrice().doubleValue(),
                    snapshot.getBar(i).getClosePrice());
        }
    }

    @Test
    public void testGetSizeReflectsSeriesBarCount() {
        BaseTradingRecord record = buildLongTradingRecord();
        CashFlow cf = new CashFlow(series, record);
        assertEquals(series.getBarCount(), cf.getSize());
    }

    @Test
    public void testCalculatePositionWithFutureEntryIsIgnored() {
        // Create a position whose entry index is beyond finalIndex -> should be ignored.
        Position pos = new Position(Trade.buyAt(10, series)); // index 10 does not exist
        CashFlow cf = new CashFlow(series, pos, EquityCurveMode.MARK_TO_MARKET);
        // All values should stay at the initial 1.0
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(1.0, cf.getValue(i));
        }
    }

    @Test
    public void testCalculatePositionWithNoEntryIsIgnored() {
        // Position without entry (null) should be ignored.
        Position emptyPos = new Position(); // default ctor creates empty position
        CashFlow cf = new CashFlow(series, emptyPos, EquityCurveMode.MARK_TO_MARKET);
        for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
            assertNumEquals(1.0, cf.getValue(i));
        }
    }

    @Test
    public void testGetCountOfUnstableBarsIsZero() {
        BaseTradingRecord record = buildLongTradingRecord();
        CashFlow cf = new CashFlow(series, record);
        assertEquals(0, cf.getCountOfUnstableBars());
    }
}
###Test END##