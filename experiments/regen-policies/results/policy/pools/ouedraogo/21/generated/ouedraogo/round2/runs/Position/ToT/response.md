###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>
 * The test suite is written without any external mocking framework.  Minimal
 * concrete implementations of {@link Num}, {@link NumFactory} and {@link BarSeries}
 * are provided inside the test class so that the public API of {@code Position}
 * can be exercised with real objects.
 * </p>
 */
public class PositionTest {

    /* ----------------------------------------------------------------------
     * Minimal numeric implementation (Num) and its factory
     * ---------------------------------------------------------------------- */

    private static class SimpleNum implements Num {
        private final double value;
        private final SimpleNumFactory factory;

        SimpleNum(double value, SimpleNumFactory factory) {
            this.value = value;
            this.factory = factory;
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return factory; }
        @Override public String getName() { return "SimpleNum"; }

        private SimpleNum newNum(double v) { return new SimpleNum(v, factory); }

        @Override public Num plus(Num augend)   { return newNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend){ return newNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand){ return newNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor){ return newNum(this.value / ((SimpleNum) divisor).value); }
        @Override public Num remainder(Num divisor){ return newNum(this.value % ((SimpleNum) divisor).value); }
        @Override public Num floor(){ return newNum(Math.floor(this.value)); }
        @Override public Num ceil(){ return newNum(Math.ceil(this.value)); }
        @Override public Num pow(int n){ return newNum(Math.pow(this.value, n)); }
        @Override public Num pow(Num n){ return newNum(Math.pow(this.value, ((SimpleNum)n).value)); }
        @Override public Num log(){ return newNum(Math.log(this.value)); }
        @Override public Num sqrt(){ return newNum(Math.sqrt(this.value)); }
        @Override public Num sqrt(int precision){ return sqrt(); }
        @Override public Num abs(){ return newNum(Math.abs(this.value)); }
        @Override public Num negate(){ return newNum(-this.value); }

        @Override public boolean isZero(){ return value == 0.0; }
        @Override public boolean isPositive(){ return value > 0.0; }
        @Override public boolean isPositiveOrZero(){ return value >= 0.0; }
        @Override public boolean isNegative(){ return value < 0.0; }
        @Override public boolean isNegativeOrZero(){ return value <= 0.0; }
        @Override public boolean isEqual(Num other){ return Double.compare(this.value, ((SimpleNum)other).value) == 0; }
        @Override public boolean isGreaterThan(Num other){ return this.value > ((SimpleNum)other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other){ return this.value >= ((SimpleNum)other).value; }
        @Override public boolean isLessThan(Num other){ return this.value < ((SimpleNum)other).value; }
        @Override public boolean isLessThanOrEqual(Num other){ return this.value <= ((SimpleNum)other).value; }
        @Override public Num min(Num other){ return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other){ return this.isGreaterThan(other) ? this : other; }

        @Override public boolean isNaN(){ return Double.isNaN(value); }
        @Override public int intValue(){ return (int) value; }
        @Override public long longValue(){ return (long) value; }
        @Override public float floatValue(){ return (float) value; }
        @Override public double doubleValue(){ return value; }
        @Override public BigDecimal bigDecimalValue(){ return BigDecimal.valueOf(value); }

        @Override public int hashCode(){ return Double.hashCode(value); }
        @Override public String toString(){ return Double.toString(value); }
        @Override public boolean equals(Object obj){
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum)obj).value) == 0;
        }

        @Override public int compareTo(Num o){ return Double.compare(this.value, ((SimpleNum)o).value); }
    }

    private static class SimpleNumFactory implements NumFactory {
        @Override public Num zero(){ return new SimpleNum(0.0, this); }
        @Override public Num one(){ return new SimpleNum(1.0, this); }
        public Num valueOf(double d){ return new SimpleNum(d, this); }
        // other factory methods are not needed for the tests
    }

    /* ----------------------------------------------------------------------
     * Minimal BarSeries implementation (only what Position needs)
     * ---------------------------------------------------------------------- */

    private static class SimpleBarSeries implements BarSeries {
        private final SimpleNumFactory factory = new SimpleNumFactory();

        @Override public NumFactory numFactory(){ return factory; }
        @Override public BaseBarConvertibleBuilder barBuilder(){ throw new UnsupportedOperationException(); }
        @Override public String getName(){ return "simple"; }
        @Override public Bar getBar(int i){ throw new UnsupportedOperationException(); }
        @Override public int getBarCount(){ return 0; }
        @Override public List<Bar> getBarData(){ return Collections.emptyList(); }
        @Override public int getBeginIndex(){ return 0; }
        @Override public int getEndIndex(){ return 0; }
        @Override public int getMaximumBarCount(){ return 0; }
        @Override public void setMaximumBarCount(int maximumBarCount){ }
        @Override public int getRemovedBarsCount(){ return 0; }
        @Override public void addBar(Bar bar){ }
        @Override public void addBar(Bar bar, boolean replace){ }
        @Override public void addTrade(Number tradeVolume, Number tradePrice){ }
        @Override public void addTrade(Num tradeVolume, Num tradePrice){ }
        @Override public void addPrice(Num price){ }
        @Override public void addPrice(Number price){ }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex){ return this; }
        @Override public String getSeriesPeriodDescription(){ return ""; }
        @Override public boolean isEmpty(){ return true; }
    }

    private SimpleNumFactory nf;
    private SimpleBarSeries series;

    @Before
    public void setUp() {
        nf = new SimpleNumFactory();
        series = new SimpleBarSeries();
    }

    /* ----------------------------------------------------------------------
     * Helper methods to create trades with our SimpleNum
     * ---------------------------------------------------------------------- */

    private Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, nf.valueOf(price), nf.valueOf(amount));
    }

    private Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, nf.valueOf(price), nf.valueOf(amount));
    }

    /* ----------------------------------------------------------------------
     * 1. Constructor tests
     * ---------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorSetsStartingTypeToBuy() {
        Position pos = new Position();
        assertEquals(TradeType.BUY, pos.getStartingType());
        assertTrue(pos.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null);
    }

    @Test
    public void testPositionFromEntryExitTrades() {
        Trade entry = buy(0, 100, 10);
        Trade exit = sell(5, 110, 10);
        Position pos = new Position(entry, exit);
        assertEquals(entry, pos.getEntry());
        assertEquals(exit, pos.getExit());
        assertTrue(pos.isClosed());
        assertEquals(TradeType.BUY, pos.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntryExitMustHaveDifferentTypes() {
        Trade entry = buy(0, 100, 10);
        Trade exit = buy(5, 110, 10); // same type -> illegal
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntryExitMustShareSameCostModel() {
        Trade entry = Trade.buyAt(0, nf.valueOf(100), nf.valueOf(10), new ZeroCostModel());
        Trade exit = Trade.sellAt(5, nf.valueOf(110), nf.valueOf(10), new ZeroCostModel()); // different model
        new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    }

    /* ----------------------------------------------------------------------
     * 2. State query methods (isNew / isOpened / isClosed)
     * ---------------------------------------------------------------------- */

    @Test
    public void testStateTransitions() {
        Position pos = new Position();
        assertTrue(pos.isNew());
        assertFalse(pos.isOpened());
        assertFalse(pos.isClosed());

        // operate first time -> entry created
        Trade entry = pos.operate(0, nf.valueOf(100), nf.valueOf(10));
        assertNotNull(entry);
        assertEquals(TradeType.BUY, entry.getType());
        assertTrue(pos.isOpened());
        assertFalse(pos.isClosed());

        // operate second time -> exit created
        Trade exit = pos.operate(5, nf.valueOf(110), nf.valueOf(10));
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertTrue(pos.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position pos = new Position();
        pos.operate(3, nf.valueOf(100), nf.valueOf(10)); // creates entry at index 3
        // now try to close with an earlier index
        pos.operate(2, nf.valueOf(105), nf.valueOf(10));
    }

    /* ----------------------------------------------------------------------
     * 3. Profit / loss calculations on closed positions
     * ---------------------------------------------------------------------- */

    @Test
    public void testProfitForLongPosition() {
        // BUY @100 (10 units)  -> cost = 1000
        // SELL @110 (10 units) -> revenue = 1100
        Position pos = new Position();
        pos.operate(0, nf.valueOf(100), nf.valueOf(10));   // entry
        pos.operate(5, nf.valueOf(110), nf.valueOf(10));   // exit

        Num profit = pos.getProfit(); // no transaction cost (ZeroCostModel)
        assertEquals(100.0, profit.doubleValue(), 0.0001);
        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());
    }

    @Test
    public void testProfitForShortPosition() {
        // SELL @150 (5 units)  -> receive 750
        // BUY  @130 (5 units)  -> spend   650
        Position pos = new Position(TradeType.SELL);
        pos.operate(0, nf.valueOf(150), nf.valueOf(5));   // entry (short)
        pos.operate(4, nf.valueOf(130), nf.valueOf(5));   // exit (cover)

        Num profit = pos.getProfit();
        assertEquals(100.0, profit.doubleValue(), 0.0001);
        assertTrue(pos.hasProfit());
    }

    @Test
    public void testLossDetection() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(200), nf.valueOf(2));   // entry
        pos.operate(3, nf.valueOf(180), nf.valueOf(2));   // exit -> loss 40
        assertEquals(-40.0, pos.getProfit().doubleValue(), 0.0001);
        assertTrue(pos.hasLoss());
        assertFalse(pos.hasProfit());
    }

    @Test
    public void testProfitWhenPositionIsOpenReturnsZero() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(100), nf.valueOf(10));
        assertEquals(0.0, pos.getProfit().doubleValue(), 0.0);
        assertFalse(pos.hasProfit());
        assertFalse(pos.hasLoss());
    }

    /* ----------------------------------------------------------------------
     * 4. Gross profit / gross return calculations
     * ---------------------------------------------------------------------- */

    @Test
    public void testGrossProfitClosedLong() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(50), nf.valueOf(20));   // entry 1000
        pos.operate(2, nf.valueOf(55), nf.valueOf(20));   // exit   1100
        assertEquals(100.0, pos.getGrossProfit().doubleValue(), 0.0001);
    }

    @Test
    public void testGrossProfitClosedShort() {
        Position pos = new Position(TradeType.SELL);
        pos.operate(0, nf.valueOf(80), nf.valueOf(15));   // entry (short) receive 1200
        pos.operate(3, nf.valueOf(70), nf.valueOf(15));   // exit  spend 1050
        // gross profit for short is (entry - exit) = 150, but method negates for short
        assertEquals(150.0, pos.getGrossProfit().doubleValue(), 0.0001);
    }

    @Test
    public void testGrossReturnClosedLong() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(100), nf.valueOf(1));
        pos.operate(1, nf.valueOf(110), nf.valueOf(1));
        // return = exit/entry = 1.10
        assertEquals(1.10, pos.getGrossReturn().doubleValue(), 0.0001);
    }

    @Test
    public void testGrossReturnClosedShort() {
        Position pos = new Position(TradeType.SELL);
        pos.operate(0, nf.valueOf(150), nf.valueOf(1));
        pos.operate(1, nf.valueOf(130), nf.valueOf(1));
        // short return = ((exit/entry - 1) * -1) + 1
        // exit/entry = 130/150 = 0.866666...,  (0.8666-1) = -0.13333, negate -> 0.13333, +1 = 1.13333
        assertEquals(1.13333, pos.getGrossReturn().doubleValue(), 0.0001);
    }

    @Test
    public void testGrossReturnWithBarSeries() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(20), nf.valueOf(5));
        pos.operate(2, nf.valueOf(25), nf.valueOf(5));

        // create a dummy BarSeries that returns the same factory – we already have one
        Num grossReturn = pos.getGrossReturn(series);
        assertEquals(1.25, grossReturn.doubleValue(), 0.0001);
    }

    /* ----------------------------------------------------------------------
     * 5. Equality, hashCode and toString
     * ---------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position();
        Position p2 = new Position();

        // both new -> equal
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // after entry they diverge
        p1.operate(0, nf.valueOf(100), nf.valueOf(1));
        assertNotEquals(p1, p2);
        assertNotEquals(p1.hashCode(), p2.hashCode());

        // make p2 identical
        p2.operate(0, nf.valueOf(100), nf.valueOf(1));
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(10), nf.valueOf(2));
        pos.operate(1, nf.valueOf(12), nf.valueOf(2));
        String s = pos.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
    }

    /* ----------------------------------------------------------------------
     * 6. Position cost calculations (using ZeroCostModel – should be zero)
     * ---------------------------------------------------------------------- */

    @Test
    public void testPositionCostClosedIsZeroWithZeroCostModel() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(30), nf.valueOf(3));
        pos.operate(2, nf.valueOf(35), nf.valueOf(3));
        assertEquals(0.0, pos.getPositionCost().doubleValue(), 0.0);
    }

    @Test
    public void testPositionCostOpenUsesFinalIndex() {
        Position pos = new Position();
        pos.operate(0, nf.valueOf(40), nf.valueOf(4));
        // still opened – ask for cost up to index 5
        Num cost = pos.getPositionCost(5);
        assertEquals(0.0, cost.doubleValue(), 0.0);
    }
}
```
###Test END##