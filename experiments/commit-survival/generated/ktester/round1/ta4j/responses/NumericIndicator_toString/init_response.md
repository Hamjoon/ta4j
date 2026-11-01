```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NumericIndicator#toString()}.
 * The {@code toString()} method should delegate to the wrapped {@link Indicator}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_toString_Test {

    @Mock
    private Indicator<Num> mockDelegate; // mocked delegate used in the tests

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // MockitoExtension already initialises @Mock fields before each test.
        // Additional per‑test configuration can be placed here if needed.
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
    void toString_delegatesToUnderlyingIndicator() {
        // GIVEN: a mock delegate whose toString() returns a known value
        String expectedString = "MockIndicatorDelegate";
        when(mockDelegate.toString()).thenReturn(expectedString);

        // WHEN: we create a NumericIndicator wrapping the mock delegate
        NumericIndicator numericIndicator = NumericIndicator.of(mockDelegate);

        // THEN: NumericIndicator.toString() should return the delegate's string
        assertEquals(expectedString, numericIndicator.toString(),
                "NumericIndicator.toString() must delegate to the underlying Indicator");
    }
}
```