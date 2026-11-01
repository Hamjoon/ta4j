{
    "group": "CashFlow.getSize() additional scenarios",
    "cases": [
        {
            "input": [],
            "expected": 2,
            "description": "When the BarSeries contains exactly two bars, getSize() should return 2."
        },
        {
            "input": [],
            "expected": 999999,
            "description": "When the BarSeries contains a large but non‑maximum positive number of bars (e.g., 999,999), getSize() should return that exact count."
        },
        {
            "input": [],
            "expected": 2147483646,
            "description": "When the BarSeries reports Integer.MAX_VALUE‑1 as its bar count, getSize() should correctly return 2147483646 without overflow."
        },
        {
            "input": [],
            "expected": -1,
            "description": "If a custom/mock BarSeries incorrectly returns a negative bar count (e.g., -1), getSize() will propagate that value; this test verifies the method does not perform validation on the count."
        },
        {
            "input": [],
            "expected": 6,
            "description": "After creating a BarSeries with 5 bars, adding one more bar should cause getSize() to reflect the updated count (6)."
        },
        {
            "input": [],
            "expected": "RuntimeException",
            "description": "When the underlying BarSeries throws a RuntimeException from getBarCount(), getSize() should propagate the exception."
        }
    ]
}