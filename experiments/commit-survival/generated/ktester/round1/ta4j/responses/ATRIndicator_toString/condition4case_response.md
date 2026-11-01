{
    "group": "ATRIndicator.toString() format verification",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 0
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: 0",
            "description": "Verify that toString() returns the class simple name followed by the barCount when the barCount is zero."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 1
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: 1",
            "description": "Verify that toString() correctly reflects a single‑period barCount."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 14
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: 14",
            "description": "Typical use‑case: ensure the method concatenates a positive barCount value."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 2147483647
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: 2147483647",
            "description": "Edge case: verify that the maximum int value is correctly represented in the string."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": -5
                },
                {
                    "parameter": "series",
                    "value": null
                }
            ],
            "expected": "ATRIndicator barCount: -5",
            "description": "Negative barCount (unlikely in production) – confirm that toString() does not alter the sign and simply concatenates the value."
        }
    ]
}