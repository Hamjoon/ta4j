{
    "group": "NumericIndicator.isLessThan - mismatched BarSeries handling",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(5) built from a different BarSeries than the NumericIndicator under test"
                }
            ],
            "expected": "throws IllegalArgumentException",
            "description": "UnderIndicatorRule requires both indicators to be based on the same BarSeries. Supplying an Indicator that references a different BarSeries should cause the constructor to throw an IllegalArgumentException."
        }
    ]
}