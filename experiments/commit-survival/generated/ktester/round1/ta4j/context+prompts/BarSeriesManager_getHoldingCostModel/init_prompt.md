You are tasked with creating a framework for a test class in Java. Now analyze the following information carefully:

1. The method that needs to be tested and its parameters.
2. The constructor of the target class and its parameters.
3. Any dependencies or external resources used by the target class.

Based on this analysis, complete the test class template in the provided structure:

1. Initialize necessary fields and dependencies within setupBeforeAll() and setupBeforeEach().
2. Handle any required cleanup in teardownAfterEach() and teardownAfterAll().
3. Make sure not to invoke any private methods or fields of the target class and dependent class.
4. Refer to the "given" phase in the existing test class for guidance.
5. Add necessary import statements and ensure there are no syntax errors in the generated code.

@input{focal method and target class}
Here is the code of focal method, `getHoldingCostModel()`, with the focal class `BarSeriesManager_getHoldingCostModel_Test`:
```Java
package org.ta4j.core.backtest;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.Strategy;
public class BarSeriesManager {
    private final CostModel holdingCostModel;
    public CostModel getHoldingCostModel() {
        return holdingCostModel;
    }
public BarSeries getBarSeries();
    public CostModel getTransactionCostModel();
    public TradingRecord run(Strategy strategy);
    public TradingRecord run(Strategy strategy, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType);
    public TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex);
}
```


@input{context information}

api document of class org.ta4j.core.backtest.BarSeriesManager: 

 * A manager for {@link BarSeries} objects used for backtesting. Allows to run a
 * {@link Strategy trading strategy} over the managed bar series.
 

constructors for class `org.ta4j.core.backtest.BarSeriesManager`: 
params: org.ta4j.core.BarSeries barSeries 
body:
```java
    public BarSeriesManager(BarSeries barSeries) {
        this(barSeries, new ZeroCostModel(), new ZeroCostModel(), new TradeOnNextOpenModel());
    }

```
params: org.ta4j.core.BarSeries barSeries 
org.ta4j.core.backtest.TradeExecutionModel tradeExecutionModel 
body:
```java
    public BarSeriesManager(BarSeries barSeries, TradeExecutionModel tradeExecutionModel) {
        this(barSeries, new ZeroCostModel(), new ZeroCostModel(), tradeExecutionModel);
    }

```
params: org.ta4j.core.BarSeries barSeries 
org.ta4j.core.analysis.cost.CostModel transactionCostModel 
org.ta4j.core.analysis.cost.CostModel holdingCostModel 
body:
```java
    public BarSeriesManager(BarSeries barSeries, CostModel transactionCostModel, CostModel holdingCostModel) {
        this.barSeries = barSeries;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
        this.tradeExecutionModel = new TradeOnNextOpenModel();
    }

```
params: org.ta4j.core.BarSeries barSeries 
org.ta4j.core.analysis.cost.CostModel transactionCostModel 
org.ta4j.core.analysis.cost.CostModel holdingCostModel 
org.ta4j.core.backtest.TradeExecutionModel tradeExecutionModel 
body:
```java
    public BarSeriesManager(BarSeries barSeries, CostModel transactionCostModel, CostModel holdingCostModel,
            TradeExecutionModel tradeExecutionModel) {
        this.barSeries = barSeries;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
        this.tradeExecutionModel = tradeExecutionModel;
    }

```

parameters in constructors and focal method: 
class `org.ta4j.core.BarSeries`:
api document : 
 * A {@code BarSeries} is a sequence of {@link Bar bars} separated by a
 * predefined period (e.g. 15 minutes, 1 day, etc.).
 * 
 * Notably, it can be:
 * 
 * <ul>
 * <li>the base of {@link Indicator indicator} calculations
 * <li>constrained between beginning and ending indices (e.g. for some
 * backtesting cases)
 * <li>limited to a fixed number of bars (e.g. for actual trading)
 * </ul>
 
constructor:
```java
```
class `org.ta4j.core.backtest.TradeExecutionModel`:
api document : 
 * An execution model for {@link BarSeriesManager} objects.
 *
 * Used for backtesting. Instructs {@link BarSeriesManager} on how to execute
 * trades.
 
constructor:
```java
```
class `org.ta4j.core.analysis.cost.CostModel`:
api document : 
 * With the {@code CostModel}, we can include trading costs that may be incurred
 * when opening or closing a position.
 
constructor:
```java
```



@input{test class template}
```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
// Add other imports if necessary

@ExtendWith(MockitoExtension.class)
class BarSeriesManager_getHoldingCostModel_Test {
    @BeforeAll
    static void setupBeforeAll() {
    }

    @BeforeEach
    void setupBeforeEach() {
    }

    @AfterEach
    void teardownAfterEach() {
    }

    @AfterAll
    static void teardownAfterAll() {
    }
}
```

@output{initial test class}: now consider the invoke examples output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.