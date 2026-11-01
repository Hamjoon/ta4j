
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_Test {

    private CostModel transactionModel;

    private CostModel holdingModel;

    private Trade entryTrade;

    private Trade exitTrade;

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

    @Test
    void whenPositionIsOpened_getGrossReturnShouldReturnZero() {
        // GIVEN – an opened position (entry only, no exit)
        Position openedPosition = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        // creates the entry trade
        openedPosition.operate(0);
        // WHEN
        Num grossReturn = openedPosition.getGrossReturn();
        // THEN – for an opened position the method must return zero
        assertEquals(DoubleNum.valueOf(0), grossReturn, "Gross return of an opened position must be zero");
    }

    @Test
    void whenPositionIsClosed_getGrossReturnShouldDelegateToPriceBasedMethod() {
        // GIVEN – a fully closed position (entry + exit)
        Position closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
        // WHEN
        Num grossReturnNoArg = closedPosition.getGrossReturn();
        Num grossReturnWithPrice = closedPosition.getGrossReturn(exitTrade.getPricePerAsset());
        // THEN – the no‑arg version must delegate to the price‑based version
        assertEquals(grossReturnWithPrice, grossReturnNoArg, "Gross return without argument should equal gross return computed with exit price");
    }

    @Test
    void closedPositionPositiveReturn() {
        // entry 100, exit 110 → (110‑100)/100 = 0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0.10), result, "Positive gross return (10%) should be calculated correctly");
    }

    @Test
    void closedPositionNegativeReturn() {
        // entry 100, exit 90 → (90‑100)/100 = -0.10
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(90), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(-0.10), result, "Negative gross return (-10%) should be calculated correctly");
    }

    @Test
    void closedPositionZeroReturn() {
        // entry 100, exit 100 → (100‑100)/100 = 0
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, holdingModel);
        Num result = position.getGrossReturn();
        assertEquals(DoubleNum.valueOf(0), result, "Zero gross return should be returned when entry and exit prices are equal");
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
        assertEquals(DoubleNum.valueOf(0.2345), result, "Gross return with fractional prices should be calculated with full precision");
    }

    @Test
    void closedPositionWithNullExitTradeThrowsNPE() {
        // Create a position with a null exit trade (simulating a malformed closed position)
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, null, transactionModel, holdingModel);
        // The method should attempt to dereference exit and throw NPE
        assertThrows(NullPointerException.class, position::getGrossReturn, "Calling getGrossReturn on a position with a null exit trade must throw NPE");
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
        assertThrows(NullPointerException.class, position::getGrossReturn, "Null exit price should lead to NullPointerException");
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
        assertThrows(IllegalArgumentException.class, position::getGrossReturn, "Passing a null finalPrice to the price‑based overload must throw IllegalArgumentException");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:4: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:5: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:10: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:12: error: package org.mockito does not exist
import static org.mockito.Mockito.*;
                         ^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:13: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:16: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:18: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:15: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:18: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:29: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:49: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(0), grossReturn, "Gross return of an opened position must be zero");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:60: error: cannot find symbol
        assertEquals(grossReturnWithPrice, grossReturnNoArg, "Gross return without argument should equal gross return computed with exit price");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:70: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(0.10), result, "Positive gross return (10%) should be calculated correctly");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:80: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(-0.10), result, "Negative gross return (-10%) should be calculated correctly");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:90: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(0), result, "Zero gross return should be returned when entry and exit prices are equal");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:100: error: cannot find symbol
        assertEquals(NaN, result, "Gross return should be NaN when exit price is zero");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:110: error: cannot find symbol
        assertEquals(NaN, result, "Gross return should be NaN when entry price is zero");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:120: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(0.2345), result, "Gross return with fractional prices should be calculated with full precision");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:129: error: cannot find symbol
        assertThrows(NullPointerException.class, position::getGrossReturn, "Calling getGrossReturn on a position with a null exit trade must throw NPE");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,position::[...]eturn,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:135: error: cannot find symbol
        Trade mockedExit = mock(Trade.class);
                           ^
  symbol:   method mock(Class<Trade>)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:136: error: cannot find symbol
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        ^
  symbol:   method when(Num)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:141: error: cannot find symbol
        assertThrows(NullPointerException.class, position::getGrossReturn, "Null exit price should lead to NullPointerException");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,position::[...]eturn,String)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:147: error: cannot find symbol
        Trade mockedExit = mock(Trade.class);
                           ^
  symbol:   method mock(Class<Trade>)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:148: error: cannot find symbol
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        ^
  symbol:   method when(Num)
  location: class Position_getGrossReturn_Test
src/test/java/org/ta4j/core/Position_getGrossReturn_Test.java:153: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, position::getGrossReturn, "Passing a null finalPrice to the price‑based overload must throw IllegalArgumentException");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,position::[...]eturn,String)
  location: class Position_getGrossReturn_Test
26 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getGrossReturn()': '\n     * Calculates the gross return of the position if it is closed. The gross return\n     * excludes any trading costs (and includes the base).\n     *\n     * @return the gross return of the position in percent\n     * @see #getGrossReturn(Num)\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.Trade org.ta4j.core.Position.exit;\n\trelated functions:\n\t\tmethod `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getGrossReturn()`;\n\t\tmethod `public Trade getExit()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: \n     * @return the exit {@link Trade trade} of the position\n     ;\n\t\tmethod `public Num getProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: \n     * Calculates the net profit of the position if it is closed. The net profit\n     * includes any trading costs.\n     *\n     * @return the profit or loss of the position\n     ;\n\t\tmethod `public Num getGrossProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: \n     * Calculates the gross profit of the position if it is closed. The gross profit\n     * excludes any trading costs.\n     *\n     * @return the gross profit of the position\n     ;\n\t\tmethod `public Num getGrossProfit(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn()`, api document: \n     * Calculates the gross profit of the position. The gross profit excludes any\n     * trading costs.\n     *\n     * @param finalPrice the price of the final bar to be considered (if position is\n     *                   open)\n     * @return the profit or loss of the position\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


