###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests are written by three “experts”.  Each expert contributed at least
 * one test case for every public method, covering:
 *
 * <ul>
 *   <li>normal usage (happy path)</li>
 *   <li>edge cases (null entry, opened vs closed position)</li>
 *   <li>error scenarios (illegal arguments, index ordering)</li>
 * </ul>
 *
 * No external mocking frameworks are used – only the public API that appears
 * in the source code or the dependency signatures is exercised.
 */
public class PositionTest {

    private CostModel zeroCostModel;

    @Before
    public void setUp() {
        zeroCostModel = new ZeroCostModel(); // always returns Num zero
    }

    /* --------------------------------------------------------------------- *
     *  Construction & basic getters
     * --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructor_hasBuyStartingType() {
        Position p = new Position(); // default BUY
        assertEquals(Trade.TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
        assertNull(p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void testConstructorWithStartingType_sell() {
        Position p = new Position(Trade.TradeType.SELL);
        assertEquals(Trade.TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testSideAndAmountWhenNoEntry() {
        Position p = new Position();
        assertNull(p.side());
        assertNull(p.amount());
        assertNull(p.averageEntryPrice());
        assertNull(p.totalEntryCost());
        assertNull(p.totalFees());
    }

    /* --------------------------------------------------------------------- *
     *  operate – creation of entry and exit trades
     * --------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryTrade() {
        Position p = new Position();                     // new position
        Trade entry = p.operate(0);                       // price/amount = NaN
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        assertEquals(Trade.TradeType.BUY, entry.getType());
        assertSame(entry, p.getEntry());
        assertTrue(p.isOpened());
    }

    @Test
    public void testOperateCreatesExitTrade() {
        Position p = new Position();                     // new position
        p.operate(0);                                     // entry
        Trade exit = p.operate(5);                         // exit trade
        assertNotNull(exit);
        assertEquals(5, exit.getIndex());
        assertEquals(Trade.TradeType.SELL, exit.getType());
        assertSame(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexLowerThanEntryThrows() {
        Position p = new Position(); // new
        p.operate(10);               // entry at index 10
        p.operate(5);                // attempt exit before entry -> exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateWithMismatchedCostModelThrows() {
        // Build a trade with a different cost model (still ZeroCostModel but a new instance)
        CostModel otherModel = new ZeroCostModel();
        Trade entry = Trade.buyAt(0, NaN, NaN, otherModel);
        Position p = new Position(); // uses its own internal ZeroCostModel
        p.operate(entry);            // should fail because models differ (instance check)
    }

    /* --------------------------------------------------------------------- *
     *  State query methods
     * --------------------------------------------------------------------- */

    @Test
    public void testStateTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0); // entry
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(1); // exit
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position();
        Position p2 = new Position();

        // both new -> equal
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // add entry to p1 only
        p1.operate(0);
        assertNotEquals(p1, p2);

        // make both have same entry
        p2.operate(0);
        assertEquals(p1, p2);

        // close both
        p1.operate(5);
        p2.operate(5);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    /* --------------------------------------------------------------------- *
     *  Profit / loss related methods (using NaN trades – result should be NaN)
     * --------------------------------------------------------------------- */

    @Test
    public void testProfitForOpenedPositionIsZero() {
        Position p = new Position();
        p.operate(0); // entry
        Num profit = p.getProfit();
        assertTrue(profit.isZero()); // zero() comes from entry's NumFactory
    }

    @Test
    public void testProfitForClosedPositionIsNaN() {
        Position p = new Position();
        p.operate(0); // entry (NaN price/amount)
        p.operate(1); // exit (NaN)
        Num profit = p.getProfit();
        assertTrue(profit.isNaN());
    }

    @Test
    public void testHasProfitAndHasLossWithNaNProfit() {
        Position p = new Position();
        p.operate(0);
        p.operate(1);
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGrossReturnForOpenedPositionIsZero() {
        Position p = new Position();
        p.operate(0);
        assertTrue(p.getGrossReturn().isZero());
    }

    @Test
    public void testGrossReturnForClosedPositionIsNaN() {
        Position p = new Position();
        p.operate(0);
        p.operate(1);
        assertTrue(p.getGrossReturn().isNaN());
    }

    /* --------------------------------------------------------------------- *
     *  Cost model accessors (defaulting to ZeroCostModel after deserialization)
     * --------------------------------------------------------------------- */

    @Test
    public void testGetTransactionCostModelDefaultsToZeroCostModel() {
        Position p = new Position(); // internal model is ZeroCostModel
        CostModel cm = p.getTransactionCostModel();
        assertTrue(cm instanceof ZeroCostModel);
    }

    @Test
    public void testGetHoldingCostModelDefaultsToZeroCostModel() {
        Position p = new Position();
        CostModel cm = p.getHoldingCostModel();
        assertTrue(cm instanceof ZeroCostModel);
    }

    /* --------------------------------------------------------------------- *
     *  toString representation
     * --------------------------------------------------------------------- */

    @Test
    public void testToStringShowsEntryAndExit() {
        Position p = new Position();
        p.operate(0); // entry
        String s1 = p.toString();
        assertTrue(s1.contains("Entry:"));
        assertTrue(s1.contains("exit: null"));

        p.operate(5); // exit
        String s2 = p.toString();
        assertTrue(s2.contains("Entry:"));
        assertTrue(s2.contains("exit:"));
    }

    /* --------------------------------------------------------------------- *
     *  Miscellaneous getters after entry is set
     * --------------------------------------------------------------------- */

    @Test
    public void testGettersAfterEntry() {
        Position p = new Position();
        p.operate(0); // entry with NaN values

        assertNotNull(p.side());
        assertEquals(ExecutionSide.BUY, p.side());

        assertNotNull(p.amount());
        assertNotNull(p.averageEntryPrice());
        assertNotNull(p.totalEntryCost());
        assertNotNull(p.totalFees());
    }
}
```
###Test END##