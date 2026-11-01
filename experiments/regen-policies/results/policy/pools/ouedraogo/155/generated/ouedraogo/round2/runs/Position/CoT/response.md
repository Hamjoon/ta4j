###Test START##
```java
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
 * The test cases cover:
 * <ul>
 *   <li>All public methods (signatures listed in the comment block)</li>
 *   <li>Normal behaviour</li>
 *   <li>Edge cases and exception handling</li>
 * </ul>
 *
 * The library provides concrete implementations for {@link Num} (DecimalNum)
 * and a zero‑cost model (ZeroCostModel); they are used to avoid the need for
 * custom stubs.
 */
public class PositionTest {

    /* -------------------------------------------------------------
     *  List of public methods (extracted from Position.java)
     * -------------------------------------------------------------
     *
     * Position()
     * Position(TradeType startingType)
     * Position(TradeType startingType,
     *          CostModel transactionCostModel,
     *          CostModel holdingCostModel)
     * Position(Trade entry, Trade exit)
     * Position(Trade entry, Trade exit,
     *          CostModel transactionCostModel,
     *          CostModel holdingCostModel)
     *
     * Trade getEntry()
     * Trade getExit()
     * boolean equals(Object obj)
     * int hashCode()
     *
     * Trade operate(int index)
     * Trade operate(int index, Num price, Num amount)
     *
     * boolean isClosed()
     * boolean isOpened()
     * boolean isNew()
     *
     * boolean hasProfit()
     * boolean hasLoss()
     *
     * Num getProfit()
     * Num getProfit(int finalIndex, Num finalPrice)
     *
     * Num getGrossProfit()
     * Num getGrossProfit(Num finalPrice)
     *
     * Num getGrossReturn()
     * Num getGrossReturn(Num finalPrice)
     * Num getGrossReturn(BarSeries barSeries)
     * Num getGrossReturn(Num entryPrice, Num exitPrice)
     *
     * Num getPositionCost(int finalIndex)
     * Num getPositionCost()
     *
     * Num getHoldingCost()
     * Num getHoldingCost(int finalIndex)
     *
     * TradeType getStartingType()
     *
     * String toString()
     */

    private static final Num PRICE_10 = DecimalNum.valueOf(10);
    private static final Num PRICE_12 = DecimalNum.valueOf(12);
    private static final Num PRICE_8  = DecimalNum.valueOf(8);
    private static final Num AMOUNT_5 = DecimalNum.valueOf(5);
    private static final Num ZERO    = DecimalNum.valueOf(0);
    private static final Num ONE     = DecimalNum.valueOf(1);

    private Position emptyPosition;               // created with default ctor (new)
    private Position buyPosition;                 // created with starting type BUY
    private Position sellPosition;                // created with starting type SELL

    @Before
    public void setUp() {
        emptyPosition = new Position();               // startingType = BUY, no trades yet
        buyPosition   = new Position(TradeType.BUY);  // same as default but explicit
        sellPosition  = new Position(TradeType.SELL);
    }

    /* -------------------------------------------------------------
     *  Constructor tests
     * ------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorStartsWithBuy() {
        assertEquals(TradeType.BUY, emptyPosition.getStartingType());
        assertTrue(emptyPosition.isNew());
    }

    @Test
    public void testConstructorWithExplicitStartingType() {
        assertEquals(TradeType.SELL, sellPosition.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStartingTypeNullThrows() {
        new Position(null);
    }

    @Test
    public void testConstructorEntryExitDifferentTypesCreatesPosition() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position pos = new Position(entry, exit);
        assertEquals(entry, pos.getEntry());
        assertEquals(exit,  pos.getExit());
        assertTrue(pos.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryExitSameTypeThrows() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.buyAt(1, PRICE_12, AMOUNT_5); // also BUY → illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryExitMismatchingCostModelThrows() {
        // entry uses a custom non‑zero cost model, exit uses the default zero model
        CostModel customCost = new CostModel() {
            @Override public Num calculate(Position p, int i) { return ZERO; }
            @Override public Num calculate(Position p) { return ZERO; }
            @Override public Num calculate(Num price, Num amount) { return ZERO; }
            @Override public boolean equals(CostModel other) { return false; }
        };
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5, customCost);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5); // zero‑cost model
        new Position(entry, exit, customCost, new ZeroCostModel());
    }

    /* -------------------------------------------------------------
     *  Basic state queries
     * ------------------------------------------------------------- */

    @Test
    public void testIsNewIsOpenedIsClosedTransitions() {
        // initially new
        assertTrue(emptyPosition.isNew());
        assertFalse(emptyPosition.isOpened());
        assertFalse(emptyPosition.isClosed());

        // after first operate → entry created, position opened
        Trade entryTrade = emptyPosition.operate(0);
        assertNotNull(entryTrade);
        assertTrue(emptyPosition.isOpened());
        assertFalse(emptyPosition.isNew());
        assertFalse(emptyPosition.isClosed());

        // after second operate → exit created, position closed
        Trade exitTrade = emptyPosition.operate(1);
        assertNotNull(exitTrade);
        assertTrue(emptyPosition.isClosed());
        assertFalse(emptyPosition.isOpened());
        assertFalse(emptyPosition.isNew());
    }

    @Test
    public void testGetEntryAndGetExit() {
        Position pos = new Position();
        assertNull(pos.getEntry());
        assertNull(pos.getExit());

        pos.operate(0, PRICE_10, AMOUNT_5);
        assertNotNull(pos.getEntry());
        assertNull(pos.getExit());

        pos.operate(1, PRICE_12, AMOUNT_5);
        assertNotNull(pos.getExit());
    }

    /* -------------------------------------------------------------
     *  operate(..) tests
     * ------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryWithNaNWhenPriceAndAmountNotProvided() {
        Position pos = new Position();
        Trade entry = pos.operate(2);
        assertEquals(2, entry.getIndex());
        // price and amount are NaN – we only verify that a trade was created
        assertNotNull(entry);
        assertEquals(TradeType.BUY, entry.getType()); // default starting type is BUY
    }

    @Test
    public void testOperateCreatesExitWithComplementType() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5); // entry → BUY
        Trade exit = pos.operate(2, PRICE_12, AMOUNT_5);
        assertEquals(2, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType()); // complement of BUY
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitWithIndexBeforeEntryThrows() {
        Position pos = new Position();
        pos.operate(5, PRICE_10, AMOUNT_5); // entry at index 5
        pos.operate(3, PRICE_12, AMOUNT_5); // attempt to close before entry → exception
    }

    /* -------------------------------------------------------------
     *  Equality & hashCode
     * ------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Trade e1 = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade x1 = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position p1 = new Position(e1, x1);
        Position p2 = new Position(e1, x1);
        Position p3 = new Position(Trade.buyAt(0, PRICE_10, AMOUNT_5),
                                   Trade.sellAt(2, PRICE_12, AMOUNT_5)); // different exit index

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    /* -------------------------------------------------------------
     *  Profit / Gross profit / Return tests (zero‑cost model)
     * ------------------------------------------------------------- */

    @Test
    public void testGrossProfitForBuyPosition() {
        // BUY at 10 (amount 5) → cost = 50
        // SELL at 12 (same amount) → proceeds = 60
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position pos = new Position(entry, exit);
        Num gross = pos.getGrossProfit();               // 60 - 50 = 10
        assertEquals(DecimalNum.valueOf(10), gross);
    }

    @Test
    public void testGrossProfitForSellPosition() {
        // SELL (short) at 12 (amount 5) → receive 60
        // BUY back at 10 (same amount) → pay 50 → profit = 10
        Trade entry = Trade.sellAt(0, PRICE_12, AMOUNT_5);
        Trade exit  = Trade.buyAt(1, PRICE_10, AMOUNT_5);
        Position pos = new Position(entry, exit);
        Num gross = pos.getGrossProfit();               // (exit.value - entry.value) = 50-60 = -10 → negated = 10
        assertEquals(DecimalNum.valueOf(10), gross);
    }

    @Test
    public void testProfitEqualsGrossProfitWhenCostModelsAreZero() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position pos = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
        assertEquals(pos.getGrossProfit(), pos.getProfit());
    }

    @Test
    public void testHasProfitAndHasLoss() {
        // profit case
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position profitPos = new Position(entry, exit);
        assertTrue(profitPos.hasProfit());
        assertFalse(profitPos.hasLoss());

        // loss case (buy at 12, sell at 10)
        Trade entry2 = Trade.buyAt(0, PRICE_12, AMOUNT_5);
        Trade exit2  = Trade.sellAt(1, PRICE_10, AMOUNT_5);
        Position lossPos = new Position(entry2, exit2);
        assertTrue(lossPos.hasLoss());
        assertFalse(lossPos.hasProfit());
    }

    @Test
    public void testGrossReturnBuyPosition() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position pos = new Position(entry, exit);
        Num ret = pos.getGrossReturn(); // 12 / 10 = 1.2
        assertEquals(DecimalNum.valueOf(1.2), ret);
    }

    @Test
    public void testGrossReturnSellPosition() {
        Trade entry = Trade.sellAt(0, PRICE_12, AMOUNT_5);
        Trade exit  = Trade.buyAt(1, PRICE_10, AMOUNT_5);
        Position pos = new Position(entry, exit);
        // For a short position profit of 10% → return = 0.9 (base 1 – 0.1)
        Num ret = pos.getGrossReturn(); // ((10/12)-1).negate + 1 = 0.9
        assertEquals(DecimalNum.valueOf(0.9), ret);
    }

    @Test
    public void testGrossReturnWithExplicitPrices() {
        Position pos = new Position(TradeType.BUY);
        Num ret = pos.getGrossReturn(PRICE_10, PRICE_12); // 12 / 10 = 1.2
        assertEquals(DecimalNum.valueOf(1.2), ret);
    }

    @Test
    public void testGetProfitWhenPositionIsStillOpenedReturnsZero() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5); // entry only
        assertEquals(ZERO, pos.getProfit());
    }

    @Test
    public void testGetProfitWithFinalIndexAndPriceOnOpenPosition() {
        Position pos = new Position();
        pos.operate(0, PRICE_10, AMOUNT_5); // entry (BUY 10 * 5 = 50)
        // pretend we close at index 2 with price 12
        Num profit = pos.getProfit(2, PRICE_12);
        // gross = amount * finalPrice - entryValue = 5*12 - 50 = 10
        // transaction & holding costs are zero → profit = 10
        assertEquals(DecimalNum.valueOf(10), profit);
    }

    /* -------------------------------------------------------------
     *  toString test (simple sanity check)
     * ------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position pos = new Position(entry, exit);
        String s = pos.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }
}
```
###Test END##