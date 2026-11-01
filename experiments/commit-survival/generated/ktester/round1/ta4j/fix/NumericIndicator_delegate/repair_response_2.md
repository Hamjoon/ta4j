```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link NumericIndicator} focusing on the {@code delegate()}
 * method.
 */
public class NumericIndicator_delegate_Test {

    /**
     * Shared series used by all tests
     */
    private static BarSeries series;

    /**
     * A simple ClosePriceIndicator based on the shared series
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * NumericIndicator wrapper for {@link #closePriceIndicator}
     */
    private NumericIndicator numericIndicator;

    /**
     * Simple constant indicator used instead of Mockito.
     */
    private static class ConstantIndicator implements Indicator<Num> {

        private final Num constant;

        ConstantIndicator(Num constant) {
            this.constant = constant;
        }

        @Override
        public Num getValue(int index) {
            return constant;
        }

        @Override
        public BarSeries getBarSeries() {
            return null;
        }

        @Override
        public int getUnstableBars() {
            // No unstable bars for this simple constant indicator
            return 0;
        }

        @Override
        public String toString() {
            return "ConstantIndicator(" + constant + ")";
        }
    }

    @BeforeClass
    public static void setupBeforeAll() {
        // Deterministic Num implementation (DecimalNum) for reproducible values
        series = new BaseBarSeries("test-series");
        // add 5 bars with close prices 1..5
        ZonedDateTime start = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (int i = 1; i <= 5; i++) {
            Num price = DecimalNum.valueOf(i);
            BaseBar bar = new BaseBar(barDuration,
                    start.plusMinutes(i - 1), // time
                    price.doubleValue(), // open
                    price.doubleValue(), // high
                    price.doubleValue(), // low
                    price.doubleValue(), // close
                    price.doubleValue(), // volume
                    price.doubleValue()  // amount (not used)
            );
            series.addBar(bar);
        }
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @AfterClass
    public static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
    }

    @Before
    public void setupBeforeEach() {
        // Wrap the close‑price indicator before each test
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @Test
    public void delegateReturnsOriginalIndicator() {
        // The delegate of the wrapper must be exactly the indicator we passed in.
        Indicator<Num> delegate = numericIndicator.delegate();
        assertSame(closePriceIndicator, delegate);
    }

    @Test
    public void delegateWorksWithEmaIndicator() {
        // Create a cached EMA indicator based on the same close‑price indicator
        EMAIndicator ema = new EMAIndicator(closePriceIndicator, 3);
        NumericIndicator numericEma = NumericIndicator.of(ema);
        // The delegate must be the EMA indicator itself
        assertSame(ema, numericEma.delegate());
    }

    @Test
    public void delegateWorksWithConstantIndicator() {
        // Use a simple constant indicator instead of a Mockito mock
        ConstantIndicator constant = new ConstantIndicator(DecimalNum.valueOf(10));
        NumericIndicator numericConst = NumericIndicator.of(constant);
        // Verify that delegate returns the exact instance we supplied
        assertSame(constant, numericConst.delegate());
    }

    /**
     * Verify that a {@link NumericIndicator} created from a constant indicator
     * returns that very instance via {@code delegate()}.
     */
    @Test
    public void delegateReturnsConstantIndicator() {
        ConstantIndicator constant = new ConstantIndicator(DecimalNum.valueOf(10));
        NumericIndicator numeric = NumericIndicator.of(constant);
        assertSame(constant, numeric.delegate());
    }

    /**
     * When a {@link NumericIndicator} is created via the static
     * {@code closePrice(BarSeries)} factory, its delegate must be a
     * {@link ClosePriceIndicator} that wraps the same {@link BarSeries}.
     */
    @Test
    public void delegateFromClosePriceFactoryIsClosePriceIndicator() {
        NumericIndicator fromFactory = NumericIndicator.closePrice(series);
        Indicator<Num> delegate = fromFactory.delegate();
        assertTrue(delegate instanceof ClosePriceIndicator);
        // Cast is safe because of the previous assertion
        ClosePriceIndicator cpi = (ClosePriceIndicator) delegate;
        assertSame(series, cpi.getBarSeries());
    }

    /**
     * When a {@link NumericIndicator} is created via the static
     * {@code volume(BarSeries)} factory, its delegate must be a
     * {@link VolumeIndicator} that wraps the same {@link BarSeries}.
     */
    @Test
    public void delegateFromVolumeFactoryIsVolumeIndicator() {
        NumericIndicator fromFactory = NumericIndicator.volume(series);
        Indicator<Num> delegate = fromFactory.delegate();
        assertTrue(delegate instanceof VolumeIndicator);
        VolumeIndicator vi = (VolumeIndicator) delegate;
        assertSame(series, vi.getBarSeries());
    }

    /**
     * Edge‑case: passing {@code null} to {@code NumericIndicator.of(...)}.
     * The library currently validates the argument and throws a
     * {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void delegateWithNullArgumentThrowsNPE() {
        NumericIndicator.of(null);
    }
}
```