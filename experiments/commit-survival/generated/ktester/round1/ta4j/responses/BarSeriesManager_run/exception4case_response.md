{
    "group": "BarSeriesManager.run(Strategy) - null BarSeries",
    "cases": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "AlwaysEnterExitStrategy"
                }
            ],
            "expected": "NullPointerException",
            "description": "The BarSeriesManager is configured with a null BarSeries (e.g., via a constructor or setter that allows null). Invoking run(strategy) with a valid non‑null strategy should trigger a NullPointerException because the implementation attempts to access the series before delegating to the overloaded run method."
        }
    ]
}