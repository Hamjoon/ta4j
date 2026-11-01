```java
package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NumericIndicator#of(Indicator)}.
 *
 * <p>The original test mixed JUnit 5 and Mockito JUnit 5 extensions, which are not
 * available in the compilation environment.  This version uses JUnit 4 and the
 * classic Mockito runner, fixing all import and annotation problems.
 */
@RunWith(MockitoJUnitRunner.class)
public class NumericIndicator_of_Test {

    /**
     * Mocked Indicator used in most tests.
     */
    @Mock
    private Indicator<Num> mockIndicator;

    /**
     * The NumericIndicator created by the factory method under test.
     */
    private NumericIndicator numericIndicator;

    /**
     * A simple BarSeries used for the concrete‑indicator test.
     */
    private static BarSeries series;

    /**
     * A very small stub {@link Indicator} used for the delegation tests.
     */
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

    @BeforeClass
    public static void setupBeforeAll() {
        // Build a very small series – the actual values are irrelevant for the
        // delegation tests, they just need to be non‑null.
        series = new org.ta4j.core.BaseBarSeriesBuilder()
                .withName("test-series")
                .build();

        // Add five bars with close prices 1,2,3,4,5
        for (int i = 1; i <= 5; i++) {
            series.addBar(new org.ta4j.core.BaseBarBuilder()
                    .time(ZonedDateTime.now().plusDays(i))
                    .openPrice(DecimalNum.valueOf(i))
                    .highPrice(DecimalNum.valueOf(i))
                    .lowPrice(DecimalNum.valueOf(i))
                    .closePrice(DecimalNum.valueOf(i))
                    .volume(DecimalNum.valueOf(0))
                    .build());
        }
    }

    @Before
    public void setupBeforeEach() {
        // Reset mock behaviour before each test.
        reset(mockIndicator);
        // Create the object under test.
        numericIndicator = NumericIndicator.of(mockIndicator);
    }

    @After
    public void teardownAfterEach() {
        // Verify that no unexpected interactions happened on the mock.
        verifyNoMoreInteractions(mockIndicator);
    }

    /** Simple main method to run the static tests without a test framework. */
    public static void main(String[] args) {
        // Enable assertions when running the tests:
        //   java -ea org.ta4j.core.indicators.numeric.NumericIndicator_of_Test
        testOfCreatesNumericIndicatorWithCorrectDelegate();
        testGetValueIsDelegatedToUnderlyingIndicator();
        testOfRejectsNullDelegate();
        System.out.println("All NumericIndicator.of() tests passed.");
    }

    /**
     * Verifies that {@code NumericIndicator.of} returns a non‑null wrapper
     * whose {@code delegate()} method returns the original indicator.
     */
    private static void testOfCreatesNumericIndicatorWithCorrectDelegate() {
        Indicator<Num> stub = new StubIndicator(DecimalNum.valueOf(1));
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        assert numericIndicator != null : "NumericIndicator.of should not return null";
        assert numericIndicator.delegate() == stub : "The delegate returned by NumericIndicator must be the original Indicator";
    }

    /**
     * Checks that {@code getValue(int)} is correctly delegated to the wrapped
     * indicator.
     */
    private static void testGetValueIsDelegatedToUnderlyingIndicator() {
        Num expected = DecimalNum.valueOf(42);
        Indicator<Num> stub = new StubIndicator(expected);
        NumericIndicator numericIndicator = NumericIndicator.of(stub);
        // arbitrary index – the stub ignores it
        int index = 7;
        Num actual = numericIndicator.getValue(index);
        assert expected.equals(actual) : "NumericIndicator#getValue must delegate to the underlying Indicator";
    }

    /**
     * {@code NumericIndicator.of(null)} must throw a {@link NullPointerException}.
     */
    private static void testOfRejectsNullDelegate() {
        boolean threw = false;
        try {
            NumericIndicator.of(null);
        } catch (NullPointerException e) {
            threw = true;
        }
        assert threw : "Calling NumericIndicator.of with a null Indicator should throw NullPointerException";
    }

    @Test
    public void ofCreatesNumericIndicatorWithCorrectDelegate() {
        assertNotNull(numericIndicator);
        // The delegate accessor is public, so we can check it directly.
        assertSame(mockIndicator, numericIndicator.delegate());
    }

    @Test
    public void getValueIsDelegatedToUnderlyingIndicator() {
        // Arrange
        int index = 7;
        Num expected = DecimalNum.valueOf(42);
        when(mockIndicator.getValue(index)).thenReturn(expected);
        // Act
        Num actual = numericIndicator.getValue(index);
        // Assert
        assertEquals(expected, actual);
        verify(mockIndicator).getValue(index);
    }

    @Test
    public void getBarSeriesIsDelegatedToUnderlyingIndicator() {
        // Arrange
        BarSeries expectedSeries = series;
        when(mockIndicator.getBarSeries()).thenReturn(expectedSeries);
        // Act
        BarSeries actualSeries = numericIndicator.getBarSeries();
        // Assert
        assertSame(expectedSeries, actualSeries);
        verify(mockIndicator).getBarSeries();
    }

    @Test
    public void getUnstableBarsIsDelegatedToUnderlyingIndicator() {
        // Arrange
        int expectedUnstable = 3;
        when(mockIndicator.getUnstableBars()).thenReturn(expectedUnstable);
        // Act
        int actualUnstable = numericIndicator.getUnstableBars();
        // Assert
        assertEquals(expectedUnstable, actualUnstable);
        verify(mockIndicator).getUnstableBars();
    }

    @Test
    public void ofWorksWithARealIndicatorImplementation() {
        // Use a concrete indicator (ClosePriceIndicator) to ensure the factory
        // works with non‑mock objects as well.
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        NumericIndicator fromClose = NumericIndicator.of(closePrice);
        // The delegate should be exactly the ClosePriceIndicator we passed.
        assertSame(closePrice, fromClose.delegate());
        // Spot‑check a few values to confirm correct delegation.
        for (int i = 0; i < series.getBarCount(); i++) {
            assertEquals(closePrice.getValue(i), fromClose.getValue(i));
        }
    }

    /**
     * {@code NumericIndicator.of(null)} must fail fast with a {@link NullPointerException}
     * because the underlying {@code NumericIndicator} constructor requires a non‑null delegate.
     */
    @Test(expected = NullPointerException.class)
    public void testOf_NullDelegate() {
        NumericIndicator.of(null);
    }
}
```