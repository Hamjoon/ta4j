{
    "group": "NumericIndicator.sqrt()",
    "cases": [
        {
            "input": [],
            "expected": "NumericIndicator whose values are the square roots of the original indicator's values: [0.0, 1.0, 2.0, 3.0] for original values [0, 1, 4, 9]",
            "description": "Verify sqrt() on a constant indicator with non‑negative values returns a new NumericIndicator whose getValue(i) equals Math.sqrt(originalValue(i))."
        },
        {
            "input": [],
            "expected": "NumericIndicator whose values are all 0.0 for an original indicator with all zeros",
            "description": "Edge case: sqrt() of zero should produce zero without error."
        },
        {
            "input": [],
            "expected": "NumericIndicator whose values are NaN (or the library's representation of an undefined Num) for negative original values, e.g., sqrt(-4) => NaN",
            "description": "Negative input: ensure sqrt() handles negative numbers according to the underlying Num implementation (typically returns NaN) and does not throw an unexpected exception."
        },
        {
            "input": [],
            "expected": "A different NumericIndicator instance than the original (reference inequality) and delegate is an instance of UnaryOperation.Sqrt",
            "description": "Confirm that sqrt() creates a new NumericIndicator (not the same object) and that its delegate is the sqrt unary operation."
        }
    ]
}