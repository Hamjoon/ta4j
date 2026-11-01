
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.ta4j.core.analysis.cost.*;
import org.ta4j.core.num.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.ta4j.core.num.NaN;
import org.ta4j.core.TestUtils;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;
import static org.ta4j.core.num.NaN.NaN;
import org.junit.jupiter.api.*;

/* --------------------------------------------------------------------- */
@ExtendWith(MockitoExtension.class)
class Position_getProfit_2_Test {

    private CostModel transactionModel;
    private CostModel holdingModel;

    // BUY
    private Trade longEntry;
    // SELL
    private Trade longExit;
    // SELL
    private Trade shortEntry;
    // BUY
    private Trade shortExit;

    @BeforeEach
    void setupBeforeEach() {
        // Linear transaction cost = 1 % of price * amount
        transactionModel = new LinearTransactionCostModel(0.01);
        // Linear borrowing (holding) cost = 0.1 % per bar
        holdingModel = new LinearBorrowingCostModel(0.001);
        // Long trades (BUY → SELL)
        longEntry = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        longExit = Trade.sellAt(10, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        // Short trades (SELL → BUY)
        shortEntry = Trade.sellAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        shortExit = Trade.buyAt(10, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
    }

    /** Helper to build a closed long position with arbitrary entry/exit data. */
    private static Position buildClosedLong(double entryPrice, double exitPrice, int entryIdx, int exitIdx,
                                            double amount, CostModel txModel, CostModel holdModel) {
        Trade entry = Trade.buyAt(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount), txModel);
        Trade exit = Trade.sellAt(exitIdx, DoubleNum.valueOf(exitPrice), DoubleNum.valueOf(amount), txModel);
        return new Position(entry, exit, txModel, holdModel);
    }

    /** Helper to build an open long position (only entry). */
    private static Position buildOpenLong(double entryPrice, int entryIdx, double amount,
                                          CostModel txModel, CostModel holdModel) {
        Position pos = new Position(Trade.TradeType.BUY, txModel, holdModel);
        pos.operate(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount));
        return pos;
    }

    /** Helper to build an open short position (only entry). */
    private static Position buildOpenShort(double entryPrice, int entryIdx, double amount,
                                           CostModel txModel, CostModel holdModel) {
        Position pos = new Position(Trade.TradeType.SELL, txModel, holdModel);
        pos.operate(entryIdx, DoubleNum.valueOf(entryPrice), DoubleNum.valueOf(amount));
        return pos;
    }

    private static Stream<Arguments> basicProfitProvider() {
        // Positive profit case
        Position posPos = buildClosedLong(50, 200, 1, 10, 1,
                new LinearTransactionCostModel(0.01), new LinearBorrowingCostModel(0.001));
        Num expectedPositive = posPos.getProfit();

        // Zero profit case (gross == cost)
        CostModel expensiveTx = new LinearTransactionCostModel(0.2);
        Position posZeroExact = buildClosedLong(100, 120, 1, 5, 1,
                expensiveTx, new ZeroCostModel());
        Num expectedZero = posZeroExact.getProfit();

        // Negative profit case (cost > gross)
        CostModel highTx = new LinearTransactionCostModel(0.5);
        Position posNeg = buildClosedLong(100, 110, 1, 5, 1,
                highTx, new ZeroCostModel());
        Num expectedNeg = posNeg.getProfit();

        return Stream.of(
                Arguments.of("positive", posPos, 10, DoubleNum.valueOf(200), expectedPositive),
                Arguments.of("zero", posZeroExact, 5, DoubleNum.valueOf(120), expectedZero),
                Arguments.of("negative", posNeg, 5, DoubleNum.valueOf(110), expectedNeg)
        );
    }

    @Test
    void whenClosedLongPosition_getProfitWithParamsEqualsParameterless() {
        Position closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);
        Num expected = closedLong.getProfit();
        Num actual = closedLong.getProfit(10, DoubleNum.valueOf(2));
        assertNumEquals(expected, actual);
    }

    @Test
    void whenOpenLongPosition_getProfitWithParamsReflectsHoldingCost() {
        Position openLong = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        openLong.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1));

        Num finalPrice = DoubleNum.valueOf(3);
        int finalIndex = 10;

        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(2)).multipliedBy(DoubleNum.valueOf(1));
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(2)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = openLong.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void whenClosedShortPosition_getProfitWithParamsEqualsParameterless() {
        Position closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);
        Num expected = closedShort.getProfit();
        Num actual = closedShort.getProfit(10, DoubleNum.valueOf(2));
        assertNumEquals(expected, actual);
    }

    @Test
    void whenOpenShortPosition_getProfitWithParamsReflectsHoldingCost() {
        Position openShort = new Position(Trade.TradeType.SELL, transactionModel, holdingModel);
        openShort.operate(5, DoubleNum.valueOf(2), DoubleNum.valueOf(1));

        Num finalPrice = DoubleNum.valueOf(1.5);
        int finalIndex = 12;

        Num grossProfit = DoubleNum.valueOf(2).minus(finalPrice).multipliedBy(DoubleNum.valueOf(1));
        Num transactionCost = DoubleNum.valueOf(2).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(2)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = openShort.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @ParameterizedTest(name = "basic profit [{0}]")
    @MethodSource("basicProfitProvider")
    void testBasicProfitCalculations(String label, Position position, int finalIndex,
                                     Num finalPrice, Num expected) {
        Num actual = position.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void whenNullFinalPrice_thenNullPointerException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(NullPointerException.class, () -> pos.getProfit(10, null));
    }

    @Test
    void whenNegativeFinalIndex_thenIllegalArgumentException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, () -> pos.getProfit(-1, DoubleNum.valueOf(100)));
    }

    @Test
    void whenFinalIndexBeyondSeries_thenException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(RuntimeException.class, () -> pos.getProfit(999_999, DoubleNum.valueOf(100)));
    }

    @Test
    void whenZeroCostModel_profitEqualsGrossProfit() {
        CostModel zero = new ZeroCostModel();
        Position pos = buildClosedLong(90, 120, 1, 8, 1, zero, zero);
        Num expectedGross = DoubleNum.valueOf(120).minus(DoubleNum.valueOf(90)).multipliedBy(DoubleNum.valueOf(1));
        Num actual = pos.getProfit(8, DoubleNum.valueOf(120));
        assertNumEquals(expectedGross, actual);
    }

    @Test
    void whenFinalIndexEqualsEntryIndex_breakEvenZeroProfit() {
        Position pos = buildOpenLong(50, 2, 1, transactionModel, holdingModel);
        Num actual = pos.getProfit(2, DoubleNum.valueOf(50));
        assertNumEquals(DoubleNum.valueOf(0), actual);
    }

    @Test
    void whenFinalPriceIsNaN_resultIsNaN() {
        Position pos = buildOpenLong(100, 3, 1, transactionModel, holdingModel);
        Num nan = DoubleNum.NaN;
        Num actual = pos.getProfit(7, nan);
        assertTrue(actual.isNaN(), "Profit should be NaN when finalPrice is NaN");
    }

    @Test
    void whenOpenPosition_calculateProfitIncludingHoldingCost() {
        Position pos = buildOpenLong(150, 5, 1, transactionModel, holdingModel);
        int finalIndex = 15;
        Num finalPrice = DoubleNum.valueOf(200);

        Num grossProfit = finalPrice.minus(DoubleNum.valueOf(150));
        Num transactionCost = DoubleNum.valueOf(150).multipliedBy(DoubleNum.valueOf(0.01));
        Num holdingCost = DoubleNum.valueOf(150)
                .multipliedBy(DoubleNum.valueOf(1))
                .multipliedBy(DoubleNum.valueOf(0.001))
                .multipliedBy(DoubleNum.valueOf(finalIndex - 5));

        Num expected = grossProfit.minus(transactionCost).minus(holdingCost);
        Num actual = pos.getProfit(finalIndex, finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void whenHighPrecisionFinalPrice_preservePrecision() {
        Position pos = buildClosedLong(100, 123.456789, 1, 12, 1,
                new LinearTransactionCostModel(0.004), new ZeroCostModel());
        Num expected = pos.getProfit(12, DoubleNum.valueOf(123.456789));
        Num actual = pos.getProfit(12, DoubleNum.valueOf(123.456789));
        assertNumEquals(expected, actual);
    }

    @Test
    void whenPositionNeverOpened_thenIllegalStateException() {
        Position pos = new Position(Trade.TradeType.BUY, transactionModel, holdingModel);
        assertThrows(IllegalStateException.class, () -> pos.getProfit(5, DoubleNum.valueOf(100)));
    }

    @Test
    void whenNegativeFinalPrice_thenIllegalArgumentException() {
        Position pos = buildClosedLong(100, 150, 1, 10, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, () -> pos.getProfit(10, DoubleNum.valueOf(-50)));
    }

    @Test
    void whenNullCostModel_thenNullPointerException() {
        Position pos = new Position(longEntry, longExit, null, holdingModel);
        assertThrows(NullPointerException.class, () -> pos.getProfit(8, DoubleNum.valueOf(120)));
    }

    @Test
    void whenFinalIndexBeforeEntry_thenIllegalArgumentException() {
        Position pos = buildOpenLong(100, 5, 1, transactionModel, holdingModel);
        assertThrows(IllegalArgumentException.class, () -> pos.getProfit(2, DoubleNum.valueOf(100)));
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getProfit_2_Test.java:13: error: ';' expected
import org.junit.jupiter.params.provider.MethodSource
                                                     ^
1 error

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getProfit(int, Num)': '\n     * Calculates the net profit of the position. If it is open, calculates the\n     * profit until the final bar. The net profit includes any trading costs.\n     *\n     * @param finalIndex the index of the final bar to be considered (if position is\n     *                   open)\n     * @param finalPrice the price of the final bar to be considered (if position is\n     *                   open)\n     * @return the profit or loss of the position\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.num.Num`:\n\t\nclass `org.ta4j.core.Position`:\n\trelated functions:\n\t\tmethod `public Num getProfit(int, Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit(int, Num)`, api document: \n     * Calculates the net profit of the position. If it is open, calculates the\n     * profit until the final bar. The net profit includes any trading costs.\n     *\n     * @param finalIndex the index of the final bar to be considered (if position is\n     *                   open)\n     * @param finalPrice the price of the final bar to be considered (if position is\n     *                   open)\n     * @return the profit or loss of the position\n     ;\n\t\tmethod `public Num getProfit()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit(int, Num)`, api document: \n     * Calculates the net profit of the position if it is closed. The net profit\n     * includes any trading costs.\n     *\n     * @return the profit or loss of the position\n     ;\nclass `org.ta4j.core.analysis.CashFlow`:\n\trelated functions:\n\t\tmethod `private static Num getIntermediateRatio(boolean, Num, Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit(int, Num)`, api document: \n     * Calculates the ratio of intermediate prices.\n     *\n     * @param isLongTrade true, if the entry trade type is BUY\n     * @param entryPrice  price ratio denominator\n     * @param exitPrice   price ratio numerator\n     ;\n\t\tmethod ` static Num addCost(Num, Num, boolean)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getProfit(int, Num)`, api document: \n     * Adjusts (intermediate) price to incorporate trading costs.\n     *\n     * @param rawPrice    the gross asset price\n     * @param holdingCost share of the holding cost per period\n     * @param isLongTrade true, if the entry trade type is BUY\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


