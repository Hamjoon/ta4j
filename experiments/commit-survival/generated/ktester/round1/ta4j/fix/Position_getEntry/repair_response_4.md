```java
package org.ta4j.core;

import static org.ta4j.core.num.NaN.NaN;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.ta4j.core.Trade;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.*;
import java.util.stream.Stream;

/**
 * Tests for {@link Position#getEntry()} and related behaviour.
 */
class Position_getEntry_Test {

    private Position defaultPosition;
    private Position sellPosition;
    private Position positionWithTrades;
    private Trade entryTrade;
    private Trade exitTrade;

    @BeforeEach
    void setupBeforeEach() {
        // Positions created with the default and custom starting types.
        // BUY by default
        defaultPosition = new Position();
        // explicit SELL
        sellPosition = new Position(TradeType.SELL);
        // Create simple trades (price/amount are NaN because they are not needed for
        // the getEntry() contract). Use ZeroCostModel to avoid dealing with cost
        // calculations.
        CostModel zeroCost = new ZeroCostModel();
        entryTrade = Trade.buyAt(0, NaN, NaN, zeroCost);
        exitTrade = Trade.sellAt(1, NaN, NaN, zeroCost);
        // Position built directly from entry/exit trades.
        positionWithTrades = new Position(entryTrade, exitTrade, zeroCost, zeroCost);
    }

    /**
     * Parameter source for operate‑based entry creation.
     */
    private static Stream<Arguments> operateParameters() {
        return Stream.of(
                Arguments.of(5, DoubleNum.valueOf(100), DoubleNum.valueOf(10)),
                Arguments.of(2, DoubleNum.valueOf(200), DoubleNum.valueOf(5))
        );
    }

    @Test
    void whenPositionCreatedWithDefaultConstructor_entryIsCreatedAfterFirstOperate() {
        // Act – creates the entry trade using NaN price/amount
        defaultPosition.operate(0, NaN, NaN);
        // Assert
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN, new ZeroCostModel());
        assertEquals(expectedEntry, defaultPosition.getEntry(),
                "Entry trade should be a BUY at index 0 with NaN price/amount");
    }

    @Test
    void whenPositionCreatedWithSellStartingType_entryIsSellAfterFirstOperate() {
        // Act – creates the entry trade (SELL) using NaN price/amount
        sellPosition.operate(0, NaN, NaN);
        // Assert
        Trade expectedEntry = Trade.sellAt(0, NaN, NaN, new ZeroCostModel());
        assertEquals(expectedEntry, sellPosition.getEntry(),
                "Entry trade should be a SELL at index 0 with NaN price/amount");
    }

    @Test
    void whenPositionConstructedWithExplicitTrades_getEntryReturnsSuppliedEntry() {
        // No operation needed – the entry is already set via the constructor.
        assertEquals(entryTrade, positionWithTrades.getEntry(),
                "getEntry() must return the entry trade supplied to the constructor");
    }

    @Test
    void whenPositionHasNotOperatedYet_getEntryReturnsNull() {
        // A freshly created Position (default constructor) has not been operated.
        Position fresh = new Position();
        assertNull(fresh.getEntry(), "Before any operate call, entry should be null");
    }

    @Test
    void entryTradeIsIndependentFromExitTrade() {
        // Verify that the entry returned is exactly the object we passed in,
        // not the exit trade.
        assertNotEquals(exitTrade, positionWithTrades.getEntry(),
                "Entry trade must not be the same as the exit trade");
    }

    @Test
    void entryTradePreservesCustomPriceAndAmount() {
        // Use a non‑NaN price/amount to ensure they are preserved.
        CostModel zeroCost = new ZeroCostModel();
        Num price = DoubleNum.valueOf(10.5);
        Num amount = DoubleNum.valueOf(2);
        Trade customEntry = Trade.buyAt(5, price, amount, zeroCost);
        Trade customExit = Trade.sellAt(6, price, amount, zeroCost);
        Position customPos = new Position(customEntry, customExit, zeroCost, zeroCost);
        Trade result = customPos.getEntry();
        assertEquals(customEntry, result, "The entry trade should be exactly the one supplied");
        assertEquals(price, result.getPricePerAsset(),
                "Price per asset of the entry trade must be preserved");
        assertEquals(amount, result.getAmount(),
                "Amount of the entry trade must be preserved");
    }

    @Test
    void operateCreatesEntryAndGetEntryReturnsSameInstance() {
        // given
        Position pos = new Position();
        Num price = DoubleNum.valueOf(100);
        Num amount = DoubleNum.valueOf(10);
        int index = 5;
        // when
        Trade created = pos.operate(index, price, amount);
        // then
        assertNotNull(created, "operate should return a non‑null Trade");
        assertSame(created, pos.getEntry(),
                "getEntry() must return the same Trade instance created by operate");
    }

    @Test
    void secondOperateDoesNotReplaceEntryTrade() {
        // given
        Position pos = new Position();
        Num price = DoubleNum.valueOf(100);
        Num amount = DoubleNum.valueOf(10);
        int firstIdx = 5;
        int secondIdx = 6;
        // first operate creates the entry
        Trade entry = pos.operate(firstIdx, price, amount);
        // second operate should create the exit (or be ignored)
        Trade second = pos.operate(secondIdx, price, amount);
        // then
        assertSame(entry, pos.getEntry(),
                "Entry trade must stay the same after a second operate call");
        assertNotSame(entry, second,
                "The second trade (exit) must be a different instance");
    }

    @Test
    void entryRemainsAfterExitIsSet() {
        // given
        Position pos = new Position();
        Num entryPrice = DoubleNum.valueOf(50);
        Num exitPrice = DoubleNum.valueOf(55);
        Num amount = DoubleNum.valueOf(20);
        int entryIdx = 3;
        int exitIdx = 8;
        // open position
        Trade entry = pos.operate(entryIdx, entryPrice, amount);
        // close position
        Trade exit = pos.operate(exitIdx, exitPrice, amount);
        // then
        assertSame(entry, pos.getEntry(),
                "Entry trade must remain unchanged after the position is closed");
        assertNotSame(entry, exit,
                "Exit trade must be a different instance");
    }

    @Test
    void getEntryIsIdempotent() {
        // given
        Position pos = new Position();
        pos.operate(2, DoubleNum.valueOf(200), DoubleNum.valueOf(5));
        // when
        Trade first = pos.getEntry();
        Trade second = pos.getEntry();
        Trade third = pos.getEntry();
        // then
        assertSame(first, second,
                "Second call must return the same reference as first");
        assertSame(first, third,
                "Third call must return the same reference as first");
    }

    @Test
    void entryTradeIsPreservedThroughSerialization() throws IOException, ClassNotFoundException {
        // given
        Position original = new Position();
        original.operate(1, DoubleNum.valueOf(75), DoubleNum.valueOf(15));
        Trade originalEntry = original.getEntry();
        // serialize
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(original);
            out.flush();
            bytes = bos.toByteArray();
        }
        // deserialize
        Position deserialized;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            deserialized = (Position) in.readObject();
        }
        // then
        Trade deserializedEntry = deserialized.getEntry();
        assertNotNull(deserializedEntry, "Deserialized position must have a non‑null entry");
        assertEquals(originalEntry, deserializedEntry,
                "Deserialized entry trade must be equal to the original entry trade");
    }

    @ParameterizedTest
    @MethodSource("operateParameters")
    void parameterizedOperateCreatesEntry(int index, Num price, Num amount) {
        Position pos = new Position();
        Trade created = pos.operate(index, price, amount);
        assertSame(created, pos.getEntry(),
                "For index " + index + " getEntry must return the Trade created by operate");
    }
}
```