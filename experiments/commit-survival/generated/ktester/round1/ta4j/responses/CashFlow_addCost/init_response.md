```java
package org.ta4j.core.analysis;

import org.junit.jupiter.api.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;

/**
 * Unit tests for the static {@code CashFlow.addCost} method.
 *
 * <p>The method is pure – it only depends on the three arguments
 * {@code rawPrice}, {@code holdingCost} and {@code isLongTrade}.  The tests
 * therefore focus on the two logical branches (long vs. short trade) and on a
 * few edge‑case values (zero, negative numbers).</p>
 *
 * <p>We use {@link DecimalNum} (the default {@code Num} implementation in TA4J)
 * because it provides the required arithmetic operations ({@code plus},
 * {@code minus}) and a convenient {@code valueOf} factory method.</p>
 */
@ExtendWith(MockitoExtension.class)
class CashFlow_addCost_Test {

    /** Reusable {@code Num} values used by several tests. */
    private Num rawPrice;
    private Num holdingCost;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for this pure‑function test.
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise a typical positive price and a positive holding cost.
        rawPrice = DecimalNum.valueOf(100.0);
        holdingCost = DecimalNum.valueOf(5.0);
    }

    @AfterEach
    void teardownAfterEach() {
        // Nothing to clean up; method under test has no side‑effects.
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global resources to release.
    }

    @Test
    void addCost_longTrade_subtractsHoldingCost() {
        // isLongTrade = true → rawPrice - holdingCost
        Num result = CashFlow.addCost(rawPrice, holdingCost, true);
        Num expected = rawPrice.minus(holdingCost);
        assertEquals(expected, result, "Long trade should subtract holding cost");
    }

    @Test
    void addCost_shortTrade_addsHoldingCost() {
        // isLongTrade = false → rawPrice + holdingCost
        Num result = CashFlow.addCost(rawPrice, holdingCost, false);
        Num expected = rawPrice.plus(holdingCost);
        assertEquals(expected, result, "Short trade should add holding cost");
    }

    @Test
    void addCost_zeroHoldingCost_returnsRawPrice() {
        Num zeroHolding = DecimalNum.valueOf(0);
        Num resultLong = CashFlow.addCost(rawPrice, zeroHolding, true);
        Num resultShort = CashFlow.addCost(rawPrice, zeroHolding, false);
        assertEquals(rawPrice, resultLong, "Long trade with zero holding cost must equal raw price");
        assertEquals(rawPrice, resultShort, "Short trade with zero holding cost must equal raw price");
    }

    @Test
    void addCost_negativeValues_behaveCorrectly() {
        Num negativeRaw = DecimalNum.valueOf(-50.0);
        Num negativeHolding = DecimalNum.valueOf(-10.0);

        // Long trade: -50 - (-10) = -40
        Num longResult = CashFlow.addCost(negativeRaw, negativeHolding, true);
        Num longExpected = negativeRaw.minus(negativeHolding);
        assertEquals(longExpected, longResult, "Long trade with negative numbers");

        // Short trade: -50 + (-10) = -60
        Num shortResult = CashFlow.addCost(negativeRaw, negativeHolding, false);
        Num shortExpected = negativeRaw.plus(negativeHolding);
        assertEquals(shortExpected, shortResult, "Short trade with negative numbers");
    }

    @Test
    void addCost_holdingCostGreaterThanRawPrice_longTrade() {
        // rawPrice = 10, holdingCost = 20 → result = -10
        Num smallRaw = DecimalNum.valueOf(10.0);
        Num bigHolding = DecimalNum.valueOf(20.0);
        Num result = CashFlow.addCost(smallRaw, bigHolding, true);
        Num expected = smallRaw.minus(bigHolding);
        assertEquals(expected, result, "Long trade where holding cost exceeds raw price");
    }

    @Test
    void addCost_holdingCostGreaterThanRawPrice_shortTrade() {
        // rawPrice = 10, holdingCost = 20 → result = 30
        Num smallRaw = DecimalNum.valueOf(10.0);
        Num bigHolding = DecimalNum.valueOf(20.0);
        Num result = CashFlow.addCost(smallRaw, bigHolding, false);
        Num expected = smallRaw.plus(bigHolding);
        assertEquals(expected, result, "Short trade where holding cost exceeds raw price");
    }
}
```