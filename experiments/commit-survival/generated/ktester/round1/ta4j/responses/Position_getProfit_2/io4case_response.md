{
  "testCases": [
    {
      "group": "getProfit - zero trading cost",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 8
            },
            {
              "parameter": "finalPrice",
              "value": "120.0"
            }
          ],
          "expected": "Profit equals gross profit because trading cost is zero. For a position entered at 90.0 and exited at 120.0 with amount 1, expected profit = 30.0",
          "description": "The Position is built with a ZeroCostModel (no commission, spread or holding cost). The test verifies that getProfit simply returns the gross profit when the cost model contributes zero."
        }
      ]
    },
    {
      "group": "getProfit - break‑even when finalIndex equals entry index",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 2
            },
            {
              "parameter": "finalPrice",
              "value": "50.0"
            }
          ],
          "expected": "0.0",
          "description": "The position is closed at the same index where it was opened and the final price equals the entry price. Both gross profit and trading cost are zero, so profit must be exactly zero."
        }
      ]
    },
    {
      "group": "getProfit - NaN final price handling",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 7
            },
            {
              "parameter": "finalPrice",
              "value": "NaN"
            }
          ],
          "expected": "NaN",
          "description": "When the supplied finalPrice is NaN, getGrossProfit(finalPrice) should propagate NaN and the final profit must also be NaN. This checks that the method does not mask NaN values."
        }
      ]
    },
    {
      "group": "getProfit - open position profit calculation",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 15
            },
            {
              "parameter": "finalPrice",
              "value": "200.0"
            }
          ],
          "expected": "Profit equals (finalPrice - entryPrice) * amount minus holding cost up to finalIndex. For entry price 150.0, amount 1, holding cost 2.5, expected profit = 47.5",
          "description": "The Position has not been closed (no exit trade). getProfit must compute gross profit using the supplied finalPrice and subtract the holding cost returned by getHoldingCost(finalIndex). This test validates the calculation for an open position."
        }
      ]
    },
    {
      "group": "getProfit - high precision final price",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 12
            },
            {
              "parameter": "finalPrice",
              "value": "123.456789"
            }
          ],
          "expected": "Profit equals gross profit (123.456789 - 100.0) * 1 minus trading cost 0.5, i.e. 22.956789",
          "description": "Ensures that getProfit correctly handles Num values with many decimal places and does not lose precision during subtraction of the trading cost."
        }
      ]
    }
  ]
}