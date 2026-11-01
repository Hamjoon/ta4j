{
    "group": "NumericIndicator.getBarSeries",
    "cases": [
        {
            "input": [],
            "expected": "the same BarSeries instance that the delegate returns",
            "description": "Normal case: NumericIndicator is created with a non‑null delegate whose getBarSeries() returns a concrete BarSeries. The test verifies that NumericIndicator.getBarSeries() returns exactly that BarSeries instance."
        },
        {
            "input": [],
            "expected": "null (delegated getBarSeries() returns null)",
            "description": "Edge case: delegate is a mock that returns null from getBarSeries(). The test ensures that NumericIndicator.getBarSeries() propagates the null value without throwing an exception."
        }
    ]
}