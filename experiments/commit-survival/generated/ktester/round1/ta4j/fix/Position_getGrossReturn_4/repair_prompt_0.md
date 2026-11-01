
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.num.DecimalNum;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.ta4j.core.num.NaN;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;
import static org.ta4j.core.num.NaN.NaN;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_4_Test {

    // entry = BUY
    private Position longPosition;

    // entry = SELL
    private Position shortPosition;

    private Trade longEntry;

    private Trade longExit;

    private Trade shortEntry;

    private Trade shortExit;

    @BeforeEach
    void setupBeforeEach() {
        // --------------------------------------------------------------------
        // BUY → SELL (long position)
        // --------------------------------------------------------------------
        longEntry = Trade.buyAt(0, DecimalNum.valueOf("10"), DecimalNum.valueOf("1"));
        longExit = Trade.sellAt(1, DecimalNum.valueOf("15"), DecimalNum.valueOf("1"));
        longPosition = new Position(longEntry, longExit);
        // --------------------------------------------------------------------
        // SELL → BUY (short position)
        // --------------------------------------------------------------------
        shortEntry = Trade.sellAt(0, DecimalNum.valueOf("20"), DecimalNum.valueOf("1"));
        shortExit = Trade.buyAt(1, DecimalNum.valueOf("10"), DecimalNum.valueOf("1"));
        shortPosition = new Position(shortEntry, shortExit);
    }

    /**
     * BUY position successful calculations
     */
    private static Stream<Arguments> provideBuySuccessCases() {
        return Stream.of(// entry 100, exit 150 → 1.5
        Arguments.of(longPosition, DecimalNum.valueOf("100"), DecimalNum.valueOf("150"), DecimalNum.valueOf("1.5")), // entry 200, exit 200 → 1
        Arguments.of(longPosition, DecimalNum.valueOf("200"), DecimalNum.valueOf("200"), DecimalNum.valueOf("1")), // entry 120, exit 90 → 0.75
        Arguments.of(longPosition, DecimalNum.valueOf("120"), DecimalNum.valueOf("90"), DecimalNum.valueOf("0.75")), // entry 100, exit 0 → 0
        Arguments.of(longPosition, DecimalNum.valueOf("100"), DecimalNum.valueOf("0"), DecimalNum.valueOf("0")), // entry NaN, exit 100 → NaN
        Arguments.of(longPosition, DecimalNum.NaN, DecimalNum.valueOf("100"), DecimalNum.NaN), // entry 100, exit NaN → NaN
        Arguments.of(longPosition, DecimalNum.valueOf("100"), DecimalNum.NaN, DecimalNum.NaN));
    }

    /**
     * SELL position successful calculations
     */
    private static Stream<Arguments> provideSellSuccessCases() {
        return Stream.of(// entry 200, exit 150 → 1.25
        Arguments.of(shortPosition, DecimalNum.valueOf("200"), DecimalNum.valueOf("150"), DecimalNum.valueOf("1.25")), // entry 100, exit 150 → 0.5
        Arguments.of(shortPosition, DecimalNum.valueOf("100"), DecimalNum.valueOf("150"), DecimalNum.valueOf("0.5")), // entry 120, exit 120 → 1
        Arguments.of(shortPosition, DecimalNum.valueOf("120"), DecimalNum.valueOf("120"), DecimalNum.valueOf("1")), // entry 100, exit 0 → 2
        Arguments.of(shortPosition, DecimalNum.valueOf("100"), DecimalNum.valueOf("0"), DecimalNum.valueOf("2")), // entry NaN, exit 100 → NaN
        Arguments.of(shortPosition, DecimalNum.NaN, DecimalNum.valueOf("100"), DecimalNum.NaN), // entry 100, exit NaN → NaN
        Arguments.of(shortPosition, DecimalNum.valueOf("100"), DecimalNum.NaN, DecimalNum.NaN));
    }

    /**
     * Cases that must throw ArithmeticException (division by zero)
     */
    private static Stream<Arguments> provideDivisionByZeroCases() {
        return Stream.of(// BUY: entry 0, exit 100
        Arguments.of(longPosition, DecimalNum.valueOf("0"), DecimalNum.valueOf("100")), // BUY: entry 0, exit 0
        Arguments.of(longPosition, DecimalNum.valueOf("0"), DecimalNum.valueOf("0")), // SELL: entry 0, exit 100
        Arguments.of(shortPosition, DecimalNum.valueOf("0"), DecimalNum.valueOf("100")), // SELL: entry 0, exit 0
        Arguments.of(shortPosition, DecimalNum.valueOf("0"), DecimalNum.valueOf("0")));
    }

    /**
     * Cases that must throw NullPointerException
     */
    private static Stream<Arguments> provideNullArgumentCases() {
        return Stream.of(// null entry price
        Arguments.of(longPosition, null, DecimalNum.valueOf("100")), // null exit price
        Arguments.of(shortPosition, DecimalNum.valueOf("100"), null));
    }

    @Test
    @DisplayName("Gross return for a long position equals exit/entry")
    void whenLongPosition_thenGrossReturnIsExitDividedByEntry() {
        // entry price = 10, exit price = 15 → expected = 15 / 10 = 1.5
        Num entryPrice = longEntry.getPricePerAsset();
        Num exitPrice = longExit.getPricePerAsset();
        Num expected = exitPrice.dividedBy(entryPrice);
        Num actual = longPosition.getGrossReturn(entryPrice, exitPrice);
        assertEquals(expected, actual, "Gross return for long position should be exit/entry");
    }

    @Test
    @DisplayName("Gross return for a short position follows the SELL formula")
    void whenShortPosition_thenGrossReturnFollowsSellFormula() {
        // entry price = 20, exit price = 10
        // Expected = exit/entry → 10/20 = 0.5
        // then: 0.5 - 1 = -0.5
        // negate → 0.5
        // plus 1 → 1.5
        Num entryPrice = shortEntry.getPricePerAsset();
        Num exitPrice = shortExit.getPricePerAsset();
        Num ratio = exitPrice.dividedBy(entryPrice);
        // same Num implementation (DoubleNum)
        Num one = entryPrice.numOf(1);
        Num expected = ratio.minus(one).negate().plus(one);
        Num actual = shortPosition.getGrossReturn(entryPrice, exitPrice);
        assertEquals(expected, actual, "Gross return for short position should follow the SELL formula");
    }

    @Test
    @DisplayName("Gross return works with identical entry and exit prices")
    void whenEntryEqualsExit_thenGrossReturnIsOneForLongAndTwoForShort() {
        // Long: entry = exit = 5 → 5/5 = 1
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Position pos = new Position(entry, exit);
        Num expectedLong = DoubleNum.valueOf(1);
        Num actualLong = pos.getGrossReturn(entry.getPricePerAsset(), exit.getPricePerAsset());
        assertEquals(expectedLong, actualLong, "Long position with equal prices should return 1");
        // Short: entry = exit = 5 → ratio = 1 → 1-1 =0 → negate 0 → +1 = 1
        // Actually according to the formula it also yields 1
        Trade shortEntry = Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Trade shortExit = Trade.buyAt(1, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Position shortPos = new Position(shortEntry, shortExit);
        Num expectedShort = DoubleNum.valueOf(1);
        Num actualShort = shortPos.getGrossReturn(shortEntry.getPricePerAsset(), shortExit.getPricePerAsset());
        assertEquals(expectedShort, actualShort, "Short position with equal prices should also return 1");
    }

    @ParameterizedTest(name = "[BUY] {3}")
    @MethodSource("provideBuySuccessCases")
    @DisplayName("BUY position – successful gross‑return calculations")
    void testBuyGrossReturn(Position position, Num entryPrice, Num exitPrice, Num expected) {
        Num actual = position.getGrossReturn(entryPrice, exitPrice);
        assertEquals(expected, actual, () -> "BUY gross return expected " + expected + " but got " + actual);
    }

    @ParameterizedTest(name = "[SELL] {3}")
    @MethodSource("provideSellSuccessCases")
    @DisplayName("SELL position – successful gross‑return calculations")
    void testSellGrossReturn(Position position, Num entryPrice, Num exitPrice, Num expected) {
        Num actual = position.getGrossReturn(entryPrice, exitPrice);
        assertEquals(expected, actual, () -> "SELL gross return expected " + expected + " but got " + actual);
    }

    @ParameterizedTest(name = "Division‑by‑zero → ArithmeticException")
    @MethodSource("provideDivisionByZeroCases")
    @DisplayName("Division by zero should raise ArithmeticException")
    void testDivisionByZeroThrows(Position position, Num entryPrice, Num exitPrice) {
        assertThrows(ArithmeticException.class, () -> position.getGrossReturn(entryPrice, exitPrice), "Expected ArithmeticException when entry price is zero");
    }

    @ParameterizedTest(name = "Null argument → NullPointerException")
    @MethodSource("provideNullArgumentCases")
    @DisplayName("Null arguments should raise NullPointerException")
    void testNullArgumentsThrow(Position position, Num entryPrice, Num exitPrice) {
        assertThrows(NullPointerException.class, () -> position.getGrossReturn(entryPrice, exitPrice), "Expected NullPointerException when a null argument is supplied");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getGrossReturn_4_Test.java:15: error: ';' expected
import org.junit.jupiter.params.provider.MethodSource
                                                     ^
1 error

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getGrossReturn(Num, Num)': '\n     * Calculates the gross return between entry and exit price in percent. Includes\n     * the base.\n     *\n     * <p>\n     * For example:\n     * <ul>\n     * <li>For buy position with a profit of 4%, it returns 1.04 (includes the base)\n     * <li>For sell position with a loss of 4%, it returns 0.96 (includes the base)\n     * </ul>\n     *\n     * @param entryPrice the entry price\n     * @param exitPrice  the exit price\n     * @return the gross return in percent between entryPrice and exitPrice\n     *         (includes the base)\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.num.Num`:\n\t\nclass `org.ta4j.core.Position`:\n\trelated functions:\n\t\tmethod `public Num getGrossReturn(Num, Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num, Num)`, api document: \n     * Calculates the gross return between entry and exit price in percent. Includes\n     * the base.\n     *\n     * <p>\n     * For example:\n     * <ul>\n     * <li>For buy position with a profit of 4%, it returns 1.04 (includes the base)\n     * <li>For sell position with a loss of 4%, it returns 0.96 (includes the base)\n     * </ul>\n     *\n     * @param entryPrice the entry price\n     * @param exitPrice  the exit price\n     * @return the gross return in percent between entryPrice and exitPrice\n     *         (includes the base)\n     ;\nclass `org.ta4j.core.analysis.CashFlow`:\n\trelated functions:\n\t\tmethod `private static Num getIntermediateRatio(boolean, Num, Num)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num, Num)`, api document: \n     * Calculates the ratio of intermediate prices.\n     *\n     * @param isLongTrade true, if the entry trade type is BUY\n     * @param entryPrice  price ratio denominator\n     * @param exitPrice   price ratio numerator\n     ;\n\t\tmethod ` static Num addCost(Num, Num, boolean)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num, Num)`, api document: \n     * Adjusts (intermediate) price to incorporate trading costs.\n     *\n     * @param rawPrice    the gross asset price\n     * @param holdingCost share of the holding cost per period\n     * @param isLongTrade true, if the entry trade type is BUY\n     ;\nclass `org.ta4j.core.Trade`:\n\trelated functions:\n\t\tmethod `private void setPricesAndCost(Num, Num, CostModel)` returns `void`, related with `org.ta4j.core.Position.getGrossReturn(Num, Num)`, api document: \n     * Sets the raw and net prices of the trade.\n     *\n     * @param pricePerAsset        the raw price of the asset\n     * @param amount               the amount of assets ordered\n     * @param transactionCostModel the cost model for trade execution\n     ;\nclass `org.ta4j.core.indicators.statistics.SimpleLinearRegressionIndicator`:\n\trelated functions:\n\t\tmethod `private void calculateRegressionLine(int, int)` returns `void`, related with `org.ta4j.core.Position.getGrossReturn(Num, Num)`, api document: \n     * Calculates the regression line.\n     *\n     * @param startIndex the start index (inclusive) in the bar series\n     * @param endIndex   the end index (inclusive) in the bar series\n     ;\nclass `org.ta4j.core.indicators.StochasticRSIIndicator`:\n\trelated functions:\n\t\tmethod `protected Num calculate(int)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getGrossReturn(Num, Num)`;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


