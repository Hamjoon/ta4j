{
    "testCases": [
        {
            "group": "operate(int) – edge cases and error handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": -1
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "Negative index is invalid; the method should detect the out‑of‑range value and throw an IndexOutOfBoundsException."
                },
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 1000
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "Index far beyond the size of the underlying BarSeries; the method must reject it with an IndexOutOfBoundsException."
                },
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": null
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Passing a null Integer (when autoboxed) is not allowed; the method should throw an IllegalArgumentException."
                }
            ]
        },
        {
            "group": "operate(int) – position not yet opened (new)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 2
                        }
                    ],
                    "expected": "null",
                    "description": "The Position is in a NEW state (no entry trade recorded). Any call to operate should return null because there is nothing to operate on yet."
                }
            ]
        },
        {
            "group": "operate(int) – position already closed",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 12
                        }
                    ],
                    "expected": "null",
                    "description": "The Position has already been closed (exit trade recorded). Invoking operate with an index after the exit should return null, indicating no further operations are possible."
                }
            ]
        },
        {
            "group": "operate(int) – index inside an opened position but not entry/exit",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 7
                        }
                    ],
                    "expected": "null",
                    "description": "The Position is opened (entry exists) but not yet closed, and the supplied index is neither the entry nor the exit index. The method should return null because no trade occurs at this intermediate index."
                }
            ]
        },
        {
            "group": "operate(int) – delegation to overloaded method with NaN values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 4
                        }
                    ],
                    "expected": "Trade object returned by operate(4, NaN, NaN)",
                    "description": "Ensures that operate(int) forwards the call to operate(int, Num, Num) using NaN for price and amount, and that the resulting Trade (if any) matches the one produced by the overloaded method."
                }
            ]
        }
    ]
}