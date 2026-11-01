package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.NumFactory;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>The tests cover:
 * <ul>
 *   <li>All public methods (signatures listed in the first section)</li>
 *   <li>Basic functional checks</li>
 *   <li>Edge‑case and exception scenarios</li>
 *   <li>Equality / hash‑code contract</li>
 * </ul>
 *
 * <p>Only APIs that appear in the provided source or in the listed dependency
 * signatures are used.  For the {@link Num} interface a tiny concrete
 * implementation {@code SimpleNum} is provided inside the test class – this
 * satisfies the contract without pulling in any external library code.
 */
public class PositionTest {

    /* --------------------------------------------------------------------- *
     *  Minimal Num implementation (used only for the tests)                *
     * --------------------------------------------------------------------- */
    private static class SimpleNum implements Num {
        private final java.math.BigDecimal value;

        SimpleNum(java.math.BigDecimal v) {
            this.value = v;
        }

        SimpleNum(double v) {
            this(java.math.BigDecimal.valueOf(v));
        }

        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return SimpleNumFactory.INSTANCE; }
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num plus(Num augend) { return new SimpleNum(this.value.add(((SimpleNum) augend).value)); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value.subtract(((SimpleNum) subtrahend).value)); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value.multiply(((SimpleNum) multiplicand).value)); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value.divide(((SimpleNum) divisor).value, java.math.MathContext.DECIMAL128)); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(this.value.remainder(((SimpleNum) divisor).value)); }
        @Override public Num floor() { return new SimpleNum(this.value.setScale(0, java.math.RoundingMode.FLOOR)); }
        @Override public Num ceil() { return new SimpleNum(this.value.setScale(0, java.math.RoundingMode.CEILING)); }
        @Override public Num pow(int n) { return new SimpleNum(this.value.pow(n)); }
        @Override public Num pow(Num n) { return new SimpleNum(this.value.pow(n.intValue())); }
        @Override public Num log() { return new SimpleNum(Math.log(this.value.doubleValue())); }
        @Override public Num sqrt() { return new SimpleNum(Math.sqrt(this.value.doubleValue())); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs() { return new SimpleNum(this.value.abs()); }
        @Override public Num negate() { return new SimpleNum(this.value.negate()); }
        @Override public boolean isZero() { return this.value.compareTo(java.math.BigDecimal.ZERO) == 0; }
        @Override public boolean isPositive() { return this.value.compareTo(java.math.BigDecimal.ZERO) > 0; }
        @Override public boolean isPositiveOrZero() { return this.value.compareTo(java.math.BigDecimal.ZERO) >= 0; }
        @Override public boolean isNegative() { return this.value.compareTo(java.math.BigDecimal.ZERO) < 0; }
        @Override public boolean isNegativeOrZero() { return this.value.compareTo(java.math.BigDecimal.ZERO) <= 0; }
        @Override public boolean isEqual(Num other) { return this.value.compareTo(((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value.compareTo(((SimpleNum) other).value) > 0; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value.compareTo(((SimpleNum) other).value) >= 0; }
        @Override public boolean isLessThan(Num other) { return this.value.compareTo(((SimpleNum) other).value) < 0; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value.compareTo(((SimpleNum) other).value) <= 0; }
        @Override public Num min(Num other) { return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return this.isGreaterThan(other) ? this : other; }
        @Override public boolean isNaN() { return false; }
        @Override public int intValue() { return value.intValue(); }
        @Override public long longValue() { return value.longValue(); }
        @Override public float floatValue() { return value.floatValue(); }
        @Override public double doubleValue() { return value.doubleValue(); }
        @Override public java.math.BigDecimal bigDecimalValue() { return value; }
        @Override public int hashCode() { return value.hashCode(); }
        @Override public String toString() { return value.toString(); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && this.value.compareTo(((SimpleNum) obj).value) == 0;
        }
    }

    private static class SimpleNumFactory implements Num.NumFactory {
        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();
        @Override public Num zero() { return new SimpleNum(0.0); }
        @Override public Num one() { return new SimpleNum(1.0); }
        @Override public Num of(double v) { return new SimpleNum(v); }
        @Override public Num of(java.math.BigDecimal v) { return new SimpleNum(v); }
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods to create Trades                                      *
     * --------------------------------------------------------------------- */
    private Trade buy(int index, double price, double amount) {
        return Trade.buyAt(index, new SimpleNum(price), new SimpleNum(amount));
    }

    private Trade sell(int index, double price, double amount) {
        return Trade.sellAt(index, new SimpleNum(price), new SimpleNum(amount));
    }

    /* --------------------------------------------------------------------- *
     *  Public methods of Position (extracted)                               *
     * --------------------------------------------------------------------- *
     * public Position()
     * public Position(TradeType startingType)
     * public Position(TradeType startingType, CostModel transactionCostModel,
     *                 CostModel holdingCostModel)
     * public Position(Trade entry, Trade exit)
     * public Position(Trade entry, Trade exit, CostModel transactionCostModel,
     *                 CostModel holdingCostModel)
     *
     * public Trade getEntry()
     * public Trade getExit()
     * public boolean equals(Object obj)
     * public int hashCode()
     * public Trade operate(int index)
     * public Trade operate(int index, Num price, Num amount)
     * public boolean isClosed()
     * public boolean isOpened()
     * public boolean isNew()
     * public boolean hasProfit()
     * public boolean hasLoss()
     * public Num getProfit()
     * public Num getProfit(int finalIndex, Num finalPrice)
     * public Num getGrossProfit()
     * public Num getGrossProfit(Num finalPrice)
     * public Num getGrossReturn()
     * public Num getGrossReturn(Num finalPrice)
     * public Num getGrossReturn(BarSeries barSeries)
     * public Num getGrossReturn(Num entryPrice, Num exitPrice)
     * public Num getPositionCost(int finalIndex)
     * public Num getPositionCost()
     * public Num getHoldingCost()
     * public Num getHoldingCost(int finalIndex)
     * public TradeType getStartingType()
     * private Num zero()          // not public, accessed indirectly
     * public String toString()
     */

    /* --------------------------------------------------------------------- *
     *  Test fixtures                                                         *
     * --------------------------------------------------------------------- */
    private Position emptyPosition;               // created with default constructor
    private Position buyPosition;                 // starting type BUY
    private Position sellPosition;                // starting type SELL
    private final CostModel zeroCost = new ZeroCostModel();

    @Before
    public void setUp() {
        emptyPosition = new Position();                     // default BUY
        buyPosition = new Position(TradeType.BUY);
        sellPosition = new Position(TradeType.SELL);
    }

    /* --------------------------------------------------------------------- *
     *  Constructor tests                                                     *
     * --------------------------------------------------------------------- */
    @Test
    public void testDefaultConstructorStartsWithBuy() {
        assertEquals(TradeType.BUY, emptyPosition.getStartingType());
        assertTrue(emptyPosition.isNew());
    }

    @Test
    public void testConstructorWithStartingType() {
        assertEquals(TradeType.SELL, sellPosition.getStartingType());
        assertTrue(sellPosition.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingTypeThrows() {
        new Position(null);
    }

    @Test
    public void testConstructorWithTradesCreatesPosition() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit  = sell(5, 12.0, 1.0);
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        assertEquals(entry, pos.getEntry());
        assertEquals(exit, pos.getExit());
        assertFalse(pos.isNew());
        assertTrue(pos.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTradesSameTypeThrows() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit  = buy(5, 12.0, 1.0);   // also BUY → illegal
        new Position(entry, exit, zeroCost, zeroCost);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCostModelThrows() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit  = sell(5, 12.0, 1.0);
        // use a different transaction cost model for the exit trade
        CostModel otherCost = new ZeroCostModel(); // still zero, but we’ll pass a different instance
        new Position(entry, exit, otherCost, zeroCost);
    }

    /* --------------------------------------------------------------------- *
     *  State‑query tests (isNew, isOpened, isClosed)                        *
     * --------------------------------------------------------------------- */
    @Test
    public void testInitialStateIsNew() {
        assertTrue(emptyPosition.isNew());
        assertFalse(emptyPosition.isOpened());
        assertFalse(emptyPosition.isClosed());
    }

    @Test
    public void testStateAfterEntryTrade() {
        Trade entry = emptyPosition.operate(0, new SimpleNum(10.0), new SimpleNum(1.0));
        assertNotNull(entry);
        assertTrue(emptyPosition.isOpened());
        assertFalse(emptyPosition.isClosed());
        assertEquals(entry, emptyPosition.getEntry());
        assertNull(emptyPosition.getExit());
    }

    @Test
    public void testStateAfterExitTrade() {
        // first create entry
        emptyPosition.operate(0, new SimpleNum(10.0), new SimpleNum(1.0));
        // then exit
        Trade exit = emptyPosition.operate(3, new SimpleNum(12.0), new SimpleNum(1.0));
        assertNotNull(exit);
        assertTrue(emptyPosition.isClosed());
        assertEquals(exit, emptyPosition.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexLessThanEntryThrows() {
        emptyPosition.operate(0, new SimpleNum(10.0), new SimpleNum(1.0)); // entry at 0
        emptyPosition.operate(-1, new SimpleNum(9.0), new SimpleNum(1.0)); // invalid index
    }

    /* --------------------------------------------------------------------- *
     *  Equality & hashCode                                                   *
     * --------------------------------------------------------------------- */
    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = buy(0, 10.0, 1.0);
        Trade exit1  = sell(5, 12.0, 1.0);
        Position p1 = new Position(entry1, exit1, zeroCost, zeroCost);
        Position p2 = new Position(entry1, exit1, zeroCost, zeroCost);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // different exit trade -> not equal
        Trade exit2 = sell(5, 13.0, 1.0);
        Position p3 = new Position(entry1, exit2, zeroCost, zeroCost);
        assertNotEquals(p1, p3);
    }

    /* --------------------------------------------------------------------- *
     *  Profit / loss calculations                                            *
     * --------------------------------------------------------------------- */
    @Test
    public void testProfitClosedLongPosition() {
        Trade entry = buy(0, 10.0, 2.0);   // cost = 20
        Trade exit  = sell(4, 15.0, 2.0);  // value = 30
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        Num profit = pos.getProfit();    // 30 - 20 = 10
        assertTrue(profit.isPositive());
        assertEquals(new SimpleNum(10.0), profit);
        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());
    }

    @Test
    public void testProfitClosedShortPosition() {
        Trade entry = sell(0, 15.0, 2.0); // value = 30 (short entry)
        Trade exit  = buy(4, 10.0, 2.0);  // cost = 20
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        Num profit = pos.getProfit();   // (entry - exit) = 10
        assertEquals(new SimpleNum(10.0), profit);
        assertTrue(pos.hasProfit());
    }

    @Test
    public void testProfitOpenedPositionIsZero() {
        emptyPosition.operate(0, new SimpleNum(10.0), new SimpleNum(1.0));
        Num profit = emptyPosition.getProfit();
        assertTrue(profit.isZero());
    }

    @Test
    public void testHasLossWhenProfitIsNegative() {
        Trade entry = buy(0, 20.0, 1.0);   // cost = 20
        Trade exit  = sell(3, 15.0, 1.0);  // value = 15
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        assertTrue(pos.getProfit().isNegative());
        assertTrue(pos.hasLoss());
        assertFalse(pos.hasProfit());
    }

    @Test
    public void testGetProfitWithFinalDataForOpenPosition() {
        emptyPosition.operate(0, new SimpleNum(10.0), new SimpleNum(2.0)); // entry
        // open position, calculate profit as if we exited at index 5 price 12
        Num profit = emptyPosition.getProfit(5, new SimpleNum(12.0));
        // Expected gross profit: 2 * 12 - (2 * 10) = 4
        assertEquals(new SimpleNum(4.0), profit);
    }

    /* --------------------------------------------------------------------- *
     *  Gross profit / return tests                                           *
     * --------------------------------------------------------------------- */
    @Test
    public void testGrossProfitClosedLong() {
        Trade entry = buy(0, 5.0, 3.0);   // value = 15
        Trade exit  = sell(2, 7.0, 3.0);  // value = 21
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        assertEquals(new SimpleNum(6.0), pos.getGrossProfit());
    }

    @Test
    public void testGrossProfitClosedShort() {
        Trade entry = sell(0, 7.0, 3.0);  // value = 21 (short)
        Trade exit  = buy(2, 5.0, 3.0);   // cost = 15
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        // Gross profit for short is -(exit - entry) = 6
        assertEquals(new SimpleNum(6.0), pos.getGrossProfit());
    }

    @Test
    public void testGrossReturnLong() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit  = sell(3, 12.0, 1.0);
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        Num grossReturn = pos.getGrossReturn();
        // (12 / 10) = 1.2
        assertEquals(new SimpleNum(1.2), grossReturn);
    }

    @Test
    public void testGrossReturnShort() {
        Trade entry = sell(0, 12.0, 1.0);
        Trade exit  = buy(3, 10.0, 1.0);
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        // For short: ((exit/entry - 1) * -1) + 1 = ((10/12 -1)*-1)+1 = ( -0.1666 * -1)+1 = 1.1666...
        Num expected = new SimpleNum(10.0).dividedBy(new SimpleNum(12.0));
        expected = expected.minus(new SimpleNum(1.0)).negate().plus(new SimpleNum(1.0));
        assertEquals(expected, pos.getGrossReturn());
    }

    @Test
    public void testGrossReturnWithExplicitPrices() {
        Position pos = new Position(TradeType.BUY);
        Num entryPrice = new SimpleNum(8.0);
        Num exitPrice  = new SimpleNum(10.0);
        Num result = pos.getGrossReturn(entryPrice, exitPrice);
        assertEquals(new SimpleNum(1.25), result); // 10/8 = 1.25
    }

    @Test
    public void testGrossReturnShortWithExplicitPrices() {
        Position pos = new Position(TradeType.SELL);
        Num entryPrice = new SimpleNum(10.0);
        Num exitPrice  = new SimpleNum(8.0);
        // Expected: ((8/10 -1)*-1)+1 = 1.2
        Num expected = new SimpleNum(8.0).dividedBy(new SimpleNum(10.0));
        expected = expected.minus(new SimpleNum(1.0)).negate().plus(new SimpleNum(1.0));
        assertEquals(expected, pos.getGrossReturn(entryPrice, exitPrice));
    }

    /* --------------------------------------------------------------------- *
     *  Cost model related tests                                              *
     * --------------------------------------------------------------------- */
    @Test
    public void testPositionCostZeroWhenUsingZeroCostModel() {
        Trade entry = buy(0, 10.0, 1.0);
        Trade exit  = sell(5, 12.0, 1.0);
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        assertTrue(pos.getPositionCost().isZero());
        assertTrue(pos.getHoldingCost().isZero());
    }

    /* --------------------------------------------------------------------- *
     *  toString test                                                         *
     * --------------------------------------------------------------------- */
    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = buy(0, 5.0, 1.0);
        Trade exit  = sell(2, 7.0, 1.0);
        Position pos = new Position(entry, exit, zeroCost, zeroCost);
        String s = pos.toString();
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    /* --------------------------------------------------------------------- *
     *  Additional edge cases                                                  *
     * --------------------------------------------------------------------- */
    @Test
    public void testOperateWithoutPriceAmountUsesNaN() {
        // The operate(int) overload forwards NaN for price/amount.
        // NaN values are accepted by the Trade constructor; profit should still be zero while opened.
        Position p = new Position();
        Trade t = p.operate(0);
        assertNotNull(t);
        assertEquals(TradeType.BUY, t.getType());
        assertTrue(p.isOpened());
        // profit of an opened position is zero regardless of NaN values
        assertTrue(p.getProfit().isZero());
    }

    @Test
    public void testHashCodeConsistentWhenOnlyEntryIsSet() {
        Position p = new Position();
        Trade entry = p.operate(0, new SimpleNum(10.0), new SimpleNum(1.0));
        int hash1 = p.hashCode();
        // after setting exit, hash should change
        p.operate(2, new SimpleNum(12.0), new SimpleNum(1.0));
        int hash2 = p.hashCode();
        assertNotEquals(hash1, hash2);
    }
}
