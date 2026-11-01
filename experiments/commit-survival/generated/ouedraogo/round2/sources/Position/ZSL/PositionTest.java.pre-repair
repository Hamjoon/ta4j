package org.ta4j.core;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 tests for {@link Position}.
 *
 * The tests only use the public API that is declared in the provided source
 * code and the listed dependency signatures.  No additional library classes
 * (e.g. DecimalNum, BaseBarSeries) are used – all numeric values are created
 * with the {@code NaN} implementation which is part of the public API.
 */
public class PositionTest {

    /** Helper to create a zero Num (price / amount = 0). */
    private static Num zero() {
        return NaN.zero();
    }

    /** Helper to create a one Num (price / amount = 1). */
    private static Num one() {
        return NaN.one();
    }

    /** Simple CostModel that always returns zero and never equals another model. */
    private static CostModel differentZeroCostModel() {
        return new CostModel() {
            @Override
            public Num calculate(Position position, int finalIndex) {
                return zero();
            }

            @Override
            public Num calculate(Position position) {
                return zero();
            }

            @Override
            public Num calculate(Num price, Num amount) {
                return zero();
            }

            @Override
            public boolean equals(CostModel otherModel) {
                // never equal to any other model (including itself) – forces the
                // Position constructor to reject mismatched models.
                return false;
            }
        };
    }

    @Test
    public void testDefaultConstructorAndStartingType() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void testConstructorWithCustomStartingType() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullStartingType() {
        new Position(null);
    }

    @Test
    public void testOperateCreatesEntryAndExitTrades() {
        Position p = new Position(); // starts with BUY
        // first operate -> entry
        Trade entry = p.operate(0, one(), one());
        assertNotNull(entry);
        assertEquals(0, entry.getIndex());
        assertEquals(TradeType.BUY, entry.getType());
        assertTrue(p.isOpened());
        assertEquals(entry, p.getEntry());
        assertNull(p.getExit());

        // second operate -> exit (SELL)
        Trade exit = p.operate(5, one(), one());
        assertNotNull(exit);
        assertEquals(5, exit.getIndex());
        assertEquals(TradeType.SELL, exit.getType());
        assertTrue(p.isClosed());
        assertEquals(exit, p.getExit());

        // further operate should return null (position already closed)
        assertNull(p.operate(10, one(), one()));
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateThrowsWhenIndexBeforeEntry() {
        Position p = new Position();
        p.operate(3, one(), one()); // creates entry at index 3
        // now try to create exit with a lower index
        p.operate(2, one(), one());
    }

    @Test
    public void testEqualityAndHashCode() {
        Position p1 = new Position();
        Position p2 = new Position();

        // both new -> equal
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // after entry creation they are no longer equal
        p1.operate(0, one(), one());
        assertNotEquals(p1, p2);

        // make p2 identical
        p2.operate(0, one(), one());
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // close both positions at same index
        p1.operate(5, one(), one());
        p2.operate(5, one(), one());
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToStringContainsEntryAndExit() {
        Position p = new Position();
        p.operate(0, one(), one());
        p.operate(2, one(), one());
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
    }

    @Test
    public void testProfitAndLossWhenClosedWithZeroValues() {
        Position p = new Position();
        // use zero price and amount – all calculations should result in zero
        p.operate(0, zero(), zero()); // entry
        p.operate(1, zero(), zero()); // exit

        Num profit = p.getProfit();
        assertTrue(profit.isZero());

        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());

        // gross profit must also be zero
        assertTrue(p.getGrossProfit().isZero());

        // gross return for a zero‑price position is NaN (division by zero);
        // we only assert that the method does not throw.
        Num grossReturn = p.getGrossReturn();
        assertNotNull(grossReturn);
    }

    @Test
    public void testProfitWhenOpenReturnsZero() {
        Position p = new Position();
        p.operate(0, one(), one()); // only entry
        assertTrue(p.isOpened());

        Num profit = p.getProfit();
        assertTrue(profit.isZero());

        // hasProfit / hasLoss should be false because profit == 0
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGetGrossReturnBuyAndSellScenarios() {
        // BUY position with profit 4% -> entry 100, exit 104
        Position buyPos = new Position();
        buyPos.operate(0, NaN.numOf(100), one()); // entry price 100
        buyPos.operate(1, NaN.numOf(104), one()); // exit price 104

        Num grossReturnBuy = buyPos.getGrossReturn();
        // 104 / 100 = 1.04
        assertEquals(NaN.numOf(1.04), grossReturnBuy);

        // SELL position with loss 4% -> entry 100, exit 96
        Position sellPos = new Position(TradeType.SELL);
        sellPos.operate(0, NaN.numOf(100), one()); // entry (sell) price 100
        sellPos.operate(1, NaN.numOf(96), one());  // exit (buy) price 96

        Num grossReturnSell = sellPos.getGrossReturn();
        // (96/100 - 1) = -0.04 -> negate -> 0.04 -> +1 = 0.96
        assertEquals(NaN.numOf(0.96), grossReturnSell);
    }

    @Test
    public void testGetGrossReturnWithBarSeriesDelegates() {
        // For this test we only need to ensure that the overload that accepts a
        // BarSeries does not throw.  We use a minimal anonymous BarSeries that
        // returns the same Num for any price request.
        BarSeries series = new BarSeries() {
            @Override public String getName() { return "test"; }
            @Override public Num num() { return NaN; }
            @Override public Bar getBar(int i) { return null; }
            @Override public int getBarCount() { return 0; }
            @Override public List<Bar> getBarData() { return null; }
            @Override public int getBeginIndex() { return 0; }
            @Override public int getEndIndex() { return 0; }
            @Override public int getMaximumBarCount() { return 0; }
            @Override public void setMaximumBarCount(int maximumBarCount) {}
            @Override public int getRemovedBarsCount() { return 0; }
            @Override public void addBar(Bar bar) {}
            @Override public void addBar(Bar bar, boolean replace) {}
            @Override public void addBar(Duration timePeriod, ZonedDateTime endTime) {}
            @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                          Number lowPrice, Number closePrice) {}
            @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                          Number lowPrice, Number closePrice, Number volume) {}
            @Override public void addBar(ZonedDateTime endTime, Number openPrice, Number highPrice,
                                          Number lowPrice, Number closePrice, Number volume, Number amount) {}
            @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                          Number openPrice, Number highPrice, Number lowPrice,
                                          Number closePrice, Number volume) {}
            @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                          Number openPrice, Number highPrice, Number lowPrice,
                                          Number closePrice, Number volume, Number amount) {}
            @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                          String lowPrice, String closePrice) {}
            @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                          String lowPrice, String closePrice, String volume) {}
            @Override public void addBar(ZonedDateTime endTime, String openPrice, String highPrice,
                                          String lowPrice, String closePrice, String volume, String amount) {}
            @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                          Num lowPrice, Num closePrice, Num volume) {}
            @Override public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice,
                                          Num lowPrice, Num closePrice, Num volume, Num amount) {}
            @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                          Num openPrice, Num highPrice, Num lowPrice,
                                          Num closePrice, Num volume) {}
            @Override public void addBar(Duration timePeriod, ZonedDateTime endTime,
                                          Num openPrice, Num highPrice, Num lowPrice,
                                          Num closePrice, Num volume, Num amount) {}
            @Override public void addTrade(Number tradeVolume, Number tradePrice) {}
            @Override public void addTrade(String tradeVolume, String tradePrice) {}
            @Override public void addTrade(Num tradeVolume, Num tradePrice) {}
            @Override public void addPrice(Num price) {}
            @Override public void addPrice(String price) {}
            @Override public void addPrice(Number price) {}
            @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return null; }
            @Override public String getSeriesPeriodDescription() { return ""; }
            @Override public boolean isEmpty() { return true; }
        };

        Position p = new Position();
        p.operate(0, NaN.numOf(10), one());
        p.operate(1, NaN.numOf(20), one());

        // The call should succeed and return a Num (NaN in this minimal setup)
        Num result = p.getGrossReturn(series);
        assertNotNull(result);
    }

    @Test
    public void testPositionCostUsesProvidedCostModels() {
        CostModel zeroCost = new ZeroCostModel();
        CostModel customCost = differentZeroCostModel();

        // Build a position with custom transaction cost model
        Position p = new Position(TradeType.BUY, customCost, zeroCost);
        p.operate(0, one(), one()); // entry
        p.operate(2, one(), one()); // exit

        // getPositionCost should invoke our custom model (which returns zero)
        Num cost = p.getPositionCost();
        assertTrue(cost.isZero());
    }

    @Test
    public void testConstructorWithMismatchedTradeTypesThrows() {
        Trade buy = Trade.buyAt(0, one(), one());
        Trade anotherBuy = Trade.buyAt(1, one(), one());

        try {
            new Position(buy, anotherBuy);
            fail("Expected IllegalArgumentException due to identical trade types");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithMismatchedCostModelsThrows() {
        Trade buy = Trade.buyAt(0, one(), one()); // uses default ZeroCostModel
        Trade sell = Trade.sellAt(1, one(), one());

        CostModel different = differentZeroCostModel();

        try {
            new Position(buy, sell, different, new ZeroCostModel());
            fail("Expected IllegalArgumentException because trade cost model differs from supplied model");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetHoldingCostDelegatesToModel() {
        CostModel holding = new CostModel() {
            @Override public Num calculate(Position position, int finalIndex) { return NaN.numOf(5); }
            @Override public Num calculate(Position position) { return NaN.numOf(3); }
            @Override public Num calculate(Num price, Num amount) { return zero(); }
            @Override public boolean equals(CostModel otherModel) { return otherModel == this; }
        };

        Position p = new Position(TradeType.BUY, new ZeroCostModel(), holding);
        p.operate(0, one(), one());
        p.operate(1, one(), one());

        assertEquals(NaN.numOf(3), p.getHoldingCost());
        assertEquals(NaN.numOf(5), p.getHoldingCost(10));
    }
}
