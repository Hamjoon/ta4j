{
    "group": "minus_withConstantIndicator",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(5)"
                }
            ],
            "expected": "[5, 15, 25]",
            "description": "Subtract a constant indicator (value 5) from the close‑price indicator of a series with close prices [10,20,30]. The resulting indicator should yield [10‑5, 20‑5, 30‑5] = [5,15,25]."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(-3)"
                }
            ],
            "expected": "[13, 23, 33]",
            "description": "Subtract a negative constant (-3) from the same close‑price indicator. Expected values are [10‑(-3), 20‑(-3), 30‑(-3)] = [13,23,33]."
        }
    ]
}