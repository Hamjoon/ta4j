{
    "tests": [
        {
            "group": "NumericIndicator.max(Number) - edge cases",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 0
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), 0) for all i",
                    "description": "Constant 0 is compared against delegate values that include negative, zero and positive numbers (e.g., -3, 0, 4). The resulting indicator should return 0 for negative delegate values, keep 0 when delegate is 0, and keep the delegate value when it is positive."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": -5
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), -5) for all i",
                    "description": "Constant -5 is lower than some delegate values (e.g., -2, 3) and higher than others (e.g., -10). The indicator should return the larger of the two at each index."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 5.5
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), 5.5) for all i",
                    "description": "Constant is a Double with a fractional part. Verify that the indicator correctly handles non‑integer Numbers and returns the maximum with delegate values such as 5, 6, 5.5."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Number) - type variations",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 2147483647
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), Integer.MAX_VALUE) for all i",
                    "description": "Using Integer.MAX_VALUE ensures the method works with the largest 32‑bit integer. All delegate values should be lower, so the result is always Integer.MAX_VALUE."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": -2147483648
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), Integer.MIN_VALUE) for all i",
                    "description": "Using Integer.MIN_VALUE checks that a very low constant does not affect the max when delegate values are higher."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 9.223372036854776E18
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), Long.MAX_VALUE as Double) for all i",
                    "description": "Constant is a Double representation of Long.MAX_VALUE. Verify handling of large floating‑point constants."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 1.7976931348623157E308
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), Double.POSITIVE_INFINITY) for all i",
                    "description": "Constant is Double.MAX_VALUE (close to positive infinity). The result should always be this constant regardless of delegate values."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "-Infinity"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), Double.NEGATIVE_INFINITY) for all i",
                    "description": "Constant is negative infinity. The indicator should simply return the delegate values because any finite number is greater than negative infinity."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Number) - null handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": null
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Passing a null Number should result in a NullPointerException according to the contract of createConstant(Number)."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Number) - NaN and special floating values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "NaN"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), NaN) for all i",
                    "description": "When the constant is Double.NaN, the max operation should propagate NaN according to Num implementation (i.e., result is NaN for every index)."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "Infinity"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), Double.POSITIVE_INFINITY) for all i",
                    "description": "Constant is positive infinity; the resulting indicator should always return positive infinity."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Number) - delegate edge values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 0
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), 0) for all i",
                    "description": "Delegate is a ConstantIndicator of 0. The max with constant 0 should return 0 for every index, confirming that max works when both sides are equal."
                },
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": -10
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), -10) for all i",
                    "description": "Delegate is a ConstantIndicator of -20. Since -10 > -20, the resulting indicator should return -10 for all indices, testing max when delegate is constant and lower."
                }
            ]
        }
    ]
}