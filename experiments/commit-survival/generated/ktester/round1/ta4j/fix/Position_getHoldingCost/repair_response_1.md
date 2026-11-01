```java
package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.Trade.TradeType;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import static org.ta4j.core.num.NaN.NaN;
import static org.junit.Assert.*;

public class Position_getHoldingCost_Test {

    // -------------------------------------------------------------------------
    // Helper abstract class to satisfy the CostModel contract (equals method)
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Simple hand‑rolled mock for {@link CostModel}
    // -------------------------------------------------------------------------
    private static class MockCostModel extends AbstractCostModel {

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

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------
    private MockCostModel mockHoldingCostModel;
    private Position position;

    @Before
    public void setUp() {
        mockHoldingCostModel = new MockCostModel();
        CostModel transactionCostModel = new ZeroCostModel();
        position = new Position(TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

    @After
    public void tearDown() {
        mockHoldingCostModel.reset();
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void getHoldingCost_delegatesToHoldingCostModel_andReturnsCalculatedValue() {
        // GIVEN
        Num expectedHoldingCost = DoubleNum.valueOf(12.34);
        mockHoldingCostModel.setReturn(expectedHoldingCost);
        // WHEN
        Num actualHoldingCost = position.getHoldingCost();
        // THEN
        assertEquals("Position#getHoldingCost should return the value provided by the holding cost model",
                expectedHoldingCost, actualHoldingCost);
        assertEquals("calculate() must be called exactly once", 1, mockHoldingCostModel.getCallCount());
    }

    @Test
    public void getHoldingCost_returnsNaN_whenHoldingCostModelReturnsNaN() {
        // GIVEN
        mockHoldingCostModel.setReturn(NaN);
        // WHEN
        Num result = position.getHoldingCost();
        // THEN
        assertTrue("When the holding cost model returns NaN, Position#getHoldingCost should also return NaN",
                result.isNaN());
        assertEquals(1, mockHoldingCostModel.getCallCount());
    }

    @Test
    public void getHoldingCost_throwsWhenHoldingCostModelIsNull() {
        // GIVEN – a Position constructed with a null holding‑cost model
        CostModel transactionCostModel = new ZeroCostModel();
        try {
            new Position(Trade.TradeType.BUY, transactionCostModel, null);
            fail("Expected IllegalArgumentException because holdingCostModel is null");
        } catch (IllegalArgumentException e) {
            // THEN
            assertEquals("Starting type must not be null", e.getMessage(),
                    "Constructor should validate non‑null holding cost model (via starting type check)");
        }
    }

    @Test
    public void zeroCostModelBehavior_returnsZero() {
        // Use a real ZeroCostModel as the holding‑cost model
        CostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), zeroCostModel);
        Num holdingCost = pos.getHoldingCost();
        assertEquals("Holding cost with ZeroCostModel must be exactly zero", DoubleNum.ZERO, holdingCost);
    }

    @Test
    public void customConstantCostModel_returnsFive() {
        CostModel constantFive = new AbstractCostModel() {
            @Override
            public Num calculate(Position position) {
                return DoubleNum.valueOf(5);
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), constantFive);
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(5), holdingCost);
    }

    @Test
    public void barBasedHoldingCostModel_computesCorrectCost() {
        final Num unitCost = DoubleNum.valueOf(3);
        CostModel perBarCost = new AbstractCostModel() {
            @Override
            public Num calculate(Position position) {
                if (!position.isClosed()) {
                    return NaN;
                }
                int entryIdx = position.getEntry().getIndex();
                int exitIdx = position.getExit().getIndex();
                int barsHeld = exitIdx - entryIdx;
                return unitCost.multipliedBy(DoubleNum.valueOf(barsHeld));
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), perBarCost);
        // open at index 2 and close at index 6
        pos.operate(2);
        pos.operate(6);
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(12), holdingCost);
    }

    @Test
    public void nanHoldingCostModelForOpenPosition_returnsNaN() {
        CostModel nanIfOpen = new AbstractCostModel() {
            @Override
            public Num calculate(Position position) {
                return position.isClosed() ? DoubleNum.valueOf(0) : NaN;
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), nanIfOpen);
        // only entry trade – position stays open
        pos.operate(0);
        Num holdingCost = pos.getHoldingCost();
        assertTrue(holdingCost.isNaN());
    }

    @Test
    public void costModelReceivesSamePositionInstance() {
        MockCostModel identityCheck = new MockCostModel() {
            @Override
            public Num calculate(Position position) {
                // store the received instance and return a dummy value
                this.received = position;
                return DoubleNum.valueOf(7);
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), identityCheck);
        Num result = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(7), result);
        assertSame("The CostModel should receive the exact same Position instance", pos, identityCheck.getReceived());
    }

    @Test
    public void zeroCostModelWithNonZeroTrades_returnsZero() {
        CostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), zeroCostModel);
        // Simulate a BUY of amount 10 at price 100 at index 0
        pos.operate(0, DoubleNum.valueOf(100), DoubleNum.valueOf(10));
        // Simulate the corresponding SELL at index 5
        pos.operate(5, DoubleNum.valueOf(110), DoubleNum.valueOf(10));
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.ZERO, holdingCost);
    }

    @Test
    public void nullHoldingCostModel_throwsNullPointerException() throws Exception {
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        Field field = Position.class.getDeclaredField("holdingCostModel");
        field.setAccessible(true);
        field.set(pos, null);
        assertThrows(NullPointerException.class, pos::getHoldingCost);
    }

    @Test
    public void costModelThrowsNullPointerException_propagates() {
        CostModel throwsNPE = new AbstractCostModel() {
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
    public void costModelThrowsIllegalArgumentException_propagates() {
        CostModel throwsIAE = new AbstractCostModel() {
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
    public void costModelThrowsArithmeticException_propagates() {
        CostModel throwsAE = new AbstractCostModel() {
            @Override
            public Num calculate(Position position) {
                // force a division by zero inside the Num implementation
                return DoubleNum.valueOf(1).dividedBy(DoubleNum.valueOf(0));
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), throwsAE);
        assertThrows(ArithmeticException.class, pos::getHoldingCost);
    }

    @Test
    public void costModelReturnsNull_getHoldingCostReturnsNull() {
        CostModel returnsNull = new AbstractCostModel() {
            @Override
            public Num calculate(Position position) {
                return null;
            }
        };
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), returnsNull);
        Num result = pos.getHoldingCost();
        assertNull("When the CostModel returns null, getHoldingCost should return null", result);
        // Demonstrate the downstream NPE that would happen if the caller dereferences the result
        assertThrows(NullPointerException.class, () -> result.isZero());
    }
}
```