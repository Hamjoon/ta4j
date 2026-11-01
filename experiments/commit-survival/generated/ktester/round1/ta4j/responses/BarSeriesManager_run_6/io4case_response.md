{
    "newTestCases": [
        {
            "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - startIndex greater than finishIndex (no iteration)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategyNeverOperates"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(1)"
                        },
                        {
                            "parameter": "startIndex",
                            "value": "10"
                        },
                        {
                            "parameter": "finishIndex",
                            "value": "5"
                        }
                    ],
                    "expected": "tradeExecutionModel.execute is never called; runBeginIndex (10) > runEndIndex (5) so the main loop is skipped; returned TradingRecord is closed with zero trades; extra‑bars block is not entered because tradingRecord.isClosed() is true",
                    "description": "When startIndex is greater than finishIndex the calculated runBeginIndex exceeds runEndIndex, resulting in no iteration over the series. Even though the strategy never operates, the test confirms that the method safely handles inverted bounds without throwing and returns an empty closed TradingRecord."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - single‑index operation (open only)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategyOperatesOnlyAtIndex(3)"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(7)"
                        },
                        {
                            "parameter": "startIndex",
                            "value": "3"
                        },
                        {
                            "parameter": "finishIndex",
                            "value": "3"
                        }
                    ],
                    "expected": "tradeExecutionModel.execute is called exactly once with i=3; returned TradingRecord is open (one entry trade, no exit) because no closing signal occurs within the single‑index range; extra‑bars block is not executed because runEndIndex != seriesEnd",
                    "description": "The run is limited to a single bar where the strategy signals an operation. The method should open a position on that bar and then exit the loop, leaving the position open. This validates correct handling of a degenerate range where startIndex equals finishIndex."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - extra bars skipped when position already closed at series end",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategyOpenCloseAtEnd"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(4)"
                        },
                        {
                            "parameter": "startIndex",
                            "value": "0"
                        },
                        {
                            "parameter": "finishIndex",
                            "value": "barSeries.getEndIndex()"
                        }
                    ],
                    "expected": "tradeExecutionModel.execute is called twice (open at index X, close at index Y) within the main loop; returned TradingRecord is closed; tradeExecutionModel.execute is NOT called in the extra‑bars loop because tradingRecord.isClosed() is true",
                    "description": "The strategy opens a position early and closes it exactly at the last bar of the series. Even though runEndIndex equals the series end, the extra‑bars block must be bypassed because the record is already closed. This ensures the guard `!tradingRecord.isClosed()` works correctly."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - zero amount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategyOperatesOnlyAtIndex(2)"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.SELL"
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(0)"
                        },
                        {
                            "parameter": "startIndex",
                            "value": "1"
                        },
                        {
                            "parameter": "finishIndex",
                            "value": "5"
                        }
                    ],
                    "expected": "tradeExecutionModel.execute is called exactly once with amount equal to Num.valueOf(0); returned TradingRecord is open (entry with zero amount); no exception is thrown",
                    "description": "Even when the amount is zero, the manager should still forward the call to the TradeExecutionModel. This test verifies that a zero‑amount does not cause early termination or an exception."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - null strategy throws NPE",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "null"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(1)"
                        },
                        {
                            "parameter": "startIndex",
                            "value": "0"
                        },
                        {
                            "parameter": "finishIndex",
                            "value": "barSeries.getEndIndex()"
                        }
                    ],
                    "expected": "NullPointerException is thrown when the method attempts to call strategy.shouldOperate",
                    "description": "The method does not perform an explicit null‑check on the supplied strategy. Passing a null reference should result in an NPE at the first invocation of `strategy.shouldOperate`. This test confirms the current contract (i.e., that a null strategy is not tolerated)."
                }
            ]
        },
        {
            "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - consecutive true signals",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "strategy",
                            "value": "mockStrategyOperatesOnIndexes([2,3,4])"
                        },
                        {
                            "parameter": "tradeType",
                            "value": "TradeType.BUY"
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(1)"
                        },
                        {
                            "parameter": "startIndex",
                            "value": "1"
                        },
                        {
                            "parameter": "finishIndex",
                            "value": "5"
                        }
                    ],
                    "expected": "tradeExecutionModel.execute is called three times (i=2, i=3, i=4); the resulting TradingRecord contains three trades (each may be entry or exit depending on internal state of BaseTradingRecord); record may be open or closed depending on parity of calls – specifically, after three calls starting from a closed state it will be open with one entry and one exit completed, and a third entry pending; no extra‑bars loop execution because runEndIndex != seriesEnd",
                    "description": "The strategy returns true on three consecutive indices. This tests that the manager does not skip any index and forwards each signal to the execution model, allowing the underlying TradingRecord to handle multiple rapid operations correctly."
                }
            ]
        }
    ]
}