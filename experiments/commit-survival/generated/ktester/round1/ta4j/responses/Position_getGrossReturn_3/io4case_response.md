{
    "group": "Position.getGrossReturn(Num) – additional edge cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(120)"
                }
            ],
            "expected": "Num.valueOf(0.2)",
            "description": "Entry price is 100, final price 120 → gross return = (120‑100)/100 = 0.2. Tests normal profit calculation."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(0)"
                }
            ],
            "expected": "Num.valueOf(-1)",
            "description": "Entry price 100, final price 0 → gross return = (0‑100)/100 = -1. Tests a total loss scenario."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(0)"
                }
            ],
            "expected": "NaN",
            "description": "Entry price is 0, final price 0 → division by zero, library should propagate NaN."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(50)"
                }
            ],
            "expected": "NaN",
            "description": "Entry price is 0, final price 50 → division by zero, expected NaN."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(-30)"
                }
            ],
            "expected": "Num.valueOf(-1.3)",
            "description": "Entry price 100, final price -30 → gross return = (-30‑100)/100 = -1.3. Tests negative final price handling."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(110)"
                }
            ],
            "expected": "Num.valueOf(-2.1)",
            "description": "Entry price -100, final price 110 → gross return = (110‑(-100))/(-100) = -2.1. Tests negative entry price."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(100)"
                }
            ],
            "expected": "Num.valueOf(0)",
            "description": "Entry price 100, final price 100 → no change, gross return should be 0."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "NaN"
                }
            ],
            "expected": "NaN",
            "description": "Entry price 100, final price NaN → any NaN operand propagates NaN."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(120)"
                }
            ],
            "expected": "NullPointerException",
            "description": "Entry trade exists but its price per asset is null; calling getPricePerAsset() returns null, causing NPE when delegated."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(130)"
                }
            ],
            "expected": "NaN",
            "description": "Both entry price and final price are NaN → result must be NaN."
        }
    ]
}