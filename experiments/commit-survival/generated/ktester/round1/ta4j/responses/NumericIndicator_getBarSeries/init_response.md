```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NumericIndicator#getBarSeries()}.
 *
 * The test creates a mocked {@link Indicator} that returns a mocked {@link BarSeries}
 * and verifies that {@link NumericIndicator} delegates the call correctly.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_getBarSeries_Test {

    @Mock
    private BarSeries mockSeries;          // mocked BarSeries used by the delegate
    @Mock
    private Indicator<Num> mockDelegate; // mocked delegate Indicator

    private NumericIndicator numericIndicator; // instance under test

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for this test suite
    }

    @BeforeEach
    void setupBeforeEach() {
        // Stub the delegate to return the mocked BarSeries
        when(mockDelegate.getBarSeries()).thenReturn(mockSeries);
        // Build the NumericIndicator using the public factory method
        numericIndicator = NumericIndicator.of(mockDelegate);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    @Test
    void shouldReturnDelegateBarSeries() {
        // Act
        BarSeries result = numericIndicator.getBarSeries();

        // Assert
        assertSame(mockSeries, result,
                "NumericIndicator#getBarSeries() must return the BarSeries supplied by its delegate");
    }

    @Test
    void shouldReturnSameBarSeriesOnRepeatedCalls() {
        // Act
        BarSeries firstCall = numericIndicator.getBarSeries();
        BarSeries secondCall = numericIndicator.getBarSeries();

        // Assert
        assertSame(firstCall, secondCall,
                "Repeated calls to getBarSeries() should return the same instance");
    }
}
```