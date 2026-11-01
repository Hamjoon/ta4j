{
    "group": "NumericIndicator.previous(int) - behavior verification",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 0
                }
            ],
            "expected": "The returned NumericIndicator yields the same value as the original indicator for any index (i.e., getValue(i) == original.getValue(i)).",
            "description": "Verify that a barCount of 0 returns an indicator that mirrors the original indicator (no shift)."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 1
                }
            ],
            "expected": "For any valid index i > 0, returnedIndicator.getValue(i) == originalIndicator.getValue(i - 1). For i == 0, the behavior follows PreviousValueIndicator contract (usually returns original value or throws).",
            "description": "Check that a barCount of 1 shifts the values by one bar backward."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 5
                }
            ],
            "expected": "For any valid index i >= 5, returnedIndicator.getValue(i) == originalIndicator.getValue(i - 5). For i < 5, the behavior follows PreviousValueIndicator contract.",
            "description": "Validate correct shifting for a larger positive barCount (5)."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": -1
                }
            ],
            "expected": "IllegalArgumentException (or any RuntimeException) is thrown because a negative barCount is invalid for PreviousValueIndicator.",
            "description": "Ensure that providing a negative barCount results in an exception, as the underlying PreviousValueIndicator does not accept negative offsets."
        }
    ]
}