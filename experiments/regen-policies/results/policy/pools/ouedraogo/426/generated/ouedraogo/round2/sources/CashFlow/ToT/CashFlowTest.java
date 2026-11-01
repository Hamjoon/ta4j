/**
 * JUnit‑4 test suite for {@link org.ta4j.core.analysis.CashFlow}.
 *
 * The tests cover:
 * <ul>
 *   <li>Construction of a {@code CashFlow} with different constructor overloads.</li>
 *   <li>Correct initialisation of the internal {@code values} list.</li>
 *   <li>The public API:
 *     <ul>
 *       <li>{@link CashFlow#getValue(int)}</li>
 *       <li>{@link CashFlow#getBarSeries()}</li>
 *       <li>{@link CashFlow#getSize()}</li>
 *       <li>{@link CashFlow#getEquityCurveMode()}</li>
 *     </ul>
 *   </li>
 *   <li>Behaviour of {@link CashFlow#calculatePosition(Position,int)} for
 *       both {@code EquityCurveMode.MARK_TO_MARKET} and {@code EquityCurveMode.REALIZED}
 *       with long and short positions.</li>
 *   <li>Open‑position handling (MARK_TO_MARKET vs IGNORE).</li>
 *   <li>Immutability of the snapshot series returned by {@code getBarSeries()}.</li>
 * </ul>
 *
 * All objects are created through the public constructors / factory methods that
 * appear in the supplied source code – no mocking or subclassing of library
 * types is used.
 */
package org.ta4j.core.analysis;
import org.ta4j.core.BarSeries;


import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Test class for {@link CashFlow}.
 */
public class CashFlowTest {

    private BarSeries series;
    private NumFactory numFactory;

    /**
     * Helper that builds a {@link BarSeries} with the supplied close prices.
     *
     * @param closePrices close price for each bar (ordered by index)
     * @return a {@code BarSeries} containing the bars
     */
    private BarSeries buildSeries(double... closePrices) {
        List<Bar> bars = new ArrayList<>();
        ZonedDateTime time = ZonedDateTime.now();
        for (double price : closePrices) {
            // Using the default {@code BaseBar} constructor (available in ta4j)
            Num nPrice = numFactory.numOf(price);
            Bar bar = new BaseBar(Duration.ofMinutes(1), time, nPrice, nPrice, nPrice, nPrice, numFactory.one());
            bars.add(bar);
            time = time.plusMinutes(1);
        }
        return new BaseBarSeriesBuilder()
                .withName("test-series")
                .withNumFactory(numFactory)
                .withBars(bars)
                .build();
    }

    @Before
    public void setUp() {
        // The default implementation of NumFactory is obtained from a dummy series.
        // We create a minimal series with a single bar just to fetch its factory.
        Bar dummyBar = new BaseBar(Duration.ofMinutes(1), ZonedDateTime.now(),
                org.ta4j.core.num.DecimalNum.valueOf(1),
                org.ta4j.core.num.DecimalNum.valueOf(1),
                org.ta4j.core.num.DecimalNum.valueOf(1),
                org.ta4j.core.num.DecimalNum.valueOf(1),
                org.ta4j.core.num.DecimalNum.valueOf(1));
        BarSeries dummySeries = new BaseBarSeriesBuilder()
                .withName("dummy")
                .withBars(Collections.singletonList(dummyBar))
                .build();
        this.numFactory = dummySeries.numFactory();

        // Build a series with 5 bars (close prices: 10, 11, 12, 13, 14)
        this.series = buildSeries(10, 11, 12, 13, 14);
    }

    /* ---------------------------------------------------------------------- */
    /*  Constructor / basic getters                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testConstructorCreatesCorrectWindowAndInitialValues() {
        // Use the constructor that materialises a window [1,3] (inclusive)
        TradingRecord record = new BaseTradingRecord(); // empty record
        CashFlow cf = new CashFlow(series, record, 1, 3, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // The internal list should contain one Num per bar in the window (3‑1+1 = 3)
        assertEquals(3, cf.values.size());

        // Every value must be initialised to NumFactory.one()
        for (int i = 1; i <= 3; i++) {
            assertEquals(numFactory.one(), cf.getValue(i));
        }

        // Out‑of‑window indices must not be accessible (should throw IndexOutOfBounds)
        try {
            cf.getValue(0);
            fail("Expected IndexOutOfBoundsException for index outside window");
        } catch (IndexOutOfBoundsException expected) {
            // ok
        }
    }

    @Test
    public void testGetBarSeriesReturnsSnapshotAndIsImmutable() {
        TradingRecord record = new BaseTradingRecord(); // empty
        CashFlow cf = new CashFlow(series, record);

        BarSeries snapshot = cf.getBarSeries();
        assertNotSame(series, snapshot);
        assertEquals(series.getBarCount(), snapshot.getBarCount());

        // Modify the original series (add a new bar) and verify snapshot stays unchanged
        Bar extraBar = new BaseBar(Duration.ofMinutes(1), ZonedDateTime.now(),
                numFactory.numOf(100), numFactory.numOf(100), numFactory.numOf(100),
                numFactory.numOf(100), numFactory.one());
        series.addBar(extraBar);
        assertEquals(5, snapshot.getBarCount()); // original count
        assertEquals(6, series.getBarCount());   // after addition
    }

    @Test
    public void testGetSizeAndEquityCurveMode() {
        TradingRecord record = new BaseTradingRecord();
        CashFlow cf = new CashFlow(series, record, EquityCurveMode.REALIZED);
        assertEquals(5, cf.getSize());
        assertEquals(EquityCurveMode.REALIZED, cf.getEquityCurveMode());
    }

    /* ---------------------------------------------------------------------- */
    /*  calculatePosition – MARK_TO_MARKET (long)                              */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCalculatePositionLongMarkToMarket() {
        // Position: buy at index 1 (price 11) , sell at index 3 (price 13)
        Trade entry = Trade.buyAt(1, series);
        Trade exit = Trade.sellAt(3, series);
        Position position = new Position(entry, exit);
        TradingRecord record = new BaseTradingRecord(position);

        CashFlow cf = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // Expected ratio = exitPrice / entryPrice = 13 / 11 = 1.181818...
        Num expectedRatio = numFactory.numOf(13).dividedBy(numFactory.numOf(11));

        // Values before entry (index 0) stay at 1
        assertEquals(numFactory.one(), cf.getValue(0));

        // From entry index onward the cash flow should be multiplied by the ratio
        for (int i = 1; i <= series.getEndIndex(); i++) {
            Num actual = cf.getValue(i);
            if (i >= 1) {
                // after entry, the accumulated ratio is applied
                assertEquals(expectedRatio, actual);
            } else {
                assertEquals(numFactory.one(), actual);
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  calculatePosition – REALIZED (short)                                   */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCalculatePositionShortRealized() {
        // Short position: sell at index 0 (price 10), buy (cover) at index 2 (price 12)
        Trade entry = Trade.sellAt(0, series);
        Trade exit = Trade.buyAt(2, series);
        Position position = new Position(entry, exit);
        TradingRecord record = new BaseTradingRecord(position);

        CashFlow cf = new CashFlow(series, record, EquityCurveMode.REALIZED,
                OpenPositionHandling.MARK_TO_MARKET);

        // For short realised mode the ratio is: 2 - (exit/entry)
        // exit = 12, entry = 10 => 2 - (12/10) = 2 - 1.2 = 0.8
        Num expectedRatio = numFactory.two().minus(numFactory.numOf(12).dividedBy(numFactory.numOf(10)));

        // Index 0 is entry, so the cash flow after the exit (index 2) should be multiplied
        // by the ratio; before that it stays at 1.
        assertEquals(numFactory.one(), cf.getValue(0)); // entry point (no multiplication yet)
        assertEquals(numFactory.one(), cf.getValue(1)); // still 1
        assertEquals(expectedRatio, cf.getValue(2));    // after exit
        // Subsequent bars keep the same accumulated value
        for (int i = 3; i <= series.getEndIndex(); i++) {
            assertEquals(expectedRatio, cf.getValue(i));
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Open‑position handling – IGNORE                                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOpenPositionIgnoredDoesNotAffectCashFlow() {
        // Open long position: entry at index 1, never exited.
        Trade entry = Trade.buyAt(1, series);
        Position openPosition = new Position(entry);
        TradingRecord record = new BaseTradingRecord(openPosition);

        // Use IGNORE – open positions must be ignored completely
        CashFlow cf = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.IGNORE);

        // All values must stay at the initial 1 because the open position is ignored
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertEquals(numFactory.one(), cf.getValue(i));
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Edge cases – entry after finalIndex, null entry, empty record           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testCalculatePositionEntryAfterFinalIndexIsIgnored() {
        // Entry at index 4, finalIndex set to 2 => should be ignored
        Trade entry = Trade.buyAt(4, series);
        Position position = new Position(entry);
        TradingRecord record = new BaseTradingRecord(position);

        CashFlow cf = new CashFlow(series, record, 2, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // All values must remain 1 because the position is beyond finalIndex
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertEquals(numFactory.one(), cf.getValue(i));
        }
    }

    @Test
    public void testCalculatePositionWithNullEntryDoesNothing() {
        // Position with no entry (both entry and exit are null)
        Position emptyPosition = new Position();
        TradingRecord record = new BaseTradingRecord(emptyPosition);

        CashFlow cf = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // No change expected
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertEquals(numFactory.one(), cf.getValue(i));
        }
    }

    @Test
    public void testCashFlowWithEmptyTradingRecord() {
        TradingRecord emptyRecord = new BaseTradingRecord(); // no positions at all
        CashFlow cf = new CashFlow(series, emptyRecord);

        // All values are the default initialisation (1)
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertEquals(numFactory.one(), cf.getValue(i));
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  Internal multiplyRange edge – empty values list (window out of range)  */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testMultiplyRangeWithWindowOutsideSeriesDoesNotThrow() {
        // Use a window that lies completely after the series end
        TradingRecord record = new BaseTradingRecord();
        // startIndex = 10, endIndex = 12 (series only has 0‑4)
        CashFlow cf = new CashFlow(series, record, 10, 12, 12,
                EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.MARK_TO_MARKET);

        // The internal list is empty – calling multiplyRange must be a no‑op, not an exception
        // (the constructor already called calculate, we just ensure no exception is thrown)
        // No assertions needed – test passes if no exception occurs.
    }
}
