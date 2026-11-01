```java
package org.ta4j.core.indicators.numeric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.Num;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link NumericIndicator#getUnstableBars()}.
 *
 * <p>The original version of this test class was package‑private and used
 * JUnit‑5 annotations together with JUnit‑4 imports, which resulted in the test
 * engine not discovering any tests (0 tests run).  The class has been made
 * public, the annotations have been aligned to JUnit‑4, and Mockito is used
 * to create the required mock objects.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class NumericIndicator_getUnstableBars_Test {

    /** A generic delegate mock – its behaviour is irrelevant for the tests. */
    @Mock
    private Indicator<Num> mockDelegate;

    /** A mock {@link BarSeries} required by the factory methods. */
    @Mock
    private BarSeries mockSeries;

    /**
     * A minimal {@link Indicator} implementation used as a concrete delegate
     * in one of the tests.  It returns {@code 0} for {@link #getUnstableBars()}
     * and {@code null} for the other methods because they are never invoked.
     */
    private static class DummyIndicator implements Indicator<Num> {

        @Override
        public Num getValue(int index) {
            return null; // not used in these tests
        }

        @Override
        public BarSeries getBarSeries() {
            return null; // not used in these tests
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    @Before
    public void setUp() {
        // Mockito automatically creates the mocks annotated with @Mock,
        // so no additional initialisation is required here.
    }

    @Test
    public void getUnstableBars_returnsZero_forDelegateBasedInstance() {
        NumericIndicator numeric = NumericIndicator.of(mockDelegate);
        assertEquals(
                "NumericIndicator created from a generic delegate should report 0 unstable bars",
                0,
                numeric.getUnstableBars()
        );
    }

    @Test
    public void getUnstableBars_returnsZero_forClosePriceFactory() {
        NumericIndicator numeric = NumericIndicator.closePrice(mockSeries);
        assertEquals(
                "NumericIndicator created via closePrice() should report 0 unstable bars",
                0,
                numeric.getUnstableBars()
        );
    }

    @Test
    public void getUnstableBars_returnsZero_forVolumeFactory() {
        NumericIndicator numeric = NumericIndicator.volume(mockSeries);
        assertEquals(
                "NumericIndicator created via volume() should report 0 unstable bars",
                0,
                numeric.getUnstableBars()
        );
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedClosePriceIndicator() {
        ClosePriceIndicator cp = new ClosePriceIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(cp);
        assertEquals(
                "NumericIndicator wrapping a ClosePriceIndicator should report 0 unstable bars",
                0,
                numeric.getUnstableBars()
        );
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedVolumeIndicator() {
        VolumeIndicator vi = new VolumeIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(vi);
        assertEquals(
                "NumericIndicator wrapping a VolumeIndicator should report 0 unstable bars",
                0,
                numeric.getUnstableBars()
        );
    }

    @Test
    public void getUnstableBars_returnsZero_forWrappedDummyIndicator() {
        DummyIndicator dummy = new DummyIndicator();
        NumericIndicator numeric = NumericIndicator.of(dummy);
        assertEquals(
                "NumericIndicator wrapping a DummyIndicator should report 0 unstable bars",
                0,
                numeric.getUnstableBars()
        );
    }
}
```