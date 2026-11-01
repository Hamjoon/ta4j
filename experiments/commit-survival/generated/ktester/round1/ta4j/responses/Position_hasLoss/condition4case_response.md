{
    "group": "Position.hasLoss() - profit sign scenarios",
    "cases": [
        {
            "input": [],
            "expected": true,
            "description": "When the position profit is negative, hasLoss() must return true."
        },
        {
            "input": [],
            "expected": false,
            "description": "When the position profit is zero, hasLoss() must return false (zero is not negative)."
        },
        {
            "input": [],
            "expected": false,
            "description": "When the position profit is positive, hasLoss() must return false."
        },
        {
            "input": [],
            "expected": "NullPointerException",
            "description": "If getProfit() unexpectedly returns null, calling isNegative() should throw a NullPointerException. This case verifies the method does not swallow the exception."
        }
    ]
}