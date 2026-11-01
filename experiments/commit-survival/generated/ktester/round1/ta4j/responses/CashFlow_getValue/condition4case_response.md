{
    "group": "Valid index retrieval",
    "cases": [
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 0
                }
            ],
            "expected": "Num instance representing the first element of the internal list (e.g., DecimalNum.valueOf(100))",
            "description": "When the list contains at least one element and the index is 0, getValue should return the element at position 0."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 2
                }
            ],
            "expected": "Num instance representing the third element of the internal list (e.g., DecimalNum.valueOf(300))",
            "description": "When the index points to a middle element of a non‑empty list, getValue must return the correct Num object."
        },
        {
            "input": [
                {
                    "parameter": "index",
                    "value": 4
                }
            ],
            "expected": "Num instance representing the last element of the internal list (e.g., DecimalNum.valueOf(500))",
            "description": "When the index equals list.size()‑1, getValue should correctly return the last element."
        }
    ]
}