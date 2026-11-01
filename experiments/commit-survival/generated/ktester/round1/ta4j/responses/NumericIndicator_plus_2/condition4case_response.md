{
    "group": "plus(Number) – basic arithmetic behavior",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "5"
                }
            ],
            "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 5",
            "description": "Verify that plus(Number) correctly adds a positive integer constant to the underlying indicator."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-3"
                }
            ],
            "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i minus 3",
            "description": "Verify that plus(Number) correctly adds a negative integer constant (i.e., subtraction) to the underlying indicator."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i (no change)",
            "description": "Edge case where the constant is zero; the resulting indicator should be identical to the original."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "2.75"
                }
            ],
            "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 2.75 (preserving decimal precision)",
            "description": "Verify that plus(Number) works with a Double constant and preserves fractional part."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new java.math.BigDecimal(\"12345678901234567890\")"
                }
            ],
            "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 12345678901234567890 (big integer precision)",
            "description": "Verify that plus(Number) correctly handles a BigDecimal constant, testing large‑value arithmetic."
        }
    ]
}