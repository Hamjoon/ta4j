package org.ta4j.core.indicators.numeric;

import java.util.function.Function;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.junit.jupiter.api.Assertions.*;

class NumericIndicator_plus_Test {

    /**
     * Function used by the mock series to create {@link Num} instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * Shared series used by all tests.
     */
    private static BarSeries series;

    /**
     * Simple close‑price indicator based on the series.
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * EMA indicator used as the dynamic operand for {@code plus}.
     */
    private static EMAIndicator emaIndicator;

    /**
     * The {@link NumericIndicator} under test.
     */
    private NumericIndicator numericIndicator;

    /**
     * Simple indicator that returns its index as a {@link DecimalNum}.
     */
    private static class IndexIndicator implements Indicator<Num> {

        @Override
        public Num getValue(int index) {
            return DecimalNum.valueOf(index);
        }

        @Override
        public BarSeries getBarSeries() {
            // Not needed for the tests
            return null;
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

    //     @BeforeAll
    static void setupBeforeAll() {
        // Create a Num factory that uses DecimalNum (the default implementation in Ta4j)
        numFunction = DecimalNum::valueOf;
    }

    //     @BeforeEach
    void setupBeforeEach() {
        // By default we work with a NumericIndicator that decorates an IndexIndicator.
        numericIndicator = NumericIndicator.of(new IndexIndicator());
    }

    //     @AfterAll
    static void teardownAfterAll() {
        // Nullify static fields for GC friendliness
        numFunction = null;
    }

//     @Test
//     void plusWithIndicator_shouldReturnSumOfValues() {
//         // Arrange: another indicator that returns index * 2
//         Indicator<Num> doubleIndex = new Indicator<Num>() {
// 
//             @Override
//             public Num getValue(int index) {
//                 return DecimalNum.valueOf(index * 2);
//             }
// 
//             @Override
//             public BarSeries getBarSeries() {
//                 return null;
//             }
// 
//             @Override
//             public int getUnstableBars() {
//                 return 0;
//             }
// 
//             @Override
//             public String toString() {
//                 return "DoubleIndexIndicator";
//             }
//         };
//         // Act: create a new NumericIndicator that adds the doubleIndex indicator
//         NumericIndicator result = numericIndicator.plus(doubleIndex);
//         // Verify a few representative indexes (including the first, a middle, and the last)
//         for (int index : new int[] { 0, 5, 8, 15, 20 }) {
//             Num expected = DecimalNum.valueOf(index).plus(DecimalNum.valueOf(index * 2));
//             Num actual = result.getValue(index);
//             assertEquals(expected, actual, "Sum at index " + index + " is not as expected");
//         }
//     }

//     @Test
//     void plusWithConstantIndicator_returnsConstantSum() {
//         // Arrange: a NumericIndicator that always yields 3
//         ConstantIndicator<Num> constThree = new ConstantIndicator<>(null, DecimalNum.valueOf(3));
//         NumericIndicator base = NumericIndicator.of(constThree);
//         // Operand: constant 5
//         ConstantIndicator<Num> constFive = new ConstantIndicator<>(null, DecimalNum.valueOf(5));
//         // Act
//         NumericIndicator result = base.plus(constFive);
//         // Assert: every index must return 8 (3 + 5)
//         for (int i = 0; i < 10; i++) {
//             assertEquals(DecimalNum.valueOf(8), result.getValue(i), "Expected sum 8 at index " + i);
//         }
//     }

//     @Test
//     void plusWithSelf_returnsDoubleValues() {
//         // Arrange: a NumericIndicator that always yields 3
//         ConstantIndicator<Num> constThree = new ConstantIndicator<>(null, DecimalNum.valueOf(3));
//         NumericIndicator base = NumericIndicator.of(constThree);
//         // Act: add the indicator to itself
//         NumericIndicator result = base.plus(base);
//         // Assert: each index must return 6 (3 + 3)
//         for (int i = 0; i < 10; i++) {
//             assertEquals(DecimalNum.valueOf(6), result.getValue(i), "Expected doubled value 6 at index " + i);
//         }
//     }

//     @Test
//     void plusWithNull_throwsNullPointerException() {
//         // Arrange: a simple NumericIndicator (any will do)
//         NumericIndicator base = NumericIndicator.of(new IndexIndicator());
//         // Act & Assert
//         assertThrows(NullPointerException.class, () -> base.plus((Indicator<Num>) null));
//     }

//     @Test
//     void plusWithPreviousValueIndicator_returnsCorrectSum() {
//         // Arrange: base indicator always 3
//         ConstantIndicator<Num> constThree = new ConstantIndicator<>(null, DecimalNum.valueOf(3));
//         NumericIndicator base = NumericIndicator.of(constThree);
//         // Operand: PreviousValueIndicator wrapping a constant 2 (always returns 2)
//         ConstantIndicator<Num> constTwo = new ConstantIndicator<>(null, DecimalNum.valueOf(2));
//         PreviousValueIndicator prevTwo = new PreviousValueIndicator(constTwo);
//         // Act
//         NumericIndicator result = base.plus(prevTwo);
//         // Assert: first few indexes should be 5 (3 + 2)
//         for (int i = 0; i < 5; i++) {
//             assertEquals(DecimalNum.valueOf(5), result.getValue(i), "Expected sum 5 at index " + i);
//         }
//     }

//     @Test
//     void plusWithFaultyIndicator_propagatesException() {
//         // Arrange: base indicator (constant 3) – value does not matter for the exception test
//         ConstantIndicator<Num> constThree = new ConstantIndicator<>(null, DecimalNum.valueOf(3));
//         NumericIndicator base = NumericIndicator.of(constThree);
//         // Operand: custom indicator that throws IndexOutOfBoundsException for negative indexes
//         Indicator<Num> faulty = new Indicator<Num>() {
// 
//             @Override
//             public Num getValue(int index) {
//                 if (index < 0) {
//                     throw new IndexOutOfBoundsException("Negative index: " + index);
//                 }
//                 return DecimalNum.valueOf(0);
//             }
// 
//             @Override
//             public BarSeries getBarSeries() {
//                 return null;
//             }
// 
//             @Override
//             public int getUnstableBars() {
//                 return 0;
//             }
// 
//             @Override
//             public String toString() {
//                 return "FaultyIndicator";
//             }
//         };
//         // Act: create summed indicator
//         NumericIndicator result = base.plus(faulty);
//         // Assert: calling getValue with a negative index must propagate the same exception
//         assertThrows(IndexOutOfBoundsException.class, () -> result.getValue(-1));
//     }
}