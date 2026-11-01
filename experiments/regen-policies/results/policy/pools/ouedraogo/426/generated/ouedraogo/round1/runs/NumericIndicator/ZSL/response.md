###Test START##
```java
package org.ta4j.core.indicators.numeric;

import org.junit.Test;
import org.junit.Assert;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comprehensive JUnit‑4 test suite for {@link NumericIndicator}.
 *
 * <p>
 * The test creates a very small in‑memory implementation of {@link BarSeries},
 * {@link Num} and {@link NumFactory} so that we can instantiate the real TA4J
 * indicators without using any external mocking framework. All public methods
 * of {@code NumericIndicator} are exercised, both the overloads that accept
 * {@link Indicator} and those that accept a plain {@link Number}.
 * </p>
 */
public class NumericIndicatorTest {

    /* --------------------------------------------------------------------- *
     *  Simple in‑memory implementations of the TA4J core interfaces      *
     * --------------------------------------------------------------------- */

    /** Minimal {@link Num} implementation based on {@link BigDecimal}. */
    private static final class SimpleNum implements Num {

        private final BigDecimal value;
        private static final MathContext MC = MathContext.DECIMAL64;

        SimpleNum(BigDecimal value) {
            this.value = value;
        }

        SimpleNum(double d) {
            this(BigDecimal.valueOf(d));
        }

        @Override
        public Number getDelegate() {
            return value;
        }

        @Override
        public NumFactory getNumFactory() {
            return SimpleNumFactory.INSTANCE;
        }

        @Override
        public String getName() {
            return "SimpleNum";
        }

        private SimpleNum newNum(BigDecimal v) {
            return new SimpleNum(v);
        }

        @Override
        public Num plus(Num augend) {
            return newNum(this.value.add(((SimpleNum) augend).value, MC));
        }

        @Override
        public Num minus(Num subtrahend) {
            return newNum(this.value.subtract(((SimpleNum) subtrahend).value, MC));
        }

        @Override
        public Num multipliedBy(Num multiplicand) {
            return newNum(this.value.multiply(((SimpleNum) multiplicand).value, MC));
        }

        @Override
        public Num dividedBy(Num divisor) {
            return newNum(this.value.divide(((SimpleNum) divisor).value, MC));
        }

        @Override
        public Num remainder(Num divisor) {
            return newNum(this.value.remainder(((SimpleNum) divisor).value, MC));
        }

        @Override
        public Num floor() {
            return newNum(new BigDecimal(Math.floor(value.doubleValue()), MC));
        }

        @Override
        public Num ceil() {
            return newNum(new BigDecimal(Math.ceil(value.doubleValue()), MC));
        }

        @Override
        public Num pow(int n) {
            return newNum(this.value.pow(n, MC));
        }

        @Override
        public Num pow(Num n) {
            double exponent = ((SimpleNum) n).value.doubleValue();
            return newNum(BigDecimal.valueOf(Math.pow(this.value.doubleValue(), exponent), MC));
        }

        @Override
        public Num log() {
            return newNum(BigDecimal.valueOf(Math.log(value.doubleValue()), MC));
        }

        @Override
        public Num exp() {
            return newNum(BigDecimal.valueOf(Math.exp(value.doubleValue()), MC));
        }

        @Override
        public Num sqrt() {
            return newNum(BigDecimal.valueOf(Math.sqrt(value.doubleValue()), MC));
        }

        @Override
        public Num sqrt(MathContext mathContext) {
            return newNum(BigDecimal.valueOf(Math.sqrt(value.doubleValue()), mathContext));
        }

        @Override
        public Num abs() {
            return newNum(value.abs(MC));
        }

        @Override
        public Num negate() {
            return newNum(value.negate(MC));
        }

        @Override
        public boolean isZero() {
            return value.compareTo(BigDecimal.ZERO) == 0;
        }

        @Override
        public boolean isPositive() {
            return value.compareTo(BigDecimal.ZERO) > 0;
        }

        @Override
        public boolean isPositiveOrZero() {
            return value.compareTo(BigDecimal.ZERO) >= 0;
        }

        @Override
        public boolean isNegative() {
            return value.compareTo(BigDecimal.ZERO) < 0;
        }

        @Override
        public boolean isNegativeOrZero() {
            return value.compareTo(BigDecimal.ZERO) <= 0;
        }

        @Override
        public boolean isEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) == 0;
        }

        @Override
        public boolean isGreaterThan(Num other) {
            return value.compareTo(((SimpleNum) other).value) > 0;
        }

        @Override
        public boolean isGreaterThanOrEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) >= 0;
        }

        @Override
        public boolean isLessThan(Num other) {
            return value.compareTo(((SimpleNum) other).value) < 0;
        }

        @Override
        public boolean isLessThanOrEqual(Num other) {
            return value.compareTo(((SimpleNum) other).value) <= 0;
        }

        @Override
        public Num min(Num other) {
            return (value.compareTo(((SimpleNum) other).value) <= 0) ? this : other;
        }

        @Override
        public Num max(Num other) {
            return (value.compareTo(((SimpleNum) other).value) >= 0) ? this : other;
        }

        @Override
        public int intValue() {
            return value.intValue();
        }

        @Override
        public long longValue() {
            return value.longValue();
        }

        @Override
        public float floatValue() {
            return value.floatValue();
        }

        @Override
        public double doubleValue() {
            return value.doubleValue();
        }

        @Override
        public BigDecimal bigDecimalValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.stripTrailingZeros().toPlainString();
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SimpleNum)) return false;
            SimpleNum other = (SimpleNum) obj;
            return value.compareTo(other.value) == 0;
        }

        @Override
        public int compareTo(Num o) {
            return value.compareTo(((SimpleNum) o).value);
        }
    }

    /** Minimal {@link NumFactory} that creates {@link SimpleNum}. */
    private static final class SimpleNumFactory implements NumFactory {

        static final SimpleNumFactory INSTANCE = new SimpleNumFactory();

        private SimpleNumFactory() {}

        @Override
        public Num numOf(Number number) {
            return new SimpleNum(BigDecimal.valueOf(number.doubleValue()));
        }

        @Override
        public Num numOf(String number) {
            return new SimpleNum(new BigDecimal(number));
        }

        @Override
        public Num numOf(double value) {
            return new SimpleNum(BigDecimal.valueOf(value));
        }

        @Override
        public Num numOf(BigDecimal value) {
            return new SimpleNum(value);
        }
    }

    /** Very small in‑memory {@link BarSeries}. Only the methods required by the
     *  indicators used in the tests are implemented. */
    private static final class SimpleBarSeries implements BarSeries {

        private final List<SimpleNum> values; // used as "close price"
        private final SimpleNumFactory factory = SimpleNumFactory.INSTANCE;

        SimpleBarSeries(List<Double> raw) {
            List<SimpleNum> list = new ArrayList<>();
            for (Double d : raw) {
                list.add(new SimpleNum(d));
            }
            this.values = Collections.unmodifiableList(list);
        }

        @Override
        public NumFactory numFactory() {
            return factory;
        }

        @Override
        public int getBarCount() {
            return values.size();
        }

        @Override
        public int getBeginIndex() {
            return 0;
        }

        @Override
        public int getEndIndex() {
            return values.isEmpty() ? -1 : values.size() - 1;
        }

        @Override
        public int getMaximumBarCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setMaximumBarCount(int maximumBarCount) {
            // no‑op for test implementation
        }

        @Override
        public int getRemovedBarsCount() {
            return 0;
        }

        @Override
        public String getName() {
            return "SimpleBarSeries";
        }

        // The remaining methods are not needed for the test suite; they return
        // sensible defaults or throw UnsupportedOperationException.

        @Override
        public org.ta4j.core.Bar getBar(int i) {
            throw new UnsupportedOperationException("Bar object not required for this test");
        }

        @Override
        public List<org.ta4j.core.Bar> getBarData() {
            return Collections.emptyList();
        }

        @Override
        public BarSeries getSubSeries(int startIndex, int endIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.ta4j.core.BarBuilder barBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSeriesPeriodDescription() {
            return "";
        }

        @Override
        public String getSeriesPeriodDescriptionInSystemTimeZone() {
            return "";
        }

        @Override
        public void addBar(org.ta4j.core.Bar bar) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addBar(org.ta4j.core.Bar bar, boolean replace) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTrade(Number tradeVolume, Number tradePrice) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTrade(Num tradeVolume, Num tradePrice) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPrice(Num price) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPrice(Number price) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    /** Simple {@link Indicator} that returns the values supplied at construction time. */
    private static final class SimpleIndicator implements Indicator<Num> {

        private final List<SimpleNum> values;
        private final SimpleBarSeries series;

        SimpleIndicator(SimpleBarSeries series, List<Double> raw) {
            this.series = series;
            List<SimpleNum> list = new ArrayList<>();
            for (Double d : raw) {
                list.add(new SimpleNum(d));
            }
            this.values = Collections.unmodifiableList(list);
        }

        @Override
        public Num getValue(int index) {
            return values.get(index);
        }

        @Override
        public int getCountOfUnstableBars() {
            return 0;
        }

        @Override
        public BarSeries getBarSeries() {
            return series;
        }

        @Override
        public String toString() {
            return "SimpleIndicator" + values;
        }
    }

    /* --------------------------------------------------------------------- *
     *  Helper methods for building test fixtures                           *
     * --------------------------------------------------------------------- */

    private NumericIndicator numericOf(List<Double> data) {
        SimpleBarSeries series = new SimpleBarSeries(data);
        SimpleIndicator indicator = new SimpleIndicator(series, data);
        return NumericIndicator.of(indicator);
    }

    private SimpleNum num(double d) {
        return new SimpleNum(d);
    }

    /* --------------------------------------------------------------------- *
     *                         Test Cases                                    *
     * --------------------------------------------------------------------- */

    @Test
    public void testPlusIndicatorAndNumber() {
        NumericIndicator a = numericOf(List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        NumericIndicator b = numericOf(List.of(10.0, 10.0, 10.0, 10.0, 10.0));

        // plus with another indicator
        NumericIndicator sum = a.plus(b);
        Assert.assertEquals(num(11.0), sum.getValue(0));
        Assert.assertEquals(num(12.0), sum.getValue(1));

        // plus with a plain Number
        NumericIndicator sumNum = a.plus(10);
        Assert.assertEquals(num(11.0), sumNum.getValue(0));
        Assert.assertEquals(num(12.0), sumNum.getValue(1));
    }

    @Test
    public void testMinusIndicatorAndNumber() {
        NumericIndicator a = numericOf(List.of(5.0, 6.0, 7.0));
        NumericIndicator b = numericOf(List.of(2.0, 2.0, 2.0));

        NumericIndicator diff = a.minus(b);
        Assert.assertEquals(num(3.0), diff.getValue(0));
        Assert.assertEquals(num(4.0), diff.getValue(1));

        NumericIndicator diffNum = a.minus(2);
        Assert.assertEquals(num(3.0), diffNum.getValue(0));
        Assert.assertEquals(num(4.0), diffNum.getValue(1));
    }

    @Test
    public void testMultiplicationAndDivision() {
        NumericIndicator a = numericOf(List.of(2.0, 3.0, 4.0));
        NumericIndicator b = numericOf(List.of(5.0, 5.0, 5.0));

        NumericIndicator prod = a.multipliedBy(b);
        Assert.assertEquals(num(10.0), prod.getValue(0));
        Assert.assertEquals(num(15.0), prod.getValue(1));

        NumericIndicator prodNum = a.multipliedBy(5);
        Assert.assertEquals(num(10.0), prodNum.getValue(0));
        Assert.assertEquals(num(15.0), prodNum.getValue(1));

        NumericIndicator div = a.dividedBy(b);
        Assert.assertEquals(num(0.4), div.getValue(0));
        Assert.assertEquals(num(0.6), div.getValue(1));

        NumericIndicator divNum = a.dividedBy(2);
        Assert.assertEquals(num(1.0), divNum.getValue(0));
        Assert.assertEquals(num(1.5), divNum.getValue(1));
    }

    @Test
    public void testMinAndMax() {
        NumericIndicator a = numericOf(List.of(1.0, 4.0, 7.0));
        NumericIndicator b = numericOf(List.of(3.0, 2.0, 8.0));

        NumericIndicator minInd = a.min(b);
        Assert.assertEquals(num(1.0), minInd.getValue(0));
        Assert.assertEquals(num(2.0), minInd.getValue(1));
        Assert.assertEquals(num(7.0), minInd.getValue(2));

        NumericIndicator maxInd = a.max(b);
        Assert.assertEquals(num(3.0), maxInd.getValue(0));
        Assert.assertEquals(num(4.0), maxInd.getValue(1));
        Assert.assertEquals(num(8.0), maxInd.getValue(2));

        NumericIndicator minNum = a.min(5);
        Assert.assertEquals(num(1.0), minNum.getValue(0));
        Assert.assertEquals(num(4.0), minNum.getValue(1));

        NumericIndicator maxNum = a.max(5);
        Assert.assertEquals(num(5.0), maxNum.getValue(0));
        Assert.assertEquals(num(5.0), maxNum.getValue(1));
        Assert.assertEquals(num(7.0), maxNum.getValue(2));
    }

    @Test
    public void testAbsAndSqrtAndSquared() {
        NumericIndicator negative = numericOf(List.of(-4.0, -9.0, 0.0, 16.0));

        NumericIndicator abs = negative.abs();
        Assert.assertEquals(num(4.0), abs.getValue(0));
        Assert.assertEquals(num(9.0), abs.getValue(1));
        Assert.assertEquals(num(0.0), abs.getValue(2));
        Assert.assertEquals(num(16.0), abs.getValue(3));

        NumericIndicator sqrt = numericOf(List.of(0.0, 1.0, 4.0, 9.0)).sqrt();
        Assert.assertEquals(num(0.0), sqrt.getValue(0));
        Assert.assertEquals(num(1.0), sqrt.getValue(1));
        Assert.assertEquals(num(2.0), sqrt.getValue(2));
        Assert.assertEquals(num(3.0), sqrt.getValue(3));

        NumericIndicator original = numericOf(List.of(2.0, 3.0));
        NumericIndicator squared = original.squared();
        Assert.assertEquals(num(4.0), squared.getValue(0));
        Assert.assertEquals(num(9.0), squared.getValue(1));
    }

    @Test
    public void testSmaAndEma() {
        // series: 1,2,3,4,5
        NumericIndicator ind = numericOf(List.of(1.0, 2.0, 3.0, 4.0, 5.0));

        // SMA with barCount = 3
        NumericIndicator sma = ind.sma(3);
        // SMA values: index 0 & 1 are unstable (NaN), index 2 = (1+2+3)/3 = 2
        Assert.assertEquals(num(2.0), sma.getValue(2));
        Assert.assertEquals(num(3.0), sma.getValue(3)); // (2+3+4)/3
        Assert.assertEquals(num(4.0), sma.getValue(4)); // (3+4+5)/3

        // EMA with barCount = 1 should be identical to original values
        NumericIndicator ema = ind.ema(1);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(ind.getValue(i), ema.getValue(i));
        }
    }

    @Test
    public void testStandardDeviationHighestLowestPrevious() {
        // data: 1,2,3,4,5
        NumericIndicator ind = numericOf(List.of(1.0, 2.0, 3.0, 4.0, 5.0));

        // stddev with barCount = 2 (sample standard deviation)
        NumericIndicator stddev = ind.stddev(2);
        // For two consecutive numbers x,y the sample stddev = sqrt(((x-y)^2)/1)
        // index 1: values 1,2 -> stddev = sqrt(1) = 1
        Assert.assertEquals(num(1.0), stddev.getValue(1));
        // index 2: values 2,3 -> stddev = 1
        Assert.assertEquals(num(1.0), stddev.getValue(2));

        // highest with barCount = 3
        NumericIndicator highest = ind.highest(3);
        Assert.assertEquals(num(3.0), highest.getValue(2));
        Assert.assertEquals(num(4.0), highest.getValue(3));
        Assert.assertEquals(num(5.0), highest.getValue(4));

        // lowest with barCount = 3
        NumericIndicator lowest = ind.lowest(3);
        Assert.assertEquals(num(1.0), lowest.getValue(2));
        Assert.assertEquals(num(2.0), lowest.getValue(3));
        Assert.assertEquals(num(3.0), lowest.getValue(4));

        // previous (default 1)
        NumericIndicator previous = ind.previous();
        Assert.assertEquals(num(1.0), previous.getValue(1));
        Assert.assertEquals(num(2.0), previous.getValue(2));
        // previous(2)
        NumericIndicator previous2 = ind.previous(2);
        Assert.assertEquals(num(1.0), previous2.getValue(2));
        Assert.assertEquals(num(2.0), previous2.getValue(3));
    }

    @Test
    public void testCrossedOverAndCrossedUnderRules() {
        // values: 1, 2, 3, 2, 1
        NumericIndicator ind = numericOf(List.of(1.0, 2.0, 3.0, 2.0, 1.0));

        // crossed over constant 2 => true at index 2 (3 crosses up from 2)
        Rule crossedOver = ind.crossedOver(2);
        Assert.assertFalse(crossedOver.isSatisfied(0));
        Assert.assertFalse(crossedOver.isSatisfied(1));
        Assert.assertTrue(crossedOver.isSatisfied(2));
        Assert.assertFalse(crossedOver.isSatisfied(3));

        // crossed under constant 2 => true at index 4 (1 crosses down from 2)
        Rule crossedUnder = ind.crossedUnder(2);
        Assert.assertFalse(crossedUnder.isSatisfied(0));
        Assert.assertFalse(crossedUnder.isSatisfied(1));
        Assert.assertFalse(crossedUnder.isSatisfied(2));
        Assert.assertTrue(crossedUnder.isSatisfied(4));
    }

    @Test
    public void testGreaterThanAndLessThanRules() {
        NumericIndicator ind = numericOf(List.of(5.0, 3.0, 7.0));

        Rule greaterThan3 = ind.isGreaterThan(3);
        Assert.assertTrue(greaterThan3.isSatisfied(0)); // 5 > 3
        Assert.assertFalse(greaterThan3.isSatisfied(1)); // 3 > 3 false
        Assert.assertTrue(greaterThan3.isSatisfied(2)); // 7 > 3

        Rule lessThan6 = ind.isLessThan(6);
        Assert.assertFalse(lessThan6.isSatisfied(0)); // 5 < 6 true
        Assert.assertTrue(lessThan6.isSatisfied(1)); // 3 < 6 true
        Assert.assertFalse(lessThan6.isSatisfied(2)); // 7 < 6 false
    }

    @Test
    public void testDelegateAndToString() {
        NumericIndicator ind = numericOf(List.of(1.0, 2.0));
        Indicator<Num> delegate = ind.delegate();
        Assert.assertNotNull(delegate);
        Assert.assertEquals("SimpleIndicator[SimpleNum(1), SimpleNum(2)]", delegate.toString());
        Assert.assertEquals(delegate.toString(), ind.toString());
    }
}
```
###Test END##