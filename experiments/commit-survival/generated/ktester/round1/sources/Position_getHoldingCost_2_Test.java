package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.ta4j.core.num.NaN.NaN;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Tests for {@link Position#getHoldingCost(int)} covering a wide range of
 * {@link CostModel} behaviours.
 */
@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_2_Test {

    /**
     * Position that uses a mocked {@link CostModel} for holding costs.
     */
    private Position positionWithMockModel;

    /**
     * Mocked holding‑cost model.
     */
    @Mock
    private CostModel holdingCostModelMock;

    /**
     * Position that uses the default {@link ZeroCostModel}.
     */
    private Position positionWithZeroModel;

    // ------------------------------------------------------------------------
    private static class SimpleConditionalCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            if (finalIndex == 0) {
                return DoubleNum.valueOf(0);
            }
            return DoubleNum.valueOf(finalIndex);
        }
    }

    // ------------------------------------------------------------------------
    private static class ThrowsOnNegativeCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            if (finalIndex < 0) {
                throw new IllegalArgumentException("finalIndex must be >= 0");
            }
            return DoubleNum.valueOf(finalIndex);
        }
    }

    // ------------------------------------------------------------------------
    private static class NullReturningCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    private static class NaNAtSpecificIndexCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            return finalIndex == 42 ? NaN : DoubleNum.valueOf(finalIndex);
        }
    }

    // ------------------------------------------------------------------------
    private static class PassThroughCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            return DoubleNum.valueOf(finalIndex);
        }
    }

    // ------------------------------------------------------------------------
    private static class EntryPriceTimesIndexCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            // For the purpose of the test we assume entry price = 100.
            Num entryPrice = DoubleNum.valueOf(100);
            return entryPrice.multipliedBy(DoubleNum.valueOf(finalIndex));
        }
    }

    // ------------------------------------------------------------------------
    private static class AlwaysThrowsIllegalStateCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            throw new IllegalStateException("forced");
        }
    }

    private static class ThrowsOnNegativeArithmeticCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            if (finalIndex < 0) {
                throw new ArithmeticException("negative index");
            }
            return DoubleNum.valueOf(finalIndex);
        }
    }

    // ------------------------------------------------------------------------
    private static class ThrowsNPECostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            throw new NullPointerException("internal NPE");
        }
    }

    // ------------------------------------------------------------------------
    private static class ThrowsErrorCostModel implements CostModel {

        @Override
        public Num calculate(Position position, int finalIndex) {
            throw new AssertionError("assertion failed");
        }
    }

    @BeforeEach
    void setupBeforeEach() {
        // Transaction‑cost model is irrelevant for holding‑cost tests,
        // therefore we use the simple ZeroCostModel.
        CostModel transactionModel = new ZeroCostModel();

        // Build a Position that uses the mocked holding‑cost model.
        positionWithMockModel = new Position(Trade.TradeType.BUY, transactionModel, holdingCostModelMock);

        // Position that relies on the library’s default ZeroCostModel.
        positionWithZeroModel = new Position();
    }

    private static Stream<Arguments> simpleConditionalCases() {
        return Stream.of(
                Arguments.of(0, "0"),
                Arguments.of(5, "5"),
                Arguments.of(-3, "-3"));
    }

    private static Stream<Arguments> nanSpecificIndexCases() {
        return Stream.of(
                Arguments.of(42, "NaN"),
                Arguments.of(41, "41"));
    }

    private static Stream<Arguments> extremeIndexCases() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE));
    }

    private static Stream<Arguments> stateDependentCases() {
        return Stream.of(
                Arguments.of(2, "200"),
                Arguments.of(0, "0"));
    }

    @Test
    void whenHoldingCostModelIsMocked_thenResultIsReturnedFromModel() {
        // GIVEN
        int finalIndex = 7;
        Num expected = DoubleNum.valueOf(3.1415);
        when(holdingCostModelMock.calculate(positionWithMockModel, finalIndex)).thenReturn(expected);
        // WHEN
        Num actual = positionWithMockModel.getHoldingCost(finalIndex);
        // THEN
        assertEquals(expected, actual,
                "Position#getHoldingCost should return the value supplied by the holding‑cost model");
        verify(holdingCostModelMock, times(1)).calculate(positionWithMockModel, finalIndex);
    }

    @Test
    void whenUsingDefaultZeroCostModel_thenHoldingCostIsZero() {
        // GIVEN
        int finalIndex = 12;
        Num expectedZero = DoubleNum.valueOf(0);
        // WHEN
        Num actual = positionWithZeroModel.getHoldingCost(finalIndex);
        // THEN
        assertEquals(expectedZero, actual,
                "Default ZeroCostModel must produce a holding cost of zero");
    }

    // ------------------------------------------------------------------------
    @ParameterizedTest(name = "ZeroCostModel returns NaN for finalIndex={0}")
    @ValueSource(ints = {10, -5})
    void zeroCostModelReturnsNaN(int finalIndex) {
        Num actual = positionWithZeroModel.getHoldingCost(finalIndex);
        assertEquals(NaN, actual,
                "ZeroCostModel must produce NaN regardless of the supplied finalIndex");
    }

    @ParameterizedTest(name = "SimpleConditionalCostModel finalIndex={0} => expected={1}")
    @MethodSource("simpleConditionalCases")
    void customSimpleConditionalCostModel(int finalIndex, String expectedStr) {
        CostModel model = new SimpleConditionalCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        Num expected = expectedStr.equals("NaN") ? NaN : DoubleNum.valueOf(Double.parseDouble(expectedStr));
        Num actual = pos.getHoldingCost(finalIndex);
        assertEquals(expected, actual);
    }

    @Test
    void customCostModelThrowsIllegalArgumentException() {
        CostModel model = new ThrowsOnNegativeCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        assertThrows(IllegalArgumentException.class,
                () -> pos.getHoldingCost(-1),
                "The custom CostModel should propagate IllegalArgumentException");
    }

    // ------------------------------------------------------------------------
    @Test
    void positionWithNullHoldingCostModelThrowsNPE() throws Exception {
        // Build a normal Position first.
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());

        // Use reflection to nullify the private final field.
        Field field = Position.class.getDeclaredField("holdingCostModel");
        field.setAccessible(true);
        field.set(pos, null);

        assertThrows(NullPointerException.class,
                () -> pos.getHoldingCost(3),
                "Calling getHoldingCost on a Position with null holdingCostModel must throw NPE");
    }

    @Test
    void customCostModelReturnsNull() {
        CostModel model = new NullReturningCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        assertNull(pos.getHoldingCost(7),
                "Position#getHoldingCost should forward a null value returned by the CostModel");
    }

    @ParameterizedTest(name = "NaNAtSpecificIndexCostModel finalIndex={0} => expected={1}")
    @MethodSource("nanSpecificIndexCases")
    void customCostModelReturnsNaNForSpecificIndex(int finalIndex, String expectedStr) {
        CostModel model = new NaNAtSpecificIndexCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        Num expected = expectedStr.equals("NaN") ? NaN : DoubleNum.valueOf(Double.parseDouble(expectedStr));
        Num actual = pos.getHoldingCost(finalIndex);
        assertEquals(expected, actual);
    }

    // ------------------------------------------------------------------------
    @Test
    void costModelReceivesCorrectPositionReference() {
        // Arrange – mock CostModel that captures the Position argument.
        CostModel model = mock(CostModel.class);
        when(model.calculate(any(Position.class), eq(3))).thenReturn(DoubleNum.valueOf(0));

        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        // Act
        pos.getHoldingCost(3);
        // Assert – capture the Position argument.
        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        verify(model, times(1)).calculate(captor.capture(), eq(3));
        assertSame(pos, captor.getValue(),
                "The Position instance passed to CostModel.calculate must be the same object on which getHoldingCost was invoked");
    }

    @ParameterizedTest(name = "Extreme index {0} should be passed through")
    @MethodSource("extremeIndexCases")
    void extremeIndexValuesAreForwarded(int finalIndex) {
        CostModel model = new PassThroughCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        Num expected = DoubleNum.valueOf(finalIndex);
        assertEquals(expected, pos.getHoldingCost(finalIndex));
    }

    @ParameterizedTest(name = "State‑dependent model finalIndex={0} => expected={1}")
    @MethodSource("stateDependentCases")
    void costModelDependingOnPositionState(int finalIndex, String expectedStr) {
        CostModel model = new EntryPriceTimesIndexCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        Num expected = DoubleNum.valueOf(Double.parseDouble(expectedStr));
        assertEquals(expected, pos.getHoldingCost(finalIndex));
    }

    @Test
    void costModelThrowsIllegalStateException() {
        CostModel model = new AlwaysThrowsIllegalStateCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        assertThrows(IllegalStateException.class,
                () -> pos.getHoldingCost(5),
                "Unchecked IllegalStateException must propagate unchanged");
    }

    @Test
    void costModelThrowsArithmeticExceptionOnNegativeIndex() {
        CostModel model = new ThrowsOnNegativeArithmeticCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        assertThrows(ArithmeticException.class,
                () -> pos.getHoldingCost(-2),
                "ArithmeticException must propagate unchanged");
    }

    @Test
    void costModelThrowsNullPointerExceptionInternally() {
        CostModel model = new ThrowsNPECostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        assertThrows(NullPointerException.class,
                () -> pos.getHoldingCost(0),
                "NullPointerException from CostModel must propagate");
    }

    @Test
    void costModelThrowsErrorIsPropagated() {
        CostModel model = new ThrowsErrorCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), model);
        assertThrows(AssertionError.class,
                () -> pos.getHoldingCost(1),
                "Errors must not be caught; they should propagate to the caller");
    }
}