package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.Num;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.assertEquals;

// import org.mockito.Mock;
// import org.mockito.junit.MockitoJUnitRunner;
// import static org.mockito.Mockito.mock;
class NumericIndicator_getUnstableBars_Test {

    private Indicator<?> mockDelegate;

    private BarSeries mockSeries;

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

    //     @BeforeEach
    void setupBeforeEach() {
        // Create simple Mockito mocks for the delegate and series.
        // The concrete behavior of the delegate is irrelevant because
        // getUnstableBars() does not use it.
        //         mockDelegate = mock(Indicator.class);
        //         mockSeries = mock(BarSeries.class);
    }

//     @Test
//     void getUnstableBars_returnsZero_forClosePriceFactory() {
//         NumericIndicator numeric = NumericIndicator.closePrice(mockSeries);
//         assertEquals(0, numeric.getUnstableBars(), "NumericIndicator created via closePrice() should report 0 unstable bars");
//     }

//     @Test
//     void getUnstableBars_returnsZero_forVolumeFactory() {
//         NumericIndicator numeric = NumericIndicator.volume(mockSeries);
//         assertEquals(0, numeric.getUnstableBars(), "NumericIndicator created via volume() should report 0 unstable bars");
//     }

//     @Test
//     void getUnstableBars_returnsZero_forWrappedClosePriceIndicator() {
//         ClosePriceIndicator cp = new ClosePriceIndicator(mockSeries);
//         NumericIndicator numeric = NumericIndicator.of(cp);
//         assertEquals(0, numeric.getUnstableBars(), "NumericIndicator wrapping a ClosePriceIndicator should report 0 unstable bars");
//     }

//     @Test
//     void getUnstableBars_returnsZero_forWrappedVolumeIndicator() {
//         VolumeIndicator vi = new VolumeIndicator(mockSeries);
//         NumericIndicator numeric = NumericIndicator.of(vi);
//         assertEquals(0, numeric.getUnstableBars(), "NumericIndicator wrapping a VolumeIndicator should report 0 unstable bars");
//     }

//     @Before
//     public void setUp() {
//         // No real mocking framework is required – the series is never accessed
//         // by the code under test (only stored). Using {@code null} is sufficient.
//         mockSeries = null;
//     }

//     @Test
//     public void getUnstableBars_returnsZero_forWrappedDummyIndicator() {
//         DummyIndicator dummy = new DummyIndicator();
//         NumericIndicator numeric = NumericIndicator.of(dummy);
//         assertEquals(0, numeric.getUnstableBars(), "NumericIndicator wrapping a DummyIndicator should report 0 unstable bars");
//     }
}