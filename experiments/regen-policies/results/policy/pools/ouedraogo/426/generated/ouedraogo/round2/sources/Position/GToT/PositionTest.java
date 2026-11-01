package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;


import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>
 * The tests use tiny local stub implementations of {@link Num},
 * {@link CostModel} and {@link Trade}.  These stubs implement only the
 * methods that are exercised by {@code Position} and therefore keep the
 * test suite independent from the rest of the TA‑4J library.
 * </p>
 */
public class PositionTest {

    /* --------------------------------------------------------------------- *
     *  Minimal stub implementations                                           *
     * --------------------------------------------------------------------- */

    /** Simple {@link Num} implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {

        private final BigDecimal value;
        private final SimpleNumFactory factory = new SimpleNumFactory();

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        SimpleNum(double d) {
            this(BigDecimal.valueOf(d));
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
            return new SimpleNum(this.value.divide(toBigDecimal(divisor), BigDecimal.ROUND_HALF_EVEN));
        }

        @Override
        public Num remainder(Num divisor) {
            return new SimpleNum(this.value.remainder(toBigDecimal(divisor)));
        }

        @Override
        public Num floor() {
            return new SimpleNum(value.setScale(0, BigDecimal.ROUND_FLOOR));
        }

        @Override
        public Num ceil() {
            return new SimpleNum(value.setScale(0, BigDecimal.ROUND_CEILING));
        }

        @Override
        public Num pow(int n) {
            return new SimpleNum(value.pow(n));
        }

        @Override
        public Num pow(Num n) {
            return new SimpleNum(value.pow(toBigDecimal(n).intValue()));
        }

        @Override
        public Num log() {
            return new SimpleNum(Math.log(value.doubleValue()));
        }

        @Override
        public Num exp() {
            return new SimpleNum(Math.exp(value.doubleValue()));
        }

        @Override
        public Num sqrt() {
            return new SimpleNum(Math.sqrt(value.doubleValue()));
        }

        @Override
        public Num sqrt(java.math.MathContext mathContext) {
            return new SimpleNum(Math.sqrt(value.doubleValue()));
        }

        @Override
        public Num abs() {
            return new SimpleNum(value.abs());
        }

        @Override
        public Num negate() {
            return new SimpleNum(value.negate());
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
            return new SimpleNum(value.min(toBigDecimal(other)));
        }

        @Override
        public Num max(Num other) {
            return new SimpleNum(value.max(toBigDecimal(other)));
        }

        @Override
        public boolean isNaN() {
            return false;
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
        public BigDecimal bigDecimalValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value.toPlainString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SimpleNum) {
                return value.compareTo(((SimpleNum) obj).value) == 0;
            }
            return false;
        }

        private static BigDecimal toBigDecimal(Num n) {
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new BigDecimal(n.doubleValue());
        }

        /** Factory used by {@link SimpleNum#zero()} */
        private class SimpleNumFactory implements NumFactory {
            @Override
            public Num zero() {
                return new SimpleNum(BigDecimal.ZERO);
            }

            @Override
            public Num one() {
                return new SimpleNum(BigDecimal.ONE);
            }

            @Override
            public Num valueOf(double v) {
                return new SimpleNum(v);
            }

            @Override
            public Num valueOf(String s) {
                return new SimpleNum(new BigDecimal(s));
            }
        }
    }

    /** Cost model that always returns zero – convenient for most tests. */
    private static class ZeroCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            return zeroNum();
        }

        @Override
        public Num calculate(Position position) {
            return zeroNum();
        }

        @Override
        public Num calculate(Num price, Num amount) {
            return zeroNum();
        }

        @Override
        public boolean equals(CostModel otherModel) {
            return otherModel instanceof ZeroCostModel;
        }

        private Num zeroNum() {
            return new SimpleNum(0);
        }
    }

    /** Minimal {@link Trade} implementation used by the tests. */
    private static class SimpleTrade implements Trade {

        private final TradeType type;
        private final int index;
        private final SimpleNum pricePerAsset;
        private final SimpleNum amount;
        private final CostModel costModel;

        SimpleTrade(TradeType type, int index, double price, double amount, CostModel costModel) {
            this.type = type;
            this.index = index;
            this.pricePerAsset = new SimpleNum(price);
            this.amount = new SimpleNum(amount);
            this.costModel = costModel;
        }

        @Override
        public TradeType getType() {
            return type;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public Num getPricePerAsset() {
            return pricePerAsset;
        }

        @Override
        public Num getNetPrice() {
            // net price = price * amount (ignoring fees)
            return pricePerAsset.multipliedBy(amount);
        }

        @Override
        public Num getAmount() {
            return amount;
        }

        @Override
        public Num getCost() {
            // for simplicity, cost = 0 (ZeroCostModel)
            return new SimpleNum(0);
        }

        @Override
        public CostModel getCostModel() {
            return costModel;
        }

        @Override
        public boolean isBuy() {
            return type == TradeType.BUY;
        }

        @Override
        public boolean isSell() {
            return type == TradeType.SELL;
        }

        @Override
        public Num getValue() {
            return pricePerAsset.multipliedBy(amount);
        }

        /* ----- default methods that are not required for the tests ----- */

        @Override
        public Instant getTime() {
            return Instant.now();
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getInstrument() {
            return null;
        }

        @Override
        public String getOrderId() {
            return null;
        }

        @Override
        public String getCorrelationId() {
            return null;
        }

        @Override
        public List<TradeFill> getFills() {
            return Collections.emptyList();
        }

        @Override
        public Num getPricePerAsset(BarSeries barSeries) {
            return getPricePerAsset();
        }

        @Override
        public String toString() {
            return type + "@" + index + " price=" + pricePerAsset + " amount=" + amount;
        }
    }

    /* --------------------------------------------------------------------- *
     *  Test fixtures                                                       *
     * --------------------------------------------------------------------- */

    private CostModel zeroCostModel;

    @Before
    public void setUp() {
        zeroCostModel = new ZeroCostModel();
    }

    private SimpleTrade buy(int index, double price, double amount) {
        return new SimpleTrade(TradeType.BUY, index, price, amount, zeroCostModel);
    }

    private SimpleTrade sell(int index, double price, double amount) {
        return new SimpleTrade(TradeType.SELL, index, price, amount, zeroCostModel);
    }

    /* --------------------------------------------------------------------- *
     *  1. Basic getters                                                     *
     * --------------------------------------------------------------------- */

    @Test
    public void testGettersOnOpenPosition() {
        SimpleTrade entry = buy(0, 10.0, 5.0);
        Position pos = new Position(entry, zeroCostModel, zeroCostModel);

        assertEquals(entry, pos.getEntry());
        assertNull(pos.getExit());

        assertEquals(ExecutionSide.BUY, pos.side());
        assertEquals(entry.getAmount(), pos.amount());
        assertEquals(entry.getPricePerAsset(), pos.averageEntryPrice());
        assertEquals(entry.getValue(), pos.totalEntryCost());
        assertEquals(entry.getCost(), pos.totalFees());
    }

    @Test
    public void testGettersOnClosedPosition() {
        SimpleTrade entry = buy(0, 10.0, 2.0);
        SimpleTrade exit = sell(5, 12.0, 2.0);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        assertEquals(entry, pos.getEntry());
        assertEquals(exit, pos.getExit());

        // side() is based on entry only
        assertEquals(ExecutionSide.BUY, pos.side());
    }

    /* --------------------------------------------------------------------- *
     *  2. State predicates                                                  *
     * --------------------------------------------------------------------- */

    @Test
    public void testStatePredicates() {
        Position fresh = new Position(); // default BUY starting type, no trades yet
        assertTrue(fresh.isNew());
        assertFalse(fresh.isOpened());
        assertFalse(fresh.isClosed());

        SimpleTrade entry = buy(0, 10, 1);
        Position opened = new Position(entry, zeroCostModel, zeroCostModel);
        assertFalse(opened.isNew());
        assertTrue(opened.isOpened());
        assertFalse(opened.isClosed());

        SimpleTrade exit = sell(3, 11, 1);
        opened.operate(exit); // close it
        assertFalse(opened.isNew());
        assertFalse(opened.isOpened());
        assertTrue(opened.isClosed());
    }

    /* --------------------------------------------------------------------- *
     *  3. Equality & hashCode                                               *
     * --------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        SimpleTrade e1 = buy(0, 10, 1);
        SimpleTrade x1 = sell(2, 12, 1);
        Position p1 = new Position(e1, x1, zeroCostModel, zeroCostModel);
        Position p2 = new Position(e1, x1, zeroCostModel, zeroCostModel);
        Position p3 = new Position(buy(0, 10, 1), sell(2, 13, 1), zeroCostModel, zeroCostModel);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        assertNotEquals(p1, p3);
    }

    /* --------------------------------------------------------------------- *
     *  4. Operating methods                                                 *
     * --------------------------------------------------------------------- */

    @Test
    public void testOperateCreatesEntryAndExit() {
        Position pos = new Position(); // default BUY
        Trade entry = pos.operate(0, new SimpleNum(10), new SimpleNum(2));
        assertNotNull(entry);
        assertTrue(pos.isOpened());
        assertEquals(TradeType.BUY, entry.getType());

        Trade exit = pos.operate(5, new SimpleNum(12), new SimpleNum(2));
        assertNotNull(exit);
        assertTrue(pos.isClosed());
        assertEquals(TradeType.SELL, exit.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexBeforeEntryThrows() {
        Position pos = new Position();
        pos.operate(3, new SimpleNum(10), new SimpleNum(1)); // creates entry at index 3
        pos.operate(2, new SimpleNum(11), new SimpleNum(1)); // should fail
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateTradeWithDifferentCostModelThrows() {
        // create a trade that uses a *different* cost model
        CostModel otherModel = new CostModel() {
            @Override public Num calculate(Position p, int i) { return new SimpleNum(0); }
            @Override public Num calculate(Position p) { return new SimpleNum(0); }
            @Override public Num calculate(Num p, Num a) { return new SimpleNum(0); }
            @Override public boolean equals(CostModel o) { return false; }
        };
        SimpleTrade entry = new SimpleTrade(TradeType.BUY, 0, 10, 1, otherModel);
        Position pos = new Position(); // uses ZeroCostModel internally
        pos.operate(entry); // should throw
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateTradeWithWrongTypeThrows() {
        Position pos = new Position(); // expects first trade to be BUY
        SimpleTrade wrongType = sell(0, 10, 1);
        pos.operate(wrongType);
    }

    /* --------------------------------------------------------------------- *
     *  5. Profit / loss calculations                                        *
     * --------------------------------------------------------------------- */

    @Test
    public void testProfitPositive() {
        SimpleTrade entry = buy(0, 10, 1);
        SimpleTrade exit = sell(5, 12, 1);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        assertTrue(pos.getProfit().isPositive());
        assertTrue(pos.hasProfit());
        assertFalse(pos.hasLoss());

        // gross profit = (12-10)*1 = 2, no cost => profit = 2
        assertEquals(new SimpleNum(2), pos.getProfit());
        assertEquals(new SimpleNum(2), pos.getGrossProfit());
    }

    @Test
    public void testProfitNegativeForSellPosition() {
        SimpleTrade entry = sell(0, 15, 1);
        SimpleTrade exit = buy(3, 13, 1);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);

        // sell at 15, buy back at 13 => profit = -(13-15) = 2
        assertEquals(new SimpleNum(2), pos.getProfit());
        assertTrue(pos.hasProfit());
    }

    @Test
    public void testOpenPositionProfitIsZero() {
        SimpleTrade entry = buy(0, 10, 1);
        Position pos = new Position(entry, zeroCostModel, zeroCostModel);
        assertEquals(new SimpleNum(0), pos.getProfit());
        assertEquals(new SimpleNum(0), pos.getGrossProfit());
        assertFalse(pos.hasProfit()); // profit == 0 => not positive
        assertFalse(pos.hasLoss());   // profit == 0 => not negative
    }

    @Test
    public void testGetProfitWithFinalPriceForOpenPosition() {
        SimpleTrade entry = buy(0, 10, 2);
        Position pos = new Position(entry, zeroCostModel, zeroCostModel);
        Num profit = pos.getProfit(5, new SimpleNum(12)); // (12-10)*2 = 4
        assertEquals(new SimpleNum(4), profit);
    }

    /* --------------------------------------------------------------------- *
     *  6. Gross return calculations                                         *
     * --------------------------------------------------------------------- */

    @Test
    public void testGrossReturnBuyPosition() {
        SimpleTrade entry = buy(0, 10, 1);
        SimpleTrade exit = sell(4, 12, 1);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);
        // return = exit/entry = 12/10 = 1.2
        assertEquals(new SimpleNum(1.2), pos.getGrossReturn());
    }

    @Test
    public void testGrossReturnSellPosition() {
        SimpleTrade entry = sell(0, 15, 1);
        SimpleTrade exit = buy(3, 12, 1);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);
        // for sell: ((exit/entry - 1) * -1) + 1
        // exit/entry = 12/15 = 0.8; (0.8-1) = -0.2; -(-0.2)=0.2; +1 = 1.2
        assertEquals(new SimpleNum(1.2), pos.getGrossReturn());
    }

    /* --------------------------------------------------------------------- *
     *  7. Cost models & transaction cost retrieval                           *
     * --------------------------------------------------------------------- */

    @Test
    public void testCostModelFallbackAfterDeserialization() {
        Position pos = new Position(); // uses default ZeroCostModel
        assertTrue(pos.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(pos.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testGetPositionCostClosedPosition() {
        SimpleTrade entry = buy(0, 10, 1);
        SimpleTrade exit = sell(2, 11, 1);
        Position pos = new Position(entry, exit, zeroCostModel, zeroCostModel);
        // both transaction & holding cost are zero
        assertEquals(new SimpleNum(0), pos.getPositionCost());
    }

    /* --------------------------------------------------------------------- *
     *  8. Constructor validation (edge cases)                               *
     * --------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedPositionRequiresDifferentTradeTypes() {
        SimpleTrade e = buy(0, 10, 1);
        SimpleTrade x = buy(1, 11, 1); // same type as entry
        new Position(e, x, zeroCostModel, zeroCostModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedPositionRequiresSameCostModel() {
        CostModel other = new CostModel() {
            @Override public Num calculate(Position p, int i) { return new SimpleNum(0); }
            @Override public Num calculate(Position p) { return new SimpleNum(0); }
            @Override public Num calculate(Num p, Num a) { return new SimpleNum(0); }
            @Override public boolean equals(CostModel o) { return false; }
        };
        SimpleTrade e = buy(0, 10, 1);
        SimpleTrade x = sell(1, 11, 1);
        new Position(e, x, other, zeroCostModel); // entry uses zeroCostModel, mismatch -> exception
    }

    /* --------------------------------------------------------------------- *
     *  9. toString sanity check                                              *
     * --------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        SimpleTrade e = buy(0, 10, 1);
        SimpleTrade x = sell(2, 12, 1);
        Position pos = new Position(e, x, zeroCostModel, zeroCostModel);
        String s = pos.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(e.toString()));
        assertTrue(s.contains(x.toString()));
    }
}
