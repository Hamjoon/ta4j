{
    "group": "NumericIndicator.dividedBy(Number) - special floating values",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NaN"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Pass NaN as a Double. The Num implementation should reject non‑finite numbers and throw IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.POSITIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Pass positive infinity as a Double. The method should detect the non‑finite value and throw IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NEGATIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Pass negative infinity as a Double. The method should reject the value and throw IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Float.NaN"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Pass NaN as a Float. The underlying Num conversion should fail and raise IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Float.POSITIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Pass positive infinity as a Float. The method should throw IllegalArgumentException due to non‑finite input."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Float.NEGATIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Pass negative infinity as a Float. The method should throw IllegalArgumentException because the number is not finite."
        }
    ]
}