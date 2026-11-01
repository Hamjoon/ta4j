```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Position#isClosed()}.
 *
 * <p>The test suite covers:
 *
 * <ul>
 *   <li>Direct state manipulation (using reflection) to verify the four possible
 *       combinations of {@code entry} / {@code exit} being {@code null} or non‑null.
 *   <li>Behaviour after calling {@link Position#operate(int)} and its overloads,
 *       including edge cases such as invoking {@code operate} on the same index
 *       and invoking it after a position is already closed.
 *   <li>Sanity checks using the public API ({@link Position#getEntry()},
 *       {@link Position#getExit()}) to avoid accessing private fields directly.
 * </ul>
 *
 * <p>All tests use only the public API of {@link Position} and {@link Trade},
 * except for the intentional reflective manipulation needed to create otherwise
 * unreachable internal states.
 */
@ExtendWith(MockitoExtension.class)
class Position_isClosed_Test {

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------
    private static final CostModel ZERO_COST_MODEL = new ZeroCostModel();

    private Position newPosition;          // freshly created, no trades
    private Position openedPosition;       // only entry trade
    private Position closedPosition;       // entry + exit trades
    private Position ctorClosedPosition;   // created via (Trade, Trade) ctor
    private Position ctorOpenedPosition;   // created via (TradeType, ...) ctor

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------
    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed.
    }

    @BeforeEach
    void setupBeforeEach() {
        // 1. New position – nothing has been operated yet.
        newPosition = new Position();

        // 2. Opened position – only entry trade (default BUY).
        openedPosition = new Position();
        openedPosition.operate(0); // creates entry trade at index 0

        // 3. Closed position – entry then exit.
        closedPosition = new Position();
        closedPosition.operate(0); // entry
        closedPosition.operate(1); // exit

        // 4. Position created directly with entry & exit trades.
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        ctorClosedPosition = new Position(entry, exit, ZERO_COST_MODEL, ZERO_COST_MODEL);

        // 5. Position created with only a starting type (SELL) – still no entry.
        ctorOpenedPosition = new Position(TradeType.SELL, ZERO_COST_MODEL, ZERO_COST_MODEL);
    }

    @AfterEach
    void teardownAfterEach() {
        newPosition = null;
        openedPosition = null;
        closedPosition = null;
        ctorClosedPosition = null;
        ctorOpenedPosition = null;
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up.
    }

    // -------------------------------------------------------------------------
    // Helper for reflective field injection
    // -------------------------------------------------------------------------
    private static void setPrivateField(Position position, String fieldName, Trade value) {
        try {
            Field field = Position.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(position, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set private field '" + fieldName + "'", e);
        }
    }

    // -------------------------------------------------------------------------
    // Branch‑coverage tests (direct state manipulation)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Both entry and exit are null → isClosed() must return false")
    void bothNull_thenIsClosedFalse() {
        Position p = new Position(); // entry & exit are null by default
        assertFalse(p.isClosed(),
                "When both entry and exit are null the position must be considered NOT closed.");
    }

    @Test
    @DisplayName("Only entry is non‑null → isClosed() must return false")
    void onlyEntryNonNull_thenIsClosedFalse() {
        Position p = new Position();
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "entry", entry);
        // exit stays null
        assertFalse(p.isClosed(),
                "When only entry is set the position must be considered NOT closed.");
    }

    @Test
    @DisplayName("Only exit is non‑null → isClosed() must return false")
    void onlyExitNonNull_thenIsClosedFalse() {
        Position p = new Position();
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "exit", exit);
        // entry stays null
        assertFalse(p.isClosed(),
                "When only exit is set (entry missing) the position must be considered NOT closed.");
    }

    @Test
    @DisplayName("Both entry and exit are non‑null → isClosed() must return true")
    void bothNonNull_thenIsClosedTrue() {
        Position p = new Position();
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "entry", entry);
        setPrivateField(p, "exit", exit);
        assertTrue(p.isClosed(),
                "When both entry and exit are present the position must be considered CLOSED.");
    }

    // -------------------------------------------------------------------------
    // Tests using the public API (operate)
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Fresh Position → operate(0) creates entry only → isClosed() false")
    void operateCreatesOnlyEntry_thenIsClosedFalse() {
        Position p = new Position();
        p.operate(0);
        assertFalse(p.isClosed(),
                "After a single operate call the position is opened but not closed.");
    }

    @Test
    @DisplayName("Two operates (different indices) → entry then exit → isClosed() true")
    void operateEntryThenExit_thenIsClosedTrue() {
        Position p = new Position();
        p.operate(0); // entry
        p.operate(5); // exit
        assertTrue(p.isClosed(),
                "After entry and a later exit trade the position must be closed.");
    }

    @Test
    @DisplayName("Operate with explicit price/amount → entry & exit both set → isClosed() true")
    void operateWithExplicitParams_thenIsClosedTrue() {
        Position p = new Position();
        Num price = DoubleNum.valueOf(10);
        Num amount = DoubleNum.valueOf(1);
        p.operate(0, price, amount); // entry
        p.operate(7, price, amount); // exit
        assertTrue(p.isClosed(),
                "Explicit price/amount overload should also result in a closed position when both trades exist.");
    }

    @Test
    @DisplayName("operate on same index twice → no exit created → isClosed() false")
    void operateSameIndexTwice_thenIsClosedFalse() {
        Position p = new Position();
        p.operate(0); // entry
        p.operate(0); // same index, should not create exit
        assertFalse(p.isClosed(),
                "Calling operate with the same index when the position is already opened must not close it.");
    }

    @Test
    @DisplayName("Additional operate after closed does not change state → isClosed() true")
    void operateAfterClosed_keepsClosedState() {
        Position p = new Position();
        p.operate(0); // entry
        p.operate(5); // exit → closed
        assertTrue(p.isClosed(), "Pre‑condition: position must be closed before the extra operate.");

        // Extra operation – should have no effect on closed state
        p.operate(10);
        assertTrue(p.isClosed(),
                "Calling operate after the position is already closed must keep the closed state.");
    }

    // -------------------------------------------------------------------------
    // Existing sanity checks (kept for completeness)
    // -------------------------------------------------------------------------
    @Test
    void givenNewPosition_whenIsClosed_thenFalse() {
        assertFalse(newPosition.isClosed(),
                "A newly created Position (no entry, no exit) must not be considered closed.");
    }

    @Test
    void givenOpenedPosition_whenIsClosed_thenFalse() {
        assertFalse(openedPosition.isClosed(),
                "A Position with only an entry trade (opened) must not be considered closed.");
    }

    @Test
    void givenClosedPosition_whenIsClosed_thenTrue() {
        assertTrue(closedPosition.isClosed(),
                "A Position with both entry and exit trades must be considered closed.");
    }

    @Test
    void givenCtorClosedPosition_whenIsClosed_thenTrue() {
        assertTrue(ctorClosedPosition.isClosed(),
                "A Position constructed with entry and exit trades must be considered closed.");
    }

    @Test
    void givenCtorOpenedPosition_whenIsClosed_thenFalse() {
        assertFalse(ctorOpenedPosition.isClosed(),
                "A Position constructed only with a starting type (no entry trade yet) must not be considered closed.");
    }

    @Test
    void whenClosedPositionHasEntryAndExit_thenGetEntryAndGetExitAreNonNull() {
        assertTrue(closedPosition.isClosed());
        assertNotNull(closedPosition.getEntry(),
                "Entry trade should be non‑null for a closed position.");
        assertNotNull(closedPosition.getExit(),
                "Exit trade should be non‑null for a closed position.");
    }

    @Test
    void whenOpenedPositionHasOnlyEntry_thenGetExitIsNull() {
        assertFalse(openedPosition.isClosed());
        assertNotNull(openedPosition.getEntry(),
                "Entry trade should be non‑null for an opened position.");
        assertNull(openedPosition.getExit(),
                "Exit trade should be null for an opened position.");
    }
}
```