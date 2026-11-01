{
    "group": "Position.getProfit()",
    "cases": [
        {
            "input": [],
            "expected": "Num.ZERO (value 0)",
            "description": "When the position is still opened (no exit trade), getProfit() should return zero(). This covers the `if (isOpened())` branch."
        },
        {
            "input": [],
            "expected": "Num value equal to getGrossProfit(exitPrice).minus(getPositionCost())",
            "description": "When the position is closed (exit trade present), getProfit() must return the gross profit calculated with the exit price minus the position cost. This covers the `else` branch. For example, with an entry trade of 100 units at price 10, an exit trade at price 12, and a position cost of 5, the expected profit is (12‑10) * 100 – 5 = 195."
        }
    ]
}