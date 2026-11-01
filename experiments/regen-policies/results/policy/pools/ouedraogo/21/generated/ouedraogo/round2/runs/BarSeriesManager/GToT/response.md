###Test START##
```java
package org.ta4j.core.backtest;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit‑4 test suite for {@link BarSeriesManager}.
 *
 * The test suite is written without any external mocking frameworks.
 * All required collaborators (BarSeries, Strategy, TradeExecutionModel,
 * CostModel, Num and NumFactory) are provided as lightweight inner
 * implementations that expose only the behaviour needed for the tests.
 *
 * The tests cover:
 *  • All public constructors and accessor methods.
 *  • All overloaded {@code run(...)} methods (basic, bounded,
 *    custom trade type, custom amount).
 *  • Edge‑case handling for out‑of‑range indexes.
 *  • Correct clamping of start/finish indexes to the series bounds.
 *  • Behaviour when the strategy never triggers a trade.
 *  • Behaviour when the strategy triggers an entry and later an exit.
 *  • Null‑argument handling (expected NPE).
 */
public class BarSeriesManagerTest {

    /** Simple {@link Num} implementation backed by a {@code double}. */
    private static final class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        static SimpleNum of(double v) {
            return new SimpleNum(v);
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public NumFactory getNumFactory() {
            return SimpleNumFactory.INSTANCE;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        @Override
        public Num plus(Num augend) {
            return of(this.value + ((SimpleNum) augend).value);
        }

        @Override
        public Num minus(Num subtrahend) {
            return of(this.value - ((SimpleNum) subtrahend).value);
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return of(this.value * ((SimpleNum) multiplicand).value);
        }

        @Override
        public Num dividedBy(Num divisor) {
            return of(this.value / ((SimpleNum) divisor).value);
        }

        @Override
        public Num remainder(Num divisor) {
            return of(this.value % ((SimpleNum) divisor).value);
        }

        @Override
        public Num floor() {
            return of(Math.floor(value));
        }

        @Override
        public Num ceil() {
            return of(Math.ceil(value));
        }

        @Override
        public Num pow(int n) {
            return of(Math.pow(value, n));
        }

        @Override
        public Num pow(Num n) {
            return of(Math.pow(value, ((SimpleNum) n).value));
        }

        @Override
        public Num log() {
            return of(Math.log(value));
        }

        @Override
        public Num sqrt() {
            return of(Math.sqrt(value));
        }

        @Override
        public Num sqrt(int precision) {
            return sqrt(); // precision ignored for the simple implementation
        }

        @Override
        public Num abs() {
            return of(Math.abs(value));
        }

        @Override
        public Num negate() {
            return of(-value);
        }

        @Override
        public boolean isZero() {
            return value == 0.0;
        }

        @Override
        public boolean isPositive() {
            return value > 0.0;
        }

        @Override
        public boolean isPositiveOrZero() {
            return value >= 0.0;
        }

        @Override
        public boolean isNegative() {
            return value < 0.0;
        }

        @Override
        public boolean isNegativeOrZero() {
            return value <= 0.0;
        }

        @Override
        public boolean isEqual(Num other) {
            return Double.compare(this.value, ((SimpleNum) other).value) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return this.value > ((SimpleNum) other).value;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return this.value >= ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThan(Num other) {
            return this.value < ((SimpleNum) other).value;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return this.value <= ((SimpleNum) other).value;
        }

        @Override
        public Num min(Num other) {
            return of(Math.min(this.value, ((SimpleNum) other).value));
        }

        @Override
        public Num max(Num other) {
            return of(Math.max(this.value, ((SimpleNum) other).value));
        }

        @Override
        public boolean isNaN() {
            return Double.isNaN(value);
        }

        @Override
        public int intValue() {
            return (int) value;
        }

        @Override
        public long longValue() {
            return (long) value;
        }

        @Override
        public float floatValue() {
            return (float) value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        @Override
        public BigDecimal bigDecimalValue() {
            return BigDecimal.valueOf(value);
        }

        @Override
        public int compareTo(Num o) {
            return Double.compare(this.value, ((SimpleNum) o).value);
        }

        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && isEqual((SimpleNum) obj);
        }
    }

    /** Simple {@link NumFactory} that only provides {@code one()} and {@code numOf(double)}. */
    private static final class SimpleNumFactory implements NumFactory {

        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();

        private SimpleNumFactory() {}

        @Override
        public Num numOf(Number number) {
            return SimpleNum.of(number.doubleValue());
        }

        @Override
        public Num numOf(String number) {
            return SimpleNum.of(Double.parseDouble(number));
        }

        @Override
        public Num of(double value) {
            return SimpleNum.of(value);
        }

        @Override
        public Num one() {
            return SimpleNum.of(1.0);
        }

        // The real interface contains many more default methods; we rely on defaults.
    }

    /** Minimal {@link BarSeries} implementation sufficient for the manager tests. */
    private static final class SimpleBarSeries implements BarSeries {

        private final List<Bar> bars;
        private final SimpleNumFactory numFactory = SimpleNumFactory.INSTANCE;

        SimpleBarSeries(int size) {
            this.bars = Collections.nCopies(size, (Bar) null);
        }

        @Override
        public NumFactory numFactory() {
            return numFactory;
        }

        @Override
        public BaseBarConvertibleBuilder barBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "SimpleBarSeries";
        }

        @Override
        public Bar getBar(int i) {
            return null; // not needed for the tests
        }

        @Override
        public int getBarCount() {
            return bars.size();
        }

        @Override
        public List<Bar> getBarData() {
            return bars;
        }

        @Override
        public int getBeginIndex() {
            return 0;
        }

        @Override
        public int getEndIndex() {
            return bars.isEmpty() ? -1 : bars.size() - 1;
        }

        @Override
        public void addBar(Bar bar, boolean replace) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTrade(Num tradeVolume, Num tradePrice) {
            // No‑op for tests
        }

        @Override
        public void addPrice(Num price) {
            // No‑op for tests
        }

        // All other default methods are left untouched.
    }

    /** Simple {@link Strategy} that triggers on a pre‑defined set of indexes. */
    private static final class SimpleStrategy implements Strategy {

        private final int[] triggerIndexes;

        SimpleStrategy(int... triggerIndexes) {
            this.triggerIndexes = triggerIndexes;
        }

        @Override
        public boolean shouldOperate(int index, TradingRecord tradingRecord) {
            for (int i : triggerIndexes) {
                if (i == index) {
                    return true;
                }
            }
            return false;
        }

        // The remaining methods are not used by BarSeriesManager and are
        // therefore left with default behaviour or simple stubs.
        @Override public String getName() { return "SimpleStrategy"; }
        @Override public Rule getEntryRule() { throw new UnsupportedOperationException(); }
        @Override public Rule getExitRule() { throw new UnsupportedOperationException(); }
        @Override public Strategy and(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(Strategy strategy) { throw new UnsupportedOperationException(); }
        @Override public Strategy and(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy or(String name, Strategy strategy, int unstableBars) { throw new UnsupportedOperationException(); }
        @Override public Strategy opposite() { throw new UnsupportedOperationException(); }
        @Override public void setUnstableBars(int unstableBars) { }
        @Override public int getUnstableBars() { return 0; }
        @Override public boolean isUnstableAt(int index) { return false; }
        @Override public boolean shouldEnter(int index) { return false; }
        @Override public boolean shouldEnter(int index, TradingRecord tradingRecord) { return false; }
        @Override public boolean shouldExit(int index) { return false; }
        @Override public boolean shouldExit(int index, TradingRecord tradingRecord) { return false; }
    }

    /** Trade execution model that simply calls {@code tradingRecord.enter} on the first trigger
     *  and {@code tradingRecord.exit} on the second trigger. */
    private static final class SimpleExecutionModel implements TradeExecutionModel {

        private boolean entered = false;

        @Override
        public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
            if (!entered) {
                // first call -> entry
                tradingRecord.enter(index, amount, amount);
                entered = true;
            } else {
                // second call -> exit
                tradingRecord.exit(index, amount, amount);
            }
        }
    }

    private BarSeries series5;   // series with 5 bars (indexes 0‑4)
    private CostModel zeroCost;
    private TradeExecutionModel execModel;

    @Before
    public void setUp() {
        series5 = new SimpleBarSeries(5);
        zeroCost = new ZeroCostModel();
        execModel = new SimpleExecutionModel();
    }

    /* ----------------------------------------------------------------------
     *  Accessor tests (constructors + getters)
     * ---------------------------------------------------------------------- */

    @Test
    public void testConstructorWithOnlyBarSeries() {
        BarSeriesManager mgr = new BarSeriesManager(series5);
        assertSame(series5, mgr.getBarSeries());
        assertNotNull(mgr.getTransactionCostModel());
        assertNotNull(mgr.getHoldingCostModel());
    }

    @Test
    public void testConstructorWithBarSeriesAndExecutionModel() {
        BarSeriesManager mgr = new BarSeriesManager(series5, execModel);
        assertSame(series5, mgr.getBarSeries());
        // default cost models are ZeroCostModel
        assertTrue(mgr.getTransactionCostModel() instanceof ZeroCostModel);
        assertTrue(mgr.getHoldingCostModel() instanceof ZeroCostModel);
    }

    @Test
    public void testConstructorWithCostModels() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost);
        assertSame(zeroCost, mgr.getTransactionCostModel());
        assertSame(zeroCost, mgr.getHoldingCostModel());
    }

    @Test
    public void testFullConstructor() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        assertSame(series5, mgr.getBarSeries());
        assertSame(zeroCost, mgr.getTransactionCostModel());
        assertSame(zeroCost, mgr.getHoldingCostModel());
    }

    /* ----------------------------------------------------------------------
     *  Basic run() tests (no trade should be performed)
     * ---------------------------------------------------------------------- */

    @Test
    public void testRunWithNeverTriggeringStrategy() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        Strategy never = new SimpleStrategy(); // no indexes
        TradingRecord rec = mgr.run(never);
        assertTrue(rec.getPositions().isEmpty());
        assertTrue(rec.isClosed()); // no open position -> considered closed
    }

    @Test
    public void testRunWithDefaultBuyAndSingleEntryExit() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, new SimpleExecutionModel());
        // trigger on index 1 (entry) and index 3 (exit)
        Strategy strat = new SimpleStrategy(1, 3);
        TradingRecord rec = mgr.run(strat);
        // one position should be recorded and closed
        assertEquals(1, rec.getPositions().size());
        assertTrue(rec.isClosed());
        // verify entry and exit indexes
        Position pos = rec.getCurrentPosition();
        assertEquals(Integer.valueOf(1), pos.getEntry().getIndex());
        assertEquals(Integer.valueOf(3), pos.getExit().getIndex());
    }

    /* ----------------------------------------------------------------------
     *  Overloaded run() variations
     * ---------------------------------------------------------------------- */

    @Test
    public void testRunWithExplicitStartAndFinishIndexes() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, new SimpleExecutionModel());
        Strategy strat = new SimpleStrategy(2, 4); // both inside the range [2,4]
        TradingRecord rec = mgr.run(strat, 2, 4);
        assertEquals(1, rec.getPositions().size());
        Position pos = rec.getCurrentPosition();
        assertEquals(Integer.valueOf(2), pos.getEntry().getIndex());
        assertEquals(Integer.valueOf(4), pos.getExit().getIndex());
    }

    @Test
    public void testRunWithTradeTypeSellAndCustomAmount() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, new SimpleExecutionModel());
        Strategy strat = new SimpleStrategy(0, 2);
        Num customAmount = SimpleNum.of(10.0);
        TradingRecord rec = mgr.run(strat, TradeType.SELL, customAmount);
        // Even with SELL as starting type, our SimpleExecutionModel just calls
        // enter/exit; BaseTradingRecord records the correct starting type.
        assertEquals(TradeType.SELL, rec.getStartingType());
        assertEquals(1, rec.getPositions().size());
        Position pos = rec.getCurrentPosition();
        // Entry and exit indexes are as supplied by the strategy
        assertEquals(Integer.valueOf(0), pos.getEntry().getIndex());
        assertEquals(Integer.valueOf(2), pos.getExit().getIndex());
    }

    @Test
    public void testRunWithOutOfBoundsIndexesIsClamped() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, new SimpleExecutionModel());
        // startIndex = -5 (before begin) , finishIndex = 100 (after end)
        Strategy strat = new SimpleStrategy(0, 4);
        TradingRecord rec = mgr.run(strat, TradeType.BUY, SimpleNum.of(1.0), -5, 100);
        // should behave as if run from 0 to 4
        assertEquals(1, rec.getPositions().size());
        Position pos = rec.getCurrentPosition();
        assertEquals(Integer.valueOf(0), pos.getEntry().getIndex());
        assertEquals(Integer.valueOf(4), pos.getExit().getIndex());
    }

    /* ----------------------------------------------------------------------
     *  Edge‑case: start index greater than finish index – loop never executes
     * ---------------------------------------------------------------------- */

    @Test
    public void testRunWhenStartIndexGreaterThanFinishIndex() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        Strategy strat = new SimpleStrategy(0, 1, 2);
        // start > finish => no iteration
        TradingRecord rec = mgr.run(strat, TradeType.BUY, SimpleNum.of(1.0), 4, 2);
        assertTrue(rec.getPositions().isEmpty());
        assertTrue(rec.isClosed());
    }

    /* ----------------------------------------------------------------------
     *  Null argument handling – the manager does not guard against null,
     *  therefore a NullPointerException is expected.
     * ---------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testRunWithNullStrategyThrowsNPE() {
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost, execModel);
        mgr.run(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullBarSeriesThrowsNPE() {
        new BarSeriesManager(null);
    }

    /* ----------------------------------------------------------------------
     *  Verify that the internal clamping logic does not create extra
     *  iterations after the series end when the last position stays open.
     * ---------------------------------------------------------------------- */

    @Test
    public void testRunLeavesOpenPositionClosedAtSeriesEnd() {
        // Strategy triggers entry at last index only – no chance to exit.
        BarSeriesManager mgr = new BarSeriesManager(series5, zeroCost, zeroCost,
                // Execution model that only tries to enter; never exits
                (i, record, series, amount) -> record.enter(i, amount, amount));
        Strategy strat = new SimpleStrategy(4);
        TradingRecord rec = mgr.run(strat);
        // Position remains open because there is no further bar.
        assertFalse(rec.isClosed());
        // The manager should *not* try to iterate beyond series end.
        // No exception means the behaviour is correct.
    }
}
```
###Test END##