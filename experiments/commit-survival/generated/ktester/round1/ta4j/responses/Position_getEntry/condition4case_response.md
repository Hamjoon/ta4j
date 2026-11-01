{
    "group": "Position.getEntry",
    "cases": [
        {
            "input": [],
            "expected": "null",
            "description": "When a newly created Position has no entry trade (the internal 'entry' field is null), getEntry() should return null."
        },
        {
            "input": [],
            "expected": "tradeInstance",
            "description": "When a Position's entry field is set to a Trade object (e.g., via operate(...) or reflection), getEntry() should return the exact same Trade instance."
        }
    ]
}