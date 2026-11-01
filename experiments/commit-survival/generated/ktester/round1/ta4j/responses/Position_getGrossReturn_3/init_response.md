```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link Position#getGrossReturn(Num)}.
 *
 * The method simply delegates to {@code getGrossReturn(entryPrice, finalPrice)}.
 * Therefore the expected behaviour is a ratio between the final price and the
 * entry price (i.e. {@code finalPrice / entryPrice}).
 *
 * The tests cover:
 * <ul>
 *   <li>a closed position (entry + exit trade)</li>
 *   <li>an open position (only entry trade, final price supplied by the caller)</li>
 *   <li>the handling of {@code NaN} as the final price</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_3_Test {

    private static final CostModel ZERO_COST = new ZeroCostModel();

    private Position closedPosition;
    private Position openPosition;

    private Trade entryTrade;
    private Trade exitTrade;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // entry price = 2, amount = 1 (the amount is irrelevant for gross return)
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(2), DoubleNum.valueOf(1), ZERO_COST);
        exitTrade  = Trade.sellAt(5, DoubleNum.valueOf(4), DoubleNum.valueOf(1), ZERO_COST);

        // Closed position – both entry and exit are supplied
        closedPosition = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);

        // Open position – only the entry trade is supplied via the constructor
        openPosition = new Position(TradeType.BUY, ZERO_COST, ZERO_COST);
        // Simulate the entry trade (index 0, price 2)
        openPosition.operate(0, DoubleNum.valueOf(2), DoubleNum.valueOf(1));
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up after each test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release
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

    /**
     * Helper to compare {@link Num} values using {@link Assertions#assertEquals(Object, Object)}.
     * The {@code DoubleNum} implementation provides proper {@code equals} semantics.
     */
    private static void assertEquals(Num expected, Num actual) {
        assertTrue(expected.equals(actual),
                () -> "Expected: " + expected + " but was: " + actual);
    }
}
```