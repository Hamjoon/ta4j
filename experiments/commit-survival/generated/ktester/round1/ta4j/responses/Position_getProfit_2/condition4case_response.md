{
    "testCases": [
        {
            "group": "getProfit - basic profit calculations",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 10
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "100.0"
                        }
                    ],
                    "expected": "Positive profit value equal to (grossProfit - tradingCost). For example, if gross profit = 150.0 and trading cost = 5.0, expected profit = 145.0",
                    "description": "Standard case where the position is closed at index 10 with a final price that yields a positive gross profit. Verifies that getProfit correctly subtracts the position cost from the gross profit."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 5
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "50.0"
                        }
                    ],
                    "expected": "Zero profit value (0.0). For example, if gross profit = 20.0 and trading cost = 20.0, expected profit = 0.0",
                    "description": "Edge case where gross profit exactly equals the trading cost, resulting in a break‑even (zero) profit. Ensures subtraction does not produce rounding errors."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 3
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "30.0"
                        }
                    ],
                    "expected": "Negative profit value (loss). For example, if gross profit = 10.0 and trading cost = 25.0, expected profit = -15.0",
                    "description": "Loss scenario where the trading cost exceeds the gross profit. Confirms that getProfit returns a negative number representing a loss."
                }
            ]
        },
        {
            "group": "getProfit - exceptional / boundary conditions",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 0
                        },
                        {
                            "parameter": "finalPrice",
                            "value": null
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Passing a null Num for finalPrice should cause a NullPointerException because getGrossProfit(finalPrice) dereferences the argument."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -1
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "100.0"
                        }
                    ],
                    "expected": "IndexOutOfBoundsException (or IllegalArgumentException depending on implementation)",
                    "description": "Using a negative finalIndex is invalid for getPositionCost(int). The method should propagate the exception thrown by the underlying cost calculation."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 999999
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "100.0"
                        }
                    ],
                    "expected": "IndexOutOfBoundsException (or IllegalArgumentException)",
                    "description": "Using an index far beyond the series length should trigger an exception from getPositionCost(int). This verifies that getProfit does not swallow such errors."
                }
            ]
        }
    ]
}