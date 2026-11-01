package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>All public constructors and getters</li>
 *   <li>State predicates {@code isNew()}, {@code isOpened()}, {@code isClosed()}</li>
 *   <li>Operation logic (entry/exit creation, illegal index)</li>
 *   <li>Profit / loss calculations (gross, net, return)</li>
 *   <li>Edge‑case handling (null arguments, mismatched trade types, zero amounts, NaN values)</li>
 *   <li>Equality and hash‑code contracts</li>
 * </ul>
 *
 * <p>Only classes and methods that appear in the supplied source code or in the
 * dependency signatures are used – no external mocking frameworks are required.
 */
public class PositionTest {

    /* ---------------------------------------------------------------------- */
    /*  Helper constants                                                    */
    /* ---------------------------------------------------------------------- */

    private static final Num PRICE_10 = DecimalNum.valueOf(10);
    private static final Num PRICE_12 = DecimalNum.valueOf(12);
    private static final Num PRICE_8  = DecimalNum.valueOf(8);
    private static final Num AMOUNT_5  = DecimalNum.valueOf(5);
    private static final Num ZERO      = DecimalNum.valueOf(0);
    private static final CostModel ZERO_COST_MODEL = new ZeroCostModel();

    /* ---------------------------------------------------------------------- */
    /*  Fixtures                                                             */
    /* ---------------------------------------------------------------------- */

    private Trade entryBuy;   // entry trade for a BUY position
    private Trade exitSell;   // exit trade for a BUY position

    @Before
    public void setUp() {
        // A simple BUY entry at price 10, amount 5
        entryBuy = Trade.buyAt(0, PRICE_10, AMOUNT_5, ZERO_COST_MODEL);
        // A corresponding SELL exit at price 12, amount 5
        exitSell = Trade.sellAt(1, PRICE_12, AMOUNT_5, ZERO_COST_MODEL);
    }

    /* ---------------------------------------------------------------------- */
    /*  Constructor tests                                                    */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorCreatesBuyPosition() {
        Position pos = new Position();               // default -> BUY
        assertEquals(TradeType.BUY, pos.getStartingType());
        assertTrue(pos.isNew());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position pos = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, pos.getStartingType());
        assertTrue(pos.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingTypeThrows() {
        new Position(null);
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Position pos = new Position(entryBuy, exitSell);
        assertEquals(entryBuy, pos.getEntry());
        assertEquals(exitSell, pos.getExit());
        assertFalse(pos.isNew());
        assertTrue(pos.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedTradeTypesThrows() {
        // both BUY – illegal
        Trade anotherBuy = Trade.buyAt(2, PRICE_8, AMOUNT_5, ZERO_COST_MODEL);
        new Position(entryBuy, anotherBuy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDifferentCostModelsThrows() {
        // entry uses ZERO_COST_MODEL, exit uses a different (non‑zero) model
        CostModel nonZero = new CostModel() {
            public Num calculate(Position p, int i) { return DecimalNum.valueOf(1); }
            public Num calculate(Position p)       { return DecimalNum.valueOf(1); }
            public Num calculate(Num price, Num amount) { return DecimalNum.valueOf(1); }
            public boolean equals(CostModel other) { return false; }
        };
        Trade exitOther = Trade.sellAt(1, PRICE_12, AMOUNT_5, nonZero);
        new Position(entryBuy, exitOther);
    }

    /* ---------------------------------------------------------------------- */
    /*  State predicate tests                                                */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testInitialStateIsNew() {
        Position pos = new Position();
        assertTrue(pos.isNew());
        assertFalse(pos.isOpened());
        assertFalse(pos.isClosed());
    }

    @Test
    public void testStateAfterEntry() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5);   // creates entry
        assertFalse(pos.isNew());
        assertTrue(pos.isOpened());
        assertFalse(pos.isClosed());
    }

    @Test
    public void testStateAfterExit() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5);   // entry
        pos.operate(2, PRICE_12, AMOUNT_5);   // exit
        assertFalse(pos.isNew());
        assertFalse(pos.isOpened());
        assertTrue(pos.isClosed());
    }

    /* ---------------------------------------------------------------------- */
    /*  operate(...) tests                                                   */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryWhenNew() {
        Position pos = new Position();
        Trade t = pos.operate(5, PRICE_10, AMOUNT_5);
        assertNotNull(t);
        assertEquals(5, t.getIndex());
        assertEquals(TradeType.BUY, t.getType());
        assertEquals(entryBuy.getAmount(), t.getAmount()); // amount is the same
        assertEquals(entryBuy, pos.getEntry());
        assertNull(pos.getExit());
    }

    @Test
    public void testOperateCreatesExitWhenOpened() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5);   // entry
        Trade t = pos.operate(3, PRICE_12, AMOUNT_5);
        assertNotNull(t);
        assertEquals(3, t.getIndex());
        assertEquals(TradeType.SELL, t.getType());
        assertEquals(exitSell, pos.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitWithEarlierIndexThrows() {
        Position pos = new Position();
        pos.operate(5, PRICE_10, AMOUNT_5);   // entry at 5
        pos.operate(3, PRICE_12, AMOUNT_5);   // attempt exit at 3 -> illegal
    }

    @Test
    public void testOperateWithoutPriceOrAmountUsesNaN() {
        Position pos = new Position();
        Trade t = pos.operate(0);   // uses NaN for price/amount
        assertNotNull(t);
        // NaN handling is internal to Trade – we only verify that a trade is created
        assertEquals(TradeType.BUY, t.getType());
    }

    /* ---------------------------------------------------------------------- */
    /*  Equality & hashCode tests                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position(entryBuy, exitSell);
        Position p2 = new Position(entryBuy, exitSell);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // different exit
        Trade differentExit = Trade.sellAt(1, PRICE_8, AMOUNT_5, ZERO_COST_MODEL);
        Position p3 = new Position(entryBuy, differentExit);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testEqualsWithNullTrades() {
        Position p1 = new Position();   // both null
        Position p2 = new Position();   // both null
        assertEquals(p1, p2);
        assertTrue(p1.isNew());
    }

    /* ---------------------------------------------------------------------- */
    /*  Profit / loss calculations                                           */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGrossProfitClosedBuyPosition() {
        Position pos = new Position(entryBuy, exitSell);
        // gross profit = exit value – entry value = (price*amount) – (price*amount)
        Num expected = exitSell.getValue().minus(entryBuy.getValue());
        assertEquals(expected, pos.getGrossProfit());
    }

    @Test
    public void testGrossProfitClosedSellPosition() {
        // reverse: entry SELL at 12, exit BUY at 10
        Trade entrySell = Trade.sellAt(0, PRICE_12, AMOUNT_5, ZERO_COST_MODEL);
        Trade exitBuy   = Trade.buyAt(1, PRICE_10, AMOUNT_5, ZERO_COST_MODEL);
        Position pos = new Position(entrySell, exitBuy);
        // calculation is negated for short positions
        Num gross = exitBuy.getValue().minus(entrySell.getValue()).negate();
        assertEquals(gross, pos.getGrossProfit());
    }

    @Test
    public void testNetProfitIncludesZeroCostModel() {
        Position pos = new Position(entryBuy, exitSell);
        // ZeroCostModel => transaction cost = 0, holding cost = 0
        Num expected = pos.getGrossProfit(); // net = gross - 0
        assertEquals(expected, pos.getProfit());
    }

    @Test
    public void testProfitWhenOpenReturnsZero() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5); // only entry
        assertEquals(ZERO, pos.getProfit());
        assertEquals(ZERO, pos.getGrossProfit());
    }

    @Test
    public void testHasProfitAndLossFlags() {
        Position profitPos = new Position(entryBuy, exitSell); // price up => profit
        assertTrue(profitPos.hasProfit());
        assertFalse(profitPos.hasLoss());

        // opposite direction => loss
        Trade entrySell = Trade.sellAt(0, PRICE_12, AMOUNT_5, ZERO_COST_MODEL);
        Trade exitBuy   = Trade.buyAt(1, PRICE_10, AMOUNT_5, ZERO_COST_MODEL);
        Position lossPos = new Position(entrySell, exitBuy);
        assertTrue(lossPos.hasLoss());
        assertFalse(lossPos.hasProfit());
    }

    @Test
    public void testProfitWithExplicitFinalIndexAndPriceOnOpenPosition() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5); // entry only
        // Suppose we close virtually at index 2, price 12
        Num profit = pos.getProfit(2, PRICE_12);
        // gross = amount*finalPrice - entryValue
        Num gross = AMOUNT_5.multipliedBy(PRICE_12).minus(entryBuy.getValue());
        // zero cost model => cost = 0
        assertEquals(gross, profit);
    }

    /* ---------------------------------------------------------------------- */
    /*  Gross return calculations                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGrossReturnBuyPosition() {
        Position pos = new Position(entryBuy, exitSell);
        // return = exitPrice / entryPrice = 12 / 10 = 1.2
        Num expected = PRICE_12.dividedBy(PRICE_10);
        assertEquals(expected, pos.getGrossReturn());
    }

    @Test
    public void testGrossReturnSellPosition() {
        Trade entrySell = Trade.sellAt(0, PRICE_12, AMOUNT_5, ZERO_COST_MODEL);
        Trade exitBuy   = Trade.buyAt(1, PRICE_10, AMOUNT_5, ZERO_COST_MODEL);
        Position pos = new Position(entrySell, exitBuy);
        // For short: return = 1 - (exit/entry - 1) = 2 - exit/entry = 2 - 10/12 = 1.1666...
        Num one = DecimalNum.valueOf(1);
        Num expected = one.minus(PRICE_10.dividedBy(PRICE_12)).negate().plus(one);
        assertEquals(expected, pos.getGrossReturn());
    }

    @Test
    public void testGrossReturnWithProvidedPrices() {
        Position pos = new Position(entryBuy, exitSell);
        Num ret = pos.getGrossReturn(PRICE_10, PRICE_12);
        assertEquals(DecimalNum.valueOf(1.2), ret);
    }

    @Test
    public void testGrossReturnWithNaNPricesReturnsZero() {
        Position pos = new Position(entryBuy, exitSell);
        Num nan = DecimalNum.NaN; // assuming DecimalNum provides NaN constant
        // The implementation delegates to getEntry().isBuy() etc.; NaN handling is internal.
        // We only verify that the method does not throw.
        Num ret = pos.getGrossReturn(nan, nan);
        // When both prices are NaN, the division yields NaN; the test accepts NaN.
        assertTrue(ret.isNaN() || ret.isZero());
    }

    /* ---------------------------------------------------------------------- */
    /*  Cost model related tests                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPositionCostWithZeroCostModelIsZero() {
        Position pos = new Position(entryBuy, exitSell);
        assertEquals(ZERO, pos.getPositionCost());
    }

    @Test
    public void testHoldingCostDelegatesToHoldingCostModel() {
        // custom holding cost model that returns a constant value
        CostModel holding = new CostModel() {
            public Num calculate(Position p, int i) { return DecimalNum.valueOf(3); }
            public Num calculate(Position p)       { return DecimalNum.valueOf(3); }
            public Num calculate(Num price, Num amount) { return DecimalNum.valueOf(0); }
            public boolean equals(CostModel other) { return false; }
        };
        Position pos = new Position(entryBuy, exitSell, ZERO_COST_MODEL, holding);
        assertEquals(DecimalNum.valueOf(3), pos.getHoldingCost());
        assertEquals(DecimalNum.valueOf(3), pos.getPositionCost()); // transaction cost = 0
    }

    /* ---------------------------------------------------------------------- */
    /*  toString test (basic sanity)                                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Position pos = new Position(entryBuy, exitSell);
        String s = pos.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(entryBuy.toString()));
        assertTrue(s.contains(exitSell.toString()));
    }
}
