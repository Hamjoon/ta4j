```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#sqrt()}.
 *
 * The test creates a mocked {@link Indicator}&lt;{@link Num}&gt; that returns a
 * predefined sequence of numbers. The {@code sqrt()} method should produce a
 * new {@link NumericIndicator} whose values are the square‑roots of the original
 * values. No private members are accessed – only the public API is used.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_sqrt_Test {

    /** Mocked delegate indicator used in all tests. */
    @Mock
    private Indicator<Num> delegate;

    /** The {@link NumericIndicator} under test (wrapping {@code delegate}). */
    private NumericIndicator numericIndicator;

    /** Sample data (index → value) used by the mock. */
    private static final double[] VALUES = {
            0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0
    };

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Stub the mock to return the appropriate Num for each index.
        // The mock will be asked for getValue(int) many times, so we use an
        // Answer that converts the index to the corresponding DecimalNum.
        when(delegate.getValue(anyInt())).thenAnswer(invocation -> {
            int index = invocation.getArgument(0);
            // Guard against out‑of‑range access – return NaN for safety.
            if (index < 0 || index >= VALUES.length) {
                return DecimalNum.NaN;
            }
            return DecimalNum.valueOf(VALUES[index]);
        });

        // The NumericIndicator simply decorates the mock.
        numericIndicator = NumericIndicator.of(delegate);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset the mock to avoid cross‑test interference.
        reset(delegate);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up.
    }

    @Test
    void sqrt_computesSquareRootCorrectly() {
        // When
        NumericIndicator sqrtIndicator = numericIndicator.sqrt();

        // Then – verify each value is the square‑root of the original.
        for (int i = 0; i < VALUES.length; i++) {
            Num expected = DecimalNum.valueOf(Math.sqrt(VALUES[i]));
            Num actual = sqrtIndicator.getValue(i);
            assertEquals(expected, actual,
                    "sqrt value at index " + i + " should be " + expected);
        }

        // Also verify that the underlying delegate was queried the expected number
        // of times (once per index). This is optional but demonstrates mock usage.
        verify(delegate, times(VALUES.length)).getValue(anyInt());
    }
}
```