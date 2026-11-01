{
    "group": "operate(int) – valid scenarios",
    "cases": [
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 5
                }
            ],
            "expected": "Trade object representing the operation at index 5 (e.g., a BUY if the position is opened at that index)",
            "description": "Index is within the series range and the position is still open; the method should delegate to operate(index, NaN, NaN) and return a valid Trade."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 0
                }
            ],
            "expected": "Trade object representing the entry trade (the first trade of the position)",
            "description": "Index corresponds exactly to the entry index of the position; the method should return the entry Trade."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 10
                }
            ],
            "expected": "Trade object representing the exit trade (the closing trade of the position)",
            "description": "Index corresponds exactly to the exit index of the position; the method should return the exit Trade."
        }
    ]
}