/**
 * Comprehensive JUnit‑4 test suite for {@link org.ta4j.core.Position}.
 *
 * The tests are written from the perspective of three “experts”.  Each expert
 * contributed a set of test cases that together cover:
 *
 *  • Construction (default & explicit)
 *  • State‑transition (new → opened → closed)
 *  • Public API behaviour (getters, side(), amount(), etc.)
 *  • Profit / loss calculations for long and short positions
 *  • Edge cases (null entry, illegal indexes, wrong trade types)
 *  • Equality / hashCode contract
 *
 * Real library objects are used – no mocks or custom implementations of the
 * library interfaces are created.  The TA4J concrete implementations
 * {@code DecimalNum}, {@code BaseBarSeries} and the static factory methods in
 * {@code Trade} are employed.
 */
package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;


import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Test class for {@link Position}.
 */
public class PositionTest {

    private static final Num TEN = DecimalNum.valueOf(10);
    private static final Num TWELVE = DecimalNum.valueOf(12);
    private static final Num EIGHT = DecimalNum.valueOf(8);
    private static final Num ONE = DecimalNum.valueOf(1);
    private static final Num ZERO = DecimalNum.valueOf(0);

    private BarSeries series;

    /**
     * Helper that builds a {@link BarSeries} with a single bar whose close price
     * equals the supplied {@code price}.  The series is needed for the static
     * {@code Trade.buyAt(...)} / {@code Trade.sellAt(...)} factory methods.
     */
    private BarSeries buildSeries(Num price) {
        // BaseBarSeries is the standard concrete implementation in TA4J.
        BarSeries s = new BaseBarSeries("test-series");
        // the builder creates a bar with the same open/high/low/close = price
        s.addBar(s.barBuilder()
                .timePeriod(1)
                .openPrice(price)
                .highPrice(price)
                .lowPrice(price)
                .closePrice(price)
                .volume(ONE)
                .build());
        return s;
    }

    @Before
    public void setUp() {
        series = buildSeries(TEN); // a dummy series – price 10 at index 0
    }

    /* ---------------------------------------------------------------------- */
    /*  1. Construction & basic state checks                                 */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructor() {
        Position pos = new Position(); // default BUY, zero‑cost models
        assertEquals(TradeType.BUY, pos.getStartingType());
        assertNull(pos.getEntry());
        assertNull(pos.getExit());
        assertTrue(pos.isNew());
        assertFalse(pos.isOpened());
        assertFalse(pos.isClosed());
        // cost models must fallback to ZeroCostModel when null
        assertTrue(pos.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(pos.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testExplicitStartingTypeConstructor() {
        Position longPos = new Position(TradeType.BUY);
        Position shortPos = new Position(TradeType.SELL);
        assertEquals(TradeType.BUY, longPos.getStartingType());
        assertEquals(TradeType.SELL, shortPos.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null); // should throw
    }

    /* ---------------------------------------------------------------------- */
    /*  2. State transitions using the operate(int,Num,Num) overload          */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOperateEntryAndExitUsingIndexPriceAmount() {
        Position pos = new Position(TradeType.BUY);
        // create entry at index 0, price 10, amount 1
        Trade entry = pos.operate(0, TEN, ONE);
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        assertEquals(TradeType.BUY, entry.getType());

        // after entry the position must be opened
        assertTrue(pos.isOpened());
        assertFalse(pos.isClosed());

        // create exit at index 1, price 12, amount 1 (sell – complement type)
        Trade exit = pos.operate(1, TWELVE, ONE);
        assertNotNull(exit);
        assertEquals(1, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType());

        // final state
        assertTrue(pos.isClosed());
        assertEquals(entry, pos.getEntry());
        assertEquals(exit, pos.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitWithEarlierIndexThrows() {
        Position pos = new Position(TradeType.BUY);
        pos.operate(5, TEN, ONE); // entry at index 5
        // trying to close before entry index must fail
        pos.operate(3, TWELVE, ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateWithWrongTradeTypeThrows() {
        // Position expects first trade to be BUY (default)
        Position pos = new Position();
        // Build a SELL trade manually and try to operate it
        Trade wrong = Trade.sellAt(0, series, ONE);
        pos.operate(wrong);
    }

    /* ---------------------------------------------------------------------- */
    /*  3. operate(Trade) overload – validation of cost model consistency   */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOperateWithPreBuiltTradeValidatesCostModel() {
        // Use ZeroCostModel (the default) for both entry & exit trades
        CostModel zeroCost = new ZeroCostModel();
        Trade entry = Trade.buyAt(0, series, ONE, zeroCost);
        Position pos = new Position(entry, zeroCost); // open‑position ctor validates cost model

        // operation with the same cost model succeeds
        Trade result = pos.operate(entry);
        assertSame(entry, result);
        assertTrue(pos.isOpened());

        // create a second trade with a *different* cost model
        CostModel otherCost = new ZeroCostModel() {
            // anonymous subclass only to differentiate instances
        };
        Trade exit = Trade.sellAt(1, series, ONE, otherCost);
        try {
            pos.operate(exit);
            fail("Expected IllegalArgumentException because cost models differ");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /* ---------------------------------------------------------------------- */
    /*  4. Equality & hashCode                                                */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = Trade.buyAt(0, series, ONE);
        Trade exit1 = Trade.sellAt(1, series, ONE);
        Position p1 = new Position(entry1, exit1);
        Position p2 = new Position(entry1, exit1);
        Position p3 = new Position(); // a different (new) position

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    /* ---------------------------------------------------------------------- */
    /*  5. Side, amount, averageEntryPrice, totalEntryCost, totalFees        */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testAccessorsWhenPositionIsNew() {
        Position pos = new Position();
        assertNull(pos.side());
        assertNull(pos.amount());
        assertNull(pos.averageEntryPrice());
        assertNull(pos.totalEntryCost());
        assertNull(pos.totalFees());
    }

    @Test
    public void testAccessorsAfterEntry() {
        Position pos = new Position();
        Trade entry = pos.operate(0, TEN, ONE);
        assertEquals(ExecutionSide.BUY, pos.side());
        assertEquals(ONE, pos.amount());
        assertEquals(TEN, pos.averageEntryPrice());
        // totalEntryCost = entry.getValue() = price * amount = 10
        assertEquals(TEN, pos.totalEntryCost());
        // totalFees = entry cost model is ZeroCostModel → zero
        assertEquals(ZERO, pos.totalFees());
    }

    /* ---------------------------------------------------------------------- */
    /*  6. Profit / loss calculations – long (BUY) position                 */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testProfitLongPosition() {
        // entry 10, exit 12, amount 1 → gross profit = 2, no costs → net profit = 2
        Trade entry = Trade.buyAt(0, series, ONE);
        Trade exit = Trade.sellAt(1, series, ONE);
        Position pos = new Position(entry, exit);

        assertEquals(TWELVE.minus(TEN), pos.getGrossProfit());
        assertEquals(TWELVE.minus(TEN), pos.getProfit()); // zero costs
        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());

        // gross return = exit / entry = 12 / 10 = 1.2
        Num expectedReturn = TWELVE.dividedBy(TEN);
        assertEquals(expectedReturn, pos.getGrossReturn());
    }

    @Test
    public void testProfitLongPositionWithLoss() {
        // entry 12, exit 8 → gross profit = -4
        BarSeries s = buildSeries(TWELVE);
        Trade entry = Trade.buyAt(0, s, ONE);
        Trade exit = Trade.sellAt(1, s, ONE);
        Position pos = new Position(entry, exit);

        Num gross = TWELVE.negated().plus(EIGHT); // (8 - 12) = -4
        assertEquals(gross, pos.getGrossProfit());
        assertEquals(gross, pos.getProfit());
        assertTrue(pos.hasLoss());
        assertFalse(pos.hasProfit());

        // gross return = 8 / 12 = 0.666...
        Num expectedReturn = EIGHT.dividedBy(TWELVE);
        assertEquals(expectedReturn, pos.getGrossReturn());
    }

    /* ---------------------------------------------------------------------- */
    /*  7. Profit / loss calculations – short (SELL) position                */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testProfitShortPosition() {
        // short entry (sell) at 12, cover (buy) at 10 → profit = 2
        BarSeries s = buildSeries(TWELVE);
        Trade entry = Trade.sellAt(0, s, ONE);
        Trade exit = Trade.buyAt(1, s, ONE);
        Position pos = new Position(entry, exit);

        // gross profit is calculated as -(exit - entry) for short positions
        Num gross = TWELVE.minus(TEN); // 2
        assertEquals(gross, pos.getGrossProfit());
        assertEquals(gross, pos.getProfit());
        assertTrue(pos.hasProfit());

        // gross return for short: ((exit/entry - 1) * -1) + 1
        // exit/entry = 10/12 = 0.83333..., 1 - 0.83333 = 0.16666..., so return = 0.83333...
        Num expectedReturn = ONE.minus(EIGHT.dividedBy(TWELVE).negate()).minus(ONE).negate().plus(ONE);
        // simpler: (exit/entry - 1) = -0.16666..., negated = 0.16666..., +1 = 1.16666? Wait formula:
        // For sell: ((exit/entry - 1) * -1) + 1 = ( (10/12 - 1) * -1 ) + 1 = ( -0.16666 * -1 ) + 1 = 0.16666 + 1 = 1.16666
        Num expectedGrossReturn = EIGHT.dividedBy(TWELVE); // actually the library returns 0.83333? Let's compute using formula in code:
        // getGrossReturn(entry, exit) for sell: one = entry.one(); ((exit/entry - one).negate()).plus(one)
        // exit/entry = 10/12 = 0.83333, minus one = -0.16666, negate = 0.16666, plus one = 1.16666
        Num one = TEN.getNumFactory().one(); // 1
        Num exitDivEntry = TEN.dividedBy(TWELVE);
        Num expected = exitDivEntry.minus(one).negate().plus(one);
        assertEquals(expected, pos.getGrossReturn());
    }

    /* ---------------------------------------------------------------------- */
    /*  8. Open position profit calculations (should be zero)                */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOpenPositionProfitIsZero() {
        Position pos = new Position();
        pos.operate(0, TEN, ONE); // entry only
        assertTrue(pos.isOpened());
        assertEquals(ZERO, pos.getGrossProfit());
        assertEquals(ZERO, pos.getProfit());

        // profit for an open position with a supplied final price
        Num profitWithFinal = pos.getProfit(5, TWELVE); // use final price 12
        // gross part = (12 * 1) - 10 = 2 ; costs are zero → profit = 2
        assertEquals(TWELVE.multipliedBy(ONE).minus(TEN), profitWithFinal);
    }

    /* ---------------------------------------------------------------------- */
    /*  9. Holding / transaction cost integration (ZeroCostModel)            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPositionCostWithZeroCostModels() {
        Trade entry = Trade.buyAt(0, series, ONE);
        Trade exit = Trade.sellAt(1, series, ONE);
        Position pos = new Position(entry, exit); // uses ZeroCostModel internally

        // both transaction and holding costs are zero → total cost = 0
        assertEquals(ZERO, pos.getPositionCost());
        assertEquals(ZERO, pos.getTransactionCostModel().calculate(pos));
        assertEquals(ZERO, pos.getHoldingCostModel().calculate(pos));
    }

    /* ---------------------------------------------------------------------- */
    /* 10. toString representation (basic sanity check)                      */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0, series, ONE);
        Trade exit = Trade.sellAt(1, series, ONE);
        Position pos = new Position(entry, exit);
        String txt = pos.toString();
        assertTrue(txt.contains("Entry"));
        assertTrue(txt.contains("exit"));
        assertTrue(txt.contains(entry.toString()));
        assertTrue(txt.contains(exit.toString()));
    }
}
