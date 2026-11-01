{
    "tests": [
        {
            "group": "Null handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "null"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "When the supplied Strategy is null the method should throw a NullPointerException before delegating to the overload."
                },
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategy"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "null"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "When the supplied TradeType is null the method should throw a NullPointerException."
                }
            ]
        },
        {
            "group": "Delegation to overload with default amount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategy"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        }
                    ],
                    "expected": "TradingRecord returned from run(strategy, tradeType, barSeries.one())",
                    "description": "The method must delegate to the three‑argument overload using the amount returned by barSeries.one(). Verify that the overload is invoked exactly once with the same Strategy, TradeType and the amount from barSeries.one()."
                },
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategy"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        }
                    ],
                    "expected": "TradingRecord returned from run(strategy, tradeType, barSeries.one())",
                    "description": "Same delegation test for the SELL TradeType to ensure both enum values are passed correctly."
                }
            ]
        },
        {
            "group": "BarSeries.one() edge values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategy"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        }
                    ],
                    "expected": "TradingRecord with zero‑amount trades (amount = Num.ZERO)",
                    "description": "When barSeries.one() returns Num.ZERO the overload should be called with a zero amount, resulting in a TradingRecord that contains no executed trades."
                },
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategy"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        }
                    ],
                    "expected": "TradingRecord with a single trade of amount = Num.valueOf(1000)",
                    "description": "When barSeries.one() returns a positive amount (e.g., 1000) the overload should receive that amount and the resulting TradingRecord should reflect trades executed with that amount."
                }
            ]
        },
        {
            "group": "Strategy signal handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "strategyAlwaysEnter"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        }
                    ],
                    "expected": "TradingRecord containing a BUY trade at the first index",
                    "description": "A Strategy that always returns true for shouldEnter should cause the delegated overload to open a BUY trade at the start index. Verify that the returned TradingRecord contains exactly one BUY trade."
                },
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "strategyNeverEnter"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        }
                    ],
                    "expected": "Empty TradingRecord (no trades)",
                    "description": "A Strategy that never signals entry should result in an empty TradingRecord after delegation."
                },
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "strategyEnterThenExit"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        }
                    ],
                    "expected": "TradingRecord with one completed BUY‑SELL pair",
                    "description": "A Strategy that signals entry on the first bar and exit on the second bar should produce a TradingRecord containing a completed BUY trade followed by a SELL exit."
                }
            ]
        }
    ]
}