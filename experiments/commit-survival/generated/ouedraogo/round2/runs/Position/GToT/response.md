###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests are organised as follows:
 *
 * <ul>
 *   <li>Extraction of all public methods (see the comment block at the top of the file).</li>
 *   <li>Basic functionality tests for each public method.</li>
 *   <li>Edge‑case / exception tests (null arguments, illegal state, mismatched cost models, etc.).</li>
 *   <li>Combined test class {@code PositionTest} that can be executed directly.</li>
 * </ul>
 *
 * All objects are created using the public API of the library (no mocking, no subclassing of
 * library interfaces).  The concrete numeric implementation used is {@code DecimalNum},
 * which is the standard implementation shipped with ta4j.
 */
public class PositionTest {

    /* -----------------------------------------------------------------------
     *  List of public methods of Position (extracted from the source)
     *
     *  Constructors
     *  -----------------------------------------------------------------------
     *  Position()
     *  Position(TradeType startingType)
     *  Position(TradeType startingType, CostModel transactionCostModel, CostModel holdingCostModel)
     *  Position(Trade entry, Trade exit)
     *  Position(Trade entry, Trade exit, CostModel transactionCostModel, CostModel holdingCostModel)
     *
     *  Accessors / state query
     *  -----------------------------------------------------------------------
     *  Trade getEntry()
     *  Trade getExit()
     *  boolean equals(Object obj)
     *  int hashCode()
     *  Trade operate(int index)
     *  Trade operate(int index, Num price, Num amount)
     *  boolean isClosed()
     *  boolean isOpened()
     *  boolean isNew()
     *  boolean hasProfit()
     *  boolean hasLoss()
     *  Num getProfit()
     *  Num getProfit(int finalIndex, Num finalPrice)
     *  Num getGrossProfit()
     *  Num getGrossProfit(Num finalPrice)
     *  Num getGrossReturn()
     *  Num getGrossReturn(Num finalPrice)
     *  Num getGrossReturn(BarSeries barSeries)
     *  Num getGrossReturn(Num entryPrice, Num exitPrice)
     *  Num getPositionCost(int finalIndex)
     *  Num getPositionCost()
     *  Num getHoldingCost()
     *  Num getHoldingCost(int finalIndex)
     *  TradeType getStartingType()
     * ----------------------------------------------------------------------- */

    /* -----------------------------------------------------------------------
     *  Helper methods used by several tests
     * ----------------------------------------------------------------------- */

    private static final CostModel ZERO_COST = new ZeroCostModel();

    /** Creates a BUY trade at the given index with the supplied price & amount. */
    private static Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, DecimalNum.valueOf(price), DecimalNum.valueOf(amount));
    }

    /** Creates a SELL trade at the given index with the supplied price & amount. */
    private static Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, DecimalNum.valueOf(price), DecimalNum.valueOf(amount));
    }

    /** Returns a Num representing zero (derived from any Num instance). */
    private static Num zero(Num any) {
        return any.zero();
    }

    /** Returns a Num representing one (derived from any Num instance). */
    private static Num one(Num any) {
        return any.one();
    }

    /* -----------------------------------------------------------------------
     *  1. Constructor validation tests (edge cases)
     * ----------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullStartingType_throws() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_sameTradeTypes_throws() {
        Trade entry = buy(0, 10, 1);
        Trade exit  = buy(1, 12, 1);               // also BUY → illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_mismatchedCostModel_throws() {
        // entry & exit use the default ZeroCostModel, we deliberately pass a different one
        CostModel otherCost = new CostModel() {
            @Override public Num calculate(Position p, int i) { return DecimalNum.ZERO; }
            @Override public Num calculate(Position p) { return DecimalNum.ZERO; }
            @Override public Num calculate(Num price, Num amount) { return DecimalNum.ZERO; }
            @Override public boolean equals(CostModel other) { return false; }
        };
        Trade entry = buy(0, 10, 1);
        Trade exit  = sell(1, 12, 1);
        new Position(entry, exit, otherCost, ZERO_COST);
    }

    /* -----------------------------------------------------------------------
     *  2. Basic state‑query tests
     * ----------------------------------------------------------------------- */

    @Test
    public void newPosition_isNew_isOpened_isClosed() {
        Position p = new Position();               // default BUY starting type
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void operate_entryCreatesTrade_andStateTransitions() {
        Position p = new Position(TradeType.BUY);
        Trade entry = p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE);
        assertNotNull(entry);
        assertEquals(TradeType.BUY, entry.getType());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        Trade exit = p.operate(1, DecimalNum.valueOf(15), DecimalNum.ONE);
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void operate_exitBeforeEntryIndex_throws() {
        Position p = new Position(TradeType.BUY);
        p.operate(5, DecimalNum.valueOf(10), DecimalNum.ONE); // creates entry at index 5
        p.operate(3, DecimalNum.valueOf(12), DecimalNum.ONE); // index < entry → exception
    }

    @Test
    public void getStartingType_returnsConstructorValue() {
        Position pBuy = new Position(TradeType.BUY);
        Position pSell = new Position(TradeType.SELL);
        assertEquals(TradeType.BUY, pBuy.getStartingType());
        assertEquals(TradeType.SELL, pSell.getStartingType());
    }

    /* -----------------------------------------------------------------------
     *  3. Profit / loss / gross return tests (closed positions)
     * ----------------------------------------------------------------------- */

    @Test
    public void profit_longPosition_positive_hasProfit() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE); // entry BUY @10
        p.operate(1, DecimalNum.valueOf(15), DecimalNum.ONE); // exit SELL @15

        Num profit = p.getProfit();
        assertEquals(DecimalNum.valueOf(5), profit);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void profit_longPosition_negative_hasLoss() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(15), DecimalNum.ONE); // entry BUY @15
        p.operate(1, DecimalNum.valueOf(10), DecimalNum.ONE); // exit SELL @10

        Num profit = p.getProfit();
        assertEquals(DecimalNum.valueOf(-5), profit);
        assertTrue(p.hasLoss());
        assertFalse(p.hasProfit());
    }

    @Test
    public void profit_shortPosition_positive() {
        // Short (SELL first, BUY later) – profit when price falls
        Position p = new Position(TradeType.SELL);
        p.operate(0, DecimalNum.valueOf(15), DecimalNum.ONE); // entry SELL @15
        p.operate(1, DecimalNum.valueOf(10), DecimalNum.ONE); // exit BUY @10

        Num profit = p.getProfit();
        assertEquals(DecimalNum.valueOf(5), profit);
        assertTrue(p.hasProfit());
    }

    @Test
    public void profit_shortPosition_negative() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE); // entry SELL @10
        p.operate(1, DecimalNum.valueOf(15), DecimalNum.ONE); // exit BUY @15

        Num profit = p.getProfit();
        assertEquals(DecimalNum.valueOf(-5), profit);
        assertTrue(p.hasLoss());
    }

    @Test
    public void grossReturn_longPosition() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE);
        p.operate(1, DecimalNum.valueOf(15), DecimalNum.ONE);
        Num grossReturn = p.getGrossReturn(); // 15 / 10 = 1.5
        assertEquals(DecimalNum.valueOf(1.5), grossReturn);
    }

    @Test
    public void grossReturn_shortPosition() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, DecimalNum.valueOf(15), DecimalNum.ONE);
        p.operate(1, DecimalNum.valueOf(10), DecimalNum.ONE);
        // For short: ((exit/entry - 1).negate) + 1 = ((10/15 -1).negate)+1 = 1.333...
        Num grossReturn = p.getGrossReturn();
        assertEquals(DecimalNum.valueOf(1.3333333333333333), grossReturn);
    }

    @Test
    public void grossReturn_openPosition_returnsZero() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE);
        // No exit yet → position opened
        Num grossReturn = p.getGrossReturn();
        assertEquals(DecimalNum.ZERO, grossReturn);
    }

    @Test
    public void getGrossReturn_fromExplicitPrices() {
        Position p = new Position();
        // Use the helper method that directly receives entry & exit prices
        Num result = p.getGrossReturn(DecimalNum.valueOf(10), DecimalNum.valueOf(15));
        assertEquals(DecimalNum.valueOf(1.5), result);
    }

    /* -----------------------------------------------------------------------
     *  4. Open‑position profit (should be zero) and cost calculations
     * ----------------------------------------------------------------------- */

    @Test
    public void profit_whenOpen_returnsZero() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE); // entry only
        assertTrue(p.isOpened());
        assertEquals(DecimalNum.ZERO, p.getProfit());
        assertEquals(DecimalNum.ZERO, p.getGrossProfit());
    }

    @Test
    public void positionCost_withZeroCostModel_isZero() {
        Position p = new Position(TradeType.BUY, ZERO_COST, ZERO_COST);
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE);
        p.operate(1, DecimalNum.valueOf(15), DecimalNum.ONE);
        assertEquals(DecimalNum.ZERO, p.getPositionCost());
        assertEquals(DecimalNum.ZERO, p.getHoldingCost());
    }

    /* -----------------------------------------------------------------------
     *  5. equals() and hashCode()
     * ----------------------------------------------------------------------- */

    @Test
    public void equalsAndHashCode_identicalPositions() {
        Trade entry1 = buy(0, 10, 1);
        Trade exit1  = sell(1, 15, 1);
        Position p1 = new Position(entry1, exit1);

        Trade entry2 = buy(0, 10, 1);
        Trade exit2  = sell(1, 15, 1);
        Position p2 = new Position(entry2, exit2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void equals_differentPositions_notEqual() {
        Position p1 = new Position();
        Position p2 = new Position(TradeType.SELL);
        assertNotEquals(p1, p2);
    }

    /* -----------------------------------------------------------------------
     *  6. getProfit(int, Num) – profit calculation for an open position
     * ----------------------------------------------------------------------- */

    @Test
    public void profitOpenPosition_withFinalValues() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE); // opened BUY @10
        // pretend the series ends at index 5 with price 12
        Num profit = p.getProfit(5, DecimalNum.valueOf(12));
        // Expected: (amount * finalPrice - entryValue) = (1*12 - 10) = 2
        assertEquals(DecimalNum.valueOf(2), profit);
    }

    /* -----------------------------------------------------------------------
     *  7. toString() sanity check (not required for coverage but useful)
     * ----------------------------------------------------------------------- */

    @Test
    public void toString_containsEntryAndExit() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.ONE);
        p.operate(1, DecimalNum.valueOf(15), DecimalNum.ONE);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
    }
}
```
###Test END##