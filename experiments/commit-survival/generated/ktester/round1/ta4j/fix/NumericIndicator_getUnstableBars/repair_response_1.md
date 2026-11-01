```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#getUnstableBars()}.
 * <p>
 * The tests do not rely on any external mocking framework; simple stub
 * implementations are used because the method under test never touches the
 * underlying {@link Indicator} or {@link BarSeries} instances.
 */
public class NumericIndicator_getUnstableBars_Test {

    /** A minimal {@link Indicator} implementation used as a delegate. */
    private static class DummyIndicator implements Indicator<Num> {
        @Override
        public Num getValue(int index) {
            return null; // value is never used in these tests
        }

        @Override
        public BarSeries getBarSeries() {
            return null; // not needed for getUnstableBars()
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public String getName() {
            return "dummy";
        }
    }

    private Indicator<Num> mockDelegate;
    private BarSeries mockSeries; // not used, can stay null

    @Before
    public void setUp() {
        mockDelegate = new DummyIndicator();
        mockSeries = null; // ClosePriceIndicator / VolumeIndicator only store the reference
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