{
    "group": "NumericIndicator.max(Number) - basic functionality",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 10
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), 10) for all i",
            "description": "Constant 10 is greater than every value produced by the delegate (delegate returns 5 at index 0, 7 at index 1). The resulting indicator should return 10 for those indices."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 3
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), 3) for all i",
            "description": "Constant 3 is lower than delegate values (delegate returns 5 at index 0, 7 at index 1). The resulting indicator should return the delegate values (5, 7)."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 5
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals max(delegate.getValue(i), 5) for all i",
            "description": "Constant 5 equals the delegate value at index 0 and is lower at index 1. Expected values are 5 (index 0) and 7 (index 1)."
        }
    ]
}