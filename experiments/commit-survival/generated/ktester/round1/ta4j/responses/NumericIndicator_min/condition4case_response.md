{
    "group": "NumericIndicator.min(Indicator<Num>) basic functionality",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(Num.valueOf(10))"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals min(originalIndicator.getValue(i), 10) for all i",
            "description": "The 'other' indicator provides a constant value higher than the original indicator values; the resulting indicator should return the original values unchanged."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(Num.valueOf(-5))"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals -5 for all i (since -5 is lower than any original value)",
            "description": "The 'other' indicator provides a constant value lower than the original indicator values; the resulting indicator should always return the constant lower value."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "originalIndicator (same instance as the caller)"
                }
            ],
            "expected": "NumericIndicator that is effectively a copy of the original (min(x, x) = x) and returns the same values as the original indicator",
            "description": "Passing the same indicator instance as 'other' should result in an indicator that yields the same values as the original (no change)."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(Num.valueOf(0))"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals min(originalIndicator.getValue(i), 0) for each index i",
            "description": "Mixed values: some original values are positive, some negative. The resulting indicator should return the smaller of the two at each index."
        }
    ]
}