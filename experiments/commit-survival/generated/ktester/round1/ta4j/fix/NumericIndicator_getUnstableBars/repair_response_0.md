```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#getUnstableBars()}.
 *
 * <p>The tests use JUnit 4 and Mockito (both available in the project’s test
 * classpath). They verify that a {@code NumericIndicator} created through any
 * of its factory methods or by wrapping other indicators always reports zero
 * unstable bars.</p>
 */
public class NumericIndicator_getUnstableBars_Test {

    /** Mocked delegate indicator – generic type must be {@code Num}. */
    private Indicator<Num> mockDelegate;

    /** Mocked series – required by the concrete helpers. */
    private BarSeries mockSeries;

    @Before
    public void setUp() {
        // Mockito creates simple mocks; the concrete behavior is irrelevant
        // because getUnstableBars() never touches the delegate.
        mockDelegate = (Indicator<Num>) mock(Indicator.class);
        mockSeries   = mock(BarSeries.class);
    }

    @Test
    public void getUnstableBars_returnsZero_forDelegateBasedInstance() {
        NumericIndicator numeric = NumericIndicator.of(mockDelegate);
        assertEquals("NumericIndicator created from a generic delegate should report 0 unstable bars",
                     0, numeric.getUnstableBars());
    }

    @Test
    public void getUnstableBars_returnsZero_forClosePriceFactory() {
        NumericIndicator numeric = NumericIndicator.closePrice(mockSeries);
        assertEquals("NumericIndicator created via closePrice() should report 0 unstable bars",
                     0, numeric.getUnstableBars());
    }

    @Test
    public void getUnstableBars_returnsZero_forVolumeFactory() {
        NumericIndicator numeric = NumericIndicator.volume(mockSeries);
        assertEquals("NumericIndicator created via volume() should report 0 unstable bars",
                     0, numeric.getUnstableBars());
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedClosePriceIndicator() {
        ClosePriceIndicator cp = new ClosePriceIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(cp);
        assertEquals("NumericIndicator wrapping a ClosePriceIndicator should report 0 unstable bars",
                     0, numeric.getUnstableBars());
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedVolumeIndicator() {
        VolumeIndicator vi = new VolumeIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(vi);
        assertEquals("NumericIndicator wrapping a VolumeIndicator should report 0 unstable bars",
                     0, numeric.getUnstableBars());
    }

    @Test
    public void getUnstableBars_returnsZero_forChainedOperations() {
        // Chain a few fluent methods (SMA followed by absolute‑value) and verify the contract.
        NumericIndicator numeric = NumericIndicator.of(mockDelegate)
                                                   .sma(5)   // creates an SMAIndicator based on the delegate
                                                   .abs();   // wraps the result in an absolute‑value indicator
        assertEquals("NumericIndicator after chaining operations should still report 0 unstable bars",
                     0, numeric.getUnstableBars());
    }
}
```