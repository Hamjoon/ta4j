{
    "group": "NumericIndicator.closePrice(BarSeries) additional tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "fiveBarSeries"
                }
            ],
            "expected": "NumericIndicator whose delegate returns the close price of each bar; getValue(0) == 10.0, getValue(2) == 30.5, getValue(4) == 50.0",
            "description": "Validate that closePrice correctly wraps a ClosePriceIndicator for a typical series with five bars and that values are accessible at arbitrary indices."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "extremeValueSeries"
                }
            ],
            "expected": "NumericIndicator whose delegate returns extreme close prices; getValue(0) == Double.MAX_VALUE, getValue(1) == Double.MIN_VALUE, getValue(2) == 0.0, getValue(3) == -12345.678",
            "description": "Check handling of very large, very small, zero and negative close prices to ensure no overflow or sign errors."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "constantSeries"
                }
            ],
            "expected": "NumericIndicator whose delegate returns the same close price for all bars; getValue(i) == 42.42 for any valid i",
            "description": "Confirm that a series where every bar has the identical close price yields identical values from the NumericIndicator."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "dynamicSeries"
                }
            ],
            "expected": "After appending a new bar with close price 200.0 to the series, the previously created NumericIndicator reflects the new value at the new index (getValue(2) == 200.0)",
            "description": "Ensure that the NumericIndicator is live‑linked to the underlying BarSeries and reflects changes made after its creation."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "fiveBarSeries"
                }
            ],
            "expected": "Two separate calls to closePrice(fiveBarSeries) return distinct NumericIndicator instances (different object identity) but their delegates produce identical values for all indices.",
            "description": "Verify that closePrice creates a new NumericIndicator each time rather than returning a cached instance."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "fiveBarSeries"
                }
            ],
            "expected": "The NumericIndicator returned by closePrice reports getBarSeries() == fiveBarSeries and getUnstableBars() == 0",
            "description": "Check that the wrapper correctly propagates the original BarSeries reference and reports zero unstable bars (ClosePriceIndicator is stable)."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "nanCloseSeries"
                }
            ],
            "expected": "NumericIndicator whose delegate returns Num.NaN for bars where the close price is undefined; getValue(1).isNaN() == true",
            "description": "Test behavior when a bar’s close price is NaN (e.g., missing data); the indicator should propagate the NaN value without throwing."
        }
    ]
}