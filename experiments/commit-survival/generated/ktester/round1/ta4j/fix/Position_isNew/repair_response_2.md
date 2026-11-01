```java
package org.ta4j.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Tests for {@link Position#isNew()}.
 */
@ExtendWith(MockitoExtension.class)
class Position_isNew_Test {

    /**
     * A cost model is required when creating {@link Trade} instances via the static factories.
     */
    private static final CostModel transactionModel = new ZeroCostModel();

    /**
     * Functional interface used to configure a {@link Position} instance
     * before the assertion is performed.
     */
    @FunctionalInterface
    private interface PositionSetup {
        Position apply(Position position);
    }

    /**
     * Provides the four scenarios described in the JSON test specification:
     * <ul>
     *   <li>new Position → true</li>
     *   <li>only entry set → false</li>
     *   <li>only exit set → false</li>
     *   <li>both entry and exit set → false</li>
     * </ul>
     */
    private static Stream<Arguments> isNewScenarios() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        return Stream.of(
                // 1️⃣ freshly created Position (both trades null)
                arguments("new Position -> true", (PositionSetup) pos -> pos, true),

                // 2️⃣ only entry trade is non‑null (created via public API)
                arguments("only entry set -> false", (PositionSetup) pos -> {
                    pos.operate(0);          // creates entry
                    return pos;
                }, false),

                // 3️⃣ only exit trade is non‑null (set via reflection)
                arguments("only exit set -> false", (PositionSetup) pos -> {
                    setPrivateField(pos, "exit", exit);
                    return pos;
                }, false),

                // 4️⃣ both entry and exit are non‑null (set via reflection)
                arguments("both entry and exit set -> false", (PositionSetup) pos -> {
                    setPrivateField(pos, "entry", entry);
                    setPrivateField(pos, "exit", exit);
                    return pos;
                }, false)
        );
    }

    /**
     * Sets a private field of {@link Position} via reflection.
     *
     * @param target    the Position instance whose field should be modified
     * @param fieldName name of the private field ("entry" or "exit")
     * @param value     the Trade instance to assign
     */
    private static void setPrivateField(Position target, String fieldName, Trade value) {
        try {
            Field field = Position.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(
                    String.format("Failed to set field '%s' via reflection", fieldName), e);
        }
    }

    @Test
    void whenCreatedWithDefaultConstructor_thenIsNewIsTrue() {
        // starts with BUY, entry & exit are null
        Position position = new Position();
        assertTrue(position.isNew(), "A freshly created Position should be new");
    }

    @Test
    void whenCreatedWithStartingType_thenIsNewIsTrue() {
        // entry & exit are still null
        Position position = new Position(TradeType.SELL);
        assertTrue(position.isNew(), "A Position created with a starting type should be new");
    }

    @Test
    void afterFirstOperateEntry_thenIsNewIsFalse() {
        Position position = new Position();
        // creates the entry trade
        position.operate(0);
        assertFalse(position.isNew(), "A Position with an entry trade is no longer new");
    }

    @Test
    void afterEntryAndExitOperate_thenIsNewIsFalse() {
        Position position = new Position();
        // entry
        position.operate(0);
        // exit
        position.operate(1);
        assertFalse(position.isNew(), "A Position with both entry and exit trades is not new");
    }

    @Test
    void whenConstructedWithEntryAndExitTrades_thenIsNewIsFalse() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        Position position = new Position(entry, exit, transactionModel, new ZeroCostModel());
        assertFalse(position.isNew(), "A Position built with entry and exit trades should not be new");
    }

    @Test
    void whenConstructedWithEntryAndExitButSameType_thenThrowsException() {
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        // Same type as entry – illegal for Position constructor
        Trade wrongExit = Trade.buyAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        assertThrows(IllegalArgumentException.class,
                () -> new Position(entry, wrongExit, transactionModel, new ZeroCostModel()),
                "Constructing a Position with two trades of the same type must fail");
    }

    /**
     * Parameterized test that iterates over the scenarios defined in {@link #isNewScenarios()}.
     *
     * @param description human‑readable description of the scenario (used only for readability)
     * @param setup       lambda that mutates the freshly created Position into the desired state
     * @param expected    the expected result of {@link Position#isNew()}
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("isNewScenarios")
    void isNewParameterized(String description, PositionSetup setup, boolean expected) {
        // start from a clean instance
        Position position = new Position();
        position = setup.apply(position);
        assertEquals(expected, position.isNew(),
                () -> String.format("Scenario \"%s\" expected isNew() to be %s", description, expected));
    }
}
```