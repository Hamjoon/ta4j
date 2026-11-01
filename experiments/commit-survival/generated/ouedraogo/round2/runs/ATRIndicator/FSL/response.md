###Test START##
```java
package org.ta4j.core.indicators;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.TRIndicator;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests focus on the public contract of the class:
 * <ul>
 *   <li>construction with both constructors</li>
 *   <li>exposure of the internal {@link TRIndicator}</li>
 *   <li>correct reporting of the configured bar‑count</li>
 *   <li>unstable‑bars handling</li>
 *   <li>the {@code toString()} representation</li>
 *   <li>basic behaviour of {@code getValue(int)}</li>
 * </ul>
 *
 * A very small in‑memory implementation of {@link BarSeries} and {@link Num}
 * is provided so that the tests do not depend on any external library code.
 * The {@link FixedTRIndicator} subclass overrides {@code calculate(int)} to
 * return deterministic values, which makes it possible to assert that the
 * {@code ATRIndicator} forwards the request to its internal {@link MMAIndicator}
 * without having to reproduce the whole MMA algorithm.
 */
public class ATRIndicatorTest {

    /** Simple {@link Num} implementation based on {@code double}. */
    private static final class SimpleNum implements Num {

        private final double value;

        SimpleNum(double value) {
            this.value = value;
        }

        /* ---------- factory helpers ---------- */
        static SimpleNum of(Number n) {
            return new SimpleNum(n.doubleValue());
        }

        /* ---------- Num arithmetic (only what we need) ---------- */
        @Override public Num plus(Num augend)   { return new SimpleNum(this.value + augend.doubleValue()); }
        @Override public Num minus(Num subtrahend) { return new SimpleNum(this.value - subtrahend.doubleValue()); }
        @Override public Num multipliedBy(Num multiplicand) { return new SimpleNum(this.value * multiplicand.doubleValue()); }
        @Override public Num dividedBy(Num divisor) { return new SimpleNum(this.value / divisor.doubleValue()); }

        /* ---------- comparison ---------- */
        @Override public int compareTo(Num o) { return Double.compare(this.value, o.doubleValue()); }
        @Override public boolean isEqual(Num other) { return Double.compare(this.value, other.doubleValue()) == 0; }
        @Override public boolean isGreaterThan(Num other) { return this.value > other.doubleValue(); }
        @Override public boolean isGreaterThanOrEqual(Num other) { return this.value >= other.doubleValue(); }
        @Override public boolean isLessThan(Num other) { return this.value < other.doubleValue(); }
        @Override public boolean isLessThanOrEqual(Num other) { return this.value <= other.doubleValue(); }

        /* ---------- other required methods (minimal stubs) ---------- */
        @Override public Number getDelegate() { return value; }
        @Override public String getName() { return Double.toString(value); }
        @Override public Num floor() { return new SimpleNum(Math.floor(value)); }
        @Override public Num ceil() { return new SimpleNum(Math.ceil(value)); }
        @Override public Num pow(int n) { return new SimpleNum(Math.pow(value, n)); }
        @Override public Num pow(Num n) { return new SimpleNum(Math.pow(value, n.doubleValue())); }
        @Override public Num log() { return new SimpleNum(Math.log(value)); }
        @Override public Num sqrt() { return new SimpleNum(Math.sqrt(value)); }
        @Override public Num sqrt(int precision) { return sqrt(); }
        @Override public Num abs() { return new SimpleNum(Math.abs(value)); }
        @Override public Num negate() { return new SimpleNum(-value); }
        @Override public boolean isZero() { return value == 0.0; }
        @Override public boolean isPositive() { return value > 0.0; }
        @Override public boolean isPositiveOrZero() { return value >= 0.0; }
        @Override public boolean isNegative() { return value < 0.0; }
        @Override public boolean isNegativeOrZero() { return value <= 0.0; }
        @Override public Num min(Num other) { return new SimpleNum(Math.min(value, other.doubleValue())); }
        @Override public Num max(Num other) { return new SimpleNum(Math.max(value, other.doubleValue())); }
        @Override public Num remainder(Num divisor) { return new SimpleNum(value % divisor.doubleValue()); }
        @Override public boolean isNaN() { return Double.isNaN(value); }
        @Override public double doubleValue() { return value; }
        @Override public int intValue() { return (int) value; }
        @Override public long longValue() { return (long) value; }
        @Override public float floatValue() { return (float) value; }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override public boolean equals(Object obj) {
            return (obj instanceof SimpleNum) && Double.compare(value, ((SimpleNum) obj).value) == 0;
        }
        @Override public String toString() { return Double.toString(value); }

        /* ---------- default methods from the interface that we do not use ---------- */
        @Override public Num zero() { return SimpleNum.of(0); }
        @Override public Num one() { return SimpleNum.of(1); }
        @Override public Num hundred() { return SimpleNum.of(100); }
        @Override public java.util.function.Function<Number, Num> function() { return SimpleNum::of; }
        @Override public Num numOf(Number value) { return SimpleNum.of(value); }
        @Override public Num numOf(String value, int precision) { return SimpleNum.of(Double.parseDouble(value)); }
        @Override public java.util.stream.Stream<Num> stream() { throw new UnsupportedOperationException(); }
    }

    /** Minimal {@link BarSeries} implementation – only the methods required by the
     *  constructors of {@link TRIndicator} and {@link ATRIndicator} are implemented.
     *  The series does not hold any real {@link Bar}s because the test supplies a
     *  custom {@link TRIndicator} that does not depend on them. */
    private static final class SimpleBarSeries implements BarSeries {

        private final SimpleNumFactory factory = new SimpleNumFactory();

        @Override public String getName() { return "test-series"; }
        @Override public Num num() { return factory.zero(); }
        @Override public Bar getBar(int i) { return null; }
        @Override public int getBarCount() { return 0; }
        @Override public java.util.List<Bar> getBarData() { return java.util.Collections.emptyList(); }
        @Override public int getBeginIndex() { return 0; }
        @Override public int getEndIndex() { return -1; }
        @Override public int getMaximumBarCount() { return Integer.MAX_VALUE; }
        @Override public void setMaximumBarCount(int maximumBarCount) { /* no‑op */ }
        @Override public int getRemovedBarsCount() { return 0; }
        @Override public BarSeries getSubSeries(int startIndex, int endIndex) { return this; }
        @Override public String getSeriesPeriodDescription() { return ""; }

        /* the remaining default methods are not needed for the tests – they delegate
         * to the default implementations provided by the interface, therefore we
         * simply let the compiler use those defaults. */
    }

    /** Helper factory that produces {@link SimpleNum} instances. */
    private static final class SimpleNumFactory {
        SimpleNum zero() { return SimpleNum.of(0); }
        SimpleNum one()  { return SimpleNum.of(1); }
        SimpleNum hundred() { return SimpleNum.of(100); }
        SimpleNum numOf(Number n) { return SimpleNum.of(n); }
    }

    /**
     * A {@link TRIndicator} whose {@code calculate(int)} method returns a
     * predefined array of {@link SimpleNum} values.  This makes the behaviour
     * of the indicator completely deterministic and independent from the
     * underlying {@link BarSeries}.
     */
    private static final class FixedTRIndicator extends TRIndicator {

        private final SimpleNum[] values;

        FixedTRIndicator(BarSeries series, SimpleNum[] values) {
            super(series);
            this.values = values.clone();
        }

        @Override
        protected Num calculate(int index) {
            if (index < 0 || index >= values.length) {
                throw new IndexOutOfBoundsException("index " + index);
            }
            return values[index];
        }
    }

    private SimpleBarSeries series;
    private FixedTRIndicator fixedTR;
    private ATRIndicator atr;

    @Before
    public void setUp() {
        series = new SimpleBarSeries();

        // deterministic TR values: 10.0, 20.0, 30.0, 40.0
        SimpleNum[] trValues = new SimpleNum[] {
                SimpleNum.of(10),
                SimpleNum.of(20),
                SimpleNum.of(30),
                SimpleNum.of(40)
        };
        fixedTR = new FixedTRIndicator(series, trValues);

        // use a barCount of 2 – small enough to keep the expected values easy
        atr = new ATRIndicator(fixedTR, 2);
    }

    @Test
    public void testConstructorWithBarSeries() {
        // The public constructor that receives a BarSeries should create an internal
        // TRIndicator that works on the same series.
        ATRIndicator atrFromSeries = new ATRIndicator(series, 3);
        assertNotNull("TRIndicator must not be null", atrFromSeries.getTRIndicator());
        assertEquals("Bar count must match constructor argument", 3, atrFromSeries.getBarCount());
    }

    @Test
    public void testGetTRIndicatorReturnsProvidedInstance() {
        assertSame("getTRIndicator should return the instance supplied to the constructor",
                fixedTR, atr.getTRIndicator());
    }

    @Test
    public void testGetBarCountMatchesConstructorArgument() {
        assertEquals("getBarCount must return the barCount supplied to the constructor",
                2, atr.getBarCount());
    }

    @Test
    public void testGetUnstableBarsIsBarCount() {
        assertEquals("getUnstableBars should be equal to the configured barCount",
                atr.getBarCount(), atr.getUnstableBars());
    }

    @Test
    public void testToStringContainsClassNameAndBarCount() {
        String s = atr.toString();
        assertTrue("toString must contain the simple class name", s.contains("ATRIndicator"));
        assertTrue("toString must contain the configured barCount", s.contains("barCount: 2"));
    }

    @Test
    public void testGetValueReturnsConsistentResult() {
        // For the given FixedTRIndicator (10,20,30,40) and a barCount of 2,
        // the MMA (modified moving average) algorithm will produce a deterministic
        // sequence.  We are not interested in the exact numeric values here – we only
        // verify that the indicator returns a non‑null Num and that repeated calls
        // for the same index yield equal results (caching behaviour).
        for (int i = 0; i < 4; i++) {
            Num v1 = atr.getValue(i);
            Num v2 = atr.getValue(i);
            assertNotNull("ATRIndicator.getValue(" + i + ") must not return null", v1);
            assertEquals("Repeated calls must return equal Num objects", v1, v2);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueWithInvalidIndexThrowsException() {
        // The underlying FixedTRIndicator throws IndexOutOfBoundsException for
        // indexes outside the supplied array; ATRIndicator should propagate it.
        atr.getValue(10);
    }
}
```
###Test END##