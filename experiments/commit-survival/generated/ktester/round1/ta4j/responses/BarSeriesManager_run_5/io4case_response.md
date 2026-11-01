{
    "group": "Amount handling – barSeries.one() is passed correctly",
    "cases": [
        {
            "input": [
                {
                    "parameter": "strategy",
                    "value": "strategyMock"
                },
                {
                    "parameter": "tradeType",
                    "value": "TradeType.BUY"
                },
                {
                    "parameter": "startIndex",
                    "value": 2
                },
                {
                    "parameter": "finishIndex",
                    "value": 5
                }
            ],
            "expected": "the overloaded run(Strategy, TradeType, Num, int, int) receives Num amount equal to barSeries.one() (e.g., Num.valueOf(123.45)) and returns the TradingRecord supplied by that overload",
            "description": "Overrides the 5‑argument run method to capture the Num argument. The test builds a BarSeries whose one() returns a known Num (123.45). After invoking run(strategy, BUY, 2, 5) the captured amount must equal 123.45, proving that the delegating method forwards the correct amount."
        }
    ]
}