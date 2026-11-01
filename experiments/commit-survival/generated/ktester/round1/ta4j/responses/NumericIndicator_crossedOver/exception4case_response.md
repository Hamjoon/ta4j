{
    "test_cases": [
        {
            "group": "crossedOver(Indicator<Num>) - other indicator with mismatched BarSeries",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(5) built on a different BarSeries than the NumericIndicator under test"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "The CrossedUpIndicatorRule requires both indicators to belong to the same BarSeries. Supplying an indicator that references a different series should cause the rule construction to throw an IllegalArgumentException."
                }
            ]
        },
        {
            "group": "crossedOver(Indicator<Num>) - other indicator throws on getValue",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ThrowingIndicator (custom Indicator whose getValue(int) throws RuntimeException(\"test\"))"
                        }
                    ],
                    "expected": "RuntimeException with message \"test\"",
                    "description": "When the rule evaluates isSatisfied(index), it calls getValue on the supplied indicator. If that indicator throws an exception, the same exception should propagate to the caller."
                }
            ]
        },
        {
            "group": "crossedOver(Indicator<Num>) - other indicator returns null Num",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "NullValueIndicator (custom Indicator that returns null for any index)"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "A Num value of null is not valid for numeric comparisons. The CrossedUpIndicatorRule should detect the null value during evaluation and throw an IllegalArgumentException."
                }
            ]
        }
    ]
}