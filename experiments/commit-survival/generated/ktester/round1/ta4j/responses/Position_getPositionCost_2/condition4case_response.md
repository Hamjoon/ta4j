{
    "test_cases": [
        {
            "group": "Standard calculations",
            "cases": [
                {
                    "input": [],
                    "expected": "Num value equal to 150.0 (transactionCost 100.0 + borrowingCost 50.0)",
                    "description": "Both transaction cost and holding cost are positive numbers. The method should return the sum of the two values."
                },
                {
                    "input": [],
                    "expected": "Num value equal to 0.0 (transactionCost 0.0 + borrowingCost 0.0)",
                    "description": "Both transaction cost and holding cost are zero. The method should correctly return zero."
                },
                {
                    "input": [],
                    "expected": "Num value equal to -30.0 (transactionCost -10.0 + borrowingCost -20.0)",
                    "description": "Both transaction cost and holding cost are negative. The method should return the algebraic sum (still negative)."
                },
                {
                    "input": [],
                    "expected": "Num value equal to NaN (transactionCost NaN + borrowingCost 25.0)",
                    "description": "Transaction cost is NaN while borrowing cost is a normal number. According to Num arithmetic, any operation with NaN yields NaN."
                },
                {
                    "input": [],
                    "expected": "Num value equal to NaN (transactionCost 40.0 + borrowingCost NaN)",
                    "description": "Borrowing cost is NaN while transaction cost is a normal number. The result should be NaN."
                },
                {
                    "input": [],
                    "expected": "Num value equal to NaN (transactionCost NaN + borrowingCost NaN)",
                    "description": "Both transaction cost and borrowing cost are NaN. The result must be NaN."
                }
            ]
        },
        {
            "group": "Error handling / edge cases",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "The transactionCostModel field is null. Calling calculate(this) should throw a NullPointerException. This verifies that the method does not swallow the exception."
                },
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "The getHoldingCost() method returns null (simulated via a subclass that overrides it). Adding a null Num to a valid transaction cost should raise a NullPointerException."
                }
            ]
        }
    ]
}