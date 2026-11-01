###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * JUnit‑4 test suite for {@link Position}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public constructors and methods</li>
 *   <li>Normal behaviour</li>
 *   <li>Edge cases (null arguments, illegal states, mismatched trades, etc.)</li>
 *   <li>Correct profit / loss calculations for BUY and SELL positions</li>
 *   <li>Equality / hashCode contracts</li>
 * </ul>
 *
 * The library supplies {@code DecimalNum} as a concrete {@link Num}
 * implementation and {@code ZeroCostModel} as a cost model that always returns zero.
 * A lightweight {@link BaseBarSeries} (part of TA‑4J) is used for the
 * {@code getGrossReturn(BarSeries)} test.
 */
public class PositionTest {

    /* --------------------------------------------------------------------- *
     *  Helper methods                                                    *
     * --------------------------------------------------------------------- */

    private static Num n(double d) {
        return DecimalNum.valueOf(d);
    }

    /** Creates a BUY trade with the given index, price and amount. */
    private static Trade buy(int index, Num price, Num amount) {
        return Trade.buyAt(index, price, amount);
    }

    /** Creates a SELL trade with the given index, price and amount. */
    private static Trade sell(int index, Num price, Num amount) {
        return Trade.sellAt(index, price, amount);
    }

    /* --------------------------------------------------------------------- *
     *  Constructor tests                                                  *
     * --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorSetsBuyStartingType() {
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
    public void testConstructorNullStartingTypeThrows() {
        new Position(null);
    }

    @Test
    public void testConstructorWithEntryAndExit() {
        Trade entry = buy(0, n(10), n(5));
        Trade exit = sell(1, n(12), n(5));
        Position p = new Position(entry, exit);
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryAndExitSameTypeThrows() {
        Trade entry = buy(0, n(10), n(5));
        Trade exit = buy(1, n(12), n(5)); // both BUY
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModelThrows() {
        // a custom cost model (different from the trade’s default ZeroCostModel)
        CostModel other = new CostModel() {
            public Num calculate(Position position, int finalIndex) { return n(0); }
            public Num calculate(Position position) { return n(0); }
            public Num calculate(Num price, Num amount) { return n(0); }
            public boolean equals(CostModel other) { return false; }
        };
        Trade entry = buy(0, n(10), n(5));
        Trade exit = sell(1, n(12), n(5));
        new Position(entry, exit, other, new ZeroCostModel());
    }

    /* --------------------------------------------------------------------- *
     *  State‑transition tests                                            *
     * --------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryWhenNew() {
        Position p = new Position(TradeType.BUY);
        Trade t = p.operate(0, n(10), n(5));
        assertNotNull(t);
        assertEquals(TradeType.BUY, t.getType());
        assertEquals(t, p.getEntry());
        assertTrue(p.isOpened());
    }

    @Test
    public void testOperateCreatesExitWhenOpened() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(5));               // entry
        Trade exit = p.operate(3, n(12), n(5));   // exit
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateIndexBeforeEntryThrows() {
        Position p = new Position(TradeType.BUY);
        p.operate(5, n(10), n(5));   // entry at index 5
        p.operate(2, n(12), n(5));   // attempt exit at earlier index
    }

    @Test
    public void testIsNewOpenedClosedTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0, n(10), n(5));
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(1, n(12), n(5));
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    /* --------------------------------------------------------------------- *
     *  Profit / loss calculations                                        *
     * --------------------------------------------------------------------- */

    @Test
    public void testProfitForClosedBuyPositionPositive() {
        Trade entry = buy(0, n(10), n(5));   // value = 50
        Trade exit = sell(1, n(12), n(5));   // value = 60
        Position p = new Position(entry, exit);
        assertEquals(n(10), p.getProfit());          // 60‑50 = 10
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitForClosedBuyPositionNegative() {
        Trade entry = buy(0, n(15), n(4));   // value = 60
        Trade exit = sell(1, n(12), n(4));   // value = 48
        Position p = new Position(entry, exit);
        assertEquals(n(-12), p.getProfit());         // 48‑60 = -12
        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());
    }

    @Test
    public void testProfitForClosedSellPositionPositive() {
        // short: sell high, buy low
        Trade entry = sell(0, n(20), n(3));  // value = 60
        Trade exit = buy(1, n(15), n(3));    // value = 45
        Position p = new Position(entry, exit);
        // gross profit = -(45‑60) = 15
        assertEquals(n(15), p.getProfit());
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitForOpenedPositionIsZero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(5)); // only entry
        assertEquals(n(0), p.getProfit());
    }

    @Test
    public void testGetProfitWithFinalValuesForOpenedPosition() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, n(20), n(2)); // entry (short)
        // final price 18 (price moved down, profit expected)
        Num profit = p.getProfit(10, n(18));
        // entry value = 40, final gross = 2*18 = 36, gross profit = -(36‑40)=4
        assertEquals(n(4), profit);
    }

    /* --------------------------------------------------------------------- *
     *  Gross profit / return tests                                        *
     * --------------------------------------------------------------------- */

    @Test
    public void testGrossProfitClosedBuy() {
        Trade entry = buy(0, n(10), n(2));
        Trade exit = sell(1, n(13), n(2));
        Position p = new Position(entry, exit);
        assertEquals(n(6), p.getGrossProfit()); // (13*2)‑(10*2)=6
    }

    @Test
    public void testGrossProfitClosedSell() {
        Trade entry = sell(0, n(20), n(1));
        Trade exit = buy(1, n(15), n(1));
        Position p = new Position(entry, exit);
        // entry value = 20, exit value = 15, gross = -(15‑20)=5
        assertEquals(n(5), p.getGrossProfit());
    }

    @Test
    public void testGrossProfitOpenedIsZero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(1));
        assertEquals(n(0), p.getGrossProfit());
    }

    @Test
    public void testGrossReturnBuy() {
        Trade entry = buy(0, n(10), n(1));
        Trade exit = sell(1, n(12), n(1));
        Position p = new Position(entry, exit);
        // (12 / 10) = 1.2
        assertEquals(DecimalNum.valueOf(1.2), p.getGrossReturn());
    }

    @Test
    public void testGrossReturnSell() {
        Trade entry = sell(0, n(20), n(1));
        Trade exit = buy(1, n(15), n(1));
        Position p = new Position(entry, exit);
        // ((15/20 – 1) * -1) + 1 = 0.75
        assertEquals(DecimalNum.valueOf(0.75), p.getGrossReturn());
    }

    @Test
    public void testGrossReturnWithBarSeries() {
        // Use a very small BarSeries implementation – BaseBarSeries is part of TA‑4J
        BarSeries series = new BaseBarSeries("test-series");
        // add two dummy bars (price values are not used because we supply the price directly)
        series.addBar(new BaseBar(0, n(10), n(10), n(10), n(10), n(0)));
        series.addBar(new BaseBar(1, n(12), n(12), n(12), n(12), n(0)));

        Trade entry = buy(0, n(10), n(1));
        Trade exit = sell(1, n(12), n(1));
        Position p = new Position(entry, exit);
        assertEquals(DecimalNum.valueOf(1.2), p.getGrossReturn(series));
    }

    /* --------------------------------------------------------------------- *
     *  Position cost tests (ZeroCostModel => zero)                       *
     * --------------------------------------------------------------------- */

    @Test
    public void testPositionCostClosedIsZero() {
        Trade entry = buy(0, n(10), n(1));
        Trade exit = sell(1, n(12), n(1));
        Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
        assertEquals(n(0), p.getPositionCost());
    }

    @Test
    public void testPositionCostOpenedIsZero() {
        Position p = new Position(TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        p.operate(0, n(10), n(1));
        assertEquals(n(0), p.getPositionCost(5));
    }

    /* --------------------------------------------------------------------- *
     *  Equality & hashCode                                               *
     * --------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = buy(0, n(10), n(2));
        Trade exit1   = sell(1, n(13), n(2));
        Position p1 = new Position(entry1, exit1);

        Trade entry2 = buy(0, n(10), n(2));
        Trade exit2   = sell(1, n(13), n(2));
        Position p2 = new Position(entry2, exit2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // different exit price -> not equal
        Trade exit3 = sell(1, n(14), n(2));
        Position p3 = new Position(entry1, exit3);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, n(10), n(1));
        Trade exit = sell(1, n(12), n(1));
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }
}
```
###Test END##