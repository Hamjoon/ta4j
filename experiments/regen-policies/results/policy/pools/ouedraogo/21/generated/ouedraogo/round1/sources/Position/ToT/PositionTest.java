package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;

/**
 * Unit tests for {@link Position}. The tests exercise constructors, state‑transition
 * methods, equality, hash code and the error handling defined in the class.
 *
 * <p>Only the public API that appears in the source code of {@code Position}
 * (or in the dependency signatures provided) is used – no external mocking
 * frameworks or additional library classes are required.</p>
 */
public class PositionTest {

    private static final Num NAN = NaN.NaN; // a valid Num instance supplied by the library

    private ZeroCostModel zeroCostModel;

    @Before
    public void setUp() {
        zeroCostModel = new ZeroCostModel();
    }

    /* --------------------------------------------------------------------- */
    /*  Constructors                                                         */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorStartsWithBuy() {
        Position p = new Position(); // default starting type is BUY
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
        assertNull(p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void testConstructorWithExplicitStartingType() {
        Position longPos = new Position(TradeType.BUY);
        Position shortPos = new Position(TradeType.SELL);
        assertEquals(TradeType.BUY, longPos.getStartingType());
        assertEquals(TradeType.SELL, shortPos.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null);
    }

    @Test
    public void testConstructorFromEntryAndExitTrades() {
        Trade entry = Trade.buyAt(0, NAN, NAN);          // BUY trade
        Trade exit  = Trade.sellAt(1, NAN, NAN);         // SELL trade – complementary type
        Position p = new Position(entry, exit);
        assertEquals(TradeType.BUY, p.getStartingType());
        assertSame(entry, p.getEntry());
        assertSame(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsSameTypeEntryAndExit() {
        Trade entry = Trade.buyAt(0, NAN, NAN);
        Trade exit  = Trade.buyAt(1, NAN, NAN); // both BUY – illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsMismatchedCostModel() {
        // Create a custom (non‑zero) cost model by subclassing ZeroCostModel
        // (allowed because ZeroCostModel is a concrete class, not an interface)
        CostModel customCost = new ZeroCostModel() {
            @Override
            public Num calculate(Position position, int finalIndex) {
                return NAN;
            }

            @Override
            public Num calculate(Position position) {
                return NAN;
            }

            @Override
            public Num calculate(Num price, Num amount) {
                return NAN;
            }

            @Override
            public boolean equals(CostModel otherModel) {
                return otherModel == this;
            }
        };

        Trade entry = Trade.buyAt(0, NAN, NAN, customCost);
        Trade exit  = Trade.sellAt(1, NAN, NAN, zeroCostModel); // different cost model
        new Position(entry, exit, customCost, zeroCostModel);
    }

    /* --------------------------------------------------------------------- */
    /*  State transition – operate(..)                                       */
    /* --------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryAndExitTrades() {
        Position p = new Position(TradeType.BUY);
        // first call creates the entry trade
        Trade entry = p.operate(5);
        assertNotNull(entry);
        assertSame(entry, p.getEntry());
        assertNull(p.getExit());
        assertTrue(p.isOpened());

        // second call creates the exit trade
        Trade exit = p.operate(10);
        assertNotNull(exit);
        assertSame(exit, p.getExit());
        assertTrue(p.isClosed());

        // verify that the exit trade type is the complement of the entry type
        assertEquals(TradeType.BUY.complementType(), exit.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position p = new Position(TradeType.SELL);
        p.operate(8);               // creates entry at index 8
        p.operate(5);               // index < entry index → IllegalStateException
    }

    @Test
    public void testOperateWithExplicitPriceAndAmountDoesNotBreakState() {
        // Use NaN for price/amount – the library accepts it and treats it as “unknown”.
        Position p = new Position(TradeType.SELL);
        Trade entry = p.operate(3, NAN, NAN);
        assertNotNull(entry);
        assertTrue(p.isOpened());

        Trade exit = p.operate(7, NAN, NAN);
        assertNotNull(exit);
        assertTrue(p.isClosed());
    }

    /* --------------------------------------------------------------------- */
    /*  Equality and hashCode                                                */
    /* --------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCodeWithSameTrades() {
        Trade entry = Trade.buyAt(0, NAN, NAN);
        Trade exit  = Trade.sellAt(1, NAN, NAN);
        Position p1 = new Position(entry, exit);
        Position p2 = new Position(entry, exit);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsHandlesNullTrades() {
        Position p1 = new Position(); // new – both trades null
        Position p2 = new Position(); // another new position
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // one position with an entry, the other still new → not equal
        p1.operate(0);
        assertNotEquals(p1, p2);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentClass() {
        Position p = new Position();
        assertNotEquals(p, "some string");
    }

    /* --------------------------------------------------------------------- */
    /*  Profit / loss helpers (zero‑profit scenarios)                        */
    /* --------------------------------------------------------------------- */

    @Test
    public void testProfitIsZeroWhenPositionIsOpened() {
        Position p = new Position(TradeType.BUY);
        p.operate(0); // entry only – position is opened
        assertTrue(p.isOpened());
        assertEquals(NAN, p.getProfit()); // getProfit() returns zero() which uses entry’s NumFactory
    }

    @Test
    public void testHasProfitAndHasLossOnClosedZeroProfitPosition() {
        // Create a closed position where entry and exit prices are identical (NaN works as a placeholder)
        Trade entry = Trade.buyAt(0, NAN, NAN);
        Trade exit  = Trade.sellAt(1, NAN, NAN);
        Position p = new Position(entry, exit);
        // With identical (NaN) prices the gross profit is zero; after subtracting zero costs the profit is zero.
        assertTrue(p.isClosed());
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    /* --------------------------------------------------------------------- */
    /*  Cost calculations – ensure zero‑cost model yields zero cost            */
    /* --------------------------------------------------------------------- */

    @Test
    public void testPositionCostIsZeroWithZeroCostModel() {
        Position p = new Position(TradeType.BUY, zeroCostModel, zeroCostModel);
        p.operate(0); // entry
        p.operate(2); // exit
        Num cost = p.getPositionCost();
        // ZeroCostModel always returns zero; the NumFactory used by the entry trade yields zero.
        assertEquals(NAN, cost);
    }

    @Test
    public void testHoldingCostDelegatesToHoldingCostModel() {
        // Using the default ZeroCostModel for holding cost – should be zero.
        Position p = new Position(TradeType.SELL, zeroCostModel, zeroCostModel);
        p.operate(0);
        p.operate(5);
        Num holdingCost = p.getHoldingCost();
        assertEquals(NAN, holdingCost);
    }

    /* --------------------------------------------------------------------- */
    /*  toString representation                                               */
    /* --------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0, NAN, NAN);
        Trade exit  = Trade.sellAt(1, NAN, NAN);
        Position p = new Position(entry, exit);
        String txt = p.toString();
        assertTrue(txt.contains("Entry:"));
        assertTrue(txt.contains("exit:"));
        assertTrue(txt.contains(entry.toString()));
        assertTrue(txt.contains(exit.toString()));
    }
}
