{
    "newTestCases": [
        {
            "group": "Gross Return – Division by Zero",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesEntryZeroExitPositive"
                        }
                    ],
                    "expected": "ArithmeticException (division by zero)",
                    "description": "Entry price is zero, exit price is 100 → gross return calculation attempts division by zero and should throw an ArithmeticException"
                },
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesEntryZeroExitZero"
                        }
                    ],
                    "expected": "Num NaN",
                    "description": "Both entry and exit prices are zero → (0‑0)/0 results in NaN, which should be propagated as a Num NaN"
                }
            ]
        },
        {
            "group": "Gross Return – Extreme Values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesTinyEntryHugeExit"
                        }
                    ],
                    "expected": "Num value 9_999_900 (≈ (1000‑0.0001)/0.0001)",
                    "description": "Entry price is extremely small (0.0001) and exit price is very large (1000) → gross return should be a huge positive number, testing numeric stability"
                }
            ]
        },
        {
            "group": "Gross Return – Null Price Per Asset Handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesEntryPriceNull"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Entry trade's getPricePerAsset returns null while exit price is valid → method should throw NPE when trying to use the null entry price"
                },
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesExitPriceNull"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Exit trade's getPricePerAsset returns null while entry price is valid → method should throw NPE when trying to use the null exit price"
                }
            ]
        },
        {
            "group": "Gross Return – BarSeries Throws RuntimeException",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesThrowsOnPrice"
                        }
                    ],
                    "expected": "RuntimeException",
                    "description": "BarSeries implementation throws a RuntimeException when price per asset is requested → getGrossReturn should propagate the exception"
                }
            ]
        }
    ]
}