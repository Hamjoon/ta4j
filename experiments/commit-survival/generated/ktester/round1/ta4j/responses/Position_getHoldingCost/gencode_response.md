```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_Test {

    // -------------------------------------------------------------------------
    // Common test fixtures
    // -------------------------------------------------------------------------

    @Mock
    private CostModel mockHoldingCostModel;   // used only by the first two tests

    private Position position;               // the Position under test

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources needed
    }

    @BeforeEach
    void setupBeforeEach() {
        // By default we create a Position that uses the mocked holding‑cost model.
        // Individual tests replace the holdingCostModel when they need a concrete one.
        CostModel transactionCostModel = new ZeroCostModel(); // transaction costs are irrelevant here
        position = new Position(TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

    @AfterEach
    void teardownAfterEach() {
        reset(mockHoldingCostModel);
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    // -------------------------------------------------------------------------
    // Existing tests (kept unchanged)
    // -------------------------------------------------------------------------

    @Test
    void getHoldingCost_delegatesToHoldingCostModel_andReturnsCalculatedValue() {
        // GIVEN – the holding cost model returns a deterministic value
        Num expectedHoldingCost = DoubleNum.valueOf(12.34);
        when(mockHoldingCostModel.calculate(position)).thenReturn(expectedHoldingCost);

        // WHEN – the method under test is invoked
        Num actualHoldingCost = position.getHoldingCost();

        // THEN – the result matches the mocked value and the model was called exactly once
        assertEquals(expectedHoldingCost, actualHoldingCost,
                "Position#getHoldingCost should return the value provided by the holding cost model");
        verify(mockHoldingCostModel, times(1)).calculate(position);
    }

    @Test
    void getHoldingCost_returnsNaN_whenHoldingCostModelReturnsNaN() {
        // GIVEN – the holding cost model returns NaN
        Num nan = DoubleNum.NaN;
        when(mockHoldingCostModel.calculate(position)).thenReturn(nan);

        // WHEN
        Num result = position.getHoldingCost();

        // THEN
        assertTrue(result.isNaN(),
                "When the holding cost model returns NaN, Position#getHoldingCost should also return NaN");
        verify(mockHoldingCostModel, times(1)).calculate(position);
    }

    // -------------------------------------------------------------------------
    // New test groups derived from the JSON specification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ZeroCostModel behavior – holding cost must be zero")
    void zeroCostModelBehavior_returnsZero() {
        // Use a real ZeroCostModel as the holding‑cost model
        CostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), zeroCostModel);

        // No trades are required – the model always returns zero
        Num holdingCost = pos.getHoldingCost();

        assertEquals(ZeroCostModel.ZERO, holdingCost,
                "Holding cost with ZeroCostModel must be exactly zero");
    }

    @Test
    @DisplayName("Custom constant CostModel – should return the constant value")
    void customConstantCostModel_returnsFive() {
        CostModel constantFive = new CostModel() {
            @Override
            public Num calculate(Position position) {
                return DoubleNum.valueOf(5);
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), constantFive);

        Num holdingCost = pos.getHoldingCost();

        assertEquals(DoubleNum.valueOf(5), holdingCost,
                "Holding cost should equal the constant value supplied by the custom CostModel");
    }

    @Test
    @DisplayName("Bar‑based HoldingCostModel – cost = (exitIndex‑entryIndex) * unitCost")
    void barBasedHoldingCostModel_computesCorrectCost() {
        // unitCost = 3
        final Num unitCost = DoubleNum.valueOf(3);

        CostModel perBarCost = new CostModel() {
            @Override
            public Num calculate(Position position) {
                // If the position is not closed, we cannot compute a holding cost
                if (!position.isClosed()) {
                    return DoubleNum.NaN;
                }
                int entryIdx = position.getEntry().getIndex();
                int exitIdx = position.getExit().getIndex();
                int barsHeld = exitIdx - entryIdx;
                return unitCost.multipliedBy(barsHeld);
            }
        };

        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), perBarCost);
        // open at index 2 and close at index 6
        pos.operate(2); // entry trade
        pos.operate(6); // exit trade

        Num holdingCost = pos.getHoldingCost();

        assertEquals(DoubleNum.valueOf(12), holdingCost,
                "Holding cost should be (6‑2) * 3 = 12");
    }

    @Test
    @DisplayName("NaN HoldingCostModel for open positions – should return NaN")
    void nanHoldingCostModelForOpenPosition_returnsNaN() {
        CostModel nanIfOpen = new CostModel() {
            @Override
            public Num calculate(Position position) {
                return position.isClosed() ? DoubleNum.valueOf(0) : DoubleNum.NaN;
            }
        };

        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), nanIfOpen);
        // only entry trade – position stays open
        pos.operate(0);

        Num holdingCost = pos.getHoldingCost();

        assertTrue(holdingCost.isNaN(),
                "Holding cost should be NaN when the position is still open");
    }

    @Test
    @DisplayName("CostModel receives the same Position instance – verify identity")
    void costModelReceivesSamePositionInstance() {
        CostModel identityCheck = new CostModel() {
            private Position received;

            @Override
            public Num calculate(Position position) {
                this.received = position;
                // return a dummy value
                return DoubleNum.valueOf(7);
            }

            public Position getReceived() {
                return received;
            }
        };

        // Cast to access the helper method later
        CostModel cm = identityCheck;
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), cm);
        // trigger calculation
        Num result = pos.getHoldingCost();

        assertEquals(DoubleNum.valueOf(7), result,
                "Returned value should be the constant defined in the custom CostModel");
        // Verify that the Position passed to calculate() is exactly the one we created
        assertSame(pos, ((CostModel) cm).calculate(pos),
                "The CostModel should receive the exact same Position instance");
    }

    @Test
    @DisplayName("ZeroCostModel with non‑zero trades – still returns zero")
    void zeroCostModelWithNonZeroTrades_returnsZero() {
        CostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), zeroCostModel);

        // Simulate a BUY of amount 10 at price 100 at index 0
        pos.operate(0, DoubleNum.valueOf(100), DoubleNum.valueOf(10));
        // Simulate the corresponding SELL at index 5
        pos.operate(5, DoubleNum.valueOf(110), DoubleNum.valueOf(10));

        Num holdingCost = pos.getHoldingCost();

        assertEquals(ZeroCostModel.ZERO, holdingCost,
                "Even with non‑zero trades, ZeroCostModel must yield a holding cost of zero");
    }

    @Test
    @DisplayName("Null holdingCostModel via reflection – getHoldingCost throws NPE")
    void nullHoldingCostModel_throwsNullPointerException() throws Exception {
        // Build a normal Position first
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());

        // Use reflection to set the private final field to null
        Field field = Position.class.getDeclaredField("holdingCostModel");
        field.setAccessible(true);
        field.set(pos, null);

        // The call should now fail with NPE because the method dereferences the field
        assertThrows(NullPointerException.class, pos::getHoldingCost,
                "Calling getHoldingCost when holdingCostModel is null must throw NullPointerException");
    }

    @Test
    @DisplayName("CostModel that throws NullPointerException – exception propagates")
    void costModelThrowsNullPointerException_propagates() {
        CostModel throwsNPE = new CostModel() {
            @Override
            public Num calculate(Position position) {
                throw new NullPointerException("deliberate NPE");
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), throwsNPE);

        NullPointerException ex = assertThrows(NullPointerException.class, pos::getHoldingCost);
        assertEquals("deliberate NPE", ex.getMessage());
    }

    @Test
    @DisplayName("CostModel that throws IllegalArgumentException – exception propagates")
    void costModelThrowsIllegalArgumentException_propagates() {
        CostModel throwsIAE = new CostModel() {
            @Override
            public Num calculate(Position position) {
                throw new IllegalArgumentException("invalid position");
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), throwsIAE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, pos::getHoldingCost);
        assertEquals("invalid position", ex.getMessage());
    }

    @Test
    @DisplayName("CostModel that throws ArithmeticException – exception propagates")
    void costModelThrowsArithmeticException_propagates() {
        CostModel throwsAE = new CostModel() {
            @Override
            public Num calculate(Position position) {
                // force a division by zero inside the Num implementation
                return DoubleNum.valueOf(1).dividedBy(DoubleNum.valueOf(0));
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), throwsAE);

        assertThrows(ArithmeticException.class, pos::getHoldingCost,
                "ArithmeticException should propagate from the CostModel");
    }

    @Test
    @DisplayName("CostModel returns null – getHoldingCost returns null")
    void costModelReturnsNull_getHoldingCostReturnsNull() {
        CostModel returnsNull = new CostModel() {
            @Override
            public Num calculate(Position position) {
                return null;
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), returnsNull);

        Num result = pos.getHoldingCost();

        assertNull(result, "When the CostModel returns null, getHoldingCost should return null");

        // Demonstrate the downstream NPE that would happen if the caller dereferences the result
        assertThrows(NullPointerException.class, () -> result.isZero(),
                "Dereferencing the null result should cause NullPointerException");
    }
}
```