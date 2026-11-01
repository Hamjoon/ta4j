package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import static org.ta4j.core.num.NaN.NaN;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

class Position_getGrossReturn_Test {

    private CostModel transactionModel;

    private CostModel holdingModel;

    private Trade entryTrade;

    private Trade exitTrade;

    //     @BeforeEach
    void setupBeforeEach() {
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitTrade = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
    }

    @Test
    void whenPositionIsOpened_getGrossReturnShouldReturnZero() {
        // GIVEN – an opened position (entry only, no exit)
        Position openedPosition = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        // creates the entry trade
        openedPosition.operate(0);
        // WHEN
        Num grossReturn = openedPosition.getGrossReturn();
        // THEN – for an opened position the method must return zero
        assertEquals(DoubleNum.valueOf(0), grossReturn);
    }

    @Test
    void whenPositionIsClosed_getGrossReturnShouldDelegateToPriceBasedMethod() {
        // GIVEN – a fully closed position (entry + exit)
        Position closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
        // WHEN
        Num grossReturnNoArg = closedPosition.getGrossReturn();
        Num grossReturnWithPrice = closedPosition.getGrossReturn(exitTrade.getPricePerAsset());
        // THEN – the no‑arg version must delegate to the price‑based version
        assertEquals(grossReturnWithPrice, grossReturnNoArg);
    }

    @Test
    void closedPositionPositiveReturn() {
        // entry 100, exit 110 → (110‑100)/100 = 0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0.10), result);
    }

    @Test
    void closedPositionNegativeReturn() {
        // entry 100, exit 90 → (90‑100)/100 = -0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(90), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(-0.10), result);
    }

    @Test
    void closedPositionZeroReturn() {
        // entry 100, exit 100 → (100‑100)/100 = 0
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0), result);
    }

    @Test
    void closedPositionExitPriceZeroProducesNaN() {
        // entry 100, exit 0 → division by zero → NaN
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(0), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(NaN, result);
    }

    @Test
    void closedPositionEntryPriceZeroProducesNaN() {
        // entry 0, exit 50 → division by zero → NaN
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(0), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(50), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(NaN, result);
    }

    @Test
    void closedPositionFractionalPrices() {
        // entry 1.0000, exit 1.2345 → (1.2345‑1.0000)/1.0000 = 0.2345
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(1.0000), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(1.2345), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0.2345), result);
    }

    @Test
    void closedPositionWithNullExitTradeThrowsNPE() {
        // Create a position with a null exit trade (simulating a malformed closed position)
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, null, transactionModel, holdingModel);
        try {
            position.getGrossReturn();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Before
    public void setup() {
        // Simple zero‑cost models – they do not affect gross‑return calculations
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();
        // BUY entry trade at price 10, amount 1
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        // Complementary SELL exit trade at price 12, amount 1
        exitTrade = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
    }
}
