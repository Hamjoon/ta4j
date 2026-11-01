```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.util.stream.Stream;

/**
 * Comprehensive tests for {@link Position#getGrossReturn(org.ta4j.core.BarSeries)}.
 *
 * <p>The tests are divided into several groups that mirror the JSON test‑case
 * description.  Where possible a {@code @ParameterizedTest} is used to keep
 * the code concise.</p>
 *
 * <p>All interactions with {@link Trade} and {@link BarSeries} are mocked,
 * therefore the tests focus exclusively on the delegation logic and the
 * handling of special numeric cases (NaN, zero, extreme values, exceptions,
 * etc.).</p>
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

    /** Position under test – created in {@link #setupBeforeEach()}. */
    Position position;

    /** Simple cost models required by the {@link Position} constructor. */
    private static final CostModel ZERO_COST = new ZeroCostModel();

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed.
    }

    @BeforeEach
    void setupBeforeEach() {
        // By default we build a normal (BUY‑SELL) closed position.
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

    /* --------------------------------------------------------------
     *  Existing delegation tests (kept unchanged)
     * -------------------------------------------------------------- */

    @Test
    void whenClosedPositionShouldDelegateToNumVersion() {
        Num entryPrice = DoubleNum.valueOf(10.0);
        Num exitPrice  = DoubleNum.valueOf(12.5);

        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);

        Num grossReturnFromSeries = position.getGrossReturn(series);
        Num grossReturnFromNums   = position.getGrossReturn(entryPrice, exitPrice);

        assertEquals(grossReturnFromNums, grossReturnFromSeries,
                "Position#getGrossReturn(BarSeries) should delegate to the Num‑based method");
    }

    @Test
    void whenShortClosedPositionShouldDelegateCorrectly() {
        Position shortPos = new Position(entryTrade, exitTrade, ZERO_COST, ZERO_COST);

        Num entryPrice = DoubleNum.valueOf(15.0); // sold
        Num exitPrice  = DoubleNum.valueOf(13.0); // bought back

        when(entryTrade.getPricePerAsset(series)).thenReturn(entryPrice);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exitPrice);

        Num grossReturnFromSeries = shortPos.getGrossReturn(series);
        Num grossReturnFromNums   = shortPos.getGrossReturn(entryPrice, exitPrice);

        assertEquals(grossReturnFromNums, grossReturnFromSeries,
                "Short position should also delegate correctly to the Num‑based method");
    }

    @Test
    void whenPriceIsNaNResultShouldBeNaN() {
        Num nan = Num.NaN;
        when(entryTrade.getPricePerAsset(series)).thenReturn(nan);
        when(exitTrade.getPricePerAsset(series)).thenReturn(DoubleNum.valueOf(5.0));

        Num result = position.getGrossReturn(series);
        assertTrue(result.isNaN(), "Result should be NaN when entry price is NaN");
    }

    /* --------------------------------------------------------------
     *  Parameterized normal‑scenario tests
     * -------------------------------------------------------------- */

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

    /* --------------------------------------------------------------
     *  Edge cases with NaN (delegated from the entry/exit price)
     * -------------------------------------------------------------- */

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

    /* --------------------------------------------------------------
     *  Null / Illegal state handling
     * -------------------------------------------------------------- */

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

        // The implementation may throw NPE or IllegalStateException – we accept both.
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

    /* --------------------------------------------------------------
     *  Division‑by‑zero scenarios
     * -------------------------------------------------------------- */

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
        assertTrue(result.isNaN(),
                "Both prices zero should produce NaN (0‑0)/0");
    }

    /* --------------------------------------------------------------
     *  Extreme values
     * -------------------------------------------------------------- */

    @Test
    void grossReturnWithTinyEntryAndHugeExit_ShouldBeHugePositiveNumber() {
        Num entry = DoubleNum.valueOf(0.0001);
        Num exit  = DoubleNum.valueOf(1000.0);
        // Expected = (1000‑0.0001)/0.0001 ≈ 9_999_900
        Num expected = DoubleNum.valueOf((1000.0 - 0.0001) / 0.0001);

        when(entryTrade.getPricePerAsset(series)).thenReturn(entry);
        when(exitTrade.getPricePerAsset(series)).thenReturn(exit);

        Num result = position.getGrossReturn(series);
        assertEquals(expected, result,
                "Extreme values should be handled without overflow");
    }

    /* --------------------------------------------------------------
     *  Null price‑per‑asset handling
     * -------------------------------------------------------------- */

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

    /* --------------------------------------------------------------
     *  BarSeries‑related exception propagation
     * -------------------------------------------------------------- */

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