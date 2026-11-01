{
    "group": "BarSeriesManager.getTransactionCostModel - basic behavior",
    "cases": [
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "new ZeroCostModel()"
                }
            ],
            "expected": "returns the same ZeroCostModel instance passed to the constructor",
            "description": "Verify that when the manager is created with a ZeroCostModel, the getter returns that exact instance."
        },
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "mock(CostModel.class)"
                }
            ],
            "expected": "returns the same mock CostModel instance passed to the constructor",
            "description": "Ensure that a custom CostModel (e.g., a Mockito mock) is returned unchanged by the getter."
        },
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "null"
                }
            ],
            "expected": "returns null",
            "description": "Check the getter's behavior when the manager is constructed with a null transactionCostModel (if the constructor permits null)."
        }
    ]
}