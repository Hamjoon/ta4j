{
    "test_cases": [
        {
            "group": "Opened Position - Null entry trade",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(5)"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Position is opened (isOpened() == true) but the entry trade reference is null. Accessing entry.getAmount() should throw a NullPointerException."
                }
            ]
        },
        {
            "group": "Closed Position - Null exit trade",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Position is closed (isOpened() == false) but the exit trade reference is null. Accessing exit.getValue() should throw a NullPointerException."
                }
            ]
        },
        {
            "group": "Opened Position - Null finalPrice argument",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "null"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The finalPrice argument is null. entry.getAmount().multipliedBy(null) triggers a NullPointerException."
                }
            ]
        },
        {
            "group": "Opened Position - Null entry amount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(5)"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The entry trade exists but its amount field is null. entry.getAmount().multipliedBy(finalPrice) causes a NullPointerException."
                }
            ]
        },
        {
            "group": "Opened Position - Null entry value",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(5)"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The entry trade exists but its value field is null. The subtraction entry.getAmount().multipliedBy(finalPrice).minus(entry.getValue()) triggers a NullPointerException."
                }
            ]
        },
        {
            "group": "Closed Position - Null exit value",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The position is closed, exit trade exists but its value field is null. exit.getValue().minus(entry.getValue()) throws a NullPointerException."
                }
            ]
        },
        {
            "group": "Closed Position - Null entry value",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The position is closed, entry trade exists but its value field is null. exit.getValue().minus(entry.getValue()) triggers a NullPointerException."
                }
            ]
        }
    ]
}