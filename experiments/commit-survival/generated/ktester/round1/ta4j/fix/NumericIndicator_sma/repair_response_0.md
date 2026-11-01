```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;

/**
 * Unit tests for {@link NumericIndicator#sma(int)}.
 *
 * The original test suite relied on JUnit 5 and Mockito which are not available
 * in the execution environment.  This rewritten version uses only JUnit 4
 * (which is present) and plain Java to keep the same logical checks while
 * remaining completely self‑contained.
 */
public class NumericIndicator_sma_Test {

    private BarSeries series;
    private ClosePriceIndicator closePrice;
    private NumericIndicator numericIndicator;

    @Before
    public void setUp() {
        // Deterministic series – values are simple integers for easy manual verification.
        series = new MockBarSeries(DecimalNum::valueOf,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
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
            assertEquals("Mismatch at index " + i, expected, actual);
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
            assertTrue("Direct SMAIndicator should return NaN for index " + i,
                    expected instanceof NaN);
            assertTrue("NumericIndicator.sma() should also return NaN for index " + i,
                    actual instanceof NaN);
        }
    }

    @Test
    public void smaInvalidBarCount_ThrowsException() {
        int[] invalid = {0, -3};
        for (int barCount : invalid) {
            try {
                numericIndicator.sma(barCount);
                fail("Expected IllegalArgumentException for barCount = " + barCount);
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
        BarSeries shortSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40);
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
        BarSeries shortSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40);
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
        sma.getValue(series.getBarCount() - 1); // force evaluation
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
        BarSeries shortSeries = new MockBarSeries(DecimalNum::valueOf, 10, 20, 30, 40);
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        NumericIndicator firstSma = base.sma(3);   // [10, 15, 20, 30]
        NumericIndicator secondSma = firstSma.sma(2); // [10, 12.5, 17.5, 25]
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
            fail("Expected NullPointerException when delegating from a null indicator");
        } catch (NullPointerException expected) {
            // ok
        }
    }

    /** Simple indicator whose {@code getBarSeries()} returns {@code null}. */
    private static class BadIndicator implements Indicator<Num> {
        @Override public Num getValue(int index) { return DecimalNum.ZERO; }
        @Override public BarSeries getBarSeries() { return null; }
        @Override public int getUnstableBars() { return 0; }
    }

    @Test
    public void smaWithDelegateHavingNullBarSeriesThrowsException() {
        Indicator<Num> bad = new BadIndicator();
        NumericIndicator wrapper = NumericIndicator.of(bad);
        try {
            wrapper.sma(3);
            fail("Expected RuntimeException when creating SMA with null BarSeries");
        } catch (RuntimeException expected) {
            // ok – the SMAIndicator constructor validates the series
        }
    }

    @Test
    public void smaPropagatesRuntimeExceptionFromUnderlyingIndicator() {
        // Indicator that throws IllegalStateException for index >= 2
        Indicator<Num> flaky = new Indicator<Num>() {
            @Override public Num getValue(int index) {
                if (index >= 2) {
                    throw new IllegalStateException("Deliberate failure at index " + index);
                }
                return DecimalNum.valueOf(index + 1);
            }
            @Override public BarSeries getBarSeries() { return series; }
            @Override public int getUnstableBars() { return 0; }
        };
        NumericIndicator wrapper = NumericIndicator.of(flaky);
        NumericIndicator sma = wrapper.sma(4);
        // indices 0 and 1 should work
        sma.getValue(0);
        sma.getValue(1);
        // index 2 must propagate the exception
        try {
            sma.getValue(2);
            fail("Expected IllegalStateException to be propagated");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Deliberate failure"));
        }
    }
}
```