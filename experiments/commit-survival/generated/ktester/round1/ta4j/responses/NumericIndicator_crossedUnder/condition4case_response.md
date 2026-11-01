{
    "group": "crossedUnder",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(5)))"
                }
            ],
            "expected": "Rule.isSatisfied(2) == true (this indicator crosses under at index 2)",
            "description": "this indicator values: [10, 8, 4]; other constant 5. At index 2 the value goes from 8 (above) to 4 (below) → crossed under, rule should be satisfied."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(5)))"
                }
            ],
            "expected": "Rule.isSatisfied(1) == false (no crossing at index 1)",
            "description": "this indicator values: [10, 8, 6]; other constant 5. Values stay above other, never cross under → rule must be false."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(5)))"
                }
            ],
            "expected": "Rule.isSatisfied(2) == false (equal values do not count as crossing)",
            "description": "this indicator values: [7, 5, 5]; other constant 5. At index 2 the value equals other, not a strict crossing under → rule should be false."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException thrown",
            "description": "Passing a null indicator should cause the method to throw NPE because CrossedDownIndicatorRule constructor expects non‑null arguments."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(5)))"
                }
            ],
            "expected": "Rule.isSatisfied(0) == false (first index cannot have a previous value)",
            "description": "CrossedDownIndicatorRule requires a previous value to detect a crossing. At index 0 there is no previous bar, so rule must be false even if current value is below other."
        }
    ]
}