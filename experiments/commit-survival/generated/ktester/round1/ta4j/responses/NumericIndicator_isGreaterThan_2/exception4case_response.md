{
    "group": "NumericIndicator.isGreaterThan(Number)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Infinity"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value Double.POSITIVE_INFINITY",
            "description": "Positive infinity: ensures that Double.POSITIVE_INFINITY is accepted and wrapped without throwing."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Float.NaN"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value Float.NaN",
            "description": "Float NaN: verifies that a Float NaN constant is handled similarly to Double.NaN and does not raise an exception."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Float.POSITIVE_INFINITY"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value Float.POSITIVE_INFINITY",
            "description": "Float positive infinity: checks that Float.POSITIVE_INFINITY is correctly wrapped into a ConstantIndicator."
        }
    ]
}