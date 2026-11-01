{
    "testCases": [
        {
            "group": "ZeroCostModel returns NaN",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 10
                        }
                    ],
                    "expected": "NaN",
                    "description": "When the Position is created with a ZeroCostModel, any call to getHoldingCost(int) should return NaN regardless of the supplied finalIndex."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -5
                        }
                    ],
                    "expected": "NaN",
                    "description": "ZeroCostModel must also return NaN for negative finalIndex values; the method does not perform its own validation."
                }
            ]
        },
        {
            "group": "CustomCostModel with simple conditional logic",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 0
                        }
                    ],
                    "expected": "0",
                    "description": "CustomCostModel returns Num.valueOf(0) when finalIndex == 0. This tests the branch where the index is exactly zero."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 5
                        }
                    ],
                    "expected": "5",
                    "description": "CustomCostModel returns Num.valueOf(finalIndex) for positive indices. This covers the 'positive index' branch."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -3
                        }
                    ],
                    "expected": "-3",
                    "description": "CustomCostModel returns Num.valueOf(finalIndex) for negative indices as well, exercising the 'negative index' branch."
                }
            ]
        },
        {
            "group": "CustomCostModel that throws on invalid index",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -1
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "The custom CostModel is designed to throw IllegalArgumentException when finalIndex < 0. The Position#getHoldingCost method should propagate this exception."
                }
            ]
        },
        {
            "group": "Position with null holdingCostModel",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 3
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "If a Position is instantiated with a null holdingCostModel (e.g., via reflection or a faulty constructor), calling getHoldingCost should raise a NullPointerException because the method dereferences the field."
                }
            ]
        }
    ]
}