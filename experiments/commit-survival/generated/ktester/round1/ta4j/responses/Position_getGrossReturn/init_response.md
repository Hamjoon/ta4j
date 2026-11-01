```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_Test {

    private CostModel transactionModel;
    private CostModel holdingModel;
    private Trade entryTrade;
    private Trade exitTrade;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Use the simplest cost models – they do not affect gross‑return calculations
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();

        // Create a BUY entry trade at price 10, amount 1
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        // Create the complementary SELL exit trade at price 12, amount 1
        exitTrade = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up for these unit tests
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown needed
    }

    @Test
    void whenPositionIsOpened_getGrossReturnShouldReturnZero() {
        // GIVEN – an opened position (entry only, no exit)
        Position openedPosition = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        openedPosition.operate(0); // creates the entry trade

        // WHEN
        Num grossReturn = openedPosition.getGrossReturn();

        // THEN – for an opened position the method must return zero
        assertEquals(DoubleNum.valueOf(0), grossReturn,
                "Gross return of an opened position must be zero");
    }

    @Test
    void whenPositionIsClosed_getGrossReturnShouldDelegateToPriceBasedMethod() {
        // GIVEN – a fully closed position (entry + exit)
        Position closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);

        // WHEN
        Num grossReturnNoArg = closedPosition.getGrossReturn();
        Num grossReturnWithPrice = closedPosition.getGrossReturn(exitTrade.getPricePerAsset());

        // THEN – the no‑arg version must delegate to the price‑based version
        assertEquals(grossReturnWithPrice, grossReturnNoArg,
                "Gross return without argument should equal gross return computed with exit price");
    }
}
```