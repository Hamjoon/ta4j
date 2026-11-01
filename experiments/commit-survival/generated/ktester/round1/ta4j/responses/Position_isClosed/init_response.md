```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position#isClosed()}.
 *
 * <p>The tests cover the following scenarios:
 *
 * <ul>
 *   <li>a newly created {@code Position} (no entry, no exit) – should be {@code false}
 *   <li>a {@code Position} that has only an entry trade (opened) – should be {@code false}
 *   <li>a {@code Position} that has both entry and exit trades (closed) – should be {@code true}
 *   <li>a {@code Position} created via the constructor that receives entry and exit trades – should be {@code true}
 *   <li>a {@code Position} created via the constructor that receives only the starting type – should be {@code false}
 * </ul>
 *
 * <p>All tests use only the public API of {@link Position} and {@link Trade}; no private members are accessed.
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
        // No global resources needed for these tests.
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
        // Explicitly nullify to help GC and to ensure no test leaks state.
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
    // Tests
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

    // Additional sanity checks – ensure that the internal state matches expectations
    // without accessing private fields directly.

    @Test
    void whenClosedPositionHasEntryAndExit_thenGetEntryAndGetExitAreNonNull() {
        assertTrue(closedPosition.isClosed());
        assertNotNull(closedPosition.getEntry(), "Entry trade should be non‑null for a closed position.");
        assertNotNull(closedPosition.getExit(), "Exit trade should be non‑null for a closed position.");
    }

    @Test
    void whenOpenedPositionHasOnlyEntry_thenGetExitIsNull() {
        assertFalse(openedPosition.isClosed());
        assertNotNull(openedPosition.getEntry(), "Entry trade should be non‑null for an opened position.");
        assertNull(openedPosition.getExit(), "Exit trade should be null for an opened position.");
    }
}
```