###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>construction (including illegal arguments)</li>
 *   <li>state transitions (new → opened → closed)</li>
 *   <li>operate() behaviour and error handling</li>
 *   <li>profit / loss calculations (gross & net)</li>
 *   <li>gross return for BUY and SELL positions</li>
 *   <li>equals() / hashCode()</li>
 *   <li>hasProfit() / hasLoss()</li>
 * </ul>
 *
 * <p>A very small {@code Num} implementation ( {@link SimpleNum}) is provided
 * so that the tests do not depend on the library’s concrete numeric type
 * (e.g. {@code DecimalNum}).  It implements only the operations required by
 * {@link Position} and {@link Trade}.
 */
public class PositionTest {

    /* --------------------------------------------------------------------- *
     *  Simple Num implementation (only what the tests need)
     * --------------------------------------------------------------------- */
    private static final class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* factory */
        static SimpleNum of(double v) {
            return new SimpleNum(v);
        }

        /* basic arithmetic */
        @Override public Num plus(Num augend)   { return of(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend){ return of(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand){ return of(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor){ return of(this.value / ((SimpleNum) divisor).value); }
        @Override public Num negate() { return of(-this.value); }

        /* comparisons */
        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum) o).value); }
        @Override public boolean isPositive() { return this.value > 0; }
        @Override public boolean isNegative() { return this.value < 0; }
        @Override public boolean isZero() { return this.value == 0; }
        @Override public boolean isNaN() { return Double.isNaN(this.value); }

        /* required default helpers */
        @Override public Num zero() { return of(0); }
        @Override public Num one()  { return of(1); }
        @Override public Num hundred(){ return of(100); }
        @Override public Num abs()   { return of(Math.abs(this.value)); }
        @Override public Num floor() { return of(Math.floor(this.value)); }
        @Override public Num ceil()  { return of(Math.ceil(this.value)); }
        @Override public Num pow(int n) { return of(Math.pow(this.value, n)); }
        @Override public Num pow(Num n) { return of(Math.pow(this.value, ((SimpleNum) n).value)); }
        @Override public Num log() { return of(Math.log(this.value)); }
        @Override public Num sqrt() { return of(Math.sqrt(this.value)); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num remainder(Num divisor) { return of(this.value % ((SimpleNum) divisor).value); }
        @Override public Num min(Num other) { return of(Math.min(this.value, ((SimpleNum) other).value)); }
        @Override public Num max(Num other) { return of(Math.max(this.value, ((SimpleNum) other).value)); }

        @Override public Number getDelegate() { return value; }
        @Override public String getName() { return "SimpleNum"; }

        @Override public boolean isPositiveOrZero() { return this.value >= 0; }
        @Override public boolean isNegativeOrZero() { return this.value <= 0; }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other){ return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other){ return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other){ return this.value <= ((SimpleNum) other).value; }

        @Override public double doubleValue() { return value; }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }

        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }
        @Override public String toString() { return Double.toString(value); }

        /* the default methods from the interface that rely on the above are
           left untouched – they work automatically. */
    }

    /* --------------------------------------------------------------------- *
     *  Helper to build a Position with explicit entry/exit Trades
     * --------------------------------------------------------------------- */
    private Position createClosedBuyPosition(double entryPrice, double exitPrice, double amount) {
        Trade entry = Trade.buyAt(0, SimpleNum.of(entryPrice), SimpleNum.of(amount));
        Trade exit  = Trade.sellAt(1, SimpleNum.of(exitPrice), SimpleNum.of(amount));
        return new Position(entry, exit);
    }

    private Position createClosedSellPosition(double entryPrice, double exitPrice, double amount) {
        Trade entry = Trade.sellAt(0, SimpleNum.of(entryPrice), SimpleNum.of(amount));
        Trade exit  = Trade.buyAt(1, SimpleNum.of(exitPrice), SimpleNum.of(amount));
        return new Position(entry, exit);
    }

    /* --------------------------------------------------------------------- *
     *  1. Constructor validation
     * --------------------------------------------------------------------- */
    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullStartingType_throws() {
        new Position(null);
    }

    @Test
    public void defaultConstructor_hasBuyStartingType() {
        Position p = new Position(); // default = BUY
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    /* --------------------------------------------------------------------- *
     *  2. operate() – state transitions and illegal index
     * --------------------------------------------------------------------- */
    @Test
    public void operate_createsEntryAndExitCorrectly() {
        Position p = new Position(TradeType.BUY);
        // entry
        Trade e = p.operate(0, SimpleNum.of(10), SimpleNum.of(1));
        assertNotNull(e);
        assertEquals(TradeType.BUY, e.getType());
        assertTrue(p.isOpened());

        // exit (must be complement type)
        Trade x = p.operate(2, SimpleNum.of(12), SimpleNum.of(1));
        assertNotNull(x);
        assertEquals(TradeType.SELL, x.getType());
        assertTrue(p.isClosed());

        // verify stored trades are the same objects
        assertSame(e, p.getEntry());
        assertSame(x, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void operate_indexBeforeEntry_throws() {
        Position p = new Position(TradeType.BUY);
        p.operate(5, SimpleNum.of(10), SimpleNum.of(1)); // creates entry at index 5
        // now try to close with a lower index
        p.operate(3, SimpleNum.of(12), SimpleNum.of(1));
    }

    @Test
    public void operate_onClosedPosition_returnsNull() {
        Position p = createClosedBuyPosition(10, 12, 1);
        // position already closed – operate should do nothing
        assertNull(p.operate(5, SimpleNum.of(15), SimpleNum.of(1)));
    }

    /* --------------------------------------------------------------------- *
     *  3. State queries
     * --------------------------------------------------------------------- */
    @Test
    public void stateQueries_workAsExpected() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0, SimpleNum.of(10), SimpleNum.of(1));
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(1, SimpleNum.of(12), SimpleNum.of(1));
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    /* --------------------------------------------------------------------- *
     *  4. Profit / loss calculations (closed positions)
     * --------------------------------------------------------------------- */
    @Test
    public void profit_closedBuyPosition_positive() {
        Position p = createClosedBuyPosition(10, 12, 1);
        // gross profit = 12 - 10 = 2 ; no costs -> net profit = 2
        assertEquals(SimpleNum.of(2).doubleValue(), p.getProfit().doubleValue(), 0.000001);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void profit_closedBuyPosition_negative() {
        Position p = createClosedBuyPosition(12, 10, 1);
        // gross profit = -2
        assertEquals(SimpleNum.of(-2).doubleValue(), p.getProfit().doubleValue(), 0.000001);
        assertTrue(p.hasLoss());
        assertFalse(p.hasProfit());
    }

    @Test
    public void profit_closedSellPosition_positive() {
        Position p = createClosedSellPosition(12, 10, 1);
        // entry is SELL at 12, exit BUY at 10 -> profit = 2 (shorting)
        assertEquals(SimpleNum.of(2).doubleValue(), p.getProfit().doubleValue(), 0.000001);
    }

    @Test
    public void profit_openedPosition_returnsZero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, SimpleNum.of(10), SimpleNum.of(1));
        assertEquals(0.0, p.getProfit().doubleValue(), 0.000001);
    }

    /* --------------------------------------------------------------------- *
     *  5. Gross profit & gross return
     * --------------------------------------------------------------------- */
    @Test
    public void grossProfit_closedBuyPosition() {
        Position p = createClosedBuyPosition(10, 15, 2); // value = 30, entry value = 20
        // exit value - entry value = (15*2) - (10*2) = 10
        assertEquals(SimpleNum.of(10).doubleValue(), p.getGrossProfit().doubleValue(), 0.000001);
    }

    @Test
    public void grossReturn_buyPosition() {
        Position p = createClosedBuyPosition(10, 12, 1);
        // 12 / 10 = 1.2
        assertEquals(1.2, p.getGrossReturn().doubleValue(), 0.000001);
    }

    @Test
    public void grossReturn_sellPosition() {
        Position p = createClosedSellPosition(12, 10, 1);
        // ((10/12 - 1) * -1) + 1 = 1.166666...
        assertEquals(1.1666666667, p.getGrossReturn().doubleValue(), 0.000001);
    }

    @Test
    public void grossReturn_openedPosition_isZero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, SimpleNum.of(10), SimpleNum.of(1));
        assertEquals(0.0, p.getGrossReturn().doubleValue(), 0.000001);
    }

    /* --------------------------------------------------------------------- *
     *  6. Position cost (uses ZeroCostModel -> always zero)
     * --------------------------------------------------------------------- */
    @Test
    public void positionCost_closedPosition_isZero() {
        Position p = createClosedBuyPosition(10, 12, 1);
        assertEquals(0.0, p.getPositionCost().doubleValue(), 0.000001);
    }

    @Test
    public void positionCost_openedPosition_isZero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, SimpleNum.of(10), SimpleNum.of(1));
        assertEquals(0.0, p.getPositionCost(5).doubleValue(), 0.000001);
    }

    /* --------------------------------------------------------------------- *
     *  7. equals() and hashCode()
     * --------------------------------------------------------------------- */
    @Test
    public void equalsAndHashCode_work() {
        Position p1 = createClosedBuyPosition(10, 12, 1);
        Position p2 = createClosedBuyPosition(10, 12, 1);
        Position p3 = createClosedBuyPosition(10, 13, 1); // different exit price

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        assertNotEquals(p1, p3);
        assertNotEquals(p1.hashCode(), p3.hashCode());
    }

    /* --------------------------------------------------------------------- *
     *  8. toString sanity check (not exhaustive)
     * --------------------------------------------------------------------- */
    @Test
    public void toString_containsEntryAndExit() {
        Position p = createClosedBuyPosition(10, 12, 1);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
    }
}
```
###Test END##