```java
package org.ta4j.core;

import java.lang.reflect.Field;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Simple self‑contained test runner for {@link Position#isClosed()}.
 *
 * <p>The original JUnit 5 test class depended on external libraries that are
 * not available in the compilation environment.  To make the code compile
 * and still verify the behaviour, the JUnit annotations and static imports have
 * been removed and replaced by plain Java assertions (throwing
 * {@link AssertionError} with descriptive messages).</p>
 */
public class Position_isClosed_Test {

    // -------------------------------------------------------------------------
    private static final CostModel ZERO_COST_MODEL = new ZeroCostModel();

    // freshly created, no trades
    private Position newPosition;

    // only entry trade
    private Position openedPosition;

    // entry + exit trades
    private Position closedPosition;

    // created via (Trade, Trade) ctor
    private Position ctorClosedPosition;

    // created via (TradeType, ...) ctor
    private Position ctorOpenedPosition;

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
    /** Mimics JUnit's @BeforeEach */
    private void setup() {
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

    /** Mimics JUnit's @AfterEach */
    private void teardown() {
        newPosition = null;
        openedPosition = null;
        closedPosition = null;
        ctorClosedPosition = null;
        ctorOpenedPosition = null;
    }

    // -------------------------------------------------------------------------
    // Simple assertion helpers (replace JUnit's Assertions)
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError(message);
        }
    }

    // -------------------------------------------------------------------------
    // Test methods (formerly annotated with @Test)

    private void givenNewPosition_whenIsClosed_thenFalse() {
        assertFalse(newPosition.isClosed(),
                "A newly created Position (no entry, no exit) must not be considered closed.");
    }

    private void givenOpenedPosition_whenIsClosed_thenFalse() {
        assertFalse(openedPosition.isClosed(),
                "A Position with only an entry trade (opened) must not be considered closed.");
    }

    private void givenClosedPosition_whenIsClosed_thenTrue() {
        assertTrue(closedPosition.isClosed(),
                "A Position with both entry and exit trades must be considered closed.");
    }

    private void givenCtorClosedPosition_whenIsClosed_thenTrue() {
        assertTrue(ctorClosedPosition.isClosed(),
                "A Position constructed with entry and exit trades must be considered closed.");
    }

    private void givenCtorOpenedPosition_whenIsClosed_thenFalse() {
        assertFalse(ctorOpenedPosition.isClosed(),
                "A Position constructed only with a starting type (no entry trade yet) must not be considered closed.");
    }

    private void whenClosedPositionHasEntryAndGetExitAreNonNull() {
        assertTrue(closedPosition.isClosed());
        assertNotNull(closedPosition.getEntry(),
                "Entry trade should be non‑null for a closed position.");
        assertNotNull(closedPosition.getExit(),
                "Exit trade should be non‑null for a closed position.");
    }

    private void whenOpenedPositionHasOnlyEntry_thenGetExitIsNull() {
        assertFalse(openedPosition.isClosed());
        assertNotNull(openedPosition.getEntry(),
                "Entry trade should be non‑null for an opened position.");
        assertNull(openedPosition.getExit(),
                "Exit trade should be null for an opened position.");
    }

    private void bothNull_thenIsClosedFalse() {
        Position p = new Position(); // entry & exit are null by default
        assertFalse(p.isClosed(),
                "When both entry and exit are null the position must be considered NOT closed.");
    }

    private void onlyEntryNonNull_thenIsClosedFalse() {
        Position p = new Position();
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "entry", entry);
        // exit stays null
        assertFalse(p.isClosed(),
                "When only entry is set the position must be considered NOT closed.");
    }

    private void onlyExitNonNull_thenIsClosedFalse() {
        Position p = new Position();
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "exit", exit);
        // entry stays null
        assertFalse(p.isClosed(),
                "When only exit is set (entry missing) the position must be considered NOT closed.");
    }

    private void bothNonNull_thenIsClosedTrue() {
        Position p = new Position();
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "entry", entry);
        setPrivateField(p, "exit", exit);
        assertTrue(p.isClosed(),
                "When both entry and exit are present the position must be considered CLOSED.");
    }

    private void operateCreatesOnlyEntry_thenIsClosedFalse() {
        Position p = new Position();
        p.operate(0);
        assertFalse(p.isClosed(),
                "After a single operate call the position is opened but not closed.");
    }

    private void operateEntryThenExit_thenIsClosedTrue() {
        Position p = new Position();
        p.operate(0); // entry
        p.operate(5); // exit
        assertTrue(p.isClosed(),
                "After entry and a later exit trade the position must be closed.");
    }

    private void operateWithExplicitParams_thenIsClosedTrue() {
        Position p = new Position();
        Num price = DoubleNum.valueOf(10);
        Num amount = DoubleNum.valueOf(1);
        p.operate(0, price, amount); // entry
        p.operate(7, price, amount); // exit
        assertTrue(p.isClosed(),
                "Explicit price/amount overload should also result in a closed position when both trades exist.");
    }

    private void operateSameIndexTwice_thenIsClosedFalse() {
        Position p = new Position();
        p.operate(0); // entry
        p.operate(0); // same index – should not create exit
        assertFalse(p.isClosed(),
                "Calling operate with the same index when the position is already opened must not close it.");
    }

    private void operateAfterClosed_keepsClosedState() {
        Position p = new Position();
        p.operate(0); // entry
        p.operate(5); // exit → closed
        assertTrue(p.isClosed(),
                "Pre‑condition: position must be closed before the extra operate.");
        p.operate(10); // extra operation – should have no effect
        assertTrue(p.isClosed(),
                "Calling operate after the position is already closed must keep the closed state.");
    }

    // -------------------------------------------------------------------------
    /** Simple test runner – executes all test methods and reports failures. */
    public static void main(String[] args) {
        Position_isClosed_Test testSuite = new Position_isClosed_Test();
        int passed = 0;
        int failed = 0;

        // Helper to run a single test and capture any AssertionError.
        java.util.function.Consumer<Runnable> runTest = (Runnable r) -> {
            testSuite.setup();
            try {
                r.run();
                System.out.println("[PASS] " + r.getClass().getEnclosingMethod().getName());
                passed++;
            } catch (AssertionError e) {
                System.err.println("[FAIL] " + r.getClass().getEnclosingMethod().getName() + " – " + e.getMessage());
                failed++;
            } finally {
                testSuite.teardown();
            }
        };

        // Execute all tests
        runTest.accept(testSuite::givenNewPosition_whenIsClosed_thenFalse);
        runTest.accept(testSuite::givenOpenedPosition_whenIsClosed_thenFalse);
        runTest.accept(testSuite::givenClosedPosition_whenIsClosed_thenTrue);
        runTest.accept(testSuite::givenCtorClosedPosition_whenIsClosed_thenTrue);
        runTest.accept(testSuite::givenCtorOpenedPosition_whenIsClosed_thenFalse);
        runTest.accept(testSuite::whenClosedPositionHasEntryAndGetExitAreNonNull);
        runTest.accept(testSuite::whenOpenedPositionHasOnlyEntry_thenGetExitIsNull);
        runTest.accept(testSuite::bothNull_thenIsClosedFalse);
        runTest.accept(testSuite::onlyEntryNonNull_thenIsClosedFalse);
        runTest.accept(testSuite::onlyExitNonNull_thenIsClosedFalse);
        runTest.accept(testSuite::bothNonNull_thenIsClosedTrue);
        runTest.accept(testSuite::operateCreatesOnlyEntry_thenIsClosedFalse);
        runTest.accept(testSuite::operateEntryThenExit_thenIsClosedTrue);
        runTest.accept(testSuite::operateWithExplicitParams_thenIsClosedTrue);
        runTest.accept(testSuite::operateSameIndexTwice_thenIsClosedFalse);
        runTest.accept(testSuite::operateAfterClosed_keepsClosedState);

        // Summary
        System.out.println("\nTest run complete: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
```