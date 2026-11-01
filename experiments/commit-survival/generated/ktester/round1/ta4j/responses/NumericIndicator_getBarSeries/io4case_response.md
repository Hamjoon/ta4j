{
    "group": "NumericIndicator.getBarSeries_additional",
    "cases": [
        {
            "input": [],
            "expected": "the same BarSeries instance on successive calls (reference equality)",
            "description": "Idempotence: after constructing NumericIndicator with a delegate that returns a concrete BarSeries, invoke getBarSeries() multiple times and verify each call returns the exact same instance (==)."
        },
        {
            "input": [],
            "expected": "different BarSeries instances if the delegate returns a new instance each call",
            "description": "Dynamic delegate: mock the delegate so that getBarSeries() returns a new BarSeries object on each invocation. Verify that NumericIndicator.getBarSeries() forwards the call and returns the new instance each time (no caching)."
        },
        {
            "input": [],
            "expected": "RuntimeException propagated from delegate.getBarSeries()",
            "description": "Exception propagation: configure the delegate to throw a RuntimeException when getBarSeries() is invoked. Ensure NumericIndicator.getBarSeries() does not catch the exception and the same exception bubbles up to the caller."
        },
        {
            "input": [],
            "expected": "the returned BarSeries reflects mutations performed on the original series",
            "description": "Live reference: obtain the BarSeries from NumericIndicator, then add a new Bar to the underlying series (through the original BarSeries reference). After the mutation, call getBarSeries() again and verify the returned series contains the added Bar, confirming that the reference is not a copy."
        }
    ]
}