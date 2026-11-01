{
  "testCases": [
    {
      "group": "plus(Number) – null and special numeric values",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": "null"
            }
          ],
          "expected": "NullPointerException",
          "description": "When the argument is null the method should throw a NullPointerException rather than creating a ConstantIndicator."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "Double.NaN"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus NaN (result is NaN)",
          "description": "Verify that adding Double.NaN propagates the NaN semantics through the Num implementation."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "Double.POSITIVE_INFINITY"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus +Infinity (result is +Infinity)",
          "description": "Check handling of positive infinite constant – the result should be infinite according to Num rules."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "Double.NEGATIVE_INFINITY"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus -Infinity (result is -Infinity)",
          "description": "Check handling of negative infinite constant – the result should be infinite according to Num rules."
        }
      ]
    },
    {
      "group": "plus(Number) – integer boundary values",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": "Integer.MAX_VALUE"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 2147483647",
          "description": "Test addition of the maximum int value to ensure no unexpected overflow in the underlying Num representation."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "Integer.MIN_VALUE"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i minus 2147483648",
          "description": "Test addition of the minimum int value (a large negative constant)."
        }
      ]
    },
    {
      "group": "plus(Number) – various Number subclasses",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": "new Float(1.5f)"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 1.5 (float precision)",
          "description": "Ensure that Float constants are correctly converted and added with proper decimal precision."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "(short)2"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 2 (short constant)",
          "description": "Validate handling of Short values."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "(byte)-4"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i minus 4 (byte constant)",
          "description": "Validate handling of Byte values."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "new java.math.BigInteger(\"98765432109876543210\")"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 98765432109876543210 (big integer precision)",
          "description": "Check that a BigInteger constant is accepted and added with arbitrary‑precision arithmetic."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "new java.math.BigDecimal(\"0.0000001\")"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 0.0000001 (preserves scale)",
          "description": "Verify that a small‑scale BigDecimal constant preserves its scale and precision when added."
        },
        {
          "input": [
            {
              "parameter": "n",
              "value": "new java.math.BigDecimal(\"1234567890.123456789\")"
            }
          ],
          "expected": "Returned NumericIndicator whose value at any index i equals original delegate value at i plus 1234567890.123456789 (high‑precision decimal)",
          "description": "Test addition with a high‑precision decimal constant to ensure no rounding loss."
        }
      ]
    },
    {
      "group": "plus(Number) – object identity and immutability",
      "cases": [
        {
          "input": [
            {
              "parameter": "n",
              "value": "7"
            }
          ],
          "expected": "Returned object is a new NumericIndicator instance (different from the original) and original delegate remains unchanged",
          "description": "Confirm that plus(Number) creates a new indicator rather than mutating the original instance."
        }
      ]
    }
  ]
}