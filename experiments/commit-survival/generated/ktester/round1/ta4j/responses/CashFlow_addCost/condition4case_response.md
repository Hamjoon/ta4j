{
    "group": "addCost",
    "cases": [
        {
            "input": [
                { "parameter": "rawPrice", "value": "DecimalNum.valueOf(\"10\")" },
                { "parameter": "holdingCost", "value": "DecimalNum.valueOf(\"2\")" },
                { "parameter": "isLongTrade", "value": "true" }
            ],
            "expected": "DecimalNum.valueOf(\"8\")",
            "description": "Long trade: net price should be rawPrice minus holdingCost (10 - 2 = 8)."
        },
        {
            "input": [
                { "parameter": "rawPrice", "value": "DecimalNum.valueOf(\"10\")" },
                { "parameter": "holdingCost", "value": "DecimalNum.valueOf(\"2\")" },
                { "parameter": "isLongTrade", "value": "false" }
            ],
            "expected": "DecimalNum.valueOf(\"12\")",
            "description": "Short trade: net price should be rawPrice plus holdingCost (10 + 2 = 12)."
        },
        {
            "input": [
                { "parameter": "rawPrice", "value": "DecimalNum.valueOf(\"5\")" },
                { "parameter": "holdingCost", "value": "DecimalNum.valueOf(\"0\")" },
                { "parameter": "isLongTrade", "value": "true" }
            ],
            "expected": "DecimalNum.valueOf(\"5\")",
            "description": "Long trade with zero holding cost: net price remains unchanged (5 - 0 = 5)."
        },
        {
            "input": [
                { "parameter": "rawPrice", "value": "DecimalNum.valueOf(\"-3\")" },
                { "parameter": "holdingCost", "value": "DecimalNum.valueOf(\"4\")" },
                { "parameter": "isLongTrade", "value": "false" }
            ],
            "expected": "DecimalNum.valueOf(\"1\")",
            "description": "Short trade with negative raw price: net price = -3 + 4 = 1."
        },
        {
            "input": [
                { "parameter": "rawPrice", "value": "DecimalNum.valueOf(\"1000000000\")" },
                { "parameter": "holdingCost", "value": "DecimalNum.valueOf(\"999999999\")" },
                { "parameter": "isLongTrade", "value": "true" }
            ],
            "expected": "DecimalNum.valueOf(\"1\")",
            "description": "Long trade with very large numbers to verify arithmetic precision (1,000,000,000 - 999,999,999 = 1)."
        },
        {
            "input": [
                { "parameter": "rawPrice", "value": "null" },
                { "parameter": "holdingCost", "value": "DecimalNum.valueOf(\"1\")" },
                { "parameter": "isLongTrade", "value": "true" }
            ],
            "expected": "NullPointerException",
            "description": "Passing null for rawPrice should result in a NullPointerException."
        },
        {
            "input": [
                { "parameter": "rawPrice", "value": "DecimalNum.valueOf(\"1\")" },
                { "parameter": "holdingCost", "value": "null" },
                { "parameter": "isLongTrade", "value": "false" }
            ],
            "expected": "NullPointerException",
            "description": "Passing null for holdingCost should result in a NullPointerException."
        }
    ]
}