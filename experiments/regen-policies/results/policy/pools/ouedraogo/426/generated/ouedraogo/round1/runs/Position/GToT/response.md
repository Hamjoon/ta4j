**Step 1 – Public API of `Position`**

| # | Method signature | Description |
|---|------------------|-------------|
| 1 | `public Trade getEntry()` | Returns the entry trade (may be `null`). |
| 2 | `public Trade getExit()` | Returns the exit trade (may be `null`). |
| 3 | `public ExecutionSide side()` | Returns the side (BUY/SELL) of the entry trade or `null`. |
| 4 | `public Num amount()` | Returns the amount of the entry trade or `null`. |
| 5 | `public Num averageEntryPrice()` | Returns the entry price per asset or `null`. |
| 6 | `public Num totalEntryCost()` | Returns the entry value (price × amount) or `null`. |
| 7 | `public Num totalFees()` | Returns the entry fees (cost) or `null`. |
| 8 | `public boolean equals(Object obj)` | Equality based on entry & exit trades. |
| 9 | `public int hashCode()` | Hash based on entry & exit. |
|10| `public Trade operate(int index)` | Operates the position with *NaN* price/amount. |
|11| `public Trade operate(int index, Num price, Num amount)` | Operates the position creating a new entry or exit trade. |
|12| `public Trade operate(Trade trade)` | Operates the position with a pre‑built trade. |
|13| `public boolean isClosed()` | `true` when both entry & exit are set. |
|14| `public boolean isOpened()` | `true` when entry is set but exit is not. |
|15| `public boolean isNew()` | `true` when neither entry nor exit is set. |
|16| `public boolean hasProfit()` | `true` when closed **and** profit > 0. |
|17| `public boolean hasLoss()` | `true` when closed **and** profit < 0. |
|18| `public Num getProfit()` | Net profit of a **closed** position (0 for opened). |
|19| `public Num getProfit(int finalIndex, Num finalPrice)` | Net profit for an opened position up to a given bar. |
|20| `public Num getGrossProfit()` | Gross profit of a **closed** position (0 for opened). |
|21| `public Num getGrossProfit(Num finalPrice)` | Gross profit for opened or closed position. |
|22| `public Num getGrossReturn()` | Gross return (incl. base) for a **closed** position (0 for opened). |
|23| `public Num getGrossReturn(Num finalPrice)` | Gross return for a given exit price. |
|24| `public Num getGrossReturn(BarSeries barSeries)` | Gross return using prices taken from a `BarSeries`. |
|25| `public Num getGrossReturn(Num entryPrice, Num exitPrice)` | Gross return between two explicit prices. |
|26| `public Num getPositionCost(int finalIndex)` | Total cost (transaction + holding) for an opened position. |
|27| `public Num getPositionCost()` | Total cost for a closed position. |
|28| `public Num getHoldingCost()` | Holding cost for a closed position. |
|29| `public Num getHoldingCost(int finalIndex)` | Holding cost for an opened position. |
|30| `public CostModel getTransactionCostModel()` | Transaction‑cost model (defaults to `ZeroCostModel`). |
|31| `public CostModel getHoldingCostModel()` | Holding‑cost model (defaults to `ZeroCostModel`). |
|32| `public TradeType getStartingType()` | The type that the position expects for its *first* trade. |
|33| `public String toString()` | Human readable representation (not used in tests). |

---

**Step 2 – Basic test cases (one per method)**  

Below each method a minimal “happy‑path” test is sketched.  
All tests use the real library classes (`Trade`, `TradeType`, `DecimalNum`, `ZeroCostModel`, `BaseBarSeries`).

```java
// 1 – getEntry()
@Test public void testGetEntry_returnsEntryTrade() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(entry, p.getEntry());
}

// 2 – getExit()
@Test public void testGetExit_returnsExitTrade() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(exit, p.getExit());
}

// 3 – side()
@Test public void testSide_returnsBUY_forBuyEntry() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(ExecutionSide.BUY, p.side());
}

// 4 – amount()
@Test public void testAmount_returnsEntryAmount() {
    Num amount = DecimalNum.valueOf(2);
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), amount, new ZeroCostModel());
    Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(amount, p.amount());
}

// 5 – averageEntryPrice()
@Test public void testAverageEntryPrice_returnsEntryPrice() {
    Num price = DecimalNum.valueOf(123.45);
    Trade entry = Trade.buyAt(0, price, DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(price, p.averageEntryPrice());
}

// 6 – totalEntryCost()
@Test public void testTotalEntryCost_returnsValue() {
    Num price = DecimalNum.valueOf(10);
    Num amount = DecimalNum.valueOf(3);
    Trade entry = Trade.buyAt(0, price, amount, new ZeroCostModel());
    Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
    Num expected = price.multipliedBy(amount);
    assertEquals(expected, p.totalEntryCost());
}

// 7 – totalFees()
@Test public void testTotalFees_returnsEntryCostModelFee() {
    // ZeroCostModel returns zero, but we can still check the call chain
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(entry.getCost(), p.totalFees());
}

// 8 – equals() & 9 – hashCode()
@Test public void testEqualsAndHashCode() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p1 = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    Position p2 = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    assertTrue(p1.equals(p2));
    assertEquals(p1.hashCode(), p2.hashCode());
}

//10 – operate(int)
@Test public void testOperateCreatesEntryWhenNew() {
    Position p = new Position(); // default BUY, new
    Trade t = p.operate(0);
    assertNotNull(t);
    assertTrue(p.isOpened());
    assertEquals(TradeType.BUY, t.getType());
}

//11 – operate(int,price,amount)
@Test public void testOperateCreatesExitWhenOpened() {
    Position p = new Position(); // new
    p.operate(0); // entry
    Trade exit = p.operate(3, DecimalNum.valueOf(105), DecimalNum.valueOf(1));
    assertNotNull(exit);
    assertTrue(p.isClosed());
    assertEquals(TradeType.SELL, exit.getType());
}

//12 – operate(Trade)
@Test public void testOperateWithTradeObject() {
    Position p = new Position(); // new
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    p.operate(entry);
    assertTrue(p.isOpened());
    Trade exit = Trade.sellAt(4, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    p.operate(exit);
    assertTrue(p.isClosed());
}

//13 – isClosed()
@Test public void testIsClosed() {
    Position p = new Position(); // new
    assertFalse(p.isClosed());
    p.operate(0);
    assertFalse(p.isClosed());
    p.operate(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1));
    assertTrue(p.isClosed());
}

//14 – isOpened()
@Test public void testIsOpened() {
    Position p = new Position();
    assertFalse(p.isOpened());
    p.operate(0);
    assertTrue(p.isOpened());
    p.operate(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1));
    assertFalse(p.isOpened());
}

//15 – isNew()
@Test public void testIsNew() {
    Position p = new Position();
    assertTrue(p.isNew());
    p.operate(0);
    assertFalse(p.isNew());
}

//16 – hasProfit()
@Test public void testHasProfit_trueWhenProfitPositive() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(120), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    assertTrue(p.hasProfit());
}

//17 – hasLoss()
@Test public void testHasLoss_trueWhenProfitNegative() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(120), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    assertTrue(p.hasLoss());
}

//18 – getProfit()
@Test public void testGetProfit_closedPosition() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(2), new ZeroCostModel());
    Trade exit  = Trade.sellAt(3, DecimalNum.valueOf(110), DecimalNum.valueOf(2), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    Num expected = DecimalNum.valueOf(20); // (110‑100)*2 = 20, no costs
    assertEquals(expected, p.getProfit());
}

//19 – getProfit(int,price) – opened position
@Test public void testGetProfit_openedPosition() {
    Position p = new Position(); // new BUY
    p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1));
    Num profit = p.getProfit(5, DecimalNum.valueOf(110));
    Num expected = DecimalNum.valueOf(10); // (110‑100)*1
    assertEquals(expected, profit);
}

//20 – getGrossProfit()
@Test public void testGetGrossProfit_closed() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(50), DecimalNum.valueOf(2), new ZeroCostModel());
    Trade exit  = Trade.sellAt(4, DecimalNum.valueOf(70), DecimalNum.valueOf(2), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    Num expected = DecimalNum.valueOf(40); // (70‑50)*2
    assertEquals(expected, p.getGrossProfit());
}

//21 – getGrossProfit(Num) – opened
@Test public void testGetGrossProfit_opened() {
    Position p = new Position();
    p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(3));
    Num gross = p.getGrossProfit(DecimalNum.valueOf(115));
    Num expected = DecimalNum.valueOf(45); // (115‑100)*3
    assertEquals(expected, gross);
}

//22 – getGrossReturn()
@Test public void testGetGrossReturn_closedBuy() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    Num expected = DecimalNum.valueOf(1.10); // 110/100
    assertEquals(expected, p.getGrossReturn());
}

//23 – getGrossReturn(Num)
@Test public void testGetGrossReturn_givenPrice() {
    Trade entry = Trade.sellAt(0, DecimalNum.valueOf(200), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.buyAt(3, DecimalNum.valueOf(180), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    // short position: profit = entry‑exit → return = 1‑(exit/entry)
    Num expected = DecimalNum.valueOf(0.90); // ((180/200)-1) = -0.10 → 1‑0.10 = 0.90
    assertEquals(expected, p.getGrossReturn());
}

//24 – getGrossReturn(BarSeries)
@Test public void testGetGrossReturn_withBarSeries() {
    BaseBarSeries series = new BaseBarSeriesBuilder().withName("test").build();
    series.addBar(100); // index 0
    series.addBar(115); // index 1
    Trade entry = Trade.buyAt(0, series, DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, series, DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    Num ret = p.getGrossReturn(series);
    assertEquals(DecimalNum.valueOf(1.15), ret);
}

//25 – getGrossReturn(entryPrice,exitPrice)
@Test public void testGetGrossReturn_twoPrices() {
    Num entry = DecimalNum.valueOf(50);
    Num exit  = DecimalNum.valueOf(75);
    Position p = new Position(); // type does not matter for static method
    Num ret = p.getGrossReturn(entry, exit);
    assertEquals(DecimalNum.valueOf(1.5), ret);
}

//26 – getPositionCost()
@Test public void testGetPositionCost_zeroWhenZeroCostModel() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    assertEquals(DecimalNum.valueOf(0), p.getPositionCost());
}

//27 – getHoldingCost()
@Test public void testGetHoldingCost_zeroWhenZeroCostModel() {
    Position p = new Position(); // new – holding cost model is ZeroCostModel
    assertEquals(DecimalNum.valueOf(0), p.getHoldingCost());
}

//28 – getTransactionCostModel()
@Test public void testGetTransactionCostModel_defaultsToZero() {
    Position p = new Position(); // default ctor uses ZeroCostModel
    assertTrue(p.getTransactionCostModel() instanceof ZeroCostModel);
}

//29 – getHoldingCostModel()
@Test public void testGetHoldingCostModel_defaultsToZero() {
    Position p = new Position();
    assertTrue(p.getHoldingCostModel() instanceof ZeroCostModel);
}

//30 – getStartingType()
@Test public void testGetStartingType_isBUY_byDefault() {
    Position p = new Position();
    assertEquals(TradeType.BUY, p.getStartingType());
}
```

---

**Step 3 – Edge‑case & exception scenarios**

| Method | Edge case / error | Expected behaviour |
|--------|-------------------|--------------------|
| `operate(int,price,amount)` | Called on an **opened** position with `index < entry.getIndex()` | `IllegalStateException` |
| `operate(Trade)` | `trade` is `null` | `NullPointerException` (via `Objects.requireNonNull`) |
| `operate(Trade)` | Trade’s cost model differs from the position’s model | `IllegalArgumentException` |
| `operate(Trade)` | First trade type does **not** match `startingType` | `IllegalArgumentException` |
| `operate(Trade)` | Exit trade type does **not** complement the entry type | `IllegalArgumentException` |
| `operate(Trade)` | Exit trade index < entry index | `IllegalStateException` |
| Constructor `Position(Trade entry, Trade exit)` | `entry` and `exit` have the **same** `TradeType` | `IllegalArgumentException` |
| Constructor `Position(Trade entry, Trade exit)` | `entry` or `exit` is `null` | `NullPointerException` |
| `side()`, `amount()`, `averageEntryPrice()`, `totalEntryCost()`, `totalFees()` when **new** (no entry) | Should return `null` (no exception) |
| `getProfit()` on an **opened** position | Returns **zero** (via `zero()`) – test that it is exactly zero even when entry price is NaN |
| `getGrossReturn(BarSeries)` when either entry or exit price is **NaN** | Uses bar‑series price – we verify that the method does not throw. |

Corresponding JUnit tests:

```java
// operate with index < entry index -> IllegalStateException
@Test(expected = IllegalStateException.class)
public void testOperateIndexBeforeEntry_throws() {
    Position p = new Position(); // new BUY
    p.operate(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1)); // creates entry at 5
    p.operate(3, DecimalNum.valueOf(110), DecimalNum.valueOf(1)); // index 3 < 5
}

// operate(null) -> NullPointerException
@Test(expected = NullPointerException.class)
public void testOperateNullTrade_throws() {
    Position p = new Position();
    p.operate((Trade) null);
}

// mismatching cost model
@Test(expected = IllegalArgumentException.class)
public void testOperateTradeWithDifferentCostModel_throws() {
    CostModel other = new FixedTransactionCostModel(DecimalNum.valueOf(1)); // any non‑zero model
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), other);
    Position p = new Position(); // uses ZeroCostModel internally
    p.operate(entry); // should fail
}

// first trade type does not match starting type
@Test(expected = IllegalArgumentException.class)
public void testOperateFirstTradeWrongType_throws() {
    Position p = new Position(TradeType.SELL); // expects first trade = SELL
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    p.operate(entry);
}

// exit trade type not complement
@Test(expected = IllegalArgumentException.class)
public void testOperateExitWrongComplement_throws() {
    Position p = new Position(); // expects BUY first
    p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1)); // entry BUY
    Trade badExit = Trade.sellAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    // try to add another BUY -> not complement
    Trade wrong = Trade.buyAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    p.operate(wrong);
}

// exit index before entry index
@Test(expected = IllegalStateException.class)
public void testOperateExitIndexBeforeEntry_throws() {
    Position p = new Position();
    p.operate(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1)); // entry at 5
    Trade exit = Trade.sellAt(3, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel());
    p.operate(exit);
}

// constructor with same trade types -> IllegalArgumentException
@Test(expected = IllegalArgumentException.class)
public void testClosedConstructorSameTradeTypes_throws() {
    Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.buyAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), new ZeroCostModel()); // also BUY
    new Position(entry, exit);
}

// side() etc. on a brand‑new position return null
@Test public void testNullValuesWhenNew() {
    Position p = new Position();
    assertNull(p.side());
    assertNull(p.amount());
    assertNull(p.averageEntryPrice());
    assertNull(p.totalEntryCost());
    assertNull(p.totalFees());
}

// getProfit() on opened returns zero
@Test public void testGetProfitOnOpened_isZero() {
    Position p = new Position();
    p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1));
    Num profit = p.getProfit();
    assertEquals(DecimalNum.valueOf(0), profit);
}

// getGrossReturn(BarSeries) when entry price is NaN – uses series price
@Test public void testGrossReturnWithNaNEntryUsesSeries() {
    BaseBarSeries series = new BaseBarSeriesBuilder().withName("s").build();
    series.addBar(DecimalNum.valueOf(150)); // index 0
    series.addBar(DecimalNum.valueOf(180)); // index 1
    // entry price NaN → we create a trade with NaN price (use NaN constant)
    Trade entry = Trade.buyAt(0, NaN, DecimalNum.valueOf(1), new ZeroCostModel());
    Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(180), DecimalNum.valueOf(1), new ZeroCostModel());
    Position p = new Position(entry, exit, new ZeroCostModel(), new ZeroCostModel());
    Num ret = p.getGrossReturn(series);
    // series entry price = 150, exit price = 180 → 180/150 = 1.2
    assertEquals(DecimalNum.valueOf(1.2), ret);
}
```

---

**Step 4 – Merged, ready‑to‑run JUnit 4 test file**

Below is the **complete** test suite.  
All imports are from the public API of the library (the concrete classes `DecimalNum`, `BaseBarSeries`, `BaseBarSeriesBuilder` are part of the same artifact and therefore allowed).  
The file starts with `###Test START##` and ends with `###Test END##` as requested.

```java
###Test START##
package org.ta4j.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NaN;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

/**
 * Comprehensive JUnit‑4 test suite for {@link Position}.
 *
 * The suite covers:
 *   • happy‑path tests for every public method,
 *   • edge‑case handling,
 *   • validation of exception throwing,
 *   • behaviour of new / opened / closed states.
 *
 * No mocking is used – all objects are created through the library's
 * public factories.
 */
public class PositionTest {

    private CostModel zeroCostModel;

    @Before
    public void setUp() {
        zeroCostModel = new ZeroCostModel();
    }

    /* -------------------------------------------------
     *  1 – getEntry()
     * ------------------------------------------------- */
    @Test
    public void testGetEntry_returnsEntryTrade() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        assertEquals(entry, p.getEntry());
    }

    /* -------------------------------------------------
     *  2 – getExit()
     * ------------------------------------------------- */
    @Test
    public void testGetExit_returnsExitTrade() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(exit, p.getExit());
    }

    /* -------------------------------------------------
     *  3 – side()
     * ------------------------------------------------- */
    @Test
    public void testSide_returnsBUY_forBuyEntry() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        assertEquals(ExecutionSide.BUY, p.side());
    }

    @Test
    public void testSide_returnsSELL_forSellEntry() {
        Trade entry = Trade.sellAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        assertEquals(ExecutionSide.SELL, p.side());
    }

    @Test
    public void testSide_returnsNull_whenNoEntry() {
        Position p = new Position(); // default ctor → no entry yet
        assertNull(p.side());
    }

    /* -------------------------------------------------
     *  4 – amount()
     * ------------------------------------------------- */
    @Test
    public void testAmount_returnsEntryAmount() {
        Num amount = DecimalNum.valueOf(2);
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), amount, zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        assertEquals(amount, p.amount());
    }

    @Test
    public void testAmount_returnsNull_whenNew() {
        Position p = new Position();
        assertNull(p.amount());
    }

    /* -------------------------------------------------
     *  5 – averageEntryPrice()
     * ------------------------------------------------- */
    @Test
    public void testAverageEntryPrice_returnsEntryPrice() {
        Num price = DecimalNum.valueOf(123.45);
        Trade entry = Trade.buyAt(0, price, DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        assertEquals(price, p.averageEntryPrice());
    }

    @Test
    public void testAverageEntryPrice_returnsNull_whenNew() {
        Position p = new Position();
        assertNull(p.averageEntryPrice());
    }

    /* -------------------------------------------------
     *  6 – totalEntryCost()
     * ------------------------------------------------- */
    @Test
    public void testTotalEntryCost_returnsValue() {
        Num price = DecimalNum.valueOf(10);
        Num amount = DecimalNum.valueOf(3);
        Trade entry = Trade.buyAt(0, price, amount, zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        Num expected = price.multipliedBy(amount);
        assertEquals(expected, p.totalEntryCost());
    }

    @Test
    public void testTotalEntryCost_returnsNull_whenNew() {
        Position p = new Position();
        assertNull(p.totalEntryCost());
    }

    /* -------------------------------------------------
     *  7 – totalFees()
     * ------------------------------------------------- */
    @Test
    public void testTotalFees_returnsEntryCost() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, zeroCostModel, zeroCostModel);
        // ZeroCostModel yields zero fees – we simply compare with entry.getCost()
        assertEquals(entry.getCost(), p.totalFees());
    }

    @Test
    public void testTotalFees_returnsNull_whenNew() {
        Position p = new Position();
        assertNull(p.totalFees());
    }

    /* -------------------------------------------------
     *  8 – equals() & 9 – hashCode()
     * ------------------------------------------------- */
    @Test
    public void testEqualsAndHashCode() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p1 = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Position p2 = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEquals_returnsFalseForDifferentPositions() {
        Trade entry1 = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit1  = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p1 = new Position(entry1, exit1, zeroCostModel, zeroCostModel);

        Trade entry2 = Trade.buyAt(0, DecimalNum.valueOf(200), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit2  = Trade.sellAt(5, DecimalNum.valueOf(210), DecimalNum.valueOf(1), zeroCostModel);
        Position p2 = new Position(entry2, exit2, zeroCostModel, zeroCostModel);

        assertFalse(p1.equals(p2));
    }

    /* -------------------------------------------------
     * 10 – operate(int)
     * ------------------------------------------------- */
    @Test
    public void testOperateCreatesEntryWhenNew() {
        Position p = new Position(); // default BUY, new
        Trade t = p.operate(0);
        assertNotNull(t);
        assertTrue(p.isOpened());
        assertEquals(TradeType.BUY, t.getType());
    }

    /* -------------------------------------------------
     * 11 – operate(int,price,amount)
     * ------------------------------------------------- */
    @Test
    public void testOperateCreatesExitWhenOpened() {
        Position p = new Position(); // new
        p.operate(0); // entry
        Trade exit = p.operate(3, DecimalNum.valueOf(105), DecimalNum.valueOf(1));
        assertNotNull(exit);
        assertTrue(p.isClosed());
        assertEquals(TradeType.SELL, exit.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateIndexBeforeEntry_throws() {
        Position p = new Position(); // new BUY
        p.operate(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1)); // entry @5
        p.operate(3, DecimalNum.valueOf(110), DecimalNum.valueOf(1)); // index < entry → exception
    }

    /* -------------------------------------------------
     * 12 – operate(Trade)
     * ------------------------------------------------- */
    @Test
    public void testOperateWithTradeObject() {
        Position p = new Position(); // new
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        p.operate(entry);
        assertTrue(p.isOpened());

        Trade exit = Trade.sellAt(4, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        p.operate(exit);
        assertTrue(p.isClosed());
    }

    @Test(expected = NullPointerException.class)
    public void testOperateNullTrade_throws() {
        Position p = new Position();
        p.operate((Trade) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateTradeWithDifferentCostModel_throws() {
        // custom non‑zero model to provoke mismatch
        CostModel other = new FixedTransactionCostModel(DecimalNum.valueOf(1));
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), other);
        Position p = new Position(); // internal model = ZeroCostModel
        p.operate(entry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateFirstTradeWrongType_throws() {
        Position p = new Position(TradeType.SELL); // expects first trade = SELL
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        p.operate(entry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateExitWrongComplement_throws() {
        Position p = new Position(); // expects BUY first
        p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1)); // entry BUY
        Trade wrongExit = Trade.buyAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel); // BUY again
        p.operate(wrongExit);
    }

    @Test(expected = IllegalStateException.class)
    public void testOperateExitIndexBeforeEntry_throws() {
        Position p = new Position();
        p.operate(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1)); // entry @5
        Trade exit = Trade.sellAt(3, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        p.operate(exit);
    }

    /* -------------------------------------------------
     * 13 – isClosed()
     * ------------------------------------------------- */
    @Test
    public void testIsClosed() {
        Position p = new Position();
        assertFalse(p.isClosed());

        p.operate(0);
        assertFalse(p.isClosed());

        p.operate(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1));
        assertTrue(p.isClosed());
    }

    /* -------------------------------------------------
     * 14 – isOpened()
     * ------------------------------------------------- */
    @Test
    public void testIsOpened() {
        Position p = new Position();
        assertFalse(p.isOpened());

        p.operate(0);
        assertTrue(p.isOpened());

        p.operate(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1));
        assertFalse(p.isOpened());
    }

    /* -------------------------------------------------
     * 15 – isNew()
     * ------------------------------------------------- */
    @Test
    public void testIsNew() {
        Position p = new Position();
        assertTrue(p.isNew());

        p.operate(0);
        assertFalse(p.isNew());
    }

    /* -------------------------------------------------
     * 16 – hasProfit()
     * ------------------------------------------------- */
    @Test
    public void testHasProfit_trueWhenProfitPositive() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(120), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertTrue(p.hasProfit());
    }

    @Test
    public void testHasProfit_falseWhenNoProfit() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertFalse(p.hasProfit());
    }

    /* -------------------------------------------------
     * 17 – hasLoss()
     * ------------------------------------------------- */
    @Test
    public void testHasLoss_trueWhenProfitNegative() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(120), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertTrue(p.hasLoss());
    }

    @Test
    public void testHasLoss_falseWhenNoLoss() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertFalse(p.hasLoss());
    }

    /* -------------------------------------------------
     * 18 – getProfit()
     * ------------------------------------------------- */
    @Test
    public void testGetProfit_closedPosition() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(2), zeroCostModel);
        Trade exit  = Trade.sellAt(3, DecimalNum.valueOf(110), DecimalNum.valueOf(2), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Num expected = DecimalNum.valueOf(20); // (110‑100)*2
        assertEquals(expected, p.getProfit());
    }

    @Test
    public void testGetProfit_openedPosition_returnsZero() {
        Position p = new Position(); // new BUY
        p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1));
        assertEquals(DecimalNum.valueOf(0), p.getProfit());
    }

    /* -------------------------------------------------
     * 19 – getProfit(int,finalPrice) – opened position
     * ------------------------------------------------- */
    @Test
    public void testGetProfit_openedPosition_withFinalValues() {
        Position p = new Position(); // default BUY
        p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1));
        Num profit = p.getProfit(5, DecimalNum.valueOf(115));
        assertEquals(DecimalNum.valueOf(15), profit);
    }

    /* -------------------------------------------------
     * 20 – getGrossProfit()
     * ------------------------------------------------- */
    @Test
    public void testGetGrossProfit_closed() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(50), DecimalNum.valueOf(2), zeroCostModel);
        Trade exit  = Trade.sellAt(4, DecimalNum.valueOf(70), DecimalNum.valueOf(2), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(DecimalNum.valueOf(40), p.getGrossProfit());
    }

    @Test
    public void testGetGrossProfit_opened() {
        Position p = new Position();
        p.operate(0, DecimalNum.valueOf(100), DecimalNum.valueOf(3));
        assertEquals(DecimalNum.valueOf(45), p.getGrossProfit(DecimalNum.valueOf(115)));
    }

    /* -------------------------------------------------
     * 21 – getGrossProfit(Num) – opened already covered
     * ------------------------------------------------- */

    /* -------------------------------------------------
     * 22 – getGrossReturn()
     * ------------------------------------------------- */
    @Test
    public void testGetGrossReturn_closedBuy() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(DecimalNum.valueOf(1.10), p.getGrossReturn());
    }

    @Test
    public void testGetGrossReturn_closedSell() {
        Trade entry = Trade.sellAt(0, DecimalNum.valueOf(200), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.buyAt(3, DecimalNum.valueOf(180), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        // short profit = (entry‑exit)/entry → 1‑(180/200) = 0.10 → return = 0.90
        assertEquals(DecimalNum.valueOf(0.90), p.getGrossReturn());
    }

    /* -------------------------------------------------
     * 23 – getGrossReturn(Num)
     * ------------------------------------------------- */
    @Test
    public void testGetGrossReturn_givenPrice() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(50), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(75), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(DecimalNum.valueOf(1.5), p.getGrossReturn(DecimalNum.valueOf(75)));
    }

    /* -------------------------------------------------
     * 24 – getGrossReturn(BarSeries)
     * ------------------------------------------------- */
    @Test
    public void testGetGrossReturn_withBarSeries() {
        BaseBarSeries series = new BaseBarSeriesBuilder().withName("testSeries").build();
        series.addBar(DecimalNum.valueOf(100));
        series.addBar(DecimalNum.valueOf(115));

        Trade entry = Trade.buyAt(0, series, DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(1, series, DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(DecimalNum.valueOf(1.15), p.getGrossReturn(series));
    }

    @Test
    public void testGrossReturnWithNaNEntryUsesSeries() {
        BaseBarSeries series = new BaseBarSeriesBuilder().withName("s").build();
        series.addBar(DecimalNum.valueOf(150));
        series.addBar(DecimalNum.valueOf(180));

        Trade entry = Trade.buyAt(0, NaN, DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(1, DecimalNum.valueOf(180), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(DecimalNum.valueOf(1.2), p.getGrossReturn(series));
    }

    /* -------------------------------------------------
     * 25 – getGrossReturn(entryPrice,exitPrice)
     * ------------------------------------------------- */
    @Test
    public void testGetGrossReturn_twoPrices_buy() {
        Position p = new Position(); // starting type irrelevant
        Num ret = p.getGrossReturn(DecimalNum.valueOf(50), DecimalNum.valueOf(75));
        assertEquals(DecimalNum.valueOf(1.5), ret);
    }

    @Test
    public void testGetGrossReturn_twoPrices_sell() {
        // simulate a short position: entry price higher than exit price
        Trade entry = Trade.sellAt(0, DecimalNum.valueOf(200), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit = Trade.buyAt(1, DecimalNum.valueOf(180), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        Num ret = p.getGrossReturn(DecimalNum.valueOf(200), DecimalNum.valueOf(180));
        // Expected: ((180/200)-1).negate() + 1 = 0.90
        assertEquals(DecimalNum.valueOf(0.90), ret);
    }

    /* -------------------------------------------------
     * 26 – getPositionCost()
     * ------------------------------------------------- */
    @Test
    public void testGetPositionCost_zeroWhenZeroCostModel() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        assertEquals(DecimalNum.valueOf(0), p.getPositionCost());
    }

    /* -------------------------------------------------
     * 27 – getHoldingCost()
     * ------------------------------------------------- */
    @Test
    public void testGetHoldingCost_zeroWhenZeroCostModel() {
        Position p = new Position(); // default holding cost = ZeroCostModel
        assertEquals(DecimalNum.valueOf(0), p.getHoldingCost());
    }

    /* -------------------------------------------------
     * 28 – getTransactionCostModel()
     * ------------------------------------------------- */
    @Test
    public void testGetTransactionCostModel_defaultsToZero() {
        Position p = new Position();
        assertTrue(p.getTransactionCostModel() instanceof ZeroCostModel);
    }

    /* -------------------------------------------------
     * 29 – getHoldingCostModel()
     * ------------------------------------------------- */
    @Test
    public void testGetHoldingCostModel_defaultsToZero() {
        Position p = new Position();
        assertTrue(p.getHoldingCostModel() instanceof ZeroCostModel);
    }

    /* -------------------------------------------------
     * 30 – getStartingType()
     * ------------------------------------------------- */
    @Test
    public void testGetStartingType_isBUY_byDefault() {
        Position p = new Position();
        assertEquals(TradeType.BUY, p.getStartingType());
    }

    @Test
    public void testGetStartingType_respectsConstructorArgument() {
        Position p = new Position(TradeType.SELL);
        assertEquals(TradeType.SELL, p.getStartingType());
    }

    /* -------------------------------------------------
     * 31 – Constructor validation edge cases
     * ------------------------------------------------- */
    @Test(expected = IllegalArgumentException.class)
    public void testClosedConstructorSameTradeTypes_throws() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.buyAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        new Position(entry, exit);
    }

    @Test(expected = NullPointerException.class)
    public void testClosedConstructorNullEntry_throws() {
        Trade exit = Trade.sellAt(5, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        new Position(null, exit);
    }

    @Test(expected = NullPointerException.class)
    public void testClosedConstructorNullExit_throws() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        new Position(entry, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateStartingPosition_nullStartingType_throws() {
        new Position((TradeType) null); // constructor forwards to validateStartingPosition
    }

    /* -------------------------------------------------
     * 32 – toString() (sanity)
     * ------------------------------------------------- */
    @Test
    public void testToString_containsEntryAndExit() {
        Trade entry = Trade.buyAt(0, DecimalNum.valueOf(100), DecimalNum.valueOf(1), zeroCostModel);
        Trade exit  = Trade.sellAt(2, DecimalNum.valueOf(110), DecimalNum.valueOf(1), zeroCostModel);
        Position p = new Position(entry, exit, zeroCostModel, zeroCostModel);
        String s = p.toString();
        assertTrue(s.contains("Entry"));
        assertTrue(s.contains("exit"));
    }
}
###Test END##
```