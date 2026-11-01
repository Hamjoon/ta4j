```java
package org.ta4j.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Position_toString_Test {

    private Position emptyPosition;
    private Position openedPosition;
    private Position closedPosition;
    private Trade entryTrade;
    private Trade exitTrade;
    private CostModel transactionModel;
    private CostModel holdingModel;

    @BeforeEach
    void setupBeforeEach() {
        // Cost models – the simplest (zero) models are sufficient for toString().
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();

        // Trades used for opened / closed positions.
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        exitTrade = Trade.sellAt(5, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);

        // 1️⃣ Position with no trades.
        emptyPosition = new Position();

        // 2️⃣ Position with only an entry trade (opened).
        openedPosition = new Position();
        // creates the entry trade internally
        openedPosition.operate(entryTrade.getIndex());

        // 3️⃣ Position with both entry and exit trades (closed).
        closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    private static Stream<Arguments> simpleScenariosProvider() {
        return Stream.of(
                Arguments.of(null, null, "Entry: null exit: null"),
                Arguments.of("Trade[id=E1]", null, "Entry: Trade[id=E1] exit: null"),
                Arguments.of(null, "Trade[id=X1]", "Entry: null exit: Trade[id=X1]"),
                Arguments.of("Trade[id=E2]", "Trade[id=X2]", "Entry: Trade[id=E2] exit: Trade[id=X2]")
        );
    }

    /**
     * Helper that returns a mocked {@link Trade} whose {@code toString()} returns the supplied value.
     * If the supplied value is {@code null}, the method returns {@code null} (meaning the field stays unset).
     */
    private static Trade mockTrade(String toStringValue) {
        if (toStringValue == null) {
            return null;
        }
        Trade trade = mock(Trade.class);
        when(trade.toString()).thenReturn(toStringValue);
        return trade;
    }

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
        assertTrue(result.contains(entryString), "The string representation must contain the entry trade");
        assertTrue(result.endsWith("null"), "The exit part should be the literal string \"null\" when the position is not closed");
    }

    @Test
    void whenClosed_thenToStringContainsBothTrades() {
        String result = closedPosition.toString();
        String entryString = entryTrade.toString();
        String exitString = exitTrade.toString();
        assertTrue(result.contains(entryString), "The string representation must contain the entry trade");
        assertTrue(result.contains(exitString), "The string representation must contain the exit trade");
    }

    @Test
    void whenOnlyExit_thenToStringShowsNullEntry() throws Exception {
        Position position = new Position();
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenReturn("Trade[id=X]");
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
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenReturn("");
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenReturn("Trade[id=X3]");
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
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenThrow(new IllegalStateException("entry broken"));
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);
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
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenReturn("Trade[id=E]");
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenThrow(new RuntimeException("exit broken"));
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
        Trade entryMock = mock(Trade.class);
        when(entryMock.toString()).thenThrow(new IllegalStateException("entry broken"));
        Trade exitMock = mock(Trade.class);
        when(exitMock.toString()).thenThrow(new RuntimeException("exit broken"));
        Field entryField = Position.class.getDeclaredField("entry");
        entryField.setAccessible(true);
        entryField.set(position, entryMock);
        Field exitField = Position.class.getDeclaredField("exit");
        exitField.setAccessible(true);
        exitField.set(position, exitMock);
        IllegalStateException thrown = assertThrows(IllegalStateException.class, position::toString,
                "The entry exception should be propagated before the exit one");
        assertEquals("entry broken", thrown.getMessage());
    }

    @ParameterizedTest(name = "{index} => entry={0}, exit={1}")
    @MethodSource("simpleScenariosProvider")
    void whenVariousCombinations_thenToStringMatchesExpected(String entryStr, String exitStr, String expected) throws Exception {
        Position position = new Position();
        // create mocks (or null) for entry and exit based on the supplied strings
        Trade entryMock = mockTrade(entryStr);
        Trade exitMock = mockTrade(exitStr);
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