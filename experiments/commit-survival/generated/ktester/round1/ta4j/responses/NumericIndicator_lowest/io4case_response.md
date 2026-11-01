{
    "testCases": [
        {
            "group": "lowest_zero_barCount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 0
                        }
                    ],
                    "expected": "IllegalArgumentException is thrown because barCount must be greater than 0",
                    "description": "Verify that calling `lowest(0)` on a NumericIndicator rejects a zero window size and throws an IllegalArgumentException."
                }
            ]
        },
        {
            "group": "lowest_negative_barCount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": -5
                        }
                    ],
                    "expected": "IllegalArgumentException is thrown because barCount must be positive",
                    "description": "Ensure that a negative barCount is considered invalid and results in an IllegalArgumentException."
                }
            ]
        },
        {
            "group": "lowest_barCount_exceeds_series",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 10
                        }
                    ],
                    "expected": "NumericIndicator whose delegate is a LowestValueIndicator that returns the minimum of all values seen so far (window limited by series start). For a source indicator with values [4, 9, 2, 7, 5] the returned indicator should produce values [4, 4, 2, 2, 2] at indexes 0‑4 respectively.",
                    "description": "Check that when barCount is larger than the series length, the indicator correctly uses the available values up to the current index instead of throwing an error."
                }
            ]
        },
        {
            "group": "lowest_barCount_one",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 1
                        }
                    ],
                    "expected": "NumericIndicator whose delegate is a LowestValueIndicator that simply mirrors the source values. For a source indicator with values [3, 6, 1, 8] the returned indicator should produce values [3, 6, 1, 8] at indexes 0‑3 respectively.",
                    "description": "Validate that a window size of 1 returns the original indicator values unchanged."
                }
            ]
        },
        {
            "group": "lowest_on_derived_indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": 3
                        }
                    ],
                    "expected": "NumericIndicator whose delegate is a LowestValueIndicator applied to the derived indicator (source + 2). For a source indicator with values [5, 1, 4, 9, 2] the derived indicator (source.plus(2)) yields [7, 3, 6, 11, 4]; the lowest(3) over this derived series should produce values [7, 3, 3, 3, 3] at indexes 0‑4 respectively.",
                    "description": "Confirm that `lowest` works on a NumericIndicator that has been transformed (e.g., via `plus`) and that the window calculation uses the transformed values."
                }
            ]
        }
    ]
}