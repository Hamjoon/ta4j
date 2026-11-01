package org.ta4j.core.indicators;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
class ATRIndicator_getValue_Test {

    private ATRIndicator atrIndicator;

    private Num expectedNum;

    private MockedConstruction<MMAIndicator> mmaConstruction;

    // Num returned for any valid index
    private Num genericNum;

    // reference to the constructed mock
    private MMAIndicator mockedMMA;

    // pretend series size
    private static final int BAR_COUNT = 10;

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
        mmaConstruction = Mockito.mockConstruction(MMAIndicator.class, (mock, context) -> {
            // keep a reference for later verification
            mockedMMA = mock;
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

    @Test
    void testGetValueDelegatesToAverageTrueRangeIndicator() {
        int testIndex = 7;
        // Invoke the method under test.
        Num result = atrIndicator.getValue(testIndex);
        // Verify that the result is exactly the Num returned by the mocked MMAIndicator.
        assertSame(expectedNum, result, "ATRIndicator.getValue should delegate to MMAIndicator.getValue");
        // Verify that the delegated MMAIndicator received the correct index.
        MMAIndicator mockedMMA = mmaConstruction.constructed().get(0);
        verify(mockedMMA, times(1)).getValue(testIndex);
    }

    // -----------------------------------------------------------------
    @ParameterizedTest(name = "getValue({0}) should delegate and return the same Num")
    // first, a middle, and the last valid index
    @ValueSource(ints = { 0, 5, 9 })
    void testGetValue_ValidIndices_DelegatesAndReturnsNum(int index) {
        Num result = atrIndicator.getValue(index);
        assertSame(genericNum, result, "ATRIndicator.getValue should return the Num provided by MMAIndicator");
        // Verify that the MMAIndicator received exactly one call with the same index.
        verify(mockedMMA, times(1)).getValue(index);
        // Reset interaction count for the next parameter iteration.
        clearInvocations(mockedMMA);
    }

    // -----------------------------------------------------------------
    @ParameterizedTest(name = "getValue({0}) should propagate IndexOutOfBoundsException")
    // negative, equal to barCount, far beyond
    @ValueSource(ints = { -1, 10, 1000 })
    void testGetValue_InvalidIndices_PropagatesException(int index) {
        IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, () -> atrIndicator.getValue(index), "Expected IndexOutOfBoundsException for index " + index);
        assertTrue(ex.getMessage().contains(String.valueOf(index)));
        // Verify that the MMAIndicator was indeed called (and threw).
        verify(mockedMMA, times(1)).getValue(index);
        clearInvocations(mockedMMA);
    }

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
}
