###Test START##
package org.ta4j.core.analysis;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CashFlowTest {

    /** Simple Num implementation based on {@link BigDecimal}. */
    private static class SimpleNum implements Num {
        private final BigDecimal value;
        private final SimpleNumFactory factory;

        SimpleNum(BigDecimal value, SimpleNumFactory factory) {
            this.value = value;
            this.factory = factory;
        }

        SimpleNum(long v, SimpleNumFactory factory) {
            this(BigDecimal.valueOf(v), factory);
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
            return new SimpleNum(value.add(toBigDecimal(augend)), factory);
        }

        @Override
        public Num minus(Num subtrahend) {
            return new SimpleNum(value.subtract(toBigDecimal(subtrahend)), factory);
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return new SimpleNum(value.multiply(toBigDecimal(multiplicand)), factory);
        }

        @Override
        public Num dividedBy(Num divisor) {
            return new SimpleNum(value.divide(toBigDecimal(divisor), 10, BigDecimal.ROUND_HALF_UP), factory);
        }

        @Override
        public Num remainder(Num divisor) {
            return new SimpleNum(value.remainder(toBigDecimal(divisor)), factory);
        }

        @Override
        public Num floor() {
            return new SimpleNum(value.setScale(0, BigDecimal.ROUND_FLOOR), factory);
        }

        @Override
        public Num ceil() {
            return new SimpleNum(value.setScale(0, BigDecimal.ROUND_CEILING), factory);
        }

        @Override
        public Num pow(int n) {
            return new SimpleNum(value.pow(n), factory);
        }

        @Override
        public Num pow(Num n) {
            return new SimpleNum(value.pow(toBigDecimal(n).intValue()), factory);
        }

        @Override
        public Num log() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Num exp() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Num sqrt() {
            return new SimpleNum(value.sqrt(new java.math.MathContext(10)), factory);
        }

        @Override
        public Num sqrt(java.math.MathContext mathContext) {
            return new SimpleNum(value.sqrt(mathContext), factory);
        }

        @Override
        public Num abs() {
            return new SimpleNum(value.abs(), factory);
        }

        @Override
        public Num negate() {
            return new SimpleNum(value.negate(), factory);
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
            return new SimpleNum(value.min(toBigDecimal(other)), factory);
        }

        @Override
        public Num max(Num other) {
            return new SimpleNum(value.max(toBigDecimal(other)), factory);
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
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value.toPlainString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleNum)) return false;
            SimpleNum other = (SimpleNum) obj;
            return value.compareTo(other.value) == 0;
        }

        private static BigDecimal toBigDecimal(Num n) {
            if (n instanceof SimpleNum) {
                return ((SimpleNum) n).value;
            }
            return new BigDecimal(n.toString());
        }
    }

    /** Simple NumFactory used by the test series. */
    private static class SimpleNumFactory implements NumFactory {
        private final SimpleNum ONE = new SimpleNum(BigDecimal.ONE, this);
        private final SimpleNum ZERO = new SimpleNum(BigDecimal.ZERO, this);

        @Override
        public Num one() {
            return ONE;
        }

        @Override
        public Num zero() {
            return ZERO;
        }

        @Override
        public Num numOf(Number n) {
            return new SimpleNum(new BigDecimal(n.toString()), this);
        }

        @Override
        public Num numOf(String s) {
            return new SimpleNum(new BigDecimal(s), this);
        }
    }

    /** Minimal Bar implementation needed for CashFlow calculations. */
    private static class SimpleBar implements org.ta4j.core.Bar {
        private final Num closePrice;

        SimpleBar(Num closePrice) {
            this.closePrice = closePrice;
        }

        @Override
        public Num getClosePrice() {
            return closePrice;
        }

        // The remaining methods are not used by CashFlow; they throw
        // UnsupportedOperationException to keep the implementation minimal.
        @Override public Num getOpenPrice() { throw new UnsupportedOperationException(); }
        @Override public Num getHighPrice() { throw new UnsupportedOperationException(); }
        @Override public Num getLowPrice() { throw new UnsupportedOperationException(); }
        @Override public Num getVolume() { throw new UnsupportedOperationException(); }
        @Override public Num getAmount() { throw new UnsupportedOperationException(); }
        @Override public int getIndex() { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.num.Num getTimestamp() { throw new UnsupportedOperationException(); }
        @Override public org.ta4j.core.num.Num getTimePeriod() { throw new UnsupportedOperationException(); }
    }

    /** Minimal BarSeries implementation used in the tests. */
    private static class SimpleBarSeries implements BarSeries {
        private final List<Bar> bars;
        private final SimpleNumFactory numFactory = new SimpleNumFactory();

        SimpleBarSeries(List<Bar> bars) {
            this.bars = new ArrayList<>(bars);
        }

        @Override
        public NumFactory numFactory() {
            return numFactory;
        }

        @Override
        public Bar getBar(int i) {
            return bars.get(i);
        }

        @Override
        public int getBarCount() {
            return bars.size();
        }

        @Override
        public int getEndIndex() {
            return bars.size() - 1;
        }

        @Override
        public List<Bar> getBarData() {
            return Collections.unmodifiableList(bars);
        }

        // Unused methods throw UnsupportedOperationException
        @Override public BarBuilder barBuilder() { throw new UnsupportedOperationException(); }
        @Override public String getName() { throw new UnsupportedOperationException(); }
        @Override public Bar getFirstBar() { throw new UnsupportedOperationException(); }
        @Override public Bar getLastBar() { throw new UnsupportedOperationException(); }
        @Override public int getBeginIndex() { throw new UnsupportedOperationException(); }
        @Override public boolean isEmpty() { throw new UnsupportedOperationException(); }
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
        @Override public String getSeriesPeriodDescription() { throw new UnsupportedOperationException(); }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { throw new UnsupportedOperationException(); }
    }

    /** Minimal Trade implementation used for Position construction. */
    private static class SimpleTrade implements Trade {
        private final int index;
        private final boolean isBuy;
        private final Num netPrice;

        SimpleTrade(int index, boolean isBuy, Num netPrice) {
            this.index = index;
            this.isBuy = isBuy;
            this.netPrice = netPrice;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isBuy() {
            return isBuy;
        }

        @Override
        public Num getNetPrice() {
            return netPrice;
        }

        // Remaining methods are not required for the tests
        @Override public Num getPrice() { throw new UnsupportedOperationException(); }
        @Override public Num getAmount() { throw new UnsupportedOperationException(); }
        @Override public Num getVolume() { throw new UnsupportedOperationException(); }
        @Override public Num getCost() { throw new UnsupportedOperationException(); }
        @Override public Num getFee() { throw new UnsupportedOperationException(); }
        @Override public Num getCommission() { throw new UnsupportedOperationException(); }
        @Override public boolean isEntry() { throw new UnsupportedOperationException(); }
        @Override public boolean isExit() { throw new UnsupportedOperationException(); }
        @Override public TradeType getTradeType() { throw new UnsupportedOperationException(); }
        @Override public void setTradeType(TradeType tradeType) { throw new UnsupportedOperationException(); }
    }

    /** Minimal TradingRecord implementation for testing multiple positions. */
    private static class SimpleTradingRecord implements TradingRecord {
        private final List<Position> positions;
        private final Position currentPosition;

        SimpleTradingRecord(List<Position> positions, Position currentPosition) {
            this.positions = positions;
            this.currentPosition = currentPosition;
        }

        @Override
        public List<Position> getPositions() {
            return positions;
        }

        @Override
        public Position getCurrentPosition() {
            return currentPosition;
        }

        @Override
        public CostModel getTransactionCostModel() { throw new UnsupportedOperationException(); }

        @Override
        public CostModel getHoldingCostModel() { throw new UnsupportedOperationException(); }

        @Override
        public List<Trade> getTrades() { throw new UnsupportedOperationException(); }

        @Override
        public Trade getLastTrade() { throw new UnsupportedOperationException(); }

        @Override
        public Trade getLastTrade(TradeType tradeType) { throw new UnsupportedOperationException(); }

        @Override
        public Trade getLastEntry() { throw new UnsupportedOperationException(); }

        @Override
        public Trade getLastExit() { throw new UnsupportedOperationException(); }

        @Override
        public Integer getStartIndex() { throw new UnsupportedOperationException(); }

        @Override
        public Integer getEndIndex() { throw new UnsupportedOperationException(); }

        @Override
        public int getStartIndex(BarSeries series) { throw new UnsupportedOperationException(); }

        @Override
        public int getEndIndex(BarSeries series) { throw new UnsupportedOperationException(); }

        @Override
        public TradeType getStartingType() { throw new UnsupportedOperationException(); }

        @Override
        public String getName() { throw new UnsupportedOperationException(); }

        @Override
        public boolean isClosed() { throw new UnsupportedOperationException(); }
    }

    private SimpleNumFactory numFactory;

    @Before
    public void setUp() {
        numFactory = new SimpleNumFactory();
    }

    @Test
    public void testSingleClosedLongPosition() {
        // Bar series with two bars (indices 0 and 1)
        List<Bar> bars = List.of(
                new SimpleBar(numFactory.numOf(10)),
                new SimpleBar(numFactory.numOf(20))
        );
        BarSeries series = new SimpleBarSeries(bars);

        // Position: long (buy at 10, sell at 20)
        Position position = new Position(
                new SimpleTrade(0, true, numFactory.numOf(10)),
                new SimpleTrade(1, false, numFactory.numOf(20))
        );

        CashFlow cashFlow = new CashFlow(series, position);

        // Verify series size
        assertEquals(series.getBarCount(), cashFlow.getSize());

        // Cash flow should start at 1 and double to 2 at the exit bar
        assertEquals(1.0, cashFlow.getValue(0).doubleValue(), 1e-9);
        assertEquals(2.0, cashFlow.getValue(1).doubleValue(), 1e-9);

        // BarSeries reference must be the same
        assertSame(series, cashFlow.getBarSeries());

        // Unstable bars count is always 0
        assertEquals(0, cashFlow.getCountOfUnstableBars());
    }

    @Test
    public void testSingleClosedShortPosition() {
        // Bar series with two bars
        List<Bar> bars = List.of(
                new SimpleBar(numFactory.numOf(20)), // entry price (sell)
                new SimpleBar(numFactory.numOf(10))  // exit price (buy)
        );
        BarSeries series = new SimpleBarSeries(bars);

        // Position: short (sell at 20, buy at 10)
        Position position = new Position(
                new SimpleTrade(0, false, numFactory.numOf(20)),
                new SimpleTrade(1, true, numFactory.numOf(10))
        );

        CashFlow cashFlow = new CashFlow(series, position);

        // Expected ratio = 2 - (10/20) = 1.5
        assertEquals(1.0, cashFlow.getValue(0).doubleValue(), 1e-9);
        assertEquals(1.5, cashFlow.getValue(1).doubleValue(), 1e-9);
    }

    @Test
    public void testTradingRecordMultiplePositionsWithPadding() {
        // Bar series with four bars (indices 0..3)
        List<Bar> bars = List.of(
                new SimpleBar(numFactory.numOf(10)), // 0
                new SimpleBar(numFactory.numOf(20)), // 1
                new SimpleBar(numFactory.numOf(30)), // 2
                new SimpleBar(numFactory.numOf(15))  // 3 (no trade, only padding)
        );
        BarSeries series = new SimpleBarSeries(bars);

        // First position: long 10 -> 20 (indices 0..1)
        Position longPos = new Position(
                new SimpleTrade(0, true, numFactory.numOf(10)),
                new SimpleTrade(1, false, numFactory.numOf(20))
        );

        // Second position: short 30 -> 15 (indices 2..3)
        Position shortPos = new Position(
                new SimpleTrade(2, false, numFactory.numOf(30)),
                new SimpleTrade(3, true, numFactory.numOf(15))
        );

        List<Position> positions = List.of(longPos, shortPos);
        // Current position is closed (empty) so no extra calculation
        Position closedCurrent = new Position();

        TradingRecord record = new SimpleTradingRecord(positions, closedCurrent);

        CashFlow cashFlow = new CashFlow(series, record);

        // Expected cash flow values:
        // index 0 : 1.0 (initial)
        // index 1 : 2.0 (long double)
        // index 2 : 3.0 (short multiplies previous by 1.5)
        // index 3 : 3.0 (padding)
        assertEquals(1.0, cashFlow.getValue(0).doubleValue(), 1e-9);
        assertEquals(2.0, cashFlow.getValue(1).doubleValue(), 1e-9);
        assertEquals(3.0, cashFlow.getValue(2).doubleValue(), 1e-9);
        assertEquals(3.0, cashFlow.getValue(3).doubleValue(), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpenPositionThrowsExceptionInSingleConstructor() {
        // Series with a single bar
        List<Bar> bars = List.of(new SimpleBar(numFactory.numOf(10)));
        BarSeries series = new SimpleBarSeries(bars);

        // Open position (no exit)
        Position openPos = new Position(
                new SimpleTrade(0, true, numFactory.numOf(10)),
                null // no exit trade
        );

        // Should throw IllegalArgumentException because position is not closed
        new CashFlow(series, openPos);
    }
}
###Test END##