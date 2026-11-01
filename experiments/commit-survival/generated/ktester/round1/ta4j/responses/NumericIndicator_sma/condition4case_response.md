{
  "testCases": [
    {
      "group": "sma() - valid barCount",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 5
            }
          ],
          "expected": "NumericIndicator instance whose delegate is an SMAIndicator with barCount=5. Calling getValue(i) on the returned object yields the simple moving average of the original NumericIndicator over the last 5 bars (or fewer for i < 4).",
          "description": "Verify that sma(5) creates a NumericIndicator wrapping an SMAIndicator with the requested period and that the SMA values are correctly computed."
        },
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 1
            }
          ],
          "expected": "NumericIndicator instance whose delegate is an SMAIndicator with barCount=1. For any index i, getValue(i) must be equal to the original NumericIndicator's getValue(i).",
          "description": "When the period is 1, SMA should return the original series unchanged. This test confirms that the shortcut works."
        }
      ]
    },
    {
      "group": "sma() - edge / error cases",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 0
            }
          ],
          "expected": "IllegalArgumentException (or any RuntimeException) thrown because SMAIndicator requires a positive barCount.",
          "description": "SMAIndicator does not accept a period of 0; the method should propagate the exception."
        },
        {
          "input": [
            {
              "parameter": "barCount",
              "value": -3
            }
          ],
          "expected": "IllegalArgumentException (or any RuntimeException) thrown because SMAIndicator requires a positive barCount.",
          "description": "Negative barCount is invalid; verify that the method fails fast with an appropriate exception."
        }
      ]
    },
    {
      "group": "sma() - delegate integrity",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 10
            }
          ],
          "expected": "The returned NumericIndicator's delegate() method returns an instance of SMAIndicator and not the original delegate. The delegate's getBarSeries() must be the same BarSeries as the original NumericIndicator.",
          "description": "Ensure that the wrapper correctly forwards the series reference while substituting the calculation logic with SMA."
        }
      ]
    }
  ]
}