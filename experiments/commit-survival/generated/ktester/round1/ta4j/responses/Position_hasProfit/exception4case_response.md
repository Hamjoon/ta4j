{
    "group": "hasProfit exception scenarios",
    "cases": [
        {
            "input": [],
            "expected": "NullPointerException",
            "description": "Position has never been entered (entry trade is null). When hasProfit() calls getProfit(), a NullPointerException is thrown because getProfit() attempts to access the entry trade."
        },
        {
            "input": [],
            "expected": "IllegalStateException",
            "description": "Open position (entry trade present, exit trade null) and the underlying BarSeries is not set. getProfit() cannot determine the current price and throws IllegalStateException, which propagates through hasProfit()."
        },
        {
            "input": [],
            "expected": "ArithmeticException",
            "description": "Position where the profit calculation overflows the numeric type used by the Num implementation (e.g., extremely large entry and exit prices causing arithmetic overflow). getProfit() throws ArithmeticException, which is not caught by hasProfit()."
        },
        {
            "input": [],
            "expected": "IllegalArgumentException",
            "description": "Position with a null CostModel (if a custom cost model is injected and later used in profit calculation). getProfit() validates the cost model and throws IllegalArgumentException, which propagates to hasProfit()."
        }
    ]
}