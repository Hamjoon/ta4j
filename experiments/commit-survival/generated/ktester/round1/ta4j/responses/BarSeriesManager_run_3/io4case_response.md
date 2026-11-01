{
  "testCases": [
    {
      "group": "Negative amount handling",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "amount", "value": "Num.valueOf(-100)" }
          ],
          "expected": "IllegalArgumentException (amount must be >= 0)",
          "description": "A negative amount is not a valid investment size; the manager should reject the call with an IllegalArgumentException."
        }
      ]
    },
    {
      "group": "Strategy runtime exception propagation",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyThrowsOnFirstCall" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "amount", "value": "Num.valueOf(1000)" }
          ],
          "expected": "RuntimeException propagated from strategy",
          "description": "If the supplied strategy throws an unchecked exception while being evaluated, the run method must not swallow it; the exception should propagate to the caller."
        }
      ]
    },
    {
      "group": "Multiple sequential trades",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyEnterExitMultipleTimes" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "amount", "value": "Num.valueOf(500)" }
          ],
          "expected": "TradingRecord containing exactly 2 trades: first from index 0 to 2, second from index 3 to 5",
          "description": "A strategy that signals entry at bar 0, exit at bar 2, re‑entry at bar 3 and exit at bar 5 should produce a TradingRecord with two distinct trades covering those index ranges."
        }
      ]
    },
    {
      "group": "Non‑zero start index series",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
            { "parameter": "tradeType", "value": "TradeType.SELL" },
            { "parameter": "amount", "value": "Num.valueOf(250)" }
          ],
          "expected": "TradingRecord containing trades only for bars with index 5‑10 (the series' beginIndex‑endIndex)",
          "description": "When the underlying BarSeries is constructed so that getBeginIndex() returns 5 and getEndIndex() returns 10, the run method must evaluate the strategy only on that window and produce trades limited to those indices."
        }
      ]
    },
    {
      "group": "TradeType mismatch – strategy signals opposite direction",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyAlwaysSell" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "amount", "value": "Num.valueOf(300)" }
          ],
          "expected": "TradingRecord with zero trades (empty record)",
          "description": "If the strategy only generates SELL signals but the caller restricts the run to BUY trades, the manager should ignore those signals and return an empty TradingRecord."
        }
      ]
    },
    {
      "group": "Custom transaction cost model effect",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
            { "parameter": "tradeType", "value": "TradeType.BUY" },
            { "parameter": "amount", "value": "Num.valueOf(1000)" }
          ],
          "expected": "TradingRecord with trades where each entry and exit amount is reduced by the configured transaction cost (e.g., 0.1% per trade)",
          "description": "When a non‑zero TransactionCostModel is set on the BarSeriesManager, the run method must apply the cost to every trade; the test verifies that the resulting trades reflect the deducted cost."
        }
      ]
    },
    {
      "group": "Large amount handling (no overflow)",
      "cases": [
        {
          "input": [
            { "parameter": "strategy", "value": "MockStrategyAlwaysEnterExit" },
            { "parameter": "tradeType", "value": "TradeType.SELL" },
            { "parameter": "amount", "value": "Num.valueOf(Long.MAX_VALUE)" }
          ],
          "expected": "TradingRecord with trades correctly created; no ArithmeticException or overflow occurs",
          "description": "Using the maximum possible long value as the amount should be handled gracefully; the manager must not throw an overflow‑related exception and should still generate the expected trades."
        }
      ]
    }
  ]
}