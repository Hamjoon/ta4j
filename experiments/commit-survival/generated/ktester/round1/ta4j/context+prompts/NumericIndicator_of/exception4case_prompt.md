You are a professional software tester. Write test cases for a focal method in a target class.

**Instructions:**
1. Analyze method behavior and identify ALL possible exception scenarios
2. Design test cases that trigger each identified exception
3. Follow existing test case structure, Avoid duplication and create new test cases for uncovered exception scenarios

Test Case Format:
1. Group test cases by test function name
2. Each test case MUST include:
   - Input parameters with values (use empty list `[]` if none)
   - Expected exception or observable behavior
   - Clear description of test scenario
3. Return empty array if no exception scenarios found

Output Schema (STRICT JSON format):
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
Here is the code of focal method, `of(Indicator<Num>)`, with the focal class `NumericIndicator`:
```Java
package org.ta4j.core.indicators.numeric;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.rules.UnderIndicatorRule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
public class NumericIndicator implements Indicator<Num> {
    protected final Indicator<Num> delegate;
    public static NumericIndicator of(Indicator<Num> delegate) {
        return new NumericIndicator(delegate);
    }
public NumericIndicator abs();
    public static NumericIndicator closePrice(BarSeries bs);
    public Rule crossedOver(Number n);
    public Rule crossedOver(Indicator<Num> other);
    public Rule crossedUnder(Number n);
    public Rule crossedUnder(Indicator<Num> other);
    public Indicator<Num> delegate();
    public NumericIndicator dividedBy(Number n);
    public NumericIndicator dividedBy(Indicator<Num> other);
    public NumericIndicator ema(int barCount);
    @Override
public BarSeries getBarSeries();
    @Override
public int getUnstableBars();
    @Override
public Num getValue(int index);
    public NumericIndicator highest(int barCount);
    public Rule isGreaterThan(Number n);
    public Rule isGreaterThan(Indicator<Num> other);
    public Rule isLessThan(Number n);
    public Rule isLessThan(Indicator<Num> other);
    public NumericIndicator lowest(int barCount);
    public NumericIndicator max(Number n);
    public NumericIndicator max(Indicator<Num> other);
    public NumericIndicator min(Number n);
    public NumericIndicator min(Indicator<Num> other);
    public NumericIndicator minus(Number n);
    public NumericIndicator minus(Indicator<Num> other);
    public NumericIndicator multipliedBy(Number n);
    public NumericIndicator multipliedBy(Indicator<Num> other);
    public NumericIndicator plus(Number n);
    public NumericIndicator plus(Indicator<Num> other);
    public Indicator<Num> previous();
    public NumericIndicator previous(int barCount);
    public NumericIndicator sma(int barCount);
    public NumericIndicator sqrt();
    public NumericIndicator squared();
    public NumericIndicator stddev(int barCount);
    @Override
public String toString();
    public static NumericIndicator volume(BarSeries bs);
}
```
@input{existing test case}
here's the existing test cases:
```json
<cases_json>
```

@output{test cases}: output the test cases in the JSON format: