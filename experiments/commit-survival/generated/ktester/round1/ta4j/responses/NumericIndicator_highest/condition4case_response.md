{
    "group": "NumericIndicator.highest(int) tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 5
                }
            ],
            "expected": "Returns a NumericIndicator whose delegate is a HighestValueIndicator initialized with the same NumericIndicator as the source and barCount = 5. The returned object should not be null and its delegate should be an instance of HighestValueIndicator.",
            "description": "Verify normal operation with a positive barCount. The method should wrap a HighestValueIndicator around the current NumericIndicator and return a new NumericIndicator."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 0
                }
            ],
            "expected": "IllegalArgumentException (or any RuntimeException thrown by HighestValueIndicator for non‑positive barCount).",
            "description": "Check that passing a non‑positive barCount (zero) results in an exception, because HighestValueIndicator requires a strictly positive window size."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": -3
                }
            ],
            "expected": "IllegalArgumentException (or any RuntimeException thrown by HighestValueIndicator for non‑positive barCount).",
            "description": "Validate that a negative barCount also triggers an exception, ensuring input validation is enforced."
        }
    ]
}