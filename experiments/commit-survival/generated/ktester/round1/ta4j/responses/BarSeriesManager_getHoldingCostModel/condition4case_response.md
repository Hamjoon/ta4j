{
    "group": "BarSeriesManager.getHoldingCostModel",
    "cases": [
        {
            "input": [],
            "expected": "instance of ZeroCostModel returned (same object as provided to constructor)",
            "description": "Verify that getHoldingCostModel() returns the CostModel that was injected via the BarSeriesManager constructor when a non‑null model (ZeroCostModel) is supplied."
        },
        {
            "input": [],
            "expected": "null returned",
            "description": "Verify that getHoldingCostModel() returns null when the BarSeriesManager is constructed with a null holdingCostModel (allowed by the class)."
        }
    ]
}