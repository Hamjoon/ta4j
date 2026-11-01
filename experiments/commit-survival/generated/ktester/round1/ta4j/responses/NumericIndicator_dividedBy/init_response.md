```java
package org.ta4j.core.indicators.numeric;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.TestUtils.assertNumEquals;

import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#dividedBy(Indicator)}.
 */
@ExtendWith(MockitoExtension.class)
class NumericIndicator_dividedBy_Test {

    /** a simple number‑to‑Num mapper used by {@link MockBarSeries} */
    private static final Function<Number, Num> NUM_FUNCTION = n -> DecimalNum.valueOf(n.doubleValue());

    private static BarSeries series;
    private static ClosePriceIndicator closePrice;
    private static EMAIndicator ema;

    @BeforeAll
    static void setupBeforeAll() {
        // a short series – the values are not important, they just need to be reproducible
        series = new MockBarSeries(NUM_FUNCTION,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        ema = new EMAIndicator(closePrice, 3);
    }

    @BeforeEach
    void setupBeforeEach() {
        // nothing to initialise per‑test at the moment
    }

    @AfterEach
    void teardownAfterEach() {
        // nothing to clean up per‑test at the moment
    }

    @AfterAll
    static void teardownAfterAll() {
        // no global resources to release
    }

    @Test
    void dividedByConstantIndicator() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> constantFive = new ConstantIndicator<>(series, DecimalNum.valueOf(5));

        // when
        NumericIndicator result = numeric.dividedBy(constantFive);

        // then – verify a few sample points
        assertNumEquals(closePrice.getValue(0).dividedBy(DecimalNum.valueOf(5)),
                result.getValue(0));
        assertNumEquals(closePrice.getValue(8).dividedBy(DecimalNum.valueOf(5)),
                result.getValue(8));
    }

    @Test
    void dividedByDynamicIndicator() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> divisor = ema; // EMAIndicator implements Indicator<Num>

        // when
        NumericIndicator result = numeric.dividedBy(divisor);

        // then – verify a few sample points
        assertNumEquals(closePrice.getValue(0).dividedBy(ema.getValue(0)),
                result.getValue(0));
        assertNumEquals(closePrice.getValue(8).dividedBy(ema.getValue(8)),
                result.getValue(8));
    }

    @Test
    void delegateIsCorrectAfterDivision() {
        // given
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        Indicator<Num> divisor = new ConstantIndicator<>(series, DecimalNum.valueOf(2));

        // when
        NumericIndicator result = numeric.dividedBy(divisor);

        // then – the delegate of the resulting indicator must be a BinaryOperation (quotient)
        // we only assert that the delegate is not the original one and that it implements Indicator
        assertNotSame(numeric.delegate(), result.delegate(),
                "The delegate of the divided indicator should be a new instance");
        assertTrue(result.delegate() instanceof Indicator,
                "The delegate must still be an Indicator");
    }
}
```