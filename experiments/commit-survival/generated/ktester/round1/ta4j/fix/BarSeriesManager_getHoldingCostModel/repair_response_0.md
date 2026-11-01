```java
package org.ta4j.core.backtest;

import org.junit.Test;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * Unit tests for {@link BarSeriesManager#getHoldingCostModel()}.
 *
 * <p>The original test suite used JUnit 5 and Mockito, which are not available in the
 * execution environment.  This rewritten version relies only on JUnit 4 (which is
 * provided) and on concrete implementations of the required interfaces.</p>
 */
public class BarSeriesManager_getHoldingCostModel_Test {

    /** Simple {@link CostModel} implementation used in a few tests. */
    private static class SimpleCostModel implements CostModel {
        @Override
        public Num calculate(Num price, Num amount) {
            // Very simple implementation – just return the price.
            return price;
        }

        /** The {@code CostModel} interface in ta4j declares a custom {@code equals}
         *  method that takes another {@code CostModel}.  For the purpose of the tests
         *  we implement it as reference equality. */
        @Override
        public boolean equals(CostModel other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    // -----------------------------------------------------------------------
    // A (possibly null) BarSeries – the manager does not use it for the
    // getHoldingCostModel() tests, so we can safely pass {@code null}.
    private final BarSeries barSeries = null;

    // -----------------------------------------------------------------------
    @Test
    public void whenHoldingCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a manager built with an explicit holding‑cost model
        CostModel explicitHoldingCost = new SimpleCostModel();
        BarSeriesManager manager = new BarSeriesManager(barSeries,
                new ZeroCostModel(),          // transaction‑cost model (default)
                explicitHoldingCost);        // holding‑cost model under test

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
        BarSeriesManager manager = new BarSeriesManager(barSeries,
                new ZeroCostModel(),   // transaction‑cost model (any non‑null value)
                null);                 // explicit null holding‑cost model

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the getter must return null
        assertNull(result);
    }

    @Test
    public void whenCustomCostModelProvided_thenGetterReturnsSameInstance() {
        // GIVEN a user‑defined CostModel implementation
        CostModel customCostModel = new SimpleCostModel();

        BarSeriesManager manager = new BarSeriesManager(barSeries,
                new ZeroCostModel(),
                customCostModel);

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the exact custom instance must be returned
        assertSame(customCostModel, result);
    }

    @Test
    public void whenReferenceReassignedAfterConstruction_thenOriginalInstanceIsReturned() {
        // GIVEN an original CostModel instance
        CostModel original = new SimpleCostModel();

        // Hold the reference in a variable that will later be reassigned
        CostModel holder = original;
        BarSeriesManager manager = new BarSeriesManager(barSeries,
                new ZeroCostModel(),
                holder);

        // Reassign the external variable to a different object
        holder = new ZeroCostModel();

        // WHEN the holding cost model is retrieved
        CostModel result = manager.getHoldingCostModel();

        // THEN the manager must still hold the original reference
        assertSame(original, result);
    }
}
```