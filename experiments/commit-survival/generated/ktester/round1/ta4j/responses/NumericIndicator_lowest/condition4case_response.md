{
    "group": "lowest_positive_barCount",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "NumericIndicator whose delegate is a LowestValueIndicator that returns the minimum of the last 3 values of the source indicator. For a source indicator with values [10, 8, 12, 5, 7] the returned indicator should produce values [10, 8, 8, 5, 5] at indexes 0‑4 respectively.",
            "description": "Verify that `lowest(3)` creates a NumericIndicator wrapping a LowestValueIndicator and that it correctly calculates the lowest value over the specified window."
        }
    ]
}