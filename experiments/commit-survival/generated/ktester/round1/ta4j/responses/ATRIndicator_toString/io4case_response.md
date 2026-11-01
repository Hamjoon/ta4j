{
    "group": "ATRIndicator.toString() additional format verification",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 2
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: 2",
            "description": "Verify that toString() correctly concatenates a small positive barCount that was not covered by existing tests."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": -2147483648
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: -2147483648",
            "description": "Edge case: ensure that the minimum int value is represented accurately in the string."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 0
                },
                {
                    "parameter": "series",
                    "value": "non-null BarSeries instance"
                }
            ],
            "expected": "ATRIndicator barCount: 0",
            "description": "Confirm that the presence of a non‑null BarSeries does not affect the toString() output (method only uses class name and barCount)."
        }
    ]
}