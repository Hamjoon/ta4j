{
    "group": "NumericIndicator.squared()",
    "cases": [
        {
            "input": [],
            "expected": "NumericIndicator where getValue(i) = original.getValue(i) * original.getValue(i) for each index i. For a delegate returning [2, 3, 4] the squared values are [4, 9, 16].",
            "description": "Validate that squared() correctly multiplies positive numbers by themselves."
        },
        {
            "input": [],
            "expected": "NumericIndicator where getValue(i) = 0 for all i when the original delegate returns 0. For a delegate returning [0, 0, 0] the squared values are [0, 0, 0].",
            "description": "Check behaviour when the underlying values are zero (0 * 0 = 0)."
        },
        {
            "input": [],
            "expected": "NumericIndicator where getValue(i) = original.getValue(i) * original.getValue(i) yielding positive results for negative inputs. For a delegate returning [-2, -5] the squared values are [4, 25].",
            "description": "Ensure that negative numbers are squared correctly (sign is lost)."
        },
        {
            "input": [],
            "expected": "NumericIndicator where getValue(i) = original.getValue(i) * original.getValue(i) with decimal precision. For a delegate returning [1.5, -2.3] the squared values are [2.25, 5.29] (within Num precision).",
            "description": "Verify correct handling of fractional numbers and rounding behaviour of the Num implementation."
        },
        {
            "input": [],
            "expected": "The object returned by squared() is a distinct instance from the original NumericIndicator (reference inequality).",
            "description": "Confirm that squared() creates a new NumericIndicator rather than returning the same instance."
        },
        {
            "input": [],
            "expected": "Original NumericIndicator remains unchanged after calling squared(). Subsequent calls to original.getValue(i) still return the initial values.",
            "description": "Assert that the squared() operation does not mutate the original indicator’s internal state."
        },
        {
            "input": [],
            "expected": "Chaining squared() twice yields fourth power: (x^2)^2 = x^4. For a delegate returning [2] the double‑squared values are [16].",
            "description": "Validate composability of squared() – applying it twice results in the fourth power of the original values."
        }
    ]
}