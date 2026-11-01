```java
package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Unit tests for {@link BarSeriesManager#getHoldingCostModel()}.
 */
public class BarSeriesManager_getHoldingCostModel_Test {

    /**
     * Simple {@link CostModel} implementation used in the tests.
     */
    private static class SimpleCostModel implements CostModel {

        @Override
        public Num calculate(Num price, Num amount) {
            // Very simple implementation – just return the price.
            return price;
        }

        /**
         * The {@code CostModel} interface in ta4j declares a custom {@code equals}
         * method that takes another {@code CostModel}. For the purpose of the
         * tests we implement it as reference equality.
         */
        @Override
        public boolean equals(CostModel other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        // The interface also defines an abstract {@code calculate(Position)} method.
        // The tests never invoke it, so a trivial implementation returning {@code null}
        // is sufficient.
        @Override
        public Num calculate(Position position) {
            return null;
        }

        // Some newer versions of ta4j add a method with a different signature.
        // Providing a stub implementation satisfies the compiler.
        @Override
        public Num calculate(Position position, int amount) {
            return null;
        }
    }

    private BarSeries barSeries;

    @Before
    public void setUp() {
        // A minimal concrete BarSeries implementation.
        this.barSeries = new BaseBarSeries("test-series");
    }

    @Test
    public void whenHoldingCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a manager built with an explicit holding‑cost model
        CostModel explicitHoldingCost = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),
                explicitHoldingCost);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the exact instance supplied at construction is returned
        assertSame(explicitHoldingCost, result);
    }

    @Test
    public void whenUsingDefaultConstructor_thenHoldingCostModelIsZeroCostModel() {
        // GIVEN a manager created with the single‑argument constructor (defaults to ZeroCostModel)
        BarSeriesManager manager = new BarSeriesManager(barSeries);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the result should be an instance of ZeroCostModel
        assertTrue(result instanceof ZeroCostModel);
    }

    @Test
    public void whenHoldingCostModelIsNull_thenGetterReturnsNull() {
        // GIVEN a manager constructed with a null holding‑cost model
        BarSeriesManager manager = new BarSeriesManager(
                barSeries,
                new ZeroCostModel(),
                null);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the getter must return null
        assertNull(result);
    }

    @Test
    public void whenCustomCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a user‑defined CostModel implementation
        CostModel customCostModel = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), customCostModel);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the exact custom instance must be returned
        assertSame(customCostModel, result);
    }

    @Test
    public void whenMockedCostModelProvided_thenGetterReturnsMockInstance() {
        // In this context we use another SimpleCostModel instance as a stand‑in for a mock.
        CostModel mockCostModel = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries, new ZeroCostModel(), mockCostModel);
        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();
        // THEN the same instance must be returned unchanged
        assertSame("The getter should return the provided CostModel instance without wrapping or copying it.",
                mockCostModel, result);
    }

    @Test
    public void whenReferenceReassignedAfterConstruction_thenOriginalInstanceIsReturned() {
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