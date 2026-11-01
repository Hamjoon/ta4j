As a professional Java software testing expert, your task is to generate test cases for a focal method.

Guidelines:
1. Analyze the method's functionality and identify possible input combinations including normal cases and boundary conditions.
2. Review the provided existing test cases. Avoid duplication and create new test cases for untested input combinations.

Context Provided:
1. The focal method that needs testing.
2. Relevant code and metadata for the class containing the method, including fields and other methods.
3. The existing test cases (in JSON format) to help avoid redundancy.

Output Instructions:
- Organize the test cases by the name of the test function (group).
- Each test case must include:
    - input: A list of parameter names and values. Use an empty list `[]` if no input is required.
    - expected: The return value or expected observable behavior.
    - description: A clear and concise explanation of the scenario and the intent of the test case.

Maintain the following strict JSON format:
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
Here is the code of focal method, `getValue(int)`, with the focal class `NumericIndicator`:
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
    @Override
    public Num getValue(int index) {
        return delegate.getValue(index);
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
    public static NumericIndicator of(Indicator<Num> delegate);
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