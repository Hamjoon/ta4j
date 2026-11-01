
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class Position_isNew_Test {

    /**
     * A cost model is required when creating {@link Trade} instances via the static factories.
     */
    private static CostModel transactionModel;

    /**
     * Functional interface used to configure a {@link Position} instance
     * before the assertion is performed.
     */
    @FunctionalInterface
    private interface PositionSetup {

        Position apply(Position position);
    }

    @BeforeAll
    static void setupBeforeAll() {
        // Use a zero‑cost model – the actual cost does not matter for the isNew() logic.
        transactionModel = new ZeroCostModel();
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
        return Stream.of(// 1️⃣ freshly created Position (both trades null)
        arguments("new Position -> true", (PositionSetup) Position::new, true), // 2️⃣ only entry trade is non‑null (created via public API)
        arguments("only entry set -> false", (PositionSetup) pos -> {
            pos.operate(0);
            return pos;
        }, // creates entry
        false), // 3️⃣ only exit trade is non‑null (set via reflection)
        arguments("only exit set -> false", (PositionSetup) pos -> {
            setPrivateField(pos, "exit", exit);
            return pos;
        }, false), // 4️⃣ both entry and exit are non‑null (set via reflection)
        arguments("both entry and exit set -> false", (PositionSetup) pos -> {
            setPrivateField(pos, "entry", entry);
            setPrivateField(pos, "exit", exit);
            return pos;
        }, false));
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
            throw new RuntimeException(String.format("Failed to set field '%s' via reflection", fieldName), e);
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
        // new
        Position position = new Position();
        // creates the entry trade
        position.operate(0);
        assertFalse(position.isNew(), "A Position with an entry trade is no longer new");
    }

    @Test
    void afterEntryAndExitOperate_thenIsNewIsFalse() {
        // new
        Position position = new Position();
        // entry
        position.operate(0);
        // exit
        position.operate(1);
        assertFalse(position.isNew(), "A Position with both entry and exit trades is not new");
    }

    @Test
    void whenConstructedWithEntryAndExitTrades_thenIsNewIsFalse() {
        // Build a complementary pair of trades (BUY then SELL)
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
        assertThrows(IllegalArgumentException.class, () -> {
            new Position(entry, wrongExit, transactionModel, new ZeroCostModel());
        }, "Constructing a Position with two trades of the same type must fail");
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
        assertEquals(expected, position.isNew(), () -> String.format("Scenario \"%s\" expected isNew() to be %s", description, expected));
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_isNew_Test.java:16: error: ';' expected
import org.junit.jupiter.params.provider.MethodSource
                                                     ^
1 error

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method isNew()': '\n     * @return true if the position is new, false otherwise\n     ', 'return type': 'boolean', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.Trade org.ta4j.core.Position.entry;\n\t\torg.ta4j.core.Trade org.ta4j.core.Position.exit;\n\trelated functions:\n\t\tmethod `public Trade getEntry()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.isNew()`, api document: \n     * @return the entry {@link Trade trade} of the position\n     ;\n\t\tmethod `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.isNew()`;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


