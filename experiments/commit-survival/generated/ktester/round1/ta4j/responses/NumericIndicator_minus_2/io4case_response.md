{
    "testCases": [
        {
            "group": "minus(Number) with Short constant",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 7
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) returns 8 (original delegate value 15 minus 7) and getValue(1) returns 13 (original delegate value 20 minus 7)",
                    "description": "Validate that a Short (auto‑boxed from int literal) is correctly turned into a constant indicator and subtracted from a delegate that has different values at multiple indices."
                }
            ]
        },
        {
            "group": "minus(Number) with Byte constant (negative value)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": -2
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) returns -3 (original delegate value -5 minus -2)",
                    "description": "Check handling of a negative Byte constant; the subtraction should effectively add the absolute value."
                }
            ]
        },
        {
            "group": "minus(Number) with BigInteger constant",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "12345678901234567890"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) returns -12345678901234567890 (original delegate value 0 minus the BigInteger constant)",
                    "description": "Ensure that a BigInteger (passed as a Number via its toString representation) is accepted, converted to a Num, and subtracted without overflow."
                }
            ]
        },
        {
            "group": "minus(Number) with Double.NaN constant",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "NaN"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) is NaN",
                    "description": "When the constant is Double.NaN the resulting Num should also be NaN, propagating the undefined value."
                }
            ]
        },
        {
            "group": "minus(Number) with Double.POSITIVE_INFINITY constant",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "Infinity"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) is -Infinity (original delegate value finite minus +Infinity)",
                    "description": "Test subtraction of a positive infinite constant; the result must be negative infinite according to IEEE‑754 rules."
                }
            ]
        },
        {
            "group": "minus(Number) with custom Number subclass (AtomicInteger)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "AtomicInteger(10)"
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(0) returns 20 (original delegate value 30 minus 10)",
                    "description": "Verify that any subclass of java.lang.Number (e.g., java.util.concurrent.atomic.AtomicInteger) is correctly handled by createConstant."
                }
            ]
        },
        {
            "group": "minus(Number) chaining with plus(Number)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 5
                        }
                    ],
                    "expected": "NumericIndicator (result of minus(5).plus(3)) whose getValue(0) returns 48 (original delegate value 50 minus 5 plus 3)",
                    "description": "Confirm that minus(Number) returns a NumericIndicator that can be further chained; the intermediate result should be correct."
                }
            ]
        },
        {
            "group": "minus(Number) on derived SMA indicator",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": 2.5
                        }
                    ],
                    "expected": "NumericIndicator whose getValue(2) equals SMA(closePrice, 3) at index 2 minus 2.5",
                    "description": "Apply minus(Number) to a non‑constant delegate (SMA of close prices). Verify subtraction works for a non‑trivial index (e.g., 2) where SMA has already performed averaging."
                }
            ]
        }
    ]
}