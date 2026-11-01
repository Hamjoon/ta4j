package org.ta4j.core.analysis;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import static org.mockito.Mockito.*;
import org.ta4j.core.Bar;

@ExtendWith(MockitoExtension.class)
class CashFlow_getBarSeries_Test {

    @Mock
    private BarSeries mockBarSeries;

    @Mock
    private Position mockPosition;

    @Mock
    private TradingRecord mockTradingRecord;

    @Mock
    private Bar // used for the reference‑identity test
    mockBar;

    @Test
    void getBarSeries_returnsSeriesProvidedInPositionConstructor() {
        // Given: a CashFlow created with a BarSeries and a Position
        CashFlow cashFlow = new CashFlow(mockBarSeries, mockPosition);
        // When: retrieving the BarSeries
        BarSeries returnedSeries = cashFlow.getBarSeries();
        // Then: the returned instance is the same as the one supplied to the constructor
        assertSame(mockBarSeries, returnedSeries, "CashFlow#getBarSeries() should return the BarSeries passed to the constructor");
    }

    @Test
    void getBarSeries_returnsSeriesProvidedInTradingRecordConstructor() {
        // Given: a CashFlow created with a BarSeries and a TradingRecord
        CashFlow cashFlow = new CashFlow(mockBarSeries, mockTradingRecord);
        // When: retrieving the BarSeries
        BarSeries returnedSeries = cashFlow.getBarSeries();
        // Then: the returned instance is the same as the one supplied to the constructor
        assertSame(mockBarSeries, returnedSeries, "CashFlow#getBarSeries() should return the BarSeries passed to the constructor");
    }

    @Test
    void getBarSeries_returnsNullWhenConstructedWithNullSeries_PositionConstructor() {
        // Given: a CashFlow built with a null BarSeries and a Position
        CashFlow cashFlow = new CashFlow(null, mockPosition);
        // When: retrieving the BarSeries
        BarSeries returnedSeries = cashFlow.getBarSeries();
        // Then: the method should return null
        assertNull(returnedSeries, "CashFlow#getBarSeries() must return null when the instance was constructed with a null BarSeries");
    }

    @Test
    void getBarSeries_returnsNullWhenConstructedWithNullSeries_TradingRecordConstructor() {
        // Given: a CashFlow built with a null BarSeries and a TradingRecord
        CashFlow cashFlow = new CashFlow(null, mockTradingRecord);
        // When: retrieving the BarSeries
        BarSeries returnedSeries = cashFlow.getBarSeries();
        // Then: the method must return null
        assertNull(returnedSeries, "CashFlow#getBarSeries() must return null when the instance was constructed with a null BarSeries");
    }

    @Test
    void getBarSeries_returnsSameReference_modificationsAffectOriginal() {
        // Given: a CashFlow created with a mock BarSeries
        CashFlow cashFlow = new CashFlow(mockBarSeries, mockPosition);
        // When: we obtain the series and invoke a mutating operation on it
        BarSeries seriesFromCashFlow = cashFlow.getBarSeries();
        // BarSeries has an addBar(Bar) method
        seriesFromCashFlow.addBar(mockBar);
        // Then: the underlying mock must have received the same call,
        // proving that the returned reference is the actual stored instance.
        verify(mockBarSeries, times(1)).addBar(mockBar);
    }
}
