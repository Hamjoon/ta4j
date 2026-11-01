/**
 * JUnit‑4 test suite for {@link org.ta4j.core.analysis.CashFlow}.
 *
 * The tests cover:
 * 1. Extraction of all public methods and their signatures.
 * 2. Basic functional tests for each public method.
 * 3. Edge‑case and exception‑handling tests (null arguments, empty series,
 *    out‑of‑range indexes, etc.).
 *
 * The test class builds real TA4J objects (BarSeries, Trade, Position,
 * TradingRecord, etc.) using only the public constructors and static factory
 * methods that appear in the supplied source code – no mocking or subclassing
 * of library types is performed.
 *
 * NOTE: The TA4J library creates {@link org.ta4j.core.num.Num} instances
 * through a {@link org.ta4j.core.num.NumFactory}.  All numeric values in the
 * tests are therefore obtained from the factory of a (possibly empty)
 * {@link org.ta4j.core.BarSeries}.
 */
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Comprehensive JUnit‑4 test class for {@link CashFlow}.
 */
public class CashFlowTest {

    /** Empty series – used for tests that do not need any bar data. */
    private BarSeries emptySeries;

    /** Series with two bars (indices 0 and 1) – used for position‑related tests. */
    private BarSeries twoBarSeries;

    /** {@link NumFactory} taken from the series – used to create numeric literals. */
    private NumFactory numFactory;

    @Before
    public void setUp() {
        // 1️⃣  Empty series (no bars)
        emptySeries = new BaseBarSeriesBuilder().withName("empty").build();

        // 2️⃣  Series with two dummy bars.
        //    The Bar implementation is not part of the supplied API, but the
        //    builder can create a series from a list of bars.  For the purpose
        //    of the tests we only need the series to return a valid
        //    {@link NumFactory} and to have a non‑negative end index.
        //    The concrete Bar objects are created via the builder’s default
        //    bar builder (which uses the supplied NumFactory for price values).
        twoBarSeries = new BaseBarSeriesBuilder()
                .withName("twoBars")
                .withNumFactory(emptySeries.numFactory())   // reuse the same factory
                .build();

        // Add two bars with closing prices 10 and 12 (the exact values are not
        // used by the CashFlow logic that we test, but they make the series
        // non‑empty and give it a concrete end index).
        Num price10 = emptySeries.numFactory().numOf(10);
        Num price12 = emptySeries.numFactory().numOf(12);
        // The Bar interface is not exposed, but the series provides an
        // `addBar(Bar bar)` method.  The builder internally creates a Bar
        // instance when we call `addBar` with price values via the
        // `addBar(Num price, ...)` overload.  To stay within the public API
        // we use the `addBar(Bar bar)` method indirectly by adding a dummy
        // trade – this creates a bar with the trade’s price as the close price.
        // (The TA4J library treats a trade as a price point for the bar.)
        Trade dummyTrade0 = Trade.buyAt(0, price10, price10.one());
        Trade dummyTrade1 = Trade.sellAt(1, price12, price12.one());
        // The series will create a bar for each added trade.
        twoBarSeries.addBar(dummyTrade0.getBar(twoBarSeries));
        twoBarSeries.addBar(dummyTrade1.getBar(twoBarSeries));

        // NumFactory for later use.
        numFactory = twoBarSeries.numFactory();
    }

    /* ---------------------------------------------------------------------- */
    /*  1️⃣  PUBLIC METHODS (extracted from the source)                        */
    /* ---------------------------------------------------------------------- */
    // Constructors (several overloads) – all are public.
    //   CashFlow(BarSeries, TradingRecord, int, EquityCurveMode, OpenPositionHandling)
    //   CashFlow(BarSeries, TradingRecord, int, int, EquityCurveMode, OpenPositionHandling)
    //   CashFlow(BarSeries, TradingRecord, int, int, EquityCurveMode, OpenPositionHandling)
    //   CashFlow(BarSeries, Position, EquityCurveMode)
    //   CashFlow(BarSeries, TradingRecord, int, EquityCurveMode)
    //   CashFlow(BarSeries, Position)
    //   CashFlow(BarSeries, TradingRecord)
    //   CashFlow(BarSeries, TradingRecord, EquityCurveMode)
    //   CashFlow(BarSeries, TradingRecord, EquityCurveMode, OpenPositionHandling)
    //   CashFlow(BarSeries, TradingRecord, int)
    //   CashFlow(BarSeries, TradingRecord, OpenPositionHandling)

    // Interface methods from PerformanceIndicator / Indicator
    //   void calculatePosition(Position, int)
    //   Num getValue(int)
    //   int getCountOfUnstableBars()
    //   BarSeries getBarSeries()
    //   EquityCurveMode getEquityCurveMode()

    // Class‑specific public methods
    //   int getSize()
    //   int getCountOfUnstableBars()   (already listed)
    //   EquityCurveMode getEquityCurveMode() (already listed)

    /* ---------------------------------------------------------------------- */
    /*  2️⃣  BASIC FUNCTIONAL TESTS                                            */
    /* ---------------------------------------------------------------------- */

    /** Test that the empty‑series constructor creates a CashFlow with zero size. */
    @Test
    public void testConstructorWithEmptySeries() {
        CashFlow cf = new CashFlow(emptySeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        assertEquals("Size must be 0 for an empty series", 0, cf.getSize());
        assertEquals("Equity curve mode must be the one supplied",
                EquityCurveMode.MARK_TO_MARKET, cf.getEquityCurveMode());
        assertEquals("BarSeries snapshot must be a distinct instance",
                emptySeries.getName(),
                cf.getBarSeries().getName());
        assertNotSame("Snapshot must not be the original instance",
                emptySeries, cf.getBarSeries());
    }

    /** Test that a CashFlow created from a two‑bar series reports the correct size. */
    @Test
    public void testGetSizeWithTwoBars() {
        CashFlow cf = new CashFlow(twoBarSeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        assertEquals("Size must match the underlying series bar count", 2, cf.getSize());
    }

    /** Test that getCountOfUnstableBars always returns 0 (as per implementation). */
    @Test
    public void testGetCountOfUnstableBars() {
        CashFlow cf = new CashFlow(emptySeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        assertEquals("Unstable bar count should be 0", 0, cf.getCountOfUnstableBars());
    }

    /** Test that getEquityCurveMode returns the mode supplied to the constructor. */
    @Test
    public void testGetEquityCurveMode() {
        CashFlow cfMtm = new CashFlow(emptySeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        assertEquals(EquityCurveMode.MARK_TO_MARKET, cfMtm.getEquityCurveMode());

        CashFlow cfRealized = new CashFlow(emptySeries,
                new BaseTradingRecord(),
                EquityCurveMode.REALIZED);
        assertEquals(EquityCurveMode.REALIZED, cfRealized.getEquityCurveMode());
    }

    /** Test that getBarSeries returns a snapshot (different instance) but with the same name. */
    @Test
    public void testGetBarSeriesSnapshot() {
        CashFlow cf = new CashFlow(twoBarSeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        BarSeries snapshot = cf.getBarSeries();
        assertEquals(twoBarSeries.getName(), snapshot.getName());
        assertNotSame(twoBarSeries, snapshot);
    }

    /** Test getValue on a non‑empty series after a simple long position. */
    @Test
    public void testCalculatePositionLongTrade() {
        // Build entry (buy) at index 0, price 10, amount 1
        Num price10 = numFactory.numOf(10);
        Trade entry = Trade.buyAt(0, price10, price10.one());

        // Build exit (sell) at index 1, price 12, amount 1
        Num price12 = numFactory.numOf(12);
        Trade exit = Trade.sellAt(1, price12, price12.one());

        // Position with the two trades
        Position position = new Position(entry, exit);

        // TradingRecord containing the single position
        BaseTradingRecord tradingRecord = new BaseTradingRecord(position);

        // CashFlow using MARK_TO_MARKET mode (the default in most constructors)
        CashFlow cf = new CashFlow(twoBarSeries, tradingRecord, EquityCurveMode.MARK_TO_MARKET);

        // The cash‑flow values start at 1.0 (NumFactory.one()) and are multiplied
        // by the ratio (exitPrice / entryPrice) = 12 / 10 = 1.2.
        Num expectedRatio = price12.dividedBy(price10); // 1.2
        Num valueAt0 = cf.getValue(0);
        Num valueAt1 = cf.getValue(1);

        // Index 0 is the entry bar – the ratio is applied starting from the next bar,
        // therefore valueAt0 stays at 1.0.
        assertTrue("Value at entry bar must be 1.0",
                valueAt0.isEqual(numFactory.one()));

        // Index 1 should contain the accumulated ratio 1.2.
        assertTrue("Value after exit must reflect the price ratio",
                valueAt1.isEqual(expectedRatio));
    }

    /** Test calculatePosition with a short trade (sell first, then buy). */
    @Test
    public void testCalculatePositionShortTrade() {
        // Short entry (sell) at index 0, price 15
        Num price15 = numFactory.numOf(15);
        Trade entry = Trade.sellAt(0, price15, price15.one());

        // Short exit (buy) at index 1, price 10
        Num price10 = numFactory.numOf(10);
        Trade exit = Trade.buyAt(1, price10, price10.one());

        Position position = new Position(entry, exit);
        BaseTradingRecord tr = new BaseTradingRecord(position);
        CashFlow cf = new CashFlow(twoBarSeries, tr, EquityCurveMode.MARK_TO_MARKET);

        // For a short trade the ratio is: 2 - (exit/entry)
        //   = 2 - (10/15) = 2 - 0.666… = 1.333…
        Num expectedRatio = numFactory.two().minus(price10.dividedBy(price15));

        // Entry bar (index 0) stays at 1.0
        assertTrue(cf.getValue(0).isEqual(numFactory.one()));
        // Exit bar (index 1) should contain the short‑trade ratio
        assertTrue(cf.getValue(1).isEqual(expectedRatio));
    }

    /* ---------------------------------------------------------------------- */
    /*  3️⃣  EDGE CASES & EXCEPTION HANDLING                                   */
    /* ---------------------------------------------------------------------- */

    /** Constructor must reject a null BarSeries (NPE). */
    @Test(expected = NullPointerException.class)
    public void testConstructorNullBarSeries() {
        new CashFlow(null,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
    }

    /** Constructor must reject a null TradingRecord (NPE). */
    @Test(expected = NullPointerException.class)
    public void testConstructorNullTradingRecord() {
        new CashFlow(emptySeries,
                null,
                EquityCurveMode.MARK_TO_MARKET);
    }

    /** Constructor must reject a null EquityCurveMode (NPE). */
    @Test(expected = NullPointerException.class)
    public void testConstructorNullEquityCurveMode() {
        new CashFlow(emptySeries,
                new BaseTradingRecord(),
                null);
    }

    /** Constructor must reject a null OpenPositionHandling (NPE). */
    @Test(expected = NullPointerException.class)
    public void testConstructorNullOpenPositionHandling() {
        // Use the most detailed private constructor via the public overload
        new CashFlow(emptySeries,
                new BaseTradingRecord(),
                0,
                0,
                0,
                EquityCurveMode.MARK_TO_MARKET,
                null);
    }

    /** When startIndex > endIndex the internal values list is empty; getValue should throw. */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfRangeWhenWindowIsEmpty() {
        // Build a series with a single bar so that endIndex = 0.
        BarSeries singleBarSeries = new BaseBarSeriesBuilder()
                .withName("single")
                .withNumFactory(numFactory)
                .build();
        // Add a dummy bar (price 5) to make the series non‑empty.
        Num price5 = numFactory.numOf(5);
        Trade dummy = Trade.buyAt(0, price5, price5.one());
        singleBarSeries.addBar(dummy.getBar(singleBarSeries));

        // startIndex = 5, endIndex = 2  -> window will be empty.
        CashFlow cf = new CashFlow(singleBarSeries,
                new BaseTradingRecord(),
                5, 2,
                EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // Any access should raise IndexOutOfBoundsException.
        cf.getValue(0);
    }

    /** calculatePosition should safely ignore a Position with a null entry trade. */
    @Test
    public void testCalculatePositionNullEntry() {
        Position position = new Position(); // empty position, entry == null
        CashFlow cf = new CashFlow(emptySeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        // Should not throw any exception.
        cf.calculatePosition(position, 0);
    }

    /** calculatePosition should ignore a Position whose entry index is beyond finalIndex. */
    @Test
    public void testCalculatePositionEntryAfterFinalIndex() {
        // Entry at index 5, but finalIndex = 3
        Trade entry = Trade.buyAt(5, numFactory.numOf(10), numFactory.one());
        Position position = new Position(entry, null);
        CashFlow cf = new CashFlow(twoBarSeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        // No exception, and values remain the default (1.0)
        cf.calculatePosition(position, 3);
        assertTrue(cf.getValue(0).isEqual(numFactory.one()));
        assertTrue(cf.getValue(1).isEqual(numFactory.one()));
    }

    /** calculatePosition should handle a Position whose exit is null (open position). */
    @Test
    public void testCalculatePositionOpenPositionHandledMarkToMarket() {
        // Open long position (buy) at index 0, no exit.
        Trade entry = Trade.buyAt(0, numFactory.numOf(8), numFactory.one());
        Position position = new Position(entry, null);
        BaseTradingRecord tr = new BaseTradingRecord(position);
        // finalIndex = 1 (we want cash‑flow up to the next bar)
        CashFlow cf = new CashFlow(twoBarSeries, tr, 1, EquityCurveMode.MARK_TO_MARKET);
        // The calculation should not throw; the value at index 1 will be multiplied
        // by the mark‑to‑market ratio (close price / entry price).
        Num closePrice = twoBarSeries.getBar(1).getClosePrice();
        Num expectedRatio = closePrice.dividedBy(numFactory.numOf(8));
        Num expectedValue = numFactory.one().multipliedBy(expectedRatio);
        assertTrue(cf.getValue(1).isEqual(expectedValue));
    }

    /** calculatePosition with OpenPositionHandling.IGNORE must leave values unchanged for open positions. */
    @Test
    public void testCalculatePositionOpenPositionIgnored() {
        // Open long position at index 0, no exit.
        Trade entry = Trade.buyAt(0, numFactory.numOf(7), numFactory.one());
        Position position = new Position(entry, null);
        BaseTradingRecord tr = new BaseTradingRecord(position);
        // Use IGNORE handling – the open position should not affect cash flow.
        CashFlow cf = new CashFlow(twoBarSeries, tr, 1,
                EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.IGNORE);
        // Values must stay at the initial 1.0
        assertTrue(cf.getValue(0).isEqual(numFactory.one()));
        assertTrue(cf.getValue(1).isEqual(numFactory.one()));
    }

    /** Verify that getValue throws an exception when the requested index is outside the materialised window. */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueInvalidIndex() {
        CashFlow cf = new CashFlow(emptySeries,
                new BaseTradingRecord(),
                EquityCurveMode.MARK_TO_MARKET);
        cf.getValue(5); // No values were materialised.
    }

    /* ---------------------------------------------------------------------- */
    /*  4️⃣  COMPLETE TEST FILE – all cases merged together                     */
    /* ---------------------------------------------------------------------- */
}
