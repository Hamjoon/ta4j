
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
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
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.Test;
import org.ta4j.core.TestUtils;
import org.ta4j.core.mocks.MockBarSeries;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class NumericIndicator_lowest_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * Series used by the tests.
     */
    private static BarSeries series;

    /**
     * Close‑price indicator based on {@link #series}.
     */
    private static ClosePriceIndicator closePrice;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
        // A simple series with positive, zero and negative values.
        series = new MockBarSeries(numFunction, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
        // DecimalNum is the default Num implementation in ta4j.
        numFunction = n -> DecimalNum.valueOf(n.doubleValue());
    }

    @AfterAll
    static void teardownAfterAll() {
        // Release static references (optional, helps GC in long test suites).
        series = null;
        closePrice = null;
        numFunction = null;
    }

    @Test
    void lowestCreatesCorrectDelegateAndValues() {
        // GIVEN a NumericIndicator wrapping the close‑price indicator
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // WHEN we ask for the lowest value over a window of 3 bars
        int barCount = 3;
        NumericIndicator lowest = numeric.lowest(barCount);
        // THEN the underlying delegate must be a LowestValueIndicator
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator, "Delegate should be an instance of LowestValueIndicator");
        // AND the computed values must match those of a direct LowestValueIndicator
        LowestValueIndicator expected = new LowestValueIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected.getValue(i), lowest.getValue(i), "Mismatch at index " + i);
        }
    }

    @Test
    void lowestWithBarCountGreaterThanSeriesLength() {
        // GIVEN a NumericIndicator
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        // WHEN the barCount exceeds the series length
        int barCount = series.getBarCount() + 5;
        NumericIndicator lowest = numeric.lowest(barCount);
        // THEN the delegate is still a LowestValueIndicator
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        // AND the value at the last index must be the minimum of the whole series
        Num min = closePrice.getValue(0);
        for (int i = 1; i < series.getBarCount(); i++) {
            min = min.min(closePrice.getValue(i));
        }
        assertNumEquals(min, lowest.getValue(series.getBarCount() - 1));
    }

    @Test
    void lowest_positive_barCount_calculatesCorrectValues() {
        // GIVEN a series with values [10, 8, 12, 5, 7]
        BarSeries series = new MockBarSeries(numFunction, 10, 8, 12, 5, 7);
        ClosePriceIndicator source = new ClosePriceIndicator(series);
        NumericIndicator numeric = NumericIndicator.of(source);
        // WHEN we request the lowest value over a window of 3 bars
        int barCount = 3;
        NumericIndicator lowest = numeric.lowest(barCount);
        // THEN the delegate must be a LowestValueIndicator
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator, "Delegate should be an instance of LowestValueIndicator");
        // AND the computed values must match the expected sequence:
        // [10, 8, 8, 5, 5] for indexes 0‑4
        Num[] expected = { DecimalNum.valueOf(10), DecimalNum.valueOf(8), DecimalNum.valueOf(8), DecimalNum.valueOf(5), DecimalNum.valueOf(5) };
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i], lowest.getValue(i), "Mismatch at index " + i);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -5 })
    void lowest_invalid_barCount_throwsIllegalArgumentException(int barCount) {
        // GIVEN a simple numeric indicator based on close price
        BarSeries series = new MockBarSeries(numFunction, 1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(series));
        // WHEN we call lowest with an illegal barCount
        // THEN an IllegalArgumentException is expected
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(barCount), "lowest(" + barCount + ") should throw IllegalArgumentException");
    }

    @Test
    void lowest_barCount_exceeds_series_usesAvailableValues() {
        // GIVEN a series with values [4, 9, 2, 7, 5]
        BarSeries series = new MockBarSeries(numFunction, 4, 9, 2, 7, 5);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(series));
        // WHEN we request a window larger than the series (barCount = 10)
        int barCount = 10;
        NumericIndicator lowest = numeric.lowest(barCount);
        // THEN the delegate is still a LowestValueIndicator
        assertTrue(lowest.delegate() instanceof LowestValueIndicator, "Delegate should be a LowestValueIndicator even when barCount > series length");
        // AND the values should be the minimum of all values seen so far:
        // Expected: [4, 4, 2, 2, 2]
        Num[] expected = { DecimalNum.valueOf(4), DecimalNum.valueOf(4), DecimalNum.valueOf(2), DecimalNum.valueOf(2), DecimalNum.valueOf(2) };
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i], lowest.getValue(i), "Mismatch at index " + i);
        }
    }

    @Test
    void lowest_barCount_one_mirrorsSourceValues() {
        // GIVEN a series with values [3, 6, 1, 8]
        BarSeries series = new MockBarSeries(numFunction, 3, 6, 1, 8);
        ClosePriceIndicator source = new ClosePriceIndicator(series);
        NumericIndicator numeric = NumericIndicator.of(source);
        // WHEN we ask for the lowest value with a window of 1
        NumericIndicator lowest = numeric.lowest(1);
        // THEN the result must be identical to the source values
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(source.getValue(i), lowest.getValue(i), "Value at index " + i + " should be unchanged");
        }
    }

    @Test
    void lowest_on_derived_indicator_calculatesCorrectWindow() {
        // GIVEN a series with values [5, 1, 4, 9, 2]
        BarSeries series = new MockBarSeries(numFunction, 5, 1, 4, 9, 2);
        ClosePriceIndicator source = new ClosePriceIndicator(series);
        // derived = source + 2
        NumericIndicator derived = NumericIndicator.of(source).plus(2);
        // WHEN we apply lowest(3) on the derived indicator
        NumericIndicator lowest = derived.lowest(3);
        // THEN the expected values are:
        // derived values: [7, 3, 6, 11, 4]
        // lowest(3) over them: [7, 3, 3, 3, 3]
        Num[] expected = { DecimalNum.valueOf(7), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3) };
        for (int i = 0; i < series.getBarCount(); i++) {
            assertNumEquals(expected[i], lowest.getValue(i), "Mismatch at index " + i);
        }
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:3: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:6: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:7: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:12: error: package org.ta4j.core.mocks does not exist
import org.ta4j.core.mocks.MockBarSeries;
                          ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:16: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:17: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.ValueSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:18: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:19: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:21: error: cannot find symbol
import org.ta4j.core.TestUtils;
                    ^
  symbol:   class TestUtils
  location: package org.ta4j.core
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:22: error: package org.ta4j.core.mocks does not exist
import org.ta4j.core.mocks.MockBarSeries;
                          ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:23: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:24: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.ValueSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:27: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:5: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:25: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:27: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:45: error: cannot find symbol
    @BeforeAll
     ^
  symbol:   class BeforeAll
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:56: error: cannot find symbol
    @AfterAll
     ^
  symbol:   class AfterAll
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:118: error: cannot find symbol
    @ParameterizedTest
     ^
  symbol:   class ParameterizedTest
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:119: error: cannot find symbol
    @ValueSource(ints = { 0, -5 })
     ^
  symbol:   class ValueSource
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:50: error: cannot find symbol
        series = new MockBarSeries(numFunction, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
                     ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:73: error: cannot find symbol
        assertTrue(delegate instanceof LowestValueIndicator, "Delegate should be an instance of LowestValueIndicator");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:77: error: cannot find symbol
            assertNumEquals(expected.getValue(i), lowest.getValue(i), "Mismatch at index " + i);
            ^
  symbol:   method assertNumEquals(Num,Num,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:89: error: cannot find symbol
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:95: error: cannot find symbol
        assertNumEquals(min, lowest.getValue(series.getBarCount() - 1));
        ^
  symbol:   method assertNumEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:101: error: cannot find symbol
        BarSeries series = new MockBarSeries(numFunction, 10, 8, 12, 5, 7);
                               ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:109: error: cannot find symbol
        assertTrue(delegate instanceof LowestValueIndicator, "Delegate should be an instance of LowestValueIndicator");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:114: error: cannot find symbol
            assertNumEquals(expected[i], lowest.getValue(i), "Mismatch at index " + i);
            ^
  symbol:   method assertNumEquals(Num,Num,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:122: error: cannot find symbol
        BarSeries series = new MockBarSeries(numFunction, 1, 2, 3);
                               ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:126: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(barCount), "lowest(" + barCount + ") should throw IllegalArgumentException");
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->numeri[...]ount),String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:132: error: cannot find symbol
        BarSeries series = new MockBarSeries(numFunction, 4, 9, 2, 7, 5);
                               ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:138: error: cannot find symbol
        assertTrue(lowest.delegate() instanceof LowestValueIndicator, "Delegate should be a LowestValueIndicator even when barCount > series length");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:143: error: cannot find symbol
            assertNumEquals(expected[i], lowest.getValue(i), "Mismatch at index " + i);
            ^
  symbol:   method assertNumEquals(Num,Num,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:150: error: cannot find symbol
        BarSeries series = new MockBarSeries(numFunction, 3, 6, 1, 8);
                               ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:157: error: cannot find symbol
            assertNumEquals(source.getValue(i), lowest.getValue(i), "Value at index " + i + " should be unchanged");
            ^
  symbol:   method assertNumEquals(Num,Num,String)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:164: error: cannot find symbol
        BarSeries series = new MockBarSeries(numFunction, 5, 1, 4, 9, 2);
                               ^
  symbol:   class MockBarSeries
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:175: error: cannot find symbol
            assertNumEquals(expected[i], lowest.getValue(i), "Mismatch at index " + i);
            ^
  symbol:   method assertNumEquals(Num,Num,String)
  location: class NumericIndicator_lowest_Test
37 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.indicators.numeric.NumericIndicator': '\n * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides\n * methods to create rules and other "lightweight" indicators, using a\n * (hopefully) natural-looking and expressive series of method calls.\n * \n * <p>\n * Methods like plus(), minus() and sqrt() correspond directly to methods in the\n * {@code Num} interface. These methods create "lightweight" (not cached)\n * indicators to add, subtract, etc. Many methods are overloaded to accept\n * either {@code Indicator<Num>} or {@code Number} arguments.\n * \n * <p>\n * Methods like sma() and ema() simply create the corresponding indicator\n * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first\n * argument. These methods usually instantiate cached objects.\n * \n * <p>\n * Another set of methods, like crossedOver() and isGreaterThan() create Rule\n * objects. These are also overloaded to accept both {@code Indicator<Num>} and\n * {@code Number} arguments.\n ', 'api document of method lowest(int)': '\n     * @param barCount the time frame\n     * @return the {@link LowestValueIndicator} of {@code this}\n     ', 'return type': 'org.ta4j.core.indicators.numeric.NumericIndicator', 'dependent classes': 'class `org.ta4j.core.indicators.numeric.NumericIndicator`:\n\trelated functions:\n\t\tmethod `public static NumericIndicator closePrice(BarSeries)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.lowest(int)`, api document: \n     * Creates a fluent version of the ClosePriceIndicator.\n     *\n     * @return a NumericIndicator wrapped around a ClosePriceIndicator\n     ;\n\t\tmethod `public NumericIndicator lowest(int)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.lowest(int)`, api document: \n     * @param barCount the time frame\n     * @return the {@link LowestValueIndicator} of {@code this}\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


