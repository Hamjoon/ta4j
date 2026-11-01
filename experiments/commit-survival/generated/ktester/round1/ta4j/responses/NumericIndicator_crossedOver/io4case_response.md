{
    "testCases": [
        {
            "group": "crossedOver(Indicator<Num>) - multiple upward crossings",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(4)"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(1) == true",
                    "description": "NumericIndicator yields values [3, 5, 2, 6] (e.g., via a custom Indicator). The series crosses upward over 4 at index 1 (3→5) and again at index 3 (2→6). The rule must report a satisfied crossing at index 1."
                },
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(4)"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(2) == false",
                    "description": "Using the same series as above, at index 2 the NumericIndicator moves from 5 (above 4) to 2 (below 4); no upward crossing occurs, so the rule must be unsatisfied."
                },
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(4)"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(3) == true",
                    "description": "Continuing the same series, at index 3 the value rises from 2 (below 4) to 6 (above 4); the rule must be satisfied again."
                }
            ]
        },
        {
            "group": "crossedOver(Indicator<Num>) - other indicator decreasing while this stays constant",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "CustomIndicator(values=[6,5,4])"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(0) == false",
                    "description": "NumericIndicator is a constant 5 (via ConstantIndicator). The other indicator decreases from 6 to 4, never falling below 5, therefore no upward crossing can happen at any index."
                },
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "CustomIndicator(values=[6,5,4])"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(2) == false",
                    "description": "Even at the last index (2) where the other value is 4 (below 5), the crossing condition requires the NumericIndicator to move from below to above the other; since NumericIndicator never moves, the rule stays unsatisfied."
                }
            ]
        },
        {
            "group": "crossedOver(Indicator<Num>) - other indicator contains NaN (invalid numeric value)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(NaN)"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Passing an indicator that returns Num.NaN should cause the CrossedUpIndicatorRule constructor (or its evaluation) to reject the input and throw an IllegalArgumentException."
                }
            ]
        },
        {
            "group": "crossedOver(Indicator<Num>) - other indicator with same delegate but distinct instance",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "NumericIndicator.of(originalDelegate)"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(any index) == false",
                    "description": "Both NumericIndicator instances wrap the same underlying delegate (e.g., ClosePriceIndicator). Since they produce identical values at every index, no upward crossing can occur; the rule must always be unsatisfied."
                }
            ]
        },
        {
            "group": "crossedOver(Indicator<Num>) - other indicator lagging by one bar (offset series)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ShiftedIndicator(shift=1, base=ConstantIndicator(2))"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(0) == true",
                    "description": "NumericIndicator yields values [1,2,3] (via ConstantIndicator(1) then plus 1 each bar). The other indicator is the same series shifted right by one bar, so at index 0 the other returns null/undefined (treated as below). The NumericIndicator moves from undefined to 1, which is considered an upward crossing, so the rule should be satisfied at index 0."
                },
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ShiftedIndicator(shift=1, base=ConstantIndicator(2))"
                        }
                    ],
                    "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(1) == false",
                    "description": "At index 1 both series have the same value (2). Since there is no transition from below to above, the rule must be unsatisfied."
                }
            ]
        }
    ]
}