{
    "testCases": [
        {
            "group": "Negative index",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": -1
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "When the provided index is negative, List#get throws IndexOutOfBoundsException; getValue should propagate this exception."
                }
            ]
        },
        {
            "group": "Index equal to size (upper bound)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 5
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "For a list of size 5 (indices 0‑4), an index equal to the size is out of range and should cause IndexOutOfBoundsException."
                }
            ]
        },
        {
            "group": "Index greater than size",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 10
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "When the index is larger than the last valid index, getValue must throw IndexOutOfBoundsException."
                }
            ]
        },
        {
            "group": "Empty list handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 0
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "If the internal values list is empty, any index (including 0) is invalid and should result in IndexOutOfBoundsException."
                }
            ]
        },
        {
            "group": "Single element list",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 0
                        }
                    ],
                    "expected": "Num instance representing the sole element (e.g., DecimalNum.valueOf(42))",
                    "description": "When the list contains exactly one Num element, getValue(0) must return that same Num instance."
                }
            ]
        },
        {
            "group": "Null element retrieval",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 1
                        }
                    ],
                    "expected": "null",
                    "description": "If the internal list stores a null at the requested position, getValue should return null without throwing an exception."
                }
            ]
        }
    ]
}