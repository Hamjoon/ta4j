package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * JUnit‑4 test suite for {@link Position}.
 *
 * The test class follows the required steps:
 * <ol>
 *   <li>List all public methods.</li>
 *   <li>Provide a basic test for each method.</li>
 *   <li>Identify edge‑cases / exception scenarios.</li>
 *   <li>Write dedicated tests for those edge‑cases.</li>
 *   <li>Combine everything into a single test file.</li>
 * </ol>
 *
 * Note: The library supplies concrete implementations of {@link Num}
 * (e.g. {@link DecimalNum}) and a {@link ZeroCostModel} that always returns zero.
 * These are used to build real {@link Trade} objects without any mocking.
 */
public class PositionTest {

    /* --------------------------------------------------------------------- *
     *  1.  Public method signatures of {@link Position}
     * --------------------------------------------------------------------- *
     *
     * Constructors
     *   Position()
     *   Position(TradeType startingType)
     *   Position(TradeType startingType, CostModel transactionCostModel, CostModel holdingCostModel)
     *   Position(Trade entry, Trade exit)
     *   Position(Trade entry, Trade exit, CostModel transactionCostModel, CostModel holdingCostModel)
     *
     * Business methods
     *   Trade getEntry()
     *   Trade getExit()
     *   Trade operate(int index)
     *   Trade operate(int index, Num price, Num amount)
     *   boolean isClosed()
     *   boolean isOpened()
     *   boolean isNew()
     *   boolean hasProfit()
     *   boolean hasLoss()
     *   Num getProfit()
     *   Num getProfit(int finalIndex, Num finalPrice)
     *   Num getGrossProfit()
     *   Num getGrossProfit(Num finalPrice)
     *   Num getGrossReturn()
     *   Num getGrossReturn(Num finalPrice)
     *   Num getGrossReturn(BarSeries barSeries)
     *   Num getGrossReturn(Num entryPrice, Num exitPrice)
     *   Num getPositionCost(int finalIndex)
     *   Num getPositionCost()
     *   Num getHoldingCost()
     *   Num getHoldingCost(int finalIndex)
     *   TradeType getStartingType()
     *   String toString()
     *
     * --------------------------------------------------------------------- */

    private static final CostModel ZERO_COST = new ZeroCostModel();

    private Num price(int value) {
        return DecimalNum.valueOf(value);
    }

    private Num amount(int value) {
        return DecimalNum.valueOf(value);
    }

    /* --------------------------------------------------------------------- *
     *  2.  Basic tests for each public method
     * --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorUsesBuyAsStartingType() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithCostModels() {
        Position p = new Position(TradeType.BUY, ZERO_COST, ZERO_COST);
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Trade entry = Trade.buyAt(0, price(10), amount(1), ZERO_COST);
        Trade exit = Trade.sellAt(1, price(12), amount(1), ZERO_COST);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
        assertFalse(p.isNew());
        assertTrue(p.isClosed());
    }

    @Test
    public void testGetEntryAndGetExitOnNewPosition() {
        Position p = new Position();
        assertNull(p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void testOperateCreatesEntryWhenPositionIsNew() {
        Position p = new Position();
        Trade created = p.operate(0, price(10), amount(2));
        assertNotNull(created);
        assertEquals(TradeType.BUY, created.getType());
        assertEquals(created, p.getEntry());
        assertNull(p.getExit());
        assertTrue(p.isOpened());
    }

    @Test
    public void testOperateCreatesExitWhenPositionIsOpened() {
        Position p = new Position();
        p.operate(0, price(10), amount(2)); // entry
        Trade exit = p.operate(1, price(12), amount(2));
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test
    public void testOperateWithoutPriceAmountUsesNaN() {
        Position p = new Position();
        Trade entry = p.operate(0);
        assertNotNull(entry);
        // NaN values are accepted – we only verify that a trade was created
        assertEquals(TradeType.BUY, entry.getType());
    }

    @Test
    public void testIsNewOpenedClosedTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0, price(10), amount(1));
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(1, price(12), amount(1));
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    @Test
    public void testHasProfitAndHasLossOnClosedLongPosition() {
        Position p = new Position();
        p.operate(0, price(10), amount(1)); // entry BUY @10
        p.operate(1, price(15), amount(1)); // exit SELL @15
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testHasProfitAndHasLossOnClosedShortPosition() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, price(15), amount(1)); // entry SELL @15
        p.operate(1, price(10), amount(1)); // exit BUY @10
        assertTrue(p.hasProfit()); // profit because price fell
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGetProfitClosedLong() {
        Position p = new Position();
        p.operate(0, price(10), amount(2)); // entry BUY 2 @10 = -20
        p.operate(1, price(13), amount(2)); // exit SELL 2 @13 = +26
        // profit = 26 - 20 = 6 (ZeroCostModel => no extra cost)
        assertEquals(DecimalNum.valueOf(6), p.getProfit());
    }

    @Test
    public void testGetProfitClosedShort() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, price(20), amount(3)); // entry SELL 3 @20 = +60 (cash received)
        p.operate(1, price(15), amount(3)); // exit BUY 3 @15 = -45 (cost)
        // net profit = 60 - 45 = 15
        assertEquals(DecimalNum.valueOf(15), p.getProfit());
    }

    @Test
    public void testGetProfitOpenPositionReturnsZero() {
        Position p = new Position();
        p.operate(0, price(10), amount(1));
        assertEquals(DecimalNum.valueOf(0), p.getProfit());
    }

    @Test
    public void testGetGrossProfitClosedLong() {
        Position p = new Position();
        p.operate(0, price(5), amount(4));   // entry -20
        p.operate(1, price(8), amount(4));   // exit +32
        // gross profit = 32 - 20 = 12
        assertEquals(DecimalNum.valueOf(12), p.getGrossProfit());
    }

    @Test
    public void testGetGrossProfitClosedShort() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, price(30), amount(2)); // entry +60
        p.operate(1, price(25), amount(2)); // exit -50
        // gross profit = -(50 - 60) = 10  (method negates for short)
        assertEquals(DecimalNum.valueOf(10), p.getGrossProfit());
    }

    @Test
    public void testGetGrossReturnLong() {
        Position p = new Position();
        p.operate(0, price(100), amount(1));
        p.operate(1, price(110), amount(1));
        // return = 110 / 100 = 1.1
        assertEquals(DecimalNum.valueOf(1.1), p.getGrossReturn());
    }

    @Test
    public void testGetGrossReturnShort() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, price(200), amount(1));
        p.operate(1, price(180), amount(1));
        // For short: ((exit/entry - 1) * -1) + 1
        // exit/entry = 0.9 ; 0.9-1 = -0.1 ; -(-0.1) = 0.1 ; +1 = 1.1
        assertEquals(DecimalNum.valueOf(1.1), p.getGrossReturn());
    }

    @Test
    public void testGetGrossReturnWithExplicitPrices() {
        Position p = new Position();
        p.operate(0, price(50), amount(1));
        p.operate(1, price(55), amount(1));
        assertEquals(DecimalNum.valueOf(1.1), p.getGrossReturn(price(55)));
        // Using the two‑price overload directly
        assertEquals(DecimalNum.valueOf(1.1), p.getGrossReturn(price(50), price(55)));
    }

    @Test
    public void testGetPositionCostIsZeroWithZeroCostModel() {
        Position p = new Position();
        p.operate(0, price(10), amount(1));
        p.operate(1, price(12), amount(1));
        assertEquals(DecimalNum.valueOf(0), p.getPositionCost());
        assertEquals(DecimalNum.valueOf(0), p.getPositionCost(1));
    }

    @Test
    public void testGetHoldingCostDelegatesToModel() {
        // ZeroCostModel returns zero; we simply verify the call path.
        Position p = new Position();
        p.operate(0, price(10), amount(1));
        p.operate(1, price(12), amount(1));
        assertEquals(DecimalNum.valueOf(0), p.getHoldingCost());
        assertEquals(DecimalNum.valueOf(0), p.getHoldingCost(1));
    }

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position();
        Position p2 = new Position();
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        p1.operate(0, price(10), amount(1));
        assertNotEquals(p1, p2);
        p2.operate(0, price(10), amount(1));
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        p1.operate(1, price(12), amount(1));
        p2.operate(1, price(12), amount(1));
        assertEquals(p1, p2);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position();
        p.operate(0, price(10), amount(1));
        p.operate(1, price(12), amount(1));
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
    }

    /* --------------------------------------------------------------------- *
     *  3.  Edge‑case / exception scenarios
     * --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsEntryAndExitWithSameType() {
        Trade entry = Trade.buyAt(0, price(10), amount(1));
        Trade exit = Trade.buyAt(1, price(12), amount(1));
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsMismatchedCostModels() {
        Trade entry = Trade.buyAt(0, price(10), amount(1), ZERO_COST);
        // use a different (non‑zero) cost model for exit
        CostModel other = new CostModel() {
            public Num calculate(Position position, int finalIndex) { return DecimalNum.valueOf(0); }
            public Num calculate(Position position) { return DecimalNum.valueOf(0); }
            public Num calculate(Num price, Num amount) { return DecimalNum.valueOf(0); }
            public boolean equals(CostModel otherModel) { return false; }
        };
        Trade exit = Trade.sellAt(1, price(12), amount(1), other);
        new Position(entry, exit, ZERO_COST, ZERO_COST);
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position p = new Position();
        p.operate(5, price(10), amount(1)); // entry at index 5
        // now attempt to create exit with earlier index
        p.operate(3, price(12), amount(1));
    }

    /* --------------------------------------------------------------------- *
     *  4.  Additional tests for edge cases already covered above
     * --------------------------------------------------------------------- */

    @Test
    public void testHasProfitAndHasLossWhenPositionIsOpen() {
        Position p = new Position();
        p.operate(0, price(10), amount(1));
        // open position => profit = 0, both flags should be false
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGetProfitWithExplicitFinalIndexAndPriceOnOpenPosition() {
        Position p = new Position();
        p.operate(0, price(10), amount(2)); // entry BUY 2 @10 = -20
        // pretend we close at index 5 with price 12
        Num profit = p.getProfit(5, price(12));
        // gross profit = 2*12 - 20 = 4 ; transaction cost = 0 ; holding cost = 0
        assertEquals(DecimalNum.valueOf(4), profit);
    }

    @Test
    public void testGetGrossReturnWithBarSeriesUsesPricesFromSeries() {
        // Minimal BarSeries implementation provided by the library – we use a
        // real series with two bars so that getPricePerAsset(barSeries) works.
        BarSeries series = new org.ta4j.core.BaseBarSeriesBuilder().withName("test").build();
        series.addBar(DecimalNum.valueOf(10), DecimalNum.valueOf(10), DecimalNum.valueOf(10),
                DecimalNum.valueOf(10), DecimalNum.valueOf(10));
        series.addBar(DecimalNum.valueOf(12), DecimalNum.valueOf(12), DecimalNum.valueOf(12),
                DecimalNum.valueOf(12), DecimalNum.valueOf(12));

        Trade entry = Trade.buyAt(0, series, DecimalNum.valueOf(1));
        Trade exit = Trade.sellAt(1, series, DecimalNum.valueOf(1));
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);

        Num grossReturn = p.getGrossReturn(series);
        // exit/entry = 12/10 = 1.2
        assertEquals(DecimalNum.valueOf(1.2), grossReturn);
    }
}
