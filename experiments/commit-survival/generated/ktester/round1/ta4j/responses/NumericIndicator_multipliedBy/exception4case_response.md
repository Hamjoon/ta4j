{
    "new_test_cases": [
        {
            "group": "Different BarSeries handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "ClosePriceIndicator(seriesA) where seriesA values are [1, 2, 3]"
                        },
                        {
                            "parameter": "other",
                            "value": "ClosePriceIndicator(seriesB) where seriesB values are [10, 20, 30]"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "When the two indicators belong to different BarSeries instances, multipliedBy must reject the operation and throw an IllegalArgumentException."
                }
            ]
        },
        {
            "group": "Null delegate indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(null)"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(5))"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Creating a NumericIndicator with a null delegate and then calling multipliedBy should result in a NullPointerException because the internal delegate is accessed during the multiplication."
                }
            ]
        },
        {
            "group": "Indicator returning null values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(4)))"
                        },
                        {
                            "parameter": "other",
                            "value": "CustomIndicatorReturningNull() where getValue(i) returns null"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "If the supplied indicator returns null for a bar value, the product operation will attempt to multiply a Num with null, leading to a NullPointerException when getValue is invoked on the resulting indicator."
                }
            ]
        }
    ]
}