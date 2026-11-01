```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Position#getGrossProfit()}.
 *
 * <p>The method has two distinct behaviours:
 * <ul>
 *   <li>If the position is still opened (no exit trade), it must return zero.</li>
 *   <li>If the position is closed, it must delegate to {@link Position#getGrossProfit(Num)}
 *       using the exit trade price.</li>
 * </ul>
 *
 * <p>Additional edge‑case tests are added based on the JSON test specification:
 * <ul>
 *   <li>Opened position returns zero.</li>
 *   <li>Closed position with a {@code null} internal {@code exit} trade throws {@link NullPointerException}.</li>
 *   <li>Closed position whose exit trade returns {@code null} for {@code getPricePerAsset()}
 *       throws {@link NullPointerException}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossProfit_Test {

    private Position openedPosition;
    private Position closedPosition;
    private Trade entryBuy;
    private Trade exitSell;
    private Trade entrySell;
    private Trade exitBuy;
    private CostModel transactionModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();

        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        openedPosition = new Position();                 // default BUY‑starting position
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));

        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0,
                DoubleNum.valueOf(10),                     // entry price
                DoubleNum.valueOf(1),                      // amount
                transactionModel);
        exitSell = Trade.sellAt(5,
                DoubleNum.valueOf(12),                     // exit price
                DoubleNum.valueOf(1),
                transactionModel);
        closedPosition = new Position(entryBuy, exitSell,
                transactionModel, new ZeroCostModel());

        /* -------------------------------------------------
         *  Closed short position (SELL → BUY) – used in a dedicated test.
         * ------------------------------------------------- */
        entrySell = Trade.sellAt(0,
                DoubleNum.valueOf(15),
                DoubleNum.valueOf(1),
                transactionModel);
        exitBuy = Trade.buyAt(4,
                DoubleNum.valueOf(13),
                DoubleNum.valueOf(1),
                transactionModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release.
    }

    /* ----------------------------------------------------------------------
     *  1️⃣  Opened position must return zero.
     * ---------------------------------------------------------------------- */
    @Test
    void whenPositionIsOpened_getGrossProfitReturnsZero() {
        assertTrue(openedPosition.isOpened(),
                "Position should be opened (no exit trade).");

        Num grossProfit = openedPosition.getGrossProfit();

        Num expectedZero = DoubleNum.valueOf(0);
        assertEquals(expectedZero, grossProfit,
                "Gross profit of an opened position must be zero.");
    }

    /* ----------------------------------------------------------------------
     *  2️⃣  Closed long position delegates correctly and yields the expected profit.
     * ---------------------------------------------------------------------- */
    @Test
    void whenPositionIsClosed_getGrossProfitDelegatesToOverload() {
        assertTrue(closedPosition.isClosed(),
                "Position should be closed.");

        Num grossProfitViaMethod = closedPosition.getGrossProfit();
        Num grossProfitViaOverload = closedPosition.getGrossProfit(exitSell.getPricePerAsset());

        assertEquals(grossProfitViaOverload, grossProfitViaMethod,
                "getGrossProfit() must delegate to the overload using exit price.");

        // Expected profit: exit – entry = 12 – 10 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfitViaMethod,
                "Gross profit for BUY→SELL should equal exit‑price minus entry‑price.");
    }

    /* ----------------------------------------------------------------------
     *  3️⃣  Closed short position (SELL → BUY) profit calculation.
     * ---------------------------------------------------------------------- */
    @Test
    void whenShortPositionIsClosed_getGrossProfitIsEntryMinusExit() {
        Position shortPosition = new Position(entrySell, exitBuy,
                transactionModel, new ZeroCostModel());

        assertTrue(shortPosition.isClosed(),
                "Short position should be closed.");

        Num grossProfit = shortPosition.getGrossProfit();

        // Expected profit: entry – exit = 15 – 13 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfit,
                "Gross profit for SELL→BUY should equal entry‑price minus exit‑price.");
    }

    /* ----------------------------------------------------------------------
     *  4️⃣  Edge case: closed position with a null internal exit trade.
     *
     *  We create a Position where the exit reference is null but force
     *  {@code isOpened()} to return {@code false} via a Mockito spy.
     *  The method should attempt to dereference {@code exit} and thus throw
     *  a {@link NullPointerException}.
     * ---------------------------------------------------------------------- */
    @Test
    void whenClosedPositionHasNullExit_throwsNullPointerException() throws Exception {
        // Construct a Position with a valid entry and a null exit.
        Position position = new Position(entryBuy, null,
                transactionModel, new ZeroCostModel());

        // Spy the instance and stub isOpened() to return false (simulating a closed state).
        Position spy = Mockito.spy(position);
        doReturn(false).when(spy).isOpened(); // force the "closed" branch

        // Verify that invoking getGrossProfit() triggers a NPE because exit is null.
        assertThrows(NullPointerException.class, spy::getGrossProfit,
                "Calling getGrossProfit() on a closed position with a null exit should throw NPE.");
    }

    /* ----------------------------------------------------------------------
     *  5️⃣  Edge case: closed position whose exit trade returns null for price.
     *
     *  We mock the exit Trade so that {@code getPricePerAsset()} returns null.
     *  The overloaded {@code getGrossProfit(Num)} will receive a null argument,
     *  which is expected to cause a {@link NullPointerException}.
     * ---------------------------------------------------------------------- */
    @Test
    void whenClosedPositionExitPriceIsNull_throwsNullPointerException() {
        // Mock an exit trade that returns null for price per asset.
        Trade mockedExit = mock(Trade.class);
        when(mockedExit.getPricePerAsset()).thenReturn(null);
        // Ensure the mock reports the correct type (SELL) to keep the Position consistent.
        when(mockedExit.getType()).thenReturn(TradeType.SELL);

        // Build the Position with a valid entry and the mocked exit.
        Position position = new Position(entryBuy, mockedExit,
                transactionModel, new ZeroCostModel());

        // The position is naturally considered closed because exit != null.
        assertTrue(position.isClosed(),
                "Position should be closed when exit trade is non‑null.");

        // Expect a NPE when the null price propagates to the overload.
        assertThrows(NullPointerException.class, position::getGrossProfit,
                "Calling getGrossProfit() when exit price is null should throw NPE.");
    }
}
```