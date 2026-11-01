
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.ta4j.core.Strategy;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class BarSeriesManager_getBarSeries_Test {

    /**
     * Mocked BarSeries used for all tests.
     */
    @Mock
    private BarSeries mockBarSeries;

    /**
     * Instance under test.
     */
    private BarSeriesManager manager;

    /**
     * Simple {@link BarSeries} implementation used for the majority of tests.
     */
    private BarSeries simpleSeries;

    /**
     * A minimal concrete subclass of {@link BaseBarSeries} used to verify that
     * custom subclasses are preserved by {@link BarSeriesManager}.
     */
    private static class MyBarSeries extends BaseBarSeries {

        MyBarSeries() {
            super("mySeries");
        }
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise Mockito annotations (required when not using @InjectMocks)
        MockitoAnnotations.openMocks(this);
        // Use the simplest constructor – it internally delegates to the full one.
        manager = new BarSeriesManager(mockBarSeries);
        // Use a plain BaseBarSeries instance – it is sufficient for the tests.
        simpleSeries = new BaseBarSeries("simpleSeries");
        manager = new BarSeriesManager(simpleSeries);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset the mock to avoid interaction bleed‑over between tests.
        reset(mockBarSeries);
        // No special cleanup required.
        manager = null;
        simpleSeries = null;
    }

    @Test
    void getBarSeriesReturnsExactInstanceProvidedInConstructor() {
        BarSeries result = manager.getBarSeries();
        assertSame(simpleSeries, result, "The manager should return the exact BarSeries instance supplied to its constructor.");
    }

    @Test
    void getBarSeriesWorksWithFullConstructor() {
        // GIVEN: a manager created with all constructor arguments
        CostModel transactionCostModel = mock(CostModel.class);
        CostModel holdingCostModel = mock(CostModel.class);
        TradeExecutionModel executionModel = mock(TradeExecutionModel.class);
        BarSeriesManager fullManager = new BarSeriesManager(mockBarSeries, transactionCostModel, holdingCostModel, executionModel);
        // WHEN
        BarSeries result = fullManager.getBarSeries();
        // THEN
        assertSame(mockBarSeries, result);
    }

    @Test
    void getBarSeriesReturnsNullWhenConstructedWithNullSeries() {
        BarSeriesManager nullManager = new BarSeriesManager(null);
        BarSeries result = nullManager.getBarSeries();
        assertNull(result, "When the manager is constructed with a null BarSeries, getBarSeries() should return null.");
    }

    @Test
    void getBarSeriesRemainsSameAfterRunInvocation() {
        // GIVEN a no‑op strategy (always returns false)
        Strategy noOpStrategy = mock(Strategy.class);
        when(noOpStrategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(false);
        // WHEN we run the strategy (the actual implementation is not under test)
        manager.run(noOpStrategy);
        // THEN the internal BarSeries reference must stay unchanged
        BarSeries result = manager.getBarSeries();
        assertSame(mockBarSeries, result);
    }

    @Test
    void getBarSeriesPreservesCustomSubclassInstance() {
        MyBarSeries mySeries = new MyBarSeries();
        BarSeriesManager customManager = new BarSeriesManager(mySeries);
        BarSeries result = customManager.getBarSeries();
        assertSame(mySeries, result, "The manager must return the exact custom subclass instance.");
        assertTrue(result instanceof MyBarSeries, "The returned instance should be of type MyBarSeries.");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:5: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.AfterEach;
                            ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:6: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.BeforeEach;
                            ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:7: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.Test;
                            ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:8: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:10: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:14: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:19: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:15: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:17: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:19: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:20: error: cannot find symbol
@RunWith(MockitoJUnitRunner.class)
         ^
  symbol: class MockitoJUnitRunner
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:26: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:50: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:61: error: cannot find symbol
    @AfterEach
     ^
  symbol:   class AfterEach
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:53: error: cannot find symbol
        MockitoAnnotations.openMocks(this);
        ^
  symbol:   variable MockitoAnnotations
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:64: error: cannot find symbol
        reset(mockBarSeries);
        ^
  symbol:   method reset(BarSeries)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:73: error: cannot find symbol
        assertSame(simpleSeries, result, "The manager should return the exact BarSeries instance supplied to its constructor.");
        ^
  symbol:   method assertSame(BarSeries,BarSeries,String)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:79: error: cannot find symbol
        CostModel transactionCostModel = mock(CostModel.class);
                                         ^
  symbol:   method mock(Class<CostModel>)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:80: error: cannot find symbol
        CostModel holdingCostModel = mock(CostModel.class);
                                     ^
  symbol:   method mock(Class<CostModel>)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:81: error: cannot find symbol
        TradeExecutionModel executionModel = mock(TradeExecutionModel.class);
                                             ^
  symbol:   method mock(Class<TradeExecutionModel>)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:86: error: cannot find symbol
        assertSame(mockBarSeries, result);
        ^
  symbol:   method assertSame(BarSeries,BarSeries)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:93: error: cannot find symbol
        assertNull(result, "When the manager is constructed with a null BarSeries, getBarSeries() should return null.");
        ^
  symbol:   method assertNull(BarSeries,String)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:99: error: cannot find symbol
        Strategy noOpStrategy = mock(Strategy.class);
                                ^
  symbol:   method mock(Class<Strategy>)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:100: error: cannot find symbol
        when(noOpStrategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(false);
                                        ^
  symbol:   method anyInt()
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:100: error: cannot find symbol
        when(noOpStrategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(false);
                                                  ^
  symbol:   method any(Class<TradingRecord>)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:105: error: cannot find symbol
        assertSame(mockBarSeries, result);
        ^
  symbol:   method assertSame(BarSeries,BarSeries)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:113: error: cannot find symbol
        assertSame(mySeries, result, "The manager must return the exact custom subclass instance.");
        ^
  symbol:   method assertSame(MyBarSeries,BarSeries,String)
  location: class BarSeriesManager_getBarSeries_Test
src/test/java/org/ta4j/core/backtest/BarSeriesManager_getBarSeries_Test.java:114: error: cannot find symbol
        assertTrue(result instanceof MyBarSeries, "The returned instance should be of type MyBarSeries.");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class BarSeriesManager_getBarSeries_Test
28 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.backtest.BarSeriesManager': '\n * A manager for {@link BarSeries} objects used for backtesting. Allows to run a\n * {@link Strategy trading strategy} over the managed bar series.\n ', 'api document of method getBarSeries()': '\n     * @return the managed bar series\n     ', 'return type': 'org.ta4j.core.BarSeries', 'dependent classes': 'class `org.ta4j.core.backtest.BarSeriesManager`:\n\tdependent fields:\n\t\torg.ta4j.core.BarSeries org.ta4j.core.backtest.BarSeriesManager.barSeries;\n\trelated functions:\n\t\tmethod `public BarSeries getBarSeries()` returns `org.ta4j.core.BarSeries`, related with `org.ta4j.core.backtest.BarSeriesManager.getBarSeries()`, api document: \n     * @return the managed bar series\n     ;\n\t\tmethod `public TradingRecord run(Strategy, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.getBarSeries()`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * Opens the position with a {@link TradeType} BUY trade.\n     *\n     * @param strategy    the trading strategy\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\n\t\tmethod `public TradingRecord run(Strategy, TradeType, Num, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.getBarSeries()`, api document: \n     * Runs the provided strategy over the managed series (from startIndex to\n     * finishIndex).\n     *\n     * @param strategy    the trading strategy\n     * @param tradeType   the {@link TradeType} used to open the trades\n     * @param amount      the amount used to open/close the trades\n     * @param startIndex  the start index for the run (included)\n     * @param finishIndex the finish index for the run (included)\n     * @return the trading record coming from the run\n     ;\nclass `org.ta4j.core.Strategy`:\n\trelated functions:\n\t\tmethod ` boolean shouldOperate(int, TradingRecord)` returns `boolean`, related with `org.ta4j.core.backtest.BarSeriesManager.getBarSeries()`, api document: \n     * @param index         the bar index\n     * @param tradingRecord the potentially needed trading history\n     * @return true to recommend a trade, false otherwise (no recommendation)\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


