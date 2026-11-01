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
Here is the code of focal method, `dividedBy(Number)`, with the focal class `NumericIndicator`:
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
    public NumericIndicator dividedBy(Number n) {
        return dividedBy(createConstant(n));
    }
public NumericIndicator abs();
    public static NumericIndicator closePrice(BarSeries bs);
    private Indicator<Num> createConstant(Number n);
    public Rule crossedOver(Number n);
    public Rule crossedOver(Indicator<Num> other);
    public Rule crossedUnder(Number n);
    public Rule crossedUnder(Indicator<Num> other);
    public Indicator<Num> delegate();
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

api document of class org.ta4j.core.indicators.numeric.NumericIndicator: 

 * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides
 * methods to create rules and other "lightweight" indicators, using a
 * (hopefully) natural-looking and expressive series of method calls.
 * 
 * <p>
 * Methods like plus(), minus() and sqrt() correspond directly to methods in the
 * {@code Num} interface. These methods create "lightweight" (not cached)
 * indicators to add, subtract, etc. Many methods are overloaded to accept
 * either {@code Indicator<Num>} or {@code Number} arguments.
 * 
 * <p>
 * Methods like sma() and ema() simply create the corresponding indicator
 * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first
 * argument. These methods usually instantiate cached objects.
 * 
 * <p>
 * Another set of methods, like crossedOver() and isGreaterThan() create Rule
 * objects. These are also overloaded to accept both {@code Indicator<Num>} and
 * {@code Number} arguments.
 

api document of method dividedBy(Number): 

     * @param n the other number
     * @return {@code this / n}, rounded as necessary
     

return type: 
org.ta4j.core.indicators.numeric.NumericIndicator

dependent classes: 
class `org.ta4j.core.indicators.numeric.NumericIndicator`:
	related functions:
		method `public NumericIndicator dividedBy(Number)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.dividedBy(Number)`, api document: 
     * @param n the other number
     * @return {@code this / n}, rounded as necessary
     ;
		method `public NumericIndicator plus(Number)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.dividedBy(Number)`, api document: 
     * @param n the other number
     * @return {@code this + n}, rounded as necessary
     ;



@output{test class}: output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.