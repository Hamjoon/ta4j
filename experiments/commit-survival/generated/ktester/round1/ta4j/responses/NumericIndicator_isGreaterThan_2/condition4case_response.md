{
    "group": "NumericIndicator.isGreaterThan(Number)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 10
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 10",
            "description": "Standard case: a positive integer should be wrapped into a ConstantIndicator and an OverIndicatorRule should be returned."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 0
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 0",
            "description": "Edge case with zero: verifies that zero is handled correctly and not treated as a special value."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -5
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value -5",
            "description": "Negative number: ensures that negative constants are accepted and the rule is still created."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 3.14159
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 3.14159",
            "description": "Floating‑point number (Double): checks that non‑integer Number implementations are correctly converted."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": null
                }
            ],
            "expected": "IllegalArgumentException (or NullPointerException) indicating that the number argument must not be null",
            "description": "Null argument: validates that the method guards against null and throws the appropriate exception."
        }
    ]
}