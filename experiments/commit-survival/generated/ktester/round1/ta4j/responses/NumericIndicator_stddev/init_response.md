```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#stddev(int)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_stddev_Test {

    private static Function<Number, Num> numFunction;
    private BarSeries series;
    private ClosePriceIndicator closePriceIndicator;
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Use DecimalNum as the concrete Num implementation for the whole test suite
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
    }

    @BeforeEach
    void setupBeforeEach() {
        // Build a simple deterministic series (same data as other NumericIndicator tests)
        series = new MockBarSeries(numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @AfterEach
    void teardownAfterEach() {
        // No resources to release for this test
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required
    }

    @Test
    void stddevCreatesStandardDeviationIndicatorAndComputesCorrectValues() {
        // given: a NumericIndicator based on close price
        final int barCount = 3;

        // when: we create a stddev NumericIndicator
        NumericIndicator stddevNumeric = numericIndicator.stddev(barCount);

        // then: the underlying delegate must be a StandardDeviationIndicator
        Indicator<Num> delegate = stddevNumeric.delegate();
        assertTrue(delegate instanceof StandardDeviationIndicator,
                "Delegate should be an instance of StandardDeviationIndicator");

        // also verify that the computed values match a directly created StandardDeviationIndicator
        StandardDeviationIndicator expectedStdDev = new StandardDeviationIndicator(closePriceIndicator, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = expectedStdDev.getValue(i);
            Num actual = stddevNumeric.getValue(i);
            assertEquals(expected, actual,
                    "Standard deviation value mismatch at index " + i);
        }
    }
}
```