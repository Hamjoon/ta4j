```java
package org.ta4j.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import static org.ta4j.core.num.NaN.NaN;

import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;

/**
 * Tests for {@link Position#getGrossReturn(Num)}.
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_3_Test {

    private static final CostModel ZERO_COST = new ZeroCostModel();

    private Position closedPosition;
    private Position openPosition;
    private Trade entryTrade;
    private Trade exitTrade;

    @BeforeEach
    void setupBeforeEach() {
        // entry price = 2, amount = 1 (the amount is irrelevant for gross return)
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(2), DoubleNum.valueOf(1), ZERO_COST);
        exitTrade = Trade.sellAt(5, DoubleNum.valueOf(4), DoubleNum.valueOf(1), ZERO_COST);
        // Closed position – both entry and exit are supplied
        closedPosition = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);
        // Open position – only the entry trade is supplied via the constructor
        openPosition = new Position(TradeType.BUY, ZERO_COST, ZERO_COST);
        // Simulate the entry trade (index 0, price 2)
        openPosition.operate(0, DoubleNum.valueOf(2), DoubleNum.valueOf(1));
    }

    /**
     * Helper to compare {@link Num} values using {@link Assertions#assertTrue(boolean, java.util.function.Supplier)}.
     * The {@code DoubleNum} implementation provides proper {@code equals} semantics.
     */
    private static void assertEquals(Num expected, Num actual) {
        assertTrue(expected.equals(actual), () -> "Expected: " + expected + " but was: " + actual);
    }

    /**
     * Creates a fresh {@link Position} whose entry trade price is {@code entryPrice}.
     */
    private static Position positionWithEntryPrice(Num entryPrice) {
        Trade entry = Trade.buyAt(0, entryPrice, DoubleNum.valueOf(1), ZERO_COST);
        // exit trade is irrelevant for the one‑argument overload – we can reuse a dummy one
        Trade exit = Trade.sellAt(1, DoubleNum.valueOf(entryPrice.doubleValue() + 1), DoubleNum.valueOf(1), ZERO_COST);
        return new Position(entry, exit, ZERO_COST, ZERO_COST);
    }

    /**
     * Sets the private {@code entry} field of {@code position} to {@code null} via reflection.
     */
    private static void nullifyEntryTrade(Position position) throws Exception {
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, null);
    }

    static Stream<Arguments> delegationAndEdgeCasesProvider() {
        return Stream.of(
                // happy‑path
                Arguments.of(DoubleNum.valueOf(110), DoubleNum.valueOf(100), null),
                // loss scenario
                Arguments.of(DoubleNum.valueOf(95), DoubleNum.valueOf(100), null),
                // final price NaN
                Arguments.of(NaN, DoubleNum.valueOf(100), null),
                // final price null -> NPE
                Arguments.of(null, DoubleNum.valueOf(100), NullPointerException.class),
                // entry price null (entry trade null) -> NPE
                Arguments.of(DoubleNum.valueOf(120), null, NullPointerException.class),
                // entry price NaN
                Arguments.of(DoubleNum.valueOf(130), NaN, null)
        );
    }

    static Stream<Arguments> additionalEdgeCasesProvider() {
        return Stream.of(
                // normal profit: (120‑100)/100 = 0.2
                Arguments.of(DoubleNum.valueOf(120), DoubleNum.valueOf(100), DoubleNum.valueOf(0.2)),
                // total loss: (0‑100)/100 = -1
                Arguments.of(DoubleNum.valueOf(0), DoubleNum.valueOf(100), DoubleNum.valueOf(-1)),
                // division by zero (entry=0, final=0) → NaN
                Arguments.of(DoubleNum.valueOf(0), DoubleNum.valueOf(0), NaN),
                // division by zero (entry=0, final=50) → NaN
                Arguments.of(DoubleNum.valueOf(50), DoubleNum.valueOf(0), NaN),
                // negative final price: (-30‑100)/100 = -1.3
                Arguments.of(DoubleNum.valueOf(-30), DoubleNum.valueOf(100), DoubleNum.valueOf(-1.3)),
                // negative entry price: (110‑(-100))/(-100) = -2.1
                Arguments.of(DoubleNum.valueOf(110), DoubleNum.valueOf(-100), DoubleNum.valueOf(-2.1)),
                // no change: (100‑100)/100 = 0
                Arguments.of(DoubleNum.valueOf(100), DoubleNum.valueOf(100), DoubleNum.valueOf(0)),
                // final price NaN → NaN
                Arguments.of(NaN, DoubleNum.valueOf(100), NaN),
                // entry price null (price per asset returns null) → NPE
                Arguments.of(DoubleNum.valueOf(130), null, NullPointerException.class),
                // both NaN → NaN
                Arguments.of(NaN, NaN, NaN)
        );
    }

    /**
     * For a closed position the gross return should be {@code exitPrice / entryPrice}.
     * With entry = 2 and exit = 4 the expected gross return is 2.
     */
    @Test
    void whenClosedPosition_thenGrossReturnIsRatioOfExitToEntry() {
        Num result = closedPosition.getGrossReturn(DoubleNum.valueOf(4));
        assertEquals(DoubleNum.valueOf(2), result);
    }

    /**
     * For an open position the method must use the supplied final price.
     * With entry = 2 and a supplied final price of 4 the expected gross return is 2.
     */
    @Test
    void whenOpenPosition_thenGrossReturnUsesProvidedFinalPrice() {
        Num result = openPosition.getGrossReturn(DoubleNum.valueOf(4));
        assertEquals(DoubleNum.valueOf(2), result);
    }

    /**
     * If the final price is {@code NaN} the method should propagate the NaN value.
     */
    @Test
    void whenFinalPriceIsNaN_thenResultIsNaN() {
        Num result = openPosition.getGrossReturn(NaN);
        assertTrue(result.isNaN(), "Result should be NaN when final price is NaN");
    }

    @ParameterizedTest(name = "{index}: finalPrice={0}, entryPrice={1}, expectedException={2}")
    @MethodSource("delegationAndEdgeCasesProvider")
    void testDelegationAndEdgeCases(Num finalPrice, Num entryPrice, Class<? extends Throwable> expectedException) throws Exception {
        Position position;
        if (entryPrice == null) {
            // create a normal position first, then nullify its entry trade
            position = positionWithEntryPrice(DoubleNum.valueOf(100));
            nullifyEntryTrade(position);
        } else {
            position = positionWithEntryPrice(entryPrice);
        }
        if (expectedException != null) {
            assertThrows(expectedException, () -> position.getGrossReturn(finalPrice));
        } else {
            // Expected value is the result of the two‑argument overload
            Num expected = position.getGrossReturn(entryPrice, finalPrice);
            Num actual = position.getGrossReturn(finalPrice);
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest(name = "{index}: entry={1}, final={0}")
    @MethodSource("additionalEdgeCasesProvider")
    void testAdditionalEdgeCases(Num finalPrice, Num entryPrice, Object expected) throws Exception {
        Position position;
        if (entryPrice == null) {
            // Mock a Trade whose getPricePerAsset() returns null
            Trade entryMock = mock(Trade.class);
            when(entryMock.getPricePerAsset()).thenReturn(null);
            // Dummy exit trade – never used
            Trade exitMock = mock(Trade.class);
            position = new Position(entryMock, exitMock, ZERO_COST, ZERO_COST);
        } else {
            position = positionWithEntryPrice(entryPrice);
        }
        if (expected instanceof Class && Throwable.class.isAssignableFrom((Class<?>) expected)) {
            // Expected an exception
            assertThrows((Class<? extends Throwable>) expected, () -> position.getGrossReturn(finalPrice));
        } else {
            // Expected a Num (could be NaN)
            Num actual = position.getGrossReturn(finalPrice);
            Num expectedNum = (Num) expected;
            if (expectedNum.isNaN()) {
                assertTrue(actual.isNaN(), "Result should be NaN");
            } else {
                assertEquals(expectedNum, actual);
            }
        }
    }

    @Test
    void whenEntryPriceIsNaN_thenResultIsNaN() {
        Position pos = positionWithEntryPrice(NaN);
        Num result = pos.getGrossReturn(DoubleNum.valueOf(130));
        assertTrue(result.isNaN(), "Result should be NaN when entry price is NaN");
    }

    @Test
    void whenEntryTradeIsNull_thenNPE() throws Exception {
        Position pos = positionWithEntryPrice(DoubleNum.valueOf(100));
        nullifyEntryTrade(pos);
        assertThrows(NullPointerException.class, () -> pos.getGrossReturn(DoubleNum.valueOf(120)));
    }

    @Test
    void whenFinalPriceIsNull_thenNPE() {
        Position pos = positionWithEntryPrice(DoubleNum.valueOf(100));
        assertThrows(NullPointerException.class, () -> pos.getGrossReturn(null));
    }
}
```