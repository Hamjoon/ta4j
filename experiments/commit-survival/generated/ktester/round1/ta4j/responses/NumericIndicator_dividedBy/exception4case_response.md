{
    "test_cases": [
        {
            "group": "Divisor returns null Num",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "NullReturningIndicator()"
                        }
                    ],
                    "expected": "NullPointerException thrown when getValue(i) is evaluated",
                    "description": "The divisor indicator returns null for its Num value. When the resulting NumericIndicator evaluates getValue(i), it attempts to divide by a null Num, which should raise a NullPointerException."
                }
            ]
        },
        {
            "group": "Divisor shorter series (IndexOutOfBounds)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(2) based on BarSeries of length 5"
                        }
                    ],
                    "expected": "IndexOutOfBoundsException thrown when getValue(i) is called for i >= 5",
                    "description": "The numerator NumericIndicator is built on a BarSeries with 10 bars, while the divisor indicator is built on a different BarSeries that only has 5 bars. Accessing an index beyond the divisor's size should result in an IndexOutOfBoundsException."
                }
            ]
        },
        {
            "group": "Divisor throws custom exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ExceptionThrowingIndicator()"
                        }
                    ],
                    "expected": "IllegalStateException propagated from divisor indicator",
                    "description": "The divisor indicator deliberately throws an IllegalStateException from its getValue method. The exception should propagate through the division operation unchanged."
                }
            ]
        },
        {
            "group": "Numerator with null delegate",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(2)"
                        }
                    ],
                    "expected": "NullPointerException thrown when getValue(i) is evaluated",
                    "description": "A NumericIndicator created via NumericIndicator.of(null) has a null internal delegate. Dividing this indicator by a valid divisor should cause a NullPointerException when the resulting indicator attempts to read the numerator's value."
                }
            ]
        }
    ]
}