{
    "group": "NumericIndicator.closePrice(BarSeries) tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "validBarSeries"
                }
            ],
            "expected": "NumericIndicator whose delegate returns the close price of each bar; getValue(0) == 100.5, getValue(1) == 101.2",
            "description": "Verify that closePrice creates a NumericIndicator that correctly wraps a ClosePriceIndicator for a typical BarSeries containing two bars with known close prices."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "singleBarSeries"
                }
            ],
            "expected": "NumericIndicator whose delegate returns the only close price; getValue(0) == 55.0",
            "description": "Check behavior when the BarSeries contains exactly one bar – the returned NumericIndicator should still provide the correct close price."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "emptyBarSeries"
                }
            ],
            "expected": "IndexOutOfBoundsException when calling getValue(0) on the returned NumericIndicator",
            "description": "An empty BarSeries has no bars; the NumericIndicator should be created but any attempt to access a value should raise an IndexOutOfBoundsException."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null BarSeries should cause a NullPointerException because ClosePriceIndicator requires a non‑null series."
        }
    ]
}