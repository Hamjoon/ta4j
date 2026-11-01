
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import java.lang.reflect.Field;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class Position_isClosed_Test {

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

    @BeforeEach
    void setupBeforeEach() {
        // 1. New position – nothing has been operated yet.
        newPosition = new Position();
        // 2. Opened position – only entry trade (default BUY).
        openedPosition = new Position();
        // creates entry trade at index 0
        openedPosition.operate(0);
        // 3. Closed position – entry then exit.
        closedPosition = new Position();
        // entry
        closedPosition.operate(0);
        // exit
        closedPosition.operate(1);
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

    /**
     * Mimics JUnit's @BeforeEach
     */
    private void setup() {
        // 1. New position – nothing has been operated yet.
        newPosition = new Position();
        // 2. Opened position – only entry trade (default BUY).
        openedPosition = new Position();
        openedPosition.operate(0);
        // 3. Closed position – entry then exit.
        closedPosition = new Position();
        closedPosition.operate(0);
        closedPosition.operate(1);
        // 4. Position created directly with entry & exit trades.
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        ctorClosedPosition = new Position(entry, exit, ZERO_COST_MODEL, ZERO_COST_MODEL);
        // 5. Position created with only a starting type (SELL) – still no entry.
        ctorOpenedPosition = new Position(TradeType.SELL, ZERO_COST_MODEL, ZERO_COST_MODEL);
    }

    /**
     * Mimics JUnit's @AfterEach
     */
    private void teardown() {
        newPosition = null;
        openedPosition = null;
        closedPosition = null;
        ctorClosedPosition = null;
        ctorOpenedPosition = null;
    }

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

    private void whenClosedPositionHasEntryAndGetExitAreNonNull() {
        assertTrue(closedPosition.isClosed());
        assertNotNull(closedPosition.getEntry(), "Entry trade should be non‑null for a closed position.");
        assertNotNull(closedPosition.getExit(), "Exit trade should be non‑null for a closed position.");
    }

    /**
     * Simple test runner – executes all test methods and reports failures.
     */
    public static void main(String[] args) {
        Position_isClosed_Test testSuite = new Position_isClosed_Test();
        int passed = 0;
        int failed = 0;
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
        runTest.accept(testSuite::whenClosedPositionHasEntryAndExit_thenGetEntryAndGetExitAreNonNull);
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

    @Test
    void givenNewPosition_whenIsClosed_thenFalse() {
        assertFalse(newPosition.isClosed(), "A newly created Position (no entry, no exit) must not be considered closed.");
    }

    @Test
    void givenOpenedPosition_whenIsClosed_thenFalse() {
        assertFalse(openedPosition.isClosed(), "A Position with only an entry trade (opened) must not be considered closed.");
    }

    @Test
    void givenClosedPosition_whenIsClosed_thenTrue() {
        assertTrue(closedPosition.isClosed(), "A Position with both entry and exit trades must be considered closed.");
    }

    @Test
    void givenCtorClosedPosition_whenIsClosed_thenTrue() {
        assertTrue(ctorClosedPosition.isClosed(), "A Position constructed with entry and exit trades must be considered closed.");
    }

    @Test
    void givenCtorOpenedPosition_whenIsClosed_thenFalse() {
        assertFalse(ctorOpenedPosition.isClosed(), "A Position constructed only with a starting type (no entry trade yet) must not be considered closed.");
    }

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

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Both entry and exit are null → isClosed() must return false")
    void bothNull_thenIsClosedFalse() {
        Position p = new Position();
        assertFalse(p.isClosed(), "When both entry and exit are null the position must be considered NOT closed.");
    }

    @Test
    @DisplayName("Only entry is non‑null → isClosed() must return false")
    void onlyEntryNonNull_thenIsClosedFalse() {
        Position p = new Position();
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "entry", entry);
        // exit stays null
        assertFalse(p.isClosed(), "When only entry is set the position must be considered NOT closed.");
    }

    @Test
    @DisplayName("Only exit is non‑null → isClosed() must return false")
    void onlyExitNonNull_thenIsClosedFalse() {
        Position p = new Position();
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "exit", exit);
        // entry stays null
        assertFalse(p.isClosed(), "When only exit is set (entry missing) the position must be considered NOT closed.");
    }

    @Test
    @DisplayName("Both entry and exit are non‑null → isClosed() must return true")
    void bothNonNull_thenIsClosedTrue() {
        Position p = new Position();
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        Trade exit = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), ZERO_COST_MODEL);
        setPrivateField(p, "entry", entry);
        setPrivateField(p, "exit", exit);
        assertTrue(p.isClosed(), "When both entry and exit are present the position must be considered CLOSED.");
    }

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Fresh Position → operate(0) creates entry only → isClosed() false")
    void operateCreatesOnlyEntry_thenIsClosedFalse() {
        Position p = new Position();
        p.operate(0);
        assertFalse(p.isClosed(), "After a single operate call the position is opened but not closed.");
    }

    @Test
    @DisplayName("Two operates (different indices) → entry then exit → isClosed() true")
    void operateEntryThenExit_thenIsClosedTrue() {
        Position p = new Position();
        // entry
        p.operate(0);
        // exit
        p.operate(5);
        assertTrue(p.isClosed(), "After entry and a later exit trade the position must be closed.");
    }

    @Test
    @DisplayName("Operate with explicit price/amount → entry & exit both set → isClosed() true")
    void operateWithExplicitParams_thenIsClosedTrue() {
        Position p = new Position();
        Num price = DoubleNum.valueOf(10);
        Num amount = DoubleNum.valueOf(1);
        // entry
        p.operate(0, price, amount);
        // exit
        p.operate(7, price, amount);
        assertTrue(p.isClosed(), "Explicit price/amount overload should also result in a closed position when both trades exist.");
    }

    @Test
    @DisplayName("operate on same index twice → no exit created → isClosed() false")
    void operateSameIndexTwice_thenIsClosedFalse() {
        Position p = new Position();
        // entry
        p.operate(0);
        // same index – should not create exit
        p.operate(0);
        assertFalse(p.isClosed(), "Calling operate with the same index when the position is already opened must not close it.");
    }

    @Test
    @DisplayName("Additional operate after closed does not change state → isClosed() true")
    void operateAfterClosed_keepsClosedState() {
        Position p = new Position();
        // entry
        p.operate(0);
        // exit → closed
        p.operate(5);
        assertTrue(p.isClosed(), "Pre‑condition: position must be closed before the extra operate.");
        // extra operation – should have no effect
        p.operate(10);
        assertTrue(p.isClosed(), "Calling operate after the position is already closed must keep the closed state.");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_isClosed_Test.java:9: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_isClosed_Test.java:12: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_isClosed_Test.java:14: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/Position_isClosed_Test.java:11: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_isClosed_Test.java:14: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/Position_isClosed_Test.java:35: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:57: error: cannot find symbol
    @AfterEach
     ^
  symbol:   class AfterEach
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:226: error: cannot find symbol
    @DisplayName("Both entry and exit are null → isClosed() must return false")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:233: error: cannot find symbol
    @DisplayName("Only entry is non‑null → isClosed() must return false")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:243: error: cannot find symbol
    @DisplayName("Only exit is non‑null → isClosed() must return false")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:253: error: cannot find symbol
    @DisplayName("Both entry and exit are non‑null → isClosed() must return true")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:265: error: cannot find symbol
    @DisplayName("Fresh Position → operate(0) creates entry only → isClosed() false")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:273: error: cannot find symbol
    @DisplayName("Two operates (different indices) → entry then exit → isClosed() true")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:284: error: cannot find symbol
    @DisplayName("Operate with explicit price/amount → entry & exit both set → isClosed() true")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:297: error: cannot find symbol
    @DisplayName("operate on same index twice → no exit created → isClosed() false")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:308: error: cannot find symbol
    @DisplayName("Additional operate after closed does not change state → isClosed() true")
     ^
  symbol:   class DisplayName
  location: class Position_isClosed_Test
src/test/java/org/ta4j/core/Position_isClosed_Test.java:136: error: method assertTrue in class Position_isClosed_Test cannot be applied to given types;
        assertTrue(closedPosition.isClosed());
        ^
  required: boolean,String
  found:    boolean
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/Position_isClosed_Test.java:212: error: method assertTrue in class Position_isClosed_Test cannot be applied to given types;
        assertTrue(closedPosition.isClosed());
        ^
  required: boolean,String
  found:    boolean
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/Position_isClosed_Test.java:219: error: method assertFalse in class Position_isClosed_Test cannot be applied to given types;
        assertFalse(openedPosition.isClosed());
        ^
  required: boolean,String
  found:    boolean
  reason: actual and formal argument lists differ in length
19 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method isClosed()': '\n     * @return true if the position is closed, false otherwise\n     ', 'return type': 'boolean', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.Trade org.ta4j.core.Position.entry;\n\t\torg.ta4j.core.Trade org.ta4j.core.Position.exit;\n\trelated functions:\n\t\tmethod `public Trade getEntry()` returns `org.ta4j.core.Trade`, related with `org.ta4j.core.Position.isClosed()`, api document: \n     * @return the entry {@link Trade trade} of the position\n     ;\n\t\tmethod `public boolean equals(Object)` returns `boolean`, related with `org.ta4j.core.Position.isClosed()`;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


