{
    "group": "NumericIndicator.crossedOver(Number) - basic functionality and edge cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "5"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(2) == true (delegate crosses from <=5 to >5 at index 2)",
            "description": "Verify that crossedOver(Number) creates a CrossedUpIndicatorRule that becomes satisfied when the underlying NumericIndicator value crosses above the constant 5. The series is set up so the value is 4 at index 1 and 6 at index 2."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-3"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(3) == true (cross from <=-3 to >-3)",
            "description": "Test crossing over a negative constant. The series values are -4 at index 2 and -2 at index 3, causing the rule to be satisfied at index 3."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(1) == false, rule.isSatisfied(2) == true",
            "description": "Crossing over zero. The series is -1 at index 0, -0.5 at index 1, and 0.2 at index 2, so the rule should become satisfied only at index 2."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "1000"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(any index) == false",
            "description": "Constant far above any series value – the rule should never be satisfied because the indicator never crosses the constant."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null Number should result in a NullPointerException, as the method attempts to create a constant indicator from the argument."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NaN"
                }
            ],
            "expected": "IllegalArgumentException or Rule that is never satisfied",
            "description": "NaN is not a valid constant for comparison; the implementation should either reject it with an exception or produce a rule that never becomes satisfied."
        }
    ]
}