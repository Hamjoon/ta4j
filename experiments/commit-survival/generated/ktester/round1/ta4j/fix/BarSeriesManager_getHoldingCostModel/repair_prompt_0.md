
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class BarSeriesManager_getHoldingCostModel_Test {

    // -----------------------------------------------------------------------
    @Mock
    BarSeries // mocked BarSeries – no real data needed
    barSeries;

    @Mock
    CostModel // a custom holding‑cost model to be injected
    customHoldingCostModel;

    // instance under test
    BarSeriesManager manager;

    @Mock
    CostModel // a Mockito mock for the holding‑cost model
    mockedHoldingCostModel;

    @Test
    void whenHoldingCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN: a manager built with a custom holding‑cost model (mocked)
        manager = new BarSeriesManager(barSeries, // transaction cost model (default)
        new ZeroCostModel(), // holding cost model under test
        mockedHoldingCostModel);
        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN: the exact instance supplied at construction is returned
        assertSame(mockedHoldingCostModel, result, "The holding cost model returned by getHoldingCostModel() should be the same instance that was injected via the constructor.");
    }

    @Test
    void whenUsingDefaultConstructor_thenHoldingCostModelIsZeroCostModel() {
        // GIVEN: a manager created with the single‑argument constructor (defaults to ZeroCostModel)
        manager = new BarSeriesManager(barSeries);
        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN: the result should be an instance of ZeroCostModel
        assertTrue(result instanceof ZeroCostModel, "The default holding cost model should be an instance of ZeroCostModel.");
    }

    @Test
    void whenHoldingCostModelIsNull_thenGetterReturnsNull() {
        // GIVEN: a manager constructed with a null holding‑cost model
        manager = new BarSeriesManager(barSeries, // transaction cost model (any non‑null value)
        new ZeroCostModel(), // explicit null holding cost model
        null);
        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN: the getter must return null
        assertNull(result, "When the holding cost model is constructed as null, getHoldingCostModel() should return null.");
    }

    @Test
    void whenCustomCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN: a user‑defined CostModel implementation (anonymous class)
        CostModel customCostModel = new CostModel() {

            @Override
            public Num calculate(Num price, Num amount) {
                // Simple stub implementation – just return the price.
                return price;
            }
        };
        manager = new BarSeriesManager(barSeries, new ZeroCostModel(), customCostModel);
        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN: the exact custom instance must be returned
        assertSame(customCostModel, result, "The getter should return the exact custom CostModel instance supplied to the constructor.");
    }

    @Test
    void whenMockedCostModelProvided_thenGetterReturnsMockInstance() {
        // GIVEN: a Mockito‑mocked CostModel (already injected via @Mock field)
        manager = new BarSeriesManager(barSeries, new ZeroCostModel(), mockedHoldingCostModel);
        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN: the mock instance must be returned unchanged
        assertSame(mockedHoldingCostModel, result, "The getter should return the mocked CostModel instance without wrapping or copying it.");
    }

    @Test
    void whenReferenceReassignedAfterConstruction_thenOriginalInstanceIsReturned() {
        // GIVEN: an original CostModel instance
        CostModel original = new CostModel() {

            @Override
            public Num calculate(Num price, Num amount) {
                return price;
            }
        };
        // Hold the reference in a variable that will later be reassigned
        CostModel holder = original;
        manager = new BarSeriesManager(barSeries, new ZeroCostModel(), holder);
        // Reassign the external variable to a different object
        // any other CostModel
        holder = new ZeroCostModel();
        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN: the manager must still hold the original reference
        assertSame(original, result, "BarSeriesManager should keep the original CostModel reference even if the external variable is later reassigned.");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:4: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:5: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:6: error: package org.mockito does not exist
import org.mockito.Mock;
                  ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:7: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:12: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:14: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:18: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:15: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:16: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:18: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:22: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:26: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:33: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:46: error: cannot find symbol
        assertSame(mockedHoldingCostModel, result, "The holding cost model returned by getHoldingCostModel() should be the same instance that was injected via the constructor.");
        ^
  symbol:   method assertSame(CostModel,CostModel,String)
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:56: error: cannot find symbol
        assertTrue(result instanceof ZeroCostModel, "The default holding cost model should be an instance of ZeroCostModel.");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:68: error: cannot find symbol
        assertNull(result, "When the holding cost model is constructed as null, getHoldingCostModel() should return null.");
        ^
  symbol:   method assertNull(CostModel,String)
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:74: error: <anonymous org.ta4j.core.backtest.BarSeriesManager_getHoldingCostModel_Test$1> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel customCostModel = new CostModel() {
                                                    ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:86: error: cannot find symbol
        assertSame(customCostModel, result, "The getter should return the exact custom CostModel instance supplied to the constructor.");
        ^
  symbol:   method assertSame(CostModel,CostModel,String)
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:96: error: cannot find symbol
        assertSame(mockedHoldingCostModel, result, "The getter should return the mocked CostModel instance without wrapping or copying it.");
        ^
  symbol:   method assertSame(CostModel,CostModel,String)
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:102: error: <anonymous org.ta4j.core.backtest.BarSeriesManager_getHoldingCostModel_Test$2> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel original = new CostModel() {
                                             ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:118: error: cannot find symbol
        assertSame(original, result, "BarSeriesManager should keep the original CostModel reference even if the external variable is later reassigned.");
        ^
  symbol:   method assertSame(CostModel,CostModel,String)
  location: class BarSeriesManager_getHoldingCostModel_Test
22 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.backtest.BarSeriesManager': '\n * A manager for {@link BarSeries} objects used for backtesting. Allows to run a\n * {@link Strategy trading strategy} over the managed bar series.\n ', 'api document of method getHoldingCostModel()': '\n     * @return the holding cost model\n     ', 'return type': 'org.ta4j.core.analysis.cost.CostModel', 'dependent classes': 'class `org.ta4j.core.backtest.BarSeriesManager`:\n\tdependent fields:\n\t\torg.ta4j.core.analysis.cost.CostModel org.ta4j.core.backtest.BarSeriesManager.holdingCostModel;\n\trelated functions:\n\t\tmethod `public TradingRecord run(Strategy, TradeType, Num, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.getHoldingCostModel()`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * @param strategy    the trading strategy\n     * @param tradeType   the {@link TradeType} used to open the trades\n     * @param amount      the amount used to open/close the trades\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public CostModel getHoldingCostModel()` returns `org.ta4j.core.analysis.cost.CostModel`, related with `org.ta4j.core.backtest.BarSeriesManager.getHoldingCostModel()`, api document: \n     * @return the holding cost model\n     ;\nclass `org.ta4j.core.Strategy`:\n\trelated functions:\n\t\tmethod ` boolean shouldOperate(int, TradingRecord)` returns `boolean`, related with `org.ta4j.core.backtest.BarSeriesManager.getHoldingCostModel()`, api document: \n     * @param index         the bar index\n     * @param tradingRecord the potentially needed trading history\n     * @return true to recommend a trade, false otherwise (no recommendation)\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


