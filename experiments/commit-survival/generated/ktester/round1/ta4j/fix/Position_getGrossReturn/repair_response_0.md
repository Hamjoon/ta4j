```java
package org.ta4j.core;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import static org.ta4j.core.num.NaN.NaN;

/**
 * Unit tests for {@link Position#getGrossReturn()}.
 *
 * The original test suite used JUnit 5 and Mockito, which are not available in the
 * compilation environment.  The tests have been rewritten to rely only on
 * JUnit 4 (which is present) and on the public API of the library, removing any
 * Mockito usage.
 */
public class Position_getGrossReturn_Test {

    private CostModel transactionModel;
    private CostModel holdingModel;
    private Trade entryTrade;
    private Trade exitTrade;

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

    @Test
    public void whenPositionIsOpened_getGrossReturnShouldReturnZero() {
        // GIVEN – an opened position (entry only, no exit)
        Position openedPosition = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        openedPosition.operate(0); // creates the entry trade

        // WHEN
        Num grossReturn = openedPosition.getGrossReturn();

        // THEN – for an opened position the method must return zero
        assertEquals(DoubleNum.valueOf(0), grossReturn);
    }

    @Test
    public void whenPositionIsClosed_getGrossReturnShouldDelegateToPriceBasedMethod() {
        // GIVEN – a fully closed position (entry + exit)
        Position closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);

        // WHEN
        Num grossReturnNoArg = closedPosition.getGrossReturn();
        Num grossReturnWithPrice = closedPosition.getGrossReturn(exitTrade.getPricePerAsset());

        // THEN – the no‑arg version must delegate to the price‑based version
        assertEquals(grossReturnWithPrice, grossReturnNoArg);
    }

    @Test
    public void closedPositionPositiveReturn() {
        // entry 100, exit 110 → (110‑100)/100 = 0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0.10), result);
    }

    @Test
    public void closedPositionNegativeReturn() {
        // entry 100, exit 90 → (90‑100)/100 = -0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(90), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(-0.10), result);
    }

    @Test
    public void closedPositionZeroReturn() {
        // entry 100, exit 100 → (100‑100)/100 = 0
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0), result);
    }

    @Test
    public void closedPositionExitPriceZeroProducesNaN() {
        // entry 100, exit 0 → division by zero → NaN
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(0), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();
        assertEquals(NaN, result);
    }

    @Test
    public void closedPositionEntryPriceZeroProducesNaN() {
        // entry 0, exit 50 → division by zero → NaN
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(0), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(50), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();
        assertEquals(NaN, result);
    }

    @Test
    public void closedPositionFractionalPrices() {
        // entry 1.0000, exit 1.2345 → (1.2345‑1.0000)/1.0000 = 0.2345
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(1.0000), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(1.2345), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0.2345), result);
    }

    @Test
    public void closedPositionWithNullExitTradeThrowsNPE() {
        // Create a position with a null exit trade (simulating a malformed closed position)
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, null, transactionModel, holdingModel);

        // The method should attempt to dereference the exit trade and throw NPE
        assertThrows(NullPointerException.class, position::getGrossReturn);
    }
}
```