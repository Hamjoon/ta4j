{
    "group": "BarSeriesManager.getTransactionCostModel - advanced scenarios",
    "cases": [
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "new CostModel() { @Override public Num calculate(Num amount, Num price) { return amount.times(price); } }"
                }
            ],
            "expected": "returns the exact custom CostModel instance; repeated calls return the same reference (==)",
            "description": "Validate that a custom anonymous CostModel implementation passed to the constructor is returned unchanged by the getter, and that multiple invocations of getTransactionCostModel() yield the identical object reference."
        },
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "spy(new ZeroCostModel())"
                }
            ],
            "expected": "returns the spy instance without invoking any CostModel methods",
            "description": "Use a Mockito spy on ZeroCostModel to ensure that calling getTransactionCostModel() does not trigger any interaction with the CostModel (no calculate() call). The spy should have zero interactions after the getter call."
        },
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "mock(CostModel.class)"
                }
            ],
            "expected": "returns the mock instance; after constructing a second manager with a different mock, each getter returns its own mock (no cross‑sharing)",
            "description": "Create two BarSeriesManager objects, each with a distinct Mockito mock CostModel. Verify that each manager's getter returns its own mock, confirming that the field is not shared across instances."
        },
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "new CostModel() { @Override public boolean equals(Object o) { return true; } @Override public int hashCode() { return 42; } @Override public Num calculate(Num amount, Num price) { return amount; } }"
                }
            ],
            "expected": "returns the same instance; equality checks do not cause a copy to be returned",
            "description": "Provide a CostModel with overridden equals() and hashCode() to ensure the getter returns the original object reference, not a new object that merely compares equal."
        },
        {
            "input": [
                {
                    "parameter": "transactionCostModel",
                    "value": "new ZeroCostModel()"
                }
            ],
            "expected": "returns the same ZeroCostModel instance even after invoking other BarSeriesManager methods (e.g., run)",
            "description": "Construct a manager with ZeroCostModel, call getTransactionCostModel() to store the reference, then invoke run(...) (using a dummy strategy). After the run, call getTransactionCostModel() again and verify that the returned reference is still the original instance, confirming immutability of the field."
        }
    ]
}