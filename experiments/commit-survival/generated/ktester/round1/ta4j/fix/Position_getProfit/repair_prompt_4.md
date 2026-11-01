


You are an experienced Java developer tasked with fixing a piece of code that contains execution errors. Your task is to resolve these issues after analyzing the code and its execution feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any logical errors.
2. Analyze the execution feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

// import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;
import java.lang.reflect.Field;
import java.util.stream.Stream;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
// import static org.mockito.Mockito.*;
// import static org.ta4j.core.TestUtils.assertNumEquals;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link Position#getProfit()}.
 */
// @ExtendWith(MockitoExtension.class)
class Position_getProfit_Test {

    /**
     * Cost model applied to every trade (1 % of the trade value).
     */
    private CostModel transactionModel;

    /**
     * Holding‑cost model (0.1 % per bar).
     */
    private CostModel holdingModel;

    /**
     * A BUY trade used as entry in long‑position tests.
     */
    private Trade longEntry;

    /**
     * A SELL trade used as exit in long‑position tests.
     */
    private Trade longExit;

    /**
     * A SELL trade used as entry in short‑position tests.
     */
    private Trade shortEntry;

    /**
     * A BUY trade used as exit in short‑position tests.
     */
    private Trade shortExit;

    //     @BeforeEach
    void setupBeforeEach() {
        // Initialise the cost models used by all tests.
        // 1 %
        transactionModel = new LinearTransactionCostModel(0.01);
        // 0.1 %
        holdingModel = new LinearBorrowingCostModel(0.001);
        // Long (BUY → SELL) trades – same price so gross profit = 0.
        longEntry = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        longExit = Trade.sellAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        // Short (SELL → BUY) trades – same price so gross profit = 0.
        shortEntry = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        shortExit = Trade.buyAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
    }

    /* -------------------------------------------------------------
     *  Parameterised tests for closed long positions (ZeroCostModel)
     * ------------------------------------------------------------- */
//     private static Stream<Arguments> closedLongZeroCostProvider() {
//         return Stream.of(// profit case
//         Arguments.of(DoubleNum.valueOf(20), DoubleNum.valueOf(25), DoubleNum.valueOf(50), DoubleNum.valueOf(250)), // breakeven case
//         Arguments.of(DoubleNum.valueOf(15), DoubleNum.valueOf(15), DoubleNum.valueOf(100), DoubleNum.valueOf(0)), // loss case
//         Arguments.of(DoubleNum.valueOf(40), DoubleNum.valueOf(35), DoubleNum.valueOf(30), DoubleNum.valueOf(-150)));
//     }

    /* -------------------------------------------------------------
     *  Parameterised tests for closed short positions (ZeroCostModel)
     * ------------------------------------------------------------- */
//     private static Stream<Arguments> closedShortZeroCostProvider() {
//         return Stream.of(// profit case (price falls)
//         Arguments.of(DoubleNum.valueOf(60), DoubleNum.valueOf(52), DoubleNum.valueOf(80), DoubleNum.valueOf(640)), // loss case (price rises)
//         Arguments.of(DoubleNum.valueOf(45), DoubleNum.valueOf(50), DoubleNum.valueOf(70), DoubleNum.valueOf(-350)));
//     }

//     @Test
//     void getProfit_whenPositionOpened_returnsZero() {
//         // A brand‑new position (default BUY) that has only been entered.
//         Position opened = new Position();
//         opened.operate(0);
//         Num profit = opened.getProfit();
//         assertNumEquals(DoubleNum.valueOf(0), profit);
//     }

//     @Test
//     void getProfit_whenClosedLongPosition_returnsGrossProfitMinusTransactionCosts() {
//         // Long position: BUY then SELL at the same price.
//         Position closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);
//         Num profit = closedLong.getProfit();
//         // Expected profit = 0 – (2 * 0.01 * 1) * 2 = -0.04
//         assertNumEquals(DoubleNum.valueOf(-0.04), profit);
//     }

//     @Test
//     void getProfit_whenClosedShortPosition_returnsGrossProfitMinusCostsAndHoldingCosts() {
//         // Short position: SELL then BUY at the same price, held for 9 bars (indices 1 → 10).
//         Position closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);
//         Num profit = closedShort.getProfit();
//         // holding cost = 2 * 9 * 0.001 = 0.018
//         Num expectedHoldingCost = DoubleNum.valueOf(2.0 * 9.0 * 0.001);
//         // expected profit = -0.04 - holdingCost
//         Num expectedProfit = DoubleNum.valueOf(-0.04).minus(expectedHoldingCost);
//         assertNumEquals(expectedProfit, profit);
//     }

//     @Test
//     void getProfit_whenOpenShortPosition_returnsZero() {
//         // Open a short position (starting type SELL) without an exit.
//         Position openedShort = new Position(TradeType.SELL, transactionModel, holdingModel);
//         openedShort.operate(0);
//         Num profit = openedShort.getProfit();
//         assertNumEquals(DoubleNum.valueOf(0), profit);
//     }

//     @ParameterizedTest
//     @MethodSource("closedLongZeroCostProvider")
//     void getProfit_closedLongZeroCostModel_returnsExpected(Num entryPrice, Num exitPrice, Num amount, Num expected) {
//         CostModel zeroCost = new ZeroCostModel();
//         Trade entry = Trade.buyAt(0, entryPrice, amount, zeroCost);
//         Trade exit = Trade.sellAt(1, exitPrice, amount, zeroCost);
//         Position position = new Position(entry, exit, zeroCost, zeroCost);
//         Num profit = position.getProfit();
//         assertNumEquals(expected, profit);
//     }

//     @ParameterizedTest
//     @MethodSource("closedShortZeroCostProvider")
//     void getProfit_closedShortZeroCostModel_returnsExpected(Num entryPrice, Num exitPrice, Num amount, Num expected) {
//         CostModel zeroCost = new ZeroCostModel();
//         Trade entry = Trade.sellAt(0, entryPrice, amount, zeroCost);
//         Trade exit = Trade.buyAt(1, exitPrice, amount, zeroCost);
//         Position position = new Position(entry, exit, zeroCost, zeroCost);
//         Num profit = position.getProfit();
//         assertNumEquals(expected, profit);
//     }

//     @Test
//     void getProfit_closedLongWithProportionalCost_returnsProfitMinusCost() {
//         // 2 % transaction cost on both entry and exit
//         CostModel percentCost = new LinearTransactionCostModel(0.02);
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(30), DoubleNum.valueOf(200), percentCost);
//         Trade exit = Trade.sellAt(1, DoubleNum.valueOf(33), DoubleNum.valueOf(200), percentCost);
//         // PositionCost = (30*200*0.02) + (33*200*0.02) = 252
//         // GrossProfit = (33‑30)*200 = 600
//         // Expected profit = 600 – 252 = 348
//         Position position = new Position(entry, exit, percentCost, new ZeroCostModel());
//         Num profit = position.getProfit();
//         assertNumEquals(DoubleNum.valueOf(348), profit);
//     }

//     @Test
//     void getProfit_closedPositionWithNaNExitPrice_returnsNaN() {
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), new ZeroCostModel());
//         Trade exit = Trade.sellAt(1, DoubleNum.NaN, DoubleNum.valueOf(10), new ZeroCostModel());
//         Position position = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
//         Num profit = position.getProfit();
//         assertTrue(profit.isNaN(), "Profit should be NaN when exit price is NaN");
//     }

//     @Test
//     void getProfit_closedPositionWithZeroAmount_returnsMinusPositionCost() {
//         // amount = 0, but a transaction cost of 7 % is forced via a custom model
//         CostModel customCost = new LinearTransactionCostModel(0.07);
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(0), customCost);
//         Trade exit = Trade.sellAt(1, DoubleNum.valueOf(120), DoubleNum.valueOf(0), customCost);
//         Position position = new Position(entry, exit, customCost, new ZeroCostModel());
//         // Expected profit = 0 – 7 = -7 (the custom model returns a non‑zero cost for zero amount)
//         Num profit = position.getProfit();
//         assertNumEquals(DoubleNum.valueOf(-7), profit);
//     }

//     @Test
//     void getProfit_exitNullAndIsOpenedFalse_throwsNullPointerException() throws Exception {
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
//         Trade exit = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
//         Position real = new Position(entry, exit, transactionModel, holdingModel);
//         Position spy = spy(real);
//         doReturn(false).when(spy).isOpened();
//         Field exitField = Position.class.getDeclaredField("exit");
//         exitField.setAccessible(true);
//         exitField.set(spy, null);
//         assertThrows(NullPointerException.class, spy::getProfit, "Calling getProfit() on a position whose exit is null should throw NPE");
//     }

//     @Test
//     void getProfit_exitPricePerAssetNull_throwsNullPointerException() {
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
//         Trade exitMock = mock(Trade.class);
//         when(exitMock.getPricePerAsset()).thenReturn(null);
//         when(exitMock.getAmount()).thenReturn(DoubleNum.valueOf(1));
//         when(exitMock.getType()).thenReturn(Trade.TradeType.SELL);
//         Position position = new Position(entry, exitMock, transactionModel, holdingModel);
//         assertThrows(NullPointerException.class, position::getProfit, "Profit calculation should NPE when exit price per asset is null");
//     }

//     @Test
//     void getProfit_negativeExitPrice_throwsIllegalArgumentException() {
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
//         Trade exit = Trade.sellAt(1, DoubleNum.valueOf(-5), DoubleNum.valueOf(1), transactionModel);
//         Position position = new Position(entry, exit, transactionModel, holdingModel);
//         assertThrows(IllegalArgumentException.class, position::getProfit, "Negative exit price should cause IllegalArgumentException");
//     }

//     @Test
//     void getProfit_nullCostModel_throwsNullPointerException() {
//         Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
//         Trade exit = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
//         Position position = new Position(entry, exit, transactionModel, holdingModel);
//         Position spy = spy(position);
//         doThrow(new NullPointerException("cost model is null")).when(spy).getPositionCost();
//         assertThrows(NullPointerException.class, spy::getProfit, "If the internal cost model is null, getProfit() should propagate NPE");
//     }
}
```

Corresponding execution feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8

╷
├─ JUnit Jupiter ✔
├─ JUnit Vintage ✔
└─ JUnit Platform Suite ✔

Test run finished after 24 ms
[         3 containers found      ]
[         0 containers skipped    ]
[         3 containers started    ]
[         0 containers aborted    ]
[         3 containers successful ]
[         0 containers failed     ]
[         0 tests found           ]
[         0 tests skipped         ]
[         0 tests started         ]
[         0 tests aborted         ]
[         0 tests successful      ]
[         0 tests failed          ]


```


And you can use the following related information to assist you in your analysis and correction:

api document of class org.ta4j.core.Position: 

 * A {@code Position} is a pair of two {@link Trade trades}.
 *
 * <p>
 * The exit trade has the complement type of the entry trade, i.e.:
 * <ul>
 * <li>entry == BUY --> exit == SELL
 * <li>entry == SELL --> exit == BUY
 * </ul>
 

api document of method getProfit(): 

     * Calculates the net profit of the position if it is closed. The net profit
     * includes any trading costs.
     *
     * @return the profit or loss of the position
     

return type: 
org.ta4j.core.num.Num

dependent classes: 
class `org.ta4j.core.Position`:
	dependent fields:
		org.ta4j.core.Trade org.ta4j.core.Position.exit;
	related functions:
		method `public Trade getExit()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.getProfit()`, api document: 
     * @return the exit {@link Trade trade} of the position
     ;
		method `public Num getGrossReturn()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit()`, api document: 
     * Calculates the gross return of the position if it is closed. The gross return
     * excludes any trading costs (and includes the base).
     *
     * @return the gross return of the position in percent
     * @see #getGrossReturn(Num)
     ;
		method `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getProfit()`;
		method `public Num getGrossProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit()`, api document: 
     * Calculates the gross profit of the position if it is closed. The gross profit
     * excludes any trading costs.
     *
     * @return the gross profit of the position
     ;
		method `public Num getGrossProfit(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit()`, api document: 
     * Calculates the gross profit of the position. The gross profit excludes any
     * trading costs.
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     *                   open)
     * @return the profit or loss of the position
     ;



Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 
