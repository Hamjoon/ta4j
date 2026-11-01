{
    "testCases": [
        {
            "group": "Mixed sign and zero combinations",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 100.0 (transactionCost 100.0 + borrowingCost 0.0)",
                    "description": "Transaction cost is positive while borrowing cost is zero. The method should return the transaction cost unchanged."
                },
                {
                    "input": [],
                    "expected": "Num value equal to 75.0 (transactionCost 0.0 + borrowingCost 75.0)",
                    "description": "Transaction cost is zero while borrowing cost is positive. The method should return the borrowing cost unchanged."
                },
                {
                    "input": [],
                    "expected": "Num value equal to -40.0 (transactionCost -40.0 + borrowingCost 0.0)",
                    "description": "Transaction cost is negative and borrowing cost is zero. The result should be the negative transaction cost."
                },
                {
                    "input": [],
                    "expected": "Num value equal to -25.0 (transactionCost 0.0 + borrowingCost -25.0)",
                    "description": "Transaction cost is zero and borrowing cost is negative. The method should return the negative borrowing cost."
                },
                {
                    "input": [],
                    "expected": "Num value equal to 50.0 (transactionCost 80.0 + borrowingCost -30.0)",
                    "description": "Transaction cost positive, borrowing cost negative. The sum should reflect the algebraic addition (80 - 30 = 50)."
                },
                {
                    "input": [],
                    "expected": "Num value equal to -35.0 (transactionCost -55.0 + borrowingCost 20.0)",
                    "description": "Transaction cost negative, borrowing cost positive. The result should be -55 + 20 = -35."
                }
            ]
        },
        {
            "group": "Large values and potential overflow",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 3.0E20 (transactionCost 1.0E20 + borrowingCost 2.0E20)",
                    "description": "Both costs are very large positive numbers; the method must correctly sum them without loss of precision."
                },
                {
                    "input": [],
                    "expected": "Num value equal to -2.0E30 (transactionCost -1.0E30 + borrowingCost -1.0E30)",
                    "description": "Both costs are very large negative numbers; the method must correctly sum them."
                }
            ]
        },
        {
            "group": "Transaction cost null handling",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "The CostModel.calculate(this) returns null. Adding a null Num to a valid borrowing cost should raise a NullPointerException."
                }
            ]
        },
        {
            "group": "CostModel exception propagation",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalStateException",
                    "description": "The CostModel.calculate(this) throws an IllegalStateException. The method should not catch it; the exception propagates to the caller."
                }
            ]
        },
        {
            "group": "Idempotent result",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 120.0 (transactionCost 70.0 + borrowingCost 50.0) on both calls",
                    "description": "Calling getPositionCost() multiple times on the same Position instance must return the same Num value each time, demonstrating that the method is side‑effect free."
                }
            ]
        }
    ]
}