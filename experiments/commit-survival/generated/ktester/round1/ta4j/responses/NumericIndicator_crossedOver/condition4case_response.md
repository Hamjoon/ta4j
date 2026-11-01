{
  "group": "crossedOver(Indicator<Num>) - basic functionality",
  "cases": [
    {
      "input": [
        {
          "parameter": "other",
          "value": "null"
        }
      ],
      "expected": "NullPointerException",
      "description": "Passing a null Indicator should cause the method to throw a NullPointerException (the CrossedUpIndicatorRule constructor validates its arguments)."
    },
    {
      "input": [
        {
          "parameter": "other",
          "value": "ConstantIndicator(5)"
        }
      ],
      "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(0) == false",
      "description": "When the other indicator is a constant 5 and the NumericIndicator under test is also constant 5, there is no upward crossing at index 0, so the returned rule must be a CrossedUpIndicatorRule and evaluate to false."
    },
    {
      "input": [
        {
          "parameter": "other",
          "value": "ConstantIndicator(3)"
        }
      ],
      "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(1) == true",
      "description": "Create a NumericIndicator that yields values [2, 4] (e.g., using a ConstantIndicator for 2 and then a custom Indicator that returns 4 at index 1). The other indicator is constant 3. At index 1 the NumericIndicator moves from below 3 to above 3, so the CrossedUpIndicatorRule should be satisfied."
    },
    {
      "input": [
        {
          "parameter": "other",
          "value": "ConstantIndicator(7)"
        }
      ],
      "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(2) == false",
      "description": "NumericIndicator values are [6, 5, 4]; the other indicator is constant 7. The NumericIndicator never crosses upward over 7, therefore the rule must evaluate to false at any index (here index 2 is tested)."
    },
    {
      "input": [
        {
          "parameter": "other",
          "value": "NumericIndicator that mirrors the same series"
        }
      ],
      "expected": "instanceof CrossedUpIndicatorRule and rule.isSatisfied(any index) == false",
      "description": "When the 'other' indicator is the same instance as the NumericIndicator itself, a crossing cannot occur; the rule should always return false."
    }
  ]
}