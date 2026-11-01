Invalid control character at: line 8 column 45 (char 233)
{
    "testCases": [
        {
            "group": "getGrossProfit - opened position (branch: isOpened == true)",
            "cases": [
                {
                    "input": [],
                    "expected": "Num.zero()  // a Num instance representing the value 0",
                    "description": "The position is still opened (isOpened() returns true). The method should return the result of the private zero() helper, i.e., a Num equal to zero."
                }
            ]
        },
        {
            "group": "getGrossProfit - closed position (branch: isOpened == false)",
            "cases": [
                {
                    "input": [],
                    "expected": "Num.valueOf(50)  // (exitPrice - entryPrice) * amount = (110 - 100) * 5 = 50",
                    "description": "The position is closed (isOpened() returns false) with an exit trade whose price per asset is 110. The entry trade price is 100 and the traded amount is 5. The method should call getGrossProfit(exit.getPricePerAsset()) and return the calculated gross profit, which equals 50."
                }
            ]
        },
        {
            "group": "getGrossProfit - closed position with zero profit",
            "cases": [
                {
                    "input": [],
                    "expected": "Num.zero()  // (exitPrice - entryPrice) * amount = (100 - 100) * 10 = 0",
                    "description": "The position is closed but the exit price equals the entry price, resulting in a gross profit of zero. The method should still delegate to getGrossProfit(exit.getPricePerAsset()) and return a Num representing zero."
                }
            ]
        },
        {
            "group": "getGrossProfit - closed position with negative profit (loss)",
            "cases": [
                {
                    "input": [],
                    "expected": "Num.valueOf(-30)  // (exitPrice - entryPrice) * amount = (90 - 100) * 3 = -30",
                    "description": "The position is closed with an exit price lower than the entry price, producing a loss. The method should return a Num with the negative gross profit value."
                }
            ]
        }
    ]
}