{
    "test_nullTradeType_throws_NPE": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategyOperatesOnlyAtIndex(2)"
                },
                {
                    "parameter": "tradeType",
                    "value": "null"
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
                    "value": "5"
                }
            ],
            "expected": "NullPointerException is thrown when BaseTradingRecord constructor receives a null TradeType",
            "description": "Passing a null TradeType should cause an NPE inside the BaseTradingRecord constructor (or later when the record is used). The manager does not validate the trade type."
        }
    ],
    "test_nullAmount_throws_NPE": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategyOperatesOnlyAtIndex(3)"
                },
                {
                    "parameter": "tradeType",
                    "value": "TradeType.BUY"
                },
                {
                    "parameter": "amount",
                    "value": "null"
                },
                {
                    "parameter": "startIndex",
                    "value": "1"
                },
                {
                    "parameter": "finishIndex",
                    "value": "4"
                }
            ],
            "expected": "NullPointerException is thrown when tradeExecutionModel.execute is invoked with a null amount",
            "description": "The method forwards the amount directly to TradeExecutionModel.execute. Supplying a null Num should trigger an NPE at that call."
        }
    ],
    "test_nullTradeExecutionModel_throws_NPE": [
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
                    "value": "Num.valueOf(5)"
                },
                {
                    "parameter": "startIndex",
                    "value": "0"
                },
                {
                    "parameter": "finishIndex",
                    "value": "10"
                }
            ],
            "expected": "NullPointerException is thrown when the method attempts to call tradeExecutionModel.execute",
            "description": "Construct BarSeriesManager with a null TradeExecutionModel. When the strategy signals an operation, the manager will try to invoke execute on a null reference, causing an NPE."
        }
    ],
    "test_nullTransactionCostModel_throws_NPE": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategyOperatesOnlyAtIndex(1)"
                },
                {
                    "parameter": "tradeType",
                    "value": "TradeType.BUY"
                },
                {
                    "parameter": "amount",
                    "value": "Num.valueOf(2)"
                },
                {
                    "parameter": "startIndex",
                    "value": "0"
                },
                {
                    "parameter": "finishIndex",
                    "value": "5"
                }
            ],
            "expected": "NullPointerException is thrown during BaseTradingRecord construction because transactionCostModel is null",
            "description": "Create BarSeriesManager with a null TransactionCostModel while keeping other fields valid. BaseTradingRecord's constructor expects a non‑null CostModel; passing null should raise an NPE."
        }
    ],
    "test_nullHoldingCostModel_throws_NPE": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategyOperatesOnlyAtIndex(1)"
                },
                {
                    "parameter": "tradeType",
                    "value": "TradeType.SELL"
                },
                {
                    "parameter": "amount",
                    "value": "Num.valueOf(3)"
                },
                {
                    "parameter": "startIndex",
                    "value": "0"
                },
                {
                    "parameter": "finishIndex",
                    "value": "5"
                }
            ],
            "expected": "NullPointerException is thrown during BaseTradingRecord construction because holdingCostModel is null",
            "description": "Construct BarSeriesManager with a null HoldingCostModel. The BaseTradingRecord constructor will receive a null holding cost model and throw an NPE."
        }
    ],
    "test_strategyThrowsException_propagates": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategyThrowsRuntimeException"
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
                    "value": "3"
                }
            ],
            "expected": "RuntimeException is propagated out of BarSeriesManager.run",
            "description": "The supplied strategy throws a RuntimeException inside shouldOperate. The manager does not catch it, so the exception should bubble up to the caller."
        }
    ],
    "test_tradeExecutionModelThrowsException_propagates": [
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
                    "value": "Num.valueOf(1)"
                },
                {
                    "parameter": "startIndex",
                    "value": "0"
                },
                {
                    "parameter": "finishIndex",
                    "value": "5"
                }
            ],
            "expected": "RuntimeException thrown by tradeExecutionModel.execute is propagated",
            "description": "Configure the TradeExecutionModel mock to throw a RuntimeException when execute is called. The manager should not swallow the exception; it must propagate it."
        }
    ],
    "test_nullBarSeries_throws_NPE": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategyOperatesOnlyAtIndex(0)"
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
                    "value": "5"
                }
            ],
            "expected": "NullPointerException is thrown when accessing barSeries.getBeginIndex() or getEndIndex()",
            "description": "Instantiate BarSeriesManager with a null BarSeries. The first call to barSeries.getBeginIndex() will cause an NPE."
        }
    ]
}