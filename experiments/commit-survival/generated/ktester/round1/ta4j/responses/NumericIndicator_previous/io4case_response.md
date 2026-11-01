{
    "testCases": [
        {
            "group": "NumericIndicator.previous(int) - large barCount handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 10
                        }
                    ],
                    "expected": "For a series of length 10, any call to returnedIndicator.getValue(i) where i < 10 returns the original indicator's value at index 0 (the earliest available value). For i >= 10 the value equals original.getValue(i‑10).",
                    "description": "Validate behavior when barCount equals the series length: values before the first valid shifted index should fallback to the first element."
                },
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 15
                        }
                    ],
                    "expected": "Because 15 > series length (10), every index i returns original.getValue(0). No IndexOutOfBoundsException is thrown.",
                    "description": "Check that a barCount larger than the series size safely returns the earliest value for all indices."
                },
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 2147483647
                        }
                    ],
                    "expected": "For any index i, returnedIndicator.getValue(i) == original.getValue(0). The implementation must handle Integer.MAX_VALUE without overflow or exception.",
                    "description": "Stress‑test the method with an extreme positive barCount to ensure robust handling of integer overflow scenarios."
                },
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 0
                        }
                    ],
                    "expected": "The delegate of the returned NumericIndicator is an instance of PreviousValueIndicator whose internal offset is 0.",
                    "description": "Confirm that even for barCount 0 the method wraps the original indicator with a PreviousValueIndicator (rather than returning the original object directly)."
                }
            ]
        },
        {
            "group": "NumericIndicator.previous(int) - chaining behavior",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 2
                        }
                    ],
                    "expected": "Let A = original.previous(2) and B = A.previous(3). For any index i >= 5, B.getValue(i) == original.getValue(i‑5). For i < 5 the fallback to the earliest value applies as defined by PreviousValueIndicator.",
                    "description": "Verify that successive calls to previous() accumulate their offsets (2 + 3 = 5)."
                },
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 5
                        }
                    ],
                    "expected": "original.previous(5) yields exactly the same values as the chained version original.previous(2).previous(3) for every index.",
                    "description": "Ensure that a single call with the summed barCount is equivalent to multiple chained calls."
                }
            ]
        },
        {
            "group": "NumericIndicator.previous(int) - constant delegate behavior",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 4
                        }
                    ],
                    "expected": "When the underlying delegate is a ConstantIndicator (always returns Num.valueOf(42)), the returned NumericIndicator also returns 42 for every index, regardless of barCount.",
                    "description": "Confirm that previous() does not alter constant values and works correctly with a constant delegate."
                }
            ]
        }
    ]
}