```java
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

@ExtendWith(MockitoExtension.class)
class ATRIndicator_getValue_Test {

    private ATRIndicator atrIndicator;
    private Num expectedNum;
    private MockedConstruction<MMAIndicator> mmaConstruction;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for this test class.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Mock the Num that will be returned by the delegated MMAIndicator.
        expectedNum = mock(Num.class);
        when(expectedNum.doubleValue()).thenReturn(123.45);

        // Mock the BarSeries required by TRIndicator.
        BarSeries mockSeries = mock(BarSeries.class);

        // Mock TRIndicator – only getBarSeries() is needed for the ATRIndicator constructor.
        TRIndicator mockTR = mock(TRIndicator.class);
        when(mockTR.getBarSeries()).thenReturn(mockSeries);

        // Mock the construction of MMAIndicator so that the ATRIndicator receives the mock.
        mmaConstruction = Mockito.mockConstruction(MMAIndicator.class, (mock, context) -> {
            when(mock.getValue(anyInt())).thenReturn(expectedNum);
        });

        // Create the ATRIndicator with the mocked TRIndicator.
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
}
```