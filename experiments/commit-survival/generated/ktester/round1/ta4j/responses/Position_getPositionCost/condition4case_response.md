{
    "testCases": [
        {
            "group": "Standard calculations",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 0
                        }
                    ],
                    "expected": "15",
                    "description": "finalIndex = 0, transactionCostModel returns 5, holdingCost returns 10 → position cost = 5 + 10 = 15"
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 5
                        }
                    ],
                    "expected": "0",
                    "description": "finalIndex = 5, both transaction cost and holding cost are zero → position cost = 0"
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -1
                        }
                    ],
                    "expected": "5",
                    "description": "finalIndex = -1 (edge case), transactionCost = 2, holdingCost = 3 → position cost = 5"
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 1000
                        }
                    ],
                    "expected": "15",
                    "description": "finalIndex = 1000, transactionCost = 7, holdingCost = 8 → position cost = 15"
                }
            ]
        },
        {
            "group": "Zero cost scenarios",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 3
                        }
                    ],
                    "expected": "12",
                    "description": "ZeroCostModel returns 0 for transaction cost, holding cost = 12 → position cost = 12"
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 8
                        }
                    ],
                    "expected": "0",
                    "description": "ZeroCostModel returns 0 and holding cost also 0 → total cost = 0"
                }
            ]
        },
        {
            "group": "Exception propagation",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 2
                        }
                    ],
                    "expected": "RuntimeException",
                    "description": "transactionCostModel.calculate throws RuntimeException; getPositionCost should propagate the exception"
                }
            ]
        }
    ]
}