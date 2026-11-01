{
    "group": "ZeroCostModel behavior",
    "cases": [
        {
            "input": [],
            "expected": "Num value equal to ZeroCostModel.ZERO (i.e., Num representing 0)",
            "description": "When a Position is constructed with the built‑in ZeroCostModel, getHoldingCost() should delegate to the model and return a Num equal to zero, regardless of the Position state."
        }
    ]
}