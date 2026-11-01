{
    "group": "Null transactionCostModel field",
    "cases": [
        {
            "input": [
                {
                    "parameter": "finalIndex",
                    "value": 0
                }
            ],
            "expected": "NullPointerException",
            "description": "The Position instance is constructed with a null transactionCostModel; calling getPositionCost should throw NullPointerException when attempting to invoke calculate()"
        }
    ]
}