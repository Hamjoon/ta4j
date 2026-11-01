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
import org.ta4j.core.num.Num;

/**
 * Tests for {@link BarSeriesManager#getHoldingCostModel()}.
 *
 * <p>
 * The tests cover the following scenarios (derived from the supplied JSON
 * test‑case description):
 * <ul>
 *   <li>Default constructor – the holding cost model must be a {@link ZeroCostModel}.</li>
 *   <li>Constructor with an explicit holding‑cost model – the same instance must be returned.</li>
 *   <li>Explicit <code>null</code> holding‑cost model – the getter must return {@code null}.</li>
 *   <li>Custom user‑defined {@link CostModel} implementation – the exact instance must be returned.</li>
 *   <li>Mockito‑mocked {@link CostModel} – the mock must be returned unchanged.</li>
 *   <li>Reference reassignment after construction – the manager must keep the original reference.</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_getHoldingCostModel_Test {

    // -----------------------------------------------------------------------
    // Mocks and objects used in the tests
    // -----------------------------------------------------------------------
    @Mock
    BarSeries barSeries;                 // mocked BarSeries – no real data needed

    @Mock
    CostModel mockedHoldingCostModel;    // a Mockito mock for the holding‑cost model

    BarSeriesManager manager;            // instance under test

    // -----------------------------------------------------------------------
    // Lifecycle callbacks (kept unchanged as required)
    // -----------------------------------------------------------------------
    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Each test creates its own manager instance.
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
        // GIVEN: a manager built with a custom holding‑cost model (mocked)
        manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),          // transaction cost model (default)
                mockedHoldingCostModel        // holding cost model under test
        );

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the exact instance supplied at construction is returned
        assertSame(mockedHoldingCostModel, result,
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

    @Test
    void whenHoldingCostModelIsNull_thenGetterReturnsNull() {
        // GIVEN: a manager constructed with a null holding‑cost model
        manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(), // transaction cost model (any non‑null value)
                null                 // explicit null holding cost model
        );

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the getter must return null
        assertNull(result, "When the holding cost model is constructed as null, getHoldingCostModel() should return null.");
    }

    @Test
    void whenCustomCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN: a user‑defined CostModel implementation (anonymous class)
        CostModel customCostModel = new CostModel() {
            @Override
            public Num calculate(Num price, Num amount) {
                // Simple stub implementation – just return the price.
                return price;
            }
        };

        manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),
                customCostModel
        );

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the exact custom instance must be returned
        assertSame(customCostModel, result,
                "The getter should return the exact custom CostModel instance supplied to the constructor.");
    }

    @Test
    void whenMockedCostModelProvided_thenGetterReturnsMockInstance() {
        // GIVEN: a Mockito‑mocked CostModel (already injected via @Mock field)
        manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),
                mockedHoldingCostModel
        );

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the mock instance must be returned unchanged
        assertSame(mockedHoldingCostModel, result,
                "The getter should return the mocked CostModel instance without wrapping or copying it.");
    }

    @Test
    void whenReferenceReassignedAfterConstruction_thenOriginalInstanceIsReturned() {
        // GIVEN: an original CostModel instance
        CostModel original = new CostModel() {
            @Override
            public Num calculate(Num price, Num amount) {
                return price;
            }
        };
        // Hold the reference in a variable that will later be reassigned
        CostModel holder = original;

        manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),
                holder
        );

        // Reassign the external variable to a different object
        holder = new ZeroCostModel(); // any other CostModel

        // WHEN: the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN: the manager must still hold the original reference
        assertSame(original, result,
                "BarSeriesManager should keep the original CostModel reference even if the external variable is later reassigned.");
    }
}
```