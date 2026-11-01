{
    "tests": [
        {
            "group": "BarSeriesManager.run(Strategy) - entry without exit",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "AlwaysEnterNeverExitStrategy"
                        }
                    ],
                    "expected": "TradingRecord containing exactly one BUY trade (entry) with no corresponding SELL trade (position remains open)",
                    "description": "The strategy signals entry on every bar but never signals exit. The default BUY trade type should create a single open trade at the first signal and never close it, resulting in a TradingRecord with one entry and no exit."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy) - exit without prior entry",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "NeverEnterAlwaysExitStrategy"
                        }
                    ],
                    "expected": "TradingRecord with zero trades (empty record)",
                    "description": "The strategy never signals an entry but always signals an exit. Since there is no open position, the manager must ignore exit signals and return an empty TradingRecord."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy) - single‑bar series with immediate entry/exit",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "EnterAndExitSameBarStrategy"
                        }
                    ],
                    "expected": "TradingRecord containing one completed BUY‑SELL trade whose entry and exit indices are both 0",
                    "description": "The BarSeries contains only one bar. The strategy signals both entry and exit on that bar. The manager should record a trade that opens and closes on the same index, yielding a single completed trade."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy) - strategy throws exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "ExceptionThrowingStrategy"
                        }
                    ],
                    "expected": "RuntimeException (propagated from strategy)",
                    "description": "When the supplied strategy throws a RuntimeException during signal evaluation, the manager must not catch it silently; the exception should propagate to the caller."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy) - empty BarSeries",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "AlwaysEnterExitStrategy"
                        }
                    ],
                    "expected": "TradingRecord with zero trades (empty record)",
                    "description": "The manager is configured with an empty BarSeries (no bars). Even if the strategy would normally generate signals, there are no data points to evaluate, so the resulting TradingRecord must be empty."
                }
            ]
        }
    ]
}