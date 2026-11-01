package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

// import org.mockito.junit.MockitoJUnitRunner;
class Position_getGrossProfit_Test {

    private Position openedPosition;

    private Position closedPosition;

    private Trade entryBuy;

    private Trade exitSell;

    private Trade entrySell;

    private Trade exitBuy;

    private CostModel transactionModel;

    //     @BeforeEach
    void setupBeforeEach() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();
        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        openedPosition = new Position();
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        closedPosition = new Position(entryBuy, exitSell, transactionModel, new ZeroCostModel());
        /* -------------------------------------------------
         *  Closed short position (SELL → BUY) – used in a dedicated test.
         * ------------------------------------------------- */
        entrySell = Trade.sellAt(0, DoubleNum.valueOf(15), DoubleNum.valueOf(1), transactionModel);
        exitBuy = Trade.buyAt(4, DoubleNum.valueOf(13), DoubleNum.valueOf(1), transactionModel);
    }

    @Test
    void whenPositionIsOpened_getGrossProfitReturnsZero() {
        // The position created in setUp is still opened (no exit trade)
        assertTrue(openedPosition.isOpened());
        Num grossProfit = openedPosition.getGrossProfit();
        // Expected zero – the library uses a Num representing 0
        Num expectedZero = DoubleNum.valueOf(0);
        assertEquals(expectedZero, grossProfit);
    }

    @Test
    void whenPositionIsClosed_getGrossProfitDelegatesToOverload() {
        // Verify that the closed BUY → SELL position returns the correct gross profit
        assertTrue(closedPosition.isClosed());
        Num grossProfitViaMethod = closedPosition.getGrossProfit();
        Num grossProfitViaOverload = closedPosition.getGrossProfit(exitSell.getPricePerAsset());
        assertEquals(grossProfitViaOverload, grossProfitViaMethod);
        // Manual expected value: exit price – entry price = 12 – 10 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfitViaMethod);
    }

    @Test
    void whenShortPositionIsClosed_getGrossProfitIsEntryMinusExit() {
        // Build a short (SELL → BUY) closed position
        Position shortPosition = new Position(entrySell, exitBuy, transactionModel, new ZeroCostModel());
        assertTrue(shortPosition.isClosed());
        Num grossProfit = shortPosition.getGrossProfit();
        // Expected: entry price – exit price = 15 – 13 = 2
        Num expected = DoubleNum.valueOf(2);
        assertEquals(expected, grossProfit);
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
        Position closedWithNullExit = new Position(entryBuy, null, transactionModel, new ZeroCostModel()) {

            @Override
            public boolean isOpened() {
                // force the "closed" branch
                return false;
            }
        };
        try {
            closedWithNullExit.getGrossProfit();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected exception
        }
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
        // The position is naturally considered closed because it has a non‑null exit.
        assertTrue(closedPosition.isClosed());
        try {
            // Pass null as the final price – the overload should NPE.
            closedPosition.getGrossProfit((Num) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }

    @Before
    public void setup() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();
        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        openedPosition = new Position();
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        closedPosition = new Position(entryBuy, exitSell, transactionModel, new ZeroCostModel());
        /* -------------------------------------------------
         *  Closed short position (SELL → BUY) – used in a dedicated test.
         * ------------------------------------------------- */
        entrySell = Trade.sellAt(0, DoubleNum.valueOf(15), DoubleNum.valueOf(1), transactionModel);
        exitBuy = Trade.buyAt(4, DoubleNum.valueOf(13), DoubleNum.valueOf(1), transactionModel);
    }

    @Before
    public void setUp() {
        // Simple transaction cost model – costs are irrelevant for gross profit.
        transactionModel = new ZeroCostModel();
        /* -------------------------------------------------
         *  Opened (still active) position.
         *  The entry trade is a BUY at price 10, amount 1.
         * ------------------------------------------------- */
        openedPosition = new Position();
        // Use the overloaded operate to set a deterministic entry price.
        openedPosition.operate(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        /* -------------------------------------------------
         *  Closed long position (BUY → SELL).
         * ------------------------------------------------- */
        entryBuy = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        exitSell = Trade.sellAt(5, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);
        closedPosition = new Position(entryBuy, exitSell, transactionModel, new ZeroCostModel());
        /* -------------------------------------------------
         *  Closed short position (SELL → BUY) – used in a dedicated test.
         * ------------------------------------------------- */
        entrySell = Trade.sellAt(0, DoubleNum.valueOf(15), DoubleNum.valueOf(1), transactionModel);
        exitBuy = Trade.buyAt(4, DoubleNum.valueOf(13), DoubleNum.valueOf(1), transactionModel);
    }
}
