{
  "testCases": [
    {
      "group": "NumericIndicator.isLessThan - negative and zero values",
      "cases": [
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(-1)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == true",
          "description": "NumericIndicator wraps ConstantIndicator(-2). Since -2 < -1, the UnderIndicatorRule should be satisfied at index 0."
        },
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(0)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == false",
          "description": "Both the NumericIndicator and the other indicator return 0. Equality does not satisfy UnderIndicatorRule, therefore the rule must be unsatisfied."
        },
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(0)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == true",
          "description": "NumericIndicator wraps ConstantIndicator(-0.5). Since -0.5 < 0, the rule must be satisfied at index 0."
        }
      ]
    },
    {
      "group": "NumericIndicator.isLessThan - fractional values",
      "cases": [
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(2.5)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == true",
          "description": "NumericIndicator wraps ConstantIndicator(2.4). 2.4 is less than 2.5, so the rule should be satisfied at index 0."
        },
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(2.5)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == false",
          "description": "NumericIndicator wraps ConstantIndicator(2.5). Equality does not satisfy UnderIndicatorRule, therefore the rule must be unsatisfied."
        }
      ]
    },
    {
      "group": "NumericIndicator.isLessThan - varying other indicator",
      "cases": [
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(2)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == true",
          "description": "Series close prices are [1,2,3]; NumericIndicator wraps ClosePriceIndicator(series). At index 0 the value is 1, which is less than 2, so the rule is satisfied."
        },
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(2)"
            }
          ],
          "expected": "Rule.isSatisfied(2) == false",
          "description": "Using the same series, at index 2 the close price is 3, which is not less than 2; the rule must be unsatisfied."
        }
      ]
    },
    {
      "group": "NumericIndicator.isLessThan - large numeric values",
      "cases": [
        {
          "input": [
            {
              "parameter": "other",
              "value": "ConstantIndicator(2000000)"
            }
          ],
          "expected": "Rule.isSatisfied(0) == true",
          "description": "NumericIndicator wraps ConstantIndicator(1000000). Since 1,000,000 < 2,000,000, the rule should be satisfied at index 0."
        }
      ]
    },
    {
      "group": "NumericIndicator.isLessThan - null argument handling",
      "cases": [
        {
          "input": [
            {
              "parameter": "other",
              "value": "null"
            }
          ],
          "expected": "throws NullPointerException",
          "description": "Passing a null Indicator to isLessThan should result in a NullPointerException according to the contract of UnderIndicatorRule."
        }
      ]
    }
  ]
}