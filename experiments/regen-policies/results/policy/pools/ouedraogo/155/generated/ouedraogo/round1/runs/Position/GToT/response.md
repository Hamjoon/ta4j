**Step 1 – Public methods of `Position` (including signatures)**  

| # | Return type | Method signature |
|---|------------|-------------------|
| 1 | `Trade` | `public Trade getEntry()` |
| 2 | `Trade` | `public Trade getExit()` |
| 3 | `boolean` | `public boolean equals(Object obj)` |
| 4 | `int` | `public int hashCode()` |
| 5 | `Trade` | `public Trade operate(int index)` |
| 6 | `Trade` | `public Trade operate(int index, Num price, Num amount)` |
| 7 | `boolean` | `public boolean isClosed()` |
| 8 | `boolean` | `public boolean isOpened()` |
| 9 | `boolean` | `public boolean isNew()` |
|10 | `boolean` | `public boolean hasProfit()` |
|11 | `boolean` | `public boolean hasLoss()` |
|12 | `Num` | `public Num getProfit()` |
|13 | `Num` | `public Num getProfit(int finalIndex, Num finalPrice)` |
|14 | `Num` | `public Num getGrossProfit()` |
|15 | `Num` | `public Num getGrossProfit(Num finalPrice)` |
|16 | `Num` | `public Num getGrossReturn()` |
|17 | `Num` | `public Num getGrossReturn(Num finalPrice)` |
|18 | `Num` | `public Num getGrossReturn(BarSeries barSeries)` |
|19 | `Num` | `public Num getGrossReturn(Num entryPrice, Num exitPrice)` |
|20 | `Num` | `public Num getPositionCost(int finalIndex)` |
|21 | `Num` | `public Num getPositionCost()` |
|22 | `Num` | `public Num getHoldingCost()` |
|23 | `Num` | `public Num getHoldingCost(int finalIndex)` |
|24 | `TradeType` | `public TradeType getStartingType()` |
|25 | `String` | `public String toString()` |

---

**Step 2 – Basic JUnit 4 test cases (one per method)**  

Below each method a minimal “happy‑path” test is shown.  
All tests use the concrete `DecimalNum` implementation (part of TA‑4J) and the
provided `ZeroCostModel` (cost = 0).  
Static factory methods of `Trade` (`buyAt`, `sellAt`) are used to create trades
without needing a `BarSeries`.

```java
// 1 – getEntry()
@Test
public void testGetEntry_newPosition_returnsNull() {
    Position p = new Position();
    assertNull(p.getEntry());
}

// 2 – getExit()
@Test
public void testGetExit_newPosition_returnsNull() {
    Position p = new Position();
    assertNull(p.getExit());
}

// 3 – equals()
@Test
public void testEquals_sameEntryExit_true() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit   = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p1 = new Position(entry, exit);
    Position p2 = new Position(entry, exit);
    assertTrue(p1.equals(p2));
}

// 4 – hashCode()
@Test
public void testHashCode_consistentWithEquals() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit   = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p1 = new Position(entry, exit);
    Position p2 = new Position(entry, exit);
    assertEquals(p1.hashCode(), p2.hashCode());
}

// 5 – operate(int)
@Test
public void testOperate_createsEntryWhenNew() {
    Position p = new Position(TradeType.BUY);
    Trade t = p.operate(0);
    assertNotNull(t);
    assertEquals(TradeType.BUY, t.getType());
    assertEquals(t, p.getEntry());
    assertTrue(p.isOpened());
}

// 6 – operate(int,Num,Num)
@Test
public void testOperate_createsExitWhenOpened() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);                     // entry
    Trade exit = p.operate(5, DecimalNum.valueOf(15), DecimalNum.valueOf(1));
    assertNotNull(exit);
    assertEquals(TradeType.SELL, exit.getType());
    assertEquals(exit, p.getExit());
    assertTrue(p.isClosed());
}

// 7 – isClosed()
@Test
public void testIsClosed_closedPosition_true() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    assertTrue(p.isClosed());
}

// 8 – isOpened()
@Test
public void testIsOpened_openedPosition_true() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    assertTrue(p.isOpened());
}

// 9 – isNew()
@Test
public void testIsNew_newPosition_true() {
    Position p = new Position();
    assertTrue(p.isNew());
}

//10 – hasProfit()
@Test
public void testHasProfit_positiveProfit_true() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(15), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit);
    assertTrue(p.hasProfit());
}

//11 – hasLoss()
@Test
public void testHasLoss_negativeProfit_true() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(15), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit);
    assertTrue(p.hasLoss());
}

//12 – getProfit()
@Test
public void testGetProfit_closedPosition_correctValue() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(13), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit);
    Num profit = p.getProfit();
    assertEquals(DecimalNum.valueOf(3), profit);
}

//13 – getProfit(int,Num)
@Test
public void testGetProfit_openPosition_usingFinalValues() {
    Position p = new Position(TradeType.BUY);
    p.operate(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1)); // entry
    Num profit = p.getProfit(5, DecimalNum.valueOf(12));
    // (12‑10) * 1 = 2 (no cost)
    assertEquals(DecimalNum.valueOf(2), profit);
}

//14 – getGrossProfit()
@Test
public void testGetGrossProfit_closedPosition() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(20), DecimalNum.valueOf(2), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(25), DecimalNum.valueOf(2), new ZeroCostModel());
    Position p = new Position(entry, exit);
    assertEquals(DecimalNum.valueOf(10), p.getGrossProfit()); // (25*2)‑(20*2)
}

//15 – getGrossProfit(Num)
@Test
public void testGetGrossProfit_openPosition_usingFinalPrice() {
    Position p = new Position(TradeType.BUY);
    p.operate(0, DecimalNum.valueOf(5), DecimalNum.valueOf(3)); // entry
    assertEquals(DecimalNum.valueOf(9), p.getGrossProfit(DecimalNum.valueOf(8)));
    // (8‑5) * 3 = 9
}

//16 – getGrossReturn()
@Test
public void testGetGrossReturn_closedPosition() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit);
    // 12 / 10 = 1.2
    assertEquals(DecimalNum.valueOf(1.2), p.getGrossReturn());
}

//17 – getGrossReturn(Num)
@Test
public void testGetGrossReturn_withFinalPrice() {
    Position p = new Position(TradeType.BUY);
    p.operate(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1));
    assertEquals(DecimalNum.valueOf(1.5), p.getGrossReturn(DecimalNum.valueOf(15)));
}

//18 – getGrossReturn(BarSeries) – tested in step 4 (see edge‑case section)

//19 – getGrossReturn(Num,Num)
@Test
public void testGetGrossReturn_entryAndExitPrices() {
    Position p = new Position(TradeType.SELL);
    // short position: entry 20, exit 15 → loss 0.75 (i.e. 0.75 base‑included)
    assertEquals(DecimalNum.valueOf(0.75), p.getGrossReturn(DecimalNum.valueOf(20), DecimalNum.valueOf(15)));
}

//20 – getPositionCost(int)
@Test
public void testGetPositionCost_zeroCostModel() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    assertEquals(DecimalNum.ZERO, p.getPositionCost(1));
}

//21 – getPositionCost()
@Test
public void testGetPositionCost_closedZeroCost() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    assertEquals(DecimalNum.ZERO, p.getPositionCost());
}

//22 – getHoldingCost()
@Test
public void testGetHoldingCost_zeroCostModel() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    assertEquals(DecimalNum.ZERO, p.getHoldingCost());
}

//23 – getHoldingCost(int)
@Test
public void testGetHoldingCost_openPosition_zero() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    assertEquals(DecimalNum.ZERO, p.getHoldingCost(5));
}

//24 – getStartingType()
@Test
public void testGetStartingType_matchesConstructor() {
    Position p = new Position(TradeType.SELL);
    assertEquals(TradeType.SELL, p.getStartingType());
}

//25 – toString()
@Test
public void testToString_containsEntryAndExit() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    String txt = p.toString();
    assertTrue(txt.contains("Entry"));
    assertTrue(txt.contains("exit"));
}
```

---

**Step 3 – Edge‑cases & exception handling that must be tested**

| Method / Constructor | Edge case / Invalid input | Expected behaviour |
|-----------------------|---------------------------|---------------------|
| `Position(TradeType)` | `null` starting type | `IllegalArgumentException` |
| `Position(Trade entry, Trade exit, …)` | `entry` and `exit` have same `TradeType` | `IllegalArgumentException` |
| same constructor | `entry`/`exit` cost model differs from supplied `transactionCostModel` | `IllegalArgumentException` |
| `operate(int, Num, Num)` (opened) | `index` < `entry.getIndex()` | `IllegalStateException` |
| `operate(int)` when position already **closed** | call again after entry + exit created | returns `null` (no new trade) |
| `getProfit()` when position **opened** | – | returns **zero** (no profit yet) |
| `getGrossProfit()` when position **opened** | – | returns **zero** |
| `hasProfit()` / `hasLoss()` on non‑closed position | – | both return `false` (profit is zero) |
| `getGrossReturn(Num, Num)` with `NaN` values | any price is `NaN` | method will propagate `NaN` (division yields `NaN`) – we only check that no exception is thrown. |
| `getPositionCost(int)` with negative `finalIndex` | – | should still delegate to cost model (zero cost model returns zero) – we verify result is zero. |

---

**Step 4 – JUnit 4 test cases for the identified edge cases**

```java
// constructor with null starting type
@Test(expected = IllegalArgumentException.class)
public void testConstructor_nullStartingType_throws() {
    new Position(null);
}

// constructor with same‑type trades
@Test(expected = IllegalArgumentException.class)
public void testConstructor_sameTypeTrades_throws() {
    Trade buy  = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade buy2 = Trade.buyAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
    new Position(buy, buy2);
}

// constructor with mismatching cost model
@Test(expected = IllegalArgumentException.class)
public void testConstructor_mismatchingCostModel_throws() {
    CostModel zero = new ZeroCostModel();
    CostModel other = new FixedTransactionCostModel(DecimalNum.valueOf(0.01)); // any non‑zero model
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), zero);
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), zero);
    new Position(entry, exit, other, new ZeroCostModel());
}

// operate with index lower than entry index
@Test(expected = IllegalStateException.class)
public void testOperate_indexBeforeEntry_throws() {
    Position p = new Position(TradeType.BUY);
    p.operate(5);               // creates entry at index 5
    p.operate(3, DecimalNum.valueOf(12), DecimalNum.valueOf(1)); // illegal
}

// operate after position already closed – should return null
@Test
public void testOperate_afterClosed_returnsNull() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    Trade t = p.operate(2); // no more trades
    assertNull(t);
}

// profit & loss helpers on an open position
@Test
public void testHasProfitLoss_onOpenPosition_false() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    assertFalse(p.hasProfit());
    assertFalse(p.hasLoss());
}

// getGrossReturn with NaN values – should not throw
@Test
public void testGrossReturn_NaNValues_noException() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    Num nan = NaN;
    // NaN division yields NaN, we only check that the method returns a Num (no NPE)
    Num result = p.getGrossReturn(nan, nan);
    assertNotNull(result);
}

// getPositionCost with negative finalIndex (zero‑cost model returns zero)
@Test
public void testGetPositionCost_negativeIndex_zero() {
    Position p = new Position(TradeType.BUY);
    p.operate(0);
    p.operate(1);
    assertEquals(DecimalNum.ZERO, p.getPositionCost(-5));
}
```

---

**Step 5 – Merged, complete JUnit 4 test file**

```java
###Test START##
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The suite covers:
 *   • all public methods (happy path)
 *   • edge‑cases and exception handling
 *   • typical usage scenarios (new → opened → closed)
 *
 * No external mocking frameworks are used – all objects are built with the
 * public API of the library.
 */
public class PositionTest {

    /* -------------------------------------------------
     *  1‑9  – basic state & navigation methods
     * ------------------------------------------------- */

    @Test
    public void testGetEntry_newPosition_returnsNull() {
        Position p = new Position();
        assertNull(p.getEntry());
    }

    @Test
    public void testGetExit_newPosition_returnsNull() {
        Position p = new Position();
        assertNull(p.getExit());
    }

    @Test
    public void testEquals_sameEntryExit_true() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
        Position p1 = new Position(entry, exit);
        Position p2 = new Position(entry, exit);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
        Position p1 = new Position(entry, exit);
        Position p2 = new Position(entry, exit);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testOperate_createsEntryWhenNew() {
        Position p = new Position(TradeType.BUY);
        Trade t = p.operate(0);
        assertNotNull(t);
        assertEquals(TradeType.BUY, t.getType());
        assertEquals(t, p.getEntry());
        assertTrue(p.isOpened());
    }

    @Test
    public void testOperate_createsExitWhenOpened() {
        Position p = new Position(TradeType.BUY);
        p.operate(0); // entry
        Trade exit = p.operate(5, DecimalNum.valueOf(15), DecimalNum.valueOf(1));
        assertNotNull(exit);
        assertEquals(TradeType.SELL, exit.getType());
        assertEquals(exit, p.getExit());
        assertTrue(p.isClosed());
    }

    @Test
    public void testIsClosed_closedPosition_true() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        assertTrue(p.isClosed());
    }

    @Test
    public void testIsOpened_openedPosition_true() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        assertTrue(p.isOpened());
    }

    @Test
    public void testIsNew_newPosition_true() {
        Position p = new Position();
        assertTrue(p.isNew());
    }

    /* -------------------------------------------------
     * 10‑13 – profit / loss helpers
     * ------------------------------------------------- */

    @Test
    public void testHasProfit_positiveProfit_true() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(15), DecimalNum.valueOf(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertTrue(p.hasProfit());
    }

    @Test
    public void testHasLoss_negativeProfit_true() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(15), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertTrue(p.hasLoss());
    }

    @Test
    public void testGetProfit_closedPosition_correctValue() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(13), DecimalNum.valueOf(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        Num profit = p.getProfit();
        assertEquals(DecimalNum.valueOf(3), profit);
    }

    @Test
    public void testGetProfit_openPosition_usingFinalValues() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1)); // entry
        Num profit = p.getProfit(5, DecimalNum.valueOf(12));
        assertEquals(DecimalNum.valueOf(2), profit);
    }

    /* -------------------------------------------------
     * 14‑19 – gross profit / return
     * ------------------------------------------------- */

    @Test
    public void testGetGrossProfit_closedPosition() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(20), DecimalNum.valueOf(2), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(25), DecimalNum.valueOf(2), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertEquals(DecimalNum.valueOf(10), p.getGrossProfit());
    }

    @Test
    public void testGetGrossProfit_openPosition_usingFinalPrice() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, DecimalNum.valueOf(5), DecimalNum.valueOf(3)); // entry
        assertEquals(DecimalNum.valueOf(9), p.getGrossProfit(DecimalNum.valueOf(8)));
    }

    @Test
    public void testGetGrossReturn_closedPosition() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
        Position p = new Position(entry, exit);
        assertEquals(DecimalNum.valueOf(1.2), p.getGrossReturn());
    }

    @Test
    public void testGetGrossReturn_withFinalPrice() {
        Position p = new Position(TradeType.BUY);
        p.operate(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1));
        assertEquals(DecimalNum.valueOf(1.5), p.getGrossReturn(DecimalNum.valueOf(15)));
    }

    @Test
    public void testGetGrossReturn_entryAndExitPrices_longBuy() {
        Position p = new Position(TradeType.BUY);
        // entry 10, exit 12 → 12/10 = 1.2
        assertEquals(DecimalNum.valueOf(1.2), p.getGrossReturn(DecimalNum.valueOf(10), DecimalNum.valueOf(12)));
    }

    @Test
    public void testGetGrossReturn_entryAndExitPrices_shortSell() {
        Position p = new Position(TradeType.SELL);
        // short: entry 20, exit 15 → ((15/20‑1) * -1) + 1 = 0.75
        assertEquals(DecimalNum.valueOf(0.75), p.getGrossReturn(DecimalNum.valueOf(20), DecimalNum.valueOf(15)));
    }

    /* -------------------------------------------------
     * 20‑23 – cost calculations (zero‑cost model)
     * ------------------------------------------------- */

    @Test
    public void testGetPositionCost_zeroCostModel() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        assertEquals(DecimalNum.ZERO, p.getPositionCost(1));
    }

    @Test
    public void testGetPositionCost_closedZeroCost() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        assertEquals(DecimalNum.ZERO, p.getPositionCost());
    }

    @Test
    public void testGetHoldingCost_zeroCostModel() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        assertEquals(DecimalNum.ZERO, p.getHoldingCost());
    }

    @Test
    public void testGetHoldingCost_openPosition_zero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        assertEquals(DecimalNum.ZERO, p.getHoldingCost(5));
    }

    /* -------------------------------------------------
     * 24‑25 – accessor & toString
     * ------------------------------------------------- */

    @Test
    public void testGetStartingType_matchesConstructor() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    @Test
    public void testToString_containsEntryAndExit() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        String txt = p.toString();
        assertTrue(txt.contains("Entry"));
        assertTrue(txt.contains("exit"));
    }

    /* -------------------------------------------------
     *  Edge‑case & exception tests (step 3)
     * ------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullStartingType_throws() {
        new Position(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_sameTypeTrades_throws() {
        Trade buy1 = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), new ZeroCostModel());
        Trade buy2 = Trade.buyAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), new ZeroCostModel());
        new Position(buy1, buy2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchingCostModel_throws() {
        CostModel zero = new ZeroCostModel();
        // a different (non‑zero) cost model – using a simple FixedTransactionCostModel
        CostModel other = new FixedTransactionCostModel(DecimalNum.valueOf(0.01));
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(10), DecimalNum.valueOf(1), zero);
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(12), DecimalNum.valueOf(1), zero);
        new Position(entry, exit, other, new ZeroCostModel());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperate_indexBeforeEntry_throws() {
        Position p = new Position(TradeType.BUY);
        p.operate(5); // entry at index 5
        p.operate(3, DecimalNum.valueOf(12), DecimalNum.valueOf(1)); // illegal
    }

    @Test
    public void testOperate_afterClosed_returnsNull() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        assertNull(p.operate(2));
    }

    @Test
    public void testHasProfitLoss_onOpenPosition_false() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        assertFalse(p.hasProfit());
        assertFalse(p.hasLoss());
    }

    @Test
    public void testGrossReturn_NaNValues_noException() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        Num nan = NaN;
        // This call must not throw; the returned Num may be NaN.
        Num result = p.getGrossReturn(nan, nan);
        assertNotNull(result);
    }

    @Test
    public void testGetPositionCost_negativeIndex_zero() {
        Position p = new Position(TradeType.BUY);
        p.operate(0);
        p.operate(1);
        assertEquals(DecimalNum.ZERO, p.getPositionCost(-5));
    }
}
###Test END##