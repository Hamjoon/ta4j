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
Here is the code of focal method, `getGrossReturn(BarSeries)`, with the focal class `Position_getGrossReturn_2_Test`:
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
    
    public Num getGrossReturn(BarSeries barSeries) {
        Num entryPrice = getEntry().getPricePerAsset(barSeries);
        Num exitPrice = getExit().getPricePerAsset(barSeries);
        return getGrossReturn(entryPrice, exitPrice);
    }
@Override
public boolean equals(Object obj);
    public Trade getEntry();
    public Trade getExit();
    public Num getGrossProfit();
    public Num getGrossProfit(Num finalPrice);
    public Num getGrossReturn();
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
}
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
 

constructors for class `org.ta4j.core.Position`: 
params: 
body:
```java
    public Position() {
        this(TradeType.BUY);
    }

```
params: org.ta4j.core.Trade.TradeType startingType 
body:
```java
    public Position(TradeType startingType) {
        this(startingType, new ZeroCostModel(), new ZeroCostModel());
    }

```
params: org.ta4j.core.Trade.TradeType startingType 
org.ta4j.core.analysis.cost.CostModel transactionCostModel 
org.ta4j.core.analysis.cost.CostModel holdingCostModel 
body:
```java
    public Position(TradeType startingType, CostModel transactionCostModel, CostModel holdingCostModel) {
        if (startingType == null) {
            throw new IllegalArgumentException("Starting type must not be null");
        }
        this.startingType = startingType;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
    }

```
params: org.ta4j.core.Trade entry 
org.ta4j.core.Trade exit 
body:
```java
    public Position(Trade entry, Trade exit) {
        this(entry, exit, entry.getCostModel(), new ZeroCostModel());
    }

```
params: org.ta4j.core.Trade entry 
org.ta4j.core.Trade exit 
org.ta4j.core.analysis.cost.CostModel transactionCostModel 
org.ta4j.core.analysis.cost.CostModel holdingCostModel 
body:
```java
    public Position(Trade entry, Trade exit, CostModel transactionCostModel, CostModel holdingCostModel) {

        if (entry.getType().equals(exit.getType())) {
            throw new IllegalArgumentException("Both trades must have different types");
        }

        if (!(entry.getCostModel().equals(transactionCostModel))
                || !(exit.getCostModel().equals(transactionCostModel))) {
            throw new IllegalArgumentException("Trades and the position must incorporate the same trading cost model");
        }

        this.startingType = entry.getType();
        this.entry = entry;
        this.exit = exit;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
    }

```

parameters in constructors and focal method: 
class `org.ta4j.core.Trade.TradeType`:
api document :  The type of a {@link Trade trade}. 
constructor:
```java
```
class `org.ta4j.core.analysis.cost.CostModel`:
api document : 
 * With the {@code CostModel}, we can include trading costs that may be incurred
 * when opening or closing a position.
 
constructor:
```java
```
class `org.ta4j.core.Trade`:
api document : 
 * A {@code Trade} is defined by:
 * 
 * <ul>
 * <li>the index (in the {@link BarSeries bar series}) on which the trade is
 * executed
 * <li>a {@link TradeType type} (BUY or SELL)
 * <li>a pricePerAsset (optional)
 * <li>a trade amount (optional)
 * </ul>
 * 
 * A {@link Position position} is a pair of complementary trades.
 
constructor:
```java
    protected Trade(int index, BarSeries series, TradeType type) {
        this(index, series, type, series.one());
    }

    protected Trade(int index, BarSeries series, TradeType type, Num amount) {
        this(index, series, type, amount, new ZeroCostModel());
    }

    protected Trade(int index, BarSeries series, TradeType type, Num amount, CostModel transactionCostModel) {
        this.type = type;
        this.index = index;
        this.amount = amount;
        setPricesAndCost(series.getBar(index).getClosePrice(), amount, transactionCostModel);
    }

    protected Trade(int index, TradeType type, Num pricePerAsset) {
        this(index, type, pricePerAsset, pricePerAsset.one());
    }

    protected Trade(int index, TradeType type, Num pricePerAsset, Num amount) {
        this(index, type, pricePerAsset, amount, new ZeroCostModel());
    }

    protected Trade(int index, TradeType type, Num pricePerAsset, Num amount, CostModel transactionCostModel) {
        this.type = type;
        this.index = index;
        this.amount = amount;

        setPricesAndCost(pricePerAsset, amount, transactionCostModel);
    }

```
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
package org.ta4j.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public class PositionTest {

    private Position newPosition, uncoveredPosition, posEquals1, posEquals2, posNotEquals1, posNotEquals2;

    private CostModel transactionModel;
    private CostModel holdingModel;
    private Trade enter;
    private Trade exitSameType;
    private Trade exitDifferentType;

    @Before
    public void setUp() {
        this.newPosition = new Position();
        this.uncoveredPosition = new Position(TradeType.SELL);

        posEquals1 = new Position();
        posEquals1.operate(1);
        posEquals1.operate(2);

        posEquals2 = new Position();
        posEquals2.operate(1);
        posEquals2.operate(2);

        posNotEquals1 = new Position(TradeType.SELL);
        posNotEquals1.operate(1);
        posNotEquals1.operate(2);

        posNotEquals2 = new Position(TradeType.SELL);
        posNotEquals2.operate(1);
        posNotEquals2.operate(2);

        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        enter = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        exitSameType = Trade.sellAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        exitDifferentType = Trade.buyAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1));
    }

    @Test
    public void whenNewShouldCreateBuyOrderWhenEntering() {
        newPosition.operate(0);
        assertEquals(Trade.buyAt(0, NaN, NaN), newPosition.getEntry());
    }

    @Test
    public void whenNewShouldNotExit() {
        assertFalse(newPosition.isOpened());
    }

    @Test
    public void whenOpenedShouldCreateSellOrderWhenExiting() {
        newPosition.operate(0);
        newPosition.operate(1);
        assertEquals(Trade.sellAt(1, NaN, NaN), newPosition.getExit());
    }

......

        Num profitOfClosedPosition = closedPosition.getProfit(10, DoubleNum.valueOf(12));
        Num profitOfOpenPosition = openPosition.getProfit(10, DoubleNum.valueOf(12));

        assertNumEquals(DoubleNum.valueOf(9.98), profitOfOpenPosition);
        assertNumEquals(DoubleNum.valueOf(-0.04), profitOfClosedPosition);
    }

    @Test
    public void getProfitShortNoFinalBarTest() {
        Trade sell = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        Trade buyBack = Trade.buyAt(10, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);

        Position closedPosition = new Position(sell, buyBack, transactionModel, holdingModel);
        Position openPosition = new Position(TradeType.SELL, transactionModel, holdingModel);
        openPosition.operate(5, DoubleNum.valueOf(100), DoubleNum.valueOf(1));

        Num profitOfClosedPosition = closedPosition.getProfit();
        Num proftOfOpenPosition = openPosition.getProfit();

        Num expectedHoldingCosts = DoubleNum.valueOf(2.0 * 9.0 * 0.001);
        Num expectedProfitOfClosedPosition = DoubleNum.valueOf(-0.04).minus(expectedHoldingCosts);

        assertNumEquals(expectedProfitOfClosedPosition, profitOfClosedPosition);
        assertNumEquals(DoubleNum.valueOf(0), proftOfOpenPosition);
    }

    @Test
    public void getProfitShortWithFinalBarTest() {
        Trade sell = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        Trade buyBack = Trade.buyAt(10, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);

        Position closedPosition = new Position(sell, buyBack, transactionModel, holdingModel);
        Position openPosition = new Position(TradeType.SELL, transactionModel, holdingModel);
        openPosition.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1));

        Num profitOfClosedPositionFinalAfter = closedPosition.getProfit(20, DoubleNum.valueOf(3));
        Num profitOfOpenPositionFinalAfter = openPosition.getProfit(20, DoubleNum.valueOf(3));
        Num profitOfClosedPositionFinalBefore = closedPosition.getProfit(5, DoubleNum.valueOf(3));
        Num profitOfOpenPositionFinalBefore = openPosition.getProfit(5, DoubleNum.valueOf(3));

        Num expectedHoldingCosts = DoubleNum.valueOf(2.0 * 9.0 * 0.001);
        Num expectedProfitOfClosedPosition = DoubleNum.valueOf(-0.04).minus(expectedHoldingCosts);

        assertNumEquals(DoubleNum.valueOf(-1.05), profitOfOpenPositionFinalAfter);
        assertNumEquals(DoubleNum.valueOf(-1.02), profitOfOpenPositionFinalBefore);
        assertNumEquals(expectedProfitOfClosedPosition, profitOfClosedPositionFinalAfter);
        assertNumEquals(expectedProfitOfClosedPosition, profitOfClosedPositionFinalBefore);
    }
}
```

invoke examples of focal method: 
example 1:
```java
    private Num calculateProfit(BarSeries series, Position position) {
        if (position.isClosed()) {
            return position.getGrossReturn(series);
    }
```
example 2:
```java
    @Override
    public Num calculate(BarSeries series, Position position) {
        int beginIndex = position.getEntry().getIndex();
        int endIndex = series.getEndIndex();
        return createEnterAndHoldTrade(series, beginIndex, endIndex).getGrossReturn(series);
    }
```
example 3:
```java
    @Override
    public Num calculate(BarSeries series, TradingRecord tradingRecord) {
        if (series.isEmpty()) {
            return series.numOf(1);
        }
        int beginIndex = tradingRecord.getStartIndex(series);
        int endIndex = tradingRecord.getEndIndex(series);
        return createEnterAndHoldTrade(series, beginIndex, endIndex).getGrossReturn(series);
    }
```



@input{test class template}
```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
// Add other imports if necessary

@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_2_Test {
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