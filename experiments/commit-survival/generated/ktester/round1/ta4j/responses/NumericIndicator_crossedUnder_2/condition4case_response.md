{
    "group": "CrossedUnder(Number) – basic functionality",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; when applied to a NumericIndicator whose values are [12, 11, 9, 8] the rule is satisfied at index 2 (value 9 crossed under 10) and false at other indexes",
            "description": "Verify that crossedUnder(10) creates a CrossedDownIndicatorRule that becomes true when the delegate indicator moves from above 10 to below 10."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-5"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [-3, -4, -6, -7] the rule is satisfied at index 2 (value -6 crossed under -5)",
            "description": "Check behavior with a negative constant – the rule should trigger when the series drops from >‑5 to ≤‑5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [1, 0.5, -0.1, -0.5] the rule is satisfied at index 2 (value -0.1 crossed under 0)",
            "description": "Edge case where the constant is zero; crossing from positive to negative should satisfy the rule."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "15"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [13, 14, 15, 16] the rule is never satisfied because the series never goes from above 15 to below 15",
            "description": "Validate that the rule does NOT fire when there is no crossing (values stay below or equal to the constant)."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null Number should result in a NullPointerException (defensive programming)."
        }
    ]
}