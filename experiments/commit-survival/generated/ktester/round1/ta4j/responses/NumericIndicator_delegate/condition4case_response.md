{
    "group": "NumericIndicator.delegate()",
    "cases": [
        {
            "input": [
                {
                    "parameter": "delegate",
                    "value": "MockIndicator (returns Num value 10 at any index)"
                }
            ],
            "expected": "The same MockIndicator instance is returned by delegate()",
            "description": "Verify that a NumericIndicator created via NumericIndicator.of(delegate) returns the exact delegate object supplied to the factory method."
        },
        {
            "input": [
                {
                    "parameter": "BarSeries",
                    "value": "BarSeries containing 5 dummy bars (close prices 1..5)"
                }
            ],
            "expected": "Instance of ClosePriceIndicator is returned by delegate()",
            "description": "When a NumericIndicator is created with NumericIndicator.closePrice(barSeries), its delegate should be a ClosePriceIndicator that wraps the same BarSeries."
        },
        {
            "input": [
                {
                    "parameter": "BarSeries",
                    "value": "BarSeries containing 3 dummy bars (volume values 100,200,300)"
                }
            ],
            "expected": "Instance of VolumeIndicator is returned by delegate()",
            "description": "When a NumericIndicator is created with NumericIndicator.volume(barSeries), its delegate should be a VolumeIndicator that wraps the same BarSeries."
        },
        {
            "input": [
                {
                    "parameter": "delegate",
                    "value": "null"
                }
            ],
            "expected": "null is returned by delegate() (or a NullPointerException is thrown if the factory validates input)",
            "description": "Edge‑case: creating a NumericIndicator with a null delegate (if the library permits) should cause delegate() to return null; if the factory method guards against null, the test expects the corresponding exception."
        }
    ]
}