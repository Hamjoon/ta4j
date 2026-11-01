```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;

/**
 * Unit tests for {@link Position#isOpened()}.
 *
 * The tests cover the following scenarios:
 * <ul>
 *   <li>A newly created {@code Position} (no entry trade) is not opened.</li>
 *   <li>After the first {@code operate(...)} call (entry trade) the position is opened.</li>
 *   <li>After a second {@code operate(...)} call (exit trade) the position is closed again.</li>
 *   <li>The same behaviour for a position that starts with a {@code SELL} entry.</li>
 *   <li>A position created directly with entry and exit trades is closed.</li>
 * </ul>
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
}
```