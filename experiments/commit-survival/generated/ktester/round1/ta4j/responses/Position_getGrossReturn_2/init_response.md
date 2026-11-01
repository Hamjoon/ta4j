```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

/**
 * Tests for {@link Position#getGrossReturn(BarSeries)}.
 *
 * The method should retrieve the entry‑price and exit‑price from the
 * underlying {@link Trade}s using the supplied {@link BarSeries} and then
 * delegate to {@link Position#getGrossReturn(Num, Num)}.
 *
 * The tests use Mockito to stub the {@link Trade#getPricePerAsset(BarSeries)}
 * calls so that we can verify the delegation without needing a real
 * {@link BarSeries} implementation.
 */
@ExtendWith(MockitoExtension.class)
class Position_getGrossReturn_2_Test {

    /** Mocked series that will be passed to the position. */
    @Mock
    BarSeries series;

    /** Mocked entry trade. */
    @Mock
    Trade entryTrade;

    /** Mocked exit trade. */
    @Mock
    Trade exitTrade;

    /** Position under test. */
    Position position;

    /** Simple cost models required by the {@link Position} constructor. */
    private static final CostModel ZERO_COST = new ZeroCostModel();

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed.
    }

    @BeforeEach
    void setupBeforeEach() {
        // By default, entry is a BUY and exit is a SELL – this matches a
        // normal closed position.
        position = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks to avoid cross‑test interference.
        reset(series, entryTrade, exitTrade);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    /**
     * Verifies that {@code getGrossReturn(series)} returns the same value as
     * {@code getGrossReturn(entryPrice, exitPrice)} when the entry and exit
     * trades provide deterministic prices.
     */
    @Test
    void whenClosedPositionShouldDelegateToNumVersion() {
        // Arrange – stub the price returned by each trade.
        Num entryPrice = DoubleNum.valueOf(10.0);
        Num exitPrice  = DoubleNum.valueOf(12.5);

        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);

        // Act
        Num grossReturnFromSeries = position.getGrossReturn(series);
        Num grossReturnFromNums   = position.getGrossReturn(entryPrice, exitPrice);

        // Assert – both results must be equal.
        assertEquals(grossReturnFromNums, grossReturnFromSeries,
                "Position#getGrossReturn(BarSeries) should delegate to the Num‑based method");
    }

    /**
     * The same delegation test but with a short (SELL‑first) position.
     */
    @Test
    void whenShortClosedPositionShouldDelegateCorrectly() {
        // Arrange – create a short position (entry SELL, exit BUY).
        // For clarity we rebuild the Position with the same mocks.
        Position shortPos = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);

        Num entryPrice = DoubleNum.valueOf(15.0); // price at which we sold
        Num exitPrice  = DoubleNum.valueOf(13.0); // price at which we bought back

        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);

        // Act
        Num grossReturnFromSeries = shortPos.getGrossReturn(series);
        Num grossReturnFromNums   = shortPos.getGrossReturn(entryPrice, exitPrice);

        // Assert
        assertEquals(grossReturnFromNums, grossReturnFromSeries,
                "Short position should also delegate correctly to the Num‑based method");
    }

    /**
     * If either trade returns {@code NaN}, the gross return should also be {@code NaN}.
     * This mirrors the behaviour of the underlying Num implementation.
     */
    @Test
    void whenPriceIsNaNResultShouldBeNaN() {
        // Arrange
        Num nan = NaN;
        when(entryTrade.getPricePerAsset(series)).thenReturn(nan);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(5.0));

        // Act
        Num result = position.getGrossReturn(series);

        // Assert
        assertTrue(result.isNaN(), "Result should be NaN when entry price is NaN");
    }
}
```