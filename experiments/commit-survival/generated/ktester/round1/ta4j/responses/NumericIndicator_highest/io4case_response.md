{
    "group": "NumericIndicator.highest(int) additional tests",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 1
                }
            ],
            "expected": "The returned NumericIndicator is not null, its delegate is an instance of HighestValueIndicator with barCount = 1, and for any index i the value returned by getValue(i) is exactly the same as the original NumericIndicator's getValue(i).",
            "description": "When barCount = 1 the highest‑value window contains only the current bar, therefore the highest indicator should behave identically to the source indicator."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 5
                }
            ],
            "expected": "For a series of values [1, 3, 2, 5, 4] the returned NumericIndicator yields: getValue(0)=1, getValue(1)=3, getValue(2)=3, getValue(3)=5, getValue(4)=5. The delegate is a HighestValueIndicator with barCount = 5.",
            "description": "Validate the correct highest calculation over a normal window size that equals the series length. The highest at each index must be the maximum of the last up‑to‑5 values."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 10
                }
            ],
            "expected": "Using the same series [1, 3, 2, 5, 4] the returned NumericIndicator yields the same values as the previous case (1, 3, 3, 5, 5) because the window exceeds the series length; the indicator should treat missing earlier bars as unavailable and compute the maximum of all existing bars up to the current index.",
            "description": "Check behaviour when barCount is larger than the number of bars in the series – the highest indicator must gracefully fallback to the maximum of the available bars."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "The original NumericIndicator remains unchanged: after calling highest(3) the original indicator still returns its original values (e.g., for the series [1,3,2,5,4] getValue(3) = 5). The newly created NumericIndicator returns the highest values (as in case 2). No side‑effects on the source indicator.",
            "description": "Ensure that highest(int) is non‑mutating; the source NumericIndicator must not be altered by the method call."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 4
                }
            ],
            "expected": "The delegate of the returned NumericIndicator is an instance of HighestValueIndicator and its internal barCount field equals 4 (verified via instanceof and reflection or by checking behaviour).",
            "description": "Confirm that the method wraps the source indicator with a HighestValueIndicator configured with the exact barCount supplied."
        }
    ]
}