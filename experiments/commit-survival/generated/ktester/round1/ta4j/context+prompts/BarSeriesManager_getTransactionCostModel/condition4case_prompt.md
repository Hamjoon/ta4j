As a professional Java software testing expert, your task is to generate test cases for a focal method.

You will be provided with:
1. The focal method that needs testing.
2. Basic information of the target class to which the focus method belongs, including fields and methods.

Ensure the generated test cases meet the following requirements:

Test Coverage Requirements:
- Create test cases to cover all conditional branches in the target method, including `if`, `else if`, `else`, `switch`, and `case` statements.

Output Format:
- Organize the test cases by its functions logically.
- Include input parameters, expected output, and a description for each test case.
- Use empty list `[]` for the input when a test case does not require input parameters.
- Provide an expected return value for each test case, or specify the behavior that should be verified if there's no return value.
- Clearly describe the intention of each test case in the description.

Follow the JSON format provided here for the output:
```json
[
    {
        "group": "test name",
        "cases": [
            {
                "input": [
                    {
                        "parameter": "param name",
                        "value": "param value"
                    }
                ],
                "expected": "expected exception or behavior",
                "description": "test scenario description"
            }
        ]
    },
    {...}
]
```
@input{focal method and target class}
Here is the code of focal method, `getTransactionCostModel()`, with the focal class `BarSeriesManager`:
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
    private final CostModel transactionCostModel;
    public CostModel getTransactionCostModel() {
        return transactionCostModel;
    }
public BarSeries getBarSeries();
    public CostModel getHoldingCostModel();
    public TradingRecord run(Strategy strategy);
    public TradingRecord run(Strategy strategy, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType);
    public TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount);
    public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount, int startIndex, int finishIndex);
}
```

@output{test cases}: output the test cases in the JSON format: