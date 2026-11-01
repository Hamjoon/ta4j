You are tasked with creating a framework for a test class in Java. Now analyze the following information carefully:

1. The method that needs to be tested and its parameters.
2. The constructor of the target class and its parameters.
3. Any dependencies or external resources used by the target class.

Based on this analysis, complete the test class template in the provided structure:

1. Initialize necessary fields and dependencies within setupBeforeAll() and setupBeforeEach().
2. Handle any required cleanup in teardownAfterEach() and teardownAfterAll().
3. Make sure not to invoke any private methods or fields of the target class and dependent class.
4. Refer to the "given" phase in the existing test class for guidance.
5. Add necessary import statements and ensure there are no syntax errors in the generated code.

@input{focal method and target class}
Here is the code of focal method, `isLessThan(Number)`, with the focal class `NumericIndicator_isLessThan_2_Test`:
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
    public Rule isLessThan(Number n) {
        return isLessThan(createConstant(n));
    }
public NumericIndicator abs();
    public static NumericIndicator closePrice(BarSeries bs);
    private Indicator<Num> createConstant(Number n);
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
 

constructors for class `org.ta4j.core.indicators.numeric.NumericIndicator`: 
params: 
body:
```java
    protected NumericIndicator(Indicator<Num> delegate) {
        this.delegate = delegate;
    }

```

existing test class: 
```java
/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.assertEquals;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class NumericIndicatorTest extends AbstractIndicatorTest<NumericIndicator, Num> {

    private final BarSeries series = new MockBarSeries(numFunction, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1,
            0, -1, -2);
    private final ClosePriceIndicator cp1 = new ClosePriceIndicator(series);
    private final EMAIndicator ema = new EMAIndicator(cp1, 3);

    public NumericIndicatorTest(Function<Number, Num> numFunction) {
        super(numFunction);
    }

    @Test
    public void plus() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);

        final NumericIndicator staticOp = numericIndicator.plus(5);
        assertNumEquals(1 + 5, staticOp.getValue(0));
        assertNumEquals(9 + 5, staticOp.getValue(8));

        final NumericIndicator dynamicOp = numericIndicator.plus(ema);
        assertNumEquals(cp1.getValue(0).plus(ema.getValue(0)), dynamicOp.getValue(0));
        assertNumEquals(cp1.getValue(8).plus(ema.getValue(8)), dynamicOp.getValue(8));
    }

    @Test
    public void minus() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);

        final NumericIndicator staticOp = numericIndicator.minus(5);
        assertNumEquals(1 - 5, staticOp.getValue(0));
        assertNumEquals(9 - 5, staticOp.getValue(8));

        final NumericIndicator dynamicOp = numericIndicator.minus(ema);
        assertNumEquals(cp1.getValue(0).minus(ema.getValue(0)), dynamicOp.getValue(0));
        assertNumEquals(cp1.getValue(8).minus(ema.getValue(8)), dynamicOp.getValue(8));
    }

    @Test
    public void multipliedBy() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);

        final NumericIndicator staticOp = numericIndicator.multipliedBy(5);
        assertNumEquals(1 * 5, staticOp.getValue(0));
        assertNumEquals(9 * 5, staticOp.getValue(8));

        final NumericIndicator dynamicOp = numericIndicator.multipliedBy(ema);
        assertNumEquals(cp1.getValue(0).multipliedBy(ema.getValue(0)), dynamicOp.getValue(0));
        assertNumEquals(cp1.getValue(8).multipliedBy(ema.getValue(8)), dynamicOp.getValue(8));
    }

......
        ADXIndicator adx = new ADXIndicator(series, 5);
        assertEquals(ADXIndicator.class, NumericIndicator.of(adx).delegate().getClass());
    }

    @Test
    public void rules() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);

        assertEquals(CrossedUpIndicatorRule.class, numericIndicator.crossedOver(5).getClass());
        assertEquals(CrossedUpIndicatorRule.class, numericIndicator.crossedOver(ema).getClass());
        assertEquals(CrossedDownIndicatorRule.class, numericIndicator.crossedUnder(5).getClass());
        assertEquals(CrossedDownIndicatorRule.class, numericIndicator.crossedUnder(ema).getClass());
        assertEquals(OverIndicatorRule.class, numericIndicator.isGreaterThan(5).getClass());
        assertEquals(OverIndicatorRule.class, numericIndicator.isGreaterThan(ema).getClass());
        assertEquals(UnderIndicatorRule.class, numericIndicator.isLessThan(5).getClass());
        assertEquals(UnderIndicatorRule.class, numericIndicator.isLessThan(ema).getClass());
    }

    @Test
    public void previous() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);
        final Indicator<Num> previous = numericIndicator.previous();
        assertNumEquals(cp1.getValue(0), previous.getValue(1));

        final Indicator<Num> previous3 = numericIndicator.previous(3);
        assertNumEquals(cp1.getValue(3), previous3.getValue(6));
    }

    @Test
    public void getValue() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);

        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(cp1.getValue(i), numericIndicator.getValue(i));
        }
    }

    @Test
    public void barSeries() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);
        assertEquals(cp1.getBarSeries(), numericIndicator.getBarSeries());
    }

    @Test
    public void numOf() {
        final NumericIndicator numericIndicator = NumericIndicator.of(cp1);
        assertNumEquals(cp1.numOf(0), numericIndicator.numOf(0));
    }

}
```



@input{test class template}
```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
// Add other imports if necessary

@ExtendWith(MockitoExtension.class)
class NumericIndicator_isLessThan_2_Test {
    @BeforeAll
    static void setupBeforeAll() {
    }

    @BeforeEach
    void setupBeforeEach() {
    }

    @AfterEach
    void teardownAfterEach() {
    }

    @AfterAll
    static void teardownAfterAll() {
    }
}
```

@output{initial test class}: now consider the invoke examples output the code wrapped in a Markdown code block ```java at the beginning and ``` at the end.