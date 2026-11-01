package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>The suite follows the steps requested in the prompt:
 * <ul>
 *   <li>All public methods (including constructors) are listed.</li>
 *   <li>For each method a basic test is provided.</li>
 *   <li>Edge‑case and exception scenarios are identified and tested.</li>
 *   <li>All tests are merged into a single file {@code PositionTest.java}.</li>
 * </ul>
 *
 * <p>Only the APIs that appear in the supplied source code or in the
 * dependency signatures are used.  For the purpose of the tests a tiny
 * concrete implementation of {@link Num} and {@link NumFactory} is provided
 * ( {@code SimpleNum} / {@code SimpleNumFactory} ).  A minimal {@link CostModel}
 * implementation that always returns zero is also supplied – this is equivalent
 * to {@link ZeroCostModel} but avoids the need for the library’s internal
 * factories.
 */
public class PositionTest {

    /* ---------------------------------------------------------------------- */
    /* Helper classes – very small concrete implementations of library types. */
    /* ---------------------------------------------------------------------- */

    /** Simple immutable {@link Num} implementation based on {@link java.math.BigDecimal}. */
    private static final class SimpleNum implements Num {

        private final java.math.BigDecimal value;

        SimpleNum(java.math.BigDecimal v) {
            this.value = v;
        }

        SimpleNum(double v) {
            this(java.math.BigDecimal.valueOf(v));
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public NumFactory getNumFactory() {
            return SimpleNumFactory.INSTANCE;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        /* Arithmetic operations – only the ones used by Position are implemented. */
        @Override public Num plus(Num augend)   { return new SimpleNum(value.add(toBigDecimal(augend))); }
        @Override public Num minus(Num subtrahend){ return new SimpleNum(value.subtract(toBigDecimal(subtrahend))); }
        @Override public Num multipliedBy(Num multiplicand){ return new SimpleNum(value.multiply(toBigDecimal(multiplicand))); }
        @Override public Num dividedBy(Num divisor){ return new SimpleNum(value.divide(toBigDecimal(divisor), java.math.MathContext.DECIMAL128)); }
        @Override public Num negate(){ return new SimpleNum(value.negate()); }

        @Override public boolean isZero(){ return value.compareTo(java.math.BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive(){ return value.compareTo(java.math.BigDecimal.ZERO) > 0; }
        @Override public boolean isNegative(){ return value.compareTo(java.math.BigDecimal.ZERO) < 0; }
        @Override public boolean isPositiveOrZero(){ return value.compareTo(java.math.BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegativeOrZero(){ return value.compareTo(java.math.BigDecimal.ZERO) <= 0; }

        @Override public boolean isEqual(Num other){ return value.compareTo(toBigDecimal(other)) == 0; }
        @Override public boolean isGreaterThan(Num other){ return value.compareTo(toBigDecimal(other)) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other){ return value.compareTo(toBigDecimal(other)) >= 0; }
        @Override public boolean isLessThan(Num other){ return value.compareTo(toBigDecimal(other)) < 0; }
        @Override public boolean isLessThanOrEqual(Num other){ return value.compareTo(toBigDecimal(other)) <= 0; }

        @Override public int compareTo(Num o){ return value.compareTo(toBigDecimal(o)); }

        @Override public java.math.BigDecimal bigDecimalValue(){ return value; }

        @Override public int intValue(){ return value.intValue(); }
        @Override public long longValue(){ return value.longValue(); }
        @Override public float floatValue(){ return value.floatValue(); }
        @Override public double doubleValue(){ return value.doubleValue(); }

        @Override public String toString(){ return value.toPlainString(); }
        @Override public int hashCode(){ return value.hashCode(); }
        @Override public boolean equals(Object obj){
            return (obj instanceof Num) && isEqual((Num) obj);
        }

        /* ------------------------------------------------------------------ */
        /* Unused methods – provide default behaviour that does not affect tests */
        /* ------------------------------------------------------------------ */
        @Override public Num remainder(Num divisor){ throw new UnsupportedOperationException(); }
        @Override public Num floor(){ throw new UnsupportedOperationException(); }
        @Override public Num ceil(){ throw new UnsupportedOperationException(); }
        @Override public Num pow(int n){ throw new UnsupportedOperationException(); }
        @Override public Num pow(Num n){ throw new UnsupportedOperationException(); }
        @Override public Num log(){ throw new UnsupportedOperationException(); }
        @Override public Num exp(){ throw new UnsupportedOperationException(); }
        @Override public Num sqrt(){ throw new UnsupportedOperationException(); }
        @Override public Num sqrt(java.math.MathContext mc){ throw new UnsupportedOperationException(); }
        @Override public Num abs(){ throw new UnsupportedOperationException(); }
        @Override public Num min(Num other){ throw new UnsupportedOperationException(); }
        @Override public Num max(Num other){ throw new UnsupportedOperationException(); }
        @Override public boolean isNaN(){ return false; }

        private static java.math.BigDecimal toBigDecimal(Num n){
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new java.math.BigDecimal(n.toString());
        }
    }

    /** Factory used by {@link SimpleNum}. */
    private static final class SimpleNumFactory implements NumFactory {

        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();

        private SimpleNumFactory(){}

        public Num zero(){ return new SimpleNum(0.0); }
        public Num one(){ return new SimpleNum(1.0); }
        public Num valueOf(double v){ return new SimpleNum(v); }
        public Num valueOf(java.math.BigDecimal v){ return new SimpleNum(v); }
    }

    /** Minimal cost model that always returns zero (identical to {@link ZeroCostModel}). */
    private static final class ZeroCost implements CostModel {
        private static final SimpleNum ZERO = SimpleNumFactory.INSTANCE.zero();

        @Override public Num calculate(Position position, int finalIndex){ return ZERO; }
        @Override public Num calculate(Position position){ return ZERO; }
        @Override public Num calculate(Num price, Num amount){ return ZERO; }
        @Override public boolean equals(CostModel other){ return other instanceof ZeroCost; }
    }

    /* ---------------------------------------------------------------------- */
    /* Test fixtures */
    /* ---------------------------------------------------------------------- */

    private CostModel zeroCostModel;
    private Trade buyTrade;
    private Trade sellTrade;

    @Before
    public void setUp() {
        zeroCostModel = new ZeroCost();

        // price = 10, amount = 5  →  value = 50
        SimpleNum price = SimpleNumFactory.INSTANCE.valueOf(10.0);
        SimpleNum amount = SimpleNumFactory.INSTANCE.valueOf(5.0);

        // Use static factory methods of Trade (the signatures are part of the API)
        buyTrade  = Trade.buyAt(0, price, amount, zeroCostModel);
        sellTrade = Trade.sellAt(1, price, amount, zeroCostModel);
    }

    /* ---------------------------------------------------------------------- */
    /* 1 – Constructors */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructorCreatesBuyPosition() {
        Position p = new Position(); // default BUY
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(TradeType.SELL, zeroCostModel, zeroCostModel);
        assertEquals(TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null, zeroCostModel, zeroCostModel);
    }

    @Test
    public void testConstructorWithEntryAndExit() {
        Position p = new Position(buyTrade, sellTrade);
        assertEquals(buyTrade, p.getEntry());
        assertEquals(sellTrade, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsSameTradeTypes() {
        // both BUY → illegal
        Trade anotherBuy = Trade.buyAt(2, SimpleNumFactory.INSTANCE.valueOf(9.0),
                                      SimpleNumFactory.INSTANCE.valueOf(2.0), zeroCostModel);
        new Position(buyTrade, anotherBuy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsMismatchedCostModel() {
        // entry uses zeroCostModel, but we pass a different (non‑zero) model
        CostModel nonZero = new CostModel() {
            private final SimpleNum ONE = SimpleNumFactory.INSTANCE.one();
            @Override public Num calculate(Position p, int i){ return ONE; }
            @Override public Num calculate(Position p){ return ONE; }
            @Override public Num calculate(Num price, Num amount){ return ONE; }
            @Override public boolean equals(CostModel other){ return false; }
        };
        new Position(buyTrade, sellTrade, nonZero, zeroCostModel);
    }

    /* ---------------------------------------------------------------------- */
    /* 2 – Getters */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testGetEntryAndExit() {
        Position p = new Position(buyTrade, sellTrade);
        assertSame(buyTrade, p.getEntry());
        assertSame(sellTrade, p.getExit());
    }

    @Test
    public void testGetStartingType() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    /* ---------------------------------------------------------------------- */
    /* 3 – State predicates */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testIsNewWhenNoTrades() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());
    }

    @Test
    public void testIsOpenedAfterEntryOnly() {
        Position p = new Position();
        p.operate(0, SimpleNumFactory.INSTANCE.valueOf(10.0), SimpleNumFactory.INSTANCE.valueOf(1.0));
        assertTrue(p.isOpened());
        assertFalse(p.isNew());
        assertFalse(p.isClosed());
    }

    @Test
    public void testIsClosedAfterEntryAndExit() {
        Position p = new Position();
        p.operate(0, SimpleNumFactory.INSTANCE.valueOf(10.0), SimpleNumFactory.INSTANCE.valueOf(1.0));
        p.operate(1, SimpleNumFactory.INSTANCE.valueOf(12.0), SimpleNumFactory.INSTANCE.valueOf(1.0));
        assertTrue(p.isClosed());
        assertFalse(p.isOpened());
        assertFalse(p.isNew());
    }

    /* ---------------------------------------------------------------------- */
    /* 4 – operate(...) */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryWhenNew() {
        Position p = new Position();
        Trade entry = p.operate(3);
        assertNotNull(entry);
        assertEquals(3, entry.getIndex());
        assertEquals(TradeType.BUY, entry.getType());
        assertSame(entry, p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void testOperateCreatesExitWhenOpened() {
        Position p = new Position();
        p.operate(0); // entry
        Trade exit = p.operate(5);
        assertNotNull(exit);
        assertEquals(5, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType());
        assertSame(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position p = new Position();
        p.operate(10); // entry at 10
        p.operate(5);  // should throw
    }

    @Test
    public void testOperateOnClosedPositionReturnsNull() {
        Position p = new Position(buyTrade, sellTrade);
        assertNull(p.operate(42));
    }

    /* ---------------------------------------------------------------------- */
    /* 5 – Equality & hashCode */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position(buyTrade, sellTrade);
        Position p2 = new Position(buyTrade, sellTrade);
        Position p3 = new Position(sellTrade, buyTrade); // reversed – not equal

        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());

        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("some string"));
    }

    /* ---------------------------------------------------------------------- */
    /* 6 – Profit / loss related methods */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testProfitClosedLongPositionPositive() {
        // entry price 10, amount 5 → value 50
        // exit price 12, amount 5 → value 60
        Trade entry = Trade.buyAt(0, SimpleNumFactory.INSTANCE.valueOf(10.0),
                                  SimpleNumFactory.INSTANCE.valueOf(5.0), zeroCostModel);
        Trade exit  = Trade.sellAt(1, SimpleNumFactory.INSTANCE.valueOf(12.0),
                                  SimpleNumFactory.INSTANCE.valueOf(5.0), zeroCostModel);
        Position p = new Position(entry, exit);
        Num profit = p.getProfit(); // (60 - 50) - 0 = 10
        assertTrue(profit.isPositive());
        assertEquals(10.0, profit.doubleValue(), 1e-12);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitClosedShortPositionNegative() {
        // Short (sell first, buy later)
        Trade entry = Trade.sellAt(0, SimpleNumFactory.INSTANCE.valueOf(15.0),
                                   SimpleNumFactory.INSTANCE.valueOf(4.0), zeroCostModel);
        Trade exit  = Trade.buyAt(1, SimpleNumFactory.INSTANCE.valueOf(13.0),
                                   SimpleNumFactory.INSTANCE.valueOf(4.0), zeroCostModel);
        Position p = new Position(entry, exit);
        // entry value = 60, exit value = 52 → gross = -8, sign flips → 8 profit (short profit)
        Num profit = p.getProfit();
        assertTrue(profit.isPositive());
        assertEquals(8.0, profit.doubleValue(), 1e-12);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitWhenOpenedReturnsZero() {
        Position p = new Position();
        p.operate(0, SimpleNumFactory.INSTANCE.valueOf(10.0), SimpleNumFactory.INSTANCE.valueOf(1.0));
        Num profit = p.getProfit(); // opened → zero
        assertTrue(profit.isZero());
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGetGrossProfitClosedLong() {
        Trade entry = Trade.buyAt(0, SimpleNumFactory.INSTANCE.valueOf(8.0),
                                  SimpleNumFactory.INSTANCE.valueOf(3.0), zeroCostModel);
        Trade exit  = Trade.sellAt(1, SimpleNumFactory.INSTANCE.valueOf(11.0),
                                   SimpleNumFactory.INSTANCE.valueOf(3.0), zeroCostModel);
        Position p = new Position(entry, exit);
        Num gross = p.getGrossProfit(); // (33 - 24) = 9
        assertEquals(9.0, gross.doubleValue(), 1e-12);
    }

    @Test
    public void testGetGrossProfitOpenedUsesCurrentPrice() {
        Position p = new Position();
        p.operate(0, SimpleNumFactory.INSTANCE.valueOf(7.0), SimpleNumFactory.INSTANCE.valueOf(2.0)); // entry
        Num gross = p.getGrossProfit(SimpleNumFactory.INSTANCE.valueOf(9.0));
        // (2 * 9) - (2 * 7) = 4
        assertEquals(4.0, gross.doubleValue(), 1e-12);
    }

    @Test
    public void testGetGrossReturnBuyPosition() {
        Trade entry = Trade.buyAt(0, SimpleNumFactory.INSTANCE.valueOf(5.0),
                                  SimpleNumFactory.INSTANCE.valueOf(1.0), zeroCostModel);
        Trade exit  = Trade.sellAt(1, SimpleNumFactory.INSTANCE.valueOf(6.0),
                                   SimpleNumFactory.INSTANCE.valueOf(1.0), zeroCostModel);
        Position p = new Position(entry, exit);
        Num grossReturn = p.getGrossReturn(); // 6 / 5 = 1.2
        assertEquals(1.2, grossReturn.doubleValue(), 1e-12);
    }

    @Test
    public void testGetGrossReturnSellPosition() {
        Trade entry = Trade.sellAt(0, SimpleNumFactory.INSTANCE.valueOf(10.0),
                                   SimpleNumFactory.INSTANCE.valueOf(2.0), zeroCostModel);
        Trade exit  = Trade.buyAt(1, SimpleNumFactory.INSTANCE.valueOf(8.0),
                                   SimpleNumFactory.INSTANCE.valueOf(2.0), zeroCostModel);
        Position p = new Position(entry, exit);
        // For short: ((exit/entry - 1) * -1) + 1 = (0.8 - 1)*-1 + 1 = 0.2 + 1 = 1.2
        Num grossReturn = p.getGrossReturn(); // should be 1.2
        assertEquals(1.2, grossReturn.doubleValue(), 1e-12);
    }

    /* ---------------------------------------------------------------------- */
    /* 7 – Cost related methods (always zero with ZeroCostModel) */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testPositionCostClosedIsZero() {
        Position p = new Position(buyTrade, sellTrade);
        Num cost = p.getPositionCost();
        assertTrue(cost.isZero());
    }

    @Test
    public void testHoldingCostClosedIsZero() {
        Position p = new Position(buyTrade, sellTrade);
        Num cost = p.getHoldingCost();
        assertTrue(cost.isZero());
    }

    @Test
    public void testPositionCostOpenIsZero() {
        Position p = new Position();
        p.operate(0, SimpleNumFactory.INSTANCE.valueOf(10.0), SimpleNumFactory.INSTANCE.valueOf(1.0));
        Num cost = p.getPositionCost(5);
        assertTrue(cost.isZero());
    }

    /* ---------------------------------------------------------------------- */
    /* 8 – toString */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position(buyTrade, sellTrade);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains(buyTrade.toString()));
        assertTrue(s.contains(sellTrade.toString()));
    }

    /* ---------------------------------------------------------------------- */
    /* 9 – Edge cases for equals with null trades */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testEqualsWhenBothPositionsAreNew() {
        Position p1 = new Position();
        Position p2 = new Position();
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsWhenOneHasEntryOnly() {
        Position p1 = new Position();
        p1.operate(0, SimpleNumFactory.INSTANCE.valueOf(10.0), SimpleNumFactory.INSTANCE.valueOf(1.0));
        Position p2 = new Position();
        assertFalse(p1.equals(p2));
    }

    /* ---------------------------------------------------------------------- */
    /* End of test suite */
    /* ---------------------------------------------------------------------- */
}
