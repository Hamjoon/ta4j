```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.runner.RunWith;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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

    @BeforeClass
    public static void setupBeforeAll() {
        // Deterministic Num implementation (DecimalNum) for reproducible values
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new BaseBarSeries("test-series", numFunction);

        // add 5 bars with close prices 1..5
        ZonedDateTime start = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (int i = 1; i <= 5; i++) {
            Num price = DecimalNum.valueOf(i);
            BaseBar bar = new BaseBar(barDuration, start.plusMinutes(i - 1), price, price, price, price, price);
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
    public void delegateWorksWithMockedIndicator() {
        // Use Mockito to create a dummy Indicator<Num>
        @SuppressWarnings("unchecked")
        Indicator<Num> mockIndicator = mock(Indicator.class);
        // The mock does not need any behaviour for this test; we only care about identity
        NumericIndicator numericMock = NumericIndicator.of(mockIndicator);
        // Verify that delegate returns the exact mock instance
        assertSame(mockIndicator, numericMock.delegate());
    }

    /**
     * Verify that a {@link NumericIndicator} created from a mock that always
     * returns the constant value {@code 10} returns that very mock instance
     * via {@code delegate()}.
     */
    @Test
    public void delegateReturnsConstantMockIndicator() {
        // Create a mock Indicator that returns DecimalNum.valueOf(10) for any index
        @SuppressWarnings("unchecked")
        Indicator<Num> constantMock = mock(Indicator.class);
        when(constantMock.getValue(anyInt())).thenReturn(DecimalNum.valueOf(10));
        NumericIndicator numeric = NumericIndicator.of(constantMock);
        assertSame(constantMock, numeric.delegate());
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