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
Here is the code of focal method, `toString()`, with the focal class `ATRIndicator`:
```Java
package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;
public class ATRIndicator extends AbstractIndicator<Num> {
    
    @Override
    public String toString() {
        return (getClass().getSimpleName() + " barCount: ") + getBarCount();
    }
public int getBarCount();
    public TRIndicator getTRIndicator();
    @Override
public int getUnstableBars();
    @Override
public Num getValue(int index);
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

api document of class org.ta4j.core.indicators.ATRIndicator: 

 * Average true range indicator.
 

dependent classes: 
class `org.ta4j.core.indicators.ATRIndicator`:
	related functions:
		method `public String toString()` returns `java.lang.String`, related with `org.ta4j.core.indicators.ATRIndicator.toString()`;
class `org.ta4j.core.indicators.statistics.SigmaIndicator`:
	related functions:
		method `public String toString()` returns `java.lang.String`, related with `org.ta4j.core.indicators.ATRIndicator.toString()`;



@output{test class}: output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.