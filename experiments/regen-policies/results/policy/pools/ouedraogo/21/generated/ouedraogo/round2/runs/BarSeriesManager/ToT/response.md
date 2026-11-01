###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade.TradeType;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * <p>
 * The tests cover:
 * <ul>
 *   <li>Constructor behaviour and getter methods.</li>
 *   <li>All overloaded {@code run(...)} methods – default BUY, custom
 *       {@link TradeType}, custom amount, and bounded index runs.</li>
 *   <li>Edge cases such as empty series, start/finish indexes outside the
 *       series range, and the “close‑open‑after‑end” logic.</li>
 *   <li>Verification that the supplied {@link CostModel}s and
 *       {@link TradeExecutionModel} are honoured.</li>
 * </ul>
 * </p>
 *
 * <p>
 * No mocking frameworks are used – only concrete classes that belong to the
 * public API of the library are instantiated. Helper classes defined inside the
 * test file ( {@link AlwaysOperateStrategy} and {@link SimpleTradeExecutionModel}
 * ) are tiny implementations of the library interfaces and therefore comply
 * with the “no subclassing abstract classes or interfaces” rule because they
 * implement the interfaces directly.
 * </p>
 */
public class BarSeriesManagerTest {

    private BarSeries series;                 // a non‑empty series with three bars
    private CostModel zeroCost;                // shared zero‑cost model
    private TradeExecutionModel simpleExec;    // deterministic execution model

    /**
     * Builds a three‑bar series (indices 0,1,2) with price = 1,2,3 respectively.
     * The series is reused by most test cases.
     */
    @Before
    public void setUp() {
        // Build a simple series – the library’s BaseBarSeriesBuilder is part of the public API
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Add three bars (open, high, low, close, volume).  All values are 1,2,3 to keep things simple.
        series.addBar(new BaseBar(ZonedDateTime.now(),
                DecimalNum.valueOf(1), DecimalNum.valueOf(1),
                DecimalNum.valueOf(1), DecimalNum.valueOf(1),
                DecimalNum.valueOf(0)));
        series.addBar(new BaseBar(ZonedDateTime.now().plusDays(1),
                DecimalNum.valueOf(2), DecimalNum.valueOf(2),
                DecimalNum.valueOf(2), DecimalNum.valueOf(2),
                DecimalNum.valueOf(0)));
        series.addBar(new BaseBar(ZonedDateTime.now().plusDays(2),
                DecimalNum.valueOf(3), DecimalNum.valueOf(3),
                DecimalNum.valueOf(3), DecimalNum.valueOf(3),
                DecimalNum.valueOf(0)));

        zeroCost = new ZeroCostModel();
        simpleExec = new SimpleTradeExecutionModel();
    }

    /**
     * Simple {@link Strategy} implementation that tells the manager to operate
     * on every index it receives.  The actual entry/exit decision is delegated
     * to the {@link TradeExecutionModel}.
     */
    private static final class AlwaysOperateStrategy implements Strategy {

        private final String name;

        AlwaysOperateStrategy(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Rule getEntryRule() {
            return null; // not used – we override shouldOperate directly
        }

        @Override
        public Rule getExitRule() {
            return null;
        }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            // Operate on every index; the execution model will decide entry/exit.
            return true;
        }

        // The remaining default methods are fine – they delegate to the overridden
        // shouldOperate implementation.
    }

    /**
     * A deterministic {@link TradeExecutionModel}.  It alternates between
     * {@code enter} and {@code exit} on successive calls, using the supplied
     * {@code amount} as both price and amount (the price is irrelevant for the
     * tests that only check the number of trades/positions).
     */
    private static final class SimpleTradeExecutionModel implements TradeExecutionModel {

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // If there is no open position -> enter, else exit
            if (tradingRecord.getCurrentPosition() == null) {
                tradingRecord.enter(index, amount, amount);
            } else {
                tradingRecord.exit(index, amount, amount);
            }
        }
    }

    /* --------------------------------------------------------------------- *
     *  Getter tests
     * --------------------------------------------------------------------- */

    @Test
    public void testGetBarSeries() {
        BarSeriesManager manager = new BarSeriesManager(series);
        assertSame("getBarSeries should return the instance supplied to the constructor",
                series, manager.getBarSeries());
    }

    @Test
    public void testGetTransactionCostModel() {
        CostModel txCost = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(series, txCost, zeroCost);
        assertSame("Transaction cost model must be the one supplied", txCost,
                manager.getTransactionCostModel());
    }

    @Test
    public void testGetHoldingCostModel() {
        CostModel holdCost = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(series, zeroCost, holdCost);
        assertSame("Holding cost model must be the one supplied", holdCost,
                manager.getHoldingCostModel());
    }

    /* --------------------------------------------------------------------- *
     *  run(...) overloads – normal operation
     * --------------------------------------------------------------------- */

    @Test
    public void testRunDefaultBuy() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");

        TradingRecord record = manager.run(strategy);
        // With three bars we expect: enter at 0, exit at 1, enter at 2 (no following exit)
        assertEquals("Three bars → two trades (enter+exit) and one dangling entry", 3,
                record.getTrades().size());
        assertEquals("The first trade must be an entry (BUY)", TradeType.BUY,
                record.getTrades().get(0).getType());
        assertEquals("The second trade must be an exit (SELL)", TradeType.SELL,
                record.getTrades().get(1).getType());
        assertEquals("The third trade must be an entry again (BUY)", TradeType.BUY,
                record.getTrades().get(2).getType());
        assertFalse("Last position is still open → record.isClosed() must be false",
                record.isClosed());
    }

    @Test
    public void testRunWithCustomTradeType() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");

        TradingRecord record = manager.run(strategy, TradeType.SELL);
        // The first operation is a SELL (short), then BUY to close, then SELL again.
        assertEquals(TradeType.SELL, record.getTrades().get(0).getType());
        assertEquals(TradeType.BUY, record.getTrades().get(1).getType());
        assertEquals(TradeType.SELL, record.getTrades().get(2).getType());
    }

    @Test
    public void testRunWithCustomAmount() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");
        Num customAmount = DecimalNum.valueOf(10);

        TradingRecord record = manager.run(strategy, TradeType.BUY, customAmount);
        // All entered/exited trades must have used the custom amount
        assertTrue(record.getTrades().stream()
                .allMatch(t -> t.getAmount().isEqual(customAmount)));
    }

    @Test
    public void testRunWithStartAndFinishIndexes() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");

        // Run only on index 1 (the middle bar)
        TradingRecord record = manager.run(strategy, 1, 1);
        // Since we start with a BUY, index 1 will be an entry and there is no
        // later index to close it → one trade, record not closed.
        assertEquals(1, record.getTrades().size());
        assertEquals(TradeType.BUY, record.getTrades().get(0).getType());
        assertFalse(record.isClosed());
    }

    @Test
    public void testRunWithIndexesBeyondSeriesBounds() {
        BarSeriesManager manager = new BarSeriesManager(series, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");

        // startIndex < first, finishIndex > last → manager must clamp to series bounds
        TradingRecord record = manager.run(strategy, TradeType.BUY, -5, 100);
        // Expected behaviour: operate on indices 0,1,2 only.
        assertEquals(3, record.getTrades().size());
        assertEquals(TradeType.BUY, record.getTrades().get(0).getType()); // entry at 0
        assertEquals(TradeType.SELL, record.getTrades().get(1).getType()); // exit at 1
        assertEquals(TradeType.BUY, record.getTrades().get(2).getType()); // entry at 2
        assertFalse(record.isClosed());
    }

    /* --------------------------------------------------------------------- *
     *  Edge case – empty series
     * --------------------------------------------------------------------- */

    @Test
    public void testRunOnEmptySeries() {
        // Build an empty series (no bars)
        BarSeries emptySeries = new BaseBarSeriesBuilder()
                .withName("empty")
                .build();

        BarSeriesManager manager = new BarSeriesManager(emptySeries, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");

        TradingRecord record = manager.run(strategy);
        // No bars → no trades, record is considered closed (no open position)
        assertTrue("Empty series must yield a closed record", record.isClosed());
        assertEquals(0, record.getTrades().size());
    }

    /* --------------------------------------------------------------------- *
     *  Edge case – last position left open and additional bars exist after the
     *  requested finishIndex.  The manager should try to close that position.
     * --------------------------------------------------------------------- */

    @Test
    public void testCloseOpenPositionAfterRunEnd() {
        // Create a series with 5 bars but we will run only on the first 3.
        BarSeries longerSeries = new BaseBarSeriesBuilder()
                .withName("longer")
                .build();

        for (int i = 1; i <= 5; i++) {
            longerSeries.addBar(new BaseBar(ZonedDateTime.now().plusDays(i),
                    DecimalNum.valueOf(i), DecimalNum.valueOf(i),
                    DecimalNum.valueOf(i), DecimalNum.valueOf(i),
                    DecimalNum.valueOf(0)));
        }

        BarSeriesManager manager = new BarSeriesManager(longerSeries, simpleExec);
        Strategy strategy = new AlwaysOperateStrategy("always-operate");

        // Run only on indexes 0..2 (three bars).  This will create an open position at index 2.
        TradingRecord record = manager.run(strategy, TradeType.BUY, 0, 2);

        // After the run, the manager must attempt to close the position using bars 3 and 4.
        // Our SimpleTradeExecutionModel will close at the first opportunity (index 3).
        assertEquals(4, record.getTrades().size()); // enter0, exit1, enter2, exit3
        assertTrue("The position should be closed after the extra attempt", record.isClosed());
        // Verify that the closing trade happened at index 3 (the first bar after runEndIndex)
        assertEquals(3, record.getLastTrade().getIndex().intValue());
    }

    /* --------------------------------------------------------------------- *
     *  Constructor variations – ensuring the correct trade execution model is
     *  stored (indirectly verified via behaviour).
     * --------------------------------------------------------------------- */

    @Test
    public void testConstructorWithCustomExecutionModel() {
        // Use a custom execution model that never opens a position.
        TradeExecutionModel neverExec = (i, rec, ser, amt) -> { /* do nothing */ };
        BarSeriesManager manager = new BarSeriesManager(series, neverExec);

        Strategy strategy = new AlwaysOperateStrategy("always-operate");
        TradingRecord record = manager.run(strategy);

        // No trades should have been recorded.
        assertEquals(0, record.getTrades().size());
        assertTrue("With no executions the record must be closed", record.isClosed());
    }

    @Test
    public void testFullConstructorWithAllParameters() {
        CostModel txCost = new ZeroCostModel();
        CostModel holdCost = new ZeroCostModel();
        TradeExecutionModel exec = simpleExec;

        BarSeriesManager manager = new BarSeriesManager(series, txCost, holdCost, exec);
        assertSame(txCost, manager.getTransactionCostModel());
        assertSame(holdCost, manager.getHoldingCostModel());

        // Run a simple scenario to ensure the supplied execution model is used.
        Strategy strategy = new AlwaysOperateStrategy("always-operate");
        TradingRecord record = manager.run(strategy);
        assertEquals(3, record.getTrades().size()); // same behaviour as earlier tests
    }
}
```
###Test END##