{
    "test_cases": [
        {
            "group": "getProfit - unopened position exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 5
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "100.0"
                        }
                    ],
                    "expected": "IllegalStateException",
                    "description": "Calling getProfit on a Position that has never been opened (no entry trade) should throw IllegalStateException because gross profit cannot be computed without an entry price."
                }
            ]
        },
        {
            "group": "getProfit - negative final price exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 10
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "-50.0"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Supplying a negative finalPrice should be rejected by getGrossProfit(finalPrice) and result in IllegalArgumentException (price must be non‑negative)."
                }
            ]
        },
        {
            "group": "getProfit - null cost model exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 8
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "120.0"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The Position is constructed with a null CostModel. When getProfit invokes getPositionCost(finalIndex), the underlying null reference causes a NullPointerException."
                }
            ]
        },
        {
            "group": "getProfit - finalIndex before entry index exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 2
                        },
                        {
                            "parameter": "finalPrice",
                            "value": "100.0"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "finalIndex is earlier than the index at which the Position was entered. getPositionCost should detect the invalid range and throw IllegalArgumentException."
                }
            ]
        }
    ]
}