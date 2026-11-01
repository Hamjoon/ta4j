package org.ta4j.core;

// import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import java.util.stream.Stream;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.provider.MethodSource;

// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.junit.jupiter.MockitoExtension;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.junit.jupiter.api.Assertions.*;
class Position_operate_2_Test {

    /**
     * Position with default starting type (BUY).
     */
    private Position buyPosition;

    /**
     * Position with explicit starting type (SELL).
     */
    private Position sellPosition;

    /**
     * Simple cost model – the implementation under test does not depend on its behaviour.
     */
    private CostModel zeroCostModel;

    /**
     * Sample numeric values used in the tests.
     */
    private Num price10;

    private Num amount1;

    //     @BeforeEach
    void setupBeforeEach() {
        zeroCostModel = new ZeroCostModel();
        // The Position constructor used in the library takes the starting type and two cost models
        buyPosition = new Position(Trade.TradeType.BUY, zeroCostModel, zeroCostModel);
        sellPosition = new Position(Trade.TradeType.SELL, zeroCostModel, zeroCostModel);
        price10 = DoubleNum.valueOf(10);
        amount1 = DoubleNum.valueOf(1);
    }

//     private static Stream<Arguments> nullArgumentProvider() {
//         Num price = DoubleNum.valueOf(100.0);
//         Num amount = DoubleNum.valueOf(10.0);
//         return Stream.of(// price null, entry creation
//         Arguments.of(5, null, amount), // amount null, entry creation
//         Arguments.of(5, price, null), // price null, exit creation (need an entry first)
//         Arguments.of(10, null, amount), // amount null, exit creation (need an entry first)
//         Arguments.of(10, price, null));
//     }

//     @Test
//     void whenNewShouldCreateBuyEntryTrade() {
//         // act
//         Trade created = buyPosition.operate(5, price10, amount1);
//         // assert
//         Trade expected = Trade.buyAt(5, price10, amount1);
//         Assertions.assertEquals(expected, created, "The created trade should be a BUY entry trade");
//         Assertions.assertEquals(expected, buyPosition.getEntry(), "Entry trade stored in the position must match");
//         Assertions.assertNull(buyPosition.getExit(), "No exit trade should be present yet");
//     }

//     @Test
//     void whenNewShouldCreateSellEntryTradeForSellStartingType() {
//         // act
//         Trade created = sellPosition.operate(3, price10, amount1);
//         // assert
//         Trade expected = Trade.sellAt(3, price10, amount1);
//         Assertions.assertEquals(expected, created, "The created trade should be a SELL entry trade");
//         Assertions.assertEquals(expected, sellPosition.getEntry());
//         Assertions.assertNull(sellPosition.getExit());
//     }

//     @Test
//     void whenOpenedShouldCreateComplementExitTrade() {
//         // arrange – first create an entry trade
//         buyPosition.operate(2, price10, amount1);
//         // act – now create the exit trade
//         Trade exit = buyPosition.operate(8, price10, amount1);
//         // assert
//         Trade expectedExit = Trade.sellAt(8, price10, amount1);
//         Assertions.assertEquals(expectedExit, exit, "The exit trade must be the complement (SELL) of the BUY entry");
//         Assertions.assertEquals(expectedExit, buyPosition.getExit(), "Exit trade stored in the position must match");
//     }

//     @Test
//     void whenOpenedOnSellStartingTypeShouldCreateBuyExitTrade() {
//         // arrange
//         sellPosition.operate(1, price10, amount1);
//         // act
//         Trade exit = sellPosition.operate(7, price10, amount1);
//         // assert
//         Trade expectedExit = Trade.buyAt(7, price10, amount1);
//         Assertions.assertEquals(expectedExit, exit);
//         Assertions.assertEquals(expectedExit, sellPosition.getExit());
//     }

//     @Test
//     void whenIndexIsLessThanEntryIndexShouldThrowIllegalStateException() {
//         // arrange
//         buyPosition.operate(10, price10, amount1);
//         // act & assert
//         IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> buyPosition.operate(5, price10, amount1), "Operating with an index lower than the entry index must fail");
//         Assertions.assertTrue(ex.getMessage().contains("The index i is less than the entryTrade index"));
//     }

//     @Test
//     void operateShouldReturnTheCreatedTrade() {
//         // act
//         Trade entry = buyPosition.operate(0, price10, amount1);
//         Trade exit = buyPosition.operate(4, price10, amount1);
//         // assert
//         Assertions.assertEquals(entry, buyPosition.getEntry());
//         Assertions.assertEquals(exit, buyPosition.getExit());
//     }

//     @Test
//     void whenPositionClosedOperateReturnsNullAndLeavesStateUnchanged() {
//         // arrange – create a full (closed) position
//         // entry
//         buyPosition.operate(1, price10, amount1);
//         // exit, now closed
//         buyPosition.operate(3, price10, amount1);
//         Trade entryBefore = buyPosition.getEntry();
//         Trade exitBefore = buyPosition.getExit();
//         // act – attempt another operation
//         Trade result = buyPosition.operate(5, price10, amount1);
//         // assert – method should return null and not modify entry/exit
//         Assertions.assertNull(result, "When position is closed, operate should return null");
//         Assertions.assertEquals(entryBefore, buyPosition.getEntry(), "Entry trade must stay unchanged");
//         Assertions.assertEquals(exitBefore, buyPosition.getExit(), "Exit trade must stay unchanged");
//     }

//     @Test
//     void whenNewAndNegativeIndexEntryTradeCreated() {
//         // act – negative index is allowed for entry creation
//         Trade created = buyPosition.operate(-1, price10, amount1);
//         // assert
//         Trade expected = Trade.buyAt(-1, price10, amount1);
//         Assertions.assertEquals(expected, created, "Entry trade should be created even with negative index");
//         Assertions.assertEquals(expected, buyPosition.getEntry(), "Entry field must contain the created trade");
//         Assertions.assertNull(buyPosition.getExit(), "Exit must still be null");
//     }

    /**
     * Parameterized test for all combinations where a {@code null} argument is supplied.
     * The method is expected to throw {@link NullPointerException} because the
     * underlying {@link Trade} constructor does not accept {@code null} values.
     */
//     @ParameterizedTest(name = "{index}: index={0}, price={1}, amount={2}")
//     @MethodSource("nullArgumentProvider")
//     void whenNullArgumentsProvidedShouldThrowNullPointerException(int index, Num price, Num amount) {
//         // act & assert
//         Assertions.assertThrows(NullPointerException.class, () -> buyPosition.operate(index, price, amount), "Passing null price or amount must result in NullPointerException");
//     }

//     @Test
//     void nullArgumentsWhenPositionOpenedShouldThrowNullPointerException() {
//         // arrange – create entry so that position becomes opened
//         buyPosition.operate(1, price10, amount1);
//         // act & assert – price null
//         Assertions.assertThrows(NullPointerException.class, () -> buyPosition.operate(2, null, amount1), "Null price on exit creation must throw NPE");
//         // act & assert – amount null
//         Assertions.assertThrows(NullPointerException.class, () -> buyPosition.operate(2, price10, null), "Null amount on exit creation must throw NPE");
//     }
}