{
  "testCases": [
    {
      "group": "Valid scenarios – normal delegation",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "startIndex", "value": 0 },
            { "parameter": "finishIndex", "value": 10 }
          ],
          "expected": "returns the TradingRecord produced by the overloaded run(strategy, tradeType, amount, startIndex, finishIndex) call",
          "description": "Ensures that with a typical non‑null strategy, BUY trade type and a valid index range, the method delegates correctly to the 5‑argument overload and returns its result."
        },
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": "TradeType.SELL" },
            { "parameter": "startIndex", "value": 5 },
            { "parameter": "finishIndex", "value": 15 }
          ],
          "expected": "returns the TradingRecord produced by the delegated overload",
          "description": "Same as above but with SELL trade type to verify that the trade type is passed unchanged."
        },
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "startIndex", "value": 3 },
            { "parameter": "finishIndex", "value": 3 }
          ],
          "expected": "returns the TradingRecord produced by the delegated overload",
          "description": "Edge case where startIndex equals finishIndex (single bar). The method should still delegate and return a TradingRecord."
        }
      ]
    },
    {
      "group": "Invalid index arguments",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "startIndex", "value": -1 },
            { "parameter": "finishIndex", "value": 5 }
          ],
          "expected": "throws IllegalArgumentException",
          "description": "Negative startIndex should be rejected; the method is expected to validate indices and throw IllegalArgumentException."
        },
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": "TradeType.SELL" },
            { "parameter": "startIndex", "value": 8 },
            { "parameter": "finishIndex", "value": 7 }
          ],
          "expected": "throws IllegalArgumentException",
          "description": "When startIndex is greater than finishIndex the method should detect the invalid range and throw IllegalArgumentException."
        },
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "startIndex", "value": 0 },
            { "parameter": "finishIndex", "value": 1000 }
          ],
          "expected": "throws IllegalArgumentException",
          "description": "finishIndex beyond the size of the underlying BarSeries should cause an IllegalArgumentException (assuming bounds checking)."
        }
      ]
    },
    {
      "group": "Null argument handling",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": null },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "startIndex", "value": 0 },
            { "parameter": "finishIndex", "value": 5 }
          ],
          "expected": "throws NullPointerException",
          "description": "A null strategy is not allowed; the method should throw NullPointerException before delegating."
        },
        {
          "input": [
            { "parameter": "strategy", "value": "validStrategyMock" },
            { "parameter": "tradeType", "value": null },
            { "parameter": "startIndex", "value": 0 },
            { "parameter": "finishIndex", "value": 5 }
          ],
          "expected": "throws NullPointerException",
          "description": "A null tradeType should cause a NullPointerException."
        }
      ]
    }
  ]
}