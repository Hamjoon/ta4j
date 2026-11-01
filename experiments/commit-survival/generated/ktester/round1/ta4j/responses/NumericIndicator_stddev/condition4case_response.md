{
    "group": "stddev method tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "NumericIndicator whose delegate is a StandardDeviationIndicator with barCount=3; getValue(2) equals standard deviation of the first three values of the underlying series",
            "description": "Normal case: create a NumericIndicator from a simple BarSeries (e.g., close prices 1, 2, 3, 4). Calling stddev(3) should wrap a StandardDeviationIndicator that computes the standard deviation over the last 3 bars. Verify that the returned object is a NumericIndicator and that its delegate is an instance of StandardDeviationIndicator. Also verify the computed value matches the manually‑calculated standard deviation."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 0
                }
            ],
            "expected": "IllegalArgumentException thrown with message containing \"barCount must be > 0\"",
            "description": "Edge case: passing a barCount of 0 should cause the StandardDeviationIndicator constructor to reject the argument and the stddev method to propagate the IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": -5
                }
            ],
            "expected": "IllegalArgumentException thrown with message containing \"barCount must be > 0\"",
            "description": "Edge case: passing a negative barCount should also trigger an IllegalArgumentException from the underlying StandardDeviationIndicator."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 10
                }
            ],
            "expected": "NumericIndicator whose delegate returns NaN (Num.NaN) for any index because the underlying series contains fewer than 10 bars",
            "description": "Boundary case: request a standard deviation over more bars than exist in the series. The StandardDeviationIndicator returns Num.NaN for indices where the window is incomplete. Verify that the wrapped NumericIndicator propagates this NaN value."
        }
    ]
}