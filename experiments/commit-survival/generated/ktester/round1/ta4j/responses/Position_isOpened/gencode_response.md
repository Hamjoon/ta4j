```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.DoubleNum;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Position#isOpened()}.
 *
 * <p>These tests verify the logical contract of {@code isOpened()} under
 * different internal states of a {@link Position}. The test suite is divided
 * into two groups:</p>
 *
 * <ul>
 *   <li>Direct field manipulation (using reflection) to emulate all possible
 *       combinations of {@code entry} and {@code exit} being {@code null} or
 *       non‑null.</li>
 *   <li>State‑transition tests that exercise the public {@code operate(...)}
 *       method and also a manual clearing of the {@code entry} field.</li>
 * </ul>
 *
 * <p>Mockito is used to create lightweight {@link Trade} mocks when needed.
 * No private methods or fields are accessed directly except via reflection,
 * which is required because {@code entry} and {@code exit} are private and have
 * no setters.</p>
 */
@ExtendWith(MockitoExtension.class)
class Position_isOpened_Test {

    private Position defaultPosition;          // BUY starting type
    private Position sellStartingPosition;    // SELL starting type
    private Position closedPosition;          // entry & exit set via constructor

    private Trade entryBuy;
    private Trade exitSell;
    private Trade entrySell;
    private Trade exitBuy;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Positions created with the default constructors.
        defaultPosition = new Position();                 // starts with BUY
        sellStartingPosition = new Position(TradeType.SELL);

        // Trades used to build a closed position via the constructor.
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        exitSell = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1));

        entrySell = Trade.sellAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        exitBuy = Trade.buyAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1));

        // Closed position (entry + exit already defined).
        closedPosition = new Position(entryBuy, exitSell);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup needed.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    /* -------------------------------------------------------------
     *  Existing straightforward behavioural tests (kept unchanged)
     * ------------------------------------------------------------- */

    @Test
    void newPositionShouldNotBeOpened() {
        assertFalse(defaultPosition.isOpened(),
                "A newly created Position must not be opened (no entry trade).");
    }

    @Test
    void newSellStartingPositionShouldNotBeOpened() {
        assertFalse(sellStartingPosition.isOpened(),
                "A newly created SELL‑starting Position must not be opened.");
    }

    @Test
    void positionShouldBeOpenedAfterEntryTrade() {
        defaultPosition.operate(0); // creates a BUY entry
        assertTrue(defaultPosition.isOpened(),
                "After the first operate call the position should be opened.");
    }

    @Test
    void sellStartingPositionShouldBeOpenedAfterEntryTrade() {
        sellStartingPosition.operate(0); // creates a SELL entry
        assertTrue(sellStartingPosition.isOpened(),
                "After the first operate call on a SELL‑starting Position the position should be opened.");
    }

    @Test
    void positionShouldBeClosedAfterExitTrade() {
        defaultPosition.operate(0); // entry
        defaultPosition.operate(1); // exit
        assertFalse(defaultPosition.isOpened(),
                "After a second operate call (exit trade) the position must be closed.");
    }

    @Test
    void sellStartingPositionShouldBeClosedAfterExitTrade() {
        sellStartingPosition.operate(0); // entry (SELL)
        sellStartingPosition.operate(1); // exit (BUY)
        assertFalse(sellStartingPosition.isOpened(),
                "After the exit trade the SELL‑starting position must be closed.");
    }

    @Test
    void positionCreatedWithEntryAndExitTradesIsNotOpened() {
        assertFalse(closedPosition.isOpened(),
                "A Position constructed with both entry and exit trades should not be opened.");
    }

    @Test
    void positionCreatedWithComplementaryTradesIsClosed() {
        Position pos = new Position(entrySell, exitBuy);
        assertFalse(pos.isOpened(),
                "A Position built from complementary SELL/BUY trades must be closed.");
    }

    /* -------------------------------------------------------------
     *  Parameterised tests – direct field manipulation
     * ------------------------------------------------------------- */

    /**
     * Provides all combinations of {@code entry} / {@code exit} field values
     * together with the expected result of {@link Position#isOpened()}.
     *
     * <p>Each {@link Arguments} contains:
     * <ul>
     *   <li>boolean entryPresent – whether the {@code entry} field should hold a mock.</li>
     *   <li>boolean exitPresent – whether the {@code exit} field should hold a mock.</li>
     *   <li>boolean expected – the expected return value of {@code isOpened()}.</li>
     *   <li>String description – a human‑readable explanation used in the assertion message.</li>
     * </ul>
     */
    private static Stream<Arguments> fieldStateProvider() {
        return Stream.of(
                Arguments.of(false, false, false,
                        "Both entry and exit are null → position is not opened."),
                Arguments.of(true, false, true,
                        "Entry trade is set, exit is still null → position is opened."),
                Arguments.of(false, true, false,
                        "Entry is null but exit is non‑null (illegal state) → position is not opened."),
                Arguments.of(true, true, false,
                        "Both entry and exit are non‑null (position already closed) → position is not opened.")
        );
    }

    @ParameterizedTest(name = "[{index}] {3}")
    @MethodSource("fieldStateProvider")
    void isOpenedReflectively(boolean entryPresent, boolean exitPresent, boolean expected, String description) throws Exception {
        // Create a fresh Position instance (any constructor works – we will overwrite fields).
        Position position = new Position();

        // Use reflection to set the private fields.
        if (entryPresent) {
            Trade entryMock = mock(Trade.class);
            setPrivateField(position, "entry", entryMock);
        } else {
            setPrivateField(position, "entry", null);
        }

        if (exitPresent) {
            Trade exitMock = mock(Trade.class);
            setPrivateField(position, "exit", exitMock);
        } else {
            setPrivateField(position, "exit", null);
        }

        assertEquals(expected, position.isOpened(), description);
    }

    /**
     * Helper that sets a private field on the given object via reflection.
     *
     * @param target    the object whose field must be modified
     * @param fieldName the name of the private field
     * @param value     the value to inject (may be {@code null})
     */
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /* -------------------------------------------------------------
     *  Parameterised tests – state transitions using public API
     * ------------------------------------------------------------- */

    /**
     * Provides the three state‑transition scenarios described in the JSON
     * test specification.
     *
     * <ul>
     *   <li>Opened after first {@code operate}</li>
     *   <li>Closed after second {@code operate}</li>
     *   <li>Closed after manually clearing the entry field</li>
     * </ul>
     */
    private static Stream<Arguments> stateTransitionProvider() {
        return Stream.of(
                // Scenario 1: newly created → operate once → opened
                Arguments.of(1, false, "A newly created Position is opened by calling operate(index) which creates the entry trade."),
                // Scenario 2: operate twice → closed
                Arguments.of(2, false, "A Position that has been opened and then closed by a second operate(index) must report isOpened() as false."),
                // Scenario 3: operate once, then clear entry via reflection → not opened
                Arguments.of(1, true, "A Position that was opened, then its entry trade is manually cleared while exit remains null must not be opened.")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("stateTransitionProvider")
    void isOpenedStateTransitions(int operateCalls, boolean clearEntryViaReflection, String description) throws Exception {
        Position position = new Position(); // default BUY start

        // Apply the requested number of operate calls (each call creates the next trade).
        for (int i = 0; i < operateCalls; i++) {
            position.operate(i);
        }

        // Optional manual clearing of the entry field to simulate an illegal state.
        if (clearEntryViaReflection) {
            setPrivateField(position, "entry", null);
        }

        // Expected outcome follows the logical rules of isOpened().
        boolean expected = (position.getEntry() != null) && (position.getExit() == null);
        assertEquals(expected, position.isOpened(), description);
    }
}
```