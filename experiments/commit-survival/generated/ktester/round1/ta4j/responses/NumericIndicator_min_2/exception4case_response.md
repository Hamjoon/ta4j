{
    "group": "NumericIndicator.min(Number) - exception handling",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": null
                }
            ],
            "expected": "NullPointerException (or IllegalArgumentException) thrown because the Number argument is null",
            "description": "Passing a null Number should cause the method to reject the argument and raise an exception before any indicator is created."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NaN"
                }
            ],
            "expected": "IllegalArgumentException thrown because NaN is not a valid numeric constant for a ConstantIndicator",
            "description": "The library does not accept NaN as a constant; the method should validate the value and throw an exception."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.POSITIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException thrown because infinite values are not allowed for a ConstantIndicator",
            "description": "Providing an infinite numeric value should be rejected by the constant‑creation logic, resulting in an exception."
        }
    ]
}