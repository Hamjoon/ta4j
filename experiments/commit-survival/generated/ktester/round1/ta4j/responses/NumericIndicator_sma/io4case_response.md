{
  "testCases": [
    {
      "group": "sma() - barCount larger than series length",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 10
            }
          ],
          "expected": "NumericIndicator instance whose delegate is an SMAIndicator with barCount=10. For a BarSeries containing the values [10, 20, 30, 40] the SMA values must be: index0 → 10, index1 → 15, index2 → 20, index3 → 25 (average of all available values up to the current index).",
          "description": "When the requested period exceeds the number of available bars, SMAIndicator should compute the average over the existing subset (i+1 values). This test verifies correct handling of oversized barCount."
        }
      ]
    },
    {
      "group": "sma() - barCount equal to series length",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 4
            }
          ],
          "expected": "NumericIndicator instance whose delegate is an SMAIndicator with barCount=4. Using the same series [10, 20, 30, 40] the SMA values are: index0 → 10, index1 → 15, index2 → 20, index3 → 25 (full‑period average at the last index).",
          "description": "Ensures that a barCount exactly matching the series length yields the same progressive averages as the ‘larger than length’ case, and that the final SMA equals the overall mean."
        }
      ]
    },
    {
      "group": "sma() - multiple independent invocations",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 3
            }
          ],
          "expected": "Two successive calls to sma(3) on the same NumericIndicator must return two distinct NumericIndicator objects (different identity) but both delegating to separate SMAIndicator instances that produce identical SMA values for every index.",
          "description": "Verifies that sma() does not cache or reuse the same wrapper, guaranteeing independence of each call."
        }
      ]
    },
    {
      "group": "sma() - original indicator immutability",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 3
            }
          ],
          "expected": "After invoking sma(3) the original NumericIndicator’s getValue(i) must continue to return the original series values (10, 20, 30, 40) unchanged, i.e., the SMA wrapper does not mutate the source indicator.",
          "description": "Confirms that the sma() method is non‑destructive and the source indicator remains intact."
        }
      ]
    },
    {
      "group": "sma() - toString representation",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 5
            }
          ],
          "expected": "The toString() of the returned NumericIndicator contains the substring \"SMAIndicator\" and the period value (5), e.g., \"NumericIndicator{delegate=SMAIndicator[5]}\".",
          "description": "Checks that the wrapper’s string representation reflects the underlying SMAIndicator, aiding debugging."
        }
      ]
    },
    {
      "group": "sma() - nested SMA composition",
      "cases": [
        {
          "input": [
            {
              "parameter": "barCount",
              "value": 2
            }
          ],
          "expected": "When a NumericIndicator first creates sma(3) and then on the resulting indicator calls sma(2), the final values correspond to a double‑smoothed series. For the base series [10, 20, 30, 40]: first SMA(3) yields [10, 15, 20, 30]; applying SMA(2) on that yields final values [10, 12.5, 17.5, 25].",
          "description": "Ensures that SMA wrappers can be chained and that each layer correctly uses the previous indicator as its source."
        }
      ]
    }
  ]
}