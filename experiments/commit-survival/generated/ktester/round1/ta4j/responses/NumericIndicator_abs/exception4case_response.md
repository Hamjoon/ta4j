{
    "group": "abs method exception scenarios",
    "cases": [
        {
            "input": [],
            "expected": "IllegalArgumentException (or NullPointerException) when creating a NumericIndicator with a null delegate before calling abs().",
            "description": "Attempt to construct a NumericIndicator using NumericIndicator.of(null) and then invoke abs(); the factory should reject a null delegate and throw an exception, preventing abs() from being executed."
        },
        {
            "input": [],
            "expected": "NullPointerException when the underlying delegate's getValue(index) returns null and abs() attempts to compute the absolute value.",
            "description": "Create a custom Indicator implementation whose getValue method returns null. Wrap it with NumericIndicator.of(...). Calling abs().getValue(0) should trigger a NPE because UnaryOperation.abs cannot handle a null Num."
        },
        {
            "input": [],
            "expected": "IndexOutOfBoundsException when the delegate throws this exception for an invalid index and abs() propagates it.",
            "description": "Use a delegate that throws IndexOutOfBoundsException for negative indices (e.g., a ConstantIndicator that checks index). Calling abs().getValue(-1) must propagate the same exception."
        },
        {
            "input": [],
            "expected": "RuntimeException (custom) when the delegate's getValue throws a user‑defined unchecked exception and abs() does not swallow it.",
            "description": "Implement a delegate whose getValue method throws a custom RuntimeException (e.g., new RuntimeException(\"delegate failure\")). Verify that abs().getValue(0) re‑throws the same exception."
        }
    ]
}