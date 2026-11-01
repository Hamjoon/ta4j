###Test START##
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

/**
 * Unit tests for {@link CashFlow}.
 *
 * The tests focus on the public API of {@code CashFlow} and on the
 * interaction between the constructors and the accessor methods.
 *
 * All objects are created through the public factories provided by the
 * library – no mocking or subclassing of library classes is performed.
 */
public class CashFlowTest {

    /**
     * Helper method to create a minimal {@link BarSeries} with a single bar.
     * The bar uses the default {@link NumFactory} supplied by the library
     * (DecimalNumFactory in the reference implementation). The close price
     * of the bar is set to {@code 100}.
     */
    private BarSeries createSingleBarSeries() {
        NumFactory numFactory = org.ta4j.core.num.DecimalNumFactory.getInstance();
        Num price = numFactory.numOf(100);
        // Build a single‑bar series.  The {@code BaseBarSeriesBuilder} allows us to
        // directly supply a list containing one bar.  For the purpose of the test
        // we can use the bar builder supplied by the series itself.
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .withNumFactory(numFactory)
                .withBars(Collections.singletonList(
                        // The builder creates a bar with the same price for open,
                        // high, low and close – this is sufficient for cash‑flow
                        // calculations.
                        new org.ta4j.core.BaseBar(numFactory.numOf(0), price, price, price, price, numFactory.one())
                ))
                .build();
        return series;
    }

    /**
     * Test that the default constructor (the one that receives only a
     * {@link BarSeries} and a {@link TradingRecord}) creates a {@code CashFlow}
     * instance with the expected default values:
     * <ul>
     *   <li>Equity‑curve mode is {@link EquityCurveMode#MARK_TO_MARKET}</li>
     *   <li>The internal value list is sized according to the bar series</li>
     *   <li> {@link #getCountOfUnstableBars()} returns {@code 0}</li>
     * </ul>
     */
    @Test
    public void testDefaultConstructorAndAccessors() {
        BarSeries series = createSingleBarSeries();

        // A trading record with no trades – this is a valid (empty) record.
        BaseTradingRecord tradingRecord = new BaseTradingRecord();

        CashFlow cashFlow = new CashFlow(series, tradingRecord);

        // The series snapshot returned by CashFlow must have the same name
        // and the same number of bars as the original series, but it must be
        // a different instance (snapshot semantics).
        BarSeries snapshot = cashFlow.getBarSeries();
        assertNotSame("Snapshot must be a different instance", series, snapshot);
        assertEquals("Series name must be preserved", series.getName(), snapshot.getName());
        assertEquals("Bar count must be preserved", series.getBarCount(), snapshot.getBarCount());

        // Default mode
        assertEquals(EquityCurveMode.MARK_TO_MARKET, cashFlow.getEquityCurveMode());

        // Size must reflect the underlying series
        assertEquals(series.getBarCount(), cashFlow.getSize());

        // No unstable bars are defined for CashFlow
        assertEquals(0, cashFlow.getCountOfUnstableBars());
    }

    /**
     * Verify that the constructor which receives an explicit {@link EquityCurveMode}
     * stores that mode and that the getter returns the same instance.
     */
    @Test
    public void testConstructorWithEquityCurveMode() {
        BarSeries series = createSingleBarSeries();
        BaseTradingRecord tradingRecord = new BaseTradingRecord();

        CashFlow cashFlow = new CashFlow(series, tradingRecord, EquityCurveMode.REALIZED);

        assertEquals(EquityCurveMode.REALIZED, cashFlow.getEquityCurveMode());
    }

    /**
     * Test the windowed constructor (startIndex / finalIndex) – the internal
     * value list should be correctly sized to the window length.
     */
    @Test
    public void testWindowedConstructor() {
        BarSeries series = createSingleBarSeries(); // 1 bar, index 0
        BaseTradingRecord tradingRecord = new BaseTradingRecord();

        // Use a window that starts at index 0 and ends at index 0 (the only bar)
        CashFlow cashFlow = new CashFlow(series, tradingRecord, 0, 0, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // The internal list must contain exactly one element (the value for index 0)
        assertEquals(1, cashFlow.getSize());
    }

    /**
     * Ensure that {@link CashFlow#getValue(int)} returns the initial value
     * (which is {@code Num.one()} for every index) when no trades have been
     * recorded.
     */
    @Test
    public void testGetValueInitialOne() {
        BarSeries series = createSingleBarSeries();
        BaseTradingRecord tradingRecord = new BaseTradingRecord();

        CashFlow cashFlow = new CashFlow(series, tradingRecord);

        Num value = cashFlow.getValue(0);
        assertNotNull("Value must not be null", value);
        assertTrue("Initial value must be equal to one", value.isEqual(series.numFactory().one()));
    }

    /**
     * Test that a {@link CashFlow} built from a single closed {@link Position}
     * (with entry and exit) does not throw any exception and returns a non‑null
     * series snapshot.
     */
    @Test
    public void testCashFlowFromSingleClosedPosition() {
        BarSeries series = createSingleBarSeries();

        // Create a simple long position: buy at index 0, sell at index 0
        Trade entry = Trade.buyAt(0, series);
        Trade exit = Trade.sellAt(0, series);
        Position position = new Position(entry, exit);

        CashFlow cashFlow = new CashFlow(series, position, EquityCurveMode.MARK_TO_MARKET);

        // The snapshot must be present and correctly sized
        BarSeries snapshot = cashFlow.getBarSeries();
        assertNotNull(snapshot);
        assertEquals(1, snapshot.getBarCount());

        // The cash‑flow value after the closed trade should still be one
        // because the default implementation multiplies by the ratio which,
        // for a zero‑profit trade (buy and sell at same price), equals one.
        Num value = cashFlow.getValue(0);
        assertTrue(value.isEqual(series.numFactory().one()));
    }

    /**
     * Verify that {@link CashFlow#getBarSeries()} always returns a fresh
     * snapshot – mutating the returned series must not affect the internal
     * series stored inside {@code CashFlow}.
     */
    @Test
    public void testBarSeriesSnapshotIndependence() {
        BarSeries series = createSingleBarSeries();
        BaseTradingRecord tradingRecord = new BaseTradingRecord();

        CashFlow cashFlow = new CashFlow(series, tradingRecord);

        BarSeries snapshot1 = cashFlow.getBarSeries();
        BarSeries snapshot2 = cashFlow.getBarSeries();

        // The two snapshots must be different objects
        assertNotSame(snapshot1, snapshot2);
        // Changing one snapshot (e.g., adding a bar) must not affect the other
        NumFactory nf = series.numFactory();
        snapshot1.addBar(new org.ta4j.core.BaseBar(nf.numOf(1), nf.numOf(200), nf.numOf(200), nf.numOf(200), nf.numOf(200), nf.one()));
        assertEquals(1, snapshot2.getBarCount()); // still only the original bar
    }

    /**
     * Edge case: construct a {@code CashFlow} with a start index greater than the
     * series end index. The internal list should be empty and calls to
     * {@link CashFlow#getValue(int)} must throw {@link IndexOutOfBoundsException}.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testStartIndexBeyondSeries() {
        BarSeries series = createSingleBarSeries(); // end index = 0
        BaseTradingRecord tradingRecord = new BaseTradingRecord();

        // startIndex = 5 (beyond series end)
        CashFlow cashFlow = new CashFlow(series, tradingRecord, 5, 5,
                EquityCurveMode.MARK_TO_MARKET, OpenPositionHandling.MARK_TO_MARKET);

        // The values list is empty; accessing any index should fail.
        cashFlow.getValue(0);
    }

    /**
     * Verify that the internal helper {@code multiplyRange} works as expected
     * via the public {@code calculatePosition} method when the position yields a
     * profit.  The test creates a long position bought at 50 and sold at 100,
     * which should double the cash‑flow value (ratio = 2).  After calculation,
     * the stored value must be {@code 2}.
     */
    @Test
    public void testCalculatePositionProfitMarkToMarket() {
        // Build a series with two bars: price 50 at index 0, price 100 at index 1
        NumFactory nf = org.ta4j.core.num.DecimalNumFactory.getInstance();
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("profit-series")
                .withNumFactory(nf)
                .withBars(List.of(
                        new org.ta4j.core.BaseBar(nf.numOf(0), nf.numOf(50), nf.numOf(50), nf.numOf(50), nf.numOf(50), nf.one()),
                        new org.ta4j.core.BaseBar(nf.numOf(1), nf.numOf(100), nf.numOf(100), nf.numOf(100), nf.numOf(100), nf.one())
                ))
                .build();

        // Long position: buy at index 0, sell at index 1
        Trade entry = Trade.buyAt(0, nf.numOf(50), nf.one(), null);
        Trade exit = Trade.sellAt(1, nf.numOf(100), nf.one(), null);
        Position position = new Position(entry, exit);

        BaseTradingRecord record = new BaseTradingRecord(position);

        CashFlow cashFlow = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // After calculation, the cash‑flow ratio should be 2 (100 / 50)
        Num expected = nf.numOf(2);
        Num actual = cashFlow.getValue(1); // value at the exit bar
        assertTrue("Cash‑flow should reflect profit ratio 2", actual.isEqual(expected));
    }

    /**
     * Verify that when the position is a short trade (sell first, buy later)
     * the cash‑flow ratio is computed using the short‑trade formula.
     */
    @Test
    public void testCalculatePositionProfitShortTrade() {
        NumFactory nf = org.ta4j.core.num.DecimalNumFactory.getInstance();
        BarSeries series = new BaseBarSeriesBuilder()
                .withName("short-profit-series")
                .withNumFactory(nf)
                .withBars(List.of(
                        new org.ta4j.core.BaseBar(nf.numOf(0), nf.numOf(100), nf.numOf(100), nf.numOf(100), nf.numOf(100), nf.one()),
                        new org.ta4j.core.BaseBar(nf.numOf(1), nf.numOf(50), nf.numOf(50), nf.numOf(50), nf.numOf(50), nf.one())
                ))
                .build();

        // Short position: sell at 100 (index 0), buy at 50 (index 1)
        Trade entry = Trade.sellAt(0, nf.numOf(100), nf.one(), null);
        Trade exit = Trade.buyAt(1, nf.numOf(50), nf.one(), null);
        Position position = new Position(entry, exit);

        BaseTradingRecord record = new BaseTradingRecord(position);

        CashFlow cashFlow = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // For a short trade the ratio is 2 - (exit/entry) = 2 - (50/100) = 1.5
        Num expected = nf.numOf(1.5);
        Num actual = cashFlow.getValue(1);
        assertTrue("Short‑trade cash‑flow ratio should be 1.5", actual.isEqual(expected));
    }
}
###Test END##