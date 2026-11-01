{
    "group": "Position.getProfit() - closed position variations",
    "cases": [
        {
            "input": [],
            "expected": "Num value equal to (exitPrice - entryPrice) * amount (cost model ZeroCostModel, i.e., position cost = 0)",
            "description": "Closed long position with zero transaction cost. Entry: BUY 50 units @ 20, Exit: SELL @ 25, CostModel = ZeroCostModel. Expected profit = (25‑20) * 50 = 250."
        },
        {
            "input": [],
            "expected": "Num.ZERO (value 0)",
            "description": "Closed long position where entry and exit prices are identical, with zero transaction cost. Entry: BUY 100 units @ 15, Exit: SELL @ 15, CostModel = ZeroCostModel. Gross profit = 0, position cost = 0, so profit = 0."
        },
        {
            "input": [],
            "expected": "Num value equal to (exitPrice - entryPrice) * amount (negative result)",
            "description": "Closed long position that ends with a loss, zero transaction cost. Entry: BUY 30 units @ 40, Exit: SELL @ 35, CostModel = ZeroCostModel. Expected profit = (35‑40) * 30 = -150."
        },
        {
            "input": [],
            "expected": "Num value equal to (entryPrice - exitPrice) * amount (positive result for short)",
            "description": "Closed short (SELL) position with zero transaction cost. Entry: SELL 80 units @ 60, Exit: BUY @ 52, CostModel = ZeroCostModel. Expected profit = (60‑52) * 80 = 640."
        },
        {
            "input": [],
            "expected": "Num value equal to (entryPrice - exitPrice) * amount (negative result for short)",
            "description": "Closed short (SELL) position that ends with a loss, zero transaction cost. Entry: SELL 70 units @ 45, Exit: BUY @ 50, CostModel = ZeroCostModel. Expected profit = (45‑50) * 70 = -350."
        },
        {
            "input": [],
            "expected": "Num value equal to -positionCost (gross profit = 0, cost > 0)",
            "description": "Closed long position where entry and exit prices are equal but a non‑zero cost model is used. Entry: BUY 20 units @ 10, Exit: SELL @ 10, PositionCost = 12 (e.g., commission). Expected profit = 0 – 12 = -12."
        },
        {
            "input": [],
            "expected": "Num.NaN",
            "description": "Closed position where the exit trade price is NaN. Entry: BUY 10 units @ 5, Exit: price = NaN, any cost model. Gross profit becomes NaN, subtraction of position cost yields NaN, so getProfit() should return NaN."
        },
        {
            "input": [],
            "expected": "Num value equal to -positionCost (gross profit = 0 because amount = 0)",
            "description": "Closed position with zero amount traded. Entry: BUY 0 units @ 100, Exit: SELL @ 120, PositionCost = 7. Gross profit = (120‑100) * 0 = 0, profit = 0 – 7 = -7."
        },
        {
            "input": [],
            "expected": "Num value equal to (exitPrice - entryPrice) * amount – positionCost where positionCost is computed by a non‑ZeroCostModel (e.g., 2% commission)",
            "description": "Closed long position using a proportional cost model. Entry: BUY 200 units @ 30, Exit: SELL @ 33, CostModel = 2% of trade value on both entry and exit. PositionCost = (30*200*0.02) + (33*200*0.02) = 120 + 132 = 252. Gross profit = (33‑30)*200 = 600. Expected profit = 600 – 252 = 348."
        }
    ]
}