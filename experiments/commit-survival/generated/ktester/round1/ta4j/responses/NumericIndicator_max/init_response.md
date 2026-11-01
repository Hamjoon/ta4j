```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#max(Indicator)}.
 *
 * <p>The test creates two mocked {@link Indicator} instances that return
 * deterministic {@link Num} values. The {@code max} operation is exercised and
 * the resulting {@link NumericIndicator} is verified to return the element‑wise
 * maximum of the two input indicators.</p>
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_max_Test {

    /** Mocked first operand of the max operation. */
    @Mock
    private Indicator<Num> firstIndicator;

    /** Mocked second operand of the max operation. */
    @Mock
    private Indicator<Num> secondIndicator;

    /** The {@link NumericIndicator} under test (wraps {@code firstIndicator}). */
    private NumericIndicator numericFirst;

    /** Helper to create {@link Num} instances from primitive numbers. */
    private static final Function<Number, Num> NUM_FACTORY = DecimalNum::valueOf;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for this test class.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise the NumericIndicator that decorates the first mock.
        numericFirst = NumericIndicator.of(firstIndicator);

        // Define deterministic behaviour for the two mocked indicators.
        // Indexes 0, 1, 2 are enough to demonstrate the max logic.
        when(firstIndicator.getValue(0)).thenReturn(NUM_FACTORY.apply(1));
        when(firstIndicator.getValue(1)).thenReturn(NUM_FACTORY.apply(5));
        when(firstIndicator.getValue(2)).thenReturn(NUM_FACTORY.apply(-3));

        when(secondIndicator.getValue(0)).thenReturn(NUM_FACTORY.apply(2));
        when(secondIndicator.getValue(1)).thenReturn(NUM_FACTORY.apply(3));
        when(secondIndicator.getValue(2)).thenReturn(NUM_FACTORY.apply(-4));
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks after each test to avoid interaction leakage.
        reset(firstIndicator, secondIndicator);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to clean up.
    }

    @Test
    void maxWithIndicator_returnsElementWiseMaximum() {
        // Exercise the method under test.
        NumericIndicator result = numericFirst.max(secondIndicator);

        // Verify that the returned object is indeed a NumericIndicator.
        assertNotNull(result);
        assertTrue(result instanceof NumericIndicator);

        // Expected values are the element‑wise maximum of the two mocked series.
        Num expected0 = NUM_FACTORY.apply(2);   // max(1, 2)
        Num expected1 = NUM_FACTORY.apply(5);   // max(5, 3)
        Num expected2 = NUM_FACTORY.apply(-3);  // max(-3, -4)

        assertEquals(expected0, result.getValue(0));
        assertEquals(expected1, result.getValue(1));
        assertEquals(expected2, result.getValue(2));

        // Ensure the underlying delegates were queried the expected number of times.
        verify(firstIndicator, times(3)).getValue(anyInt());
        verify(secondIndicator, times(3)).getValue(anyInt());
    }
}
```