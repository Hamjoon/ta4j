
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class Position_getGrossProfit_Test {

    private Position openedPosition;

    private Position closedPosition;

    private Trade entryBuy;

    private Trade exitSell;

    private Trade entrySell;

    private Trade exitBuy;

    private CostModel transactionModel;

    @BeforeEach
    void setupBeforeEach() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();
        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        // default BUY‑starting position
        openedPosition = new Position();
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0, // entry price
        DoubleNum.valueOf(10), // amount
        DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, // exit price
        DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
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
        assertTrue(openedPosition.isOpened(), "Position should be opened");
        Num grossProfit = openedPosition.getGrossProfit();
        // Expected zero – the library uses a Num representing 0
        Num expectedZero = DoubleNum.valueOf(0);
        assertEquals(expectedZero, grossProfit, "Gross profit of an opened position must be zero");
    }

    @Test
    void whenPositionIsClosed_getGrossProfitDelegatesToOverload() {
        // Verify that the closed BUY → SELL position returns the correct gross profit
        assertTrue(closedPosition.isClosed(), "Position should be closed");
        Num grossProfitViaMethod = closedPosition.getGrossProfit();
        Num grossProfitViaOverload = closedPosition.getGrossProfit(exitSell.getPricePerAsset());
        assertEquals(grossProfitViaOverload, grossProfitViaMethod, "getGrossProfit() must delegate to the overload using exit price");
        // Manual expected value: exit price – entry price = 12 – 10 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfitViaMethod, "Gross profit for BUY→SELL should equal exit‑price minus entry‑price");
    }

    @Test
    void whenShortPositionIsClosed_getGrossProfitIsEntryMinusExit() {
        // Build a short (SELL → BUY) closed position
        Position shortPosition = new Position(entrySell, exitBuy, transactionModel, new ZeroCostModel());
        assertTrue(shortPosition.isClosed(), "Short position should be closed");
        Num grossProfit = shortPosition.getGrossProfit();
        // Expected: entry price – exit price = 15 – 13 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfit, "Gross profit for SELL→BUY should equal entry‑price minus exit‑price");
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
        Position position = new Position(entryBuy, null, transactionModel, new ZeroCostModel());
        // Spy the instance and stub isOpened() to return false (simulating a closed state).
        Position spy = Mockito.spy(position);
        // force the "closed" branch
        doReturn(false).when(spy).isOpened();
        // Verify that invoking getGrossProfit() triggers a NPE because exit is null.
        assertThrows(NullPointerException.class, spy::getGrossProfit, "Calling getGrossProfit() on a closed position with a null exit should throw NPE.");
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
        // Mock an exit trade that returns null for price per asset.
        Trade mockedExit = mock(Trade.class);
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        // Ensure the mock reports the correct type (SELL) to keep the Position consistent.
        when(mockedExit.getType()).thenReturn(TradeType.SELL);
        // Build the Position with a valid entry and the mocked exit.
        Position position = new Position(entryBuy, mockedExit, transactionModel, new ZeroCostModel());
        // The position is naturally considered closed because exit != null.
        assertTrue(position.isClosed(), "Position should be closed when exit trade is non‑null.");
        // Expect a NPE when the null price propagates to the overload.
        assertThrows(NullPointerException.class, position::getGrossProfit, "Calling getGrossProfit() when exit price is null should throw NPE.");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:9: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:10: error: package org.mockito does not exist
import org.mockito.Mockito;
                  ^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:11: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:13: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:16: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:18: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:15: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:18: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:35: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:66: error: cannot find symbol
        assertTrue(openedPosition.isOpened(), "Position should be opened");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:70: error: cannot find symbol
        assertEquals(expectedZero, grossProfit, "Gross profit of an opened position must be zero");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:76: error: cannot find symbol
        assertTrue(closedPosition.isClosed(), "Position should be closed");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:79: error: cannot find symbol
        assertEquals(grossProfitViaOverload, grossProfitViaMethod, "getGrossProfit() must delegate to the overload using exit price");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:82: error: cannot find symbol
        assertEquals(expected, grossProfitViaMethod, "Gross profit for BUY→SELL should equal exit‑price minus entry‑price");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:89: error: cannot find symbol
        assertTrue(shortPosition.isClosed(), "Short position should be closed");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:93: error: cannot find symbol
        assertEquals(expected, grossProfit, "Gross profit for SELL→BUY should equal entry‑price minus exit‑price");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:109: error: cannot find symbol
        Position spy = Mockito.spy(position);
                       ^
  symbol:   variable Mockito
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:111: error: cannot find symbol
        doReturn(false).when(spy).isOpened();
        ^
  symbol:   method doReturn(boolean)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:113: error: cannot find symbol
        assertThrows(NullPointerException.class, spy::getGrossProfit, "Calling getGrossProfit() on a closed position with a null exit should throw NPE.");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,spy::getGrossProfit,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:126: error: cannot find symbol
        Trade mockedExit = mock(Trade.class);
                           ^
  symbol:   method mock(Class<Trade>)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:127: error: cannot find symbol
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        ^
  symbol:   method when(Num)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:129: error: cannot find symbol
        when(mockedExit.getType()).thenReturn(TradeType.SELL);
        ^
  symbol:   method when(TradeType)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:133: error: cannot find symbol
        assertTrue(position.isClosed(), "Position should be closed when exit trade is non‑null.");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_getGrossProfit_Test
src/test/java/org/ta4j/core/Position_getGrossProfit_Test.java:135: error: cannot find symbol
        assertThrows(NullPointerException.class, position::getGrossProfit, "Calling getGrossProfit() when exit price is null should throw NPE.");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,position::[...]rofit,String)
  location: class Position_getGrossProfit_Test
25 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getGrossProfit()': '\n     * Calculates the gross profit of the position if it is closed. The gross profit\n     * excludes any trading costs.\n     *\n     * @return the gross profit of the position\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.Trade org.ta4j.core.Position.exit;\n\trelated functions:\n\t\tmethod `public Num getGrossReturn()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: \n     * Calculates the gross return of the position if it is closed. The gross return\n     * excludes any trading costs (and includes the base).\n     *\n     * @return the gross return of the position in percent\n     * @see #getGrossReturn(Num)\n     ;\n\t\tmethod `public Trade getExit()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: \n     * @return the exit {@link Trade trade} of the position\n     ;\n\t\tmethod `public Num getGrossProfit(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: \n     * Calculates the gross profit of the position. The gross profit excludes any\n     * trading costs.\n     *\n     * @param finalPrice the price of the final bar to be considered (if position is\n     *                   open)\n     * @return the profit or loss of the position\n     ;\n\t\tmethod `public Num getProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: \n     * Calculates the net profit of the position if it is closed. The net profit\n     * includes any trading costs.\n     *\n     * @return the profit or loss of the position\n     ;\n\t\tmethod `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getGrossProfit()`;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


