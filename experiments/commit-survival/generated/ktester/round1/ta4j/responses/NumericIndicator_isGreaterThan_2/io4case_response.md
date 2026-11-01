{
    "group": "NumericIndicator.isGreaterThan(Number)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 9223372036854775807
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 9223372036854775807L",
            "description": "Long maximum value: verifies that a large Long is correctly wrapped into a ConstantIndicator and an OverIndicatorRule is returned."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2.5
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 2.5f",
            "description": "Float value (as Double in JSON): ensures that Float numbers are accepted and converted to the appropriate Num representation."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "12345.6789"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 12345.6789 (BigDecimal)",
            "description": "BigDecimal input given as string: checks that arbitrary‑precision decimal numbers are handled correctly."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "999999999999999999"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 999999999999999999 (BigInteger)",
            "description": "BigInteger input given as string: validates handling of very large integer values beyond primitive ranges."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 7
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value 7 (Short)",
            "description": "Short value (implicitly via JSON number): confirms that short literals are accepted."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -2
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value -2 (Byte)",
            "description": "Byte value (implicitly via JSON number): ensures that byte literals are processed correctly."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2147483647
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value Integer.MAX_VALUE",
            "description": "Integer.MAX_VALUE: tests the upper bound of 32‑bit int handling."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -2147483648
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value Integer.MIN_VALUE",
            "description": "Integer.MIN_VALUE: tests the lower bound of 32‑bit int handling."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 1.7976931348623157E308
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value Double.MAX_VALUE",
            "description": "Double.MAX_VALUE: verifies that the method can wrap the largest finite double."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "NaN"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value NaN",
            "description": "Double.NaN: checks that a Not‑a‑Number constant is accepted and encapsulated in a ConstantIndicator."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-Infinity"
                }
            ],
            "expected": "Rule instance of OverIndicatorRule whose constant indicator has value -Infinity",
            "description": "Double.NEGATIVE_INFINITY: ensures that infinite values are handled without throwing."
        }
    ]
}