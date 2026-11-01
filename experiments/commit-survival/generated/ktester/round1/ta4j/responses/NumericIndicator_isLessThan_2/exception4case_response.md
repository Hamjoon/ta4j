{
    "group": "NumericIndicator.isLessThan(Number) - exception scenarios",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "10"
                }
            ],
            "expected": "NullPointerException thrown because the NumericIndicator instance was created with a null delegate (e.g., NumericIndicator.of(null)). The method attempts to create a ConstantIndicator and then build an UnderIndicatorRule, which dereferences the null delegate.",
            "description": "NumericIndicator without an underlying delegate. Verifies that isLessThan fails fast when the instance is improperly initialized."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "new FaultyNumber()"
                }
            ],
            "expected": "RuntimeException (or subclass) propagated from the FaultyNumber.doubleValue() call during constant creation.",
            "description": "Custom Number implementation whose doubleValue() throws an exception. Ensures that isLessThan does not swallow unexpected runtime errors from the Number argument."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException (or IllegalArgumentException) thrown by createConstant when the argument is null.",
            "description": "Null argument: verifies that the method validates its input and fails fast when a null Number is supplied."
        }
    ]
}