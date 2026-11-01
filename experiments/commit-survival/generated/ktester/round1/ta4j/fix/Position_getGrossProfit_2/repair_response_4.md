```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link Position#getGrossProfit(Num)}.
 */
class Position_getGrossProfit_2_Test {

    /** Zero‑cost models keep the calculations simple. */
    private static final CostModel transactionModel = new ZeroCostModel();
    private static final CostModel holdingModel = new ZeroCostModel();

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                       */
    /* --------------------------------------------------------------------- */

    /** Creates a {@link Position} that is still open (no exit trade). */
    private Position openedPosition(Trade entry) {
        Position pos = new Position();
        setField(pos, "entry", entry);
        // exit stays null → opened position
        return pos;
    }

    /** Creates a {@link Position} that is already closed. */
    private Position closedPosition(Trade entry, Trade exit) {
        Position pos = new Position();
        setField(pos, "entry", entry);
        setField(pos, "exit", exit);
        return pos;
    }

    /** Creates a {@link Trade} but allows {@code null} for amount or value. */
    private Trade mockTrade(Trade.TradeType type,
                            Num price,
                            Num amount,
                            Num value) {
        Trade trade = (type == Trade.TradeType.BUY)
                ? Trade.buyAt(0, price, amount, transactionModel)
                : Trade.sellAt(0, price, amount, transactionModel);

        // force the fields to the supplied (possibly null) values
        if (amount == null) {
            setField(trade, "amount", null);
        }
        if (value == null) {
            setField(trade, "value", null);
        }
        return trade;
    }

    /** Simple reflection helper to set a private field. */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test data providers                                                  */
    /* --------------------------------------------------------------------- */

    static Stream<Arguments> openedLongProvider() {
        return Stream.of(
                // entry price 5, amount 10 → entry value = 50
                // final price 7 → profit = (7‑5) * 10 = 20
                Arguments.of(
                        Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel),
                        DoubleNum.valueOf(7),
                        DoubleNum.valueOf(20),
                        "Long opened, price rises")
        );
    }

    static Stream<Arguments> openedShortProvider() {
        return Stream.of(
                // entry price 5, amount 10 → entry value = 50
                // final price 3 → profit = (5‑3) * 10 = 20
                Arguments.of(
                        Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel),
                        DoubleNum.valueOf(3),
                        DoubleNum.valueOf(20),
                        "Short opened, price falls")
        );
    }

    static Stream<Arguments> closedLongProvider() {
        return Stream.of(
                // entry 5×10, exit 8×10 → profit = (8‑5) * 10 = 30
                Arguments.of(
                        Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel),
                        Trade.sellAt(1, DoubleNum.valueOf(8), DoubleNum.valueOf(10), transactionModel),
                        DoubleNum.valueOf(0), // finalPrice is ignored for closed positions
                        DoubleNum.valueOf(30),
                        "Long closed, price rises")
        );
    }

    static Stream<Arguments> closedShortProvider() {
        return Stream.of(
                // entry 5×10, exit 2×10 → profit = (5‑2) * 10 = 30
                Arguments.of(
                        Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel),
                        Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(10), transactionModel),
                        DoubleNum.valueOf(0),
                        DoubleNum.valueOf(30),
                        "Short closed, price falls")
        );
    }

    /* --------------------------------------------------------------------- */
    /* Actual tests                                                         */
    /* --------------------------------------------------------------------- */

    @Nested
    class OpenedLongTests {

        @ParameterizedTest(name = "{index} ⇒ finalPrice={1} ⇒ expected={2} ({3})")
        @MethodSource("org.ta4j.core.Position_getGrossProfit_2_Test#openedLongProvider")
        void openedLong(Trade entry, Num finalPrice, Num expected, String _desc) {
            Position pos = openedPosition(entry);
            Num actual = pos.getGrossProfit(finalPrice);
            assertEquals(expected, actual);
        }

        @Test
        void nullFinalPriceThrowsNPE() {
            Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel);
            Position pos = openedPosition(entry);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(null));
        }

        @Test
        void nullEntryAmountThrowsNPE() {
            Trade entry = mockTrade(Trade.TradeType.BUY, DoubleNum.valueOf(5), null, DoubleNum.valueOf(50));
            Position pos = openedPosition(entry);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(7)));
        }

        @Test
        void nullEntryValueThrowsNPE() {
            Trade entry = mockTrade(Trade.TradeType.BUY, DoubleNum.valueOf(5), DoubleNum.valueOf(10), null);
            Position pos = openedPosition(entry);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(7)));
        }
    }

    @Nested
    class OpenedShortTests {

        @ParameterizedTest(name = "{index} ⇒ finalPrice={1} ⇒ expected={2} ({3})")
        @MethodSource("org.ta4j.core.Position_getGrossProfit_2_Test#openedShortProvider")
        void openedShort(Trade entry, Num finalPrice, Num expected, String _desc) {
            Position pos = openedPosition(entry);
            Num actual = pos.getGrossProfit(finalPrice);
            assertEquals(expected, actual);
        }

        @Test
        void nullFinalPriceThrowsNPE() {
            Trade entry = Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel);
            Position pos = openedPosition(entry);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(null));
        }

        @Test
        void nullEntryAmountThrowsNPE() {
            Trade entry = mockTrade(Trade.TradeType.SELL, DoubleNum.valueOf(5), null, DoubleNum.valueOf(50));
            Position pos = openedPosition(entry);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(7)));
        }

        @Test
        void nullEntryValueThrowsNPE() {
            Trade entry = mockTrade(Trade.TradeType.SELL, DoubleNum.valueOf(5), DoubleNum.valueOf(10), null);
            Position pos = openedPosition(entry);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(7)));
        }
    }

    @Nested
    class ClosedLongTests {

        @ParameterizedTest(name = "{index} ⇒ finalPrice={2} ⇒ expected={3}")
        @MethodSource("org.ta4j.core.Position_getGrossProfit_2_Test#closedLongProvider")
        void closedLong(Trade entry, Trade exit, Num finalPrice, Num expected, String _desc) {
            Position pos = closedPosition(entry, exit);
            Num actual = pos.getGrossProfit(finalPrice);
            assertEquals(expected, actual);
        }

        @Test
        void nullExitTradeThrowsNPE() {
            Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel);
            Trade exit = Trade.sellAt(1, DoubleNum.valueOf(8), DoubleNum.valueOf(10), transactionModel);
            Position pos = closedPosition(entry, exit);
            setField(pos, "exit", null);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(0)));
        }

        @Test
        void nullExitValueThrowsNPE() {
            Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel);
            Trade exit = mockTrade(Trade.TradeType.SELL, DoubleNum.valueOf(8), DoubleNum.valueOf(10), null);
            Position pos = closedPosition(entry, exit);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(0)));
        }

        @Test
        void nullEntryValueThrowsNPE() {
            Trade entry = mockTrade(Trade.TradeType.BUY, DoubleNum.valueOf(5), DoubleNum.valueOf(10), null);
            Trade exit = Trade.sellAt(1, DoubleNum.valueOf(8), DoubleNum.valueOf(10), transactionModel);
            Position pos = closedPosition(entry, exit);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(0)));
        }
    }

    @Nested
    class ClosedShortTests {

        @ParameterizedTest(name = "{index} ⇒ finalPrice={2} ⇒ expected={3}")
        @MethodSource("org.ta4j.core.Position_getGrossProfit_2_Test#closedShortProvider")
        void closedShort(Trade entry, Trade exit, Num finalPrice, Num expected, String _desc) {
            Position pos = closedPosition(entry, exit);
            Num actual = pos.getGrossProfit(finalPrice);
            assertEquals(expected, actual);
        }

        @Test
        void nullExitTradeThrowsNPE() {
            Trade entry = Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel);
            Trade exit = Trade.buyAt(1, DoubleNum.valueOf(8), DoubleNum.valueOf(10), transactionModel);
            Position pos = closedPosition(entry, exit);
            setField(pos, "exit", null);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(0)));
        }

        @Test
        void nullExitValueThrowsNPE() {
            Trade entry = Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(10), transactionModel);
            Trade exit = mockTrade(Trade.TradeType.BUY, DoubleNum.valueOf(8), DoubleNum.valueOf(10), null);
            Position pos = closedPosition(entry, exit);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(0)));
        }

        @Test
        void nullEntryValueThrowsNPE() {
            Trade entry = mockTrade(Trade.TradeType.SELL, DoubleNum.valueOf(5), DoubleNum.valueOf(10), null);
            Trade exit = Trade.buyAt(1, DoubleNum.valueOf(8), DoubleNum.valueOf(10), transactionModel);
            Position pos = closedPosition(entry, exit);
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(0)));
        }
    }

    @Nested
    class NullEntryTradeTests {

        @Test
        void openedPositionWithNullEntryThrowsNPE() {
            Position pos = new Position(); // entry stays null
            assertThrows(NullPointerException.class, () -> pos.getGrossProfit(DoubleNum.valueOf(5)));
        }
    }
}
```