{
    "test_cases": [
        {
            "group": "stddev null delegate tests",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 3
                        }
                    ],
                    "expected": "NullPointerException thrown when constructing StandardDeviationIndicator because the NumericIndicator was created with a null delegate",
                    "description": "Create a NumericIndicator via NumericIndicator.of(null). Calling stddev(3) should propagate a NullPointerException from the StandardDeviationIndicator constructor, which expects a non‑null delegate."
                }
            ]
        },
        {
            "group": "stddev null series tests",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 3
                        }
                    ],
                    "expected": "NullPointerException thrown because the underlying delegate's getBarSeries() returns null",
                    "description": "Implement a custom Indicator<Num> that returns null for getBarSeries() (while providing valid getValue and getUnstableBars). Wrap it with NumericIndicator.of(customIndicator) and invoke stddev(3). The StandardDeviationIndicator will attempt to access the series and throw a NullPointerException."
                }
            ]
        }
    ]
}