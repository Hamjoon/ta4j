```java
package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.HoldingCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.junit.*;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class Position_getHoldingCost_Test {

    // System under test
    private Position position;

    // -------------------------------------------------------------------------
    private static abstract class AbstractHoldingCostModel implements HoldingCostModel {

        @Override
        public boolean equals(HoldingCostModel other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    // -------------------------------------------------------------------------
    private static class MockHoldingCostModel extends AbstractHoldingCostModel {

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

    private MockHoldingCostModel mockHoldingCostModel;

    @Before
    public void setUp() {
        mockHoldingCostModel = new MockHoldingCostModel();
        CostModel transactionCostModel = new ZeroCostModel();
        position = new Position(Trade.TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

    @After
    public void tearDown() {
        mockHoldingCostModel.reset();
    }

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
        mockHoldingCostModel.setReturn(NaN.NaN);
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
        HoldingCostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), zeroCostModel);
        Num holdingCost = pos.getHoldingCost();
        assertEquals("Holding cost with ZeroCostModel must be exactly zero", DoubleNum.ZERO, holdingCost);
    }

    @Test
    public void customConstantCostModel_returnsFive() {
        HoldingCostModel constantFive = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                return DoubleNum.valueOf(5);
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), constantFive);
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(5), holdingCost);
    }

    @Test
    public void barBasedHoldingCostModel_computesCorrectCost() {
        final Num unitCost = DoubleNum.valueOf(3);
        HoldingCostModel perBarCost = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                if (!position.isClosed()) {
                    return NaN.NaN;
                }
                int entryIdx = position.getEntry().getIndex();
                int exitIdx = position.getExit().getIndex();
                int barsHeld = exitIdx - entryIdx;
                return unitCost.multipliedBy(DoubleNum.valueOf(barsHeld));
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), perBarCost);
        // open at index 2 and close at index 6
        pos.operate(2);
        pos.operate(6);
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(12), holdingCost);
    }

    @Test
    public void nanHoldingCostModelForOpenPosition_returnsNaN() {
        HoldingCostModel nanIfOpen = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                return position.isClosed() ? DoubleNum.valueOf(0) : NaN.NaN;
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), nanIfOpen);
        // only entry trade – position stays open
        pos.operate(0);
        Num holdingCost = pos.getHoldingCost();
        assertTrue(holdingCost.isNaN());
    }

    @Test
    public void costModelReceivesSamePositionInstance() {
        MockHoldingCostModel identityCheck = new MockHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                this.received = position;
                return DoubleNum.valueOf(7);
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), identityCheck);
        Num result = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(7), result);
        assertSame("The CostModel should receive the exact same Position instance", pos, identityCheck.getReceived());
    }

    @Test
    public void zeroCostModelWithNonZeroTrades_returnsZero() {
        HoldingCostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), zeroCostModel);
        // Simulate a BUY of amount 10 at price 100 at index 0
        pos.operate(0, DoubleNum.valueOf(100), DoubleNum.valueOf(10));
        // Simulate the corresponding SELL at index 5
        pos.operate(5, DoubleNum.valueOf(110), DoubleNum.valueOf(10));
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.ZERO, holdingCost);
    }

    @Test
    public void nullHoldingCostModel_throwsNullPointerException() throws Exception {
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        Field field = Position.class.getDeclaredField("holdingCostModel");
        field.setAccessible(true);
        field.set(pos, null);
        assertThrows(NullPointerException.class, pos::getHoldingCost);
    }

    @Test
    public void costModelThrowsNullPointerException_propagates() {
        HoldingCostModel throwsNPE = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                throw new NullPointerException("deliberate NPE");
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), throwsNPE);
        NullPointerException ex = assertThrows(NullPointerException.class, pos::getHoldingCost);
        assertEquals("deliberate NPE", ex.getMessage());
    }

    @Test
    public void costModelThrowsIllegalArgumentException_propagates() {
        HoldingCostModel throwsIAE = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                throw new IllegalArgumentException("invalid position");
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), throwsIAE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, pos::getHoldingCost);
        assertEquals("invalid position", ex.getMessage());
    }

    @Test
    public void costModelThrowsArithmeticException_propagates() {
        HoldingCostModel throwsAE = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                // force a division by zero inside the Num implementation
                return DoubleNum.valueOf(1).dividedBy(DoubleNum.valueOf(0));
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), throwsAE);
        assertThrows(ArithmeticException.class, pos::getHoldingCost);
    }

    @Test
    public void costModelReturnsNull_getHoldingCostReturnsNull() {
        HoldingCostModel returnsNull = new AbstractHoldingCostModel() {
            @Override
            public Num calculate(Position position) {
                return null;
            }
        };
        Position pos = new Position(Trade.TradeType.BUY, new ZeroCostModel(), returnsNull);
        Num result = pos.getHoldingCost();
        assertNull("When the CostModel returns null, getHoldingCost should return null", result);
        // Demonstrate the downstream NPE that would happen if the caller dereferences the result
        assertThrows(NullPointerException.class, () -> result.isZero());
    }
}
```