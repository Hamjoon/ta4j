###Test START##
```java
package org.ta4j.core;

import static org.junit.Assert.*;
import static org.ta4j.core.num.NaN.NaN;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The tests cover:
 * <ul>
 *   <li>All public constructors (including error scenarios)</li>
 *   <li>State query methods (isNew, isOpened, isClosed)</li>
 *   <li>operate(...) behaviour and its IllegalStateException</li>
 *   <li>Profit / loss calculations (gross & net, open & closed)</li>
 *   <li>Return calculations (gross return for BUY/SELL and via BarSeries)</li>
 *   <li>equals, hashCode and toString contracts</li>
 *   <li>Cost model integration (ZeroCostModel and a custom cost model)</li>
 * </ul>
 */
public class PositionTest {

    private static final Num PRICE_10 = DecimalNum.valueOf(10);
    private static final Num PRICE_12 = DecimalNum.valueOf(12);
    private static final Num PRICE_8  = DecimalNum.valueOf(8);
    private static final Num AMOUNT_5 = DecimalNum.valueOf(5);
    private static final Num ZERO = DecimalNum.ZERO;

    private BarSeries series;

    @Before
    public void setUp() {
        // a minimal bar series – only the Num factory is required for the tests
        series = new BaseBarSeriesBuilder().withName("test-series").build();
    }

    /* ---------------------------------------------------------------------- */
    /* Constructor tests                                                      */
    /* ---------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenStartingTypeIsNull() {
        new Position(null);
    }

    @Test
    public void shouldCreateBuyPositionByDefault() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
        assertTrue(p.isNew());
    }

    @Test
    public void shouldCreatePositionFromEntryAndExitWithSameCostModel() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position p = new Position(entry, exit);
        assertEquals(entry, p.getEntry());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEntryAndExitWithSameType() {
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade exit  = Trade.buyAt(1, PRICE_12, AMOUNT_5); // same BUY type
        new Position(entry, exit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectMismatchedCostModels() {
        CostModel zero = new ZeroCostModel();
        CostModel custom = new CostModel() {
            @Override public Num calculate(Position p, int i) { return ZERO; }
            @Override public Num calculate(Position p) { return ZERO; }
            @Override public Num calculate(Num price, Num amount) { return ZERO; }
            @Override public boolean equals(CostModel other) { return false; }
        };
        Trade entry = Trade.buyAt(0, PRICE_10, AMOUNT_5, zero);
        Trade exit  = Trade.sellAt(1, PRICE_12, AMOUNT_5, custom); // different model
        new Position(entry, exit, zero, zero);
    }

    /* ---------------------------------------------------------------------- */
    /* State query tests                                                     */
    /* ---------------------------------------------------------------------- */

    @Test
    public void testStateTransitions() {
        Position p = new Position();
        assertTrue(p.isNew());
        assertFalse(p.isOpened());
        assertFalse(p.isClosed());

        // first operate creates entry
        p.operate(0, PRICE_10, AMOUNT_5);
        assertFalse(p.isNew());
        assertTrue(p.isOpened());
        assertFalse(p.isClosed());

        // second operate creates exit
        p.operate(2, PRICE_12, AMOUNT_5);
        assertFalse(p.isNew());
        assertFalse(p.isOpened());
        assertTrue(p.isClosed());
    }

    /* ---------------------------------------------------------------------- */
    /* operate(...) tests                                                    */
    /* ---------------------------------------------------------------------- */

    @Test
    public void shouldCreateEntryTradeWhenNew() {
        Position p = new Position(TradeType.SELL);
        Trade t = p.operate(3, PRICE_8, AMOUNT_5);
        assertNotNull(t);
        assertEquals(TradeType.SELL, t.getType());
        assertEquals(3, t.getIndex());
        assertEquals(t, p.getEntry());
        assertNull(p.getExit());
    }

    @Test
    public void shouldCreateExitTradeWhenOpened() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5); // entry (BUY)
        Trade exit = p.operate(5, PRICE_12, AMOUNT_5);
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType()); // complement of BUY
        assertEquals(exit, p.getExit());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfExitIndexBeforeEntry() {
        Position p = new Position();
        p.operate(10, PRICE_10, AMOUNT_5); // entry at 10
        p.operate(5, PRICE_12, AMOUNT_5);  // exit index < entry index -> exception
    }

    @Test
    public void operateWithoutPriceAndAmountUsesNaN() {
        Position p = new Position();
        Trade t = p.operate(0);
        assertEquals(NaN, t.getPricePerAsset());
        assertEquals(NaN, t.getAmount());
    }

    /* ---------------------------------------------------------------------- */
    /* Profit / loss calculations                                            */
    /* ---------------------------------------------------------------------- */

    @Test
    public void profitIsZeroWhenPositionIsOpened() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5); // only entry
        assertEquals(ZERO, p.getProfit());
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void closedBuyPositionWithGainHasProfit() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5); // entry BUY @10
        p.operate(2, PRICE_12, AMOUNT_5); // exit SELL @12
        Num profit = p.getProfit(); // (12-10)*5 = 10
        assertEquals(DecimalNum.valueOf(10), profit);
        assertTrue(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void closedSellPositionWithLossHasLoss() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, PRICE_12, AMOUNT_5); // entry SELL @12
        p.operate(3, PRICE_10, AMOUNT_5); // exit BUY @10
        Num profit = p.getProfit(); // -(12-10)*5 = -10
        assertEquals(DecimalNum.valueOf(-10), profit);
        assertTrue(p.hasLoss());
        assertFalse(p.hasProfit());
    }

    @Test
    public void profitForOpenPositionUsesFinalPrice() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5); // entry BUY @10
        // still opened, compute profit as if we closed at price 11
        Num profit = p.getProfit(5, DecimalNum.valueOf(11));
        // (11-10)*5 = 5
        assertEquals(DecimalNum.valueOf(5), profit);
    }

    @Test
    public void grossProfitAndReturnForBuy() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5); // entry BUY
        p.operate(4, PRICE_12, AMOUNT_5); // exit SELL

        Num grossProfit = p.getGrossProfit(); // (12-10)*5 = 10
        assertEquals(DecimalNum.valueOf(10), grossProfit);

        Num grossReturn = p.getGrossReturn(); // 12/10 = 1.2
        assertEquals(DecimalNum.valueOf(1.2), grossReturn);
    }

    @Test
    public void grossProfitAndReturnForSell() {
        Position p = new Position(TradeType.SELL);
        p.operate(0, PRICE_12, AMOUNT_5); // entry SELL
        p.operate(4, PRICE_10, AMOUNT_5); // exit BUY

        Num grossProfit = p.getGrossProfit(); // -(12-10)*5 = -10
        assertEquals(DecimalNum.valueOf(-10), grossProfit);

        // For a sell position the gross return formula yields 0.8333... (1/1.2)
        Num grossReturn = p.getGrossReturn(); // ((10/12)-1).negate() + 1 = 1.166666...
        assertEquals(DecimalNum.valueOf(1.1666666666666667), grossReturn);
    }

    @Test
    public void grossReturnWithExplicitPricesMatchesFormula() {
        Position p = new Position(); // BUY
        p.operate(0, PRICE_10, AMOUNT_5);
        // use getGrossReturn(entryPrice, exitPrice) directly
        Num ret = p.getGrossReturn(PRICE_10, PRICE_12);
        assertEquals(DecimalNum.valueOf(1.2), ret);
    }

    @Test
    public void grossReturnBarSeriesUsesSeriesPrices() {
        // Build a series with two bars (prices 10 and 12)
        series.addBar(DecimalNum.valueOf(10), DecimalNum.valueOf(10),
                DecimalNum.valueOf(10), DecimalNum.valueOf(10), DecimalNum.valueOf(10));
        series.addBar(DecimalNum.valueOf(12), DecimalNum.valueOf(12),
                DecimalNum.valueOf(12), DecimalNum.valueOf(12), DecimalNum.valueOf(12));

        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5);
        p.operate(1, PRICE_12, AMOUNT_5);
        Num ret = p.getGrossReturn(series);
        assertEquals(DecimalNum.valueOf(1.2), ret);
    }

    /* ---------------------------------------------------------------------- */
    /* Cost model integration tests                                          */
    /* ---------------------------------------------------------------------- */

    @Test
    public void zeroCostModelResultsInZeroPositionCost() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5);
        p.operate(2, PRICE_12, AMOUNT_5);
        assertEquals(ZERO, p.getPositionCost());
        assertEquals(ZERO, p.getHoldingCost());
    }

    @Test
    public void customCostModelIsTakenIntoAccount() {
        final Num transactionFee = DecimalNum.valueOf(2);
        CostModel custom = new CostModel() {
            @Override public Num calculate(Position position, int finalIndex) { return transactionFee; }
            @Override public Num calculate(Position position) { return transactionFee; }
            @Override public Num calculate(Num price, Num amount) { return ZERO; }
            @Override public boolean equals(CostModel other) { return false; }
        };
        Position p = new Position(TradeType.BUY, custom, new ZeroCostModel());
        p.operate(0, PRICE_10, AMOUNT_5);
        p.operate(1, PRICE_12, AMOUNT_5);
        assertEquals(transactionFee, p.getPositionCost());
        assertEquals(transactionFee, p.getPositionCost(1));
    }

    /* ---------------------------------------------------------------------- */
    /* Equality, hashCode and toString                                       */
    /* ---------------------------------------------------------------------- */

    @Test
    public void equalsAndHashCodeRespectEntryAndExit() {
        Trade e1 = Trade.buyAt(0, PRICE_10, AMOUNT_5);
        Trade x1 = Trade.sellAt(1, PRICE_12, AMOUNT_5);
        Position p1 = new Position(e1, x1);
        Position p2 = new Position(e1, x1);
        Position p3 = new Position(Trade.buyAt(0, PRICE_10, AMOUNT_5),
                                   Trade.sellAt(1, PRICE_8, AMOUNT_5));

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    public void toStringContainsEntryAndExit() {
        Position p = new Position();
        p.operate(0, PRICE_10, AMOUNT_5);
        p.operate(2, PRICE_12, AMOUNT_5);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
        assertTrue(s.contains("BUY"));
        assertTrue(s.contains("SELL"));
    }
}
```
###Test END##