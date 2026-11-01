###Test START##
package org.ta4j.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PositionTest {

    /** Simple Num implementation used for the tests */
    private static class SimpleNum implements Num {

        private final double value;
        private static final SimpleNumFactory FACTORY = new SimpleNumFactory();

        SimpleNum(double value) {
            this.value = value;
        }

        /** Factory for SimpleNum */
        static SimpleNumFactory factory() {
            return FACTORY;
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public NumFactory getNumFactory() {
            return FACTORY;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        @Override
        public Num plus(Num augend) {
            return new SimpleNum(this.value + ((SimpleNum) augend).value);
        }

        @Override
        public Num minus(Num subtrahend) {
            return new SimpleNum(this.value - ((SimpleNum) subtrahend).value);
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return new SimpleNum(this.value * ((SimpleNum) multiplicand).value);
        }

        @Override
        public Num dividedBy(Num divisor) {
            return new SimpleNum(this.value / ((SimpleNum) divisor).value);
        }

        @Override
        public Num remainder(Num divisor) {
            return new SimpleNum(this.value % ((SimpleNum) divisor).value);
        }

        @Override
        public Num floor() {
            return new SimpleNum(Math.floor(this.value));
        }

        @Override
        public Num ceil() {
            return new SimpleNum(Math.ceil(this.value));
        }

        @Override
        public Num pow(int n) {
            return new SimpleNum(Math.pow(this.value, n));
        }

        @Override
        public Num pow(Num n) {
            return new SimpleNum(Math.pow(this.value, ((SimpleNum) n).value));
        }

        @Override
        public Num log() {
            return new SimpleNum(Math.log(this.value));
        }

        @Override
        public Num exp() {
            return new SimpleNum(Math.exp(this.value));
        }

        @Override
        public Num sqrt() {
            return new SimpleNum(Math.sqrt(this.value));
        }

        @Override
        public Num sqrt(java.math.MathContext mathContext) {
            return new SimpleNum(Math.sqrt(this.value));
        }

        @Override
        public Num abs() {
            return new SimpleNum(Math.abs(this.value));
        }

        @Override
        public Num negate() {
            return new SimpleNum(-this.value);
        }

        @Override
        public boolean isZero() {
            return Double.compare(this.value, 0.0) == 0;
        }

        @Override
        public boolean isPositive() {
            return this.value > 0;
        }

        @Override
        public boolean isPositiveOrZero() {
            return this.value >= 0;
        }

        @Override
        public boolean isNegative() {
            return this.value < 0;
        }

        @Override
        public boolean isNegativeOrZero() {
            return this.value <= 0;
        }

        @Override
        public boolean isEqual(Num other) {
            return Double.compare(this.value, ((SimpleNum) other).value) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return this.value > ((SimpleNum) other).value;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return this.value >= ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThan(Num other) {
            return this.value < ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return this.value <= ((SimpleNum) other).value;
        }

        @Override
        public Num min(Num other) {
            return this.isLessThan(other) ? this : other;
        }

        @Override
        public Num max(Num other) {
            return this.isGreaterThan(other) ? this : other;
        }

        @Override
        public int intValue() {
            return (int) value;
        }

        @Override
        public long longValue() {
            return (long) value;
        }

        @Override
        public float floatValue() {
            return (float) value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public java.math.BigDecimal bigDecimalValue() {
            return java.math.BigDecimal.valueOf(value);
        }

        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        // default methods from the interface are inherited unchanged
    }

    /** Factory used by SimpleNum */
    private static class SimpleNumFactory implements NumFactory {

        @Override
        public Num zero() {
            return new SimpleNum(0.0);
        }

        @Override
        public Num one() {
            return new SimpleNum(1.0);
        }

        // The real library contains many more methods; they are not needed for the tests
    }

    /** Minimal BarSeries implementation – only the methods required for compilation */
    private static class SimpleBarSeries implements BarSeries {

        private final SimpleNumFactory factory = SimpleNumFactory.class.cast(SimpleNum.factory());

        @Override
        public NumFactory numFactory() {
            return factory;
        }

        @Override
        public BarBuilder barBuilder() {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public String getName() {
            return "SimpleBarSeries";
        }

        @Override
        public Bar getBar(int i) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public int getBarCount() {
            return 0;
        }

        @Override
        public java.util.List<Bar> getBarData() {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public int getBeginIndex() {
            return 0;
        }

        @Override
        public int getEndIndex() {
            return 0;
        }

        @Override
        public int getMaximumBarCount() {
            return 0;
        }

        @Override
        public void setMaximumBarCount(int maximumBarCount) {
            // no‑op
        }

        @Override
        public int getRemovedBarsCount() {
            return 0;
        }

        @Override
        public void addBar(Bar bar) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void addBar(Bar bar, boolean replace) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void addTrade(Number tradeVolume, Number tradePrice) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void addTrade(Num tradeVolume, Num tradePrice) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void addPrice(Num price) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public void addPrice(Number price) {
            throw new UnsupportedOperationException("Not needed for tests");
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            throw new UnsupportedOperationException("Not needed for tests");
        }
    }

    private SimpleNum price(double v) {
        return new SimpleNum(v);
    }

    private SimpleNum amount(double v) {
        return new SimpleNum(v);
    }

    private CostModel zeroCostModel;

    @Before
    public void setUp() {
        zeroCostModel = new ZeroCostModel();
    }

    @Test
    public void testNewPositionState() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testOperateEntryAndExit() {
        Position p = new Position();
        Trade entry = p.operate(0, price(10.0), amount(2.0));
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        assertEquals(TradeType.BUY, entry.getType());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        Trade exit = p.operate(5, price(12.0), amount(2.0));
        assertNotNull(exit);
        assertEquals(5, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType());
        assertTrue(p.isClosed());
        assertFalse(p.isOpened());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position p = new Position();
        p.operate(3, price(10.0), amount(1.0)); // entry at index 3
        // attempt to close before entry index
        p.operate(2, price(11.0), amount(1.0));
    }

    @Test
    public void testProfitClosedLong() {
        // Long (BUY then SELL)
        Trade entry = Trade.buyAt(0, price(10.0), amount(5.0), zeroCostModel);
        Trade exit = Trade.sellAt(4, price(12.0), amount(5.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);

        Num profit = p.getProfit();
        assertEquals(10.0, profit.doubleValue(), 0.000001);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitClosedShort() {
        // Short (SELL then BUY)
        Trade entry = Trade.sellAt(0, price(15.0), amount(3.0), zeroCostModel);
        Trade exit = Trade.buyAt(4, price(13.0), amount(3.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);

        Num profit = p.getProfit();
        assertEquals(6.0, profit.doubleValue(), 0.000001);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGrossReturnLong() {
        Trade entry = Trade.buyAt(0, price(100.0), amount(1.0), zeroCostModel);
        Trade exit = Trade.sellAt(2, price(110.0), amount(1.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Num grossReturn = p.getGrossReturn();
        assertEquals(1.10, grossReturn.doubleValue(), 0.00001);
    }

    @Test
    public void testGrossReturnShort() {
        // Short with profit (price drops)
        Trade entry = Trade.sellAt(0, price(100.0), amount(1.0), zeroCostModel);
        Trade exit = Trade.buyAt(2, price(90.0), amount(1.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Num grossReturn = p.getGrossReturn();
        assertEquals(1.10, grossReturn.doubleValue(), 0.00001);
    }

    @Test
    public void testHasProfitAndLossOnOpenPosition() {
        Position p = new Position();
        p.operate(0, price(10.0), amount(1.0)); // only entry, still opened
        Num profit = p.getProfit();
        assertEquals(0.0, profit.doubleValue(), 0.000001);
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = Trade.buyAt(0, price(10.0), amount(2.0), zeroCostModel);
        Trade exit1 = Trade.sellAt(1, price(12.0), amount(2.0), zeroCostModel);
        Position pos1 = new Position(entry1, exit1, zeroCostModel, zeroCostModel);

        Trade entry2 = Trade.buyAt(0, price(10.0), amount(2.0), zeroCostModel);
        Trade exit2 = Trade.sellAt(1, price(12.0), amount(2.0), zeroCostModel);
        Position pos2 = new Position(entry2, exit2, zeroCostModel, zeroCostModel);

        assertEquals(pos1, pos2);
        assertEquals(pos1.hashCode(), pos2.hashCode());

        // Different exit price -> not equal
        Trade exitDiff = Trade.sellAt(1, price(13.0), amount(2.0), zeroCostModel);
        Position pos3 = new Position(entry1, exitDiff, zeroCostModel, zeroCostModel);
        assertNotEquals(pos1, pos3);
    }

    @Test
    public void testGetPositionCostZero() {
        Position p = new Position();
        p.operate(0, price(10.0), amount(1.0));
        p.operate(5, price(12.0), amount(1.0));
        Num cost = p.getPositionCost();
        assertEquals(0.0, cost.doubleValue(), 0.000001);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0, price(5.0), amount(1.0), zeroCostModel);
        Trade exit = Trade.sellAt(2, price(7.0), amount(1.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        String txt = p.toString();
        assertTrue(txt.contains(entry.toString()));
        assertTrue(txt.contains(exit.toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingType() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEntryExitSameType() {
        Trade entry = Trade.buyAt(0, price(10.0), amount(1.0), zeroCostModel);
        Trade exit = Trade.buyAt(1, price(12.0), amount(1.0), zeroCostModel);
        new Position(entry, exit, zeroCostModel, zeroCostModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModel() {
        CostModel otherCost = new CostModel() {
            @Override public Num calculate(Position position, int finalIndex) { return SimpleNumFactory.class.cast(SimpleNum.factory()).zero(); }
            @Override public Num calculate(Position position) { return SimpleNumFactory.class.cast(SimpleNum.factory()).zero(); }
            @Override public Num calculate(Num price, Num amount) { return SimpleNumFactory.class.cast(SimpleNum.factory()).zero(); }
            @Override public boolean equals(CostModel otherModel) { return false; }
        };
        Trade entry = Trade.buyAt(0, price(10.0), amount(1.0), zeroCostModel);
        Trade exit = Trade.sellAt(1, price(12.0), amount(1.0), otherCost);
        new Position(entry, exit, zeroCostModel, zeroCostModel);
    }

    @Test
    public void testGetGrossProfitOpenReturnsZero() {
        Position p = new Position();
        p.operate(0, price(10.0), amount(2.0));
        Num gross = p.getGrossProfit();
        assertEquals(0.0, gross.doubleValue(), 0.000001);
    }

    @Test
    public void testGetGrossProfitClosedLong() {
        Trade entry = Trade.buyAt(0, price(20.0), amount(3.0), zeroCostModel);
        Trade exit = Trade.sellAt(3, price(25.0), amount(3.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Num gross = p.getGrossProfit();
        // (25*3) - (20*3) = 15
        assertEquals(15.0, gross.doubleValue(), 0.000001);
    }

    @Test
    public void testGetGrossProfitClosedShort() {
        Trade entry = Trade.sellAt(0, price(30.0), amount(2.0), zeroCostModel);
        Trade exit = Trade.buyAt(4, price(28.0), amount(2.0), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Num gross = p.getGrossProfit();
        // entry value 60, exit value 56, exit - entry = -4, negated = 4
        assertEquals(4.0, gross.doubleValue(), 0.000001);
    }

    @Test
    public void testGetProfitWithFinalParametersOnOpenPosition() {
        Position p = new Position();
        p.operate(0, price(10.0), amount(2.0)); // entry
        // open position, compute profit as if exited at index 5, price 11
        Num profit = p.getProfit(5, price(11.0));
        // Expected gross profit: (11*2) - (10*2) = 2
        // No cost, so profit = 2
        assertEquals(2.0, profit.doubleValue(), 0.000001);
    }
}
###Test END##