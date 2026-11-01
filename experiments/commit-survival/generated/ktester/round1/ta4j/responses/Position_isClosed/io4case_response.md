{
    "group": "Position.isClosed() after operate",
    "cases": [
        {
            "input": [],
            "expected": false,
            "description": "A freshly created Position, then operate(0) is called – this creates the entry trade only. The position is opened but not closed, so isClosed() must return false."
        },
        {
            "input": [],
            "expected": true,
            "description": "A Position where operate(0) creates the entry trade and a subsequent operate(5) creates the exit trade. After both trades exist the position is fully closed, therefore isClosed() must return true."
        },
        {
            "input": [],
            "expected": true,
            "description": "A Position where the entry trade is created with explicit price and amount via operate(0, price, amount) and the exit trade is later created with operate(7, price, amount). The presence of both non‑null trades must make isClosed() return true."
        },
        {
            "input": [],
            "expected": false,
            "description": "A Position where operate(0) creates the entry trade and a second call to operate(0) (same index) does not create an exit trade because the position is already opened with the same type. The position remains open, so isClosed() must return false."
        },
        {
            "input": [],
            "expected": true,
            "description": "A Position where after being fully closed (operate for entry then exit), a third call to operate(10) is made – the method should not alter the closed state, and isClosed() must still return true."
        }
    ]
}