package org.ta4j.core;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Objects;

import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position}.
 */
public class PositionTest {

    /** Simple {@link Num} implementation based on {@link BigDecimal}. */
    private static final class SimpleNum implements Num {
        private final BigDecimal value;

        SimpleNum(Number n) {
            this.value = new BigDecimal(n.toString());
        }

        private SimpleNum(BigDecimal bd) {
            this.value = bd;
        }

        @Override
        public Num zero() {
            return new SimpleNum(BigDecimal.ZERO);
        }

        @Override
        public Num one() {
            return new SimpleNum(BigDecimal.ONE);
        }

        @Override
        public Num hundred() {
            return new SimpleNum(new BigDecimal(100));
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        @Override
        public Num plus(Num augend) {
            return new SimpleNum(this.value.add(toBigDecimal(augend)));
        }

        @Override
        public Num minus(Num subtrahend) {
            return new SimpleNum(this.value.subtract(toBigDecimal(subtrahend)));
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return new SimpleNum(this.value.multiply(toBigDecimal(multiplicand)));
        }

        @Override
        public Num dividedBy(Num divisor) {
            return new SimpleNum(this.value.divide(toBigDecimal(divisor), BigDecimal.ROUND_HALF_UP);
        }

        @Override
        public Num remainder(Num divisor) {
            return new SimpleNum(this.value.remainder(toBigDecimal(divisor)));
        }

        @Override
        public Num floor() {
            return new SimpleNum(this.value.setScale(0, BigDecimal.ROUND_FLOOR));
        }

        @Override
        public Num ceil() {
            return new SimpleNum(this.value.setScale(0, BigDecimal.ROUND_CEILING));
        }

        @Override
        public Num pow(int n) {
            return new SimpleNum(this.value.pow(n));
        }

        @Override
        public Num pow(Num n) {
            return new SimpleNum(this.value.pow(toBigDecimal(n).intValue()));
        }

        @Override
        public Num log() {
            return new SimpleNum(Math.log(this.value.doubleValue()));
        }

        @Override
        public Num sqrt() {
            return new SimpleNum(Math.sqrt(this.value.doubleValue()));
        }

        @Override
        public Num sqrt(int precision) {
            return sqrt();
        }

        @Override
        public Num abs() {
            return new SimpleNum(this.value.abs());
        }

        @Override
        public Num negate() {
            return new SimpleNum(this.value.negate());
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
            return value.compareTo(toBigDecimal(other)) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return value.compareTo(toBigDecimal(other)) > 0;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return value.compareTo(toBigDecimal(other)) >= 0;
        }

        @Override
        public boolean isLessThan(Num other) {
            return value.compareTo(toBigDecimal(other)) < 0;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return value.compareTo(toBigDecimal(other)) <= 0;
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
        public java.util.function.Function<Number, Num> function() {
            return n -> new SimpleNum(n);
        }

        @Override
        public Num numOf(Number value) {
            return new SimpleNum(value);
        }

        @Override
        public Num numOf(String value, int precision) {
            return new SimpleNum(new BigDecimal(value));
        }

        @Override
        public boolean isNaN() {
            return false;
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
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
        public int compareTo(Num o) {
            return this.value.compareTo(toBigDecimal(o));
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleNum)) return false;
            SimpleNum that = (SimpleNum) obj;
            return this.value.compareTo(that.value) == 0;
        }

        @Override
        public String toString() {
            return value.toPlainString();
        }

        private static BigDecimal toBigDecimal(Num n) {
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new BigDecimal(n.doubleValue());
        }
    }

    /** Simple {@link CostModel} that always returns a fixed {@link Num}. */
    private static final class FixedCostModel implements CostModel {
        private final Num fixed;

        FixedCostModel(Num fixed) {
            this.fixed = fixed;
        }

        @Override
        public Num calculate(Position position, int finalIndex) {
            return fixed;
        }

        @Override
        public Num calculate(Position position) {
            return fixed;
        }

        @Override
        public Num calculate(Num price, Num amount) {
            return price.multipliedBy(amount);
        }

        @Override
        public boolean equals(CostModel otherModel) {
            if (this == otherModel) return true;
            if (!(otherModel instanceof FixedCostModel)) return false;
            FixedCostModel that = (FixedCostModel) otherModel;
            return this.fixed.equals(that.fixed);
        }
    }

    /* Helper to create a SimpleNum */
    private static Num n(Number v) {
        return new SimpleNum(v);
    }

    @Test
    public void testDefaultConstructor() {
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
    public void testConstructorWithNullStartingType() {
        new Position(null);
    }

    @Test
    public void testConstructorWithEntryAndExitTrades() {
        Trade entry = Trade.buyAt(0, n(10), n(2));
        Trade exit = Trade.sellAt(5, n(12), n(2));
        Position p = new Position(entry, exit);
        assertFalse(p.isNew());
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithSameTypeTrades() {
        Trade entry = Trade.buyAt(0, n(10), n(1));
        Trade exit = Trade.buyAt(5, n(12), n(1));
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithMismatchedCostModel() {
        CostModel cm1 = new ZeroCostModel();
        CostModel cm2 = new FixedCostModel(n(1));
        Trade entry = Trade.buyAt(0, n(10), n(1), cm1);
        Trade exit = Trade.sellAt(5, n(12), n(1), cm1);
        new Position(entry, exit, cm2, new ZeroCostModel());
    }

    @Test
    public void testOperateLifecycle() {
        Position p = new Position();
        // first call creates entry
        Trade e = p.operate(0, n(10), n(1));
        assertNotNull(e);
        assertEquals(TradeType.BUY, e.getType());
        assertTrue(p.isOpened());

        // second call creates exit
        Trade x = p.operate(3, n(12), n(1));
        assertNotNull(x);
        assertEquals(TradeType.SELL, x.getType());
        assertTrue(p.isClosed());

        // calling operate after closed returns null
        assertNull(p.operate(5, n(13), n(1)));
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexBeforeEntry() {
        Position p = new Position();
        p.operate(2, n(10), n(1)); // entry at index 2
        p.operate(1, n(12), n(1)); // should throw
    }

    @Test
    public void testProfitAndLoss() {
        // profitable BUY position
        Position longPos = new Position();
        longPos.operate(0, n(10), n(2)); // entry
        longPos.operate(4, n(15), n(2)); // exit
        assertTrue(longPos.hasProfit());
        assertFalse(longPos.hasLoss());

        // losing SELL (short) position
        Trade entry = Trade.sellAt(0, n(20), n(1));
        Trade exit = Trade.buyAt(3, n(25), n(1));
        Position shortPos = new Position(entry, exit);
        assertFalse(shortPos.hasProfit());
        assertTrue(shortPos.hasLoss());
    }

    @Test
    public void testGrossProfitAndReturn() {
        // BUY position: entry 10, exit 14, amount 1
        Position p = new Position();
        p.operate(0, n(10), n(1));
        p.operate(2, n(14), n(1));

        Num grossProfit = p.getGrossProfit();
        assertEquals(n(4), grossProfit); // 14-10

        Num grossReturn = p.getGrossReturn();
        assertEquals(n(1.4), grossReturn); // 14/10 = 1.4
    }

    @Test
    public void testGrossProfitShortPosition() {
        // SELL position: entry 20, exit 15 (profit for short)
        Trade entry = Trade.sellAt(0, n(20), n(1));
        Trade exit = Trade.buyAt(3, n(15), n(1));
        Position p = new Position(entry, exit);
        Num grossProfit = p.getGrossProfit();
        // (entry value - exit value) = (20-15)=5, then negated because entry.isSell()
        assertEquals(n(5), grossProfit);
        // gross return for short: ((15/20 -1) * -1) + 1 = (0.75-1)*-1+1 = 0.25+1 = 1.25
        assertEquals(n(1.25), p.getGrossReturn());
    }

    @Test
    public void testPositionCostWithCustomCostModel() {
        Num fixedCost = n(3);
        CostModel custom = new FixedCostModel(fixedCost);
        Position p = new Position(TradeType.BUY, custom, new ZeroCostModel());

        // open entry only
        p.operate(0, n(10), n(1));
        // cost for opened position (should use calculate(Position, index))
        assertEquals(fixedCost, p.getPositionCost(5));

        // close position
        p.operate(2, n(12), n(1));
        // cost for closed position (no index)
        assertEquals(fixedCost, p.getPositionCost());
    }

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position();
        p1.operate(0, n(10), n(1));
        p1.operate(2, n(12), n(1));

        Position p2 = new Position();
        p2.operate(0, n(10), n(1));
        p2.operate(2, n(12), n(1));

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // different exit price -> not equal
        Position p3 = new Position();
        p3.operate(0, n(10), n(1));
        p3.operate(2, n(13), n(1));
        assertNotEquals(p1, p3);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position();
        p.operate(0, n(5), n(1));
        p.operate(1, n(7), n(1));
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(p.getEntry().toString()));
        assertTrue(s.contains(p.getExit().toString()));
    }
}
