```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive tests for {@link NumericIndicator#volume(BarSeries)}.
 *
 * <p>
 * The test suite covers:
 * <ul>
 *   <li>basic non‑empty series forwarding</li>
 *   <li>empty series handling</li>
 *   <li>null {@link BarSeries} argument</li>
 *   <li>single‑bar series</li>
 *   <li>series containing a {@code null} bar</li>
 *   <li>series containing a bar with a {@code null} volume</li>
 *   <li>defensive checks for {@link NumericIndicator#of(Indicator)} with a {@code null} delegate</li>
 *   <li>invalid series reporting a negative bar count</li>
 * </ul>
 *
 * <p>All tests use JUnit 5 and Mockito (via {@code @ExtendWith(MockitoExtension.class)}).
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_volume_Test {

    /** Simple Num factory used by {@link MockBarSeries}. */
    private static final Function<Number, Num> NUM_FACTORY = DecimalNum::valueOf;

    /** Series used for deterministic test cases. */
    private BarSeries series;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Default series – 10 bars with volume equal to the close price (convenient for tests).
        series = new MockBarSeries(
                NUM_FACTORY,
                10, 20, 30, 40, 50, 60, 70, 80, 90, 100
        );
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    /* --------------------------------------------------------------------- */
    /* Basic non‑empty series – already covered by the original test method. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeCreatesNumericIndicatorWithVolumeDelegate() {
        // When
        NumericIndicator numericVolume = NumericIndicator.volume(series);

        // Then: the delegate must be a VolumeIndicator
        assertNotNull(numericVolume, "NumericIndicator.volume(...) must not return null");
        assertNotNull(numericVolume.delegate(),
                "Delegate of the created NumericIndicator must not be null");
        assertTrue(numericVolume.delegate() instanceof VolumeIndicator,
                "Delegate should be an instance of VolumeIndicator");

        // And the values must match a directly created VolumeIndicator
        VolumeIndicator directVolume = new VolumeIndicator(series);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = directVolume.getValue(i);
            Num actual = numericVolume.getValue(i);
            assertEquals(expected, actual,
                    "NumericIndicator volume value at index " + i + " must match VolumeIndicator");
        }
    }

    /* --------------------------------------------------------------------- */
    /* Empty series – the wrapper is created but any value access must fail. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeWithEmptySeriesCreatesDelegateButAccessThrows() {
        // Empty series (no bars)
        BarSeries emptySeries = new MockBarSeries(NUM_FACTORY);

        NumericIndicator numericVolume = NumericIndicator.volume(emptySeries);

        assertNotNull(numericVolume);
        assertTrue(numericVolume.delegate() instanceof VolumeIndicator,
                "Delegate should still be a VolumeIndicator");

        // Accessing any index must throw IndexOutOfBoundsException (behaviour of VolumeIndicator)
        assertThrows(IndexOutOfBoundsException.class,
                () -> numericVolume.getValue(0),
                "Calling getValue on an empty series must throw IndexOutOfBoundsException");
    }

    /* --------------------------------------------------------------------- */
    /* Null BarSeries argument – factory should reject it. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeWithNullSeriesThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> NumericIndicator.volume(null),
                "Passing null BarSeries should result in NullPointerException");
    }

    /* --------------------------------------------------------------------- */
    /* Single‑bar series – minimal non‑empty case. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeWithSingleBarSeriesForwardsValueAndSeriesReference() {
        BarSeries singleBarSeries = new MockBarSeries(NUM_FACTORY, 42);

        NumericIndicator numericVolume = NumericIndicator.volume(singleBarSeries);

        assertNotNull(numericVolume);
        assertEquals(singleBarSeries, numericVolume.getBarSeries(),
                "NumericIndicator must preserve the original BarSeries reference");
        assertEquals(DecimalNum.valueOf(42), numericVolume.getValue(0),
                "The single volume value must be correctly forwarded");
    }

    /* --------------------------------------------------------------------- */
    /* Series containing a null Bar – should propagate NullPointerException. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeWithSeriesHavingNullBarThrowsNullPointerException(@Mock BarSeries mockSeries,
                                                                 @Mock Bar validBar) {
        // Configure a series of size 2: first bar is valid, second bar is null.
        Mockito.when(mockSeries.getBarCount()).thenReturn(2);
        Mockito.when(mockSeries.getBar(0)).thenReturn(validBar);
        Mockito.when(validBar.getVolume()).thenReturn(DecimalNum.valueOf(100));
        Mockito.when(mockSeries.getBar(1)).thenReturn(null); // the problematic bar

        NumericIndicator numericVolume = NumericIndicator.volume(mockSeries);

        // Accessing the second index must throw NPE because the underlying bar is null.
        assertThrows(NullPointerException.class,
                () -> numericVolume.getValue(1),
                "Accessing a null bar should propagate NullPointerException");
    }

    /* --------------------------------------------------------------------- */
    /* Series containing a bar whose getVolume() returns null. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeWithBarHavingNullVolumeThrowsNullPointerException(@Mock BarSeries mockSeries,
                                                                 @Mock Bar barWithNullVolume) {
        Mockito.when(mockSeries.getBarCount()).thenReturn(1);
        Mockito.when(mockSeries.getBar(0)).thenReturn(barWithNullVolume);
        Mockito.when(barWithNullVolume.getVolume()).thenReturn(null); // null volume

        NumericIndicator numericVolume = NumericIndicator.volume(mockSeries);

        assertThrows(NullPointerException.class,
                () -> numericVolume.getValue(0),
                "Accessing a bar with null volume should propagate NullPointerException");
    }

    /* --------------------------------------------------------------------- */
    /* Defensive check: NumericIndicator.of(null) must reject a null delegate. */
    /* --------------------------------------------------------------------- */

    @Test
    void ofWithNullDelegateThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> NumericIndicator.of(null),
                "NumericIndicator.of(null) must throw NullPointerException");
    }

    /* --------------------------------------------------------------------- */
    /* Invalid series reporting a negative bar count. */
    /* --------------------------------------------------------------------- */

    @Test
    void volumeWithNegativeBarCountSeriesThrowsIllegalArgumentException(@Mock BarSeries mockSeries) {
        // Simulate an ill‑formed series
        Mockito.when(mockSeries.getBarCount()).thenReturn(-1);

        // The constructor of VolumeIndicator validates the bar count and should throw.
        assertThrows(IllegalArgumentException.class,
                () -> NumericIndicator.volume(mockSeries),
                "Creating a VolumeIndicator with a series reporting negative bar count must fail");
    }
}
```