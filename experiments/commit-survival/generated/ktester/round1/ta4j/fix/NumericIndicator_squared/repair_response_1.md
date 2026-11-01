```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.time.ZonedDateTime;

/**
 * Tests for {@link NumericIndicator#squared()}.
 *
 * The original test suite used JUnit 5 and Mockito.  In this kata only JUnit 4
 * (and the core ta4j library) are available, therefore the test has been
 * rewritten to rely solely on JUnit 4 and on simple hand‑crafted stub
 * {@link Indicator} implementations.
 */
public class NumericIndicator_squared_Test {

    /** Series used in most tests. */
    private static BarSeries series;

    /** Simple delegate indicator (close price). */
    private static ClosePriceIndicator closePriceIndicator;

    /** NumericIndicator wrapping the delegate. */
    private NumericIndicator numericIndicator;

    @BeforeClass
    public static void setupBeforeAll() {
        // Build a series with the same 20 values used in the original test suite.
        series = new BaseBarSeriesBuilder()
                .withName("test")
                .build();

        int[] raw = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2 };
        for (int i = 0; i < raw.length; i++) {
            Num n = DecimalNum.valueOf(raw[i]);
            // all price fields are the same; volume is zero
            series.addBar(
                    ZonedDateTime.now().plusDays(i), // time
                    n, // open
                    n, // high
                    n, // low
                    n, // close
                    n, // volume
                    DecimalNum.ZERO); // amount
        }

        closePriceIndicator = new ClosePriceIndicator(series);
    }

    @Before
    public void setupBeforeEach() {
        // fresh NumericIndicator for each test method
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    @Test
    public void squaredReturnsNewInstance() {
        NumericIndicator squared = numericIndicator.squared();
        Assert.assertNotNull("squared() must not return null", squared);
        Assert.assertNotSame("squared() should return a new NumericIndicator instance",
                numericIndicator, squared);
    }

    @Test
    public void squaredValuesAreCorrect() {
        NumericIndicator squared = numericIndicator.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = closePriceIndicator.getValue(i).multipliedBy(closePriceIndicator.getValue(i));
            Num actual = squared.getValue(i);
            Assert.assertEquals("Incorrect squared value at index " + i, expected, actual);
        }
    }

    @Test
    public void squaredWorksWithNegativeAndZeroValues() {
        NumericIndicator squared = numericIndicator.squared();
        int[] indices = { 0, 8, 15, 16, 18, 19 };
        for (int i : indices) {
            Num value = closePriceIndicator.getValue(i);
            Num expected = value.multipliedBy(value);
            Num actual = squared.getValue(i);
            Assert.assertEquals("Squared value mismatch at index " + i + " (value=" + value + ")",
                    expected, actual);
        }
    }

    @Test
    public void delegateOfSquaredIsAnIndicator() {
        NumericIndicator squared = numericIndicator.squared();
        Indicator<Num> delegate = squared.delegate();
        Assert.assertNotNull("Delegate of the squared indicator must not be null", delegate);
        Assert.assertTrue("Delegate of squared() should be a NumericIndicator",
                delegate instanceof NumericIndicator);
    }

    @Test
    public void squaredDoesNotMutateOriginalIndicator() {
        Num[] originalValues = new Num[series.getBarCount()];
        for (int i = 0; i < series.getBarCount(); i++) {
            originalValues[i] = numericIndicator.getValue(i);
        }
        numericIndicator.squared(); // should not mutate
        for (int i = 0; i < series.getBarCount(); i++) {
            Assert.assertEquals("Original indicator mutated at index " + i,
                    originalValues[i], numericIndicator.getValue(i));
        }
    }

    @Test
    public void doubleSquaredYieldsFourthPower() {
        NumericIndicator firstSquare = numericIndicator.squared();
        NumericIndicator fourthPower = firstSquare.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.pow(4);
            Num actual = fourthPower.getValue(i);
            Assert.assertEquals("Fourth‑power mismatch at index " + i, expected, actual);
        }
    }

    @Test
    public void compositionWithMultipliedByWorksCorrectly() {
        NumericIndicator multiplied = numericIndicator.multipliedBy(2);
        NumericIndicator composed = multiplied.squared();
        for (int i = 0; i < series.getBarCount(); i++) {
            Num x = closePriceIndicator.getValue(i);
            Num expected = x.multipliedBy(x).multipliedBy(DecimalNum.valueOf(4));
            Num actual = composed.getValue(i);
            Assert.assertEquals("Composed square mismatch at index " + i, expected, actual);
        }
    }

    @Test
    public void squaredPropagatesBarSeriesReference() {
        NumericIndicator squared = numericIndicator.squared();
        Assert.assertSame("BarSeries reference should be propagated unchanged by squared()",
                series, squared.getBarSeries());
    }

    @Test
    public void squaredPropagatesUnstableBarsCount() {
        // ClosePriceIndicator has 0 unstable bars.
        NumericIndicator squared = numericIndicator.squared();
        Assert.assertEquals("Unstable bars count must be identical after squared()",
                numericIndicator.getUnstableBars(), squared.getUnstableBars());
    }

    @Test
    public void emptySeriesThrowsIndexOutOfBoundsOnGetValue() {
        BarSeries emptySeries = new BaseBarSeriesBuilder()
                .withName("empty")
                .build(); // no bars added
        ClosePriceIndicator emptyDelegate = new ClosePriceIndicator(emptySeries);
        NumericIndicator emptyNumeric = NumericIndicator.of(emptyDelegate);
        NumericIndicator squared = emptyNumeric.squared();
        try {
            squared.getValue(0);
            Assert.fail("Accessing any index on an empty series must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    @Test
    public void largeNumberDoesNotOverflow() {
        // 1e10
        final Num large = DecimalNum.valueOf("10000000000");
        // simple stub indicator returning the large number
        Indicator<Num> largeIndicator = new Indicator<Num>() {
            @Override public Num getValue(int index) { return large; }
            @Override public BarSeries getBarSeries() { return series; }
            @Override public int getUnstableBars() { return 0; }
        };
        NumericIndicator indicator = NumericIndicator.of(largeIndicator);
        NumericIndicator squared = indicator.squared();
        Num expected = large.multipliedBy(large); // 1e20
        Num actual = squared.getValue(0);
        Assert.assertEquals("Large number squaring produced unexpected result", expected, actual);
    }

    @Test
    public void fractionalNumbersPreservePrecision() {
        // Series: [1.5, -2.3]
        BarSeries fractionalSeries = new BaseBarSeriesBuilder()
                .withName("fractional")
                .build();
        fractionalSeries.addBar(ZonedDateTime.now(),
                DecimalNum.valueOf(1.5), DecimalNum.valueOf(1.5),
                DecimalNum.valueOf(1.5), DecimalNum.valueOf(1.5),
                DecimalNum.valueOf(1.5), DecimalNum.ZERO);
        fractionalSeries.addBar(ZonedDateTime.now().plusDays(1),
                DecimalNum.valueOf(-2.3), DecimalNum.valueOf(-2.3),
                DecimalNum.valueOf(-2.3), DecimalNum.valueOf(-2.3),
                DecimalNum.valueOf(-2.3), DecimalNum.ZERO);
        ClosePriceIndicator fractionalDelegate = new ClosePriceIndicator(fractionalSeries);
        NumericIndicator indicator = NumericIndicator.of(fractionalDelegate);
        NumericIndicator squared = indicator.squared();

        Num[] expected = {
                DecimalNum.valueOf(1.5).multipliedBy(DecimalNum.valueOf(1.5)),   // 2.25
                DecimalNum.valueOf(-2.3).multipliedBy(DecimalNum.valueOf(-2.3)) // 5.29
        };
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals("Fractional squaring mismatch at index " + i,
                    expected[i], squared.getValue(i));
        }
    }

    @Test
    public void nullDelegateCausesNullPointerException() throws Exception {
        // Create a normal NumericIndicator and then force its delegate to null via reflection.
        NumericIndicator normal = NumericIndicator.of(closePriceIndicator);
        java.lang.reflect.Field delegateField = NumericIndicator.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        delegateField.set(normal, null);

        try {
            normal.squared();
            Assert.fail("Calling squared() on a NumericIndicator with a null delegate should throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void delegateReturnsNullNumThrowsNPE() {
        Indicator<Num> nullNumDelegate = new Indicator<Num>() {
            @Override public Num getValue(int index) { return null; }
            @Override public BarSeries getBarSeries() { return series; }
            @Override public int getUnstableBars() { return 0; }
        };
        NumericIndicator indicator = NumericIndicator.of(nullNumDelegate);
        NumericIndicator squared = indicator.squared();
        try {
            squared.getValue(0);
            Assert.fail("Squaring when delegate returns null Num must throw NPE");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void delegateRuntimeExceptionIsPropagated() {
        Indicator<Num> explodingDelegate = new Indicator<Num>() {
            @Override public Num getValue(int index) { throw new ArithmeticException("boom"); }
            @Override public BarSeries getBarSeries() { return series; }
            @Override public int getUnstableBars() { return 0; }
        };
        NumericIndicator indicator = NumericIndicator.of(explodingDelegate);
        NumericIndicator squared = indicator.squared();
        try {
            squared.getValue(0);
            Assert.fail("Exception from delegate should propagate unchanged");
        } catch (ArithmeticException ex) {
            Assert.assertEquals("boom", ex.getMessage());
        }
    }

    @Test
    public void mixedValuesAreHandledCorrectly() {
        // Series: [0, -3, 5, -1.2]
        BarSeries mixedSeries = new BaseBarSeriesBuilder()
                .withName("mixed")
                .build();
        Num[] vals = {
                DecimalNum.valueOf(0),
                DecimalNum.valueOf(-3),
                DecimalNum.valueOf(5),
                DecimalNum.valueOf(-1.2)
        };
        for (int i = 0; i < vals.length; i++) {
            mixedSeries.addBar(
                    ZonedDateTime.now().plusDays(i),
                    vals[i], vals[i], vals[i], vals[i],
                    vals[i], DecimalNum.ZERO);
        }
        ClosePriceIndicator mixedDelegate = new ClosePriceIndicator(mixedSeries);
        NumericIndicator indicator = NumericIndicator.of(mixedDelegate);
        NumericIndicator squared = indicator.squared();

        Num[] expected = {
                DecimalNum.valueOf(0).multipliedBy(DecimalNum.valueOf(0)),       // 0
                DecimalNum.valueOf(-3).multipliedBy(DecimalNum.valueOf(-3)),    // 9
                DecimalNum.valueOf(5).multipliedBy(DecimalNum.valueOf(5)),      // 25
                DecimalNum.valueOf(-1.2).multipliedBy(DecimalNum.valueOf(-1.2)) // 1.44
        };
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals("Mixed value squaring mismatch at index " + i,
                    expected[i], squared.getValue(i));
        }
    }
}
```