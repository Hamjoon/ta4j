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
Here is the code of focal method, `run(Strategy, int, int)`, with the focal class `BarSeriesManager`:
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
    private final BarSeries barSeries;
    public TradingRecord run(Strategy strategy, int startIndex, int finishIndex) {
        return run(strategy, TradeType.BUY, barSeries.one(), startIndex, finishIndex);
    }
public BarSeries getBarSeries();
    public CostModel getHoldingCostModel();
    public CostModel getTransactionCostModel();
    public TradingRecord run(Strategy strategy);
    public TradingRecord run(Strategy strategy, TradeType tradeType);
    public TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex);
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
 

api document of method run(Strategy, int, int): 

     * Runs the provided strategy over the managed series (from startIndex to
     * finishIndex).
     *
     * Opens the position with a {@link TradeType} BUY trade.
     *
     * @param strategy    the trading strategy
     * @param startIndex  the start index for the run (included)
     * @param finishIndex the finish index for the run (included)
     * @return the trading record coming from the run
     

return type: 
org.ta4j.core.TradingRecord

dependent classes: 
class `org.ta4j.core.Strategy`:
	
class `org.ta4j.core.Trade.TradeType`:
	dependent fields:
		org.ta4j.core.Trade.TradeType org.ta4j.core.Trade.TradeType.BUY;
	
class `org.ta4j.core.backtest.BarSeriesManager`:
	dependent fields:
		org.ta4j.core.BarSeries org.ta4j.core.backtest.BarSeriesManager.barSeries;
	related functions:
		method `public TradingRecord run(Strategy, TradeType, int, int)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: 
     * Runs the provided strategy over the managed series (from startIndex to
     * finishIndex).
     *
     * Opens the position with a trade of {@link TradeType tradeType}.
     *
     * @param strategy    the trading strategy
     * @param tradeType   the {@link TradeType} used to open the position
     * @param startIndex  the start index for the run (included)
     * @param finishIndex the finish index for the run (included)
     * @return the trading record coming from the run
     ;
		method `public TradingRecord run(Strategy, TradeType, Num)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: 
     * Runs the provided strategy over the managed series.
     *
     * @param strategy  the trading strategy
     * @param tradeType the {@link TradeType} used to open the position
     * @param amount    the amount used to open/close the trades
     * @return the trading record coming from the run
     ;
		method `public TradingRecord run(Strategy, TradeType)` returns `org.ta4j.core.TradingRecord`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: 
     * Runs the provided strategy over the managed series.
     *
     * Opens the position with a trade of {@link TradeType tradeType}.
     *
     * @param strategy  the trading strategy
     * @param tradeType the {@link TradeType} used to open the position
     * @return the trading record coming from the run
     ;
class `org.ta4j.core.criteria.NumberOfPositionsCriterion`:
	related functions:
		method `public Num calculate(BarSeries, Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`;
class `org.ta4j.core.Indicator`:
	related functions:
		method ` Num one()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.backtest.BarSeriesManager.run(Strategy, int, int)`, api document: 
     * @return the Num of 1
     ;



@output{test class}: output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.