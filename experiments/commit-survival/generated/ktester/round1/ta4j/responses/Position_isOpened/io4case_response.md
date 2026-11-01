{
    "group": "Position.isOpened() state transitions",
    "cases": [
        {
            "input": [],
            "expected": true,
            "description": "A newly created Position is opened by calling `operate(index)` which creates the entry trade (exit remains null). After this operation `isOpened()` must return true."
        },
        {
            "input": [],
            "expected": false,
            "description": "A Position that has been opened (entry set) and then closed by calling `operate(index)` a second time (which creates the exit trade) must report `isOpened()` as false."
        },
        {
            "input": [],
            "expected": false,
            "description": "A Position that was opened, then its entry trade is manually cleared (e.g., via reflection or a setter) while the exit trade remains null. Even though the exit is still null, the absence of an entry means the position is not opened, so `isOpened()` returns false."
        }
    ]
}