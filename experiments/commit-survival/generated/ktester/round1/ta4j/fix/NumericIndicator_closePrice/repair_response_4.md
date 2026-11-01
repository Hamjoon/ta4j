```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Bar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Unit‑tests for {@link NumericIndicator#closePrice(BarSeries)}.
 * <p>
 * The test class is deliberately self‑contained and does not depend on JUnit or
 * Mockito. Simple assertion helpers are provided locally. The test methods are
 * invoked from {@code main(String[])}.
 */
class NumericIndicator_closePrice_Test {

    /** Series used for the JUnit‑style tests. */
    private BarSeries series;

    /* --------------------------------------------------------------------- *
     *  Minimal {@link Bar} implementation (stores only a close price).       *
     * --------------------------------------------------------------------- */
    private static final class SimpleBar implements Bar {

        private final Num closePrice;

        SimpleBar(Num closePrice) {
            this.closePrice = closePrice;
        }

        @Override
        public Num getClosePrice() {
            return closePrice;
        }

        @Override public Num getOpenPrice()  { throw new UnsupportedOperationException(); }
        @Override public Num getHighPrice()  { throw new UnsupportedOperationException(); }
        @Override public Num getLowPrice()   { throw new UnsupportedOperationException(); }
        @Override public Num getVolume()    { throw new UnsupportedOperationException(); }
        @Override public Num getAmount()    { throw new UnsupportedOperationException(); }
        @Override public long getTrades()   { throw new UnsupportedOperationException(); }
        @Override public long getTimestamp(){ throw new UnsupportedOperationException(); }

        /** Not required for the tests – return {@code null}. */
        @Override public ZonedDateTime getBeginTime() { return null; }

        @Override public ZonedDateTime getEndTime()       { throw new UnsupportedOperationException(); }
        @Override public boolean isCompleted()           { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price)        { /* no‑op */ }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { /* no‑op */ }
    }

    /* --------------------------------------------------------------------- *
     *  Minimal mutable {@link BarSeries} implementation.                    *
     * --------------------------------------------------------------------- */
    private static final class MockBarSeries implements BarSeries {

        private final List<Bar> bars = new ArrayList<>();

        @SafeVarargs
        MockBarSeries(Function<Number, Num> numFactory, double... closePrices) {
            for (double cp : closePrices) {
                Num n = numFactory.apply(cp);
                bars.add(new SimpleBar(n));
            }
        }

        /** Empty constructor used for sub‑series / copy. */
        MockBarSeries(Function<Number, Num> numFactory) {
            // nothing to initialise
        }

        /** Adds a bar to the series (used by the dynamic‑series test). */
        public void addBar(Bar bar) {
            bars.add(bar);
        }

        /* ------------ BarSeries contract (only the methods needed for the tests) ------------ */

        @Override public int getBarCount()                     { return bars.size(); }
        @Override public Bar getBar(int index)                { return bars.get(index); }
        @Override public BarSeries getSubSeries(int start, int end) {
            MockBarSeries sub = new MockBarSeries(DecimalNum::valueOf);
            for (int i = start; i < end; i++) {
                sub.bars.add(this.bars.get(i));
            }
            return sub;
        }
        @Override public BarSeries copy() {
            MockBarSeries copy = new MockBarSeries(DecimalNum::valueOf);
            copy.bars.addAll(this.bars);
            return copy;
        }
        @Override public String getName()                     { throw new UnsupportedOperationException(); }
        @Override public Duration getBarDuration()            { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getBeginTime()          { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getEndTime()            { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData()               { throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price)             { /* no‑op */ }
        @Override public void addTrade(Num tradeVolume, Num tradePrice) { /* no‑op */ }

        /**
         * Full signature required by {@link BarSeries}. For the purposes of
         * these tests we simply create a {@link SimpleBar} with the supplied
         * close price and ignore the remaining parameters.
         */
        @Override
        public void addBar(Duration duration,
                           ZonedDateTime endTime,
                           Num openPrice,
                           Num highPrice,
                           Num lowPrice,
                           Num closePrice,
                           Num volume,
                           Num amount) {
            bars.add(new SimpleBar(closePrice));
        }
    }

    /* --------------------------------------------------------------------- *
     *  Series that returns {@code null} for {@code getBar(0)}.              *
     * --------------------------------------------------------------------- */
    private static final class NullBarSeries implements BarSeries {

        @Override public int getBarCount()                     { return 1; }
        @Override public Bar getBar(int index)                { return null; }
        @Override public String getName()                     { return "nullBarSeries"; }
        @Override public Duration getBarDuration()            { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getBeginTime()          { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getEndTime()            { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData()               { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy()                     { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int s, int e){ throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price)             { /* no‑op */ }
        @Override public void addTrade(Num v, Num p)          { /* no‑op */ }

        @Override
        public void addBar(Duration d,
                           ZonedDateTime t,
                           Num o,
                           Num h,
                           Num l,
                           Num c,
                           Num v,
                           Num a) {
            // not used in the tests
        }
    }

    /* --------------------------------------------------------------------- *
     *  Series whose single bar has a {@code null} close price.              *
     * --------------------------------------------------------------------- */
    private static final class BarWithNullCloseSeries implements BarSeries {

        private final Bar bar = new Bar() {
            @Override public Num getClosePrice()  { return null; }
            @Override public Num getOpenPrice()   { throw new UnsupportedOperationException(); }
            @Override public Num getHighPrice()   { throw new UnsupportedOperationException(); }
            @Override public Num getLowPrice()    { throw new UnsupportedOperationException(); }
            @Override public Num getVolume()      { throw new UnsupportedOperationException(); }
            @Override public Num getAmount()      { throw new UnsupportedOperationException(); }
            @Override public long getTrades()     { throw new UnsupportedOperationException(); }
            @Override public long getTimestamp()  { throw new UnsupportedOperationException(); }
            @Override public ZonedDateTime getBeginTime() { return null; }   // required by Bar
            @Override public ZonedDateTime getEndTime()   { throw new UnsupportedOperationException(); }
            @Override public boolean isCompleted()       { throw new UnsupportedOperationException(); }
            @Override public void addPrice(Num price)    { /* no‑op */ }
            @Override public void addTrade(Num v, Num p){ /* no‑op */ }
        };

        @Override public int getBarCount()                     { return 1; }
        @Override public Bar getBar(int index)                { return bar; }
        @Override public String getName()                     { return "barWithNullCloseSeries"; }
        @Override public Duration getBarDuration()            { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getBeginTime()          { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getEndTime()            { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData()               { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy()                     { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int s, int e){ throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price)             { /* no‑op */ }
        @Override public void addTrade(Num v, Num p)          { /* no‑op */ }

        @Override
        public void addBar(Duration d,
                           ZonedDateTime t,
                           Num o,
                           Num h,
                           Num l,
                           Num c,
                           Num v,
                           Num a) {
            // not used in the tests
        }
    }

    /* --------------------------------------------------------------------- *
     *  Series that throws on any {@code getBar} access.                     *
     * --------------------------------------------------------------------- */
    private static final class FaultySeries implements BarSeries {

        @Override public int getBarCount()                     { return 1; }
        @Override public Bar getBar(int index)                { throw new RuntimeException("Faulty series access"); }
        @Override public String getName()                     { return "faultySeries"; }
        @Override public Duration getBarDuration()            { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getBeginTime()          { throw new UnsupportedOperationException(); }
        @Override public ZonedDateTime getEndTime()            { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData()               { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy()                     { throw new UnsupportedOperationException(); }
        @Override public BarSeries getSubSeries(int s, int e){ throw new UnsupportedOperationException(); }
        @Override public void addPrice(Num price)             { /* no‑op */ }
        @Override public void addTrade(Num v, Num p)          { /* no‑op */ }

        @Override
        public void addBar(Duration d,
                           ZonedDateTime t,
                           Num o,
                           Num h,
                           Num l,
                           Num c,
                           Num v,
                           Num a) {
            // not used in the tests
        }
    }

    /* --------------------------------------------------------------------- *
     *  JUnit‑style helper methods (very small subset)                       *
     * --------------------------------------------------------------------- */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " – expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " – expected same instance");
        }
    }

    private static void assertNotSame(Object a, Object b, String message) {
        if (a == b) {
            throw new AssertionError(message + " – both references are the same");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return;
            }
            throw new AssertionError("Unexpected exception type thrown", t);
        }
        throw new AssertionError("Expected exception of type " + expectedType.getName() + " but nothing was thrown");
    }

    /* --------------------------------------------------------------------- *
     *  Test data factories                                                   *
     * --------------------------------------------------------------------- */
    private static BarSeries validBarSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 100.5, 101.2);
    }

    private static BarSeries singleBarSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 55.0);
    }

    private static BarSeries emptyBarSeries() {
        return new MockBarSeries(DecimalNum::valueOf);
    }

    private static BarSeries fiveBarSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 10.0, 20.0, 30.5, 40.0, 50.0);
    }

    private static BarSeries extremeValueSeries() {
        return new MockBarSeries(DecimalNum::valueOf, Double.MAX_VALUE, Double.MIN_VALUE, 0.0, -12345.678);
    }

    private static BarSeries constantSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 42.42, 42.42, 42.42);
    }

    private static BarSeries nanCloseSeries() {
        return new MockBarSeries(num -> DecimalNum.valueOf(num), 10.0, Double.NaN);
    }

    private static BarSeries dynamicSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 10.0, 20.0);
    }

    private static BarSeries seriesWithNullBar() {
        return new NullBarSeries();
    }

    private static BarSeries seriesWithBarHavingNullClose() {
        return new BarWithNullCloseSeries();
    }

    private static BarSeries faultySeriesThrowsOnAccess() {
        return new FaultySeries();
    }

    /* --------------------------------------------------------------------- *
     *  Actual test methods (invoked from main)                               *
     * --------------------------------------------------------------------- */
    private static void testClosePriceCreatesClosePriceIndicator() {
        BarSeries series = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40, 50);
        NumericIndicator numericIndicator = NumericIndicator.closePrice(series);
        assertNotNull(numericIndicator, "NumericIndicator should not be null");
        assertEquals