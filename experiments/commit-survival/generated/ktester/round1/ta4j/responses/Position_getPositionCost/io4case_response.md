{
  "testCases": [
    {
      "group": "Negative transaction cost",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 4
            }
          ],
          "expected": "5",
          "description": "finalIndex = 4, transactionCostModel returns -5 (rebate), holdingCost returns 10 → position cost = -5 + 10 = 5"
        }
      ]
    },
    {
      "group": "Negative holding cost (profit)",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 9
            }
          ],
          "expected": "1",
          "description": "finalIndex = 9, transactionCostModel returns 3, holdingCost returns -2 (profit) → position cost = 3 + (-2) = 1"
        }
      ]
    },
    {
      "group": "NaN cost handling",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 6
            }
          ],
          "expected": "NaN",
          "description": "finalIndex = 6, transactionCostModel returns NaN, holdingCost returns 4 → position cost = NaN + 4 = NaN"
        },
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 11
            }
          ],
          "expected": "NaN",
          "description": "finalIndex = 11, both transactionCostModel and holdingCost return NaN → position cost = NaN"
        }
      ]
    },
    {
      "group": "Null transaction cost",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 12
            }
          ],
          "expected": "NullPointerException",
          "description": "finalIndex = 12, transactionCostModel.calculate returns null, holdingCost returns 5 → getPositionCost should throw NullPointerException when calling plus()"
        }
      ]
    },
    {
      "group": "Holding cost exception propagation",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 13
            }
          ],
          "expected": "IllegalStateException",
          "description": "finalIndex = 13, transactionCostModel returns 4, getHoldingCost(finalIndex) throws IllegalStateException → getPositionCost should propagate the exception"
        }
      ]
    },
    {
      "group": "Extreme index values",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": 2147483647
            }
          ],
          "expected": "0",
          "description": "finalIndex = Integer.MAX_VALUE, both transactionCostModel and holdingCost return 0 (e.g., position already closed) → position cost = 0"
        },
        {
          "input": [
            {
              "parameter": "finalIndex",
              "value": -2147483648
            }
          ],
          "expected": "RuntimeException",
          "description": "finalIndex = Integer.MIN_VALUE, transactionCostModel throws RuntimeException due to invalid index → exception should be propagated"
        }
      ]
    }
  ]
}