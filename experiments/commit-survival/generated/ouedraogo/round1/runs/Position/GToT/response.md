###Test START##
```java
/**
 * JUnit‑4 test suite for {@link org.ta4j.core.Position}.
 *
 * The tests are organised in three logical steps that mimic the collaborative
 * workflow described in the task:
 *
 * 1.  **Public API extraction** – the list of public methods (including
 *     signatures) is provided as a comment at the top of the file.
 *
 * 2.  **Basic functionality tests** – one simple test per method that checks
 *     the “happy path”.
 *
 * 3.  **Edge‑case & exception tests** – additional tests that cover illegal
 *     arguments, state violations and special numeric situations.
 *
 * Only classes, constructors and static factories that appear in the supplied
 * source code or in the dependency signatures are used.  No mocking framework
 * is employed; concrete objects are created directly (e.g. {@link Trade},
 * {@link ZeroCostModel}, {@link NaN} for {@link Num} values).
 *
 * The test class can be compiled and executed with any JUnit‑4 runner.
 */
package org.ta4j.core;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * List of public methods of {@link Position} (extracted from the source):
 *
 * <pre>
 * Position()
 * Position(TradeType startingType)
 * Position(TradeType startingType, CostModel transactionCostModel, CostModel holdingCostModel)
 * Position(Trade entry, Trade exit)
 * Position(Trade entry, Trade exit, CostModel transactionCostModel, CostModel holdingCostModel)
 *
 * Trade getEntry()
 * Trade getExit()
 * boolean equals(Object obj)
 * int hashCode()
 *
 * Trade operate(int index)
 * Trade operate(int index, Num price, Num amount)
 *
 * boolean isClosed()
 * boolean isOpened()
 * boolean isNew()
 *
 * boolean hasProfit()
 * boolean hasLoss()
 *
 * Num getProfit()
 * Num getProfit(int finalIndex, Num finalPrice)
 *
 * Num getGrossProfit()
 * Num getGrossProfit(Num finalPrice)
 *
 * Num getGrossReturn()
 * Num getGrossReturn(Num finalPrice)
 * Num getGrossReturn(BarSeries barSeries)
 * Num getGrossReturn(Num entryPrice, Num exitPrice)
 *
 * Num getPositionCost(int finalIndex)
 * Num getPositionCost()
 * Num getHoldingCost()
 * Num getHoldingCost(int finalIndex)
 *
 * TradeType getStartingType()
 *
 * String toString()
 * </pre>
 */
public class PositionTest {

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                       */
    /* --------------------------------------------------------------------- */

    /** Shortcut to create a {@link Num} from a primitive value. */
    private static Num numOf(Number n) {
        return NaN.numOf(n);
    }

    /** Creates a BUY trade at the given index, price and amount. */
    private static Trade buy(int index, Number price, Number amount) {
        return Trade.buyAt(index, numOf(price), numOf(amount));
    }

    /** Creates a SELL trade at the given index, price and amount. */
    private static Trade sell(int index, Number price, Number amount) {
        return Trade.sellAt(index, numOf(price), numOf(amount));
    }

    /** A zero‑cost model – all cost calculations return 0. */
    private static ZeroCostModel zeroCostModel() {
        return new ZeroCostModel();
    }

    /* --------------------------------------------------------------------- */
    /* 1. Constructors – basic usage & illegal arguments                    */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorCreatesBuyStartingType() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStartingTypeThrows() {
        new Position(null);
    }

    @Test
    public void testConstructorWithCostModels() {
        Position p = new Position(TradeType.BUY, zeroCostModel(), zeroCostModel());
        assertEquals(TradeType.BUY, p.getStartingType());
    }

    @Test
    public void testConstructorFromEntryAndExitTrades() {
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        Position p = new Position(entry, exit);
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryExitSameTypeThrows() {
        Trade entry = buy(0, 10, 5);
        Trade exit = buy(1, 12, 5); // same type → illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModelThrows() {
        // entry/exit use the default ZeroCostModel, we pass a different model
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        CostModel other = new ZeroCostModel(); // still zero but a different instance
        new Position(entry, exit, other, zeroCostModel());
    }

    /* --------------------------------------------------------------------- */
    /* 2. Basic getters & state predicates                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGettersAndStateTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        // first operate creates entry
        Trade entry = p.operate(0, numOf(10), numOf(5));
        assertEquals(entry, p.getEntry());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // second operate creates exit
        Trade exit = p.operate(1, numOf(12), numOf(5));
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test
    public void testEqualsAndHashCode() {
        Trade e1 = buy(0, 10, 5);
        Trade x1 = sell(1, 12, 5);
        Position p1 = new Position(e1, x1);
        Position p2 = new Position(e1, x1);
        Position p3 = new Position(buy(0, 10, 5), sell(2, 13, 5));

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "some string");
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    /* --------------------------------------------------------------------- */
    /* 3. operate() edge cases                                               */
    /* --------------------------------------------------------------------- */

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexBeforeEntryThrows() {
        Position p = new Position();
        p.operate(5, numOf(10), numOf(5)); // creates entry at index 5
        p.operate(3, numOf(12), numOf(5)); // index < entry index → exception
    }

    @Test
    public void testOperateWithoutPriceAmountUsesNaN() {
        Position p = new Position();
        Trade entry = p.operate(0); // price & amount are NaN
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        // entry should have the starting type (BUY by default)
        assertEquals(TradeType.BUY, entry.getType());
    }

    /* --------------------------------------------------------------------- */
    /* 4. Profit / loss calculations (closed position)                      */
    /* --------------------------------------------------------------------- */

    @Test
    public void testProfitForClosedLongPosition() {
        // BUY at 10, SELL at 12 → profit = (12‑10)*5 = 10 (no costs)
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        Position p = new Position(entry, exit, zeroCostModel(), zeroCostModel());

        Num profit = p.getProfit();
        assertEquals(numOf(10), profit);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitForClosedShortPosition() {
        // SELL at 12, BUY at 10 → profit = (12‑10)*5 = 10 (short profit is positive)
        Trade entry = sell(0, 12, 5);
        Trade exit = buy(1, 10, 5);
        Position p = new Position(entry, exit, zeroCostModel(), zeroCostModel());

        Num profit = p.getProfit();
        assertEquals(numOf(10), profit);
        assertTrue(p.hasProfit());
    }

    @Test
    public void testLossDetection() {
        // BUY at 15, SELL at 10 → loss = (10‑15)*5 = -25
        Trade entry = buy(0, 15, 5);
        Trade exit = sell(1, 10, 5);
        Position p = new Position(entry, exit, zeroCostModel(), zeroCostModel());

        Num profit = p.getProfit();
        assertEquals(numOf(-25), profit);
        assertTrue(p.hasLoss());
        assertFalse(p.hasProfit());
    }

    @Test
    public void testGetProfitOnOpenedPositionReturnsZero() {
        Position p = new Position();
        p.operate(0, numOf(10), numOf(5)); // only entry, still opened
        assertEquals(numOf(0), p.getProfit());
    }

    @Test
    public void testGetProfitWithFinalIndexAndPriceOnOpenedPosition() {
        Position p = new Position();
        p.operate(0, numOf(10), numOf(5)); // entry only
        // assume price moves to 11 at index 5
        Num profit = p.getProfit(5, numOf(11));
        // gross profit = (11‑10)*5 = 5, no costs
        assertEquals(numOf(5), profit);
    }

    /* --------------------------------------------------------------------- */
    /* 5. Gross profit & return calculations                                 */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGrossProfitClosedLong() {
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        Position p = new Position(entry, exit);
        assertEquals(numOf(10), p.getGrossProfit());
    }

    @Test
    public void testGrossProfitClosedShort() {
        Trade entry = sell(0, 12, 5);
        Trade exit = buy(1, 10, 5);
        Position p = new Position(entry, exit);
        // For short positions the gross profit is negated inside the method
        assertEquals(numOf(10), p.getGrossProfit());
    }

    @Test
    public void testGrossProfitOnOpenedPosition() {
        Position p = new Position();
        p.operate(0, numOf(10), numOf(5)); // entry only
        // Suppose final price = 13
        Num gross = p.getGrossProfit(numOf(13));
        // (13‑10)*5 = 15
        assertEquals(numOf(15), gross);
    }

    @Test
    public void testGrossReturnBuyPosition() {
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        Position p = new Position(entry, exit);
        // return = exit/entry = 12/10 = 1.2
        assertEquals(numOf(1.2), p.getGrossReturn());
    }

    @Test
    public void testGrossReturnSellPosition() {
        Trade entry = sell(0, 12, 5);
        Trade exit = buy(1, 10, 5);
        Position p = new Position(entry, exit);
        // For short: ((exit/entry – 1) * -1) + 1 = ((10/12 – 1)*-1)+1 = ( -0.1666… * -1)+1 = 1.1666…
        Num expected = numOf(10).dividedBy(numOf(12));
        expected = expected.minus(numOf(1)).negate().plus(numOf(1));
        assertEquals(expected, p.getGrossReturn());
    }

    @Test
    public void testGrossReturnWithExplicitEntryAndExitPrices() {
        Position p = new Position();
        // entry price 8, exit price 10 → return = 10/8 = 1.25
        Num ret = p.getGrossReturn(numOf(8), numOf(10));
        assertEquals(numOf(1.25), ret);
    }

    @Test
    public void testGrossReturnBarSeriesNullThrowsNPE() {
        Position p = new Position();
        p.operate(0, numOf(10), numOf(5)); // entry
        p.operate(1, numOf(12), numOf(5)); // exit
        try {
            p.getGrossReturn(null);
            fail("Expected NullPointerException when BarSeries is null");
        } catch (NullPointerException ignored) {
            // expected
        }
    }

    /* --------------------------------------------------------------------- */
    /* 6. Position cost calculations (ZeroCostModel yields zero)            */
    /* --------------------------------------------------------------------- */

    @Test
    public void testPositionCostClosedIsZero() {
        Trade entry = buy(0, 10, 5);
        Trade exit = sell(1, 12, 5);
        Position p = new Position(entry, exit, zeroCostModel(), zeroCostModel());
        assertEquals(numOf(0), p.getPositionCost());
    }

    @Test
    public void testPositionCostOpenIsZero() {
        Position p = new Position();
        p.operate(0, numOf(10), numOf(5));
        assertEquals(numOf(0), p.getPositionCost(10));
    }

    @Test
    public void testHoldingCostDelegatesToModel() {
        // ZeroCostModel also implements holding cost → always zero
        Position p = new Position();
        p.operate(0, numOf(10), numOf(5));
        assertEquals(numOf(0), p.getHoldingCost());
        assertEquals(numOf(0), p.getHoldingCost(10));
    }

    /* --------------------------------------------------------------------- */
    /* 7. Starting type accessor                                               */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGetStartingType() {
        Position pBuy = new Position(TradeType.BUY);
        Position pSell = new Position(TradeType.SELL);
        assertEquals(TradeType.BUY, pBuy.getStartingType());
        assertEquals(TradeType.SELL, pSell.getStartingType());
    }
}
```
###Test END##