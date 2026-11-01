package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests cover:
 * <ul>
 *   <li>Constructor validation (null starting type, mismatched trade types, mismatched cost models)</li>
 *   <li>State transitions (new → opened → closed) via {@link Position#operate(int)}</li>
 *   <li>Profit / loss calculations for both BUY and SELL positions</li>
 *   <li>Gross profit, gross return and helper predicates {@code hasProfit}/{@code hasLoss}</li>
 *   <li>Equality, hash‑code and {@code toString}</li>
 *   <li>Cost model integration (using {@link ZeroCostModel})</li>
 * </ul>
 *
 * All numeric values are created with {@link DecimalNum} (the standard
 * implementation of {@link Num} in TA‑4J).  The {@link ZeroCostModel} is used
 * everywhere so that transaction and holding costs are always zero – this
 * isolates the profit‑related logic.
 */
public class PositionTest {

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                          */
    /* ---------------------------------------------------------------------- */

    /** Shortcut to create a {@link DecimalNum} from a primitive double. */
    private static Num d(double value) {
        return DecimalNum.valueOf(value);
    }

    /** Creates a BUY trade at the given index, price and amount. */
    private static Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, d(price), d(amount));
    }

    /** Creates a SELL trade at the given index, price and amount. */
    private static Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, d(price), d(amount));
    }

    /* ---------------------------------------------------------------------- */
    /* Constructor validation                                                  */
    /* ---------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingType() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSameTradeTypes() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit = buy(5, 12.0, 1.0); // also BUY → illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModels() {
        // entry uses a non‑zero cost model, exit uses the default zero model
        CostModel customCost = new CostModel() {
            public Num calculate(Position p, int i) { return d(0); }
            public Num calculate(Position p) { return d(0); }
            public Num calculate(Num price, Num amount) { return d(0); }
            public boolean equals(CostModel other) { return false; }
        };
        Trade entry = Trade.buyAt(0, d(10), d(1), customCost);
        Trade exit  = Trade.sellAt(5, d(12), d(1)); // ZeroCostModel internally
        new Position(entry, exit, customCost, new ZeroCostModel());
    }

    /* ---------------------------------------------------------------------- */
    /* State transition tests                                                  */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testInitialStateIsNew() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testOperateCreatesEntryAndExit() {
        Position p = new Position(TradeType.BUY);
        // first call creates entry
        Trade entry = p.operate(0, d(10), d(1));
        assertNotNull(entry);
        assertEquals(TradeType.BUY, entry.getType());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // second call creates exit
        Trade exit = p.operate(5, d(12), d(1));
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertTrue(p.isClosed());

        // verify entry and exit stored inside the position
        assertSame(entry, p.getEntry());
        assertSame(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateIndexBeforeEntryThrows() {
        Position p = new Position(TradeType.BUY);
        p.operate(3, d(10), d(1)); // creates entry at index 3
        p.operate(2, d(12), d(1)); // index < entry index → exception
    }

    /* ---------------------------------------------------------------------- */
    /* Profit / loss calculations (BUY)                                        */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testBuyPositionProfitAndLoss() {
        Position p = new Position(TradeType.BUY);
        // entry at price 10, amount 2  → value = 20
        p.operate(0, d(10), d(2));
        // exit at price 13, amount 2 → value = 26
        p.operate(4, d(13), d(2));

        // Gross profit = exit.value - entry.value = 26 - 20 = 6
        Num expectedGross = d(6);
        assertEquals(expectedGross, p.getGrossProfit());

        // With ZeroCostModel, net profit = gross profit
        assertEquals(expectedGross, p.getProfit());

        // Positive profit → hasProfit true, hasLoss false
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());

        // Gross return = exitPrice / entryPrice = 13 / 10 = 1.3
        Num expectedReturn = d(13).dividedBy(d(10));
        assertEquals(expectedReturn, p.getGrossReturn());
    }

    @Test
    public void testBuyPositionLoss() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, d(15), d(1));
        p.operate(3, d(12), d(1)); // price fell

        Num expectedGross = d(12).minus(d(15)); // -3
        assertEquals(expectedGross, p.getGrossProfit());
        assertEquals(expectedGross, p.getProfit());

        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());

        // Gross return = 12 / 15 = 0.8
        Num expectedReturn = d(12).dividedBy(d(15));
        assertEquals(expectedReturn, p.getGrossReturn());
    }

    /* ---------------------------------------------------------------------- */
    /* Profit / loss calculations (SELL)                                       */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testSellPositionProfit() {
        Position p = new Position(TradeType.SELL);
        // short entry at price 20, amount 1 (value = 20)
        p.operate(0, d(20), d(1));
        // cover (buy) at price 15, amount 1 (value = 15)
        p.operate(5, d(15), d(1));

        // For short, gross profit = -(exit.value - entry.value) = -(15-20)=5
        Num expectedGross = d(5);
        assertEquals(expectedGross, p.getGrossProfit());
        assertEquals(expectedGross, p.getProfit());

        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());

        // Gross return for short: ((exit/entry - 1) * -1) + 1
        // exit/entry = 15/20 = 0.75 → (0.75-1) = -0.25 → -(-0.25)=0.25 → +1 = 1.25
        Num expectedReturn = d(15).dividedBy(d(20));
        Num one = d(1);
        Num grossReturn = expectedReturn.minus(one).negate().plus(one);
        assertEquals(grossReturn, p.getGrossReturn());
    }

    @Test
    public void testSellPositionLoss() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, d(10), d(1));
        p.operate(2, d(12), d(1)); // price went up → loss

        // Gross profit = -(12-10) = -2
        Num expectedGross = d(-2);
        assertEquals(expectedGross, p.getGrossProfit());
        assertEquals(expectedGross, p.getProfit());

        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());

        // Gross return for short: exit/entry = 12/10 = 1.2
        // (1.2-1) = 0.2 → -(0.2) = -0.2 → +1 = 0.8
        Num expectedReturn = d(12).dividedBy(d(10));
        Num one = d(1);
        Num grossReturn = expectedReturn.minus(one).negate().plus(one);
        assertEquals(grossReturn, p.getGrossReturn());
    }

    /* ---------------------------------------------------------------------- */
    /* Open position profit calculations (intermediate profit)                 */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOpenPositionProfitUsesFinalPrice() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, d(10), d(1)); // entry only, still opened

        // Simulate final bar at price 12 (still open)
        Num profit = p.getProfit(10, d(12));
        // Expected gross: amount * finalPrice - entry.value = 1*12 - 10 = 2
        Num expectedGross = d(2);
        assertEquals(expectedGross, profit);
    }

    /* ---------------------------------------------------------------------- */
    /* Cost model integration (ZeroCostModel ⇒ zero cost)                     */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testZeroCostModelYieldsZeroPositionCost() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, d(5), d(1));
        p.operate(3, d(7), d(1));

        Num cost = p.getPositionCost();
        assertTrue(cost.isZero());

        Num costWithIndex = p.getPositionCost(3);
        assertTrue(costWithIndex.isZero());
    }

    /* ---------------------------------------------------------------------- */
    /* Equality, hashCode and toString                                         */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position(TradeType.BUY);
        Position p2 = new Position(TradeType.BUY);

        // both new → equal
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // after entry creation they diverge
        p1.operate(0, d(10), d(1));
        assertNotEquals(p1, p2);

        // make p2 identical
        p2.operate(0, d(10), d(1));
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // after exit creation they stay equal
        p1.operate(5, d(12), d(1));
        p2.operate(5, d(12), d(1));
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, d(8), d(1));
        p.operate(4, d(9), d(1));

        String str = p.toString();
        assertTrue(str.contains("Entry"));
        assertTrue(str.contains("exit"));
        assertTrue(str.contains(p.getEntry().toString()));
        assertTrue(str.contains(p.getExit().toString()));
    }
}
