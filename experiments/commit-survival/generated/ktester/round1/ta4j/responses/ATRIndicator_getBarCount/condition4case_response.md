{
    "group": "ATRIndicator.getBarCount",
    "cases": [
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": 10
                }
            ],
            "expected": 10,
            "description": "Typical case – the underlying MMAIndicator reports a positive bar count (10). getBarCount() should return the same value."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": 0
                }
            ],
            "expected": 0,
            "description": "Edge case – the underlying MMAIndicator reports zero bars. getBarCount() must forward this value unchanged."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": -5
                }
            ],
            "expected": -5,
            "description": "Defensive case – if the underlying MMAIndicator (perhaps due to a bug) returns a negative bar count, getBarCount() should still return the exact value without modification."
        }
    ]
}