{
    "testCases": [
        {
            "group": "hasProfit short position scenarios",
            "cases": [
                {
                    "input": [],
                    "expected": true,
                    "description": "Short (SELL) position with a positive profit: entry price 100, exit price 80 (exit lower than entry). getProfit() is positive, so hasProfit() should return true."
                },
                {
                    "input": [],
                    "expected": false,
                    "description": "Short (SELL) position with zero profit: entry price 100, exit price 100. getProfit() is zero, isPositive() is false, so hasProfit() should return false."
                },
                {
                    "input": [],
                    "expected": false,
                    "description": "Short (SELL) position with a negative profit: entry price 100, exit price 120 (exit higher than entry). getProfit() is negative, so hasProfit() should return false."
                }
            ]
        },
        {
            "group": "hasProfit open position scenarios",
            "cases": [
                {
                    "input": [],
                    "expected": false,
                    "description": "Position that is still open (no exit trade). getProfit() returns zero (or NaN) for an open position, therefore hasProfit() should return false."
                },
                {
                    "input": [],
                    "expected": false,
                    "description": "Open BUY position where the current market price is unavailable (price data missing). getProfit() yields NaN, isPositive() is false, so hasProfit() should return false."
                }
            ]
        },
        {
            "group": "hasProfit extreme profit values",
            "cases": [
                {
                    "input": [],
                    "expected": true,
                    "description": "BUY position with an extremely large positive profit (entry price 1, exit price 1,000,000,000). getProfit() is positive, so hasProfit() should return true."
                },
                {
                    "input": [],
                    "expected": true,
                    "description": "BUY position with a very small positive profit (entry price 99.9999, exit price 100). getProfit() is positive albeit tiny, so hasProfit() should return true."
                }
            ]
        }
    ]
}