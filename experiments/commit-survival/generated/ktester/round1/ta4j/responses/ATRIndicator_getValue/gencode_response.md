```java
package org.ta4j.core.indicators;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ATRIndicator#getValue(int)}.
 *
 * <p>The {@code ATRIndicator} delegates the actual calculation to an internal
 * {@link MMAIndicator}.  In these tests we mock the construction of the
 * {@code MMAIndicator} so that we can verify the delegation behaviour,
 * the propagation of exceptions and that the correct index is passed exactly
 * once.</p>
 */
@ExtendWith(MockitoExtension.class)
class ATRIndicator_getValue_Test {

    private ATRIndicator atrIndicator;
    private Num genericNum;                     // Num returned for any valid index
    private MockedConstruction<MMAIndicator> mmaConstruction;
    private MMAIndicator mockedMMA;             // reference to the constructed mock
    private static final int BAR_COUNT = 10;     // pretend series size

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required.
    }

    @BeforeEach
    void setupBeforeEach() {
        // -----------------------------------------------------------------
        // 1. Prepare a generic Num that will be returned for every valid index.
        // -----------------------------------------------------------------
        genericNum = mock(Num.class);
        when(genericNum.doubleValue()).thenReturn(123.45);

        // -----------------------------------------------------------------
        // 2. Mock the BarSeries required by TRIndicator (only getBarCount()
        //    is needed for the ATRIndicator constructor chain).
        // -----------------------------------------------------------------
        BarSeries mockSeries = mock(BarSeries.class);
        when(mockSeries.getBarCount()).thenReturn(BAR_COUNT);

        // -----------------------------------------------------------------
        // 3. Mock TRIndicator – the ATRIndicator receives it in its constructor.
        // -----------------------------------------------------------------
        TRIndicator mockTR = mock(TRIndicator.class);
        when(mockTR.getBarSeries()).thenReturn(mockSeries);

        // -----------------------------------------------------------------
        // 4. Mock the construction of MMAIndicator.  The lambda receives each
        //    mock instance; we configure it to:
        //      * return {@code genericNum} for any index inside the series,
        //      * throw {@code IndexOutOfBoundsException} for illegal indices,
        //      * return the series bar count via {@code getBarCount()}.
        // -----------------------------------------------------------------
        mmaConstruction = Mockito.mockConstruction(MMAIndicator.class,
                (mock, context) -> {
                    mockedMMA = mock; // keep a reference for later verification
                    when(mock.getBarCount()).thenReturn(BAR_COUNT);
                    when(mock.getValue(anyInt())).thenAnswer(invocation -> {
                        int idx = invocation.getArgument(0);
                        if (idx < 0 || idx >= BAR_COUNT) {
                            throw new IndexOutOfBoundsException("Invalid index: " + idx);
                        }
                        return genericNum;
                    });
                });

        // -----------------------------------------------------------------
        // 5. Finally create the ATRIndicator with the mocked TRIndicator.
        // -----------------------------------------------------------------
        atrIndicator = new ATRIndicator(mockTR, 5);
    }

    @AfterEach
    void teardownAfterEach() {
        // Release the mocked construction to avoid side‑effects between tests.
        if (mmaConstruction != null) {
            mmaConstruction.close();
        }
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    // -----------------------------------------------------------------
    //  VALID INDEX DELEGATION
    // -----------------------------------------------------------------
    @ParameterizedTest(name = "getValue({0}) should delegate and return the same Num")
    @ValueSource(ints = {0, 5, 9}) // first, a middle, and the last valid index
    void testGetValue_ValidIndices_DelegatesAndReturnsNum(int index) {
        Num result = atrIndicator.getValue(index);
        assertSame(genericNum, result,
                "ATRIndicator.getValue should return the Num provided by MMAIndicator");
        // Verify that the MMAIndicator received exactly one call with the same index.
        verify(mockedMMA, times(1)).getValue(index);
        // Reset interaction count for the next parameter iteration.
        clearInvocations(mockedMMA);
    }

    // -----------------------------------------------------------------
    //  INVALID INDEX – NEGATIVE OR OUT OF BOUNDS
    // -----------------------------------------------------------------
    @ParameterizedTest(name = "getValue({0}) should propagate IndexOutOfBoundsException")
    @ValueSource(ints = {-1, 10, 1000}) // negative, equal to barCount, far beyond
    void testGetValue_InvalidIndices_PropagatesException(int index) {
        IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class,
                () -> atrIndicator.getValue(index),
                "Expected IndexOutOfBoundsException for index " + index);
        assertTrue(ex.getMessage().contains(String.valueOf(index)));
        // Verify that the MMAIndicator was indeed called (and threw).
        verify(mockedMMA, times(1)).getValue(index);
        clearInvocations(mockedMMA);
    }

    // -----------------------------------------------------------------
    //  CALL COUNT VERIFICATION
    // -----------------------------------------------------------------
    @Test
    void testGetValue_CallsMMAIndicatorExactlyOnce() {
        int testIndex = 3;
        Num result = atrIndicator.getValue(testIndex);
        assertSame(genericNum, result, "Returned Num should be the one from MMAIndicator");
        // Ensure a single invocation with the exact index.
        verify(mockedMMA, times(1)).getValue(testIndex);
        // No other interactions should have happened.
        verifyNoMoreInteractions(mockedMMA);
    }

    // -----------------------------------------------------------------
    //  ORIGINAL DELEGATION TEST (kept for completeness)
    // -----------------------------------------------------------------
    @Test
    void testGetValueDelegatesToAverageTrueRangeIndicator() {
        int testIndex = 7;
        Num result = atrIndicator.getValue(testIndex);
        assertSame(genericNum, result,
                "ATRIndicator.getValue should delegate to MMAIndicator.getValue");
        verify(mockedMMA, times(1)).getValue(testIndex);
    }
}
```