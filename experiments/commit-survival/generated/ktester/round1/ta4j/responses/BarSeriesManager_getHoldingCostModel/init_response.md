```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;

/**
 * Tests for {@link BarSeriesManager#getHoldingCostModel()}.
 *
 * The tests verify that the holding cost model supplied via the constructor
 * is correctly returned by the getter and that the default constructor
 * (which uses a {@link ZeroCostModel}) behaves as expected.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_getHoldingCostModel_Test {

    // -----------------------------------------------------------------------
    // Mocks and objects used in the tests
    // -----------------------------------------------------------------------
    @Mock
    BarSeries barSeries;                 // mocked BarSeries – no real data needed

    @Mock
    CostModel customHoldingCostModel;    // a custom holding‑cost model to be injected

    BarSeriesManager manager;            // instance under test

    // -----------------------------------------------------------------------
    // Lifecycle callbacks
    // -----------------------------------------------------------------------
    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // The manager is created in each test method to keep tests independent.
        // No common initialisation here.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------

    @Test
    void whenHoldingCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN: a manager built with a custom holding‑cost model
        manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),          // transaction cost model (default)
                customHoldingCostModel        // holding cost model under test
        );

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the exact instance supplied at construction is returned
        assertSame(customHoldingCostModel, result,
                "The holding cost model returned by getHoldingCostModel() should be the same instance that was injected via the constructor.");
    }

    @Test
    void whenUsingDefaultConstructor_thenHoldingCostModelIsZeroCostModel() {
        // GIVEN: a manager created with the single‑argument constructor (defaults to ZeroCostModel)
        manager = new BarSeriesManager(barSeries);

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the result should be an instance of ZeroCostModel
        assertTrue(result instanceof ZeroCostModel,
                "The default holding cost model should be an instance of ZeroCostModel.");
    }
}
```