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
Here is the code of focal method, `getGrossReturn(Num)`, with the focal class `Position`:
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
    
    public Num getGrossReturn(Num finalPrice) {
        return getGrossReturn(getEntry().getPricePerAsset(), finalPrice);
    }
@Override
public boolean equals(Object obj);
    public Trade getEntry();
    public Trade getExit();
    public Num getGrossProfit();
    public Num getGrossProfit(Num finalPrice);
    public Num getGrossReturn();
    public Num getGrossReturn(BarSeries barSeries);
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
 

api document of method getGrossReturn(Num): 

     * Calculates the gross return of the position, if it exited at the provided
     * price. The gross return excludes any trading costs (and includes the base).
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     *                   open)
     * @return the gross return of the position in percent
     * @see #getGrossReturn(Num, Num)
     

return type: 
org.ta4j.core.num.Num

dependent classes: 
class `org.ta4j.core.num.Num`:
	
class `org.ta4j.core.Position`:
	related functions:
		method `public Num getGrossReturn(BarSeries)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num)`, api document: 
     * Calculates the gross return of the position. If either the entry or exit
     * price is {@code NaN}, the close price from given {@code barSeries} is used.
     * The gross return excludes any trading costs (and includes the base).
     *
     * @param barSeries
     * @return the gross return in percent with entry and exit prices from the
     *         barSeries
     * @see #getGrossReturn(Num, Num)
     ;
		method `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.getGrossReturn(Num)`;
		method `public Num getGrossReturn(Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num)`, api document: 
     * Calculates the gross return of the position, if it exited at the provided
     * price. The gross return excludes any trading costs (and includes the base).
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     *                   open)
     * @return the gross return of the position in percent
     * @see #getGrossReturn(Num, Num)
     ;
class `org.ta4j.core.analysis.cost.FixedTransactionCostModel`:
	related functions:
		method `public Num calculate(Position, int)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num)`, api document: 
     * @param position     the position
     * @param currentIndex the current bar index (irrelevant for
     *                     {@code FixedTransactionCostModel})
     * @return the transaction cost of the single {@code position}
     ;
class `org.ta4j.core.analysis.cost.LinearTransactionCostModel`:
	related functions:
		method `public Num calculate(Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num)`;
class `org.ta4j.core.criteria.ExpectedShortfallCriterion`:
	related functions:
		method `public Num calculate(BarSeries, Position)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num)`;



@output{test class}: output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.