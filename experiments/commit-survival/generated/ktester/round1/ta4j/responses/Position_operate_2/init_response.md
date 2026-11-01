```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Position#operate(int, Num, Num)}.
 *
 * The tests cover the three main branches of the method:
 * <ul>
 *   <li>creation of an entry trade when the position is {@code new}</li>
 *   <li>creation of an exit trade when the position is {@code opened}</li>
 *   <li>validation that the index of a subsequent trade cannot be lower than the entry index</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_operate_2_Test {

    /** Position with default starting type (BUY). */
    private Position buyPosition;

    /** Position with explicit starting type (SELL). */
    private Position sellPosition;

    /** Simple cost model – the implementation under test does not depend on its behaviour. */
    private CostModel zeroCostModel;

    /** Sample numeric values used in the tests. */
    private Num price10;
    private Num amount1;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        zeroCostModel = new ZeroCostModel();
        buyPosition = new Position(Trade.TradeType.BUY, zeroCostModel, zeroCostModel);
        sellPosition = new Position(Trade.TradeType.SELL, zeroCostModel, zeroCostModel);

        price10 = DoubleNum.valueOf(10);
        amount1 = DoubleNum.valueOf(1);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up between tests.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required.
    }

    @Test
    void whenNewShouldCreateBuyEntryTrade() {
        // act
        Trade created = buyPosition.operate(5, price10, amount1);

        // assert
        Trade expected = Trade.buyAt(5, price10, amount1);
        assertEquals(expected, created, "The created trade should be a BUY entry trade");
        assertEquals(expected, buyPosition.getEntry(), "Entry trade stored in the position must match");
        assertNull(buyPosition.getExit(), "No exit trade should be present yet");
    }

    @Test
    void whenNewShouldCreateSellEntryTradeForSellStartingType() {
        // act
        Trade created = sellPosition.operate(3, price10, amount1);

        // assert
        Trade expected = Trade.sellAt(3, price10, amount1);
        assertEquals(expected, created, "The created trade should be a SELL entry trade");
        assertEquals(expected, sellPosition.getEntry());
        assertNull(sellPosition.getExit());
    }

    @Test
    void whenOpenedShouldCreateComplementExitTrade() {
        // arrange – first create an entry trade
        buyPosition.operate(2, price10, amount1);

        // act – now create the exit trade
        Trade exit = buyPosition.operate(8, price10, amount1);

        // assert
        Trade expectedExit = Trade.sellAt(8, price10, amount1);
        assertEquals(expectedExit, exit, "The exit trade must be the complement (SELL) of the BUY entry");
        assertEquals(expectedExit, buyPosition.getExit(), "Exit trade stored in the position must match");
    }

    @Test
    void whenOpenedOnSellStartingTypeShouldCreateBuyExitTrade() {
        // arrange
        sellPosition.operate(1, price10, amount1);

        // act
        Trade exit = sellPosition.operate(7, price10, amount1);

        // assert
        Trade expectedExit = Trade.buyAt(7, price10, amount1);
        assertEquals(expectedExit, exit);
        assertEquals(expectedExit, sellPosition.getExit());
    }

    @Test
    void whenIndexIsLessThanEntryIndexShouldThrowIllegalStateException() {
        // arrange
        buyPosition.operate(10, price10, amount1);

        // act & assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> buyPosition.operate(5, price10, amount1),
                "Operating with an index lower than the entry index must fail"
        );
        assertTrue(ex.getMessage().contains("The index i is less than the entryTrade index"));
    }

    @Test
    void operateShouldReturnTheCreatedTrade() {
        // act
        Trade entry = buyPosition.operate(0, price10, amount1);
        Trade exit = buyPosition.operate(4, price10, amount1);

        // assert
        assertEquals(entry, buyPosition.getEntry());
        assertEquals(exit, buyPosition.getExit());
    }
}
```