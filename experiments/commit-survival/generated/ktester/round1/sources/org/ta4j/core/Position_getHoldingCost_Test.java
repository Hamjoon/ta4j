package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.junit.*;
import org.ta4j.core.num.NaN;
import static org.junit.Assert.*;

// @ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_Test {

    // Mocks
//     @Mock
    private CostModel mockHoldingCostModel;

    // System under test
    private Position position;

    // -------------------------------------------------------------------------
//     private static class MockCostModel extends AbstractCostModel {

        private Num toReturn;

        private int callCount = 0;

        private Position received;

        void setReturn(Num value) {
            this.toReturn = value;
        }

        void reset() {
            this.callCount = 0;
            this.received = null;
        }

        int getCallCount() {
            return callCount;
        }

        Position getReceived() {
            return received;
        }

        @Override
        public Num calculate(Position position) {
            callCount++;
            received = position;
            return toReturn;
        }
    }

    /**
     * Simple mock implementation of {@link HoldingCostModel}.
     */
//     private static abstract class AbstractHoldingCostModel implements HoldingCostModel {

//         @Override
//         public boolean equals(HoldingCostModel other) {
//             return this == other;
//         }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    /**
     * Simple mock implementation of {@link CostModel}.
     */
    private static abstract class AbstractCostModel implements CostModel {

        @Override
        public boolean equals(CostModel other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    /**
     * Concrete mock that records calls and can be instructed to return a value.
     */
//     private static class MockHoldingCostModel extends AbstractCostModel {

        private Num toReturn;

        private int callCount = 0;

        private Position received;

        void setReturn(Num value) {
            this.toReturn = value;
        }

        void reset() {
            this.callCount = 0;
            this.received = null;
        }

        int getCallCount() {
            return callCount;
        }

        Position getReceived() {
            return received;
        }

        @Override
        public Num calculate(Position position) {
            callCount++;
            received = position;
            return toReturn;
        }
    }

//     @BeforeEach
    void setupBeforeEach() {
        // By default we create a Position that uses the mocked holding‑cost model.
        // Individual tests replace the holdingCostModel when they need a concrete one.
        // transaction costs are irrelevant here
        CostModel transactionCostModel = new ZeroCostModel();
//         position = new Position(TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

//     @AfterEach
    void teardownAfterEach() {
        // Reset mock interactions to avoid cross‑test interference
//         reset(mockHoldingCostModel);
    }

//     @Test
//     void getHoldingCost_delegatesToHoldingCostModel_andReturnsCalculatedValue() {
//         // GIVEN
//         Num expectedHoldingCost = DoubleNum.valueOf(12.34);
//         mockHoldingCostModel.setReturn(expectedHoldingCost);
//         // WHEN
//         Num actualHoldingCost = position.getHoldingCost();
//         // THEN
//         assertEquals("Position#getHoldingCost should return the value provided by the holding cost model", expectedHoldingCost, actualHoldingCost);
//         assertEquals("calculate() must be called exactly once", 1, mockHoldingCostModel.getCallCount());
//     }

//     @Test
//     void getHoldingCost_returnsNaN_whenHoldingCostModelReturnsNaN() {
//         // GIVEN
//         mockHoldingCostModel.setReturn(NaN.NaN);
//         // WHEN
//         Num result = position.getHoldingCost();
//         // THEN
//         assertTrue("When the holding cost model returns NaN, Position#getHoldingCost should also return NaN", result.isNaN());
//         assertEquals(1, mockHoldingCostModel.getCallCount());
//     }

    @Test
    void getHoldingCost_throwsWhenHoldingCostModelIsNull() {
        // GIVEN – a Position constructed with a null holding‑cost model
        CostModel transactionCostModel = new ZeroCostModel();
        try {
            new Position(Trade.TradeType.BUY, transactionCostModel, null);
            fail("Expected IllegalArgumentException because holdingCostModel is null");
        } catch (IllegalArgumentException e) {
            // THEN
            assertEquals("Starting type must not be null", e.getMessage());
        }
    }

//     @Test
//     @DisplayName("ZeroCostModel behavior – holding cost must be zero")
//     void zeroCostModelBehavior_returnsZero() {
//         // Use a real ZeroCostModel as the holding‑cost model
//         CostModel zeroCostModel = new ZeroCostModel();
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), zeroCostModel);
//         Num holdingCost = pos.getHoldingCost();
//         assertEquals("Holding cost with ZeroCostModel must be exactly zero", DoubleNum.ZERO, holdingCost);
//     }

//     @Test
//     @DisplayName("Custom constant CostModel – should return the constant value")
//     void customConstantCostModel_returnsFive() {
//         CostModel constantFive = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 return DoubleNum.valueOf(5);
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), constantFive);
//         Num holdingCost = pos.getHoldingCost();
//         assertEquals(DoubleNum.valueOf(5), holdingCost);
//     }

//     @Test
//     @DisplayName("Bar‑based HoldingCostModel – cost = (exitIndex‑entryIndex) * unitCost")
//     void barBasedHoldingCostModel_computesCorrectCost() {
//         final Num unitCost = DoubleNum.valueOf(3);
//         CostModel perBarCost = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 if (!position.isClosed()) {
//                     return NaN.NaN;
//                 }
//                 int entryIdx = position.getEntry().getIndex();
//                 int exitIdx = position.getExit().getIndex();
//                 int barsHeld = exitIdx - entryIdx;
//                 return unitCost.multipliedBy(DoubleNum.valueOf(barsHeld));
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), perBarCost);
//         // open at index 2 and close at index 6
//         pos.operate(2);
//         pos.operate(6);
//         Num holdingCost = pos.getHoldingCost();
//         assertEquals(DoubleNum.valueOf(12), holdingCost);
//     }

//     @Test
//     @DisplayName("NaN HoldingCostModel for open positions – should return NaN")
//     void nanHoldingCostModelForOpenPosition_returnsNaN() {
//         CostModel nanIfOpen = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 return position.isClosed() ? DoubleNum.valueOf(0) : NaN.NaN;
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), nanIfOpen);
//         // only entry trade – position stays open
//         pos.operate(0);
//         Num holdingCost = pos.getHoldingCost();
//         assertTrue(holdingCost.isNaN());
//     }

//     @Test
//     @DisplayName("CostModel receives the same Position instance – verify identity")
//     void costModelReceivesSamePositionInstance() {
//         MockHoldingCostModel identityCheck = new MockHoldingCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 this.received = position;
//                 return DoubleNum.valueOf(7);
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), identityCheck);
//         Num result = pos.getHoldingCost();
//         assertEquals(DoubleNum.valueOf(7), result);
//         assertSame("The CostModel should receive the exact same Position instance", pos, identityCheck.getReceived());
//     }

//     @Test
//     @DisplayName("ZeroCostModel with non‑zero trades – still returns zero")
//     void zeroCostModelWithNonZeroTrades_returnsZero() {
//         CostModel zeroCostModel = new ZeroCostModel();
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), zeroCostModel);
//         // Simulate a BUY of amount 10 at price 100 at index 0
//         pos.operate(0, DoubleNum.valueOf(100), DoubleNum.valueOf(10));
//         // Simulate the corresponding SELL at index 5
//         pos.operate(5, DoubleNum.valueOf(110), DoubleNum.valueOf(10));
//         Num holdingCost = pos.getHoldingCost();
//         assertEquals(DoubleNum.ZERO, holdingCost);
//     }

//     @Test
//     @DisplayName("Null holdingCostModel via reflection – getHoldingCost throws NPE")
//     void nullHoldingCostModel_throwsNullPointerException() throws Exception {
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
//         java.lang.reflect.Field field = Position.class.getDeclaredField("holdingCostModel");
//         field.setAccessible(true);
//         field.set(pos, null);
//         assertThrows(NullPointerException.class, pos::getHoldingCost);
//     }

//     @Test
//     @DisplayName("CostModel that throws NullPointerException – exception propagates")
//     void costModelThrowsNullPointerException_propagates() {
//         CostModel throwsNPE = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 throw new NullPointerException("deliberate NPE");
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), throwsNPE);
//         NullPointerException ex = assertThrows(NullPointerException.class, pos::getHoldingCost);
//         assertEquals("deliberate NPE", ex.getMessage());
//     }

//     @Test
//     @DisplayName("CostModel that throws IllegalArgumentException – exception propagates")
//     void costModelThrowsIllegalArgumentException_propagates() {
//         CostModel throwsIAE = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 throw new IllegalArgumentException("invalid position");
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), throwsIAE);
//         IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, pos::getHoldingCost);
//         assertEquals("invalid position", ex.getMessage());
//     }

//     @Test
//     @DisplayName("CostModel that throws ArithmeticException – exception propagates")
//     void costModelThrowsArithmeticException_propagates() {
//         CostModel throwsAE = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 // force a division by zero inside the Num implementation
//                 return DoubleNum.valueOf(1).dividedBy(DoubleNum.valueOf(0));
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), throwsAE);
//         assertThrows(ArithmeticException.class, pos::getHoldingCost);
//     }

//     @Test
//     @DisplayName("CostModel returns null – getHoldingCost returns null")
//     void costModelReturnsNull_getHoldingCostReturnsNull() {
//         CostModel returnsNull = new AbstractCostModel() {
// 
//             @Override
//             public Num calculate(Position position) {
//                 return null;
//             }
//         };
//         Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), returnsNull);
//         Num result = pos.getHoldingCost();
//         assertNull("When the CostModel returns null, getHoldingCost should return null", result);
//         // Demonstrate the downstream NPE that would happen if the caller dereferences the result
//         assertThrows(NullPointerException.class, () -> result.isZero());
//     }

    @Before
    public void setUp() {
        mockHoldingCostModel = new MockHoldingCostModel();
        CostModel transactionCostModel = new ZeroCostModel();
        position = new Position(Trade.TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

//     @After
//     public void tearDown() {
//         mockHoldingCostModel.reset();
//     }
}