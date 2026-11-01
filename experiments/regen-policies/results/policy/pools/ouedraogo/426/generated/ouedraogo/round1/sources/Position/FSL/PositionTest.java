package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.num.NumFactory;


import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link Position}.
 *
 * The tests use simple concrete implementations of {@link Num} and {@link Trade}
 * that are defined inside the test class.  They avoid any external library
 * classes not listed in the provided signatures.
 */
public class PositionTest {

    /** Simple {@link Num} implementation based on a {@code double} value. */
    private static class SimpleNum implements Num {
        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* ---------- arithmetic ---------- */
        @Override public Num plus(Num augend)   { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value / ((SimpleNum) divisor).value); }
        @Override public Num negate() { return new SimpleNum(-this.value); }

        /* ---------- comparisons ---------- */
        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum) o).value); }
        @Override public boolean isZero()       { return this.value == 0.0; }
        @Override public boolean isPositive()   { return this.value > 0.0; }
        @Override public boolean isNegative()   { return this.value < 0.0; }
        @Override public boolean isPositiveOrZero() { return this.value >= 0.0; }
        @Override public boolean isNegativeOrZero() { return this.value <= 0.0; }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other) { return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= ((SimpleNum) other).value; }

        /* ---------- other required methods ---------- */
        @Override public Number getDelegate() { return value; }
        @Override public NumFactory getNumFactory() { return null; } // not needed for the tested paths
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num remainder(Num divisor) { throw new UnsupportedOperationException(); }
        @Override public Num floor() { return new SimpleNum(Math.floor(value)); }
        @Override public Num ceil() { return new SimpleNum(Math.ceil(value)); }
        @Override public Num pow(int n) { return new SimpleNum(Math.pow(value, n)); }
        @Override public Num pow(Num n) { return new SimpleNum(Math.pow(value, ((SimpleNum) n).value)); }
        @Override public Num log() { return new SimpleNum(Math.log(value)); }
        @Override public Num exp() { return new SimpleNum(Math.exp(value)); }
        @Override public Num sqrt() { return new SimpleNum(Math.sqrt(value)); }
        @Override public Num sqrt(java.math.MathContext mc) { return new SimpleNum(Math.sqrt(value)); }
        @Override public Num abs() { return new SimpleNum(Math.abs(value)); }
        @Override public java.math.BigDecimal bigDecimalValue() { return java.math.BigDecimal.valueOf(value); }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
        @Override public double doubleValue() { return value; }
        @Override public boolean isNaN() { return Double.isNaN(value); }

        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }

        @Override public int hashCode() { return Double.hashCode(value); }

        @Override public String toString() { return Double.toString(value); }

        // The remaining default methods from the interface are not required for the tests.
    }

    /** Minimal {@link Trade} implementation used for the tests. */
    private static class SimpleTrade implements Trade {
        private final TradeType type;
        private final int index;
        private final SimpleNum pricePerAsset;
        private final SimpleNum amount;
        private final SimpleNum cost; // transaction cost (set to zero in tests)
        private final ZeroCostModel costModel = new ZeroCostModel();

        SimpleTrade(TradeType type, int index, double price, double amount) {
            this.type = type;
            this.index = index;
            this.pricePerAsset = new SimpleNum(price);
            this.amount = new SimpleNum(amount);
            this.cost = new SimpleNum(0.0);
        }

        @Override public TradeType getType() { return type; }
        @Override public int getIndex() { return index; }
        @Override public Num getPricePerAsset() { return pricePerAsset; }
        @Override public Num getNetPrice() { return pricePerAsset.multipliedBy(amount); }
        @Override public Num getAmount() { return amount; }
        @Override public Num getCost() { return cost; }
        @Override public CostModel getCostModel() { return costModel; }
        @Override public Num getValue() { return pricePerAsset.multipliedBy(amount); }

        @Override public boolean isBuy() { return type == TradeType.BUY; }
        @Override public boolean isSell() { return type == TradeType.SELL; }

        // The remaining default methods are not needed for the tests.
        @Override public java.time.Instant getTime() { return null; }
        @Override public String getId() { return null; }
        @Override public String getInstrument() { return null; }
        @Override public String getOrderId() { return null; }
        @Override public String getCorrelationId() { return null; }
        @Override public java.util.List<TradeFill> getFills() { return java.util.Collections.emptyList(); }
    }

    /* --------------------------------------------------------------------- */
    /* Constructors & basic state                                            */
    /* --------------------------------------------------------------------- */

    @Test
    public void testDefaultConstructor() {
        Position p = new Position(); // defaults to BUY
        assertTrue(p.isNew());
        assertNull(p.getEntry());
        assertNull(p.getExit());
        assertEquals(TradeType.BUY, p.getStartingType());
    }

    @Test
    public void testConstructorWithStartingType() {
        Position p = new Position(TradeType.SELL);
        assertTrue(p.isNew());
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    @Test
    public void testClosedPositionConstructor() {
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 2.0);
        Trade exit = new SimpleTrade(TradeType.SELL, 5, 12.0, 2.0);
        Position p = new Position(entry, exit);
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedPositionWithSameTradeTypesThrows() {
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 1.0);
        Trade exit = new SimpleTrade(TradeType.BUY, 3, 12.0, 1.0);
        new Position(entry, exit); // should fail because types are identical
    }

    @Test(expected = NullPointerException.class)
    public void testOpenPositionWithNullEntryThrows() {
        new Position(null, new ZeroCostModel(), new ZeroCostModel());
    }

    @Test
    public void testOpenPositionConstructor() {
        Trade entry = new SimpleTrade(TradeType.SELL, 2, 15.0, 3.0);
        Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
        assertTrue(p.isOpened());
        assertEquals(entry, p.getEntry());
        assertNull(p.getExit());
    }

    /* --------------------------------------------------------------------- */
    /* Accessor methods (side, amount, prices, costs)                        */
    /* --------------------------------------------------------------------- */

    @Test
    public void testSideAndAmountOnOpenPosition() {
        Trade entry = new SimpleTrade(TradeType.BUY, 1, 8.0, 5.0);
        Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());

        assertEquals(ExecutionSide.BUY, p.side());
        assertEquals(entry.getAmount(), p.amount());
        assertEquals(entry.getPricePerAsset(), p.averageEntryPrice());
        assertEquals(entry.getValue(), p.totalEntryCost());
        assertEquals(entry.getCost(), p.totalFees());
    }

    @Test
    public void testSideReturnsNullWhenNoEntry() {
        Position p = new Position();
        assertNull(p.side());
        assertNull(p.amount());
        assertNull(p.averageEntryPrice());
        assertNull(p.totalEntryCost());
        assertNull(p.totalFees());
    }

    /* --------------------------------------------------------------------- */
    /* Equality & hashCode                                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testEqualsAndHashCode() {
        Trade entry1 = new SimpleTrade(TradeType.BUY, 0, 10.0, 1.0);
        Trade exit1   = new SimpleTrade(TradeType.SELL, 4, 12.0, 1.0);
        Position p1 = new Position(entry1, exit1);
        Position p2 = new Position(entry1, exit1);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Different exit trade -> not equal
        Trade exit2 = new SimpleTrade(TradeType.SELL, 4, 13.0, 1.0);
        Position p3 = new Position(entry1, exit2);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testEqualsWithNullEntryAndExit() {
        Position p1 = new Position();
        Position p2 = new Position();
        assertEquals(p1, p2);
    }

    /* --------------------------------------------------------------------- */
    /* operate(Trade) – basic flow                                           */
    /* --------------------------------------------------------------------- */

    @Test
    public void testOperateWithEntryAndExitTrades() {
        Position p = new Position(); // new position, starting type BUY

        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 2.0);
        Trade resultEntry = p.operate(entry);
        assertEquals(entry, resultEntry);
        assertTrue(p.isOpened());

        Trade exit = new SimpleTrade(TradeType.SELL, 3, 12.0, 2.0);
        Trade resultExit = p.operate(exit);
        assertEquals(exit, resultExit);
        assertTrue(p.isClosed());

        // further calls should return null (position already closed)
        Trade another = new SimpleTrade(TradeType.BUY, 5, 13.0, 1.0);
        assertNull(p.operate(another));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateEntryWithWrongStartingTypeThrows() {
        Position p = new Position(TradeType.SELL); // starting type SELL
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 1.0);
        p.operate(entry); // mismatch → exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateExitWithWrongComplementTypeThrows() {
        Position p = new Position(); // default BUY
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 1.0);
        p.operate(entry); // ok

        Trade wrongExit = new SimpleTrade(TradeType.BUY, 2, 12.0, 1.0); // not SELL
        p.operate(wrongExit); // should fail
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitWithIndexBeforeEntryThrows() {
        Position p = new Position(); // default BUY
        Trade entry = new SimpleTrade(TradeType.BUY, 5, 10.0, 1.0);
        p.operate(entry); // entry at index 5

        Trade exit = new SimpleTrade(TradeType.SELL, 3, 12.0, 1.0); // index 3 < entry index
        p.operate(exit); // should throw IllegalStateException
    }

    /* --------------------------------------------------------------------- */
    /* Profit / Gross profit / Gross return                                   */
    /* --------------------------------------------------------------------- */

    @Test
    public void testProfitAndGrossProfitForClosedBuyPosition() {
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 2.0); // value = 20
        Trade exit  = new SimpleTrade(TradeType.SELL, 4, 12.0, 2.0); // value = 24
        Position p = new Position(entry, exit);

        // No transaction/holding cost (ZeroCostModel) → profit = 24 - 20 = 4
        Num expectedProfit = new SimpleNum(4.0);
        assertEquals(expectedProfit, p.getProfit());
        assertEquals(expectedProfit, p.getGrossProfit());

        // Gross return = exitPrice / entryPrice = 12 / 10 = 1.2
        Num expectedReturn = new SimpleNum(1.2);
        assertEquals(expectedReturn, p.getGrossReturn());
        assertEquals(expectedReturn, p.getGrossReturn(entry.getPricePerAsset()));
    }

    @Test
    public void testProfitAndGrossProfitForClosedSellPosition() {
        // Short position: entry SELL at 12, exit BUY at 10
        Trade entry = new SimpleTrade(TradeType.SELL, 0, 12.0, 2.0); // value = 24
        Trade exit  = new SimpleTrade(TradeType.BUY, 5, 10.0, 2.0); // value = 20
        Position p = new Position(entry, exit);

        // Gross profit for a short is (entryValue - exitValue) = 24 - 20 = 4
        // The implementation negates the long profit, giving +4 as well.
        Num expectedProfit = new SimpleNum(4.0);
        assertEquals(expectedProfit, p.getGrossProfit());
        assertEquals(expectedProfit, p.getProfit());

        // Gross return for short profit: ((exit/entry - 1).negate()) + 1
        // exit/entry = 10/12 = 0.83333...,  minus 1 = -0.16666..., negate = 0.16666..., +1 = 1.16666...
        Num expectedReturn = new SimpleNum(1.1666666666666667);
        assertEquals(expectedReturn, p.getGrossReturn(entry.getPricePerAsset()));
    }

    @Test
    public void testHasProfitAndHasLoss() {
        // Profit scenario
        Trade entry1 = new SimpleTrade(TradeType.BUY, 0, 5.0, 1.0);
        Trade exit1  = new SimpleTrade(TradeType.SELL, 2, 7.0, 1.0);
        Position profitPos = new Position(entry1, exit1);
        assertTrue(profitPos.hasProfit());
        assertFalse(profitPos.hasLoss());

        // Loss scenario
        Trade entry2 = new SimpleTrade(TradeType.BUY, 0, 8.0, 1.0);
        Trade exit2  = new SimpleTrade(TradeType.SELL, 3, 6.0, 1.0);
        Position lossPos = new Position(entry2, exit2);
        assertTrue(lossPos.hasLoss());
        assertFalse(lossPos.hasProfit());
    }

    /* --------------------------------------------------------------------- */
    /* Cost related methods (ZeroCostModel)                                  */
    /* --------------------------------------------------------------------- */

    @Test
    public void testCostModelsReturnZeroCostModelWhenNotSet() {
        Position p = new Position(); // default constructor uses ZeroCostModel internally
        assertTrue(p.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(p.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testGetPositionCostReturnsZeroForClosedPosition() {
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 10.0, 1.0);
        Trade exit  = new SimpleTrade(TradeType.SELL, 4, 12.0, 1.0);
        Position p = new Position(entry, exit);
        // Both transaction and holding cost models are ZeroCostModel → total cost = 0
        Num zero = new SimpleNum(0.0);
        assertEquals(zero, p.getPositionCost());
        assertEquals(zero, p.getHoldingCost());
        assertEquals(zero, p.getTransactionCostModel().calculate(p));
    }

    /* --------------------------------------------------------------------- */
    /* getGrossReturn(entryPrice, exitPrice) helper method                    */
    /* --------------------------------------------------------------------- */

    @Test
    public void testGrossReturnHelperMethodBuy() {
        Num entryPrice = new SimpleNum(10.0);
        Num exitPrice  = new SimpleNum(13.0);
        Position p = new Position(); // starting type irrelevant for this static-like method
        Num expected = exitPrice.dividedBy(entryPrice); // 1.3
        assertEquals(expected, p.getGrossReturn(entryPrice, exitPrice));
    }

    @Test
    public void testGrossReturnHelperMethodSell() {
        Num entryPrice = new SimpleNum(20.0);
        Num exitPrice  = new SimpleNum(15.0);
        Position p = new Position(TradeType.SELL); // starting SELL
        // ((exit/entry - 1).negate()) + 1
        Num ratio = exitPrice.dividedBy(entryPrice); // 0.75
        Num one = new SimpleNum(1.0);
        Num expected = ratio.minus(one).negate().plus(one); // 0.75 -1 = -0.25 → 0.25 +1 = 1.25
        assertEquals(expected, p.getGrossReturn(entryPrice, exitPrice));
    }

    /* --------------------------------------------------------------------- */
    /* toString sanity check                                                 */
    /* --------------------------------------------------------------------- */

    @Test
    public void testToStringContainsEntryAndExit() {
        Trade entry = new SimpleTrade(TradeType.BUY, 0, 5.0, 1.0);
        Trade exit = new SimpleTrade(TradeType.SELL, 2, 6.0, 1.0);
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }
}
