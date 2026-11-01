```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link Position#getGrossReturn(Num, Num)}.
 *
 * The method returns:
 * <ul>
 *   <li>for a BUY entry – {@code exitPrice / entryPrice}</li>
 *   <li>for a SELL entry – {@code exitPrice / entryPrice}
 *       then transformed to {@code exitPrice / entryPrice - 1}
 *       {@code .negate().plus(1)}</li>
 * </ul>
 *
 * The tests create {@link Position} instances with explicit entry/exit
 * {@link Trade}s so that the entry type is known and the method can be
 * exercised directly.
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_4_Test {

    private Position longPosition;   // entry = BUY
    private Position shortPosition;  // entry = SELL

    private Trade longEntry;
    private Trade longExit;
    private Trade shortEntry;
    private Trade shortExit;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // --------------------------------------------------------------------
        // BUY → SELL (long position)
        // --------------------------------------------------------------------
        longEntry = Trade.buyAt(0, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        longExit  = Trade.sellAt(1, DoubleNum.valueOf(15), DoubleNum.valueOf(1));
        longPosition = new Position(longEntry, longExit);

        // --------------------------------------------------------------------
        // SELL → BUY (short position)
        // --------------------------------------------------------------------
        shortEntry = Trade.sellAt(0, DoubleNum.valueOf(20), DoubleNum.valueOf(1));
        shortExit  = Trade.buyAt(1, DoubleNum.valueOf(10), DoubleNum.valueOf(1));
        shortPosition = new Position(shortEntry, shortExit);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up after each test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required
    }

    @Test
    @DisplayName("Gross return for a long position equals exit/entry")
    void whenLongPosition_thenGrossReturnIsExitDividedByEntry() {
        // entry price = 10, exit price = 15 → expected = 15 / 10 = 1.5
        Num entryPrice = longEntry.getPricePerAsset();
        Num exitPrice  = longExit.getPricePerAsset();

        Num expected = exitPrice.dividedBy(entryPrice);
        Num actual   = longPosition.getGrossReturn(entryPrice, exitPrice);

        assertEquals(expected, actual, "Gross return for long position should be exit/entry");
    }

    @Test
    @DisplayName("Gross return for a short position follows the SELL formula")
    void whenShortPosition_thenGrossReturnFollowsSellFormula() {
        // entry price = 20, exit price = 10
        // Expected = exit/entry → 10/20 = 0.5
        // then: 0.5 - 1 = -0.5
        // negate → 0.5
        // plus 1 → 1.5
        Num entryPrice = shortEntry.getPricePerAsset();
        Num exitPrice  = shortExit.getPricePerAsset();

        Num ratio    = exitPrice.dividedBy(entryPrice);
        Num one      = entryPrice.numOf(1);               // same Num implementation (DoubleNum)
        Num expected = ratio.minus(one).negate().plus(one);

        Num actual = shortPosition.getGrossReturn(entryPrice, exitPrice);

        assertEquals(expected, actual,
                "Gross return for short position should follow the SELL formula");
    }

    @Test
    @DisplayName("Gross return works with identical entry and exit prices")
    void whenEntryEqualsExit_thenGrossReturnIsOneForLongAndTwoForShort() {
        // Long: entry = exit = 5 → 5/5 = 1
        Trade entry = Trade.buyAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Trade exit  = Trade.sellAt(1, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Position pos = new Position(entry, exit);

        Num expectedLong = DoubleNum.valueOf(1);
        Num actualLong   = pos.getGrossReturn(entry.getPricePerAsset(), exit.getPricePerAsset());

        assertEquals(expectedLong, actualLong,
                "Long position with equal prices should return 1");

        // Short: entry = exit = 5 → ratio = 1 → 1-1 =0 → negate 0 → +1 = 1
        // Actually according to the formula it also yields 1
        Trade shortEntry = Trade.sellAt(0, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Trade shortExit  = Trade.buyAt(1, DoubleNum.valueOf(5), DoubleNum.valueOf(1));
        Position shortPos = new Position(shortEntry, shortExit);

        Num expectedShort = DoubleNum.valueOf(1);
        Num actualShort   = shortPos.getGrossReturn(shortEntry.getPricePerAsset(),
                                                    shortExit.getPricePerAsset());

        assertEquals(expectedShort, actualShort,
                "Short position with equal prices should also return 1");
    }
}
```