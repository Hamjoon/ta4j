package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NaN;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link Position}.
 *
 * The tests cover:
 * <ul>
 *   <li>construction (default, with starting type, with trades)</li>
 *   <li>state transitions (new, opened, closed)</li>
 *   <li>operate() behaviour and illegal‑state handling</li>
 *   <li>equality / hashCode</li>
 *   <li>profit / loss calculations (gross & net)</li>
 *   <li>gross return for BUY and SELL positions</li>
 *   <li>cost model integration (ZeroCostModel)</li>
 *   <li>toString representation</li>
 * </ul>
 */
public class PositionTest {

    /** Simple {@link Num} implementation used only for the tests */
    private static class SimpleNum implements Num {
        private final BigDecimal value;
        private final SimpleNumFactory factory;

        SimpleNum(BigDecimal value, SimpleNumFactory factory) {
            this.value = value;
            this.factory = factory;
        }

        static SimpleNum of(double d, SimpleNumFactory factory) {
            return new SimpleNum(BigDecimal.valueOf(d), factory);
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public NumFactory getNumFactory() {
            return factory;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        @Override
        public Num plus(Num augend) {
            return of(this.value.add(((SimpleNum) augend).value), factory);
        }

        @Override
        public Num minus(Num subtrahend) {
            return of(this.value.subtract(((SimpleNum) subtrahend).value), factory);
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return of(this.value.multiply(((SimpleNum) multiplicand).value), factory);
        }

        @Override
        public Num dividedBy(Num divisor) {
            return of(this.value.divide(((SimpleNum) divisor).value, BigDecimal.ROUND_HALF_EVEN), factory);
        }

        @Override
        public Num remainder(Num divisor) {
            return of(this.value.remainder(((SimpleNum) divisor).value), factory);
        }

        @Override
        public Num floor() {
            return of(this.value.setScale(0, BigDecimal.ROUND_FLOOR), factory);
        }

        @Override
        public Num ceil() {
            return of(this.value.setScale(0, BigDecimal.ROUND_CEILING), factory);
        }

        @Override
        public Num pow(int n) {
            return of(this.value.pow(n), factory);
        }

        @Override
        public Num pow(Num n) {
            return of(this.value.pow(((SimpleNum) n).value.intValue()), factory);
        }

        @Override
        public Num log() {
            return of(Math.log(this.value.doubleValue()), factory);
        }

        @Override
        public Num exp() {
            return of(Math.exp(this.value.doubleValue()), factory);
        }

        @Override
        public Num sqrt() {
            return of(Math.sqrt(this.value.doubleValue()), factory);
        }

        @Override
        public Num sqrt(java.math.MathContext mathContext) {
            return of(new BigDecimal(Math.sqrt(this.value.doubleValue()), mathContext), factory);
        }

        @Override
        public Num abs() {
            return of(this.value.abs(), factory);
        }

        @Override
        public Num negate() {
            return of(this.value.negate(), factory);
        }

        @Override
        public boolean isZero() {
            return value.compareTo(BigDecimal.ZERO) == 0;
        }

        @Override
        public boolean isPositive() {
            return value.compareTo(BigDecimal.ZERO) > 0;
        }

        @Override
        public boolean isPositiveOrZero() {
            return value.compareTo(BigDecimal.ZERO) >= 0;
        }

        @Override
        public boolean isNegative() {
            return value.compareTo(BigDecimal.ZERO) < 0;
        }

        @Override
        public boolean isNegativeOrZero() {
            return value.compareTo(BigDecimal.ZERO) <= 0;
        }

        @Override
        public boolean isEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return value.compareTo(((SimpleNum) other).value) > 0;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) >= 0;
        }

        @Override
        public boolean isLessThan(Num other) {
            return value.compareTo(((SimpleNum) other).value) < 0;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) <= 0;
        }

        @Override
        public Num min(Num other) {
            return (isLessThan(other)) ? this : other;
        }

        @Override
        public Num max(Num other) {
            return (isGreaterThan(other)) ? this : other;
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public java.math.BigDecimal bigDecimalValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.toPlainString();
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && ((SimpleNum) obj).value.compareTo(this.value) == 0;
        }

        @Override
        public int compareTo(Num o) {
            return this.value.compareTo(((SimpleNum) o).value);
        }
    }

    /** Simple {@link NumFactory} implementation */
    private static class SimpleNumFactory implements NumFactory {
        SimpleNum zero() {
            return SimpleNum.of(0.0, this);
        }

        SimpleNum one() {
            return SimpleNum.of(1.0, this);
        }

        SimpleNum numOf(Number number) {
            return SimpleNum.of(number.doubleValue(), this);
        }

        // The real interface contains many more methods; only the ones used in the
        // tests are implemented. Any other call will throw an
        // UnsupportedOperationException.
        @Override public Num numOf(String s) { throw new UnsupportedOperationException(); }
        @Override public Num numOf(double value) { return numOf(Double.valueOf(value)); }
        @Override public Num numOf(int value) { return numOf(Integer.valueOf(value)); }
        @Override public Num numOf(long value) { return numOf(Long.valueOf(value)); }
        @Override public Num zero() { return zero(); }
        @Override public Num one() { return one(); }
    }

    /** Minimal {@link BarSeries} implementation sufficient for the tests */
    private static class SimpleBarSeries implements BarSeries {
        private final SimpleNumFactory factory = new SimpleNumFactory();

        @Override public NumFactory numFactory() { return factory; }

        // All other methods are irrelevant for the current test suite.
        @Override public BarBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { return "SimpleSeries"; }
        @Override public Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return 0; }
        @Override public List<Bar> getBarData() { return Collections.emptyList(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return 0; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(Bar bar) {}
        @Override public void addBar(Bar bar, boolean replace) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(Number price) {}
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { return ""; }
        @Override public boolean isEmpty() { return true; }
    }

    private SimpleNumFactory nf;
    private SimpleBarSeries series;
    private CostModel zeroCostModel;

    @Before
    public void setUp() {
        nf = new SimpleNumFactory();
        series = new SimpleBarSeries();
        zeroCostModel = new ZeroCostModel();
    }

    private SimpleNum n(double v) {
        return SimpleNum.of(v, nf);
    }

    @Test
    public void testDefaultConstructor() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testStartingTypeConstructor() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStartingType() {
        new Position(null);
    }

    @Test
    public void testOperateCreatesEntryAndExit() {
        Position p = new Position();

        // first operate -> entry trade
        Trade entry = p.operate(5, n(10.0), n(2.0));
        assertNotNull(entry);
        assertEquals(5, entry.getIndex());
        assertEquals(TradeType.BUY, entry.getType());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // second operate -> exit trade
        Trade exit = p.operate(8, n(12.0), n(2.0));
        assertNotNull(exit);
        assertEquals(8, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType());

        assertTrue(p.isClosed());
        assertFalse(p.isOpened());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position p = new Position();
        p.operate(10, n(10), n(1)); // entry at index 10
        p.operate(5, n(12), n(1));  // should fail
    }

    @Test
    public void testEqualityAndHashCode() {
        Trade entry1 = Trade.buyAt(1, n(10), n(1), zeroCostModel);
        Trade exit1   = Trade.sellAt(2, n(12), n(1), zeroCostModel);
        Position pos1 = new Position(entry1, exit1);

        Trade entry2 = Trade.buyAt(1, n(10), n(1), zeroCostModel);
        Trade exit2   = Trade.sellAt(2, n(12), n(1), zeroCostModel);
        Position pos2 = new Position(entry2, exit2);

        assertEquals(pos1, pos2);
        assertEquals(pos1.hashCode(), pos2.hashCode());

        // change exit price -> not equal
        Trade exit3 = Trade.sellAt(2, n(13), n(1), zeroCostModel);
        Position pos3 = new Position(entry1, exit3);
        assertNotEquals(pos1, pos3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTradeConstructorRejectsSameType() {
        Trade buy = Trade.buyAt(0, n(10), n(1), zeroCostModel);
        Trade anotherBuy = Trade.buyAt(1, n(12), n(1), zeroCostModel);
        new Position(buy, anotherBuy);
    }

    @Test
    public void testProfitAndReturnForBuyPosition() {
        Trade entry = Trade.buyAt(0, n(10), n(2), zeroCostModel);
        Trade exit  = Trade.sellAt(5, n(12), n(2), zeroCostModel);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        // Gross profit = (12*2) - (10*2) = 4
        assertEquals(n(4.0), pos.getGrossProfit());

        // Net profit = gross profit - transaction cost (zero) - holding cost (zero) = 4
        assertEquals(n(4.0), pos.getProfit());

        // Gross return = exit / entry = 12 / 10 = 1.2
        assertEquals(n(1.2), pos.getGrossReturn());

        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());
    }

    @Test
    public void testProfitAndReturnForSellPosition() {
        Trade entry = Trade.sellAt(0, n(12), n(2), zeroCostModel); // short at 12
        Trade exit  = Trade.buyAt(5, n(10), n(2), zeroCostModel);  // cover at 10
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        // Gross profit = -( (10*2) - (12*2) ) = 4
        assertEquals(n(4.0), pos.getGrossProfit());

        // Net profit = 4 (no costs)
        assertEquals(n(4.0), pos.getProfit());

        // Gross return for short = ((exit/entry - 1) * -1) + 1
        // = ((10/12 - 1) * -1) + 1 = ( -0.166666... * -1) + 1 = 1.166666...
        Num expectedReturn = n(10.0).dividedBy(n(12.0));
        expectedReturn = expectedReturn.minus(n(1.0)).negate().plus(n(1.0));
        assertEquals(expectedReturn, pos.getGrossReturn());

        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());
    }

    @Test
    public void testHasLossForNegativeProfit() {
        Trade entry = Trade.buyAt(0, n(15), n(1), zeroCostModel);
        Trade exit  = Trade.sellAt(3, n(10), n(1), zeroCostModel);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        assertEquals(n(-5.0), pos.getProfit());
        assertFalse(pos.hasProfit());
        assertTrue(pos.hasLoss());
    }

    @Test
    public void testGetPositionCostIsZeroWithZeroCostModel() {
        Trade entry = Trade.buyAt(0, n(10), n(1), zeroCostModel);
        Trade exit  = Trade.sellAt(2, n(12), n(1), zeroCostModel);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        assertEquals(n(0.0), pos.getPositionCost());
        assertEquals(n(0.0), pos.getPositionCost(10));
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0, n(10), n(1), zeroCostModel);
        Trade exit  = Trade.sellAt(1, n(12), n(1), zeroCostModel);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        String s = pos.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    @Test
    public void testOperateWithoutPriceUsesNaN() {
        Position p = new Position();
        Trade entry = p.operate(0); // price & amount = NaN
        assertNotNull(entry);
        assertEquals(TradeType.BUY, entry.getType());

        Trade exit = p.operate(1);
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());

        // profit with NaN values should be zero (no arithmetic can be performed)
        assertEquals(n(0.0), p.getProfit());
        assertEquals(n(0.0), p.getGrossProfit());
    }
}
