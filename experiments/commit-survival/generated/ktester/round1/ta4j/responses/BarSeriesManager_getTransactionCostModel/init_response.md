```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.BarSeries;
import org.ta4j.core.backtest.TradeExecutionModel;

/**
 * Tests for {@link BarSeriesManager#getTransactionCostModel()}.
 *
 * The tests verify that the constructor‑injected {@code transactionCostModel}
 * is correctly returned by the getter for the different overloads of the
 * {@code BarSeriesManager} constructors.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_getTransactionCostModel_Test {

    // Shared mock objects
    @Mock
    private BarSeries mockSeries;

    @Mock
    private TradeExecutionModel mockExecutionModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise Mockito annotations for @Mock fields
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    @Test
    void whenUsingDefaultConstructor_thenTransactionCostModelIsZeroCostModel() {
        // given
        BarSeriesManager manager = new BarSeriesManager(mockSeries);
        // when
        CostModel result = manager.getTransactionCostModel();
        // then
        assertTrue(result instanceof ZeroCostModel,
                "Default constructor should initialise a ZeroCostModel");
    }

    @Test
    void whenProvidingCustomTransactionCostModel_thenGetterReturnsSameInstance() {
        // given
        CostModel customTransactionCost = mock(CostModel.class);
        CostModel holdingCost = new ZeroCostModel(); // any holding cost works
        BarSeriesManager manager = new BarSeriesManager(mockSeries,
                customTransactionCost,
                holdingCost);
        // when
        CostModel result = manager.getTransactionCostModel();
        // then
        assertSame(customTransactionCost, result,
                "Getter must return the transaction cost model supplied to the constructor");
    }

    @Test
    void whenUsingFullConstructor_thenGetterReturnsProvidedTransactionCostModel() {
        // given
        CostModel customTransactionCost = mock(CostModel.class);
        CostModel customHoldingCost = mock(CostModel.class);
        // mockExecutionModel is already injected via @Mock
        BarSeriesManager manager = new BarSeriesManager(mockSeries,
                customTransactionCost,
                customHoldingCost,
                mockExecutionModel);
        // when
        CostModel result = manager.getTransactionCostModel();
        // then
        assertSame(customTransactionCost, result,
                "Getter must return the transaction cost model supplied to the full‑args constructor");
    }
}
```