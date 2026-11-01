{
    "test_cases": [
        {
            "group": "sma() - null delegate exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 5
                        }
                    ],
                    "expected": "NullPointerException thrown when constructing SMAIndicator because the NumericIndicator's internal delegate is null.",
                    "description": "Create a NumericIndicator via NumericIndicator.of(null). Calling sma(5) should fail immediately with NPE, proving that the method does not accept a null underlying indicator."
                }
            ]
        },
        {
            "group": "sma() - delegate with null BarSeries",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 3
                        }
                    ],
                    "expected": "NullPointerException (or IllegalArgumentException) thrown because the underlying delegate's getBarSeries() returns null, which SMAIndicator requires.",
                    "description": "Use a custom Indicator implementation whose getBarSeries() returns null. Invoking sma(3) must propagate an exception indicating the missing BarSeries."
                }
            ]
        },
        {
            "group": "sma() - underlying indicator runtime exception propagation",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 4
                        }
                    ],
                    "expected": "RuntimeException (e.g., IllegalStateException) thrown when calling getValue(index) on the returned NumericIndicator, originating from the wrapped indicator's getValue implementation.",
                    "description": "Create a NumericIndicator wrapping an Indicator whose getValue(int) deliberately throws IllegalStateException for index >= 2. After calling sma(4), invoking getValue(2) on the SMA wrapper must propagate the same exception, confirming that sma() does not swallow runtime errors from the source indicator."
                }
            ]
        }
    ]
}