{
    "group": "BarSeriesManager.getHoldingCostModel - additional cases",
    "cases": [
        {
            "input": [],
            "expected": "instance of CustomCostModel returned (same object as provided to constructor)",
            "description": "Verify that getHoldingCostModel() returns the exact CostModel instance when a user‑defined implementation (anonymous class) is injected via the constructor."
        },
        {
            "input": [],
            "expected": "instance of mocked CostModel returned (same object as provided to constructor)",
            "description": "Verify that getHoldingCostModel() returns the mocked CostModel (e.g., created with Mockito) that was supplied to the constructor, confirming that no proxy or copy is created."
        },
        {
            "input": [],
            "expected": "original CostModel instance still returned after external reference reassignment",
            "description": "Confirm that the manager keeps a reference to the CostModel given at construction: after reassigning the variable that originally held the CostModel to a different object, getHoldingCostModel() still returns the original instance."
        }
    ]
}