```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link NumericIndicator#sma(int)}.
 *
 * The original test mixed JUnit 4 and JUnit 5 APIs. This version has been
 * rewritten to use only JUnit 4, fixing compilation errors and keeping the
 * original test intent.
 */
@RunWith(JUnit4.class)
public class NumericIndicator_sma_Test {

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private NumericIndicator numericIndicator;

    /**
     * Simple indicator whose {@code getBarSeries()} returns {@code null}.
     */
    private static class BadIndicator implements Indicator<Num> {
        @Override
        public Num getValue(int index) {
            return DecimalNum.ZERO;
        }

        @Override
        public BarSeries getBarSeries() {
            return null;
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }
    }

    /**
     * Helper to convert a CSV string into an array of {@link Num}.
     */
    private static Num[] toNumArray(String csv) {
        String[] parts = csv.split("\\s*,\\s*");
        Num[] nums = new Num[parts.length];
        for (int i = 0; i < parts.length; i++) {
            nums[i] = DecimalNum.valueOf(parts[i]);
        }
        return nums;
    }

    /**
     * Helper to build a series from primitive double values.
     */
    private static BarSeries buildSeries(double... values) {
        DecimalNum[] nums = new DecimalNum[values.length];
        for (int i = 0; i < values.length; i++) {
            nums[i] = DecimalNum.valueOf(values[i]);
        }
        return new BaseBarSeriesBuilder()
                .withName("mock")
                .withNumTypeOf(DecimalNum.class)
                .withNumValues(nums)
                .build();
    }

    /**
     * Helper to build a short series used in a few tests.
     */
    private static BarSeries buildShortSeries(double... values) {
        DecimalNum[] nums = new DecimalNum[values.length];
        for (int i = 0; i < values.length; i++) {
            nums[i] = DecimalNum.valueOf(values[i]);
        }
        return new BaseBarSeriesBuilder()
                .withName("short")
                .withNumTypeOf(DecimalNum.class)
                .withNumValues(nums)
                .build();
    }

    @Before
    public void setUp() {
        // deterministic series – values are simple integers for easy manual verification.
        series = buildSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @Test
    public void smaCreatesCorrectDelegate() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        Indicator<Num> delegate = sma.delegate();
        assertEquals(SMAIndicator.class, delegate.getClass());
    }

    @Test
    public void smaValuesMatchDirectSMAIndicator() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        SMAIndicator directSma = new SMAIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = directSma.getValue(i);
            Num actual = sma.getValue(i);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void smaHandlesInsufficientDataGracefully() {
        int barCount = 5;
        NumericIndicator sma = numericIndicator.sma(barCount);
        SMAIndicator directSma = new SMAIndicator(closePrice, barCount);
        for (int i = 0; i < barCount - 1; i++) {
            Num expected = directSma.getValue(i);
            Num actual = sma.getValue(i);
            assertTrue(expected instanceof NaN);
            assertTrue(actual instanceof NaN);
        }
    }

    @Test
    public void smaInvalidBarCount_ThrowsException() {
        int[] invalid = {0, -3};
        for (int bc : invalid) {
            try {
                numericIndicator.sma(bc);
                fail("Expected IllegalArgumentException for barCount = " + bc);
            } catch (IllegalArgumentException ignored) {
                // expected
            }
        }
    }

    @Test
    public void smaDelegateIntegrity() {
        int barCount = 10;
        NumericIndicator sma = numericIndicator.sma(barCount);
        Indicator<Num> delegate = sma.delegate();
        assertTrue(delegate instanceof SMAIndicator);
        assertSame(series, delegate.getBarSeries());
    }

    @Test
    public void smaBarCountLargerThanSeriesLength() {
        BarSeries shortSeries = buildShortSeries(10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        int barCount = 10;
        NumericIndicator sma = base.sma(barCount);
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(20),
                DecimalNum.valueOf(25)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i));
        }
    }

    @Test
    public void smaBarCountEqualToSeriesLength() {
        BarSeries shortSeries = buildShortSeries(10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        int barCount = 4;
        NumericIndicator sma = base.sma(barCount);
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(15),
                DecimalNum.valueOf(20),
                DecimalNum.valueOf(25)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i));
        }
    }

    @Test
    public void smaMultipleIndependentInvocations() {
        int barCount = 3;
        NumericIndicator sma1 = numericIndicator.sma(barCount);
        NumericIndicator sma2 = numericIndicator.sma(barCount);
        assertNotSame(sma1, sma2);
        assertNotSame(sma1.delegate(), sma2.delegate());
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(sma1.getValue(i), sma2.getValue(i));
        }
    }

    @Test
    public void smaDoesNotMutateOriginalIndicator() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        // force evaluation
        sma.getValue(series.getBarCount() - 1);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), numericIndicator.getValue(i));
        }
    }

    @Test
    public void smaToStringContainsDelegateInfo() {
        int barCount = 5;
        NumericIndicator sma = numericIndicator.sma(barCount);
        String str = sma.toString();
        assertTrue(str.contains("SMAIndicator"));
        assertTrue(str.contains(String.valueOf(barCount)));
    }

    @Test
    public void nestedSmaCompositionProducesDoubleSmoothedSeries() {
        BarSeries shortSeries = buildShortSeries(10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        // first SMA with period 3 -> [10, 15, 20, 30]
        NumericIndicator firstSma = base.sma(3);
        // second SMA with period 2 -> [10, 12.5, 17.5, 25]
        NumericIndicator secondSma = firstSma.sma(2);
        Num[] expected = {
                DecimalNum.valueOf(10),
                DecimalNum.valueOf(12.5),
                DecimalNum.valueOf(17.5),
                DecimalNum.valueOf(25)
        };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], secondSma.getValue(i));
        }
    }

    @Test
    public void smaWithNullDelegateThrowsNPE() {
        NumericIndicator nullWrapped = NumericIndicator.of(null);
        try {
            nullWrapped.sma(5);
            fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
            // expected
        }
    }

    @Test
    public void smaWithDelegateHavingNullBarSeriesThrowsException() {
        Indicator<Num> bad = new BadIndicator();
        NumericIndicator wrapper = NumericIndicator.of(bad);
        try {
            wrapper.sma(3);
            fail("Expected RuntimeException");
        } catch (RuntimeException ignored) {
            // expected
        }
    }

    @Test
    public void smaPropagatesRuntimeExceptionFromUnderlyingIndicator() {
        // Indicator that throws IllegalStateException for index >= 2
        Indicator<Num> flaky = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                if (index >= 2) {
                    throw new IllegalStateException("Deliberate failure at index " + index);
                }
                return DecimalNum.valueOf(index + 1);
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
        NumericIndicator wrapper = NumericIndicator.of(flaky);
        NumericIndicator sma = wrapper.sma(4);
        // indices 0 and 1 should work
        sma.getValue(0);
        sma.getValue(1);
        // index 2 must propagate the exception
        try {
            sma.getValue(2);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Deliberate failure"));
        }
    }

    @Test
    public void smaValidBarCount_ComputesExpectedValues_case1() {
        int barCount = 5;
        String csv = "3.0,4.0,5.0,6.0,7.0,8.0,9.0,9.0,8.0,7.0,6.0,5.0,4.0,3.0,2.0,1.0,0.0,-0.5,-1.5";
        Num[] expected = toNumArray(csv);
        NumericIndicator sma = numericIndicator.sma(barCount);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Unexpected SMA value at index " + i + " for barCount=" + barCount,
                    expected[i], sma.getValue(i));
        }
    }

    @Test
    public void smaValidBarCount_ComputesExpectedValues_case2() {
        int barCount = 1;
        String csv = "1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,8.0,7.0,6.0,5.0,4.0,3.0,2.0,1.0,0.0,-1.0,-2.0";
        Num[] expected = toNumArray(csv);
        NumericIndicator sma = numericIndicator.sma(barCount);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Unexpected SMA value at index " + i + " for barCount=" + barCount,
                    expected[i], sma.getValue(i));
        }
    }
}
```