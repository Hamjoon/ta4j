{
    "group": "NumericIndicator.getValue(int) - delegation behavior",
    "cases": [
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 0
                }
            ],
            "expected": "Num value equal to delegate.getValue(0) (e.g., DecimalNum.valueOf(100))",
            "description": "Delegate returns a concrete Num for index 0; getValue should return exactly the same Num instance/value."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 5
                }
            ],
            "expected": "Num value equal to delegate.getValue(5) (e.g., DecimalNum.valueOf(42.5))",
            "description": "Delegate provides a different Num for a non‑zero index; verify that getValue forwards the call and returns the correct value."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": -1
                }
            ],
            "expected": "IndexOutOfBoundsException (or whatever exception the delegate throws for negative indices)",
            "description": "Delegate is configured to throw an exception for an invalid (negative) index; getValue must propagate the exception unchanged."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 10
                }
            ],
            "expected": "IndexOutOfBoundsException (or whatever exception the delegate throws for out‑of‑range indices)",
            "description": "Delegate throws an exception when the index exceeds the underlying series length; getValue should not swallow the exception."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 3
                }
            ],
            "expected": "null",
            "description": "Delegate returns null for a given index (simulating a missing value); getValue should return null as well, preserving the delegate's contract."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 2
                }
            ],
            "expected": "Num value equal to delegate.getValue(2) (e.g., DecimalNum.valueOf(0))",
            "description": "Edge case where the delegate returns a Num representing zero; ensures that zero values are correctly propagated."
        }
    ]
}