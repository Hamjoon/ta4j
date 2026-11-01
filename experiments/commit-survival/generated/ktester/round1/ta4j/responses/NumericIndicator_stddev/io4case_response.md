{
    "group": "stddev additional tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 1
                }
            ],
            "expected": "NumericIndicator whose delegate returns Num.ZERO for any index because the standard deviation of a single value is 0",
            "description": "Edge case: barCount = 1. For any index the StandardDeviationIndicator should compute a variance of 0, therefore the wrapped NumericIndicator must return Num.ZERO. Verify with a series of arbitrary values (e.g., 10, 20, 30)."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 4
                }
            ],
            "expected": "NumericIndicator whose delegate returns the correct standard deviation of the whole series (indices 0‑3) and Num.NaN for any index < 3 (none in this case)",
            "description": "Boundary case: barCount equals the exact number of bars in the series. Use a series with known close prices (e.g., 2, 4, 6, 8). The expected standard deviation is sqrt(((2‑5)²+(4‑5)²+(6‑5)²+(8‑5)²)/4) = 2.236... Verify that getValue(3) matches this value."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 2
                }
            ],
            "expected": "NumericIndicator returns Num.NaN for index 0 and the correct standard deviation for index 1 (and later indices)",
            "description": "Check unstable‑bars handling: with barCount = 2 the StandardDeviationIndicator is unstable for the first (barCount‑1) = 1 bar. For a series 1, 5, 9 verify that getValue(0) is NaN and getValue(1) equals sqrt(((1‑3)²+(5‑3)²)/2) = 2.828..."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 5
                }
            ],
            "expected": "NumericIndicator returns the correct standard deviation for index 4 (the first complete window) and NaN for indices 0‑3",
            "description": "Series length exactly matches barCount. Use a series of five values (e.g., 3, 3, 3, 3, 7). The first four windows are incomplete → NaN. At index 4 the window contains [3,3,3,3,7]; expected stddev = sqrt(((3‑3.8)²*4+(7‑3.8)²)/5) ≈ 1.720."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "NumericIndicator returns Num.ZERO for index 2 because the first three values are identical, and correct non‑zero stddev for later windows",
            "description": "Zero‑variance window: series 5,5,5,6,7. For barCount 3, the window ending at index 2 is [5,5,5] → stddev 0. Verify getValue(2) == Num.ZERO, while getValue(3) and getValue(4) produce the expected non‑zero values."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "NumericIndicator.getUnstableBars() returns 2 (barCount‑1)",
            "description": "Confirm that the wrapper propagates the unstable‑bars information from the underlying StandardDeviationIndicator. After creating stddev(3) on any series, calling getUnstableBars() on the resulting NumericIndicator should equal 2."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "NumericIndicator.delegate() is an instance of StandardDeviationIndicator",
            "description": "Verify the internal delegate type: the stddev method must wrap the StandardDeviationIndicator. After calling stddev(3), the returned NumericIndicator’s delegate() should be instanceof StandardDeviationIndicator."
        }
    ]
}