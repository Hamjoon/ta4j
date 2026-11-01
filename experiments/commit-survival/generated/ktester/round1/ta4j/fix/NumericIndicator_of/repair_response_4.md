```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.junit.*;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link NumericIndicator#of(Indicator)}.
 *
 * <p>The original version of this test class mixed JUnit 4 and JUnit 5
 * annotations, used a non‑public class, and relied on a Mockito mock that was
 * never initialised. As a consequence no tests were discovered or executed.
 *
 * <p>This rewritten version uses pure JUnit 4, makes the class public, adds the
 * missing annotations, and replaces the unused mock with a simple stub
 * implementation that is sufficient for the tests.
 */
public class NumericIndicator_of_Test {

    /** Simple stub {@link Indicator} used for delegation tests. */
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

    /** Series used by the concrete‑indicator test. */
    private static BarSeries series;

    /** The indicator that will be wrapped by {@link NumericIndicator}. */
    private Indicator<Num> delegateIndicator;

    /** The {@link NumericIndicator} created by the factory method under test. */
    private NumericIndicator numericIndicator;

    /** Builds a tiny series – the actual bar values are irrelevant for the delegation tests. */
    @BeforeClass
    public static void setupBeforeAll() {
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // add a few dummy bars (the values are not important)
        for (int i = 1; i <= 5; i++) {
            series.addBar(new org.ta4j.core.BaseBar(
                    Duration.ofDays(1),
                    ZonedDateTime.now().plusDays(i),
                    DecimalNum.valueOf(i), // open
                    DecimalNum.valueOf(i), // high
                    DecimalNum.valueOf(i), // low
                    DecimalNum.valueOf(i), // close
                    DecimalNum.valueOf(i), // volume
                    DecimalNum.valueOf(i), // amount
                    DecimalNum.valueOf(0)  // trades
            ));
        }
    }

    /** Initialise a fresh delegate and wrapper before each test. */
    @Before
    public void setupBeforeEach() {
        // a trivial delegate that always returns 1
        delegateIndicator = new StubIndicator(DecimalNum.valueOf(1), series, 0);
        numericIndicator = NumericIndicator.of(delegateIndicator);
    }

    @After
    public void teardownAfterEach() {
        // nothing to verify – we are not using a mock framework
    }

    /** Simple sanity check that the factory never returns {@code null}. */
    @Test
    public void ofCreatesNumericIndicatorWithCorrectDelegate() {
        assertNotNull("NumericIndicator.of should not return null", numericIndicator);
        assertSame("The delegate returned by NumericIndicator must be the original Indicator",
                delegateIndicator, numericIndicator.delegate());
    }

    /** {@code getValue(int)} must be delegated to the wrapped indicator. */
    @Test
    public void getValueIsDelegatedToUnderlyingIndicator() {
        Num expected = DecimalNum.valueOf(42);
        Indicator<Num> stub = new StubIndicator(expected, series, 0);
        NumericIndicator ni = NumericIndicator.of(stub);

        int index = 7; // arbitrary – the stub ignores it
        Num actual = ni.getValue(index);
        assertEquals("NumericIndicator#getValue must delegate to the underlying Indicator",
                expected, actual);
    }

    /** {@code getBarSeries()} must be delegated to the wrapped indicator. */
    @Test
    public void getBarSeriesIsDelegatedToUnderlyingIndicator() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), series, 0);
        NumericIndicator ni = NumericIndicator.of(stub);
        BarSeries actualSeries = ni.getBarSeries();
        assertSame("getBarSeries must be delegated to the underlying indicator",
                series, actualSeries);
    }

    /** {@code getUnstableBars()} must be delegated to the wrapped indicator. */
    @Test
    public void getUnstableBarsIsDelegatedToUnderlyingIndicator() {
        int expectedUnstable = 3;
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), series, expectedUnstable);
        NumericIndicator ni = NumericIndicator.of(stub);
        int actualUnstable = ni.getUnstableBars();
        assertEquals("getUnstableBars must be delegated to the underlying indicator",
                expectedUnstable, actualUnstable);
    }

    /** The factory must also work with a real indicator implementation. */
    @Test
    public void ofWorksWithARealIndicatorImplementation() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        NumericIndicator fromClose = NumericIndicator.of(closePrice);

        assertSame("Delegate should be the original ClosePriceIndicator",
                closePrice, fromClose.delegate());

        // spot‑check a few values to confirm correct delegation
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("Value delegation must be correct for index " + i,
                    closePrice.getValue(i), fromClose.getValue(i));
        }
    }

    /** {@code NumericIndicator.of(null)} must fail fast with a {@link NullPointerException}. */
    @Test(expected = NullPointerException.class)
    public void ofRejectsNullDelegate() {
        NumericIndicator.of(null);
    }

    /** Allows running the test class from the command line. */
    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main(NumericIndicator_of_Test.class.getName());
    }
}
```