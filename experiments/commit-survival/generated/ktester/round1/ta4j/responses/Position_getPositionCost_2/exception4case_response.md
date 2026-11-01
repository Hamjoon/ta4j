{
    "testCases": [
        {
            "group": "Mismatched Num implementations",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalArgumentException",
                    "description": "CostModel.calculate(this) returns a Num implementation (e.g., DoubleNum) that is incompatible with the Num returned by getHoldingCost() (e.g., DecimalNum). The plus operation should detect the type mismatch and throw IllegalArgumentException."
                }
            ]
        },
        {
            "group": "Num plus operation exception",
            "cases": [
                {
                    "input": [],
                    "expected": "ArithmeticException",
                    "description": "A custom Num subclass is returned by CostModel.calculate(this) whose plus(Num) method deliberately throws an ArithmeticException (e.g., to simulate overflow). The exception must propagate out of getPositionCost()."
                }
            ]
        },
        {
            "group": "Holding cost exception propagation",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalStateException",
                    "description": "The overridden getHoldingCost() method throws IllegalStateException (e.g., because the position is not properly initialized). The method should not catch it, allowing the exception to propagate."
                }
            ]
        },
        {
            "group": "CostModel calculate throws NullPointerException",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "CostModel.calculate(this) itself throws a NullPointerException (e.g., due to internal null state). The exception must propagate unchanged from getPositionCost()."
                }
            ]
        },
        {
            "group": "CostModel calculate throws generic RuntimeException",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalArgumentException",
                    "description": "CostModel.calculate(this) throws an IllegalArgumentException (or any other unchecked exception). getPositionCost() should not swallow it; the same exception should be observed by the caller."
                }
            ]
        }
    ]
}