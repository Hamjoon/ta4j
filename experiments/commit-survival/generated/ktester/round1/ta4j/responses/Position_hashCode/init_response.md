```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Position_hashCode_Test {

    /** Common cost model used for creating trades */
    private CostModel transactionModel;

    /** Trades used in several tests */
    private Trade entryBuy;
    private Trade exitSell;
    private Trade entryBuyOther;
    private Trade exitSellOther;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple zero‑cost model is enough for most hashCode scenarios
        transactionModel = new ZeroCostModel();

        // Standard BUY entry at index 1, price 10, amount 1
        entryBuy = Trade.buyAt(1, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        // Complementary SELL exit at index 2, price 12, amount 1
        exitSell = Trade.sellAt(2, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);

        // Slightly different trades for negative tests
        entryBuyOther = Trade.buyAt(3, DoubleNum.valueOf(20), DoubleNum.valueOf(2), transactionModel);
        exitSellOther = Trade.sellAt(4, DoubleNum.valueOf(22), DoubleNum.valueOf(2), transactionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    @Test
    void hashCode_isConsistentAcrossCalls() {
        Position p = new Position(entryBuy, exitSell, transactionModel, transactionModel);
        int first = p.hashCode();
        int second = p.hashCode();
        assertEquals(first, second, "hashCode should be consistent across multiple invocations");
    }

    @Test
    void hashCode_equalPositions_haveSameHashCode() {
        // Two positions built with the same entry and exit trades
        Position p1 = new Position(entryBuy, exitSell, transactionModel, transactionModel);
        Position p2 = new Position(entryBuy, exitSell, transactionModel, transactionModel);

        assertEquals(p1, p2, "Positions built from identical trades must be equal");
        assertEquals(p1.hashCode(), p2.hashCode(),
                "Equal positions must return the same hashCode");
    }

    @Test
    void hashCode_differentEntryResultsInDifferentHashCode() {
        // Same exit trade, different entry trade
        Position p1 = new Position(entryBuy, exitSell, transactionModel, transactionModel);
        Position p2 = new Position(entryBuyOther, exitSell, transactionModel, transactionModel);

        assertNotEquals(p1, p2, "Positions with different entry trades must not be equal");
        assertNotEquals(p1.hashCode(), p2.hashCode(),
                "Positions with different entry trades should have different hashCodes");
    }

    @Test
    void hashCode_differentExitResultsInDifferentHashCode() {
        // Same entry trade, different exit trade
        Position p1 = new Position(entryBuy, exitSell, transactionModel, transactionModel);
        Position p2 = new Position(entryBuy, exitSellOther, transactionModel, transactionModel);

        assertNotEquals(p1, p2, "Positions with different exit trades must not be equal");
        assertNotEquals(p1.hashCode(), p2.hashCode(),
                "Positions with different exit trades should have different hashCodes");
    }

    @Test
    void hashCode_whenOnePositionIsOpenAndOtherClosedDiffer() {
        // Open position (only entry) vs closed position (entry + exit)
        Position open = new Position(entryBuy, null, transactionModel, transactionModel);
        Position closed = new Position(entryBuy, exitSell, transactionModel, transactionModel);

        assertNotEquals(open, closed, "Open and closed positions must not be equal");
        assertNotEquals(open.hashCode(), closed.hashCode(),
                "Open and closed positions should have different hashCodes");
    }

    @Test
    void hashCode_usingDefaultConstructorAndOperateProducesSameResultAsExplicitTrades() {
        // Build a position via the public API (operate) and compare its hashCode
        Position viaOperate = new Position();          // defaults to BUY start
        viaOperate.operate(1);                         // entry BUY at index 1 (price NaN)
        viaOperate.operate(2);                         // exit SELL at index 2 (price NaN)

        // Manually create the same logical trades (price NaN is represented by NaN constant)
        Trade entry = Trade.buyAt(1, NaN, NaN);
        Trade exit = Trade.sellAt(2, NaN, NaN);
        Position explicit = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());

        assertEquals(explicit, viaOperate,
                "Positions built via operate() should be equal to those built explicitly");
        assertEquals(explicit.hashCode(), viaOperate.hashCode(),
                "HashCodes must match for logically identical positions");
    }
}
```