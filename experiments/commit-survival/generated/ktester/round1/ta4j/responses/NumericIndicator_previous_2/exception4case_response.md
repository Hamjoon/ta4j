{
    "groups": [
        {
            "group": "NumericIndicator.previous(int) invalid arguments",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": -5
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Calling previous(-5) should reject negative shift values. The method is expected to validate the argument and throw IllegalArgumentException."
                },
                {
                    "input": [
                        {
                            "parameter": "barCount",
                            "value": -1
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Edge case: the smallest negative integer (-1) is passed. The method must still throw IllegalArgumentException rather than silently accept it."
                }
            ]
        },
        {
            "group": "NumericIndicator.previous() with null delegate",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "When a NumericIndicator is created via NumericIndicator.of(null) (or any constructor that accepts a null delegate), invoking previous() should attempt to access the delegate and consequently throw NullPointerException."
                }
            ]
        }
    ]
}