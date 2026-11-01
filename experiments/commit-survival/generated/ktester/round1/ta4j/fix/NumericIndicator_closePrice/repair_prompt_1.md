
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.util.function.Function;
import org.ta4j.core.Bar;
import java.util.ArrayList;
import java.util.List;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_closePrice_Test {

    /**
     * Series used for all tests in this class.
     */
    private BarSeries series;

    /**
     * Simple {@link Bar} implementation that only stores a close price.
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

        /* The remaining Bar methods are not needed for the tests. */
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
        public int getTrades() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTimestamp() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getEndTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCompleted() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Very small {@link BarSeries} implementation used by the tests.
     * It stores bars in a mutable list so that new bars can be appended.
     */
    private static final class MockBarSeries implements BarSeries {

        private final List<Bar> bars = new ArrayList<>();

        /**
         * Constructs a series from the supplied close‑price values.
         */
        @SafeVarargs
        MockBarSeries(Function<Number, Num> numFactory, double... closePrices) {
            for (double cp : closePrices) {
                Num n = numFactory.apply(cp);
                bars.add(new SimpleBar(n));
            }
        }

        /**
         * Adds a bar to the series (used by the dynamic‑series test).
         */
        void addBar(Bar bar) {
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

        /* The remaining BarSeries methods are not required for the current tests. */
        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getEndTime() {
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
        public org.joda.time.Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getEndTime() {
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
            public int getTrades() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getTimestamp() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getEndTime() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isCompleted() {
                throw new UnsupportedOperationException();
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
        public org.joda.time.Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getEndTime() {
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
    }

    /**
     * BarSeries that throws a {@link RuntimeException} on any {@code getBar} call.
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
        public org.joda.time.Duration getBarDuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getBeginTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.joda.time.ZonedDateTime getEndTime() {
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
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple mock series with known close‑price values.
        // DecimalNum::valueOf converts a Number into the library's Num implementation.
        series = new // 5 bars with close prices 10‑50
        MockBarSeries(// 5 bars with close prices 10‑50
        DecimalNum::valueOf, // 5 bars with close prices 10‑50
        10, // 5 bars with close prices 10‑50
        20, // 5 bars with close prices 10‑50
        30, // 5 bars with close prices 10‑50
        40, 50);
    }

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
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(0)).thenReturn(null);
        return mockSeries;
    }

    private static BarSeries seriesWithBarHavingNullClose() {
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
        Bar mockBar = Mockito.mock(Bar.class);
        Mockito.when(mockBar.getClosePrice()).thenReturn(null);
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(0)).thenReturn(mockBar);
        return mockSeries;
    }

    private static BarSeries faultySeriesThrowsOnAccess() {
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(Mockito.anyInt())).thenThrow(new RuntimeException("Faulty series access"));
        return mockSeries;
    }

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
                // success
                return;
            }
            throw new AssertionError(message + " – threw unexpected exception: " + t);
        }
        throw new AssertionError(message + " – no exception thrown");
    }

    private static void testClosePriceCreatesClosePriceIndicator() {
        BarSeries series = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40, 50);
        NumericIndicator numericIndicator = NumericIndicator.closePrice(series);
        assertNotNull(numericIndicator, "NumericIndicator should not be null");
        assertEquals(ClosePriceIndicator.class, numericIndicator.delegate().getClass(), "Delegate should be a ClosePriceIndicator");
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getBar(i).getClosePrice();
            Num actual = numericIndicator.getValue(i);
            assertEquals(0, expected.compareTo(actual), "Value at index " + i + " should match the series close price");
        }
        assertSame(series, numericIndicator.getBarSeries(), "BarSeries returned by NumericIndicator should be the original series");
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
        assertThrows(IndexOutOfBoundsException.class, () -> ni.getValue(0), "empty series should throw");
    }

    private static void testClosePriceNullSeriesThrows() {
        assertThrows(NullPointerException.class, () -> NumericIndicator.closePrice(null), "null series should throw");
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
        assertEquals(DecimalNum.valueOf(200.0), ni.getValue(2), "NumericIndicator must reflect newly added bar");
    }

    private static void testClosePriceDistinctInstances() {
        BarSeries series = fiveBarSeries();
        NumericIndicator ni1 = NumericIndicator.closePrice(series);
        NumericIndicator ni2 = NumericIndicator.closePrice(series);
        assertNotSame(ni1, ni2, "Two calls must return distinct NumericIndicator objects");
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(ni1.getValue(i), ni2.getValue(i), "Both instances should produce identical values at index " + i);
        }
    }

    private static void testClosePricePropagatesSeriesAndUnstableBars() {
        BarSeries series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertSame(series, ni.getBarSeries(), "NumericIndicator must return the original BarSeries");
        assertEquals(0, ni.getUnstableBars(), "ClosePriceIndicator is stable – unstable bars should be 0");
    }

    private static void testClosePriceNaNCloseSeries() {
        BarSeries series = nanCloseSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        assertTrue(ni.getValue(1).isNaN(), "Close price NaN must be propagated as Num.NaN");
    }

    private static void testClosePriceSeriesWithNullBarThrowsOnAccess() {
        BarSeries series = new NullBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0), "null bar should cause NPE");
    }

    private static void testClosePriceBarWithNullCloseThrowsOnAccess() {
        BarSeries series = new BarWithNullCloseSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0), "null close price should cause NPE");
    }

    private static void testClosePriceFaultySeriesPropagatesException() {
        BarSeries series = new FaultySeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ni.getValue(0), "faulty series should propagate exception");
        assertEquals("Faulty series access", ex.getMessage());
    }

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

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    @Test
    void closePriceCreatesClosePriceIndicator() {
        // when
        NumericIndicator numericIndicator = NumericIndicator.closePrice(series);
        // then
        assertNotNull(numericIndicator, "NumericIndicator should not be null");
        // the underlying delegate must be a ClosePriceIndicator
        assertEquals(ClosePriceIndicator.class, numericIndicator.delegate().getClass(), "Delegate should be a ClosePriceIndicator");
        // verify that the values are the same as the series' close prices
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = series.getBar(i).getClosePrice();
            Num actual = numericIndicator.getValue(i);
            assertEquals(0, expected.compareTo(actual), "Value at index " + i + " should match the series close price");
        }
        // also ensure the series reference is correctly propagated
        assertSame(series, numericIndicator.getBarSeries(), "BarSeries returned by NumericIndicator should be the original series");
    }

    @Test
    void closePriceTypicalTwoBars() {
        series = validBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(100.5), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(101.2), ni.getValue(1));
    }

    @Test
    void closePriceSingleBar() {
        series = singleBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(55.0), ni.getValue(0));
    }

    @Test
    void closePriceEmptySeriesThrows() {
        series = emptyBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(IndexOutOfBoundsException.class, () -> ni.getValue(0));
    }

    @Test
    void closePriceNullSeriesThrows() {
        assertThrows(NullPointerException.class, () -> NumericIndicator.closePrice(null));
    }

    @Test
    void closePriceFiveBarSeriesValues() {
        series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(30.5), ni.getValue(2));
        assertEquals(DecimalNum.valueOf(50.0), ni.getValue(4));
    }

    @Test
    void closePriceExtremeValues() {
        series = extremeValueSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertEquals(DecimalNum.valueOf(Double.MAX_VALUE), ni.getValue(0));
        assertEquals(DecimalNum.valueOf(Double.MIN_VALUE), ni.getValue(1));
        assertEquals(DecimalNum.valueOf(0.0), ni.getValue(2));
        assertEquals(DecimalNum.valueOf(-12345.678), ni.getValue(3));
    }

    @Test
    void closePriceConstantSeries() {
        series = constantSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(DecimalNum.valueOf(42.42), ni.getValue(i));
        }
    }

    @Test
    void closePriceDynamicSeriesReflectsAppend() {
        // start with two bars
        BarSeries mutableSeries = dynamicSeries();
        NumericIndicator ni = NumericIndicator.closePrice(mutableSeries);
        // Append a third bar (close price 200.0) using a Mockito mock Bar
        Bar newBar = Mockito.mock(Bar.class);
        Mockito.when(newBar.getClosePrice()).thenReturn(DecimalNum.valueOf(200.0));
        // MockBarSeries implements BarSeries, so we can cast and add
        ((MockBarSeries) mutableSeries).addBar(newBar);
        assertEquals(DecimalNum.valueOf(200.0), ni.getValue(2), "NumericIndicator must reflect newly added bar");
    }

    @Test
    void closePriceDistinctInstances() {
        series = fiveBarSeries();
        NumericIndicator ni1 = NumericIndicator.closePrice(series);
        NumericIndicator ni2 = NumericIndicator.closePrice(series);
        assertNotSame(ni1, ni2, "Two calls must return distinct NumericIndicator objects");
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(ni1.getValue(i), ni2.getValue(i), "Both instances should produce identical values at index " + i);
        }
    }

    @Test
    void closePricePropagatesSeriesAndUnstableBars() {
        series = fiveBarSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertSame(series, ni.getBarSeries(), "NumericIndicator must return the original BarSeries");
        assertEquals(0, ni.getUnstableBars(), "ClosePriceIndicator is stable – unstable bars should be 0");
    }

    @Test
    void closePriceNaNCloseSeries() {
        series = nanCloseSeries();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        // first value normal
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        // second value should be NaN
        assertTrue(ni.getValue(1).isNaN(), "Close price NaN must be propagated as Num.NaN");
    }

    @Test
    void closePriceSeriesWithNullBarThrowsOnAccess() {
        series = seriesWithNullBar();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
    }

    @Test
    void closePriceBarWithNullCloseThrowsOnAccess() {
        series = seriesWithBarHavingNullClose();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
    }

    @Test
    void closePriceFaultySeriesPropagatesException() {
        series = faultySeriesThrowsOnAccess();
        NumericIndicator ni = NumericIndicator.closePrice(series);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ni.getValue(0));
        assertEquals("Faulty series access", ex.getMessage());
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:11: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:14: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:16: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:13: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:130: error: package org.joda.time does not exist
        public org.joda.time.Duration getBarDuration() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:135: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getBeginTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:140: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getEndTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:176: error: package org.joda.time does not exist
        public org.joda.time.Duration getBarDuration() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:181: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getBeginTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:186: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getEndTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:275: error: package org.joda.time does not exist
        public org.joda.time.Duration getBarDuration() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:280: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getBeginTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:285: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getEndTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:321: error: package org.joda.time does not exist
        public org.joda.time.Duration getBarDuration() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:326: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getBeginTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:331: error: package org.joda.time does not exist
        public org.joda.time.ZonedDateTime getEndTime() {
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:16: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:346: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:27: error: SimpleBar is not abstract and does not override abstract method addPrice(Num) in Bar
    private static final class SimpleBar implements Bar {
                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:67: error: getTrades() in SimpleBar cannot implement getTrades() in Bar
        public int getTrades() {
                   ^
  return type int is not compatible with long
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:77: error: getEndTime() in SimpleBar cannot implement getEndTime() in Bar
        public long getEndTime() {
                    ^
  return type long is not compatible with ZonedDateTime
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:66: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:71: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:76: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:81: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:91: error: MockBarSeries is not abstract and does not override abstract method getSubSeries(int,int) in BarSeries
    private static final class MockBarSeries implements BarSeries {
                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:109: error: addBar(Bar) in MockBarSeries cannot implement addBar(Bar) in BarSeries
        void addBar(Bar bar) {
             ^
  attempting to assign weaker access privileges; was public
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:129: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:134: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:139: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:149: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:158: error: NullBarSeries is not abstract and does not override abstract method getSubSeries(int,int) in BarSeries
    private static final class NullBarSeries implements BarSeries {
                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:175: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:180: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:185: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:195: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:204: error: BarWithNullCloseSeries is not abstract and does not override abstract method getSubSeries(int,int) in BarSeries
    private static final class BarWithNullCloseSeries implements BarSeries {
                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:206: error: <anonymous org.ta4j.core.indicators.numeric.NumericIndicator_closePrice_Test$BarWithNullCloseSeries$1> is not abstract and does not override abstract method addPrice(Num) in Bar
        private final Bar bar = new Bar() {
                                          ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:239: error: getTrades() in <anonymous org.ta4j.core.indicators.numeric.NumericIndicator_closePrice_Test$BarWithNullCloseSeries$1> cannot implement getTrades() in Bar
            public int getTrades() {
                       ^
  return type int is not compatible with long
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:249: error: getEndTime() in <anonymous org.ta4j.core.indicators.numeric.NumericIndicator_closePrice_Test$BarWithNullCloseSeries$1> cannot implement getEndTime() in Bar
            public long getEndTime() {
                        ^
  return type long is not compatible with ZonedDateTime
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:238: error: method does not override or implement a method from a supertype
            @Override
            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:243: error: method does not override or implement a method from a supertype
            @Override
            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:248: error: method does not override or implement a method from a supertype
            @Override
            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:253: error: method does not override or implement a method from a supertype
            @Override
            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:274: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:279: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:284: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:294: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:303: error: FaultySeries is not abstract and does not override abstract method getSubSeries(int,int) in BarSeries
    private static final class FaultySeries implements BarSeries {
                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:320: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:325: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:330: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:340: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:393: error: cannot find symbol
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
                               ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:394: error: cannot find symbol
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:395: error: cannot find symbol
        Mockito.when(mockSeries.getBar(0)).thenReturn(null);
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:400: error: cannot find symbol
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
                               ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:401: error: cannot find symbol
        Bar mockBar = Mockito.mock(Bar.class);
                      ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:402: error: cannot find symbol
        Mockito.when(mockBar.getClosePrice()).thenReturn(null);
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:403: error: cannot find symbol
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:404: error: cannot find symbol
        Mockito.when(mockSeries.getBar(0)).thenReturn(mockBar);
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:409: error: cannot find symbol
        BarSeries mockSeries = Mockito.mock(BarSeries.class);
                               ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:410: error: cannot find symbol
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:411: error: cannot find symbol
        Mockito.when(mockSeries.getBar(Mockito.anyInt())).thenThrow(new RuntimeException("Faulty series access"));
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:411: error: cannot find symbol
        Mockito.when(mockSeries.getBar(Mockito.anyInt())).thenThrow(new RuntimeException("Faulty series access"));
                                       ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:491: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:492: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(30.5), ni.getValue(2));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:493: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(50.0), ni.getValue(4));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:499: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(Double.MAX_VALUE), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:500: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(Double.MIN_VALUE), ni.getValue(1));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:501: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(0.0), ni.getValue(2));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:502: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(-12345.678), ni.getValue(3));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:509: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
            assertEquals(DecimalNum.valueOf(42.42), ni.getValue(i));
            ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:542: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:561: error: incompatible types: void cannot be converted to RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ni.getValue(0), "faulty series should propagate exception");
                                          ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:562: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals("Faulty series access", ex.getMessage());
        ^
  required: Object,Object,String
  found:    String,String
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:613: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(100.5), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:614: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(101.2), ni.getValue(1));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:621: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(55.0), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:628: error: method assertThrows in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertThrows(IndexOutOfBoundsException.class, () -> ni.getValue(0));
        ^
  required: Class<? extends Throwable>,Runnable,String
  found:    Class<IndexOutOfBoundsException>,()->ni.getValue(0)
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:633: error: method assertThrows in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertThrows(NullPointerException.class, () -> NumericIndicator.closePrice(null));
        ^
  required: Class<? extends Throwable>,Runnable,String
  found:    Class<NullPointerException>,()->Numeri[...]null)
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:640: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:641: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(30.5), ni.getValue(2));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:642: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(50.0), ni.getValue(4));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:649: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(Double.MAX_VALUE), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:650: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(Double.MIN_VALUE), ni.getValue(1));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:651: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(0.0), ni.getValue(2));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:652: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(-12345.678), ni.getValue(3));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:660: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
            assertEquals(DecimalNum.valueOf(42.42), ni.getValue(i));
            ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:670: error: cannot find symbol
        Bar newBar = Mockito.mock(Bar.class);
                     ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:671: error: cannot find symbol
        Mockito.when(newBar.getClosePrice()).thenReturn(DecimalNum.valueOf(200.0));
        ^
  symbol:   variable Mockito
  location: class NumericIndicator_closePrice_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:701: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals(DecimalNum.valueOf(10.0), ni.getValue(0));
        ^
  required: Object,Object,String
  found:    DecimalNum,Num
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:710: error: method assertThrows in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
        ^
  required: Class<? extends Throwable>,Runnable,String
  found:    Class<NullPointerException>,()->ni.getValue(0)
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:717: error: method assertThrows in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertThrows(NullPointerException.class, () -> ni.getValue(0));
        ^
  required: Class<? extends Throwable>,Runnable,String
  found:    Class<NullPointerException>,()->ni.getValue(0)
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:724: error: method assertThrows in class NumericIndicator_closePrice_Test cannot be applied to given types;
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ni.getValue(0));
                              ^
  required: Class<? extends Throwable>,Runnable,String
  found:    Class<RuntimeException>,()->ni.getValue(0)
  reason: actual and formal argument lists differ in length
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_closePrice_Test.java:725: error: method assertEquals in class NumericIndicator_closePrice_Test cannot be applied to given types;
        assertEquals("Faulty series access", ex.getMessage());
        ^
  required: Object,Object,String
  found:    String,String
  reason: actual and formal argument lists differ in length
96 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.indicators.numeric.NumericIndicator': '\n * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides\n * methods to create rules and other "lightweight" indicators, using a\n * (hopefully) natural-looking and expressive series of method calls.\n * \n * <p>\n * Methods like plus(), minus() and sqrt() correspond directly to methods in the\n * {@code Num} interface. These methods create "lightweight" (not cached)\n * indicators to add, subtract, etc. Many methods are overloaded to accept\n * either {@code Indicator<Num>} or {@code Number} arguments.\n * \n * <p>\n * Methods like sma() and ema() simply create the corresponding indicator\n * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first\n * argument. These methods usually instantiate cached objects.\n * \n * <p>\n * Another set of methods, like crossedOver() and isGreaterThan() create Rule\n * objects. These are also overloaded to accept both {@code Indicator<Num>} and\n * {@code Number} arguments.\n ', 'api document of method closePrice(BarSeries)': '\n     * Creates a fluent version of the ClosePriceIndicator.\n     *\n     * @return a NumericIndicator wrapped around a ClosePriceIndicator\n     ', 'return type': 'org.ta4j.core.indicators.numeric.NumericIndicator', 'dependent classes': 'class `org.ta4j.core.BarSeries`:\n\t\nclass `org.ta4j.core.indicators.numeric.NumericIndicator`:\n\trelated functions:\n\t\tmethod `public static NumericIndicator closePrice(BarSeries)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.closePrice(BarSeries)`, api document: \n     * Creates a fluent version of the ClosePriceIndicator.\n     *\n     * @return a NumericIndicator wrapped around a ClosePriceIndicator\n     ;\n\t\tmethod `public static NumericIndicator volume(BarSeries)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.closePrice(BarSeries)`, api document: \n     * Creates a fluent version of the VolumeIndicator.\n     *\n     * @return a NumericIndicator wrapped around a VolumeIndicator\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


