package org.ta4j.core;
import org.ta4j.core.num.Num;


import static org.junit.Assert.*;
import org.junit.Test;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * Unit tests for {@link Position}.
 *
 * The tests use simple in‑memory implementations of {@link Num} and {@link BarSeries}
 * that are sufficient for exercising the logic of {@code Position} without
 * depending on the full TA4J library.
 */
public class PositionTest {

    /** Simple {@link Num} implementation based on {@code double}. */
    private static class SimpleNum implements Num {
        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        @Override public Num zero() { return new SimpleNum(0); }
        @Override public Num one() { return new SimpleNum(1); }
        @Override public Num hundred() { return new SimpleNum(100); }
        @Override public Number getDelegate() { return value; }
        @Override public String getName() { return "SimpleNum"; }
        @Override public Num plus(Num augend) { return new SimpleNum(this.value + ((SimpleNum) augend).value); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value - ((SimpleNum) subtrahend).value); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value * ((SimpleNum) multiplicand).value); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value / ((SimpleNum) divisor).value); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(this.value % ((SimpleNum) divisor).value); }
        @Override public Num floor() { return new SimpleNum(Math.floor(value)); }
        @Override public Num ceil() { return new SimpleNum(Math.ceil(value)); }
        @Override public Num pow(int n) { return new SimpleNum(Math.pow(value, n)); }
        @Override public Num pow(Num n) { return new SimpleNum(Math.pow(value, ((SimpleNum) n).value)); }
        @Override public Num log() { return new SimpleNum(Math.log(value)); }
        @Override public Num sqrt() { return new SimpleNum(Math.sqrt(value)); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs() { return new SimpleNum(Math.abs(value)); }
        @Override public Num negate() { return new SimpleNum(-value); }
        @Override public boolean isZero() { return value == 0; }
        @Override public boolean isPositive() { return value > 0; }
        @Override public boolean isPositiveOrZero() { return value >= 0; }
        @Override public boolean isNegative() { return value < 0; }
        @Override public boolean isNegativeOrZero() { return value <= 0; }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, ((SimpleNum) other).value) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > ((SimpleNum) other).value; }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= ((SimpleNum) other).value; }
        @Override public boolean isLessThan(Num other) { return this.value < ((SimpleNum) other).value; }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= ((SimpleNum) other).value; }
        @Override public Num min(Num other) { return this.isLessThan(other) ? this : other; }
        @Override public Num max(Num other) { return this.isGreaterThan(other) ? this : other; }
        @Override public java.util.function.Function<Number, Num> function() { return n -> new SimpleNum(n.doubleValue()); }
        @Override public Num numOf(Number value) { return new SimpleNum(value.doubleValue()); }
        @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
        @Override public boolean isNaN() { return Double.isNaN(value); }
        @Override public double doubleValue() { return value; }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public String toString() { return Double.toString(value); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }
        @Override public int compareTo(Num o) { return Double.compare(this.value, ((SimpleNum) o).value); }
    }

    /** Minimal {@link BarSeries} implementation – only the methods required by the
     *  tests are provided. */
    private static class SimpleBarSeries implements BarSeries {
        private final SimpleNumFactory factory = new SimpleNumFactory();

        @Override public String getName() { return "simple"; }
        @Override public Num num() { return factory.zero(); }
        @Override public Num zero() { return factory.zero(); }
        @Override public Num one() { return factory.one(); }
        @Override public Num hundred() { return factory.hundred(); }
        @Override public Num numOf(Number number) { return factory.numOf(number); }
        @Override public Bar getBar(int i) { throw new UnsupportedOperationException(); }
        @Override public int getBarCount() { return 0; }
        @Override public java.util.List<Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return 0; }
        @Override public String getSeriesPeriodDescription() { return ""; }
        @Override public int getMaximumBarCount() { return 0; }
        @Override public void setMaximumBarCount(int maximumBarCount) {}
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public void addBar(Bar bar) {}
        @Override public void addBar(Bar bar, boolean replace) {}
        @Override public void addBar(java.time.Duration timePeriod, java.time.ZonedDateTime endTime) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
                                    Number closePrice) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
                                    Number closePrice, Number volume) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, Number openPrice, Number highPrice, Number lowPrice,
                                    Number closePrice, Number volume, Number amount) {}
        @Override public void addBar(java.time.Duration timePeriod, java.time.ZonedDateTime endTime,
                                    Number openPrice, Number highPrice, Number lowPrice, Number closePrice,
                                    Number volume) {}
        @Override public void addBar(java.time.Duration timePeriod, java.time.ZonedDateTime endTime,
                                    Number openPrice, Number highPrice, Number lowPrice, Number closePrice,
                                    Number volume, Number amount) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, String openPrice, String highPrice,
                                    String lowPrice, String closePrice) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, String openPrice, String highPrice,
                                    String lowPrice, String closePrice, String volume) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, String openPrice, String highPrice,
                                    String lowPrice, String closePrice, String volume, String amount) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, Num openPrice, Num highPrice,
                                    Num lowPrice, Num closePrice, Num volume) {}
        @Override public void addBar(java.time.ZonedDateTime endTime, Num openPrice, Num highPrice,
                                    Num lowPrice, Num closePrice, Num volume, Num amount) {}
        @Override public void addBar(java.time.Duration timePeriod, java.time.ZonedDateTime endTime,
                                    Num openPrice, Num highPrice, Num lowPrice, Num closePrice,
                                    Num volume) {}
        @Override public void addBar(java.time.Duration timePeriod, java.time.ZonedDateTime endTime,
                                    Num openPrice, Num highPrice, Num lowPrice, Num closePrice,
                                    Num volume, Num amount) {}
        @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
        @Override public void addTrade(String tradeVolume, String tradePrice) {}
        @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
        @Override public void addPrice(Num price) {}
        @Override public void addPrice(String price) {}
        @Override public void addPrice(Number price) {}
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { throw new UnsupportedOperationException(); }
        @Override public boolean isEmpty() { return true; }

        /** Helper factory for numbers. */
        private static class SimpleNumFactory implements Num {
            @Override public Num zero() { return new SimpleNum(0); }
            @Override public Num one() { return new SimpleNum(1); }
            @Override public Num hundred() { return new SimpleNum(100); }
            @Override public Number getDelegate() { return null; }
            @Override public String getName() { return "factory"; }
            @Override public Num plus(Num augend) { throw new UnsupportedOperationException(); }
            @Override public Num minus(Num subtrahend) { throw new UnsupportedOperationException(); }
            @Override public Num multipliedBy(Num multiplicand) { throw new UnsupportedOperationException(); }
            @Override public Num dividedBy(Num divisor) { throw new UnsupportedOperationException(); }
            @Override public Num remainder(Num divisor) { throw new UnsupportedOperationException(); }
            @Override public Num floor() { throw new UnsupportedOperationException(); }
            @Override public Num ceil() { throw new UnsupportedOperationException(); }
            @Override public Num pow(int n) { throw new UnsupportedOperationException(); }
            @Override public Num pow(Num n) { throw new UnsupportedOperationException(); }
            @Override public Num log() { throw new UnsupportedOperationException(); }
            @Override public Num sqrt() { throw new UnsupportedOperationException(); }
            @Override public Num sqrt(int precision) { throw new UnsupportedOperationException(); }
            @Override public Num abs() { throw new UnsupportedOperationException(); }
            @Override public Num negate() { throw new UnsupportedOperationException(); }
            @Override public boolean isZero() { throw new UnsupportedOperationException(); }
            @Override public boolean isPositive() { throw new UnsupportedOperationException(); }
            @Override public boolean isPositiveOrZero() { throw new UnsupportedOperationException(); }
            @Override public boolean isNegative() { throw new UnsupportedOperationException(); }
            @Override public boolean isNegativeOrZero() { throw new UnsupportedOperationException(); }
            @Override public boolean isEqual(Num other) { throw new UnsupportedOperationException(); }
            @Override public boolean isGreaterThan(Num other) { throw new UnsupportedOperationException(); }
            @Override public boolean isGreaterThanOrEqual(Num other) { throw new UnsupportedOperationException(); }
            @Override public boolean isLessThan(Num other) { throw new UnsupportedOperationException(); }
            @Override public boolean isLessThanOrEqual(Num other) { throw new UnsupportedOperationException(); }
            @Override public Num min(Num other) { throw new UnsupportedOperationException(); }
            @Override public Num max(Num other) { throw new UnsupportedOperationException(); }
            @Override public java.util.function.Function<Number, Num> function() { return n -> new SimpleNum(n.doubleValue()); }
            @Override public Num numOf(Number value) { return new SimpleNum(value.doubleValue()); }
            @Override public Num numOf(String value, int precision) { return new SimpleNum(Double.parseDouble(value)); }
            @Override public boolean isNaN() { return false; }
            @Override public double doubleValue() { return 0; }
            @Override public int intValue() { return 0; }
            @Override public long longValue() { return 0; }
            @Override public float floatValue() { return 0; }
            @Override public int hashCode() { return 0; }
            @Override public String toString() { return "factory"; }
            @Override public boolean equals(Object obj) { return this == obj; }
            @Override public int compareTo(Num o) { return 0; }
        }
    }

    /** Helper to create a {@link Num} from a double. */
    private static Num n(double v) {
        return new SimpleNum(v);
    }

    @Test
    public void testDefaultConstructorUsesBuy() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithSellStartingType() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartingTypeThrows() {
        new Position((TradeType) null);
    }

    @Test
    public void testOperateCreatesEntryAndExitAndStateTransitions() {
        Position p = new Position(TradeType.BUY);
        // first operate creates entry
        Trade entry = p.operate(0, n(10), n(1));
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // second operate creates exit
        Trade exit = p.operate(1, n(12), n(1));
        assertNotNull(exit);
        assertEquals(1, exit.getIndex());
        assertTrue(p.isClosed());
        assertFalse(p.isOpened());

        // entry and exit must be stored inside the position
        assertSame(entry, p.getEntry());
        assertSame(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateWithIndexLessThanEntryThrows() {
        Position p = new Position(TradeType.BUY);
        p.operate(5, n(10), n(1)); // creates entry at index 5
        // now try to close with an earlier index
        p.operate(3, n(11), n(1));
    }

    @Test
    public void testProfitForClosedBuyPosition() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(1)); // entry
        p.operate(1, n(12), n(1)); // exit

        Num profit = p.getProfit();
        assertEquals(2.0, profit.doubleValue(), 1e-9);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testProfitForClosedSellPosition() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, n(12), n(1)); // entry (sell)
        p.operate(1, n(10), n(1)); // exit (buy)

        Num profit = p.getProfit();
        assertEquals(2.0, profit.doubleValue(), 1e-9); // short profit when price falls
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testHasLossWhenProfitIsNegative() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(15), n(1)); // entry
        p.operate(1, n(10), n(1)); // exit – loss

        Num profit = p.getProfit();
        assertEquals(-5.0, profit.doubleValue(), 1e-9);
        assertFalse(p.hasProfit());
        assertTrue(p.hasLoss());
    }

    @Test
    public void testGrossProfitAndReturnForBuy() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(1));
        p.operate(1, n(12), n(1));

        Num grossProfit = p.getGrossProfit();
        assertEquals(2.0, grossProfit.doubleValue(), 1e-9);

        Num grossReturn = p.getGrossReturn();
        assertEquals(1.2, grossReturn.doubleValue(), 1e-9);
    }

    @Test
    public void testGrossProfitAndReturnForSell() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, n(12), n(1));
        p.operate(1, n(10), n(1));

        Num grossProfit = p.getGrossProfit();
        assertEquals(2.0, grossProfit.doubleValue(), 1e-9); // short profit

        Num grossReturn = p.getGrossReturn();
        // entry 12, exit 10 → ((10/12 - 1).negate()) + 1 = 1.166666...
        assertEquals(1.1666666666666667, grossReturn.doubleValue(), 1e-9);
    }

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position(TradeType.BUY);
        p1.operate(0, n(10), n(1));
        p1.operate(1, n(12), n(1));

        Position p2 = new Position(TradeType.BUY);
        p2.operate(0, n(10), n(1));
        p2.operate(1, n(12), n(1));

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // change p2 exit price – should no longer be equal
        p2 = new Position(TradeType.BUY);
        p2.operate(0, n(10), n(1));
        p2.operate(1, n(13), n(1));
        assertNotEquals(p1, p2);
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(1));
        p.operate(1, n(12), n(1));

        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
    }

    @Test
    public void testPositionCostUsesZeroCostModel() {
        // ZeroCostModel yields zero for all calculations
        Position p = new Position(TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        p.operate(0, n(10), n(1));
        p.operate(1, n(12), n(1));

        Num cost = p.getPositionCost();
        assertEquals(0.0, cost.doubleValue(), 1e-9);
    }

    @Test
    public void testGetProfitWhenPositionIsOpenReturnsZero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(10), n(1)); // only entry, position still opened
        assertTrue(p.isOpened());
        assertEquals(0.0, p.getProfit().doubleValue(), 1e-9);
    }

    @Test
    public void testOperateWithNaNPriceAndAmountCreatesTrade() {
        Position p = new Position(TradeType.BUY);
        Trade t = p.operate(0); // uses NaN for price/amount
        assertNotNull(t);
        assertEquals(0, t.getIndex());
    }

    @Test
    public void testGetGrossReturnUsingExplicitPrices() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, n(5), n(2));
        p.operate(1, n(7), n(2));

        Num ret = p.getGrossReturn(n(5), n(7));
        assertEquals(1.4, ret.doubleValue(), 1e-9);
    }
}
