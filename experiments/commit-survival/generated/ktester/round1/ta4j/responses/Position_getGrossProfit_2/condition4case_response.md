{
  "testCases": [
    {
      "group": "Opened Position - Long",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.valueOf(7)"
            }
          ],
          "expected": "DecimalNum.valueOf(20)",
          "description": "Position is opened (no exit trade) and entry is a BUY (isSell() == false). Gross profit should be amount * finalPrice - entryValue = 10 * 7 - 50 = 20."
        },
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.valueOf(5)"
            }
          ],
          "expected": "DecimalNum.valueOf(0)",
          "description": "Opened long position where finalPrice equals entry price, resulting in zero gross profit."
        },
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.valueOf(3)"
            }
          ],
          "expected": "DecimalNum.valueOf(-20)",
          "description": "Opened long position with a final price lower than entry price, producing a negative gross profit (loss)."
        }
      ]
    },
    {
      "group": "Opened Position - Short",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.valueOf(7)"
            }
          ],
          "expected": "DecimalNum.valueOf(-20)",
          "description": "Position is opened and entry is a SELL (isSell() == true). Gross profit is negated, so profit = -(amount * finalPrice - entryValue) = -(70‑50) = -20."
        },
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.valueOf(5)"
            }
          ],
          "expected": "DecimalNum.valueOf(0)",
          "description": "Opened short position where finalPrice equals entry price; after negation the profit is zero."
        },
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.valueOf(3)"
            }
          ],
          "expected": "DecimalNum.valueOf(20)",
          "description": "Opened short position with finalPrice lower than entry price; after negation the profit becomes positive (gain)."
        }
      ]
    },
    {
      "group": "Closed Position - Long",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.NaN"
            }
          ],
          "expected": "DecimalNum.valueOf(30)",
          "description": "Position is closed (exit trade present) and entry is a BUY. Gross profit = exitValue - entryValue = 80‑50 = 30. finalPrice is ignored for closed positions, so NaN is supplied."
        }
      ]
    },
    {
      "group": "Closed Position - Short",
      "cases": [
        {
          "input": [
            {
              "parameter": "finalPrice",
              "value": "DecimalNum.NaN"
            }
          ],
          "expected": "DecimalNum.valueOf(-30)",
          "description": "Closed position where entry is a SELL. Gross profit = -(exitValue - entryValue) = -(80‑50) = -30."
        }
      ]
    }
  ]
}