package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.num.DoubleNum;
import java.util.stream.Stream;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import static org.junit.jupiter.api.Assertions.*;

// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.junit.jupiter.MockitoExtension;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.junit.jupiter.api.Assertions.*;
// @ExtendWith(MockitoExtension.class)
class Position_equals_Test {

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

    //     @BeforeEach
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
        defaultPos1 = new Position();
        defaultPos1.operate(1);
        defaultPos1.operate(2);
        defaultPos2 = new Position();
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
        // BUY start, no trades
        nullPos1 = new Position();
        // SELL start, no trades
        nullPos2 = new Position(Trade.TradeType.SELL);
        /* -------------------------------------------------------------
         * Positions that differ only by entry trade
         * ------------------------------------------------------------- */
        diffEntryPos = new Position();
        diffEntryPos.operate(5);
        diffEntryPos.operate(6);
        // another position with a different entry trade (kept for a dedicated test)
        Position otherEntryPos = new Position();
        otherEntryPos.operate(7);
        otherEntryPos.operate(8);
        // reference kept for clarity (not used directly in assertions)
        Position diffEntryPosReference = otherEntryPos;
        /* -------------------------------------------------------------
         * Positions that differ only by exit trade
         * ------------------------------------------------------------- */
        diffExitPos = new Position();
        diffExitPos.operate(1);
        diffExitPos.operate(2);
        // another position with a different exit trade (kept for a dedicated test)
        Position otherExitPos = new Position();
        otherExitPos.operate(1);
        otherExitPos.operate(3);
        Position diffExitPosReference = otherExitPos;
        /* -------------------------------------------------------------
         * Positions built from explicit Trade objects
         * ------------------------------------------------------------- */
        tradesPos1 = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
        tradesPos2 = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    /**
     * Provides pairs of positions for which both entry and exit are {@code null}.
     * The starting type (BUY/SELL) is varied to ensure that {@code equals}
     * treats the null‑state as equal irrespective of the type.
     */
//     static Stream<Arguments> nullEntryExitProvider() {
//         // BUY start, no trades
//         Position buyNull = new Position();
//         // SELL start, no trades
//         Position sellNull = new Position(Trade.TradeType.SELL);
//         return Stream.of(Arguments.of(buyNull, buyNull), Arguments.of(buyNull, sellNull), Arguments.of(sellNull, buyNull), Arguments.of(sellNull, sellNull));
//     }

//     @Test
//     void equalsSameTradesDefaultStartingType() {
//         assertTrue(defaultPos1.equals(defaultPos2), "Two positions with identical BUY entry/exit trades must be equal");
//         assertEquals(defaultPos1, defaultPos2, "assertEquals should delegate to equals");
//     }

//     @Test
//     void equalsSameTradesSellStartingType() {
//         assertTrue(sellPos1.equals(sellPos2), "Two positions with identical SELL entry/exit trades must be equal");
//         assertEquals(sellPos1, sellPos2);
//     }

//     @Test
//     void equalsWhenBothEntryAndExitAreNull() {
//         assertTrue(nullPos1.equals(nullPos2), "Positions with null entry and null exit should be equal regardless of starting type");
//         assertEquals(nullPos1, nullPos2);
//     }

//     @Test
//     void notEqualsWhenEntryDiffers() {
//         Position p1 = new Position();
//         p1.operate(1);
//         p1.operate(2);
//         Position p2 = new Position();
//         p2.operate(3);
//         p2.operate(4);
//         assertFalse(p1.equals(p2), "Positions with different entry trades must not be equal");
//     }

//     @Test
//     void notEqualsWhenExitDiffers() {
//         Position p1 = new Position();
//         p1.operate(1);
//         p1.operate(2);
//         Position p2 = new Position();
//         p2.operate(1);
//         p2.operate(3);
//         assertFalse(p1.equals(p2), "Positions with same entry but different exit trades must not be equal");
//     }

//     @Test
//     void equalsWhenConstructedFromSameTradeObjects() {
//         assertTrue(tradesPos1.equals(tradesPos2), "Positions built from the same entry/exit Trade instances must be equal");
//         assertEquals(tradesPos1, tradesPos2);
//     }

//     @Test
//     void notEqualsWithDifferentObjectType() {
//         assertFalse(defaultPos1.equals("not a position"), "equals must return false when compared with an object of another type");
//     }

//     @Test
//     void reflexiveAndSymmetricProperties() {
//         // Reflexive
//         assertTrue(defaultPos1.equals(defaultPos1), "equals must be reflexive");
//         // Symmetric
//         assertTrue(defaultPos1.equals(defaultPos2) && defaultPos2.equals(defaultPos1), "equals must be symmetric");
//     }

//     @Test
//     void hashCodeConsistentWithEquals() {
//         assertEquals(defaultPos1, defaultPos2);
//         assertEquals(defaultPos1.hashCode(), defaultPos2.hashCode(), "Equal objects must have identical hashCodes");
//     }

//     @Test
//     @DisplayName("equals returns false when compared with null")
//     void notEqualsWithNullArgument() {
//         assertFalse(defaultPos1.equals(null), "equals must return false when compared with null");
//     }

//     @ParameterizedTest(name = "[{index}] {0} vs {1}")
//     @MethodSource("nullEntryExitProvider")
//     @DisplayName("Positions with null entry/exit are equal regardless of start type")
//     void equalsWithBothNullEntryAndExit(Position p1, Position p2) {
//         assertTrue(p1.equals(p2), "When both entry and exit are null, positions must be equal regardless of starting type");
//         assertEquals(p1, p2);
//     }

//     @Test
//     @DisplayName("Positions with same trades but opposite starting types are not equal")
//     void notEqualsWhenStartingTypeDiffers() {
//         // BUY position
//         Position buyPos = new Position();
//         // entry BUY
//         buyPos.operate(1);
//         // exit SELL
//         buyPos.operate(2);
//         // SELL position using the same indices (but opposite trade types)
//         Position sellPos = new Position(Trade.TradeType.SELL);
//         // entry SELL
//         sellPos.operate(1);
//         // exit BUY
//         sellPos.operate(2);
//         assertFalse(buyPos.equals(sellPos), "Positions that start with opposite trade types must not be equal even if indices match");
//     }
}