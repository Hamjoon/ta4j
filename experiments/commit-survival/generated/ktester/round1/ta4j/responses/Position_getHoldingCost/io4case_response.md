{
    "tests": [
        {
            "group": "Custom constant CostModel",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 5",
                    "description": "Create a Position with a custom CostModel whose calculate(Position) method always returns Num.valueOf(5). Call getHoldingCost() and verify that the returned Num equals 5, confirming that the method delegates to the supplied CostModel."
                }
            ]
        },
        {
            "group": "Bar‑based HoldingCostModel (cost per bar held)",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 12",
                    "description": "Use a CostModel that computes holding cost as (exitIndex - entryIndex) * unitCost, where unitCost = 3. Build a Position with an entry Trade at index 2 and an exit Trade at index 6. The expected cost is (6‑2) * 3 = 12. Verify that getHoldingCost() returns Num.valueOf(12)."
                }
            ]
        },
        {
            "group": "NaN HoldingCostModel for open positions",
            "cases": [
                {
                    "input": [],
                    "expected": "Num NaN",
                    "description": "Provide a CostModel that returns Num.NaN when the Position is not closed (i.e., exit trade is null). Construct a Position with only an entry Trade (open position) and invoke getHoldingCost(). The method should return NaN, demonstrating correct handling of incomplete positions."
                }
            ]
        },
        {
            "group": "CostModel receives the same Position instance",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 7",
                    "description": "Implement a CostModel that records the hashCode of the Position passed to calculate(Position) and returns Num.valueOf(7) if the hashCode matches the Position under test. After constructing the Position with this model, call getHoldingCost() and verify that the returned Num is 7, confirming that the exact Position instance is forwarded to the CostModel."
                }
            ]
        },
        {
            "group": "ZeroCostModel with non‑zero trades",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to ZeroCostModel.ZERO (i.e., Num representing 0)",
                    "description": "Even when the Position has entry and exit trades with non‑zero amounts and prices, using the built‑in ZeroCostModel must still yield a holding cost of zero. Construct such a Position, call getHoldingCost(), and assert the result is zero."
                }
            ]
        }
    ]
}