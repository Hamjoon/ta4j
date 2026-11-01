{
    "group": "Starting Type Retrieval",
    "cases": [
        {
            "input": [
                {
                    "parameter": "startingType",
                    "value": "BUY"
                }
            ],
            "expected": "BUY",
            "description": "A Position created with an entry trade of type BUY should return BUY from getStartingType()."
        },
        {
            "input": [
                {
                    "parameter": "startingType",
                    "value": "SELL"
                }
            ],
            "expected": "SELL",
            "description": "A Position created with an entry trade of type SELL should return SELL from getStartingType()."
        },
        {
            "input": [],
            "expected": "IllegalStateException",
            "description": "If a Position is constructed without a valid entry trade (e.g., null entry), getStartingType() should throw an IllegalStateException (or behave according to the actual implementation). This case verifies the method’s defensive behavior when the internal state is inconsistent."
        }
    ]
}