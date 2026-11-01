package org.ta4j.core.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;

// import static org.junit.jupiter.api.Assertions.*;
// import org.mockito.junit.MockitoJUnitRunner;
class BarSeriesManager_getBarSeries_Test {

    //     @Mock
    private BarSeries mockBarSeries;

    /**
     * Instance under test.
     */
    private BarSeriesManager manager;

    /**
     * Simple {@link BarSeries} implementation used for the majority of tests.
     */
    private BarSeries simpleSeries;

    /**
     * A minimal concrete subclass of {@link BaseBarSeries} used to verify that
     * custom subclasses are preserved by {@link BarSeriesManager}.
     */
    private static class MyBarSeries extends BaseBarSeries {

        MyBarSeries() {
            super("mySeries");
        }
    }

    //     @BeforeEach
    void setupBeforeEach() {
        // Use a plain BaseBarSeries instance – it is sufficient for the tests.
        simpleSeries = new BaseBarSeries("simpleSeries");
        manager = new BarSeriesManager(simpleSeries);
    }

    //     @AfterEach
    void teardownAfterEach() {
        manager = null;
        simpleSeries = null;
    }

//     @Test
//     void getBarSeriesReturnsExactInstanceProvidedInConstructor() {
//         BarSeries result = manager.getBarSeries();
//         assertSame(simpleSeries, result, "The manager should return the exact BarSeries instance supplied to its constructor.");
//     }

//     @Test
//     void getBarSeriesReturnsNullWhenConstructedWithNullSeries() {
//         BarSeriesManager nullManager = new BarSeriesManager(null);
//         BarSeries result = nullManager.getBarSeries();
//         assertNull(result, "When the manager is constructed with a null BarSeries, getBarSeries() should return null.");
//     }

//     @Test
//     void getBarSeriesPreservesCustomSubclassInstance() {
//         MyBarSeries mySeries = new MyBarSeries();
//         BarSeriesManager customManager = new BarSeriesManager(mySeries);
//         BarSeries result = customManager.getBarSeries();
//         assertSame(mySeries, result, "The manager must return the exact custom subclass instance.");
//         assertTrue(result instanceof MyBarSeries, "The returned instance should be of type MyBarSeries.");
//     }
}