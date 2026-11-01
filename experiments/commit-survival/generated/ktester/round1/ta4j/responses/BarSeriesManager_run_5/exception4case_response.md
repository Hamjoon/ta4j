{
    "test_cases": [
        {
            "group": "Null BarSeries handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": null
                        },
                        {
                            "parameter": "strategy",
                            "value": "validStrategyMock"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "startIndex",
                            "value": 0
                        },
                        {
                            "parameter": "finishIndex",
                            "value": 5
                        }
                    ],
                    "expected": "throws NullPointerException",
                    "description": "When the BarSeriesManager is constructed with a null BarSeries, the call to barSeries.one() inside run(...) triggers a NullPointerException before any delegation occurs."
                }
            ]
        },
        {
            "group": "Invalid finishIndex arguments",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "validStrategyMock"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        },
                        {
                            "parameter": "startIndex",
                            "value": 0
                        },
                        {
                            "parameter": "finishIndex",
                            "value": -2
                        }
                    ],
                    "expected": "throws IllegalArgumentException",
                    "description": "A negative finishIndex is illegal; the method should validate the range and throw IllegalArgumentException."
                }
            ]
        },
        {
            "group": "Start index beyond series size",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "validStrategyMock"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "startIndex",
                            "value": 50
                        },
                        {
                            "parameter": "finishIndex",
                            "value": 55
                        }
                    ],
                    "expected": "throws IllegalArgumentException",
                    "description": "If startIndex is greater than or equal to barSeries.getBarCount() (e.g., series size is 30), the method must reject the request with IllegalArgumentException."
                }
            ]
        },
        {
            "group": "Amount (Num) returned by barSeries.one() is null",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "validStrategyMock"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "startIndex",
                            "value": 1
                        },
                        {
                            "parameter": "finishIndex",
                            "value": 4
                        },
                        {
                            "parameter": "barSeries.one()",
                            "value": null
                        }
                    ],
                    "expected": "throws NullPointerException",
                    "description": "When barSeries.one() returns null, the delegating run method passes a null Num to the 5‑argument overload, which is expected to throw NullPointerException (or propagate it)."
                }
            ]
        }
    ]
}