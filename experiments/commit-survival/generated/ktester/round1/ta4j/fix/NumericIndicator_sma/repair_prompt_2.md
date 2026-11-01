
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.ValueSource;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
import org.junit.jupiter.params.provider.CsvSource;
import org.ta4j.core.mocks.MockBarSeries;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.Before;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_sma_Test {

    private BarSeries series;

    private ClosePriceIndicator closePrice;

    private NumericIndicator numericIndicator;

    /**
     * Converter that turns a comma‑separated list of numbers into an array of {@link Num}.
     */
    public static class NumArrayConverter extends SimpleArgumentConverter {

        @Override
        protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
            if (source instanceof String && targetType == Num[].class) {
                String[] parts = ((String) source).split("\\s*,\\s*");
                Num[] nums = new Num[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    nums[i] = DecimalNum.valueOf(parts[i]);
                }
                return nums;
            }
            throw new ArgumentConversionException("Cannot convert " + source + " to Num[]");
        }
    }

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

    @BeforeEach
    void setupBeforeEach() {
        // Deterministic series – values are simple integers for easy manual verification.
        series = new BaseBarSeriesBuilder().withName("mock").withNumTypeOf(DecimalNum.class).withValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2).build();
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }

    @Test
    void smaCreatesCorrectDelegate() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        Indicator<Num> delegate = sma.delegate();
        assertEquals(SMAIndicator.class, delegate.getClass());
    }

    @Test
    void smaValuesMatchDirectSMAIndicator() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        SMAIndicator directSma = new SMAIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            Num expected = directSma.getValue(i);
            Num actual = sma.getValue(i);
            assertEquals(expected, actual, "Mismatch at index " + i);
        }
    }

    @Test
    void smaHandlesInsufficientDataGracefully() {
        int barCount = 5;
        NumericIndicator sma = numericIndicator.sma(barCount);
        SMAIndicator directSma = new SMAIndicator(closePrice, barCount);
        for (int i = 0; i < barCount - 1; i++) {
            Num expected = directSma.getValue(i);
            Num actual = sma.getValue(i);
            assertTrue(expected instanceof NaN, "Direct SMAIndicator should return NaN for index " + i);
            assertTrue(actual instanceof NaN, "NumericIndicator.sma() should also return NaN for index " + i);
        }
    }

    @ParameterizedTest(name = "sma({0}) should compute correct SMA")
    @CsvSource({ "5, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -0.5, -1.5", "1, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0" })
    void smaValidBarCount_ComputesExpectedValues(int barCount, @ConvertWith(NumArrayConverter.class) Num[] expected) {
        NumericIndicator sma = numericIndicator.sma(barCount);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i), () -> "Unexpected SMA value at index " + i + " for barCount=" + barCount);
        }
    }

    @ParameterizedTest(name = "sma({0}) must throw IllegalArgumentException")
    @ValueSource(ints = { 0, -3 })
    void smaInvalidBarCount_ThrowsException(int barCount) {
        int[] invalid = { 0, -3 };
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
    void smaDelegateIntegrity() {
        int barCount = 10;
        NumericIndicator sma = numericIndicator.sma(barCount);
        Indicator<Num> delegate = sma.delegate();
        assertTrue(delegate instanceof SMAIndicator);
        assertSame(series, delegate.getBarSeries());
    }

    @Test
    void smaBarCountLargerThanSeriesLength() {
        BarSeries shortSeries = new BaseBarSeriesBuilder().withName("short").withNumTypeOf(DecimalNum.class).withValues(10, 20, 30, 40).build();
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        int barCount = 10;
        NumericIndicator sma = base.sma(barCount);
        Num[] expected = { DecimalNum.valueOf(10), DecimalNum.valueOf(15), DecimalNum.valueOf(20), DecimalNum.valueOf(25) };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i));
        }
    }

    @Test
    void smaBarCountEqualToSeriesLength() {
        BarSeries shortSeries = new BaseBarSeriesBuilder().withName("short").withNumTypeOf(DecimalNum.class).withValues(10, 20, 30, 40).build();
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        int barCount = 4;
        NumericIndicator sma = base.sma(barCount);
        Num[] expected = { DecimalNum.valueOf(10), DecimalNum.valueOf(15), DecimalNum.valueOf(20), DecimalNum.valueOf(25) };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sma.getValue(i));
        }
    }

    @Test
    void smaMultipleIndependentInvocations() {
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
    void smaDoesNotMutateOriginalIndicator() {
        int barCount = 3;
        NumericIndicator sma = numericIndicator.sma(barCount);
        // force evaluation
        sma.getValue(series.getBarCount() - 1);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), numericIndicator.getValue(i));
        }
    }

    @Test
    void smaToStringContainsDelegateInfo() {
        int barCount = 5;
        NumericIndicator sma = numericIndicator.sma(barCount);
        String str = sma.toString();
        assertTrue(str.contains("SMAIndicator"));
        assertTrue(str.contains(String.valueOf(barCount)));
    }

    @Test
    void nestedSmaCompositionProducesDoubleSmoothedSeries() {
        BarSeries shortSeries = new BaseBarSeriesBuilder().withName("short").withNumTypeOf(DecimalNum.class).withValues(10, 20, 30, 40).build();
        NumericIndicator base = NumericIndicator.of(new ClosePriceIndicator(shortSeries));
        // [10, 15, 20, 30]
        NumericIndicator firstSma = base.sma(3);
        // [10, 12.5, 17.5, 25]
        NumericIndicator secondSma = firstSma.sma(2);
        Num[] expected = { DecimalNum.valueOf(10), DecimalNum.valueOf(12.5), DecimalNum.valueOf(17.5), DecimalNum.valueOf(25) };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], secondSma.getValue(i));
        }
    }

    @Test
    void smaWithNullDelegateThrowsNPE() {
        NumericIndicator nullWrapped = NumericIndicator.of(null);
        assertThrows(NullPointerException.class, () -> nullWrapped.sma(5));
    }

    @Test
    void smaWithDelegateHavingNullBarSeriesThrowsException() {
        Indicator<Num> bad = new BadIndicator();
        NumericIndicator wrapper = NumericIndicator.of(bad);
        assertThrows(RuntimeException.class, () -> wrapper.sma(3));
    }

    @Test
    void smaPropagatesRuntimeExceptionFromUnderlyingIndicator() {
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
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> sma.getValue(2));
        assertTrue(ex.getMessage().contains("Deliberate failure"));
    }

    @Before
    public void setUp() {
        // Deterministic series – values are simple integers for easy manual verification.
        series = new MockBarSeries(DecimalNum::valueOf, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        numericIndicator = NumericIndicator.of(closePrice);
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:10: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:11: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:12: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.CsvSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:13: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:14: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.ValueSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:16: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.BeforeEach;
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:17: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.Test;
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:18: error: package org.junit.jupiter.params.converter does not exist
import org.junit.jupiter.params.converter.ArgumentConversionException;
                                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:19: error: package org.junit.jupiter.params.converter does not exist
import org.junit.jupiter.params.converter.ConvertWith;
                                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:20: error: package org.junit.jupiter.params.converter does not exist
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
                                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:21: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:22: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:23: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:25: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.CsvSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:26: error: package org.ta4j.core.mocks does not exist
import org.ta4j.core.mocks.MockBarSeries;
                          ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:27: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:28: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.ValueSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:32: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:30: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:44: error: cannot find symbol
    public static class NumArrayConverter extends SimpleArgumentConverter {
                                                  ^
  symbol:   class SimpleArgumentConverter
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:47: error: cannot find symbol
        protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
                                                                            ^
  symbol:   class ArgumentConversionException
  location: class NumArrayConverter
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:32: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:81: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:124: error: cannot find symbol
    void smaValidBarCount_ComputesExpectedValues(int barCount, @ConvertWith(NumArrayConverter.class) Num[] expected) {
                                                                ^
  symbol:   class ConvertWith
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:122: error: cannot find symbol
    @ParameterizedTest(name = "sma({0}) should compute correct SMA")
     ^
  symbol:   class ParameterizedTest
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:123: error: cannot find symbol
    @CsvSource({ "5, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -0.5, -1.5", "1, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0" })
     ^
  symbol:   class CsvSource
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:131: error: cannot find symbol
    @ParameterizedTest(name = "sma({0}) must throw IllegalArgumentException")
     ^
  symbol:   class ParameterizedTest
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:132: error: cannot find symbol
    @ValueSource(ints = { 0, -3 })
     ^
  symbol:   class ValueSource
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:46: error: method does not override or implement a method from a supertype
        @Override
        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:56: error: cannot find symbol
            throw new ArgumentConversionException("Cannot convert " + source + " to Num[]");
                      ^
  symbol:   class ArgumentConversionException
  location: class NumArrayConverter
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:84: error: cannot find symbol
        series = new BaseBarSeriesBuilder().withName("mock").withNumTypeOf(DecimalNum.class).withValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2).build();
                                                                                            ^
  symbol:   method withValues(int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int,int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:94: error: cannot find symbol
        assertEquals(SMAIndicator.class, delegate.getClass());
        ^
  symbol:   method assertEquals(Class<SMAIndicator>,Class<CAP#1>)
  location: class NumericIndicator_sma_Test
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Indicator from capture of ? extends Indicator
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:105: error: cannot find symbol
            assertEquals(expected, actual, "Mismatch at index " + i);
            ^
  symbol:   method assertEquals(Num,Num,String)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:117: error: cannot find symbol
            assertTrue(expected instanceof NaN, "Direct SMAIndicator should return NaN for index " + i);
            ^
  symbol:   method assertTrue(boolean,String)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:118: error: cannot find symbol
            assertTrue(actual instanceof NaN, "NumericIndicator.sma() should also return NaN for index " + i);
            ^
  symbol:   method assertTrue(boolean,String)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:127: error: cannot find symbol
            assertEquals(expected[i], sma.getValue(i), () -> "Unexpected SMA value at index " + i + " for barCount=" + barCount);
            ^
  symbol:   method assertEquals(Num,Num,()->"Unexp[...]Count)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:138: error: cannot find symbol
                fail("Expected IllegalArgumentException for barCount = " + bc);
                ^
  symbol:   method fail(String)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:150: error: cannot find symbol
        assertTrue(delegate instanceof SMAIndicator);
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:151: error: cannot find symbol
        assertSame(series, delegate.getBarSeries());
        ^
  symbol:   method assertSame(BarSeries,BarSeries)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:156: error: cannot find symbol
        BarSeries shortSeries = new BaseBarSeriesBuilder().withName("short").withNumTypeOf(DecimalNum.class).withValues(10, 20, 30, 40).build();
                                                                                                            ^
  symbol:   method withValues(int,int,int,int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:162: error: cannot find symbol
            assertEquals(expected[i], sma.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:168: error: cannot find symbol
        BarSeries shortSeries = new BaseBarSeriesBuilder().withName("short").withNumTypeOf(DecimalNum.class).withValues(10, 20, 30, 40).build();
                                                                                                            ^
  symbol:   method withValues(int,int,int,int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:174: error: cannot find symbol
            assertEquals(expected[i], sma.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:183: error: cannot find symbol
        assertNotSame(sma1, sma2);
        ^
  symbol:   method assertNotSame(NumericIndicator,NumericIndicator)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:184: error: cannot find symbol
        assertNotSame(sma1.delegate(), sma2.delegate());
        ^
  symbol:   method assertNotSame(Indicator<Num>,Indicator<Num>)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:186: error: cannot find symbol
            assertEquals(sma1.getValue(i), sma2.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:197: error: cannot find symbol
            assertEquals(closePrice.getValue(i), numericIndicator.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:206: error: cannot find symbol
        assertTrue(str.contains("SMAIndicator"));
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:207: error: cannot find symbol
        assertTrue(str.contains(String.valueOf(barCount)));
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:212: error: cannot find symbol
        BarSeries shortSeries = new BaseBarSeriesBuilder().withName("short").withNumTypeOf(DecimalNum.class).withValues(10, 20, 30, 40).build();
                                                                                                            ^
  symbol:   method withValues(int,int,int,int)
  location: class BaseBarSeriesBuilder
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:220: error: cannot find symbol
            assertEquals(expected[i], secondSma.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:227: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> nullWrapped.sma(5));
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->nullWr[...]ma(5))
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:234: error: cannot find symbol
        assertThrows(RuntimeException.class, () -> wrapper.sma(3));
        ^
  symbol:   method assertThrows(Class<RuntimeException>,()->wrapper.sma(3))
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:266: error: cannot find symbol
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> sma.getValue(2));
                                   ^
  symbol:   method assertThrows(Class<IllegalStateException>,()->sma.getValue(2))
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:267: error: cannot find symbol
        assertTrue(ex.getMessage().contains("Deliberate failure"));
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_sma_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_sma_Test.java:273: error: cannot find symbol
        series = new MockBarSeries(DecimalNum::valueOf, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
                     ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_sma_Test
56 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.indicators.numeric.NumericIndicator': '\n * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides\n * methods to create rules and other "lightweight" indicators, using a\n * (hopefully) natural-looking and expressive series of method calls.\n * \n * <p>\n * Methods like plus(), minus() and sqrt() correspond directly to methods in the\n * {@code Num} interface. These methods create "lightweight" (not cached)\n * indicators to add, subtract, etc. Many methods are overloaded to accept\n * either {@code Indicator<Num>} or {@code Number} arguments.\n * \n * <p>\n * Methods like sma() and ema() simply create the corresponding indicator\n * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first\n * argument. These methods usually instantiate cached objects.\n * \n * <p>\n * Another set of methods, like crossedOver() and isGreaterThan() create Rule\n * objects. These are also overloaded to accept both {@code Indicator<Num>} and\n * {@code Number} arguments.\n ', 'api document of method sma(int)': '\n     * @param barCount the time frame\n     * @return the {@link SMAIndicator} of {@code this}\n     ', 'return type': 'org.ta4j.core.indicators.numeric.NumericIndicator', 'dependent classes': 'class `org.ta4j.core.indicators.numeric.NumericIndicator`:\n\trelated functions:\n\t\tmethod `public NumericIndicator sma(int)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.sma(int)`, api document: \n     * @param barCount the time frame\n     * @return the {@link SMAIndicator} of {@code this}\n     ;\n\t\tmethod `public static NumericIndicator closePrice(BarSeries)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.sma(int)`, api document: \n     * Creates a fluent version of the ClosePriceIndicator.\n     *\n     * @return a NumericIndicator wrapped around a ClosePriceIndicator\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


