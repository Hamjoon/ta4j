{
    "group": "Extreme negative barCount values (exception handling)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": -2147483648
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "The EMAIndicator should reject the minimum int value as a barCount; calling ema(Integer.MIN_VALUE) must throw IllegalArgumentException."
        }
    ]
}