{
    "group": "CrossedUnder(Number) – edge and special cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [11, 10, 9] the rule is satisfied only at index 2 (value 9) because the previous value (10) is equal to the constant, so the crossing is detected when the series moves from >10 to <=10.",
            "description": "Verify that a crossing from exactly the constant to a lower value still triggers the rule (previous > n, current <= n)."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [10, 9] the rule is NOT satisfied at index 1 because the previous value is not greater than the constant (it is equal).",
            "description": "Check that a transition from equal to below does NOT count as a crossing when the implementation requires previous > n."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with a single‑element series [12] the rule is never satisfied (no previous value to compare).",
            "description": "Edge case: series length of 1 – crossing cannot occur."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with an empty series the rule is never satisfied (no data points).",
            "description": "Edge case: empty series – rule should safely return false for all indexes."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "1000000000000"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [1000000000001, 999999999999] the rule is satisfied at index 1 (value 999999999999) because the series crossed under the large constant.",
            "description": "Validate behavior with very large numeric constants."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "NaN"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Passing Double.NaN should be rejected by createConstant and result in an IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Infinity"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [1e9, 2e9, 3e9] the rule is never satisfied because a value can never be greater than positive infinity, thus no crossing can occur.",
            "description": "Edge case with positive infinity – rule should never fire."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [12, 9, 11, 8, 10, 7] the rule is satisfied at indexes 1, 3 and 5 (values 9, 8, 7) where the series moves from >10 to <=10 multiple times.",
            "description": "Verify that the rule correctly identifies multiple crossings in the same series."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new java.math.BigDecimal(\"10\")"
                }
            ],
            "expected": "Rule instance of CrossedDownIndicatorRule; with delegate values [12, 11, 9] the rule is satisfied at index 2 (value 9) demonstrating that BigDecimal constants are handled correctly.",
            "description": "Confirm that createConstant correctly wraps non‑primitive Number types such as BigDecimal."
        }
    ]
}