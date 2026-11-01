package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.junit.*;
import java.time.ZonedDateTime;
import static org.junit.Assert.*;
import java.time.Duration;

// import org.mockito.junit.MockitoJUnitRunner;
// import static org.mockito.Mockito.*;
// @ExtendWith(MockitoExtension.class)
class NumericIndicator_of_Test {

    //     @Mock
    private Indicator<Num> mockIndicator;

    /**
     * The NumericIndicator created by the factory method under test.
     */
    private NumericIndicator numericIndicator;

    /**
     * A simple BarSeries used for the concrete‑indicator test.
     */
    private static BarSeries series;

    /**
     * The indicator that will be wrapped by {@link NumericIndicator}.
     */
    private Indicator<Num> delegateIndicator;

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

    //     @BeforeAll
//     static void setupBeforeAll() {
//         series = new org.ta4j.core.BaseBarSeriesBuilder().withName("test-series").build();
//         // add a few dummy bars (the values are not important)
//         for (int i = 1; i <= 5; i++) {
//             series.addBar(new org.ta4j.core.BaseBar(Duration.ofDays(1), ZonedDateTime.now().plusDays(i), // open
//             DecimalNum.valueOf(i), // high
//             DecimalNum.valueOf(i), // low
//             DecimalNum.valueOf(i), // close
//             DecimalNum.valueOf(i), // volume
//             DecimalNum.valueOf(i), // amount
//             DecimalNum.valueOf(i), // trades
//             DecimalNum.valueOf(0)));
//         }
//     }

    //     @BeforeEach
    void setupBeforeEach() {
        // a trivial delegate that always returns 1
        delegateIndicator = new StubIndicator(DecimalNum.valueOf(1), series, 0);
        numericIndicator = NumericIndicator.of(delegateIndicator);
    }

    //     @AfterEach
    void teardownAfterEach() {
        // nothing to verify – we are not using a mock framework
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main(NumericIndicator_of_Test.class.getName());
    }

    /**
     * Verifies that {@code NumericIndicator.of} returns a non‑null wrapper
     * whose {@code delegate()} method returns the original indicator.
     */
    private static void testOfCreatesNumericIndicatorWithCorrectDelegate() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), null, 0);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        assertNotNull("NumericIndicator.of should not return null", numericIndicator);
        assertSame("The delegate returned by NumericIndicator must be the original Indicator", stub, numericIndicator.delegate());
    }

    /**
     * Checks that {@code getValue(int)} is correctly delegated to the wrapped
     * indicator.
     */
    private static void testGetValueIsDelegatedToUnderlyingIndicator() {
        Num expected = DecimalNum.valueOf(42);
        Indicator<Num> stub = new StubIndicator(expected, null, 0);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        // arbitrary – stub ignores it
        int index = 7;
        Num actual = numericIndicator.getValue(index);
        assertEquals("NumericIndicator#getValue must delegate to the underlying Indicator", expected, actual);
    }

    /**
     * {@code NumericIndicator.of(null)} must throw a {@link NullPointerException}.
     */
    private static void testOfRejectsNullDelegate() {
        NumericIndicator.of(null);
    }

    @Test
    void ofCreatesNumericIndicatorWithCorrectDelegate() {
        assertNotNull("NumericIndicator.of should not return null", numericIndicator);
        assertSame("The delegate returned by NumericIndicator must be the original Indicator", delegateIndicator, numericIndicator.delegate());
    }

    @Test
    void getBarSeriesIsDelegatedToUnderlyingIndicator() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), series, 0);
        NumericIndicator ni = NumericIndicator.of(stub);
        BarSeries actualSeries = ni.getBarSeries();
        assertSame("getBarSeries must be delegated to the underlying indicator", series, actualSeries);
    }

    @Test
    void getUnstableBarsIsDelegatedToUnderlyingIndicator() {
        int expectedUnstable = 3;
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1), series, expectedUnstable);
        NumericIndicator ni = NumericIndicator.of(stub);
        int actualUnstable = ni.getUnstableBars();
        assertEquals("getUnstableBars must be delegated to the underlying indicator", expectedUnstable, actualUnstable);
    }

    @Test
    void ofWorksWithARealIndicatorImplementation() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        NumericIndicator fromClose = NumericIndicator.of(closePrice);
        assertSame("Delegate should be the original ClosePriceIndicator", closePrice, fromClose.delegate());
        // spot‑check a few values to confirm correct delegation
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("Value delegation must be correct for index " + i, closePrice.getValue(i), fromClose.getValue(i));
        }
    }

    /**
     * {@code NumericIndicator.of(null)} must fail fast with a {@link NullPointerException}
     * because the underlying {@code NumericIndicator} constructor requires a non‑null delegate.
     */
    @Test
    void testOf_NullDelegate() {
        NumericIndicator.of(null);
    }

    /**
     * {@code getValue(int)} must be delegated to the wrapped indicator.
     */
    @Test
    public void getValueIsDelegatedToUnderlyingIndicator() {
        Num expected = DecimalNum.valueOf(42);
        Indicator<Num> stub = new StubIndicator(expected, series, 0);
        NumericIndicator ni = NumericIndicator.of(stub);
        // arbitrary – the stub ignores it
        int index = 7;
        Num actual = ni.getValue(index);
        assertEquals("NumericIndicator#getValue must delegate to the underlying Indicator", expected, actual);
    }

    /**
     * {@code NumericIndicator.of(null)} must fail fast with a {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void ofRejectsNullDelegate() {
        NumericIndicator.of(null);
    }
}