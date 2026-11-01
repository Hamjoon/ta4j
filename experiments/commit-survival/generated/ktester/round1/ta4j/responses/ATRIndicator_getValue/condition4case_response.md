{
    "group": "ATRIndicator.getValue(int) delegation tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 5
                }
            ],
            "expected": "Num value returned by underlying MMAIndicator (e.g., DecimalNum.valueOf(12.34))",
            "description": "When a valid index is supplied, getValue should delegate to averageTrueRangeIndicator and return the same Num value."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": -1
                }
            ],
            "expected": "IndexOutOfBoundsException",
            "description": "If a negative index is provided, the underlying MMAIndicator is expected to throw IndexOutOfBoundsException, which should propagate from getValue."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 1000
                }
            ],
            "expected": "IndexOutOfBoundsException",
            "description": "When the index exceeds the bar count of the series, the underlying MMAIndicator should throw IndexOutOfBoundsException, and getValue must propagate it."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 3
                }
            ],
            "expected": "verify that averageTrueRangeIndicator.getValue(3) is called exactly once",
            "description": "Ensures that getValue forwards the call to the internal MMAIndicator with the exact same index and does not invoke it multiple times."
        }
    ]
}