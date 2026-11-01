package org.ta4j.core.indicators.numeric;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import java.time.Duration;
import java.time.ZonedDateTime;
// import org.junit.jupiter.api.extension.ExtendWith;
import org.ta4j.core.BaseBarSeries;
// import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BaseBar;

// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
// @ExtendWith(MockitoExtension.class)
class NumericIndicator_delegate_Test {

    /**
     * Shared series used by all tests
     */
    private static BarSeries series;

    /**
     * A simple ClosePriceIndicator based on the shared series
     */
    private static ClosePriceIndicator closePriceIndicator;

    /**
     * NumericIndicator wrapper for {@link #closePriceIndicator}
     */
    private NumericIndicator numericIndicator;

    /**
     * Simple constant indicator used instead of Mockito.
     */
    private static class ConstantIndicator implements Indicator<Num> {

        private final Num constant;

        ConstantIndicator(Num constant) {
            this.constant = constant;
        }

        @Override
        public Num getValue(int index) {
            return constant;
        }

        @Override
        public BarSeries getBarSeries() {
            return null;
        }

        @Override
        public int getUnstableBars() {
            // No unstable bars for this simple constant indicator
            return 0;
        }

        @Override
        public String toString() {
            return "ConstantIndicator(" + constant + ")";
        }
    }

    //     @BeforeAll
    static void setupBeforeAll() {
        // Deterministic Num implementation (DecimalNum) for reproducible values
        series = new BaseBarSeries("test-series");
        ZonedDateTime start = ZonedDateTime.now();
        Duration barDuration = Duration.ofMinutes(1);
        for (int i = 1; i <= 5; i++) {
            Num price = DecimalNum.valueOf(i);
            // BaseBar constructor: (Duration timePeriod, ZonedDateTime endTime,
            // double openPrice, double highPrice, double lowPrice,
            // double closePrice, double volume)
            BaseBar bar = new BaseBar(barDuration, start.plusMinutes(i - 1), // open
            price.doubleValue(), // high
            price.doubleValue(), // low
            price.doubleValue(), // close
            price.doubleValue(), // volume
            price.doubleValue());
            series.addBar(bar);
        }
        closePriceIndicator = new ClosePriceIndicator(series);
    }

    //     @BeforeEach
    void setupBeforeEach() {
        numericIndicator = NumericIndicator.of(closePriceIndicator);
    }

    //     @AfterAll
    static void teardownAfterAll() {
        series = null;
        closePriceIndicator = null;
    }

    void delegateReturnsOriginalIndicator() {
        Indicator<Num> delegate = numericIndicator.delegate();
        if (delegate != closePriceIndicator) {
            throw new AssertionError("Delegate should be the original indicator");
        }
    }

    void delegateWorksWithEmaIndicator() {
        EMAIndicator ema = new EMAIndicator(closePriceIndicator, 3);
        NumericIndicator numericEma = NumericIndicator.of(ema);
        if (numericEma.delegate() != ema) {
            throw new AssertionError("Delegate should be the EMA indicator");
        }
    }

    void delegateWorksWithConstantIndicator() {
        ConstantIndicator constant = new ConstantIndicator(DecimalNum.valueOf(10));
        NumericIndicator numericConst = NumericIndicator.of(constant);
        if (numericConst.delegate() != constant) {
            throw new AssertionError("Delegate should be the constant indicator");
        }
    }

    void delegateFromClosePriceFactoryIsClosePriceIndicator() {
        NumericIndicator fromFactory = NumericIndicator.closePrice(series);
        Indicator<Num> delegate = fromFactory.delegate();
        if (!(delegate instanceof ClosePriceIndicator)) {
            throw new AssertionError("Delegate should be a ClosePriceIndicator");
        }
        ClosePriceIndicator cpi = (ClosePriceIndicator) delegate;
        if (cpi.getBarSeries() != series) {
            throw new AssertionError("ClosePriceIndicator must wrap the same series");
        }
    }

    void delegateFromVolumeFactoryIsVolumeIndicator() {
        NumericIndicator fromFactory = NumericIndicator.volume(series);
        Indicator<Num> delegate = fromFactory.delegate();
        if (!(delegate instanceof VolumeIndicator)) {
            throw new AssertionError("Delegate should be a VolumeIndicator");
        }
        VolumeIndicator vi = (VolumeIndicator) delegate;
        if (vi.getBarSeries() != series) {
            throw new AssertionError("VolumeIndicator must wrap the same series");
        }
    }

    void delegateWithNullArgumentThrowsNPE() {
        try {
            NumericIndicator.of(null);
            throw new AssertionError("Expected NullPointerException");
        } catch (NullPointerException ignored) {
            // expected
        }
    }
}
