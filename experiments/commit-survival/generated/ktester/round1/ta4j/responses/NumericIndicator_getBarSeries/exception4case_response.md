{
    "group": "NumericIndicator.getBarSeries_exceptions",
    "cases": [
        {
            "input": [
                {
                    "parameter": "delegate",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Exception scenario: NumericIndicator is instantiated with a null delegate. Invoking getBarSeries() should throw a NullPointerException because the method attempts to call delegate.getBarSeries() on a null reference."
        },
        {
            "input": [
                {
                    "parameter": "delegate",
                    "value": "mock that throws IllegalArgumentException when getBarSeries() is called"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Exception propagation: the delegate's getBarSeries() method is configured to throw an IllegalArgumentException. NumericIndicator.getBarSeries() must not catch it and should let the same IllegalArgumentException bubble up to the caller."
        },
        {
            "input": [
                {
                    "parameter": "delegate",
                    "value": "mock that throws AssertionError when getBarSeries() is called"
                }
            ],
            "expected": "AssertionError",
            "description": "Error propagation: the delegate throws an AssertionError (an Error, not an Exception). NumericIndicator.getBarSeries() should propagate this Error unchanged, confirming that no catch‑all handling interferes with Errors."
        }
    ]
}