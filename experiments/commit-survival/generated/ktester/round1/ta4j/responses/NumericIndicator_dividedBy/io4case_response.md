{
    "testCases": [
        {
            "group": "Division by zero indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(0)"
                        }
                    ],
                    "expected": "ArithmeticException thrown when getValue(i) is evaluated",
                    "description": "Dividing a NumericIndicator wrapping constant 10 by a constant indicator 0 should raise an ArithmeticException (division by zero)."
                }
            ]
        },
        {
            "group": "Numerator zero",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(5)"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == 0 for any index i (0 / 5)",
                    "description": "When the numerator indicator wraps constant 0, division by any non‑zero constant must yield 0 for all indices."
                }
            ]
        },
        {
            "group": "Negative divisor",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(-2)"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == -5 for any index i (10 / -2)",
                    "description": "Verify that division correctly propagates the sign of a negative divisor."
                }
            ]
        },
        {
            "group": "Negative numerator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "ConstantIndicator(2)"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == -5 for any index i (-10 / 2)",
                    "description": "Dividing a negative constant numerator by a positive divisor should produce a negative result."
                }
            ]
        },
        {
            "group": "Varying divisor indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "IndexIndicator(start=1)" 
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(i) == 10 / (i + 1) for each index i",
                    "description": "Use an indicator that returns the index+1 as its value (e.g., 1,2,3,…). The quotient must be computed per index, confirming element‑wise division."
                }
            ]
        },
        {
            "group": "Mixed zero divisor (runtime exception on zero index)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "AlternatingZeroIndicator" 
                        }
                    ],
                    "expected": "ArithmeticException thrown when getValue(i) is called for an index where divisor == 0",
                    "description": "The divisor indicator returns 2 for even indices and 0 for odd indices. Accessing an odd index should raise an ArithmeticException, while even indices return 5 (10/2)."
                }
            ]
        },
        {
            "group": "Division by non‑numeric (null) indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "other",
                            "value": "null"
                        }
                    ],
                    "expected": "NullPointerException thrown at method entry",
                    "description": "Passing a null Indicator to dividedBy should result in an immediate NullPointerException."
                }
            ]
        }
    ]
}