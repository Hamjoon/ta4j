{
    "group": "Null BarSeries in manager",
    "cases": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "MockStrategyAlwaysEnterExit"
                },
                {
                    "parameter": "tradeType",
                    "value": "TradeType.BUY"
                },
                {
                    "parameter": "amount",
                    "value": "Num.valueOf(1000)"
                }
            ],
            "expected": "NullPointerException",
            "description": "BarSeriesManager is instantiated with a null BarSeries; calling run should immediately throw a NullPointerException because the internal call to barSeries.getBeginIndex() dereferences a null reference."
        }
    ]
}