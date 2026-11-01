{
    "newTestCases": [
        {
            "group": "Multiplication of two varying indicators",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "ClosePriceIndicator(series) where series values are [1, 2, 3]"
                        },
                        {
                            "parameter": "other",
                            "value": "VolumeIndicator(series) where series values are [10, 20, 30]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) == DecimalNum.valueOf(10), getValue(1) == DecimalNum.valueOf(40), getValue(2) == DecimalNum.valueOf(90)",
                    "description": "Both operands vary per bar; product should be element‑wise multiplication of close price and volume series."
                }
            ]
        },
        {
            "group": "Multiplication with negative and zero values in varying indicators",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "ClosePriceIndicator(series) where series values are [-5, 0, 5]"
                        },
                        {
                            "parameter": "other",
                            "value": "ClosePriceIndicator(series) where series values are [2, -2, 0]"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) == DecimalNum.valueOf(-10), getValue(1) == DecimalNum.valueOf(0), getValue(2) == DecimalNum.valueOf(0)",
                    "description": "Both indicators contain negative and zero values; the product must reflect sign rules and zero propagation."
                }
            ]
        },
        {
            "group": "Multiplication involving derived indicators (HighestValueIndicator)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "HighestValueIndicator(series, 2) where series values are [1, 3, 2, 5]"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(2))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) == DecimalNum.valueOf(2), getValue(1) == DecimalNum.valueOf(6), getValue(2) == DecimalNum.valueOf(6), getValue(3) == DecimalNum.valueOf(10)",
                    "description": "Highest value over the last 2 bars yields [1,3,3,5]; multiplied by 2 the expected series is [2,6,6,10]."
                }
            ]
        },
        {
            "group": "Chained operations resulting indicator multiplication",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "NumericIndicator.of(ConstantIndicator(DecimalNum.valueOf(2))).plus(ConstantIndicator(DecimalNum.valueOf(3)))"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(4))"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == DecimalNum.valueOf(20) for any i",
                    "description": "((2 + 3) = 5) * 4 = 20; verifies that chaining of plus() followed by multipliedBy() yields the correct constant result."
                }
            ]
        },
        {
            "group": "Commutativity check between two indicators",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "ClosePriceIndicator(series) where series values are [1, 4]"
                        },
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(DecimalNum.valueOf(5))"
                        }
                    ],
                    "expected": "Both a.multipliedBy(b) and b.multipliedBy(a) produce NumericIndicator whose getValue(0) == DecimalNum.valueOf(5), getValue(1) == DecimalNum.valueOf(20)",
                    "description": "Multiplication should be commutative; swapping operands must not change the resulting series."
                }
            ]
        },
        {
            "group": "Mismatched series length handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "this",
                            "value": "ClosePriceIndicator(seriesA) where seriesA values are [1, 2]"
                        },
                        {
                            "parameter": "other",
                            "value": "ClosePriceIndicator(seriesB) where seriesB values are [10]"
                        }
                    ],
                    "expected": "IndexOutOfBoundsException when getValue(1) is called on the resulting indicator",
                    "description": "When the two underlying indicators have different bar counts, accessing an index that exists only in the longer series should raise an IndexOutOfBoundsException."
                }
            ]
        }
    ]
}