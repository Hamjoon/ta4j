```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Additional tests for {@link NumericIndicator#delegate()} that cover the
 * scenarios described in the JSON test‑case specification.
 *
 * <p>
 * The existing test class already verifies the basic behaviour with a real
 * {@link ClosePriceIndicator}, an {@link EMAIndicator} and a Mockito mock.
 * Here we add:
 * <ul>
 *   <li>a mock indicator that returns a constant {@link Num} value (10)</li>
 *   <li>verification that the factory methods {@code closePrice(...)} and
 *       {@code volume(...)} create the correct delegate types</li>
 *   <li>an edge‑case test for a {@code null} delegate passed to the
 *       {@code of(...)} factory method</li>
 * </ul>
 *
 * All tests keep the original {@code @BeforeAll}, {@code @BeforeEach},
 * {@code @AfterEach} and {@code @AfterAll} lifecycle methods untouched.
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
        // Deterministic Num implementation (DecimalNum) for reproducible values
        Function<Number, Num> numFunction = DecimalNum::valueOf;
        series = new MockBarSeries(numFunction,
                // close prices 1..5 – enough for the factory‑method tests
                1, 2, 3, 4, 5);
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setupBeforeEach() {
        // Wrap the close‑price indicator before each test
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // Placeholder for future clean‑up
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
    }

    /* --------------------------------------------------------------------- */
    /* Existing tests (kept unchanged)                                      */
    /* --------------------------------------------------------------------- */

    @Test
    void delegateReturnsOriginalIndicator() {
        Indicator<Num> delegate = numericIndicator.delegate();
        assertSame(closePriceIndicator, delegate,
                "delegate() should return the original ClosePriceIndicator instance");
    }

    @Test
    void delegateWorksWithEmaIndicator() {
        EMAIndicator ema = new EMAIndicator(closePriceIndicator, 3);
        NumericIndicator numericEma = NumericIndicator.of(ema);
        assertSame(ema, numericEma.delegate(),
                "delegate() should return the EMAIndicator instance used in the factory");
    }

    @Test
    void delegateWorksWithMockedIndicator() {
        Indicator<Num> mockIndicator = mock(Indicator.class);
        NumericIndicator numericMock = NumericIndicator.of(mockIndicator);
        assertSame(mockIndicator, numericMock.delegate(),
                "delegate() should return the mocked Indicator instance");
    }

    /* --------------------------------------------------------------------- */
    /* New tests derived from the JSON specification                         */
    /* --------------------------------------------------------------------- */

    /**
     * Verify that a {@link NumericIndicator} created from a mock that always
     * returns the constant value {@code 10} returns that very mock instance
     * via {@code delegate()}.
     */
    @Test
    void delegateReturnsConstantMockIndicator() {
        // Create a mock Indicator that returns DecimalNum.valueOf(10) for any index
        Indicator<Num> constantMock = mock(Indicator.class);
        when(constantMock.getValue(anyInt())).thenReturn(DecimalNum.valueOf(10));

        NumericIndicator numeric = NumericIndicator.of(constantMock);
        assertSame(constantMock, numeric.delegate(),
                "delegate() must return the exact mock instance supplied to the factory");
    }

    /**
     * When a {@link NumericIndicator} is created via the static
     * {@code closePrice(BarSeries)} factory, its delegate must be a
     * {@link ClosePriceIndicator} that wraps the same {@link BarSeries}.
     */
    @Test
    void delegateFromClosePriceFactoryIsClosePriceIndicator() {
        NumericIndicator fromFactory = NumericIndicator.closePrice(series);
        Indicator<Num> delegate = fromFactory.delegate();

        assertTrue(delegate instanceof ClosePriceIndicator,
                "delegate() should be an instance of ClosePriceIndicator");
        // Cast is safe because of the previous assertion
        ClosePriceIndicator cpi = (ClosePriceIndicator) delegate;
        assertSame(series, cpi.getBarSeries(),
                "The ClosePriceIndicator should reference the original BarSeries");
    }

    /**
     * When a {@link NumericIndicator} is created via the static
     * {@code volume(BarSeries)} factory, its delegate must be a
     * {@link VolumeIndicator} that wraps the same {@link BarSeries}.
     */
    @Test
    void delegateFromVolumeFactoryIsVolumeIndicator() {
        NumericIndicator fromFactory = NumericIndicator.volume(series);
        Indicator<Num> delegate = fromFactory.delegate();

        assertTrue(delegate instanceof VolumeIndicator,
                "delegate() should be an instance of VolumeIndicator");
        VolumeIndicator vi = (VolumeIndicator) delegate;
        assertSame(series, vi.getBarSeries(),
                "The VolumeIndicator should reference the original BarSeries");
    }

    /**
     * Edge‑case: passing {@code null} to {@code NumericIndicator.of(...)}.
     * The library currently validates the argument and throws a
     * {@link NullPointerException}. If the implementation changes to allow a
     * null delegate, the test can be adapted to expect {@code null} from
     * {@code delegate()}.
     */
    @Test
    void delegateWithNullArgumentThrowsNPE() {
        assertThrows(NullPointerException.class,
                () -> NumericIndicator.of(null),
                "Creating a NumericIndicator with a null delegate should throw NullPointerException");
    }
}
```