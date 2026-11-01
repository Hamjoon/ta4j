```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for {@link Position#toString()}.
 *
 * <p>These tests cover the following scenarios:
 *
 * <ul>
 *   <li>Both {@code entry} and {@code exit} are {@code null}.</li>
 *   <li>Only {@code entry} is non‑null.</li>
 *   <li>Only {@code exit} is non‑null.</li>
 *   <li>Both trades are present.</li>
 *   <li>Entry trade's {@code toString()} returns an empty string.</li>
 *   <li>Entry or exit {@code toString()} throws various runtime exceptions.</li>
 * </ul>
 *
 * <p>The real {@link Trade#toString()} implementation is not part of the contract,
 * therefore most assertions verify that the resulting string contains the expected
 * substrings rather than matching an exact format, except for the cases where the
 * exact output is deterministic (e.g., empty entry string or null values).</p>
 */
@ExtendWith(MockitoExtension.class)
class Position_toString_Test {

    private Position emptyPosition;          // no entry / exit
    private Position openedPosition;         // entry only
    private Position closedPosition;         // entry + exit

    private Trade entryTrade;
    private Trade exitTrade;

    private CostModel transactionModel;
    private CostModel holdingModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Zero cost models are sufficient for toString().
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();

        // Real trades used for the normal (non‑mock) scenarios.
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        exitTrade  = Trade.sellAt(5, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);

        // 1️⃣ Position with no trades.
        emptyPosition = new Position();

        // 2️⃣ Position with only an entry trade (opened).
        openedPosition = new Position();
        openedPosition.operate(entryTrade.getIndex()); // creates the entry trade internally

        // 3️⃣ Position with both entry and exit trades (closed).
        closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    /* --------------------------------------------------------------------- */
    /*                Existing test cases (kept unchanged)                  */
    /* --------------------------------------------------------------------- */

    @Test
    void whenNoTrades_thenToStringShowsNulls() {
        String result = emptyPosition.toString();
        assertEquals("Entry: null exit: null", result,
                "A freshly created Position should report both entry and exit as null");
    }

    @Test
    void whenOnlyEntry_thenToStringContainsEntryAndNullExit() {
        String result = openedPosition.toString();

        // The entry trade created by operate() is accessible via getEntry().
        String entryString = openedPosition.getEntry().toString();

        assertTrue(result.contains(entryString),
                "The string representation must contain the entry trade");
        assertTrue(result.endsWith("null"),
                "The exit part should be the literal string \"null\" when the position is not closed");
    }

    @Test
    void whenClosed_thenToStringContainsBothTrades() {
        String result = closedPosition.toString();

        String entryString = entryTrade.toString();
        String exitString  = exitTrade.toString();

        assertTrue(result.contains(entryString),
                "The string representation must contain the entry trade");
        assertTrue(result.contains(exitString),
                "The string representation must contain the exit trade");
    }

    /* --------------------------------------------------------------------- */
    /*                Additional scenarios derived from JSON                 */
    /* --------------------------------------------------------------------- */

    @Test
    void whenOnlyExit_thenToStringShowsNullEntry() throws Exception {
        // Create a Position with a non‑null exit and a null entry.
        Position position = new Position();

        // Mock exit trade with a predictable toString().
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenReturn("Trade[id=X]");

        // Inject the mock into the private 'exit' field via reflection.
        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(position, exitMock);

        String result = position.toString();

        assertEquals("Entry: null exit: Trade[id=X]", result,
                "When only exit is set, entry must be shown as null and exit's toString() used");
    }

    @Test
    void whenEntryToStringReturnsEmpty_thenResultHasTwoSpaces() throws Exception {
        Position position = new Position();

        // Mock entry returning an empty string.
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenReturn("");

        // Mock exit with a normal representation.
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenReturn("Trade[id=X3]");

        // Inject both mocks.
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);

        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(position, exitMock);

        String result = position.toString();

        assertEquals("Entry:  exit: Trade[id=X3]", result,
                "Empty entry string should lead to two spaces between ':' and 'exit:'");
    }

    @Test
    void whenEntryToStringThrowsIllegalStateException_thenPropagated() throws Exception {
        Position position = new Position();

        // Mock entry that throws IllegalStateException on toString().
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenThrow(new IllegalStateException("entry broken"));

        // Inject mock entry.
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);

        // No exit (null) – we only care about entry's exception.
        assertThrows(IllegalStateException.class, position::toString,
                "IllegalStateException from entry.toString() must be propagated");
    }

    @Test
    void whenEntryToStringThrowsNullPointerException_thenPropagated() throws Exception {
        Position position = new Position();

        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenThrow(new NullPointerException("null entry"));

        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);

        assertThrows(NullPointerException.class, position::toString,
                "NullPointerException from entry.toString() must be propagated");
    }

    @Test
    void whenExitToStringThrowsRuntimeException_thenPropagated() throws Exception {
        Position position = new Position();

        // Normal entry.
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenReturn("Trade[id=E]");

        // Exit that throws RuntimeException.
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenThrow(new RuntimeException("exit broken"));

        // Inject both.
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);

        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(position, exitMock);

        assertThrows(RuntimeException.class, position::toString,
                "RuntimeException from exit.toString() must be propagated");
    }

    @Test
    void whenBothEntryAndExitThrow_thenEntryExceptionPropagated() throws Exception {
        Position position = new Position();

        // Entry throws IllegalStateException.
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenThrow(new IllegalStateException("entry broken"));

        // Exit throws a different exception.
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenThrow(new RuntimeException("exit broken"));

        // Inject both.
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);

        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(position, exitMock);

        // The first exception encountered (entry) should be the one that propagates.
        IllegalStateException thrown = assertThrows(IllegalStateException.class, position::toString,
                "The entry exception should be propagated before the exit one");
        assertEquals("entry broken", thrown.getMessage());
    }

    /* --------------------------------------------------------------------- */
    /*                Parameterized version for the first four cases        */
    /* --------------------------------------------------------------------- */

    private static Stream<Arguments> simpleScenariosProvider() {
        return Stream.of(
                // Both null – already covered but kept for completeness.
                Arguments.of(null, null, "Entry: null exit: null"),
                // Only entry.
                Arguments.of("Trade[id=E1]", null, "Entry: Trade[id=E1] exit: null"),
                // Only exit.
                Arguments.of(null, "Trade[id=X1]", "Entry: null exit: Trade[id=X1]"),
                // Both non‑null.
                Arguments.of("Trade[id=E2]", "Trade[id=X2]", "Entry: Trade[id=E2] exit: Trade[id=X2]")
        );
    }

    @ParameterizedTest(name = "{index} => entry={0}, exit={1}")
    @MethodSource("simpleScenariosProvider")
    void whenVariousCombinations_thenToStringMatchesExpected(String entryStr, String exitStr, String expected) throws Exception {
        Position position = new Position();

        // Helper to create a mock Trade that returns the supplied string via toString().
        Trade mockTrade(String representation) {
            if (representation == null) {
                return null;
            }
            Trade mock = mock(Trade.class);
            when(mock.toString()).thenReturn(representation);
            return mock;
        }

        Trade entryMock = mockTrade(entryStr);
        Trade exitMock = mockTrade(exitStr);

        // Inject via reflection.
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);

        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(position, exitMock);

        assertEquals(expected, position.toString(),
                "toString() should produce the expected concatenation for the given entry/exit values");
    }
}
```