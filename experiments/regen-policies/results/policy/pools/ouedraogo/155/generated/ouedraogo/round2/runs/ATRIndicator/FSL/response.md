###Test START##
```java
package org.ta4j.core.indicators;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;

/**
 * Unit tests for {@link ATRIndicator}.
 *
 * The tests use a very small stub implementation of {@link BarSeries}
 * that satisfies the constructor requirements of {@link ATRIndicator}
 * and {@link TRIndicator}.  The stub does not provide any real market data
 * – the purpose of the tests is to verify the behaviour of the public API
 * (delegation, getters, {@code toString()}, etc.) without exercising the
 * internal numeric calculations of {@link TRIndicator} or {@link MMAIndicator}.
 */
public class ATRIndicatorTest {

    /** Minimal stub of {@link BarSeries} used only for construction. */
    private static class DummyBarSeries implements BarSeries {

        @Override public org.ta4j.core.num.NumFactory numFactory()            { return null; }
        @Override public org.ta4j.core.BarBuilder barBuilder()                { return null; }
        @Override public String getName()                                      { return "dummy"; }
        @Override public org.ta4j.core.Bar getBar(int index)                  { return null; }
        @Override public int getBarCount()                                    { return 0; }
        @Override public int getMaximumBarCount()                              { return 0; }
        @Override public void setMaximumBarCount(int maximumBarCount)          { /* no‑op */ }
        @Override public int getRemovedBarsCount()                             { return 0; }
        @Override public List<org.ta4j.core.Bar> getBarData()                  { return Collections.emptyList(); }
        @Override public int getBeginIndex()                                   { return 0; }
        @Override public int getEndIndex()                                     { return -1; }
        @Override public String getSeriesPeriodDescription()                  { return ""; }
        @Override public String getSeriesPeriodDescriptionInSystemTimeZone() { return ""; }
        // All other default methods of the interface are left untouched.
    }

    /** Helper to create a fresh {@link ATRIndicator} for the given bar count. */
    private ATRIndicator newAtr(int barCount) {
        BarSeries series = new DummyBarSeries();
        return new ATRIndicator(series, barCount);
    }

    @Test
    public void testConstructorDoesNotThrow() {
        ATRIndicator atr = newAtr(14);
        assertNotNull("ATRIndicator should be instantiated", atr);
    }

    @Test
    public void testGetBarCountReturnsConstructorArgument() {
        int barCount = 10;
        ATRIndicator atr = newAtr(barCount);
        assertEquals("getBarCount() must return the barCount supplied to the constructor",
                     barCount, atr.getBarCount());
    }

    @Test
    public void testGetCountOfUnstableBarsMatchesBarCount() {
        int barCount = 7;
        ATRIndicator atr = newAtr(barCount);
        assertEquals("getCountOfUnstableBars() must equal the bar count",
                     barCount, atr.getCountOfUnstableBars());
    }

    @Test
    public void testGetTRIndicatorIsNotNullAndUsesSameSeries() {
        BarSeries series = new DummyBarSeries();
        ATRIndicator atr = new ATRIndicator(series, 5);
        TRIndicator tr = atr.getTRIndicator();

        assertNotNull("TRIndicator should be created", tr);
        assertSame("TRIndicator must reference the same BarSeries as the ATRIndicator",
                   series, tr.getBarSeries());
    }

    @Test
    public void testToStringContainsBarCount() {
        int barCount = 12;
        ATRIndicator atr = newAtr(barCount);
        String description = atr.toString();

        assertTrue("toString() must contain the class name",
                   description.startsWith(ATRIndicator.class.getSimpleName()));
        assertTrue("toString() must contain the barCount value",
                   description.contains("barCount: " + barCount));
    }

    @Test
    public void testSecondConstructorDelegatesCorrectly() {
        BarSeries series = new DummyBarSeries();
        TRIndicator tr = new TRIndicator(series);
        int barCount = 4;

        ATRIndicator atr = new ATRIndicator(tr, barCount);

        // the ATRIndicator should expose the same TRIndicator instance
        assertSame("ATRIndicator should keep the supplied TRIndicator",
                   tr, atr.getTRIndicator());

        // bar count must be the one supplied to the constructor
        assertEquals(barCount, atr.getBarCount());
    }

    @Test
    public void testGetValueDoesNotThrowWhenSeriesIsEmpty() {
        // The stub series is empty; we only verify that calling getValue()
        // does not raise an unexpected exception.  The returned value is
        // implementation‑specific (could be NaN, zero, etc.) and is not part
        // of the contract verified by these tests.
        ATRIndicator atr = newAtr(3);
        try {
            atr.getValue(0);
        } catch (Exception e) {
            fail("Calling getValue on an empty series should not throw, but got: " + e);
        }
    }
}
```
###Test END##