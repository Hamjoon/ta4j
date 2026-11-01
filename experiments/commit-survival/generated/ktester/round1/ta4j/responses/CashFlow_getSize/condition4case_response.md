{
    "group": "CashFlow.getSize()",
    "cases": [
        {
            "input": [],
            "expected": 0,
            "description": "When the underlying BarSeries contains no bars, getSize() should return 0."
        },
        {
            "input": [],
            "expected": 1,
            "description": "When the BarSeries contains exactly one bar, getSize() should return 1."
        },
        {
            "input": [],
            "expected": 42,
            "description": "When the BarSeries contains a typical positive number of bars (e.g., 42), getSize() should return that number."
        },
        {
            "input": [],
            "expected": 2147483647,
            "description": "When the BarSeries reports the maximum int value as its bar count, getSize() should correctly return Integer.MAX_VALUE without overflow."
        }
    ]
}