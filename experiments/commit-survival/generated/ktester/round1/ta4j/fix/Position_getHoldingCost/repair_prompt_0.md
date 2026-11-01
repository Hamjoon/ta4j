
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.ta4j.core.Trade.TradeType;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Test;
import org.ta4j.core.num.NaN;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import static org.ta4j.core.num.NaN.NaN;
import org.junit.jupiter.api.*;

@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_Test {

    // Mocks
    @Mock
    private CostModel mockHoldingCostModel;

    // System under test
    private Position position;

    @BeforeEach
    void setupBeforeEach() {
        // By default we create a Position that uses the mocked holding‑cost model.
        // Individual tests replace the holdingCostModel when they need a concrete one.
        // transaction costs are irrelevant here
        CostModel transactionCostModel = new ZeroCostModel();
        position = new Position(TradeType.BUY, transactionCostModel, mockHoldingCostModel);
    }

    @AfterEach
    void teardownAfterEach() {
        // Reset mock interactions to avoid cross‑test interference
        reset(mockHoldingCostModel);
    }

    @Test
    void getHoldingCost_delegatesToHoldingCostModel_andReturnsCalculatedValue() {
        // GIVEN – the holding cost model returns a deterministic value
        Num expectedHoldingCost = DoubleNum.valueOf(12.34);
        when(mockHoldingCostModel.calculate(position)).thenReturn(expectedHoldingCost);
        // WHEN – the method under test is invoked
        Num actualHoldingCost = position.getHoldingCost();
        // THEN – the result matches the mocked value and the model was called exactly once
        assertEquals(expectedHoldingCost, actualHoldingCost, "Position#getHoldingCost should return the value provided by the holding cost model");
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
        assertTrue(result.isNaN(), "When the holding cost model returns NaN, Position#getHoldingCost should also return NaN");
        verify(mockHoldingCostModel, times(1)).calculate(position);
    }

    @Test
    void getHoldingCost_throwsWhenHoldingCostModelIsNull() {
        // GIVEN – a Position constructed with a null holding cost model
        CostModel transactionCostModel = new ZeroCostModel();
        Position posWithNullHolding = assertThrows(IllegalArgumentException.class, () -> new Position(Trade.TradeType.BUY, transactionCostModel, null));
        // THEN – the constructor should reject a null holding cost model
        assertEquals("Starting type must not be null", posWithNullHolding.getMessage(), "Constructor should validate non‑null holding cost model (via starting type check)");
    }

    @Test
    @DisplayName("ZeroCostModel behavior – holding cost must be zero")
    void zeroCostModelBehavior_returnsZero() {
        // Use a real ZeroCostModel as the holding‑cost model
        CostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), zeroCostModel);
        // No trades are required – the model always returns zero
        Num holdingCost = pos.getHoldingCost();
        assertEquals(ZeroCostModel.ZERO, holdingCost, "Holding cost with ZeroCostModel must be exactly zero");
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
        assertEquals(DoubleNum.valueOf(5), holdingCost, "Holding cost should equal the constant value supplied by the custom CostModel");
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
        // entry trade
        pos.operate(2);
        // exit trade
        pos.operate(6);
        Num holdingCost = pos.getHoldingCost();
        assertEquals(DoubleNum.valueOf(12), holdingCost, "Holding cost should be (6‑2) * 3 = 12");
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
        assertTrue(holdingCost.isNaN(), "Holding cost should be NaN when the position is still open");
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
        assertEquals(DoubleNum.valueOf(7), result, "Returned value should be the constant defined in the custom CostModel");
        // Verify that the Position passed to calculate() is exactly the one we created
        assertSame(pos, ((CostModel) cm).calculate(pos), "The CostModel should receive the exact same Position instance");
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
        assertEquals(ZeroCostModel.ZERO, holdingCost, "Even with non‑zero trades, ZeroCostModel must yield a holding cost of zero");
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
        assertThrows(NullPointerException.class, pos::getHoldingCost, "Calling getHoldingCost when holdingCostModel is null must throw NullPointerException");
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
        assertThrows(ArithmeticException.class, pos::getHoldingCost, "ArithmeticException should propagate from the CostModel");
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
        assertThrows(NullPointerException.class, () -> result.isZero(), "Dereferencing the null result should cause NullPointerException");
    }
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:4: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:10: error: package org.junit.jupiter.api does not exist
import static org.junit.jupiter.api.Assertions.*;
                                   ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:11: error: package org.mockito does not exist
import static org.mockito.Mockito.*;
                         ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:14: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:17: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:22: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:3: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:5: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:18: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:20: error: package org.junit.jupiter.api does not exist
import org.junit.jupiter.api.*;
^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:22: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:26: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:32: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:41: error: cannot find symbol
    @AfterEach
     ^
  symbol:   class AfterEach
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:81: error: cannot find symbol
    @DisplayName("ZeroCostModel behavior – holding cost must be zero")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:92: error: cannot find symbol
    @DisplayName("Custom constant CostModel – should return the constant value")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:107: error: cannot find symbol
    @DisplayName("Bar‑based HoldingCostModel – cost = (exitIndex‑entryIndex) * unitCost")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:136: error: cannot find symbol
    @DisplayName("NaN HoldingCostModel for open positions – should return NaN")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:153: error: cannot find symbol
    @DisplayName("CostModel receives the same Position instance – verify identity")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:181: error: cannot find symbol
    @DisplayName("ZeroCostModel with non‑zero trades – still returns zero")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:194: error: cannot find symbol
    @DisplayName("Null holdingCostModel via reflection – getHoldingCost throws NPE")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:207: error: cannot find symbol
    @DisplayName("CostModel that throws NullPointerException – exception propagates")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:222: error: cannot find symbol
    @DisplayName("CostModel that throws IllegalArgumentException – exception propagates")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:237: error: cannot find symbol
    @DisplayName("CostModel that throws ArithmeticException – exception propagates")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:252: error: cannot find symbol
    @DisplayName("CostModel returns null – getHoldingCost returns null")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:44: error: cannot find symbol
        reset(mockHoldingCostModel);
        ^
  symbol:   method reset(CostModel)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:51: error: cannot find symbol
        when(mockHoldingCostModel.calculate(position)).thenReturn(expectedHoldingCost);
        ^
  symbol:   method when(Num)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:55: error: cannot find symbol
        assertEquals(expectedHoldingCost, actualHoldingCost, "Position#getHoldingCost should return the value provided by the holding cost model");
        ^
  symbol:   method assertEquals(Num,Num,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:56: error: cannot find symbol
        verify(mockHoldingCostModel, times(1)).calculate(position);
                                     ^
  symbol:   method times(int)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:62: error: cannot find symbol
        Num nan = DoubleNum.NaN;
                           ^
  symbol:   variable NaN
  location: class DoubleNum
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:63: error: cannot find symbol
        when(mockHoldingCostModel.calculate(position)).thenReturn(nan);
        ^
  symbol:   method when(Num)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:67: error: cannot find symbol
        assertTrue(result.isNaN(), "When the holding cost model returns NaN, Position#getHoldingCost should also return NaN");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:68: error: cannot find symbol
        verify(mockHoldingCostModel, times(1)).calculate(position);
                                     ^
  symbol:   method times(int)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:75: error: cannot find symbol
        Position posWithNullHolding = assertThrows(IllegalArgumentException.class, () -> new Position(Trade.TradeType.BUY, transactionCostModel, null));
                                      ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,()->new Po[...]null))
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:77: error: cannot find symbol
        assertEquals("Starting type must not be null", posWithNullHolding.getMessage(), "Constructor should validate non‑null holding cost model (via starting type check)");
                                                                         ^
  symbol:   method getMessage()
  location: variable posWithNullHolding of type Position
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:88: error: cannot find symbol
        assertEquals(ZeroCostModel.ZERO, holdingCost, "Holding cost with ZeroCostModel must be exactly zero");
                                  ^
  symbol:   variable ZERO
  location: class ZeroCostModel
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:94: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$1> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel constantFive = new CostModel() {
                                                 ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:103: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(5), holdingCost, "Holding cost should equal the constant value supplied by the custom CostModel");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:111: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$2> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel perBarCost = new CostModel() {
                                               ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:117: error: cannot find symbol
                    return DoubleNum.NaN;
                                    ^
  symbol:   variable NaN
  location: class DoubleNum
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:122: error: incompatible types: int cannot be converted to Num
                return unitCost.multipliedBy(barsHeld);
                                             ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:132: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(12), holdingCost, "Holding cost should be (6‑2) * 3 = 12");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:138: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$3> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel nanIfOpen = new CostModel() {
                                              ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:142: error: cannot find symbol
                return position.isClosed() ? DoubleNum.valueOf(0) : DoubleNum.NaN;
                                                                             ^
  symbol:   variable NaN
  location: class DoubleNum
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:149: error: cannot find symbol
        assertTrue(holdingCost.isNaN(), "Holding cost should be NaN when the position is still open");
        ^
  symbol:   method assertTrue(boolean,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:155: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$4> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel identityCheck = new CostModel() {
                                                  ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:175: error: cannot find symbol
        assertEquals(DoubleNum.valueOf(7), result, "Returned value should be the constant defined in the custom CostModel");
        ^
  symbol:   method assertEquals(DoubleNum,Num,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:177: error: cannot find symbol
        assertSame(pos, ((CostModel) cm).calculate(pos), "The CostModel should receive the exact same Position instance");
        ^
  symbol:   method assertSame(Position,Num,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:190: error: cannot find symbol
        assertEquals(ZeroCostModel.ZERO, holdingCost, "Even with non‑zero trades, ZeroCostModel must yield a holding cost of zero");
                                  ^
  symbol:   variable ZERO
  location: class ZeroCostModel
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:203: error: cannot find symbol
        assertThrows(NullPointerException.class, pos::getHoldingCost, "Calling getHoldingCost when holdingCostModel is null must throw NullPointerException");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,pos::getHoldingCost,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:209: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$5> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel throwsNPE = new CostModel() {
                                              ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:217: error: cannot find symbol
        NullPointerException ex = assertThrows(NullPointerException.class, pos::getHoldingCost);
                                  ^
  symbol:   method assertThrows(Class<NullPointerException>,pos::getHoldingCost)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:218: error: cannot find symbol
        assertEquals("deliberate NPE", ex.getMessage());
        ^
  symbol:   method assertEquals(String,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:224: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$6> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel throwsIAE = new CostModel() {
                                              ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:232: error: cannot find symbol
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, pos::getHoldingCost);
                                      ^
  symbol:   method assertThrows(Class<IllegalArgumentException>,pos::getHoldingCost)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:233: error: cannot find symbol
        assertEquals("invalid position", ex.getMessage());
        ^
  symbol:   method assertEquals(String,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:239: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$7> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel throwsAE = new CostModel() {
                                             ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:248: error: cannot find symbol
        assertThrows(ArithmeticException.class, pos::getHoldingCost, "ArithmeticException should propagate from the CostModel");
        ^
  symbol:   method assertThrows(Class<ArithmeticException>,pos::getHoldingCost,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:254: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$8> is not abstract and does not override abstract method equals(CostModel) in CostModel
        CostModel returnsNull = new CostModel() {
                                                ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:263: error: cannot find symbol
        assertNull(result, "When the CostModel returns null, getHoldingCost should return null");
        ^
  symbol:   method assertNull(Num,String)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:265: error: cannot find symbol
        assertThrows(NullPointerException.class, () -> result.isZero(), "Dereferencing the null result should cause NullPointerException");
        ^
  symbol:   method assertThrows(Class<NullPointerException>,()->result.isZero(),String)
  location: class Position_getHoldingCost_Test
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
61 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getHoldingCost()': '\n     * Calculates the holding cost of the closed position.\n     *\n     * @return the cost of the position\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.analysis.cost.CostModel org.ta4j.core.Position.holdingCostModel;\n\trelated functions:\n\t\tmethod `public Num getHoldingCost(int)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getHoldingCost()`, api document: \n     * Calculates the holding cost of the position.\n     *\n     * @param finalIndex the index of the final bar to be considered (if position is\n     *                   open)\n     * @return the cost of the position\n     ;\n\t\tmethod `public Num getPositionCost()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getHoldingCost()`, api document: \n     * Calculates the total cost of the closed position.\n     *\n     * @return the cost of the position\n     ;\nclass `org.ta4j.core.Strategy`:\n\trelated functions:\n\t\tmethod ` boolean shouldOperate(int, TradingRecord)` returns `boolean`, related with `org.ta4j.core.Position.getHoldingCost()`, api document: \n     * @param index         the bar index\n     * @param tradingRecord the potentially needed trading history\n     * @return true to recommend a trade, false otherwise (no recommendation)\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


