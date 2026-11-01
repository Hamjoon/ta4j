```java
package org.ta4j.core.indicators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.mocks.MockBar;

/**
 * Unit tests for {@link ATRIndicator#getBarCount()}.
 *
 * <p>The method simply delegates to the underlying {@link MMAIndicator}'s
 * {@code getBarCount()} method. The tests therefore focus on:
 *
 * <ul>
 *   <li>Correct forwarding of various integer values (including edge and
 *   boundary cases).</li>
 *   <li>Propagation of {@code NullPointerException} when the internal
 *   {@code averageTrueRangeIndicator} is {@code null}.</li>
 *   <li>Propagation of unchecked exceptions thrown by the underlying
 *   {@code MMAIndicator}.</li>
 * </ul>
 *
 * <p>Mockito is used to mock {@link MMAIndicator} so that we can control the
 * return value or exception of {@code getBarCount()} without relying on the
 * actual moving‑average implementation.</p>
 */
@ExtendWith(MockitoExtension.class)
class ATRIndicator_getBarCount_Test {

    private BarSeries series;

    @BeforeEach
    void setUp() {
        // A minimal series – its content is irrelevant for getBarCount()
        series = new BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Add a single dummy bar to avoid an empty series (optional)
        series.addBar(new MockBar(
                ZonedDateTime.now(),
                0, 10, 12, 9, 0, 0, 0,
                org.ta4j.core.num.DecimalNum::valueOf));
    }

    /**
     * Helper that injects a mocked {@link MMAIndicator} into an {@link ATRIndicator}
     * instance via reflection.
     *
     * @param atr the instance whose private field will be replaced
     * @param mockMMA the mock to inject
     */
    private void injectMMAIndicator(ATRIndicator atr, MMAIndicator mockMMA) throws Exception {
        Field field = ATRIndicator.class.getDeclaredField("averageTrueRangeIndicator");
        field.setAccessible(true);
        field.set(atr, mockMMA);
    }

    /* --------------------------------------------------------------------- *
     *  Normal forwarding cases (positive, zero, negative, large values)    *
     * --------------------------------------------------------------------- */

    @ParameterizedTest(name = "[{index}] underlying MMAIndicator returns {0}")
    @MethodSource("provideBarCountValues")
    void getBarCount_forwardsUnderlyingValue(int underlyingBarCount) throws Exception {
        // Arrange: mock MMAIndicator to return the supplied value
        MMAIndicator mockMMA = mock(MMAIndicator.class);
        when(mockMMA.getBarCount()).thenReturn(underlyingBarCount);

        // Build an ATRIndicator using the public constructor (any constructor works)
        ATRIndicator atr = new ATRIndicator(series, 1); // period value is irrelevant here
        injectMMAIndicator(atr, mockMMA);

        // Act
        int actual = atr.getBarCount();

        // Assert
        assertEquals(underlyingBarCount, actual,
                "ATRIndicator#getBarCount() must return exactly the value provided by the underlying MMAIndicator");
    }

    private static Stream<Arguments> provideBarCountValues() {
        return Stream.of(
                Arguments.of(10),                 // typical positive
                Arguments.of(0),                  // zero
                Arguments.of(-5),                 // negative (defensive)
                Arguments.of(Integer.MAX_VALUE), // max int boundary
                Arguments.of(Integer.MIN_VALUE), // min int boundary
                Arguments.of(7),                  // odd number
                Arguments.of(123_456_789),        // large positive
                Arguments.of(-42)                 // another negative
        );
    }

    /* --------------------------------------------------------------------- *
     *  Exception propagation cases                                         *
     * --------------------------------------------------------------------- */

    @Test
    void getBarCount_throwsNullPointerException_whenUnderlyingIndicatorIsNull() throws Exception {
        // Arrange: create a normal ATRIndicator and then nullify its internal field
        ATRIndicator atr = new ATRIndicator(series, 1);
        injectMMAIndicator(atr, null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                atr::getBarCount,
                "Calling getBarCount() with a null averageTrueRangeIndicator should throw NPE");
    }

    @Test
    void getBarCount_propagatesRuntimeException_fromUnderlyingIndicator() throws Exception {
        // Arrange: mock MMAIndicator to throw a RuntimeException
        MMAIndicator mockMMA = mock(MMAIndicator.class);
        RuntimeException runtimeEx = new IllegalStateException("Underlying failure");
        when(mockMMA.getBarCount()).thenThrow(runtimeEx);

        ATRIndicator atr = new ATRIndicator(series, 1);
        injectMMAIndicator(atr, mockMMA);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class,
                atr::getBarCount,
                "RuntimeException from the underlying MMAIndicator must be propagated unchanged");
        assertSame(runtimeEx, thrown, "The same exception instance should be propagated");
    }
}
```