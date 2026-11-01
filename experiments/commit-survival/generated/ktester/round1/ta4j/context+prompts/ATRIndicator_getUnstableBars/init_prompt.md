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
Here is the code of focal method, `getUnstableBars()`, with the focal class `ATRIndicator_getUnstableBars_Test`:
```Java
package org.ta4j.core.indicators;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;
public class ATRIndicator extends AbstractIndicator<Num> {
    
    @Override
    public int getUnstableBars() {
        return getBarCount();
    }
public int getBarCount();
    public TRIndicator getTRIndicator();
    @Override
public Num getValue(int index);
    @Override
public String toString();
}
```


@input{context information}

api document of class org.ta4j.core.indicators.ATRIndicator: 

 * Average true range indicator.
 

constructors for class `org.ta4j.core.indicators.ATRIndicator`: 
params: org.ta4j.core.BarSeries series 
body:
```java
    public ATRIndicator(BarSeries series, int barCount) {
        this(new TRIndicator(series), barCount);
    }

```
params: org.ta4j.core.indicators.helpers.TRIndicator tr 
body:
```java
    public ATRIndicator(TRIndicator tr, int barCount) {
        super(tr.getBarSeries());
        this.trIndicator = tr;
        this.averageTrueRangeIndicator = new MMAIndicator(new TRIndicator(tr.getBarSeries()), barCount);
    }

```

parameters in constructors and focal method: 
class `org.ta4j.core.BarSeries`:
api document : 
 * A {@code BarSeries} is a sequence of {@link Bar bars} separated by a
 * predefined period (e.g. 15 minutes, 1 day, etc.).
 * 
 * Notably, it can be:
 * 
 * <ul>
 * <li>the base of {@link Indicator indicator} calculations
 * <li>constrained between beginning and ending indices (e.g. for some
 * backtesting cases)
 * <li>limited to a fixed number of bars (e.g. for actual trading)
 * </ul>
 
constructor:
```java
```
class `org.ta4j.core.indicators.helpers.TRIndicator`:
api document : 
 * True range indicator.
 * 
 * <pre>
 * TrueRange = MAX(high - low, high - previousClose, previousClose - low)
 * </pre>
 
constructor:
```java
    public TRIndicator(BarSeries series) {
        super(series);
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
package org.ta4j.core.indicators;

import static org.junit.Assert.assertEquals;
import static org.ta4j.core.TestUtils.assertIndicatorEquals;

import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.ExternalIndicatorTest;
import org.ta4j.core.Indicator;
import org.ta4j.core.TestUtils;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.num.Num;

public class ATRIndicatorTest extends AbstractIndicatorTest<BarSeries, Num> {

    private final ExternalIndicatorTest xls;

    public ATRIndicatorTest(Function<Number, Num> numFunction) throws Exception {
        super((data, params) -> new ATRIndicator(data, (int) params[0]), numFunction);
        xls = new XLSIndicatorTest(this.getClass(), "ATR.xls", 7, numFunction);
    }

    @Test
    public void testDummy() throws Exception {
        BarSeries series = new BaseBarSeriesBuilder().withNumTypeOf(numFunction).build();
        series.addBar(new MockBar(ZonedDateTime.now().minusSeconds(5), 0, 12, 15, 8, 0, 0, 0, numFunction));
        series.addBar(new MockBar(ZonedDateTime.now().minusSeconds(4), 0, 8, 11, 6, 0, 0, 0, numFunction));
        series.addBar(new MockBar(ZonedDateTime.now().minusSeconds(3), 0, 15, 17, 14, 0, 0, 0, numFunction));
        series.addBar(new MockBar(ZonedDateTime.now().minusSeconds(2), 0, 15, 17, 14, 0, 0, 0, numFunction));
        series.addBar(new MockBar(ZonedDateTime.now().minusSeconds(1), 0, 0, 0, 2, 0, 0, 0, numFunction));
        Indicator<Num> indicator = getIndicator(series, 3);

        assertEquals(7d, indicator.getValue(0).doubleValue(), TestUtils.GENERAL_OFFSET);
        assertEquals(6d / 3 + (1 - 1d / 3) * indicator.getValue(0).doubleValue(), indicator.getValue(1).doubleValue(),
                TestUtils.GENERAL_OFFSET);
        assertEquals(9d / 3 + (1 - 1d / 3) * indicator.getValue(1).doubleValue(), indicator.getValue(2).doubleValue(),
                TestUtils.GENERAL_OFFSET);
        assertEquals(3d / 3 + (1 - 1d / 3) * indicator.getValue(2).doubleValue(), indicator.getValue(3).doubleValue(),
                TestUtils.GENERAL_OFFSET);
        assertEquals(15d / 3 + (1 - 1d / 3) * indicator.getValue(3).doubleValue(), indicator.getValue(4).doubleValue(),
                TestUtils.GENERAL_OFFSET);
    }

    @Test
    public void testXls() throws Exception {
        BarSeries xlsSeries = xls.getSeries();
        Indicator<Num> indicator;

        indicator = getIndicator(xlsSeries, 1);
        assertIndicatorEquals(xls.getIndicator(1), indicator);
        assertEquals(4.8, indicator.getValue(indicator.getBarSeries().getEndIndex()).doubleValue(),
                TestUtils.GENERAL_OFFSET);

        indicator = getIndicator(xlsSeries, 3);
        assertIndicatorEquals(xls.getIndicator(3), indicator);
        assertEquals(7.4225, indicator.getValue(indicator.getBarSeries().getEndIndex()).doubleValue(),
                TestUtils.GENERAL_OFFSET);

        indicator = getIndicator(xlsSeries, 13);
        assertIndicatorEquals(xls.getIndicator(13), indicator);
        assertEquals(8.8082, indicator.getValue(indicator.getBarSeries().getEndIndex()).doubleValue(),
                TestUtils.GENERAL_OFFSET);
    }

}

```



@input{test class template}
```java
package org.ta4j.core.indicators;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
// Add other imports if necessary

@ExtendWith(MockitoExtension.class)
class ATRIndicator_getUnstableBars_Test {
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