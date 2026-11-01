{
    "testCases": [
        {
            "group": "NumericIndicator.previous(int) - extreme negative barCount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": -2147483648
                        }
                    ],
                    "expected": "IllegalArgumentException is thrown because the barCount is less than 0 (Integer.MIN_VALUE).",
                    "description": "Verify that the method rejects the most negative int value, which is still an invalid offset."
                }
            ]
        },
        {
            "group": "NumericIndicator.previous(int) - null delegate handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 0
                        }
                    ],
                    "expected": "IllegalArgumentException (or NullPointerException) is thrown when the NumericIndicator instance was created with a null delegate and previous() attempts to wrap it.",
                    "description": "Create a NumericIndicator with a null underlying Indicator (using NumericIndicator.of(null) if allowed) and call previous(0) to ensure the method validates the delegate before constructing PreviousValueIndicator."
                }
            ]
        }
    ]
}