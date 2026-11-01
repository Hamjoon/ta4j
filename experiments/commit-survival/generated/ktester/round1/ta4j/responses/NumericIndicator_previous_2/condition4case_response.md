{
    "group": "NumericIndicator.previous()",
    "cases": [
        {
            "input": [],
            "expected": "Returned Indicator yields getValue(i) == original delegate.getValue(i-1) for all i >= 1",
            "description": "Verify that previous() delegates to previous(1) by checking that the returned Indicator shifts the underlying values by one bar. A simple stub Indicator returning the index as a Num is used as the delegate; calling previous() and then getValue(5) must equal stub.getValue(4)."
        },
        {
            "input": [],
            "expected": "Returned Indicator is equal (by .equals) to the result of calling previous(1) directly",
            "description": "Confirm that previous() is just a convenience wrapper for previous(1). The test creates a NumericIndicator from a stub delegate, obtains indicatorA = numeric.previous() and indicatorB = numeric.previous(1) and asserts that indicatorA.equals(indicatorB) (or that they are the same instance if the implementation caches)."
        },
        {
            "input": [],
            "expected": "No exception is thrown and the original delegate remains unchanged",
            "description": "Ensure that invoking previous() does not mutate the original delegate Indicator. After calling numeric.previous(), the test calls delegate.getValue(3) and expects the same value as before the call."
        },
        {
            "input": [],
            "expected": "Two successive calls to previous() produce distinct Indicator objects",
            "description": "Check that each call to previous() creates a new Indicator instance (i.e., numeric.previous() != numeric.previous()). This guarantees that the method does not return a cached singleton unless explicitly designed."
        },
        {
            "input": [],
            "expected": "Calling getValue(0) on the Indicator returned by previous() yields Num.NaN (or the library‑defined default for out‑of‑range access)",
            "description": "Validate the edge case where the shifted index would be negative. For index 0 the underlying previous(1) cannot fetch a value, so the expected behavior is the library’s standard out‑of‑range value (usually Num.NaN). The test asserts that the returned Num is NaN."
        }
    ]
}