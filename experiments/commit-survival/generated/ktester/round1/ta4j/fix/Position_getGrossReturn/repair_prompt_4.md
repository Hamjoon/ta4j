


You are an experienced Java developer tasked with fixing a piece of code that contains execution errors. Your task is to resolve these issues after analyzing the code and its execution feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any logical errors.
2. Analyze the execution feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
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
        // Use the simplest cost models – they do not affect gross‑return calculations
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();
        // Create a BUY entry trade at price 10, amount 1
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        // Create the complementary SELL exit trade at price 12, amount 1
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

```

Corresponding execution feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8

╷
├─ JUnit Jupiter ✔
├─ JUnit Vintage ✔
└─ JUnit Platform Suite ✔

Test run finished after 18 ms
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
 

api document of method getGrossReturn(): 

     * Calculates the gross return of the position if it is closed. The gross return
     * excludes any trading costs (and includes the base).
     *
     * @return the gross return of the position in percent
     * @see #getGrossReturn(Num)
     

return type: 
org.ta4j.core.num.Num

dependent classes: 
class `org.ta4j.core.Position`:
	dependent fields:
		org.ta4j.core.Trade org.ta4j.core.Position.exit;
	related functions:
		method `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getGrossReturn()`;
		method `public Trade getExit()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: 
     * @return the exit {@link Trade trade} of the position
     ;
		method `public Num getProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: 
     * Calculates the net profit of the position if it is closed. The net profit
     * includes any trading costs.
     *
     * @return the profit or loss of the position
     ;
		method `public Num getGrossProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: 
     * Calculates the gross profit of the position if it is closed. The gross profit
     * excludes any trading costs.
     *
     * @return the gross profit of the position
     ;
		method `public Num getGrossProfit(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: 
     * Calculates the gross profit of the position. The gross profit excludes any
     * trading costs.
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     *                   open)
     * @return the profit or loss of the position
     ;



Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 
