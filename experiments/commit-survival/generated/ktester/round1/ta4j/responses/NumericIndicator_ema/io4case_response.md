{
    "testCases": [
        {
            "group": "Edge barCount values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 2147483647
                        }
                    ],
                    "expected": "NumericIndicator wrapping an EMAIndicator with barCount=2147483647 and delegate equal to the original NumericIndicator (no exception thrown)",
                    "description": "Verify that the method can handle the maximum int value without throwing and still returns a proper NumericIndicator wrapper."
                },
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 1000000
                        }
                    ],
                    "expected": "NumericIndicator wrapping an EMAIndicator with barCount=1000000 and delegate equal to the original NumericIndicator",
                    "description": "Check that a very large but realistic barCount (1,000,000) is accepted and the wrapper is created correctly."
                }
            ]
        },
        {
            "group": "BarCount larger than series length",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 15
                        }
                    ],
                    "expected": "NumericIndicator wrapping an EMAIndicator with barCount=15 and delegate equal to the original NumericIndicator (no exception thrown)",
                    "description": "When the series contains fewer bars than the requested barCount (e.g., series length 10, barCount 15), ema should still construct the indicator without failing."
                }
            ]
        },
        {
            "group": "Multiple ema calls produce independent instances",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 5
                        }
                    ],
                    "expected": "Two distinct NumericIndicator objects, each wrapping an EMAIndicator with barCount=5 and delegate equal to the original NumericIndicator",
                    "description": "Calling ema(5) twice on the same NumericIndicator must return two different wrapper objects (different references) while preserving the same configuration."
                }
            ]
        },
        {
            "group": "Delegate integrity after ema",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 8
                        }
                    ],
                    "expected": "Original NumericIndicator's delegate remains unchanged; the returned NumericIndicator's delegate is a new EMAIndicator whose source is the original NumericIndicator",
                    "description": "Ensure that invoking ema does not mutate the original NumericIndicator's internal delegate."
                }
            ]
        },
        {
            "group": "Chaining ema calls",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "firstBarCount",
                            "value": 3
                        },
                        {
                            "parameter": "secondBarCount",
                            "value": 4
                        }
                    ],
                    "expected": "NumericIndicator wrapping an EMAIndicator(barCount=4) whose delegate is another EMAIndicator(barCount=3) whose delegate is the original NumericIndicator",
                    "description": "Test that chaining ema calls (numeric.ema(3).ema(4)) creates a nested EMAIndicator structure, confirming that each call uses the current NumericIndicator as the source."
                }
            ]
        }
    ]
}