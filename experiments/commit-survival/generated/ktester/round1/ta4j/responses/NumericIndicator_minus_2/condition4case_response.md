{
  "testCases": [
    {
      "group": "minus(Number) with positive integer constant",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": 5
            }
          ],
          "expected": "NumericIndicator whose getValue(0) returns 95 (original delegate value 100 minus 5)",
          "description": "Verify that minus(Number) correctly creates a constant indicator from a positive Integer and subtracts it from the delegate."
        }
      ]
    },
    {
      "group": "minus(Number) with negative double constant",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": -4.2
            }
          ],
          "expected": "NumericIndicator whose getValue(0) returns 24.7 (original delegate value 20.5 minus -4.2)",
          "description": "Check that a negative Double is handled as a constant and subtraction works (adds the absolute value)."
        }
      ]
    },
    {
      "group": "minus(Number) with zero constant",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": 0
            }
          ],
          "expected": "NumericIndicator whose getValue(0) returns 0 (original delegate value 0 minus 0)",
          "description": "Edge case where the constant is zero; the result should be identical to the original delegate."
        }
      ]
    },
    {
      "group": "minus(Number) with null argument",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": null
            }
          ],
          "expected": "NullPointerException",
          "description": "Passing a null Number should cause a NullPointerException when createConstant(n) is invoked."
        }
      ]
    },
    {
      "group": "minus(Number) with large BigDecimal constant",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": "100000000"
            }
          ],
          "expected": "NumericIndicator whose getValue(0) returns 900000000 (original delegate value 1000000000 minus 100000000)",
          "description": "Validate handling of large numeric values (BigDecimal representation) without overflow."
        }
      ]
    },
    {
      "group": "minus(Number) with different numeric types (Long, Float)",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": 1234567890123
            }
          ],
          "expected": "NumericIndicator whose getValue(0) returns -1234567890023 (original delegate value -10 minus 1234567890123)",
          "description": "Ensure that Long values are correctly converted to a constant indicator and subtracted."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": 3.14
            }
          ],
          "expected": "NumericIndicator whose getValue(0) returns 6.86 (original delegate value 10.0 minus 3.14)",
          "description": "Confirm that Float/Double values are handled with proper precision."
        }
      ]
    }
  ]
}