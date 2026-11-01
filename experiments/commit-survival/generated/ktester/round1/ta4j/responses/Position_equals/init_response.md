```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Position_equals_Test {

    /* --------------------------------------------------------------------- */
    /*  Shared fixtures                                                    */
    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;
    private CostModel holdingModel;

    private Trade entryTrade;
    private Trade exitTrade;

    private Position defaultPos1;
    private Position defaultPos2;
    private Position sellPos1;
    private Position sellPos2;
    private Position nullPos1;
    private Position nullPos2;
    private Position diffEntryPos;
    private Position diffExitPos;
    private Position tradesPos1;
    private Position tradesPos2;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // Cost models used by the trades created via the static factory methods
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        // Trades used for the constructor that receives explicit trades
        entryTrade = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        exitTrade = Trade.sellAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);

        /* -------------------------------------------------------------
         * Positions built using the default (BUY) starting type
         * ------------------------------------------------------------- */
        defaultPos1 = new Position();          // BUY start
        defaultPos1.operate(1);                // entry
        defaultPos1.operate(2);                // exit

        defaultPos2 = new Position();          // BUY start
        defaultPos2.operate(1);
        defaultPos2.operate(2);

        /* -------------------------------------------------------------
         * Positions built using an explicit SELL starting type
         * ------------------------------------------------------------- */
        sellPos1 = new Position(Trade.TradeType.SELL);
        sellPos1.operate(1);
        sellPos1.operate(2);

        sellPos2 = new Position(Trade.TradeType.SELL);
        sellPos2.operate(1);
        sellPos2.operate(2);

        /* -------------------------------------------------------------
         * Positions that have never been operated on – both entry and
         * exit are null
         * ------------------------------------------------------------- */
        nullPos1 = new Position();                 // BUY start, no trades
        nullPos2 = new Position(Trade.TradeType.SELL); // SELL start, no trades

        /* -------------------------------------------------------------
         * Positions that differ only by entry trade
         * ------------------------------------------------------------- */
        diffEntryPos = new Position();   // entry at index 5, exit at 6
        diffEntryPos.operate(5);
        diffEntryPos.operate(6);

        Position otherEntryPos = new Position(); // entry at index 7, exit at 8
        otherEntryPos.operate(7);
        otherEntryPos.operate(8);
        diffEntryPos = otherEntryPos; // reuse variable for clarity in test

        /* -------------------------------------------------------------
         * Positions that differ only by exit trade
         * ------------------------------------------------------------- */
        diffExitPos = new Position();   // entry at 1, exit at 2
        diffExitPos.operate(1);
        diffExitPos.operate(2);

        Position otherExitPos = new Position(); // entry at 1, exit at 3
        otherExitPos.operate(1);
        otherExitPos.operate(3);
        diffExitPos = otherExitPos; // reuse variable for clarity in test

        /* -------------------------------------------------------------
         * Positions built from explicit Trade objects
         * ------------------------------------------------------------- */
        tradesPos1 = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
        tradesPos2 = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    /* --------------------------------------------------------------------- */
    /*  Tests for Position.equals                                            */
    /* --------------------------------------------------------------------- */

    @Test
    void equalsSameTradesDefaultStartingType() {
        assertTrue(defaultPos1.equals(defaultPos2),
                "Two positions with identical BUY entry/exit trades must be equal");
        assertEquals(defaultPos1, defaultPos2, "equals and assertEquals should agree");
    }

    @Test
    void equalsSameTradesSellStartingType() {
        assertTrue(sellPos1.equals(sellPos2),
                "Two positions with identical SELL entry/exit trades must be equal");
        assertEquals(sellPos1, sellPos2);
    }

    @Test
    void equalsWhenBothEntryAndExitAreNull() {
        // Both positions have never been operated on – entry & exit are null
        assertTrue(nullPos1.equals(nullPos2),
                "Positions with null entry and null exit should be equal regardless of starting type");
        assertEquals(nullPos1, nullPos2);
    }

    @Test
    void notEqualsWhenEntryDiffers() {
        Position p1 = new Position();   // entry at 1, exit at 2
        p1.operate(1);
        p1.operate(2);

        Position p2 = new Position();   // entry at 3, exit at 4
        p2.operate(3);
        p2.operate(4);

        assertFalse(p1.equals(p2), "Positions with different entry trades must not be equal");
    }

    @Test
    void notEqualsWhenExitDiffers() {
        Position p1 = new Position();   // entry at 1, exit at 2
        p1.operate(1);
        p1.operate(2);

        Position p2 = new Position();   // entry at 1, exit at 3
        p2.operate(1);
        p2.operate(3);

        assertFalse(p1.equals(p2), "Positions with the same entry but different exit trades must not be equal");
    }

    @Test
    void equalsWhenConstructedFromSameTradeObjects() {
        assertTrue(tradesPos1.equals(tradesPos2),
                "Positions built from the same entry/exit Trade instances must be equal");
        assertEquals(tradesPos1, tradesPos2);
    }

    @Test
    void notEqualsWithDifferentObjectType() {
        assertFalse(defaultPos1.equals("not a position"),
                "equals must return false when compared with an object of another type");
    }

    @Test
    void reflexiveAndSymmetricProperties() {
        assertTrue(defaultPos1.equals(defaultPos1), "equals must be reflexive");
        assertTrue(defaultPos1.equals(defaultPos2) && defaultPos2.equals(defaultPos1),
                "equals must be symmetric");
    }

    @Test
    void hashCodeConsistentWithEquals() {
        // When two objects are equal, their hashCodes must also be equal
        assertEquals(defaultPos1, defaultPos2);
        assertEquals(defaultPos1.hashCode(), defaultPos2.hashCode(),
                "Equal objects must have identical hashCodes");
    }
}
```