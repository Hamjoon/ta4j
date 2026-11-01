###Test START##
package org.ta4j.core;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests focus on:
 * <ul>
 *   <li>state transitions (new → opened → closed)</li>
 *   <li>behaviour of {@code operate(...)} including illegal index handling</li>
 *   <li>equality, hashCode and toString contracts</li>
 *   <li>profit / loss helpers when the position is opened or closed</li>
 *   <li>gross profit / gross return calculations (using NaN values – the
 *       {@link ZeroCostModel} guarantees a zero cost)</li>
 * </ul>
 *
 * <p>All numeric values are supplied via {@code NaN.NaN} which is a valid
 * {@link Num} instance in the library.  The {@link ZeroCostModel} guarantees
 * that any cost‑related method returns a zero {@link Num}, allowing us to
 * assert results with {@code isZero()} without needing a concrete numeric
 * implementation.</p>
 */
public class PositionTest {

    private Position positionBuy;
    private Position positionSell;

    @Before
    public void setUp() {
        // default constructor uses BUY as starting type
        positionBuy = new Position();
        // explicit SELL starting type
        positionSell = new Position(Trade.TradeType.SELL);
    }

    /* ----------------------------------------------------------------------
     *  State checks for a freshly created position
     * ---------------------------------------------------------------------- */
    @Test
    public void testNewPositionState() {
        assertTrue("New position should be new", positionBuy.isNew());
        assertFalse("New position should not be opened", positionBuy.isOpened());
        assertFalse("New position should not be closed", positionBuy.isClosed());

        assertTrue("New position should be new (SELL start)", positionSell.isNew());
    }

    /* ----------------------------------------------------------------------
     *  operate() – creating entry and exit trades
     * ---------------------------------------------------------------------- */
    @Test
    public void testOperateCreatesEntryAndExit() {
        // first call creates entry trade
        Trade entry = positionBuy.operate(0, NaN, NaN);
        assertNotNull("Entry trade must be created", entry);
        assertEquals("Entry type must match starting type (BUY)", Trade.TradeType.BUY, entry.getType());
        assertTrue("After entry, position must be opened", positionBuy.isOpened());
        assertFalse("After entry, position must not be closed", positionBuy.isClosed());

        // second call creates exit trade
        Trade exit = positionBuy.operate(5, NaN, NaN);
        assertNotNull("Exit trade must be created", exit);
        assertEquals("Exit type must be complement of entry (SELL)", Trade.TradeType.SELL, exit.getType());
        assertTrue("After exit, position must be closed", positionBuy.isClosed());
        assertFalse("After exit, position must not be opened", positionBuy.isOpened());

        // verify indices are stored correctly
        assertEquals("Entry index must be 0", 0, entry.getIndex());
        assertEquals("Exit index must be 5", 5, exit.getIndex());
    }

    @Test
    public void testOperateCreatesEntryAndExitForSellStart() {
        Trade entry = positionSell.operate(2, NaN, NaN);
        assertEquals("Entry type must match starting type (SELL)", Trade.TradeType.SELL, entry.getType());

        Trade exit = positionSell.operate(7, NaN, NaN);
        assertEquals("Exit type must be complement (BUY)", Trade.TradeType.BUY, exit.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexLessThanEntry() {
        positionBuy.operate(10, NaN, NaN); // creates entry at index 10
        // try to create exit with an earlier index – should throw
        positionBuy.operate(5, NaN, NaN);
    }

    /* ----------------------------------------------------------------------
     *  Equality, hashCode and toString
     * ---------------------------------------------------------------------- */
    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position();
        Position p2 = new Position();

        // both are new – should be equal
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // after operating on p1, they diverge
        p1.operate(0, NaN, NaN);
        assertNotEquals(p1, p2);
        assertNotEquals(p1.hashCode(), p2.hashCode());

        // make p2 identical by operating with same parameters
        p2.operate(0, NaN, NaN);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // close both positions
        p1.operate(5, NaN, NaN);
        p2.operate(5, NaN, NaN);
        assertEquals(p1, p2);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        positionBuy.operate(1, NaN, NaN);
        positionBuy.operate(3, NaN, NaN);
        String str = positionBuy.toString();
        assertTrue("toString should contain 'Entry:'", str.contains("Entry:"));
        assertTrue("toString should contain 'exit:'", str.contains("exit:"));
    }

    /* ----------------------------------------------------------------------
     *  Profit / loss helpers – using ZeroCostModel guarantees zero cost,
     *  therefore profit for a closed position built with NaN values is zero.
     * ---------------------------------------------------------------------- */
    @Test
    public void testProfitAndLossOnOpenedPosition() {
        positionBuy.operate(0, NaN, NaN); // open only
        Num profit = positionBuy.getProfit();
        assertTrue("Profit of an opened position must be zero", profit.isZero());
        assertFalse("hasProfit should be false for opened position", positionBuy.hasProfit());
        assertFalse("hasLoss should be false for opened position", positionBuy.hasLoss());
    }

    @Test
    public void testProfitAndLossOnClosedPosition() {
        positionBuy.operate(0, NaN, NaN); // entry
        positionBuy.operate(4, NaN, NaN); // exit
        Num profit = positionBuy.getProfit();
        assertTrue("Profit of a closed position with ZeroCostModel should be zero", profit.isZero());
        assertFalse("hasProfit should be false for zero profit", positionBuy.hasProfit());
        assertFalse("hasLoss should be false for zero profit", positionBuy.hasLoss());
    }

    /* ----------------------------------------------------------------------
     *  Gross profit / gross return – with NaN prices the calculations resolve
     *  to zero (or one for return) because NaN propagates through arithmetic.
     * ---------------------------------------------------------------------- */
    @Test
    public void testGrossProfitAndReturn() {
        positionBuy.operate(0, NaN, NaN); // entry
        positionBuy.operate(8, NaN, NaN); // exit

        Num grossProfit = positionBuy.getGrossProfit();
        assertTrue("Gross profit should be zero with NaN values", grossProfit.isZero());

        Num grossReturn = positionBuy.getGrossReturn();
        // For a zero‑profit position the gross return should be 1 (base included)
        assertTrue("Gross return should be one for zero profit", grossReturn.isEqual(grossReturn.getNumFactory().one()));
    }

    /* ----------------------------------------------------------------------
     *  Position cost – ZeroCostModel guarantees zero cost
     * ---------------------------------------------------------------------- */
    @Test
    public void testPositionCostIsZero() {
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        pos.operate(0, NaN, NaN); // entry
        pos.operate(3, NaN, NaN); // exit

        Num cost = pos.getPositionCost();
        assertTrue("Total position cost must be zero with ZeroCostModel", cost.isZero());

        Num costWithIndex = pos.getPositionCost(3);
        assertTrue("Total position cost with index must be zero", costWithIndex.isZero());
    }

    /* ----------------------------------------------------------------------
     *  Starting type accessor
     * ---------------------------------------------------------------------- */
    @Test
    public void testGetStartingType() {
        assertEquals("Default constructor should start with BUY", Trade.TradeType.BUY,
                positionBuy.getStartingType());
        assertEquals("Constructor with SELL should start with SELL", Trade.TradeType.SELL,
                positionSell.getStartingType());
    }
}
###Test END##