package org.ta4j.core;
import org.ta4j.core.analysis.cost.CostModel;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The suite covers:
 * <ul>
 *   <li>Construction (including illegal arguments)</li>
 *   <li>State‑transition methods {@code isNew()}, {@code isOpened()}, {@code isClosed()}</li>
 *   <li>Operation handling via {@code operate(...)} (normal flow and error cases)</li>
 *   <li>Profit / loss calculations (gross, net, profit sign helpers)</li>
 *   <li>Return calculations for BUY and SELL positions</li>
 *   <li>Cost calculations (transaction &amp; holding – using {@link ZeroCostModel})</li>
 *   <li>Equality, hashCode and {@code toString()}</li>
 * </ul>
 *
 * Only public API of the library is used – no mocks or subclassing.
 */
public class PositionTest {

    // -------------------------------------------------------------------------
    // Helper factories
    // -------------------------------------------------------------------------

    /** Simple {@link BarSeries} implementation used for price‑based constructions. */
    private BarSeries series;

    /** Factory method for {@link Num} values (DecimalNum is the reference implementation). */
    private static Num num(double d) {
        return DecimalNum.valueOf(d);
    }

    @Before
    public void setUp() {
        // BaseBarSeries is the standard concrete implementation in ta4j.
        series = new BaseBarSeries("test-series");
        // Add a few dummy bars so that price‑based methods (e.g. getPricePerAsset(BarSeries))
        // have something to work with.
        series.addBar(series.barBuilder().timePeriod(java.time.Duration.ofDays(1))
                .openPrice(num(100)).highPrice(num(110)).lowPrice(num(90)).closePrice(num(105))
                .volume(num(1000)).build());
        series.addBar(series.barBuilder().timePeriod(java.time.Duration.ofDays(1))
                .openPrice(num(106)).highPrice(num(115)).lowPrice(num(100)).closePrice(num(110))
                .volume(num(1200)).build());
    }

    // -------------------------------------------------------------------------
    // Constructor tests
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullStartingType_throws() {
        new Position(null);
    }

    @Test
    public void constructor_defaultStartingType_isBuy() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
    }

    @Test
    public void constructor_withEntryAndExit_validatesTradeTypes() {
        Trade entry = Trade.buyAt(0, series, num(1000));
        Trade exit = Trade.sellAt(1, series, num(1000));
        Position p = new Position(entry, exit);
        assertTrue(p.isClosed());
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_sameTradeTypes_throws() {
        Trade entry = Trade.buyAt(0, series, num(1000));
        Trade exit = Trade.buyAt(1, series, num(1000));
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_mismatchedCostModel_throws() {
        CostModel zero = new ZeroCostModel();
        CostModel custom = new ZeroCostModel(); // different instance, but equal – we need a non‑equal model
        // For the purpose of the test we create an anonymous CostModel that is not equal to zero.
        CostModel other = new CostModel() {
            @Override public Num calculate(Position position, int finalIndex) { return num(0); }
            @Override public Num calculate(Position position) { return num(0); }
            @Override public Num calculate(Num price, Num amount) { return num(0); }
            @Override public boolean equals(CostModel otherModel) { return false; }
        };
        Trade entry = Trade.buyAt(0, series, num(1000));
        Trade exit = Trade.sellAt(1, series, num(1000));
        new Position(entry, exit, other, zero);
    }

    // -------------------------------------------------------------------------
    // State transition tests (isNew / isOpened / isClosed)
    // -------------------------------------------------------------------------

    @Test
    public void stateTransitions_viaOperate() {
        Position p = new Position(); // new, no trades
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        // first operate creates entry
        Trade entry = p.operate(0, num(100), num(10));
        assertNotNull(entry);
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());
        assertEquals(entry, p.getEntry());

        // second operate creates exit
        Trade exit = p.operate(5, num(110), num(10));
        assertNotNull(exit);
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void operate_exitIndexBeforeEntry_throws() {
        Position p = new Position();
        p.operate(10, num(100), num(5)); // creates entry at index 10
        // now try to close before entry index
        p.operate(5, num(90), num(5));
    }

    // -------------------------------------------------------------------------
    // Equality, hashCode and toString
    // -------------------------------------------------------------------------

    @Test
    public void equalsAndHashCode_behaveCorrectly() {
        Trade entry1 = Trade.buyAt(0, series, num(1000));
        Trade exit1   = Trade.sellAt(1, series, num(1000));
        Position p1 = new Position(entry1, exit1);

        Trade entry2 = Trade.buyAt(0, series, num(1000));
        Trade exit2   = Trade.sellAt(1, series, num(1000));
        Position p2 = new Position(entry2, exit2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // different exit price -> not equal
        Trade exitDiff = Trade.sellAt(1, series, num(2000));
        Position p3 = new Position(entry1, exitDiff);
        assertNotEquals(p1, p3);
    }

    @Test
    public void toString_containsEntryAndExit() {
        Trade entry = Trade.buyAt(0, series, num(1000));
        Trade exit   = Trade.sellAt(1, series, num(1000));
        Position p = new Position(entry, exit);
        String s = p.toString();
        assertTrue(s.contains("Entry:"));
        assertTrue(s.contains("exit:"));
        assertTrue(s.contains(entry.toString()));
        assertTrue(s.contains(exit.toString()));
    }

    // -------------------------------------------------------------------------
    // Profit / loss calculations (closed position)
    // -------------------------------------------------------------------------

    @Test
    public void profit_closedBuyPosition_correct() {
        // BUY 10 @ 100, SELL 10 @ 110  -> gross profit = (110-100)*10 = 100
        Trade entry = Trade.buyAt(0, series, num(10));
        entry = new Trade(0, TradeType.BUY, num(100), num(10), new ZeroCostModel()); // price per asset = 100
        Trade exit = Trade.sellAt(1, series, num(10));
        exit = new Trade(1, TradeType.SELL, num(110), num(10), new ZeroCostModel());

        Position p = new Position(entry, exit);
        Num profit = p.getProfit();
        assertEquals(num(100), profit);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void profit_closedSellPosition_correct() {
        // SHORT 5 @ 200, BUY 5 @ 180 -> gross profit = (200-180)*5 = 100
        Trade entry = Trade.sellAt(0, series, num(5));
        entry = new Trade(0, TradeType.SELL, num(200), num(5), new ZeroCostModel());
        Trade exit = Trade.buyAt(1, series, num(5));
        exit = new Trade(1, TradeType.BUY, num(180), num(5), new ZeroCostModel());

        Position p = new Position(entry, exit);
        Num profit = p.getProfit();
        assertEquals(num(100), profit);
        assertTrue(p.hasProfit());
    }

    @Test
    public void profit_closedPosition_withLoss() {
        // BUY 1 @ 50, SELL 1 @ 40 -> loss = -10
        Trade entry = new Trade(0, TradeType.BUY, num(50), num(1), new ZeroCostModel());
        Trade exit = new Trade(1, TradeType.SELL, num(40), num(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertEquals(num(-10), p.getProfit());
        assertTrue(p.hasLoss());
        assertFalse(p.hasProfit());
    }

    @Test
    public void profit_openPosition_returnsZero() {
        Position p = new Position();
        p.operate(0, num(100), num(1)); // entry only
        assertEquals(num(0), p.getProfit());
    }

    @Test
    public void grossProfit_closedBuyPosition() {
        Trade entry = new Trade(0, TradeType.BUY, num(100), num(2), new ZeroCostModel());
        Trade exit = new Trade(1, TradeType.SELL, num(115), num(2), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertEquals(num(30), p.getGrossProfit()); // (115*2) - (100*2) = 30
    }

    @Test
    public void grossProfit_closedSellPosition_negativeAdjusted() {
        // Short 3 @ 200, cover 3 @ 190
        Trade entry = new Trade(0, TradeType.SELL, num(200), num(3), new ZeroCostModel());
        Trade exit = new Trade(1, TradeType.BUY, num(190), num(3), new ZeroCostModel());
        Position p = new Position(entry, exit);
        // exit value - entry value = (190*3) - (200*3) = -30, then negated because entry.isSell()
        assertEquals(num(30), p.getGrossProfit());
    }

    @Test
    public void grossProfit_openPosition_usesCurrentPrice() {
        Position p = new Position();
        p.operate(0, num(100), num(5)); // entry BUY 5 @ 100
        Num gross = p.getGrossProfit(num(108)); // simulate price 108
        // (108*5) - (100*5) = 40
        assertEquals(num(40), gross);
    }

    // -------------------------------------------------------------------------
    // Gross return calculations
    // -------------------------------------------------------------------------

    @Test
    public void grossReturn_buyPosition() {
        Trade entry = new Trade(0, TradeType.BUY, num(50), num(1), new ZeroCostModel());
        Trade exit = new Trade(1, TradeType.SELL, num(55), num(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        Num ret = p.getGrossReturn(); // 55 / 50 = 1.1
        assertEquals(num(1.1), ret);
    }

    @Test
    public void grossReturn_sellPosition() {
        Trade entry = new Trade(0, TradeType.SELL, num(80), num(1), new ZeroCostModel());
        Trade exit = new Trade(1, TradeType.BUY, num(72), num(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        // For sell: 1 - (exit/entry - 1) = 1 - (72/80 -1) = 1 - (-0.1) = 1.1
        Num ret = p.getGrossReturn();
        assertEquals(num(1.1), ret);
    }

    @Test
    public void grossReturn_openPosition_returnsZero() {
        Position p = new Position();
        p.operate(0, num(200), num(1));
        assertEquals(num(0), p.getGrossReturn());
    }

    // -------------------------------------------------------------------------
    // Position cost (transaction + holding) – using ZeroCostModel => zero
    // -------------------------------------------------------------------------

    @Test
    public void positionCost_closedZeroModel_isZero() {
        Trade entry = new Trade(0, TradeType.BUY, num(10), num(1), new ZeroCostModel());
        Trade exit = new Trade(1, TradeType.SELL, num(12), num(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertEquals(num(0), p.getPositionCost());
    }

    @Test
    public void positionCost_openZeroModel_isZero() {
        Position p = new Position();
        p.operate(0, num(100), num(2));
        assertEquals(num(0), p.getPositionCost(5));
    }

    // -------------------------------------------------------------------------
    // Helper methods – zero() uses entry's NumFactory, we verify it works
    // -------------------------------------------------------------------------

    @Test
    public void zeroFactoryMatchesEntryFactory() {
        Position p = new Position();
        p.operate(0, num(10), num(1));
        // zero() is private; we indirectly test via getProfit on an opened position
        assertEquals(num(0), p.getProfit());
    }
}
