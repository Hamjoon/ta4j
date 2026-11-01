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
import static org.ta4j.core.num.NaN.NaN;
import static org.mockito.Mockito.*;

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

    @Test
    void closedPositionPositiveReturn() {
        // entry 100, exit 110 → (110‑100)/100 = 0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();

        assertEquals(DoubleNum.valueOf(0.10), result,
                "Positive gross return (10%) should be calculated correctly");
    }

    @Test
    void closedPositionNegativeReturn() {
        // entry 100, exit 90 → (90‑100)/100 = -0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(90), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();

        assertEquals(DoubleNum.valueOf(-0.10), result,
                "Negative gross return (-10%) should be calculated correctly");
    }

    @Test
    void closedPositionZeroReturn() {
        // entry 100, exit 100 → (100‑100)/100 = 0
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();

        assertEquals(DoubleNum.valueOf(0), result,
                "Zero gross return should be returned when entry and exit prices are equal");
    }

    @Test
    void closedPositionExitPriceZeroProducesNaN() {
        // entry 100, exit 0 → division by zero → NaN
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(0), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();

        assertEquals(NaN, result, "Gross return should be NaN when exit price is zero");
    }

    @Test
    void closedPositionEntryPriceZeroProducesNaN() {
        // entry 0, exit 50 → division by zero → NaN
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(0), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(50), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();

        assertEquals(NaN, result, "Gross return should be NaN when entry price is zero");
    }

    @Test
    void closedPositionFractionalPrices() {
        // entry 1.0000, exit 1.2345 → (1.2345‑1.0000)/1.0000 = 0.2345
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(1.0000), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(1.2345), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);

        Num result = position.getGrossReturn();

        assertEquals(DoubleNum.valueOf(0.2345), result,
                "Gross return with fractional prices should be calculated with full precision");
    }

    @Test
    void closedPositionWithNullExitTradeThrowsNPE() {
        // Create a position with a null exit trade (simulating a malformed closed position)
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, null, transactionModel, holdingModel);

        // The method should attempt to dereference exit and throw NPE
        assertThrows(NullPointerException.class, position::getGrossReturn,
                "Calling getGrossReturn on a position with a null exit trade must throw NPE");
    }

    @Test
    void closedPositionWithNullExitPriceThrowsNPE() {
        // Mock an exit trade whose getPricePerAsset() returns null
        Trade mockedExit = mock(Trade.class);
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        // Entry trade is normal
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, mockedExit, transactionModel, holdingModel);

        // Expect NPE when the method tries to use the null price
        assertThrows(NullPointerException.class, position::getGrossReturn,
                "Null exit price should lead to NullPointerException");
    }

    @Test
    void closedPositionWhenPriceBasedMethodReceivesNullArgumentThrowsIllegalArgument() {
        // Mock an exit trade to return null price, then call the overloaded method directly
        Trade mockedExit = mock(Trade.class);
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, mockedExit, transactionModel, holdingModel);

        // The private overload getGrossReturn(Num) is invoked via the public no‑arg method.
        // According to the specification it should throw IllegalArgumentException for null.
        assertThrows(IllegalArgumentException.class, position::getGrossReturn,
                "Passing a null finalPrice to the price‑based overload must throw IllegalArgumentException");
    }
}
```