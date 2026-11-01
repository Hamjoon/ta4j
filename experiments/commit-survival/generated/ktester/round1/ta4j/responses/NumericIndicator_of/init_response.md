```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link NumericIndicator#of(Indicator)}.
 *
 * <p>The tests verify that the static factory method correctly creates a
 * {@link NumericIndicator} instance that delegates all calls to the supplied
 * {@link Indicator} implementation. Both a Mockito mock and a concrete
 * {@link ClosePriceIndicator} are used to ensure the behaviour works for any
 * {@link Indicator} implementation.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_of_Test {

    /** Mocked Indicator used in most tests. */
    @Mock
    private Indicator<Num> mockIndicator;

    /** The NumericIndicator created by the factory method under test. */
    private NumericIndicator numericIndicator;

    /** A simple BarSeries used for the concrete‑indicator test. */
    private static BarSeries series;

    @BeforeAll
    static void setupBeforeAll() {
        // A very small series – the actual values are irrelevant for the
        // delegation tests, they just need to be non‑null.
        series = new org.ta4j.core.mocks.MockBarSeries(
                DecimalNum::valueOf,
                1, 2, 3, 4, 5);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Reset mock behaviour before each test.
        reset(mockIndicator);
        // Create the object under test.
        numericIndicator = NumericIndicator.of(mockIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // Verify that no unexpected interactions happened on the mock.
        verifyNoMoreInteractions(mockIndicator);
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing to clean up for this test class.
    }

    @Test
    void ofCreatesNumericIndicatorWithCorrectDelegate() {
        assertNotNull(numericIndicator, "NumericIndicator.of should not return null");
        // The delegate accessor is public, so we can check it directly.
        assertSame(mockIndicator, numericIndicator.delegate(),
                "The delegate returned by NumericIndicator should be the original Indicator");
    }

    @Test
    void getValueIsDelegatedToUnderlyingIndicator() {
        // Arrange
        int index = 7;
        Num expected = DecimalNum.valueOf(42);
        when(mockIndicator.getValue(index)).thenReturn(expected);

        // Act
        Num actual = numericIndicator.getValue(index);

        // Assert
        assertEquals(expected, actual, "NumericIndicator#getValue must delegate to the underlying Indicator");
        verify(mockIndicator).getValue(index);
    }

    @Test
    void getBarSeriesIsDelegatedToUnderlyingIndicator() {
        // Arrange
        BarSeries expectedSeries = series;
        when(mockIndicator.getBarSeries()).thenReturn(expectedSeries);

        // Act
        BarSeries actualSeries = numericIndicator.getBarSeries();

        // Assert
        assertSame(expectedSeries, actualSeries,
                "NumericIndicator#getBarSeries must delegate to the underlying Indicator");
        verify(mockIndicator).getBarSeries();
    }

    @Test
    void getUnstableBarsIsDelegatedToUnderlyingIndicator() {
        // Arrange
        int expectedUnstable = 3;
        when(mockIndicator.getUnstableBars()).thenReturn(expectedUnstable);

        // Act
        int actualUnstable = numericIndicator.getUnstableBars();

        // Assert
        assertEquals(expectedUnstable, actualUnstable,
                "NumericIndicator#getUnstableBars must delegate to the underlying Indicator");
        verify(mockIndicator).getUnstableBars();
    }

    @Test
    void ofWorksWithARealIndicatorImplementation() {
        // Use a concrete indicator (ClosePriceIndicator) to ensure the factory
        // works with non‑mock objects as well.
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        NumericIndicator fromClose = NumericIndicator.of(closePrice);

        // The delegate should be exactly the ClosePriceIndicator we passed.
        assertSame(closePrice, fromClose.delegate(),
                "NumericIndicator.of should keep the concrete Indicator as its delegate");

        // Spot‑check a few values to confirm correct delegation.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), fromClose.getValue(i),
                    "Value at index " + i + " must be delegated correctly");
        }
    }
}
```