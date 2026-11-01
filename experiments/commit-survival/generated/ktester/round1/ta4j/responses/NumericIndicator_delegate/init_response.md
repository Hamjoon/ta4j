```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#delegate()}.
 *
 * The tests verify that the {@code delegate()} method returns the exact
 * {@link Indicator} instance that was used to create the {@link NumericIndicator}
 * via the {@code of(...)} factory method. Different concrete indicator types
 * (real and mocked) are exercised.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_delegate_Test {

    /** Shared series used by all tests */
    private static BarSeries series;

    /** A simple ClosePriceIndicator based on the shared series */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator wrapper for {@link #closePriceIndicator} */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Create a series with a deterministic Num implementation (DecimalNum)
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Wrap the close‑price indicator before each test
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No state to clean up for now; placeholder for future resources
    }

    @AfterAll
    static void teardownAfterAll() {
        // Explicitly release the static references (helps GC in large test suites)
        series = null;
        closePriceIndicator = null;
    }

    @Test
    void delegateReturnsOriginalIndicator() {
        // The delegate of the wrapper must be exactly the indicator we passed in.
        Indicator<Num> delegate = numericIndicator.delegate();
        assertSame(closePriceIndicator, delegate,
                "delegate() should return the original ClosePriceIndicator instance");
    }

    @Test
    void delegateWorksWithEmaIndicator() {
        // Create a cached EMA indicator based on the same close‑price indicator
        EMAIndicator ema = new EMAIndicator(closePriceIndicator, 3);
        NumericIndicator numericEma = NumericIndicator.of(ema);

        // The delegate must be the EMA indicator itself
        assertSame(ema, numericEma.delegate(),
                "delegate() should return the EMAIndicator instance used in the factory");
    }

    @Test
    void delegateWorksWithMockedIndicator() {
        // Use Mockito to create a dummy Indicator<Num>
        Indicator<Num> mockIndicator = mock(Indicator.class);
        // The mock does not need any behaviour for this test; we only care about identity
        NumericIndicator numericMock = NumericIndicator.of(mockIndicator);

        // Verify that delegate returns the exact mock instance
        assertSame(mockIndicator, numericMock.delegate(),
                "delegate() should return the mocked Indicator instance");
    }
}
```