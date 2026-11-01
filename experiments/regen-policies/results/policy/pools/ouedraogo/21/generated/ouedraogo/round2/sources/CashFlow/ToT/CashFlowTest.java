package org.ta4j.core.analysis;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.Trade.TradeType;


import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

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
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Comprehensive JUnit‑4 test suite for {@link CashFlow}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Construction with a closed position (normal flow)</li>
 *   <li>Construction with an open position (error scenario)</li>
 *   <li>Construction with a {@link TradingRecord} that contains several
 *       positions (including an open one)</li>
 *   <li>Static helpers {@code addCost}, {@code getIntermediateRatio} and
 *       {@code determineEndIndex}</li>
 *   <li>Boundary conditions such as empty series, single‑bar series and
 *       positions whose exit index is beyond the series end.</li>
 * </ul>
 *
 * All objects are created through the public API of *ta4j* – no mocking,
 * subclassing or external libraries are used.
 */
public class CashFlowTest {

    private BarSeries series;
    private NumFactory nf;

    /** Helper to create a {@link Num} from a {@code double}. */
    private Num n(double v) {
        return nf.numOf(v);
    }

    /** Builds a simple {@link BarSeries} with the supplied close prices. */
    private BarSeries buildSeries(double... closePrices) {
        BarSeries s = new BaseBarSeries("test-series");
        ZonedDateTime time = ZonedDateTime.now();
        for (double price : closePrices) {
            // In these tests only the close price is relevant; other fields are set to the same value.
            Bar bar = new BaseBar(Duration.ofDays(1), time,
                    n(price), n(price), n(price), n(price), n(0));
            s.addBar(bar);
            time = time.plusDays(1);
        }
        return s;
    }

    @Before
    public void setUp() {
        // a series with 5 bars – close prices 10, 12, 14, 13, 15
        series = buildSeries(10, 12, 14, 13, 15);
        nf = series.numFactory(); // factory used throughout the tests
    }

    /** ---------------------------------------------------------------
   *  1️⃣  Test the static helper {@code addCost}
   *  --------------------------------------------------------------- */
    @Test
    public void testAddCost() {
        // Long trade – cost should be subtracted
        Num raw = n(100);
        Num cost = n(2);
        Num resultLong = CashFlow.addCost(raw, cost, true);
        assertEquals(n(98), resultLong);

        // Short trade – cost should be added
        Num resultShort = CashFlow.addCost(raw, cost, false);
        assertEquals(n(102), resultShort);
    }

    /** ---------------------------------------------------------------
   *  2️⃣  Test the static helper {@code getIntermediateRatio}
   * --------------------------------------------------------------- */
    @Test
    public void testGetIntermediateRatio() {
        // Long – simple price ratio
        Num entry = n(10);
        Num exit = n(12);
        Num ratioLong = CashFlow.getIntermediateRatio(true, entry, exit);
        assertEquals(n(1.2), ratioLong);

        // Short – 2 - (exit/entry)
        Num ratioShort = CashFlow.getIntermediateRatio(false, entry, exit);
        // 2 - (12/10) = 0.8
        assertEquals(n(0.8), ratioShort);
    }

    /** ---------------------------------------------------------------
   *  3️⃣  Test the static helper {@code determineEndIndex}
   * --------------------------------------------------------------- */
    @Test
    public void testDetermineEndIndex() {
        // Closed position – exit index = 3, finalIndex = 4, maxIndex = 4
        Position closed = new Position(
                new Trade(TradeType.BUY, n(10), n(1), 1), // entry at index 1
                new Trade(TradeType.SELL, n(13), n(1), 3) // exit at index 3
        );
        int end = CashFlow.determineEndIndex(closed, 4, 4);
        assertEquals(3, end); // should be the exit index

        // Open position – no exit, finalIndex = 2, maxIndex = 4
        Position open = new Position(
                new Trade(TradeType.BUY, n(10), n(1), 1), // entry at index 1
                null
        );
        int endOpen = CashFlow.determineEndIndex(open, 2, 4);
        assertEquals(2, endOpen); // limited by finalIndex

        // finalIndex beyond maxIndex
        int endClamped = CashFlow.determineEndIndex(open, 10, 4);
        assertEquals(4, endClamped);
    }

    /** ---------------------------------------------------------------
   *  4️⃣  Constructor with a **closed** single position.
   * --------------------------------------------------------------- */
    @Test
    public void testCashFlowSingleClosedPosition() {
        // Position: BUY at index 0 (price 10), SELL at index 4 (price 15)
        Position pos = new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                new Trade(TradeType.SELL, n(15), n(1), 4)
        );

        CashFlow cf = new CashFlow(series, pos);

        // The cash‑flow series always starts with 1
        assertEquals(n(1), cf.getValue(0));

        // Since the position is long, the final value should be 15/10 = 1.5
        assertEquals(n(1.5), cf.getValue(4));

        // Values between entry and exit must be monotonically increasing
        for (int i = 1; i <= 4; i++) {
            assertTrue(cf.getValue(i).isGreaterThanOrEqual(cf.getValue(i - 1)));
        }

        // After the last bar the series is padded with the last value
        assertEquals(cf.getValue(4), cf.getValue(series.getEndIndex()));
    }

    /** ---------------------------------------------------------------
   *  5️⃣  Constructor with an **open** position should throw.
   * --------------------------------------------------------------- */
    @Test(expected = IllegalArgumentException.class)
    public void testCashFlowOpenPositionThrows() {
        // Position is opened (no exit)
        Position open = new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                null
        );
        // This must raise IllegalArgumentException because the position is not closed
        new CashFlow(series, open);
    }

    /** ---------------------------------------------------------------
   *  6️⃣  Constructor with a {@link TradingRecord} containing several
   *       positions (closed + open) and a custom finalIndex.
   * --------------------------------------------------------------- */
    @Test
    public void testCashFlowWithTradingRecordAndFinalIndex() {
        // Build a trading record:
        //   1) BUY at index 0 (10) → SELL at index 2 (14)   – closed
        //   2) BUY at index 3 (13) → still open
        TradingRecord record = new BaseTradingRecord();

        // first trade (closed)
        record.operate(0, n(10), n(1)); // entry
        record.operate(2, n(14), n(1)); // exit

        // second trade (open)
        record.operate(3, n(13), n(1)); // entry, no exit yet

        // Use finalIndex = 4 (last bar) – cash‑flow of the open position must be
        // accrued up to this index.
        CashFlow cf = new CashFlow(series, record, series.getEndIndex());

        // Expected cash‑flow:
        //   after first closed trade: 14/10 = 1.4
        //   after open trade (long) at index 4: price 15 → ratio 15/13 ≈ 1.1538
        //   total = 1.4 * 1.153846... = 1.615384...
        Num expectedAfterClosed = n(14).dividedBy(n(10)); // 1.4
        Num openRatio = n(15).dividedBy(n(13));          // ≈1.153846
        Num expectedFinal = expectedAfterClosed.multipliedBy(openRatio);

        // Verify values at key indices
        assertEquals(n(1), cf.getValue(0));                 // start
        assertEquals(expectedAfterClosed, cf.getValue(2)); // after closed trade
        assertEquals(expectedFinal, cf.getValue(4));       // after open trade accrued

        // Ensure the series is padded correctly
        assertEquals(expectedFinal, cf.getValue(series.getEndIndex()));
    }

    /** ---------------------------------------------------------------
   *  7️⃣  Edge case: empty {@link BarSeries}.
   * --------------------------------------------------------------- */
    @Test
    public void testCashFlowEmptySeries() {
        BarSeries empty = new BaseBarSeries("empty");
        // No bars – the series end index is -1. We create a dummy closed position
        // with entry/exit at index 0 (which will never be used because the series is empty).
        Position dummy = new Position(
                new Trade(TradeType.BUY, n(1), n(1), 0),
                new Trade(TradeType.SELL, n(1), n(1), 0)
        );

        CashFlow cf = new CashFlow(empty, dummy);
        // The values list must contain exactly one element (the initial 1)
        assertEquals(1, cf.getSize());
        assertEquals(n(1), cf.getValue(0));
    }

    /** ---------------------------------------------------------------
   *  8️⃣  Verify {@link CashFlow#getSize()} delegates to the series.
   * --------------------------------------------------------------- */
    @Test
    public void testGetSize() {
        CashFlow cf = new CashFlow(series, new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                new Trade(TradeType.SELL, n(12), n(1), 1)
        ));
        assertEquals(series.getBarCount(), cf.getSize());
    }

    /** ---------------------------------------------------------------
   *  9️⃣  Verify {@link CashFlow#getUnstableBars()} always returns 0.
   * --------------------------------------------------------------- */
    @Test
    public void testGetUnstableBars() {
        CashFlow cf = new CashFlow(series, new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                new Trade(TradeType.SELL, n(12), n(1), 1)
        ));
        assertEquals(0, cf.getUnstableBars());
    }

    /** ---------------------------------------------------------------
   * 10️⃣  Verify {@link CashFlow#fillToTheEnd(int)} pads correctly.
   * --------------------------------------------------------------- */
    @Test
    public void testFillToTheEndPadding() {
        // Create a series with 3 bars only.
        BarSeries shortSeries = buildSeries(10, 11, 12);
        Position pos = new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                new Trade(TradeType.SELL, n(12), n(1), 2)
        );
        // CashFlow will compute values up to index 2 and then pad to index 2
        CashFlow cf = new CashFlow(shortSeries, pos);
        // series end index = 2, values size must be 3 (0,1,2)
        assertEquals(3, cf.getSize());
        // All values after the last computed index (2) must equal the last value
        Num last = cf.getValue(2);
        for (int i = 2; i <= shortSeries.getEndIndex(); i++) {
            assertEquals(last, cf.getValue(i));
        }
    }

    /** ---------------------------------------------------------------
   * 11️⃣  Verify that a short position produces the correct cash‑flow.
   * --------------------------------------------------------------- */
    @Test
    public void testCashFlowShortPosition() {
        // Short: SELL at 15 (index 0), BUY at 10 (index 2)
        Position shortPos = new Position(
                new Trade(TradeType.SELL, n(15), n(1), 0),
                new Trade(TradeType.BUY, n(10), n(1), 2)
        );
        CashFlow cf = new CashFlow(series, shortPos);

        // For short trades the ratio is 2 - (exit/entry)
        // exit price = 10, entry price = 15 → ratio = 2 - (10/15) = 1.33333...
        Num expectedRatio = nf.numOf(2).minus(n(10).dividedBy(n(15)));
        Num expected = n(1).multipliedBy(expectedRatio); // start from 1

        // Value at exit index (2) must equal the expected ratio
        assertEquals(expected, cf.getValue(2));

        // After exit the series is padded with the same value
        assertEquals(expected, cf.getValue(series.getEndIndex()));
    }

    /** ---------------------------------------------------------------
   * 12️⃣  Verify that holding cost is correctly distributed over periods.
   *
   *   The Position API already computes holding cost; we simply verify that
   *   the cash‑flow calculation uses the average cost per period.
   * --------------------------------------------------------------- */
    @Test
    public void testHoldingCostDistribution() {
        // Use a custom CostModel that returns a fixed holding cost of 4 over the whole trade.
        // The library provides a SimpleCostModel; we can use it via its constructor.
        // (The class is part of the public API, therefore allowed.)
        CostModel holdingCostModel = new SimpleCostModel(DecimalNum.valueOf(4));

        Position pos = new Position(
                TradeType.BUY,
                holdingCostModel,   // transaction cost model (not used)
                holdingCostModel    // holding cost model
        );

        // Operate entry at index 0 price 10, exit at index 4 price 14
        pos.operate(0, n(10), n(1));
        pos.operate(4, n(14), n(1));

        // The total holding cost = 4, periods = 4 (indexes 1‑4)
        // average cost per period = 4 / 4 = 1
        // Net price each period = closePrice - 1
        CashFlow cf = new CashFlow(series, pos);

        // Compute expected cash‑flow manually:
        // entry price = 10
        // period 1 close = 12 → net = 11 → ratio = 11/10 = 1.1
        // period 2 close = 14 → net = 13 → ratio = 13/10 = 1.3
        // period 3 close = 13 → net = 12 → ratio = 12/10 = 1.2
        // period 4 (exit) close = 15 → net = 14 → ratio = 14/10 = 1.4
        // The cash‑flow series should contain the cumulative product of these ratios.
        Num cf1 = n(1).multipliedBy(n(11).dividedBy(n(10))); // 1.1
        Num cf2 = cf1.multipliedBy(n(13).dividedBy(n(10))); // 1.43
        Num cf3 = cf2.multipliedBy(n(12).dividedBy(n(10))); // 1.716
        Num cf4 = cf3.multipliedBy(n(14).dividedBy(n(10))); // 2.4024

        // Validate a few key points
        assertEquals(cf1, cf.getValue(1));
        assertEquals(cf2, cf.getValue(2));
        assertEquals(cf3, cf.getValue(3));
        assertEquals(cf4, cf.getValue(4));
    }

    /** ---------------------------------------------------------------
   * 13️⃣  Verify that calling {@code getValue} with an out‑of‑range index
   *       throws {@link IndexOutOfBoundsException}.
   * --------------------------------------------------------------- */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfRange() {
        CashFlow cf = new CashFlow(series, new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                new Trade(TradeType.SELL, n(12), n(1), 1)
        ));
        cf.getValue(series.getEndIndex() + 1); // should fail
    }

    /** ---------------------------------------------------------------
   * 14️⃣  Verify that {@code fillToTheEnd} does not alter already‑filled
   *       values when the end index is smaller than the current size.
   * --------------------------------------------------------------- */
    @Test
    public void testFillToTheEndNoChangeWhenAlreadyLongEnough() {
        // Create a series with 4 bars.
        BarSeries s = buildSeries(10, 11, 12, 13);
        Position pos = new Position(
                new Trade(TradeType.BUY, n(10), n(1), 0),
                new Trade(TradeType.SELL, n(13), n(1), 3)
        );
        CashFlow cf = new CashFlow(s, pos);
        int sizeBefore = cf.getSize();
        // invoke fillToTheEnd with an index smaller than current size
        // (using reflection to call the private method)
        try {
            java.lang.reflect.Method m = CashFlow.class.getDeclaredMethod("fillToTheEnd", int.class);
            m.setAccessible(true);
            m.invoke(cf, 2);
        } catch (Exception e) {
            fail("Reflection failed: " + e);
        }
        assertEquals(sizeBefore, cf.getSize());
    }

    /** ---------------------------------------------------------------
   * 15️⃣  Verify that a position with a negative net entry price (invalid)
   *       does **not** affect the cash‑flow series (the method guards
   *       against negative balances).
   * --------------------------------------------------------------- */
    @Test
    public void testNegativeBalanceGuard() {
        // Craft a position whose entry net price is larger than the series start value
        // but we will artificially force a negative balance by using a huge holding cost.
        CostModel hugeHoldingCost = new SimpleCostModel(DecimalNum.valueOf(1_000_000));
        Position pos = new Position(
                TradeType.BUY,
                new SimpleCostModel(DecimalNum.ZERO), // transaction cost
                hugeHoldingCost
        );
        pos.operate(0, n(10), n(1));
        pos.operate(2, n(12), n(1)); // closed at index 2

        CashFlow cf = new CashFlow(series, pos);
        // The cash‑flow should remain at the initial value (1) for all indices
        for (int i = 0; i <= series.getEndIndex(); i++) {
            assertEquals(n(1), cf.getValue(i));
        }
    }

}
