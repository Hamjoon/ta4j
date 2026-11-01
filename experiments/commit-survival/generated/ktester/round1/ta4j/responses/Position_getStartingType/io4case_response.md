{
    "testCases": [
        {
            "group": "Starting Type Consistency",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "BUY"
                        },
                        {
                            "parameter": "exitTradeType",
                            "value": "SELL"
                        }
                    ],
                    "expected": "BUY",
                    "description": "A Position created with an entry trade of type BUY and later closed with a SELL trade must still return BUY from getStartingType()."
                },
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "SELL"
                        },
                        {
                            "parameter": "exitTradeType",
                            "value": "BUY"
                        }
                    ],
                    "expected": "SELL",
                    "description": "A Position created with an entry trade of type SELL and later closed with a BUY trade must still return SELL from getStartingType()."
                }
            ]
        },
        {
            "group": "Idempotent Retrieval",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "BUY"
                        }
                    ],
                    "expected": "BUY",
                    "description": "Calling getStartingType() multiple times on the same Position must consistently return the same value (BUY)."
                },
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "SELL"
                        }
                    ],
                    "expected": "SELL",
                    "description": "Calling getStartingType() multiple times on the same Position must consistently return the same value (SELL)."
                }
            ]
        },
        {
            "group": "Serialization Integrity",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "BUY"
                        }
                    ],
                    "expected": "BUY",
                    "description": "After a Position with starting type BUY is serialized and then deserialized, getStartingType() must still return BUY."
                },
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "SELL"
                        }
                    ],
                    "expected": "SELL",
                    "description": "After a Position with starting type SELL is serialized and then deserialized, getStartingType() must still return SELL."
                }
            ]
        },
        {
            "group": "State‑Independent Retrieval",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "BUY"
                        }
                    ],
                    "expected": "BUY",
                    "description": "Invoking other Position methods (e.g., getGrossProfit(), isClosed()) must not affect the value returned by getStartingType(). The method should always return BUY for this Position."
                },
                {
                    "input": [
                        {
                            "parameter": "entryTradeType",
                            "value": "SELL"
                        }
                    ],
                    "expected": "SELL",
                    "description": "Invoking other Position methods (e.g., getHoldingCost(), getProfit()) must not affect the value returned by getStartingType(). The method should always return SELL for this Position."
                }
            ]
        }
    ]
}