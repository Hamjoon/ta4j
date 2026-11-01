{
    "group": "NumericIndicator.isLessThan - basic comparisons",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(5)"
                }
            ],
            "expected": "Rule.isSatisfied(0) == true",
            "description": "The NumericIndicator under test wraps a ConstantIndicator(3). Since 3 < 5, the UnderIndicatorRule returned by isLessThan should be satisfied at index 0."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(3)"
                }
            ],
            "expected": "Rule.isSatisfied(0) == false",
            "description": "Both the NumericIndicator under test and the other indicator return the same value (3). UnderIndicatorRule uses a strict < comparison, therefore the rule must not be satisfied."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(2)"
                }
            ],
            "expected": "Rule.isSatisfied(0) == false",
            "description": "The NumericIndicator under test returns 3 while the other indicator returns 2. Because 3 is not less than 2, the rule must be unsatisfied."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(5)"
                }
            ],
            "expected": "Rule.isSatisfied(1) == true",
            "description": "Using a multi‑index series: the NumericIndicator returns values [3,4] (index 0 → 3, index 1 → 4) and the other indicator returns 5 for every index. At index 1, 4 < 5, so the rule must be satisfied."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(4)"
                }
            ],
            "expected": "Rule.isSatisfied(1) == false",
            "description": "At index 1 the NumericIndicator returns 4 and the other indicator also returns 4. Equality does not satisfy UnderIndicatorRule, therefore the rule must be unsatisfied."
        }
    ]
}