{
    "testCases": [
        {
            "group": "CustomCostModel returning null",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 7
                        }
                    ],
                    "expected": "null",
                    "description": "A CostModel implementation that deliberately returns null from calculate(...). The Position#getHoldingCost method should forward this null value without throwing an exception."
                }
            ]
        },
        {
            "group": "CustomCostModel returning NaN for a specific index",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 42
                        }
                    ],
                    "expected": "NaN",
                    "description": "CostModel returns Num.NaN only when finalIndex equals 42. This verifies that Position#getHoldingCost propagates NaN exactly as produced by the model."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 41
                        }
                    ],
                    "expected": "41",
                    "description": "For any index other than 42 the same CostModel returns Num.valueOf(finalIndex). Ensures normal values are still passed through."
                }
            ]
        },
        {
            "group": "CostModel receives correct Position reference",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 3
                        }
                    ],
                    "expected": "true",
                    "description": "A mock CostModel records the Position instance received in calculate(...). The test asserts that the recorded reference is the same Position object on which getHoldingCost was invoked."
                }
            ]
        },
        {
            "group": "Extreme index values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 2147483647
                        }
                    ],
                    "expected": "2147483647",
                    "description": "Using Integer.MAX_VALUE to ensure the method correctly forwards very large positive indices to the CostModel."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -2147483648
                        }
                    ],
                    "expected": "-2147483648",
                    "description": "Using Integer.MIN_VALUE to verify that extreme negative indices are handled (no overflow) and passed unchanged to the CostModel."
                }
            ]
        },
        {
            "group": "CostModel that depends on Position state",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 2
                        }
                    ],
                    "expected": "200",
                    "description": "A CostModel calculates holding cost as entryPrice * finalIndex. The Position is created with an entry Trade whose price is Num.valueOf(100). For finalIndex 2 the expected cost is 100 * 2 = 200. This tests that Position#getHoldingCost forwards the Position itself so the model can inspect its trades."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 0
                        }
                    ],
                    "expected": "0",
                    "description": "Same state‑dependent CostModel, but with finalIndex 0, should yield a cost of 0, confirming correct handling of zero multiplier."
                }
            ]
        }
    ]
}