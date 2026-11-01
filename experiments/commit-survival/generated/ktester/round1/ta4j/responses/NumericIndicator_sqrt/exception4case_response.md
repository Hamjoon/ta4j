{
    "test_cases": [
        {
            "group": "NumericIndicator.sqrt() - null delegate",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Calling sqrt() on a NumericIndicator that was created with a null delegate should throw NullPointerException because UnaryOperation.sqrt attempts to access the delegate."
                }
            ]
        },
        {
            "group": "NumericIndicator.sqrt() - delegate returns null value",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException when accessing sqrtIndicator.getValue(0)",
                    "description": "Create a custom Indicator whose getValue(0) returns null. Wrap it with NumericIndicator.of(...). Calling sqrt() succeeds, but retrieving the sqrt value at the index where the original value is null must raise NullPointerException (the sqrt operation cannot be applied to a null Num)."
                }
            ]
        }
    ]
}