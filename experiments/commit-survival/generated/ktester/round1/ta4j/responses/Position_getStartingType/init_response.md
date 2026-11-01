```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Position_getStartingType_Test {

    /** Common cost models used for the tests that need explicit models */
    private CostModel transactionModel;
    private CostModel holdingModel;

    /** Trades used for the constructor that receives entry/exit trades */
    private Trade entryBuy;
    private Trade exitSell;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise simple linear cost models – they are lightweight and deterministic
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        // Create a BUY entry trade and a complementary SELL exit trade.
        // Prices and amounts are arbitrary; they are not used by getStartingType().
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
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
    void defaultConstructorShouldSetStartingTypeToBuy() {
        Position position = new Position(); // uses default starting type BUY
        assertEquals(Trade.TradeType.BUY, position.getStartingType(),
                "Default Position should start with BUY type");
    }

    @Test
    void constructorWithExplicitStartingTypeShouldReturnThatType() {
        Position sellPosition = new Position(Trade.TradeType.SELL);
        assertEquals(Trade.TradeType.SELL, sellPosition.getStartingType(),
                "Position constructed with SELL should return SELL as starting type");
    }

    @Test
    void constructorWithEntryAndExitTradesShouldDeriveStartingTypeFromEntry() {
        // The entry trade is BUY, the exit trade is SELL – they are complementary.
        Position position = new Position(entryBuy, exitSell, transactionModel, holdingModel);
        assertEquals(Trade.TradeType.BUY, position.getStartingType(),
                "Starting type must match the entry trade type (BUY)");
    }

    @Test
    void constructorWithNullStartingTypeShouldThrowIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Position(null, new ZeroCostModel(), new ZeroCostModel()),
                "Passing null as starting type must raise IllegalArgumentException");
        assertTrue(thrown.getMessage().contains("Starting type must not be null"));
    }

    @Test
    void constructorWithEntryAndExitTradesHavingSameTypeShouldThrowIllegalArgumentException() {
        // Create two BUY trades – they are not complementary.
        Trade buy1 = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade buy2 = Trade.buyAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Position(buy1, buy2, transactionModel, holdingModel),
                "Both trades having the same type must raise IllegalArgumentException");
        assertTrue(thrown.getMessage().contains("Both trades must have different types"));
    }
}
```