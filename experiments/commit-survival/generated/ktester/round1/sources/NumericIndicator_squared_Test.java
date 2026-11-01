package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ta4j.core.BaseBarSeriesBuilder;
import java.util.function.Function;
import static org.junit.Assert.*;
import org.junit.Assert;
import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_squared_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static final Function<Number, Num> NUM_FUNCTION = DecimalNum::valueOf;

    /**
     * Series used in all tests.
     */
    private static BarSeries series;

    /**
     * Simple delegate indicator (close price).
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * NumericIndicator wrapping the delegate.
     */
    private NumericIndicator numericIndicator;

    @BeforeAll
    static void setupBeforeAll() {
        // Build a series with the same 20 values used in the original test suite.
        series = new BaseBarSeriesBuilder().withName("test").build();
        int[] raw = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (int i = 0; i < raw.length; i++) {
            Num n = DecimalNum.valueOf(raw[i]);
            // all price fields are the same; volume is zero
            series.addBar(// duration (unused)
            Duration.ZERO, // time
            ZonedDateTime.now().plusDays(i), // open
            n, // high
            n, // low
            n, // close
            n, // volume
            n, // amount
            n, // bid/ask spread (ignored)
            n);
        }
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @BeforeEach
    void setupBeforeEach() {
        // fresh NumericIndicator for each test method
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @Test
    void squaredReturnsNewInstance() {
        NumericIndicator squared = numericIndicator.squared();
        assertNotNull("squared() must not return null", squared);
        assertNotSame("squared() should return a new NumericIndicator instance", numericIndicator, squared);
    }

    @Test
    void squaredValuesAreCorrect() {
        NumericIndicator squared = numericIndicator.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i).multipliedBy(closePriceIndicator.getValue(i));
            Num actual = squared.getValue(i);
            assertEquals("Incorrect squared value at index " + i, expected, actual);
        }
    }

    @Test
    void squaredWorksWithNegativeAndZeroValues() {
        NumericIndicator squared = numericIndicator.squared();
        int[] indices = { 0, 8, 15, 16, 18, 19 };
        for (int i : indices) {
            Num value = closePriceIndicator.getValue(i);
            Num expected = value.multipliedBy(value);
            Num actual = squared.getValue(i);
            assertEquals("Squared value mismatch at index " + i + " (value=" + value + ")", expected, actual);
        }
    }

    @Test
    void delegateOfSquaredIsAnIndicator() {
        NumericIndicator squared = numericIndicator.squared();
        Indicator<Num> delegate = squared.delegate();
        assertNotNull("Delegate of the squared indicator must not be null", delegate);
        assertTrue("Delegate of squared() should be a NumericIndicator", delegate instanceof NumericIndicator);
    }

    @Test
    void squaredDoesNotMutateOriginalIndicator() {
        Num[] originalValues = new Num[series.getBarCount()];
        for (int i = 0; i < series.getBarCount(); i++) {
            originalValues[i] = numericIndicator.getValue(i);
        }
        // should not mutate
        numericIndicator.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals("Original indicator mutated at index " + i, originalValues[i], numericIndicator.getValue(i));
        }
    }

    @Test
    void doubleSquaredYieldsFourthPower() {
        NumericIndicator firstSquare = numericIndicator.squared();
        NumericIndicator fourthPower = firstSquare.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.pow(4);
            Num actual = fourthPower.getValue(i);
            assertEquals("Fourth‑power mismatch at index " + i, expected, actual);
        }
    }

    @Test
    void compositionWithMultipliedByWorksCorrectly() {
        NumericIndicator multiplied = numericIndicator.multipliedBy(2);
        NumericIndicator composed = multiplied.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.multipliedBy(x).multipliedBy(DecimalNum.valueOf(4));
            Num actual = composed.getValue(i);
            assertEquals("Composed square mismatch at index " + i, expected, actual);
        }
    }

    @Test
    void squaredPropagatesBarSeriesReference() {
        NumericIndicator squared = numericIndicator.squared();
        assertSame("BarSeries reference should be propagated unchanged by squared()", series, squared.getBarSeries());
    }

    @Test
    void squaredPropagatesUnstableBarsCount() {
        // ClosePriceIndicator has 0 unstable bars.
        NumericIndicator squared = numericIndicator.squared();
        assertEquals("Unstable bars count must be identical after squared()", numericIndicator.getUnstableBars(), squared.getUnstableBars());
    }

    @Test
    void emptySeriesThrowsIndexOutOfBoundsOnGetValue() {
        // no bars added
        BarSeries emptySeries = new BaseBarSeriesBuilder().withName("empty").build();
        ClosePriceIndicator emptyDelegate = new ClosePriceIndicator(emptySeries);
        NumericIndicator emptyNumeric = NumericIndicator.of(emptyDelegate);
        NumericIndicator squared = emptyNumeric.squared();
        try {
            squared.getValue(0);
            fail("Accessing any index on an empty series must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    @ParameterizedTest(name = "large number {0} squared")
    // 1e10
    @ValueSource(strings = { "10000000000" })
    void largeNumberDoesNotOverflow(String valueStr) {
        // 1e10
        final Num large = DecimalNum.valueOf("10000000000");
        // simple stub indicator returning the large number
        Indicator<Num> largeIndicator = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                return large;
            }

            @Override
            public BarSeries getBarSeries() {
                return series;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        NumericIndicator indicator = NumericIndicator.of(largeIndicator);
        NumericIndicator squared = indicator.squared();
        // 1e20
        Num expected = large.multipliedBy(large);
        Num actual = squared.getValue(0);
        assertEquals("Large number squaring produced unexpected result", expected, actual);
    }

    @Test
    void fractionalNumbersPreservePrecision() {
        // Series: [1.5, -2.3]
        BarSeries fractionalSeries = new BaseBarSeriesBuilder().withName("fractional").build();
        fractionalSeries.addBar(Duration.ZERO, ZonedDateTime.now(), DecimalNum.valueOf(1.5), DecimalNum.valueOf(1.5), DecimalNum.valueOf(1.5), DecimalNum.valueOf(1.5), DecimalNum.valueOf(1.5), DecimalNum.ZERO);
        fractionalSeries.addBar(Duration.ZERO, ZonedDateTime.now().plusDays(1), DecimalNum.valueOf(-2.3), DecimalNum.valueOf(-2.3), DecimalNum.valueOf(-2.3), DecimalNum.valueOf(-2.3), DecimalNum.valueOf(-2.3), DecimalNum.ZERO);
        ClosePriceIndicator fractionalDelegate = new ClosePriceIndicator(fractionalSeries);
        NumericIndicator indicator = NumericIndicator.of(fractionalDelegate);
        NumericIndicator squared = indicator.squared();
        Num[] expected = { DecimalNum.valueOf(1.5).multipliedBy(DecimalNum.valueOf(1.5)), DecimalNum.valueOf(-2.3).multipliedBy(DecimalNum.valueOf(-2.3)) };
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Fractional squaring mismatch at index " + i, expected[i], squared.getValue(i));
        }
    }

    @Test
    void nullDelegateCausesNullPointerException() {
        // Create a normal NumericIndicator and then force its delegate to null via reflection.
        NumericIndicator normal = NumericIndicator.of(closePriceIndicator);
        java.lang.reflect.Field delegateField = NumericIndicator.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        delegateField.set(normal, null);
        try {
            normal.squared();
            fail("Calling squared() on a NumericIndicator with a null delegate should throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    void delegateReturnsNullNumThrowsNPE() {
        Indicator<Num> nullNumDelegate = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                return null;
            }

            @Override
            public BarSeries getBarSeries() {
                return series;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        NumericIndicator indicator = NumericIndicator.of(nullNumDelegate);
        NumericIndicator squared = indicator.squared();
        try {
            squared.getValue(0);
            fail("Squaring when delegate returns null Num must throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    void delegateRuntimeExceptionIsPropagated() {
        Indicator<Num> explodingDelegate = new Indicator<Num>() {

            @Override
            public Num getValue(int index) {
                throw new ArithmeticException("boom");
            }

            @Override
            public BarSeries getBarSeries() {
                return series;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }
        };
        NumericIndicator indicator = NumericIndicator.of(explodingDelegate);
        NumericIndicator squared = indicator.squared();
        try {
            squared.getValue(0);
            fail("Exception from delegate should propagate unchanged");
        } catch (ArithmeticException ex) {
            assertEquals("boom", ex.getMessage());
        }
    }

    @Test
    void mixedValuesAreHandledCorrectly() {
        // Series: [0, -3, 5, -1.2]
        BarSeries mixedSeries = new BaseBarSeriesBuilder().withName("mixed").build();
        Num[] vals = { DecimalNum.valueOf(0), DecimalNum.valueOf(-3), DecimalNum.valueOf(5), DecimalNum.valueOf(-1.2) };
        for (int i = 0; i < vals.length; i++) {
            mixedSeries.addBar(Duration.ZERO, ZonedDateTime.now().plusDays(i), vals[i], vals[i], vals[i], vals[i], vals[i], DecimalNum.ZERO);
        }
        ClosePriceIndicator mixedDelegate = new ClosePriceIndicator(mixedSeries);
        NumericIndicator indicator = NumericIndicator.of(mixedDelegate);
        NumericIndicator squared = indicator.squared();
        Num[] expected = { DecimalNum.valueOf(0).multipliedBy(DecimalNum.valueOf(0)), DecimalNum.valueOf(-3).multipliedBy(DecimalNum.valueOf(-3)), DecimalNum.valueOf(5).multipliedBy(DecimalNum.valueOf(5)), DecimalNum.valueOf(-1.2).multipliedBy(DecimalNum.valueOf(-1.2)) };
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Mixed value squaring mismatch at index " + i, expected[i], squared.getValue(i));
        }
    }
}
