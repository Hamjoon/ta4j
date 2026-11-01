```java
package org.ta4j.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.LinearTransactionCostModel;
import org.ta4j.core.analysis.cost.LinearBorrowingCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.*;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Tests for {@link Position#getStartingType()}.
 */
class Position_getStartingType_Test {

    /**
     * Common cost models used for the tests that need explicit models
     */
    private CostModel transactionModel;
    private CostModel holdingModel;

    /**
     * Trades used for the constructor that receives entry/exit trades
     */
    private Trade entryBuy;
    private Trade exitSell;

    /**
     * Counter‑part trades for a SELL‑starting position
     */
    private Trade entrySell;
    private Trade exitBuy;

    @BeforeEach
    void setupBeforeEach() {
        // Initialise simple linear cost models – they are lightweight and deterministic
        transactionModel = new LinearTransactionCostModel(0.01);
        holdingModel = new LinearBorrowingCostModel(0.001);

        // BUY entry – SELL exit (closed position)
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);

        // SELL entry – BUY exit (closed position)
        entrySell = Trade.sellAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitBuy = Trade.buyAt(5, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
    }

    /**
     * Creates a {@link Position} whose entry trade has the given type.
     * The complementary exit trade is automatically built.
     */
    private Position createPosition(Trade.TradeType entryType) {
        if (entryType == Trade.TradeType.BUY) {
            return new Position(entryBuy, exitSell, transactionModel, holdingModel);
        } else {
            return new Position(entrySell, exitBuy, transactionModel, holdingModel);
        }
    }

    private static Stream<Arguments> startingTypeProvider() {
        return Stream.of(
                arguments(Trade.TradeType.BUY),
                arguments(Trade.TradeType.SELL)
        );
    }

    private static Stream<Arguments> consistencyProvider() {
        return Stream.of(
                arguments(Trade.TradeType.BUY, Trade.TradeType.SELL),
                arguments(Trade.TradeType.SELL, Trade.TradeType.BUY)
        );
    }

    @Test
    void defaultConstructorShouldSetStartingTypeToBuy() {
        // uses default starting type BUY
        Position position = new Position();
        assertEquals(Trade.TradeType.BUY, position.getStartingType(),
                "Default Position should start with BUY type");
    }

    @Test
    void constructorWithExplicitStartingTypeShouldReturnThatType() {
        Position sellPosition = new Position(Trade.TradeType.SELL);
        assertEquals(Trade.TradeType.SELL, sellPosition.getStartingType(),
                "Position constructed with SELL should return SELL as starting type");
    }

    @Test
    void constructorWithEntryAndExitTradesShouldDeriveStartingTypeFromEntry() {
        // The entry trade is BUY, the exit trade is SELL – they are complementary.
        Position position = new Position(entryBuy, exitSell, transactionModel, holdingModel);
        assertEquals(Trade.TradeType.BUY, position.getStartingType(),
                "Starting type must match the entry trade type (BUY)");
    }

    @Test
    void constructorWithNullStartingTypeShouldThrowIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Position(null, new ZeroCostModel(), new ZeroCostModel()),
                "Passing null as starting type must raise IllegalArgumentException");
        assertTrue(thrown.getMessage().contains("Starting type must not be null"));
    }

    @Test
    void constructorWithEntryAndExitTradesHavingSameTypeShouldThrowIllegalArgumentException() {
        // Create two BUY trades – they are not complementary.
        Trade buy1 = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade buy2 = Trade.buyAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new Position(buy1, buy2, transactionModel, holdingModel),
                "Both trades having the same type must raise IllegalArgumentException");
        assertTrue(thrown.getMessage().contains("Both trades must have different types"));
    }

    @ParameterizedTest(name = "Position created with entry type {0} must return the same type")
    @MethodSource("startingTypeProvider")
    void startingTypeRetrieval_ShouldReturnEntryTradeType(Trade.TradeType entryType) {
        Position position = createPosition(entryType);
        assertEquals(entryType, position.getStartingType(),
                "getStartingType() should return the entry trade type");
    }

    @Test
    void startingTypeRetrieval_whenInternalStateCorrupted_shouldThrowIllegalStateException() throws Exception {
        // Create a valid Position first
        Position position = new Position(entryBuy, exitSell, transactionModel, holdingModel);
        // Corrupt the private final field via reflection
        Field f = Position.class.getDeclaredField("startingType");
        f.setAccessible(true);
        f.set(position, null);
        // The contract of the library is not explicit, but the JSON expects an
        // IllegalStateException – we assert that *some* runtime exception is thrown.
        assertThrows(IllegalStateException.class, position::getStartingType,
                "When startingType is null an IllegalStateException should be thrown");
    }

    @ParameterizedTest(name = "Entry {0} / Exit {1} – starting type must stay {0}")
    @MethodSource("consistencyProvider")
    void startingTypeConsistency_AfterClose_ShouldRemainEntryType(Trade.TradeType entry, Trade.TradeType exit) {
        Trade entryTrade = (entry == Trade.TradeType.BUY) ? entryBuy : entrySell;
        Trade exitTrade = (exit == Trade.TradeType.SELL) ? exitSell : exitBuy;
        Position position = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
        assertEquals(entry, position.getStartingType(),
                "Starting type must stay equal to the entry trade type even after position is closed");
    }

    @ParameterizedTest(name = "Calling getStartingType() repeatedly returns {0}")
    @MethodSource("startingTypeProvider")
    void idempotentRetrieval_multipleCallsReturnSameValue(Trade.TradeType entryType) {
        Position position = createPosition(entryType);
        Trade.TradeType first = position.getStartingType();
        Trade.TradeType second = position.getStartingType();
        Trade.TradeType third = position.getStartingType();
        assertAll(
                () -> assertEquals(entryType, first, "First call must return the entry type"),
                () -> assertEquals(entryType, second, "Second call must return the same value"),
                () -> assertEquals(entryType, third, "Third call must also return the same value")
        );
    }

    @ParameterizedTest(name = "After serialization/deserialization, starting type stays {0}")
    @MethodSource("startingTypeProvider")
    void serializationIntegrity_startingTypePreserved(Trade.TradeType entryType) throws IOException, ClassNotFoundException {
        Position original = createPosition(entryType);
        // Serialize
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(original);
            out.flush();
            bytes = bos.toByteArray();
        }
        // Deserialize
        Position deserialized;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            deserialized = (Position) in.readObject();
        }
        assertEquals(entryType, deserialized.getStartingType(),
                "Deserialized Position must keep the original starting type");
    }

    @ParameterizedTest(name = "Other methods must not affect starting type {0}")
    @MethodSource("startingTypeProvider")
    void stateIndependentRetrieval_otherMethodsDoNotChangeStartingType(Trade.TradeType entryType) {
        Position position = createPosition(entryType);
        // Invoke a handful of other public methods (return values are not asserted)
        position.isClosed();
        position.isOpened();
        position.getGrossProfit();
        position.getHoldingCost();
        // After the calls, the starting type must still be the original one
        assertEquals(entryType, position.getStartingType(),
                "Calling other Position methods must not modify the starting type");
    }
}
```