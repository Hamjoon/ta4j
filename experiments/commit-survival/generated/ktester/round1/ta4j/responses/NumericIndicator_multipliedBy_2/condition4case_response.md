{
    "group": "NumericIndicator.multipliedBy(Number)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "5"
                }
            ],
            "expected": "NumericIndicator where each value equals original delegate value multiplied by 5",
            "description": "Standard case: multiply a positive integer constant. The returned indicator should produce values = delegate * 5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "NumericIndicator where each value equals Num.ZERO regardless of delegate values",
            "description": "Edge case: multiplication by zero should yield zero for any index."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-3.2"
                }
            ],
            "expected": "NumericIndicator where each value equals original delegate value multiplied by -3.2",
            "description": "Negative and non‑integer constant: verify that sign and decimal part are handled correctly."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Invalid input: passing null should cause the method (via createConstant) to throw a NullPointerException."
        }
    ]
}