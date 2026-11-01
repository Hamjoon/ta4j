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
Here is the code of focal method, `getValue(int)`, with the focal class `ATRIndicator`:
```Java
package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;
public class ATRIndicator extends AbstractIndicator<Num> {
    private final MMAIndicator averageTrueRangeIndicator;
    @Override
    public Num getValue(int index) {
        return averageTrueRangeIndicator.getValue(index);
    }
public int getBarCount();
    public TRIndicator getTRIndicator();
    @Override
public int getUnstableBars();
    @Override
public String toString();
}
```

@input{existing test case}
here's the existing test cases:
```json
<cases_json>
```

@output{test cases}: output the test cases in the JSON format: