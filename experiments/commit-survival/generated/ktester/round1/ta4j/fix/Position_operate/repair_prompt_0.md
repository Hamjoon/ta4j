
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
import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.num.NaN.NaN;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class Position_operate_Test {

    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;

    private CostModel holdingModel;

    @BeforeEach
    void setupBeforeEach() {
        // Simple linear cost models – they are deterministic and cheap.
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);
    }

    @Test
    void whenNewPositionOperate_thenCreatesBuyEntryWithNaNValues() {
        // given
        // default starting type = BUY
        Position position = new Position();
        // when
        position.operate(0);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, position.getEntry(), "A newly created Position should generate a BUY entry trade with NaN price/amount");
    }

    @Test
    void whenPositionAlreadyOpenedOperate_thenCreatesComplementaryExit() {
        // given
        // BUY entry expected
        Position position = new Position();
        // creates entry
        position.operate(0);
        // when
        // should create SELL exit
        position.operate(1);
        // then
        Trade expectedExit = Trade.sellAt(1, NaN, NaN);
        assertEquals(expectedExit, position.getExit(), "After an entry trade, a second operate() must create the complementary exit trade");
    }

    @Test
    void whenOperateCalledOnClosedPosition_itDoesNotAlterExistingTrades() {
        // given
        // BUY entry
        Position position = new Position();
        // entry
        position.operate(0);
        // exit – position now closed
        position.operate(1);
        Trade entryBefore = position.getEntry();
        Trade exitBefore = position.getExit();
        // when – calling operate again should be a no‑op
        position.operate(2);
        // then
        assertEquals(entryBefore, position.getEntry(), "Entry trade must stay unchanged after the position is closed");
        assertEquals(exitBefore, position.getExit(), "Exit trade must stay unchanged after the position is closed");
    }

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
        assertEquals(expectedEntry, position.getEntry(), "A SELL‑starting Position must create a SELL entry with the supplied price/amount");
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
        assertEquals(expectedEntry, position.getEntry(), "A BUY‑starting Position must create a BUY entry with the supplied price/amount");
    }

    @Test
    void whenOpenedSellPositionOperateAgainWithPriceAndAmount_thenCreatesBuyExit() {
        // given
        Position position = new Position(TradeType.SELL, transactionModel, holdingModel);
        // entry (SELL)
        position.operate(0, DoubleNum.valueOf(3), DoubleNum.valueOf(2));
        // when – second call should create the complementary BUY exit
        position.operate(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2));
        // then
        Trade expectedExit = Trade.buyAt(4, DoubleNum.valueOf(3.5), DoubleNum.valueOf(2), transactionModel);
        assertEquals(expectedExit, position.getExit(), "After a SELL entry, the next operate() must create a BUY exit with the given price/amount");
    }

    @Test
    void whenOpenedBuyPositionOperateAgainWithPriceAndAmount_thenCreatesSellExit() {
        // given
        Position position = new Position(TradeType.BUY, transactionModel, holdingModel);
        // entry (BUY)
        position.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        // when – second call should create the complementary SELL exit
        position.operate(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1));
        // then
        Trade expectedExit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        assertEquals(expectedExit, position.getExit(), "After a BUY entry, the next operate() must create a SELL exit with the given price/amount");
    }

    @Test
    void whenOperateCalledWithNaNValues_itFallsBackToDefaultBehaviour() {
        // given
        // BUY start
        Position position = new Position();
        // when
        position.operate(0, NaN, NaN);
        // then
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, position.getEntry(), "Passing NaN for price/amount must behave like the simple operate(int) overload");
    }

    /**
     * Parameterised test for the three “valid scenarios” described in the JSON.
     *
     * <ul>
     *   <li>index 5 – first call creates the entry trade.</li>
     *   <li>index 0 – entry trade at index 0.</li>
     *   <li>index 10 – after a previous entry, creates the exit trade.</li>
     * </ul>
     */
    @ParameterizedTest(name = "operate({0}) on a Position should return expected Trade")
    @CsvSource({ // index,   preSetup (none=0, entryFirst=1), expectedType (BUY/SELL)
    // fresh position, entry at 5
    "5, 0, BUY", // fresh position, entry at 0
    "0, 0, BUY", // entry already created, exit at 10
    "10, 1, SELL" })
    void whenOperateValidScenarios_returnExpectedTrade(int index, int preSetup, String expectedType) {
        // given
        // default BUY start
        Position position = new Position();
        // optional pre‑setup: create an entry at index 5 when required
        if (preSetup == 1) {
            // creates entry (BUY) at 5
            position.operate(5);
        }
        // when
        Trade result = position.operate(index);
        // then
        Trade expected;
        if ("BUY".equals(expectedType)) {
            expected = Trade.buyAt(index, NaN, NaN);
        } else {
            expected = Trade.sellAt(index, NaN, NaN);
        }
        assertEquals(expected, result, () -> "operate(" + index + ") should return a " + expectedType + " trade");
    }

    @Test
    void whenOperateWithNegativeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(-1), "Negative index must cause IndexOutOfBoundsException");
    }

    @Test
    void whenOperateWithHugeIndex_throwsIndexOutOfBoundsException() {
        Position position = new Position();
        // Assuming a realistic series size is far smaller than 1000;
        // the implementation should guard against out‑of‑range indexes.
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(1000), "Index far beyond series size must cause IndexOutOfBoundsException");
    }

    @Test
    void whenOperateWithNullInteger_throwsIllegalArgumentException() throws NoSuchMethodException {
        // The method signature uses primitive int, so we have to invoke it via reflection
        // to simulate a “null” argument (which results in an IllegalArgumentException).
        Method method = Position.class.getMethod("operate", int.class);
        Position position = new Position();
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(position, new Object[] { null }), "Invoking operate(int) with null via reflection should wrap an IllegalArgumentException");
        assertTrue(thrown.getCause() instanceof IllegalArgumentException, "Root cause must be IllegalArgumentException");
    }

    @Test
    void whenPositionIsNew_operateReturnsNull() {
        // given – a brand‑new Position (no entry yet)
        Position position = new Position();
        // when
        Trade result = position.operate(2);
        // then
        assertNull(result, "operate on a NEW position should return null (no trade created yet)");
    }

    @Test
    void whenPositionAlreadyClosed_operateReturnsNull() {
        // given – create and close a position
        Position position = new Position();
        // entry
        position.operate(0);
        // exit – now closed
        position.operate(1);
        // when
        Trade result = position.operate(12);
        // then
        assertNull(result, "operate on a CLOSED position must return null");
    }

    @Test
    void whenIndexInsideOpenedPositionButNotEntryOrExit_operateReturnsNull() {
        // given – only entry trade exists
        Position position = new Position();
        // entry at index 3
        position.operate(3);
        // when – call operate with an intermediate index (neither entry nor exit)
        Trade result = position.operate(7);
        // then
        assertNull(result, "operate on an intermediate index of an opened position must return null");
    }

    @Test
    void whenOperateDelegatesToOverloadedMethod_resultMatchesOverload() {
        // given
        Position position = new Position();
        int testIndex = 4;
        // when – call the simple overload
        Trade simpleResult = position.operate(testIndex);
        // then – call the overloaded version explicitly and compare
        Trade overloadedResult = position.operate(testIndex, NaN, NaN);
        assertEquals(overloadedResult, simpleResult, "operate(int) must delegate to operate(int, NaN, NaN) and return the same Trade");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_operate_Test.java:4: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_operate_Test.java:5: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_operate_Test.java:6: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/Position_operate_Test.java:15: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/Position_operate_Test.java:16: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.CsvSource;
                                        ^
src/test/java/org/ta4j/core/Position_operate_Test.java:19: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/Position_operate_Test.java:20: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_operate_Test.java:22: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.CsvSource;
                                        ^
src/test/java/org/ta4j/core/Position_operate_Test.java:23: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_operate_Test.java:26: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/Position_operate_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_operate_Test.java:24: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_operate_Test.java:26: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/Position_operate_Test.java:34: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:159: error: cannot find symbol
    @ParameterizedTest(name = "operate({0}) on a Position should return expected Trade")
     ^
  symbol:   class ParameterizedTest
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:160: error: cannot find symbol
    @CsvSource({ // index,   preSetup (none=0, entryFirst=1), expectedType (BUY/SELL)
     ^
  symbol:   class CsvSource
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:50: error: cannot find symbol
        assertEquals(expectedEntry, position.getEntry(), "A newly created Position should generate a BUY entry trade with NaN price/amount");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:65: error: cannot find symbol
        assertEquals(expectedExit, position.getExit(), "After an entry trade, a second operate() must create the complementary exit trade");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:82: error: cannot find symbol
        assertEquals(entryBefore, position.getEntry(), "Entry trade must stay unchanged after the position is closed");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:83: error: cannot find symbol
        assertEquals(exitBefore, position.getExit(), "Exit trade must stay unchanged after the position is closed");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:96: error: cannot find symbol
        assertEquals(expectedEntry, position.getEntry(), "A SELL‑starting Position must create a SELL entry with the supplied price/amount");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:109: error: cannot find symbol
        assertEquals(expectedEntry, position.getEntry(), "A BUY‑starting Position must create a BUY entry with the supplied price/amount");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:122: error: cannot find symbol
        assertEquals(expectedExit, position.getExit(), "After a SELL entry, the next operate() must create a BUY exit with the given price/amount");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:135: error: cannot find symbol
        assertEquals(expectedExit, position.getExit(), "After a BUY entry, the next operate() must create a SELL exit with the given price/amount");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:147: error: cannot find symbol
        assertEquals(expectedEntry, position.getEntry(), "Passing NaN for price/amount must behave like the simple operate(int) overload");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:183: error: cannot find symbol
        assertEquals(expected, result, () -> "operate(" + index + ") should return a " + expectedType + " trade");
        ^
  symbol:   method assertEquals(Trade,Trade,()->"opera[...]rade")
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:189: error: cannot find symbol
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(-1), "Negative index must cause IndexOutOfBoundsException");
        ^
  symbol:   method assertThrows(Class<IndexOutOfBoundsException>,()->positi[...]e(-1),String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:197: error: cannot find symbol
        assertThrows(IndexOutOfBoundsException.class, () -> position.operate(1000), "Index far beyond series size must cause IndexOutOfBoundsException");
        ^
  symbol:   method assertThrows(Class<IndexOutOfBoundsException>,()->positi[...]1000),String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:206: error: cannot find symbol
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(position, new Object[] { null }), "Invoking operate(int) with null via reflection should wrap an IllegalArgumentException");
                                           ^
  symbol:   method assertThrows(Class<InvocationTargetException>,()->method[...]ull}),String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:207: error: cannot find symbol
        assertTrue(thrown.getCause() instanceof IllegalArgumentException, "Root cause must be IllegalArgumentException");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:217: error: cannot find symbol
        assertNull(result, "operate on a NEW position should return null (no trade created yet)");
        ^
  symbol:   method assertNull(Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:231: error: cannot find symbol
        assertNull(result, "operate on a CLOSED position must return null");
        ^
  symbol:   method assertNull(Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:243: error: cannot find symbol
        assertNull(result, "operate on an intermediate index of an opened position must return null");
        ^
  symbol:   method assertNull(Trade,String)
  location: class Position_operate_Test
src/test/java/org/ta4j/core/Position_operate_Test.java:255: error: cannot find symbol
        assertEquals(overloadedResult, simpleResult, "operate(int) must delegate to operate(int, NaN, NaN) and return the same Trade");
        ^
  symbol:   method assertEquals(Trade,Trade,String)
  location: class Position_operate_Test
34 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method operate(int)': '\n     * Operates the position at the index-th position.\n     *\n     * @param index the bar index\n     * @return the trade\n     * @see #operate(int, Num, Num)\n     ', 'return type': 'org.ta4j.core.Trade', 'dependent classes': 'class `org.ta4j.core.num.NaN`:\n\tdependent fields:\n\t\torg.ta4j.core.num.Num org.ta4j.core.num.NaN.NaN;\n\t\nclass `org.ta4j.core.TradingRecord`:\n\trelated functions:\n\t\tmethod ` void operate(int)` returns `void`, related with `org.ta4j.core.Position.operate(int)`, api document: \n     * Places a trade in the trading record.\n     *\n     * @param index the index to place the trade\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


