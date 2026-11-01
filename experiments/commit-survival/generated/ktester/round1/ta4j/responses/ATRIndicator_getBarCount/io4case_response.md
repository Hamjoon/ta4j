{
    "group": "ATRIndicator.getBarCount.additional",
    "cases": [
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": 2147483647
                }
            ],
            "expected": 2147483647,
            "description": "Boundary case – the underlying MMAIndicator reports Integer.MAX_VALUE. getBarCount() must forward this maximum int value unchanged."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": -2147483648
                }
            ],
            "expected": -2147483648,
            "description": "Boundary case – the underlying MMAIndicator reports Integer.MIN_VALUE. Even though this is an unlikely real‑world value, getBarCount() should return it verbatim."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": 7
                }
            ],
            "expected": 7,
            "description": "Typical odd number – verifies that non‑even, non‑round numbers are propagated correctly."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": 123456789
                }
            ],
            "expected": 123456789,
            "description": "Large positive number – ensures that getBarCount() works for high‑magnitude but still valid counts."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicatorBarCount",
                    "value": -42
                }
            ],
            "expected": -42,
            "description": "Negative value different from the already‑tested -5 – confirms that any negative integer is returned unchanged, highlighting the method's lack of validation."
        }
    ]
}