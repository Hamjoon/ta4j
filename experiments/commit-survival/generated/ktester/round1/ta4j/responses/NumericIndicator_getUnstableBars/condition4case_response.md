{
    "group": "getUnstableBars",
    "cases": [
        {
            "input": [],
            "expected": 0,
            "description": "Verify that a freshly created NumericIndicator (using the static factory `of`) returns 0 for getUnstableBars()."
        },
        {
            "input": [],
            "expected": 0,
            "description": "Verify that a NumericIndicator created via the helper `closePrice(BarSeries)` returns 0 for getUnstableBars()."
        },
        {
            "input": [],
            "expected": 0,
            "description": "Verify that a NumericIndicator created via the helper `volume(BarSeries)` returns 0 for getUnstableBars()."
        },
        {
            "input": [],
            "expected": 0,
            "description": "Verify that a NumericIndicator obtained after chaining operations (e.g., `of(delegate).sma(5).abs()`) still returns 0 for getUnstableBars()."
        }
    ]
}