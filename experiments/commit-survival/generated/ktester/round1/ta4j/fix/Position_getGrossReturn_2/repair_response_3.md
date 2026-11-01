```java
package org.ta4j.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.util.stream.Stream;

/**
 * Tests for {@link Position#getGrossReturn(org.ta4j.core.BarSeries)}.
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

    @BeforeEach
    void setupBeforeEach() {
        // By default, entry is a BUY and exit is a SELL – this matches a normal closed position.
        position = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mocks to avoid cross‑test interference.
        reset(series, entryTrade, exitTrade);
    }

    private static Stream<Arguments> normalScenariosProvider() {
        return Stream.of(
                // Entry 100, Exit 150 → (150‑100)/100 = 0.5
                Arguments.of(DoubleNum.valueOf(100.0), DoubleNum.valueOf(150.0), DoubleNum.valueOf(0.5)),
                // Entry 200, Exit 100 → (100‑200)/200 = -0.5
                Arguments.of(DoubleNum.valueOf(200.0), DoubleNum.valueOf(100.0), DoubleNum.valueOf(-0.5)),
                // Entry 120, Exit 120 → 0
                Arguments.of(DoubleNum.valueOf(120.0), DoubleNum.valueOf(120.0), DoubleNum.valueOf(0.0))
        );
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
        Num exitPrice = DoubleNum.valueOf(12.5);
        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);
        // Act
        Num grossReturnFromSeries = position.getGrossReturn(series);
        Num grossReturnFromNums = position.getGrossReturn(entryPrice, exitPrice);
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
        Position shortPos = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);
        Num entryPrice = DoubleNum.valueOf(15.0); // price at which we sold
        Num exitPrice = DoubleNum.valueOf(13.0);  // price at which we bought back
        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);
        // Act
        Num grossReturnFromSeries = shortPos.getGrossReturn(series);
        Num grossReturnFromNums = shortPos.getGrossReturn(entryPrice, exitPrice);
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
        Num nan = Num.NaN;
        when(entryTrade.getPricePerAsset(series)).thenReturn(nan);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(5.0));
        // Act
        Num result = position.getGrossReturn(series);
        // Assert
        assertTrue(result.isNaN(), "Result should be NaN when entry price is NaN");
    }

    @ParameterizedTest(name = "{index} ⇒ entry={0}, exit={1}, expected={2}")
    @MethodSource("normalScenariosProvider")
    void grossReturnNormalScenarios(Num entryPrice, Num exitPrice, Num expected) {
        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);
        Num result = position.getGrossReturn(series);
        assertEquals(expected, result,
                () -> String.format("Gross return for entry %s and exit %s should be %s",
                        entryPrice, exitPrice, expected));
    }

    @Test
    void grossReturnWhenEntryPriceIsNaN_ShouldBeNaN() {
        when(entryTrade.getPricePerAsset(series)).thenReturn(Num.NaN);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(150.0));
        Num result = position.getGrossReturn(series);
        assertTrue(result.isNaN(), "Result must be NaN when entry price is NaN");
    }

    @Test
    void grossReturnWhenExitPriceIsNaN_ShouldBeNaN() {
        when(entryTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(100.0));
        when(exitTrade.getPricePerAsset(series)).thenReturn(Num.NaN);
        Num result = position.getGrossReturn(series);
        assertTrue(result.isNaN(), "Result must be NaN when exit price is NaN");
    }

    @Test
    void whenBarSeriesIsNull_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> position.getGrossReturn(null),
                "Passing a null BarSeries must result in NPE");
    }

    @Test
    void whenEntryTradeIsNull_ShouldThrowIllegalStateException() {
        Position pos = new Position(null, exitTrade, ZERO_COST, ZERO_COST);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(150.0));
        assertThrows(RuntimeException.class,
                () -> pos.getGrossReturn(series),
                "Position with null entry trade should raise an exception");
    }

    @Test
    void whenExitTradeIsNull_ShouldThrowIllegalStateException() {
        Position pos = new Position(entryTrade, null, ZERO_COST, ZERO_COST);
        when(entryTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(100.0));
        assertThrows(RuntimeException.class,
                () -> pos.getGrossReturn(series),
                "Position with null exit trade should raise an exception");
    }

    @Test
    void whenEntryPriceZeroExitPositive_ShouldThrowArithmeticException() {
        when(entryTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(0.0));
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(100.0));
        assertThrows(ArithmeticException.class,
                () -> position.getGrossReturn(series),
                "Division by zero (entry price = 0) must raise ArithmeticException");
    }

    @Test
    void whenEntryAndExitPriceZero_ShouldReturnNaN() {
        when(entryTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(0.0));
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(0.0));
        Num result = position.getGrossReturn(series);
        assertTrue(result.isNaN(), "Both prices zero should produce NaN (0‑0)/0");
    }

    @Test
    void grossReturnWithTinyEntryAndHugeExit_ShouldBeHugePositiveNumber() {
        Num entry = DoubleNum.valueOf(0.0001);
        Num exit = DoubleNum.valueOf(1000.0);
        // Expected = (1000‑0.0001)/0.0001 ≈ 9_999_900
        Num expected = DoubleNum.valueOf((1000.0 - 0.0001) / 0.0001);
        when(entryTrade.getPricePerAsset(series)).thenReturn(entry);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exit);
        Num result = position.getGrossReturn(series);
        assertEquals(expected, result, "Extreme values should be handled without overflow");
    }

    @Test
    void whenEntryPricePerAssetReturnsNull_ShouldThrowNullPointerException() {
        when(entryTrade.getPricePerAsset(series)).thenReturn(null);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(150.0));
        assertThrows(NullPointerException.class,
                () -> position.getGrossReturn(series),
                "Null entry price should cause NPE");
    }

    @Test
    void whenExitPricePerAssetReturnsNull_ShouldThrowNullPointerException() {
        when(entryTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(100.0));
        when(exitTrade.getPricePerAsset(series)).thenReturn(null);
        assertThrows(NullPointerException.class,
                () -> position.getGrossReturn(series),
                "Null exit price should cause NPE");
    }

    @Test
    void whenTradeThrowsRuntimeException_ShouldPropagate() {
        RuntimeException ex = new RuntimeException("boom");
        when(entryTrade.getPricePerAsset(series)).thenThrow(ex);
        // exit price is irrelevant – stub to avoid NPE if called
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(150.0));
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> position.getGrossReturn(series));
        assertSame(ex, thrown, "RuntimeException from trade must be propagated");
    }

    @Test
    void whenTradeThrowsIndexOutOfBoundsException_ShouldPropagate() {
        IndexOutOfBoundsException ex = new IndexOutOfBoundsException("no bars");
        when(entryTrade.getPricePerAsset(series)).thenThrow(ex);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(150.0));
        IndexOutOfBoundsException thrown = assertThrows(IndexOutOfBoundsException.class,
                () -> position.getGrossReturn(series));
        assertSame(ex, thrown, "IndexOutOfBoundsException must be propagated");
    }

    @Test
    void whenTradeThrowsIllegalArgumentException_ShouldPropagate() {
        IllegalArgumentException ex = new IllegalArgumentException("bad index");
        when(entryTrade.getPricePerAsset(series)).thenThrow(ex);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(150.0));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> position.getGrossReturn(series));
        assertSame(ex, thrown, "IllegalArgumentException must be propagated");
    }
}
```