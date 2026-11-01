{
  "testCases": [
    {
      "group": "NumericIndicator.getValue(int) - last valid index propagation",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 9
            }
          ],
          "expected": "Num value equal to delegate.getValue(9) (e.g., DecimalNum.valueOf(123.45))",
          "description": "Series length is 10; index 9 is the last valid index. The delegate returns a concrete Num and getValue must forward it unchanged."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - out‑of‑range index equal to series size",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 10
            }
          ],
          "expected": "IndexOutOfBoundsException (or the same exception the delegate throws for index == size)",
          "description": "When the index equals the series size it is out of bounds; getValue should propagate the delegate's exception without alteration."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - extreme integer index",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 2147483647
            }
          ],
          "expected": "IndexOutOfBoundsException (or the delegate's exception for an impossible index)",
          "description": "Using Integer.MAX_VALUE as the index forces the delegate to reject the request; getValue must forward the exception."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - delegate returns different Num implementation",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 4
            }
          ],
          "expected": "Num instance of type DoubleNum (or any non‑DecimalNum) with value 7.89",
          "description": "The delegate is a mock that returns a DoubleNum at index 4. getValue should return that exact instance/value, proving type‑agnostic delegation."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - NaN and infinite values handling",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 6
            }
          ],
          "expected": "Num representing NaN (DecimalNum.NaN) – the same instance/value returned by the delegate",
          "description": "Delegate returns a Num that encodes NaN. getValue must forward it unchanged, confirming that special floating‑point values are not altered."
        },
        {
          "input": [
            {
              "parameter": "index",
              "value": 7
            }
          ],
          "expected": "Num representing positive infinity (DecimalNum.POSITIVE_INFINITY)",
          "description": "Delegate returns a Num representing +∞. getValue must forward this value unchanged."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - custom unchecked exception propagation",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 1
            }
          ],
          "expected": "CustomRuntimeException (a user‑defined unchecked exception thrown by the delegate)",
          "description": "Delegate throws a bespoke unchecked exception for index 1. getValue must not catch or wrap it; the same exception should bubble up."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - reference equality verification",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 2
            }
          ],
          "expected": "Exactly the same Num object instance that delegate.getValue(2) returned (reference equality)",
          "description": "Delegate returns a specific Num object (e.g., a mocked DecimalNum). The test asserts that getValue returns the identical object reference, confirming pure delegation without copying."
        }
      ]
    },
    {
      "group": "NumericIndicator.getValue(int) - immutable snapshot with mutable delegate state",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 3
            }
          ],
          "expected": "Num value equal to the delegate's value at call time (e.g., DecimalNum.valueOf(55)) even if the delegate later changes its internal data for index 3",
          "description": "Delegate initially returns 55 for index 3; after the call, the delegate's underlying series is mutated to return 99 for the same index. getValue should still return the original 55, demonstrating that it does not cache or lazily recompute after the call."
        }
      ]
    }
  ]
}