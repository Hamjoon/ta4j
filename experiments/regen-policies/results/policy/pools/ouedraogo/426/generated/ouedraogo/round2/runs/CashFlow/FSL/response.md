###Test START##
package org.ta4j.core.analysis;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.analysis.EquityCurveMode;
import org.ta4j.core.analysis.OpenPositionHandling;

/**
 * Comprehensive unit tests for {@link CashFlow}.
 *
 * The tests cover:
 * <ul>
 *   <li>Construction with an empty series / empty trading record.</li>
 *   <li>Long and short positions using {@link EquityCurveMode#MARK_TO_MARKET}.</li>
 *   <li>Realised‑only mode ({@link EquityCurveMode#REALIZED}).</li>
 *   <li>Open‑position handling with {@link OpenPositionHandling#IGNORE}.</li>
 *   <li>Utility methods {@link CashFlow#getValue(int)},
 *       {@link CashFlow#getSize()}, {@link CashFlow#getBarSeries()}
 *       and {@link CashFlow#getEquityCurveMode()}.</li>
 * </ul>
 *
 * All objects are created through the public API – no mocking or subclassing
 * of library classes is performed.
 */
public class CashFlowTest {

    /**
     * Helper to build a simple {@link Bar} with the same open/high/low/close
     * price (the close price is the one used by the {@link CashFlow}
     * calculations).
     */
    private static Bar createBar(Num price) {
        // The concrete implementation used by the library is {@code BaseBar}.
        // Its constructor signature is:
        // BaseBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice,
        //        Num highPrice, Num lowPrice, Num closePrice, Num volume)
        // We use a dummy time period and volume – only the close price matters.
        return new org.ta4j.core.BaseBar(
                Duration.ofDays(1),
                ZonedDateTime.now(),
                price,          // open
                price,          // high
                price,          // low
                price,          // close
                price           // volume (arbitrary)
        );
    }

    /**
     * Builds a {@link BarSeries} containing the supplied close prices.
     */
    private static org.ta4j.core.BarSeries buildSeries(double... closePrices) {
        BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder();
        // The series name and NumFactory are taken from the builder defaults.
        // We create the list of bars manually.
        ArrayList<Bar> bars = new ArrayList<>();
        // A temporary series is needed to obtain the NumFactory for creating Num values.
        // We therefore first build an empty series, fetch its factory, then create the bars.
        org.ta4j.core.BarSeries tmpSeries = builder.build();
        NumFactory nf = tmpSeries.numFactory();
        for (double cp : closePrices) {
            bars.add(createBar(nf.numOf(cp)));
        }
        // Re‑build the series with the populated bar list.
        return new BaseBarSeriesBuilder()
                .withName("testSeries")
                .withNumFactory(nf)
                .withBars(bars)
                .build();
    }

    @Test
    public void testEmptySeriesAndEmptyTradingRecord() {
        org.ta4j.core.BarSeries emptySeries = new BaseBarSeriesBuilder().withName("empty").build();
        BaseTradingRecord emptyRecord = new BaseTradingRecord(); // no trades

        CashFlow cf = new CashFlow(emptySeries, emptyRecord);

        // Size should be zero because the series has no bars.
        assertEquals(0, cf.getSize());

        // The underlying values list is empty – calling getValue should throw.
        try {
            cf.getValue(0);
            fail("Expected IndexOutOfBoundsException for empty cash‑flow values");
        } catch (IndexOutOfBoundsException expected) {
            // ok
        }

        // Equity curve mode defaults to MARK_TO_MARKET.
        assertEquals(EquityCurveMode.MARK_TO_MARKET, cf.getEquityCurveMode());

        // The bar series returned must be a snapshot (different instance but equal content).
        assertNotSame(emptySeries, cf.getBarSeries());
        assertEquals(emptySeries.getName(), cf.getBarSeries().getName());
    }

    @Test
    public void testLongPositionMarkToMarket() {
        // Series: close prices 10, 20, 30
        org.ta4j.core.BarSeries series = buildSeries(10, 20, 30);
        NumFactory nf = series.numFactory();

        // Create a trading record with a long entry at index 0 (price 10) and exit at index 2 (price 30)
        BaseTradingRecord record = new BaseTradingRecord();
        record.enter(0, nf.numOf(10), nf.one()); // amount = 1
        record.exit(2, nf.numOf(30), nf.one());

        CashFlow cf = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET);

        // Expected cash‑flow values:
        // index 0 : 1 (entry point)
        // index 1 : 20 / 10 = 2
        // index 2 : 30 / 10 = 3
        assertEquals(1.0, cf.getValue(0).doubleValue(), 1e-9);
        assertEquals(2.0, cf.getValue(1).doubleValue(), 1e-9);
        assertEquals(3.0, cf.getValue(2).doubleValue(), 1e-9);
    }

    @Test
    public void testShortPositionMarkToMarket() {
        // Series: close prices 30, 20, 10
        org.ta4j.core.BarSeries series = buildSeries(30, 20, 10);
        NumFactory nf = series.numFactory();

        // Short (sell) at index 0 price 30, buy back (exit) at index 2 price 10
        BaseTradingRecord record = new BaseTradingRecord();
        record.enter(0, nf.numOf(30), nf.one()); // sell = entry (isBuy() = false)
        record.exit(2, nf.numOf(10), nf.one());

        CashFlow cf = new CashFlow(series, record, EquityCurveMode.MARK_TO_MARKET);

        // For short positions the ratio is 2 - (exit/entry)
        // index 0 : 1 (seed)
        // index 1 : 2 - (20/30) = 2 - 0.666666... = 1.333333...
        // index 2 : 2 - (10/30) = 2 - 0.333333... = 1.666666...
        assertEquals(1.0, cf.getValue(0).doubleValue(), 1e-9);
        assertEquals(1.33333333333333, cf.getValue(1).doubleValue(), 1e-9);
        assertEquals(1.66666666666667, cf.getValue(2).doubleValue(), 1e-9);
    }

    @Test
    public void testLongPositionRealizedOnly() {
        // Series: close prices 10, 20, 30
        org.ta4j.core.BarSeries series = buildSeries(10, 20, 30);
        NumFactory nf = series.numFactory();

        BaseTradingRecord record = new BaseTradingRecord();
        record.enter(0, nf.numOf(10), nf.one());
        record.exit(2, nf.numOf(30), nf.one());

        // Use REALIZED mode – only the exit ratio should be applied.
        CashFlow cf = new CashFlow(series, record, EquityCurveMode.REALIZED);

        // Expected:
        // index 0 : 1 (no realised profit yet)
        // index 1 : 1 (unchanged)
        // index 2 : 30 / 10 = 3 (profit applied at exit)
        assertEquals(1.0, cf.getValue(0).doubleValue(), 1e-9);
        assertEquals(1.0, cf.getValue(1).doubleValue(), 1e-9);
        assertEquals(3.0, cf.getValue(2).doubleValue(), 1e-9);
    }

    @Test
    public void testOpenPositionIgnored() {
        // Series: close prices 10, 20, 30
        org.ta4j.core.BarSeries series = buildSeries(10, 20, 30);
        NumFactory nf = series.numFactory();

        // Open position: entry only (no exit)
        BaseTradingRecord record = new BaseTradingRecord();
        record.enter(0, nf.numOf(10), nf.one());

        // OpenPositionHandling.IGNORE means the open position must not affect cash‑flow.
        CashFlow cf = new CashFlow(series, record,
                EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.IGNORE);

        // All values should stay at the initial seed value 1.
        assertEquals(1.0, cf.getValue(0).doubleValue(), 1e-9);
        assertEquals(1.0, cf.getValue(1).doubleValue(), 1e-9);
        assertEquals(1.0, cf.getValue(2).doubleValue(), 1e-9);
    }

    @Test
    public void testWindowedConstructorAndSnapshotSeries() {
        // Series with five bars: 5, 10, 15, 20, 25
        org.ta4j.core.BarSeries series = buildSeries(5, 10, 15, 20, 25);
        NumFactory nf = series.numFactory();

        // Simple long trade from index 1 (price 10) to index 3 (price 20)
        BaseTradingRecord record = new BaseTradingRecord();
        record.enter(1, nf.numOf(10), nf.one());
        record.exit(3, nf.numOf(20), nf.one());

        // Materialise only the window [1,3]
        CashFlow cf = new CashFlow(series, record, 1, 3, EquityCurveMode.MARK_TO_MARKET,
                OpenPositionHandling.MARK_TO_MARKET);

        // Expected ratios:
        // index 1 (entry) : 1
        // index 2 : 15/10 = 1.5
        // index 3 (exit) : 20/10 = 2.0
        assertEquals(1.0, cf.getValue(1).doubleValue(), 1e-9);
        assertEquals(1.5, cf.getValue(2).doubleValue(), 1e-9);
        assertEquals(2.0, cf.getValue(3).doubleValue(), 1e-9);

        // Indexes outside the window should not be accessible.
        try {
            cf.getValue(0);
            fail("Expected IndexOutOfBoundsException for index outside the window");
        } catch (IndexOutOfBoundsException expected) {
            // ok
        }
        try {
            cf.getValue(4);
            fail("Expected IndexOutOfBoundsException for index outside the window");
        } catch (IndexOutOfBoundsException expected) {
            // ok
        }
    }
}
###Test END##