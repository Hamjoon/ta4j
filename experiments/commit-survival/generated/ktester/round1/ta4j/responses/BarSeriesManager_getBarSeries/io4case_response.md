{
    "group": "BarSeriesManager.getBarSeries after manager operations",
    "cases": [
        {
            "input": [],
            "expected": "the same BarSeries instance that was passed to the constructor, even after invoking run(...)",
            "description": "Verify that calling run(...) (with a simple no‑op strategy) does not replace or alter the internal BarSeries reference; getBarSeries() should still return the original object."
        },
        {
            "input": [],
            "expected": "the same mock BarSeries instance that was injected via the constructor",
            "description": "Inject a Mockito mock of BarSeries, invoke getBarSeries(), and confirm that the returned reference is exactly the mock instance, proving that the manager does not wrap or proxy the object."
        },
        {
            "input": [],
            "expected": "the same custom subclass instance of BarSeries that was supplied to the constructor",
            "description": "Create a concrete subclass of BarSeries (e.g., MyBarSeries extends BarSeries), construct the manager with it, and assert that getBarSeries() returns that subclass instance, ensuring type preservation."
        }
    ]
}