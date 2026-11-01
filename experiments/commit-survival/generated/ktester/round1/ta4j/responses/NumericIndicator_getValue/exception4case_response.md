{
    "group": "NumericIndicator.getValue(int) - null delegate propagation",
    "cases": [
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 0
                }
            ],
            "expected": "NullPointerException",
            "description": "NumericIndicator is created via NumericIndicator.of(null) (or constructor with null delegate). Calling getValue should throw NullPointerException because the internal delegate reference is null."
        }
    ]
}