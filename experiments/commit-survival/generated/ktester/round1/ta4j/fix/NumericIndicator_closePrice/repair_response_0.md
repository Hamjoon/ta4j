```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Simple test harness for {@link NumericIndicator#closePrice(BarSeries)}.
 *
 * <p>This class does not depend on external test libraries (JUnit, Mockito, …)
 * and therefore compiles in a minimal environment.  It reproduces the
 * assertions that were originally expressed with JUnit and Mockito.</p>
 */
public class NumericIndicatorClosePriceTest {

    /* --------------------------------------------------------------------- *
     *  Minimal mock implementations                                           *
     * --------------------------------------------------------------------- */

    /** Simple {@link Bar} implementation that only stores a close price. */
    private static final class SimpleBar implements Bar {
        private final Num closePrice;

        SimpleBar(Num closePrice) {
            this.closePrice = closePrice;
        }

        @Override
        public Num getClosePrice() {
            return closePrice;
        }

        /* The remaining Bar methods are not needed for the tests. */
        @Override public Num getOpenPrice()            { throw new UnsupportedOperationException(); }
        @Override public Num getHighPrice()            { throw new UnsupportedOperationException(); }
        @Override public Num getLowPrice()             { throw new UnsupportedOperationException(); }
        @Override public Num getVolume()               { throw new UnsupportedOperationException(); }
        @Override public Num getAmount()               { throw new UnsupportedOperationException(); }
        @Override public int getTrades()               { throw new UnsupportedOperationException(); }
        @Override public long getTimestamp()           { throw new UnsupportedOperationException(); }
        @Override public long getEndTime()             { throw new UnsupportedOperationException(); }
        @Override public boolean isCompleted()        { throw new UnsupportedOperationException(); }
    }

    /**
     * Very small {@link BarSeries} implementation used by the tests.
     * It stores bars in a mutable list so that new bars can be appended.
     */
    private static final class MockBarSeries implements BarSeries {
        private final List<Bar> bars = new ArrayList<>();

        /** Constructs a series from the supplied close‑price values. */
        @SafeVarargs
        MockBarSeries(Function<Number, Num> numFactory, double... closePrices) {
            for (double cp : closePrices) {
                Num n = numFactory.apply(cp);
                bars.add(new SimpleBar(n));
            }
        }

        /** Adds a bar to the series (used by the dynamic‑series test). */
        void addBar(Bar bar) {
            bars.add(bar);
        }

        @Override public int getBarCount() {
            return bars.size();
        }

        @Override public Bar getBar(int index) {
            return bars.get(index);
        }

        /* The remaining BarSeries methods are not required for the current tests. */
        @Override public String getName()                     { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.Duration getBarDuration() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getBeginTime() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getEndTime()   { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData()               { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy()                     { throw new UnsupportedOperationException(); }
    }

    /** BarSeries that returns {@code null} for {@code getBar(0)}. */
    private static final class NullBarSeries implements BarSeries {
        @Override public int getBarCount() { return 1; }
        @Override public Bar getBar(int index) { return null; }
        @Override public String getName() { return "nullBarSeries"; }
        @Override public org.joda.time.Duration getBarDuration() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getBeginTime() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getEndTime() { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy() { throw new UnsupportedOperationException(); }
    }

    /** BarSeries whose single bar has a {@code null} close price. */
    private static final class BarWithNullCloseSeries implements BarSeries {
        private final Bar bar = new Bar() {
            @Override public Num getClosePrice() { return null; }
            @Override public Num getOpenPrice() { throw new UnsupportedOperationException(); }
            @Override public Num getHighPrice() { throw new UnsupportedOperationException(); }
            @Override public Num getLowPrice() { throw new UnsupportedOperationException(); }
            @Override public Num getVolume() { throw new UnsupportedOperationException(); }
            @Override public Num getAmount() { throw new UnsupportedOperationException(); }
            @Override public int getTrades() { throw new UnsupportedOperationException(); }
            @Override public long getTimestamp() { throw new UnsupportedOperationException(); }
            @Override public long getEndTime() { throw new UnsupportedOperationException(); }
            @Override public boolean isCompleted() { throw new UnsupportedOperationException(); }
        };

        @Override public int getBarCount() { return 1; }
        @Override public Bar getBar(int index) { return bar; }
        @Override public String getName() { return "barWithNullCloseSeries"; }
        @Override public org.joda.time.Duration getBarDuration() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getBeginTime() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getEndTime() { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy() { throw new UnsupportedOperationException(); }
    }

    /** BarSeries that throws a {@link RuntimeException} on any {@code getBar} call. */
    private static final class FaultySeries implements BarSeries {
        @Override public int getBarCount() { return 1; }
        @Override public Bar getBar(int index) { throw new RuntimeException("Faulty series access"); }
        @Override public String getName() { return "faultySeries"; }
        @Override public org.joda.time.Duration getBarDuration() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getBeginTime() { throw new UnsupportedOperationException(); }
        @Override public org.joda.time.ZonedDateTime getEndTime() { throw new UnsupportedOperationException(); }
        @Override public List<Bar> getBarData() { throw new UnsupportedOperationException(); }
        @Override public BarSeries copy() { throw new UnsupportedOperationException(); }
    }

    /* --------------------------------------------------------------------- *
     *  Helper assertion methods (mimic JUnit)                               *
     * --------------------------------------------------------------------- */

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " – expected: " + expected + ", actual: " + actual);
        }
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

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable, String message) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return; // success
            }
            throw new AssertionError(message + " – threw unexpected exception: " + t);
        }
        throw new AssertionError(message + " – no exception thrown");
    }

    /* --------------------------------------------------------------------- *
     *  Series factories used by the tests                                    *
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
        return new MockBarSeries(DecimalNum::valueOf,
                Double.MAX_VALUE, Double.MIN_VALUE, 0.0, -12345.678);
    }

    private static BarSeries constantSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 42.42, 42.42, 42.42);
    }

    private static BarSeries nanCloseSeries() {
        // First bar normal, second bar NaN close price
        return new MockBarSeries(num -> DecimalNum.valueOf(num), 10.0, Double.NaN);
    }

    private static BarSeries dynamicSeries() {
        return new MockBarSeries(DecimalNum::valueOf, 10.0, 20.0);
    }

    /* --------------------------------------------------------------------- *
     *  Test methods                                                          *
     * --------------------------------------------------------------------- */

    private static void testClosePriceCreatesClosePriceIndicator() {
        BarSeries series = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40, 50);
        NumericIndicator numericIndicator = NumericIndicator.closePrice(series);

        assertNotNull(numericIndicator, "NumericIndicator should not be null");
        assertEquals(ClosePriceIndicator.class, numericIndicator.delegate().getClass(),
                "Delegate should be a ClosePriceIndicator");

        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getBar(i).getClosePrice();
            Num actual = numericIndicator.getValue(i);
            assertEquals(0, expected.compareTo(actual),
                    "Value at index " + i + " should match the series close price");
        }

        assertSame(series, numericIndicator.getBarSeries(),
                "BarSeries returned by NumericIndicator should be the original series");
    }

    private static void testClosePriceTypicalTwoBars() {
        BarSeries series = validBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(100.5), ni.getValue(0), "first value");
        assertEquals(DecimalNum.valueOf(101.2), ni.getValue(1), "second value");
    }

    private static void testClosePriceSingleBar() {
        BarSeries series = singleBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(55.0), ni.getValue(0), "single bar value");
    }

    private static void testClosePriceEmptySeriesThrows() {
        BarSeries series = emptyBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(IndexOutOfBoundsException.class,
                () -> ni.getValue(0),
                "empty series should throw");
    }

    private static void testClosePriceNullSeriesThrows() {
        assertThrows(NullPointerException.class,
                () -> NumericIndicator.closePrice(null),
                "null series should throw");
    }

    private static void testClosePriceFiveBarSeriesValues() {
        BarSeries series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(30.5), ni.getValue(2));
        assertEquals(DecimalNum.valueOf(50.0), ni.getValue(4));
    }

    private static void testClosePriceExtremeValues() {
        BarSeries series = extremeValueSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(Double.MAX_VALUE), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(Double.MIN_VALUE), ni.getValue(1));
        assertEquals(DecimalNum.valueOf(0.0), ni.getValue(2));
        assertEquals(DecimalNum.valueOf(-12345.678), ni.getValue(3));
    }

    private static void testClosePriceConstantSeries() {
        BarSeries series = constantSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.valueOf(42.42), ni.getValue(i));
        }
    }

    private static void testClosePriceDynamicSeriesReflectsAppend() {
        MockBarSeries mutableSeries = (MockBarSeries) dynamicSeries();
        NumericIndicator ni = NumericIndicator.closePrice(mutableSeries);

        // Append a third bar (close price 200.0)
        Bar newBar = new SimpleBar(DecimalNum.valueOf(200.0));
        mutableSeries.addBar(newBar);

        assertEquals(DecimalNum.valueOf(200.0), ni.getValue(2),
                "NumericIndicator must reflect newly added bar");
    }

    private static void testClosePriceDistinctInstances() {
        BarSeries series = fiveBarSeries();
        NumericIndicator ni1 = NumericIndicator.closePrice(series);
        NumericIndicator ni2 = NumericIndicator.closePrice(series);
        assertNotSame(ni1, ni2, "Two calls must return distinct NumericIndicator objects");
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(ni1.getValue(i), ni2.getValue(i),
                    "Both instances should produce identical values at index " + i);
        }
    }

    private static void testClosePricePropagatesSeriesAndUnstableBars() {
        BarSeries series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertSame(series, ni.getBarSeries(),
                "NumericIndicator must return the original BarSeries");
        assertEquals(0, ni.getUnstableBars(),
                "ClosePriceIndicator is stable – unstable bars should be 0");
    }

    private static void testClosePriceNaNCloseSeries() {
        BarSeries series = nanCloseSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        assertTrue(ni.getValue(1).isNaN(),
                "Close price NaN must be propagated as Num.NaN");
    }

    private static void testClosePriceSeriesWithNullBarThrowsOnAccess() {
        BarSeries series = new NullBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class,
                () -> ni.getValue(0),
                "null bar should cause NPE");
    }

    private static void testClosePriceBarWithNullCloseThrowsOnAccess() {
        BarSeries series = new BarWithNullCloseSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class,
                () -> ni.getValue(0),
                "null close price should cause NPE");
    }

    private static void testClosePriceFaultySeriesPropagatesException() {
        BarSeries series = new FaultySeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ni.getValue(0),
                "faulty series should propagate exception");
        assertEquals("Faulty series access", ex.getMessage());
    }

    /* --------------------------------------------------------------------- *
     *  Main entry point                                                       *
     * --------------------------------------------------------------------- */

    public static void main(String[] args) {
        // Run all test scenarios.  Any failure will throw an AssertionError.
        testClosePriceCreatesClosePriceIndicator();
        testClosePriceTypicalTwoBars();
        testClosePriceSingleBar();
        testClosePriceEmptySeriesThrows();
        testClosePriceNullSeriesThrows();
        testClosePriceFiveBarSeriesValues();
        testClosePriceExtremeValues();
        testClosePriceConstantSeries();
        testClosePriceDynamicSeriesReflectsAppend();
        testClosePriceDistinctInstances();
        testClosePricePropagatesSeriesAndUnstableBars();
        testClosePriceNaNCloseSeries();
        testClosePriceSeriesWithNullBarThrowsOnAccess();
        testClosePriceBarWithNullCloseThrowsOnAccess();
        testClosePriceFaultySeriesPropagatesException();

        System.out.println("All NumericIndicator.closePrice tests passed.");
    }

    /* --------------------------------------------------------------------- *
     *  Small utility – null‑check for assertions                              *
     * --------------------------------------------------------------------- */

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
}
```