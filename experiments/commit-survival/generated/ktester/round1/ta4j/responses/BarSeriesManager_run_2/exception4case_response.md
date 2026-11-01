{
    "group": "BarSeries null handling",
    "cases": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "mockStrategy"
                },
                {
                    "parameter": "tradeType",
                    "value": "TradeType.BUY"
                }
            ],
            "expected": "NullPointerException",
            "description": "When the BarSeriesManager is constructed with a null BarSeries, calling run(strategy, tradeType) should throw a NullPointerException before attempting to invoke barSeries.one()."
        }
    ]
}