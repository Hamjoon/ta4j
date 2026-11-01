{
    "test_cases": [
        {
            "group": "Null internal list",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 0
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "If the internal `values` list reference is null, calling `getValue` should throw a NullPointerException when attempting to invoke `values.get(index)`."
                }
            ]
        },
        {
            "group": "Extreme index values",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 2147483647
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "When `index` is Integer.MAX_VALUE, it is far beyond the list size and should result in an IndexOutOfBoundsException."
                },
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": -2147483648
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "When `index` is Integer.MIN_VALUE (a large negative number), the underlying list access should throw IndexOutOfBoundsException."
                }
            ]
        }
    ]
}