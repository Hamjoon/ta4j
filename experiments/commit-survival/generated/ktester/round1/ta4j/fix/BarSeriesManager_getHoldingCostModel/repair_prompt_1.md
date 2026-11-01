
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

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

    /**
     * Simple {@link CostModel} implementation used in a few tests.
     */
    private static class SimpleCostModel implements CostModel {

        @Override
        public Num calculate(Num price, Num amount) {
            // Very simple implementation – just return the price.
            return price;
        }

        /**
         * The {@code CostModel} interface in ta4j declares a custom {@code equals}
         *  method that takes another {@code CostModel}.  For the purpose of the tests
         *  we implement it as reference equality.
         */
        @Override
        public boolean equals(CostModel other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    @Test
    void whenHoldingCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a manager built with an explicit holding‑cost model
        CostModel explicitHoldingCost = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, // transaction‑cost model (default)
        new ZeroCostModel(), // holding‑cost model under test
        explicitHoldingCost);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the exact instance supplied at construction is returned
        assertSame(explicitHoldingCost, result);
    }

    @Test
    void whenUsingDefaultConstructor_thenHoldingCostModelIsZeroCostModel() {
        // GIVEN a manager created with the single‑argument constructor (defaults to ZeroCostModel)
        BarSeriesManager manager = new BarSeriesManager(barSeries);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the result should be an instance of ZeroCostModel
        assertTrue(result instanceof ZeroCostModel);
    }

    @Test
    void whenHoldingCostModelIsNull_thenGetterReturnsNull() {
        // GIVEN a manager constructed with a null holding‑cost model
        BarSeriesManager manager = new BarSeriesManager(barSeries, // transaction‑cost model (any non‑null value)
        new ZeroCostModel(), // explicit null holding‑cost model
        null);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the getter must return null
        assertNull(result);
    }

    @Test
    void whenCustomCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a user‑defined CostModel implementation
        CostModel customCostModel = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), customCostModel);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the exact custom instance must be returned
        assertSame(customCostModel, result);
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
        // GIVEN an original CostModel instance
        CostModel original = new SimpleCostModel();
        // Hold the reference in a variable that will later be reassigned
        CostModel holder = original;
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), holder);
        // Reassign the external variable to a different object
        holder = new ZeroCostModel();
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the manager must still hold the original reference
        assertSame(original, result);
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:9: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:10: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:13: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:11: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:13: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:17: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:21: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:28: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getHoldingCostModel_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:35: error: SimpleCostModel is not abstract and does not override abstract method calculate(Position) in CostModel
    private static class SimpleCostModel implements CostModel {
                   ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getHoldingCostModel_Test.java:112: error: incompatible types: CostModel cannot be converted to String
        assertSame(mockedHoldingCostModel, result, "The getter should return the mocked CostModel instance without wrapping or copying it.");
                   ^
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
10 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.backtest.BarSeriesManager': '\n * A manager for {@link BarSeries} objects used for backtesting. Allows to run a\n * {@link Strategy trading strategy} over the managed bar series.\n ', 'api document of method getHoldingCostModel()': '\n     * @return the holding cost model\n     ', 'return type': 'org.ta4j.core.analysis.cost.CostModel', 'dependent classes': 'class `org.ta4j.core.backtest.BarSeriesManager`:\n\tdependent fields:\n\t\torg.ta4j.core.analysis.cost.CostModel org.ta4j.core.backtest.BarSeriesManager.holdingCostModel;\n\trelated functions:\n\t\tmethod `public TradingRecord run(Strategy, TradeType, Num, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.getHoldingCostModel()`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * @param strategy    the trading strategy\n     * @param tradeType   the {@link TradeType} used to open the trades\n     * @param amount      the amount used to open/close the trades\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public CostModel getHoldingCostModel()` returns `org.ta4j.core.analysis.cost.CostModel`, related with `org.ta4j.core.backtest.BarSeriesManager.getHoldingCostModel()`, api document: \n     * @return the holding cost model\n     ;\nclass `org.ta4j.core.Strategy`:\n\trelated functions:\n\t\tmethod ` boolean shouldOperate(int, TradingRecord)` returns `boolean`, related with `org.ta4j.core.backtest.BarSeriesManager.getHoldingCostModel()`, api document: \n     * @param index         the bar index\n     * @param tradingRecord the potentially needed trading history\n     * @return true to recommend a trade, false otherwise (no recommendation)\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


