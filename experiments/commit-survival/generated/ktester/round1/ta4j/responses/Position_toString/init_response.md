```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;

/**
 * Tests for {@link Position#toString()}.
 *
 * <p>The tests verify that the string representation correctly reflects the
 * internal {@code entry} and {@code exit} trades in three situations:
 *
 * <ul>
 *   <li>no trades have been performed yet;
 *   <li>only an entry trade exists (position opened);
 *   <li>both entry and exit trades exist (position closed).
 * </ul>
 *
 * The actual {@link Trade#toString()} implementation is not part of the contract
 * of {@code Position}, therefore the tests only assert that the resulting string
 * contains the expected substrings rather than matching the exact format.
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
        // Cost models – the simplest (zero) models are sufficient for toString().
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();

        // Trades used for opened / closed positions.
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
}
```