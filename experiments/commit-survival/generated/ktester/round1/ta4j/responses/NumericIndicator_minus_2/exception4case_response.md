{
    "test_cases": [
        {
            "group": "minus(Number) with Number subclass that throws RuntimeException in doubleValue",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "BadNumber(throws RuntimeException on doubleValue())"
                        }
                    ],
                    "expected": "RuntimeException",
                    "description": "A custom Number implementation whose doubleValue() method throws a RuntimeException should cause minus(Number) to propagate that exception when createConstant(n) attempts to read the numeric value."
                }
            ]
        },
        {
            "group": "minus(Number) with excessively large BigDecimal causing overflow",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "n",
                            "value": "new BigDecimal(\"1E+10000\")"
                        }
                    ],
                    "expected": "ArithmeticException",
                    "description": "When a BigDecimal with a magnitude far beyond the range supported by the underlying Num implementation is supplied, the conversion inside createConstant should fail and throw an ArithmeticException (or a similar overflow‑related exception)."
                }
            ]
        }
    ]
}