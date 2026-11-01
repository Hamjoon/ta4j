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

/**
 * Tests for {@link NumericIndicator#closePrice(BarSeries)}.
 *
 * The tests are self‑contained and use only the minimal assertions defined
 * in this class, avoiding external test frameworks.
 */
class NumericIndicator_closePrice_Test {

    /**
     * Very small {@link Bar} implementation that only stores a close price.
     */
    private static final class SimpleBar implements Bar {

        private final Num closePrice;

        SimpleBar(Num closePrice) {
            this.closePrice = closePrice;
        }

        @Override
        public Num getClosePrice() {
            return closePrice;
        }

        @Override
        public Num getOpenPrice() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Num getHighPrice() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Num getLowPrice() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Num getVolume() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Num getAmount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTrades() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTimestamp() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getEndTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCompleted() {
            throw new UnsupportedOperationException();
        }

        // method required by the Bar interface in the library
        @Override
        public void addPrice(Num price) {
            // no‑op for this minimal implementation
        }
    }

    /**
     * Very small mutable {@link BarSeries} implementation used by the tests.
     */
    private static final class MockBarSeries implements BarSeries {

        private final List<Bar> bars = new ArrayList<>();

        @SafeVarargs
        MockBarSeries(java.util.function.Function<Number, Num> numFactory, double... closePrices) {
            for (double cp : closePrices) {
                Num n = numFactory.apply(cp);
                bars.add(new SimpleBar(n));
            }
        }

        // empty constructor used for sub‑series / copy
        MockBarSeries(java.util.function.Function<Number, Num> numFactory) {
        }

        /**
         * Adds a bar to the series (used by the dynamic‑series test).
         */
        public void addBar(Bar bar) {
            bars.add(bar);
        }

        @Override
        public int getBarCount() {
            return bars.size();
        }

        @Override
        public Bar getBar(int index) {
            return bars.get(index);
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            MockBarSeries sub = new MockBarSeries(DecimalNum::valueOf);
            for (int i = startIndex; i < endIndex; i++) {
                sub.bars.add(this.bars.get(i));
            }
            return sub;
        }

        @Override
        public BarSeries copy() {
            MockBarSeries copy = new MockBarSeries(DecimalNum::valueOf);
            copy.bars.addAll(this.bars);
            return copy;
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getEndTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Bar> getBarData() {
            throw new UnsupportedOperationException();
        }

        // method required by the BarSeries interface in the library
        @Override
        public void addPrice(Num price) {
            // no‑op for this minimal implementation
        }
    }

    /**
     * BarSeries that returns {@code null} for {@code getBar(0)}.
     */
    private static final class NullBarSeries implements BarSeries {

        @Override
        public int getBarCount() {
            return 1;
        }

        @Override
        public Bar getBar(int index) {
            return null;
        }

        @Override
        public String getName() {
            return "nullBarSeries";
        }

        @Override
        public Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getEndTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Bar> getBarData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BarSeries copy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPrice(Num price) {
            // no‑op
        }
    }

    /**
     * BarSeries whose single bar has a {@code null} close price.
     */
    private static final class BarWithNullCloseSeries implements BarSeries {

        private final Bar bar = new Bar() {

            @Override
            public Num getClosePrice() {
                return null;
            }

            @Override
            public Num getOpenPrice() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Num getHighPrice() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Num getLowPrice() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Num getVolume() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Num getAmount() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getTrades() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getTimestamp() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ZonedDateTime getEndTime() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isCompleted() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addPrice(Num price) {
                // no‑op
            }
        };

        @Override
        public int getBarCount() {
            return 1;
        }

        @Override
        public Bar getBar(int index) {
            return bar;
        }

        @Override
        public String getName() {
            return "barWithNullCloseSeries";
        }

        @Override
        public Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getEndTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Bar> getBarData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BarSeries copy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPrice(Num price) {
            // no‑op
        }
    }

    /**
     * BarSeries that throws a {@link RuntimeException} on any {@code getBar}
     * call.
     */
    private static final class FaultySeries implements BarSeries {

        @Override
        public int getBarCount() {
            return 1;
        }

        @Override
        public Bar getBar(int index) {
            throw new RuntimeException("Faulty series access");
        }

        @Override
        public String getName() {
            return "faultySeries";
        }

        @Override
        public Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ZonedDateTime getEndTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Bar> getBarData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BarSeries copy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPrice(Num price) {
            // no‑op
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper assertion methods (minimal JUnit‑like functionality)          */
    /* --------------------------------------------------------------------- */

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " – expected: " + expected + ", actual: " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Values differ");
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
                // success
                return;
            }
            throw new AssertionError(message + " – threw unexpected exception: " + t);
        }
        throw new AssertionError(message + " – no exception thrown");
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable) {
        assertThrows(expectedType, runnable, "Expected exception");
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test helpers – create various series                                 */
    /* --------------------------------------------------------------------- */

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
        // First bar normal, second bar NaN close price
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

    /* --------------------------------------------------------------------- */
    /* Actual test cases                                                    */
    /* --------------------------------------------------------------------- */

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
        assertEquals(DecimalNum.valueOf(100.5), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(101.2), ni.getValue(1));
    }

    private static void testClosePriceSingleBar() {
        BarSeries series = singleBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(55.0), ni.getValue(0));
    }

    private static void testClosePriceEmptySeriesThrows() {
        BarSeries series = emptyBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(IndexOutOfBoundsException.class, () -> ni.getValue(0));
    }

    private static void testClosePriceNullSeriesThrows() {
        assertThrows(NullPointerException.class, () -> NumericIndicator.closePrice(null));
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
        BarSeries series = seriesWithNullBar();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
    }

    private static void testClosePriceBarWithNullCloseThrowsOnAccess() {
        BarSeries series = seriesWithBarHavingNullClose();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
    }

    private static void testClosePriceFaultySeriesPropagatesException() {
        BarSeries series = faultySeriesThrowsOnAccess();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ni.getValue(0));
        assertEquals("Faulty series access", ex.getMessage());
    }

    public static void main(String[] args) {
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
}
```