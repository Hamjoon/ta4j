{
    "group": "Position.getExit",
    "cases": [
        {
            "input": [],
            "expected": "null",
            "description": "A newly created Position has no exit trade; getExit() should return null."
        },
        {
            "input": [],
            "expected": "non‑null Trade instance",
            "description": "After the position is closed (e.g., by calling operate(...) that creates an exit Trade), getExit() must return the stored exit Trade object."
        }
    ]
}