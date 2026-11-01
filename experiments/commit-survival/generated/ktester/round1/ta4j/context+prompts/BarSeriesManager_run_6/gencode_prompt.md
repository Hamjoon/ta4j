You are a professional Java software testing expert. Your task is to generate high-quality unit test code for a focal method, using **JUnit5** and **Mockito** libraries. 

You'll be provided with:
1. The focal method to test.
2. The target class's structure (fields and methods).
3. A pre-defined test class framework with `@Before`/`@After` methods.
4. Test cases in JSON format.
5. Any relevant context.

**Requirements:**

Code Quality:
- Include clear comments explaining each test scenario.
- Ensure code is clean, understandable, and maintainable.
- Do not directly access private methods or fields. **Use reflection if the focal method is private.**
- Include all necessary imports; code must be free of syntax errors.

**Test Structure:**
- Follow the given test class framework; do not modify `@Before`/`@After` methods.
- Create one test function per test case group, ensuring all cases are covered.
- Use parameterized tests where appropriate.
- Reinitialize variables inside test functions if `@Setup` values don't fit.
- Properly mock and set up dependencies.

@input{focal method and target class}
Here is the code of focal method, `run(Strategy, TradeType, Num, int, int)`, with the focal class `BarSeriesManager`:
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
    private static final Logger log = LoggerFactory.getLogger(BarSeriesManager.class);
    private final BarSeries barSeries;
    private final CostModel transactionCostModel;
    private final CostModel holdingCostModel;
    private final TradeExecutionModel tradeExecutionModel;
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex) {
        int runBeginIndex = Math.max(startIndex, barSeries.getBeginIndex());
        int runEndIndex = Math.min(finishIndex, barSeries.getEndIndex());
        if (log.isTraceEnabled()) {
            log.trace("Running strategy (indexes: {} -> {}): {} (starting with {})", runBeginIndex, runEndIndex, strategy, tradeType);
        }
        TradingRecord tradingRecord = new BaseTradingRecord(tradeType, runBeginIndex, runEndIndex, transactionCostModel, holdingCostModel);
        for (int i = runBeginIndex; i <= runEndIndex; i++) {
            // For each bar between both indexes...
            if (strategy.shouldOperate(i, tradingRecord)) {
                tradeExecutionModel.execute(i, tradingRecord, barSeries, amount);
            }
        }
        if ((!tradingRecord.isClosed()) && (runEndIndex == barSeries.getEndIndex())) {
            // If the last position is still open and there are still bars after the
            // endIndex of the barSeries, then we execute the strategy on these bars
            // to give an opportunity to close this position.
            int seriesMaxSize = Math.max(barSeries.getEndIndex() + 1, barSeries.getBarData().size());
            for (int i = runEndIndex + 1; i < seriesMaxSize; i++) {
                // For each bar after the end index of this run...
                // --> Trying to close the last position
                if (strategy.shouldOperate(i, tradingRecord)) {
                    tradeExecutionModel.execute(i, tradingRecord, barSeries, amount);
                    break;
                }
            }
        }
        return tradingRecord;
    }
public BarSeries getBarSeries();
    public CostModel getHoldingCostModel();
    public CostModel getTransactionCostModel();
    public TradingRecord run(Strategy strategy);
    public TradingRecord run(Strategy strategy, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType);
    public TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount);
}
```

@input{initial test class framework}
```Java
<initial_class>
```

@input{existing test case}
here's the existing test cases:
```json
<cases_json>
```


@input{context information}

api document of class org.ta4j.core.backtest.BarSeriesManager: 

 * A manager for {@link BarSeries} objects used for backtesting. Allows to run a
 * {@link Strategy trading strategy} over the managed bar series.
 

api document of method run(Strategy, TradeType, Num, int, int): 

     * Runs the provided strategy over the managed series (from startIndex to
     * finishIndex).
     *
     * @param strategy    the trading strategy
     * @param tradeType   the {@link TradeType} used to open the trades
     * @param amount      the amount used to open/close the trades
     * @param startIndex  the start index for the run (included)
     * @param finishIndex the finish index for the run (included)
     * @return the trading record coming from the run
     

return type: 
org.ta4j.core.TradingRecord

dependent classes: 
class `org.ta4j.core.Strategy`:
	
class `org.ta4j.core.Trade.TradeType`:
	
class `org.ta4j.core.num.Num`:
	
class `org.ta4j.core.backtest.BarSeriesManager`:
	dependent fields:
		org.ta4j.core.BarSeries org.ta4j.core.backtest.BarSeriesManager.barSeries;
		org.ta4j.core.analysis.cost.CostModel org.ta4j.core.backtest.BarSeriesManager.transactionCostModel;
		org.ta4j.core.backtest.TradeExecutionModel org.ta4j.core.backtest.BarSeriesManager.tradeExecutionModel;
		org.ta4j.core.analysis.cost.CostModel org.ta4j.core.backtest.BarSeriesManager.holdingCostModel;
		org.slf4j.Logger org.ta4j.core.backtest.BarSeriesManager.log;
	related functions:
		method `public TradingRecord run(Strategy, TradeType, Num)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, Num, int, int)`, api document: 
     * Runs the provided strategy over the managed series.
     *
     * @param strategy  the trading strategy
     * @param tradeType the {@link TradeType} used to open the position
     * @param amount    the amount used to open/close the trades
     * @return the trading record coming from the run
     ;
class `org.ta4j.core.indicators.CachedIndicator`:
	related functions:
		method `public synchronized T getValue(int)` returns `T`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, Num, int, int)`;
class `org.ta4j.core.BaseBarSeries`:
	related functions:
		method `public Bar getBar(int)` returns `org.ta4j.core.Bar`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, Num, int, int)`;
class `org.ta4j.core.BaseStrategy`:
	related functions:
		method `protected void traceShouldEnter(int, boolean)` returns `void`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, Num, int, int)`, api document: 
     * Traces the {@code shouldEnter()} method calls.
     *
     * @param index the bar index
     * @param enter true if the strategy should enter, false otherwise
     ;
class `org.ta4j.core.TradingRecord`:
	related functions:
		method ` int getStartIndex(BarSeries)` returns `int`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, Num, int, int)`, api document: 
     * @param series the bar series, not null
     * @return the {@link #getStartIndex()} if not null and greater than
     *         {@link BarSeries#getBeginIndex()}, otherwise
     *         {@link BarSeries#getBeginIndex()}
     ;
class `org.ta4j.core.analysis.CashFlow`:
	related functions:
		method `private void fillToTheEnd(int)` returns `void`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, TradeType, Num, int, int)`, api document: 
     * Pads {@link #values} with its last value up until {@code endIndex}.
     * 
     * @param endIndex the end index
     ;



@output{test class}: output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.