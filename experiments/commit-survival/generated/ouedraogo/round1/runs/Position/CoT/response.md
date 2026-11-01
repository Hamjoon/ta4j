###Test START##
```java
package org.ta4j.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * JUnit‑4 test suite for {@link Position}.
 *
 * <p>All public methods of {@code Position} are listed, basic functionality is
 * tested, and edge‑case / exception scenarios are covered.</p>
 *
 * <p>Only classes that appear in the supplied source code or in the listed
 * dependency signatures are used.  A tiny {@code SimpleNum} implementation and a
 * {@code SimpleCostModel} are defined locally to avoid external library
 * dependencies.</p>
 */
public class PositionTest {

    /** Simple {@link Num} implementation based on {@code double}. */
    private static class SimpleNum implements Num {
        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* ----------- factory helpers ----------- */
        static SimpleNum of(double v) {
            return new SimpleNum(v);
        }

        /* ----------- Num interface implementation (only needed methods) ----------- */
        @Override public Num zero() { return SimpleNum.of(0); }
        @Override public Num one()  { return SimpleNum.of(1); }
        @Override public Num hundred() { return SimpleNum.of(100); }

        @Override public Num plus(Num augend) {
            return SimpleNum.of(this.value + ((SimpleNum) augend).value);
        }
        @Override public Num minus(Num subtrahend) {
            return SimpleNum.of(this.value - ((SimpleNum) subtrahend).value);
        }
        @Override public Num multipliedBy(Num multiplicand) {
            return SimpleNum.of(this.value * ((SimpleNum) multiplicand).value);
        }
        @Override public Num dividedBy(Num divisor) {
            return SimpleNum.of(this.value / ((SimpleNum) divisor).value);
        }
        @Override public Num negate() { return SimpleNum.of(-this.value); }

        @Override public boolean isZero()      { return this.value == 0; }
        @Override public boolean isPositive()  { return this.value > 0; }
        @Override public boolean isNegative()   { return this.value < 0; }

        @Override public int compareTo(Num o) {
            return Double.compare(this.value, ((SimpleNum) o).value);
        }

        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(this.value, ((SimpleNum) obj).value) == 0;
        }
        @Override public int hashCode() { return Double.hashCode(value); }

        @Override public String toString() { return Double.toString(value); }

        /* ---------- methods not required for the tests are stubbed ---------- */
        @Override public Number getDelegate() { return value; }
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num remainder(Num divisor) { throw new UnsupportedOperationException(); }
        @Override public Num floor() { throw new UnsupportedOperationException(); }
        @Override public Num ceil() { throw new UnsupportedOperationException(); }
        @Override public Num pow(int n) { throw new UnsupportedOperationException(); }
        @Override public Num pow(Num n) { throw new UnsupportedOperationException(); }
        @Override public Num log() { throw new UnsupportedOperationException(); }
        @Override public Num sqrt() { throw new UnsupportedOperationException(); }
        @Override public Num sqrt(int precision) { throw new UnsupportedOperationException(); }
        @Override public Num abs() { throw new UnsupportedOperationException(); }
        @Override public boolean isPositiveOrZero() { throw new UnsupportedOperationException(); }
        @Override public boolean isNegativeOrZero() { throw new UnsupportedOperationException(); }
        @Override public boolean isEqual(Num other) { throw new UnsupportedOperationException(); }
        @Override public boolean isGreaterThan(Num other) { throw new UnsupportedOperationException(); }
        @Override public boolean isGreaterThanOrEqual(Num other) { throw new UnsupportedOperationException(); }
        @Override public boolean isLessThan(Num other) { throw new UnsupportedOperationException(); }
        @Override public boolean isLessThanOrEqual(Num other) { throw new UnsupportedOperationException(); }
        @Override public Num min(Num other) { throw new UnsupportedOperationException(); }
        @Override public Num max(Num other) { throw new UnsupportedOperationException(); }
        @Override public java.util.function.Function<Number, Num> function() { throw new UnsupportedOperationException(); }
        @Override public Num numOf(Number value) { return SimpleNum.of(value.doubleValue()); }
        @Override public Num numOf(String value, int precision) { return SimpleNum.of(Double.parseDouble(value)); }
        @Override public boolean isNaN() { return false; }
        @Override public double doubleValue() { return value; }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
    }

    /** Simple {@link CostModel} that always returns zero and reports equality only by class. */
    private static class SimpleCostModel implements CostModel {

        private static final SimpleNum ZERO = SimpleNum.of(0);

        @Override public Num calculate(Position position, int finalIndex) { return ZERO; }
        @Override public Num calculate(Position position) { return ZERO; }
        @Override public Num calculate(Num price, Num amount) { return ZERO; }

        @Override public boolean equals(CostModel otherModel) {
            return otherModel != null && otherModel.getClass() == this.getClass();
        }
    }

    /* --------------------------------------------------------------------- */
    /*  Test data shared between test methods                               */
    /* --------------------------------------------------------------------- */
    private SimpleNum price10;
    private SimpleNum price12;
    private SimpleNum price8;
    private SimpleNum amount1;

    @Before
    public void setUp() {
        price10 = SimpleNum.of(10);
        price12 = SimpleNum.of(12);
        price8  = SimpleNum.of(8);
        amount1 = SimpleNum.of(1);
    }

    /* --------------------------------------------------------------------- */
    /*  1.  Public constructors – basic & exceptional behaviours              */
    /* --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullStartingType_throws() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_entryAndExitSameType_throws() {
        Trade entry = Trade.buyAt(0, price10, amount1);
        Trade exit  = Trade.buyAt(1, price12, amount1); // also BUY -> illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_mismatchedCostModel_throws() {
        // entry & exit created with the default ZeroCostModel
        Trade entry = Trade.buyAt(0, price10, amount1);
        Trade exit  = Trade.sellAt(1, price12, amount1);
        // supply a different CostModel instance
        CostModel otherModel = new SimpleCostModel();
        new Position(entry, exit, otherModel, new ZeroCostModel());
    }

    @Test
    public void constructor_defaultStartingType_isBuy() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
    }

    @Test
    public void constructor_withStartingType_isRespected() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    /* --------------------------------------------------------------------- */
    /*  2.  operate(...) – creation of entry & exit, and illegal index       */
    /* --------------------------------------------------------------------- */

    @Test
    public void operate_newPosition_createsEntry() {
        Position p = new Position();
        Trade e = p.operate(5, price10, amount1);
        assertNotNull(e);
        assertEquals(5, e.getIndex());
        assertEquals(TradeType.BUY, e.getType());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());
        assertSame(e, p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void operate_openedPosition_createsExit() {
        Position p = new Position();
        p.operate(2, price10, amount1);               // entry
        Trade exit = p.operate(4, price12, amount1);   // exit
        assertNotNull(exit);
        assertEquals(4, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType());
        assertTrue(p.isClosed());
        assertSame(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void operate_indexBeforeEntry_throws() {
        Position p = new Position();
        p.operate(10, price10, amount1); // entry at index 10
        p.operate(5, price12, amount1);  // illegal index < entry index
    }

    @Test
    public void operate_withoutPriceAmount_usesNaN() {
        Position p = new Position();
        Trade e = p.operate(0); // uses NaN for price & amount
        assertNotNull(e);
        assertEquals(TradeType.BUY, e.getType());
    }

    /* --------------------------------------------------------------------- */
    /*  3.  State query methods (isNew, isOpened, isClosed)                  */
    /* --------------------------------------------------------------------- */

    @Test
    public void stateMethods_transitionCorrectly() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(0, price10, amount1); // entry
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        p.operate(1, price12, amount1); // exit
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    /* --------------------------------------------------------------------- */
    /*  4.  Profit / loss calculations (closed BUY position)               */
    /* --------------------------------------------------------------------- */

    @Test
    public void profit_closedBuyPosition_positive() {
        Position p = new Position();
        p.operate(0, price10, amount1); // BUY @10
        p.operate(1, price12, amount1); // SELL @12
        // Expected profit = (12*1) - (10*1) = 2
        SimpleNum expected = SimpleNum.of(2);
        assertEquals(expected, p.getProfit());
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void grossProfit_closedBuyPosition_correct() {
        Position p = new Position();
        p.operate(0, price10, amount1);
        p.operate(1, price12, amount1);
        SimpleNum expected = SimpleNum.of(2);
        assertEquals(expected, p.getGrossProfit());
    }

    @Test
    public void grossReturn_closedBuyPosition_correct() {
        Position p = new Position();
        p.operate(0, price10, amount1);
        p.operate(1, price12, amount1);
        SimpleNum expected = price12.dividedBy(price10); // 12/10 = 1.2
        assertEquals(expected, p.getGrossReturn());
    }

    /* --------------------------------------------------------------------- */
    /*  5.  Profit / loss calculations (closed SELL / short position)        */
    /* --------------------------------------------------------------------- */

    @Test
    public void profit_closedSellPosition_positive() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, price10, amount1); // SELL @10
        p.operate(1, price8, amount1);    // BUY @8
        // Expected profit = (10-8) = 2 (because short profit = entry - exit)
        SimpleNum expected = SimpleNum.of(2);
        assertEquals(expected, p.getProfit());
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void grossReturn_closedSellPosition_correct() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, price10, amount1); // entry (sell)
        p.operate(1, price8, amount1); // exit (buy)
        // Formula: ((exit/entry - 1).negate()) + 1
        SimpleNum one = SimpleNum.of(1);
        SimpleNum exitDivEntry = price8.dividedBy(price10); // 0.8
        SimpleNum expected = exitDivEntry.minus(one).negate().plus(one); // 1.2
        assertEquals(expected, p.getGrossReturn());
    }

    /* --------------------------------------------------------------------- */
    /*  6.  Open position profit (should be zero)                            */
    /* --------------------------------------------------------------------- */

    @Test
    public void profit_openPosition_returnsZero() {
        Position p = new Position();
        p.operate(0, price10, amount1); // only entry
        SimpleNum zero = price10.zero(); // zero from any Num instance
        assertEquals(zero, p.getProfit());
        assertEquals(zero, p.getGrossProfit());
    }

    /* --------------------------------------------------------------------- */
    /*  7.  Equality, hashCode and toString                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void equalsAndHashCode_identicalPositions() {
        Position p1 = new Position();
        p1.operate(0, price10, amount1);
        p1.operate(1, price12, amount1);

        Position p2 = new Position();
        p2.operate(0, price10, amount1);
        p2.operate(1, price12, amount1);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void equals_differentEntryOrExit_false() {
        Position p1 = new Position();
        p1.operate(0, price10, amount1);
        p1.operate(1, price12, amount1);

        Position p2 = new Position();
        p2.operate(0, price10, amount1);
        // p2 has no exit yet
        assertNotEquals(p1, p2);
    }

    @Test
    public void toString_containsEntryAndExit() {
        Position p = new Position();
        p.operate(0, price10, amount1);
        p.operate(1, price12, amount1);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
    }

    /* --------------------------------------------------------------------- */
    /*  8.  Position cost helpers – using ZeroCostModel (cost = 0)          */
    /* --------------------------------------------------------------------- */

    @Test
    public void positionCost_closedPosition_isZero() {
        Position p = new Position();
        p.operate(0, price10, amount1);
        p.operate(1, price12, amount1);
        SimpleNum zero = price10.zero();
        assertEquals(zero, p.getPositionCost());
    }

    @Test
    public void holdingCost_closedPosition_isZero() {
        Position p = new Position();
        p.operate(0, price10, amount1);
        p.operate(1, price12, amount1);
        SimpleNum zero = price10.zero();
        assertEquals(zero, p.getHoldingCost());
    }

    /* --------------------------------------------------------------------- */
    /*  9.  getProfit(int,Num) – open position profit up to a final price   */
    /* --------------------------------------------------------------------- */

    @Test
    public void profit_openPosition_withFinalPrice() {
        Position p = new Position();
        p.operate(0, price10, amount1); // entry BUY @10
        // final price = 11, final index = 5
        SimpleNum finalPrice = SimpleNum.of(11);
        SimpleNum profit = p.getProfit(5, finalPrice);
        // gross = amount*finalPrice - entryValue = 1*11 - 10 = 1 ; costs = 0
        SimpleNum expected = SimpleNum.of(1);
        assertEquals(expected, profit);
    }
}
```
###Test END##