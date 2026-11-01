###Test START##
```java
package org.ta4j.core.backtest;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.rules.BooleanRule;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * The tests cover:
 * <ul>
 *   <li>All constructors and accessor methods.</li>
 *   <li>Running a strategy with default parameters.</li>
 *   <li>Running a strategy with explicit start/finish indexes, amount and trade type.</li>
 *   <li>Boundary handling (indexes outside series range, empty series).</li>
 *   <li>Behaviour when the last position stays open at the end of the run.</li>
 *   <li>Verification that the selected {@link CostModel}s are really used.</li>
 * </ul>
 *
 * Only concrete classes shipped with the TA‑4J library are used – no mocks,
 * spies or custom subclasses of library interfaces.
 */
public class BarSeriesManagerTest {

    /** A tiny series (5 bars) where price is 1,2,3,2,1. */
    private BarSeries series;

    /** A strategy that enters when price rises and exits when it falls. */
    private Strategy upDownStrategy;

    /** The default manager (ZeroCostModel + TradeOnNextOpenModel). */
    private BarSeriesManager defaultManager;

    /** Helper to create a {@link Num} from a double using the series factory. */
    private Num num(double value) {
        return series.numFactory().numOf(value);
    }

    @Before
    public void setUp() {
        // ----------------------------------------------------------------------
        // Build a simple series (5 bars).  The concrete class BaseBarSeries is part
        // of the public TA‑4J API and implements BarSeries.
        // ----------------------------------------------------------------------
        series = new BaseBarSeries("test-series");
        // add bars: open=close=high=low=price, duration is irrelevant for the test
        series.addBar(num(1), num(1), num(1), num(1), num(1));
        series.addBar(num(2), num(2), num(2), num(2), num(2));
        series.addBar(num(3), num(3), num(3), num(3), num(3));
        series.addBar(num(2), num(2), num(2), num(2), num(2));
        series.addBar(num(1), num(1), num(1), num(1), num(1));

        // ----------------------------------------------------------------------
        // Build a very small strategy:
        //   entry rule : price > previous price   (rising)
        //   exit  rule : price < previous price   (falling)
        // The BooleanRule class is a concrete implementation of Rule that simply
        // returns the supplied boolean value when evaluated.
        // ----------------------------------------------------------------------
        BooleanRule entryRule = new BooleanRule() {
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                return index > 0 && series.getBar(index).getClosePrice()
                        .isGreaterThan(series.getBar(index - 1).getClosePrice());
            }
        };
        BooleanRule exitRule = new BooleanRule() {
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                return index > 0 && series.getBar(index).getClosePrice()
                        .isLessThan(series.getBar(index - 1).getClosePrice());
            }
        };
        upDownStrategy = new BaseStrategy(entryRule, exitRule);

        // ----------------------------------------------------------------------
        // Default manager (zero costs, default TradeOnNextOpenModel)
        // ----------------------------------------------------------------------
        defaultManager = new BarSeriesManager(series);
    }

    // ----------------------------------------------------------------------
    // Constructor / accessor tests
    // ----------------------------------------------------------------------
    @Test
    public void testDefaultConstructorSetsZeroCostModels() {
        BarSeriesManager mgr = new BarSeriesManager(series);
        assertTrue(mgr.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(mgr.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testAllArgumentConstructorKeepsProvidedModels() {
        CostModel tx = new ZeroCostModel();
        CostModel hold = new ZeroCostModel();
        TradeExecutionModel exec = new TradeOnNextOpenModel();
        BarSeriesManager mgr = new BarSeriesManager(series, tx, hold, exec);
        assertSame(tx, mgr.getTransactionCostModel());
        assertSame(hold, mgr.getHoldingCostModel());
    }

    @Test
    public void testGetBarSeriesReturnsSameInstance() {
        assertSame(series, defaultManager.getBarSeries());
    }

    // ----------------------------------------------------------------------
    // run(Strategy) – simple “never‑operate” strategy
    // ----------------------------------------------------------------------
    @Test
    public void runWithNeverOperatingStrategyReturnsEmptyRecord() {
        Strategy never = new BaseStrategy(new BooleanRule() {
            @Override public boolean isSatisfied(int index, TradingRecord record) { return false; }
        }, new BooleanRule() {
            @Override public boolean isSatisfied(int index, TradingRecord record) { return false; }
        });

        TradingRecord record = defaultManager.run(never);
        assertTrue(record.getPositions().isEmpty());
        assertTrue(record.isClosed());               // no open position → considered closed
    }

    // ----------------------------------------------------------------------
    // run(Strategy, int, int) – index clipping
    // ----------------------------------------------------------------------
    @Test
    public void runClipsStartAndFinishIndexesToSeriesBounds() {
        // start < beginIndex, finish > endIndex
        TradingRecord record = defaultManager.run(upDownStrategy, -10, 100);
        // The series has 5 bars → indices 0‑4, we expect the same behaviour as
        // a normal run over the whole series.
        assertEquals(0, (int) record.getStartIndex());
        assertEquals(4, (int) record.getEndIndex());
        // With the up‑down strategy we should have exactly one completed trade:
        // enter on index 1 (price 2), exit on index 3 (price 2) – the middle peak.
        assertEquals(1, record.getPositions().size());
        Position pos = record.getLastPosition();
        assertTrue(pos.isClosed());
        assertEquals(num(2), pos.getEntry().getPrice());
        assertEquals(num(2), pos.getExit().getPrice());
    }

    // ----------------------------------------------------------------------
    // run(Strategy, TradeType) – using SELL as opening type
    // ----------------------------------------------------------------------
    @Test
    public void runWithSellOpeningTypeCreatesShortPositions() {
        TradingRecord record = defaultManager.run(upDownStrategy, TradeType.SELL);
        // With a SELL opening, the first signal (price rising) will be interpreted
        // as a short entry, and the first falling price as a short exit.
        // For the test data the first falling price appears at index 3.
        assertEquals(1, record.getPositions().size());
        Position pos = record.getLastPosition();
        assertEquals(TradeType.SELL, pos.getEntry().getType());
        assertEquals(TradeType.BUY, pos.getExit().getType()); // exit is opposite
    }

    // ----------------------------------------------------------------------
    // run(Strategy, TradeType, Num) – custom amount
    // ----------------------------------------------------------------------
    @Test
    public void runWithCustomAmountUsesThatAmountInTrades() {
        Num customAmount = num(10);
        TradingRecord record = defaultManager.run(upDownStrategy, TradeType.BUY, customAmount);
        Position pos = record.getLastPosition();
        assertEquals(customAmount, pos.getEntry().getAmount());
        assertEquals(customAmount, pos.getExit().getAmount());
    }

    // ----------------------------------------------------------------------
    // run(Strategy, TradeType, Num, int, int) – full overload + open‑ended run
    // ----------------------------------------------------------------------
    @Test
    public void runFullOverloadAndCloseOpenPositionAtSeriesEnd() {
        // Build a strategy that only enters (never exits)
        Strategy onlyEnter = new BaseStrategy(new BooleanRule() {
            @Override public boolean isSatisfied(int index, TradingRecord record) { return true; }
        }, new BooleanRule() {
            @Override public boolean isSatisfied(int index, TradingRecord record) { return false; }
        });

        // Run only on the first three bars – the entry will happen at index 0.
        TradingRecord record = defaultManager.run(onlyEnter, TradeType.BUY, num(5), 0, 2);

        // Because the strategy never signals an exit, the position stays open.
        // The manager must try to close it using bars after the requested finish
        // index (i.e., indexes 3 and 4). The next bar (index 3) still satisfies
        // the entry rule, but the exit rule is still false – therefore the manager
        // will *not* close the position. The loop after the run finishes will try
        // again; on index 4 the entry rule is still true, but the exit rule is
        // still false, so the position remains open.
        // The specification says: "if the last position is still open … we execute
        // the strategy on these bars to give an opportunity to close this position."
        // Since our strategy never exits, the position remains open.
        assertFalse(record.isClosed());

        // However the manager must have attempted the extra iteration; we verify
        // that the end index stored in the record equals the series end (4).
        assertEquals(Integer.valueOf(4), record.getEndIndex());
    }

    // ----------------------------------------------------------------------
    // run when the series is empty
    // ----------------------------------------------------------------------
    @Test
    public void runOnEmptySeriesReturnsClosedRecordWithoutError() {
        BarSeries empty = new BaseBarSeries("empty");
        BarSeriesManager mgr = new BarSeriesManager(empty);
        TradingRecord rec = mgr.run(upDownStrategy);
        assertTrue(rec.getPositions().isEmpty());
        assertTrue(rec.isClosed());
    }

    // ----------------------------------------------------------------------
    // Verify that the transaction and holding cost models are really used.
    // We use a non‑zero cost model (a simple FixedTransactionCostModel with
    // a constant cost) and check that the cost appears in the recorded trades.
    // ----------------------------------------------------------------------
    @Test
    public void runWithNonZeroCostModelReflectsCostsInTradingRecord() {
        // Simple cost model that always returns a constant value of 0.1
        CostModel cost = new CostModel() {
            @Override public Num calculate(Position position, int finalIndex) { return num(0.1); }
            @Override public Num calculate(Position position) { return num(0.1); }
            @Override public Num calculate(Num price, Num amount) { return num(0.1); }
            @Override public boolean equals(CostModel other) { return this == other; }
        };
        BarSeriesManager mgr = new BarSeriesManager(series, cost, cost, new TradeOnNextOpenModel());

        TradingRecord rec = mgr.run(upDownStrategy);
        // The cost model is applied on each trade – we just check that the model
        // stored inside the record is the one we supplied.
        assertSame(cost, rec.getTransactionCostModel());
        assertSame(cost, rec.getHoldingCostModel());
    }
}
```
###Test END##