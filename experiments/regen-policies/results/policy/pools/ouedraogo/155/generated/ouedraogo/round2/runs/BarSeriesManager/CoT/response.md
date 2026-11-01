###Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.Trade.TradeType;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * The suite follows the requested steps:
 * <ul>
 *   <li>list public methods,</li>
 *   <li>basic tests for each method,</li>
 *   <li>edge‑case / exception tests,</li>
 *   <li>final merged test class.</li>
 * </ul>
 */
public class BarSeriesManagerTest {

    /* --------------------------------------------------------------------- *
     *  1.  Public API of BarSeriesManager (extracted from the source file) *
     * --------------------------------------------------------------------- */
    // public BarSeries getBarSeries()
    // public CostModel getTransactionCostModel()
    // public CostModel getHoldingCostModel()
    // public TradingRecord run(Strategy strategy)
    // public TradingRecord run(Strategy strategy, int startIndex, int finishIndex)
    // public TradingRecord run(Strategy strategy, TradeType tradeType)
    // public TradingRecord run(Strategy strategy, TradeType tradeType, int startIndex, int finishIndex)
    // public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount)
    // public TradingRecord run(Strategy strategy, TradeType tradeType, Num amount,
    //                         int startIndex, int finishIndex)

    /* --------------------------------------------------------------------- *
     *  Helper / stub implementations (no external mocking frameworks used) *
     * --------------------------------------------------------------------- */

    /** Minimal {@link Num} implementation based on {@link DecimalNum}. */
    private static class SimpleNumFactory implements NumFactory {
        @Override
        public Num zero() {
            return DecimalNum.ZERO;
        }

        @Override
        public Num one() {
            return DecimalNum.ONE;
        }

        @Override
        public Num valueOf(Number number) {
            return DecimalNum.valueOf(number);
        }

        @Override
        public Num valueOf(String number) {
            return DecimalNum.valueOf(number);
        }

        @Override
        public Num valueOf(BigDecimal number) {
            return DecimalNum.valueOf(number);
        }
    }

    /** Very small {@link BarSeries} implementation – only the methods used by
     *  {@link BarSeriesManager} are fully implemented. */
    private static class SimpleBarSeries implements BarSeries {

        private final List<Bar> bars = new ArrayList<>();
        private final SimpleNumFactory numFactory = new SimpleNumFactory();

        SimpleBarSeries(int size) {
            // fill with dummy bars – the concrete Bar type is not needed for the tests
            for (int i = 0; i < size; i++) {
                bars.add(null);
            }
        }

        @Override
        public NumFactory numFactory() {
            return numFactory;
        }

        @Override
        public BarBuilder barBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "simple-series";
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
        public List<Bar> getBarData() {
            return Collections.unmodifiableList(bars);
        }

        @Override
        public int getBeginIndex() {
            return bars.isEmpty() ? 0 : 0;
        }

        @Override
        public int getEndIndex() {
            return bars.isEmpty() ? -1 : bars.size() - 1;
        }

        @Override
        public int getMaximumBarCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setMaximumBarCount(int maximumBarCount) {
            // no‑op for the stub
        }

        @Override
        public int getRemovedBarsCount() {
            return 0;
        }

        @Override
        public void addBar(Bar bar, boolean replace) {
            // no‑op – not needed in tests
        }

        @Override
        public void addTrade(Num tradeVolume, Num tradePrice) {
            // no‑op – not needed in tests
        }

        @Override
        public void addPrice(Num price) {
            // no‑op – not needed in tests
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            throw new UnsupportedOperationException();
        }

        // default methods are inherited unchanged; they are not used directly
    }

    /** Simple {@link Strategy} that signals to operate only on a predefined set
     *  of indexes. */
    private static class DummyStrategy implements Strategy {

        private final Set<Integer> operateIndexes;

        DummyStrategy(Set<Integer> operateIndexes) {
            this.operateIndexes = new HashSet<>(operateIndexes);
        }

        @Override
        public String getName() {
            return "dummy-strategy";
        }

        @Override
        public Rule getEntryRule() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Rule getExitRule() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Strategy and(Strategy strategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Strategy or(Strategy strategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Strategy and(String name, Strategy strategy, int unstableBars) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Strategy or(String name, Strategy strategy, int unstableBars) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Strategy opposite() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUnstableBars(int unstableBars) {
            // no‑op
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public boolean isUnstableAt(int index) {
            return false;
        }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            return operateIndexes.contains(index);
        }

        @Override
        public boolean shouldEnter(int index) {
            return shouldEnter(index, null);
        }

        @Override
        public boolean shouldEnter(int index, TradingRecord tradingRecord) {
            return shouldOperate(index, tradingRecord);
        }

        @Override
        public boolean shouldExit(int index) {
            return shouldExit(index, null);
        }

        @Override
        public boolean shouldExit(int index, TradingRecord tradingRecord) {
            return shouldOperate(index, tradingRecord);
        }

        @Override
        public String toJson() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ComponentDescriptor toDescriptor() {
            throw new UnsupportedOperationException();
        }
    }

    /** Execution model used in the tests. It simply opens a position on the first
     *  call and closes it on the next call. */
    private static class DummyTradeExecutionModel implements TradeExecutionModel {

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            // If there is no open position, we open one; otherwise we close it.
            if (tradingRecord.getCurrentPosition() == null) {
                tradingRecord.enter(index, amount, amount);
            } else {
                tradingRecord.exit(index, amount, amount);
            }
        }
    }

    private SimpleBarSeries series;
    private BarSeriesManager manager;
    private DummyTradeExecutionModel execModel;

    @Before
    public void setUp() {
        // a series with three bars (indexes 0,1,2)
        series = new SimpleBarSeries(3);
        execModel = new DummyTradeExecutionModel();
        manager = new BarSeriesManager(series, execModel);
    }

    /* --------------------------------------------------------------------- *
     *  2.  Basic tests for each public method                              *
     * --------------------------------------------------------------------- */

    @Test
    public void testGetBarSeries() {
        assertSame("getBarSeries must return the series supplied at construction",
                series, manager.getBarSeries());
    }

    @Test
    public void testGetTransactionCostModel() {
        CostModel model = manager.getTransactionCostModel();
        assertNotNull("Transaction cost model must never be null", model);
        assertTrue("Default transaction cost model should be ZeroCostModel",
                model instanceof ZeroCostModel);
    }

    @Test
    public void testGetHoldingCostModel() {
        CostModel model = manager.getHoldingCostModel();
        assertNotNull("Holding cost model must never be null", model);
        assertTrue("Default holding cost model should be ZeroCostModel",
                model instanceof ZeroCostModel);
    }

    @Test
    public void testRunWithNoOperation() {
        // strategy never signals to operate
        Strategy neverStrategy = new DummyStrategy(Collections.emptySet());

        TradingRecord record = manager.run(neverStrategy);
        assertNotNull(record);
        assertTrue("No operation ⇒ no positions", record.getPositions().isEmpty());
        assertTrue("No operation ⇒ record must be closed", record.isClosed());
    }

    @Test
    public void testRunDefaultTradeType() {
        // operate on indexes 0 and 1 – should open then close a position
        Strategy strategy = new DummyStrategy(new HashSet<>(Arrays.asList(0, 1)));
        TradingRecord record = manager.run(strategy);
        assertEquals("One closed position expected", 1, record.getPositions().size());
        assertTrue("Position must be closed", record.isClosed());
    }

    @Test
    public void testRunWithExplicitTradeType() {
        // same as above but we explicitly request BUY (the default)
        Strategy strategy = new DummyStrategy(new HashSet<>(Arrays.asList(0, 1)));
        TradingRecord record = manager.run(strategy, TradeType.BUY);
        assertEquals(1, record.getPositions().size());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWithStartFinishIndexes() {
        // operate only on index 2 (the last bar)
        Strategy strategy = new DummyStrategy(new HashSet<>(Collections.singletonList(2)));
        TradingRecord record = manager.run(strategy, 2, 2);
        assertEquals(1, record.getPositions().size());
        // because the run ends on the series end, the manager tries to close the
        // position using the extra‑loop; however our execution model only closes
        // when it is called again – there is no extra bar, so the position stays open
        assertFalse("Position should stay open because there is no further bar", record.isClosed());
    }

    @Test
    public void testRunWithAmountParameter() {
        Strategy strategy = new DummyStrategy(new HashSet<>(Arrays.asList(0, 1)));
        Num amount = DecimalNum.valueOf("10");
        TradingRecord record = manager.run(strategy, TradeType.BUY, amount);
        assertEquals(1, record.getPositions().size());
        // verify that the amount used inside the execution model equals the one we passed
        // (the dummy model uses the amount as both price and amount)
        assertEquals(amount, record.getLastEntry().getAmount());
        assertEquals(amount, record.getLastExit().getAmount());
    }

    @Test
    public void testRunFullSignature() {
        Strategy strategy = new DummyStrategy(new HashSet<>(Arrays.asList(0, 1, 2)));
        Num amount = DecimalNum.valueOf("5");
        TradingRecord record = manager.run(strategy, TradeType.BUY, amount, 0, 2);
        // three calls → enter, exit, enter → one closed position and one open
        assertEquals(2, record.getPositions().size());
        assertFalse(record.isClosed());
    }

    /* --------------------------------------------------------------------- *
     *  3.  Edge‑case / exception tests                                    *
     * --------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testRunNullStrategyThrows() {
        manager.run(null);
    }

    @Test
    public void testRunWithStartIndexBeyondSeries() {
        // start index is after the series end – the manager should simply do nothing
        Strategy strategy = new DummyStrategy(new HashSet<>(Collections.singleton(5)));
        TradingRecord record = manager.run(strategy, 5, 10);
        assertTrue("No operation should be performed", record.getPositions().isEmpty());
        assertTrue("Record must be considered closed when nothing happened", record.isClosed());
    }

    @Test
    public void testRunWithNegativeIndexes() {
        // negative start index – the manager clamps it to series begin (0)
        Strategy strategy = new DummyStrategy(new HashSet<>(Arrays.asList(0, 1)));
        TradingRecord record = manager.run(strategy, -5, 1);
        assertEquals(1, record.getPositions().size());
        assertTrue(record.isClosed());
    }

    @Test
    public void testRunWhenStartGreaterThanFinish() {
        // start > finish ⇒ runBeginIndex > runEndIndex, loop never executes
        Strategy strategy = new DummyStrategy(new HashSet<>(Arrays.asList(0, 1)));
        TradingRecord record = manager.run(strategy, TradeType.BUY, DecimalNum.ONE, 2, 1);
        assertTrue("No trades expected because start > finish", record.getPositions().isEmpty());
        assertTrue(record.isClosed());
    }

    /* --------------------------------------------------------------------- *
     *  4.  Combined test class (already merged)                           *
     * --------------------------------------------------------------------- */
}
```
###Test END##