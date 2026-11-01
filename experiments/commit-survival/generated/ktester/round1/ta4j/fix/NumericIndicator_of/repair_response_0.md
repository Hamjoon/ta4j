```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Simple tests for {@link NumericIndicator#of(Indicator)} without external
 * testing or mocking frameworks. The tests are executed from the {@code main}
 * method using Java's built‑in {@code assert} statements.
 *
 * <p>Running the class with {@code -ea} (enable assertions) will cause the
 * tests to fail if any contract is broken.</p>
 */
public class NumericIndicator_of_Test {

    /** A very small stub {@link Indicator} used for the delegation tests. */
    private static final class StubIndicator implements Indicator<Num> {

        private final Num fixedValue;

        StubIndicator(Num fixedValue) {
            this.fixedValue = fixedValue;
        }

        @Override
        public Num getValue(int index) {
            // ignore the index – always return the fixed value
            return fixedValue;
        }

        @Override
        public BarSeries getBarSeries() {
            // not needed for the current tests – return {@code null}
            return null;
        }

        @Override
        public int getUnstableBars() {
            // a deterministic value for the test
            return 0;
        }

        @Override
        public String toString() {
            return "StubIndicator";
        }
    }

    public static void main(String[] args) {
        // Enable assertions when running the tests:
        //   java -ea org.ta4j.core.indicators.numeric.NumericIndicator_of_Test
        testOfCreatesNumericIndicatorWithCorrectDelegate();
        testGetValueIsDelegatedToUnderlyingIndicator();
        testOfRejectsNullDelegate();
        System.out.println("All NumericIndicator.of() tests passed.");
    }

    /** Verifies that {@code NumericIndicator.of} returns a non‑null wrapper
     * whose {@code delegate()} method returns the original indicator. */
    private static void testOfCreatesNumericIndicatorWithCorrectDelegate() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1));
        NumericIndicator numericIndicator = NumericIndicator.of(stub);

        assert numericIndicator != null : "NumericIndicator.of should not return null";
        assert numericIndicator.delegate() == stub
                : "The delegate returned by NumericIndicator must be the original Indicator";
    }

    /** Checks that {@code getValue(int)} is correctly delegated to the wrapped
     * indicator. */
    private static void testGetValueIsDelegatedToUnderlyingIndicator() {
        Num expected = DecimalNum.valueOf(42);
        Indicator<Num> stub = new StubIndicator(expected);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);

        int index = 7; // arbitrary index – the stub ignores it
        Num actual = numericIndicator.getValue(index);

        assert expected.equals(actual)
                : "NumericIndicator#getValue must delegate to the underlying Indicator";
    }

    /** {@code NumericIndicator.of(null)} must throw a {@link NullPointerException}. */
    private static void testOfRejectsNullDelegate() {
        boolean threw = false;
        try {
            NumericIndicator.of(null);
        } catch (NullPointerException e) {
            threw = true;
        }
        assert threw : "Calling NumericIndicator.of with a null Indicator should throw NullPointerException";
    }
}
```