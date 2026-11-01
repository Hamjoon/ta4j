```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.Trade.TradeType;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Tests for {@link Position#isNew()}.
 *
 * <p>The method simply checks whether both {@code entry} and {@code exit} trades are {@code null}.
 * The test suite verifies this contract for a freshly created {@link Position},
 * for positions where only one of the trades is set (using the public API or reflection),
 * and for positions where both trades are present.</p>
 */
@ExtendWith(MockitoExtension.class)
class Position_isNew_Test {

    /** A cost model is required when creating {@link Trade} instances via the static factories. */
    private static CostModel transactionModel;

    @BeforeAll
    static void setupBeforeAll() {
        // Use a zero‑cost model – the actual cost does not matter for the isNew() logic.
        transactionModel = new ZeroCostModel();
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test state needed; each test creates its own Position instances.
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up.
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing to clean up.
    }

    /* --------------------------------------------------------------------- *
     *  Existing explicit tests (kept unchanged)                               *
     * --------------------------------------------------------------------- */

    @Test
    void whenCreatedWithDefaultConstructor_thenIsNewIsTrue() {
        Position position = new Position(); // starts with BUY, entry & exit are null
        assertTrue(position.isNew(), "A freshly created Position should be new");
    }

    @Test
    void whenCreatedWithStartingType_thenIsNewIsTrue() {
        Position position = new Position(TradeType.SELL); // entry & exit are still null
        assertTrue(position.isNew(), "A Position created with a starting type should be new");
    }

    @Test
    void afterFirstOperateEntry_thenIsNewIsFalse() {
        Position position = new Position(); // new
        position.operate(0); // creates the entry trade
        assertFalse(position.isNew(), "A Position with an entry trade is no longer new");
    }

    @Test
    void afterEntryAndExitOperate_thenIsNewIsFalse() {
        Position position = new Position(); // new
        position.operate(0); // entry
        position.operate(1); // exit
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

    /* --------------------------------------------------------------------- *
     *  Parameterized tests generated from the JSON specification            *
     * --------------------------------------------------------------------- */

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
                arguments("new Position -> true",
                        (PositionSetup) Position::new,
                        true),

                // 2️⃣ only entry trade is non‑null (created via public API)
                arguments("only entry set -> false",
                        (PositionSetup) pos -> {
                            pos.operate(0); // creates entry
                            return pos;
                        },
                        false),

                // 3️⃣ only exit trade is non‑null (set via reflection)
                arguments("only exit set -> false",
                        (PositionSetup) pos -> {
                            setPrivateField(pos, "exit", exit);
                            return pos;
                        },
                        false),

                // 4️⃣ both entry and exit are non‑null (set via reflection)
                arguments("both entry and exit set -> false",
                        (PositionSetup) pos -> {
                            setPrivateField(pos, "entry", entry);
                            setPrivateField(pos, "exit", exit);
                            return pos;
                        },
                        false)
        );
    }

    /**
     * Functional interface used to configure a {@link Position} instance
     * before the assertion is performed.
     */
    @FunctionalInterface
    private interface PositionSetup {
        Position apply(Position position);
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
        Position position = new Position(); // start from a clean instance
        position = setup.apply(position);
        assertEquals(expected, position.isNew(),
                () -> String.format("Scenario \"%s\" expected isNew() to be %s", description, expected));
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods for reflection                                         *
     * --------------------------------------------------------------------- */

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
}
```