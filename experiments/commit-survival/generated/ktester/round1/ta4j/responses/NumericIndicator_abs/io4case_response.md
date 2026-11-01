{
  "group": "abs method additional scenarios",
  "cases": [
    {
      "input": [],
      "expected": "When the delegate returns a very large positive value (e.g., ConstantIndicator(9_223_372_036_854_775_807)), NumericIndicator.abs().getValue(0) should equal the same large value, confirming that abs() does not alter positive numbers regardless of magnitude.",
      "description": "Validate that abs() correctly handles extreme positive values without overflow or modification."
    },
    {
      "input": [],
      "expected": "If the delegate returns a negative zero (e.g., ConstantIndicator(-0.0)), NumericIndicator.abs().getValue(0) should be 0.0 (positive zero), confirming correct handling of signed zero.",
      "description": "Check that abs() normalises negative zero to standard zero."
    },
    {
      "input": [],
      "expected": "For a delegate that yields NaN (e.g., ConstantIndicator(Num.NaN)), NumericIndicator.abs().getValue(0) should also be NaN, preserving the undefined nature of the value.",
      "description": "Ensure that abs() propagates NaN values unchanged."
    },
    {
      "input": [],
      "expected": "The NumericIndicator returned by abs() must retain the same BarSeries as the original indicator; calling getBarSeries() on both instances should return the identical BarSeries object.",
      "description": "Confirm that the series context is preserved after applying abs()."
    },
    {
      "input": [],
      "expected": "The delegate of the indicator returned by abs() must be a different object from the original delegate (i.e., NumericIndicator.abs().delegate() != original.delegate()), while still producing identical absolute values for all indices.",
      "description": "Verify that abs() creates a new delegate wrapper rather than reusing the original, ensuring immutability of the delegate chain."
    }
  ]
}