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
Here is the code of focal method, `getGrossProfit()`, with the focal class `Position`:
```Java
package org.ta4j.core;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;
import java.util.Objects;
import static org.ta4j.core.num.NaN.NaN;
import java.io.Serializable;
import org.ta4j.core.Trade.TradeType;
public class Position implements Serializable {
    private Trade exit;
    public Num getGrossProfit() {
        if (isOpened()) {
            return zero();
        } else {
            return getGrossProfit(exit.getPricePerAsset());
        }
    }
@Override
public boolean equals(Object obj);
    public Trade getEntry();
    public Trade getExit();
    public Num getGrossProfit(Num finalPrice);
    public Num getGrossReturn();
    public Num getGrossReturn(BarSeries barSeries);
    public Num getGrossReturn(Num finalPrice);
    public Num getGrossReturn(Num entryPrice, Num exitPrice);
    public Num getHoldingCost();
    public Num getHoldingCost(int finalIndex);
    public Num getPositionCost();
    public Num getPositionCost(int finalIndex);
    public Num getProfit();
    public Num getProfit(int finalIndex, Num finalPrice);
    public TradeType getStartingType();
    public boolean hasLoss();
    public boolean hasProfit();
    @Override
public int hashCode();
    public boolean isClosed();
    public boolean isNew();
    public boolean isOpened();
    public Trade operate(int index);
    public Trade operate(int index, Num price, Num amount);
    @Override
public String toString();
    private Num zero();
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

api document of class org.ta4j.core.Position: 

 * A {@code Position} is a pair of two {@link Trade trades}.
 *
 * <p>
 * The exit trade has the complement type of the entry trade, i.e.:
 * <ul>
 * <li>entry == BUY --> exit == SELL
 * <li>entry == SELL --> exit == BUY
 * </ul>
 

api document of method getGrossProfit(): 

     * Calculates the gross profit of the position if it is closed. The gross profit
     * excludes any trading costs.
     *
     * @return the gross profit of the position
     

return type: 
org.ta4j.core.num.Num

dependent classes: 
class `org.ta4j.core.Position`:
	dependent fields:
		org.ta4j.core.Trade org.ta4j.core.Position.exit;
	related functions:
		method `public Num getGrossReturn()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * Calculates the gross return of the position if it is closed. The gross return
     * excludes any trading costs (and includes the base).
     *
     * @return the gross return of the position in percent
     * @see #getGrossReturn(Num)
     ;
		method `public Trade getExit()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * @return the exit {@link Trade trade} of the position
     ;
		method `public Num getGrossProfit(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * Calculates the gross profit of the position. The gross profit excludes any
     * trading costs.
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     *                   open)
     * @return the profit or loss of the position
     ;
		method `public Num getProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossProfit()`, api document: 
     * Calculates the net profit of the position if it is closed. The net profit
     * includes any trading costs.
     *
     * @return the profit or loss of the position
     ;
		method `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getGrossProfit()`;



@output{test class}: output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.