{
    "testCases": [
        {
            "group": "Basic multiplication with constant indicators",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(3)))"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(2))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == DecimalNum.valueOf(6) for any i",
                    "description": "Multiplying two constant indicators (3 * 2) should yield a constant indicator with value 6."
                },
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(0)))"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(5))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == DecimalNum.valueOf(0) for any i",
                    "description": "Multiplying by zero must always produce zero, regardless of the other indicator."
                },
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(-4)))"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(3))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == DecimalNum.valueOf(-12) for any i",
                    "description": "Multiplication with a negative constant should preserve the sign (‑4 * 3 = ‑12)."
                }
            ]
        },
        {
            "group": "Multiplication with a varying indicator (ClosePriceIndicator)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(5)))"
                        },
                        {
                            "parameter": "other",
                            "value": "ClosePriceIndicator(series) where series values are [1, 2, 3]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) == 5, getValue(1) == 10, getValue(2) == 15",
                    "description": "When the left operand is a constant (5) and the right operand varies (1,2,3), the product must be (5,10,15)."
                },
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "ClosePriceIndicator(series) where series values are [2, 4, 6]"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(3))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) == 6, getValue(1) == 12, getValue(2) == 18",
                    "description": "Multiplying a varying indicator (2,4,6) by a constant (3) should produce (6,12,18)."
                }
            ]
        },
        {
            "group": "Null argument handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(1)))"
                        },
                        {
                            "parameter": "other",
                            "value": "null"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Calling multipliedBy with a null indicator must throw a NullPointerException."
                }
            ]
        },
        {
            "group": "Identity multiplication (self * self)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(7)))"
                        },
                        {
                            "parameter": "other",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(7)))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == DecimalNum.valueOf(49) for any i",
                    "description": "Multiplying an indicator by itself should square its values (7 * 7 = 49)."
                }
            ]
        }
    ]
}