{
    "group": "hasProfit scenarios",
    "cases": [
        {
            "input": [],
            "expected": true,
            "description": "Position with a positive profit (exit price > entry price for a BUY trade). getProfit() returns a positive Num, so hasProfit() should return true."
        },
        {
            "input": [],
            "expected": false,
            "description": "Position with zero profit (exit price equals entry price). getProfit() returns Num zero, isPositive() is false, so hasProfit() should return false."
        },
        {
            "input": [],
            "expected": false,
            "description": "Position with a negative profit (exit price < entry price for a BUY trade). getProfit() returns a negative Num, isPositive() is false, so hasProfit() should return false."
        },
        {
            "input": [],
            "expected": false,
            "description": "Position where profit is NaN (e.g., price data missing). getProfit() returns NaN, isPositive() is defined to be false, so hasProfit() should return false."
        }
    ]
}