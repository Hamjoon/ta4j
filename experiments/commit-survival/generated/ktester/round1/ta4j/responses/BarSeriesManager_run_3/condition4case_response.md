{
    "testCases": [
        {
            "group": "Normal behavior",
            "cases": [
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
                        { "parameter": "tradeType", "value": "TradeType.BUY" },
                        { "parameter": "amount", "value": "Num.valueOf(1000)" }
                    ],
                    "expected": "TradingRecord containing trades generated from the whole BarSeries (startIndex = barSeries.getBeginIndex(), finishIndex = barSeries.getEndIndex())",
                    "description": "Valid strategy, trade type and amount should result in a non‑null TradingRecord with trades produced by the strategy over the full series."
                },
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyNeverEnter" },
                        { "parameter": "tradeType", "value": "TradeType.SELL" },
                        { "parameter": "amount", "value": "Num.valueOf(500)" }
                    ],
                    "expected": "TradingRecord with zero trades (empty record)",
                    "description": "A strategy that never signals an entry should produce an empty TradingRecord even though the amount and trade type are valid."
                },
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyEnterOnce" },
                        { "parameter": "tradeType", "value": "TradeType.BUY" },
                        { "parameter": "amount", "value": "Num.valueOf(0)" }
                    ],
                    "expected": "TradingRecord with zero trades (empty record)",
                    "description": "When the amount is zero, the manager should not open any position; the resulting TradingRecord must be empty."
                }
            ]
        },
        {
            "group": "Invalid arguments",
            "cases": [
                {
                    "input": [
                        { "parameter": "strategy", "value": "null" },
                        { "parameter": "tradeType", "value": "TradeType.BUY" },
                        { "parameter": "amount", "value": "Num.valueOf(100)" }
                    ],
                    "expected": "NullPointerException or IllegalArgumentException",
                    "description": "Passing a null Strategy must cause the method to reject the call with an exception."
                },
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
                        { "parameter": "tradeType", "value": "null" },
                        { "parameter": "amount", "value": "Num.valueOf(100)" }
                    ],
                    "expected": "NullPointerException or IllegalArgumentException",
                    "description": "A null TradeType is not allowed; the method should throw an exception."
                },
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
                        { "parameter": "tradeType", "value": "TradeType.SELL" },
                        { "parameter": "amount", "value": "null" }
                    ],
                    "expected": "NullPointerException or IllegalArgumentException",
                    "description": "A null amount must be rejected with an exception."
                }
            ]
        },
        {
            "group": "Edge cases with BarSeries bounds",
            "cases": [
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
                        { "parameter": "tradeType", "value": "TradeType.BUY" },
                        { "parameter": "amount", "value": "Num.valueOf(100)" }
                    ],
                    "expected": "TradingRecord with zero trades (empty record)",
                    "description": "If the underlying BarSeries is empty (beginIndex > endIndex), the manager should return an empty TradingRecord without throwing."
                },
                {
                    "input": [
                        { "parameter": "strategy", "value": "MockStrategyEnterAtLastBar" },
                        { "parameter": "tradeType", "value": "TradeType.SELL" },
                        { "parameter": "amount", "value": "Num.valueOf(200)" }
                    ],
                    "expected": "TradingRecord with a single trade opened at the last bar and closed immediately (if exit condition is also true)",
                    "description": "When the start and finish indices are the same (single‑bar series), the method must still evaluate the strategy for that bar and produce the appropriate trade(s)."
                }
            ]
        }
    ]
}