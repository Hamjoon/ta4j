```java
package org.ta4j.core.indicators.numeric;

import static org.junit.Assert.*;

import java.util.function.Function;

import org.junit.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link NumericIndicator#plus(Indicator)} and related overloads.
 *
 * The original test relied on JUnit‑5, Mockito and Ta4j mock classes that are not
 * available in the compilation environment.  This version uses JUnit‑4 only and
 * replaces the missing Ta4j mock objects with tiny concrete {@link Indicator}
 * implementations that are sufficient for the exercised behaviour.
 */
public class NumericIndicator_plus_Test {

    /** Simple indicator that returns its index as a {@link DecimalNum}. */
    private static class IndexIndicator implements Indicator<Num> {

        @Override
        public Num getValue(int index) {
            return DecimalNum.valueOf(index);
        }

        @Override
        public BarSeries getBarSeries() {
            return null; // not needed for the tests
        }

        @Override
        public int getUnstableBars() {
            return 0;
        }

        @Override
        public String toString() {
            return "IndexIndicator";
        }
    }

    /** Function used to create {@link Num} instances (DecimalNum is the default). */
    private static Function<Number, Num> numFunction;

    /** The {@link NumericIndicator} under test. */
    private NumericIndicator numericIndicator;

    @BeforeClass
    public static void setupBeforeAll() {
        // Create a Num factory that uses DecimalNum (the default implementation in Ta4j)
        numFunction = DecimalNum::valueOf;
    }

    @Before
    public void setupBeforeEach() {
        // By default we work with a NumericIndicator that decorates an IndexIndicator.
        numericIndicator = NumericIndicator.of(new IndexIndicator());
    }

    @AfterClass
    public static void teardownAfterAll() {
        // Nullify static fields for GC friendliness
        numFunction = null;
    }

    @Test
    public void plusWithIndicator_shouldReturnSumOfValues() {
        // Arrange: another indicator that returns index * 2
        Indicator<Num> doubleIndex = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                return DecimalNum.valueOf(index * 2);
            }

            @Override
            public BarSeries getBarSeries() {
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "DoubleIndexIndicator";
            }
        };

        // Act: create a new NumericIndicator that adds the doubleIndex indicator
        NumericIndicator result = numericIndicator.plus(doubleIndex);

        // Verify a few representative indexes (including the first, a middle, and the last)
        for (int index : new int[] { 0, 5, 8, 15, 20 }) {
            Num expected = DecimalNum.valueOf(index).plus(DecimalNum.valueOf(index * 2));
            Num actual = result.getValue(index);
            assertEquals("Sum at index " + index + " is not as expected", expected, actual);
        }
    }

    @Test
    public void plusWithConstantIndicator_returnsConstantSum() {
        // Arrange: a NumericIndicator that always yields 3
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);
        // Operand: constant 5
        ConstantIndicator<Num> constFive = new ConstantIndicator<>(DecimalNum.valueOf(5));
        // Act
        NumericIndicator result = base.plus(constFive);
        // Assert: every index must return 8 (3 + 5)
        for (int i = 0; i < 10; i++) {
            assertEquals("Expected sum 8 at index " + i,
                    DecimalNum.valueOf(8), result.getValue(i));
        }
    }

    @Test
    public void plusWithSelf_returnsDoubleValues() {
        // Arrange: a NumericIndicator that always yields 3
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);
        // Act: add the indicator to itself
        NumericIndicator result = base.plus(base);
        // Assert: each index must return 6 (3 + 3)
        for (int i = 0; i < 10; i++) {
            assertEquals("Expected doubled value 6 at index " + i,
                    DecimalNum.valueOf(6), result.getValue(i));
        }
    }

    @Test
    public void plusWithNull_throwsNullPointerException() {
        // Arrange: a simple NumericIndicator (any will do)
        NumericIndicator base = NumericIndicator.of(new IndexIndicator());
        // Act & Assert
        try {
            // Explicit cast to Indicator to avoid the overload ambiguity
            base.plus((Indicator<Num>) null);
            fail("Calling plus(null) should throw NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void plusWithPreviousValueIndicator_returnsCorrectSum() {
        // Arrange: base indicator always 3
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);
        // Operand: PreviousValueIndicator wrapping a constant 2 (always returns 2)
        ConstantIndicator<Num> constTwo = new ConstantIndicator<>(DecimalNum.valueOf(2));
        PreviousValueIndicator prevTwo = new PreviousValueIndicator<>(constTwo);
        // Act
        NumericIndicator result = base.plus(prevTwo);
        // Assert: first few indexes should be 5 (3 + 2)
        for (int i = 0; i < 5; i++) {
            assertEquals("Expected sum 5 at index " + i,
                    DecimalNum.valueOf(5), result.getValue(i));
        }
    }

    @Test
    public void plusWithFaultyIndicator_propagatesException() {
        // Arrange: base indicator (constant 3) – value does not matter for the exception test
        ConstantIndicator<Num> constThree = new ConstantIndicator<>(DecimalNum.valueOf(3));
        NumericIndicator base = NumericIndicator.of(constThree);
        // Operand: custom indicator that throws IndexOutOfBoundsException for negative indexes
        Indicator<Num> faulty = new Indicator<Num>() {
            @Override
            public Num getValue(int index) {
                if (index < 0) {
                    throw new IndexOutOfBoundsException("Negative index: " + index);
                }
                return DecimalNum.valueOf(0);
            }

            @Override
            public BarSeries getBarSeries() {
                return null;
            }

            @Override
            public int getUnstableBars() {
                return 0;
            }

            @Override
            public String toString() {
                return "FaultyIndicator";
            }
        };
        // Act: create summed indicator
        NumericIndicator result = base.plus(faulty);
        // Assert: calling getValue with a negative index must propagate the same exception
        try {
            result.getValue(-1);
            fail("Exception from the operand indicator should be propagated");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }
}
```