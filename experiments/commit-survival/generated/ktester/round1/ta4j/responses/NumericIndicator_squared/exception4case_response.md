{
    "groups": [
        {
            "group": "NumericIndicator.squared() - null delegate",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Create a NumericIndicator with a null underlying delegate (e.g., via NumericIndicator.of(null) or a custom subclass) and invoke squared(). The implementation accesses the delegate during multiplication, which should result in a NullPointerException."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - delegate returns null Num",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Provide a delegate Indicator whose getValue(int) method returns null for a given index. Calling squared().getValue(index) forces a multiplication of two null Num objects, which must raise a NullPointerException."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - delegate throws runtime exception on getValue",
            "cases": [
                {
                    "input": [],
                    "expected": "ArithmeticException",
                    "description": "Use a delegate Indicator that deliberately throws an ArithmeticException (or any RuntimeException) from its getValue(int) implementation. When squared().getValue(index) is called, the exception should propagate unchanged."
                }
            ]
        }
    ]
}