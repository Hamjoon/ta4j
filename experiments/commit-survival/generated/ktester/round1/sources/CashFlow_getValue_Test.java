package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.function.Function;
import org.junit.jupiter.api.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.Trade;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.mocks.MockBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MockitoExtension.class)
class CashFlow_getValue_Test {

    /**
     * Function used by {@link MockBarSeries} to create {@link Num} instances.
     */
    private static Function<Number, Num> numFunction;

    /**
     * Series used in the individual tests.
     */
    private BarSeries series;

    /**
     * CashFlow instance under test.
     */
    private CashFlow cashFlow;

    @BeforeAll
    static void setupBeforeAll() {
        // DecimalNum is the default implementation used by the library.
        numFunction = DecimalNum::valueOf;
    }

    @BeforeEach
    void setupBeforeEach() {
        // The concrete test data are created inside each test method,
        // therefore we only reset the fields here.
        series = null;
        cashFlow = null;
    }

    @AfterEach
    void teardownAfterEach() {
        // Help the garbage collector – not strictly required but mirrors the
        // template’s intent.
        series = null;
        cashFlow = null;
    }

    /**
     * Helper to compare a {@link Num} with a primitive double value.
     */
    private static void assertNumEquals(double expected, Num actual) {
        assertEquals(DecimalNum.valueOf(expected), actual);
    }

    /**
     * Helper to compare a {@link Num} with a string representation (used in some
     *  original tests).
     */
    private static void assertNumEquals(String expected, Num actual) {
        assertEquals(DecimalNum.valueOf(expected), actual);
    }

    /**
     * Utility that injects a value into the private {@code values} field.
     */
    private static void setValuesField(CashFlow cf, List<Num> list) {
        try {
            Field f = CashFlow.class.getDeclaredField("values");
            f.setAccessible(true);
            f.set(cf, list);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getValue_returnsInitialOneWhenNoPositions() {
        // series with five bars, no trades (empty BaseTradingRecord)
        series = new MockBarSeries(numFunction, 1d, 2d, 3d, 4d, 5d);
        cashFlow = new CashFlow(series, new BaseTradingRecord());
        // size should match the series length
        assertEquals(5, cashFlow.getSize());
        // every entry must be 1 (the initial value)
        for (int i = 0; i < cashFlow.getSize(); i++) {
            assertNumEquals(1.0, cashFlow.getValue(i));
        }
    }

    @Test
    void getValue_reflectsSimpleBuySellPosition() {
        // two‑bar series, a single BUY → SELL position
        series = new MockBarSeries(numFunction, 1d, 2d);
        TradingRecord record = new BaseTradingRecord(Trade.buyAt(0, series), Trade.sellAt(1, series));
        cashFlow = new CashFlow(series, record);
        // Expected cash‑flow values are the raw price ratios (see original tests)
        assertNumEquals(1.0, cashFlow.getValue(0));
        assertNumEquals(2.0, cashFlow.getValue(1));
    }

    @Test
    void getValue_handlesMixedBuySellTrades() {
        // series with a more complex pattern (buy‑sell‑buy‑sell‑sell‑buy)
        series = new MockBarSeries(numFunction, 2, 1, 3, 5, 6, 3, 20);
        TradingRecord record = new BaseTradingRecord(Trade.buyAt(0, series), Trade.sellAt(1, series), Trade.buyAt(3, series), Trade.sellAt(4, series), Trade.sellAt(5, series), Trade.buyAt(6, series));
        cashFlow = new CashFlow(series, record);
        // Expected values are taken from the original CashFlowTest
        assertNumEquals(1.0, cashFlow.getValue(0));
        assertNumEquals("0.5", cashFlow.getValue(1));
        assertNumEquals("0.5", cashFlow.getValue(2));
        assertNumEquals("0.5", cashFlow.getValue(3));
        assertNumEquals("0.6", cashFlow.getValue(4));
        assertNumEquals("0.6", cashFlow.getValue(5));
        assertNumEquals("-2.8", cashFlow.getValue(6));
    }

    @Test
    void getValue_correctForSellThenBuyScenario() {
        // series where the first trade is a SELL (short) followed by a BUY
        series = new MockBarSeries(numFunction, 1, 2, 4, 8, 16, 32);
        TradingRecord record = new BaseTradingRecord(Trade.sellAt(2, series), Trade.buyAt(3, series));
        cashFlow = new CashFlow(series, record);
        // Values are reproduced from the original CashFlowTest (selected few)
        assertNumEquals(1.0, cashFlow.getValue(0));
        assertNumEquals(1.0, cashFlow.getValue(1));
        assertNumEquals(2d / 3, cashFlow.getValue(2));
        assertNumEquals(5d / 3, cashFlow.getValue(3));
        assertNumEquals(5d / 3, cashFlow.getValue(4));
        assertNumEquals(5d / 3, cashFlow.getValue(5));
    }

    @Test
    void getValue_stableForVeryLargeSeries() {
        // 1 000 000 identical bars, a single long position covering the whole series
        int size = 1_000_000;
        series = new MockBarSeries(Collections.nCopies(size, new MockBar(10, numFunction)));
        TradingRecord record = new BaseTradingRecord(Trade.buyAt(0, series), Trade.sellAt(size - 1, series));
        cashFlow = new CashFlow(series, record);
        // The cash‑flow at the last index must be 1 (no profit/loss)
        assertNumEquals(1.0, cashFlow.getValue(size - 1));
    }

    @ParameterizedTest(name = "{index} ⇒ index={0}, expected={1}")
    @CsvSource({ "0, 100", "2, 300", "4, 500" })
    void getValue_validIndexRetrieval(int index, double expected) {
        // Prepare a CashFlow instance with a known list of Num values.
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        List<Num> list = new ArrayList<>();
        list.add(DecimalNum.valueOf(100));
        list.add(DecimalNum.valueOf(200));
        list.add(DecimalNum.valueOf(300));
        list.add(DecimalNum.valueOf(400));
        list.add(DecimalNum.valueOf(500));
        setValuesField(cashFlow, list);
        assertNumEquals(expected, cashFlow.getValue(index));
    }

    @Test
    void getValue_negativeIndexThrows() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        setValuesField(cashFlow, List.of(DecimalNum.valueOf(10)));
        assertThrows(IndexOutOfBoundsException.class, () -> cashFlow.getValue(-1));
    }

    @Test
    void getValue_indexEqualToSizeThrows() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        setValuesField(cashFlow, List.of(DecimalNum.valueOf(10), DecimalNum.valueOf(20)));
        assertThrows(IndexOutOfBoundsException.class, // size == 2
        () -> cashFlow.getValue(2));
    }

    @Test
    void getValue_indexGreaterThanSizeThrows() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        setValuesField(cashFlow, List.of(DecimalNum.valueOf(10), DecimalNum.valueOf(20)));
        assertThrows(IndexOutOfBoundsException.class, () -> cashFlow.getValue(10));
    }

    @Test
    void getValue_emptyListThrows() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        setValuesField(cashFlow, Collections.emptyList());
        assertThrows(IndexOutOfBoundsException.class, () -> cashFlow.getValue(0));
    }

    @Test
    void getValue_singleElementList() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        setValuesField(cashFlow, List.of(DecimalNum.valueOf(42)));
        assertNumEquals(42, cashFlow.getValue(0));
    }

    @Test
    void getValue_nullElementRetrieval() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        List<Num> list = new ArrayList<>();
        list.add(DecimalNum.valueOf(10));
        // explicit null entry at index 1
        list.add(null);
        setValuesField(cashFlow, list);
        assertNull(cashFlow.getValue(1));
    }

    @Test
    void getValue_nullInternalListThrows() {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        // Force the private field to be null.
        setValuesField(cashFlow, null);
        assertThrows(NullPointerException.class, () -> cashFlow.getValue(0));
    }

    @ParameterizedTest(name = "Extreme index {0}")
    @CsvSource({ "2147483647", "-2147483648" })
    void getValue_extremeIndexValuesThrow(int extremeIndex) {
        cashFlow = new CashFlow(new MockBarSeries(numFunction, 1d), new BaseTradingRecord());
        setValuesField(cashFlow, List.of(DecimalNum.valueOf(1), DecimalNum.valueOf(2)));
        assertThrows(IndexOutOfBoundsException.class, () -> cashFlow.getValue(extremeIndex));
    }
}
