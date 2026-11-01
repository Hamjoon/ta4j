{
    "group": "NumericIndicator.crossedOver(Number) - additional edge and type cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-1000"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(any index) == false (indicator values are always > -1000, so no crossing from <= to > can occur)",
            "description": "Constant far below the minimum series value. The underlying NumericIndicator never goes from <= -1000 to > -1000, therefore the rule must never be satisfied."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "3.5"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(2) == true, rule.isSatisfied(1) == false",
            "description": "Series values: 3.5 at index 0, 3.4 at index 1, 3.6 at index 2. The constant equals the first value; the rule should become satisfied only when the value crosses from ≤ 3.5 (index 1) to > 3.5 (index 2)."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.POSITIVE_INFINITY"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(any index) == false (a finite indicator can never cross above positive infinity)",
            "description": "Using Double.POSITIVE_INFINITY as the constant. No crossing can ever happen, so the rule must always be unsatisfied."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NEGATIVE_INFINITY"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(any index) == false (the indicator is always > -∞, thus never crosses from <= to >)",
            "description": "Using Double.NEGATIVE_INFINITY as the constant. Since every numeric value is greater than -∞, there is no crossing event."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "2147483647"  }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(any index) == false (series values stay well below Integer.MAX_VALUE)",
            "description": "Constant set to Integer.MAX_VALUE. The series values are far lower, so the rule never becomes satisfied."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(1) == true, rule.isSatisfied(2) == false, rule.isSatisfied(3) == true (multiple up‑crossings)",
            "description": "Series values: -1 at index 0, 0.1 at index 1 (first up‑cross), -0.2 at index 2, 0.5 at index 3 (second up‑cross). The rule should be satisfied only at the indices where the crossing from ≤ 0 to > 0 occurs."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "5L"
                }
            ],
            "expected": "Rule instance of CrossedUpIndicatorRule; rule.isSatisfied(1) == true",
            "description": "Constant supplied as a Long (5L). Series values: 4 at index 0, 6 at index 1. Confirms that the method correctly handles non‑Double Number types."
        }
    ]
}