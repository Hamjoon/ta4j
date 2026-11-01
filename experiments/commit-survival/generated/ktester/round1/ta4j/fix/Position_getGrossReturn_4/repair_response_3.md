```java
package org.ta4j.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position#getGrossReturn(Num, Num)}.
 */
public class Position_getGrossReturn_4_Test {

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
        return Stream.of(
                // entry 100, exit 150 → 1.5
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("150"), DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              DecimalNum.valueOf("150"),
                              DecimalNum.valueOf("1.5")),
                // entry 200, exit 200 → 1
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("200"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("200"), DecimalNum.ONE)),
                              DecimalNum.valueOf("200"),
                              DecimalNum.valueOf("200"),
                              DecimalNum.valueOf("1")),
                // entry 120, exit 90 → 0.75
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("120"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("90"), DecimalNum.ONE)),
                              DecimalNum.valueOf("120"),
                              DecimalNum.valueOf("90"),
                              DecimalNum.valueOf("0.75")),
                // entry 100, exit 0 → 0
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("0"), DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              DecimalNum.valueOf("0"),
                              DecimalNum.valueOf("0")),
                // entry NaN, exit 100 → NaN
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.NaN, DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("100"), DecimalNum.ONE)),
                              DecimalNum.NaN,
                              DecimalNum.valueOf("100"),
                              DecimalNum.NaN),
                // entry 100, exit NaN → NaN
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.NaN, DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              DecimalNum.NaN,
                              DecimalNum.NaN)
        );
    }

    /**
     * SELL position successful calculations
     */
    private static Stream<Arguments> provideSellSuccessCases() {
        return Stream.of(
                // entry 200, exit 150 → 1.25
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("200"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("150"), DecimalNum.ONE)),
                              DecimalNum.valueOf("200"),
                              DecimalNum.valueOf("150"),
                              DecimalNum.valueOf("1.25")),
                // entry 100, exit 150 → 0.5
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("150"), DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              DecimalNum.valueOf("150"),
                              DecimalNum.valueOf("0.5")),
                // entry 120, exit 120 → 1
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("120"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("120"), DecimalNum.ONE)),
                              DecimalNum.valueOf("120"),
                              DecimalNum.valueOf("120"),
                              DecimalNum.valueOf("1")),
                // entry 100, exit 0 → 2
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("0"), DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              DecimalNum.valueOf("0"),
                              DecimalNum.valueOf("2")),
                // entry NaN, exit 100 → NaN
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.NaN, DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("100"), DecimalNum.ONE)),
                              DecimalNum.NaN,
                              DecimalNum.valueOf("100"),
                              DecimalNum.NaN),
                // entry 100, exit NaN → NaN
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.NaN, DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              DecimalNum.NaN,
                              DecimalNum.NaN)
        );
    }

    /**
     * Cases that must throw ArithmeticException (division by zero)
     */
    private static Stream<Arguments> provideDivisionByZeroCases() {
        return Stream.of(
                // BUY: entry 0, exit 100
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("0"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("100"), DecimalNum.ONE)),
                              DecimalNum.valueOf("0"),
                              DecimalNum.valueOf("100")),
                // BUY: entry 0, exit 0
                Arguments.of(new Position(Trade.buyAt(0, DecimalNum.valueOf("0"), DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("0"), DecimalNum.ONE)),
                              DecimalNum.valueOf("0"),
                              DecimalNum.valueOf("0")),
                // SELL: entry 0, exit 100
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("0"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("100"), DecimalNum.ONE)),
                              DecimalNum.valueOf("0"),
                              DecimalNum.valueOf("100")),
                // SELL: entry 0, exit 0
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("0"), DecimalNum.ONE),
                                          Trade.buyAt(1, DecimalNum.valueOf("0"), DecimalNum.ONE)),
                              DecimalNum.valueOf("0"),
                              DecimalNum.valueOf("0"))
        );
    }

    /**
     * Cases that must throw NullPointerException
     */
    private static Stream<Arguments> provideNullArgumentCases() {
        return Stream.of(
                // null entry price
                Arguments.of(new Position(Trade.buyAt(0, null, DecimalNum.ONE),
                                          Trade.sellAt(1, DecimalNum.valueOf("100"), DecimalNum.ONE)),
                              null,
                              DecimalNum.valueOf("100")),
                // null exit price
                Arguments.of(new Position(Trade.sellAt(0, DecimalNum.valueOf("100"), DecimalNum.ONE),
                                          Trade.buyAt(1, null, DecimalNum.ONE)),
                              DecimalNum.valueOf("100"),
                              null)
        );
    }

    @Test
    @DisplayName("Gross return for a long position equals exit/entry")
    void whenLongPosition_thenGrossReturnIsExitDividedByEntry() {
        Num entryPrice = longEntry.getPricePerAsset();
        Num exitPrice = longExit.getPricePerAsset();
        Num expected = exitPrice.dividedBy(entryPrice);
        Num actual = longPosition.getGrossReturn(entryPrice, exitPrice);
        assertEquals(expected, actual, "Gross return for long position should be exit/entry");
    }

    @Test
    @DisplayName("Gross return for a short position follows the SELL formula")
    void whenShortPosition_thenGrossReturnFollowsSellFormula() {
        Num entryPrice = shortEntry.getPricePerAsset();
        Num exitPrice = shortExit.getPricePerAsset();
        Num ratio = exitPrice.dividedBy(entryPrice);
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
        assertThrows(ArithmeticException.class,
                () -> position.getGrossReturn(entryPrice, exitPrice),
                "Expected ArithmeticException when entry price is zero");
    }

    @ParameterizedTest(name = "Null argument → NullPointerException")
    @MethodSource("provideNullArgumentCases")
    @DisplayName("Null arguments should raise NullPointerException")
    void testNullArgumentsThrow(Position position, Num entryPrice, Num exitPrice) {
        assertThrows(NullPointerException.class,
                () -> position.getGrossReturn(entryPrice, exitPrice),
                "Expected NullPointerException when a null argument is supplied");
    }
}
```