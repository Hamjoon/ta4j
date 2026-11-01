###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests exercise:
 * <ul>
 *   <li>all public constructors</li>
 *   <li>state query methods (isNew, isOpened, isClosed, side, amount, …)</li>
 *   <li>equality & hashCode</li>
 *   <li>operate(..) overloads and the exception cases</li>
 *   <li>profit / loss calculations (gross & net, returns)</li>
 *   <li>cost model getters (should fallback to {@link ZeroCostModel})</li>
 *   <li>toString representation</li>
 * </ul>
 *
 * The library provides {@link DecimalNum} as the concrete {@link Num}
 * implementation, and {@link ZeroCostModel} as a cost model that always returns
 * zero.
 */
public class PositionTest {

    private static final CostModel ZERO_COST = new ZeroCostModel();

    private Num price10;
    private Num price12;
    private Num price8;
    private Num amount2;

    @Before
    public void setUp() {
        price10 = DecimalNum.valueOf(10);
        price12 = DecimalNum.valueOf(12);
        price8 = DecimalNum.valueOf(8);
        amount2 = DecimalNum.valueOf(2);
    }

    /* --------------------------------------------------------------------- */
    /*  Constructors                                                          */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructor() {
        Position p = new Position(); // default BUY, zero cost models
        assertTrue(p.isNew());
        assertEquals(Trade.TradeType.BUY, p.getStartingType());
        assertNotNull(p.getTransactionCostModel());
        assertNotNull(p.getHoldingCostModel());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(Trade.TradeType.SELL);
        assertTrue(p.isNew());
        assertEquals(Trade.TradeType.SELL, p.getStartingType());
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Trade entry = Trade.buyAt(0, price10, amount2, ZERO_COST);
        Trade exit = Trade.sellAt(1, price12, amount2, ZERO_COST);
        Position p = new Position(entry, exit);
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test
    public void testOpenPositionConstructor() {
        Trade entry = Trade.buyAt(0, price10, amount2, ZERO_COST);
        Position p = new Position(entry, ZERO_COST, ZERO_COST);
        assertTrue(p.isOpened());
        assertEquals(entry, p.getEntry());
        assertNull(p.getExit());
    }

    /* --------------------------------------------------------------------- */
    /*  State query methods                                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testSideAmountAndPriceAccessors() {
        Trade entry = Trade.buyAt(0, price10, amount2, ZERO_COST);
        Position p = new Position(entry, ZERO_COST, ZERO_COST);
        assertEquals(ExecutionSide.BUY, p.side());
        assertEquals(amount2, p.amount());
        assertEquals(price10, p.averageEntryPrice());
        assertEquals(entry.getValue(), p.totalEntryCost());
        assertEquals(entry.getCost(), p.totalFees());
    }

    @Test
    public void testSideWhenNoEntry() {
        Position p = new Position();
        assertNull(p.side());
        assertNull(p.amount());
        assertNull(p.averageEntryPrice());
        assertNull(p.totalEntryCost());
        assertNull(p.totalFees());
    }

    /* --------------------------------------------------------------------- */
    /*  Equality & hashCode                                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Trade e1 = Trade.buyAt(0, price10, amount2, ZERO_COST);
        Trade x1 = Trade.sellAt(1, price12, amount2, ZERO_COST);
        Position p1 = new Position(e1, x1);
        Position p2 = new Position(e1, x1);
        Position p3 = new Position(e1, ZERO_COST, ZERO_COST); // opened, not equal

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    /* --------------------------------------------------------------------- */
    /*  operate(..) – happy path                                              */
    /* --------------------------------------------------------------------- */

    @Test
    public void testOperateEntryAndExit() {
        Position p = new Position(Trade.TradeType.BUY);
        // create entry
        Trade entry = p.operate(0, price10, amount2);
        assertNotNull(entry);
        assertEquals(Trade.TradeType.BUY, entry.getType());
        assertEquals(entry, p.getEntry());
        assertTrue(p.isOpened());

        // create exit
        Trade exit = p.operate(1, price12, amount2);
        assertNotNull(exit);
        assertEquals(Trade.TradeType.SELL, exit.getType());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitWithEarlierIndexThrows() {
        Position p = new Position(Trade.TradeType.BUY);
        p.operate(5, price10, amount2); // entry at index 5
        // attempt exit at index 3 – should fail
        p.operate(3, price12, amount2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateEntryWithWrongTradeTypeThrows() {
        Position p = new Position(Trade.TradeType.SELL); // expects first trade to be SELL
        // try to add a BUY trade – illegal
        p.operate(0, price10, amount2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateExitWithMismatchedCostModelThrows() {
        // Build a position with a custom (zero) cost model
        Position p = new Position(Trade.TradeType.BUY);
        Trade entry = p.operate(0, price10, amount2);
        // Build an exit trade using a different cost model instance
        CostModel otherCost = new ZeroCostModel(); // a different instance
        Trade badExit = Trade.sellAt(1, price12, amount2, otherCost);
        // Directly invoke the overload that accepts a Trade – should reject
        p.operate(badExit);
    }

    /* --------------------------------------------------------------------- */
    /*  Profit / loss calculations                                            */
    /* --------------------------------------------------------------------- */

    @Test
    public void testProfitAndLossForLongPosition() {
        Trade entry = Trade.buyAt(0, price10, amount2, ZERO_COST);   // cost = 20
        Trade exit = Trade.sellAt(1, price12, amount2, ZERO_COST);  // proceeds = 24
        Position p = new Position(entry, exit);
        Num profit = p.getProfit(); // (12*2 - 20) = 4
        assertEquals(DecimalNum.valueOf(4), profit);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());

        // gross profit is the same because no costs
        assertEquals(profit, p.getGrossProfit());

        // gross return = exit/entry = 12/10 = 1.2
        Num grossReturn = p.getGrossReturn();
        assertEquals(DecimalNum.valueOf(1.2), grossReturn);
    }

    @Test
    public void testProfitAndLossForShortPosition() {
        Trade entry = Trade.sellAt(0, price12, amount2, ZERO_COST); // receive 24
        Trade exit = Trade.buyAt(1, price10, amount2, ZERO_COST);   // pay 20
        Position p = new Position(entry, exit);
        Num profit = p.getProfit(); // (24 - 10*2) = 4
        assertEquals(DecimalNum.valueOf(4), profit);
        assertTrue(p.hasProfit());

        // gross return for short: 1 - (exit/entry - 1) = 1 - (10/12 -1) = 1 - (-0.1666..) = 1.1666..
        Num expectedReturn = DecimalNum.valueOf(1).minus(
                DecimalNum.valueOf(10).dividedBy(DecimalNum.valueOf(12)).minus(DecimalNum.valueOf(1)).negate()
        );
        // Simpler: using Position's method
        Num grossReturn = p.getGrossReturn();
        assertEquals(DecimalNum.valueOf(0.8333333333333334).negate().plus(DecimalNum.valueOf(1)), grossReturn);
    }

    @Test
    public void testZeroProfitForOpenPosition() {
        Trade entry = Trade.buyAt(0, price10, amount2, ZERO_COST);
        Position p = new Position(entry, ZERO_COST, ZERO_COST);
        assertTrue(p.isOpened());
        assertEquals(DecimalNum.valueOf(0), p.getProfit());
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
        assertEquals(DecimalNum.valueOf(0), p.getGrossProfit());
        assertEquals(DecimalNum.valueOf(0), p.getGrossReturn());
    }

    /* --------------------------------------------------------------------- */
    /*  Cost model getters (fallback to ZeroCostModel)                        */
    /* --------------------------------------------------------------------- */

    @Test
    public void testCostModelFallbackAfterDeserialization() throws Exception {
        // Simulate a Position created with the default constructor (cost models null after deserialization)
        Position p = new Position();
        // Use reflection to nullify the transient fields (as would happen after deserialization)
        java.lang.reflect.Field txnField = Position.class.getDeclaredField("transactionCostModel");
        txnField.setAccessible(true);
        txnField.set(p, null);
        java.lang.reflect.Field holdField = Position.class.getDeclaredField("holdingCostModel");
        holdField.setAccessible(true);
        holdField.set(p, null);

        // Both getters must return a ZeroCostModel instance
        assertTrue(p.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(p.getHoldingCostModel() instanceof ZeroCostModel);
        // Position cost should be zero
        assertEquals(DecimalNum.valueOf(0), p.getPositionCost());
    }

    /* --------------------------------------------------------------------- */
    /*  toString representation                                               */
    /* --------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0, price8, amount2, ZERO_COST);
        Trade exit = Trade.sellAt(1, price10, amount2, ZERO_COST);
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(exit.toString()));
    }

    /* --------------------------------------------------------------------- */
    /*  Helper methods (none needed – using library factories directly)      */
    /* --------------------------------------------------------------------- */
}
```
###Test END##