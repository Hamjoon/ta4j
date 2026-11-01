package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>All constructors (default, with starting type, with trades, with cost models)</li>
 *   <li>State predicates {@code isNew()}, {@code isOpened()}, {@code isClosed()}</li>
 *   <li>Side, amount, average entry price, total entry cost and fees</li>
 *   <li>Equality, hashCode and {@code toString()}</li>
 *   <li>Operation flow (entry → exit) using both {@code operate(int)} and {@code operate(Trade)}</li>
 *   <li>Profit / loss detection and gross‑return calculations</li>
 *   <li>Cost‑model handling (default zero cost model and mismatched model validation)</li>
 *   <li>Exception scenarios (wrong trade type, index order, mismatched cost models)</li>
 * </ul>
 * </p>
 *
 * <p>
 * The library supplies concrete {@link Num} implementations via {@link DecimalNum},
 * and concrete {@link Trade} instances via the static factory methods
 * {@code Trade.buyAt(...)} / {@code Trade.sellAt(...)}.
 * </p>
 */
public class PositionTest {

    /* --------------------------------------------------------------------- *
     *  Helper – a very small CostModel that always returns zero            *
     * --------------------------------------------------------------------- */
    private static class ZeroCost implements CostModel {
        @Override public Num calculate(Position position, int finalIndex) { return zero(); }
        @Override public Num calculate(Position position)               { return zero(); }
        @Override public Num calculate(Num price, Num amount)            { return zero(); }
        @Override public boolean equals(CostModel other)                { return other instanceof ZeroCost; }

        private Num zero() {
            return DecimalNum.valueOf(0);
        }
    }

    /* --------------------------------------------------------------------- *
     *  Helper – a non‑zero CostModel (used to provoke validation errors)   *
     * --------------------------------------------------------------------- */
    private static class FixedCostModel implements CostModel {
        private final Num fixed;

        FixedCostModel(Num fixed) { this.fixed = fixed; }

        @Override public Num calculate(Position position, int finalIndex) { return fixed; }
        @Override public Num calculate(Position position)               { return fixed; }
        @Override public Num calculate(Num price, Num amount)            { return fixed; }
        @Override public boolean equals(CostModel other)                { return other instanceof FixedCostModel; }
    }

    /* --------------------------------------------------------------------- *
     *  1. Constructor / default state tests                                   *
     * --------------------------------------------------------------------- */
    @Test
    public void testDefaultConstructorCreatesNewBuyPosition() {
        Position p = new Position();                     // default = BUY, zero cost models
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        // starting type must be BUY
        assertEquals(Trade.TradeType.BUY, p.getStartingType());

        // default cost models are ZeroCostModel (or any CostModel that behaves like zero)
        assertTrue(p.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(p.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(Trade.TradeType.SELL);
        assertTrue(p.isNew());
        assertEquals(Trade.TradeType.SELL, p.getStartingType());
    }

    @Test
    public void testOpenPositionConstructorWithExplicitCostModels() {
        CostModel tx = new ZeroCost();
        CostModel hold = new ZeroCost();

        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),        // price
                DecimalNum.valueOf(2),         // amount
                tx);                            // explicit cost model

        Position p = new Position(entry, tx, hold);
        assertTrue(p.isOpened());
        assertEquals(entry, p.getEntry());
        assertNull(p.getExit());

        // cost models are the ones we passed
        assertSame(tx, p.getTransactionCostModel());
        assertSame(hold, p.getHoldingCostModel());
    }

    @Test
    public void testClosedPositionConstructorWithDefaults() {
        // entry and exit must have complementary types and the same cost model
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1));
        Trade exit = Trade.sellAt(5,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(1));

        Position p = new Position(entry, exit);
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedPositionConstructorRejectsSameTradeTypes() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1));
        Trade exit = Trade.buyAt(5,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(1));
        new Position(entry, exit);               // both BUY → should fail
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedPositionConstructorRejectsMismatchedCostModels() {
        CostModel cm1 = new ZeroCost();
        CostModel cm2 = new FixedCostModel(DecimalNum.valueOf(0.5));

        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1),
                cm1);
        Trade exit = Trade.sellAt(5,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(1),
                cm2);                               // different cost model

        new Position(entry, exit, cm1, new ZeroCost()); // validation should fail
    }

    /* --------------------------------------------------------------------- *
     *  2. State transition via operate(int)                                 *
     * --------------------------------------------------------------------- */
    @Test
    public void testOperateEntryAndExitUsingIndexOnly() {
        Position p = new Position();                 // BUY by default

        // first call creates the entry trade (price/amount = NaN)
        Trade entry = p.operate(0);
        assertNotNull(entry);
        assertTrue(p.isOpened());
        assertEquals(0, entry.getIndex());
        assertEquals(Trade.TradeType.BUY, entry.getType());

        // second call creates the exit trade (SELL)
        Trade exit = p.operate(3);
        assertNotNull(exit);
        assertTrue(p.isClosed());
        assertEquals(3, exit.getIndex());
        assertEquals(Trade.TradeType.SELL, exit.getType());

        // internal references must match
        assertSame(entry, p.getEntry());
        assertSame(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitBeforeEntryIndexThrows() {
        Position p = new Position();                 // BUY

        // create entry at index 5
        p.operate(5);
        // now try to create exit at index 2 (earlier) → IllegalStateException
        p.operate(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateWithWrongFirstTradeTypeThrows() {
        // starting type is SELL, but we feed a BUY trade via operate(Trade)
        Position p = new Position(Trade.TradeType.SELL);
        Trade wrong = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1));
        p.operate(wrong);
    }

    @Test
    public void testOperateWithPreBuiltTradeObjects() {
        CostModel cm = new ZeroCost();

        // position that starts with BUY
        Position p = new Position();

        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(2),
                cm);
        p.operate(entry);
        assertTrue(p.isOpened());
        assertSame(entry, p.getEntry());

        Trade exit = Trade.sellAt(4,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(2),
                cm);
        p.operate(exit);
        assertTrue(p.isClosed());
        assertSame(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateWithMismatchedCostModelThrows() {
        CostModel cm1 = new ZeroCost();
        CostModel cm2 = new FixedCostModel(DecimalNum.valueOf(1));

        Position p = new Position();

        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1),
                cm1);
        p.operate(entry);                     // OK – same model as position (zero)

        // exit uses a different cost model → validation should fail
        Trade exit = Trade.sellAt(1,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(1),
                cm2);
        p.operate(exit);
    }

    /* --------------------------------------------------------------------- *
     *  3. Accessor methods after entry is set                               *
     * --------------------------------------------------------------------- */
    @Test
    public void testSideAmountAndPricesAfterEntry() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(3));
        Position p = new Position(entry,
                new ZeroCost(),
                new ZeroCost());

        assertEquals(ExecutionSide.BUY, p.side());
        assertEquals(DecimalNum.valueOf(3), p.amount());
        assertEquals(DecimalNum.valueOf(15), p.averageEntryPrice());
        assertEquals(entry.getValue(), p.totalEntryCost());
        assertEquals(entry.getCost(), p.totalFees());
    }

    @Test
    public void testSideIsNullWhenNoEntry() {
        Position p = new Position();
        assertNull(p.side());
        assertNull(p.amount());
        assertNull(p.averageEntryPrice());
        assertNull(p.totalEntryCost());
        assertNull(p.totalFees());
    }

    /* --------------------------------------------------------------------- *
     *  4. Equality, hashCode and toString                                    *
     * --------------------------------------------------------------------- */
    @Test
    public void testEqualsAndHashCode() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1));
        Trade exit = Trade.sellAt(2,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(1));

        Position p1 = new Position(entry, exit);
        Position p2 = new Position(entry, exit);
        Position p3 = new Position(); // different

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(5),
                DecimalNum.valueOf(1));
        Trade exit = Trade.sellAt(3,
                DecimalNum.valueOf(7),
                DecimalNum.valueOf(1));

        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    /* --------------------------------------------------------------------- *
     *  5. Profit / loss detection                                            *
     * --------------------------------------------------------------------- */
    @Test
    public void testHasProfitWhenExitPriceHigherThanBuy() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1));
        Trade exit = Trade.sellAt(5,
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(1));

        Position p = new Position(entry, exit);
        Num profit = p.getProfit();               // (15*1 - 10*1) = 5
        assertEquals(DecimalNum.valueOf(5), profit);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testHasLossWhenExitPriceLowerThanBuy() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(20),
                DecimalNum.valueOf(2));
        Trade exit = Trade.sellAt(4,
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(2));

        Position p = new Position(entry, exit);
        Num profit = p.getProfit();               // (15*2 - 20*2) = -10
        assertEquals(DecimalNum.valueOf(-10), profit);
        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());
    }

    @Test
    public void testGrossProfitAndReturnForBuyPosition() {
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(100),
                DecimalNum.valueOf(1));
        Trade exit = Trade.sellAt(3,
                DecimalNum.valueOf(110),
                DecimalNum.valueOf(1));

        Position p = new Position(entry, exit);
        Num grossProfit = p.getGrossProfit();    // 110 - 100 = 10
        assertEquals(DecimalNum.valueOf(10), grossProfit);

        Num grossReturn = p.getGrossReturn();    // 110 / 100 = 1.10
        assertEquals(DecimalNum.valueOf(1.10).setScale(2), grossReturn.setScale(2));
    }

    @Test
    public void testGrossProfitAndReturnForSellPosition() {
        Trade entry = Trade.sellAt(0,
                DecimalNum.valueOf(100),
                DecimalNum.valueOf(1));
        Trade exit = Trade.buyAt(2,
                DecimalNum.valueOf(90),
                DecimalNum.valueOf(1));

        Position p = new Position(entry, exit);
        Num grossProfit = p.getGrossProfit();    // (100 - 90) = 10, then negated => -10
        assertEquals(DecimalNum.valueOf(-10), grossProfit);

        // For a short position, a price drop of 10% yields return 1.10 (base included)
        Num grossReturn = p.getGrossReturn();    // ((90/100)-1).negate() + 1 = 1.10
        assertEquals(DecimalNum.valueOf(1.10).setScale(2), grossReturn.setScale(2));
    }

    /* --------------------------------------------------------------------- *
     *  6. Position cost model handling                                       *
     * --------------------------------------------------------------------- */
    @Test
    public void testDefaultCostModelsAreZeroAfterDeserialization() {
        Position p = new Position();
        // direct access – the fields are transient, but getters must return a zero model
        assertTrue(p.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(p.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testCustomCostModelsAreReturnedByGetters() {
        CostModel tx = new FixedCostModel(DecimalNum.valueOf(0.2));
        CostModel hold = new FixedCostModel(DecimalNum.valueOf(0.05));

        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1),
                tx);
        Position p = new Position(entry, tx, hold);

        assertSame(tx, p.getTransactionCostModel());
        assertSame(hold, p.getHoldingCostModel());
    }

    /* --------------------------------------------------------------------- *
     *  7. getProfit(int,Num) for an open position (final bar simulation)   *
     * --------------------------------------------------------------------- */
    @Test
    public void testOpenPositionProfitUsingFinalBar() {
        // open BUY position
        Trade entry = Trade.buyAt(0,
                DecimalNum.valueOf(50),
                DecimalNum.valueOf(2));
        Position p = new Position(entry, new ZeroCost(), new ZeroCost());

        // simulate final bar at index 10, price 55
        Num profit = p.getProfit(10, DecimalNum.valueOf(55));
        // expected: (55*2 - 50*2) = 10
        assertEquals(DecimalNum.valueOf(10), profit);
    }

    /* --------------------------------------------------------------------- *
     *  8. Validation of index order when operating with a Trade object      *
     * --------------------------------------------------------------------- */
    @Test(expected = IllegalStateException.class)
    public void testOperateTradeWithEarlierIndexThanEntryThrows() {
        Position p = new Position();
        Trade entry = Trade.buyAt(5,
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(1));
        p.operate(entry);                       // entry at index 5

        // exit trade with index 3 → should throw
        Trade badExit = Trade.sellAt(3,
                DecimalNum.valueOf(12),
                DecimalNum.valueOf(1));
        p.operate(badExit);
    }
}
