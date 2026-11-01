{
    "groups": [
        {
            "group": "NumericIndicator.max(Indicator<Num>) - same instance",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "the same NumericIndicator instance (this)"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0)=10, getValue(1)=-20, getValue(2)=30 (identical to the original indicator)",
                    "description": "Calling max with the same instance should return an indicator that yields exactly the original values for every index."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Indicator<Num>) - commutativity",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator of Num values [10, 10, 30]"
                        }
                    ],
                    "expected": "Two NumericIndicators (a.max(b) and b.max(a)) both return values [10, 15, 30] for indices 0‑2, proving the operation is commutative.",
                    "description": "Validate that max(a, b) yields the same result as max(b, a) for a=[5,15,25] and b=[10,10,30]."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Indicator<Num>) - NaN in this indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator of Num values [1, 2, 4]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0)=NaN, getValue(1)=2, getValue(2)=4 (NaN propagates, other values selected when larger)",
                    "description": "When the left‑hand indicator contains NaN at an index, the max operation should propagate NaN according to Num semantics."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Indicator<Num>) - infinities",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator of Num values [100, POSITIVE_INFINITY, NEGATIVE_INFINITY]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0)=POSITIVE_INFINITY, getValue(1)=POSITIVE_INFINITY, getValue(2)=-5 (the larger of -5 and NEGATIVE_INFINITY)",
                    "description": "Test handling of POSITIVE_INFINITY and NEGATIVE_INFINITY values on both sides of the max operation."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Indicator<Num>) - negative numbers",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator of Num values [-20, -3, -1]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0)=-10, getValue(1)=-3, getValue(2)=0 (the greater (less negative) value per index)",
                    "description": "Both indicators contain only negative numbers; max should return the numerically larger (i.e., closer to zero) value for each index."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Indicator<Num>) - mixed NaN and infinities",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator of Num values [POSITIVE_INFINITY, 0, NaN]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0)=NaN, getValue(1)=0, getValue(2)=NaN (NaN propagates, POSITIVE_INFINITY wins over NEGATIVE_INFINITY, 0 wins over NEGATIVE_INFINITY)",
                    "description": "Combine special values (NaN, POSITIVE_INFINITY, NEGATIVE_INFINITY) on both sides to verify the max operation follows Num's defined ordering."
                }
            ]
        },
        {
            "group": "NumericIndicator.max(Indicator<Num>) - derived indicator (SMA)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "SMAIndicator built on the same series with barCount=2"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0)=15, getValue(1)=25, getValue(2)=35 (max between constant [10,20,30] and SMA [15,25,35])",
                    "description": "Use a non‑constant derived indicator (Simple Moving Average) to ensure max works correctly with lazy‑evaluated indicators."
                }
            ]
        }
    ]
}