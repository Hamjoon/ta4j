```java
package org.ta4j.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position#getGrossProfit(Num)}.
 *
 * <p>The tests cover the four logical branches of the method:
 *
 * <ul>
 *   <li>opened long position</li>
 *   <li>closed long position</li>
 *   <li>opened short position (sign must be negated)</li>
 *   <li>closed short position (sign must be negated)</li>
 * </ul>
 *
 * <p>All objects are created using the public API – no reflection or access to
 * private members is performed.
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossProfit_2_Test {

    /** Cost models used for the trades – zero cost keeps the calculations simple. */
    private static CostModel transactionModel;
    private static CostModel holdingModel;

    /** Trades used in the tests. */
    private Trade longEntry;
    private Trade longExit;
    private Trade shortEntry;
    private Trade shortExit;

    /** Positions built from the trades above. */
    private Position openedLong;
    private Position closedLong;
    private Position openedShort;
    private Position closedShort;

    @BeforeAll
    static void setupBeforeAll() {
        // Zero cost models are sufficient for gross‑profit calculations.
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();
    }

    @BeforeEach
    void setupBeforeEach() {
        /* --------------------------------------------------------------------
         * 1. LONG (BUY) trades
         * -------------------------------------------------------------------- */
        // entry: BUY 1 unit @ 10
        longEntry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1), transactionModel);
        // exit: SELL 1 unit @ 12
        longExit = Trade.sellAt(1, DoubleNum.valueOf(12), DoubleNum.valueOf(1), transactionModel);

        /* --------------------------------------------------------------------
         * 2. SHORT (SELL) trades
         * -------------------------------------------------------------------- */
        // entry: SELL 1 unit @ 15
        shortEntry = Trade.sellAt(0, DoubleNum.valueOf(15), DoubleNum.valueOf(1), transactionModel);
        // exit: BUY 1 unit @ 13
        shortExit = Trade.buyAt(1, DoubleNum.valueOf(13), DoubleNum.valueOf(1), transactionModel);

        /* --------------------------------------------------------------------
         * 3. Positions
         * -------------------------------------------------------------------- */
        // opened long – only entry trade is present
        openedLong = new Position();
        openedLong.operate(longEntry.getIndex(), longEntry.getPricePerAsset(), longEntry.getAmount(),
                transactionModel); // mimic the same constructor logic as operate()

        // closed long – entry + exit
        closedLong = new Position(longEntry, longExit, transactionModel, holdingModel);

        // opened short – entry only, starting type SELL
        openedShort = new Position(Trade.TradeType.SELL, transactionModel, holdingModel);
        openedShort.operate(shortEntry.getIndex(), shortEntry.getPricePerAsset(),
                shortEntry.getAmount(), transactionModel);

        // closed short – entry + exit
        closedShort = new Position(shortEntry, shortExit, transactionModel, holdingModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to release.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No static resources to release.
    }

    /* ------------------------------------------------------------------------
     *  Test cases
     * ------------------------------------------------------------------------ */

    @Test
    void openedLongGrossProfit() {
        // Expected: amount * finalPrice – entry value
        Num finalPrice = DoubleNum.valueOf(11);
        Num expected = longEntry.getAmount().multipliedBy(finalPrice).minus(longEntry.getValue());

        Num actual = openedLong.getGrossProfit(finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void closedLongGrossProfit() {
        // Expected: exit value – entry value (both BUY/SELL pair)
        Num expected = longExit.getValue().minus(longEntry.getValue());

        Num actual = closedLong.getGrossProfit(DoubleNum.valueOf(0)); // finalPrice ignored for closed
        assertNumEquals(expected, actual);
    }

    @Test
    void openedShortGrossProfitIsNegated() {
        // For a short (SELL) entry the raw profit is calculated the same way,
        // then negated by the method.
        Num finalPrice = DoubleNum.valueOf(14);
        Num raw = shortEntry.getAmount().multipliedBy(finalPrice).minus(shortEntry.getValue());
        Num expected = raw.negate();

        Num actual = openedShort.getGrossProfit(finalPrice);
        assertNumEquals(expected, actual);
    }

    @Test
    void closedShortGrossProfitIsNegated() {
        // For a closed short position the method returns (exit‑value – entry‑value) and
        // then negates the result because the entry trade is a SELL.
        Num raw = shortExit.getValue().minus(shortEntry.getValue());
        Num expected = raw.negate();

        Num actual = closedShort.getGrossProfit(DoubleNum.valueOf(0)); // finalPrice ignored for closed
        assertNumEquals(expected, actual);
    }
}
```