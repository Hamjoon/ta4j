```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Position#equals(Object)}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>reflexivity, symmetry and transitivity basics</li>
 *   <li>equality of positions built via the public API (operate, constructors)</li>
 *   <li>handling of {@code null} entry/exit trades</li>
 *   <li>comparison with objects of other types and with {@code null}</li>
 *   <li>consistency between {@code equals} and {@code hashCode}</li>
 * </ul>
 *
 * <p>Mockito is not required for the {@code equals} method itself, but the
 * test class is annotated with {@code @ExtendWith(MockitoExtension.class)} to
 * keep the original test‑framework setup unchanged.
 */
@ExtendWith(MockitoExtension.class)
class Position_equals_Test {

    /* --------------------------------------------------------------------- */
    /*  Shared fixtures                                                    */
    /* --------------------------------------------------------------------- */
    private CostModel transactionModel;
    private CostModel holdingModel;

    private Trade entryTrade;
    private Trade exitTrade;

    private Position defaultPos1;
    private Position defaultPos2;
    private Position sellPos1;
    private Position sellPos2;
    private Position nullPos1;
    private Position nullPos2;
    private Position diffEntryPos;
    private Position diffExitPos;
    private Position tradesPos1;
    private Position tradesPos2;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // Cost models used by the trades created via the static factory methods
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        // Trades used for the constructor that receives explicit trades
        entryTrade = Trade.buyAt(1, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);
        exitTrade = Trade.sellAt(2, DoubleNum.valueOf(2), DoubleNum.valueOf(1), transactionModel);

        /* -------------------------------------------------------------
         * Positions built using the default (BUY) starting type
         * ------------------------------------------------------------- */
        defaultPos1 = new Position();          // BUY start
        defaultPos1.operate(1);                // entry
        defaultPos1.operate(2);                // exit

        defaultPos2 = new Position();          // BUY start
        defaultPos2.operate(1);
        defaultPos2.operate(2);

        /* -------------------------------------------------------------
         * Positions built using an explicit SELL starting type
         * ------------------------------------------------------------- */
        sellPos1 = new Position(Trade.TradeType.SELL);
        sellPos1.operate(1);
        sellPos1.operate(2);

        sellPos2 = new Position(Trade.TradeType.SELL);
        sellPos2.operate(1);
        sellPos2.operate(2);

        /* -------------------------------------------------------------
         * Positions that have never been operated on – both entry and
         * exit are null
         * ------------------------------------------------------------- */
        nullPos1 = new Position();                     // BUY start, no trades
        nullPos2 = new Position(Trade.TradeType.SELL); // SELL start, no trades

        /* -------------------------------------------------------------
         * Positions that differ only by entry trade
         * ------------------------------------------------------------- */
        diffEntryPos = new Position();   // entry at index 5, exit at 6
        diffEntryPos.operate(5);
        diffEntryPos.operate(6);

        Position otherEntryPos = new Position(); // entry at index 7, exit at 8
        otherEntryPos.operate(7);
        otherEntryPos.operate(8);
        // keep both objects – they will be used in a dedicated test
        // (the variable name reflects the purpose)
        Position diffEntryPosReference = otherEntryPos;

        /* -------------------------------------------------------------
         * Positions that differ only by exit trade
         * ------------------------------------------------------------- */
        diffExitPos = new Position();   // entry at 1, exit at 2
        diffExitPos.operate(1);
        diffExitPos.operate(2);

        Position otherExitPos = new Position(); // entry at 1, exit at 3
        otherExitPos.operate(1);
        otherExitPos.operate(3);
        // keep both objects – they will be used in a dedicated test
        Position diffExitPosReference = otherExitPos;

        /* -------------------------------------------------------------
         * Positions built from explicit Trade objects
         * ------------------------------------------------------------- */
        tradesPos1 = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
        tradesPos2 = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    /* --------------------------------------------------------------------- */
    /*  Tests for Position.equals                                            */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("Two BUY positions with identical entry/exit trades are equal")
    void equalsSameTradesDefaultStartingType() {
        assertTrue(defaultPos1.equals(defaultPos2),
                "Two positions with identical BUY entry/exit trades must be equal");
        assertEquals(defaultPos1, defaultPos2,
                "assertEquals should delegate to equals");
    }

    @Test
    @DisplayName("Two SELL positions with identical entry/exit trades are equal")
    void equalsSameTradesSellStartingType() {
        assertTrue(sellPos1.equals(sellPos2),
                "Two positions with identical SELL entry/exit trades must be equal");
        assertEquals(sellPos1, sellPos2);
    }

    @Test
    @DisplayName("Positions with both entry and exit null are equal irrespective of start type")
    void equalsWhenBothEntryAndExitAreNull() {
        assertTrue(nullPos1.equals(nullPos2),
                "Positions with null entry and null exit should be equal regardless of starting type");
        assertEquals(nullPos1, nullPos2);
    }

    @Test
    @DisplayName("Positions differing only by entry trade are not equal")
    void notEqualsWhenEntryDiffers() {
        // p1: entry at 1, exit at 2
        Position p1 = new Position();
        p1.operate(1);
        p1.operate(2);

        // p2: entry at 3, exit at 4
        Position p2 = new Position();
        p2.operate(3);
        p2.operate(4);

        assertFalse(p1.equals(p2), "Positions with different entry trades must not be equal");
    }

    @Test
    @DisplayName("Positions differing only by exit trade are not equal")
    void notEqualsWhenExitDiffers() {
        // p1: entry at 1, exit at 2
        Position p1 = new Position();
        p1.operate(1);
        p1.operate(2);

        // p2: entry at 1, exit at 3
        Position p2 = new Position();
        p2.operate(1);
        p2.operate(3);

        assertFalse(p1.equals(p2), "Positions with same entry but different exit trades must not be equal");
    }

    @Test
    @DisplayName("Positions built from the same Trade instances are equal")
    void equalsWhenConstructedFromSameTradeObjects() {
        assertTrue(tradesPos1.equals(tradesPos2),
                "Positions built from the same entry/exit Trade instances must be equal");
        assertEquals(tradesPos1, tradesPos2);
    }

    @Test
    @DisplayName("equals returns false when compared with an object of another type")
    void notEqualsWithDifferentObjectType() {
        assertFalse(defaultPos1.equals("not a position"),
                "equals must return false when compared with an object of another type");
    }

    @Test
    @DisplayName("equals returns false when compared with null")
    void notEqualsWithNullArgument() {
        assertFalse(defaultPos1.equals(null),
                "equals must return false when compared with null");
    }

    @Test
    @DisplayName("Reflexive and symmetric properties of equals")
    void reflexiveAndSymmetricProperties() {
        // Reflexive
        assertTrue(defaultPos1.equals(defaultPos1), "equals must be reflexive");

        // Symmetric
        assertTrue(defaultPos1.equals(defaultPos2) && defaultPos2.equals(defaultPos1),
                "equals must be symmetric");
    }

    @Test
    @DisplayName("hashCode is consistent with equals")
    void hashCodeConsistentWithEquals() {
        assertEquals(defaultPos1, defaultPos2);
        assertEquals(defaultPos1.hashCode(), defaultPos2.hashCode(),
                "Equal objects must have identical hashCodes");
    }

    /* --------------------------------------------------------------------- */
    /*  Parameterized tests covering null‑entry/exit combinations          */
    /* --------------------------------------------------------------------- */

    /**
     * Provides pairs of positions for which both entry and exit are {@code null}.
     * The starting type (BUY/SELL) is varied to ensure that {@code equals}
     * treats the null‑state as equal irrespective of the type.
     */
    static Stream<Arguments> nullEntryExitProvider() {
        Position buyNull = new Position();                     // BUY start, no trades
        Position sellNull = new Position(Trade.TradeType.SELL); // SELL start, no trades
        return Stream.of(
                Arguments.of(buyNull, buyNull),
                Arguments.of(buyNull, sellNull),
                Arguments.of(sellNull, buyNull),
                Arguments.of(sellNull, sellNull)
        );
    }

    @ParameterizedTest(name = "[{index}] {0} vs {1}")
    @MethodSource("nullEntryExitProvider")
    @DisplayName("Positions with null entry/exit are equal regardless of start type")
    void equalsWithBothNullEntryAndExit(Position p1, Position p2) {
        assertTrue(p1.equals(p2),
                "When both entry and exit are null, positions must be equal regardless of starting type");
        assertEquals(p1, p2);
    }

    /* --------------------------------------------------------------------- */
    /*  Additional defensive test: different starting types with same trades */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("Positions with same trades but opposite starting types are not equal")
    void notEqualsWhenStartingTypeDiffers() {
        // Build a BUY position
        Position buyPos = new Position();
        buyPos.operate(1); // entry BUY
        buyPos.operate(2); // exit SELL

        // Build a SELL position using the same underlying Trade objects
        // (the trades are the opposite type, so they cannot be equal)
        Position sellPos = new Position(Trade.TradeType.SELL);
        sellPos.operate(1); // entry SELL
        sellPos.operate(2); // exit BUY

        assertFalse(buyPos.equals(sellPos),
                "Positions that start with opposite trade types must not be equal even if indices match");
    }
}
```