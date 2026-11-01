{
  "testCases": [
    {
      "group": "Valid barCount values",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 1
            }
          ],
          "expected": "NumericIndicator wrapping an EMAIndicator with barCount=1 and delegate equal to the original NumericIndicator",
          "description": "Verify that ema(1) returns a NumericIndicator whose internal delegate is an EMAIndicator constructed with the original NumericIndicator as its source and a barCount of 1."
        },
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 5
            }
          ],
          "expected": "NumericIndicator wrapping an EMAIndicator with barCount=5 and delegate equal to the original NumericIndicator",
          "description": "Check that a typical positive barCount (5) creates the correct EMAIndicator wrapper."
        },
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 20
            }
          ],
          "expected": "NumericIndicator wrapping an EMAIndicator with barCount=20 and delegate equal to the original NumericIndicator",
          "description": "Validate that larger barCount values are handled correctly and the returned object still contains an EMAIndicator with the same barCount."
        }
      ]
    },
    {
      "group": "Invalid barCount values (exception handling)",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 0
            }
          ],
          "expected": "IllegalArgumentException",
          "description": "EMAIndicator should reject a barCount of 0; ema(0) must throw IllegalArgumentException."
        },
        {
          "input": [
            {
              "parameter": "barCount",
              "value": -3
            }
          ],
          "expected": "IllegalArgumentException",
          "description": "Negative barCount values are invalid; ema(-3) must throw IllegalArgumentException."
        }
      ]
    }
  ]
}