{
    "group": "NumericIndicator.multipliedBy(Number) - additional scenarios",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "1"
                }
            ],
            "expected": "NumericIndicator where each value equals the original delegate value (identity multiplication).",
            "description": "Identity case: multiplying by 1 should return an indicator whose values are exactly the same as the delegate."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-1"
                }
            ],
            "expected": "NumericIndicator where each value equals the negated original delegate value.",
            "description": "Negation case: multiplying by -1 should invert the sign of every value produced by the delegate."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Long.MAX_VALUE"
                }
            ],
            "expected": "NumericIndicator where each value equals original delegate value multiplied by 9,223,372,036,854,775,807 (Long.MAX_VALUE).",
            "description": "Large positive integer: verify that the method correctly handles multiplication with the maximum long value without overflow in the underlying Num implementation."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new java.math.BigDecimal(\"0.0000001\")"
                }
            ],
            "expected": "NumericIndicator where each value equals original delegate value multiplied by 0.0000001.",
            "description": "Small fractional constant using BigDecimal to ensure high‑precision handling of tiny multipliers."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NaN"
                }
            ],
            "expected": "NumericIndicator where each value is Num.NaN (propagation of NaN).",
            "description": "Special floating‑point value NaN: the constant indicator should represent NaN, and multiplication should propagate NaN for every index."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.POSITIVE_INFINITY"
                }
            ],
            "expected": "NumericIndicator where each value is Num.POSITIVE_INFINITY (or an overflow representation defined by Num).",
            "description": "Infinity constant: verify that multiplying by positive infinity yields infinite values according to the Num implementation."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new java.math.BigInteger(\"123456789012345678901234567890\")"
                }
            ],
            "expected": "NumericIndicator where each value equals original delegate value multiplied by the very large BigInteger constant.",
            "description": "Very large integer via BigInteger to test that the method accepts arbitrary‑precision Number subclasses and correctly creates a constant Num."
        }
    ]
}