{
    "test_cases": [
        {
            "group": "Negative amount handling",
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
                    "expected": "TradingRecord returned from run(strategy, tradeType, Num.valueOf(-500))",
                    "description": "When barSeries.one() returns a negative Num (e.g., -500), the method must delegate to the three‑argument overload with that exact negative amount. Verify that the overload is invoked once with the same Strategy, TradeType and the negative amount, and that its returned TradingRecord is propagated unchanged."
                }
            ]
        },
        {
            "group": "Exception propagation from BarSeries.one()",
            "cases": [
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
                    "expected": "IllegalStateException",
                    "description": "If barSeries.one() throws an IllegalStateException, run(Strategy, TradeType) must not catch it and should let the exception propagate to the caller."
                }
            ]
        },
        {
            "group": "Exception propagation from overload",
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
                    "expected": "IllegalArgumentException",
                    "description": "When the delegated overload run(strategy, tradeType, amount) throws an IllegalArgumentException, the two‑argument method must propagate the same exception without alteration."
                }
            ]
        },
        {
            "group": "Delegation integrity with mock verification",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "spyStrategy"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        }
                    ],
                    "expected": "TradingRecord returned from run(strategy, tradeType, barSeries.one())",
                    "description": "Using a spy for the BarSeriesManager, verify that run(strategy, tradeType) calls the three‑argument overload exactly once with the original Strategy instance, the original TradeType enum value, and the amount obtained from barSeries.one(). Ensure no additional calls are made."
                }
            ]
        },
        {
            "group": "BarSeries.one() returns null",
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
                    "expected": "NullPointerException",
                    "description": "If barSeries.one() returns null, the method should throw a NullPointerException before delegating to the overload, because a null amount is not a valid Num."
                }
            ]
        }
    ]
}