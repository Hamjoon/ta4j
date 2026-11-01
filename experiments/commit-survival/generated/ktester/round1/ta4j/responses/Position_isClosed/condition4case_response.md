{
    "group": "Position.isClosed() branch coverage",
    "cases": [
        {
            "input": [],
            "expected": false,
            "description": "Both entry and exit are null – the position has never been opened, so isClosed() must return false."
        },
        {
            "input": [],
            "expected": false,
            "description": "Entry trade is set (non‑null) but exit trade is still null – the position is opened but not closed, therefore isClosed() must return false."
        },
        {
            "input": [],
            "expected": false,
            "description": "Exit trade is set (non‑null) while entry trade remains null – an inconsistent state that should still yield false because entry is missing."
        },
        {
            "input": [],
            "expected": true,
            "description": "Both entry and exit trades are non‑null – the position is fully closed, so isClosed() must return true."
        }
    ]
}