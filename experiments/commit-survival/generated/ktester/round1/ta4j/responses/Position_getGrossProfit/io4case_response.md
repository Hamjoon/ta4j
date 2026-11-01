{
    "group": "openedPositionReturnsZero",
    "cases": [
        {
            "input": [],
            "expected": "0",
            "description": "A Position that has only an entry trade (isOpened() == true) should return zero() for getGrossProfit(). The entry trade is a BUY at price 10 with amount 1."
        }
    ]
}