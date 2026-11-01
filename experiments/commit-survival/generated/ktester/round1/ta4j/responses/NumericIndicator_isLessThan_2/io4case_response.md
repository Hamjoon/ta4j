{
    "group": "NumericIndicator.isLessThan(Number) - additional cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Long.MAX_VALUE"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for every index where the underlying NumericIndicator value is a finite number (i.e., all typical values), because no realistic value can be greater than or equal to Long.MAX_VALUE.",
            "description": "Very large positive constant (Long.MAX_VALUE) – verifies that the rule works correctly when the threshold is far beyond any possible indicator value."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.MIN_VALUE"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for indices whose value is negative or exactly zero, and false for any positive value ≥ Double.MIN_VALUE.",
            "description": "Smallest positive non‑zero double – tests the strict‑less‑than semantics around the zero boundary."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Float.valueOf(5.5f)"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true when the underlying value < 5.5f, false otherwise.",
            "description": "Float constant – ensures that the method correctly handles Float Number implementations."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Short.valueOf((short)2)"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for values < 2 (as a short), false otherwise.",
            "description": "Short constant – verifies handling of smaller integral Number types."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Byte.valueOf((byte)-1)"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for values < -1, false for -1 and greater.",
            "description": "Byte constant – tests the method with the smallest integral wrapper type."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NaN"
                }
            ],
            "expected": "Rule.isSatisfied(index) always returns false (the rule never satisfies) because any comparison with NaN is defined as false.",
            "description": "NaN constant – checks that the rule fails fast for an undefined numeric threshold."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.POSITIVE_INFINITY"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for every finite underlying value (including negative, zero, and positive numbers), and false only when the underlying value is NaN.",
            "description": "Positive infinity – validates that the rule treats infinity as an upper bound that all finite numbers are less than."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NEGATIVE_INFINITY"
                }
            ],
            "expected": "Rule.isSatisfied(index) always returns false because no numeric value (including -Infinity itself) is less than negative infinity.",
            "description": "Negative infinity – ensures the rule never satisfies when the threshold is the lowest possible double."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new java.math.BigInteger(\"12345678901234567890\")"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for underlying values < 12345678901234567890, false otherwise.",
            "description": "BigInteger constant – tests compatibility with another arbitrary‑precision Number subclass."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0.0"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true when the underlying value is negative, false when it is zero or positive.",
            "description": "Zero as a Double – confirms that the method behaves identically for zero expressed as a floating‑point Number."
        }
    ]
}