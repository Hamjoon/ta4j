```java
package org.ta4j.core.indicators.numeric;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import static org.junit.Assert.*;

public class NumericIndicatorDelegateTest {

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

    @BeforeClass
    public static void setupBeforeAll() {
        // Deterministic Num implementation (DecimalNum) for reproducible values
        // BaseBarSeries with default Num type (DecimalNum) is sufficient
        series = new BaseBarSeries("test-series");
        // add 5 bars with close prices 1..5
        ZonedDateTime start = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (int i = 1; i <= 5; i++) {
            Num price = DecimalNum.valueOf(i);
            BaseBar bar = new BaseBar(
                    barDuration,
                    start.plusMinutes(i - 1),
                    price.doubleValue(),   // open
                    price.doubleValue(),   // high
                    price.doubleValue(),   // low
                    price.doubleValue(),   // close
                    price.doubleValue()    // volume
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
        public String toString() {
            return "ConstantIndicator(" + constant + ")";
        }
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
        NumericIndicator numericMock = NumericIndicator.of(constant);
        // Verify that delegate returns the exact instance we supplied
        assertSame(constant, numericMock.delegate());
    }

    @Test
    public void delegateFromClosePriceFactoryIsClosePriceIndicator() {
        NumericIndicator fromFactory = NumericIndicator.closePrice(series);
        Indicator<Num> delegate = fromFactory.delegate();
        assertTrue(delegate instanceof ClosePriceIndicator);
        // Cast is safe because of the previous assertion
        ClosePriceIndicator cpi = (ClosePriceIndicator) delegate;
        assertSame(series, cpi.getBarSeries());
    }

    @Test
    public void delegateFromVolumeFactoryIsVolumeIndicator() {
        NumericIndicator fromFactory = NumericIndicator.volume(series);
        Indicator<Num> delegate = fromFactory.delegate();
        assertTrue(delegate instanceof VolumeIndicator);
        VolumeIndicator vi = (VolumeIndicator) delegate;
        assertSame(series, vi.getBarSeries());
    }

    @Test(expected = NullPointerException.class)
    public void delegateWithNullArgumentThrowsNPE() {
        NumericIndicator.of(null);
    }
}
```