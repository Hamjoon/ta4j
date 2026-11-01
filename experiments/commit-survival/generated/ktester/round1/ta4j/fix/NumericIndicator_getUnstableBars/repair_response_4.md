```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#getUnstableBars()}.
 *
 * <p>The tests verify that {@code NumericIndicator} reports {@code 0}
 * unstable bars for the various factory methods and when wrapping different
 * {@link Indicator} implementations.</p>
 *
 * <p>All tests are written with JUnit 5 (JUnit Jupiter). The class and test
 * methods are {@code public} so that the test engine can discover them.</p>
 */
public class NumericIndicator_getUnstableBars_Test {

    private BarSeries mockSeries;   // a simple (null) series – not used by the tests

    /**
     * Minimal {@link Indicator} implementation used as a concrete delegate
     * in one of the tests. It returns {@code 0} for {@link #getUnstableBars()}
     * and {@code null} for the other methods because they are never invoked.
     */
    private static class DummyIndicator implements Indicator<Num> {

        @Override
        public Num getValue(int index) {
            // Not used in these tests
            return null;
        }

        @Override
        public BarSeries getBarSeries() {
            // Not used in these tests
            return null;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    @BeforeEach
    public void setUp() {
        // No real mocking framework is required – the series is never accessed
        // by the code under test (only stored). Using {@code null} is sufficient.
        mockSeries = null;
    }

    @Test
    public void getUnstableBars_returnsZero_forClosePriceFactory() {
        NumericIndicator numeric = NumericIndicator.closePrice(mockSeries);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator created via closePrice() should report 0 unstable bars");
    }

    @Test
    public void getUnstableBars_returnsZero_forVolumeFactory() {
        NumericIndicator numeric = NumericIndicator.volume(mockSeries);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator created via volume() should report 0 unstable bars");
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedClosePriceIndicator() {
        ClosePriceIndicator cp = new ClosePriceIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(cp);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator wrapping a ClosePriceIndicator should report 0 unstable bars");
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedVolumeIndicator() {
        VolumeIndicator vi = new VolumeIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(vi);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator wrapping a VolumeIndicator should report 0 unstable bars");
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedDummyIndicator() {
        DummyIndicator dummy = new DummyIndicator();
        NumericIndicator numeric = NumericIndicator.of(dummy);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator wrapping a DummyIndicator should report 0 unstable bars");
    }
}
```