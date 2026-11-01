{
    "group": "CashFlow.getBarSeries reference identity",
    "cases": [
        {
            "input": [],
            "expected": "the same BarSeries instance (identity) that reflects modifications made through the returned reference",
            "description": "Construct a CashFlow with a non‑null BarSeries, retrieve it via getBarSeries(), add a Bar to the returned series, and verify that the original BarSeries size has increased, confirming that getBarSeries returns the actual stored reference."
        }
    ]
}