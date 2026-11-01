{
    "group": "BarSeriesManager.run(Strategy) - default BUY trade type",
    "cases": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "AlwaysEnterExitStrategy"
                }
            ],
            "expected": "TradingRecord containing at least one BUY trade (entry) and one SELL trade (exit) corresponding to the series length",
            "description": "Verify that a non‑null strategy which always returns true for both entry and exit signals results in a non‑empty TradingRecord. The method should delegate to run(strategy, TradeType.BUY) and use BUY as the default trade type."
        },
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "NeverEnterStrategy"
                }
            ],
            "expected": "TradingRecord with zero trades (empty record)",
            "description": "When the supplied strategy never signals an entry, the manager should still return a TradingRecord object, but it must contain no trades. This confirms that the default BUY trade type does not cause unintended trades."
        },
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null strategy should result in a NullPointerException (or the specific exception the implementation throws). The test ensures that the method validates its input before delegating."
        }
    ]
}