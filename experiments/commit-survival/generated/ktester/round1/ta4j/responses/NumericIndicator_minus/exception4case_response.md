{
    "test_cases": [
        {
            "group": "minus_nullOther",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "null"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Calling minus(null) should throw NullPointerException because the method does not accept a null Indicator."
                }
            ]
        },
        {
            "group": "minus_differentBarSeries",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ClosePriceIndicator(seriesB)"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Subtracting an indicator that belongs to a different BarSeries than the current NumericIndicator should raise IllegalArgumentException (series mismatch)."
                }
            ]
        },
        {
            "group": "minus_indicatorReturningNull",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "CustomIndicatorReturningNull()"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "If the supplied Indicator returns null for a value at a given index, the resulting NumericIndicator will attempt to perform a subtraction on a null Num, leading to NullPointerException when the value is accessed."
                }
            ]
        }
    ]
}