{
    "group": "abs method functionality",
    "cases": [
        {
            "input": [],
            "expected": "NumericIndicator.abs() returns a new NumericIndicator whose value at any index is the absolute value of the original delegate. For a delegate ConstantIndicator(-10), getValue(0) should be 10.",
            "description": "Verify that abs() converts a purely negative constant indicator to its positive absolute value."
        },
        {
            "input": [],
            "expected": "For a delegate Indicator returning [-5, 0, 7] at indices 0,1,2 respectively, NumericIndicator.abs().getValue(0) == 5, getValue(1) == 0, getValue(2) == 7.",
            "description": "Check that abs() correctly handles a mix of negative, zero, and positive values."
        },
        {
            "input": [],
            "expected": "When the delegate already provides only non‑negative values (e.g., ConstantIndicator(12)), abs() should return a NumericIndicator whose values are unchanged; getValue(0) == 12.",
            "description": "Ensure that abs() is a no‑op for already positive values."
        },
        {
            "input": [],
            "expected": "For a delegate ConstantIndicator(0), abs().getValue(0) should be 0.",
            "description": "Validate that zero remains zero after applying abs()."
        },
        {
            "input": [],
            "expected": "Calling abs() twice yields the same result as calling it once; i.e., abs().abs().getValue(i) == abs().getValue(i) for any index i.",
            "description": "Confirm idempotence of the abs() operation."
        },
        {
            "input": [],
            "expected": "The original NumericIndicator instance remains unchanged after abs() is called; its delegate values are still the original (e.g., -3 at index 0).",
            "description": "Verify that abs() does not mutate the original indicator but returns a new instance."
        }
    ]
}