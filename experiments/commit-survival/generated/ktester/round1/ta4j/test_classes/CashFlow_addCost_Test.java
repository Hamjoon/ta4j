package org.ta4j.core.analysis;

import org.junit.jupiter.api.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DecimalNum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CashFlow_addCost_Test {

    /**
     * Reusable {@code Num} values used by several tests.
     */
    private Num rawPrice;

    private Num holdingCost;

    @BeforeEach
    void setupBeforeEach() {
        // Initialise a typical positive price and a positive holding cost.
        rawPrice = DecimalNum.valueOf(100.0);
        holdingCost = DecimalNum.valueOf(5.0);
    }

    /**
     * Supplies all test arguments for {@link #addCost_parameterized}.
     *
     * @return a stream of {@link Arguments}
     */
    private static Stream<Arguments> addCostProvider() {
        return Stream.of(// 1. Long trade: 10 - 2 = 8
        Arguments.of(DecimalNum.valueOf("10"), DecimalNum.valueOf("2"), true, DecimalNum.valueOf("8"), null), // 2. Short trade: 10 + 2 = 12
        Arguments.of(DecimalNum.valueOf("10"), DecimalNum.valueOf("2"), false, DecimalNum.valueOf("12"), null), // 3. Long trade with zero holding cost: 5 - 0 = 5
        Arguments.of(DecimalNum.valueOf("5"), DecimalNum.valueOf("0"), true, DecimalNum.valueOf("5"), null), // 4. Short trade with negative raw price: -3 + 4 = 1
        Arguments.of(DecimalNum.valueOf("-3"), DecimalNum.valueOf("4"), false, DecimalNum.valueOf("1"), null), // 5. Large numbers, long trade: 1_000_000_000 - 999_999_999 = 1
        Arguments.of(DecimalNum.valueOf("1000000000"), DecimalNum.valueOf("999999999"), true, DecimalNum.valueOf("1"), null), // 6. Null rawPrice → NullPointerException
        Arguments.of(null, DecimalNum.valueOf("1"), true, null, NullPointerException.class), // 7. Null holdingCost → NullPointerException
        Arguments.of(DecimalNum.valueOf("1"), null, false, null, NullPointerException.class), // 8. Long trade with negative holding cost: 10 - (-2) = 12
        Arguments.of(DecimalNum.valueOf("10"), DecimalNum.valueOf("-2"), true, DecimalNum.valueOf("12"), null), // 9. Short trade with negative holding cost: 10 + (-2) = 8
        Arguments.of(DecimalNum.valueOf("10"), DecimalNum.valueOf("-2"), false, DecimalNum.valueOf("8"), null), // 10. Long trade where holding cost exceeds raw price: 3 - 5 = -2
        Arguments.of(DecimalNum.valueOf("3"), DecimalNum.valueOf("5"), true, DecimalNum.valueOf("-2"), null), // 11. Short trade with zero holding cost: 7 + 0 = 7
        Arguments.of(DecimalNum.valueOf("7"), DecimalNum.valueOf("0"), false, DecimalNum.valueOf("7"), null), // 12. Long trade with zero raw price: 0 - 5 = -5
        Arguments.of(DecimalNum.valueOf("0"), DecimalNum.valueOf("5"), true, DecimalNum.valueOf("-5"), null), // 13. Short trade with zero raw price: 0 + 5 = 5
        Arguments.of(DecimalNum.valueOf("0"), DecimalNum.valueOf("5"), false, DecimalNum.valueOf("5"), null), // 14. Both negative, long trade: -4 - (-3) = -1
        Arguments.of(DecimalNum.valueOf("-4"), DecimalNum.valueOf("-3"), true, DecimalNum.valueOf("-1"), null), // 15. Both negative, short trade: -4 + (-3) = -7
        Arguments.of(DecimalNum.valueOf("-4"), DecimalNum.valueOf("-3"), false, DecimalNum.valueOf("-7"), null), // 16. Large negative raw price, long trade: -1_000_000_000 - 999_999_999 = -1_999_999_999
        Arguments.of(DecimalNum.valueOf("-1000000000"), DecimalNum.valueOf("999999999"), true, DecimalNum.valueOf("-1999999999"), null), // 17. Large negative raw price, short trade: -1_000_000_000 + 999_999_999 = -1
        Arguments.of(DecimalNum.valueOf("-1000000000"), DecimalNum.valueOf("999999999"), false, DecimalNum.valueOf("-1"), null));
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

    /**
     * Parameterized test that iterates over all cases defined in the JSON
     * specification.  For normal cases the result is compared with the
     * expected {@link Num}; for cases that expect an exception the test
     * verifies that the exception is thrown.
     */
    @ParameterizedTest(name = "[{index}] raw={0}, holding={1}, long={2} → expected={3}")
    @MethodSource("addCostProvider")
    void addCost_parameterized(Num raw, Num holding, boolean isLong, Num expectedResult, Class<? extends Throwable> expectedException) {
        if (expectedException != null) {
            // Expecting an exception (e.g., NullPointerException)
            assertThrows(expectedException, () -> CashFlow.addCost(raw, holding, isLong), "Expected exception was not thrown");
        } else {
            // Normal calculation – compare the returned Num with the expectation
            Num actual = CashFlow.addCost(raw, holding, isLong);
            assertEquals(expectedResult, actual, () -> String.format("Incorrect net price for raw=%s, holding=%s, isLong=%s", raw, holding, isLong));
        }
    }
}
