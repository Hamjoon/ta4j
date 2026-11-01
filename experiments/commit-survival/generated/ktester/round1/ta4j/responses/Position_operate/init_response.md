```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.num.NaN.NaN;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position#operate(int)} and its overload
 * {@link Position#operate(int, Num, Num)}.
 *
 * The tests are written with JUnit 5 and follow the given‑when‑then pattern.
 */
@ExtendWith(MockitoExtension.class)
class Position_operate_Test {

    /* --------------------------------------------------------------------- */
    /*  Shared fixtures                                                      */
    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;
    private CostModel holdingModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple linear cost models – they are deterministic and cheap.
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up after each test.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release.
    }

    /* --------------------------------------------------------------------- */
    /*  Tests for the simple operate(int) overload                            */
    /* --------------------------------------------------------------------- */

    @Test
    void whenNewPositionOperate_thenCreatesBuyEntryWithNaNValues() {
        // given
        Position position = new Position(); // default starting type = BUY

        // when
        position.operate(0);

        // then
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, position.getEntry(),
                "A newly created Position should generate a BUY entry trade with NaN price/amount");
    }

    @Test
    void whenPositionAlreadyOpenedOperate_thenCreatesComplementaryExit() {
        // given
        Position position = new Position(); // BUY entry expected
        position.operate(0); // creates entry

        // when
        position.operate(1); // should create SELL exit

        // then
        Trade expectedExit = Trade.sellAt(1, NaN, NaN);
        assertEquals(expectedExit, position.getExit(),
                "After an entry trade, a second operate() must create the complementary exit trade");
    }

    @Test
    void whenOperateCalledOnClosedPosition_itDoesNotAlterExistingTrades() {
        // given
        Position position = new Position(); // BUY entry
        position.operate(0); // entry
        position.operate(1); // exit – position now closed

        Trade entryBefore = position.getEntry();
        Trade exitBefore = position.getExit();

        // when – calling operate again should be a no‑op
        position.operate(2);

        // then
        assertEquals(entryBefore, position.getEntry(),
                "Entry trade must stay unchanged after the position is closed");
        assertEquals(exitBefore, position.getExit(),
                "Exit trade must stay unchanged after the position is closed");
    }

    /* --------------------------------------------------------------------- */
    /*  Tests for the overloaded operate(int, Num, Num) method                */
    /* --------------------------------------------------------------------- */

    @Test
    void whenSellPositionOperateWithPriceAndAmount_thenCreatesSellEntry() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        Num price = DoubleNum.valueOf(2.5);
        Num amount = DoubleNum.valueOf(10);

        // when
        position.operate(3, price, amount);

        // then
        Trade expectedEntry = Trade.sellAt(3, price, amount, transactionModel);
        assertEquals(expectedEntry, position.getEntry(),
                "A SELL‑starting Position must create a SELL entry with the supplied price/amount");
    }

    @Test
    void whenBuyPositionOperateWithPriceAndAmount_thenCreatesBuyEntry() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        Num price = DoubleNum.valueOf(1.2);
        Num amount = DoubleNum.valueOf(5);

        // when
        position.operate(7, price, amount);

        // then
        Trade expectedEntry = Trade.buyAt(7, price, amount, transactionModel);
        assertEquals(expectedEntry, position.getEntry(),
                "A BUY‑starting Position must create a BUY entry with the supplied price/amount");
    }

    @Test
    void whenOpenedSellPositionOperateAgainWithPriceAndAmount_thenCreatesBuyExit() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        position.operate(0, DoubleNum.valueOf(3), DoubleNum.valueOf(2)); // entry (SELL)

        // when – second call should create the complementary BUY exit
        position.operate(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2));

        // then
        Trade expectedExit = Trade.buyAt(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2), transactionModel);
        assertEquals(expectedExit, position.getExit(),
                "After a SELL entry, the next operate() must create a BUY exit with the given price/amount");
    }

    @Test
    void whenOpenedBuyPositionOperateAgainWithPriceAndAmount_thenCreatesSellExit() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        position.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1)); // entry (BUY)

        // when – second call should create the complementary SELL exit
        position.operate(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1));

        // then
        Trade expectedExit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        assertEquals(expectedExit, position.getExit(),
                "After a BUY entry, the next operate() must create a SELL exit with the given price/amount");
    }

    @Test
    void whenOperateCalledWithNaNValues_itFallsBackToDefaultBehaviour() {
        // given
        Position position = new Position(); // BUY start
        // when
        position.operate(0, NaN, NaN);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, position.getEntry(),
                "Passing NaN for price/amount must behave like the simple operate(int) overload");
    }
}
```