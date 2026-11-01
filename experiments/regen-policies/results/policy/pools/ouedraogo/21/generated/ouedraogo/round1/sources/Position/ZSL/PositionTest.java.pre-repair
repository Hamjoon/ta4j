package org.ta4j.core;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Comprehensive JUnit 4 tests for {@link Position}.
 * <p>
 * The tests use lightweight concrete implementations of {@link Num},
 * {@link NumFactory} and {@link BarSeries} defined as inner classes.
 * Only APIs that appear in the supplied source code or in the listed
 * dependency signatures are used.
 */
public class PositionTest {

    private static final CostModel ZERO_COST = new ZeroCostModel();

    private SimpleNumFactory factory;

    @Before
    public void setUp() {
        factory = new SimpleNumFactory();
    }

    /** Helper to create a {@link Num} from a double value. */
    private Num n(double d) {
        return new SimpleNum(d, factory);
    }

    /** Helper to create a BUY trade at a given index, price and amount. */
    private Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, n(price), n(amount));
    }

    /** Helper to create a SELL trade at a given index, price and amount. */
    private Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, n(price), n(amount));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingType() {
        new Position(null);
    }

    @Test
    public void testStateTransitions() {
        Position p = new Position(); // default BUY
        Assert.assertTrue(p.isNew());

        Trade entry = p.operate(0, n(10), n(1));
        Assert.assertNotNull(entry);
        Assert.assertTrue(p.isOpened());
        Assert.assertFalse(p.isClosed());

        Trade exit = p.operate(5, n(12), n(1));
        Assert.assertNotNull(exit);
        Assert.assertTrue(p.isClosed());
        Assert.assertFalse(p.isOpened());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateIndexLessThanEntryThrows() {
        Position p = new Position();
        p.operate(10, n(10), n(1)); // entry at index 10
        // now try to close with a lower index
        p.operate(5, n(12), n(1));
    }

    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = buy(0, 10, 1);
        Trade exit1 = sell(5, 12, 1);
        Position p1 = new Position(entry1, exit1);

        Trade entry2 = buy(0, 10, 1);
        Trade exit2 = sell(5, 12, 1);
        Position p2 = new Position(entry2, exit2);

        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());

        // different exit price -> not equal
        Trade exitDiff = sell(5, 13, 1);
        Position p3 = new Position(entry1, exitDiff);
        Assert.assertNotEquals(p1, p3);
    }

    @Test
    public void testProfitClosedLongPosition() {
        Trade entry = buy(0, 10, 1);
        Trade exit = sell(5, 12, 1);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);

        Num expectedProfit = n(2); // (12 - 10) * 1
        Assert.assertEquals(expectedProfit, p.getProfit());
        Assert.assertEquals(expectedProfit, p.getGrossProfit());
        Assert.assertTrue(p.hasProfit());
        Assert.assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitClosedShortPosition() {
        Trade entry = sell(0, 15, 1);
        Trade exit = buy(5, 10, 1);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);

        Num expectedProfit = n(5); // (15 - 10) * 1
        Assert.assertEquals(expectedProfit, p.getProfit());
        Assert.assertEquals(expectedProfit, p.getGrossProfit());
        Assert.assertTrue(p.hasProfit());
        Assert.assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitOpenPosition() {
        Position p = new Position();
        p.operate(0, n(10), n(1)); // entry BUY at 10

        // No exit yet, profit should be zero()
        Assert.assertEquals(p.getProfit(), p.getProfit(10, n(11)));
        Assert.assertEquals(p.getProfit(), p.getProfit(10, n(11)));

        // Unrealised profit using final price 11
        Num expected = n(1); // (11 - 10) * 1
        Assert.assertEquals(expected, p.getProfit(10, n(11)));
    }

    @Test
    public void testGrossReturnLong() {
        Trade entry = buy(0, 10, 1);
        Trade exit = sell(5, 12, 1);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);

        Num expected = n(1.2); // 12 / 10
        Assert.assertEquals(expected, p.getGrossReturn());
    }

    @Test
    public void testGrossReturnShortLoss() {
        // Short entry (sell) at 10, exit (buy) at 12 => loss
        Trade entry = sell(0, 10, 1);
        Trade exit = buy(5, 12, 1);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);

        // Expected gross return = ((12/10 - 1) * -1) + 1 = 0.8
        Num expected = n(0.8);
        Assert.assertEquals(expected, p.getGrossReturn());
    }

    @Test
    public void testHasProfitAndLoss() {
        // profit case
        Trade entry1 = buy(0, 10, 1);
        Trade exit1 = sell(5, 13, 1);
        Position profitPos = new Position(entry1, exit1, ZERO_COST, ZERO_COST);
        Assert.assertTrue(profitPos.hasProfit());
        Assert.assertFalse(profitPos.hasLoss());

        // loss case
        Trade entry2 = buy(0, 10, 1);
        Trade exit2 = sell(5, 8, 1);
        Position lossPos = new Position(entry2, exit2, ZERO_COST, ZERO_COST);
        Assert.assertFalse(lossPos.hasProfit());
        Assert.assertTrue(lossPos.hasLoss());
    }

    @Test
    public void testPositionCostZeroWhenUsingZeroCostModel() {
        Trade entry = buy(0, 10, 1);
        Trade exit = sell(5, 12, 1);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);
        Assert.assertEquals(p.getPositionCost(), p.zero());
        Assert.assertEquals(p.getPositionCost(5), p.zero());
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, 10, 1);
        Trade exit = sell(5, 12, 1);
        Position p = new Position(entry, exit, ZERO_COST, ZERO_COST);
        String s = p.toString().toLowerCase();
        Assert.assertTrue(s.contains("entry"));
        Assert.assertTrue(s.contains("exit"));
    }

    // -----------------------------------------------------------------------
    // Minimal concrete implementations required for the tests
    // -----------------------------------------------------------------------

    /** Simple implementation of {@link NumFactory} providing zero() and one(). */
    private static class SimpleNumFactory {
        Num zero() {
            return new SimpleNum(0.0, this);
        }

        Num one() {
            return new SimpleNum(1.0, this);
        }

        Num of(double d) {
            return new SimpleNum(d, this);
        }
    }

    /** Very small but functional implementation of {@link Num}. */
    private static class SimpleNum implements Num {
        private final double value;
        private final SimpleNumFactory factory;

        SimpleNum(double value, SimpleNumFactory factory) {
            this.value = value;
            this.factory = factory;
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public SimpleNumFactory getNumFactory() {
            return factory;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        private SimpleNum newNum(double v) {
            return new SimpleNum(v, factory);
        }

        @Override
        public Num plus(Num augend) {
            return newNum(this.value + ((SimpleNum) augend).value);
        }

        @Override
        public Num minus(Num subtrahend) {
            return newNum(this.value - ((SimpleNum) subtrahend).value);
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return newNum(this.value * ((SimpleNum) multiplicand).value);
        }

        @Override
        public Num dividedBy(Num divisor) {
            return newNum(this.value / ((SimpleNum) divisor).value);
        }

        @Override
        public Num remainder(Num divisor) {
            return newNum(this.value % ((SimpleNum) divisor).value);
        }

        @Override
        public Num floor() {
            return newNum(Math.floor(this.value));
        }

        @Override
        public Num ceil() {
            return newNum(Math.ceil(this.value));
        }

        @Override
        public Num pow(int n) {
            return newNum(Math.pow(this.value, n));
        }

        @Override
        public Num pow(Num n) {
            return newNum(Math.pow(this.value, ((SimpleNum) n).value));
        }

        @Override
        public Num log() {
            return newNum(Math.log(this.value));
        }

        @Override
        public Num sqrt() {
            return newNum(Math.sqrt(this.value));
        }

        @Override
        public Num sqrt(int precision) {
            return sqrt();
        }

        @Override
        public Num abs() {
            return newNum(Math.abs(this.value));
        }

        @Override
        public Num negate() {
            return newNum(-this.value);
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
        public boolean isNaN() {
            return Double.isNaN(this.value);
        }

        @Override
        public int intValue() {
            return (int) this.value;
        }

        @Override
        public long longValue() {
            return (long) this.value;
        }

        @Override
        public float floatValue() {
            return (float) this.value;
        }

        @Override
        public double doubleValue() {
            return this.value;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            return BigDecimal.valueOf(this.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleNum)) return false;
            SimpleNum other = (SimpleNum) obj;
            return Double.compare(this.value, other.value) == 0;
        }

        @Override
        public int compareTo(Num o) {
            return Double.compare(this.value, ((SimpleNum) o).value);
        }
    }

    /** Minimal stub for {@link BarSeries}. Not used by the tests that require price
     *  from a trade, because trades are created with explicit price values. */
    private static class StubBarSeries implements BarSeries {
        private final SimpleNumFactory factory;

        StubBarSeries(SimpleNumFactory factory) {
            this.factory = factory;
        }

        @Override
        public SimpleNumFactory numFactory() {
            return factory;
        }

        // All other methods throw UnsupportedOperationException because they are
        // not needed for the current test suite.
        @Override public BarSeries barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { throw new UnsupportedOperationException(); }
        @Override public Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public Bar getFirstBar() { throw new UnsupportedOperationException(); }
        @Override public Bar getLastBar() { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { throw new UnsupportedOperationException(); }
        @Override public boolean isEmpty() { throw new UnsupportedOperationException(); }
        @Override public java.util.List<Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public int getBeginIndex() { throw new UnsupportedOperationException(); }
        @Override public int getEndIndex() { throw new UnsupportedOperationException(); }
        @Override public String getSeriesPeriodDescription() { throw new UnsupportedOperationException(); }
        @Override public int getMaximumBarCount() { throw new UnsupportedOperationException(); }
        @Override public void setMaximumBarCount(int maximumBarCount) { throw new UnsupportedOperationException(); }
        @Override public int getRemovedBarsCount() { throw new UnsupportedOperationException(); }
        @Override public void addBar(Bar bar) { throw new UnsupportedOperationException(); }
        @Override public void addBar(Bar bar, boolean replace) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Number tradeVolume, Number tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price) { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Number price) { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
    }
}
