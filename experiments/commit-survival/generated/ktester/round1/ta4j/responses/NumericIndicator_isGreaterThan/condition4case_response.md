{
    "group": "NumericIndicator.isGreaterThan(Indicator<Num>) - valid inputs",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(5)"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule where rule.isSatisfied(0) == true",
            "description": "Create a NumericIndicator wrapping a ConstantIndicator(10) and compare it with another ConstantIndicator(5). The returned Rule should be an OverIndicatorRule and evaluate to true because 10 > 5 at index 0."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(15)"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule where rule.isSatisfied(0) == false",
            "description": "Create a NumericIndicator wrapping a ConstantIndicator(10) and compare it with ConstantIndicator(15). The returned Rule should be an OverIndicatorRule and evaluate to false because 10 is not greater than 15 at index 0."
        }
    ]
}