{
    "group": "NumericIndicator.max(Indicator<Num>)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator of Num values [5, 5, 5]"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) returns 5 for i=0,1,2 (same as both operands)",
            "description": "Both this and other indicators contain identical constant values; the max operation should return the same constant value for every index."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator of Num values [1, 2, 3]"
                }
            ],
            "expected": "NumericIndicator whose getValue(0)=10, getValue(1)=20, getValue(2)=30 (this indicator values are [10,20,30])",
            "description": "The 'this' indicator has larger values than the 'other' indicator at all indices; max should return the values of 'this' indicator."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator of Num values [100, 200, 300]"
                }
            ],
            "expected": "NumericIndicator whose getValue(0)=100, getValue(1)=200, getValue(2)=300 (other indicator dominates)",
            "description": "The 'other' indicator has larger values than 'this' indicator at all indices; max should return the values of the 'other' indicator."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator of Num values [5, 25, 15]"
                }
            ],
            "expected": "NumericIndicator whose getValue(0)=10, getValue(1)=25, getValue(2)=30 (mixed dominance)",
            "description": "Mixed scenario where at index 0 and 2 'this' indicator is larger, but at index 1 'other' indicator is larger; max should pick the larger value per index."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null Indicator should result in a NullPointerException (as the method forwards the argument to BinaryOperation.max which expects a non‑null argument)."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(ConstantIndicator of Num values [Num.NaN, 0, 5])"
                }
            ],
            "expected": "NumericIndicator whose getValue(0)=NaN (propagated), getValue(1)=0, getValue(2)=5 (max with NaN follows Num semantics)",
            "description": "Validate that the max operation respects the Num implementation's handling of special values such as NaN."
        }
    ]
}