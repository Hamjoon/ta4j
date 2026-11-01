{
    "test_cases": [
        {
            "group": "Null CostModel",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Construct a Position with a null holdingCostModel (e.g., via reflection or a constructor that allows null). Invoking getHoldingCost() should throw NullPointerException because the method tries to call calculate on a null reference."
                }
            ]
        },
        {
            "group": "CostModel throws NullPointerException",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Provide a custom CostModel whose calculate(Position) implementation deliberately throws NullPointerException. After creating a Position with this model, calling getHoldingCost() must propagate the NullPointerException."
                }
            ]
        },
        {
            "group": "CostModel throws IllegalArgumentException",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalArgumentException",
                    "description": "Implement a CostModel that validates the Position (e.g., requires a non‑null exit trade) and throws IllegalArgumentException when the Position is open. Construct an open Position with this model and verify that getHoldingCost() throws IllegalArgumentException."
                }
            ]
        },
        {
            "group": "CostModel throws ArithmeticException",
            "cases": [
                {
                    "input": [],
                    "expected": "ArithmeticException",
                    "description": "Create a CostModel whose calculate(Position) performs a division by zero (or any illegal arithmetic) and thus throws ArithmeticException. When getHoldingCost() is called on a Position using this model, the exception should be propagated."
                }
            ]
        },
        {
            "group": "CostModel returns null",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Supply a CostModel whose calculate(Position) returns null. The getHoldingCost() method will return null; however, if the calling code immediately dereferences the result (e.g., calls .isZero()), a NullPointerException would occur. This test asserts that getHoldingCost() itself does not throw, but the subsequent use of the returned value leads to NullPointerException, highlighting the need for non‑null contract."
                }
            ]
        }
    ]
}