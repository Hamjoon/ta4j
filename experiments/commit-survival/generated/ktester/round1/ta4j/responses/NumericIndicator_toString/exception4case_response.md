{
    "test_cases": [
        {
            "group": "toString_nullDelegate",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "NumericIndicator is constructed with a null delegate (e.g., via a factory method that allows null). Calling toString() should throw NullPointerException because it attempts to invoke toString() on a null reference."
                }
            ]
        },
        {
            "group": "toString_delegateThrowsSpecificRuntimeException",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalArgumentException",
                    "description": "Provide a delegate whose toString() method throws an IllegalArgumentException. NumericIndicator.toString() must propagate this exact exception unchanged."
                },
                {
                    "input": [],
                    "expected": "CustomUncheckedException",
                    "description": "Create a delegate that throws a user‑defined unchecked exception (extends RuntimeException) from its toString(). The NumericIndicator should forward that exception without alteration."
                }
            ]
        },
        {
            "group": "toString_delegateThrowsError",
            "cases": [
                {
                    "input": [],
                    "expected": "AssertionError",
                    "description": "Implement a delegate whose toString() throws an AssertionError (a subclass of java.lang.Error). NumericIndicator.toString() must not catch it and should let the Error propagate."
                },
                {
                    "input": [],
                    "expected": "OutOfMemoryError",
                    "description": "Simulate a delegate that throws an OutOfMemoryError from toString(). The NumericIndicator should propagate the Error unchanged."
                }
            ]
        }
    ]
}