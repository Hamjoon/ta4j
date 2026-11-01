package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.Position;
import org.ta4j.core.BaseBarSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class BarSeriesManager_getHoldingCostModel_Test {

    //     @Mock
    BarSeries // mocked BarSeries – no real data needed
    barSeries;

    //     @Mock
    CostModel // a custom holding‑cost model to be injected
    customHoldingCostModel;

    // instance under test
    BarSeriesManager manager;

    //     @Mock
    CostModel // a Mockito mock for the holding‑cost model
    mockedHoldingCostModel;

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
    @BeforeEach
    void setUp() {
        // Initialise a minimal BarSeries instance.
        this.barSeries = new BaseBarSeries("test-series");
    }

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
        assertSame(mockCostModel, result, "The getter should return the provided CostModel instance without wrapping or copying it.");
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
