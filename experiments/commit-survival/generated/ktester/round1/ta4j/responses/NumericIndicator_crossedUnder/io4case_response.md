{
    "group": "crossedUnder_additional",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new Indicator<Num>() {\n    @Override public Num getValue(int index) { return Num.valueOf(new int[]{8,7,5}[index]); }\n    @Override public BarSeries getBarSeries() { return series; }\n    @Override public int getUnstableBars() { return 0; }\n})"
                }
            ],
            "expected": "Rule.isSatisfied(1) == true (this indicator crosses under at index 1: previous 10 > 8, current 6 < 7)",
            "description": "this indicator values are [10,6,4]; other indicator values are [8,7,5]. At index 1 the value moves from above (10 > 8) to below (6 < 7) → crossed under, rule should be satisfied."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new Indicator<Num>() {\n    @Override public Num getValue(int index) { return Num.valueOf(new int[]{9,7,5}[index]); }\n    @Override public BarSeries getBarSeries() { return series; }\n    @Override public int getUnstableBars() { return 0; }\n})"
                }
            ],
            "expected": "Rule.isSatisfied(1) == false (no crossing, this stays above other)",
            "description": "this indicator values are [10,8,6]; other indicator values are [9,7,5]. The series stays above the other at every step, so no crossed‑under event occurs."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(5)))"
                }
            ],
            "expected": "Rule.isSatisfied(2) == false (previous value equal to other, not a strict crossing)",
            "description": "this indicator values are [5,5,4]; other constant 5. At index 2 the value goes from equal (5) to below (4); a crossed‑under requires previous > other, therefore the rule must be false."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(-2)))"
                }
            ],
            "expected": "Rule.isSatisfied(1) == true (crossed under at index 1 with negative numbers)",
            "description": "this indicator values are [-1,-3,-5]; other constant -2. At index 1 the value moves from -1 > -2 to -3 < -2, which is a valid crossed‑under."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(Num.valueOf(5)))"
                }
            ],
            "expected": "rule.getClass().getSimpleName() == \"CrossedDownIndicatorRule\"",
            "description": "Verify that the object returned by crossedUnder is an instance of CrossedDownIndicatorRule."
        }
    ]
}