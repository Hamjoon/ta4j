```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Position;
import org.ta4j.core.BaseBarSeries;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit‑tests for {@link BarSeriesManager#getHoldingCostModel()}.
 *
 * <p>The original test class had all test methods commented out and the
 * {@code SimpleCostModel} implementation did not match the current
 * {@link CostModel} interface (it declared {@code equals(CostModel)} instead of
 * the standard {@code equals(Object)} method).  The class below restores the
 * tests, fixes the implementation and adds the required JUnit 5 imports.</p>
 */
class BarSeriesManager_getHoldingCostModel_Test {

    // -----------------------------------------------------------------------
    // Test fixtures
    // -----------------------------------------------------------------------
    BarSeries barSeries;                     // a minimal series – no real data needed
    BarSeriesManager manager;               // instance under test

    /**
     * Very small {@link CostModel} implementation used in the tests.
     *
     * <p>Only the {@link CostModel#calculate(Num, Num)} method is needed for the
     * current test suite; the other methods are supplied with trivial
     * implementations to satisfy the compiler.</p>
     */
    private static class SimpleCostModel implements CostModel {

        @Override
        public Num calculate(Num price, Num amount) {
            // For the purpose of the test we simply return the price.
            return price;
        }

        @Override
        public Num calculate(Position position) {
            // Not used in the tests – return a dummy value.
            return null;
        }

        @Override
        public Num calculate(Position position, int amount) {
            // Not used in the tests – return a dummy value.
            return null;
        }

        // equals and hashCode are inherited from Object; no custom implementation is required.
    }

    // -----------------------------------------------------------------------
    // Test initialisation
    // -----------------------------------------------------------------------
    @BeforeEach
    void setUp() {
        // Initialise a minimal BarSeries instance.
        this.barSeries = new BaseBarSeries("test-series");
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------
    @Test
    void whenHoldingCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a manager built with an explicit holding‑cost model
        CostModel explicitHoldingCost = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), explicitHoldingCost);

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the exact instance supplied at construction is returned
        assertSame(explicitHoldingCost, result);
    }

    @Test
    void whenUsingDefaultConstructor_thenHoldingCostModelIsZeroCostModel() {
        // GIVEN a manager created with the single‑argument constructor (defaults to ZeroCostModel)
        BarSeriesManager manager = new BarSeriesManager(barSeries);

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the result should be an instance of ZeroCostModel
        assertTrue(result instanceof ZeroCostModel);
    }

    @Test
    void whenHoldingCostModelIsNull_thenGetterReturnsNull() {
        // GIVEN a manager constructed with a null holding‑cost model
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), null);

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the getter must return null
        assertNull(result);
    }

    @Test
    void whenCustomCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a user‑defined CostModel implementation
        CostModel customCostModel = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), customCostModel);

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the exact custom instance must be returned
        assertSame(customCostModel, result);
    }

    @Test
    void whenMockedCostModelProvided_thenGetterReturnsMockInstance() {
        // In this context we use another SimpleCostModel instance as a stand‑in for a mock.
        CostModel mockCostModel = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), mockCostModel);

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the same instance must be returned unchanged
        assertSame(mockCostModel, result,
                "The getter should return the provided CostModel instance without wrapping or copying it.");
    }

    @Test
    void whenReferenceReassignedAfterConstruction_thenOriginalInstanceIsReturned() {
        // GIVEN an original CostModel instance
        CostModel original = new SimpleCostModel();

        // Hold the reference in a variable that will later be reassigned
        CostModel holder = original;
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), holder);

        // Reassign the external variable to a different object
        holder = new ZeroCostModel();

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the manager must still hold the original reference
        assertSame(original, result);
    }
}
```