```java
package org.ta4j.core.indicators.numeric;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.TestUtils;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

/**
 * Tests for {@link NumericIndicator#dividedBy(Number)} and
 * {@link NumericIndicator#dividedBy(Indicator)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_dividedBy_2_Test {

    /** Series used for all tests */
    private BarSeries series;
    /** Close‑price indicator based on the series */
    private ClosePriceIndicator closePrice;
    /** EMA indicator used as a dynamic divisor */
    private EMAIndicator ema;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Simple numeric conversion using DecimalNum (the default Num implementation)
        Function<Number, Num> numFunction = n -> DecimalNum.valueOf(n.doubleValue());

        // A deterministic series – values are the same as in the original test suite
        series = new MockBarSeries(
                numFunction,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8,
                7, 6, 5, 4, 3, 2, 1, 0, -1, -2
        );

        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up per‑test at the moment
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global teardown required
    }

    @Test
    void dividedByNumber_staticDivisor() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // Divide by a constant number (2)
        NumericIndicator result = numeric.dividedBy(2);

        // Verify a few representative indices
        assertNumEquals(
                closePrice.getValue(0).dividedBy(DecimalNum.valueOf(2)),
                result.getValue(0)
        );
        assertNumEquals(
                closePrice.getValue(8).dividedBy(DecimalNum.valueOf(2)),
                result.getValue(8)
        );
        // Also test a negative value to ensure sign handling
        assertNumEquals(
                closePrice.getValue(18).dividedBy(DecimalNum.valueOf(2)),
                result.getValue(18)
        );
    }

    @Test
    void dividedByIndicator_dynamicDivisor() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        // Divide by another indicator (EMA)
        NumericIndicator result = numeric.dividedBy(ema);

        // Verify a few representative indices
        assertNumEquals(
                closePrice.getValue(0).dividedBy(ema.getValue(0)),
                result.getValue(0)
        );
        assertNumEquals(
                closePrice.getValue(8).dividedBy(ema.getValue(8)),
                result.getValue(8)
        );
        // Verify the last index as well
        assertNumEquals(
                closePrice.getValue(19).dividedBy(ema.getValue(19)),
                result.getValue(19)
        );
    }

    @Test
    void dividedByNumber_andDividedByIndicator_returnSameDelegateType() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);

        Indicator<Num> staticDiv = numeric.dividedBy(5);
        Indicator<Num> dynamicDiv = numeric.dividedBy(ema);

        // The returned objects should still be instances of NumericIndicator
        assertTrue(staticDiv instanceof NumericIndicator,
                "Static division should return a NumericIndicator");
        assertTrue(dynamicDiv instanceof NumericIndicator,
                "Dynamic division should return a NumericIndicator");
    }
}
```