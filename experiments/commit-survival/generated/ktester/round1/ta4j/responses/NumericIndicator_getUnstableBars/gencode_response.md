```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;

/**
 * Tests for {@link NumericIndicator#getUnstableBars()}.
 *
 * <p>The method is defined to always return {@code 0}. The tests verify this
 * contract for different ways of creating a {@link NumericIndicator} instance,
 * including a chained‑operation scenario.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_getUnstableBars_Test {

    private Indicator<?> mockDelegate;
    private BarSeries mockSeries;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // The concrete behaviour of the delegate is irrelevant because
        // getUnstableBars() does not use it.
        mockDelegate = mock(Indicator.class);
        mockSeries = mock(BarSeries.class);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void getUnstableBars_returnsZero_forDelegateBasedInstance() {
        NumericIndicator numeric = NumericIndicator.of(mockDelegate);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator created from a generic delegate should report 0 unstable bars");
    }

    @Test
    void getUnstableBars_returnsZero_forClosePriceFactory() {
        NumericIndicator numeric = NumericIndicator.closePrice(mockSeries);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator created via closePrice() should report 0 unstable bars");
    }

    @Test
    void getUnstableBars_returnsZero_forVolumeFactory() {
        NumericIndicator numeric = NumericIndicator.volume(mockSeries);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator created via volume() should report 0 unstable bars");
    }

    @Test
    void getUnstableBars_returnsZero_forWrappedClosePriceIndicator() {
        // Use a real ClosePriceIndicator to ensure the static factory works with
        // a concrete Indicator implementation.
        ClosePriceIndicator cp = new ClosePriceIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(cp);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator wrapping a ClosePriceIndicator should report 0 unstable bars");
    }

    @Test
    void getUnstableBars_returnsZero_forWrappedVolumeIndicator() {
        VolumeIndicator vi = new VolumeIndicator(mockSeries);
        NumericIndicator numeric = NumericIndicator.of(vi);
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator wrapping a VolumeIndicator should report 0 unstable bars");
    }

    @Test
    void getUnstableBars_returnsZero_forChainedOperations() {
        // Chain a few fluent methods (sma followed by abs) and verify the contract
        // remains unchanged.
        NumericIndicator numeric = NumericIndicator.of(mockDelegate)
                .sma(5)   // creates a SMAIndicator based on the delegate
                .abs();   // wraps the result in an absolute‑value indicator
        assertEquals(0, numeric.getUnstableBars(),
                "NumericIndicator after chaining operations should still report 0 unstable bars");
    }
}
```