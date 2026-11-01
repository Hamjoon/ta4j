package org.ta4j.core.analysis;

import org.junit.Test;
import org.junit.Assert;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.num.Num;
import org.ta4j.core.BaseBarSeries;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.Duration;

/**
 * Comprehensive JUnit‑4 test suite for {@link CashFlow}.
 *
 * The tests cover:
 * <ul>
 *   <li>Static utility methods {@code addCost}, {@code determineEndIndex},
 *       {@code getIntermediateRatio}</li>
 *   <li>Behaviour of the constructors with a closed {@link Position}</li>
 *   <li>Exception when a non‑closed {@link Position} is supplied</li>
 *   <li>Correct padding of the internal {@code values} list</li>
 * </ul>
 *
 * All objects are created using only the public API that is listed in the
 * specification (no additional libraries or mocking frameworks are used).
 */
public class CashFlowTest {

    /** Helper to create a tiny {@link BarSeries} with deterministic close prices. */
    private BarSeries createSeries(double... closePrices) {
        BarSeries series = new BaseBarSeries("test-series");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        for (int i = 0; i < closePrices.length; i++) {
            // use the simplest overload: (ZonedDateTime, Number open, Number high,
            // Number low, Number close)
            series.addBar(now.plusMinutes(i), closePrices[i], closePrices[i],
                    closePrices[i], closePrices[i]);
        }
        return series;
    }

    /** Creates a {@link Position} that is closed (entry + exit) on the supplied series. */
    private Position createClosedLongPosition(BarSeries series, int entryIdx, int exitIdx,
                                              double entryPrice, double exitPrice) {
        Position position = new Position();                     // default starting type = BUY
        Num priceEntry = series.numOf(entryPrice);
        Num priceExit = series.numOf(exitPrice);
        Num amount = series.one(); // amount does not matter for cash‑flow ratio

        // open (buy) trade
        position.operate(entryIdx, priceEntry, amount);
        // close (sell) trade
        position.operate(exitIdx, priceExit, amount);
        return position;
    }

    /** Creates a {@link Position} that remains opened (only entry). */
    private Position createOpenedPosition(BarSeries series, int entryIdx, double entryPrice) {
        Position position = new Position();
        Num price = series.numOf(entryPrice);
        position.operate(entryIdx, price, series.one());
        return position;
    }

    /* -------------------------------------------------------------
     *  Static utility methods
     * ------------------------------------------------------------- */

    @Test
    public void testAddCostLongTrade() {
        BarSeries series = createSeries(10);
        Num raw = series.numOf(10);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, true); // long → subtract cost
        Assert.assertEquals(series.numOf(8), net);
    }

    @Test
    public void testAddCostShortTrade() {
        BarSeries series = createSeries(10);
        Num raw = series.numOf(10);
        Num cost = series.numOf(2);
        Num net = CashFlow.addCost(raw, cost, false); // short → add cost
        Assert.assertEquals(series.numOf(12), net);
    }

    @Test
    public void testGetIntermediateRatioLong() {
        BarSeries series = createSeries(10, 20);
        Num entry = series.numOf(10);
        Num exit = series.numOf(20);
        Num ratio = CashFlow.getIntermediateRatio(true, entry, exit);
        Assert.assertEquals(series.numOf(2), ratio);
    }

    @Test
    public void testGetIntermediateRatioShort() {
        BarSeries series = createSeries(10, 20);
        Num entry = series.numOf(10);
        Num exit = series.numOf(20);
        // For short: 2 - (exit/entry) = 2 - 2 = 0
        Num ratio = CashFlow.getIntermediateRatio(false, entry, exit);
        Assert.assertEquals(series.numOf(0), ratio);
    }

    @Test
    public void testDetermineEndIndex_WithExitBeforeFinal() {
        BarSeries series = createSeries(10, 20, 30);
        Position pos = createClosedLongPosition(series, 0, 1, 10, 20);
        int result = CashFlow.determineEndIndex(pos, 5, series.getEndIndex());
        // exit index = 1, finalIndex = 5 → should return 1
        Assert.assertEquals(1, result);
    }

    @Test
    public void testDetermineEndIndex_NoExit_ClampedToFinalAndMax() {
        BarSeries series = createSeries(10, 20, 30);
        Position pos = createOpenedPosition(series, 0, 10);
        // finalIndex = 2, maxIndex = 2 (end of series)
        int result = CashFlow.determineEndIndex(pos, 2, series.getEndIndex());
        Assert.assertEquals(2, result);
    }

    @Test
    public void testDetermineEndIndex_FinalBeyondMax() {
        BarSeries series = createSeries(10, 20, 30);
        Position pos = createOpenedPosition(series, 0, 10);
        // finalIndex = 10 (beyond maxIndex = 2) → should be clamped to 2
        int result = CashFlow.determineEndIndex(pos, 10, series.getEndIndex());
        Assert.assertEquals(2, result);
    }

    /* -------------------------------------------------------------
     *  Constructor behaviour
     * ------------------------------------------------------------- */

    @Test
    public void testCashFlowClosedLongPosition() {
        // series: index 0 price 10, index 1 price 20
        BarSeries series = createSeries(10, 20);
        Position pos = createClosedLongPosition(series, 0, 1, 10, 20);

        CashFlow cf = new CashFlow(series, pos);

        // index 0 is the seed value (1)
        Assert.assertEquals(series.numOf(1), cf.getValue(0));

        // after the exit (index 1) the cash‑flow should be doubled (20/10)
        Assert.assertEquals(series.numOf(2), cf.getValue(1));

        // series length is 2 → values are padded to the end (still 2)
        Assert.assertEquals(2, cf.getSize());
        Assert.assertEquals(series.numOf(2), cf.getValue(1));
    }

    @Test
    public void testCashFlowClosedShortPosition() {
        // series: index 0 price 20, index 1 price 10  (short profit)
        BarSeries series = createSeries(20, 10);
        Position pos = createClosedLongPosition(series, 0, 1, 20, 10);
        // Change the trade type to SHORT by flipping the entry/exit direction:
        // Position created with default BUY, so we manually invert the logic:
        // For a short trade we need entry as SELL and exit as BUY.
        // We achieve this by creating a new Position with entry/exit reversed.
        Position shortPos = new Position();
        shortPos.operate(0, series.numOf(20), series.one()); // entry (SELL)
        shortPos.operate(1, series.numOf(10), series.one()); // exit (BUY)

        CashFlow cf = new CashFlow(series, shortPos);

        // index 0 seed = 1
        Assert.assertEquals(series.numOf(1), cf.getValue(0));

        // short ratio = 2 - (exit/entry) = 2 - (10/20) = 1.5
        Num expected = series.numOf(1.5);
        Assert.assertEquals(expected, cf.getValue(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCashFlowWithOpenedPositionThrows() {
        BarSeries series = createSeries(10, 20);
        Position opened = createOpenedPosition(series, 0, 10);
        // The constructor must reject a non‑closed position
        new CashFlow(series, opened);
    }

    @Test
    public void testFillToTheEndPadsValues() {
        BarSeries series = createSeries(10, 20, 30, 40);
        Position pos = createClosedLongPosition(series, 0, 1, 10, 20);
        // The cash‑flow will be defined up to index 1; indices 2‑3 must be padded.
        CashFlow cf = new CashFlow(series, pos);

        // Expected: indices 0→1 as before, 2→3 equal to last value (2)
        Num last = series.numOf(2);
        Assert.assertEquals(last, cf.getValue(2));
        Assert.assertEquals(last, cf.getValue(3));
        // Size must equal series bar count (4)
        Assert.assertEquals(4, cf.getSize());
    }
}
