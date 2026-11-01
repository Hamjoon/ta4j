```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.junit.*;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link NumericIndicator#of(Indicator)}.
 *
 * <p>These tests avoid external libraries such as Mockito and JUnit‑Jupiter
 * to keep the compilation environment minimal.</p>
 */
public class NumericIndicator_of_Test {

    /**
     * A very small {@link BarSeries} used for the concrete‑indicator test.
     */
    private static BarSeries series;

    /**
     * Simple stub {@link Indicator} used for delegation tests.
     */
    private static final class StubIndicator implements Indicator<Num> {

        private final Num fixedValue;
        private final BarSeries barSeries;
        private final int unstableBars;

        StubIndicator(Num fixedValue, BarSeries barSeries, int unstableBars) {
            this.fixedValue = fixedValue;
            this.barSeries = barSeries;
            this.unstableBars = unstableBars;
        }

        @Override
        public Num getValue(int index) {
            // ignore the index – always return the fixed value
            return fixedValue;
        }

        @Override
        public BarSeries getBarSeries() {
            return barSeries;
        }

        @Override
        public int getUnstableBars() {
            return unstableBars;
        }

        @Override
        public String toString() {
            return "StubIndicator";
        }
    }

    @BeforeClass
    public static void setupBeforeAll() {
        // Build a tiny series – the actual bar values are irrelevant for the
        // delegation tests, they just need to be non‑null.
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Add a few dummy bars (using the public BaseBar constructor)
        for (int i = 1; i <= 5; i++) {
            series.addBar(
                    new org.ta4j.core.BaseBar(
                            ZonedDateTime.now().plusDays(i),
                            DecimalNum.valueOf(i),   // open
                            DecimalNum.valueOf(i),   // high
                            DecimalNum.valueOf(i),   // low
                            DecimalNum.valueOf(i),   // close
                            DecimalNum.valueOf(0)    // volume
                    )
            );
        }
    }

    /**
     * Verifies that {@code NumericIndicator.of} returns a non‑null wrapper
     * whose {@code delegate()} method returns the original indicator.
     */
    @Test
    public void testOfCreatesNumericIndicatorWithCorrectDelegate() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), null, 0);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        assertNotNull("NumericIndicator.of should not return null", numericIndicator);
        assertSame("The delegate returned by NumericIndicator must be the original Indicator",
                stub, numericIndicator.delegate());
    }

    /**
     * Checks that {@code getValue(int)} is correctly delegated to the wrapped
     * indicator.
     */
    @Test
    public void testGetValueIsDelegatedToUnderlyingIndicator() {
        Num expected = DecimalNum.valueOf(42);
        Indicator<Num> stub = new StubIndicator(expected, null, 0);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        int index = 7; // arbitrary – stub ignores it
        Num actual = numericIndicator.getValue(index);
        assertEquals("NumericIndicator#getValue must delegate to the underlying Indicator",
                expected, actual);
    }

    /**
     * {@code NumericIndicator.of(null)} must throw a {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void testOfRejectsNullDelegate() {
        NumericIndicator.of(null);
    }

    @Test
    public void getBarSeriesIsDelegatedToUnderlyingIndicator() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), series, 0);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        BarSeries actualSeries = numericIndicator.getBarSeries();
        assertSame("getBarSeries must be delegated to the underlying indicator", series, actualSeries);
    }

    @Test
    public void getUnstableBarsIsDelegatedToUnderlyingIndicator() {
        int expectedUnstable = 3;
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), null, expectedUnstable);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        int actualUnstable = numericIndicator.getUnstableBars();
        assertEquals("getUnstableBars must be delegated to the underlying indicator",
                expectedUnstable, actualUnstable);
    }

    @Test
    public void ofWorksWithARealIndicatorImplementation() {
        // Use a concrete indicator (ClosePriceIndicator) to ensure the factory
        // works with non‑mock objects as well.
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        NumericIndicator fromClose = NumericIndicator.of(closePrice);
        // The delegate should be exactly the ClosePriceIndicator we passed.
        assertSame("Delegate should be the original ClosePriceIndicator", closePrice, fromClose.delegate());
        // Spot‑check a few values to confirm correct delegation.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("Value delegation must be correct for index " + i,
                    closePrice.getValue(i), fromClose.getValue(i));
        }
    }

    /**
     * Simple {@code main} method to run the tests without a test runner.
     * Useful when executing the class directly.
     */
    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main(NumericIndicator_of_Test.class.getName());
    }
}
```