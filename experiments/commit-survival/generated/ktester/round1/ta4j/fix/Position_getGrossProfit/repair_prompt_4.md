


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
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

// import org.mockito.junit.MockitoJUnitRunner;
// @ExtendWith(MockitoExtension.class)
class Position_getGrossProfit_Test {

    private Position openedPosition;

    private Position closedPosition;

    private Trade entryBuy;

    private Trade exitSell;

    private Trade entrySell;

    private Trade exitBuy;

    private CostModel transactionModel;

    //     @BeforeEach
    void setupBeforeEach() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();
        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        openedPosition = new Position();
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        closedPosition = new Position(entryBuy, exitSell, transactionModel, new ZeroCostModel());
        /* -------------------------------------------------
         *  Closed short position (SELL → BUY) – used in a dedicated test.
         * ------------------------------------------------- */
        entrySell = Trade.sellAt(0, DoubleNum.valueOf(15), DoubleNum.valueOf(1), transactionModel);
        exitBuy = Trade.buyAt(4, DoubleNum.valueOf(13), DoubleNum.valueOf(1), transactionModel);
    }

    @Test
    void whenPositionIsOpened_getGrossProfitReturnsZero() {
        // The position created in setUp is still opened (no exit trade)
        assertTrue(openedPosition.isOpened());
        Num grossProfit = openedPosition.getGrossProfit();
        // Expected zero – the library uses a Num representing 0
        Num expectedZero = DoubleNum.valueOf(0);
        assertEquals(expectedZero, grossProfit);
    }

    @Test
    void whenPositionIsClosed_getGrossProfitDelegatesToOverload() {
        // Verify that the closed BUY → SELL position returns the correct gross profit
        assertTrue(closedPosition.isClosed());
        Num grossProfitViaMethod = closedPosition.getGrossProfit();
        Num grossProfitViaOverload = closedPosition.getGrossProfit(exitSell.getPricePerAsset());
        assertEquals(grossProfitViaOverload, grossProfitViaMethod);
        // Manual expected value: exit price – entry price = 12 – 10 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfitViaMethod);
    }

    @Test
    void whenShortPositionIsClosed_getGrossProfitIsEntryMinusExit() {
        // Build a short (SELL → BUY) closed position
        Position shortPosition = new Position(entrySell, exitBuy, transactionModel, new ZeroCostModel());
        assertTrue(shortPosition.isClosed());
        Num grossProfit = shortPosition.getGrossProfit();
        // Expected: entry price – exit price = 15 – 13 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfit);
    }

    /* ----------------------------------------------------------------------
     *  4️⃣  Edge case: closed position with a null internal exit trade.
     *
     *  We create a Position where the exit reference is null but force
     *  {@code isOpened()} to return {@code false} via a Mockito spy.
     *  The method should attempt to dereference {@code exit} and thus throw
     *  a {@link NullPointerException}.
     * ---------------------------------------------------------------------- */
    @Test
    void whenClosedPositionHasNullExit_throwsNullPointerException() throws Exception {
        // Construct a Position with a valid entry and a null exit.
        Position closedWithNullExit = new Position(entryBuy, null, transactionModel, new ZeroCostModel()) {

            @Override
            public boolean isOpened() {
                // force the "closed" branch
                return false;
            }
        };
        try {
            closedWithNullExit.getGrossProfit();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }

    /* ----------------------------------------------------------------------
     *  5️⃣  Edge case: closed position whose exit trade returns null for price.
     *
     *  We mock the exit Trade so that {@code getPricePerAsset()} returns null.
     *  The overloaded {@code getGrossProfit(Num)} will receive a null argument,
     *  which is expected to cause a {@link NullPointerException}.
     * ---------------------------------------------------------------------- */
    @Test
    void whenClosedPositionExitPriceIsNull_throwsNullPointerException() {
        // The position is naturally considered closed because it has a non‑null exit.
        assertTrue(closedPosition.isClosed());
        try {
            // Pass null as the final price – the overload should NPE.
            closedPosition.getGrossProfit(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }

    @Before
    public void setup() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();
        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        openedPosition = new Position();
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        closedPosition = new Position(entryBuy, exitSell, transactionModel, new ZeroCostModel());
        /* -------------------------------------------------
         *  Closed short position (SELL → BUY) – used in a dedicated test.
         * ------------------------------------------------- */
        entrySell = Trade.sellAt(0, DoubleNum.valueOf(15), DoubleNum.valueOf(1), transactionModel);
        exitBuy = Trade.buyAt(4, DoubleNum.valueOf(13), DoubleNum.valueOf(1), transactionModel);
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

Test run finished after 21 ms
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
 

api document of method getGrossProfit(): 

     * Calculates the gross profit of the position if it is closed. The gross profit
     * excludes any trading costs.
     *
     * @return the gross profit of the position
     

return type: 
org.ta4j.core.num.Num

dependent classes: 
class `org.ta4j.core.Position`:
	dependent fields:
		org.ta4j.core.Trade org.ta4j.core.Position.exit;
	related functions:
		method `public Num getGrossReturn()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * Calculates the gross return of the position if it is closed. The gross return
     * excludes any trading costs (and includes the base).
     *
     * @return the gross return of the position in percent
     * @see #getGrossReturn(Num)
     ;
		method `public Trade getExit()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * @return the exit {@link Trade trade} of the position
     ;
		method `public Num getGrossProfit(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * Calculates the gross profit of the position. The gross profit excludes any
     * trading costs.
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     *                   open)
     * @return the profit or loss of the position
     ;
		method `public Num getProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * Calculates the net profit of the position if it is closed. The net profit
     * includes any trading costs.
     *
     * @return the profit or loss of the position
     ;
		method `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getGrossProfit()`;



Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 
