{
  "testCases": [
    {
      "group": "Gross Return – Normal Scenarios",
      "cases": [
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesPositive"
            }
          ],
          "expected": "Num value 0.5 (i.e., 50% profit)",
          "description": "Entry price 100, exit price 150 → gross return = (150‑100)/100 = 0.5"
        },
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesNegative"
            }
          ],
          "expected": "Num value -0.5 (i.e., 50% loss)",
          "description": "Entry price 200, exit price 100 → gross return = (100‑200)/200 = -0.5"
        },
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesZero"
            }
          ],
          "expected": "Num value 0",
          "description": "Entry price equals exit price (both 120) → gross return = 0"
        }
      ]
    },
    {
      "group": "Gross Return – Edge Cases with NaN",
      "cases": [
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesEntryNaN"
            }
          ],
          "expected": "Num NaN",
          "description": "Entry price is NaN, exit price is 150 → gross return should propagate NaN"
        },
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesExitNaN"
            }
          ],
          "expected": "Num NaN",
          "description": "Exit price is NaN, entry price is 100 → gross return should propagate NaN"
        }
      ]
    },
    {
      "group": "Gross Return – Null / Illegal State Handling",
      "cases": [
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": null
            }
          ],
          "expected": "NullPointerException",
          "description": "Passing a null BarSeries should cause a NullPointerException when the method tries to retrieve prices"
        },
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesNoEntry"
            }
          ],
          "expected": "IllegalStateException",
          "description": "Position has a null entry trade; calling getGrossReturn should raise an IllegalStateException (or NPE) because entry price cannot be obtained"
        },
        {
          "input": [
            {
              "parameter": "barSeries",
              "value": "mockBarSeriesNoExit"
            }
          ],
          "expected": "IllegalStateException",
          "description": "Position has a null exit trade; calling getGrossReturn should raise an IllegalStateException (or NPE) because exit price cannot be obtained"
        }
      ]
    }
  ]
}