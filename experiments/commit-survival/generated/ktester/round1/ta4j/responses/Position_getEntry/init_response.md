```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position#getEntry()}.
 *
 * <p>The tests cover the following scenarios:
 *
 * <ul>
 *   <li>When a {@code Position} is created with the default constructor (BUY), the
 *       entry trade is created after the first {@code operate} call.</li>
 *   <li>When a {@code Position} is created with a custom starting type (SELL), the
 *       entry trade reflects that type.</li>
 *   <li>When a {@code Position} is constructed with explicit entry and exit trades,
 *       {@code getEntry()} returns the supplied entry trade.</li>
 *   <li>Before any operation, {@code getEntry()} returns {@code null}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_getEntry_Test {

    private Position defaultPosition;          // starting type BUY
    private Position sellPosition;             // starting type SELL
    private Position positionWithTrades;       // constructed with explicit trades

    private Trade entryTrade;
    private Trade exitTrade;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Positions created with the default and custom starting types.
        defaultPosition = new Position();                 // BUY by default
        sellPosition = new Position(TradeType.SELL);      // explicit SELL

        // Create simple trades (price/amount are NaN because they are not needed for
        // the getEntry() contract). Use ZeroCostModel to avoid dealing with cost
        // calculations.
        CostModel zeroCost = new ZeroCostModel();
        entryTrade = Trade.buyAt(0, NaN, NaN, zeroCost);
        exitTrade = Trade.sellAt(1, NaN, NaN, zeroCost);

        // Position built directly from entry/exit trades.
        positionWithTrades = new Position(entryTrade, exitTrade, zeroCost, zeroCost);
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
    void whenPositionCreatedWithDefaultConstructor_entryIsCreatedAfterFirstOperate() {
        // Act
        defaultPosition.operate(0); // creates the entry trade

        // Assert
        Trade expectedEntry = Trade.buyAt(0, NaN, NaN);
        assertEquals(expectedEntry, defaultPosition.getEntry(),
                "Entry trade should be a BUY at index 0 with NaN price/amount");
    }

    @Test
    void whenPositionCreatedWithSellStartingType_entryIsSellAfterFirstOperate() {
        // Act
        sellPosition.operate(0); // creates the entry trade (SELL)

        // Assert
        Trade expectedEntry = Trade.sellAt(0, NaN, NaN);
        assertEquals(expectedEntry, sellPosition.getEntry(),
                "Entry trade should be a SELL at index 0 with NaN price/amount");
    }

    @Test
    void whenPositionConstructedWithExplicitTrades_getEntryReturnsSuppliedEntry() {
        // No operation needed – the entry is already set via the constructor.
        assertEquals(entryTrade, positionWithTrades.getEntry(),
                "getEntry() must return the entry trade supplied to the constructor");
    }

    @Test
    void whenPositionHasNotOperatedYet_getEntryReturnsNull() {
        // A freshly created Position (default constructor) has not been operated.
        Position fresh = new Position();
        assertNull(fresh.getEntry(),
                "Before any operate call, entry should be null");
    }

    @Test
    void entryTradeIsIndependentFromExitTrade() {
        // Verify that the entry returned is exactly the object we passed in,
        // not the exit trade.
        assertNotEquals(exitTrade, positionWithTrades.getEntry(),
                "Entry trade must not be the same as the exit trade");
    }

    @Test
    void entryTradePreservesCustomPriceAndAmount() {
        // Use a non‑NaN price/amount to ensure they are preserved.
        CostModel zeroCost = new ZeroCostModel();
        Num price = DoubleNum.valueOf(10.5);
        Num amount = DoubleNum.valueOf(2);
        Trade customEntry = Trade.buyAt(5, price, amount, zeroCost);
        Trade customExit = Trade.sellAt(6, price, amount, zeroCost);
        Position customPos = new Position(customEntry, customExit, zeroCost, zeroCost);

        Trade result = customPos.getEntry();
        assertEquals(customEntry, result, "The entry trade should be exactly the one supplied");
        assertEquals(price, result.getPricePerAsset(),
                "Price per asset of the entry trade must be preserved");
        assertEquals(amount, result.getAmount(),
                "Amount of the entry trade must be preserved");
    }
}
```