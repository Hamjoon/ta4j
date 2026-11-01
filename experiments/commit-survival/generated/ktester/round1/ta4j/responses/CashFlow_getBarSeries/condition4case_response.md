{
    "group": "CashFlow.getBarSeries",
    "cases": [
        {
            "input": [],
            "expected": "the same BarSeries instance that was passed to the CashFlow constructor",
            "description": "Verify that getBarSeries() returns the BarSeries object supplied during construction (normal, non‑null series)."
        },
        {
            "input": [],
            "expected": "null",
            "description": "Verify that getBarSeries() returns null when the CashFlow instance was constructed with a null BarSeries (edge case)."
        }
    ]
}