
You are an experienced Java developer tasked with fixing a piece of code that contains compilation errors. Your task is to resolve these issues after analyzing the code and its compilation feedback.

Use the following steps to guide your analysis and correction process:
1. Review the provided Java code for any syntactical or logical errors.
2. Analyze the compilation feedback to understand the specific errors and their locations within the code.
3. Apply necessary corrections to the code based on the feedback, and output the compelete correct code.
The Java code you need to fix is provided below:
```java
package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.Trade.TradeType;
import java.lang.reflect.Field;
import org.ta4j.core.num.NaN;
import org.junit.*;
import org.junit.runner.RunWith;
import org.ta4j.core.num.NaN;
import static org.ta4j.core.num.NaN.NaN;
import static org.junit.Assert.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

@ExtendWith(MockitoExtension.class)
class Position_getHoldingCost_Test {

    // Mocks
    @Mock
    private CostModel mockHoldingCostModel;

    // System under test
    private Position position;

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
        // GIVEN
        Num expectedHoldingCost = DoubleNum.valueOf(12.34);
        mockHoldingCostModel.setReturn(expectedHoldingCost);
        // WHEN
        Num actualHoldingCost = position.getHoldingCost();
        // THEN
        assertEquals("Position#getHoldingCost should return the value provided by the holding cost model", expectedHoldingCost, actualHoldingCost);
        assertEquals("calculate() must be called exactly once", 1, mockHoldingCostModel.getCallCount());
    }

    @Test
    void getHoldingCost_returnsNaN_whenHoldingCostModelReturnsNaN() {
        // GIVEN
        mockHoldingCostModel.setReturn(NaN);
        // WHEN
        Num result = position.getHoldingCost();
        // THEN
        assertTrue("When the holding cost model returns NaN, Position#getHoldingCost should also return NaN", result.isNaN());
        assertEquals(1, mockHoldingCostModel.getCallCount());
    }

    @Test
    void getHoldingCost_throwsWhenHoldingCostModelIsNull() {
        // GIVEN – a Position constructed with a null holding‑cost model
        CostModel transactionCostModel = new ZeroCostModel();
        try {
            new Position(Trade.TradeType.BUY, transactionCostModel, null);
            fail("Expected IllegalArgumentException because holdingCostModel is null");
        } catch (IllegalArgumentException e) {
            // THEN
            assertEquals("Starting type must not be null", e.getMessage(), "Constructor should validate non‑null holding cost model (via starting type check)");
        }
    }

    @Test
    @DisplayName("ZeroCostModel behavior – holding cost must be zero")
    void zeroCostModelBehavior_returnsZero() {
        // Use a real ZeroCostModel as the holding‑cost model
        CostModel zeroCostModel = new ZeroCostModel();
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), zeroCostModel);
        Num holdingCost = pos.getHoldingCost();
        assertEquals("Holding cost with ZeroCostModel must be exactly zero", DoubleNum.ZERO, holdingCost);
    }

    @Test
    @DisplayName("Custom constant CostModel – should return the constant value")
    void customConstantCostModel_returnsFive() {
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
    @DisplayName("Bar‑based HoldingCostModel – cost = (exitIndex‑entryIndex) * unitCost")
    void barBasedHoldingCostModel_computesCorrectCost() {
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
    @DisplayName("NaN HoldingCostModel for open positions – should return NaN")
    void nanHoldingCostModelForOpenPosition_returnsNaN() {
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
    @DisplayName("CostModel receives the same Position instance – verify identity")
    void costModelReceivesSamePositionInstance() {
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
    @DisplayName("ZeroCostModel with non‑zero trades – still returns zero")
    void zeroCostModelWithNonZeroTrades_returnsZero() {
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
    @DisplayName("Null holdingCostModel via reflection – getHoldingCost throws NPE")
    void nullHoldingCostModel_throwsNullPointerException() throws Exception {
        Position pos = new Position(TradeType.BUY, new ZeroCostModel(), new ZeroCostModel());
        Field field = Position.class.getDeclaredField("holdingCostModel");
        field.setAccessible(true);
        field.set(pos, null);
        assertThrows(NullPointerException.class, pos::getHoldingCost);
    }

    @Test
    @DisplayName("CostModel that throws NullPointerException – exception propagates")
    void costModelThrowsNullPointerException_propagates() {
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
    @DisplayName("CostModel that throws IllegalArgumentException – exception propagates")
    void costModelThrowsIllegalArgumentException_propagates() {
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
    @DisplayName("CostModel that throws ArithmeticException – exception propagates")
    void costModelThrowsArithmeticException_propagates() {
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
    @DisplayName("CostModel returns null – getHoldingCost returns null")
    void costModelReturnsNull_getHoldingCostReturnsNull() {
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
}
```

Corresponding compilation feedback:
```
Picked up _JAVA_OPTIONS: -Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:15: error: package org.mockito.junit.jupiter does not exist
import org.mockito.junit.jupiter.MockitoExtension;
                                ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:16: error: package org.junit.jupiter.api.extension does not exist
import org.junit.jupiter.api.extension.ExtendWith;
                                      ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:19: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
 ^
  symbol: class ExtendWith
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:17: error: package org.mockito does not exist
import org.mockito.*;
^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:19: error: cannot find symbol
@ExtendWith(MockitoExtension.class)
            ^
  symbol: class MockitoExtension
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:23: error: cannot find symbol
    @Mock
     ^
  symbol:   class Mock
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:77: error: cannot find symbol
    @BeforeEach
     ^
  symbol:   class BeforeEach
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:86: error: cannot find symbol
    @AfterEach
     ^
  symbol:   class AfterEach
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:129: error: cannot find symbol
    @DisplayName("ZeroCostModel behavior – holding cost must be zero")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:139: error: cannot find symbol
    @DisplayName("Custom constant CostModel – should return the constant value")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:154: error: cannot find symbol
    @DisplayName("Bar‑based HoldingCostModel – cost = (exitIndex‑entryIndex) * unitCost")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:179: error: cannot find symbol
    @DisplayName("NaN HoldingCostModel for open positions – should return NaN")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:196: error: cannot find symbol
    @DisplayName("CostModel receives the same Position instance – verify identity")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:214: error: cannot find symbol
    @DisplayName("ZeroCostModel with non‑zero trades – still returns zero")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:227: error: cannot find symbol
    @DisplayName("Null holdingCostModel via reflection – getHoldingCost throws NPE")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:237: error: cannot find symbol
    @DisplayName("CostModel that throws NullPointerException – exception propagates")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:252: error: cannot find symbol
    @DisplayName("CostModel that throws IllegalArgumentException – exception propagates")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:267: error: cannot find symbol
    @DisplayName("CostModel that throws ArithmeticException – exception propagates")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:282: error: cannot find symbol
    @DisplayName("CostModel returns null – getHoldingCost returns null")
     ^
  symbol:   class DisplayName
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:44: error: MockCostModel is not abstract and does not override abstract method calculate(Num,Num) in CostModel
    private static class MockCostModel extends AbstractCostModel {
                   ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:89: error: cannot find symbol
        reset(mockHoldingCostModel);
        ^
  symbol:   method reset(CostModel)
  location: class Position_getHoldingCost_Test
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:96: error: cannot find symbol
        mockHoldingCostModel.setReturn(expectedHoldingCost);
                            ^
  symbol:   method setReturn(Num)
  location: variable mockHoldingCostModel of type CostModel
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:101: error: cannot find symbol
        assertEquals("calculate() must be called exactly once", 1, mockHoldingCostModel.getCallCount());
                                                                                       ^
  symbol:   method getCallCount()
  location: variable mockHoldingCostModel of type CostModel
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:107: error: cannot find symbol
        mockHoldingCostModel.setReturn(NaN);
                            ^
  symbol:   method setReturn(Num)
  location: variable mockHoldingCostModel of type CostModel
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:112: error: cannot find symbol
        assertEquals(1, mockHoldingCostModel.getCallCount());
                                            ^
  symbol:   method getCallCount()
  location: variable mockHoldingCostModel of type CostModel
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:141: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$1> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel constantFive = new AbstractCostModel() {
                                                         ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:157: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$2> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel perBarCost = new AbstractCostModel() {
                                                       ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:181: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$3> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel nanIfOpen = new AbstractCostModel() {
                                                      ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:203: error: received has private access in MockCostModel
                this.received = position;
                    ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:239: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$5> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel throwsNPE = new AbstractCostModel() {
                                                      ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:254: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$6> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel throwsIAE = new AbstractCostModel() {
                                                      ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:269: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$7> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel throwsAE = new AbstractCostModel() {
                                                     ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:284: error: <anonymous org.ta4j.core.Position_getHoldingCost_Test$8> is not abstract and does not override abstract method calculate(Num,Num) in CostModel
        CostModel returnsNull = new AbstractCostModel() {
                                                        ^
src/test/java/org/ta4j/core/Position_getHoldingCost_Test.java:307: error: cannot find symbol
        mockHoldingCostModel.reset();
                            ^
  symbol:   method reset()
  location: variable mockHoldingCostModel of type CostModel
34 errors

```

And you can use the following related information to assist you in your analysis and correction:
```
{'api document of class org.ta4j.core.Position': '\n * A {@code Position} is a pair of two {@link Trade trades}.\n *\n * <p>\n * The exit trade has the complement type of the entry trade, i.e.:\n * <ul>\n * <li>entry == BUY --> exit == SELL\n * <li>entry == SELL --> exit == BUY\n * </ul>\n ', 'api document of method getHoldingCost()': '\n     * Calculates the holding cost of the closed position.\n     *\n     * @return the cost of the position\n     ', 'return type': 'org.ta4j.core.num.Num', 'dependent classes': 'class `org.ta4j.core.Position`:\n\tdependent fields:\n\t\torg.ta4j.core.analysis.cost.CostModel org.ta4j.core.Position.holdingCostModel;\n\trelated functions:\n\t\tmethod `public Num getHoldingCost(int)` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getHoldingCost()`, api document: \n     * Calculates the holding cost of the position.\n     *\n     * @param finalIndex the index of the final bar to be considered (if position is\n     *                   open)\n     * @return the cost of the position\n     ;\n\t\tmethod `public Num getPositionCost()` returns `org.ta4j.core.num.Num`, related with `org.ta4j.core.Position.getHoldingCost()`, api document: \n     * Calculates the total cost of the closed position.\n     *\n     * @return the cost of the position\n     ;\nclass `org.ta4j.core.Strategy`:\n\trelated functions:\n\t\tmethod ` boolean shouldOperate(int, TradingRecord)` returns `boolean`, related with `org.ta4j.core.Position.getHoldingCost()`, api document: \n     * @param index         the bar index\n     * @param tradingRecord the potentially needed trading history\n     * @return true to recommend a trade, false otherwise (no recommendation)\n     ;'}
```

**Note**: If the method-to-test in the @Test function is a private method, you can use **reflection** to call it.

Now you should output the corrected Java code wrapped in a Markdown code block ```java at the beginning and ``` at the end: 


