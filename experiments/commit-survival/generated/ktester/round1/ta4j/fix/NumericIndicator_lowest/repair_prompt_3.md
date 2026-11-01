
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.time.ZonedDateTime;
import java.time.Duration;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
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
        series = buildSeries(1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2);
        closePrice = new ClosePriceIndicator(series);
    }

    @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePrice = null;
        numFunction = null;
    }

    /**
     * Build a simple {@link BarSeries} from the supplied double values.
     */
    private static BarSeries buildSeries(double... values) {
        // Use the builder that ships with ta4j
        BarSeries s = new org.ta4j.core.BaseBarSeriesBuilder().withName("test").build();
        ZonedDateTime time = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (double v : values) {
            Num n = numFunction.apply(v);
            // For simplicity all price fields are the same and volume is zero
            org.ta4j.core.BaseBar bar = new org.ta4j.core.BaseBar(barDuration, time, // open
            n, // high
            n, // low
            n, // close
            n, // volume
            n, // amount (price * volume, not used here)
            n, DecimalNum.ZERO);
            s.addBar(bar);
            time = time.plusMinutes(1);
        }
        return s;
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
        assertTrue(delegate instanceof LowestValueIndicator);
        // AND the computed values must match those of a direct LowestValueIndicator
        LowestValueIndicator expected = new LowestValueIndicator(closePrice, barCount);
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(expected.getValue(i), lowest.getValue(i));
        }
    }

    @Test
    void lowestWithBarCountGreaterThanSeriesLength() {
        NumericIndicator numeric = NumericIndicator.of(closePrice);
        int barCount = series.getBarCount() + 5;
        NumericIndicator lowest = numeric.lowest(barCount);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        Num min = closePrice.getValue(0);
        for (int i = 1; i < series.getBarCount(); i++) {
            min = min.min(closePrice.getValue(i));
        }
        assertEquals(min, lowest.getValue(series.getBarCount() - 1));
    }

    @Test
    void lowest_positive_barCount_calculatesCorrectValues() {
        BarSeries s = buildSeries(10, 8, 12, 5, 7);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator numeric = NumericIndicator.of(source);
        NumericIndicator lowest = numeric.lowest(3);
        Indicator<Num> delegate = lowest.delegate();
        assertTrue(delegate instanceof LowestValueIndicator);
        Num[] expected = { DecimalNum.valueOf(10), DecimalNum.valueOf(8), DecimalNum.valueOf(8), DecimalNum.valueOf(5), DecimalNum.valueOf(5) };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -5 })
    void lowest_invalid_barCount_throwsIllegalArgumentException(int barCount) {
        BarSeries s = buildSeries(1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(barCount));
    }

    @Test
    void lowest_barCount_exceeds_series_usesAvailableValues() {
        BarSeries s = buildSeries(4, 9, 2, 7, 5);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        NumericIndicator lowest = numeric.lowest(10);
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        Num[] expected = { DecimalNum.valueOf(4), DecimalNum.valueOf(4), DecimalNum.valueOf(2), DecimalNum.valueOf(2), DecimalNum.valueOf(2) };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    @Test
    void lowest_barCount_one_mirrorsSourceValues() {
        BarSeries s = buildSeries(3, 6, 1, 8);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator numeric = NumericIndicator.of(source);
        NumericIndicator lowest = numeric.lowest(1);
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(source.getValue(i), lowest.getValue(i));
        }
    }

    @Test
    void lowest_on_derived_indicator_calculatesCorrectWindow() {
        BarSeries s = buildSeries(5, 1, 4, 9, 2);
        ClosePriceIndicator source = new ClosePriceIndicator(s);
        NumericIndicator derived = NumericIndicator.of(source).plus(2);
        NumericIndicator lowest = derived.lowest(3);
        Num[] expected = { DecimalNum.valueOf(7), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3), DecimalNum.valueOf(3) };
        for (int i = 0; i < s.getBarCount(); i++) {
            assertEquals(expected[i], lowest.getValue(i));
        }
    }

    @Test
    public void lowest_invalid_barCount_zero_throwsIllegalArgumentException() {
        BarSeries s = buildSeries(1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(0));
    }

    @Test
    public void lowest_invalid_barCount_negative_throwsIllegalArgumentException() {
        BarSeries s = buildSeries(1, 2, 3);
        NumericIndicator numeric = NumericIndicator.of(new ClosePriceIndicator(s));
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(-5));
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:12: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.ValueSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:13: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:14: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.AfterAll;
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:15: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.BeforeAll;
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:16: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.Test;
                            ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:17: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:18: error: package org.junit.jupiter.params does not exist
import org.junit.jupiter.params.ParameterizedTest;
                               ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:19: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:21: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:22: error: package org.junit.jupiter.params.provider does not exist
import org.junit.jupiter.params.provider.ValueSource;
                                        ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:25: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:23: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:25: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:43: error: cannot find symbol
    @BeforeAll
     ^
  symbol:   class BeforeAll
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:52: error: cannot find symbol
    @AfterAll
     ^
  symbol:   class AfterAll
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:127: error: cannot find symbol
    @ParameterizedTest
     ^
  symbol:   class ParameterizedTest
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:128: error: cannot find symbol
    @ValueSource(ints = { 0, -5 })
     ^
  symbol:   class ValueSource
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:76: error: incompatible types: DecimalNum cannot be converted to long
            n, DecimalNum.ZERO);
                         ^
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:92: error: cannot find symbol
        assertTrue(delegate instanceof LowestValueIndicator);
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:96: error: cannot find symbol
            assertEquals(expected.getValue(i), lowest.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:105: error: cannot find symbol
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:110: error: cannot find symbol
        assertEquals(min, lowest.getValue(series.getBarCount() - 1));
        ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:120: error: cannot find symbol
        assertTrue(delegate instanceof LowestValueIndicator);
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:123: error: cannot find symbol
            assertEquals(expected[i], lowest.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:132: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(barCount));
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->numeri[...]ount))
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:140: error: cannot find symbol
        assertTrue(lowest.delegate() instanceof LowestValueIndicator);
        ^
  symbol:   method assertTrue(boolean)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:143: error: cannot find symbol
            assertEquals(expected[i], lowest.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:154: error: cannot find symbol
            assertEquals(source.getValue(i), lowest.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:166: error: cannot find symbol
            assertEquals(expected[i], lowest.getValue(i));
            ^
  symbol:   method assertEquals(Num,Num)
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:174: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(0));
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->numeri[...]st(0))
  location: class NumericIndicator_lowest_Test
src/test/java/org/ta4j/core/indicators/numeric/NumericIndicator_lowest_Test.java:181: error: cannot find symbol
        assertThrows(IllegalArgumentException.class, () -> numeric.lowest(-5));
        ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->numeri[...]t(-5))
  location: class NumericIndicator_lowest_Test
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
31 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.indicators.numeric.NumericIndicator': '\n * NumericIndicator is a "fluent decorator" for Indicator<Num>. It provides\n * methods to create rules and other "lightweight" indicators, using a\n * (hopefully) natural-looking and expressive series of method calls.\n * \n * <p>\n * Methods like plus(), minus() and sqrt() correspond directly to methods in the\n * {@code Num} interface. These methods create "lightweight" (not cached)\n * indicators to add, subtract, etc. Many methods are overloaded to accept\n * either {@code Indicator<Num>} or {@code Number} arguments.\n * \n * <p>\n * Methods like sma() and ema() simply create the corresponding indicator\n * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first\n * argument. These methods usually instantiate cached objects.\n * \n * <p>\n * Another set of methods, like crossedOver() and isGreaterThan() create Rule\n * objects. These are also overloaded to accept both {@code Indicator<Num>} and\n * {@code Number} arguments.\n ', 'api document of method lowest(int)': '\n     * @param barCount the time frame\n     * @return the {@link LowestValueIndicator} of {@code this}\n     ', 'return type': 'org.ta4j.core.indicators.numeric.NumericIndicator', 'dependent classes': 'class `org.ta4j.core.indicators.numeric.NumericIndicator`:\n\trelated functions:\n\t\tmethod `public static NumericIndicator closePrice(BarSeries)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.lowest(int)`, api document: \n     * Creates a fluent version of the ClosePriceIndicator.\n     *\n     * @return a NumericIndicator wrapped around a ClosePriceIndicator\n     ;\n\t\tmethod `public NumericIndicator lowest(int)` returns `org.ta4j.core.indicators.numeric.NumericIndicator`, related with `org.ta4j.core.indicators.numeric.NumericIndicator.lowest(int)`, api document: \n     * @param barCount the time frame\n     * @return the {@link LowestValueIndicator} of {@code this}\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


