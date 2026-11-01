```java
package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link CashFlow#getUnstableBars()}.
 *
 * The current implementation of {@code CashFlow#getUnstableBars()} always returns {@code 0},
 * therefore the tests only verify that this contract holds for the different public
 * constructors of {@code CashFlow}.
 */
@ExtendWith(MockitoExtension.class)
class CashFlow_getUnstableBars_Test {

    /** Function used to create {@link Num} instances from primitive numbers. */
    private static Function<Number, Num> numFunction;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default Num implementation used by the library.
        numFunction = DecimalNum::valueOf;
    }

    @BeforeEach
    void setupBeforeEach() {
        // No per‑test initialisation required at the moment.
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    @Test
    void getUnstableBars_returnsZero_whenCreatedWithBarSeriesAndPosition() {
        // given
        BarSeries series = new MockBarSeries(numFunction, 1d, 2d, 3d);
        Position position = new Position(); // default BUY position with no trades

        // when
        CashFlow cashFlow = new CashFlow(series, position);
        int unstable = cashFlow.getUnstableBars();

        // then
        assertEquals(0, unstable, "CashFlow#getUnstableBars() should return 0 for the Position constructor");
    }

    @Test
    void getUnstableBars_returnsZero_whenCreatedWithBarSeriesAndTradingRecord() {
        // given
        BarSeries series = new MockBarSeries(numFunction, 10d, 20d, 30d);
        TradingRecord record = new TradingRecord(); // empty record (no positions)

        // when
        CashFlow cashFlow = new CashFlow(series, record);
        int unstable = cashFlow.getUnstableBars();

        // then
        assertEquals(0, unstable,
                "CashFlow#getUnstableBars() should return 0 for the TradingRecord constructor");
    }

    @Test
    void getUnstableBars_returnsZero_whenCreatedWithBarSeriesTradingRecordAndFinalIndex() {
        // given
        BarSeries series = new MockBarSeries(numFunction,
                // a relatively long series to ensure the final index is meaningful
                5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d);
        TradingRecord record = new TradingRecord(); // empty record
        int finalIndex = series.getEndIndex(); // use the series' natural end index

        // when
        CashFlow cashFlow = new CashFlow(series, record, finalIndex);
        int unstable = cashFlow.getUnstableBars();

        // then
        assertEquals(0, unstable,
                "CashFlow#getUnstableBars() should return 0 for the three‑argument constructor");
    }
}
```