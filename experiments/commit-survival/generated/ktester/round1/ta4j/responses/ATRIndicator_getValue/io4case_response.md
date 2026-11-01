{
    "group": "ATRIndicator.getValue(int) edge index tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 0
                }
            ],
            "expected": "Num value returned by underlying MMAIndicator for the first bar (e.g., DecimalNum.valueOf(1.23))",
            "description": "Verify that getValue correctly delegates to the underlying MMAIndicator when the index is the first valid position (0) and returns the exact Num instance."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": "lastValidIndex"
                }
            ],
            "expected": "Num value returned by underlying MMAIndicator for the last valid bar (e.g., DecimalNum.valueOf(9.87))",
            "description": "When the index equals series.getBarCount() - 1, getValue should delegate to the MMAIndicator and return its Num value without throwing."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": "seriesBarCount"
                }
            ],
            "expected": "IndexOutOfBoundsException",
            "description": "If the index equals series.getBarCount() (i.e., one past the last valid bar), the underlying MMAIndicator should throw IndexOutOfBoundsException which must propagate."
        }
    ]
}