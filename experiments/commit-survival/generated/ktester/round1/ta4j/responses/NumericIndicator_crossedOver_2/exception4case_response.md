{
    "group": "NumericIndicator.crossedOver(Number) - exception scenarios",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NaN"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Passing Double.NaN should cause the method to reject the constant and throw IllegalArgumentException, as NaN is not a valid numeric value for comparison."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.POSITIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Passing Double.POSITIVE_INFINITY should trigger an IllegalArgumentException because an infinite constant cannot be represented as a finite Num for rule evaluation."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "Double.NEGATIVE_INFINITY"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "Passing Double.NEGATIVE_INFINITY should trigger an IllegalArgumentException for the same reason as positive infinity – infinite values are not allowed in constant indicators."
        }
    ]
}