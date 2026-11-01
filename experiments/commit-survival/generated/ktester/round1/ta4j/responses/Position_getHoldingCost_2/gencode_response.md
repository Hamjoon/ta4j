```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.ta4j.core.num.NaN.NaN;

/**
 * Comprehensive unit tests for {@link Position#getHoldingCost(int)}.
 *
 * <p>
 * The method merely delegates to the {@link CostModel} supplied at construction
 * time. The test suite therefore focuses on the contract between {@code Position}
 * and the {@code CostModel} implementation:
 * <ul>
 *   <li>correct delegation of arguments,</li>
 *   <li>proper propagation of return values (including {@code NaN} and {@code null}),</li>
 *   <li>exception bubbling, and</li>
 *   <li>behaviour when the holding‑cost model itself is {@code null}.</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_2_Test {

    /** Position that uses a mocked {@link CostModel} for holding costs. */
    private Position positionWithMockModel;

    /** Mocked holding‑cost model. */
    @Mock
    private CostModel holdingCostModelMock;

    /** Position that relies on the library’s default {@link ZeroCostModel}. */
    private Position positionWithZeroModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these tests.
    }

    @BeforeEach
    void setupBeforeEach() {
        // --------------------------------------------------------------------
        // Position that delegates to a mocked CostModel.
        // --------------------------------------------------------------------
        CostModel transactionModel = new ZeroCostModel(); // transaction cost is irrelevant here
        positionWithMockModel = new Position(Trade.TradeType.BUY,
                                            transactionModel,
                                            holdingCostModelMock);

        // --------------------------------------------------------------------
        // Position that relies on the library’s default ZeroCostModel.
        // --------------------------------------------------------------------
        positionWithZeroModel = new Position(); // uses ZeroCostModel internally
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required.
    }

    // ------------------------------------------------------------------------
    // 1. Default ZeroCostModel should return NaN for any index.
    // ------------------------------------------------------------------------
    @ParameterizedTest(name = "ZeroCostModel returns NaN for finalIndex={0}")
    @ValueSource(ints = {10, -5})
    void zeroCostModelReturnsNaN(int finalIndex) {
        Num actual = positionWithZeroModel.getHoldingCost(finalIndex);
        assertEquals(NaN, actual,
                "ZeroCostModel must produce NaN regardless of the supplied finalIndex");
    }

    // ------------------------------------------------------------------------
    // 2. Custom CostModel with simple conditional logic.
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

    @ParameterizedTest(name = "SimpleConditionalCostModel finalIndex={0} => expected={1}")
    @MethodSource("simpleConditionalCases")
    void customSimpleConditionalCostModel(int finalIndex, String expectedStr) {
        CostModel model = new SimpleConditionalCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        Num expected = expectedStr.equals("NaN") ? NaN : DoubleNum.valueOf(Double.parseDouble(expectedStr));
        Num actual = pos.getHoldingCost(finalIndex);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> simpleConditionalCases() {
        return Stream.of(
                Arguments.of(0, "0"),
                Arguments.of(5, "5"),
                Arguments.of(-3, "-3")
        );
    }

    // ------------------------------------------------------------------------
    // 3. Custom CostModel that throws IllegalArgumentException for negative index.
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

    @Test
    void customCostModelThrowsIllegalArgumentException() {
        CostModel model = new ThrowsOnNegativeCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        assertThrows(IllegalArgumentException.class,
                () -> pos.getHoldingCost(-1),
                "The custom CostModel should propagate IllegalArgumentException");
    }

    // ------------------------------------------------------------------------
    // 4. Position with a null holdingCostModel should raise NPE.
    // ------------------------------------------------------------------------
    @Test
    void positionWithNullHoldingCostModelThrowsNPE() throws Exception {
        // Build a normal Position first.
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    new ZeroCostModel());

        // Use reflection to nullify the private final field.
        Field field = Position.class.getDeclaredField("holdingCostModel");
        field.setAccessible(true);
        field.set(pos, null);

        assertThrows(NullPointerException.class,
                () -> pos.getHoldingCost(3),
                "Calling getHoldingCost on a Position with null holdingCostModel must throw NPE");
    }

    // ------------------------------------------------------------------------
    // 5. Custom CostModel returning null.
    // ------------------------------------------------------------------------
    private static class NullReturningCostModel implements CostModel {
        @Override
        public Num calculate(Position position, int finalIndex) {
            return null;
        }
    }

    @Test
    void customCostModelReturnsNull() {
        CostModel model = new NullReturningCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        assertNull(pos.getHoldingCost(7),
                "Position#getHoldingCost should forward a null value returned by the CostModel");
    }

    // ------------------------------------------------------------------------
    // 6. Custom CostModel returning NaN for a specific index (42).
    // ------------------------------------------------------------------------
    private static class NaNAtSpecificIndexCostModel implements CostModel {
        @Override
        public Num calculate(Position position, int finalIndex) {
            return finalIndex == 42 ? NaN : DoubleNum.valueOf(finalIndex);
        }
    }

    @ParameterizedTest(name = "NaNAtSpecificIndexCostModel finalIndex={0} => expected={1}")
    @MethodSource("nanSpecificIndexCases")
    void customCostModelReturnsNaNForSpecificIndex(int finalIndex, String expectedStr) {
        CostModel model = new NaNAtSpecificIndexCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        Num expected = expectedStr.equals("NaN") ? NaN : DoubleNum.valueOf(Double.parseDouble(expectedStr));
        Num actual = pos.getHoldingCost(finalIndex);
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> nanSpecificIndexCases() {
        return Stream.of(
                Arguments.of(42, "NaN"),
                Arguments.of(41, "41")
        );
    }

    // ------------------------------------------------------------------------
    // 7. Verify that the CostModel receives the correct Position reference.
    // ------------------------------------------------------------------------
    @Test
    void costModelReceivesCorrectPositionReference() {
        // Arrange – mock CostModel that captures the Position argument.
        CostModel model = mock(CostModel.class);
        when(model.calculate(any(Position.class), eq(3))).thenReturn(DoubleNum.valueOf(0));

        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);

        // Act
        pos.getHoldingCost(3);

        // Assert – capture the Position argument.
        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        verify(model, times(1)).calculate(captor.capture(), eq(3));
        assertSame(pos, captor.getValue(),
                "The Position instance passed to CostModel.calculate must be the same object on which getHoldingCost was invoked");
    }

    // ------------------------------------------------------------------------
    // 8. Extreme index values (Integer.MAX_VALUE / Integer.MIN_VALUE).
    // ------------------------------------------------------------------------
    private static class PassThroughCostModel implements CostModel {
        @Override
        public Num calculate(Position position, int finalIndex) {
            return DoubleNum.valueOf(finalIndex);
        }
    }

    @ParameterizedTest(name = "Extreme index {0} should be passed through")
    @MethodSource("extremeIndexCases")
    void extremeIndexValuesAreForwarded(int finalIndex) {
        CostModel model = new PassThroughCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        Num expected = DoubleNum.valueOf(finalIndex);
        assertEquals(expected, pos.getHoldingCost(finalIndex));
    }

    private static Stream<Arguments> extremeIndexCases() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE)
        );
    }

    // ------------------------------------------------------------------------
    // 9. CostModel that depends on Position state (entry price * finalIndex).
    // ------------------------------------------------------------------------
    private static class EntryPriceTimesIndexCostModel implements CostModel {
        @Override
        public Num calculate(Position position, int finalIndex) {
            // For the purpose of the test we assume entry price = 100.
            // In a real implementation we would read position.getEntry().getPrice().
            Num entryPrice = DoubleNum.valueOf(100);
            return entryPrice.multipliedBy(DoubleNum.valueOf(finalIndex));
        }
    }

    @ParameterizedTest(name = "State‑dependent model finalIndex={0} => expected={1}")
    @MethodSource("stateDependentCases")
    void costModelDependingOnPositionState(int finalIndex, String expectedStr) {
        CostModel model = new EntryPriceTimesIndexCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        Num expected = DoubleNum.valueOf(Double.parseDouble(expectedStr));
        assertEquals(expected, pos.getHoldingCost(finalIndex));
    }

    private static Stream<Arguments> stateDependentCases() {
        return Stream.of(
                Arguments.of(2, "200"),
                Arguments.of(0, "0")
        );
    }

    // ------------------------------------------------------------------------
    // 10. CostModel throws unchecked exceptions (IllegalStateException, ArithmeticException).
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

    @Test
    void costModelThrowsIllegalStateException() {
        CostModel model = new AlwaysThrowsIllegalStateCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        assertThrows(IllegalStateException.class,
                () -> pos.getHoldingCost(5),
                "Unchecked IllegalStateException must propagate unchanged");
    }

    @Test
    void costModelThrowsArithmeticExceptionOnNegativeIndex() {
        CostModel model = new ThrowsOnNegativeArithmeticCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        assertThrows(ArithmeticException.class,
                () -> pos.getHoldingCost(-2),
                "ArithmeticException must propagate unchanged");
    }

    // ------------------------------------------------------------------------
    // 11. CostModel throws NullPointerException internally.
    // ------------------------------------------------------------------------
    private static class ThrowsNPECostModel implements CostModel {
        @Override
        public Num calculate(Position position, int finalIndex) {
            throw new NullPointerException("internal NPE");
        }
    }

    @Test
    void costModelThrowsNullPointerExceptionInternally() {
        CostModel model = new ThrowsNPECostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        assertThrows(NullPointerException.class,
                () -> pos.getHoldingCost(0),
                "NullPointerException from CostModel must propagate");
    }

    // ------------------------------------------------------------------------
    // 12. CostModel throws an Error (AssertionError).
    // ------------------------------------------------------------------------
    private static class ThrowsErrorCostModel implements CostModel {
        @Override
        public Num calculate(Position position, int finalIndex) {
            throw new AssertionError("assertion failed");
        }
    }

    @Test
    void costModelThrowsErrorIsPropagated() {
        CostModel model = new ThrowsErrorCostModel();
        Position pos = new Position(Trade.TradeType.BUY,
                                    new ZeroCostModel(),
                                    model);
        assertThrows(AssertionError.class,
                () -> pos.getHoldingCost(1),
                "Errors must not be caught; they should propagate to the caller");
    }

    // ------------------------------------------------------------------------
    // 13. Existing test – mock model returns supplied value.
    // ------------------------------------------------------------------------
    @Test
    void whenHoldingCostModelIsMocked_thenResultIsReturnedFromModel() {
        // GIVEN
        int finalIndex = 7;
        Num expected = DoubleNum.valueOf(3.1415);
        when(holdingCostModelMock.calculate(positionWithMockModel, finalIndex))
                .thenReturn(expected);

        // WHEN
        Num actual = positionWithMockModel.getHoldingCost(finalIndex);

        // THEN
        assertEquals(expected, actual,
                "Position#getHoldingCost should return the value supplied by the holding‑cost model");
        verify(holdingCostModelMock, times(1))
                .calculate(positionWithMockModel, finalIndex);
    }

    // ------------------------------------------------------------------------
    // 14. Existing test – default ZeroCostModel returns zero (kept for backward compatibility).
    // ------------------------------------------------------------------------
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
}
```