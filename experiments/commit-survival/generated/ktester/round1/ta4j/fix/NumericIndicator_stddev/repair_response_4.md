```java
package org.ta4j.core.indicators.numeric;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#stddev(int)}.
 */
public class NumericIndicator_stddev_Test {

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
        series = createSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePriceIndicator = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    /* --------------------------------------------------------------------- */
    private BarSeries createSeries(double... values) {
        BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder().withName("test");
        BarSeries s = builder.build();
        for (double v : values) {
            // open, high, low, close = v; volume = 1.0
            s.addBar(new BaseBar(Duration.ofDays(1), ZonedDateTime.now(), v, v, v, v, 1.0));
        }
        return s;
    }

    @Test
    void stddevCreatesStandardDeviationIndicatorAndComputesCorrectValues() {
        final int barCount = 3;
        NumericIndicator stddevNumeric = numericIndicator.stddev(barCount);
        Indicator<Num> delegate = stddevNumeric.delegate();
        assertTrue(delegate instanceof StandardDeviationIndicator,
                "Delegate should be a StandardDeviationIndicator");
        StandardDeviationIndicator expectedStdDev = new StandardDeviationIndicator(closePriceIndicator, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = expectedStdDev.getValue(i);
            Num actual = stddevNumeric.getValue(i);
            assertEquals(expected, actual, "StdDev value mismatch at index " + i);
        }
    }

    @Test
    void stddevBarCountGreaterThanSeriesLengthReturnsNaN() {
        BarSeries tinySeries = createSeries(10, 20, 30, 40, 50);
        NumericIndicator tinyNumeric = NumericIndicator.of(new ClosePriceIndicator(tinySeries));
        // larger than series length (5)
        int barCount = 10;
        NumericIndicator stddev = tinyNumeric.stddev(barCount);
        for (int i = 0; i < tinySeries.getBarCount(); i++) {
            assertTrue(stddev.getValue(i).isNaN(),
                    "Expected NaN for index " + i + " when barCount > series length");
        }
    }

    @Test
    void stddevBarCountOneReturnsZeroForAllIndices() {
        BarSeries s = createSeries(10, 20, 30);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(1);
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(DecimalNum.ZERO, stddev.getValue(i),
                    "StdDev should be zero when barCount == 1");
        }
    }

    @Test
    void stddevExactSeriesLengthComputesCorrectValue() {
        // series: 2,4,6,8  (mean = 5, variance = 5, stddev = sqrt(5))
        BarSeries s = createSeries(2, 4, 6, 8);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(4);
        Num expected = DecimalNum.valueOf(Math.sqrt(5.0));
        // first complete window ends at index 3
        Num actual = stddev.getValue(3);
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-8)),
                "StdDev value does not match expected sqrt(5)");
    }

    @Test
    void stddevUnstableBarsFirstValuesAreNaN() {
        BarSeries s = createSeries(1, 5, 9);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(2);
        assertTrue(stddev.getValue(0).isNaN(),
                "First value should be NaN because the window is not yet full");
        // Expected stddev for window [1,5] -> mean 3, variance ((-2)^2+(2)^2)/2 = 4, stddev = 2
        Num expected = DecimalNum.valueOf(2.0);
        Num actual = stddev.getValue(1);
        assertTrue(actual.minus(expected).abs().isLessThan(DecimalNum.valueOf(1e-8)),
                "StdDev for window [1,5] should be 2.0");
    }

    @Test
    void stddevZeroVarianceWindowReturnsZero() {
        BarSeries s = createSeries(5, 5, 5, 6, 7);
        NumericIndicator ni = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator stddev = ni.stddev(3);
        // window [5,5,5] ends at index 2 → stddev = 0
        assertEquals(DecimalNum.ZERO, stddev.getValue(2),
                "StdDev should be zero for a zero‑variance window");
        // later windows are non‑zero; just check they are not NaN nor zero
        assertFalse(stddev.getValue(3).isNaN(), "StdDev at index 3 should not be NaN");
        assertFalse(stddev.getValue(4).isNaN(), "StdDev at index 4 should not be NaN");
    }

    @Test
    void stddevPropagatesUnstableBars() {
        int barCount = 3;
        NumericIndicator stddev = numericIndicator.stddev(barCount);
        assertEquals(barCount - 1, stddev.getUnstableBars(),
                "Unstable bars should be barCount - 1");
    }

    @Test
    void stddevDelegateIsStandardDeviationIndicator() {
        NumericIndicator stddev = numericIndicator.stddev(3);
        assertTrue(stddev.delegate() instanceof StandardDeviationIndicator,
                "Delegate must be a StandardDeviationIndicator");
    }

    @Test
    void stddevWithNullDelegateThrowsNullPointerException() {
        NumericIndicator nullWrapped = NumericIndicator.of(null);
        assertThrows(NullPointerException.class, () -> nullWrapped.stddev(3));
    }

    @Test
    void stddevWithIndicatorHavingNullSeriesThrowsNullPointerException() {
        // custom indicator that returns a valid value but null BarSeries
        Indicator<Num> badIndicator = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                return DecimalNum.valueOf(1);
            }

            @Override
            public BarSeries getBarSeries() {
                // deliberately broken
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "BadIndicator";
            }
        };
        NumericIndicator wrapped = NumericIndicator.of(badIndicator);
        assertThrows(NullPointerException.class, () -> wrapped.stddev(3));
    }
}
```