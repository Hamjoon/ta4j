{
    "group": "NumericIndicator.isLessThan(Number)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for any index where the underlying NumericIndicator value is less than 10, and false otherwise.",
            "description": "Standard case: a positive integer constant. The method should create a ConstantIndicator with value 10 and return an UnderIndicatorRule that correctly evaluates the '<' relationship."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true when the underlying NumericIndicator value is negative, false when it is zero or positive.",
            "description": "Boundary case with zero. Verifies that the rule treats zero as the threshold and respects the strict less‑than semantics."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-5"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true only when the underlying NumericIndicator value is less than -5 (i.e., more negative), false otherwise.",
            "description": "Negative constant: ensures that the method works correctly with negative numbers and does not mistakenly invert the comparison."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "3.14159"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for values < 3.14159, false for values ≥ 3.14159.",
            "description": "Floating‑point constant (Double). Checks that the method correctly handles non‑integral Number implementations."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException (or IllegalArgumentException) thrown by createConstant when the argument is null.",
            "description": "Null argument: verifies that the method validates its input and fails fast when a null Number is supplied."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new java.math.BigDecimal(\"100\")"
                }
            ],
            "expected": "Rule.isSatisfied(index) returns true for values < 100, false otherwise.",
            "description": "BigDecimal constant: tests that the method works with arbitrary‑precision Number subclasses."
        }
    ]
}