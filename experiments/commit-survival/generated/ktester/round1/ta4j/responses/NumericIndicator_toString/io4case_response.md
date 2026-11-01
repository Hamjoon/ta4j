{
    "tests": [
        {
            "group": "toString_nestedNumericIndicator",
            "cases": [
                {
                    "input": [],
                    "expected": "same as inner delegate's toString() (ConstantIndicator)",
                    "description": "Create a NumericIndicator whose delegate is another NumericIndicator that wraps a ConstantIndicator. The outer NumericIndicator.toString() should delegate through both layers and return the ConstantIndicator's string representation."
                }
            ]
        },
        {
            "group": "toString_mutableDelegate",
            "cases": [
                {
                    "input": [],
                    "expected": "updated string after delegate state change",
                    "description": "Use a custom mutable Indicator whose toString() returns the current value of an internal field. After constructing the NumericIndicator, modify the field. NumericIndicator.toString() must reflect the new string, proving it does not cache the delegate's string."
                }
            ]
        },
        {
            "group": "toString_delegateReturnsNull",
            "cases": [
                {
                    "input": [],
                    "expected": null,
                    "description": "Implement an Indicator whose toString() explicitly returns null. NumericIndicator.toString() simply returns delegate.toString(), so the result should be null."
                }
            ]
        },
        {
            "group": "toString_delegateIsLambda",
            "cases": [
                {
                    "input": [],
                    "expected": "org.ta4j.core.indicators.numeric.NumericIndicator$$Lambda$* (or similar default Object toString)",
                    "description": "Create a NumericIndicator from a lambda expression implementing Indicator<Num>. The lambda inherits Object's default toString(), which yields a class name with a hash code. NumericIndicator.toString() must return exactly that default string."
                }
            ]
        },
        {
            "group": "toString_delegateThrowsRuntimeException",
            "cases": [
                {
                    "input": [],
                    "expected": "RuntimeException",
                    "description": "Provide a delegate whose toString() throws a RuntimeException. Calling NumericIndicator.toString() should propagate the exception unchanged."
                }
            ]
        }
    ]
}