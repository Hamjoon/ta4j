Invalid control character at: line 28 column 45 (char 784)
{
    "group": "equalsMethod",
    "cases": [
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "null"
                }
            ],
            "expected": "false",
            "description": "Calling equals with null should return false."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "\"someString\""
                }
            ],
            "expected": "false",
            "description": "Calling equals with an object of a different class (String) should return false."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "new Position() // both entry and exit are null"
                }
            ],
            "expected": "true",
            "description": "Two Position instances with both entry and exit fields null are considered equal."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "positionWith(entry=tradeA, exit=null)"
                }
            ],
            "expected": "true",
            "description": "Both positions have the same non‑null entry trade and both have a null exit trade; equals should return true."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "positionWith(entry=null, exit=tradeB)"
                }
            ],
            "expected": "true",
            "description": "Both positions have the same non‑null exit trade and both have a null entry trade; equals should return true."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "positionWith(entry=tradeA, exit=tradeB)"
                }
            ],
            "expected": "true",
            "description": "Both positions have identical entry and exit trades (tradeA and tradeB are equal); equals should return true."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "positionWith(entry=tradeA, exit=tradeB)"
                }
            ],
            "expected": "false",
            "description": "Positions have the same entry trade but different exit trades (exit of this position is null, other has tradeB); equals should return false."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "positionWith(entry=tradeC, exit=tradeB)"
                }
            ],
            "expected": "false",
            "description": "Positions have different entry trades (tradeA vs tradeC) while exit trades are equal; equals should return false."
        },
        {
            "input": [
                {
                    "parameter": "obj",
                    "value": "positionWith(entry=tradeC, exit=tradeD)"
                }
            ],
            "expected": "false",
            "description": "Both entry and exit trades differ between the two positions; equals should return false."
        }
    ]
}